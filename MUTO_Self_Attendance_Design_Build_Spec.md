# MUTO — Complete Android App Design & Build Specification
## Design and engineering brief for an AI coding agent

> **Reference project:** https://github.com/MdFarhan0/Self-Attendance  
> **Target app:** MUTO — one-tap total silence / sound-state manager
>
> **Important:** Use the reference project's *design system, interaction quality, animation philosophy, architecture ideas, and implementation patterns as inspiration*. Do **not** copy its source code, branding, assets, names, or proprietary visual artwork verbatim. MUTO must have its own identity.

---

# 1. Mission

Build **MUTO**, a native Android application whose primary purpose is extremely simple:

> **One tap = phone becomes silent.  
> One tap again = phone returns exactly to the previous sound state.**

The app should make this action feel instant, intentional, reliable, and premium.

The application must not feel like a settings utility with a collection of complicated controls. The main experience should remain focused on one action.

---

# 2. Reference Design Direction

Study the public **MdFarhan0/Self-Attendance** repository carefully before implementing MUTO.

The reference project is a Kotlin/Android application using modern Android UI concepts, including Jetpack Compose and Material 3 Expressive principles. Its documented design work emphasizes:

- polished Material 3 Expressive components
- clean spacing and layouts
- micro-interactions
- haptic feedback
- animated component state changes
- smooth content-size transitions
- expressive buttons
- dynamic Material colors
- responsive interaction states
- reusable Compose components
- subtle rather than distracting motion

The repository also contains an `ANIMATION_PRD.md` describing interaction patterns such as button-width animation, animated content-size transitions, long-press state changes, color transitions, and haptic feedback.

Use these principles as the **quality bar for MUTO**.

Do not turn MUTO into an attendance app or reproduce attendance-specific screens/features.

---

# 3. Product Identity

## Name

**MUTO**

Visual wordmark:

```text
MUTO.
```

The period is intentional and can become part of the brand identity.

## Brand personality

MUTO should feel:

- quiet
- premium
- minimal
- fast
- intentional
- modern Android
- slightly experimental
- privacy-first
- utility-focused

Avoid:

- excessive gradients
- unnecessary glassmorphism
- huge decorative illustrations
- excessive colors
- gamification
- complicated dashboards
- unnecessary navigation
- history screens
- social features

---

# 4. Core UX

The first screen must communicate the entire product immediately.

## Normal state

```text
MUTO.

One tap. Total silence.


              ◯
             🔊
            READY

        Tap to go silent
```

## Muted state

```text
MUTO.

Silence is active.


              ◯
             🔇
           SILENT

        Tap to restore
```

The large circular control is the primary interaction.

There should be no requirement to open Android Settings during normal use.

---

# 5. Core Toggle Behaviour

## When MUTO is activated

Before changing anything:

1. Read and store the current relevant audio state.
2. Read the current Do Not Disturb / interruption-filter state.
3. Record whether MUTO itself changed DND.
4. Save this state safely using DataStore.
5. Set applicable audio streams to zero / silent.
6. Enable DND when the required Notification Policy Access has been granted.
7. Update the UI immediately.
8. Provide subtle haptic feedback.
9. Play a short, restrained state-transition animation.

## When MUTO is deactivated

1. Load the exact state saved before muting.
2. Restore each saved volume value.
3. Restore the previous ringer mode when appropriate.
4. Restore DND only when MUTO was responsible for changing it.
5. Never blindly turn off a DND state that existed before MUTO was activated.
6. Clear the temporary mute snapshot after successful restoration.
7. Animate back to the READY state.
8. Provide confirmation haptic feedback.

---

# 6. Critical Restoration Rule

This is one of the most important requirements.

Example:

Before MUTO:

```text
Media        65%
Ring         80%
Notification 50%
Alarm        100%
System       30%
```

After activating MUTO:

```text
Media        0%
Ring         0%
Notification 0%
Alarm        0%   (if user enabled alarm muting)
System       0%
DND          ON
```

