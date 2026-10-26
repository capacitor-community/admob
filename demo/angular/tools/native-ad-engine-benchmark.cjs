const fs = require('fs'),
  crypto = require('crypto'),
  { execFileSync, spawn } = require('child_process');
const path = require('path');
const name = process.argv[2],
  apk = process.argv[3],
  out = process.env.PROOF_DIR || '/tmp/admob-native-proof',
  serial = process.env.ANDROID_SERIAL || 'emulator-5580';
fs.mkdirSync(out, { recursive: true });
const referencePath = process.argv[4] || path.join(out, 'creative-reference.png');
const wait = (ms) => new Promise((r) => setTimeout(r, ms));
const adb = (...args) => execFileSync('adb', ['-s', serial, ...args], { encoding: 'utf8', timeout: 30000 });
(async () => {
  adb('shell', 'svc', 'wifi', 'enable');
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
  const pending = new Map();
  ws.onmessage = (e) => {
    const m = JSON.parse(e.data);
    if (m.id) {
      pending.get(m.id)?.(m);
      pending.delete(m.id);
    }
  };
  const call = (method, params = {}) =>
    new Promise((resolve, reject) => {
      const id = ++seq;
      const timer = setTimeout(() => reject(Error(method + ' timeout')), 20000);
      pending.set(id, (m) => {
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
  await call('Page.navigate', { url: 'https://localhost/native?snapshot' });
  for (let i = 0; i < 90; i++) {
    if (await ev('document.querySelectorAll("[data-native-ui-snapshot], [data-admob-snapshot]").length>0')) break;
    if (i === 89) throw Error('no snapshot');
    await wait(500);
  }
  await ev(
    `(async()=>{window.f=window.__capacitorAdMobNativeFeedLookup('native-ad-demo-feed');window.sc=await document.querySelector('ion-content').getScrollElement();window.slot=document.querySelector('capacitor-admob-native');window.firstState=[...f.slots.values()].find(s=>s.element===slot);for(const s of [...f.slots.values()]){if(s===firstState)continue;if(s.element)f.detach(s.element);await f.removeStateAd(s);f.slots.delete(s.slotKey)}const readout=document.querySelector('app-native-ad-demo ion-list');readout.style.height='250px';readout.style.overflow='hidden';sc.scrollTop+=slot.getBoundingClientRect().top-180;await f.invalidateLayout()})()`,
  );
  await wait(5000);
  let png,
    hash,
    creativeMae = null;
  let reference = fs.existsSync(referencePath)
    ? execFileSync('ffmpeg', ['-v', 'error', '-i', referencePath, '-pix_fmt', 'rgba', '-f', 'rawvideo', '-'], {
        maxBuffer: 10000000,
      })
    : null;
  for (let attempt = 0; attempt < 8; attempt++) {
    const captured = await ev(
      `Capacitor.nativePromise('AdMob','captureNativeAdSnapshot',{feedId:f.feedId,sessionId:f.sessionId,slotKey:firstState.slotKey})`,
    );
    png = Buffer.from(captured.dataUrl.split(',')[1], 'base64');
    fs.writeFileSync(`${out}/${name}-creative.png`, png);
    const rgba = execFileSync(
      'ffmpeg',
      ['-v', 'error', '-i', `${out}/${name}-creative.png`, '-pix_fmt', 'rgba', '-f', 'rawvideo', '-'],
      { maxBuffer: 10000000 },
    );
    if (!reference) {
      fs.writeFileSync(referencePath, png);
      reference = rgba;
    }
    creativeMae =
      rgba.length === reference.length
        ? rgba.reduce((sum, value, i) => sum + Math.abs(value - reference[i]), 0) / rgba.length
        : Infinity;
    if (creativeMae < 0.5) break;
    if (attempt === 7) throw Error('Could not match static creative; MAE ' + creativeMae);
    console.log(name + ' retry creative MAE ' + creativeMae);
    await ev('f.reload(firstState.slotKey)');
    await wait(5000);
  }
  hash = crypto.createHash('sha256').update(png).digest('hex');
  const state = await ev(
    `({slots:f.slots.size,rect:slot.getBoundingClientRect().toJSON(),dpr:devicePixelRatio,scrollTop:sc.scrollTop,session:f.sessionId})`,
  );
  console.log(name + ' creative ' + hash + ' slots ' + state.slots);
  adb('shell', 'svc', 'wifi', 'disable');
  await wait(10000);
  await call('Performance.enable', { timeDomain: 'threadTicks' });
  const cpu = () => {
    const s = adb('shell', 'cat', `/proc/${pid}/stat`).split(') ')[1].split(' ');
    return Number(s[11]) + Number(s[12]);
  };
  const sample = async () => ({
    cpu: cpu(),
    wall: Date.now(),
    perf: Object.fromEntries((await call('Performance.getMetrics')).result.metrics.map((m) => [m.name, m.value])),
    mem: adb('shell', 'dumpsys', 'meminfo', pid),
  });
  const beforeIdle = await sample();
  await wait(10000);
  const afterIdle = await sample();
  const before = await sample();
  for (let round = 0; round < 3; round++)
    for (const [a, b] of [
      [1800, 1640],
      [1640, 1800],
    ]) {
      await new Promise((r) =>
        spawn('adb', ['-s', serial, 'shell', 'input', 'swipe', '10', String(a), '10', String(b), '800']).on('exit', r),
      );
      await wait(700);
    }
  const after = await sample();
  await wait(10000);
  const afterCool = await sample();
  // Profile separately: instrumentation does not contaminate the timed windows above.
  await ev(
    `(()=>{window.counts={engine:{},placement:0,rect:0,computedStyle:0};for(const [key,bucket] of [['nativeUIEngine','engine'],['capacitorAdMobPlacements','placement']]){const c=window[key];if(c){const original=c.postMessage.bind(c);c.postMessage=(message)=>{if(bucket==='engine'){const a=JSON.parse(message).request.action;counts.engine[a]=(counts.engine[a]||0)+1}else counts.placement++;return original(message)}}}const rect=Element.prototype.getBoundingClientRect;Element.prototype.getBoundingClientRect=function(){counts.rect++;return rect.call(this)};const style=window.getComputedStyle;window.getComputedStyle=function(...args){counts.computedStyle++;return style.apply(this,args)}})()`,
  );
  await call('Profiler.enable');
  await call('Profiler.start');
  for (const [a, b] of [
    [1800, 1640],
    [1640, 1800],
  ]) {
    await new Promise((r) =>
      spawn('adb', ['-s', serial, 'shell', 'input', 'swipe', '10', String(a), '10', String(b), '800']).on('exit', r),
    );
    await wait(700);
  }
  const profile = (await call('Profiler.stop')).result.profile;
  fs.writeFileSync(`${out}/${name}.cpuprofile`, JSON.stringify(profile));
  const counts = await ev('counts');
  fs.writeFileSync(
    `${out}/${name}.json`,
    JSON.stringify({ hash, creativeMae, state, beforeIdle, afterIdle, before, after, afterCool, counts }, null, 2),
  );
  ws.close();
  console.log(name + ' COMPLETE ' + JSON.stringify(counts));
})()
  .catch((e) => {
    console.error(e);
    process.exitCode = 1;
  })
  .finally(() => {
    adb('shell', 'svc', 'wifi', 'enable');
  });
