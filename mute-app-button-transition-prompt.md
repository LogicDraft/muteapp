# Build Prompt: MUTE. — Ring-to-Disc Ripple Transition

Paste everything below into your coding agent, run from inside the `MuteApp/` project root.
There's a reference video for this — attach `gemini_generated_video_00953feb_2.mp4` alongside
this prompt so the agent can look at it directly rather than working from the description alone.

---

## The prompt

Replace the main toggle button's current transition with the one shown in the attached
reference video. Reading of what it shows, phase by phase:

1. **Rest**: a thin outlined ring (unfilled) — this matches the button's current default look.
2. **Tap → transition**: a burst of concentric ripples radiates outward — several overlapping
   wavy rings expanding and fading, like sonar pings. Partway through, a diagonal light sweep
   glides across the button's surface like a glare passing over glass.
3. **Peak**: ripples at maximum spread, plus a brief bright radial flash at the center — quick,
   like a camera flash, not sustained.
4. **Settle**: ripples contract and fade, the ring fills solid (outline → filled disc), and the
   ACTIVE/MUTED label crossfades at the same moment.
5. **Reverse**: tapping again plays the same ripple-and-flash treatment, this time un-filling the
   disc back to an outline.

**Important — the reference clip is a slowed-down demo loop, not the real target speed.** It runs
10 seconds so the effect is easy to see frame by frame; the actual on-device transition should be
fast and snappy, roughly 400–600ms total. Don't build a 10-second animation.

### Suggested implementation approach

Drive the whole thing from **one** `Animatable<Float>` progress value (0f → 1f over ~500ms,
spring or a decelerating tween), with every sub-effect keyed off a fraction of that single value
via `androidx.compose.animation.core.keyframes` — not several independent animations that can
drift out of sync with each other. Roughly:

- **Ring/disc fill morph**: draw the button on a `Canvas`. Animate the stroke width from a thin
  ring (~2dp) up to a stroke wide enough to fully cover the radius (i.e. it reads as filled) —
  a single `drawCircle(style = Stroke(width = ...))` call with an animated width is a clean way
  to morph ring → disc without swapping draw calls. Reverse it for the un-fill direction.
- **Ripple burst**: 3–5 circles drawn with `Stroke` style, low alpha, staggered start delays,
  each animating radius outward (spring or tween) while alpha fades to 0 as it expands — a
  layered "sonar ping" burst. This is achievable in plain Canvas across every supported API
  level and is the baseline to build first.
  - Optional stretch, API 33+ only: an AGSL `RuntimeShader` can get closer to the fine wavy
    contour-line texture in the reference video than plain expanding circles can. Treat this as
    a progressive enhancement behind an SDK check, not a requirement — don't let matching the
    exact wireframe texture block finishing the baseline version.
- **Diagonal light sweep**: a `Brush.linearGradient` band (soft, low-opacity white) whose
  `Offset` animates diagonally across the circle's bounds once per transition, clipped to the
  circle shape — the standard "shimmer" technique, but a one-shot sweep synced to the transition
  rather than a repeating loop.
- **Center flash**: a small radial-gradient burst, alpha and scale spiking briefly (~150–200ms)
  around the transition's midpoint via a `keyframes` spec, then decaying — not a sustained glow.
- **Label crossfade**: reuse the ACTIVE/MUTED label transition already scoped in the earlier
  "premium animations" prompt if that's built; if not, a simple crossfade timed to the fill
  morph's midpoint is fine for now. Don't build a second, competing label animation.

### Respect what's already been specified elsewhere

- Reduce-motion: same requirement as every other animation added to this app — check the
  system's reduce-motion setting and fall back to a fast, minimal-motion version (e.g. just the
  fill morph and label change, no ripples/sweep/flash) rather than skipping the state change
  animation entirely.
- If the "Completely Material" pass has landed, keep this transition's *coloring* consistent
  with whatever the current dynamic/Material color scheme is (`colorScheme.primary` etc.) rather
  than hardcoding the old brand red — the ripples and flash should tint with the theme, not
  override it.

---

## Note for whoever runs this

Get the ring↔disc morph and the basic ripple burst working and feeling snappy first — that's
most of the perceived effect. The light sweep, center flash, and shader-based ripple texture are
each independent polish layers on top; add them once the core transition already feels good at
real speed on a device, not just in isolation at slow motion.
