#!/usr/bin/env python3
"""Render the deterministic 90-second Silent Gear tower loop."""

from fractions import Fraction
import json
from pathlib import Path

import av
import numpy as np
from scipy import signal


SAMPLE_RATE = 44_100
ROOT = Path(__file__).resolve().parents[1]
CONFIG_PATH = ROOT / "tools/music/tower_silent_gear.json"
with CONFIG_PATH.open(encoding="utf-8") as config_file:
    CONFIG = json.load(config_file)
BPM = CONFIG["bpm"]
BEAT = 60.0 / BPM
BAR = 4.0 * BEAT
BARS = CONFIG["render"]["bars"]
NOMINAL_DURATION = BARS * BAR
SAMPLES = int(np.ceil(NOMINAL_DURATION * SAMPLE_RATE / 1024.0)) * 1024
DURATION = SAMPLES / SAMPLE_RATE
OUTPUT = ROOT / "core/src/main/assets/music/tower.ogg"


def midi_frequency(note):
    return 440.0 * 2.0 ** ((note - 69) / 12.0)


def loop_frequency(frequency):
    return round(frequency * DURATION) / DURATION


def oscillator(time, frequency, phase=0.0):
    return np.sin(2.0 * np.pi * loop_frequency(frequency) * time + phase)


def event_envelope(length, attack, release):
    envelope = np.ones(length, dtype=np.float64)
    attack_samples = min(int(attack * SAMPLE_RATE), length // 2)
    release_samples = min(int(release * SAMPLE_RATE), length // 2)
    if attack_samples:
        phase = np.linspace(0.0, np.pi / 2.0, attack_samples, endpoint=False)
        envelope[:attack_samples] = np.sin(phase) ** 2
    if release_samples:
        phase = np.linspace(np.pi / 2.0, 0.0, release_samples, endpoint=True)
        envelope[-release_samples:] = np.sin(phase) ** 2
    return envelope


def pan(mix, sound, pan_value):
    left = np.sqrt((1.0 - pan_value) * 0.5)
    right = np.sqrt((1.0 + pan_value) * 0.5)
    mix[:, 0] += sound * left
    mix[:, 1] += sound * right


def add_circular(mix, sound, first, pan_value):
    first %= SAMPLES
    remaining = len(sound)
    offset = 0
    while remaining:
        length = min(remaining, SAMPLES - first)
        pan(mix[first:first + length], sound[offset:offset + length], pan_value)
        remaining -= length
        offset += length
        first = 0


def add_pad(mix, start, duration, notes, gain, pan_value):
    first = int(start * SAMPLE_RATE) % SAMPLES
    length = int(duration * SAMPLE_RATE)
    local_time = np.arange(length, dtype=np.float64) / SAMPLE_RATE
    sound = np.zeros(length, dtype=np.float64)
    phases = (0.13, 1.41, 2.67, 0.79)
    for index, note in enumerate(notes):
        frequency = midi_frequency(note)
        phase = phases[index % len(phases)]
        tone = np.sin(2.0 * np.pi * frequency * local_time + phase)
        tone += 0.22 * np.sin(2.0 * np.pi * frequency * 2.0 * local_time + phase * 0.7)
        tone += 0.08 * np.sin(2.0 * np.pi * frequency * 3.0 * local_time + phase * 1.3)
        sound += tone / len(notes)
    sound *= event_envelope(length, 1.05, 1.2) * gain
    add_circular(mix, sound, first, pan_value)


def add_bell(mix, start, midi_note, gain, pan_value, decay=2.2):
    first = int(start * SAMPLE_RATE) % SAMPLES
    length = int(4.5 * SAMPLE_RATE)
    local_time = np.arange(length, dtype=np.float64) / SAMPLE_RATE
    frequency = midi_frequency(midi_note)
    envelope = (1.0 - np.exp(-local_time * 45.0)) * np.exp(-local_time / decay)
    sound = np.sin(2.0 * np.pi * frequency * local_time)
    sound += 0.34 * np.sin(2.0 * np.pi * frequency * 2.01 * local_time + 0.7)
    sound += 0.19 * np.sin(2.0 * np.pi * frequency * 2.73 * local_time + 1.8)
    sound += 0.08 * np.sin(2.0 * np.pi * frequency * 4.12 * local_time + 0.3)
    sound *= envelope * gain
    add_circular(mix, sound, first, pan_value)


def add_gear_hit(mix, start, gain, pan_value, seed):
    first = int(start * SAMPLE_RATE)
    length = min(int(3.5 * SAMPLE_RATE), SAMPLES - first)
    local_time = np.arange(length, dtype=np.float64) / SAMPLE_RATE
    rng = np.random.default_rng(seed)
    noise = rng.standard_normal(length)
    noise = signal.sosfilt(signal.butter(2, (500, 4200), btype="bandpass",
                                          fs=SAMPLE_RATE, output="sos"), noise)
    envelope = np.exp(-local_time / 0.72)
    metal = np.sin(2.0 * np.pi * 73.4 * local_time)
    metal += 0.52 * np.sin(2.0 * np.pi * 187.7 * local_time + 0.9)
    metal += 0.28 * np.sin(2.0 * np.pi * 391.2 * local_time + 2.1)
    sound = (metal + noise * 0.10) * envelope * gain
    pan(mix[first:first + length], sound, pan_value)


def add_circular_reverb(mix, seed):
    rng = np.random.default_rng(seed)
    impulse_length = int(5.2 * SAMPLE_RATE)
    for channel in range(2):
        impulse = np.zeros(SAMPLES, dtype=np.float64)
        impulse[0] = 1.0
        delays = rng.integers(int(0.045 * SAMPLE_RATE), impulse_length, 340)
        decay = np.exp(-delays / (1.55 * SAMPLE_RATE))
        impulse[delays] += rng.uniform(-1.0, 1.0, delays.size) * decay * 0.045
        wet = np.fft.irfft(np.fft.rfft(mix[:, channel]) * np.fft.rfft(impulse), n=SAMPLES)
        mix[:, channel] = mix[:, channel] * 0.84 + wet * 0.16


def encode_ogg(audio, output):
    output.parent.mkdir(parents=True, exist_ok=True)
    with av.open(str(output), "w", format="ogg") as container:
        stream = container.add_stream("vorbis", rate=SAMPLE_RATE)
        stream.bit_rate = 64_000
        stream.layout = "stereo"
        stream.codec_context.options = {"strict": "experimental"}
        for first in range(0, len(audio), 4096):
            planar = np.ascontiguousarray(audio[first:first + 4096].T, dtype=np.float32)
            frame = av.AudioFrame.from_ndarray(planar, format="fltp", layout="stereo")
            frame.sample_rate = SAMPLE_RATE
            frame.pts = first
            frame.time_base = Fraction(1, SAMPLE_RATE)
            for packet in stream.encode(frame):
                container.mux(packet)
        for packet in stream.encode():
            container.mux(packet)


def validate_ogg(output):
    with av.open(str(output)) as container:
        stream = container.streams.audio[0]
        packet_pts = [packet.pts for packet in container.demux(stream)
                      if packet.pts is not None]
        if any(current < previous for previous, current in zip(packet_pts, packet_pts[1:])):
            raise RuntimeError("OGG packet timestamps are not monotonic")
        duration = float(stream.duration * stream.time_base)
        if abs(duration - DURATION) > 1.0 / SAMPLE_RATE:
            raise RuntimeError(f"OGG duration is {duration:.3f}s, expected {DURATION:.3f}s")
        return duration, len(packet_pts)


def render():
    time = np.arange(SAMPLES, dtype=np.float64) / SAMPLE_RATE
    mix = np.zeros((SAMPLES, 2), dtype=np.float64)

    movement = 0.70 + 0.12 * np.sin(2.0 * np.pi * time / DURATION * 2.0 - 0.6)
    movement += 0.08 * np.sin(2.0 * np.pi * time / DURATION * 5.0 + 1.2)
    drone = oscillator(time, midi_frequency(38), 0.2)
    drone += 0.43 * oscillator(time, midi_frequency(45), 1.1)
    drone += 0.21 * oscillator(time, midi_frequency(50), 2.0)
    drone += 0.10 * oscillator(time, midi_frequency(62), 0.4)
    drone *= movement * 0.105
    pan(mix, drone, -0.08)
    seam_bed = mix.copy()

    rng = np.random.default_rng(64024)
    air = rng.standard_normal(SAMPLES)
    air = signal.sosfilt(signal.butter(3, (85, 1800), btype="bandpass",
                                        fs=SAMPLE_RATE, output="sos"), air)
    air /= max(np.max(np.abs(air)), 1e-9)
    boundary = np.ones(SAMPLES, dtype=np.float64)
    edge = int(1.2 * SAMPLE_RATE)
    fade = np.sin(np.linspace(0.0, np.pi / 2.0, edge)) ** 2
    boundary[:edge] = fade
    boundary[-edge:] = fade[::-1]
    air_motion = 0.40 + 0.25 * np.sin(2.0 * np.pi * time / DURATION * 3.0 + 2.2)
    pan(mix, air * boundary * air_motion * 0.017, 0.18)

    pads = CONFIG["render"]["pads"]
    for bar, notes, position in pads:
        gain = 0.080 if bar < 8 else (0.105 if bar < 20 else 0.070)
        duration = BAR * 1.45 if bar >= 23 else BAR * 0.82
        add_pad(mix, bar * BAR + 0.28, duration, notes, gain, position)

    motifs = CONFIG["render"]["motifs"]
    for motif_index, (bar, notes) in enumerate(motifs):
        for note_index, note in enumerate(notes):
            start = bar * BAR + (0.25 + note_index * 0.82) * BEAT
            gain = 0.046 if bar < 16 else 0.058
            add_bell(mix, start, note, gain, -0.48 + 0.31 * (note_index % 4),
                     decay=1.65 + 0.18 * motif_index)

    for index, bar in enumerate(CONFIG["render"]["gear_hits"]):
        gain = 0.050 if bar < 15 else 0.066
        add_gear_hit(mix, bar * BAR + 2.55 * BEAT, gain,
                     -0.55 if index % 2 == 0 else 0.55, 8100 + index)

    add_circular_reverb(mix, 19008)
    frequencies = np.fft.rfftfreq(SAMPLES, 1.0 / SAMPLE_RATE)
    lowpass = 1.0 / np.sqrt(1.0 + (frequencies / 6500.0) ** 6)
    for channel in range(2):
        mix[:, channel] = np.fft.irfft(
                np.fft.rfft(mix[:, channel]) * lowpass, n=SAMPLES)
        seam_bed[:, channel] = np.fft.irfft(
                np.fft.rfft(seam_bed[:, channel]) * lowpass, n=SAMPLES)

    # Hand the arrangement back to its phase-continuous drone at both endpoints.
    # The returning motif remains audible, while the file boundary itself stays musical.
    seam_samples = int(0.75 * SAMPLE_RATE)
    seam_phase = np.linspace(0.0, np.pi / 2.0, seam_samples)
    arrangement_in = np.sin(seam_phase) ** 2
    arrangement_out = np.cos(seam_phase) ** 2
    mix[:seam_samples] = (seam_bed[:seam_samples] * (1.0 - arrangement_in[:, None])
                          + mix[:seam_samples] * arrangement_in[:, None])
    mix[-seam_samples:] = (mix[-seam_samples:] * arrangement_out[:, None]
                           + seam_bed[-seam_samples:] * (1.0 - arrangement_out[:, None]))

    rms = np.sqrt(np.mean(mix ** 2))
    mix *= 10.0 ** (-24.5 / 20.0) / max(rms, 1e-9)
    peak = np.max(np.abs(mix))
    if peak > 0.45:
        mix *= 0.45 / peak

    encode_ogg(mix.astype(np.float32), OUTPUT)
    encoded_duration, packet_count = validate_ogg(OUTPUT)

    rms_db = 20.0 * np.log10(np.sqrt(np.mean(mix ** 2)))
    peak_db = 20.0 * np.log10(np.max(np.abs(mix)))
    seam_jump = float(np.max(np.abs(mix[-1] - mix[0])))
    adjacent = np.max(np.abs(np.diff(mix, axis=0)), axis=1)
    reference_jump = float(np.percentile(adjacent, 99.9))
    print(f"Rendered {OUTPUT}")
    print(f"duration={DURATION:.6f}s sample_rate={SAMPLE_RATE} stereo=true")
    print(f"rms={rms_db:.2f}dBFS peak={peak_db:.2f}dBFS")
    print(f"seam_jump={seam_jump:.6f} p99.9_adjacent_jump={reference_jump:.6f}")
    print(f"encoded_duration={encoded_duration:.3f}s packets={packet_count} timestamps=monotonic")


if __name__ == "__main__":
    render()
