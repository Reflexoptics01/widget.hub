# Tasbeeh Lifetime Count and Reminders Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Recreate Widget Hub as a Kotlin Android app with a persistent lifetime Tasbeeh total, safe inset-aware main screen, and configurable daily Tasbih-e-Fatima and Durood notifications.

**Architecture:** A small native Android app will use Kotlin domain models and SharedPreferences-backed storage. The main activity owns settings and display, the AppWidgetProvider owns widget actions, and a BroadcastReceiver schedules/post reminders through AlarmManager and NotificationManager. The project will be self-contained because the previous source project is unavailable.

**Tech Stack:** Kotlin, Android SDK API 35, AndroidX core/appcompat/activity, Material Components where available, AAPT2/D8/apksigner for packaging, JVM unit tests for pure domain logic.

## Global Constraints

- Existing widget behavior remains: tap increments current count; Reset clears current count only.
- Lifetime total never decreases when Reset is pressed.
- Preset goals remain 33, 99, 100, and 1000, with custom goals.
- Widget default size remains 2×2 and resizable.
- Main screen must respect top and bottom system insets.
- Tasbih-e-Fatima reminder uses Subhan Allah 33×, Alhamdulillah 33×, Allahu Akbar 34×.
- Tasbih-e-Fatima reminder defaults to disabled and 23:00 local time.
- Durood reminder is independent, daily, and disabled by default.
- No accounts, cloud sync, analytics, or hardcoded playlist/widget data.
- Android notification permission and reboot/time-change rescheduling must be handled.

---

### Task 1: Recreate the Android project and build tooling

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `app/build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/xml/tasbeeh_widget_info.xml`

**Interfaces:**
- Produces an Android application id `com.reflex.widgethub` targeting SDK 35 with min SDK 23.
- Provides the `TasbeehWidgetProvider`, `MainActivity`, `ReminderReceiver`, and `BootReceiver` component declarations used by later tasks.

- [ ] **Step 1: Add the minimal Gradle project and Android manifest.**

  Configure one `app` module with Kotlin Android support, `minSdk = 23`, `targetSdk = 35`, and `compileSdk = 35`. Declare `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, and `SCHEDULE_EXACT_ALARM` only where supported by the target SDK. Declare the launcher activity, widget receiver, reminder receiver, and boot/time-change receiver.

- [ ] **Step 2: Add resource defaults and widget metadata.**

  Set the widget metadata to `initialLayout`, `minWidth=110dp`, `minHeight=110dp`, `targetCellWidth=2`, `targetCellHeight=2`, `resizeMode="horizontal|vertical"`, and `widgetCategory="home_screen"`.

- [ ] **Step 3: Resolve dependencies and compile the empty app.**

  Run `./gradlew :app:assembleDebug` (or the locally available equivalent if Gradle must be bootstrapped). Expected result: exit code 0 and a debug APK.

- [ ] **Step 4: Commit the project scaffold.**

  Run `git add settings.gradle.kts build.gradle.kts gradle.properties app` and commit with `feat: recreate Widget Hub Android project` when repository writes are available.

---

### Task 2: Implement counter state and persistence with tests

**Files:**
- Create: `app/src/main/java/com/reflex/widgethub/domain/CounterState.kt`
- Create: `app/src/main/java/com/reflex/widgethub/domain/CounterLogic.kt`
- Create: `app/src/main/java/com/reflex/widgethub/data/CounterStore.kt`
- Create: `app/src/test/java/com/reflex/widgethub/domain/CounterLogicTest.kt`

**Interfaces:**
- `data class CounterState(val currentCount: Long, val goal: Long, val lifetimeTotal: Long)`
- `fun increment(state: CounterState): CounterState`
- `fun resetCurrent(state: CounterState): CounterState`
- `fun completedCycles(state: CounterState): Long`
- `CounterStore.load(): CounterState`, `save(state: CounterState)`, and `update(transform: (CounterState) -> CounterState): CounterState`

- [ ] **Step 1: Write failing tests for increment and lifetime semantics.**

  Test that `CounterState(32, 33, 100)` becomes `CounterState(33, 33, 101)` after increment, and that resetting `CounterState(34, 33, 102)` produces `CounterState(0, 33, 102)`.

- [ ] **Step 2: Run the focused test and verify it fails for the missing logic.**

  Run `./gradlew :app:testDebugUnitTest --tests '*CounterLogicTest'`. Expected result: compilation/test failure because the production functions are not implemented.

- [ ] **Step 3: Implement the minimal pure counter logic.**

  Increment both counters by one, reset only `currentCount`, and calculate `completedCycles` as `currentCount / goal` for a positive goal. Normalize invalid stored goals to 1 before division so a corrupt preference cannot crash the app.

- [ ] **Step 4: Add persistence and default state.**

  Store `currentCount`, `goal`, and `lifetimeTotal` under versioned preference keys. Default to current 0, goal 33, lifetime 0. Persist atomically through one `SharedPreferences.Editor` transaction.

- [ ] **Step 5: Run the tests and verify they pass.**

  Run the focused test again, then `./gradlew :app:testDebugUnitTest`. Expected result: all counter tests pass.

- [ ] **Step 6: Commit the counter domain.**

  Run `git add app/src/main/java app/src/test` and commit with `feat: persist lifetime Tasbeeh counter` when repository writes are available.

---

### Task 3: Build the inset-safe main app UI

**Files:**
- Create: `app/src/main/java/com/reflex/widgethub/MainActivity.kt`
- Create: `app/src/main/res/layout/activity_main.xml`
- Create: `app/src/main/res/drawable/bg_black_rounded.xml`
- Create: `app/src/main/res/drawable/bg_control.xml`
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Main activity reads/writes `CounterStore` and exposes `renderCounter(state: CounterState)`.
- The top summary must display lifetime total and completed-cycle `+N` without overlapping system bars.

- [ ] **Step 1: Write a UI contract test for cycle text.**

  Add a pure formatter test asserting `completedCycleLabel(CounterState(34, 33, 500)) == "+1"` and no completed-cycle label for a state below one full goal.

- [ ] **Step 2: Run the formatter test and verify the missing formatter fails.**

  Run the focused unit test and confirm failure is due to the absent formatter.

- [ ] **Step 3: Implement the formatter and main layout.**

  Use a black surface, large expressive count, compact top summary (`Lifetime total`, value, `+N`), goal selector, reset button, and reminder section. Keep controls readable at 2×2 widget dimensions but allow the full activity to scroll on small screens.

- [ ] **Step 4: Apply system insets explicitly.**

  Use `WindowCompat.setDecorFitsSystemWindows(window, true)` and `ViewCompat.setOnApplyWindowInsetsListener` to add the status-bar inset to the root top padding and navigation-bar inset to bottom padding. Do not place the header behind the status bar.

- [ ] **Step 5: Wire increment, reset, goal selection, and persistence.**

  Every increment updates state and refreshes the widget. Reset updates only current count. Presets expose 33, 99, 100, and 1000 plus a custom numeric dialog.

- [ ] **Step 6: Run unit tests and compile the activity/resources.**

  Run `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected result: formatter/counter tests pass and Android resources compile.

