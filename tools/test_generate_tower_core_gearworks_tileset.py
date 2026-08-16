from __future__ import annotations

import ast
import hashlib
import importlib.util
import unittest
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
GENERATOR_PATH = ROOT / "tools" / "generate_tower_core_gearworks_tileset.py"
SEWERS_PATH = ENV / "tiles_sewers.png"
HALLS_PATH = ENV / "tiles_halls.png"

TILE_SIZE = 16
SHEET_COLUMNS = 16

FLOOR = 0
FLOOR_SP = 4
GRASS = 2
GRASS_ALT = 8
ENTRANCE = 16
EXIT = 17
WELL = 18
EMPTY_WELL = 19
ENTRANCE_SP = 22
WATER = 32

VEGETATION_PAIRS = (
    (66, 67, FLOOR),
    (69, 70, FLOOR),
    (122, 123, FLOOR),
    (125, 126, FLOOR),
    (234, 235, None),
    (237, 238, None),
    (250, 251, None),
    (253, 254, None),
)

RAISED_OVERHANG_PAIRS = (
    (120, 232, FLOOR),
    (121, 233, FLOOR),
    (122, 234, FLOOR),
    (123, 235, FLOOR),
    (125, 237, FLOOR),
    (126, 238, FLOOR),
    (128, 240, FLOOR),
    (129, 241, FLOOR_SP),
    (130, 242, FLOOR),
    (131, 243, FLOOR_SP),
    (132, 244, FLOOR),
    (133, 245, FLOOR),
    (134, 246, FLOOR),
)

MECHANICAL_FLAT_SLOTS = (65, 72, 73, 74, 75, 76, 77, 78)
MECHANICAL_RAISED_PAIRS = (
    (121, 233, FLOOR),
    (128, 240, FLOOR),
    (129, 241, FLOOR_SP),
    (130, 242, FLOOR),
    (131, 243, FLOOR_SP),
    (132, 244, FLOOR),
    (133, 245, FLOOR),
    (134, 246, FLOOR),
)

