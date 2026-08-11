from pathlib import Path
import subprocess
import sys
import unittest

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "core/src/main/assets/sprites/snake.png"
OUTPUT = ROOT / "core/src/main/assets/sprites/earthly_serpent.png"
GENERATOR = ROOT / "tools/generate_earthly_serpent_sprite.py"
FRAME_WIDTH = 12
FRAME_HEIGHT = 11


class EarthlySerpentSpriteGeneratorTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        subprocess.run([sys.executable, str(GENERATOR)], cwd=ROOT, check=True)
        cls.source = Image.open(SOURCE).convert("RGBA")
        cls.output = Image.open(OUTPUT).convert("RGBA")

    def test_canvas_and_frame_geometry_are_preserved(self):
        self.assertEqual((256, 16), self.source.size)
        self.assertEqual(self.source.size, self.output.size)

        for frame in range(14):
            source_box = self._frame(self.source, frame).getbbox()
            output_box = self._frame(self.output, frame).getbbox()
            self.assertIsNotNone(output_box)
            self.assertLessEqual(output_box[0], source_box[0] + 1)
            self.assertGreaterEqual(output_box[0], source_box[0] - 1)
            self.assertLessEqual(output_box[1], source_box[1] + 1)
            self.assertGreaterEqual(output_box[1], source_box[1] - 1)
            self.assertLessEqual(output_box[2], source_box[2] + 1)
            self.assertGreaterEqual(output_box[2], source_box[2] - 1)
            self.assertLessEqual(output_box[3], source_box[3] + 1)
            self.assertGreaterEqual(output_box[3], source_box[3] - 1)

    def test_new_spit_and_pull_frames_are_nonempty(self):
        for frame in range(14, 21):
            self.assertIsNotNone(self._frame(self.output, frame).getbbox())

    def test_asset_uses_crisp_limited_palette_pixels(self):
        pixels = list(self.output.getdata())
        visible = {rgba for rgba in pixels if rgba[3] != 0}

        self.assertLessEqual(len(visible), 12)
        self.assertEqual({0, 255}, {rgba[3] for rgba in pixels})
        self.assertTrue(all(rgba[:3] == (0, 0, 0) for rgba in pixels if rgba[3] == 0))

    def test_acid_and_earth_colors_are_both_present(self):
        visible_rgb = {rgba[:3] for rgba in self.output.getdata() if rgba[3] == 255}

        self.assertTrue(any(g > r and g > b for r, g, b in visible_rgb))
        self.assertTrue(any(r > g > b for r, g, b in visible_rgb))

    @staticmethod
    def _frame(image, frame):
        left = frame * FRAME_WIDTH
        return image.crop((left, 0, left + FRAME_WIDTH, FRAME_HEIGHT))


if __name__ == "__main__":
    unittest.main()
