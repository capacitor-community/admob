const fs = require('fs'),
  { execFileSync, spawn } = require('child_process');
const mode = process.argv[2],
  apk = process.argv[3],
  wait = (ms) => new Promise((r) => setTimeout(r, ms));
const serial = process.env.ANDROID_SERIAL || 'emulator-5580',
  out = process.env.PROOF_DIR || '/tmp/admob-native-proof';
fs.mkdirSync(out, { recursive: true });
const adb = (...a) => execFileSync('adb', ['-s', serial, ...a], { encoding: 'utf8', timeout: 30000 });
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
  await ev(`(async()=>{
 window.f=window.__capacitorAdMobNativeFeedLookup('native-ad-demo-feed');window.sc=await document.querySelector('ion-content').getScrollElement();
 document.querySelector('app-native-ad-demo ion-list').style.display='none';
 document.querySelectorAll('.feed > ion-card,.native-ad-row ion-item').forEach(e=>e.style.display='none');
 document.querySelector('.feed').style.cssText='padding-top:110px;padding-bottom:600px';sc.scrollTop=0;
 window.slots=[...document.querySelectorAll('capacitor-admob-native')].slice(0,2);
 window.states=slots.map(el=>[...f.slots.values()].find(s=>s.element===el));
 window.binding=el=>el[Symbol.for('capacitor-native-ui-engine.projection-placement')];
 window.multi={phases:[],messages:[],measures:[],frames:[],replies:[],batches:[]};
 const postPlacement=capacitorAdMobPlacements.postMessage.bind(capacitorAdMobPlacements);capacitorAdMobPlacements.postMessage=m=>{multi.batches.push({t:performance.now(),batch:JSON.parse(m),projected:states.map(s=>({slotKey:s.slotKey,projected:!!s.placementBinding?.projected}))});return postPlacement(m)};
 const receive=CapacitorNativeUIEngineRuntime.receive;globalThis.CapacitorNativeUIEngineRuntime={...CapacitorNativeUIEngineRuntime,receive:(id,key,value,error)=>{multi.replies.push({t:performance.now(),id,key,value,error});return receive(id,key,value,error)}};
 const measure=f.measurePlacement.bind(f);f.measurePlacement=s=>{multi.measures.push({t:performance.now(),slot:s.slotKey,projected:!!s.placementBinding?.projected});return measure(s)};
 const channel=nativeUIEngine.postMessage.bind(nativeUIEngine);window.blockedID=undefined;
 nativeUIEngine.postMessage=m=>{const msg=JSON.parse(m);multi.messages.push({t:performance.now(),...msg});const id=msg.key.split(':')[0];if(window.blockSecond && slots[1].hasAttribute('data-native-ui-session-'+id.replace(/[^a-zA-Z0-9-]/g,'')) && msg.request.action==='prepare'){multi.blockedCaptures=(multi.blockedCaptures||0)+1;queueMicrotask(()=>CapacitorNativeUIEngineRuntime.receive(id,msg.key,{}));return}return channel(m)};
 const start=CapacitorNativeUIEngineRuntime.start;CapacitorNativeUIEngineRuntime.start=c=>{if(window.blockSecond&&document.querySelector(c.projectionSelector)===slots[1])blockedID=c.id;return start(c)};
 slots.forEach((el,i)=>{el.parentElement.style.position='relative';const b=document.createElement('div');b.dataset.proofAdMarker=String(i);b.style.cssText='position:absolute;left:0;top:0;width:4px;height:320px;z-index:100;pointer-events:none;background:'+ (i?'cyan':'magenta');el.parentElement.append(b)});
 await f.invalidateLayout();
})()`);
  for (let i = 0; i < 60; i++) {
    if (
      await ev(
        'states.every(s=>s.status==="loaded") && slots.every(s=>s.querySelector("[data-native-ui-snapshot]") || s.hasAttribute("data-native-ui-snapshot-prepared"))',
      )
    )
      break;
    await wait(250);
  }
  await wait(1800);
  const startPhase = async (name) =>
    ev(
      `if(${JSON.stringify(name)}!=='both-normal')document.querySelectorAll('[data-proof-ad-marker]').forEach(e=>e.style.background='gray');multi.phases.push({name:${JSON.stringify(name)},t:performance.now(),loaded:[...f.slots.values()].filter(s=>s.status==="loaded").map(s=>s.slotKey),states:states.map(s=>({slot:s.slotKey,status:s.status,rect:s.element?.getBoundingClientRect().toJSON()}))})`,
    );
  const sample = () =>
    ev(
      `({time:performance.now(),bindings:slots.map(el=>({projected:!!binding(el)?.projected,owner:!!binding(el)?.owner,images:el.querySelectorAll('img').length})),visible:document.querySelectorAll('[data-native-ui-snapshot-visible]').length})`,
    );
  await ev(
    `window.sampleRunning=true;const tick=()=>{if(!sampleRunning)return;multi.frames.push({t:performance.now(),slots:slots.map(el=>({y:el.getBoundingClientRect().top,projected:!!binding(el)?.projected,visible:el.hasAttribute('data-native-ui-snapshot-visible')}))});requestAnimationFrame(tick)};tick()`,
  );
  const beforeMemory = adb('shell', 'dumpsys', 'meminfo', pid);
  const recordStarted = await ev('performance.now()');
  const record = spawn('adb', [
    '-s',
    serial,
    'shell',
    'screenrecord',
    '--time-limit',
    '28',
    `/sdcard/${mode}-proof.mp4`,
  ]);
  const done = new Promise((r) => record.on('exit', r));
  await wait(600);
  await startPhase('both-normal');
  for (let i = 0; i < 2; i++)
    for (const [a, b] of [
      [1800, 1700],
      [1700, 1800],
    ]) {
      await new Promise((r) =>
        spawn('adb', ['-s', serial, 'shell', 'input', 'swipe', '10', String(a), '10', String(b), '800']).on('exit', r),
      );
      await wait(450);
    }
  await startPhase('mixed-injected-capture-unavailable');
  await ev('window.blockSecond=true;f.reload(states[1].slotKey)');
  await wait(600);
  await ev(
    'window.pulse=setInterval(()=>{sc.scrollTop+=sc.scrollTop<20?2:-2;document.dispatchEvent(new Event("scroll"))},40)',
  );
  await wait(1000);
  const mixed = await sample();
  await ev('clearInterval(pulse)');
  await wait(500);
  await startPhase('reload-one-while-other-covered');
  await ev('window.blockSecond=false;blockedID=undefined;f.reload(states[1].slotKey)');
  await wait(1800);
  await ev('window.pulse=setInterval(()=>document.dispatchEvent(new Event("scroll")),30)');
  await wait(150);
  const beforeReload = await sample();
  await ev('f.reload(states[0].slotKey)');
  await wait(500);
  const afterReload = await sample();
  await ev('clearInterval(pulse)');
  await wait(1200);
  await startPhase('detach-one-while-other-covered');
  await ev('window.pulse=setInterval(()=>document.dispatchEvent(new Event("scroll")),30)');
  await wait(150);
  const beforeDetach = await sample();
  await ev('f.detach(slots[0])');
  await wait(300);
  const afterDetach = await sample();
  await ev('clearInterval(pulse)');
  await wait(500);
  await ev('f.attach(states[0].slotKey,slots[0])');
  await wait(1000);
  await startPhase('pause-both');
  await ev('window.pulse=setInterval(()=>document.dispatchEvent(new Event("scroll")),30)');
  await wait(150);
  const beforePause = await sample();
  await ev('f.pause()');
  await ev('clearInterval(pulse)');
  await wait(150);
  const afterPause = await sample();
  await done;
  adb('pull', `/sdcard/${mode}-proof.mp4`, `${out}/${mode}.mp4`);
  const trace = await ev('sampleRunning=false;multi');
  const afterMemory = adb('shell', 'dumpsys', 'meminfo', pid);
  fs.writeFileSync(
    `${out}/${mode}.json`,
    JSON.stringify(
      {
        recordStarted,
        beforeMemory,
        afterMemory,
        mixed,
        beforeReload,
        afterReload,
        beforeDetach,
        afterDetach,
        beforePause,
        afterPause,
        trace,
      },
      null,
      2,
    ),
  );
  ws.close();
  console.log(
    mode +
      ' COMPLETE ' +
      JSON.stringify({ mixed, beforeReload, afterReload, beforeDetach, afterDetach, beforePause, afterPause }),
  );
})().catch((e) => {
  console.error(e);
  process.exitCode = 1;
});