After restoring:

```text
Media        65%
Ring         80%
Notification 50%
Alarm        100%
System       30%
DND          previous state
```

Never restore everything to a hard-coded value.

---

# 7. Alarm Safety

Alarm muting must NOT be forced by default.

Recommended default:

```text
Media          Mute
Ringtone       Mute
Notifications  Mute
System sounds  Mute
Calls          Silent / DND controlled
Alarms         Keep audible
```

Settings should contain:

```text
Mute alarms
[ OFF ]
```

If enabled, MUTO includes the alarm stream in its mute operation.

This prevents accidentally disabling a user's morning alarm.

---

# 8. DND Safety

DND handling must be state-aware.

Example:

### Before MUTO

```text
DND = OFF
```

MUTO:

```text
DND = ON
MUTO owns DND state
```

After restore:

```text
DND = OFF
```

---

### Before MUTO

```text
DND = ON
```

MUTO:

```text
DND remains ON
MUTO does NOT claim ownership
```

After restore:

```text
DND = ON
```

Never destroy the user's pre-existing DND state.

---

# 9. Permission Experience

Do not dump users into Settings without explanation.

First launch should use a simple onboarding / permission explanation:

```text
Make silence one tap away.

MUTO needs access to:

• Change sound levels
• Control Do Not Disturb

Nothing is uploaded.
Your sound preferences stay on this device.

                 Continue
```

Then guide the user to the appropriate Android system settings.

If permission is denied:

```text
MUTO needs this access to control silence.

Open Settings
```

Never pretend the action succeeded when Android rejected it.

---

# 10. Main Screen Design

## Overall structure

Use a single vertically scrolling screen only if content cannot fit naturally.

Preferred hierarchy:

```text
Top app area
    ↓
MUTO.
One tap. Total silence.
    ↓
Large primary toggle
    ↓
Current state
    ↓
Sound status card
    ↓
DND status
    ↓
Quick Settings shortcut
    ↓
Optional settings
```

Avoid bottom navigation.

MUTO does not need Home / History / Dashboard tabs.

---

# 11. No History Screen

Do NOT create a History screen.

MUTO is a utility.

The user does not need:

```text
10:30 PM muted
11:15 PM restored
12:40 AM muted
```

This adds complexity without improving the primary job.

If useful later, only expose a tiny "Last action" status inside the settings screen — not a full history system.

---

# 12. Primary Toggle

The large circular toggle is the visual identity of the app.

Recommended:

- 240–300dp visual size depending on device
- thin outline
- large negative space
- centered icon
- state label
- subtle glow only when active
- no excessive shadow
- no giant filled button

Normal:

```text
      ┌──────────────┐
      │              │
      │      🔊      │
      │              │
      │    READY     │
      │              │
      └──────────────┘
```

Muted:

```text
      ┌──────────────┐
      │              │
      │      🔇      │
      │              │
      │    SILENT    │
      │              │
      └──────────────┘
```

---

# 13. Toggle Animation

Take inspiration from the reference application's micro-animation philosophy.

On activation:

1. Button slightly scales down.
2. Haptic occurs.
3. Icon morphs / crossfades from sound to muted.
4. Ring expands subtly.
5. Border transitions.
6. State text changes.
7. Secondary controls update.
8. Button returns to resting scale.

Target motion:

```text
PRESS
  ↓
scale 0.96
  ↓
haptic
  ↓
icon transition
  ↓
state transition
  ↓
scale 1.00
```

Motion must feel fast.

Recommended duration:

- press feedback: 80–120ms
- state transition: 250–400ms
- content changes: 300–500ms

Use appropriate Material motion/easing rather than arbitrary animations.

---

# 14. Haptic Feedback

Use haptics intentionally.

Examples:

### Activate MUTO

```text
HapticFeedbackType.Confirm
```

### Restore

```text
HapticFeedbackType.Confirm
```

### Invalid / permission failure

```text
HapticFeedbackType.Reject
```

