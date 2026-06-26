# Raven on Apple / Desktop — Kotlin Multiplatform Gameplan

> Status: **planning only**. No code has been changed. This document is the proposal/roadmap for bringing Raven to **macOS first** (Compose Desktop), then iOS/iPadOS, with as close to full parity as each platform allows.
>
> **Read §0 (macOS-first) before the iOS sections** — Mac is the recommended first target and the stepping stone to iOS.

---

## 0. macOS first (recommended starting point)

**Can we ship a Mac app without iOS? Yes — and it's the smart first move.**

The Mac path is **Compose Multiplatform for Desktop (JVM)**: your existing Compose UI, rendered on the JVM, packaged as a native macOS `.app` / `.dmg`. It is **substantially less work than iOS**, and it builds the exact shared foundation iOS will later reuse.

### Why Mac first is the right call
- **It runs on the JVM, not Kotlin/Native.** Room, DataStore, Metro DI, coil3, coroutines, and your Compose screens run on desktop with minimal change. No Xcode, no Kotlin/Native toolchain.
- **No sandbox, full filesystem.** Desktop has direct file access via `java.nio`, and can even watch folders with `WatchService` — *easier* and more capable than Android's Storage Access Framework. No security-scoped bookmarks, no document-picker dance.
- **It forces the KMP + Compose-Multiplatform refactor iOS also needs.** You build `commonMain` (logic + data) and the CMP UI **once**, add a `desktopMain` (JVM) target now, and later iOS adds `iosMain`. Mac de-risks iOS for free.
- **Distribution sidesteps the GPLv3 blocker.** A notarized **Developer-ID `.dmg`** (or a plain GitHub release) ships *outside* the Mac App Store, so the GPLv3/App-Store licensing conflict (§10) **does not apply**. This makes Mac the safest first platform to ship.

### What's genuinely Mac-specific to build
| Concern | Desktop (JVM) implementation | Effort |
| --- | --- | --- |
| **Audio playback** (replaces media3) | **VLCJ** (libVLC bindings) — plays m4b/mp3/opus/flac/wav, variable speed, seek; chapters via manual boundary handling. Bundle libVLC. Alternatives: GStreamer, mpv/JNI. | 🔴 Main task |
| Metadata / cover / chapters | **JAudioTagger** (tags + embedded art) + **mp4parser** (m4b chapters) | 🟡 Moderate |
| File access / scanning | `java.nio` + `WatchService` — full folder access, optional live watching | 🟢 Easy |
| Window / menu bar / tray | Compose Desktop `Window`, `MenuBar`, system tray | 🟢 Easy |
| **Now Playing / media keys** (Control Center, ▶︎⏸ keys) | macOS `MPNowPlayingInfoCenter` / `MPRemoteCommandCenter` via JNI or a small Swift/JNA helper | 🟡 Fiddly — good **fast-follow**, not a 1.0 blocker |
| Home-screen-style widgets | macOS widgets are **WidgetKit (Swift)** — out of scope; consider a **menu-bar mini-player** instead | 🟡 Optional |
| Packaging / distribution | **Conveyor** (Hydraulic) or Gradle **jpackage** → signed, notarized `.dmg`; ships via GitHub/website | 🟢 Easy |

Everything else (library, book details, settings, sleep timer, auto-rewind, bookmarks, history, speed, theming, search) is **shared `commonMain` code reused verbatim** — full parity.

### Honest caveats for desktop
- The app bundles a trimmed **JRE** (~40–80 MB) — normal for Compose Desktop; fine for desktop distribution.
- Compose Desktop renders with **Skia**, so it's *not* native AppKit chrome — it looks like Raven (consistent with Android), which suits an audiobook player. Accessibility (VoiceOver) support on desktop is more limited than native.
- **Mac App Store** distribution is *optional and later* — going that route re-introduces the GPLv3 tension + app sandboxing. Direct `.dmg` avoids both.
- A future bonus: once iOS exists, Apple-Silicon Macs can also run the iOS build via **Mac Catalyst / "iPad apps on Mac"** — but that is iOS-derived and separate from this Compose-Desktop app.

