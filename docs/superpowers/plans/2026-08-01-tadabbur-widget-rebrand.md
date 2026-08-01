# Tadabbur.widget Rebrand Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rebrand the app and widget, add the Islamic icon, and expose completed cycles as a compact goal header while retaining exact counts.

**Architecture:** Keep the existing counter and reminder stores unchanged. Add a pure formatter for the cycle header, update the programmatic main screen and RemoteViews widget, and add a vector launcher icon.

**Tech Stack:** Kotlin, Android XML/vector resources, AppCompat, RemoteViews, JVM unit tests.

## Global Constraints

- Preserve min SDK 23 and target SDK 35.
- Preserve the 2x2 default and resizable widget behavior.
- Preserve exact current count and lifetime total semantics.
- Use black/red styling and RemoteViews-safe widget controls.

### Task 1: Cycle display formatter

**Files:**
- Modify: `app/src/main/java/com/reflex/widgethub/ui/DisplayFormatters.kt`
- Modify: `app/src/test/java/com/reflex/widgethub/ui/DisplayFormattersTest.kt`

- [ ] Add failing tests for zero and completed cycle headers.
- [ ] Run the focused tests and verify failure because the formatter does not exist.
- [ ] Implement `cycleHeaderLabel(state: CounterState): String`.
- [ ] Run focused tests and verify they pass.

### Task 2: Rebrand and icon

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/drawable/ic_tadabbur_icon.xml`

- [ ] Set the app and widget labels to `Tadabbur.widget`.
- [ ] Add a vector icon with black base, red crescent, and geometric rosette.
- [ ] Reference it as the application icon.
- [ ] Compile resources.

### Task 3: Align the main app UI

**Files:**
- Modify: `app/src/main/java/com/reflex/widgethub/MainActivity.kt`

- [ ] Add a dedicated cycle header above the main count.
- [ ] Replace the loose goal/reset arrangement with aligned text controls.
- [ ] Keep `RECITE +1` centered and primary.
- [ ] Render the new cycle header and keep the exact number unchanged.
- [ ] Preserve reminders and animation behavior.

### Task 4: Update the widget

**Files:**
- Modify: `app/src/main/res/layout/widget_tasbeeh.xml`
- Modify: `app/src/main/java/com/reflex/widgethub/TasbeehWidgetProvider.kt`

- [ ] Add a compact cycle label above the count.
- [ ] Keep top `GOAL` and `RESET` text controls aligned.
- [ ] Render the same cycle formatter from the saved state.
- [ ] Keep all widget views supported by RemoteViews.

### Task 5: Verify and package

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] Run all unit tests.
- [ ] Build the debug APK.
- [ ] Verify package, version, SDK levels, and signature.
- [ ] Copy the APK to a descriptive release filename.
