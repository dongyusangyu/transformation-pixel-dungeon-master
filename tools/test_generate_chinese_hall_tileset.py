from __future__ import annotations

import ast
import hashlib
import importlib
import unittest
from pathlib import Path

from PIL import Image, ImageChops


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
GENERATOR_PATH = ROOT / "tools" / "generate_chinese_hall_tileset.py"
SEWERS_PATH = ENV / "tiles_sewers.png"

TILE_SIZE = 16
SHEET_COLUMNS = 16

FLOOR = 0
FLOOR_DECO = 1
GRASS = 2
FLOOR_SP = 4
GRASS_ALT = 8
ENTRANCE = 16
EXIT = 17
WELL = 18
PEDESTAL = 20
ENTRANCE_SP = 22
WATER = 32
FLAT_WALL = 48
FLAT_DOOR = 56
FLAT_DOOR_LOCKED = 58
FLAT_REGION_DECO = 74
FLAT_REGION_DECO_ALT = 75
RAISED_REGION_DECO = 130
RAISED_REGION_DECO_ALT = 131
REGION_DECO_OVERHANG = 242
REGION_DECO_ALT_OVERHANG = 243
FLAT_HIGH_GRASS = 66
FLAT_FURROWED_GRASS = 67
FLAT_HIGH_GRASS_ALT = 69
FLAT_FURROWED_GRASS_ALT = 70
RAISED_HIGH_GRASS = 122
RAISED_FURROWED_GRASS = 123
RAISED_HIGH_GRASS_ALT = 125
RAISED_FURROWED_GRASS_ALT = 126
HIGH_GRASS_OVERHANG = 234
FURROWED_GRASS_OVERHANG = 235
HIGH_GRASS_OVERHANG_ALT = 237
FURROWED_GRASS_OVERHANG_ALT = 238
HIGH_GRASS_UNDERHANG = 250
FURROWED_GRASS_UNDERHANG = 251
HIGH_GRASS_UNDERHANG_ALT = 253
FURROWED_GRASS_UNDERHANG_ALT = 254


def load_generator():
    if not GENERATOR_PATH.is_file():
        raise AssertionError(f"missing generator: {GENERATOR_PATH}")
    importlib.invalidate_caches()
    return importlib.import_module("tools.generate_chinese_hall_tileset")


def tile_rect(index: int) -> tuple[int, int, int, int]:
    x = index % SHEET_COLUMNS * TILE_SIZE
    y = index // SHEET_COLUMNS * TILE_SIZE
    return x, y, x + TILE_SIZE, y + TILE_SIZE


def tile(image: Image.Image, index: int) -> Image.Image:
    return image.crop(tile_rect(index)).convert("RGBA")


def digest(image: Image.Image) -> str:
    payload = image.mode.encode("ascii") + repr(image.size).encode("ascii") + image.tobytes()
    return hashlib.sha256(payload).hexdigest()


def opaque_palette(image: Image.Image) -> tuple[tuple[int, int, int, int], ...]:
    colors = tuple({pixel for pixel in image.convert("RGBA").getdata() if pixel[3]})
    if not colors:
        raise AssertionError("expected a non-empty palette")
    return colors


def rgb_distance(left: tuple[int, ...], right: tuple[int, ...]) -> int:
    return sum((left[channel] - right[channel]) ** 2 for channel in range(3))


def foreground_mask(subject: Image.Image, background: Image.Image) -> Image.Image:
    difference = ImageChops.difference(subject.convert("RGBA"), background.convert("RGBA"))
    return difference.convert("L").point(lambda value: 255 if value else 0)


def occupied_columns(mask: Image.Image, top: int, bottom: int) -> set[int]:
    return {
        x
        for y in range(top, bottom)
        for x in range(mask.width)
        if mask.getpixel((x, y))
    }