### Long press where applicable

```text
HapticFeedbackType.LongPress
```

Do not vibrate continuously.

---

# 15. Sound Controls Card

The card should show what MUTO currently controls.

Example:

```text
SOUND

♫   Media                    0
♧   Ringtone                 0
▢   Notifications            0
☎   Calls                    Silent
◷   Alarms                   80%
```

When normal:

```text
SOUND

♫   Media                    65%
♧   Ringtone                 80%
▢   Notifications            50%
☎   Calls                    Normal
◷   Alarms                   100%
```

Use Material icons with consistent optical weight.

Do not overload this card with sliders.

The card is primarily a **status display**, not a second settings panel.

---

# 16. DND Card

Example:

```text
DO NOT DISTURB

Moon icon

On
MUTO is controlling DND

                     Turn off
```

When inactive:

```text
DO NOT DISTURB

Off
MUTO will enable it when silencing
```

If DND was already enabled before MUTO:

```text
DO NOT DISTURB

Already on
MUTO won't change this state
```

This wording prevents confusion.

---

# 17. Quick Settings Tile

Implement an Android Quick Settings Tile.

States:

```text
MUTO
Ready
```

and

```text
MUTO
Silent
```

The tile should toggle the same core state as the main app.

Do not duplicate business logic.

Use a shared `MuteController`.

---

# 18. Widget

Create an optional home-screen widget.

Minimal design:

```text
┌──────────────────┐
│       🔇         │
│      MUTO        │
│     SILENT       │
└──────────────────┘
```

Tapping it performs the same operation as the Quick Settings tile.

Widget state must remain synchronized with the app.

---

# 19. Settings Screen

Use a single settings screen accessible from a top-right settings icon.

Sections:

## Sound

```text
Mute media                 ON
Mute ringtone              ON
Mute notifications         ON
Mute system sounds         ON
Mute alarms                OFF
```

## DND

```text
Enable DND when muting     ON
```

## Shortcuts

```text
Quick Settings tile
Home-screen widget
```

## Appearance

```text
Theme
○ System
○ Light
○ Dark
```

Default:

```text
System
```

## About

```text
MUTO
Version 1.x
Privacy
Open source licenses
```

Keep settings minimal.

---

# 20. Material 3 Expressive Direction

Use modern Material 3 / Material 3 Expressive principles.

Use:

- MaterialTheme
- dynamicColorScheme
- surfaceContainer
- surfaceContainerHigh
- surfaceContainerLow
- primary
- onPrimary
- onSurface
- onSurfaceVariant
- outline
- outlineVariant
- errorContainer
- appropriate Material shapes

Do not hard-code every color.

The application must support dynamic Android system colors.

---

# 21. Android Themed Icon

MUTO must support Android themed icons.

Create:

- adaptive icon
- monochrome icon layer
- foreground vector
- background layer

The monochrome version should be a simple MUTO monogram / abstract mute symbol.

The launcher should allow Android to recolor the icon according to the system theme.

The icon should work in:

- light mode
- dark mode
- themed icon mode
- monochrome launcher environments

Do not bake a fixed black or white background into the monochrome layer.

---

# 22. Typography

Use the system / Material typography system.

Prefer:

```text
displaySmall
headlineMedium
titleLarge
titleMedium
bodyLarge
bodyMedium
labelLarge
labelMedium
```

The brand title:

```text
M U T O .
```

can use slightly increased letter spacing.

Avoid excessive uppercase text everywhere.

---

# 23. Spacing

Use a consistent spacing scale.

Base:

```text
4dp
8dp
12dp
16dp
20dp
24dp
32dp
40dp
48dp
```

Primary screen horizontal padding:

```text
20–24dp
```

Cards:

```text
16–24dp
```

Avoid random values.

---

# 24. Cards

Cards should feel integrated with the background.

Preferred:

```text
surfaceContainer
```

with:

- rounded corners
- subtle tonal contrast
- minimal borders
- comfortable internal padding

Avoid:

