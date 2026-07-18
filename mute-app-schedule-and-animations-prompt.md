# Build Prompt: MUTE. — Scheduled Silence + Premium Animations

Paste everything below into your coding agent, run from inside the `MuteApp/` project root.

---

## The prompt

replace in entire/anyever gowtham with logicdraftlabs

Continue work on the MUTE. Android app (`com.gowtham.mute`). Add two things, in this order:

### 1. Scheduled Silence — a daily auto-mute/unmute window

This is a **new, separate** feature from the existing "auto-restore timer" in Settings (which is
a safety-net countdown that starts *after* a manual mute). Don't merge them — they solve
different problems. Scheduled Silence runs on its own daily clock regardless of whether the user
has manually toggled anything.

**Behavior:**
- New "Scheduled Silence" section in Settings:
  - A master switch to turn the whole feature on/off (default off)
  - A start-time picker and an end-time picker (Material3 `TimePicker`, respects the system's
    12/24-hour format — it's `@ExperimentalMaterial3Api`, so opt in accordingly)
  - Handle windows that cross midnight correctly (e.g. 22:00 → 07:00) — this is the most common
    real case (sleep hours), so test it explicitly, don't just assume start < end
  - Runs every day for v1. A day-of-week picker (e.g. weekdays only) is a reasonable next step if
    there's time, but a solid daily schedule beats a half-finished custom-days picker — don't
    let this scope-creep the core feature
- Implementation: two repeating alarms (start, end), each rescheduling itself for the next
  occurrence when it fires. `AlarmManager.setAndAllowWhileIdle` is precise enough for this —
  don't request the `SCHEDULE_EXACT_ALARM` permission unless timing genuinely needs to be
  accurate to the minute; that's a separate permission screen and not worth asking for here
- Re-arm both alarms in `BootReceiver` (alarms don't survive reboot), and reschedule immediately
  whenever the user edits or toggles the schedule

**The important edge case — don't skip this:**
`PrefsManager` currently tracks *whether* the app muted the phone, but not *why*. Scheduled and
manual mutes will fight each other unless that's fixed:
- If the user manually unmutes during a scheduled silent window, the schedule must NOT
  immediately re-mute them — respect the override until the next scheduled cycle
- If the user manually mutes outside a scheduled window, the schedule's next "end" alarm must
  NOT unmute them — that mute wasn't the schedule's to end
- Add a mute-source field (e.g. `MANUAL` vs `SCHEDULED`) alongside the existing muted flag, and
  have the schedule's start/end alarms only ever act on state they themselves own

**Surfaces to update:**
- QS tile subtitle and widget text should reflect it when relevant (e.g. "Muted · until 7:00 AM"
  when the current mute came from the schedule, vs. just "Muted" for a manual one)
- Make it visually obvious in Settings whether the schedule is active right now or just
  configured-but-off

### 2. Premium animations and transitions

The app works correctly but transitions are close to Compose defaults right now. Raise all of
these to feel deliberate and tactile, not templated — this is the whole point of the brand:

- **The mute/unmute dial** (main screen): replace the current fade with something that actually
  reads as an event — e.g. a ring that briefly expands and releases (spring physics, not linear
  easing) at the moment of the tap, synced to the haptic pulse that already fires in
  `MuteController`. The white ↔ red color change should morph, not cut.
- **The ACTIVE/MUTED label swap**: something with more character than a plain crossfade — a
  vertical slide/flip reads well against the dot-matrix-inspired numeral styling already in
  `Type.kt`, similar to a split-flap display digit changing.
- **Ambient state, not just the transition moment**: while muted, give the ring a slow, subtle
  breathing glow (opacity or stroke-width oscillation) so the screen doesn't go visually dead
  during the "nothing is happening" muted state — keep it understated, this should read as
  premium restraint, not a loading spinner.
- **Screen-to-screen**: animate the transition into and out of the Settings screen (shared
  element / container transform, or at minimum a considered slide+fade rather than the default
  activity transition) and support Android 14+ predictive back gesture properly rather than
  leaving it on default behavior.
- **Respect reduced motion**: every animation added here needs to check the system's
  reduce-motion accessibility setting and fall back to an instant or minimal-motion version —
  don't skip this for the sake of the effect.
- Widgets and the QS tile have inherently limited animation support (RemoteViews) — don't burn
  time trying to force rich motion into those; put the animation budget into the in-app screen
  where it's actually visible.

---

## Note for whoever runs this

Time pickers and predictive back need a fairly recent Compose/AGP setup — if the agent hits
missing-API errors, check `gradle/libs.versions.toml` first before downgrading the feature.