class ChineseHallPublicApiTest(unittest.TestCase):
    def test_required_public_api_exists(self) -> None:
        generator = load_generator()
        for name in ("tile_rect", "build_tileset", "build_water_texture", "generate"):
            self.assertTrue(callable(getattr(generator, name, None)), f"missing API: {name}")

    def test_tile_rect_covers_valid_indices(self) -> None:
        generator = load_generator()
        self.assertEqual((0, 0, 16, 16), generator.tile_rect(0))
        self.assertEqual((240, 240, 256, 256), generator.tile_rect(255))
        for index in range(256):
            x0, y0, x1, y1 = generator.tile_rect(index)
            self.assertEqual((16, 16), (x1 - x0, y1 - y0))
            self.assertTrue(0 <= x0 < x1 <= 256)
            self.assertTrue(0 <= y0 < y1 <= 256)


class ChineseHallImageContractTest(unittest.TestCase):
    def test_output_dimensions_and_modes(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        water = generator.build_water_texture()
        self.assertIsInstance(atlas, Image.Image)
        self.assertIsInstance(water, Image.Image)
        self.assertEqual((256, 256), atlas.size)
        self.assertEqual("RGBA", atlas.mode)
        self.assertEqual((32, 32), water.size)
        self.assertEqual("RGBA", water.mode)


class ChineseHallDeterminismTest(unittest.TestCase):
    def test_builders_are_deterministic(self) -> None:
        generator = load_generator()
        self.assertEqual(digest(generator.build_tileset()), digest(generator.build_tileset()))
        self.assertEqual(
            digest(generator.build_water_texture()),
            digest(generator.build_water_texture()),
        )


class ChineseHallSourcePolicyTest(unittest.TestCase):
    def test_text_rendering_apis_are_forbidden(self) -> None:
        if not GENERATOR_PATH.is_file():
            self.fail(f"missing generator: {GENERATOR_PATH}")
        source = GENERATOR_PATH.read_text(encoding="utf-8")
        tree = ast.parse(source, filename=str(GENERATOR_PATH))
        forbidden_symbols = {
            "ImageFont",
            "FreeTypeFont",
            "load_default",
            "truetype",
            "text",
            "multiline_text",
            "textbbox",
            "textlength",
        }
        for node in ast.walk(tree):
            if isinstance(node, ast.Name):
                self.assertNotIn(node.id, forbidden_symbols)
            elif isinstance(node, ast.Attribute):
                self.assertNotIn(node.attr, forbidden_symbols)


class ChineseHallKeySlotTest(unittest.TestCase):
    def test_key_semantic_slots_are_non_empty(self) -> None:
        atlas = load_generator().build_tileset()
        required = (
            FLOOR,
            FLOOR_DECO,
            FLOOR_SP,
            ENTRANCE,
            EXIT,
            WELL,
            PEDESTAL,
            FLAT_WALL,
            FLAT_DOOR,
            FLAT_DOOR_LOCKED,
            FLAT_REGION_DECO,
            FLAT_REGION_DECO_ALT,
            RAISED_REGION_DECO,
            RAISED_REGION_DECO_ALT,
            REGION_DECO_OVERHANG,
            REGION_DECO_ALT_OVERHANG,
        )
        for index in required:
            with self.subTest(index=index):
                alpha = tile(atlas, index).getchannel("A")
                self.assertIsNotNone(alpha.getbbox(), f"slot {index} is fully transparent")

    def test_only_semantically_unused_slots_are_fully_transparent(self) -> None:
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

    def test_well_slots_reuse_original_square_well_pixels(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")
        for index in (18, 19):
            with self.subTest(index=index):
                self.assertEqual(
                    digest(tile(sewers, index)),
                    digest(tile(atlas, index)),
                    "well silhouette and pixels must remain the original square well",
                )


class ChineseHallObjectBackgroundTest(unittest.TestCase):
    def test_objects_use_hall_floor_backgrounds(self) -> None:
        atlas = load_generator().build_tileset()
        brick_background = tile(atlas, FLOOR)
        wood_background = tile(atlas, FLOOR_SP)
        brick_objects = (64, 65, 66, 67, 69, 70, 72, 74, 76, 77, 78, 120, 121, 122, 123, 125, 126, 128, 130, 132, 133, 134)
        wood_objects = (73, 75, 129, 131)
        corners = ((0, 0), (15, 0), (0, 15), (15, 15))
        for index in brick_objects:
            with self.subTest(index=index, background="brick"):
                subject = tile(atlas, index)
                self.assertEqual(
                    [brick_background.getpixel(point) for point in corners],
                    [subject.getpixel(point) for point in corners],
                )
        for index in wood_objects:
            with self.subTest(index=index, background="wood"):
                subject = tile(atlas, index)
                self.assertEqual(
                    [wood_background.getpixel(point) for point in corners],
                    [subject.getpixel(point) for point in corners],
                )


class ChineseHallVegetationTest(unittest.TestCase):
    @staticmethod
    def green_pixels(subject: Image.Image) -> int:
        return sum(
            1
            for red, green, blue, alpha in subject.convert("RGBA").getdata()
            if alpha and green >= red + 10 and green >= blue + 6
        )

    def test_grass_floor_slots_read_as_moss(self) -> None:
        atlas = load_generator().build_tileset()
        for index in (GRASS, GRASS_ALT):
            with self.subTest(index=index):
                self.assertGreaterEqual(self.green_pixels(tile(atlas, index)), 12)
                self.assertNotEqual(digest(tile(atlas, FLOOR)), digest(tile(atlas, index)))

    def test_high_and_furrowed_grass_read_as_garden_weeds(self) -> None:
        atlas = load_generator().build_tileset()
        flat_slots = (
            FLAT_HIGH_GRASS,
            FLAT_FURROWED_GRASS,
            FLAT_HIGH_GRASS_ALT,
            FLAT_FURROWED_GRASS_ALT,
        )
        for index in flat_slots:
            with self.subTest(index=index):
                self.assertGreaterEqual(self.green_pixels(tile(atlas, index)), 24)

    def test_raised_vegetation_has_body_overhang_and_underhang(self) -> None:
        atlas = load_generator().build_tileset()
        groups = (
            (RAISED_HIGH_GRASS, HIGH_GRASS_OVERHANG, HIGH_GRASS_UNDERHANG),
            (RAISED_FURROWED_GRASS, FURROWED_GRASS_OVERHANG, FURROWED_GRASS_UNDERHANG),
            (RAISED_HIGH_GRASS_ALT, HIGH_GRASS_OVERHANG_ALT, HIGH_GRASS_UNDERHANG_ALT),
            (RAISED_FURROWED_GRASS_ALT, FURROWED_GRASS_OVERHANG_ALT, FURROWED_GRASS_UNDERHANG_ALT),
        )
        for body_index, overhang_index, underhang_index in groups:
            body = foreground_mask(tile(atlas, body_index), tile(atlas, FLOOR))
            overhang = tile(atlas, overhang_index).getchannel("A")
            underhang = tile(atlas, underhang_index).getchannel("A")
            touching = {
                x
                for x in range(TILE_SIZE)
                if overhang.getpixel((x, TILE_SIZE - 1)) and body.getpixel((x, 0))
            }
            with self.subTest(body=body_index):
                self.assertGreaterEqual(len(touching), 3)
                self.assertIsNotNone(underhang.getbbox())


class ChineseHallDoorTest(unittest.TestCase):
    def test_door_states_have_distinct_silhouettes_or_materials(self) -> None:
        atlas = load_generator().build_tileset()
        groups = (
            (56, 57, 58, 59),
            (112, 113, 114, 115, 116),
            (224, 225, 226),
            (227, 228, 229),
        )
        for indices in groups:
            variants = [digest(tile(atlas, index)) for index in indices]
            with self.subTest(indices=indices):
                self.assertEqual(
                    len(indices),
                    len(set(variants)),
                    "closed, open, locked, and jade door states must remain distinct",
                )


class ChineseHallTowerStairTest(unittest.TestCase):
    def test_tower_stairs_reverse_the_standard_dungeon_directions(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        sewers = Image.open(SEWERS_PATH).convert("RGBA")
        pairs = (
            (ENTRANCE, EXIT, generator.STONE_RAMP),
            (EXIT, ENTRANCE, generator.STONE_RAMP),
            (ENTRANCE_SP, EXIT, generator.WOOD_RAMP),
        )
        for target_index, source_index, ramp in pairs:
            expected = generator.recolor_structure(tile(sewers, source_index), ramp)
            with self.subTest(target=target_index, source=source_index):
                self.assertEqual(digest(expected), digest(tile(atlas, target_index)))

    def test_exit_slots_are_not_reused_door_states(self) -> None:
        atlas = load_generator().build_tileset()
        pairs = ((60, 57), (61, 58), (230, 225))
        for exit_index, door_index in pairs:
            with self.subTest(exit=exit_index, door=door_index):
                self.assertNotEqual(
                    digest(tile(atlas, exit_index)),
                    digest(tile(atlas, door_index)),
                    "exit visuals must not be ordinary door visuals",
                )


class ChineseHallWallStitchingTest(unittest.TestCase):
    def test_wall_details_do_not_change_prison_bitmask_alpha(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(ENV / "tiles_prison.png") as source:
            prison = source.convert("RGBA")
        wall_indices = tuple(range(80, 120)) + tuple(range(144, 224))
        for index in wall_indices:
            with self.subTest(index=index):
                self.assertEqual(
                    tuple(tile(prison, index).getchannel("A").getdata()),
                    tuple(tile(atlas, index).getchannel("A").getdata()),
                )

    def test_internal_walls_avoid_full_height_vermilion_edge_posts(self) -> None:
        atlas = load_generator().build_tileset()
        for index in range(144, 192):
            subject = tile(atlas, index)
            saturated_edge_pixels = sum(
                1
                for x in (1, 14)
                for y in range(4, 16)
                if (
                    subject.getpixel((x, y))[3]
                    and subject.getpixel((x, y))[0] >= 145
                    and subject.getpixel((x, y))[0] >= subject.getpixel((x, y))[1] + 35
                    and subject.getpixel((x, y))[0] >= subject.getpixel((x, y))[2] + 25
                )
            )
            with self.subTest(index=index):
                self.assertLessEqual(saturated_edge_pixels, 2)

    def test_raised_wall_open_edge_variants_remain_distinct(self) -> None:
        atlas = load_generator().build_tileset()
        groups = (
            range(80, 84),
            range(84, 88),
            range(88, 92),
            range(92, 96),
            range(96, 100),
            range(100, 104),
            range(108, 112),
        )
        for indices in groups:
            variants = {digest(tile(atlas, index)) for index in indices}
            with self.subTest(start=indices.start):
                self.assertGreaterEqual(len(variants), 3)

    def test_internal_wall_bitmasks_preserve_directional_variation(self) -> None:
        atlas = load_generator().build_tileset()
        for indices in (range(144, 160), range(160, 176), range(176, 192)):
            variants = {digest(tile(atlas, index)) for index in indices}
            with self.subTest(start=indices.start):
                self.assertGreaterEqual(
                    len(variants),
                    12,
                    "16-way internal walls lost too many directional silhouettes",
                )

    def test_wall_and_sideways_door_overhangs_keep_four_edge_variants(self) -> None:
        atlas = load_generator().build_tileset()
        groups = (
            range(192, 196),
            range(196, 200),
            range(200, 204),
            range(208, 212),
            range(212, 216),
            range(216, 220),
            range(220, 224),
        )
        for indices in groups:
            variants = {digest(tile(atlas, index)) for index in indices}
            with self.subTest(start=indices.start):
                self.assertEqual(4, len(variants))


class ChineseHallWaterMaskTest(unittest.TestCase):
    def test_sixteen_water_masks_follow_sewer_structure(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        water_texture = generator.build_water_texture()
        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")

        target_ground = opaque_palette(tile(atlas, FLOOR))
        target_water = opaque_palette(water_texture)
        target_masks = []

        for direction_mask in range(16):
            reference_alpha = tuple(tile(sewers, WATER + direction_mask).getchannel("A").getdata())
            target_tile = tile(atlas, WATER + direction_mask)
            target_alpha = tuple(target_tile.getchannel("A").getdata())
            with self.subTest(direction_mask=direction_mask):
                self.assertEqual(
                    reference_alpha,
                    target_alpha,
                    "water/ground boundary must preserve the sewer transparency mask",
                )
                if direction_mask:
                    opaque = [pixel for pixel in target_tile.getdata() if pixel[3]]
                    self.assertTrue(opaque, "transition lacks shoreline pixels")
                    for pixel in opaque:
                        ground_distance = min(rgb_distance(pixel, color) for color in target_ground)
                        water_distance = min(rgb_distance(pixel, color) for color in target_water)
                        self.assertLess(
                            min(ground_distance, water_distance),
                            1800,
                            "shoreline pixel must belong to the hall floor or water palette",
                        )
            target_masks.append(target_alpha)

        self.assertEqual(16, len(set(target_masks)))


class ChineseHallRegionDecoAlignmentTest(unittest.TestCase):
    def test_region_deco_bodies_align_with_overhangs(self) -> None:
        atlas = load_generator().build_tileset()
        pairs = (
            (RAISED_REGION_DECO, REGION_DECO_OVERHANG, FLOOR),
            (RAISED_REGION_DECO_ALT, REGION_DECO_ALT_OVERHANG, FLOOR_SP),
        )
        for body_index, overhang_index, background_index in pairs:
            body = foreground_mask(tile(atlas, body_index), tile(atlas, background_index))
            overhang = tile(atlas, overhang_index).getchannel("A")
            body_columns = occupied_columns(body, 0, 5)
            overhang_columns = occupied_columns(overhang, 11, 16)
            with self.subTest(body=body_index, overhang=overhang_index):
                self.assertGreaterEqual(len(body_columns), 2)
                self.assertGreaterEqual(len(overhang_columns), 2)
                self.assertGreaterEqual(len(body_columns & overhang_columns), 2)
                body_center = (min(body_columns) + max(body_columns)) / 2
                overhang_center = (min(overhang_columns) + max(overhang_columns)) / 2
                self.assertLessEqual(abs(body_center - overhang_center), 2)

    def test_region_deco_layers_touch_across_tile_boundary(self) -> None:
        atlas = load_generator().build_tileset()
        pairs = (
            (RAISED_REGION_DECO, REGION_DECO_OVERHANG, FLOOR),
            (RAISED_REGION_DECO_ALT, REGION_DECO_ALT_OVERHANG, FLOOR_SP),
        )
        for body_index, overhang_index, background_index in pairs:
            body = foreground_mask(tile(atlas, body_index), tile(atlas, background_index))
            overhang = tile(atlas, overhang_index).getchannel("A")
            touching_columns = {
                x
                for x in range(TILE_SIZE)
                if overhang.getpixel((x, TILE_SIZE - 1)) and body.getpixel((x, 0))
            }
            with self.subTest(body=body_index, overhang=overhang_index):
                self.assertGreaterEqual(
                    len(touching_columns),
                    2,
                    "body and overhang must form one continuous object",
                )

    def test_all_raised_objects_touch_their_overhangs(self) -> None:
        atlas = load_generator().build_tileset()
        pairs = (
            (120, 232, FLOOR),
            (121, 233, FLOOR),
            (122, 234, FLOOR),
            (125, 237, FLOOR),
            (128, 240, FLOOR),
            (129, 241, FLOOR_SP),
            (130, 242, FLOOR),
            (131, 243, FLOOR_SP),
            (132, 244, FLOOR),
            (133, 245, FLOOR),
            (134, 246, FLOOR),
        )
        for body_index, overhang_index, background_index in pairs:
            body = foreground_mask(tile(atlas, body_index), tile(atlas, background_index))
            overhang = tile(atlas, overhang_index).getchannel("A")
            touching_columns = {
                x
                for x in range(TILE_SIZE)
                if overhang.getpixel((x, TILE_SIZE - 1)) and body.getpixel((x, 0))
            }
            with self.subTest(body=body_index, overhang=overhang_index):
                self.assertGreaterEqual(
                    len(touching_columns),
                    1,
                    "raised object is visually detached from its overhang",
                )


if __name__ == "__main__":
    unittest.main()
