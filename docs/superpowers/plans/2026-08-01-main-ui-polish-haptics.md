# Main UI Polish and Reliable Widget Haptics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Polish main-screen spacing and make widget haptics reliable on modern and legacy Android versions.

**Architecture:** Keep the existing state and widget actions. Isolate haptic duration selection in a pure helper, use Android’s default vibrator service at runtime, and adjust only the programmatic UI layout parameters for spacing.

**Tech Stack:** Kotlin, Android View APIs, VibratorManager/VibrationEffect, JVM unit tests.

## Global Constraints

- Preserve counter persistence, reminders, compact goal display, and widget actions.
- Preserve min SDK 23 and target SDK 35.
- Do not introduce unsupported widget views.

### Task 1: Haptic behavior

**Files:**
- Create: `app/src/main/java/com/reflex/widgethub/ui/HapticFeedback.kt`
- Create: `app/src/test/java/com/reflex/widgethub/ui/HapticFeedbackTest.kt`
- Modify: `app/src/main/java/com/reflex/widgethub/TasbeehWidgetProvider.kt`

- [ ] Test 24ms normal and 120ms goal-reaching durations.
- [ ] Use `VibratorManager.defaultVibrator` on API 31+ and the legacy service below it.
- [ ] Skip vibration when unavailable.

### Task 2: Main-screen rhythm

**Files:**
- Modify: `app/src/main/java/com/reflex/widgethub/MainActivity.kt`

- [ ] Apply consistent gutters, card padding, section spacing, and button gaps.
- [ ] Improve reminder subtitle wrapping and time-button height.
- [ ] Keep all listeners and state rendering unchanged.

### Task 3: Verification

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] Run all unit tests.
- [ ] Build the debug APK.
- [ ] Verify SDK metadata and APK signature.
