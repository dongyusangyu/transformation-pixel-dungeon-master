from __future__ import annotations

import hashlib
import unittest
from pathlib import Path

from PIL import Image, ImageStat


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
ATLAS_PATH = ENV / "tiles_gothic_castle.png"
REFERENCE_PATH = ENV / "tiles_halls.png"
WATER_PATH = ENV / "water_gothic_castle.png"

TILE_SIZE = 16
SHEET_COLUMNS = 16


def tile(image: Image.Image, index: int) -> Image.Image:
    left = index % SHEET_COLUMNS * TILE_SIZE
    top = index // SHEET_COLUMNS * TILE_SIZE
    return image.crop((left, top, left + TILE_SIZE, top + TILE_SIZE)).convert("RGBA")


def digest(image: Image.Image) -> str:
    return hashlib.sha256(image.tobytes()).hexdigest()


def foreground_mask(subject: Image.Image, background: Image.Image) -> tuple[bool, ...]:
    return tuple(
        subject.getpixel((x, y)) != background.getpixel((x, y))
        for y in range(TILE_SIZE)
        for x in range(TILE_SIZE)
    )


class GothicCastleTilesetTest(unittest.TestCase):
    def setUp(self) -> None:
        self.atlas = Image.open(ATLAS_PATH).convert("RGBA")
        self.reference = Image.open(REFERENCE_PATH).convert("RGBA")

    def test_atlas_keeps_the_engine_contract(self) -> None:
        self.assertEqual((256, 256), self.atlas.size)
        self.assertEqual(
            tuple(self.reference.getchannel("A").getdata()),
            tuple(self.atlas.getchannel("A").getdata()),
            "gothic redraw must preserve every halls stitching silhouette",
        )

    def test_plain_floor_variants_are_dark_but_readable(self) -> None:
        variants = []
        for index in (0, 6, 12):
            subject = tile(self.atlas, index).convert("L")
            stats = ImageStat.Stat(subject)
            with self.subTest(index=index):
                self.assertGreaterEqual(stats.mean[0], 32)
                self.assertLessEqual(stats.mean[0], 45)
                self.assertGreaterEqual(stats.stddev[0], 3.5)
            variants.append(digest(subject))
        self.assertEqual(3, len(set(variants)))

    def test_tower_stairs_reverse_the_standard_halls_directions(self) -> None:
        pairs = (
            (16, 17, 0, 0),
            (17, 16, 0, 0),
            (22, 17, 4, 0),
        )
        for target_index, source_index, background_index, source_background_index in pairs:
            actual = foreground_mask(
                tile(self.atlas, target_index), tile(self.atlas, background_index)
            )
            expected = foreground_mask(
                tile(self.reference, source_index),
                tile(self.reference, source_background_index),
            )
            with self.subTest(target=target_index, source=source_index):
                self.assertEqual(expected, actual)

    def test_water_texture_is_a_seamless_32_pixel_loop(self) -> None:
        water = Image.open(WATER_PATH).convert("RGBA")
        self.assertEqual((32, 32), water.size)
        self.assertEqual(
            [water.getpixel((x, 0)) for x in range(32)],
            [water.getpixel((x, 31)) for x in range(32)],
        )
        self.assertEqual(
            [water.getpixel((0, y)) for y in range(32)],
            [water.getpixel((31, y)) for y in range(32)],
        )


if __name__ == "__main__":
    unittest.main()
