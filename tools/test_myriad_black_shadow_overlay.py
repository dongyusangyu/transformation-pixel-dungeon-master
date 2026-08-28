"""Contract tests for the Myriad Black Shadow overlay sprite sheet."""

import unittest
from pathlib import Path

from PIL import Image

import generate_myriad_black_shadow_overlay as overlay


class MyriadBlackShadowOverlayTest(unittest.TestCase):

    def test_builds_four_nonempty_sixteen_pixel_frames(self):
        frames = overlay.build_frames()

        self.assertEqual(4, len(frames))
        self.assertEqual((64, 16), overlay.build_sheet().size)
        for frame in frames:
            self.assertEqual((16, 16), frame.size)
            self.assertIsNotNone(frame.getchannel("A").getbbox())

    def test_uses_only_the_specified_hard_edge_palette(self):
        allowed = set(overlay.PALETTE.values())
        visible_colors = set()

        for pixel in overlay.build_sheet().getdata():
            self.assertIn(pixel[3], (0, 255))
            self.assertIn(pixel, allowed)
            if pixel[3]:
                visible_colors.add(pixel)

        self.assertLessEqual(len(visible_colors), 5)

    def test_each_state_has_a_distinct_readable_silhouette(self):
        idle, run, attack, die = overlay.build_frames()

        self.assertEqual(4, len({frame.tobytes() for frame in (idle, run, attack, die)}))
        self.assertGreater(visible_pixel_count(attack), visible_pixel_count(idle))
        self.assertGreater(component_count(die), component_count(idle))
        self.assertEqual(2, list(attack.getdata()).count(overlay.PALETTE["violet"]))

    def test_checked_in_asset_matches_deterministic_generator(self):
        self.assertTrue(overlay.OUTPUT.is_file())
        with Image.open(overlay.OUTPUT) as generated:
            self.assertEqual("RGBA", generated.mode)
            self.assertEqual(overlay.build_sheet().tobytes(), generated.tobytes())


def visible_pixel_count(image):
    return sum(pixel[3] == 255 for pixel in image.getdata())


def component_count(image):
    visible = {
        (x, y)
        for y in range(image.height)
        for x in range(image.width)
        if image.getpixel((x, y))[3]
    }
    count = 0
    while visible:
        count += 1
        stack = [visible.pop()]
        while stack:
            x, y = stack.pop()
            for neighbour in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if neighbour in visible:
                    visible.remove(neighbour)
                    stack.append(neighbour)
    return count


if __name__ == "__main__":
    unittest.main()
