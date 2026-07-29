import tempfile
import unittest
from pathlib import Path

from PIL import Image

from validate_tower_monster_sprite import SpriteValidationError, validate_sprite


class TowerMonsterSpriteValidatorTest(unittest.TestCase):

    def test_accepts_hard_edged_nonempty_frames(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "valid.png"
            image = Image.new("RGBA", (24, 16))
            image.putpixel((2, 3), (10, 20, 30, 255))
            image.putpixel((14, 3), (10, 20, 30, 255))
            image.save(path)

            report = validate_sprite(path, 12, 16, expected_frames=2)

            self.assertEqual(2, report["frame_count"])
            self.assertEqual(1, report["visible_colors"])

    def test_rejects_frames_larger_than_sixteen_pixels(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "too_large.png"
            Image.new("RGBA", (17, 16), (1, 2, 3, 255)).save(path)

            with self.assertRaisesRegex(SpriteValidationError, "16x16"):
                validate_sprite(path, 17, 16)

    def test_rejects_semitransparent_pixels(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "soft_alpha.png"
            image = Image.new("RGBA", (12, 16))
            image.putpixel((2, 3), (10, 20, 30, 128))
            image.save(path)

            with self.assertRaisesRegex(SpriteValidationError, "alpha"):
                validate_sprite(path, 12, 16)

    def test_rejects_empty_animation_frames(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "empty_frame.png"
            image = Image.new("RGBA", (24, 16))
            image.putpixel((2, 3), (10, 20, 30, 255))
            image.save(path)

            with self.assertRaisesRegex(SpriteValidationError, "frame 1"):
                validate_sprite(path, 12, 16, expected_frames=2)


if __name__ == "__main__":
    unittest.main()
