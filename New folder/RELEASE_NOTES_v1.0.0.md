# MUTE. v1.0.0 — Initial release

One-tap silence for Android. This first release covers the core toggle end to end: a Quick
Settings tile, a home-screen widget, exact volume/DND save-and-restore, and a settings screen —
no app UI required for everyday use.

## Highlights

- **Quick Settings tile + 1×1 home-screen widget**, both reflecting live state
- **Exact restore** — alarm, media, notification, and ring volumes plus ringer mode and DND
  filter are snapshotted the instant you mute and restored precisely on unmute
- **Do Not Disturb levels**: Total Silence or Priority Only
- **Optional alarm exclusion** so alarms can stay audible while everything else goes quiet
- **Auto-restore safety timer** (Off / 1h / 2h / 4h / 8h)
- **Persistent "Silent mode on" notification** with a one-tap restore action
- Survives app kill and device reboot
- Monochrome black/white + signal-red visual language throughout

## Requirements

- Android 8.0 (API 26) or newer

## Install

1. Download `mute-v1.0.0.apk` from this release's Assets below.
2. Enable "install unknown apps" for whichever app you download it through (your browser or file
   manager will prompt for this if it isn't already on).
3. Open the APK to install, launch MUTE., and grant Do Not Disturb access when prompted — this
   is a one-time system settings screen, not a normal permission dialog.
4. Add the tile from the Quick Settings edit screen and/or the widget from your home screen's
   widget picker.

## Known limitations

- The dot-matrix display font is a monospace stand-in, not Nothing's proprietary "Ndot" face
  (licensing — see the README).
- Every mute/unmute is manual in this release; there's no scheduled or automatic silence yet.
- App icon is a placeholder mark, not final branding.

## What's next

Scheduled Silence (a daily auto-mute/unmute window), premium in-app animations, and a few other
items are tracked in the [README roadmap](README.md#roadmap).

**Full changelog**: initial release, no prior tag to compare against.
