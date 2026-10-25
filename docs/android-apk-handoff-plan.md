# Raven: Android APK implementation handoff

Prepared 25 October 2026. This is a plan for a new implementation agent; no application changes or builds were completed while preparing it.

## Outcome and confirmed requirements

Deliver an installable Android APK from the KMP branch, with corrected experience progress and a restrained monthly rating reminder. Keep the KMP structure; iOS implementation comes later and must not delay Android delivery.

The user confirmed:

- Keep the settings experience/level bar and correct its calculation.
- Add “Rate Raven” under Settings → About, opening Google Play for Android.
- In each month's second week, defined as local calendar dates 8–14 inclusive, remind an eligible user after one hour of actual audiobook listening accumulated within those dates.
- Show the automatic reminder at most once per month, and stop reminders for someone who says they have already rated.

This handoff does not request a new app redesign, iOS implementation, or a production store rollout. Produce artifacts and validation evidence. Prepare signed release artifacts if the existing credentials are available; lack of release signing must not prevent delivering a debug APK.

## Starting point

Repository: https://github.com/Emerald-Realm/Vultr

Working directory: the repository root of your local checkout

Audit: [KMP status audit](kmp-status-audit.md)

Audited KMP head: `d1afd54e31c3a40c29a7f6af624160108dae0f03`. Main head: `688d7d38e3ff80cbdea28c116b3c0f1af70df667`. KMP was seven commits ahead and zero behind main. Recheck remote refs when implementation begins and review any new changes. Inspect local status and create an implementation branch from the intended KMP head before editing. Read AGENTS.md and any deeper instructions.

Application ID: `io.github.emeraldrealm.raven`. Current default version: `1.7`, versionCode `8`. Preserve the application ID. Fifteen core modules use KMP, but Android is the only target. Keep Android APIs out of commonMain and keep platform UI/store integration in Android sources.

The earlier audit did not validate compilation. Its Gradle dry-run was stopped during distribution download. Do not describe that attempt or historical commit messages as a passing build.

## Phase 1 — Establish a working Android build

1. Inspect the pinned configuration: Gradle wrapper 9.5.0, AGP 9.2.1, Kotlin 2.3.21, JVM toolchain 25, Android compile/target SDK 36, minimum SDK 28. Use the project's versions initially; do not upgrade or downgrade dependencies speculatively.
2. Configure the actual SDK path through ignored local.properties or environment configuration and install missing build prerequisites. Homebrew JDK 17 exists on the audited machine as a bootstrap runtime; it is not the requested JDK 25 toolchain. Do not run the memory bootstrap script blindly because it changes global Gradle settings.
3. Reproduce the signing configuration issue in app/build.gradle.kts. Its onVariants callback throws for unsigned release variants during project configuration, potentially blocking debug tasks too. Make debug configuration/builds work without release credentials. Keep clear signing validation on actual release packaging tasks. Test both missing-signing debug success and missing-signing release failure without creating fake production credentials.
4. Build `./gradlew :app:assemblePlayDebug --console=plain` before feature edits and record the outcome. Fix genuine migration/build failures encountered. Also compile the github flavor to preserve its independent dependency graph.

Exit: an installable baseline debug APK, or an exact reproducible toolchain/compiler failure with logs. Continue through solvable failures; do not stop at a dry-run.

## Phase 2 — Make listening measurements truthful

Current issues to investigate:

- ProfileViewModel derives Hours Listened from the sum of current book positions. Seeking ahead inflates it; rewinding reduces it.
- Experience is `completedBooks / 5` for level and `completedBooks % 5` for progress. Completion is inferred from the current position being within five seconds of the end, so seeking can award completion.
- PlaybackHistoryRecorder uses wall-clock timestamps for elapsed listening and writes on selected playback events. A simple query for events created during dates 8–14 will misattribute a session crossing midnight and can miss an ongoing session.

Use one reliable playback measurement source for profile time and monthly eligibility. Measure active playback with a monotonic elapsed-time source on Android. Do not count paused, buffering, idle, seeking, or app-open time; background playback counts. Playback speed does not multiply real listening minutes. Capture the affected book/session before asynchronous writes so a book switch cannot attribute the old session to the new book. Prevent overlapping intervals, duplicate event callbacks, and checkpoint/restart double counting. Periodically checkpoint long sessions, and flush on pause, stop, sleep timer and orderly shutdown. Bound the loss from abrupt process death instead of inventing time after restart.

Split measurement intervals at local date boundaries for monthly eligibility. Keep persisted month keys and date attribution stable; use the device's local zone, inject clock/zone dependencies for tests, and do not reinterpret already-accounted time after travel. Reject negative intervals and avoid counting clock changes as listening.

For profile history, reuse trustworthy existing listenedMs data where possible; do not backfill listening time from book positions. Document any historical measurement limitation. Prefer a small durable aggregate for lifetime counters so deleting a book or history row does not erase earned progress or reminder history.

