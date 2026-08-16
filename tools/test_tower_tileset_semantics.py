from __future__ import annotations

import math
import unittest
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
TILE_SIZE = 16
SHEET_COLUMNS = 16

THEMES = (
    "chinese_hall",
    "gothic_castle",
    "astral_library",
    "tower_core_gearworks",
    "sky_alchemy_greenhouse",
    "frost_archive",
)


def tile(image: Image.Image, index: int) -> Image.Image:
    left = index % SHEET_COLUMNS * TILE_SIZE
    top = index // SHEET_COLUMNS * TILE_SIZE
    return image.crop((left, top, left + TILE_SIZE, top + TILE_SIZE)).convert("RGBA")


def alpha_mask(image: Image.Image) -> tuple[int, ...]:
    return tuple(image.getchannel("A").getdata())


def foreground_mask(subject: Image.Image, background: Image.Image) -> tuple[bool, ...]:
    return tuple(
        subject.getpixel((x, y)) != background.getpixel((x, y))
        for y in range(TILE_SIZE)
        for x in range(TILE_SIZE)
    )


def color_distance(first: tuple[int, ...], second: tuple[int, ...]) -> float:
    return math.sqrt(sum((first[channel] - second[channel]) ** 2 for channel in range(3)))


def changed_pixels(subject: Image.Image, background: Image.Image) -> list[tuple[int, int, int, int]]:
    return [
        subject.getpixel((x, y))
        for y in range(TILE_SIZE)
        for x in range(TILE_SIZE)
        if subject.getpixel((x, y)) != background.getpixel((x, y))
    ]


def mean_foreground_distance(subject: Image.Image, background: Image.Image) -> float:
    distances = [
        color_distance(subject.getpixel((x, y)), background.getpixel((x, y)))
        for y in range(TILE_SIZE)
        for x in range(TILE_SIZE)
        if subject.getpixel((x, y)) != background.getpixel((x, y))
    ]
    return sum(distances) / max(1, len(distances))


class TowerTilesetSemanticContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.sewers = Image.open(ENV / "tiles_sewers.png").convert("RGBA")
        cls.halls = Image.open(ENV / "tiles_halls.png").convert("RGBA")

    def atlas(self, theme: str) -> Image.Image:
        return Image.open(ENV / f"tiles_{theme}.png").convert("RGBA")

    def test_fixed_wells_and_alchemy_pot_match_the_original(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            for index in (18, 19, 64, 120, 232):
                with self.subTest(theme=theme, index=index):
                    self.assertEqual(tile(self.sewers, index).tobytes(), tile(atlas, index).tobytes())

    def test_water_transitions_preserve_the_original_alpha_contract(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            floor = tile(atlas, 0)
            for index in range(32, 48):
                with self.subTest(theme=theme, index=index):
                    reference = tile(self.sewers, index)
                    subject = tile(atlas, index)
                    self.assertEqual(alpha_mask(reference), alpha_mask(subject))
                    for y in range(TILE_SIZE):
                        for x in range(TILE_SIZE):
                            alpha = reference.getpixel((x, y))[3]
                            if alpha == 0:
                                self.assertEqual((0, 0, 0, 0), subject.getpixel((x, y)))
                            elif alpha == 255:
                                self.assertEqual(floor.getpixel((x, y)), subject.getpixel((x, y)))

    def test_raised_vertical_doors_use_the_halls_geometry(self) -> None:
        reference_floor = tile(self.halls, 0)
        for theme in THEMES:
            atlas = self.atlas(theme)
            floor = tile(atlas, 0)
            for index in range(112, 117):
                with self.subTest(theme=theme, index=index):
                    self.assertEqual(
                        foreground_mask(tile(self.halls, index), reference_floor),
                        foreground_mask(tile(atlas, index), floor),
                    )

    def test_raised_vertical_doors_retain_wall_and_door_materials(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            floor_colors = set(tile(atlas, 0).getdata())
            wall_colors = set(tile(atlas, 80).getdata()) - floor_colors
            for raised, flat in zip(range(112, 116), range(56, 60)):
                colors = set(tile(atlas, raised).getdata()) - floor_colors
                door_colors = set(tile(atlas, flat).getdata()) - floor_colors
                with self.subTest(theme=theme, index=raised):
                    self.assertTrue(colors.intersection(wall_colors))
                    self.assertTrue(colors.intersection(door_colors))

    def test_door_overhangs_use_the_halls_directional_silhouettes(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            for index in range(224, 230):
                with self.subTest(theme=theme, index=index):
                    self.assertEqual(
                        tuple(alpha > 0 for alpha in alpha_mask(tile(self.halls, index))),
                        tuple(alpha > 0 for alpha in alpha_mask(tile(atlas, index))),
                    )

    def test_wall_overhang_families_use_the_halls_directional_silhouettes(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            for index in range(192, 224):
                with self.subTest(theme=theme, index=index):
                    self.assertEqual(
                        tuple(alpha > 0 for alpha in alpha_mask(tile(self.halls, index))),
                        tuple(alpha > 0 for alpha in alpha_mask(tile(atlas, index))),
                    )

    def test_door_wall_overhangs_share_wall_and_door_materials(self) -> None:
        states = (
            (57, None, 208),
            (56, 227, 212),
            (58, 228, 216),
            (59, 229, 220),
        )
        for theme in THEMES:
            atlas = self.atlas(theme)
            for flat_index, top_index, overhang_start in states:
                door_palette = {
                    color[:3]
                    for color in tile(atlas, flat_index).getdata()
                    if color[3]
                }
                source_to_target: dict[tuple[int, int, int], set[tuple[int, int, int]]] = {}
                if top_index is not None:
                    source_top = tile(self.halls, top_index)
                    target_top = tile(atlas, top_index)
                    for source, target in zip(source_top.getdata(), target_top.getdata()):
                        if source[3]:
                            source_to_target.setdefault(source[:3], set()).add(target[:3])
                            self.assertIn(target[:3], door_palette)

                for variant in range(4):
                    source_wall = tile(self.halls, 192 + variant)
                    source_door = tile(self.halls, overhang_start + variant)
                    target_wall = tile(atlas, 192 + variant)
                    target_door = tile(atlas, overhang_start + variant)
                    for source_base, source, target_base, target in zip(
                            source_wall.getdata(),
                            source_door.getdata(),
                            target_wall.getdata(),
                            target_door.getdata()):
                        if source == source_base:
                            self.assertEqual(target_base, target)
                        elif source[3]:
                            source_to_target.setdefault(source[:3], set()).add(target[:3])
                            self.assertIn(target[:3], door_palette)
                        else:
                            self.assertEqual((0, 0, 0, 0), target)

                with self.subTest(theme=theme, overhang=overhang_start):
                    self.assertTrue(source_to_target)
                    self.assertTrue(all(len(colors) == 1 for colors in source_to_target.values()))

    def test_vertical_door_halves_share_the_same_seam_colors(self) -> None:
        pairs = ((112, 224), (114, 224), (115, 226))
        for theme in THEMES:
            atlas = self.atlas(theme)
            for lower_index, upper_index in pairs:
                lower = tile(atlas, lower_index)
                upper = tile(atlas, upper_index)
                with self.subTest(theme=theme, lower=lower_index, upper=upper_index):
                    self.assertEqual(
                        tuple(lower.getpixel((x, 0)) for x in range(2, TILE_SIZE - 2)),
                        tuple(upper.getpixel((x, TILE_SIZE - 1)) for x in range(2, TILE_SIZE - 2)),
                    )

    def test_vertical_crystal_door_uses_crystal_specific_colors(self) -> None:
        reference_floor = tile(self.halls, 0)
        references = [tile(self.halls, index) for index in range(112, 116)]
        door_positions = [
            (x, y)
            for y in range(TILE_SIZE)
            for x in range(TILE_SIZE)
            if references[3].getpixel((x, y)) != reference_floor.getpixel((x, y))
            and not all(
                other.getpixel((x, y)) == references[3].getpixel((x, y))
                for other in references
            )
        ]
        for theme in THEMES:
            atlas = self.atlas(theme)
            floor_colors = set(tile(atlas, 0).getdata())
            crystal_colors = set(tile(atlas, 59).getdata()) - floor_colors
            ordinary_colors = (
                set(tile(atlas, 56).getdata())
                | set(tile(atlas, 57).getdata())
                | set(tile(atlas, 58).getdata())
            ) - floor_colors
            crystal_specific = crystal_colors - ordinary_colors
            lower = tile(atlas, 115)
            used = {lower.getpixel(position) for position in door_positions}
            crystal_used = used.intersection(crystal_specific)
            with self.subTest(theme=theme):
                self.assertTrue(crystal_specific)
                self.assertGreaterEqual(len(used), 3)
                self.assertTrue(crystal_used)
                self.assertTrue(any(
                    color[1] + color[2] >= color[0] * 2 + 12
                    for color in crystal_used
                ))

    def test_sideways_locked_door_uses_lock_specific_material(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            floor_colors = set(tile(atlas, 0).getdata())
            locked_colors = {
                color[:3] for color in tile(atlas, 58).getdata()
                if color[3]
            }
            ordinary_colors = (
                {color[:3] for color in tile(atlas, 56).getdata() if color[3]}
                | {color[:3] for color in tile(atlas, 57).getdata() if color[3]}
                | {color[:3] for color in tile(atlas, 59).getdata() if color[3]}
            ) - {color[:3] for color in floor_colors}
            lock_specific = locked_colors - ordinary_colors
            sideways = tile(atlas, 228)
            used = {color[:3] for color in sideways.getdata() if color[3]}
            with self.subTest(theme=theme):
                self.assertTrue(lock_specific)
                self.assertTrue(used.intersection(lock_specific))
                self.assertTrue(used.issubset(locked_colors))

    def test_vertical_locked_door_keeps_a_door_panel_behind_the_lock(self) -> None:
        reference_floor = tile(self.halls, 0)
        references = [tile(self.halls, index) for index in range(112, 116)]
        panel_positions = [
            (x, y)
            for y in range(TILE_SIZE)
            for x in range(TILE_SIZE)
            if references[2].getpixel((x, y)) != reference_floor.getpixel((x, y))
            and references[2].getpixel((x, y)) == references[0].getpixel((x, y))
            and not all(
                other.getpixel((x, y)) == references[2].getpixel((x, y))
                for other in references
            )
        ]
        lock_positions = [
            (x, y)
            for y in range(TILE_SIZE)
            for x in range(TILE_SIZE)
            if references[2].getpixel((x, y)) != reference_floor.getpixel((x, y))
            and references[2].getpixel((x, y)) != references[0].getpixel((x, y))
            and not all(
                other.getpixel((x, y)) == references[2].getpixel((x, y))
                for other in references
            )
        ]
        for theme in THEMES:
            atlas = self.atlas(theme)
            floor_colors = set(tile(atlas, 0).getdata())
            panel_palette = set(tile(atlas, 56).getdata()) - floor_colors
            lock_palette = (
                set(tile(atlas, 58).getdata())
                - floor_colors
                - panel_palette
                - set(tile(atlas, 59).getdata())
            )
            locked = tile(atlas, 114)
            with self.subTest(theme=theme):
                self.assertTrue({locked.getpixel(position) for position in panel_positions}.intersection(panel_palette))
                self.assertTrue({locked.getpixel(position) for position in lock_positions}.intersection(lock_palette))

    def test_sideways_crystal_door_uses_cold_crystal_material(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            floor_colors = set(tile(atlas, 0).getdata())
            crystal_colors = {
                color[:3] for color in tile(atlas, 59).getdata()
                if color[3]
            }
            ordinary_colors = (
                {color[:3] for color in tile(atlas, 56).getdata() if color[3]}
                | {color[:3] for color in tile(atlas, 57).getdata() if color[3]}
                | {color[:3] for color in tile(atlas, 58).getdata() if color[3]}
            ) - {color[:3] for color in floor_colors}
            crystal_specific = crystal_colors - ordinary_colors
            sideways = tile(atlas, 229)
            used = {color[:3] for color in sideways.getdata() if color[3]}
            with self.subTest(theme=theme):
                self.assertGreaterEqual(len(used), 3)
                self.assertTrue(used.issubset(crystal_colors))
                self.assertTrue(used.intersection(crystal_specific))
                self.assertTrue(any(
                    color[1] + color[2] >= color[0] * 2 + 12
                    for color in used.intersection(crystal_specific)
                ))

    def test_sideways_door_tops_use_exact_flat_door_materials(self) -> None:
        pairs = ((56, 227), (58, 228), (59, 229))
        for theme in THEMES:
            atlas = self.atlas(theme)
            for flat_index, upper_index in pairs:
                flat_colors = {
                    color[:3]
                    for color in tile(atlas, flat_index).getdata()
                    if color[3]
                }
                upper_colors = {
                    color[:3]
                    for color in tile(atlas, upper_index).getdata()
                    if color[3]
                }
                with self.subTest(
                        theme=theme,
                        flat=flat_index,
                        upper=upper_index):
                    self.assertTrue(upper_colors.issubset(flat_colors))

    def test_engine_uses_the_shared_sideways_door_lower_half(self) -> None:
        source = (
            ROOT
            / "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/tiles/DungeonTileSheet.java"
        ).read_text(encoding="utf-8")
        self.assertNotIn("RAISED_DOOR_SIDEWAYS_LOCKED", source)
        self.assertNotIn("RAISED_DOOR_SIDEWAYS_CRYSTAL", source)
        self.assertNotIn("Dungeon.level instanceof TowerLevel", source)
        self.assertIn("if (wallStitcheable(below))", source)
        for theme in THEMES:
            atlas = self.atlas(theme)
            with self.subTest(theme=theme):
                self.assertIsNone(tile(atlas, 117).getchannel("A").getbbox())
                self.assertIsNone(tile(atlas, 118).getchannel("A").getbbox())

    def test_gothic_vegetation_is_layered_moss_and_dry_weeds(self) -> None:
        atlas = self.atlas("gothic_castle")
        floor = tile(atlas, 0)
        live = tile(atlas, 66)
        dry = tile(atlas, 67)
        live_pixels = [
            live.getpixel((x, y))
            for y in range(TILE_SIZE)
            for x in range(TILE_SIZE)
            if live.getpixel((x, y)) != floor.getpixel((x, y))
        ]
        dry_pixels = [
            dry.getpixel((x, y))
            for y in range(TILE_SIZE)
            for x in range(TILE_SIZE)
            if dry.getpixel((x, y)) != floor.getpixel((x, y))
        ]
        self.assertGreaterEqual(sum(g > r and g > b for r, g, b, _a in live_pixels), 20)
        self.assertGreaterEqual(sum(r >= g >= b for r, g, b, _a in dry_pixels), 16)
        for index in (122, 123, 125, 126, 234, 235, 237, 238, 250, 251, 253, 254):
            with self.subTest(index=index):
                self.assertIsNotNone(tile(atlas, index).getchannel("A").getbbox())

    def test_bookshelves_preserve_theme_joinery_and_remain_distinct_from_walls(self) -> None:
        groups = (
            ((50, 48), (54, 52)),
            tuple((92 + offset, 80 + offset) for offset in range(4)),
            tuple((108 + offset, 96 + offset) for offset in range(4)),
            tuple((176 + offset, 144 + offset) for offset in range(16)),
            tuple((200 + offset, 192 + offset) for offset in range(4)),
        )
        for theme in THEMES:
            atlas = self.atlas(theme)
            for group in groups:
                for shelf_index, wall_index in group:
                    with self.subTest(theme=theme, shelf=shelf_index, wall=wall_index):
                        reference_has_shelf = any(foreground_mask(
                            tile(self.halls, shelf_index), tile(self.halls, wall_index)
                        ))
                        subject = tile(atlas, shelf_index)
                        wall = tile(atlas, wall_index)
                        pixels = changed_pixels(subject, wall)
                        if reference_has_shelf:
                            self.assertGreaterEqual(len(pixels), 12)
                            self.assertGreaterEqual(
                                mean_foreground_distance(subject, wall),
                                38,
                            )

    def test_flat_bookshelves_have_frames_shelves_and_book_spines(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            for shelf_index, wall_index in ((50, 48), (54, 52)):
                shelf = tile(atlas, shelf_index)
                wall = tile(atlas, wall_index)
                mask = [
                    [shelf.getpixel((x, y)) != wall.getpixel((x, y)) for x in range(TILE_SIZE)]
                    for y in range(TILE_SIZE)
                ]
                horizontal_runs = sum(sum(row) >= 8 for row in mask)
                vertical_runs = sum(sum(mask[y][x] for y in range(TILE_SIZE)) >= 7 for x in range(TILE_SIZE))
                colors = set(changed_pixels(shelf, wall))
                with self.subTest(theme=theme, shelf=shelf_index):
                    self.assertGreaterEqual(horizontal_runs, 2)
                    self.assertGreaterEqual(vertical_runs, 2)
                    self.assertGreaterEqual(len(colors), 4)

    def test_solid_tower_objects_have_a_readable_silhouette_against_floor(self) -> None:
        flat_object_indices = (65, 72, 73, 74, 75, 76, 77, 78)
        for theme in THEMES:
            atlas = self.atlas(theme)
            floor_candidates = [tile(atlas, index) for index in (0, 4)]
            for index in flat_object_indices:
                subject = tile(atlas, index)
                background = min(
                    floor_candidates,
                    key=lambda candidate: len(changed_pixels(subject, candidate)),
                )
                pixels = changed_pixels(subject, background)
                with self.subTest(theme=theme, index=index):
                    self.assertGreaterEqual(len(pixels), 24)
                    self.assertGreaterEqual(mean_foreground_distance(subject, background), 48)

    def test_greenhouse_and_frost_statues_have_dark_contours_and_bright_cores(self) -> None:
        for theme in ("sky_alchemy_greenhouse", "frost_archive"):
            atlas = self.atlas(theme)
            floor = tile(atlas, 0)
            statue = tile(atlas, 72)
            pixels = changed_pixels(statue, floor)
            luminances = [0.2126 * r + 0.7152 * g + 0.0722 * b for r, g, b, _a in pixels]
            with self.subTest(theme=theme):
                self.assertGreaterEqual(sum(value < 45 for value in luminances), 12)
                self.assertGreaterEqual(sum(value > 155 for value in luminances), 8)

    def test_solid_object_layers_join_across_the_tile_boundary(self) -> None:
        for theme in THEMES:
            atlas = self.atlas(theme)
            for raised_index, overhang_index in zip(range(128, 132), range(240, 244)):
                raised = tile(atlas, raised_index)
                overhang = tile(atlas, overhang_index)
                raised_columns = {x for x in range(TILE_SIZE) if raised.getpixel((x, 0))[3]}
                overhang_columns = {x for x in range(TILE_SIZE) if overhang.getpixel((x, 15))[3]}
                with self.subTest(theme=theme, raised=raised_index, overhang=overhang_index):
                    self.assertTrue(raised_columns.intersection(overhang_columns))

    def test_gothic_low_obstacles_are_grounded_and_have_no_false_overhang(self) -> None:
        atlas = self.atlas("gothic_castle")
        floors = (tile(atlas, 0), tile(atlas, 4))
        for flat_index, raised_index, overhang_index in zip(range(76, 79), range(132, 135), range(244, 247)):
            for index in (flat_index, raised_index):
                subject = tile(atlas, index)
                background = min(floors, key=lambda candidate: len(changed_pixels(subject, candidate)))
                with self.subTest(index=index):
                    self.assertGreaterEqual(len(changed_pixels(subject, background)), 24)
                    self.assertLessEqual(len(changed_pixels(subject, background)), 120)
            with self.subTest(index=overhang_index):
                self.assertIsNone(tile(atlas, overhang_index).getchannel("A").getbbox())

if __name__ == "__main__":
    unittest.main()
