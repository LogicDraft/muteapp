# Build Prompt: MUTE. — Phase 2 (Logo, Polish, Working Widgets, APK)

Paste everything below into Claude Code (or your coding agent of choice), run it from inside the
`MuteApp/` project root (where `settings.gradle.kts` lives), and attach your logo file to the
same message.

---

## The prompt

This project (`MuteApp/`) is a working one-tap mute/DND toggle app for Android — see its
`README.md` for the full feature list and architecture. Continue building on it with the
following four passes, in order:

### 1. Replace the placeholder icon with the uploaded logo

I'm attaching a logo file. Use it to replace the current placeholder app icon (a plain red ring
in `app/src/main/res/drawable/ic_launcher_background.xml` / `ic_launcher_foreground.xml`).

- If the file is raster (PNG/JPG), don't hand-scale it into every mipmap density yourself — use
  Android Studio's Image Asset workflow / adaptive-icon conventions so it renders crisply at
  every size.
- If it's vector-friendly, convert it into a `foreground` vector drawable sized to the 108×108dp
  adaptive-icon canvas, keeping the important content inside the 66dp-diameter safe zone.
- Keep the background solid black to match the app's fixed dark aesthetic, unless the logo
  clearly has its own background baked in — ask me first if that's ambiguous.
- Update `ic_launcher_round.xml` to match, and regenerate any Play Store-sized (512×512) export
  if one is needed.
- **Also derive a flat monochrome silhouette version** (single color, alpha-only shape — no
  gradients, no color fill) for the Quick Settings tile icons (`ic_tile_active.xml`,
  `ic_tile_muted.xml`) and the status-bar notification icon (`ic_notification.xml`). Android
  renders these as solid-tinted silhouettes regardless of what you give it, so a full-color logo
  will show up as a grey blob — don't skip this derivation step even if it takes a few tries to
  get the shape reading clearly at 24dp.

### 2. Cleanup + premium polish pass

- Audit for anything unused — dead resources, unused imports, leftover TODO comments — and
  remove it. Run lint (`./gradlew lint`) and fix anything above Warning severity.
- Add a proper Android 12+ `SplashScreen` (androidx.core.splashscreen) using the new logo,
  matching the black background + red accent, instead of a blank first frame.
- Smooth the mute/unmute dial animation on the main screen — replace the default fade with a
  deliberate spring-based scale/color transition, and make sure it respects the system's
  reduced-motion setting.
- Confirm every touch target (Settings chips, back button, dial) is at least 48dp.
- Re-check status/navigation bar icon contrast on the permission and settings screens now that
  edge-to-edge is enforced on API 36.
- Stay inside the brief: no Material You dynamic color, no light theme. Keep it fixed
  black/white/red — that's the whole point of the aesthetic, don't drift from it.

### 3. Make the widget actually work and look right

- Test the existing 1×1 `MuteWidgetProvider` on a real device/emulator on at least two launchers
  (stock + a third-party one like Nova) — RemoteViews rendering and grid cell sizing vary more
  than the preview tools show.
- Add the missing static `android:previewImage` drawable in `widget_mute_info.xml`. The
  `previewLayout` attribute already there only renders on API 31+; devices on API 26–30 (which
  this app supports) need a flat preview image or the widget picker shows a generic placeholder.
- Confirm text doesn't clip at the smallest grid cell some launchers allow, and that the
  red/white icon tint (`setColorFilter` via RemoteViews) renders correctly on both API 26–30 and
  API 31+, since tinting behavior changed there.
- Add a second, larger widget size (e.g. 2×1) that shows the state plus the auto-restore
  countdown when that setting is active (e.g. "Muted · 3h12m left"), reusing the same
  `ToggleReceiver` tap target. Keep 1×1 as the default.
- Confirm the widget updates instantly — not on the next `updatePeriodMillis` cycle — after a
  toggle from any of the four entry points: the tile, the widget itself, the notification
  action, and the in-app button.

### 4. Generate an installable APK

- Build and verify a debug APK: `./gradlew assembleDebug`, install it on a connected
  device/emulator, and confirm the tile, widget, and toggle all work end to end.
- Set up a local debug/testing keystore (not a production release key), wire up signing in
  `app/build.gradle.kts`, and produce a signed release build: `./gradlew assembleRelease`.
- Report back the final APK path(s) when done.

---

## Note for whoever runs this

Building an APK needs the Android SDK and a working connection to Google's Maven repository —
this won't run in a fully offline/sandboxed environment. Use Android Studio directly, or Claude
Code on a machine with the Android SDK already installed. If the logo isn't attached when this
prompt runs, the agent should pause and ask for it rather than inventing a placeholder.
