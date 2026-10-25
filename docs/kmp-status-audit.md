# Raven / Vultr branch audit

Reviewed 24 October 2026. Read-only review of repository code and GitHub history; no remote changes, signing changes, or releases were made.

## Verdict

Raven is an offline Android audiobook player, forked from Voice. The KMP branch is a real, partial migration of core logic to Kotlin Multiplatform. It still builds an Android application through the existing app module; it does not yet contain a second platform application. The migration does not require finishing iOS before producing Android APKs or AABs.

Android packaging is configured, but this audit did not produce or successfully compile a new APK/AAB. Historical commit statements are evidence of reported builds, not fresh validation.

## Branch comparison

| Item | main | KMP |
| --- | --- | --- |
| Head | 688d7d38e3ff80cbdea28c116b3c0f1af70df667 | d1afd54e31c3a40c29a7f6af624160108dae0f03 |
| Last head commit, Lagos time | 30 August 2026 | 21 September 2026 |
| Relationship | Base | 7 commits ahead, 0 behind |
| Application | Android | Android, using partially shared KMP libraries |
| UI | Jetpack Compose | Same Android UI, with type compatibility edits |
| Other platform targets | None | None |
| Default app version | 1.7 / code 8 | 1.7 / code 8 |
| Application ID | io.github.emeraldrealm.raven | io.github.emeraldrealm.raven |

GitHub currently marks the repository public. The audit inspected a clean checkout of the KMP head without modifying application source.

Comparison: https://github.com/Emerald-Realm/Vultr/compare/main...KMP

## Migration completed

15 modules apply the new `voice.kmp.library` convention plugin:

- analytics API and no-op implementation
- logging API
- remote-config API and no-op implementation
- common utilities
- document-file contracts
- data API
- feature flags
- app initialization contract
- search
- sleep-timer API and parts of its implementation
- playback contracts and parts of playback state/history
- scanner metadata models and some parsing

Shared code is in `commonMain`; Android implementations are in `androidMain`. Java date/UUID types were replaced in the shared domain with Kotlin-compatible types. File paths and URI contracts were changed to strings where appropriate. Tests were moved into common or Android host source sets, and a KMP `voiceUnitTest` task was added.

The playback change creates a platform-neutral PlayerController interface and retains the Media3 implementation as AndroidPlayerController. It does not implement an Apple player. Likewise, the scanner module's conversion shares selected models and parsing logic, not its entire scanning implementation.

The last commit removes unused Firebase Analytics, Remote Config, and Crashlytics modules and related build configuration. Analytics and remote config use no-op implementations. Play In-App Review is selected by flavor.

Sources:

- https://github.com/Emerald-Realm/Vultr/blob/KMP/plugins/src/main/kotlin/KmpLibraryPlugin.kt
- https://github.com/Emerald-Realm/Vultr/commit/ee119fe43fc74cf93a7414689aaa44d63023cbf3
- https://github.com/Emerald-Realm/Vultr/commit/d1afd54e31c3a40c29a7f6af624160108dae0f03

## Migration remaining

There are no configured iOS, desktop, web, or other non-Android targets, no Apple application project, and no shared framework export. `core:data:impl` still owns the Android Room database construction, repositories, preferences, and migrations. Android SAF file access, Media3 playback/service/notifications, most scanning, and sensor sleep-timer behavior remain Android-specific. The feature screens, UI resources, widgets, navigation, and app entry point remain Android code.

The architecture anticipates platform-native Apple UI: the migration history explicitly says “No Compose Multiplatform” and refers to native Apple UI in a future implementation. That is the committed direction, rather than proof that an Apple UI has begun.

No meaningful migration percentage is supportable: converting a module does not mean every implementation inside it is shared or that another platform has been tested.

## UI status

UI development predates the KMP commits. The 30 August main commit adds a profile screen/view model, profile editing, listening/completion summaries, revised settings rows/layout, icons, and design guidance in `.impeccable.md`. The 26 July commit improves folder selection. Both branches include those changes.

