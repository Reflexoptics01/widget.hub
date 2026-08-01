# Tadabbur.widget Rebrand and Cycle Display

## Goal

Rebrand the app as `Tadabbur.widget`, add a legible Islamic geometric icon, and make completed goal cycles visible without hiding the exact counter value.

## Display behavior

- The main count remains the exact current count, such as `34`.
- A compact header above it reads `GOAL 33 • +1 CYCLE` after one completed goal.
- Before completion it reads `GOAL 33 • +0 CYCLES`.
- The widget follows the same wording with a small cycle label.
- Lifetime total remains separate and continues to include every tap.

## Visual layout

- Main app top area uses a balanced lifetime card.
- Goal and reset are text controls aligned on one row.
- The red `RECITE +1` action is centered as the primary control.
- Existing reminder cards keep the black/red Material You expressive styling.

## Icon and naming

- Application label becomes `Tadabbur.widget`.
- Launcher icon is a black rounded-square with a red crescent and Islamic geometric rosette; it contains no small text.
- Widget label and description are updated to match the Tadabbur name.

## Compatibility

- Preserve min SDK 23, target SDK 35, 2x2 default widget size, resizability, counter persistence, and reminder scheduling.
- Use only RemoteViews-compatible widget views.