- [ ] **Step 7: Commit the main screen.**

  Run `git add app/src/main/java app/src/main/res` and commit with `feat: add lifetime total and safe main screen` when repository writes are available.

---

### Task 4: Implement the widget actions and refresh path

**Files:**
- Create: `app/src/main/java/com/reflex/widgethub/TasbeehWidgetProvider.kt`
- Create: `app/src/main/res/layout/widget_tasbeeh.xml`
- Create: `app/src/main/res/drawable/widget_button.xml`
- Modify: `app/src/main/java/com/reflex/widgethub/data/CounterStore.kt`

**Interfaces:**
- Widget provider handles `ACTION_INCREMENT`, `ACTION_RESET`, and `ACTION_OPEN_APP`.
- `refreshAllWidgets(context: Context)` updates every registered widget instance from the shared counter state.

- [ ] **Step 1: Write failing tests for widget action state transitions.**

  Test the action reducer conceptually: increment maps to `increment(state)`, reset maps to `resetCurrent(state)`, and both actions preserve lifetime totals as specified.

- [ ] **Step 2: Run the test to verify it fails before the reducer exists.**

  Run the focused test and confirm the missing reducer failure.

- [ ] **Step 3: Implement the provider and supported RemoteViews layout.**

  Use only supported widget views (`FrameLayout`, `LinearLayout`, `TextView`, `ImageButton`, `ProgressBar` or drawable-backed `ImageView`). Keep the black 2×2 design, compact goal/reset controls, current count, and a subtle progress indicator. Avoid generic `View` elements that previously caused “could not load widget.”

- [ ] **Step 4: Connect click PendingIntents and refresh.**

  Use explicit broadcast intents with unique action strings. Increment and reset persist through `CounterStore`, then call `AppWidgetManager` updates. Opening the goal area launches the main activity.

