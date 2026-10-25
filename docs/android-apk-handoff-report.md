# Android APK handoff report

Status: debug APKs built, installed, and validated. Signed release artifacts are not available because this checkout
has no Raven upload signing credentials.

- Source branch: `android-apk-handoff`
- Source commit for the APKs: `d813f40a8491ca46ecdf93015a7bb2db0fa60eb9`
  (`Add truthful listening stats and monthly rating reminder`)
- Base commit: `ca64db7` (`docs: add Android APK handoff plan and KMP audit`)
- Build date: 2026-10-25
- Toolchain: JDK 17 bootstrap (`/opt/homebrew/opt/openjdk@17`), Gradle 9.5.0, Android SDK platform 36,
  build-tools 36.0.0

## Artifacts

| Flavor | Absolute path | Size | SHA-256 |
| --- | --- | ---: | --- |
| Play debug | `app/build/outputs/apk/play/debug/app-play-debug.apk` | 82,970,264 bytes | `bad78d9d9aa16a3f2b0d23978bcb8a08fd7e322f4948ca8614f5dc74b85b1aef` |
| GitHub debug | `app/build/outputs/apk/github/debug/app-github-debug.apk` | 81,760,588 bytes | `92f6e0e7e143d3fa0a902526e7e5474f1c5f2f3a2d45a90c2b51d85d243db928` |

Both APKs report:

- Application ID: `io.github.emeraldrealm.raven.debug`
- Version name: `1.7-debug`
- Version code: `8`
- Minimum SDK: 28
- Target SDK: 36
- Debug signing certificate SHA-256: `18d8e4c48cf72455252ecd81d09c2ab44673e8d884b5bc3546cd072b59527103`

The debug APK is for testing only. It cannot be uploaded as a production update, and its application-ID suffix and
debug certificate make it unsuitable for an upgrade test over the published app.

Reproduce:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assemblePlayDebug :app:assembleGithubDebug --console=plain
shasum -a 256 app/build/outputs/apk/play/debug/app-play-debug.apk \
  app/build/outputs/apk/github/debug/app-github-debug.apk
```

## What changed

### Listening and levels

- Active listening is measured in `core/playback` with `SystemClock.elapsedRealtime()`, not wall time or saved book
  positions. Only `isPlaying` intervals accrue, so paused, buffering, seeking and idle time do not count. Playback in
  the background still counts, and speed changes affect media coverage but never multiply elapsed listening time.
- Measurements are checkpointed every 15 seconds and flushed on pause, media transition, end/idle and service shutdown.
  Pending data is removed before each write, so a restart does not re-count the same interval.
- Local-date attribution splits intervals at local midnight and at month boundaries. A wall-clock jump is attributed to
  the end date instead of inventing listening on other dates.
- Played coverage is stored as merged millisecond ranges per chapter. Coverage is clipped to known chapter durations,
  replay does not duplicate coverage, and seeking does not create coverage for skipped media.
- A book is awarded at 95% coverage of its known total duration. Zero or unknown duration never awards. Awarded book IDs
  are durable and relative to the published app's data, so deleting a book or its history cannot revoke a level.
- Settings and Profile now use the same five-books-per-level calculation and the same durable stats aggregate. The
  Settings bar shows the current fraction of the five-book level.
- Legacy session durations seed the lifetime-hours total once. They cannot provide coverage or date attribution, so
  legacy levels inferred from saved positions may be corrected. Books, chapters, bookmarks, playback positions, covers,
  folder grants, profile data and sleep preferences are not migrated or deleted.

### Rate Raven and the monthly reminder

- Added **Rate Raven** under Settings → About. It opens
  `market://details?id=io.github.emeraldrealm.raven` and falls back to
  `https://play.google.com/store/apps/details?id=io.github.emeraldrealm.raven`. The published package is targeted even
  from the debug build. An unavailable handler produces a Settings snackbar rather than a crash.
- Removed the old star-score dialog, star-based email routing, installation-time gate and lifetime prompt flag. No
  In-App Review API is called by either the manual button or the reminder.
- Play builds may show one neutral reminder with **Rate Raven**, **Not now**, and **I've already rated**:
  - only on local dates 8–14 inclusive;
  - only with at least 3,600,000 ms of genuine listening attributed to that local month's 8–14 window;
  - at most once per calendar month, claimed before display so Back/outside dismissal also consumes the month;
  - only while the app is foreground/resumed and playback is paused or stopped;
  - only while the BookOverview screen is active, so it does not open over Settings, the player or background playback;
  - never through notifications, alarms or service UI.
- **I've already rated** persists a permanent local suppression flag. Opening the store does not suppress future
  reminders. The month key is recorded on first display, including dismissal. A missed window is not carried forward.
- The reminder is behind `RatingReminderEnabledFeatureFlag`, key `rating_reminder_enabled`, default `true` in Play builds.
  GitHub builds use the noop feature and do not auto-present.

### Persistence, build and CI

- `ListeningStats` and `RatingReminderState` are DataStore-backed (`listeningStats`, `ratingReminderState`). This avoids
  a Room schema migration. Missing fields deserialize from defaults, so existing installs upgrade without data loss.
- Release signing validation moved from variant configuration to `package*Release` / `bundle*Release`. Debug builds work
  without secrets; release packaging fails before producing an artifact.
- Added `.github/workflows/android.yml`: unit tests, both debug APK flavors, and APK artifact upload. It needs no release
  secrets. See “CI status” for what was actually executed here.