### macOS-first phased plan
- **Phase D0 — Spike (1 week):** render one existing Raven screen in a Compose Desktop window on your Mac; prototype VLCJ playing a local `.m4b` with speed + seek. De-risks the two unknowns.
- **Phase D1 — Shared core (3–5 weeks):** add KMP + CMP convention plugins to `build-logic`; convert `core:common`, `core:data:*` (Room KMP + DataStore MP), `core:search`, `core:sleeptimer`, `core:featureflag`, `navigation` to KMP with a `desktopMain` (JVM) target.
- **Phase D2 — UI (2–4 weeks):** move `core:ui` + `features:*` to Compose Multiplatform (coil→coil3, resources→`compose.resources`, navigation3→an MP nav lib). Library / Book Details / Settings / History rendering on macOS.
- **Phase D3 — Playback (3–4 weeks):** VLCJ-backed player behind the shared `PlayerController` contract; chapters, position persistence, sleep-timer + auto-rewind wired in.
- **Phase D4 — Files & metadata (1–2 weeks):** folder pick + scan (`java.nio`), JAudioTagger/mp4parser metadata, covers, chapters.
- **Phase D5 — Polish & ship (1–2 weeks):** window/menus, optional menu-bar mini-player, jpackage/Conveyor `.dmg`, notarization, GitHub release. (Now Playing/media-keys can land here or as a fast-follow.)

**Desktop 1.0 effort: ~4–8 weeks of focused work once Phase D1/D2 land** — dominated by the VLCJ audio engine. The shared core + CMP UI you build here is **directly reused** when you add iOS afterward.

### Then iOS reuses everything below
After macOS ships, adding iOS means: keep `commonMain` + the CMP UI as-is, and add only the `iosMain` platform seams — the **AVFoundation player (§6)** and the **file/import model (§7)**. The rest of this document covers that iOS work.

---

## 1. TL;DR / Recommendation

**Yes, this is achievable, and your codebase is a good candidate.** Raven is already Jetpack Compose + cleanly separated Gradle modules + libraries that now have multiplatform versions (Room, DataStore, Metro DI, coroutines, serialization). That removes ~70% of the usual KMP migration pain.

**Recommended approach: Kotlin Multiplatform + Compose Multiplatform (CMP).**
- Share **business logic, data, and the entire UI** in `commonMain`.
- Re-implement only the **platform seams** (audio playback, file access, metadata extraction, background/lock-screen integration, widgets) in `androidMain` / `iosMain` behind shared interfaces (`expect`/`actual` or DI-provided implementations).
- Ship an iOS app from the same repo, built with Xcode, distributed via TestFlight → App Store.

**Honest expectation:** "Full parity" is realistic for the **core experience** (library, player, chapters, sleep timer, bookmarks, history, speed, offline). A handful of features are **platform-shaped** and will differ by necessity (folder import model, home-screen widgets, Android Auto ↔ CarPlay, Material You dynamic color, skip-silence/volume-boost DSP). Those are called out explicitly in §7.

**Rough effort:** a single experienced Kotlin+iOS engineer, **~3–6 months** to a polished, parity-level 1.0 — front-loaded by the playback engine and file-access rewrites. See §9.

**One thing to decide early (non-technical): GPLv3 + the App Store.** This is a known legal tension — see §10. It can block or complicate distribution and must be resolved before investing heavily.

---

## 2. Current architecture assessment

Modules (from `settings.gradle.kts`) grouped by how portable they are:

