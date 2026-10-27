# NativeAdFeed Engine migration verification

The Engine now lives at [webview-native-ui/engine](https://github.com/webview-native-ui/engine).
AdMob uses the published npm package `@webview-native-ui/engine@0.0.1` and Maven
artifact `io.github.webview-native-ui:engine:0.0.1`. The Java/Kotlin namespace is
`io.github.webview_native_ui.engine`. The previous unreleased-package dependency
gate is resolved; no local npm link or Maven override is needed.

`npm ci --ignore-scripts`, root/demo builds, ESLint, 47 Web tests and 84 Android unit
tests passed with these published dependencies. Gradle dependency insight resolved
`io.github.webview-native-ui:engine:0.0.1` using the default configuration.

The detailed performance/visual figures below describe the earlier linked build
(Engine `0fa8be2`, local Maven `2.0.3-admob.17`) and are retained as historical evidence.
They are not a new CPU/memory comparison of published 0.0.1. The previous Engine PR
[#333](https://github.com/rdlabo-dev/ionic-theme-ios27/pull/333) is historical context;
future Engine work uses the new standalone repository.

## Published 0.0.1 smoke verification

- Normal and partially clipped scrolling: 177/177 eligible recorded frames, zero blank candidates.
- Two visible ads: 28 and 29 eligible ad-frame samples, zero blank candidates; zero bridge errors.
- Fault-injected capture unavailability kept one ad native and the other projected.
  All 22 mixed batches retained the image-owned slot without rect/clipRect.
- Reload/detach of one ad preserved the other image; feed pause cleared both.
- Normal-run pause was exercised during image display. Position-only scrolling
  created no new PNG files; reload decoded fresh image resources.
- Contact sheet inspection found no return-position duplication. These are sampled
  emulator checks, not an assertion about every physical display frame.

[Published-release verification summary](tools/native-ad-engine-published-results.json).
Raw recordings/traces: `/private/tmp/admob-new-engine-proof`. CPU/memory comparisons
were not repeated for this repository/package relocation.

## Implementation and multiple ads

Only Android NativeAdFeed's opt-in `androidScrollSnapshot` path moves to the Engine.
AdMob retains SDK loading, impression/click handling, placement, and test-ad/opaque/
no-video/2M-pixel capture eligibility. This is ordinary Ionic scrolling, not virtual
scrolling. AdMob iOS and the low-level snapshot APIs remain compatible.

- Each ad has its own stable host selector, native registration, image token, content
  revision, placement binding and sequence. Updating, detaching or failing capture
  for one ad does not retire another ad's image.
- `CaptureMode.VIEW` captures only the ad. An opaque image is prepainted below native;
  WebView visual readiness and a draw fence precede native hiding. Native draw ACK
  precedes normal image retirement. No independent native entry/return copies are used.
- AdMob stops measuring a host only after the cover ACK confirms that native is hidden.
  An unavailable image retains ordinary native tracking (`failedFallback: native`).
- When another ad still needs updates, the batch keeps the covered slot using a
  `retain` marker with **no rect or clipRect**. Native does not change that slot's
  geometry, deferred placement or placement sequence. Omission would hide it, so a
  covered slot is not simply removed from a full placement batch.
- Return measures the returning ad once and includes its latest placement in the
  existing Engine restore request. The native callback validates feed/session/slot,
  generation, sequence, image token and View identity, then applies placement before
  reveal. It does not require another bridge round trip. Explicit placement is
  authoritative: the Engine must not overwrite it using an older image anchor.
- Full-coordinate deduplication stays separate from retain serialization. Resize,
  author visibility, pause and explicit invalidation send real new placement/hide
  data. Per-slot sequencing rejects an older batch without dropping updates to other ads.
- Session ownership and presentation generations reject old cover/resume ACKs. A new
  gesture cannot let an earlier resume ACK retire its image. Intermediate `begin()`
  wakeups cannot send an unnecessary native reveal between two image presentations.
- A failed handoff clears projection ownership, invalidates optimistic placement
  caches and schedules normal placement reconciliation. Old sessions cannot clear
  the new session's binding.
- Size/content changes and ad replacement recapture; position-only scrolling reuses
  the PNG and decoded image. Native VIEW Bitmaps are recycled after encoding. No
  periodic recapture timer or fixed 500 ms load delay is introduced.
- The window's current touch state survives initial connection/reset, preventing
  capture while a finger was already down before a new registration connected.

## Previous linked-build visual and lifecycle results

Android API 36.1 ARM64, Google test ads, final linked/Maven build:

| Scenario                                                   |    Inspected media samples | Blank candidates |
| ---------------------------------------------------------- | -------------------------: | ---------------: |
| Eight ordinary gestures, three speeds and partial clipping | 203 of 209 recorded frames |                0 |
| JavaScript blocked for 450/900/450/900 ms                  |   22 of 22 recorded frames |                0 |
| Two ads together, four gestures: first ad                  |        70 ad-frame samples |                0 |
| Two ads together, four gestures: second ad                 |        68 ad-frame samples |                0 |

Normal-run pause occurred during image display and completed with zero visible image
proxies. Position-only gestures created zero new PNGs. Reload produced three newly
decoded resources. Moving-region analysis masks SDK validator popups; contact sheets
were inspected. These variable-frame-rate recordings are sampled evidence, not a
proof that every physical frame/device is flash-free. Ad-frame samples from two ads
are not counted as separate physical display frames.

The multiple-ad run confirmed eight cover/restore cycles (four per ad), with no bridge
errors. For a controlled mixed case the harness returned capture-unavailable for the
second ad: the first stayed projected and the second used native tracking. All 22
mixed batches retained the first ad without geometry. Reloading or detaching the first
ad left the second projected; pausing the feed cleared both. The harness uses private
Feed state and explicit capture fault injection only for verification.

The two-ad lifecycle run's process PSS was 184,507 KiB before gestures and 208,111 KiB
after reload/detach/pause. This includes SDK activity and changing creatives; it is not
an isolated Engine memory allocation or a leak/no-leak conclusion. The matched static
creative comparison below is the controlled performance measurement.

Intermediate recordings (`multi-handoff1` and `handoff-final`, earlier local artifacts)
showed misplaced/duplicated native pixels around return. They are **not** final passing
evidence. Regression tests reproduced old image-anchor correction overwriting a newer
placement and an old resume ACK retiring a newer presentation. Both were fixed before
these final recordings (`retain-final`, `retain-stress-final`, `multi-retain-final`).

## Previous linked-build matched performance comparison

One static Flood-It ad, matching RGBA creative (MAE <0.5/255), identical slot geometry,
six 800 ms swipes with 700 ms pauses; Wi-Fi disabled after loading; 10 s warmup, idle
and cooldown windows. The original PR #487 APK (`12e9b393`) and the final migration
APK were alternated for three accepted runs each. No recording, build or tests ran
concurrently with timing. Profiling and counters were collected in a separate two-swipe
window. CPU is host-process time from `/proc`; WebView time is CDP thread TaskDuration.

| Three-run median                 |     PR #487 | Final migration |
| -------------------------------- | ----------: | --------------: |
| Motion window                    |    9,477 ms |        9,479 ms |
| Host process CPU                 |    2,120 ms |        2,370 ms |
| WebView main-thread TaskDuration |      118 ms |          164 ms |
| Host PSS after motion            | 183,978 KiB |     181,658 KiB |

Host CPU remains higher in this workload: +250 ms (11.8% of baseline process CPU),
with ranges 2,030–2,190 ms versus 2,170–2,580 ms. WebView main-thread time is +46 ms
at the median. PSS ranges overlap: 181,909–186,391 versus 173,758–193,851 KiB. No host
CPU improvement or memory reduction is claimed. These are comparisons of the tested
APKs, not attribution of every CPU millisecond to a single function.

Separate two-swipe counters: old PR sent 2 direct placement messages; the final
migration sent 2–3 direct messages **plus 2 placements piggybacked on restore**.
The 14 Engine lifecycle messages include those restore requests; they are not another
14 coordinate payloads. The direct messages can precede the cover ACK while native
still owns the pixels. During confirmed image ownership, continuous AdMob geometry
measurement/sending is suppressed. Mixed batches use geometry-free retain markers.
The previous migration's 58–68 direct messages are historical pre-handoff counts.

The final Engine still performs Web geometry/lifecycle work: 66–76 bounding-rect calls
and 247–278 computed-style calls versus 42 and 104 in the old PR's separate profiling
window. Eliminating continuous native placement transport does not eliminate all
Engine overhead. Accepted creative MAE ranged below 0.359/255 (threshold 0.5/255).

The earlier uncontrolled multi-ad 54% attribution remains withdrawn. Historical
pre-handoff measurements are retained in `native-ad-engine-benchmark-results.json`;
they are superseded for the current implementation by the return results linked below.
This small emulator sample does not establish performance parity or a universal
physical-device percentage, and process PSS alone does not establish memory improvement.

## Previous linked-build validation and review

- Engine browser suite: 79 passed; build/typecheck/formatting passed.
- Engine Android: 16 instrumentation tests passed with the API 23-only test skipped
  on API 36.1; lint passed. The dedicated API 23 fallback test passed separately.
- AdMob: root/demo/APK builds and ESLint passed; Web suite 47 passed (Feed spec 37);
  Android unit suite 84 passed.
- Manager and independent reviewer completed fix/re-review cycles, covering touch
  reset, mixed ads, per-slot sequencing, visibility/invalidation, failed handoff,
  old-session ACKs, rapid scroll restart, explicit native placement, and legacy callers.
- Engine CI on `0fa8be2`: Android integration, Swift, browser, package, lint/format,
  repository tests and CodeQL all passed. AdMob #488 is stacked
  on #487 and has no automatic check suite for that base; local results are listed here.

The AdMob demo cannot start on API 23 because Capacitor references
`android.webkit.ServiceWorkerController` (API 24). The original #487 APK reproduces
the same crash. Only the Engine's native fallback test is claimed on API 23.

## Reproduction and artifacts

Build against the published npm and Maven packages:

```sh
npm ci --ignore-scripts
npm run build
cd demo/angular
npm run build
npx cap sync android
cd android
./gradlew assembleDebug
```

From `demo/angular`, with Node 22+, adb, ffmpeg/ffprobe and Python 3:

```sh
ANDROID_SERIAL=emulator-5580 PROOF_DIR=/tmp/admob-native-proof \
  node tools/native-ad-engine-proof.cjs normal android/app/build/outputs/apk/debug/app-debug.apk
PROOF_DIR=/tmp/admob-native-proof python3 tools/analyze-native-ad-proof.py normal
ANDROID_SERIAL=emulator-5580 PROOF_DIR=/tmp/admob-native-proof \
  node tools/native-ad-engine-multi-proof.cjs multi android/app/build/outputs/apk/debug/app-debug.apk
PROOF_DIR=/tmp/admob-native-proof python3 tools/analyze-native-ad-multi-proof.py multi
```

Use `tools/native-ad-engine-benchmark.cjs NAME APK` with the same environment variables
for the matched comparison; its first run saves the creative reference and later runs
reject mismatching creatives. Multi-ad markers turn gray before deliberate reload/
detach/pause so those lifecycle gaps are excluded from normal-scroll blank analysis.

- [Previous linked-build visual/lifecycle summary](tools/native-ad-engine-return-visual-results.json)
- [Previous linked-build accepted performance trials](tools/native-ad-engine-return-benchmark-results.json)
- Raw recordings, captures, CDP traces and profiles: `/private/tmp/admob-final-proof`.

No production ads or synthesized ad clicks were used.