The KMP diff changes UI consumers to adapt to shared dates, UUIDs, URI strings, and cover paths. It does not introduce a new screen design, theme, or navigation structure. Deleted UI screenshots in the cleanup commit are not a UI rewrite.

Source: https://github.com/Emerald-Realm/Vultr/commit/688d7d38e3ff80cbdea28c116b3c0f1af70df667

## Why work stopped

The last KMP commit is cleanup on 21 September. No KMP pull request, KMP Actions run, issue, or explicit stop/handoff explanation was found. The actual personal reason cannot be recovered from this repository. The code shows the work paused after extracting shared logic and before adding the second platform.

Two migration commits report Android debug/release assembly success, including the playback/scanner migration. They do not supply fresh logs or a verified signed Play AAB at the current head. GitHub Actions returns zero runs for branch KMP. The repository's available Actions history concerns its documentation website.

## Android APK/AAB readiness and findings

1. **Packaging remains configured.** The app has play/github flavors and debug/release build types. Intended commands are `:app:assemblePlayDebug`, `:app:assemblePlayRelease`, `:app:assembleGithubRelease`, and `:app:bundlePlayRelease`. KMP libraries are consumed by the Android app in the normal dependency graph.
2. **Signing is required for releases.** Signing is read from ignored root keystore.properties or flavor/CI signing files. None are present in this fresh checkout. Use the existing upload key when releasing to the existing Play listing; a newly generated arbitrary key is not a substitute.
3. **Potential clean-checkout configuration blocker, inherited from main.** app/build.gradle.kts lines 139–153 throws during every release variant callback if signing is absent. Since variants are configured independently of which build task is requested, this can also obstruct debug builds without signing. It needs a runtime check and likely adjustment to preserve signing-free debug development. This is source-level analysis, not a reproduced failure in this environment.
4. **Version must be advanced for Play.** Both branches default to versionCode 8. Compare with the highest code already uploaded to Play, then supply a greater code. The actual current Play Console artifact/version/signing state was not accessible in this audit.
5. **Upgrade behavior needs validation.** Dates, UUIDs, cover paths, URI handling, and preferences changed representation in code. Database schema files and database version 62 were not changed by the KMP branch. Existing converters still store dates/UUIDs as strings, but an installed-main → KMP upgrade test is needed to confirm library, bookmarks, positions, covers, permissions, and sleep settings survive.
6. **Release documentation is stale.** docs/development.md still points to upstream Voice release workflows and describes upstream calendar versioning, while Raven defaults to version 1.7/code 8. No app build CI workflows are present. Fastlane still passes the now-unused includeProprietaryLibraries property. README screenshots point to UI shots files removed on KMP.

Source: https://github.com/Emerald-Realm/Vultr/blob/KMP/app/build.gradle.kts

## Verification limits and next steps

Static source and git comparison completed. A Gradle dry-run was started with the installed Homebrew JDK 17 bootstrap runtime; it only reached the Gradle 9.5 distribution download and was stopped. No configuration, compilation, unit test, packaging, or signing result was obtained. This machine has no Android Studio installation or SDK at the standard user SDK location; no SDK path is configured in this checkout. The project requests JDK 25 and Android API 36.

Recommended order:

1. Set up the requested toolchain and Android SDK; ensure debug tasks work without release signing.
2. Run `./gradlew voiceUnitTest` and assemble a debug APK from KMP.
3. Test an upgrade with existing production-format data, plus background playback, lock-screen controls, scanning, seeking, sleep timer, widgets, and process restart.
4. Supply existing release/upload signing locally, set a new version code, assemble the release APK and bundlePlayRelease AAB, and validate on an internal Play track.
5. Add CI for Android compilation and tests.
6. Continue the multiplatform work by adding an actual second target and its database, file access, playback, dependency injection, and UI implementations.

Android release rules: https://support.google.com/googleplay/android-developer/answer/9859350

KMP application structure: https://kotlinlang.org/docs/multiplatform/quickstart.html
