# Main UI Polish and Reliable Widget Haptics

## UX goals

- Make the main counter screen calmer and easier to scan.
- Give the counter, progress, controls, and reminders distinct spacing zones.
- Keep the black/red Tadabbur.widget identity.
- Make widget haptics reliable across Android 8 through Android 15.

## UI changes

- Use 16dp page gutters and a 24dp content top inset.
- Give the title a supporting subtitle.
- Increase summary-card padding and lifetime-number hierarchy.
- Add consistent vertical rhythm around the main number, progress, and controls.
- Add 5dp separation between adjacent action buttons.
- Use 36dp section separation before reminders and 12dp separation between reminder cards.
- Allow reminder subtitles to wrap with readable line spacing.

## Haptics

- Use `VibratorManager.defaultVibrator` on Android 12+.
- Use the legacy vibrator service on older Android versions.
- Normal widget increment: 24ms, medium amplitude.
- Goal-reaching increment: 120ms, strong amplitude.
- Skip haptics when the device reports no vibrator.
