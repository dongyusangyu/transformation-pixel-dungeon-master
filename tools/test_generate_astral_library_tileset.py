from __future__ import annotations

import ast
import hashlib
import importlib
import unittest
from pathlib import Path

from PIL import Image, ImageChops, ImageStat


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
GENERATOR_PATH = ROOT / "tools" / "generate_astral_library_tileset.py"
SEWERS_PATH = ENV / "tiles_sewers.png"
HALLS_PATH = ENV / "tiles_halls.png"

TILE_SIZE = 16
SHEET_COLUMNS = 16

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
PEDESTAL = 20
ENTRANCE_SP = 22
WATER = 32
FLAT_WALL = 48
FLAT_WALL_DECO = 49
FLAT_BOOKSHELF = 50
FLAT_DOOR = 56
FLAT_DOOR_OPEN = 57
FLAT_DOOR_LOCKED = 58
FLAT_DOOR_CRYSTAL = 59
FLAT_ALCHEMY = 64
FLAT_BARRICADE = 65
FLAT_HIGH_GRASS = 66
FLAT_FURROWED = 67
FLAT_STATUE = 72
FLAT_STATUE_SP = 73
FLAT_REGION_DECO = 74
FLAT_REGION_DECO_ALT = 75
RAISED_ALCHEMY = 120
RAISED_BARRICADE = 121
RAISED_HIGH_GRASS = 122
RAISED_HIGH_GRASS_ALT = 125
RAISED_STATUE = 128
RAISED_STATUE_SP = 129
RAISED_REGION_DECO = 130
RAISED_REGION_DECO_ALT = 131
ALCHEMY_OVERHANG = 232
BARRICADE_OVERHANG = 233
HIGH_GRASS_OVERHANG = 234
HIGH_GRASS_OVERHANG_ALT = 237
STATUE_OVERHANG = 240
STATUE_SP_OVERHANG = 241
REGION_DECO_OVERHANG = 242
REGION_DECO_ALT_OVERHANG = 243


def load_generator():
    if not GENERATOR_PATH.is_file():
        raise AssertionError(f"missing generator: {GENERATOR_PATH}")
    importlib.invalidate_caches()
    return importlib.import_module("tools.generate_astral_library_tileset")


def tile_rect(index: int) -> tuple[int, int, int, int]:
    left = index % SHEET_COLUMNS * TILE_SIZE
    top = index // SHEET_COLUMNS * TILE_SIZE
    return left, top, left + TILE_SIZE, top + TILE_SIZE


def tile(image: Image.Image, index: int) -> Image.Image:
    return image.crop(tile_rect(index)).convert("RGBA")


def digest(image: Image.Image) -> str:
    payload = image.mode.encode("ascii") + repr(image.size).encode("ascii") + image.tobytes()
    return hashlib.sha256(payload).hexdigest()


def foreground_mask(subject: Image.Image, background: Image.Image) -> Image.Image:
    difference = ImageChops.difference(subject.convert("RGBA"), background.convert("RGBA"))
    return difference.convert("L").point(lambda value: 255 if value else 0)


def warm_pixels(subject: Image.Image) -> int:
    return sum(
        1
        for red, green, blue, alpha in subject.convert("RGBA").getdata()
        if alpha and red >= green + 18 and green >= blue - 12 and red >= 90
    )


def cyan_pixels(subject: Image.Image) -> int:
    return sum(
        1
        for red, green, blue, alpha in subject.convert("RGBA").getdata()
        if alpha and green >= red + 12 and blue >= red + 18 and blue >= 75
    )


class AstralLibraryPublicApiTest(unittest.TestCase):
    def test_required_public_api_exists(self) -> None:
        generator = load_generator()
        for name in ("tile_rect", "build_tileset", "build_water_texture", "generate"):
            self.assertTrue(callable(getattr(generator, name, None)), f"missing API: {name}")

    def test_tile_rect_covers_all_slots(self) -> None:
        generator = load_generator()
        self.assertEqual((0, 0, 16, 16), generator.tile_rect(0))
        self.assertEqual((240, 240, 256, 256), generator.tile_rect(255))
        for index in range(256):
            left, top, right, bottom = generator.tile_rect(index)
            self.assertEqual((16, 16), (right - left, bottom - top))


