# Widget Hub — Tasbeeh Counter

This build adds lifetime counting and daily reminders to the Tasbeeh widget.

## Behavior

- Tapping the widget increments the current count by one.
- Reset clears the current count only.
- Lifetime total remains permanent across resets.
- The main app shows completed goal cycles as `+N`.
- The main app applies system-bar insets so its header does not sit beneath the notification panel.
- Goals remain 33, 99, 100, 1000, or a custom value.

## Reminders

Enable either reminder in the main app and choose its time:

- Tasbih-e-Fatima: Subhan Allah 33, Alhamdulillah 33, Allahu Akbar 34. Default time is 23:00, disabled by default.
- Durood: `Allahumma salli wa sallim 'ala Muhammad`, 100 times daily. Disabled by default.

Android notification permission must be allowed for notifications to appear. Reminders are rescheduled after reboot and time-zone changes.

## Widget

The widget is 2×2 by default and can be resized horizontally or vertically. If replacing an older APK signed with another key, uninstall the old Widget Hub first, then install this APK and add the widget again.
