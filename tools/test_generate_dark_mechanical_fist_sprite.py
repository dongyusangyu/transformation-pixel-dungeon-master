import unittest
from pathlib import Path

from PIL import Image

from generate_dark_mechanical_fist_sprite import OUTPUT, SOURCE, recolor


class DarkMechanicalFistSpriteTest(unittest.TestCase):
    def test_recolor_preserves_pixel_sheet_constraints(self):
        source = Image.open(SOURCE).convert("RGBA")
        output = recolor(source)

        self.assertEqual((240, 17), output.size)
        self.assertEqual(list(source.getchannel("A").getdata()), list(output.getchannel("A").getdata()))
        self.assertEqual({0, 255}, {pixel[3] for pixel in output.getdata()})
        visible_colors = {pixel[:3] for pixel in output.getdata() if pixel[3] != 0}
        self.assertLessEqual(len(visible_colors), 12)

    def test_valid_frames_have_eye_and_trailing_frames_are_empty(self):
        output = recolor(Image.open(SOURCE))
        pupil = (255, 220, 244)
        bright_pupil = pupil
        for frame in range(7):
            crop = output.crop((frame * 24, 0, frame * 24 + 24, 17))
            self.assertIsNotNone(crop.getchannel("A").getbbox())
            eye_colors = [crop.getpixel((x, y))[:3] for x in range(10, 13) for y in range(5, 8)]
            self.assertIn(pupil, eye_colors)
            if frame in (5, 6):
                self.assertIn(bright_pupil, eye_colors)
        for frame in (7, 8, 9):
            self.assertIsNone(output.crop((frame * 24, 0, frame * 24 + 24, 17)).getchannel("A").getbbox())


if __name__ == "__main__":
    unittest.main()
