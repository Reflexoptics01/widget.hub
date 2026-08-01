# Compact Goal Count and Widget Vibration

## Behavior

- For goal 33 and current count 33, show `33`.
- For current count 34, show `33⁺¹`.
- For current count 35, show `33⁺²`.
- The exact value remains persisted internally and lifetime total remains unchanged.
- Reset is a text button labeled `RESET`.
- Widget taps produce a light vibration; taps landing exactly on a goal multiple produce a stronger vibration.

## Layout

- Remove the old `GOAL 33 • +N CYCLE` header.
- Align `GOAL 33` and `RESET` as equal text controls across the widget top row.
- Use the compact count as the central widget and main-app value.

## Compatibility

- Preserve min SDK 23, target SDK 35, existing persistence, reminder scheduling, and 2x2 resizable widget behavior.
- Keep widget layout RemoteViews-compatible.
