# Build Prompt: MUTE. — Multiple Time Schedules

Paste everything below into your coding agent, run from inside the `MuteApp/` project root.

---

## The prompt

This extends — and if it hasn't been built yet, replaces outright — the single-window "Scheduled
Silence" feature from the earlier scheduling prompt. Instead of one daily on/off window, support
a **list of named schedules**, each independently configurable and toggleable, the way a clock
app handles multiple alarms. If the single-window version isn't built yet, build this
multi-schedule version directly instead of building the simple one first just to replace it a
moment later.

### 1. Data model

- `Schedule(id, label, startMinuteOfDay, endMinuteOfDay, days: Set<DayOfWeek>, enabled: Boolean)`
- Store the list as one JSON-encoded string in `PrefsManager`, via
  `kotlinx.serialization-json` (add `org.jetbrains.kotlinx:kotlinx-serialization-json` plus the
  Kotlin serialization Gradle plugin). This is a small, proportionate addition — don't reach for
  Room for what's realistically a handful of rows; that's meaningfully more setup (entities,
  DAO, schema versioning) than this needs. Update the README's "no third-party dependencies"
  line once this lands, since that claim stops being true.
- Each schedule needs a stable unique `id` (a UUID, generated on creation) — this is what keeps
  AlarmManager `PendingIntent`s and mute-source tracking distinct per schedule.

### 2. Schedules screen + the "+" add button

- A "Schedules" subscreen (under the Settings hub if that restructure has landed, otherwise its
  own screen for now) listing every saved schedule as a card: label, time range (e.g. "10:00 PM
  – 7:00 AM"), day pattern (e.g. "Every day"), and a `Switch` to enable/disable that one schedule
  without deleting it.
- A `FloatingActionButton` (`+`) opens an add screen; tapping an existing card opens the same
  screen pre-filled, for editing. Fields: label (optional text, default to something like
  "Schedule 1" if left blank), start time and end time (`TimePicker`, handle windows that cross
  midnight), and a day picker (presets: Every day / Weekdays / Weekends, plus custom
  multi-select — every day is a fine default).
- Deleting only happens from inside the edit screen, behind a confirmation dialog — not a list
  swipe gesture. This is deliberate: swipe-to-delete is a bad fit for a settings feature people
  set up once and rarely revisit.
- Empty state (no schedules yet): a short line plus the same `+` button as the obvious next step
  — don't leave it blank.

### 3. Alarms: one pair per schedule

- Each enabled schedule gets its own start alarm and end alarm, with a unique `PendingIntent`
  request code derived from its `id` — not a fixed constant, since multiple schedules need to
  coexist without overwriting each other's pending alarms.
- Pass the schedule `id` and whether it's a START or END trigger as Intent extras, so the
  receiver knows exactly which schedule fired.
- Re-arm every enabled schedule's alarms in `BootReceiver`, and reschedule immediately whenever
  a schedule is added, edited, deleted, or toggled.

### 4. The overlap edge case — handle this explicitly

With more than one schedule, two can be active at once (e.g. a 9:00–9:30 "meeting" schedule
inside a 22:00–07:00 "sleep" schedule spanning midnight). Don't let one schedule's end trigger
unmute the phone out from under another:

- On any schedule's END trigger, before unmuting: check whether "now" falls inside the window of
  any *other* currently-enabled schedule. If it does, stay muted — that other schedule still owns
  the silence.
- Extend the `MANUAL` vs `SCHEDULED` mute-source tracking from the earlier prompt to carry the
  responsible schedule's id (e.g. `SCHEDULED(scheduleId)` rather than a bare enum), so this check
  has something concrete to look up.
- A manual unmute during any active schedule window still overrides everything, same rule as
  before — the user's direct action always wins over any schedule.

### 5. Surfacing it elsewhere

- QS tile subtitle / widget text: when exactly one schedule is currently responsible for the
  mute, showing its end time is fine (e.g. "Muted · until 7:00 AM"). If it's more ambiguous than
  that (manual mute, or overlapping schedules), just show "Muted" plainly rather than guessing
  which time to surface.

---

## Note for whoever runs this

This is a genuine data-model change — a list instead of a single value. If the single-window
version from the earlier prompt was already partially built, make sure its now-obsolete fields
and scheduling code are actually removed rather than left dangling alongside the new list.