| Group | Modules | Portability |
| --- | --- | --- |
| **Pure logic / data** | `core:common`, `core:data:api`, `core:data:impl`, `core:search`, `core:scanner`*, `core:sleeptimer:*`, `core:featureflag`, `core:logging:api`, `navigation` | 🟢 Mostly portable |
| **UI** | `core:ui`, `core:strings`, all `features:*` (bookOverview, playbackScreen, settings, folderPicker, onboarding, cover, sleepTimer) | 🟡 Compose → Compose Multiplatform |
| **Android-only platform seams** | `core:playback` (media3), `core:documentfile` (SAF), `features:widget`, `core:analytics:firebase`, `core:logging:crashlytics`, `core:remoteconfig:firebase`, `features:review:play` | 🔴 Re-implement per platform |

Key dependencies and their KMP story:

| Dependency (today) | KMP path |
| --- | --- |
| Jetpack Compose + Material3 | **Compose Multiplatform** (JetBrains) — Material3 supported, iOS target stable |
| Room `2.8.4` | **Room KMP** (2.7+) with `androidx.sqlite` BundledSQLiteDriver — runs on iOS |
| DataStore `1.2.1` | **DataStore multiplatform** (okio-backed) — runs on iOS |
| Metro DI `1.0.0` | Metro is a Kotlin **compiler plugin** → multiplatform-capable |
| kotlinx-coroutines / serialization | Already multiplatform |
| coil-compose `2.7` | **coil3** (multiplatform image loading) |
| lottie-compose | **Compottie** (multiplatform Lottie) — only if onboarding still uses Lottie |
| androidx `navigation3` | Android-only → swap for a multiplatform nav (Compose Navigation MP, Decompose, or Voyager) |
| media3 / ExoPlayer | **Android-only** → AVFoundation on iOS (the big lift, §6) |
| okhttp / retrofit | Ktor if any networking is shared (Raven is offline-first, so minimal) |
| Firebase (analytics/crashlytics/remoteconfig) | Already shipped **disabled** (no-telemetry). Keep noop on iOS, or add a KMP crash reporter later |

**Takeaway:** the data layer, domain logic, and theming move to `commonMain` with relatively little friction. The UI moves with a moderate, mechanical migration (Compose → CMP APIs). The genuine engineering is the **playback engine** and **file access**.

---

## 3. Strategy options considered

| Option | What it means | Verdict |
| --- | --- | --- |
| **A. KMP + Compose Multiplatform** (recommended) | Share logic **and** UI; one Compose codebase renders on both | ✅ Best ROI given Raven is already Compose with clean modules; maximizes parity and shared maintenance |
| **B. KMP shared logic + native SwiftUI UI** | Share data/domain only; rebuild every screen in SwiftUI | More "native feel," but doubles UI work forever and throws away the Compose UI you just built |
| **C. Full native iOS rewrite (Swift/SwiftUI)** | Separate app, no sharing | Most platform-idiomatic, highest cost, two codebases to keep in lockstep — worst for a small team chasing parity |
| **D. Flutter / React Native rewrite** | Abandon Kotlin | Throws away the entire existing investment; not considered |

We go with **A**, with the explicit understanding that the **platform seams** (player, files, widgets, lock-screen) are written natively per-platform behind shared interfaces — that's normal KMP, not a compromise.

---

## 4. Target repository shape

Convert shared modules from Android-library to **Kotlin Multiplatform** modules with `androidMain` + `iosMain` (+ `commonMain`). High-level end state:

```
Vultr/
├─ app/                      # Android app (existing) — thin androidApp entry
├─ iosApp/                   # NEW: Xcode project (Swift entry point, AppDelegate, WidgetKit ext)
├─ shared/ or keep core:*    # KMP modules: commonMain + androidMain + iosMain
│   ├─ core:data (Room KMP, DataStore MP)
│   ├─ core:domain/common/search/scanner/sleeptimer
│   ├─ core:ui  (Compose Multiplatform)
│   ├─ core:player (expect/actual: ExoPlayer ⟷ AVPlayer)   # was core:playback
│   ├─ core:files  (expect/actual: SAF ⟷ security-scoped bookmarks)  # was documentfile
│   └─ features:*  (Compose Multiplatform screens)
└─ gradle/ build-logic/      # add KMP + CMP convention plugins
```

