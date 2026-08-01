# Material You 3 Expressive Nothing Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Restyle the Tasbeeh Widget Hub app and widget with a black/red Nothing identity, Material You 3 Expressive components, and lightweight safe animations.

**Architecture:** Keep the existing domain, persistence, reminder, and widget action flow. Concentrate changes in theme resources, layouts, activity rendering, widget resources, and small animation/state helpers so behavior remains stable.

**Tech Stack:** Kotlin, Android SDK 35, AppCompat/Material-compatible platform resources already used by the project, XML layouts, RemoteViews widget APIs, JVM tests.

## Global Constraints

- Preserve minimum SDK 23 and target SDK 35.
- Preserve the existing 2x2 default, resizable widget behavior, and all counter/reminder actions.
- Use only RemoteViews-supported widget views.
- Keep pure black as the base and Nothing-inspired red as the primary accent.

### Task 1: Establish the visual system

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/drawable/bg_expressive_card.xml`
- Create: `app/src/main/res/drawable/bg_expressive_control.xml`

- [ ] Add black, elevated-black, white, muted-white, Nothing-red, and red-pressed color tokens.
- [ ] Update the app theme for a black dark surface, red accent, rounded component defaults, and edge-to-edge-safe contrast.
- [ ] Add any missing accessible content descriptions and redesign labels.
- [ ] Add drawable resources for cards and controls using supported shape drawables.
- [ ] Run the resource compilation task and confirm it succeeds.

### Task 2: Redesign the main app screen

**Files:**
- Modify: `app/src/main/java/com/reflex/widgethub/MainActivity.kt`
- Modify or create: `app/src/main/res/layout/activity_main.xml`
- Create: `app/src/main/res/animator/count_pop.xml`
- Create: `app/src/main/res/animator/progress_spring.xml`

- [ ] Write a JVM-facing display-state test for count, goal, lifetime count, and completion accent mapping.
- [ ] Run the focused test and confirm it fails for the missing new state mapping.
- [ ] Add the minimal display-state mapping implementation.
- [ ] Run the focused test and confirm it passes.
- [ ] Build the main screen around a large count hero, lifetime count label, goal progress, reminder cards, and expressive controls.
- [ ] Apply count pop, progress spring, reset fade/scale, and completion pulse animations without changing counter semantics.
- [ ] Confirm existing reminder enable/time actions remain wired to the same stores and scheduler.

### Task 3: Redesign the widget safely

**Files:**
- Modify: `app/src/main/res/layout/widget_tasbeeh.xml`
- Modify: `app/src/main/res/drawable/bg_black_rounded.xml`
- Modify: `app/src/main/res/drawable/bg_control.xml`
- Modify: `app/src/main/java/com/reflex/widgethub/TasbeehWidgetProvider.kt`

- [ ] Add a widget rendering test for black/red state mapping and goal completion display.
- [ ] Run it and confirm it fails before the renderer changes.
- [ ] Update the widget layout with black elevated surface, red accent line/progress treatment, expressive typography, and compact top controls.
- [ ] Keep all widget views RemoteViews-compatible.
- [ ] Update provider refresh behavior so count changes visibly animate through a state refresh and never throw on empty/default state.
- [ ] Run the widget tests and confirm they pass.

### Task 4: Build and verify

**Files:**
- Modify: `README.md`

- [ ] Run all unit tests and record the result.
- [ ] Build the debug APK with `./gradlew assembleDebug`.
- [ ] Build/sign the release APK using the existing project flow.
- [ ] Verify APK package name, SDK levels, version, and signature.
- [ ] Update README screenshots/description for the new visual system.