- strong shadows
- excessive outlines
- glass blur everywhere
- neon gradients

---

# 25. Motion System

Create a reusable motion system.

Define constants:

```kotlin
object MutoMotion {
    val Fast = 120
    val Normal = 300
    val Medium = 400
    val Slow = 500
}
```

Use:

- AnimatedVisibility
- animateContentSize
- animateColorAsState
- animateFloatAsState
- updateTransition
- AnimatedContent
- appropriate Material motion APIs

Prefer state-driven animation.

Do not manually chain dozens of unrelated animations.

---

# 26. Interaction Principles

Every important interaction should have feedback.

Examples:

### Button press

```text
touch → scale → haptic → action
```

### Permission granted

```text
permission success → check animation → continue
```

### Permission denied

```text
error state → clear explanation → settings button
```

### Mute activated

```text
READY → SILENT
```

### Restore

```text
SILENT → READY
```

---

# 27. Architecture

Use a clean native Android architecture.

Recommended:

```text
app/
├── core/
│   ├── audio/
│   ├── dnd/
│   ├── permissions/
│   ├── haptics/
│   ├── widget/
│   └── quicksettings/
│
├── data/
│   ├── datastore/
│   └── repository/
│
├── domain/
│   ├── model/
│   └── usecase/
│
├── presentation/
│   ├── components/
│   ├── home/
│   ├── settings/
│   ├── onboarding/
│   └── theme/
│
└── MainActivity.kt
```

Use MVVM.

---

# 28. Central Mute Controller

There must be one source of truth.

Example conceptual API:

```kotlin
interface MuteController {

    suspend fun activate(): MuteResult

    suspend fun restore(): RestoreResult

    fun isMuted(): Boolean

    fun observeState(): Flow<MuteState>
}
```

The Activity, widget, and Quick Settings tile must call the same controller.

Never implement separate mute logic in three places.

---

# 29. State Model

Create an explicit state model.

```kotlin
enum class MuteState {
    READY,
    MUTING,
    SILENT,
    RESTORING,
    ERROR
}
```

Store a snapshot:

```kotlin
data class AudioSnapshot(
    val mediaVolume: Int,
    val ringVolume: Int,
    val notificationVolume: Int,
    val alarmVolume: Int?,
    val systemVolume: Int,
    val previousRingerMode: Int,
    val previousDndFilter: Int,
    val mutoChangedDnd: Boolean
)
```

Adapt the exact fields to what Android APIs expose on the supported API levels.

---

# 30. DataStore

Use DataStore for persistent preferences and temporary mute state.

Store:

- onboarding completed
- permission state where appropriate
- settings
- mute snapshot
- whether MUTO is currently active
- whether MUTO owns DND
- user preference for alarm muting
- theme preference

Do not use SharedPreferences for new application state.

---

# 31. Android Audio Implementation

Use Android's supported audio APIs.

Use:

```text
AudioManager
NotificationManager
NotificationManager.Policy
```

and appropriate Android APIs for:

- stream volumes
- ringer mode
- interruption filter
- notification policy access

Handle API-level differences.

Do not use hidden APIs.

Do not attempt to bypass Android security restrictions.

---

# 32. Permission Handling

Before enabling DND:

```kotlin
notificationManager.isNotificationPolicyAccessGranted
```

If false:

Guide the user to:

```text
Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
```

For volume modification, use the appropriate supported Android capability and verify actual results after attempting the change.

Always handle:

- denied permission
- restricted OEM behaviour
- unavailable API
- interrupted operation
- partial failure

---

# 33. Atomic Toggle

The operation must be as close to atomic as Android permits.

Activation:

```text
read all state
      ↓
persist snapshot
      ↓
change audio state
      ↓
change DND
      ↓
verify
      ↓
publish SILENT
```

If something fails:

```text
restore snapshot
      ↓
show ERROR
```

Never leave the user with an unknown half-muted state.

---

# 34. App Lifecycle

The UI should reflect the actual system state when returning to the app.

