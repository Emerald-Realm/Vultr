# Raven — Submission Handoff

_Updated 2026-06-17. Companion to [PLAY_STORE_READINESS.md](PLAY_STORE_READINESS.md). This tracks what was just done, what still needs **you**, and where every document lives._

---

## ✅ Done in this round (code + content)

- **Android Auto disabled for v1** — the three `com.google.android.gms.car.*` meta-data entries in `app/src/main/AndroidManifest.xml` are commented out with a note to re-enable later. (Removes the separate Play "Cars" review.)
- **Dead code removed** — deleted the entire unused `features:bookmark` module (the player uses `BookmarksBottomSheet`), the `AddingFab.kt` file, the `Destination.Bookmarks` entry, and their Gradle references. Build passes.
- **Store title trimmed** — `fastlane/metadata/android/en-US/title.txt` → **"Raven Audiobook Player"** (22 chars, under the 30 limit).
- **Store listing rewritten (en-US)** — friendlier copy focused on Raven's real features; removed the upstream `PaulWoitaschek/Voice` link and the Android Auto claim. Updated `short_description.txt` and added release notes at `changelogs/1.txt`.
- **In-app web links repointed** — Settings → About now points to the GitHub Pages site instead of the dead `raven.audiobook.app` placeholders (`SettingsViewModel.kt`, constant `SITE_BASE`).
- **No-telemetry confirmed** — the analytics-consent toggle is already auto-hidden when analytics aren't compiled in (`showAnalyticSetting = appInfoProvider.analyticsIncluded`). Nothing ships with Firebase/analytics/crashlytics.
- **Documents written / fixed** (see locations below): Privacy Policy, Terms, Open-Source Licenses, FAQ, site landing/about, README, Attribution. Removed the upstream **CNAME** (`docs/CNAME` pointed at `voice.woitaschek.de`!) and the stale auto-generated privacy policy.

---

## 🔴 NEEDS YOU — provide / decide / host

1. **Make the GitHub repo public** — `github.com/Emerald-Realm/Vultr` must be public before release (GPLv3 source availability + all in-app links point there).
2. **Enable GitHub Pages** — repo **Settings → Pages → Source: `main` / `/docs`**. After it deploys, confirm these resolve:
   - `https://emerald-realm.github.io/Vultr/privacy-policy/`
   - `.../terms/`, `.../licenses/`, `.../faq/`
   If your Pages URL differs (e.g. you rename the repo to `Raven`), tell me and I'll update `SITE_BASE` in `SettingsViewModel.kt` and the `/Vultr/` links in the docs.
3. **Privacy policy URL for the Play Console** — use `https://emerald-realm.github.io/Vultr/privacy-policy/` (must be live first).
4. **Data Safety form (Play Console)** — declare: **No data collected, no data shared.** No advertising ID. (Matches the shipped no-telemetry build and the new privacy policy.)
5. **Confirm the Play signing key is YOURS** — `signing/play/signing.keystore` must be a key you generated, not the upstream developer's. Recommended: enroll in **Play App Signing** and treat this as your upload key. _(I can't verify keystore ownership.)_
6. **Graphics / design assets** — see the pending list below. These are the main remaining blockers I can't produce.
7. **Non-English store descriptions** — 15 locale `full_description.txt` files still contain "Voice". Decide: (a) I delete the non-en locale folders so Play uses en-US for everyone (fastest), or (b) you supply translations. **Tell me which** and I'll do it. Titles are already localized to "Raven".
8. **Contact email** — privacy/support set to **Jonaim039@gmail.com**. Also set this as the Play Console contact email.
9. **Trademark sanity check** — confirm "Raven" doesn't collide with an existing audiobook/media app on Play.

---

## 🎨 Pending graphic / design work (you or a designer)

These must be **Raven** assets (not Voice's). Drop replacements into `fastlane/metadata/android/en-US/images/`:

- [ ] **App icon** — 512×512 PNG (`images/icon.png`). Confirm it's the Raven logo.
- [ ] **Feature graphic** — 1024×500 (`images/featureGraphic.jpeg`). Currently likely the old Voice graphic — replace.
- [ ] **Phone screenshots** — at least 2 (Play allows up to 8). Capture the **new Raven UI** in both light and dark mode: Library (grid + list), Player, Book Details, Bookmarks, Settings. Replace `images/phoneScreenshots/1..4_en-US.png`.
- [ ] **(Optional) 7-inch / tablet screenshots** — `images/sevenInchScreenshots/` currently exist; replace or remove if not targeting tablets.
- [ ] **(Optional) Promo / TV banner** — only if you opt into those surfaces.

> Tip: the emulator workflow we've used (dark mode + seeded book) is ideal for capturing clean store screenshots at device resolution.

---

## 📄 Where every document lives

| Document | Local path | Where it should be published |
|---|---|---|
| Privacy Policy | `docs/privacy-policy.md` (+ root `PRIVACY.md`) | GitHub Pages → `/privacy-policy/`; URL goes in Play Console |
| Terms of Use | `docs/terms.md` | GitHub Pages → `/terms/` (linked in-app) |
| Open-Source Licenses | `docs/licenses.md` | GitHub Pages → `/licenses/` (linked in-app) |
| FAQ | `docs/faq.md` | GitHub Pages → `/faq/` (linked in-app) |
| Site landing / About | `docs/index.md`, `docs/about.md` | GitHub Pages root |
| Full GPL text | `LICENSE.md` | Repo (already present) |
| Attribution to Voice | `ATTRIBUTION.md` | Repo root |
| README | `README.md` | Repo front page |
| Store title/short/full | `fastlane/metadata/android/en-US/*.txt` | Play Console listing |
| Release notes | `fastlane/metadata/android/en-US/changelogs/1.txt` | Play Console "What's new" |
| Store images | `fastlane/metadata/android/en-US/images/` | Play Console graphics |
| Readiness audit | `PLAY_STORE_READINESS.md` | Repo (internal) |
| This handoff | `SUBMISSION_HANDOFF.md` | Repo (internal) |

> **Note on `docs/`:** the privacy-policy is now a real file (the old symlink was removed because GitHub Pages doesn't follow symlinks). Root `PRIVACY.md` and `docs/privacy-policy.md` carry the same policy text — keep them in sync if you edit.

---

## 🟡 Optional follow-ups (not blockers)

- **In-app "Open source licenses" screen** — currently the Settings link opens the hosted `/licenses/` page in a browser. If you'd prefer an in-app native licenses list, that's a small feature I can add later.
- **Internal namespace `voice.*`** — invisible to users; leaving it. Rename only if you want zero "voice" in the source.
- `docs/license.md` is still a symlink to `LICENSE.md`; harmless (not linked in-app). Can be made a real file if you want it on the site.

---

## When you're ready to build the release (not done yet, per your instruction)

```
./gradlew :app:bundlePlayRelease   # produces the .aab to upload
./gradlew voiceUnitTest            # full test suite green on the submit commit
```
Smoke-test the **release** build on a physical device before uploading.
