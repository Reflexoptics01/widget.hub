# Playlist Resume Widget

## Goal

Add a second home-screen widget to `Tadabbur.widget` that saves a shared ReVanced/YouTube video locally and provides one-tap resume, next-episode advancement, and clear actions.

## Share flow

- The app exposes a native `ACTION_SEND` text share target named `Save to Playlist Resume`.
- It extracts the first YouTube URL, video ID, playlist ID, and optional `index` locally.
- It stores the URL, title, playlist position, and cached thumbnail in private app storage.
- It does not use login, YouTube API credentials, accessibility, or background monitoring.

## Widget behavior

- `RESUME` opens the saved direct video URL with its playlist context.
- `NEXT` increments the stored playlist index and opens a playlist URL with the new `index` parameter.
- `CLEAR` removes the saved episode and thumbnail.
- A setup card in the main app lets the user set the starting episode number.
- The widget uses a black full-bleed thumbnail, red/white controls, and a determinate wavy progress treatment.
- If no video is saved, the widget shows a clear empty state and a `SHARE VIDEO` prompt.

## Constraints

- The widget stays RemoteViews-compatible.
- No network request is made on the main thread.
- Thumbnail fetching is best-effort and falls back to the Tadabbur icon.
- Existing Tasbeeh widget and reminders remain unchanged.
