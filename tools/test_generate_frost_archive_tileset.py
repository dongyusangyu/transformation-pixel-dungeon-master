from __future__ import annotations

import ast
import hashlib
import importlib.util
import unittest
from pathlib import Path

from PIL import Image, ImageChops, ImageStat


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
GENERATOR_PATH = ROOT / "tools" / "generate_frost_archive_tileset.py"
SEWERS_PATH = ENV / "tiles_sewers.png"
ATLAS_PATH = ENV / "tiles_frost_archive.png"
WATER_PATH = ENV / "water_frost_archive.png"

T = 16
FLOOR = 0
FLOOR_DECO = 1
GRASS = 2
EMBERS = 3
FLOOR_SP = 4
FLOOR_ALT_1 = 6
FLOOR_DECO_ALT = 7
GRASS_ALT = 8
FLOOR_ALT_2 = 12
ENTRANCE = 16
EXIT = 17
WELL = 18
EMPTY_WELL = 19
ENTRANCE_SP = 22
WATER = 32
FLAT_WALL = 48
FLAT_WALL_DECO = 49
FLAT_BOOKSHELF = 50
FLAT_ALCHEMY = 64
FLAT_BARRICADE = 65
FLAT_HIGH_GRASS = 66
FLAT_FURROWED = 67
FLAT_STATUE = 72
FLAT_STATUE_SP = 73
FLAT_REGION_DECO = 74
FLAT_REGION_DECO_ALT = 75
RAISED_HIGH_GRASS = 122
RAISED_FURROWED = 123
HIGH_GRASS_OVERHANG = 234
FURROWED_OVERHANG = 235


