import subprocess,json,sys,os
name=sys.argv[1];path=os.path.join(os.environ.get('PROOF_DIR','/tmp/admob-native-proof'),name)
meta=json.load(open(path+'.json'));phase=meta['trace']['phases'];end=(phase[1]['t']-meta['recordStarted'])/1000
probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-select_streams','v:0','-show_entries','frame=best_effort_timestamp_time','-of','json',path+'.mp4']))
times=[float(f['best_effort_timestamp_time']) for f in probe['frames']]
raw=subprocess.check_output(['ffmpeg','-v','fatal','-i',path+'.mp4','-vf','scale=270:600','-fps_mode','passthrough','-pix_fmt','rgb24','-f','rawvideo','-'])
size=270*600*3;result=[]
for ad in range(2):
 frames=[]
 for i,t in enumerate(times):
  if t>end:break
  data=raw[i*size:(i+1)*size]
  def px(x,y):
   start=(y*270+x)*3;return data[start:start+3]
  def marker(v):
   r,g,b=v;return (r>180 and g<80 and b>180) if ad==0 else (r<80 and g>180 and b>180)
  cols=[(x,[y for y in range(56,580) if marker(px(x,y))]) for x in range(1,22)]
  col=max(cols,key=lambda p:len(p[1]));ys=col[1]
  if len(ys)<100:continue
  top=max(ys)-209;lo=max(56,top+5);hi=min(580,top+125)
  blueRows=[y for y in range(lo,hi) if sum(1 for x in range(140,220) for r,g,b in [px(x,y)] if r<45 and 95<g<135 and 215<b<250)>30]
  masks=[]
  for y in blueRows:
   if masks and y-masks[-1][-1]<6:masks[-1].append(y)
   else:masks.append([y])
  rows=[y for y in range(lo,hi) if not any(a[0]-65<=y<=a[-1]+8 for a in masks if a[-1]-a[0]>=8)]
  if len(rows)<25:continue
  colors=sum(1 for y in rows for x in range(25,245) for r,g,b in [px(x,y)] if max(r,g,b)-min(r,g,b)>90 and min(r,g,b)<140)
  frames.append({'frame':i,'t':t,'top':top,'pixels':colors})
 result.append({'ad':ad,'eligible':len(frames),'blankCandidates':[f for f in frames if f['pixels']<30],'detail':frames})
open(path+'-analysis.json','w').write(json.dumps({'normalEnd':end,'ads':result},indent=2));print(json.dumps({'normalEnd':end,'ads':[{k:v for k,v in r.items() if k!='detail'} for r in result]}))