Do not assume:

```text
MUTO active == phone still muted
```

because the user may have changed sound/DND state outside MUTO.

On resume:

1. inspect current state
2. reconcile UI
3. detect external changes
4. avoid corrupting the stored snapshot

Document how conflicts are handled.

---

# 35. External Volume Changes

If the phone is SILENT and the user manually changes volume:

MUTO should not automatically fight the user unless explicitly designed to.

Recommended behaviour:

- MUTO remains logically active.
- UI reflects actual system values.
- Restore operation still uses the original snapshot.
- Never continuously force volumes back to zero from a background service.

This avoids battery drain and aggressive behaviour.

---

# 36. No Permanent Background Service

Do not create a permanent foreground service merely to keep the phone muted.

The core app should be event/action based.

Background components should only exist where Android requires them for an explicitly enabled feature.

---

# 37. Privacy

MUTO should be local-first.

No:

- account
- cloud database
- analytics by default
- location tracking by default
- unnecessary network permissions

The app's core function should work offline.

---

# 38. Accessibility

Support:

- TalkBack
- semantic labels
- minimum touch target sizes
- high contrast
- dynamic font scaling
- reduced motion where appropriate

The primary button must have a useful content description:

```text
Mute phone
```

or:

```text
Restore phone sound
```

depending on state.

Do not rely only on color to communicate state.

---

# 39. Reduced Motion

Respect Android accessibility settings.

When reduced motion is enabled:

- reduce scale effects
- remove unnecessary glow
- shorten/disable decorative transitions
- preserve functional state feedback

---

# 40. Error UI

Example:

```text
Couldn't activate silence

Android didn't allow MUTO to change
one or more sound settings.

Check permissions and try again.

        Open Settings
        Try Again
```

Do not show a generic:

```text
Something went wrong
```

when a specific explanation is available.

---

# 41. Empty / First-Launch State

First launch:

```text
MUTO.

One tap.
Total silence.

MUTO can control your sound levels and
Do Not Disturb without making you open
the system sound panel every time.

             Set up MUTO
```

After setup:

```text
MUTO.

Ready.
```

---

# 42. Quick Settings Setup

After permissions are configured, show:

```text
Make MUTO even faster

Add MUTO to Quick Settings for
one-tap silence without opening the app.

                    Add
```

This is optional and should never block the core app.

---

# 43. Widget Setup

Offer:

```text
Add MUTO to your home screen
```

with a preview.

Do not force users to install the widget.

---

# 44. Settings UX

Settings should follow Material 3 patterns.

Use:

- ListItem
- Switch
- RadioButton
- Preference-like custom Compose components
- section headings

Example:

```text
SOUND

Mute media                         ON
Mute ringtone                      ON
Mute notifications                 ON
Mute system sounds                 ON
Mute alarms                        OFF

DO NOT DISTURB

Enable DND                         ON

SHORTCUTS

Quick Settings                     Add
Home screen widget                 Add

APPEARANCE

Theme                              System

ABOUT

About MUTO
Privacy
Open-source licenses
```

---

# 45. Theme

Support:

```text
System
Light
Dark
```

Default:

```text
System
```

Do not make the entire application permanently black.

The supplied concept may use a black/dark visual direction, but the actual Android app must respect the user's system theme.

Dark mode should feel especially premium.

Light mode should remain equally polished.

---

# 46. Visual Direction

### Dark theme

Use:

```text
near-black background
surfaceContainer hierarchy
white/off-white foreground
muted gray secondary text
system-derived accent
```

Avoid pure black everywhere if it destroys hierarchy.

### Light theme

Use:

```text
soft neutral background
dark foreground
subtle surface hierarchy
system-derived accent
```

---

# 47. Brand Icon

Create a custom MUTO monogram.

Concept:

- abstract `M`
- subtle mute/sound relationship
- simple geometric construction
- one-color vector
- no text
- no tiny details
- recognizable at launcher size

Deliver:

