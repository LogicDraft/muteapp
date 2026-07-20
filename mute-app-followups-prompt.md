# Build Prompt: MUTE. — Per-Schedule DND Level, Next-Schedule Glance, Device Controls, Undo

Paste everything below into your coding agent, run from inside the `MuteApp/` project root.
Builds on the multi-schedule feature — implement that first if it isn't in yet.

---

## The prompt

Four independent, fairly small additions. Do them in this order — each is quick to verify in
isolation before moving to the next.

### 1. Per-schedule Do Not Disturb level

Right now Total Silence vs. Priority Only is one global setting. Let each schedule optionally
carry its own, so e.g. "Sleep" can use Total Silence while "Meeting" uses Priority Only.

- Add `dndLevel: PrefsManager.DndLevel` to the `Schedule` data class, defaulting to whatever the
  current global setting is at creation time (so existing behavior doesn't silently change for
  anyone who already has schedules set up).
- Add the same Total Silence / Priority Only chip choice already used in the global DND setting
  to the schedule add/edit screen.
- `MuteController.mute()` currently reads the DND level from the global `PrefsManager` setting.
  Give it an optional override parameter instead (defaulting to null → use the global setting),
  and have a schedule's start-trigger pass its own level through. Manual mutes (tile, widget,
  notification, in-app button) keep using the global setting exactly as today — this change is
  scoped to scheduled mutes only.
- Leave "keep alarm audible" (the exclude-alarm toggle) global-only for now — don't extend that
  one per-schedule too, that's more scope than this pass needs.

### 2. "Next schedule" line on the main screen

The main screen is rarely opened, but when it is, it currently only shows ACTIVE/MUTED and a
static hint. Make it say something useful about what's coming:

- When the phone is **not** currently muted: if any enabled schedule exists, show a small line
  under the dial — "Next: {label} at {time}" — for whichever one triggers soonest. If none
  exist, leave the current static hint text as-is.
- When the phone **is** muted and a schedule caused it: show "Until {end time}" instead of the
  generic "Tap to restore sound" hint (this mirrors the tile/widget subtitle logic from the
  scheduling prompt — reuse that same "is exactly one schedule responsible" check rather than
  writing a second version of it).
- When muted manually, keep the existing generic hint unchanged.
- Keep the time format simple — just the clock time is fine; don't build out "today/tomorrow"
  relative-day logic for this, it's a nice-to-have label, not worth the added complexity.

### 3. Add MUTE as a Device Control

Android's Device Controls surface (`android.service.controls`, API 30+) isn't just for smart-home
devices — any app can contribute one, and it's a genuinely on-brand entry point for this app
specifically: a system-level toggle, no app UI, exactly the philosophy MUTE is already built
around.

- Add a `ControlsProviderService` implementation. Its API is built on Java's `Flow.Publisher`
  (`java.util.concurrent.Flow`), not `kotlinx.coroutines.flow.Flow` directly — add
  `org.jetbrains.kotlinx:kotlinx-coroutines-jdk9` and use its `.asPublisher()` adapter to expose
  a normal Kotlin `Flow` (built from the existing `MuteStateBus`/`MuteController.isMuted()`
  state) as the required `Flow.Publisher`.
- Declare the service in the manifest with the `ControlsProviderService` intent-filter and the
  `BIND_CONTROLS` permission, plus a label describing the control.
- One control, a toggle: current state reflects `MuteController.isMuted()`, and tapping it in the
  system panel calls `MuteController.toggle()` — the same function every other entry point
  already uses, so there's no new logic here beyond wiring it up.
- This surface's exact entry point (power-menu long-press vs. a Settings page vs. QS panel
  integration) has varied across Android versions and OEM skins — don't hardcode assumptions
  about where the user finds it; the API registration is what matters, the system places it.

### 4. Undo on schedule delete

The multi-schedule prompt specified deletion behind a confirmation dialog, reached only from the
edit screen (deliberately not a list swipe). Add a second, lighter safety net on top of that:

- After a confirmed delete, remove the schedule from the list immediately and show a `Snackbar`
  with an "Undo" action.
- Tapping Undo re-inserts the exact same schedule (same id, same fields) and re-arms its alarms
  if it was enabled — don't treat it as creating a new schedule.
- If the snackbar times out or is dismissed without tapping Undo, the deletion is already final
  (it was removed immediately, not deferred) — no further action needed.

---

## Note for whoever runs this

Item 3 (Device Controls) is the most self-contained of the four — if you want to verify things
incrementally, it's safe to build and test independently of 1, 2, and 4, which all touch the
scheduling code more directly.
