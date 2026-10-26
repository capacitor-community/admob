const fs = require('fs'),
  { execFileSync, spawn } = require('child_process');
const output = process.env.PROOF_DIR || '/tmp/admob-native-proof';
fs.mkdirSync(output, { recursive: true });
const mode = process.argv[2],
  apk = process.argv[3],
  wait = (ms) => new Promise((r) => setTimeout(r, ms));
const adb = (...a) =>
  execFileSync('adb', ['-s', process.env.ANDROID_SERIAL || 'emulator-5580', ...a], {
    encoding: 'utf8',
    timeout: 30000,
  });
(async () => {
  adb('install', '-r', apk);
  adb('shell', 'am', 'force-stop', 'io.ionic.starter');
  adb('shell', 'am', 'start', '-W', '-n', 'io.ionic.starter/.MainActivity');
  await wait(1500);
  const pid = adb('shell', 'pidof', 'io.ionic.starter').trim();
  adb('forward', 'tcp:9237', `localabstract:webview_devtools_remote_${pid}`);
  let tabs;
  for (let i = 0; i < 30; i++) {
    try {
      tabs = await (await fetch('http://localhost:9237/json')).json();
      if (tabs.length) break;
    } catch {}
    await wait(500);
  }
  const ws = new WebSocket(tabs.find((t) => t.url.startsWith('https://localhost')).webSocketDebuggerUrl);
  await new Promise((r) => (ws.onopen = r));
  let seq = 0;
  const q = new Map();
  ws.onmessage = (e) => {
    const m = JSON.parse(e.data);
    if (m.id) {
      q.get(m.id)?.(m);
      q.delete(m.id);
    }
  };
  const call = (method, params = {}) =>
    new Promise((resolve, reject) => {
      const id = ++seq;
      const timer = setTimeout(() => {
        q.delete(id);
        reject(Error('CDP timeout ' + method));
      }, 12000);
      q.set(id, (m) => {
        clearTimeout(timer);
        resolve(m);
      });
      ws.send(JSON.stringify({ id, method, params }));
    });
  const ev = async (expression) => {
    const m = await call('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true });
    if (m.result?.exceptionDetails) throw Error(JSON.stringify(m.result.exceptionDetails));
    return m.result?.result?.value;
  };
  await call('Page.enable');
  await call('Performance.enable', { timeDomain: 'threadTicks' });
  await call('Page.navigate', { url: 'https://localhost/native?snapshot' });
  for (let i = 0; i < 90; i++) {
    if (await ev('document.querySelectorAll("[data-native-ui-snapshot], [data-admob-snapshot]").length>0')) break;
    if (i === 89) throw Error('no snapshot');
    await wait(500);
  }
  await ev(
    `(async()=>{window.f=window.__capacitorAdMobNativeFeedLookup('native-ad-demo-feed');window.sc=await document.querySelector('ion-content').getScrollElement();window.slot=document.querySelector('capacitor-admob-native');const readout=document.querySelector('app-native-ad-demo ion-list');readout.style.height='250px';readout.style.overflow='hidden';sc.scrollTop+=slot.getBoundingClientRect().top-180;const bar=document.createElement('div');bar.id='proof-anchor';bar.style.cssText='position:absolute;left:4px;top:0;width:4px;height:320px;background:rgb(255,0,255);pointer-events:none;z-index:100';const feed=slot.parentElement.parentElement;feed.style.position='relative';bar.style.top=slot.parentElement.offsetTop+'px';feed.append(bar);window.proof={frames:[],decodes:[]};const decode=HTMLImageElement.prototype.decode;HTMLImageElement.prototype.decode=function(){proof.decodes.push({t:performance.now(),src:this.src});return decode.call(this)};let running=true;window.stopProof=()=>running=false;const sample=()=>{if(!running)return;proof.frames.push({t:performance.now(),y:slot.getBoundingClientRect().top,visible:slot.hasAttribute('data-native-ui-snapshot-visible'),imgs:[...document.querySelectorAll('[data-admob-snapshot]')].filter(x=>!x.hidden).length});requestAnimationFrame(sample)};sample()})()`,
  );
  await wait(1500);
  const cpu = () => {
    const x = adb('shell', 'cat', `/proc/${pid}/stat`).split(') ')[1].split(' ');
    return Number(x[11]) + Number(x[12]);
  };
  const files = () => {
    try {
      return adb('shell', 'run-as', 'io.ionic.starter', 'find', 'cache/native-ui-engine', '-type', 'f')
        .trim()
        .split('\n');
    } catch {
      return [];
    }
  };
  const perf = async () =>
    Object.fromEntries((await call('Performance.getMetrics')).result.metrics.map((m) => [m.name, m.value]));
  const before = {
    perf: await perf(),
    cpu: cpu(),
    wall: Date.now(),
    mem: adb('shell', 'dumpsys', 'meminfo', pid),
    files: files(),
  };
  const record = spawn('adb', [
    '-s',
    process.env.ANDROID_SERIAL || 'emulator-5580',
    'shell',
    'screenrecord',
    '--time-limit',
    '24',
    `/sdcard/${mode}-proof.mp4`,
  ]);
  const done = new Promise((r) => record.on('exit', r));
  await wait(700);
  for (const duration of [350, 800, 1500])
    for (const [a, b] of [
      [1800, 1640],
      [1640, 1800],
    ]) {
      await new Promise((r) =>
        spawn('adb', [
          '-s',
          process.env.ANDROID_SERIAL || 'emulator-5580',
          'shell',
          'input',
          'swipe',
          '10',
          String(a),
          '10',
          String(b),
          String(duration),
        ]).on('exit', r),
      );
      await wait(700);
    }
  for (const [a, b] of [
    [1800, 1150],
    [1150, 1800],
  ]) {
    await new Promise((r) =>
      spawn('adb', [
        '-s',
        process.env.ANDROID_SERIAL || 'emulator-5580',
        'shell',
        'input',
        'swipe',
        '10',
        String(a),
        '10',
        String(b),
        '900',
      ]).on('exit', r),
    );
    await wait(700);
  }
  await done;
  const after = {
    perf: await perf(),
    cpu: cpu(),
    wall: Date.now(),
    mem: adb('shell', 'dumpsys', 'meminfo', pid),
    files: files(),
  };
  adb('pull', `/sdcard/${mode}-proof.mp4`, `${output}/${mode}.mp4`);
  const trace = await ev('stopProof();proof');
  await ev('sc.scrollTop+=slot.getBoundingClientRect().top-180');
  await wait(700);
  await ev(
    "window.pauseMotion=setInterval(()=>document.dispatchEvent(new Event('scroll')),30);document.dispatchEvent(new Event('scroll'))",
  );
  await wait(80);
  const pauseWasImage = await ev(
    "document.querySelectorAll('[data-native-ui-snapshot-visible]').length>0 || [...document.querySelectorAll('[data-admob-snapshot]')].some(x=>!x.hidden)",
  );
  await ev('f.pause();');
  await ev('clearInterval(pauseMotion)');
  const pause = await ev(
    `({visible:document.querySelectorAll('[data-native-ui-snapshot-visible]').length,oldVisible:[...document.querySelectorAll('[data-admob-snapshot]')].filter(x=>!x.hidden).length})`,
  );
  adb('shell', 'screencap', '-p', `/sdcard/${mode}-paused.png`);
  adb('pull', `/sdcard/${mode}-paused.png`, `${output}/${mode}-paused.png`);
  await ev('f.resume()');
  await wait(1200);
  const reloadStart = trace.decodes.length;
  await ev('f.reload("sponsored-after-capacitor")');
  await wait(2000);
  const reload = await ev('proof.decodes');
  fs.writeFileSync(
    `${output}/${mode}.json`,
    JSON.stringify({ before, after, pauseWasImage, pause, trace, reload, reloadStart }, null, 2),
  );
  ws.close();
  console.log(mode + ' COMPLETE');
})().catch((e) => {
  console.error(e);
  process.exitCode = 1;
});
