# Build MUTO — Native Android System Silence Controller

Build a complete, production-quality native Android application called **MUTO**.

## Product concept

MUTO is a one-purpose Android utility:

> **One action. Total silence. One action. Everything back.**

The user should be able to open MUTO and tap one large control to:

1. Silence configured phone sound streams.
2. Enable Do Not Disturb when configured.
3. Remember the exact previous audio state.
4. Restore the exact previous audio state with one more tap.
5. Automatically perform the same operation according to recurring schedules.
6. Control the same state from an Android Quick Settings Tile.
7. Control the same state from a home-screen widget.

The application must be completely native Android. Do not build a web wrapper.

---

# 1. Platform and technology

Use:

* Native Android
* Kotlin
* Jetpack Compose
* Material 3 / Material 3 Expressive principles
* Android SDK 35+ APIs as appropriate for the current Android platform
* Architecture that remains compatible with the requested Android 17 target/API environment
* Kotlin Coroutines
* StateFlow
* ViewModel
* DataStore for persistent preferences/state
* Android AlarmManager or the appropriate modern Android scheduling mechanism for recurring schedules
* AppWidget/Glance for the home-screen widget where appropriate
* TileService for Quick Settings
* Android AudioManager for audio stream control
* NotificationManager / Notification Policy Access APIs for DND
* No unnecessary network dependency

Keep the application offline-first and local-only.

Do not add:

* Login
* Cloud account
* Analytics
* Backend
* Ads
* Unnecessary network permissions
* History page
* Social features

---

# 2. Important Android system behavior

This is a system-control utility, so do not fake system behavior.

The app must verify whether Android actually allowed each operation.

If permission is missing, clearly explain what permission is required and provide a button to open the relevant Android Settings screen.

Never display "Muted" when the operation failed.

Never silently ignore permission failures.

---

# 3. Audio behavior

Create a shared central component:

`MuteController`

All entry points must use the same controller:

* Main app
* Quick Settings Tile
* Home-screen widget
* Scheduled actions

Do NOT duplicate mute/restore business logic.

The controller should manage:

* Media volume
* Ringtone volume
* Notification volume where Android permits independent control
* System sounds
* Call/ringer behavior
* Alarm volume as an optional setting

Before muting, create a persistent snapshot containing every volume/state that MUTO intends to modify.

Example:

Media: 65%
Ringtone: 80%
Notification: 50%
Alarm: 100%
System: 30%
Ringer mode: NORMAL
DND: OFF

After MUTO activates:

Media: 0
Ringtone: 0
Notification: 0
System: 0
DND: ON

When restoring:

Restore the exact values from the snapshot.

NEVER restore everything to arbitrary values such as 50%, 80%, or 100%.

---

# 4. Alarm safety

Alarm muting must be OFF by default.

Default behavior:

* Media → mute
* Ringtone → mute
* Notifications → mute
* System sounds → mute
* Calls → controlled by ringer/DND behavior
* Alarm → remain audible

Provide:

`Mute alarms`

with default value:

`OFF`

If the user explicitly enables it, include the alarm stream in the snapshot and mute/restore process.

This is extremely important because users may rely on alarms.

---

# 5. DND behavior

DND must be state-aware.

Case 1:

Before MUTO:

DND = OFF

MUTO activation:

DND = ON

MUTO owns the DND change.

Restore:

DND = OFF

Case 2:

Before MUTO:

DND = ON

MUTO activation:

DND remains ON

MUTO does not claim ownership.

Restore:

DND remains ON.

Never turn off a DND state that existed before MUTO.

Persist whether MUTO itself changed DND.

Expose this state clearly in the UI.

---

# 6. Main screen

The main screen must be extremely simple.

No bottom navigation.

No dashboard.

No history.

No unnecessary screens.

Suggested hierarchy:

Top app area:

`MUTO.`

`One tap. Total silence.`

Top-right:

Settings icon

Then a large hero control.

READY:

`READY`

`Tap to silence`

SILENT:

`SILENT`

`Tap to restore`

Use a large expressive circular/rounded control with smooth state animation.

The primary action must be the most visually important element on the screen.

Below it show:

## Sound

Media — 0%
Ringtone — 0%
Notifications — 0%
System — 0%
Calls — Silent
Alarms — 100%

Do not create sliders on the main screen.

This is a status display, not a second settings panel.

Then:

## Do Not Disturb

Show:

`ON`

`MUTO is controlling DND`

or:

`Already on`

`MUTO did not change DND`

Then show the next scheduled action when scheduling is enabled.

Example:

`Next silence`

`Today · 10:00 PM`

---

# 7. Scheduling system

Scheduling is a major feature.

Create a dedicated:

`Schedules`

screen.

Users can create recurring silence schedules.

Each schedule should contain:

