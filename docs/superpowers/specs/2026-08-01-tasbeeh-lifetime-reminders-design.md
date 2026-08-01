# Tasbeeh Widget Hub: Lifetime Count and Daily Reminders

## Goal

Extend the Tasbeeh Counter module in Widget Hub with a permanent lifetime total, clearer goal-cycle feedback, safe main-app layout insets, and configurable daily Islamic-reminder notifications.

## Existing behavior to preserve

- The home-screen widget increments the current counter by one when tapped.
- Reset clears the current counter only.
- Preset goals remain 33, 99, 100, and 1000, with custom goals supported.
- The widget is 2×2 by default and remains resizable.
- The visual direction remains black Nothing-style surfaces with Material You 3 Expressive accents.

## Counter model

Each counter state stores:

- `currentCount`: the count since the last manual reset.
- `goal`: the active goal.
- `lifetimeTotal`: the all-time number of increments, never reduced by Reset.

Every valid increment increases both `currentCount` and `lifetimeTotal` by one. Reset sets only `currentCount` to zero. Changing the goal does not alter either count.

The main app shows `lifetimeTotal` at the top. It also shows completed goal cycles as a compact `+N` indicator, where `N = floor(currentCount / goal)`. The current count is not capped, so counts beyond a goal are retained rather than discarded.

## Main-app layout safety

The main activity must respect system window insets. Content begins below the status/notification-bar area and ends above the navigation/gesture area. The activity must not draw the counter header underneath the phone’s notification panel. Status-bar and navigation-bar colors remain compatible with the black theme, and the layout remains usable on small and tall screens.

## Reminder behavior

The app provides two independent reminder rows, each with an enable switch and time picker.

### Tasbih-e-Fatima reminder

- Disabled by default.
- Default time: 23:00 local time.
- Repeats once daily when enabled.
- Notification content presents the bedtime sequence:
  1. Subhan Allah — 33 times
  2. Alhamdulillah — 33 times
  3. Allahu Akbar — 34 times
- The app labels this as the 33–33–34 bedtime version and does not claim that other narrated orderings do not exist.

### Durood reminder

- Independent switch from the Tasbih reminder.
- Disabled by default unless the user enables it.
- Repeats once daily at a user-selected local time.
- The concise recitation shown in the app is: `Allahumma salli wa sallim 'ala Muhammad`.
- The UI explains that this is a short wording and may offer the longer Ibrahimiyyah form as optional reference text; it does not present the short wording as the only valid form.

## Scheduling and reliability

- Store reminder enabled state and time in persistent local preferences.
- Use a daily alarm receiver to post notifications at the selected local time.
- Reschedule alarms after device reboot and relevant time-zone/time changes.
- Request Android notification permission where required.
- If exact-alarm permission is unavailable, use the safest available inexact daily scheduling path and keep the reminder functional.
- Tapping a notification opens the appropriate reminder section in the main app.

## Widget presentation

The existing widget remains compact. Its count can continue to show the current counter, while the main app is the authoritative place for lifetime total and reminder settings. Widget actions must continue to work after the new persistence fields are added.

## Testing requirements

Add tests for:

- Increment increases current and lifetime totals together.
- Reset clears current count but preserves lifetime total.
- Goal changes preserve both totals.
- Completed-cycle indicator uses floor division and never loses overflow counts.
- Default Tasbih reminder time is 23:00 and default state is disabled.
- Reminder time changes persist.
- Each reminder can be enabled independently.
- Alarm rescheduling restores enabled reminders after reboot/time changes.
- Main activity applies top and bottom insets rather than drawing under system bars.

## Scope boundaries

- No account, cloud sync, or analytics.
- No change to the existing widget’s 2×2 default or resize behavior.
- No claim that notification delivery can occur at an exact second on every Android device; system battery and alarm policies may delay delivery.