`expect`/`actual` (or DI bindings) define the contracts; each platform supplies its implementation. The Compose screens, view models, repositories, DataStores, and Room DB live once in `commonMain`.

---

## 5. The portability matrix (per concern)

| Concern | Android (today) | iOS implementation | Parity |
| --- | --- | --- | --- |
| UI rendering | Jetpack Compose | Compose Multiplatform | 🟢 Full |
| Navigation | navigation3 | MP nav (Decompose/Voyager/Compose-Nav-MP) | 🟢 Full (one-time swap) |
| DB | Room | Room KMP + BundledSQLite | 🟢 Full |
| Key-value prefs | DataStore | DataStore MP | 🟢 Full |
| DI | Metro | Metro (same) | 🟢 Full |
| Images / covers | coil2 | coil3 | 🟢 Full |
| Strings/i18n | Android resources | CMP resources (`compose.resources`) | 🟢 Full |
| **Audio playback** | media3 ExoPlayer | AVFoundation (AVPlayer/AVQueuePlayer) | 🟡 Mostly — see §6 |
| **Lock screen / controls** | MediaSession + notification | MPNowPlayingInfoCenter + MPRemoteCommandCenter | 🟢 Full equivalent |
| **Background audio** | Foreground service | AVAudioSession `.playback` + UIBackgroundModes audio | 🟢 Full |
| **File/folder import** | SAF tree URIs | UIDocumentPicker + security-scoped bookmarks | 🟡 Different model |
| **Metadata + chapters** | MediaMetadataRetriever | AVAsset metadata + native chapter groups | 🟢 Full (iOS m4b chapters are first-class) |
| Skip silence | ExoPlayer built-in | Custom DSP / accept gap | 🔴 Hard / deferred |
| Volume boost (gain) | media3 audio processor | AVAudioEngine / AVAudioMix | 🟡 Possible, more work |
| Sleep timer | shared coroutine logic | same (shared) | 🟢 Full |
| Auto-rewind / bookmarks / history / speed | shared logic | same (shared) | 🟢 Full |
| **Home-screen widgets** | App Widgets (Compose RemoteViews) | WidgetKit (Swift, App Intents) | 🟡 Reimplement in Swift |
| Car integration | Android Auto | CarPlay (audio entitlement) | 🟡 Separate, needs Apple approval |
| Dynamic color | Material You | static theme (no iOS system accent) | 🟡 Cosmetic difference |
| Crash/analytics | Firebase (disabled) | noop / KMP crash lib | 🟢 N/A (privacy stance) |

---

## 6. The hard part #1 — the playback engine

`core:playback` is built on **media3** (`VoicePlayer` extends `ForwardingPlayer`, `MediaLibrarySession`, per-chapter clipping, `setMediaButtonPreferences`, custom commands). None of this exists on iOS.

**Plan:** define a platform-agnostic player contract in `commonMain` (it largely exists already as `PlayerController`), then provide:

- **Android `actual`:** keep the current media3 implementation almost as-is.
- **iOS `actual`:** built on **AVFoundation**:
  - `AVQueuePlayer` / `AVPlayer` + `AVPlayerItem` per audio file.
  - **Background audio:** `AVAudioSession` category `.playback`, `UIBackgroundModes: [audio]` in `Info.plist`.
  - **Lock screen / Control Center / headphones:** `MPRemoteCommandCenter` (play/pause/skip/seek/changePlaybackRate) + `MPNowPlayingInfoCenter` (title, author, cover art, elapsed/duration).
  - **Interruptions / route changes** (calls, unplugged headphones): `AVAudioSession` interruption + route-change notifications → mirror your existing auto-rewind-on-resume behavior.
  - **Variable speed:** `AVPlayer.rate` with `audioTimePitchAlgorithm = .timeDomain` (preserves pitch).
  - **Chapters / multi-file books:** AVPlayer doesn't "clip" like ExoPlayer. Re-implement chapter boundaries with **boundary time observers** + seek logic. iOS has **native chapter metadata** (`AVAsset.chapterMetadataGroups`) which actually makes single-file `.m4b` chapters easier than on Android.
  - **Position persistence:** reuse the shared logic; feed positions from AVPlayer's periodic time observer.