## Phase 3 — Correct the experience bar

Keep the existing five-books-per-level model as the planning default, preserving the product's current rule rather than inventing an XP economy. The user confirmed keeping/correcting the bar, but did not specify a new leveling formula.

Make earned completion depend on actual played content, not a seek-to-end shortcut. Recommended implementation: persist merged listened media ranges per stable book/chapter identity and award a distinct book once its unique played coverage reaches 95% of a positive known duration. Treat 95% as an explicit implementation assumption to document, not a user-specified number. A seek must not fill the skipped range; replay must not duplicate covered ranges. Track elapsed real time separately from media coverage, since playback speed changes the two differently. Awarded book IDs must survive rescans where identity is preserved and must not receive duplicate awards on reopen or replay.

Use one shared calculation for the settings header and profile screen:

```text
earnedBooks = number of distinct legitimately awarded completions
level = earnedBooks / 5 (integer division)
booksIntoLevel = earnedBooks % 5
barFraction = clamp(booksIntoLevel / 5.0, 0.0, 1.0)
```

At five completions, advance the level and show 0/5 toward the next level. Label the bar clearly, for example “2 of 5 books to the next level”; keep the experience treatment if desired, but don't imply that it measures listening hours. Empty libraries and unknown/zero durations must render safely. Keep profile name/photo editing and the existing light/dark appearance.

Existing installations have no reliable played-coverage history. Do not fabricate it from saved position. Start verifiable coverage tracking with this upgrade and explain in release notes that previously inferred levels may be corrected. Preserve books, bookmarks and playback positions. If a simpler completion implementation is chosen, it must still pass the seek/replay/deduplication acceptance cases and the handoff must explain its limitations.

Primary files: features/settings/.../profile/ProfileViewModel.kt, profile/ProfileScreen.kt, views/Settings.kt; core/playback/.../playstate/PlaybackHistoryRecorder.kt; data API/implementation repositories and persistence. Add proper Room migrations if schema changes are needed; never use destructive migration for this update.

## Phase 4 — Add a dependable rating entry

Add localized “Rate Raven” under Settings → About. Route the action through SettingsListener/ViewModel and an Android store-link handler, rather than hardcoding Android intents into shared code.

Open the Play listing with `market://details?id=io.github.emeraldrealm.raven`; if the Play app is unavailable, use `https://play.google.com/store/apps/details?id=io.github.emeraldrealm.raven`. Handle an unavailable URL handler gracefully. Always target the published release ID, including from a debug build with an application ID suffix.

Explicit rating buttons should open the store listing, not call the quota-limited In-App Review API. Google explains that API calls may show nothing, making a button appear broken: https://developer.android.com/guide/playcore/in-app-review#quotas

Keep this manual About action available regardless of automatic reminder eligibility. For github builds, allow the manual web/Play link but retain no automatic Play prompt by default.

## Phase 5 — Replace the old review eligibility/flow

Existing review files are in features/review/play; app/MainActivity.kt invokes ReviewFeature. ShouldShowReviewDialog currently requires two days since installation, five hours inferred from book positions, not playing, and a lifetime reviewDialogShown Boolean. ReviewEnabledFeatureFlag defaults to false, and KMP removed Firebase remote configuration. Replace those obsolete gates rather than leaving the new reminder disabled behind them.

The current ReviewFeature collects a custom star score, sends only five-star responses to Google Play, and sends lower scores to an upstream email address. Remove that rating-based routing from this flow. Feedback/report-issue actions can remain separate from rating. Do not automatically email anyone.

Implement a neutral in-app reminder offering “Rate Raven”, “Not now”, and “I've already rated”. “Rate Raven” opens the same listing as About. No preselected score, rewards, sentiment question, or star filtering. This plan chooses a store-link reminder instead of layering a custom prompt over Google's native review card.

Google's API does not report whether a review was submitted or even whether its dialog appeared. Store launch/return is also not proof of a rating. Persist a user-declared suppression flag for “I've already rated”; don't name it verifiedRated. Source: https://developer.android.com/guide/playcore/in-app-review/kotlin-java#launch-review-flow

Eligibility must require all of:

- Android Play flavor, local feature enabled, and no self-declared prior rating.
- Local date is 8th through 14th inclusive.
- At least 3,600,000 ms of genuine active listening attributed to those dates in the current year/month.
- No reminder already presented in this calendar month.
- App foreground/resumed, no competing dialog/onboarding/folder permission flow, and a safe moment when playback is paused or stopped.

If the hour is reached during background playback, wait for the next safe foreground moment within the same window. Do not interrupt playback, launch UI from the service, post notifications, request notification permission, use exact alarms, or wake a closed app. If no safe moment occurs by the end of the 14th, skip that month's prompt. Do not carry an unearned hour or a missed prompt into another month.