RESERVED_TRANSPARENT_SLOTS = {
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


def load_generator():
    spec = importlib.util.spec_from_file_location("gearworks_generator", GENERATOR_PATH)
    if spec is None or spec.loader is None:
        raise AssertionError(f"cannot load generator: {GENERATOR_PATH}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def tile_rect(index: int) -> tuple[int, int, int, int]:
    left = index % SHEET_COLUMNS * TILE_SIZE
    top = index // SHEET_COLUMNS * TILE_SIZE
    return left, top, left + TILE_SIZE, top + TILE_SIZE


def tile(image: Image.Image, index: int) -> Image.Image:
    return image.crop(tile_rect(index)).convert("RGBA")


def digest(image: Image.Image) -> str:
    payload = image.mode.encode("ascii") + repr(image.size).encode("ascii") + image.tobytes()
    return hashlib.sha256(payload).hexdigest()


def foreground_mask(
    subject: Image.Image, background: Image.Image
) -> tuple[bool, ...]:
    return tuple(
        subject_pixel != background_pixel
        for subject_pixel, background_pixel in zip(
            subject.convert("RGBA").getdata(),
            background.convert("RGBA").getdata(),
        )
    )


def alpha_mask(subject: Image.Image) -> tuple[bool, ...]:
    return tuple(alpha > 0 for alpha in subject.convert("RGBA").getchannel("A").getdata())


def mask_height(mask: tuple[bool, ...]) -> int:
    occupied_rows = {
        offset // TILE_SIZE for offset, occupied in enumerate(mask) if occupied
    }
    if not occupied_rows:
        return 0
    return max(occupied_rows) - min(occupied_rows) + 1


def foreground_pixels(
    subject: Image.Image, background: Image.Image | None
) -> list[tuple[int, int, int, int]]:
    rgba = subject.convert("RGBA")
    if background is None:
        return [pixel for pixel in rgba.getdata() if pixel[3]]
    mask = foreground_mask(rgba, background)
    return [pixel for pixel, occupied in zip(rgba.getdata(), mask) if occupied and pixel[3]]


def rgb_distance(left: tuple[int, ...], right: tuple[int, ...]) -> int:
    return sum((left[channel] - right[channel]) ** 2 for channel in range(3))


def is_dark_oil(pixel: tuple[int, int, int, int]) -> bool:
    red, green, blue, alpha = pixel
    return bool(
        alpha
        and max(red, green, blue) <= 52
        and max(red, green, blue) - min(red, green, blue) <= 14
    )


def is_deep_teal(pixel: tuple[int, int, int, int]) -> bool:
    red, green, blue, alpha = pixel
    maximum = max(red, green, blue)
    return bool(
        alpha
        and 52 < maximum <= 95
        and green >= red + 6
        and blue >= red + 4
    )


def is_verdigris(pixel: tuple[int, int, int, int]) -> bool:
    red, green, blue, alpha = pixel
    maximum = max(red, green, blue)
    return bool(
        alpha
        and 95 < maximum <= 155
        and green >= red + 12
        and green >= blue - 8
        and blue >= red
    )


class GearworksApiTest(unittest.TestCase):
    def test_required_public_api_exists(self) -> None:
        generator = load_generator()
        for name in ("tile_rect", "build_tileset", "build_water_texture", "generate"):
            self.assertTrue(callable(getattr(generator, name, None)), f"missing API: {name}")

    def test_tile_rect_covers_all_256_slots(self) -> None:
        generator = load_generator()
        self.assertEqual((0, 0, 16, 16), generator.tile_rect(0))
        self.assertEqual((240, 240, 256, 256), generator.tile_rect(255))
        for index in range(256):
            left, top, right, bottom = generator.tile_rect(index)
            with self.subTest(index=index):
                self.assertEqual(tile_rect(index), (left, top, right, bottom))
                self.assertEqual((16, 16), (right - left, bottom - top))
                self.assertTrue(0 <= left < right <= 256)
                self.assertTrue(0 <= top < bottom <= 256)

    def test_builders_return_required_rgba_images(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        water = generator.build_water_texture()
        self.assertIsInstance(atlas, Image.Image)
        self.assertIsInstance(water, Image.Image)
        self.assertEqual((256, 256), atlas.size)
        self.assertEqual("RGBA", atlas.mode)
        self.assertEqual((32, 32), water.size)
        self.assertEqual("RGBA", water.mode)

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
        for node in ast.walk(tree):
            if isinstance(node, ast.Import):
                for alias in node.names:
                    self.assertNotIn(alias.name, {"ImageFont", "PIL.ImageFont"})
            elif isinstance(node, ast.ImportFrom):
                imports_image_font = node.module in {"ImageFont", "PIL.ImageFont"} or (
                    node.module == "PIL"
                    and any(alias.name == "ImageFont" for alias in node.names)
                )
                self.assertFalse(imports_image_font, "ImageFont imports are forbidden")

        forbidden_calls = {
            "FreeTypeFont",
            "load_default",
            "truetype",
            "text",
            "multiline_text",
            "textbbox",
            "multiline_textbbox",
            "textlength",
        }
        for node in ast.walk(tree):
            if not isinstance(node, ast.Call):
                continue
            if isinstance(node.func, ast.Name):
                called_name = node.func.id
            elif isinstance(node.func, ast.Attribute):
                called_name = node.func.attr
            else:
                continue
            self.assertNotIn(called_name, forbidden_calls)


class GearworksReferenceStructureTest(unittest.TestCase):
    def test_square_wells_reuse_sewer_pixels_exactly(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")
        for index in (WELL, EMPTY_WELL):
            with self.subTest(index=index):
                self.assertEqual(digest(tile(sewers, index)), digest(tile(atlas, index)))

    def test_tower_stairs_reverse_sewer_foreground_silhouettes(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")
        pairs = (
            (ENTRANCE, EXIT, FLOOR, FLOOR),
            (EXIT, ENTRANCE, FLOOR, FLOOR),
            (ENTRANCE_SP, EXIT, FLOOR_SP, FLOOR),
        )
        for stair_index, source_index, background_index, source_background_index in pairs:
            expected = foreground_mask(
                tile(sewers, source_index), tile(sewers, source_background_index)
            )
            actual = foreground_mask(
                tile(atlas, stair_index), tile(atlas, background_index)
            )
            with self.subTest(index=stair_index):
                self.assertTrue(any(actual), "stair silhouette is empty")
                self.assertEqual(expected, actual)

    def test_water_shore_alpha_preserves_sewer_directions(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")
        source_masks = []
        for index in range(WATER, WATER + 16):
            expected = tuple(tile(sewers, index).getchannel("A").getdata())
            actual = tuple(tile(atlas, index).getchannel("A").getdata())
            with self.subTest(index=index):
                self.assertEqual(expected, actual)
            source_masks.append(expected)
        self.assertEqual(16, len(set(source_masks)))

    def test_wall_alpha_preserves_halls_bitmasks(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(HALLS_PATH) as source:
            halls = source.convert("RGBA")
        wall_indices = tuple(range(80, 117)) + (119,) + tuple(range(144, 224))
        for index in wall_indices:
            with self.subTest(index=index):
                self.assertEqual(
                    tuple(alpha > 0 for alpha in tile(halls, index).getchannel("A").getdata()),
                    tuple(alpha > 0 for alpha in tile(atlas, index).getchannel("A").getdata()),
                )

    def test_door_overhang_alpha_preserves_halls_slots(self) -> None:
        atlas = load_generator().build_tileset()
        with Image.open(HALLS_PATH) as source:
            halls = source.convert("RGBA")
        for index in range(224, 231):
            with self.subTest(index=index):
                self.assertEqual(
                    tuple(alpha > 0 for alpha in tile(halls, index).getchannel("A").getdata()),
                    tuple(alpha > 0 for alpha in tile(atlas, index).getchannel("A").getdata()),
                )

    def test_reserved_slots_remain_transparent(self) -> None:
        atlas = load_generator().build_tileset()
        actual_blank = {
            index
            for index in range(256)
            if tile(atlas, index).getchannel("A").getbbox() is None
        }
        self.assertEqual(RESERVED_TRANSPARENT_SLOTS, actual_blank)


class GearworksWallSemanticsTest(unittest.TestCase):
    @staticmethod
    def _count_colors(
        subject: Image.Image,
        colors: set[tuple[int, int, int, int]],
    ) -> int:
        return sum(pixel in colors for pixel in subject.convert("RGBA").getdata())

    def test_internal_wall_material_groups_match_engine_semantics(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        stone_colors = {
            generator.STONE_SHADOW,
            generator.STONE_DARK,
            generator.STONE_MID,
            generator.STONE_LIGHT,
            generator.STONE_EDGE,
        }
        wood_colors = {
            generator.DRY_DARK,
            generator.DRY_MID,
            generator.DRY_LIGHT,
        }
        mechanical_accents = {
            generator.BRASS_DARK,
            generator.BRASS_MID,
            generator.VERDIGRIS_DARK,
            generator.VERDIGRIS_MID,
            generator.VERDIGRIS_LIGHT,
        }

        for index in range(144, 160):
            with self.subTest(group="plain", index=index):
                self.assertGreater(self._count_colors(tile(atlas, index), stone_colors), 0)
        for index in range(160, 176):
            with self.subTest(group="decorated", index=index):
                self.assertGreater(
                    self._count_colors(tile(atlas, index), mechanical_accents),
                    0,
                )
        for index in range(176, 192):
            with self.subTest(group="wooden", index=index):
                self.assertGreaterEqual(
                    self._count_colors(tile(atlas, index), wood_colors),
                    4,
                )

    def test_wall_overhang_material_and_door_state_groups(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        wood_colors = {
            generator.DRY_DARK,
            generator.DRY_MID,
            generator.DRY_LIGHT,
        }
        stone_colors = {
            generator.STONE_SHADOW,
            generator.STONE_DARK,
            generator.STONE_MID,
            generator.STONE_LIGHT,
            generator.STONE_EDGE,
        }
        brass_colors = {
            generator.BRASS_DARK,
            generator.BRASS_MID,
            generator.BRASS_LIGHT,
        }
        pressure_colors = {
            generator.VERDIGRIS_DARK,
            generator.VERDIGRIS_MID,
            generator.VERDIGRIS_LIGHT,
            generator.COOLANT_GLINT,
        }

        for index in range(192, 196):
            with self.subTest(group="plain_overhang", index=index):
                self.assertGreater(self._count_colors(tile(atlas, index), stone_colors), 0)
        for index in range(196, 200):
            with self.subTest(group="decorated_overhang", index=index):
                self.assertGreater(
                    self._count_colors(
                        tile(atlas, index),
                        brass_colors | pressure_colors,
                    ),
                    0,
                )
        for index in range(200, 204):
            with self.subTest(group="wooden_overhang", index=index):
                self.assertGreaterEqual(
                    self._count_colors(tile(atlas, index), wood_colors),
                    4,
                )
        halls = Image.open(HALLS_PATH).convert("RGBA")
        states = ((57, 208), (56, 212), (58, 216), (59, 220))
        for flat_index, start in states:
            door_colors = {
                color[:3]
                for color in tile(atlas, flat_index).getdata()
                if color[3]
            }
            for variant in range(4):
                source_wall = tile(halls, 192 + variant)
                source_door = tile(halls, start + variant)
                target_door = tile(atlas, start + variant)
                door_pixels = {
                    target[:3]
                    for source_base, source, target in zip(
                        source_wall.getdata(),
                        source_door.getdata(),
                        target_door.getdata(),
                    )
                    if source != source_base and source[3]
                }
                with self.subTest(group="sideways_door", start=start, variant=variant):
                    self.assertTrue(door_pixels)
                    self.assertTrue(door_pixels.issubset(door_colors))

    def test_door_overhang_slot_mapping_matches_engine_constants(self) -> None:
        generator = load_generator()
        expected = {
            224: "closed",
            225: "open",
            226: "pressure",
            227: "closed",
            228: "locked",
            229: "pressure",
            230: "exit",
        }
        self.assertEqual(expected, {index: generator.door_kind(index) for index in expected})

        atlas = generator.build_tileset()
        self.assertGreater(
            self._count_colors(
                tile(atlas, 228),
                {generator.BRASS_DARK, generator.BRASS_MID, generator.BRASS_LIGHT},
            ),
            0,
        )
        for index in (226, 229):
            with self.subTest(index=index):
                self.assertGreater(
                    self._count_colors(
                        tile(atlas, index),
                        {
                            generator.VERDIGRIS_DARK,
                            generator.VERDIGRIS_MID,
                            generator.VERDIGRIS_LIGHT,
                            generator.COOLANT_GLINT,
                        },
                    ),
                    0,
                )

    def test_raised_sideways_door_slot_is_floor_threshold(self) -> None:
        generator = load_generator()
        self.assertEqual("threshold", generator.door_kind(116))
        atlas = generator.build_tileset()
        threshold = tile(atlas, 116)
        floor_colors = set(tile(atlas, FLOOR).getdata())
        opaque_pixels = [pixel for pixel in threshold.getdata() if pixel[3]]
        outline_count = opaque_pixels.count(generator.OUTLINE)
        floor_color_count = sum(pixel in floor_colors for pixel in opaque_pixels)

        self.assertLessEqual(outline_count, len(opaque_pixels) // 4)
        self.assertGreaterEqual(floor_color_count, len(opaque_pixels) // 2)

    def test_cabinet_bitmask_variants_are_all_distinct(self) -> None:
        atlas = load_generator().build_tileset()
        for start in (92, 108, 176, 180, 184, 188, 200):
            variants = [digest(tile(atlas, index)) for index in range(start, start + 4)]
            with self.subTest(start=start):
                self.assertEqual(4, len(set(variants)))

    def test_internal_cabinet_openings_follow_wall_bitmask_edges(self) -> None:
        atlas = load_generator().build_tileset()
        closed = tile(atlas, 176)
        open_right = tile(atlas, 177)
        right_below_open = tile(atlas, 178)
        open_left = tile(atlas, 184)
        rows = tuple(
            y
            for y in range(1, TILE_SIZE - 1)
            if all(closed.getpixel((x, y))[3] for x in (1, 2, 13, 14))
        )

        def dominant_border(image: Image.Image, xs: tuple[int, int]):
            pairs = [tuple(image.getpixel((x, y)) for x in xs) for y in rows]
            return max(set(pairs), key=pairs.count)

        def border_hits(
            image: Image.Image,
            xs: tuple[int, int],
            signature: tuple[tuple[int, int, int, int], ...],
        ) -> int:
            return sum(
                tuple(image.getpixel((x, y)) for x in xs) == signature
                for y in rows
            )

        left_columns = (1, 2)
        right_columns = (13, 14)
        left_frame = dominant_border(closed, left_columns)
        right_frame = dominant_border(closed, right_columns)
        closed_left_hits = border_hits(closed, left_columns, left_frame)
        closed_right_hits = border_hits(closed, right_columns, right_frame)

        self.assertGreaterEqual(closed_left_hits, len(rows) * 3 // 4)
        self.assertGreaterEqual(closed_right_hits, len(rows) * 3 // 4)
        self.assertEqual(
            closed_left_hits,
            border_hits(right_below_open, left_columns, left_frame),
            "bit 1 opens only the lower-right diagonal, so the left frame must remain",
        )
        self.assertLessEqual(
            border_hits(open_left, left_columns, left_frame),
            closed_left_hits // 3,
            "bit 3 must remove the repeated left cabinet frame",
        )
        self.assertLessEqual(
            border_hits(open_right, right_columns, right_frame),
            closed_right_hits // 3,
            "bit 0 must remove the repeated right cabinet frame",
        )


class GearworksVegetationTest(unittest.TestCase):
    @staticmethod
    def _green_pixels(pixels: list[tuple[int, int, int, int]]) -> int:
        return sum(
            1
            for red, green, blue, _alpha in pixels
            if green >= red + 8 and green >= blue + 4
        )

    @staticmethod
    def _dry_pixels(pixels: list[tuple[int, int, int, int]]) -> int:
        return sum(
            1
            for red, green, blue, _alpha in pixels
            if red >= green >= blue and red >= 55
        )

    def test_low_grass_stays_below_high_grass_silhouettes(self) -> None:
        atlas = load_generator().build_tileset()
        floor = tile(atlas, FLOOR)
        high_grass_heights = (
            mask_height(foreground_mask(tile(atlas, 66), floor)),
            mask_height(foreground_mask(tile(atlas, 69), floor)),
        )
        for low_index, high_height in zip((GRASS, GRASS_ALT), high_grass_heights):
            low_mask = foreground_mask(tile(atlas, low_index), floor)
            with self.subTest(index=low_index):
                self.assertTrue(any(low_mask), "low grass slot is empty")
                self.assertLessEqual(mask_height(low_mask) + 4, high_height)

    def test_shared_plant_geometry_is_deterministic_and_varied(self) -> None:
        generator = load_generator()
        stems = generator.plant_stems(731122)
        self.assertEqual(stems, generator.plant_stems(731122))
        self.assertNotEqual(stems, generator.plant_stems(731122, alt=True))
        self.assertGreaterEqual(len(stems), 7)
        self.assertEqual(len(stems), len({stem.x for stem in stems}))
        for stem in stems:
            with self.subTest(stem=stem):
                self.assertTrue(0 <= stem.x < TILE_SIZE)
                self.assertGreaterEqual(stem.height, TILE_SIZE)
                self.assertIn(stem.bend, (-1, 0, 1))

    def test_live_and_furrowed_grass_share_foreground_shapes(self) -> None:
        atlas = load_generator().build_tileset()
        for live_index, dry_index, background_index in VEGETATION_PAIRS:
            live = tile(atlas, live_index)
            dry = tile(atlas, dry_index)
            if background_index is None:
                live_mask = alpha_mask(live)
                dry_mask = alpha_mask(dry)
            else:
                background = tile(atlas, background_index)
                live_mask = foreground_mask(live, background)
                dry_mask = foreground_mask(dry, background)
            with self.subTest(live=live_index, furrowed=dry_index):
                self.assertTrue(any(live_mask), "vegetation silhouette is empty")
                self.assertTrue(any(dry_mask), "furrowed vegetation silhouette is empty")
                self.assertEqual(mask_height(live_mask), mask_height(dry_mask))
                hamming_distance = sum(
                    live_pixel != dry_pixel
                    for live_pixel, dry_pixel in zip(live_mask, dry_mask)
                )
                self.assertLessEqual(
                    hamming_distance,
                    2,
                    "paired grass differs by more than minor wear",
                )
                self.assertNotEqual(digest(live), digest(dry), "paired grass must be recolored")

    def test_live_and_furrowed_grass_use_distinct_semantic_palettes(self) -> None:
        atlas = load_generator().build_tileset()
        for live_index, dry_index, background_index in VEGETATION_PAIRS:
            background = (
                tile(atlas, background_index) if background_index is not None else None
            )
            live_pixels = foreground_pixels(tile(atlas, live_index), background)
            dry_pixels = foreground_pixels(tile(atlas, dry_index), background)
            with self.subTest(live=live_index, furrowed=dry_index):
                self.assertGreaterEqual(self._green_pixels(live_pixels), 6)
                self.assertGreaterEqual(self._dry_pixels(dry_pixels), 6)
                self.assertLessEqual(self._green_pixels(dry_pixels), 2)

    def test_dry_grass_has_no_living_or_resource_highlight_colors(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        forbidden = {
            generator.MOSS_DARK,
            generator.MOSS_MID,
            generator.MOSS_LIGHT,
            generator.VERDIGRIS_LIGHT,
            generator.COOLANT_GLINT,
        }
        for _live_index, dry_index, background_index in VEGETATION_PAIRS:
            background = (
                tile(atlas, background_index) if background_index is not None else None
            )
            dry_pixels = foreground_pixels(tile(atlas, dry_index), background)
            with self.subTest(index=dry_index):
                self.assertTrue(dry_pixels)
                self.assertFalse(forbidden.intersection(dry_pixels))

    def test_raised_grass_layers_share_continuous_stem_columns(self) -> None:
        atlas = load_generator().build_tileset()
        floor = tile(atlas, FLOOR)
        groups = (
            (122, 234, 250),
            (123, 235, 251),
            (125, 237, 253),
            (126, 238, 254),
        )
        for body_index, overhang_index, underhang_index in groups:
            body = foreground_mask(tile(atlas, body_index), floor)
            overhang = alpha_mask(tile(atlas, overhang_index))
            underhang = alpha_mask(tile(atlas, underhang_index))
            body_top = {x for x in range(TILE_SIZE) if body[x]}
            overhang_bottom = {
                x
                for x in range(TILE_SIZE)
                if overhang[(TILE_SIZE - 1) * TILE_SIZE + x]
            }
            underhang_top = {x for x in range(TILE_SIZE) if underhang[x]}
            with self.subTest(body=body_index):
                self.assertGreaterEqual(len(body_top & overhang_bottom), 4)
                self.assertGreaterEqual(len(body_top & underhang_top), 4)
                self.assertFalse(
                    any(underhang[row * TILE_SIZE + x] for row in range(12, 16) for x in range(16)),
                    "underhang should be foliage, not roots at the tile base",
                )


class GearworksObjectLayeringTest(unittest.TestCase):
    def test_all_raised_bodies_touch_their_overhangs(self) -> None:
        atlas = load_generator().build_tileset()
        for body_index, overhang_index, background_index in RAISED_OVERHANG_PAIRS:
            body_mask = foreground_mask(
                tile(atlas, body_index), tile(atlas, background_index)
            )
            overhang_alpha = tile(atlas, overhang_index).getchannel("A")
            touching_columns = {
                x
                for x in range(TILE_SIZE)
                if body_mask[x]
                and overhang_alpha.getpixel((x, TILE_SIZE - 1)) > 0
            }
            with self.subTest(body=body_index, overhang=overhang_index):
                self.assertGreaterEqual(
                    len(touching_columns),
                    1,
                    "raised body is detached from its overhang",
                )

    def test_core_mechanical_slots_are_non_empty_and_distinct(self) -> None:
        atlas = load_generator().build_tileset()
        variants = []
        for index in MECHANICAL_FLAT_SLOTS:
            subject = tile(atlas, index)
            with self.subTest(index=index):
                self.assertIsNotNone(subject.getchannel("A").getbbox())
            variants.append(digest(subject))
        self.assertEqual(len(MECHANICAL_FLAT_SLOTS), len(set(variants)))

    def test_mechanical_props_keep_horizontal_edges_clear(self) -> None:
        atlas = load_generator().build_tileset()
        entries = [
            (index, FLOOR if index not in (73, 75, 77) else FLOOR_SP)
            for index in MECHANICAL_FLAT_SLOTS
        ]
        entries.extend(
            (body_index, background_index)
            for body_index, _overhang_index, background_index in MECHANICAL_RAISED_PAIRS
        )
        for index, background_index in entries:
            mask = foreground_mask(tile(atlas, index), tile(atlas, background_index))
            with self.subTest(index=index):
                self.assertTrue(any(mask), "mechanical prop is missing")
                self.assertFalse(
                    any(mask[y * TILE_SIZE] or mask[y * TILE_SIZE + 15] for y in range(16)),
                    "mechanical prop reaches a horizontal tile edge",
                )

        for _body_index, overhang_index, _background_index in MECHANICAL_RAISED_PAIRS:
            mask = alpha_mask(tile(atlas, overhang_index))
            with self.subTest(index=overhang_index):
                self.assertTrue(any(mask), "mechanical overhang is missing")
                self.assertFalse(
                    any(mask[y * TILE_SIZE] or mask[y * TILE_SIZE + 15] for y in range(16)),
                    "mechanical overhang reaches a horizontal tile edge",
                )

    def test_raised_machinery_has_compact_ground_contact(self) -> None:
        atlas = load_generator().build_tileset()
        for body_index, _overhang_index, background_index in MECHANICAL_RAISED_PAIRS:
            mask = foreground_mask(tile(atlas, body_index), tile(atlas, background_index))
            bottom = {x for x in range(16) if mask[15 * TILE_SIZE + x]}
            lower_half = {
                (x, y)
                for y in range(8, 16)
                for x in range(16)
                if mask[y * TILE_SIZE + x]
            }
            with self.subTest(index=body_index):
                self.assertTrue(lower_half, "raised machinery has no grounded lower body")
                self.assertGreaterEqual(len(bottom), 2, "raised machinery appears to float")
                self.assertLessEqual(len(bottom), 8, "raised machinery has a radial or platform base")
                self.assertNotIn(0, bottom)
                self.assertNotIn(15, bottom)

    def test_mechanical_palette_is_not_vegetation_palette(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        forbidden = {generator.MOSS_DARK, generator.MOSS_MID, generator.MOSS_LIGHT}
        accents = {
            generator.RUST_DARK,
            generator.RUST_MID,
            generator.RUST_LIGHT,
            generator.BRASS_DARK,
            generator.BRASS_MID,
            generator.BRASS_LIGHT,
            generator.VERDIGRIS_DARK,
            generator.VERDIGRIS_MID,
            generator.VERDIGRIS_LIGHT,
        }
        ember = {
            generator.EMBER_DARK,
            generator.EMBER_MID,
            generator.EMBER_LIGHT,
            generator.EMBER_HOT,
        }
        for index in MECHANICAL_FLAT_SLOTS:
            background_index = FLOOR if index not in (73, 75, 77) else FLOOR_SP
            pixels = foreground_pixels(tile(atlas, index), tile(atlas, background_index))
            with self.subTest(index=index):
                self.assertTrue(pixels)
                self.assertFalse(forbidden.intersection(pixels))
                self.assertTrue(accents.intersection(pixels))
                self.assertLessEqual(sum(pixel in ember for pixel in pixels), 4)

    def test_region_decorations_do_not_join_across_horizontal_slots(self) -> None:
        atlas = load_generator().build_tileset()
        pairs = (
            (74, FLOOR, 75, FLOOR_SP),
            (130, FLOOR, 131, FLOOR_SP),
            (242, None, 243, None),
        )
        for left_index, left_background, right_index, right_background in pairs:
            if left_background is None:
                left_mask = alpha_mask(tile(atlas, left_index))
                right_mask = alpha_mask(tile(atlas, right_index))
            else:
                left_mask = foreground_mask(
                    tile(atlas, left_index), tile(atlas, left_background)
                )
                right_mask = foreground_mask(
                    tile(atlas, right_index), tile(atlas, right_background)
                )
            joined_rows = {
                y
                for y in range(TILE_SIZE)
                if left_mask[y * TILE_SIZE + TILE_SIZE - 1]
                and right_mask[y * TILE_SIZE]
            }
            with self.subTest(left=left_index, right=right_index):
                self.assertFalse(
                    joined_rows,
                    "region decoration forms a multi-slot horizontal structure",
                )


class GearworksWaterTest(unittest.TestCase):
    def test_coolant_texture_is_a_seamless_loop(self) -> None:
        water = load_generator().build_water_texture()
        self.assertEqual(
            [water.getpixel((x, 0)) for x in range(water.width)],
            [water.getpixel((x, water.height - 1)) for x in range(water.width)],
        )
        self.assertEqual(
            [water.getpixel((0, y)) for y in range(water.height)],
            [water.getpixel((water.width - 1, y)) for y in range(water.height)],
        )

    def test_coolant_uses_distinct_teal_verdigris_and_oil_colors(self) -> None:
        water = load_generator().build_water_texture().convert("RGBA")
        pixels = [pixel for pixel in water.getdata() if pixel[3]]
        colors = set(pixels)
        self.assertGreater(len(colors), 1, "coolant texture must not be a flat color")
        deep_teal = sum(is_deep_teal(pixel) for pixel in pixels)
        verdigris = sum(is_verdigris(pixel) for pixel in pixels)
        dark_oil = sum(is_dark_oil(pixel) for pixel in pixels)
        overlapping_colors = {
            pixel
            for pixel in colors
            if sum((is_deep_teal(pixel), is_verdigris(pixel), is_dark_oil(pixel))) > 1
        }
        self.assertFalse(
            overlapping_colors,
            "coolant color categories must be mutually exclusive",
        )
        bright_cyan_or_white = sum(
            1
            for red, green, blue, _alpha in pixels
            if (green >= 155 and blue >= 165) or min(red, green, blue) >= 190
        )
        self.assertGreaterEqual(deep_teal, 64)
        self.assertGreaterEqual(verdigris, 8)
        self.assertGreaterEqual(dark_oil, 2)
        self.assertLessEqual(dark_oil, max(8, len(pixels) // 8))
        self.assertLessEqual(bright_cyan_or_white, 32)

    def test_shoreline_sides_map_to_their_respective_color_families(self) -> None:
        generator = load_generator()
        atlas = generator.build_tileset()
        water = generator.build_water_texture().convert("RGBA")
        floor_palette = {
            pixel for pixel in tile(atlas, FLOOR).getdata() if pixel[3]
        }
        coolant_palette = {pixel for pixel in water.getdata() if pixel[3]}
        for name in ("COOLANT_DARK", "COOLANT_MID", "COOLANT_LIGHT", "COOLANT_GLINT"):
            color = getattr(generator, name, None)
            if isinstance(color, tuple) and len(color) == 4 and color[3]:
                coolant_palette.add(color)
        self.assertTrue(floor_palette)
        self.assertTrue(coolant_palette)

        with Image.open(SEWERS_PATH) as source:
            sewers = source.convert("RGBA")

        source_water_pixels = 0
        source_ground_pixels = 0
        source_nonzero_alphas = set()
        for index in range(WATER, WATER + 16):
            source_tile = tile(sewers, index)
            target_tile = tile(atlas, index)
            for offset, (source_pixel, target_pixel) in enumerate(
                zip(source_tile.getdata(), target_tile.getdata())
            ):
                if source_pixel[3] == 0:
                    continue
                source_alpha = source_pixel[3]
                source_nonzero_alphas.add(source_alpha)
                if source_alpha not in {128, 255}:
                    self.fail(
                        f"unexpected sewer shoreline alpha {source_alpha} "
                        f"at slot {index}, pixel {offset}"
                    )
                floor_distance = min(
                    rgb_distance(target_pixel, color) for color in floor_palette
                )
                coolant_distance = min(
                    rgb_distance(target_pixel, color) for color in coolant_palette
                )
                x = offset % TILE_SIZE
                y = offset // TILE_SIZE
                with self.subTest(index=index, x=x, y=y):
                    if source_alpha == 128:
                        source_water_pixels += 1
                        self.assertTrue(
                            is_deep_teal(target_pixel)
                            or is_verdigris(target_pixel)
                            or is_dark_oil(target_pixel),
                            "source alpha-128 water side is outside the coolant gamut",
                        )
                        self.assertLess(
                            coolant_distance,
                            floor_distance,
                            "source alpha-128 water side is closer to floor than coolant",
                        )
                        self.assertLess(
                            coolant_distance,
                            1800,
                            "source alpha-128 water side is outside the coolant color family",
                        )
                    else:
                        source_ground_pixels += 1
                        self.assertLess(
                            floor_distance,
                            coolant_distance,
                            "source alpha-255 ground side is closer to coolant than floor",
                        )
                        self.assertLess(
                            floor_distance,
                            1800,
                            "source alpha-255 ground side is outside the floor color family",
                        )

        self.assertEqual({128, 255}, source_nonzero_alphas)
        self.assertGreater(source_water_pixels, 0)
        self.assertGreater(source_ground_pixels, 0)


if __name__ == "__main__":
    unittest.main()
