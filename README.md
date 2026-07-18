# MUTE.

**One-tap silence for Android.** Tap the Quick Settings tile or home-screen widget — ringer,
media, and notification volume drop to zero and Do Not Disturb switches on, instantly, with no
app screen involved. Tap again and everything is restored to exactly what it was.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-26-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-green)

## Why

Muting a phone properly — ringer, media, notifications, *and* Do Not Disturb, all at once —
normally takes several taps across two different system menus, and none of it remembers what
your volumes were before you touched them. MUTE. is one toggle that does all of it and restores
your exact prior state, reachable without ever opening the app.

## Screenshots

| Main screen | Settings |
|---|---|
| ![Main screen — active state](screenshots/main-active.png) | ![Settings screen](screenshots/settings.png) |

## Features

- **Quick Settings tile** and a **1×1 home-screen widget** — the two intended everyday entry
  points; the app screen itself is only for one-time setup
- **Exact restore** — the moment you mute, current alarm/media/notification/ring volumes, ringer
  mode, and DND filter are snapshotted and restored precisely, not reset to a guess
- **Do Not Disturb integration** — choose Total Silence or Priority Only
- **Optional alarm exclusion** — keep alarms audible while everything else goes quiet
- **Auto-restore safety timer** — optional automatic unmute after 1/2/4/8 hours, in case you
  forget
- **Persistent low-priority notification** while muted, with a one-tap restore action
- **Survives app kill and device reboot**
- Monochrome black/white + signal-red visual language, deliberately restrained

## Requirements

- Android 8.0 (API 26) or newer
- Do Not Disturb access, granted once on first launch (see [Permissions](#permissions))

## Install

### From a release

Grab the latest APK from [Releases](../../releases), then sideload it — enable "install unknown
apps" for whichever app you download it through, open the APK, install.

### From source

1. Clone the repo and open the folder directly in **Android Studio** (a recent stable release —
   the project targets Android 16 / API 36).
2. **Gradle wrapper:** `gradle/wrapper/gradle-wrapper.properties` is committed, but the
   `gradle-wrapper.jar` binary is intentionally not (keep binaries out of the diff). Android
   Studio regenerates it automatically on first open; alternatively run `gradle wrapper` once if
   you have Gradle installed locally.
3. Run on a device or emulator on API 26+. Build a release APK with `./gradlew assembleRelease`.

## Permissions

| Permission | Why |
|---|---|
| `ACCESS_NOTIFICATION_POLICY` | Required to read/set Do Not Disturb state. Granted via a system settings screen, not a normal runtime dialog. |
| `POST_NOTIFICATIONS` | Shows the optional "Silent mode on" status notification (Android 13+). |
| `RECEIVE_BOOT_COMPLETED` | Re-arms the auto-restore timer after a reboot — alarms don't survive one. |
| `VIBRATE` | A short haptic pulse confirms the toggle, since there's no sound to confirm it with. |

MUTE. never reads contacts, location, or any personal data, and makes no network requests.

## Architecture

```
app/src/main/java/com/gowtham/mute/
├── core/MuteController.kt        All mute/unmute/toggle logic lives here — every entry point
│                                  (tile, widget, notification, in-app button) calls into this
│                                  and nothing else
├── data/PrefsManager.kt          Typed SharedPreferences: saved audio snapshot + settings
├── notification/                 Persistent "Silent mode on" notification
├── receiver/                     Widget/notification toggle target, boot re-arm, auto-restore
├── tile/MuteTileService.kt       Quick Settings tile
├── widget/MuteWidgetProvider.kt  1×1 home-screen widget
└── ui/                           Compose screens (rarely opened) + theme
```

## Roadmap

- [ ] Scheduled Silence — daily auto-mute/unmute window
- [ ] Premium in-app animations and transitions
- [ ] Custom adaptive-icon branding
- [ ] Nothing Glyph light integration
- [ ] Priority Only exceptions (starred contacts, repeat callers)
- [ ] Calendar-based auto-silence
- [ ] Tasker / external automation support via a documented broadcast

## Tech stack

Kotlin, Jetpack Compose, Material 3 — no third-party runtime dependencies beyond AndroidX and
Compose.

## Contributing

Issues and PRs welcome. This is intentionally a *single-toggle* app — check the Roadmap before
proposing net-new scope, and keep the monochrome/red visual language intact in any UI change.

## License

MIT — see [LICENSE](LICENSE).

## Acknowledgments

Visual language inspired by Nothing OS's monochrome/red aesthetic. MUTE. is an independent
project, not affiliated with or endorsed by Nothing Technology Limited. Nothing's "Ndot"
dot-matrix typeface is proprietary and isn't bundled here — see `Type.kt` for the substitution
used instead.
