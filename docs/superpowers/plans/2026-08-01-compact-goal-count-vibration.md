# Compact Goal Count and Widget Vibration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Show goal-plus-remainder notation, replace the reset icon with aligned text, and add light/goal vibration feedback to widget taps.

**Architecture:** Add a pure formatter for compact display text, reuse it from the activity and widget provider, and keep vibration in the provider after comparing the incremented state with its goal.

**Tech Stack:** Kotlin, Android vibration APIs, RemoteViews, JVM unit tests.

## Global Constraints

- Preserve exact stored counts and lifetime totals.
- Preserve min SDK 23, target SDK 35, and widget resize behavior.
- Use only RemoteViews-supported views.

### Task 1: Compact display formatter

**Files:**
- Modify: `app/src/main/java/com/reflex/widgethub/ui/DisplayFormatters.kt`
- Modify: `app/src/test/java/com/reflex/widgethub/ui/DisplayFormattersTest.kt`

- [ ] Add failing tests for `33`, `33⁺¹`, and `33⁺²`.
- [ ] Run the focused test and confirm the formatter is missing.
- [ ] Implement compact goal/remainder formatting using Unicode superscript digits.
- [ ] Run the focused test and confirm it passes.

### Task 2: Main app display

**Files:**
- Modify: `app/src/main/java/com/reflex/widgethub/MainActivity.kt`

- [ ] Render compact formatted text as the main number.
- [ ] Remove the old cycle header text.
- [ ] Keep goal and reset as aligned text controls.
- [ ] Preserve counter and reminder actions.

### Task 3: Widget layout and vibration

**Files:**
- Modify: `app/src/main/res/layout/widget_tasbeeh.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/reflex/widgethub/TasbeehWidgetProvider.kt`

- [ ] Replace the reset `ImageButton` with a text `Button`.
- [ ] Align goal and reset controls with equal sizing.
- [ ] Render compact count text.
- [ ] Add VIBRATE permission and light/strong `VibrationEffect` feedback.
- [ ] Run all tests and build the APK.
