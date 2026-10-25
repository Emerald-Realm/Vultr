# Raven next release: listening accuracy and a calmer rating prompt

## Listening and levels

- Hours listened now come from measured active playback instead of the sum of saved book positions, so
  skipping ahead no longer inflates the number and rewinding no longer lowers it.
- Paused, buffering, seeking and idle time are not counted. Background playback still is.
- A book counts toward a level only after 95% of its known duration has actually been played. Jumping
  to the end does not earn a completion, and replaying a section does not count twice.
- Levels are five books per level and now use the same calculation in Settings and Profile.
- Existing libraries begin coverage tracking with this release. Levels that were previously inferred
  from saved positions may be corrected; books, bookmarks, playback positions and the lifetime hours
  total are kept.

## Rating

- Added **Rate Raven** under Settings → About. It opens the published Raven listing in Google Play,
  with a browser fallback.
- In Play builds only, a single quiet reminder may appear during local dates 8–14 after one hour of
  listening in that window. It never interrupts playback, never uses notifications, and appears at
  most once per month.
- “I've already rated” stops future reminders. “Not now” dismisses the reminder for that month.
- No star score is collected, no feedback is routed by rating, and no email is sent.