- Updated `docs/development.md` with APK, signing, versioning, listening and reminder behavior, and removed the stale
  upstream Voice release/deploy workflow links.

## Test evidence

### Targeted tests

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew \
  :features:settings:testDebugUnitTest \
  :features:review:play:testDebugUnitTest \
  :core:playback:testAndroidHostTest \
  --tests 'voice.core.playback.playstate.ListeningTrackerTest'
```

Passed, including:

- playback speed changes coverage but not elapsed time;
- paused time is not credited;
- a seek gap is not added to played coverage;
- releasing the tracker stops future measurement;
- dates 7/8/14/15, 59m59s versus 60m, month rollover and one-claim-per-month;
- permanent self-declared rating suppression;
- disabled flag and missed-window behavior;
- 95% completion, seek-to-end, replay deduplication, zero/unknown duration;
- midnight and month-boundary attribution, timezone, and clock changes;
- levels at 0, 4, 5, 6 and 10 books with Settings/Profile parity;
- Rate Raven success and unavailable-store snackbar.

### Full host unit suite

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew voiceUnitTest \
  -x :features:cover:testDebugUnitTest --console=plain
```

`BUILD SUCCESSFUL`; 203 tests in 43 result files passed with no failures or skips after the final source commit.

`features:cover` has one pre-existing live-network test failure unrelated to this work:

```text
CoverApiTest > test FAILED
org.opentest4j.AssertionFailedError: Expecting actual not to be null
CoverApiTest.kt:49
```

That file is unchanged since base commit `d2fecdb`; it depends on a DuckDuckGo token lookup and is excluded above so the
remaining suite can run deterministically.

Release-signing guard verified after the final changes:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assemblePlayRelease --console=plain
```

Fails at `:app:packagePlayRelease` with:

```text
Release builds require signing. Create an ignored keystore.properties file at the repo root.
```

No release APK/AAB was produced. No credentials were invented and nothing was uploaded to Play.

### Emulator validation

Device: `Palace_Verification` AVD, Android 16 / API 36, 1080×2424, Play Store absent. Final Play debug APK installed
with `adb install -r`; package `io.github.emeraldrealm.raven.debug`, version `1.7-debug` (code 8), target SDK 36.

Observed during this handoff:

- Imported an `.m4a` audiobook and played it. DataStore gained `lifetimeListenedMs`, daily listening entries, and an
  awarded book after genuine coverage.
- Settings profile showed `Lvl 0`, `Books Read 1`, bar `1/5`, and `1 of 5 books to the next level`; Profile showed the
  same level/progress calculation.
- With injected qualifying stats, the reminder appeared on 2026-10-10 and again on 2026-11-10 after resetting the
  month. The month key persisted immediately, and no dialog reappeared after a process restart.
- **I've already rated** set `selfDeclaredRated: true`; no dialog appeared in 2026-12 after resetting the month key.
- Paused/stopped foreground gating and BookOverview scoping behaved as intended; background playback did not surface UI.
- **Rate Raven** opened the HTTPS Chrome fallback on this Play-less emulator.
- Final APK smoke test after commit: app launched without a fatal exception, Settings opened, and About showed
  **Rate Raven** above Terms of Service, Privacy Policy and Open-Source License. Settings showed `0 of 5 books to the
  next level`.

The emulator clock and automatic time were restored to the current date and `Africa/Lagos` after validation.

## Known limits

- No Google Play Store is installed on the emulator, so only the HTTPS fallback was exercised end to end. The
  `market://` intent is implemented but could not be physically handed to the Play Store here.
- The monthly window was exercised with emulator clock changes and injected DataStore values through the debug shell.
  No debug-only bypass ships in the APK.
- The automatic reminder is scoped to BookOverview and paused/stopped foreground playback. It does not globally inspect
  every transient modal dialog that BookOverview itself could show; there is no onboarding or folder-permission flow on
  that screen.
- No upgrade-over-published-app test was possible without the Raven upload key. Debug builds have a `.debug` suffix and
  an Android debug certificate.
- Google Play credentials, the current highest uploaded version code, and the release keystore are unavailable.

## Signed release prerequisites

To produce release artifacts, provide the existing Raven upload key through an ignored root `keystore.properties`:

```properties
storeFile=/absolute/path/to/upload-keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Then choose a version code greater than the highest code already uploaded to Play and build:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assemblePlayRelease :app:bundlePlayRelease \
  -Pvoice.versionName=<next-version> \
  -Pvoice.versionCode=<greater-than-highest-uploaded-code>
```

Verify with `apksigner verify --print-certs` and `bundletool`/Play Console validation, upload to an internal track, and
promote manually. This handoff did not publish or promote anything.

## CI status

Workflow: `.github/workflows/android.yml`, triggered by pushes to `KMP` or `android-apk-handoff`, pull requests, and
manual dispatch. It runs `./gradlew voiceUnitTest -x :features:cover:testDebugUnitTest`, assembles both debug flavors,
and uploads `raven-debug-apks`. The known live-network `CoverApiTest` is excluded so the job is deterministic.

CI run [40185653955](https://github.com/Emerald-Realm/Vultr/actions/runs/40185653955) passed all steps on commit
`9a0a2c3`: setup, unit tests, Play debug APK, GitHub debug APK, and artifact upload. Two earlier infrastructure failures
were fixed in the workflow: the live-network cover test and the removed Android `tools` SDK package.