* Name
* Enabled/disabled
* Start time
* End time
* Selected days
* Whether alarm muting is used
* Whether DND is enabled
* Sound categories to mute

Examples:

### College

Monday
Tuesday
Wednesday
Thursday
Friday

09:00 → 16:00

### Night

Every day

23:00 → 06:30

Support overnight schedules where the end time is on the next day.

Support multiple schedules.

Each schedule should have:

* Edit
* Enable/disable
* Delete

Display a clean schedule card:

`College`

`Mon–Fri · 9:00 AM – 4:00 PM`

`Enabled`

Also display:

`Next activation`

and:

`Next restore`

---

# 8. Schedule conflict handling

Design a robust schedule engine.

If two schedules overlap:

Do not restore sound simply because one schedule ended while another schedule is still active.

Example:

Schedule A:

09:00–16:00

Schedule B:

14:00–18:00

At 16:00 MUTO must remain silent because Schedule B is still active.

Only restore when no active silence schedule remains.

Handle:

* overlapping schedules
* overnight schedules
* disabled schedules
* device reboot
* timezone changes
* manual mute during an active schedule
* manual restore during an active schedule

Never corrupt the original audio snapshot.

---

# 9. Manual override

Support manual actions even when schedules exist.

Main screen:

`Mute now`

or

`Restore now`

If a schedule is currently active, clearly explain that manual restoration may be temporary until the schedule activates again.

Do not create confusing hidden behavior.

---

# 10. Quick Settings Tile

Implement a native Android Quick Settings Tile.

States:

READY:

`MUTO`
`Ready`

SILENT:

`MUTO`
`Silent`

Tapping the tile must call the same `MuteController`.

Do not duplicate business logic.

Update the tile immediately when state changes.

The tile must stay synchronized with the main app.

Provide a way from the app to explain:

`Add MUTO to Quick Settings`

---

# 11. Home-screen widget

Create a native home-screen widget.

Minimal design:

MUTO

🔇

`READY`

or

`SILENT`

Tapping the widget performs the same operation as the main button and Quick Settings Tile.

Widget state must stay synchronized with the application.

Provide appropriate widget sizes where practical.

The widget must not contain unnecessary information.

---

# 12. Settings screen

Create a single clean settings screen.

Sections:

## SOUND

Mute media
`Music, podcasts and video`

Mute ringtone
`Incoming call ring volume`

Mute notifications
`Notification sounds`

Mute system sounds
`Touch and interface sounds`

Mute alarms
`Off by default to protect alarms`

Use Material switches.

## DO NOT DISTURB

Enable DND when muting

Grant/review DND access

Open DND settings

## SHORTCUTS

Quick Settings Tile

Home-screen Widget

## SCHEDULES

Scheduled silence

Manage schedules

Allow precise schedules where applicable

## APPEARANCE

Theme:

* System
* Light
* Dark

Dynamic colors:

ON/OFF

## ABOUT

MUTO

Version

Privacy

Open-source licenses

---

# 13. Onboarding

First launch should explain the purpose before opening Android settings.

Show:

`Make silence one tap away.`

`MUTO needs access to change sound levels and control Do Not Disturb.`

`Nothing is uploaded. Your preferences stay on this device.`

Button:

`Set up MUTO`

Then guide the user through required permissions.

Do not dump the user directly into Android Settings without explanation.

---

# 14. Material 3 design

The visual quality should follow the supplied MUTO design specification and use the referenced Self-Attendance project as a UI/interaction quality reference.

Reference repository:

https://github.com/MdFarhan0/Self-Attendance

Study the reference project's:

* Compose structure
* Material 3 usage
* spacing
* rounded surfaces
* animations
* component hierarchy
* typography
* interaction patterns
* dark/light theme implementation

IMPORTANT:

Use the repository only as a design and interaction reference.

Do NOT copy its:

* branding
* logo
* source code
* proprietary assets
* exact screens
* exact content

MUTO must have its own visual identity.

---

# 15. Visual direction

Use modern Material 3 Expressive design.

Use:

* dynamicColorScheme
* MaterialTheme
* surfaceContainer
* surfaceContainerLow
* surfaceContainerHigh
* primary
* onPrimary
* onSurface
* onSurfaceVariant
* outline
* outlineVariant
* errorContainer

Do not hard-code every color.

Support:

* system theme
* light theme
* dark theme
* dynamic Android colors

Dark mode should feel premium.

Light mode must also look polished.

Use rounded cards with subtle tonal hierarchy.

Avoid:

* excessive gradients
* neon colors
* excessive glassmorphism
* giant shadows
* unnecessary borders
* excessive animations

---

# 16. Motion

Create a reusable motion system.

Use smooth state-driven transitions.

Recommended durations:

Fast: 120ms
Normal: 300ms
Medium: 400ms
Slow: 500ms

When changing:

READY → SILENT

animate:

