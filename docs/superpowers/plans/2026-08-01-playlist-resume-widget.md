# Playlist Resume Widget Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a share-driven Playlist Resume widget to the existing Tadabbur.widget Android app.

**Architecture:** A pure URL parser handles shared text; `PlaylistResumeStore` persists the parsed episode and cached art; a share receiver updates the store; a dedicated `AppWidgetProvider` renders RemoteViews and opens resume/next/clear actions. The main activity adds a small setup card for the starting index.

**Tech Stack:** Kotlin, Android SharedPreferences, `HttpURLConnection`, Bitmap storage, AppWidget RemoteViews, JVM tests.

## Global Constraints

- No login, YouTube API, accessibility permission, or background monitoring.
- Preserve the existing Tasbeeh widget, reminders, and app identity.
- Use RemoteViews-supported views only.
- Preserve min SDK 23 and target SDK 35.

### Task 1: Local parser and store

**Files:**
- Create: `app/src/main/java/com/reflex/widgethub/playlist/PlaylistLink.kt`
- Create: `app/src/main/java/com/reflex/widgethub/playlist/PlaylistResumeParser.kt`
- Create: `app/src/main/java/com/reflex/widgethub/playlist/PlaylistResumeStore.kt`
- Create: `app/src/test/java/com/reflex/widgethub/playlist/PlaylistResumeParserTest.kt`

- [ ] Add failing parser tests for `watch?v=...&list=...&index=300`, short URLs, and missing playlist IDs.
- [ ] Run the focused tests and confirm the parser is missing.
- [ ] Implement URL extraction with `java.net.URI`, query parsing, and index defaults.
- [ ] Implement private preference/file persistence and `advance()` index updates.
- [ ] Run parser/store unit tests and confirm they pass.

### Task 2: Share target and thumbnail cache

**Files:**
- Create: `app/src/main/java/com/reflex/widgethub/playlist/PlaylistResumeShareActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values/themes.xml`

- [ ] Add an exported `ACTION_SEND` text share target labeled `Save to Playlist Resume`.
- [ ] Store shared content immediately and finish the activity.
- [ ] Fetch the `i.ytimg.com` thumbnail on a worker thread, cache it privately, and refresh the widget.
- [ ] Add only INTERNET permission; do not add account/accessibility permissions.

### Task 3: Playlist Resume widget

**Files:**
- Create: `app/src/main/java/com/reflex/widgethub/playlist/PlaylistResumeWidgetProvider.kt`
- Create: `app/src/main/res/layout/widget_playlist_resume.xml`
- Create: `app/src/main/res/xml/playlist_resume_widget_info.xml`
- Create: `app/src/main/res/drawable/progress_wavy.xml`
- Create: `app/src/main/res/drawable/bg_playlist_overlay.xml`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] Render full-bleed cached art with fallback icon.
- [ ] Add visible `RESUME`, `NEXT`, and `CLEAR` actions.
- [ ] Render title, episode index, and determinate wavy progress.
- [ ] Wire PendingIntents for resume, next, and clear.
- [ ] Keep default widget size 2x2 and resizable.

### Task 4: Main-app setup card

**Files:**
- Modify: `app/src/main/java/com/reflex/widgethub/MainActivity.kt`

- [ ] Add a Playlist Resume card showing saved title/index.
- [ ] Add a starting-episode input/action and clear action.
- [ ] Refresh the widget after changes.

### Task 5: Verify and package

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] Run all unit tests.
- [ ] Build the debug APK.
- [ ] Verify manifest, SDK metadata, widget providers, and APK signature.
- [ ] Update the public GitHub repository with the source changes.