class AstralLibraryImageContractTest(unittest.TestCase):
    def test_output_dimensions_modes_and_reference_alpha(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        water = generator.build_water_texture()
        self.assertEqual((256, 256), atlas.size)
        self.assertEqual("RGBA", atlas.mode)
        self.assertEqual((32, 32), water.size)
        self.assertEqual("RGBA", water.mode)
        sewers = Image.open(ENV / "tiles_sewers.png").convert("RGBA")
        halls = Image.open(ENV / "tiles_halls.png").convert("RGBA")
        reference_alpha = (
            set(sewers.getchannel("A").getdata())
            | set(halls.getchannel("A").getdata())
            | {0, 255}
        )
        self.assertLessEqual(set(atlas.getchannel("A").getdata()), reference_alpha)

    def test_builders_are_deterministic(self) -> None:
        generator = load_generator()
        self.assertEqual(digest(generator.build_tileset()), digest(generator.build_tileset()))
        self.assertEqual(
            digest(generator.build_water_texture()),
            digest(generator.build_water_texture()),
        )

    def test_text_rendering_apis_are_forbidden(self) -> None:
        source = GENERATOR_PATH.read_text(encoding="utf-8")
        tree = ast.parse(source, filename=str(GENERATOR_PATH))
        forbidden = {
            "ImageFont",
            "load_default",
            "truetype",
            "text",
            "multiline_text",
            "textbbox",
            "textlength",
        }
        for node in ast.walk(tree):
            if isinstance(node, ast.Name):
                self.assertNotIn(node.id, forbidden)
            elif isinstance(node, ast.Attribute):
                self.assertNotIn(node.attr, forbidden)


class AstralLibraryGroundTest(unittest.TestCase):
    def test_floor_variants_are_dark_readable_and_distinct(self) -> None:
        atlas = load_generator().build_tileset()
        variants = []
        for index in (FLOOR, FLOOR_ALT_1, FLOOR_ALT_2):
            subject = tile(atlas, index).convert("L")
            stats = ImageStat.Stat(subject)
            with self.subTest(index=index):
                self.assertGreaterEqual(stats.mean[0], 30)
                self.assertLessEqual(stats.mean[0], 75)
                self.assertGreaterEqual(stats.stddev[0], 3)
            variants.append(digest(subject))
        self.assertEqual(3, len(set(variants)))

    def test_constellation_floors_have_brass_and_starlight_accents(self) -> None:
        atlas = load_generator().build_tileset()
        for index in (FLOOR_DECO, FLOOR_DECO_ALT):
            with self.subTest(index=index):
                subject = tile(atlas, index)
                self.assertGreaterEqual(warm_pixels(subject), 5)
                self.assertGreaterEqual(cyan_pixels(subject), 3)

    def test_grass_slots_read_as_scattered_pages_and_magic_dust(self) -> None:
        atlas = load_generator().build_tileset()
        for index in (GRASS, GRASS_ALT):
            with self.subTest(index=index):
                subject = tile(atlas, index)
                self.assertGreaterEqual(warm_pixels(subject), 5)
                self.assertGreaterEqual(cyan_pixels(subject), 2)

    def test_tower_stairs_reverse_the_standard_dungeon_directions(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")
        pairs = (
            (ENTRANCE, EXIT, FLOOR, generator.STONE_RAMP),
            (EXIT, ENTRANCE, FLOOR, generator.STONE_RAMP),
            (ENTRANCE_SP, EXIT, FLOOR_SP, generator.BRASS_RAMP),
        )
        for target_index, source_index, background_index, ramp in pairs:
            expected = generator.draw_stair(
                sewers, source_index, tile(atlas, background_index), ramp
            )
            with self.subTest(target=target_index, source=source_index):
                self.assertEqual(digest(expected), digest(tile(atlas, target_index)))

    def test_wells_reuse_original_square_well_pixels(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")
        for index in (WELL, EMPTY_WELL):
            with self.subTest(index=index):
                self.assertEqual(digest(tile(sewers, index)), digest(tile(atlas, index)))


class AstralLibraryWaterTest(unittest.TestCase):
    def test_water_masks_preserve_all_sixteen_sewer_shapes(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")
        masks = []
        for index in range(WATER, WATER + 16):
            expected = tuple(tile(sewers, index).getchannel("A").getdata())
            actual = tuple(tile(atlas, index).getchannel("A").getdata())
            with self.subTest(index=index):
                self.assertEqual(expected, actual)
            masks.append(actual)
        self.assertEqual(16, len(set(masks)))

    def test_water_texture_is_a_seamless_loop(self) -> None:
        water = load_generator().build_water_texture()
        self.assertEqual(
            [water.getpixel((x, 0)) for x in range(32)],
            [water.getpixel((x, 31)) for x in range(32)],
        )
        self.assertEqual(
            [water.getpixel((0, y)) for y in range(32)],
            [water.getpixel((31, y)) for y in range(32)],
        )
        self.assertGreaterEqual(cyan_pixels(water), 30)


class AstralLibrarySemanticSlotTest(unittest.TestCase):
    def test_key_semantic_slots_are_non_empty_and_distinct(self) -> None:
        atlas = load_generator().build_tileset()
        required = (
            FLOOR,
            FLOOR_DECO,
            GRASS,
            EMBERS,
            FLOOR_SP,
            ENTRANCE,
            EXIT,
            WELL,
            PEDESTAL,
            FLAT_WALL,
            FLAT_WALL_DECO,
            FLAT_BOOKSHELF,
            FLAT_DOOR,
            FLAT_DOOR_OPEN,
            FLAT_DOOR_LOCKED,
            FLAT_DOOR_CRYSTAL,
            FLAT_ALCHEMY,
            FLAT_BARRICADE,
            FLAT_HIGH_GRASS,
            FLAT_FURROWED,
            FLAT_STATUE,
            FLAT_STATUE_SP,
            FLAT_REGION_DECO,
            FLAT_REGION_DECO_ALT,
            RAISED_REGION_DECO,
            RAISED_REGION_DECO_ALT,
            REGION_DECO_OVERHANG,
            REGION_DECO_ALT_OVERHANG,
        )
        variants = []
        for index in required:
            with self.subTest(index=index):
                self.assertIsNotNone(tile(atlas, index).getchannel("A").getbbox())
                variants.append(digest(tile(atlas, index)))
        self.assertGreaterEqual(len(set(variants)), len(required) - 2)

    def test_only_engine_unused_slots_are_fully_transparent(self) -> None:
        atlas = load_generator().build_tileset()
        expected_blank = {
            32,
            117,
            118,
            119,
            204,
            205,
            206,
            207,
            231,
            236,
            239,
            247,
            248,
            249,
            252,
            255,
        }
        actual_blank = {
            index
            for index in range(256)
            if tile(atlas, index).getchannel("A").getbbox() is None
        }
        self.assertEqual(expected_blank, actual_blank)

    def test_page_piles_are_visually_distinct_from_doors_and_walls(self) -> None:
        atlas = load_generator().build_tileset()
        page_slots = (FLAT_HIGH_GRASS, FLAT_FURROWED)
        architecture = {
            digest(tile(atlas, index))
            for index in (FLAT_WALL, FLAT_BOOKSHELF, FLAT_DOOR, FLAT_DOOR_OPEN)
        }
        for index in page_slots:
            with self.subTest(index=index):
                subject = tile(atlas, index)
                self.assertGreaterEqual(warm_pixels(subject), 12)
                self.assertGreaterEqual(cyan_pixels(subject), 3)
                self.assertNotIn(digest(subject), architecture)


class AstralLibraryLayeringTest(unittest.TestCase):
    def test_raised_objects_touch_their_overhangs(self) -> None:
        atlas = load_generator().build_tileset()
        pairs = (
            (RAISED_ALCHEMY, ALCHEMY_OVERHANG, FLOOR),
            (RAISED_BARRICADE, BARRICADE_OVERHANG, FLOOR),
            (RAISED_HIGH_GRASS, HIGH_GRASS_OVERHANG, FLOOR),
            (RAISED_HIGH_GRASS_ALT, HIGH_GRASS_OVERHANG_ALT, FLOOR),
            (RAISED_STATUE, STATUE_OVERHANG, FLOOR),
            (RAISED_STATUE_SP, STATUE_SP_OVERHANG, FLOOR_SP),
            (RAISED_REGION_DECO, REGION_DECO_OVERHANG, FLOOR),
            (RAISED_REGION_DECO_ALT, REGION_DECO_ALT_OVERHANG, FLOOR_SP),
        )
        for body_index, overhang_index, background_index in pairs:
            body = foreground_mask(tile(atlas, body_index), tile(atlas, background_index))
            overhang = tile(atlas, overhang_index).getchannel("A")
            touching = {
                x
                for x in range(TILE_SIZE)
                if body.getpixel((x, 0)) and overhang.getpixel((x, TILE_SIZE - 1))
            }
            with self.subTest(body=body_index, overhang=overhang_index):
                self.assertGreaterEqual(len(touching), 1)

    def test_wall_bitmask_alpha_matches_reference_halls(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(HALLS_PATH) as source:
            halls = source.convert("RGBA")
        wall_indices = tuple(range(80, 120)) + tuple(range(144, 224))
        for index in wall_indices:
            with self.subTest(index=index):
                self.assertEqual(
                    tuple(tile(halls, index).getchannel("A").getdata()),
                    tuple(tile(atlas, index).getchannel("A").getdata()),
                )


if __name__ == "__main__":
    unittest.main()
