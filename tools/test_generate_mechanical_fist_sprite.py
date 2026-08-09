import importlib.util
import unittest
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
GENERATOR = ROOT / "tools/generate_mechanical_fist_sprite.py"
SOURCE = ROOT / "core/src/main/assets/sprites/yog_fists.png"
OUTPUT = ROOT / "core/src/main/assets/sprites/mechanical_fist.png"
PREVIEW = ROOT / "docs/pixel-art/mechanical-fist/mechanical_fist_preview.png"

FRAME_WIDTH = 24
FRAME_HEIGHT = 17
FRAME_COUNT = 10
ACTIVE_FRAME_COUNT = 7
RUSTED_ROW_TOP = FRAME_HEIGHT * 3


class MechanicalFistSpriteGeneratorTest(unittest.TestCase):

	@classmethod
	def setUpClass(cls):
		spec = importlib.util.spec_from_file_location("mechanical_fist_generator", GENERATOR)
		module = importlib.util.module_from_spec(spec)
		spec.loader.exec_module(module)
		module.generate()

		cls.source = Image.open(SOURCE).convert("RGBA").crop(
			(0, RUSTED_ROW_TOP, FRAME_WIDTH * FRAME_COUNT, RUSTED_ROW_TOP + FRAME_HEIGHT)
		)
		cls.output = Image.open(OUTPUT).convert("RGBA")

	def test_canvas_uses_ten_twenty_four_by_seventeen_frames(self):
		self.assertEqual((FRAME_WIDTH * FRAME_COUNT, FRAME_HEIGHT), self.output.size)
		for frame in range(FRAME_COUNT):
			left = frame * FRAME_WIDTH
			alpha = self.output.crop(
				(left, 0, left + FRAME_WIDTH, FRAME_HEIGHT)
			).getchannel("A")
			if frame < ACTIVE_FRAME_COUNT:
				self.assertIsNotNone(alpha.getbbox(), f"frame {frame} must not be empty")
			else:
				self.assertIsNone(alpha.getbbox(), f"reserved frame {frame} must stay empty")

	def test_output_uses_hard_transparency_and_limited_palette(self):
		alphas = {pixel[3] for pixel in self.output.getdata()}
		visible_colors = {pixel[:3] for pixel in self.output.getdata() if pixel[3] == 255}

		self.assertEqual({0, 255}, alphas)
		self.assertLessEqual(len(visible_colors), 12)

	def test_every_frame_preserves_rusted_fist_alpha_silhouette(self):
		self.assertEqual(
			list(self.source.getchannel("A").getdata()),
			list(self.output.getchannel("A").getdata()),
		)

	def test_palette_contains_brass_and_cyan_technology_accents(self):
		visible_colors = {pixel[:3] for pixel in self.output.getdata() if pixel[3] == 255}

		self.assertTrue(any(red > blue and red > 100 for red, green, blue in visible_colors))
		self.assertTrue(any(blue > red and green > red for red, green, blue in visible_colors))

	def test_preview_is_nearest_neighbor_eight_times_scale(self):
		preview = Image.open(PREVIEW).convert("RGBA")
		self.assertEqual((self.output.width * 8, self.output.height * 8), preview.size)


if __name__ == "__main__":
	unittest.main()
