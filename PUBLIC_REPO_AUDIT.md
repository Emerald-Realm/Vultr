# Public Repository Audit

Date: 2026-07-05

## Summary

This audit checked the repository for personal information, local machine paths, signing material, generated binaries, stale product links, and nonessential files that should not be published with the app source.

The Android app still builds after the cleanup:

```bash
./gradlew :app:assemblePlayDebug
```

Result: `BUILD SUCCESSFUL`.

## Files Moved To `REMOVE/`

These files are not required for the product source build or contain sensitive/private material. Do not publish `REMOVE/` when making the repository public.

| Original path | New path | Reason |
| --- | --- | --- |
| `local.properties` | `REMOVE/local.properties` | Local Android SDK path and machine-specific configuration. |
| `signing/` | `REMOVE/signing/` | Keystores and signing properties must not be public. |
| `UI/` | `REMOVE/UI/` | Design/reference screenshots, `.pen` file, device screenshots, and `.DS_Store`; not needed for builds. |
| `SUBMISSION_HANDOFF.md` | `REMOVE/SUBMISSION_HANDOFF.md` | Private release handoff notes, contact email, and operational notes. |
| `PLAY_STORE_READINESS.md` | `REMOVE/PLAY_STORE_READINESS.md` | Private release checklist and signing notes. |
| `KMP_IOS_GAMEPLAN.md` | `REMOVE/KMP_IOS_GAMEPLAN.md` | Planning document not needed for Android source release. |
| `fastlane/metadata/android/*` except `en-US` | `REMOVE/fastlane/metadata/android/` | Localized store metadata still referenced upstream Voice support links; not needed for app build. |

## Codebase Updates

### Settings links

Updated `features/settings/src/main/kotlin/voice/features/settings/SettingsViewModel.kt`:

- Website now opens `https://praiseoyegoke.framer.website/raven`.
- Privacy Policy now opens `https://github.com/Emerald-Realm/Vultr/blob/main/docs/privacy-policy.md`.
- Terms now opens `https://github.com/Emerald-Realm/Vultr/blob/main/docs/terms.md`.
- Open Source Licenses now opens `https://github.com/Emerald-Realm/Vultr/blob/main/docs/licenses.md`.
- FAQ now opens `https://github.com/Emerald-Realm/Vultr/blob/main/docs/faq.md`.
- Bug report links no longer include `Build.MODEL`, so the app does not prefill a device model/name into GitHub issue URLs.

### Signing-safe Gradle behavior

Updated `app/build.gradle.kts` so missing local signing files do not break debug/source builds. Release signing can still be supplied later through `signing/` files or CI setup, but secrets are no longer required in the public tree.

### Public docs

Removed the personal support email from:

- `PRIVACY.md`
- `docs/privacy-policy.md`
- `docs/terms.md`
- `docs/index.md`
- `docs/faq.md`

Updated stale website references:

- `mkdocs.yml`
- `docs/development.md`

## Scan Results

Final scan outside `REMOVE/` found no committed:

- `local.properties`
- `.env*`
- `*.keystore`, `*.jks`, `*.p12`, `*.pem`, `*.key`
- `google-services.json`
- `*.apk` or `*.aab`
- `/Users/...` local paths
- `accionpraise`
- `Jonaim...` email references
- stale `raven.audiobook.app` links
- stale `voice.woitaschek.de` links

## Remaining Intentional References

These are expected and should not be removed without a separate licensing/rebrand decision:

- `Voice` package/class/module names remain in code. These are internal technical identifiers from the upstream project and are not personal information.
- Upstream Voice attribution remains in `ATTRIBUTION.md`, `README.md`, and license docs. This is appropriate for GPLv3 compliance because Raven is a fork.
- Two source comments reference upstream Voice issue URLs:
  - `core/playback/src/main/kotlin/voice/core/playback/session/ImageFileProvider.kt`
  - `core/playback/src/main/kotlin/voice/core/playback/session/VoiceMediaNotificationProvider.kt`
  These are technical provenance links, not user-facing links.
- The requested website URL contains `praiseoyegoke` in the domain. This is now present in app settings and docs because it was the supplied product website.

## Before Publishing

1. Delete or exclude the entire `REMOVE/` folder before making the repository public.
2. Confirm the public GitHub repository is `https://github.com/Emerald-Realm/Vultr`; the app settings now point there for docs and support.
3. If you need release builds, recreate signing material locally or provide it through CI secrets. Do not commit keystores or signing properties.
4. If the Framer website URL should not expose a personal name, replace it before release in `SettingsViewModel.kt`, `mkdocs.yml`, and `docs/development.md`.