Persist at least the current window's month key/listened time, durable reminder history or an equivalent rollback-safe record, and self-declared rating suppression. Record the month on first display, including Back/outside dismissals, so “Not now” cannot repeat that month. Serialize presentation/state updates to prevent simultaneous collectors displaying twice. Reevaluate eligibility on resume, relevant playback/measurement state changes, and local date transitions; a one-time LaunchedEffect(Unit) check is insufficient.

Opening the listing does not permanently suppress reminders. “I've already rated” does, including across app updates. The legacy reviewDialogShown Boolean only means an old prompt was handled; it is not evidence of an actual review and must not become the new rated flag. Persist all state locally; no accounts or tracking service are required.

## Phase 6 — Tests and device validation

Write focused behavior tests with fake clocks, playback events, and in-memory stores. Follow AGENTS.md for Compose state tests. Prove these cases:

| Area | Required checks |
| --- | --- |
| Listening | Paused/buffering/seek time excluded; background included; speed doesn't inflate elapsed time; checkpoint/restart doesn't double count |
| Experience | Zero/unknown duration safe; seek to end earns nothing; replay doesn't duplicate coverage/awards; levels correct at 0, 4, 5, 6 and 10 books; settings/profile agree |
| Monthly window | Dates 7/8/14/15; 59m59s vs 60m; month/year rollover; midnight-crossing session; timezone/clock changes |
| Reminder | Once monthly across dismissal, rotation, process restart and upgrade; disabled after self-declared rating; defer while background/playing; skip missed window |
| Store | Manual action works outside the reminder window; published package used in debug; Play app absent fallback; no false rated inference |
| Migration | Preserve existing library, bookmarks, covers, folder grants, playback positions, profile data, sleep preferences and history |

Run targeted affected-module tests, `./gradlew voiceUnitTest`, and the applicable formatting/lint checks. Fix new failures; distinguish pre-existing failures with baseline evidence. Inspect the screen on an emulator/device in light/dark themes and large text. Install the APK and verify import, background playback, lock-screen controls, resume after process restart, seeking, sleep timer, and widget actions. Use debug-only injected clocks/time data to exercise the monthly window without one-hour waits or system clock changes; do not ship a production bypass.

Test upgrade behavior using a suitably signed baseline and candidate with the same application ID/certificate. Debug builds have a suffix and cannot establish compatibility with the published app. Without the original signing material, use an equivalent same-key test pair and explicitly state the limit.

## Phase 7 — Package and hand off

Required first artifact:

```sh
./gradlew :app:assemblePlayDebug --console=plain
```

Expected path: app/build/outputs/apk/play/debug/app-play-debug.apk. Verify the actual output path, install it, and report its absolute path, SHA-256, source commit, application ID, version, and test results. A debug APK is for testing, not a production Play update.

When existing signing credentials are available, also produce:

```sh
./gradlew :app:assemblePlayRelease :app:bundlePlayRelease \
  -Pvoice.versionName=<next-version> \
  -Pvoice.versionCode=<greater-than-highest-uploaded-code>
```

Verify the release APK signature and AAB structure using appropriate Android tooling. Preserve the existing upload key. Never commit credentials, invent a replacement production key, or pick a version code based only on the repository default. If credentials/Play version are unavailable, finish the debug APK and document exactly what is needed for signed release artifacts. Do not call an unsigned artifact release-ready.

Add a reproducible Android CI job for tests and debug packaging that requires no release secrets, with APK artifact upload. Update Raven's build instructions to remove stale upstream release workflow links and document the new measurement/reminder behavior. Keep release publishing separate; don't upload or promote to Play as part of this handoff without a subsequent request.

## Definition of done

- An APK built from KMP is installed and its core Android playback flow is checked.
- Experience progress uses truthful, deduplicated listening/completion evidence and agrees across screens.
- Rate Raven exists under About and has a working fallback.
- The monthly reminder honors the confirmed one-hour, dates 8–14, once-monthly rule and local suppression.
- Relevant tests, persistence migrations, and upgrade checks pass, with any external limits explicitly recorded.
- The final handoff provides artifact paths, hashes, commit, test evidence and remaining release-signing requirements.
- iOS remains deferred; Android delivery does not wait for it.

## Starter prompt for the implementation agent

Implement this plan in Emerald-Realm/Vultr starting from the current KMP branch. Read AGENTS.md and the attached audit first. Deliver a tested installable Android APK, fix the retained experience bar using genuine listening/completion data, add Rate Raven under About, and implement the confirmed monthly reminder after one hour of listening during local dates 8–14, at most once per month. Follow the detailed persistence, store-link and test requirements above. Preserve existing user data and the KMP structure. Defer iOS. Produce signed APK/AAB artifacts if the existing credentials are available; otherwise complete and deliver the debug APK with exact release prerequisites. Do not stop at a plan or a Gradle dry-run, and do not publish to Google Play.
