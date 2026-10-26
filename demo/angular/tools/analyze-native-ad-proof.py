import subprocess,json,sys,os
mode=sys.argv[1];path=os.path.join(os.environ.get('PROOF_DIR','/tmp/admob-native-proof'),mode)
x=json.loads(subprocess.check_output(['ffprobe','-v','error','-select_streams','v:0','-show_entries','frame=best_effort_timestamp_time','-of','json',path+'.mp4']));times=[float(f['best_effort_timestamp_time']) for f in x['frames']]
raw=subprocess.check_output(['ffmpeg','-v','error','-i',path+'.mp4','-vf','scale=270:600','-fps_mode','passthrough','-pix_fmt','rgb24','-f','rawvideo','-'])
size=270*600*3;frames=[];suspects=[]
for i,t in enumerate(times):
 data=raw[i*size:(i+1)*size]
 def px(x,y):
  start=(y*270+x)*3;return data[start:start+3]
 ys=[y for y in range(56,580) if any(px(x,y)[0]>180 and px(x,y)[2]>180 and px(x,y)[1]<80 for x in range(1,7))]
 if not ys:frames.append({'t':t,'excluded':'marker not visible'});continue
 top=max(ys)-209
 lo=max(56,top+10);hi=min(580,top+125)
 if hi-lo<25:frames.append({'t':t,'excluded':'media outside viewport'});continue
 blueRows=[y for y in range(56,580) if sum(1 for x in range(140,220) for r,g,b in [px(x,y)] if r<45 and 95<g<135 and 215<b<250)>30]
 groups=[]
 for y in blueRows:
  if groups and y-groups[-1][-1]<6:groups[-1].append(y)
  else:groups.append([y])
 masks=[(g[0]-65,g[-1]+8) for g in groups if g[-1]-g[0]>=8]
 visibleRows=[y for y in range(lo,hi) if not any(a<=y<=b for a,b in masks)]
 if len(visibleRows)<25:frames.append({'t':t,'excluded':'validator occludes media'});continue
 colors=sum(1 for y in visibleRows for x in range(25,245) for r,g,b in [px(x,y)] if max(r,g,b)-min(r,g,b)>90 and min(r,g,b)<140)
 frames.append({'t':t,'top':top,'pixels':colors})
 if colors<30:
  suspects.append(i)
runs=[]
for i in suspects:
 if runs and runs[-1]['last']==i-1:runs[-1]['last']=i
 else:runs.append({'first':i,'last':i})
for r in runs:r['durationMs']=round(((times[r['last']+1] if r['last']+1<len(times) else times[-1])-times[r['first']])*1000)
result={'frames':len(times),'eligible':sum('pixels'in f for f in frames),'minimumPixels':min((f['pixels'] for f in frames if 'pixels'in f),default=None),'blankRuns':runs,'detail':frames}
open(path+'-analysis.json','w').write(json.dumps(result,indent=2));print(json.dumps({k:v for k,v in result.items() if k!='detail'}))
