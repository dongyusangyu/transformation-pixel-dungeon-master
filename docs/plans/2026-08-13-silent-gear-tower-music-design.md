# Silent Gear Tower Music Design

## Scope

- Add one dedicated common tower track named `tower.ogg`.
- Use it on normal tower floors, including each visual tower style.
- Keep `city_tense.ogg` for the amulet-return state and existing boss music for locked boss combat.

## Composition

- Title: Silent Gear
- Tempo: 64 BPM
- Meter and length: 4/4, 24 bars, approximately 90 seconds (aligned to the Vorbis frame boundary)
- Tonal center: D Phrygian
- Form: empty tower, distant mechanism, rising danger, stripped return
- Palette: low drone, dark pad, sparse metallic resonance, isolated warning motif, quiet air
- Dynamics: approximately -24 dBFS RMS with peaks below -6 dBFS
- Looping: the ending restates the opening D-Eb-A-D motif, with its final resonance and D drone continuing across the file boundary

## Deliverables

- MIDI theme/harmony reference and reproducible render arrangement under `tools/music`.
- Deterministic renderer under `tools`.
- 44.1 kHz stereo OGG Vorbis asset under `core/src/main/assets/music`.
- Java resource constant, tower playback wiring, and asset contract tests.