**Parity risks here:**
- **Skip-silence**: ExoPlayer has it built in; iOS has no equivalent. Either ship a custom audio-tap DSP (significant) or mark it Android-only for v1.
- **Volume boost/gain**: doable via `AVAudioEngine`/`AVAudioMix` but more involved than media3's audio processor.
- **Gapless multi-file playback**: `AVQueuePlayer` handles queueing but seam-perfect gapless needs care.

This module is the **critical path** — prototype it first (§9 Phase 1) to de-risk the whole project.

---

## 7. The hard part #2 — file access & the library model

Android uses the **Storage Access Framework**: the user grants a persistent tree URI to a folder (SD card, internal storage), and Raven scans it. iOS is sandboxed and has **no equivalent of arbitrary persistent folder access**.

**iOS approach:**
- `UIDocumentPickerViewController` (folder mode) to let the user pick a directory.
- Persist access across launches with **security-scoped bookmarks** (`URL.bookmarkData` / `startAccessingSecurityScopedResource`).
- Enumerate with `FileManager`; coordinate reads with `NSFileCoordinator`.
- Support **import from Files app / iCloud Drive / "Open with Raven"**, and on-device app storage (copy-in model).

**Parity caveat — be upfront with users:** the Android "point at any folder, including SD card, and it stays watched" model becomes "import a folder/files; access is re-established via bookmarks." There is **no background folder-watching** on iOS — scanning is on demand (app launch / pull-to-refresh / file added). This is an OS constraint, not a Raven limitation, and should be reflected in onboarding copy.

**Metadata extraction** (currently MediaMetadataRetriever/embedded covers): on iOS use `AVAsset` async metadata loading (`load(.commonMetadata)`, artwork via `AVMetadataItem`), and native chapter groups. Wrap behind a shared `MetadataReader` interface.

---

## 8. Other platform seams