```text
ic_launcher_foreground.xml
ic_launcher_background.xml
monochrome.xml
```

Support adaptive launcher icons.

---

# 48. Don't Copy Apple / Google / Nothing

The design can be **inspired by the restraint** of premium first-party software.

Do not copy:

- Apple logo
- Google logo
- Nothing logo
- exact iconography
- proprietary layouts
- proprietary artwork
- exact animations
- exact source code

MUTO needs a recognizable identity of its own.

---

# 49. Testing Matrix

Test at minimum:

### Android versions

- Android 13
- Android 14
- Android 15
- Android 16

### Device types

- Pixel-like Android
- Motorola
- Samsung
- Nothing OS

### Themes

- light
- dark
- dynamic color
- themed icon

### State combinations

1. DND OFF + normal sound
2. DND ON + normal sound
3. DND OFF + silent
4. DND ON + silent
5. low volume
6. maximum volume
7. alarm muting enabled
8. alarm muting disabled
9. permission denied
10. permission revoked after setup
11. external volume modification while MUTO active
12. app killed and reopened while silent
13. phone reboot while silent
14. Quick Settings toggle
15. widget toggle

---

# 50. Important Edge Case: Reboot

Define what happens after reboot.

Preferred behaviour:

- do not automatically change sound state after reboot unless the user explicitly enabled a persistence/automation feature
- on opening MUTO, inspect the actual state
- safely clear stale snapshots when they can no longer be trusted

---

# 51. Important Edge Case: DND Changed Externally

If another app/system automation changes DND:

MUTO must not blindly override it.

Track:

```text
previousDndState
mutoChangedDnd
```

and reconcile carefully.

---

# 52. Performance

The app should be extremely lightweight.

Requirements:

- no unnecessary dependencies
- no network dependency
- no continuous polling
- no permanent service
- lazy initialization
- Compose state should be stable
- avoid unnecessary recompositions
- keep animations GPU-friendly
- use vector icons

The reference project's optimization philosophy should be followed: remove bloat and keep the application focused.

---

# 53. Build Stack

Use:

```text
Kotlin
Jetpack Compose
Material 3 / Material 3 Expressive
AndroidX
Coroutines
Flow
DataStore
Hilt (if justified)
```

Use current stable versions compatible with the chosen compile SDK.

Do not blindly copy dependency versions from the reference repository.

---

# 54. Code Quality

The AI coding agent must:

1. Inspect the existing project before modifying it.
2. Preserve working code.
3. Avoid unnecessary rewrites.
4. Use small reusable Compose components.
5. Keep UI and business logic separate.
6. Add comments only where they explain non-obvious Android behaviour.
7. Avoid magic numbers.
8. Use resource strings for user-facing text.
9. Add previews for important Compose components.
10. Run formatting/lint/build checks.
11. Fix compile errors before considering a task complete.

---

# 55. Required Compose Components

Create reusable components such as:

```text
MutoHeader()
MutoToggle()
MuteStateLabel()
SoundStatusCard()
DndStatusCard()
QuickSettingsCard()
SettingsSection()
MutoSettingRow()
PermissionCard()
MutoIconButton()
```

Each component should be state-driven and previewable.

---

# 56. Preview Requirements

Create Compose previews for:

```text
Home / Ready
Home / Silent
Home / Permission Required
Home / Error
Settings / Dark
Settings / Light
Sound Card / Normal
Sound Card / Silent
DND / On
DND / Off
```

---

# 57. Animation Requirements Inspired by the Reference

Use the reference project's animation quality as the benchmark.

Required concepts:

### Content size

Use `animateContentSize()` where cards change between compact and expanded states.

### Color transition

Use `animateColorAsState()` or equivalent for:

```text
READY → SILENT
```

### Content transition

Use `AnimatedContent()` for:

```text
🔊 → 🔇
READY → SILENT
Tap to go silent → Tap to restore
```

### Haptic

Use haptics at meaningful state changes.

### Button interaction

Use press-state animation.