- [ ] **Step 5: Run tests and build the debug APK.**

  Run `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected result: widget action tests and all existing tests pass.

- [ ] **Step 6: Commit the widget.**

  Run `git add app/src/main/java app/src/main/res` and commit with `feat: restore Tasbeeh widget actions` when repository writes are available.

---

### Task 5: Add reminder preferences and notification scheduling

**Files:**
- Create: `app/src/main/java/com/reflex/widgethub/reminders/ReminderType.kt`
- Create: `app/src/main/java/com/reflex/widgethub/reminders/ReminderSettings.kt`
- Create: `app/src/main/java/com/reflex/widgethub/reminders/ReminderStore.kt`
- Create: `app/src/main/java/com/reflex/widgethub/reminders/ReminderScheduler.kt`
- Create: `app/src/main/java/com/reflex/widgethub/reminders/ReminderReceiver.kt`
- Create: `app/src/main/java/com/reflex/widgethub/reminders/BootReceiver.kt`
- Create: `app/src/test/java/com/reflex/widgethub/reminders/ReminderSettingsTest.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- `enum class ReminderType { TASBIH_FATIMA, DUROOD }`
- `data class ReminderSettings(val enabled: Boolean, val hour: Int, val minute: Int)`
- `ReminderStore.get(type): ReminderSettings`, `set(type, settings)`
- `ReminderScheduler.schedule(type, settings)`, `cancel(type)`, and `rescheduleEnabled()`

- [ ] **Step 1: Write failing tests for reminder defaults and independence.**

  Assert Tasbih defaults to disabled at 23:00, Durood defaults to disabled, and updating one type does not alter the other. Assert times persist as hour/minute values.

- [ ] **Step 2: Run focused tests and verify the expected failure.**

  Run `./gradlew :app:testDebugUnitTest --tests '*ReminderSettingsTest'`. Expected result: failure because reminder types/store are not yet implemented.

- [ ] **Step 3: Implement settings and persistence.**

  Store two independent enabled flags and hour/minute pairs. Clamp hour to 0–23 and minute to 0–59 before saving. Default Tasbih to 23:00 and both reminders disabled.

- [ ] **Step 4: Implement daily AlarmManager scheduling.**

  Build a `Calendar` at the chosen local hour/minute, advance it one day if it is in the past, and schedule a daily repeating alarm. Prefer `setExactAndAllowWhileIdle` for the next occurrence plus rescheduling after delivery; fall back to `setAndAllowWhileIdle` if exact alarms are unavailable. Use distinct request codes per reminder type.

- [ ] **Step 5: Implement notification delivery.**

  Create one notification channel, post the correct title/body, include the Tasbih sequence or concise Durood wording, and use a `PendingIntent` opening `MainActivity` with a reminder type extra. Request `POST_NOTIFICATIONS` from the activity on Android 13+.

- [ ] **Step 6: Implement reboot/time-change recovery.**

  The boot receiver handles `BOOT_COMPLETED`, `TIME_SET`, and `TIMEZONE_CHANGED` by calling `rescheduleEnabled()`. The reminder receiver reschedules the same enabled reminder after posting.

- [ ] **Step 7: Run tests and build.**

  Run `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected result: reminder tests, counter tests, and resource compilation pass.

- [ ] **Step 8: Commit reminders.**

  Run `git add app/src/main/java app/src/main/res app/src/test app/src/main/AndroidManifest.xml` and commit with `feat: add configurable Tasbeeh and Durood reminders` when repository writes are available.

---

### Task 6: Finish APK packaging and verification

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `README.md`
- Create: `release/WidgetHub-Tasbeeh-Lifetime-Reminders.apk`

**Interfaces:**
- Produces a signed, zip-aligned APK with an explicit application label and version code greater than the previous 5.1 build.

- [ ] **Step 1: Build a clean release artifact.**

  Run `./gradlew clean :app:assembleRelease` and verify the output path.

- [ ] **Step 2: Sign and align the APK.**

  Use a local debug/release keystore available in the workspace, run `zipalign`, then `apksigner verify --verbose`. The APK must declare `minSdkVersion 23` and `targetSdkVersion 35`.

- [ ] **Step 3: Run the full verification suite.**

  Run `./gradlew :app:testDebugUnitTest :app:assembleRelease`; inspect manifest/package metadata with `apkanalyzer` or `aapt dump badging`; verify the APK signature; and check that the release contains the lifetime/reminder strings.

- [ ] **Step 4: Create a concise README.**

  Document installation, uninstall/reinstall behavior if signing keys differ, adding the 2×2 widget, enabling notification permission, configuring the two reminders, and the fact that Reset does not erase lifetime total.

- [ ] **Step 5: Commit the release metadata.**

  Run `git add app/build.gradle.kts README.md` and commit with `chore: package lifetime reminder release` when repository writes are available.