- **Widgets:** WidgetKit widgets are **SwiftUI only** — they cannot reuse Compose. Re-author the Audible-style widgets in Swift, sharing the current book/state via an **App Group** container (shared DataStore/SQLite file or a small shared snapshot). Playback control from widgets via **App Intents**.
- **Onboarding/permissions:** rework around iOS realities (no storage permission prompt; instead document-picker import flow). Lottie → Compottie if animations are kept.
- **Settings deep-links / website / GitHub:** `Destination.Website` already uses URLs — opens in Safari via `UIApplication.open`. Trivial.
- **Theming:** keep the existing `RavenColors`/design-token layer (it's already a custom token system, not reliant on Material You), so light/dark port cleanly. Material You dynamic color simply won't have an iOS source — fall back to the brand palette.
- **Analytics/crash:** stays noop (privacy stance). Optionally add a KMP-friendly, privacy-respecting crash reporter later.

---

## 9. Phased plan (milestones)

**Phase 0 — Spike & decision (1–2 weeks)**
- Resolve the **GPLv3/App Store** question (§10). *Gate: don't proceed until cleared.*
- Stand up a throwaway KMP+CMP module; render one existing Raven screen on the iOS simulator.
- Prototype the **iOS AVPlayer** path: play a local `.m4b`, background audio, lock-screen controls, speed change. *This de-risks the whole project.*

**Phase 1 — Foundation (3–5 weeks)**
- Add KMP + Compose Multiplatform convention plugins to `build-logic`.
- Convert `core:common`, `core:data:*`, `core:search`, `core:sleeptimer`, `core:featureflag`, `navigation` to KMP. Migrate Room → Room KMP, DataStore → DataStore MP.
- Confirm Metro DI works across `commonMain`/`iosMain`.
- Stand up the `iosApp/` Xcode project that hosts a CMP `UIViewController`.

**Phase 2 — UI migration (3–5 weeks)**
- Move `core:ui` + `features:*` screens to Compose Multiplatform (swap coil→coil3, resources→`compose.resources`, navigation3→MP nav). Most screen code is reusable verbatim.
- Get the **Library, Book Details, Settings, History** screens rendering on iOS with stub data.

**Phase 3 — Playback engine (4–6 weeks, parallelizable)**
- Define the shared player contract; keep Android media3 `actual`.
- Implement the iOS AVFoundation `actual`: queue, background, lock screen, interruptions, speed, chapters, position reporting, auto-rewind, sleep-timer integration.
- Wire the player screen and mini-player to it.

**Phase 4 — Files, scanning, metadata (3–4 weeks)**
- iOS document-picker import + security-scoped bookmarks + scanner + AVAsset metadata/chapters/cover extraction.
- End-to-end: import a folder → books appear → play → resume.

**Phase 5 — Platform polish (3–5 weeks)**
- WidgetKit widgets (Swift + App Intents + App Group).
- iOS onboarding flow, iPad layout pass (you already added wide-screen support — reuse it).
- Edge cases: interruptions, Bluetooth/CarPlay handoff, errors, accessibility (VoiceOver).

**Phase 6 — Release engineering (2–3 weeks)**
- Apple Developer Program, bundle ID, provisioning, App Store Connect.
- Fastlane for iOS, TestFlight beta, screenshots, privacy nutrition labels (easy — "no data collected"), review submission.

> These overlap in practice. Calendar time for a solo senior dev: **~3–6 months** to a parity 1.0; the AVFoundation player and file access dominate.

---

## 10. Risks, unknowns, and decisions for you

1. **GPLv3 on the App Store (highest-priority, non-technical).** Apple's App Store ToS have historically conflicted with GPLv3 (the VLC episode). Options: dual-license your own app code, add an App Store distribution exception, or distribute via TestFlight/AltStore/sideloading. **This must be decided before serious investment** — it can change the whole plan. *Decision needed.*
2. **You need macOS + Xcode** for building/running/shipping iOS (you're on macOS already — good) and a **paid Apple Developer account ($99/yr)**.
3. **Skip-silence & volume-boost** may be Android-only at launch. *Decide: block 1.0 on these, or ship parity-minus-two and add later?*
4. **File model differs** — onboarding copy and expectations must be reframed for iOS. *Decide: copy-in import vs in-place bookmarks as the primary flow.*
5. **CarPlay** needs a specific Apple entitlement and review. *Decide: in scope for 1.0 or later?*
6. **Library/dependency drift:** CMP, Room KMP, DataStore MP move fast; pin versions and budget for occasional breakage.
7. **Navigation rewrite:** `navigation3` is Android-only; choosing the MP nav library (Decompose vs Voyager vs Compose-Nav-MP) is a one-time architectural decision worth a short spike.

---

## 11. Suggested immediate next steps (when you green-light)

1. **Clear the GPLv3/App Store question** (legal/licensing) — gate.
2. Run the **two Phase-0 spikes** (one CMP screen on iOS; one AVPlayer playback prototype). These prove the riskiest assumptions in ~1–2 weeks before committing.
3. Pick the **multiplatform navigation** library.
4. Decide the **v1.0 parity scope** (which of skip-silence / volume-boost / CarPlay / widgets are in vs. fast-follow).

Once you confirm direction and the scope decisions above, I can turn this into a concrete, ticketed work breakdown and start with the `build-logic` KMP setup and the Phase-0 spikes.
