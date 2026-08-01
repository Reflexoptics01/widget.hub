# Material You 3 Expressive Nothing Redesign

## Goal

Give the Tasbeeh Widget Hub a cohesive Material You 3 Expressive visual system with a pure black Nothing-inspired base, red accent language, and purposeful motion across the main app and widget.

## Visual direction

- Base surfaces are near-black/pure black with high-contrast white text.
- Primary accent is Nothing-inspired red, used for progress, active controls, and completion states.
- Material You expressive behavior appears through large rounded surfaces, pill controls, generous spacing, and animated state transitions.
- Dynamic system color may influence secondary surfaces/text only when available; it must not replace the black/red identity.

## Main app

- Use a dark edge-to-edge layout with a large count hero.
- Display lifetime count and current goal in a prominent expressive card.
- Animate count changes with a short scale/bounce and animate progress changes smoothly.
- Animate reset with a brief fade/scale transition.
- Use rounded red-accent controls for goal selection and reminder settings.
- Reminder cards show enabled state, selected time, and the relevant recitation copy.

## Widget

- Keep the existing 2x2 default and resizable behavior.
- Use a black rounded surface with white numerals and red progress/accent controls.
- Tapping the count remains the primary increment action.
- Goal and reset controls remain compact and clearly separated.
- Use supported RemoteViews only; motion is represented by state refreshes and animated values where supported.

## Accessibility and compatibility

- Maintain minimum SDK 23 and target SDK 35.
- Preserve content descriptions, touch target sizes, and contrast.
- Do not use unsupported widget views or continuous custom widget animations.

## Verification

- Existing counter, reminder, and formatter tests remain green.
- Add tests for display styling/state mapping where practical.
- Build and inspect the signed APK metadata.