Do not make every component bounce.

---

# 58. MUTO Primary Animation Specification

```text
State: READY

User presses button
        ↓
scale 1.00 → 0.96
        ↓
haptic confirm
        ↓
icon transition
        ↓
ring expands slightly
        ↓
audio state changes
        ↓
text changes
        ↓
scale 0.96 → 1.00
        ↓
SILENT
```

Reverse the sequence for restore.

---

# 59. Optional Premium Detail

When MUTO becomes active, the primary ring may have a very subtle tonal glow.

Do not use a neon effect.

Think:

```text
minimal luminous edge
```

rather than:

```text
RGB gaming glow
```

The effect must remain disabled/reduced when reduced-motion/accessibility preferences require it.

---

# 60. Navigation

MUTO should have very little navigation.

Recommended:

```text
MainActivity
   ├── Home
   └── Settings
```

No:

```text
Home
History
Analytics
Profile
Dashboard
```

MUTO is a one-purpose utility.

---

# 61. No History

Explicit requirement:

> **Do not build a History page, history tab, or bottom navigation.**

The product should feel faster because it removes unnecessary information.

---

# 62. Optional Future Features

Keep architecture extensible for:

- scheduled mute
- calendar-based silence
- location automation
- NFC trigger
- flip-to-mute
- shake-to-mute
- custom focus modes

But **do not implement these in the MVP unless requested**.

The MVP must remain extremely simple.

---

# 63. MVP Definition

MVP includes:

- onboarding
- permission handling
- one-tap mute
- one-tap restore
- exact volume restoration
- DND control
- optional alarm muting
- main UI
- settings
- Quick Settings tile
- adaptive icon
- themed icon
- Material 3 / Expressive styling
- animations
- haptics
- light/dark/dynamic theme
- accessibility
- robust error handling

Not MVP:

- history
- accounts
- cloud
- analytics
- location automation
- calendar automation
- NFC
- shake detection
- complicated profiles

---

# 64. Definition of Done

The AI agent must not stop after generating UI mockups.

The project is complete only when:

- [ ] Android project builds successfully
- [ ] app launches
- [ ] Material 3 theme works
- [ ] dynamic colors work
- [ ] dark/light modes work
- [ ] themed icon works
- [ ] permission flow works
- [ ] mute action works
- [ ] restore action works
- [ ] exact volume snapshot is restored
- [ ] DND handling is state-aware
- [ ] alarm option works correctly
- [ ] Quick Settings tile works
- [ ] widget works
- [ ] UI animations work
- [ ] haptics work where supported
- [ ] accessibility labels exist
- [ ] reduced-motion behaviour exists
- [ ] external state changes are handled
- [ ] error states are handled
- [ ] no permanent background service is used unnecessarily
- [ ] no unnecessary network permission is requested
- [ ] no history screen exists
- [ ] no bottom navigation exists
- [ ] no copied third-party branding/assets are used
- [ ] release build succeeds

---

# 65. Final Instruction to the AI Coding Agent

**Build MUTO as a complete production-quality native Android application.**

First study the reference project's public repository and especially its documented Material 3 Expressive UI and animation approach.

Then create MUTO's own design system using the same level of polish:

- clean Compose architecture
- expressive Material components
- precise spacing
- state-driven animations
- micro-interactions
- haptics
- dynamic theming
- adaptive icons
- smooth content transitions
- excellent accessibility
- lightweight implementation

The reference project is a **quality and interaction reference**, not a source-code or branding template.

MUTO's product philosophy is:

> **One action. Total silence. One action. Everything back.**

Keep the interface extremely simple.

The large MUTO toggle is the hero.

Everything else exists only to make that action reliable, understandable, configurable, and fast.

Do not add unnecessary screens.

Do not add History.

Do not add accounts.

Do not add analytics.

Do not add cloud services.

Do not add decorative UI that competes with the main action.

Build the app end-to-end, compile it, test it, fix all errors, and leave a clean production-ready project.