def load_generator():
    spec = importlib.util.spec_from_file_location("frost_archive_generator", GENERATOR_PATH)
    if spec is None or spec.loader is None:
        raise AssertionError(f"cannot load generator: {GENERATOR_PATH}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def tile(image: Image.Image, index: int) -> Image.Image:
    left = index % 16 * T
    top = index // 16 * T
    return image.crop((left, top, left + T, top + T)).convert("RGBA")


def digest(image: Image.Image) -> str:
    return hashlib.sha256(image.mode.encode() + repr(image.size).encode() + image.tobytes()).hexdigest()


def ice_pixels(image: Image.Image) -> int:
    return sum(1 for red, green, blue, alpha in image.getdata()
               if alpha and blue >= red + 16 and blue >= green - 12 and blue >= 85)


def pale_frost_pixels(image: Image.Image) -> int:
    return sum(1 for red, green, blue, alpha in image.getdata()
               if alpha and min(red, green, blue) >= 105 and blue >= red)


def amber_pixels(image: Image.Image) -> int:
    return sum(1 for red, green, blue, alpha in image.getdata()
               if alpha and red >= blue + 35 and green >= blue + 12 and red >= 95)


def dull_pixels(image: Image.Image) -> int:
    return sum(1 for red, green, blue, alpha in image.getdata()
               if alpha and max(red, green, blue) - min(red, green, blue) <= 18
               and 55 <= max(red, green, blue) <= 145)


class FrostArchiveContractTest(unittest.TestCase):
    def test_public_api_and_output_contract(self) -> None:
        generator = load_generator()
        for name in ("tile_rect", "build_tileset", "build_water_texture", "generate"):
            self.assertTrue(callable(getattr(generator, name, None)), name)
        atlas = generator.build_tileset()
        water = generator.build_water_texture()
        self.assertEqual(((256, 256), "RGBA"), (atlas.size, atlas.mode))
        self.assertEqual(((32, 32), "RGBA"), (water.size, water.mode))

    def test_builders_are_deterministic(self) -> None:
        generator = load_generator()
        self.assertEqual(digest(generator.build_tileset()), digest(generator.build_tileset()))
        self.assertEqual(digest(generator.build_water_texture()), digest(generator.build_water_texture()))

    def test_saved_assets_match_current_generator(self) -> None:
        generator = load_generator()
        self.assertTrue(ATLAS_PATH.is_file())
        self.assertTrue(WATER_PATH.is_file())
        self.assertEqual(digest(generator.build_tileset()), digest(Image.open(ATLAS_PATH).convert("RGBA")))
        self.assertEqual(digest(generator.build_water_texture()), digest(Image.open(WATER_PATH).convert("RGBA")))

    def test_water_texture_is_seamless(self) -> None:
        water = load_generator().build_water_texture()
        self.assertEqual(list(water.crop((0, 0, 32, 1)).getdata()),
                         list(water.crop((0, 31, 32, 32)).getdata()))
        self.assertEqual(list(water.crop((0, 0, 1, 32)).getdata()),
                         list(water.crop((31, 0, 32, 32)).getdata()))

    def test_no_font_or_text_rendering_api_is_used(self) -> None:
        source = GENERATOR_PATH.read_text(encoding="utf-8")
        tree = ast.parse(source, filename=str(GENERATOR_PATH))
        forbidden = {"ImageFont", "load_default", "truetype", "text", "multiline_text"}
        for node in ast.walk(tree):
            if isinstance(node, ast.Name):
                self.assertNotIn(node.id, forbidden)
            elif isinstance(node, ast.Attribute):
                self.assertNotIn(node.attr, forbidden)


class FrostArchiveSemanticTest(unittest.TestCase):
    def setUp(self) -> None:
        self.atlas = load_generator().build_tileset()

    def test_floors_are_distinct_frosted_and_readable(self) -> None:
        variants = []
        for index in (FLOOR, FLOOR_ALT_1, FLOOR_ALT_2):
            subject = tile(self.atlas, index)
            stats = ImageStat.Stat(subject.convert("L"))
            self.assertGreaterEqual(stats.mean[0], 40)
            self.assertGreaterEqual(stats.stddev[0], 3)
            self.assertGreaterEqual(ice_pixels(subject), 4)
            variants.append(digest(subject.convert("L")))
        self.assertEqual(3, len(set(variants)))
        for index in (FLOOR_DECO, FLOOR_DECO_ALT, GRASS, GRASS_ALT):
            self.assertGreaterEqual(ice_pixels(tile(self.atlas, index)), 8)

    def test_walls_and_machinery_mix_ice_metal_and_amber_status_lights(self) -> None:
        for index in (FLAT_WALL, FLAT_WALL_DECO, FLAT_ALCHEMY, FLAT_STATUE):
            subject = tile(self.atlas, index)
            self.assertGreaterEqual(ice_pixels(subject), 5, index)
            self.assertGreaterEqual(amber_pixels(subject), 2, index)

    def test_live_and_exhausted_crystal_growths_are_visually_distinct(self) -> None:
        live = tile(self.atlas, FLAT_HIGH_GRASS)
        exhausted = tile(self.atlas, FLAT_FURROWED)
        self.assertGreaterEqual(ice_pixels(live), 24)
        self.assertGreaterEqual(pale_frost_pixels(live), 8)
        self.assertGreaterEqual(dull_pixels(exhausted), 20)
        self.assertNotEqual(digest(live), digest(exhausted))

    def test_raised_crystals_align_with_overhangs(self) -> None:
        for base, overhang in ((RAISED_HIGH_GRASS, HIGH_GRASS_OVERHANG),
                               (RAISED_FURROWED, FURROWED_OVERHANG)):
            lower = tile(self.atlas, base).getchannel("A")
            upper = tile(self.atlas, overhang).getchannel("A")
            lower_columns = {x for x in range(T) if lower.crop((x, 0, x + 1, 4)).getbbox()}
            upper_columns = {x for x in range(T) if upper.crop((x, 12, x + 1, T)).getbbox()}
            self.assertGreaterEqual(len(lower_columns & upper_columns), 4)

    def test_archive_props_have_distinct_silhouettes(self) -> None:
        indices = (FLAT_BOOKSHELF, FLAT_ALCHEMY, FLAT_BARRICADE,
                   FLAT_STATUE, FLAT_STATUE_SP, FLAT_REGION_DECO, FLAT_REGION_DECO_ALT)
        self.assertEqual(len(indices), len({digest(tile(self.atlas, index)) for index in indices}))

    def test_frost_motes_are_not_plain_floor(self) -> None:
        self.assertNotEqual(digest(tile(self.atlas, EMBERS)), digest(tile(self.atlas, FLOOR)))
        self.assertGreaterEqual(pale_frost_pixels(tile(self.atlas, EMBERS)), 3)


class FrostArchiveCompatibilityTest(unittest.TestCase):
    def setUp(self) -> None:
        self.atlas = load_generator().build_tileset()

    def test_water_transition_alpha_masks_match_sewers(self) -> None:
        sewers = Image.open(SEWERS_PATH).convert("RGBA")
        for index in range(WATER, WATER + 16):
            self.assertEqual(tuple(tile(sewers, index).getchannel("A").getdata()),
                             tuple(tile(self.atlas, index).getchannel("A").getdata()), index)

    def test_square_wells_are_pixel_identical_to_sewers(self) -> None:
        sewers = Image.open(SEWERS_PATH).convert("RGBA")
        for index in (WELL, EMPTY_WELL):
            self.assertEqual(digest(tile(sewers, index)), digest(tile(self.atlas, index)))

    def test_tower_stairs_reverse_sewer_foreground_silhouettes(self) -> None:
        generator = load_generator()
        sewers = Image.open(SEWERS_PATH).convert("RGBA")
        pairs = (
            (ENTRANCE, EXIT, FLOOR, generator.STONE_RAMP),
            (EXIT, ENTRANCE, FLOOR, generator.STONE_RAMP),
            (ENTRANCE_SP, EXIT, FLOOR_SP, generator.METAL_RAMP),
        )
        for target_index, source_index, background_index, ramp in pairs:
            expected = generator.base.draw_stair(
                sewers, source_index, tile(self.atlas, background_index), ramp
            )
            with self.subTest(target=target_index, source=source_index):
                self.assertEqual(digest(expected), digest(tile(self.atlas, target_index)))


if __name__ == "__main__":
    unittest.main()
