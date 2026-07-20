# Build Prompt: MUTE. — Fully Material 3, Dynamic Color by Default

Paste everything below into your coding agent, run from inside the `MuteApp/` project root.

**This revises the earlier Material3/dynamic-color prompt.** That version specified dynamic
color as an opt-in toggle (default off) with the fixed black/red scheme as the app's identity —
which is exactly why the app still looks like "Nothing UI" after building it: it was built
correctly to that earlier spec. This prompt flips the default. Read section 1 before touching
anything else.

---

## The prompt

### 1. Dynamic color is the default — not a setting someone has to find

- On Android 12+ (API 31+): use `dynamicDarkColorScheme(context)` /
  `dynamicLightColorScheme(context)` automatically, choosing between them based on the system's
  current light/dark setting (`isSystemInDarkTheme()`). No toggle gates this — it's just how the
  app looks.
- Below API 31 (the app still supports down to API 26, where dynamic color doesn't exist at the
  OS level): fall back to Compose Material3's own baseline `lightColorScheme()` /
  `darkColorScheme()` — the stock Material palette with no custom overrides — instead of the old
  fixed black/red scheme.
- The app should now also follow the system's light/dark setting instead of being forced dark.
  That's a deliberate change from the original fixed-dark-only brief, and it's part of what
  "genuinely Material" means here.
- Remove the black+red `MuteColorScheme` as the app's identity. If it's worth keeping around
  later as a selectable "Classic" look in Settings, that's a reasonable follow-up request — but
  don't build that escape hatch into this pass unless it's actually asked for. The point right
  now is that stock Material is what the app looks like by default, full stop.

### 2. Replace bespoke components with stock Material 3 ones

Go screen by screen and replace anything that's a custom reimplementation of something Material
already provides:

- **Shapes**: drop the hardcoded sharp 2dp `RoundedCornerShape` used throughout in favor of
  `MaterialTheme.shapes` — the default M3 shape scale, not a fixed custom radius everywhere.
- **Typography**: drop the custom monospace "dot-matrix" type scale in `Type.kt` for Compose
  Material3's default `Typography()` — the stock type scale, not a brand-specific one.
- **`ChoiceChip`** (the custom DND-level / auto-restore-timer picker) → Material3 `FilterChip`,
  or `SegmentedButton` where the options are mutually exclusive (DND level, timer length) — that's
  the more idiomatic fit for those specifically.
- **Settings rows** → Material3 `ListItem` on `Card`/`Surface` using the standard tonal
  `surfaceContainer` roles, if this wasn't already done in an earlier pass.
- **Icons** → the standard Material icon set (rounded style) throughout.
- **The main toggle dial**: worth keeping as a deliberate hero element rather than flattening it
  into a plain switch, but restyle it in Material's own language instead of the current
  bespoke bordered `Box` — a large circular `Surface` (or `FilledIconButton`-style container)
  using `colorScheme.primary` / `primaryContainer` tonal roles, with a real `indication` ripple
  on tap. Right now it's a bare `clickable` on a `Box`, so it has no Material touch feedback at
  all — that's worth fixing regardless of anything else here. The ring/circle shape itself can
  stay; everything about how it's colored and how it responds to touch shouldn't.

### 3. Motion

Keep the animation work from the earlier "premium animations" prompt, but revisit timing and
easing so it reads as consistent with Material's motion language rather than fighting it — if
anything was tuned specifically for a "Nothing OS" feel, that reference point no longer applies.

### 4. Widgets

- If the Glance migration from the earlier prompt is already done: make dynamic color the
  widget's default too — the same change as section 1, not gated behind an in-app toggle, since
  that toggle is going away.
- If it isn't done yet: build it now per the earlier prompt's spec, but with dynamic color as
  the default from the start rather than building a fixed-scheme version first and switching it
  later.

---

## Note for whoever runs this

This is a genuine reversal of a specific earlier decision (brand color as default, dynamic color
opt-in). If the previous prompt was already built, this is an edit pass on existing code, not new
scaffolding — find and replace the specific pieces called out above rather than starting over.
