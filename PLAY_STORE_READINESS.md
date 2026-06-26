# Raven — Play Store Readiness Audit

_Generated 2026-06-17. Read-only audit of the current `main` branch. No code was changed to produce this._

This document lists everything to resolve before submitting **Raven** (`io.github.emeraldrealm.raven`) to Google Play and to make it cleanly separate from the upstream **Voice** fork (PaulWoitaschek/Voice, GPLv3).

Items are grouped by severity. **🔴 Blocker** = will get rejected or is legally required. **🟠 Important** = should fix before launch. **🟡 Polish** = improves quality / can be a fast-follow.

---

## 1. Legal & Licensing (GPLv3) — 🔴

Raven is a fork of Voice, which is licensed **GNU GPL v3** (`LICENSE` at repo root is verbatim GPLv3). This is allowed on Google Play, but **non-negotiable obligations** come with it:

- [ ] **Keep the original copyright & license.** You may rebrand the product to "Raven," but you **cannot remove** the upstream copyright notices or the GPLv3 license from the source. Removing attribution to the original author is a license violation.
- [ ] **Publish your modified source.** GPLv3 requires that users of the distributed binary can obtain the corresponding source for *that exact version*. Your repo (`github.com/Emerald-Realm/Vultr`) must be **public** and contain the released code, or you must provide a written offer for source.
- [ ] **Add attribution in-app.** There is currently **no open-source-licenses / attribution screen** in the app (Settings → About only has Website / Issues / Contribute links). Add an "Open source licenses" entry that credits the original *Voice* project + GPLv3 and lists third-party libraries. This protects you legally and is expected for a GPL app.
- [ ] **Confirm you may use the name "Raven".** Generic, but there are existing apps/products named "Raven" on Play — do a quick trademark/listing search to avoid a name-collision takedown.
- [ ] **Decide on the repo name.** Product is "Raven" but the repo/folder is "Vultr" and links point to `Emerald-Realm/Vultr`. Not a blocker, but pick one identity and make in-app links resolve.

---

## 2. Branding / Fork-Separation Leftovers — 🟠

The in-app rebrand is solid (app name `Raven`, applicationId `io.github.emeraldrealm.raven`, launcher + notification raven icons, no user-facing "Voice" strings). Remaining upstream traces:

- [ ] **🔴 Store listing still references Voice.** `fastlane/metadata/android/en-US/full_description.txt` ends with:
  > "report them on our GitHub page: **https://github.com/PaulWoitaschek/Voice/issues**"
  This is the upstream issue tracker, shown publicly on your store page. **All 16 locale `full_description.txt` files** still contain "Voice"/"Woitaschek". Fix en-US first; translate or delete the others (don't ship machine descriptions that still say "Voice").
- [ ] **Store screenshots / feature graphic likely upstream.** `fastlane/metadata/android/en-US/images/` (icon.png, featureGraphic.jpeg, phoneScreenshots 1–4, sevenInchScreenshots) — verify these are **Raven** screenshots, not Voice's. If they show the old UI they must be replaced (Play also requires the new dark/light UI you built).
- [ ] **README still has Voice/Weblate badges.** `README.md` references `hosted.weblate.org/engage/voice/` and `weblate.org/widgets/voice/`. Cosmetic, but update if the public repo is your store's source-of-truth.
- [ ] **Internal package namespace is still `voice.*`** (e.g. `voice.app`, `voice.core.*`). This is **fine** — it's invisible to users and the `applicationId` is correct. Renaming is a large, risky refactor; only do it if you specifically want zero "voice" in the codebase. Not required for submission.
- [ ] **Upstream issue-tracker links in code comments** (`VoiceMediaNotificationProvider.kt`, `ImageFileProvider.kt`) — harmless, leave them.

---

## 3. Firebase / Analytics / Data Safety — 🔴

How it works today (`app/build.gradle.kts` `includeProprietaryLibraries()`): Firebase **Analytics, Crashlytics, Remote Config** and the Play **in-app review** library are compiled in **only if** `voice.includeProprietaryLibraries=true` **AND** `app/google-services.json` exists. **There is no `google-services.json` in the repo**, so the current build ships with **NO analytics, NO crashlytics, NO Firebase**. (`gradle.properties` sets the flag `true`, but the missing JSON disables it.)

You must consciously choose one path:

- **Path A — Ship with no telemetry (simplest):**
  - [ ] Leave it as-is (no `google-services.json`).
  - [ ] In the Play **Data safety** form, declare **"No data collected / No data shared."**
  - [ ] Verify the **onboarding analytics-consent screen** (strings `onboarding_analytics_consent_*` exist) does not appear or mislead when analytics are off.
- **Path B — Ship with your own telemetry:**
  - [ ] Create **your own Firebase project**, download **your** `app/google-services.json` (must NOT be Voice's project — that would send user data to the upstream author).
  - [ ] Fill **Data safety** to declare crash data / analytics collection accurately.
  - [ ] Ensure the consent screen + privacy policy match what you collect.

Either way:
- [ ] **🔴 Privacy policy URL must be live.** Settings links to `https://raven.audiobook.app/privacy` (`SettingsViewModel.kt:130`). That domain/page must exist and be reachable **before** submission — Play requires a working privacy-policy URL, and it's mandatory here because the app requests `INTERNET` and (optionally) analytics.
- [ ] `AD_ID` permission is correctly **removed** (`tools:node="remove"` in the manifest) — keep it that way; declare "no advertising ID" in Data safety.

---

## 4. Signing & Release Config — 🟠

- [ ] **Verify the Play upload/signing key is YOURS, not Voice's.** `signing/play/signing.keystore` + `signing.properties` exist. The `github` flavor was repointed to a Raven keystore earlier, but **confirm `signing/play/` is a key you generated** (you cannot sign with the upstream developer's key). Recommended: enroll in **Play App Signing** and treat `signing/play` as your *upload* key.
- [ ] **`fastlane/Appfile` references `app/play_service_account.json`** (Play Developer API key) which is **not in the repo**. Needed only if you publish via Fastlane; not needed for a manual Console upload. Add it (and keep it out of git) if automating.
- [ ] **`versionCode`/`versionName`** default to `1` / `1.0.0` (`app/build.gradle.kts:50-51`, overridable via `voice.versionCode`/`voice.versionName` gradle props). Fine for first release — just confirm the value you upload.
- [ ] **Build the release artifact you'll actually upload:** `./gradlew :app:bundlePlayRelease` (an **.aab**, not the debug APK we've been testing). Confirm it's **minified + resource-shrunk** (already `isMinifyEnabled/isShrinkResources = true` for release) and that R8/proguard (`app/proguard.pro`) doesn't strip anything needed (media3, serialization, Metro). Smoke-test the release build on a device.
- [ ] `lint { checkReleaseBuilds = false }` — consider running `./gradlew :app:lintPlayRelease` once manually to catch release-only issues before submitting.

