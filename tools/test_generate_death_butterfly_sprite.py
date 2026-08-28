"""Tests for the deterministic Death Butterfly sprite generator."""

import unittest

import generate_death_butterfly_sprite as sprite


class DeathButterflySpriteGeneratorTest(unittest.TestCase):

    def test_builds_fifteen_nonempty_sixteen_pixel_frames(self):
        frames = sprite.build_frames()

        self.assertEqual(15, len(frames))
        for frame in frames:
            self.assertEqual((16, 16), frame.size)
            self.assertIsNotNone(frame.getchannel("A").getbbox())

    def test_uses_hard_alpha_and_limited_palette(self):
        visible_colors = set()
        for frame in sprite.build_frames():
            for red, green, blue, alpha in frame.getdata():
                self.assertIn(alpha, (0, 255))
                if alpha == 0:
                    self.assertEqual((0, 0, 0), (red, green, blue))
                else:
                    visible_colors.add((red, green, blue))

        self.assertLessEqual(len(visible_colors), 12)

    def test_idle_frames_keep_bone_skull_mark_and_attack_surges_forward(self):
        frames = sprite.build_frames()
        bone = sprite.PALETTE["bone"]
        for frame in frames[:6]:
            self.assertIn(bone, frame.getdata())

        idle_center = alpha_center_x(frames[0])
        lunge_center = alpha_center_x(frames[8])
        self.assertGreater(lunge_center, idle_center + 1)

    def test_death_frames_progressively_break_apart(self):
        frames = sprite.build_frames()
        final_pixels = visible_pixel_count(frames[14])
        first_death_pixels = visible_pixel_count(frames[10])

        self.assertLess(final_pixels, first_death_pixels)
        self.assertNotEqual(frames[10].tobytes(), frames[14].tobytes())


def visible_pixel_count(image):
    return sum(1 for pixel in image.getdata() if pixel[3] == 255)


def alpha_center_x(image):
    points = [
        x
        for y in range(image.height)
        for x in range(image.width)
        if image.getpixel((x, y))[3] == 255
    ]
    return sum(points) / len(points)


if __name__ == "__main__":
    unittest.main()
