# NativeAdFeed Engine migration verification

This migration is **draft / release-gated**. Published Engine 2.0.3 does not contain
the APIs used here. Before merging, release [Engine PR #333](https://github.com/rdlabo-dev/ionic-theme-ios27/pull/333)
and update both npm/lockfile and the default Android Maven version to that release.
Local builds used the linked checkout and Maven artifact `2.0.3-admob.13`; neither
that artifact nor its local path is a distributable dependency.

## Scope and handoff design

Only Android NativeAdFeed's opt-in `androidScrollSnapshot` path moves to the Engine.
The ad plugin still owns SDK loading, impression/click handling, placement, and
capture eligibility (test ads, opaque background, no video, at most 2,000,000 pixels). iOS is not migrated.
The existing low-level snapshot APIs remain compatible. This uses ordinary Ionic
scrolling; no virtual scroller was introduced.

- A stable per-slot selector survives detach/reattach; replacing an ad closes its old registration.
- `failedFallback: native` keeps a real ad when a prepared image is unavailable.
- `CaptureMode.VIEW` captures only the ad, excluding toolbars/adjacent pixels and padding.
- The Engine prepaints an opaque DOM image below the native ad. It waits for WebView
  visual readiness and a draw before hiding native. VIEW uses no native entry/return
  image copies, avoiding their independent positioning during partial scrolling.
- Content/size changes invalidate preparation; position-only scrolling reuses the PNG
  and decoded image. Native bitmaps are recycled after encoding. No polling capture timer
  or fixed 500 ms load delay is needed; eligibility and actual image readiness govern handoff.
- Late cover callbacks check generation, ownership, visibility and eligibility before hiding.
- Pause completion waits for both native pixels and the DOM underlay to retire.
- Placement distinguishes Engine-owned hidden DOM from application `visibility: hidden`,
  including inherited visibility, and synchronously restores Engine markers before painting.

## Reproduction

Use an isolated emulator with test ads. Install/link the reviewed Engine checkout into
the plugin without changing the lockfile, build its runtime, and publish a uniquely
versioned local Maven verification artifact. The demo keeps minSdk 23.

```sh
# In the Engine package
npm run build
# Android verification publication is configured in the Engine Android Gradle project.

# In this plugin, after installing dependencies
npm install --no-save --package-lock=false /path/to/engine/packages/capacitor-native-ui-engine
npm run build
cd demo/angular
npm run build
npx cap sync android
cd android
./gradlew -PnativeUIEngineVersion=2.0.3-admob.13 \
  -PnativeUIEngineRepository=/path/to/engine/packages/capacitor-native-ui-engine/android/build/maven \
  assembleDebug

# From demo/angular (Node 22+, adb, ffmpeg/ffprobe and Python 3 required)
ANDROID_SERIAL=emulator-5580 PROOF_DIR=/tmp/admob-native-proof \
  node tools/native-ad-engine-proof.cjs trial1 android/app/build/outputs/apk/debug/app-debug.apk
PROOF_DIR=/tmp/admob-native-proof python3 tools/analyze-native-ad-proof.py trial1
```

The runner records eight gestures across three speeds, including partial viewport
clipping, then checks pause during image display and ad reload. It fixes the diagnostic
list height to avoid unrelated text layout changes. A marker follows the ad so analysis
uses moving geometry; the validator popup is masked. Inspect flagged frames manually.
Screen recordings have variable frame rate: zero detected blanks is evidence for these
recorded frames, not proof that every physical display frame is flash-free.

## Final performance comparison

The earlier multi-ad runs did not hold creatives/SDK activity constant. Their reported
54% migration-regression attribution was withdrawn; those runs are not the final
performance comparison.

The final comparison used API 36.1 ARM64, one static Flood-It test ad, identical slot
geometry, six 800 ms swipes with 700 ms pauses, and no screen recording during timing.
Other ad states were removed. The native ad image was compared against the baseline
RGBA capture (mean absolute channel difference below 0.5/255; actual maximum across
accepted runs below 0.0873/255). Different creatives were rejected and reloaded. Wi-Fi was
disabled after loading; each run had a 10 s warmup, a 10 s idle window, the motion window,
and a 10 s cooldown. Old and final APKs were run alternately, three accepted runs each.
CPU sampling/profiling and bridge counters ran separately after the timing windows.
After these measurements, the final eligibility-only fix restored AdMob's old 2M-pixel
limit (Engine's generic limit is 8M). The benchmark ad was 996 × 840 = 836,640 pixels,
so it remains eligible; final-build visual/lifecycle smoke verification was repeated.

| Metric (three-trial median)      |     PR #487 | Final migration |
| -------------------------------- | ----------: | --------------: |
| Motion window                    |    9,546 ms |        9,534 ms |
| Host process CPU time            |    2,130 ms |        2,350 ms |
| WebView main-thread TaskDuration |      122 ms |          207 ms |
| Host PSS after gestures          | 186,811 KiB |     183,747 KiB |

The final implementation still has higher CPU cost in this controlled emulator workload:
+220 ms host CPU (about 10.3% of baseline CPU time) and +85 ms WebView main-thread time.
Host CPU ranges were 2,120–2,240 ms versus 2,200–2,380 ms; they overlap. PSS ranges also
overlap (179,649–199,378 versus 178,592–189,101 KiB), so no memory improvement is claimed.
These small-sample emulator results do not establish a universal device-wide percentage.

Code inspection and separate two-swipe counter runs identified a concrete migration
change: the old Feed excluded snapshot-covered ads from native placement batches;
the migrated Feed continues geometry calculation and sending placements during image
presentation. Baseline sent 2 direct placement messages; final runs sent 58–68.
The Engine additionally sent 14 handoff/lifecycle messages per counter run. This
explains added work, but is not a measured attribution of every CPU millisecond.

The final fix defers same-size native coordinate/clip application while VIEW projection
owns the image, retaining only the latest validated placement. `beforeNativeReveal`
applies it before normal return, fallback, reset or active-image release. Hide, resize,
replacement and generation checks remain immediate. JavaScript calculation/transport
is still present: eliminating it would require an additional placement synchronization
contract. No performance-parity claim is made.

To reproduce the matched comparison, run `tools/native-ad-engine-benchmark.cjs NAME APK`
with `ANDROID_SERIAL` and `PROOF_DIR`. The first run saves `creative-reference.png`;
subsequent runs reject mismatching creatives before timing. The script uses private Feed
state only as a test harness to remove other ads; it is not application integration code.

## Final visual and lifecycle verification

Placement-deferral verification (`verified1`–`verified3`): 660 recorded frames / 625 eligible media frames, zero blank candidates. Final APK including the restored 2M-pixel guard (`verified4`): 204 / 204, zero candidates. Final 450/900/450/900 ms JavaScript-blocking recording: 22 / 22, zero candidates. All four normal runs paused during image presentation with zero visible DOM proxies after completion, reloaded to newly decoded image resources, and created no new PNG files during position-only gestures. Moving-region analysis masks the SDK validator popup; contact sheets were inspected. These variable-frame-rate recordings do not prove every physical frame is flash-free.

The earlier final-before-deferral recordings (`release1`–`release3`) contained 648
frames / 639 eligible media frames with zero blank candidates. These are retained as
intermediate evidence, not substituted for final APK verification.

Engine browser suite: 64 passed. Final Android native suite passed with the API 23-only
test skipped on API 36.1; API 23 fallback passed separately. AdMob Web suite: 42 passed,
final changed Feed spec: 32 passed; Android unit tests: 81 passed (including the pixel-limit boundary test). Builds/typecheck/lint
passed. Manager and independent reviewer approved the final native deferral change,
including the active-image release regression test.

Raw recordings, CPU profiles, captures and traces are retained locally under
`/private/tmp/admob-final-proof`. Per-trial final performance data is in
[the benchmark summary](tools/native-ad-engine-benchmark-results.json).

## API 23

The Engine's unsupported-capture test passes on Android 6.0/API 23 with the native View
visible. The demo cannot start on that emulator: Capacitor Bridge.loadWebView references
`android.webkit.ServiceWorkerController` (API 24). Installing the old PR #487 APK reproduces
the same crash. This is an existing demo/Capacitor limitation, not an Engine migration
regression; API 23 app runtime support is not claimed.

## Remaining gates

- Publish the Engine fixes and pin the same released npm/Maven version; rerun an ordinary
  clean dependency install/build with no local overrides.
- Final CPU overhead is reported above; broader physical-device performance coverage
  is outside these emulator measurements.
- No production ads or synthesized ad clicks were used. Neither PR is merged.
