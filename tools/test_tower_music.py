#!/usr/bin/env python3
"""Validate the packaged Silent Gear loop and its OGG timeline."""

from pathlib import Path
import unittest

import av
import numpy as np


ROOT = Path(__file__).resolve().parents[1]
MUSIC = ROOT / "core/src/main/assets/music/tower.ogg"
EXPECTED_SAMPLES = 3_969_024


def decoded_timeline():
    with av.open(str(MUSIC)) as container:
        stream = container.streams.audio[0]
        frames = [frame for packet in container.demux(stream) for frame in packet.decode()]
    audio = np.concatenate([frame.to_ndarray() for frame in frames], axis=1).T
    return audio


class TowerMusicTest(unittest.TestCase):

    def test_ogg_has_complete_monotonic_ninety_second_timeline(self):
        with av.open(str(MUSIC)) as container:
            stream = container.streams.audio[0]
            self.assertEqual("vorbis", stream.codec_context.name)
            self.assertEqual(44_100, stream.codec_context.sample_rate)
            self.assertEqual(2, stream.codec_context.channels)
            duration = float(stream.duration * stream.time_base)
            packet_pts = [packet.pts for packet in container.demux(stream)
                          if packet.pts is not None]

        self.assertAlmostEqual(EXPECTED_SAMPLES / 44_100.0, duration,
                               delta=1.0 / 44_100.0)
        self.assertTrue(all(current >= previous
                            for previous, current in zip(packet_pts, packet_pts[1:])))

    def test_ending_returns_to_the_opening_without_a_loop_spike(self):
        audio = decoded_timeline()
        self.assertEqual(EXPECTED_SAMPLES, len(audio))
        window = 2 * 44_100
        opening = audio[:window]
        ending = audio[-window:]

        def rms_db(values):
            return 20.0 * np.log10(np.sqrt(np.mean(values ** 2)))

        def spectrum(values):
            mono = values.mean(axis=1) * np.hanning(len(values))
            return np.abs(np.fft.rfft(mono))

        opening_spectrum = spectrum(opening)
        ending_spectrum = spectrum(ending)
        similarity = np.dot(opening_spectrum, ending_spectrum) / (
                np.linalg.norm(opening_spectrum) * np.linalg.norm(ending_spectrum))
        seam_jump = np.max(np.abs(audio[-1] - audio[0]))
        internal_jumps = np.max(np.abs(np.diff(audio, axis=0)), axis=1)

        self.assertLess(abs(rms_db(opening) - rms_db(ending)), 1.5)
        self.assertGreater(similarity, 0.90)
        self.assertLess(seam_jump, np.percentile(internal_jumps, 99.9) * 0.5)


if __name__ == "__main__":
    unittest.main()
