# Raven — React Native (Expo) edition

An initial React Native + Expo port of the Android (Kotlin/Compose) **Raven/Voice** audiobook
player. It reuses the original design system (primary `#457CFA`, **Host Grotesk** font, **Mage**
icon set) and mirrors the core UI, navigation and playback behavior.

> This lives entirely in `raven-expo/`. The original Android Gradle project is untouched.

## Status / scope (v1)

Per the agreed scope:

- **Android only** (iOS comes later — folder import differs on iOS).
- **Dropped:** Android Auto, home-screen widget, skip-silence, volume-gain.
- **Simplest first:** maintained, first-party Expo libraries over custom native code.
- **Audio:** uses **`expo-audio`** (Expo's maintained audio module, SDK-56 compatible, supports
  background playback + lock-screen controls). It's abstracted behind `src/playback/playerStore.ts`
  so it can be swapped for `react-native-track-player` later without touching the screens.

## What works — playing-experience parity

- Onboarding (Welcome) → Library → Book details → Player → History → Settings.
- Library grouped into **In Progress / Not started / Completed**, 3-column grid, mini-player.
- **Lock-screen / notification controls** with now-playing metadata (title, author, chapter,
  artwork) via `expo-audio`'s `setActiveForLockScreen` — this also enables **sustained background
  playback** on Android (required, or the OS stops audio after ~3 min).
- **Draggable seek/scrub bar** (`@react-native-community/slider`) with live time readout.
- Player: play/pause, configurable **skip ±**, prev/next chapter, **auto-advance at chapter end**,
  pitch-corrected **speed**, configurable **auto-rewind on pause**.
- **Sleep timer**: timed, **end-of-chapter**, and **shake-to-resume** within 30s (`expo-sensors`) —
  abrupt pause, no fade (matches the Android change).
- **Interruption & external-control parity**: play/pause from the **lock screen**, a **phone call**,
  or **headphone unplug** all flow through one status-driven path, so history + auto-rewind +
  position-save behave the same regardless of where the action came from.
- **Resume on launch**: the most-recently-played book is restored into the mini-player.
- **History log** (Played / Paused / Jumped / Skipped to chapter / New chapter / Sleep timer),
  grouped by day with "minutes listened" — a port of the Android `PlaybackHistoryRecorder`.
- Position + chapter index persisted to **SQLite** (`expo-sqlite`); completed chapters dimmed to 60%.

## Local library import (SAF) + real metadata

- **Folder-type selection** ("How are your audiobooks structured?") — `app/import.tsx`, reached from
  onboarding and the library **+** / empty-state CTA — lets you choose **Top-level**, **Single book**,
  or **Author–book** layout, then opens the Android **Storage Access Framework** folder picker
  (`expo-file-system/legacy`).
- **Embedded tags + cover art** are read by a small local native module, `modules/audio-metadata`
  (framework `MediaMetadataRetriever`, **no Media3 dependency** — so no duplicate-class clash with
  `expo-audio`). Title/author/album and the embedded cover image come from the file itself; folder/file
  names and a `cover.jpg`/`folder.png` are used as fallbacks. Per-file **duration** comes from the tags
  (or is probed with `expo-audio`). SAF permissions persist, so imported `content://` files keep playing
  across restarts. Long-press a book (library) or use the **⋯** menu (details) to remove.
- **Embedded M4B chapter marks** — a single-file M4B with a Nero `chpl` chapter box is split into real
  chapters in place (each chapter is the same file at a stored `startMs` offset; the player runs the file
  continuously and the chapter index advances at each boundary). Multi-file books get one chapter per file.
- Supported extensions: mp3, m4a, m4b, aac, ogg, opus, wav, flac, mp4, 3gp.

Seeded on first launch with sample books (remote clips) so playback works before importing; remove with long-press.

## Library, player & settings parity

- **Search** screen (`app/search.tsx`) — filter the library by title/author.
- **Chapter list / jump** sheet and a **playback-speed** picker on the player.
- **Edit book title** and **remove book** from the details ⋯ menu.
- **Settings**: theme (**System / Light / Dark**), **default speed**, skip duration, auto-rewind,
  replay onboarding — all persisted.
- **In-app review** prompt (`expo-store-review`) after a few opens.

## Not yet ported (deferred, with rationale)

- **Internet cover search + crop** — needs an image-search API/key.
- **Intentionally excluded:** Android Auto, home-screen widget, skip-silence, volume-gain, bookmarks,
  and analytics/crashlytics/remote-config infra.
- Optional `react-native-track-player` swap (only if richer notification customization is needed —
  `expo-audio` already covers lock-screen controls + background).

## Tech mapping

| Android | Expo |
|---|---|
| Compose / Material 3 | React Native + custom components |
| Media3 / ExoPlayer / `VoicePlayer` | `expo-audio` (behind `playerStore`) |
| Room | `expo-sqlite` (`src/db`) |
| DataStore | `@react-native-async-storage/async-storage` |
| navigation3 | `expo-router` (`app/`) |
| Metro DI | `zustand` + plain modules |
| Mage vector drawables | `react-native-svg` + `src/icons/paths.ts` |
| Host Grotesk (`res/font`) | `expo-font` + `assets/fonts/HostGrotesk.ttf` |

## Run it

```bash
cd raven-expo
npm install          # already run during scaffolding

# Quick test in Expo Go (all native modules used here are bundled in Expo Go):
npx expo start       # then scan the QR with Expo Go

# Recommended for reliable background audio (builds a dev client):
npx expo run:android
```

## Project layout

```
app/                     expo-router screens
  _layout.tsx            fonts, DB seed, audio mode, first-run onboarding gate
  index.tsx              Library
  onboarding.tsx         Welcome
  player.tsx             Full player (modal)
  history.tsx            History
  settings.tsx           Settings
  book/[id].tsx          Book details
src/
  theme/                 colors, typography, useTheme
  icons/                 Icon.tsx, Logo.tsx, paths.ts (Mage set, Apache-2.0)
  data/types.ts          domain model + progress helpers
  db/                    database.ts, seed.ts, repo.ts (expo-sqlite)
  playback/playerStore.ts  zustand store over expo-audio
  components/            Cover, ProgressBar, IconButton, MiniPlayer
  lib/format.ts          time / remaining / day formatting
assets/fonts/            HostGrotesk.ttf (copied from the Android app)
```

Validated with `tsc --noEmit`, `expo-doctor` (21/21), and a full Android JS bundle export.