---

## 5. Target API / Permissions / Compliance — 🟠

- [x] `targetSdk = 36`, `compileSdk = 36`, `minSdk = 28` — **exceeds** Play's current API-35 minimum. Good.
- [ ] **Justify each permission** in the Console where prompted:
  - `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK` — media playback (fine, but Play asks for FGS justification).
  - `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` — **Play scrutinizes this.** Be ready to justify (uninterrupted long-form playback) or you may be asked to remove it.
  - `INTERNET`, `ACCESS_NETWORK_STATE`, `WAKE_LOCK` — standard; INTERNET is used for online cover search → mention in privacy policy.
- [ ] **Android Auto / Automotive is declared** (`automotive_app_desc`, `com.google.android.gms.car.*` meta-data, exported `PlaybackService`). If you keep it, Play requires a **separate "Cars" declaration & review**; if you don't want that review burden for v1, consider removing the Auto metadata. Decide intentionally.
- [ ] **Content rating questionnaire** — complete it (audiobook player → almost certainly "Everyone").
- [ ] **Exported components** (`MainActivity`, `PlaybackService`, widget receiver) have `exported="true"` legitimately (launcher / media browse / appwidget). No action, just be aware reviewers check these.

---

## 6. Store Listing Assets To Provide — 🟠

- [ ] **App title** (≤30 chars): "Raven Offline Audiobook Player" is **31 chars** — trim to ≤30 (e.g. "Raven: Offline Audiobooks").
- [ ] **Short description** (≤80 chars) — present, verify length.
- [ ] **Full description** — rewrite ending to remove the Voice link (see §2).
- [ ] **Feature graphic** 1024×500, **app icon** 512×512, **min 2 phone screenshots** — provide **Raven** assets (light + dark showcase the work you did).
- [ ] **Release notes / changelog** — `fastlane/.../en-US/changelogs/` is empty. Add release notes (or type them in the Console).
- [ ] **Category**: "Music & Audio" (manifest `appCategory="audio"` already set).

---

## 7. Functional Verification Still Open — 🟠

- [ ] **Notification chapter prev/next buttons** (just implemented) — install and confirm on a **real device / Android 13+** that the two side controls render as previous/next-track icons and actually jump chapters. Emulator screenshot pending.
- [ ] **Chapter length in notification** — by your decision, single-file books intentionally show whole-book length (multi-file already shows chapter length). Make sure the store description doesn't promise per-chapter notification scrubbing.
- [ ] **Full release-build run-through** on a physical device: import a folder, play, background/notification controls, sleep timer, bookmarks, widget, search, cover download (network), dark + light themes.
- [ ] **Run the test suite**: `./gradlew voiceUnitTest` — confirm green on the submission commit (not verified in this audit).
- [ ] **Different book shapes**: single-file `.m4b` with embedded chapters, multi-file folder, single MP3, author-mode folder — all import and play.

---

## 8. Code Cleanup (Polish) — 🟡

Found during the audit; none block submission but worth a pass:

- [ ] **Dead screens still registered as nav destinations:**
  - `features/bookmark/.../BookmarkScreen.kt` — legacy full-screen bookmarks (with a FAB) registered for `Destination.Bookmarks`, but the player now uses `BookmarksBottomSheet`. `Destination.Bookmarks` is never navigated to → dead UI. Remove or re-wire.
  - `features/folderPicker/.../selectType/AddingFab.kt` — only self-referenced; `SelectFolderType` uses its own bottom-bar button now. Dead.
- [ ] These dead files still import Material Icons / use `ExtendedFloatingActionButton`; removing them also removes the last non-mage FABs.

---

## Quick Pre-Submit Checklist (the truly mandatory ones)

1. 🔴 Public source repo + retain GPLv3 & upstream copyright; add in-app "Open source licenses" attribution.
2. 🔴 Live privacy-policy URL at the address the app links to.
3. 🔴 Decide telemetry (Path A no-data, or Path B your own `google-services.json`) and fill **Data safety** to match.
4. 🔴 Remove the upstream `PaulWoitaschek/Voice` link from the en-US store description; fix/localize others.
5. 🔴 Confirm `signing/play` is **your** key (or use Play App Signing).
6. 🟠 Provide Raven screenshots / feature graphic / icon; trim title to ≤30 chars.
7. 🟠 Build & smoke-test `:app:bundlePlayRelease` (.aab), run `voiceUnitTest`.
8. 🟠 Decide on Android Auto (keep + extra review, or drop for v1).