* primary control
* icon
* status text
* sound status
* DND status

When changing:

SILENT → READY

animate the reverse transition.

Respect Android reduced-motion/accessibility settings.

---

# 17. Haptics

Use haptics intentionally.

Successful mute:

`HapticFeedbackType.Confirm`

Successful restore:

`HapticFeedbackType.Confirm`

Permission failure:

`HapticFeedbackType.Reject`

Long press where applicable:

`HapticFeedbackType.LongPress`

Never vibrate continuously.

---

# 18. Accessibility

Support:

* TalkBack
* semantic labels
* minimum touch targets
* dynamic font scaling
* high contrast
* reduced motion

Primary control content descriptions:

READY:

`Mute phone`

SILENT:

`Restore phone sound`

Never communicate state only through color.

---

# 19. Error handling

Create specific error states.

Example:

`Couldn't activate silence`

`Android didn't allow MUTO to change one or more sound settings.`

Buttons:

`Open Settings`

`Try Again`

Do not use vague messages such as:

`Something went wrong`

when a specific explanation is available.

---

# 20. External system changes

The user may manually change volume, ringer mode, or DND outside MUTO.

The app must handle external state changes safely.

When MUTO is SILENT:

* refresh displayed state
* don't overwrite user changes unnecessarily
* maintain the original restoration snapshot
* clearly distinguish MUTO-controlled state from externally changed state

When restoring, restore the snapshot that MUTO actually captured.

---

# 21. Reboot handling

After device reboot:

* restore schedule state
* reschedule required alarms/jobs
* update widget
* update Quick Settings Tile
* determine whether a silence schedule should currently be active
* recover safely without corrupting the audio snapshot

Do not start an unnecessary permanent background service.

---

# 22. Persistence

Use DataStore for:

* user preferences
* sound category settings
* DND preference
* theme preference
* schedule definitions
* current MUTO state
* mute snapshot
* DND ownership state

Do not store sensitive information.

Everything should remain local.

---

# 23. Architecture

Use clean separation.

Suggested structure:

`ui/`

`ui/home/`

`ui/settings/`

`ui/schedules/`

`ui/onboarding/`

`ui/components/`

`domain/`

`domain/model/`

`domain/usecase/`

`domain/controller/`

`data/`

`data/preferences/`

`data/schedules/`

`system/`

`system/audio/`

`system/dnd/`

`system/tile/`

`system/widget/`

`system/scheduler/`

Use a single shared:

`MuteController`

with clear state:

```text
READY
SILENT
```

and supporting state such as:

```text
MuteSnapshot
DndOwnership
ActiveSchedules
```

---

# 24. App icon

Create an original MUTO adaptive icon.

Concept:

* abstract M
* subtle mute/sound relationship
* simple geometric construction
* monochrome
* recognizable at launcher size
* no text

Support:

* adaptive icon
* foreground vector
* background
* monochrome themed icon

The monochrome icon must allow Android to recolor it.

---

# 25. Privacy

MUTO should be local-only.

No:

* account
* server
* cloud sync
* analytics
* advertising
* tracking

Do not request INTERNET permission unless absolutely required.

Add a simple privacy section explaining that sound preferences and schedules remain on the device.

---

# 26. Testing

Create tests for the core logic.

Test:

1. Mute from normal state.
2. Restore exact previous state.
3. Mute when DND is already ON.
4. Restore without disabling pre-existing DND.
5. Mute with alarm muting OFF.
6. Mute with alarm muting ON.
7. Overlapping schedules.
8. Overnight schedule.
9. Disabled schedule.
10. Manual mute during schedule.
11. Manual restore during schedule.
12. Device reboot.
13. Permission denied.
14. Partial system-control failure.
15. Quick Settings Tile synchronization.
16. Widget synchronization.

---

# 27. Definition of done

Do not stop at UI mockups.

The project is complete only when:

* Android project builds successfully
* application launches
* Material 3 works
* dynamic colors work
* light mode works
* dark mode works
* adaptive icon works
* themed icon works
* onboarding works
* required permissions are handled
* mute works
* restore works
* exact audio snapshot is restored
* DND behavior is state-aware
* alarm protection works
* schedules work
* overlapping schedules work
* overnight schedules work
* reboot recovery works
* Quick Settings Tile works
* home-screen widget works
* animations work
* haptics work where supported
* accessibility labels exist
* reduced-motion behavior exists
* external state changes are handled
* error states are handled
* no unnecessary permanent background service exists
* no unnecessary network permission exists
* no history screen exists
* no bottom navigation exists
* no copied third-party branding/assets are used
* release build succeeds

Finally, compile the complete project, fix all compilation/runtime errors, and leave the project in a clean production-ready state.

The final product should feel like a polished Android system utility, not a demo.

Core philosophy:

> **One action. Total silence.**
> **One action. Everything back.**
