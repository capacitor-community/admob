# NativeAdFeed Engine migration verification

This migration is **draft / release-gated**. Published Engine 2.0.3 does not contain
the APIs used here. Before merging, release [Engine PR #333](https://github.com/rdlabo-dev/ionic-theme-ios27/pull/333)
and update both npm/lockfile and the default Android Maven version to that release.
Local builds used the linked checkout and Maven artifact `2.0.3-admob.10`; neither
that artifact nor its local path is a distributable dependency.

## Scope and handoff design

Only Android NativeAdFeed's opt-in `androidScrollSnapshot` path moves to the Engine.
The ad plugin still owns SDK loading, impression/click handling, placement, and
capture eligibility (test ads, opaque background, no video). iOS is not migrated.
The existing low-level snapshot APIs remain compatible.

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
./gradlew -PnativeUIEngineVersion=2.0.3-admob.10 \
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

## Results

Measured on 2026-10-26, dedicated API 36.1 ARM64 emulator. Baseline is PR #487
commit `12e9b393`; Engine is `1bcf135` plus the final AdMob author-visibility fix.
Per-trial data: [native-ad-engine-results.json](tools/native-ad-engine-results.json).

| Metric (three-trial median)        |     PR #487 | Engine migration |
| ---------------------------------- | ----------: | ---------------: |
| Host CPU time / approximately 25 s |    1,120 ms |         1,720 ms |
| WebView main-thread TaskDuration   |      532 ms |           664 ms |
| Host PSS after gestures            | 186,931 KiB |      190,850 KiB |

These runs did **not** hold ad creatives, SDK activity, or host load constant.
The table is descriptive data only. It does not establish a migration regression rate,
performance parity, or a cause. The earlier attribution of the difference to this
migration is withdrawn. A matched comparison and workload breakdown remain outstanding.

- Final normal trials: 648 recorded frames, 639 eligible media frames, zero blank candidates.
  Baseline: 588 recorded / 579 eligible, also zero candidates. Final contact sheets were inspected.
- Additional 450/900/450/900 ms JavaScript blocking: 26 recorded / 26 eligible frames,
  zero blank candidates. This stress dispatches scroll events while JS is blocked, not a hardware swipe.
- All three trials paused while image presentation was active and had zero visible DOM
  proxy images after pause completed. Reload decoded fresh image resources every time.
- Position-only gestures created zero new PNG files in all trials. Capture/decode may still
  occur for late-loaded or resized ads; this is not a claim of zero allocation throughout scrolling.
- Engine browser suite: 64 passed. API 36.1 native suite passed with the API 23-only test
  skipped; API 23 fallback test separately passed. AdMob Web suite: 42 passed; final changed
  feed spec: 32 passed. Android unit tests: 80 passed. Build/typecheck/lint checks passed.
- Manager and independent Engine reviews approved. Manager's final AdMob review found an
  author-visibility regression, which was fixed and re-reviewed with passing regression tests.

Raw recordings, traces and logs are retained locally under `/private/tmp/admob-final-proof`
(`release1`, `release2`, `release3`, `release-stress`).

## API 23

The Engine's unsupported-capture test passes on Android 6.0/API 23 with the native View
visible. The demo cannot start on that emulator: Capacitor Bridge.loadWebView references
`android.webkit.ServiceWorkerController` (API 24). Installing the old PR #487 APK reproduces
the same crash. This is an existing demo/Capacitor limitation, not an Engine migration
regression; API 23 app runtime support is not claimed.

## Remaining gates

- Publish the Engine fixes and pin the same released npm/Maven version; rerun an ordinary
  clean dependency install/build with no local overrides.
- CPU/memory comparison remains unvalidated: repeat with matched creatives/workload and
  report the processing breakdown before making a regression claim.
- No production ads or synthesized ad clicks were used. Neither PR is merged.
