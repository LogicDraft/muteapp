# Build Prompt: MUTE. — Material 3 UI, Dynamic Color, and Material Widgets

Paste everything below into your coding agent, run from inside the `MuteApp/` project root.

---

## The prompt

Continue work on the MUTE. Android app. The reference screenshots below (from another app,
"MDify") show UI patterns worth adopting — bring the *structure and components* over, not the
literal color palette:

- Material3 tonal surface cards for list rows, with a leading icon, title, and subtitle
- A settings **hub** screen linking out to focused subscreens, rather than one long flat screen
- An **About** screen with version info and a single pill split into two tap targets (Email |
  GitHub)
- A **Look & Feel** subscreen containing a **Dynamic Colors** toggle — "automatically set the
  app theme according to the device wallpaper" — off by default, alongside the existing theme
  setting
- A slowly animated decorative icon cluster at the top of the Settings hub

MUTE keeps its own black + signal-red identity as the default look. Dynamic Color is additive:
off by default, and when off the app looks exactly like it does today. Don't re-theme the app in
the reference screenshots' green — that palette belongs to the other app.

### 1. Dynamic Color (Material You)

- Add a persisted preference (`PrefsManager`): dynamic color on/off, default **off**.
- In `Theme.kt`: when the preference is on *and* `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`,
  use `dynamicDarkColorScheme(context)` (from `androidx.compose.material3`) instead of the
  existing fixed `MuteColorScheme`. MUTE is fixed-dark only — use the dynamic *dark* scheme
  specifically, don't add a light-mode switcher as a side effect of this.
- Below API 31, show the toggle disabled with a one-line "Needs Android 12+" note rather than
  hiding it — keep the Look & Feel screen's layout consistent across OS versions.
- Leave the QS tile icon, notification icon, and widget as brand red/black regardless of this
  toggle — re-theming those from outside Compose is a much bigger lift than it's worth here, and
  a consistent brand mark on the tile/notification is arguably better anyway.

### 2. Material 3 component pass

- Replace the current custom row/chip components in the settings screens with Material3
  `ListItem` (leading icon, headline, supporting text, trailing switch/value) on `Card`/`Surface`
  using `MaterialTheme.colorScheme.surfaceContainer`, instead of the flat 1dp-outline rows used
  today.
- Move card corner radii from the current sharp 2dp up to Material3-appropriate large radii
  (~20–24dp) — a deliberate softening in line with "Material UI" being the explicit direction
  here.
- Use real Material icons (`material-icons-extended`, or a hand-picked equivalent set if you'd
  rather not pull in the full extended-icons dependency) instead of plain text glyphs.
- Standard `TopAppBar` / `CenterAlignedTopAppBar` for screen headers instead of the current
  manual back-arrow row.

### 3. Settings restructure — hub + subscreens

Split the current single flat settings screen into:

- **Settings (hub)** — the animated header (below), a tagline, then rows linking to:
  - **Do Not Disturb** — exclude-alarm toggle + DND level choice (and Scheduled Silence, once
    that feature lands)
  - **Notifications** — the persistent-notification toggle
  - **Look & Feel** — the new Dynamic Colors toggle
  - **About** — version, a short developer tagline, and an Email | GitHub split pill (pull the
    actual contact links from the README rather than inventing placeholders)
- Plain Activities are fine for these, consistent with how the app is already structured — no
  need to pull in Compose Navigation just for four subscreens.
- Each subscreen keeps its existing `PrefsManager` read/write calls; this is a layout
  reorganization, not a data-model change.

### 4. Rotating decorative header

- Add a small animated illustration at the top of the Settings hub, in the spirit of the
  reference app's rotating gear cluster, but built from MUTE's own ring/dot motif instead of
  gear icons — e.g. two or three offset rings, slowly counter-rotating
  (`rememberInfiniteTransition` driving `rotationZ`). Keep it as MUTE's identity, not a copy of
  someone else's illustration.
- Keep the motion slow and subtle — premium restraint, not a spinner — and make sure it actually
  loops smoothly.
- Respect the system's reduce-motion setting: pause or substantially slow it when that's on,
  same requirement as the rest of the animation work already scoped for this app.

### 5. Material widgets (Glance migration)

Classic `RemoteViews` widgets — what `MuteWidgetProvider` uses today — don't pick up Material You
automatically. Getting a widget that actually "follows Material UI" means migrating to
**Jetpack Glance**, which is the modern, Compose-style widget framework with built-in Material3
theming support:

- Add `androidx.glance:glance-appwidget:1.1.1` and `androidx.glance:glance-material3:1.1.1`
- Rewrite `MuteWidgetProvider` as a `GlanceAppWidget` + `GlanceAppWidgetReceiver`, wrapped in
  `GlanceTheme { }` — thread the same dynamic-color preference through so the widget matches
  whatever the in-app toggle is set to, falling back to a fixed brand-red `ColorProviders` on
  pre-S devices or when dynamic color is off
- Preserve current behavior exactly: instant visual update on toggle from any entry point (tile,
  widget tap, notification action, in-app button), same tap target, same muted/active states —
  this is a rendering-layer migration, not a behavior change
- While in here, fix the previously-flagged missing `previewImage` for the widget picker —
  Glance's preview mechanism works differently from classic RemoteViews, so re-verify it rather
  than assuming the old fix still applies

---

## Note for whoever runs this

This is the largest pass yet — Material3 componentry, dynamic color, a settings restructure, and
a widget framework migration all touch different parts of the app. If it's more efficient to
split across a couple of sessions, a sensible boundary is: (1) dynamic color + Material3
component pass + settings restructure first, then (2) the Glance widget migration on its own,
since it's the most structurally different piece and the easiest to verify in isolation.
