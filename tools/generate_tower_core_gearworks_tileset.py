from __future__ import annotations

import random
from pathlib import Path
from typing import NamedTuple

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
OUT = ENV / "tiles_tower_core_gearworks.png"
WATER_OUT = ENV / "water_tower_core_gearworks.png"

T = 16
COLS = 16
ATLAS_SIZE = T * COLS
SEED = 730241

TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (18, 21, 20, 255)
VOID = (12, 17, 17, 255)

STONE_SHADOW = (38, 42, 39, 255)
STONE_DARK = (52, 57, 53, 255)
STONE_MID = (68, 72, 66, 255)
STONE_LIGHT = (84, 88, 79, 255)
STONE_EDGE = (104, 106, 94, 255)

IRON_DARK = (42, 47, 46, 255)
IRON_MID = (61, 67, 65, 255)
IRON_LIGHT = (86, 91, 86, 255)

RUST_DARK = (72, 39, 28, 255)
RUST_MID = (105, 54, 33, 255)
RUST_LIGHT = (139, 76, 41, 255)

BRASS_DARK = (84, 67, 35, 255)
BRASS_MID = (125, 98, 48, 255)
BRASS_LIGHT = (161, 130, 65, 255)

VERDIGRIS_DARK = (35, 71, 66, 255)
VERDIGRIS_MID = (54, 102, 91, 255)
VERDIGRIS_LIGHT = (78, 132, 113, 255)

EMBER_DARK = (78, 31, 24, 255)
EMBER_MID = (128, 48, 27, 255)
EMBER_LIGHT = (174, 72, 30, 255)
EMBER_HOT = (211, 119, 43, 255)

MOSS_DARK = (35, 62, 40, 255)
MOSS_MID = (52, 86, 49, 255)
MOSS_LIGHT = (78, 111, 61, 255)

DRY_DARK = (65, 53, 39, 255)
DRY_MID = (91, 72, 47, 255)
DRY_LIGHT = (121, 96, 58, 255)

COOLANT_DARK = (24, 58, 62, 255)
COOLANT_MID = (33, 74, 77, 255)
COOLANT_RIPPLE = (38, 82, 82, 255)
COOLANT_LIGHT = (50, 112, 105, 255)
COOLANT_GLINT = (67, 137, 126, 255)
OIL_DARK = (24, 28, 30, 255)

IRON_STAIR_RAMP = (
    OUTLINE,
    RUST_DARK,
    IRON_DARK,
    IRON_MID,
    RUST_MID,
    BRASS_DARK,
    IRON_LIGHT,
)
WOOD_STAIR_RAMP = (
    OUTLINE,
    DRY_DARK,
    RUST_DARK,
    DRY_MID,
    BRASS_DARK,
    DRY_LIGHT,
    BRASS_LIGHT,
)

CAST_STONE_RAMP = (
    OUTLINE,
    STONE_SHADOW,
    STONE_DARK,
    IRON_DARK,
    STONE_MID,
    STONE_LIGHT,
    STONE_EDGE,
)
WOOD_CABINET_RAMP = (
    OUTLINE,
    RUST_DARK,
    DRY_DARK,
    DRY_MID,
    RUST_MID,
    DRY_LIGHT,
    BRASS_LIGHT,
)
IRON_CABINET_RAMP = (
    OUTLINE,
    IRON_DARK,
    STONE_SHADOW,
    IRON_MID,
    RUST_MID,
    IRON_LIGHT,
    BRASS_LIGHT,
)
OPEN_DOOR_RAMP = (
    VOID,
    OUTLINE,
    STONE_SHADOW,
    IRON_DARK,
    STONE_DARK,
    IRON_MID,
    STONE_LIGHT,
)


class PlantStem(NamedTuple):
    x: int
    height: int
    bend: int
    leaf_side: int
    leaf_offset: int


def tile_rect(index: int) -> tuple[int, int, int, int]:
    if not 0 <= index < 256:
        raise ValueError(f"tile index out of range: {index}")
    left = index % COLS * T
    top = index // COLS * T
    return left, top, left + T, top + T


def tile(image: Image.Image, index: int) -> Image.Image:
    return image.crop(tile_rect(index)).convert("RGBA")


def replace_tile(atlas: Image.Image, source: Image.Image, index: int) -> None:
    atlas.paste(source.convert("RGBA"), tile_rect(index))


def paste_tile(
    atlas: Image.Image,
    source: Image.Image,
    source_index: int,
    target_index: int,
) -> None:
    replace_tile(atlas, tile(source, source_index), target_index)


def local_draw(
    background: Image.Image | None = None,
) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = (
        background.copy().convert("RGBA")
        if background is not None
        else Image.new("RGBA", (T, T), TRANSPARENT)
    )
    return image, ImageDraw.Draw(image)


def draw_iron_floor(seed: int, variant: int = 0) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=IRON_MID)

    seam_layouts = (
        ((5, ((0, 4), (7, 12))), (11, ((2, 8), (11, 15)))),
        ((3, ((1, 6), (9, 15))), (9, ((0, 3), (6, 11))), (14, ((8, 14),))),
        ((6, ((0, 2), (5, 10), (13, 15))), (12, ((1, 7), (10, 14)))),
    )
    for seam_order, (y, segments) in enumerate(seam_layouts[variant % 3]):
        for segment_order, (left, right) in enumerate(segments):
            draw.line((left, y, right, y), fill=IRON_DARK)
            if y < 15:
                inset_left = left + ((variant + seam_order + segment_order) % 2)
                inset_right = right - 1
                if inset_left <= inset_right:
                    draw.line(
                        (inset_left, y + 1, inset_right, y + 1),
                        fill=IRON_LIGHT,
                    )

    joint_layouts = (
        ((3, 0, 4), (12, 6, 10), (7, 12, 15)),
        ((8, 0, 2), (4, 4, 8), (13, 10, 13)),
        ((11, 0, 5), (5, 7, 11), (9, 13, 15)),
    )
    for x, top, bottom in joint_layouts[variant % 3]:
        draw.line((x, top, x, bottom), fill=STONE_SHADOW)
        if x < 15 and top < bottom:
            draw.point((x + 1, top + 1), fill=STONE_LIGHT)

    stone_patches = (
        ((1, 1, 4, 3), (10, 7, 14, 9)),
        ((7, 1, 11, 3), (1, 11, 4, 14)),
        ((2, 7, 5, 10), (11, 13, 14, 15)),
    )[variant % 3]
    for left, top, right, bottom in stone_patches:
        draw.rectangle((left, top, right, bottom), fill=STONE_MID)
        draw.line((left, top, right, top), fill=STONE_LIGHT)
        draw.line((right, top + 1, right, bottom), fill=STONE_DARK)

    for mark in range(5):
        x = (rng.randrange(14) + variant * 5 + mark * 3) % 14 + 1
        y = (rng.randrange(14) + variant * 4 + mark * 5) % 14 + 1
        draw.point((x, y), fill=rng.choice((IRON_DARK, RUST_DARK, RUST_MID)))
    return image


def draw_floor_deco(
    background: Image.Image,
    seed: int,
    alt: bool = False,
) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw(background)
    if alt:
        draw.rectangle((9, 2, 14, 7), fill=IRON_DARK, outline=OUTLINE)
        draw.line((10, 3, 13, 3), fill=IRON_LIGHT)
        draw.line((10, 6, 13, 6), fill=RUST_MID)
        draw.point((10, 4), fill=BRASS_DARK)
    else:
        draw.rectangle((1, 8, 7, 13), fill=IRON_DARK, outline=OUTLINE)
        draw.rectangle((2, 9, 6, 12), fill=STONE_DARK)
        draw.line((3, 9, 5, 9), fill=STONE_EDGE)
        draw.point((2, 12), fill=RUST_LIGHT)
    for _ in range(3):
        draw.point(
            (rng.randrange(1, 15), rng.randrange(1, 15)),
            fill=rng.choice((RUST_DARK, RUST_MID, BRASS_DARK)),
        )
    return image


def draw_low_grass(
    background: Image.Image,
    seed: int,
    alt: bool = False,
) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw(background)
    anchors = (1, 4, 8, 12) if not alt else (2, 6, 10, 14)
    for order, x in enumerate(anchors):
        base_y = 14 - (order + int(alt)) % 2
        color = (MOSS_DARK, MOSS_MID, MOSS_LIGHT)[order % 3]
        draw.point((x, base_y), fill=MOSS_DARK)
        draw.point((x, base_y - 1), fill=color)
        if x < 15:
            draw.point((x + 1, base_y), fill=MOSS_MID)
        if order % 2 and base_y < 15:
            draw.point((x, base_y + 1), fill=MOSS_LIGHT)
    for _ in range(5):
        draw.point(
            (rng.randrange(1, 15), rng.randrange(12, 16)),
            fill=rng.choice((MOSS_DARK, MOSS_MID, MOSS_LIGHT)),
        )
    return image


def plant_stems(seed: int, alt: bool = False) -> tuple[PlantStem, ...]:
    """Return shared deterministic geometry for living and dead tall grass."""
    rng = random.Random(seed + (1907 if alt else 0))
    anchors = (1, 3, 5, 7, 9, 11, 13, 15) if not alt else (0, 2, 4, 6, 8, 10, 12, 14)
    stems = []
    for order, x in enumerate(anchors):
        bend = rng.choice((-1, 0, 1))
        if x == 0:
            bend = max(0, bend)
        elif x == T - 1:
            bend = min(0, bend)
        stems.append(
            PlantStem(
                x=x,
                height=rng.randrange(18, 25),
                bend=bend,
                leaf_side=-1 if (rng.randrange(2) + order) % 2 else 1,
                leaf_offset=rng.randrange(5, 10),
            )
        )
    return tuple(stems)


def _plant_palette(
    dry: bool,
) -> tuple[tuple[int, int, int, int], ...]:
    return (DRY_DARK, DRY_MID, DRY_LIGHT) if dry else (MOSS_DARK, MOSS_MID, MOSS_LIGHT)


def _paint_plant_stem(
    draw: ImageDraw.ImageDraw,
    stem: PlantStem,
    top: int,
    bottom: int,
    order: int,
    dry: bool,
    alt: bool,
) -> None:
    dark, mid, light = _plant_palette(dry)
    x = stem.x
    tip_x = max(0, min(T - 1, x + stem.bend))
    draw.line((x, bottom, x, top + 2), fill=dark if order % 3 == 0 else mid)
    draw.line((x, top + 2, tip_x, top), fill=light)

    leaf_y = max(top + 3, bottom - stem.leaf_offset)
    leaf_x = max(0, min(T - 1, x + stem.leaf_side))
    draw.line((x, leaf_y + 1, leaf_x, leaf_y), fill=mid)
    draw.point((leaf_x, leaf_y), fill=light)

    upper_leaf_y = min(bottom - 2, top + 5 + (order + int(alt)) % 3)
    upper_side = -stem.leaf_side
    upper_leaf_x = max(0, min(T - 1, x + upper_side))
    draw.line((x, upper_leaf_y + 1, upper_leaf_x, upper_leaf_y), fill=dark)
    draw.point((upper_leaf_x, upper_leaf_y), fill=mid)

    if not dry and order == (5 if alt else 2):
        draw.point((tip_x, top), fill=VERDIGRIS_LIGHT)


def draw_flat_tall_grass(
    background: Image.Image,
    seed: int,
    dry: bool = False,
    alt: bool = False,
) -> Image.Image:
    image, draw = local_draw(background)
    for order, stem in enumerate(plant_stems(seed, alt)):
        height = 11 + (stem.height + order) % 5
        _paint_plant_stem(
            draw,
            stem,
            top=max(0, 15 - height),
            bottom=15,
            order=order,
            dry=dry,
            alt=alt,
        )
    return image


def _draw_tall_plant_column(
    seed: int,
    dry: bool = False,
    alt: bool = False,
) -> Image.Image:
    image = Image.new("RGBA", (T, T * 2), TRANSPARENT)
    draw = ImageDraw.Draw(image)
    bottom = 30
    for order, stem in enumerate(plant_stems(seed, alt)):
        _paint_plant_stem(
            draw,
            stem,
            top=max(1, bottom - stem.height),
            bottom=bottom,
            order=order,
            dry=dry,
            alt=alt,
        )
    return image


def draw_raised_tall_grass(
    background: Image.Image,
    seed: int,
    dry: bool = False,
    alt: bool = False,
) -> Image.Image:
    lower = _draw_tall_plant_column(seed, dry=dry, alt=alt).crop((0, T, T, T * 2))
    return Image.alpha_composite(background.convert("RGBA"), lower)


def draw_tall_grass_overhang(
    seed: int,
    dry: bool = False,
    alt: bool = False,
) -> Image.Image:
    return _draw_tall_plant_column(seed, dry=dry, alt=alt).crop((0, 0, T, T))


def draw_tall_grass_underhang(
    seed: int,
    dry: bool = False,
    alt: bool = False,
) -> Image.Image:
    image = _draw_tall_plant_column(seed, dry=dry, alt=alt).crop((0, T, T, T * 2))
    ImageDraw.Draw(image).rectangle((0, 12, T - 1, T - 1), fill=TRANSPARENT)
    return image


def draw_embers(background: Image.Image, seed: int, alt: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw(background)
    clusters = (
        ((2, 11), (5, 13), (9, 10), (13, 13)),
        ((3, 13), (7, 11), (11, 13), (14, 9)),
    )[int(alt)]
    for order, (x, y) in enumerate(clusters):
        draw.point((x, y), fill=EMBER_DARK)
        if x < 15:
            draw.point((x + 1, y), fill=EMBER_MID)
        if order == 1 and not alt:
            draw.point((x, y - 1), fill=EMBER_HOT)
        elif order % 2:
            draw.point((x, y - 1), fill=EMBER_LIGHT)
    for _ in range(5):
        draw.point(
            (rng.randrange(1, 15), rng.randrange(9, 15)),
            fill=rng.choice((OUTLINE, EMBER_DARK, RUST_DARK)),
        )
    return image


def draw_service_floor(seed: int, alt: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=DRY_MID)
    courses = (3, 8, 13) if not alt else (4, 9, 14)
    for y in courses:
        draw.line((0, y, 15, y), fill=DRY_DARK)
        if y < 15:
            draw.line((0, y + 1, 15, y + 1), fill=DRY_LIGHT)
    band_x = 5 if not alt else 10
    draw.rectangle((band_x, 0, band_x + 1, 15), fill=IRON_DARK)
    draw.line((band_x + 1, 0, band_x + 1, 15), fill=IRON_LIGHT)
    for y in (2, 7, 12):
        draw.point((band_x, y), fill=BRASS_DARK)
    for _ in range(5):
        draw.point(
            (rng.randrange(1, 15), rng.randrange(1, 15)),
            fill=rng.choice((DRY_DARK, RUST_DARK, IRON_DARK)),
        )
    return image


def recolor_structure(
    source: Image.Image,
    ramp: tuple[tuple[int, int, int, int], ...],
) -> Image.Image:
    source = source.convert("RGBA")
    result = Image.new("RGBA", source.size, TRANSPARENT)
    for y in range(source.height):
        for x in range(source.width):
            red, green, blue, alpha = source.getpixel((x, y))
            if alpha == 0:
                continue
            value = (red * 3 + green * 5 + blue * 2) // 10
            level = min(len(ramp) - 1, value * len(ramp) // 256)
            rr, gg, bb, _ = ramp[level]
            result.putpixel((x, y), (rr, gg, bb, alpha))
    return result


def _closest_distinct_ramp_color(
    source_pixel: tuple[int, int, int, int],
    background_pixel: tuple[int, int, int, int],
    ramp: tuple[tuple[int, int, int, int], ...],
) -> tuple[int, int, int, int]:
    red, green, blue, alpha = source_pixel
    source_value = (red * 3 + green * 5 + blue * 2) // 10
    source_level = min(len(ramp) - 1, source_value * len(ramp) // 256)
    candidates = []
    for level, color in enumerate(ramp):
        candidate = color[:3] + (alpha,)
        if candidate[:3] == background_pixel[:3]:
            continue
        candidate_value = (color[0] * 3 + color[1] * 5 + color[2] * 2) // 10
        candidates.append(
            (abs(candidate_value - source_value), abs(level - source_level), candidate)
        )
    if not candidates:
        raise ValueError("stair ramp has no color distinct from its background")
    return min(candidates, key=lambda item: item[:2])[2]


def draw_stair(
    sewers: Image.Image,
    source_index: int,
    source_background_index: int,
    background: Image.Image,
    ramp: tuple[tuple[int, int, int, int], ...],
) -> Image.Image:
    source = tile(sewers, source_index)
    source_background = tile(sewers, source_background_index)
    recolored = recolor_structure(source, ramp)
    image = background.copy().convert("RGBA")
    for y in range(T):
        for x in range(T):
            source_pixel = source.getpixel((x, y))
            if source_pixel == source_background.getpixel((x, y)):
                continue
            pixel = recolored.getpixel((x, y))
            background_pixel = image.getpixel((x, y))
            if pixel[:3] == background_pixel[:3]:
                pixel = _closest_distinct_ramp_color(
                    source_pixel,
                    background_pixel,
                    ramp,
                )
            image.putpixel((x, y), pixel)
    return image


def draw_bearing_pedestal(background: Image.Image) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((4, 10, 11, 14), fill=IRON_DARK, outline=OUTLINE)
    draw.line((5, 10, 10, 10), fill=IRON_LIGHT)
    draw.rectangle((6, 7, 9, 11), fill=RUST_DARK)
    draw.rectangle((7, 7, 8, 10), fill=BRASS_MID)
    draw.point((7, 8), fill=OUTLINE)
    draw.point((8, 8), fill=VERDIGRIS_MID)
    return image


def draw_chasm(seed: int, edge_kind: str = "plain") -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=VOID)

    beam_x = 3 + seed % 8
    draw.line((beam_x, 2, beam_x, 15), fill=OUTLINE)
    draw.line((beam_x + 1, 3, beam_x + 1, 15), fill=IRON_DARK)
    rod_y = 7 + seed % 5
    draw.line((0, rod_y, 15, rod_y), fill=OUTLINE)
    for x in range((seed % 4) + 1, 16, 6):
        draw.point((x, rod_y), fill=BRASS_DARK)
    for _ in range(5):
        draw.point(
            (rng.randrange(16), rng.randrange(3, 16)),
            fill=rng.choice((OUTLINE, IRON_DARK, VERDIGRIS_DARK)),
        )

    if edge_kind == "iron":
        draw.line((0, 0, 15, 0), fill=IRON_LIGHT)
        draw.line((0, 1, 15, 1), fill=RUST_DARK)
    elif edge_kind == "stone":
        draw.rectangle((0, 0, 15, 2), fill=STONE_DARK)
        draw.line((0, 0, 15, 0), fill=STONE_EDGE)
    elif edge_kind == "service":
        draw.line((0, 0, 15, 0), fill=DRY_LIGHT)
        draw.line((0, 1, 15, 1), fill=DRY_DARK)
        for x in (3, 11):
            draw.point((x, 0), fill=BRASS_DARK)
    elif edge_kind == "coolant":
        draw.line((0, 0, 15, 0), fill=COOLANT_GLINT)
        draw.line((0, 1, 15, 1), fill=COOLANT_DARK)
        draw.point((5, 2), fill=VERDIGRIS_DARK)
    return image


def draw_pressure_console(
    background: Image.Image | None,
    raised: bool = False,
    overhang: bool = False,
) -> Image.Image:
    image, draw = local_draw(None if overhang else background)
    if overhang:
        draw.rectangle((5, 8, 10, 15), fill=IRON_DARK, outline=OUTLINE)
        draw.line((6, 8, 9, 8), fill=IRON_LIGHT)
        draw.rectangle((6, 10, 9, 13), fill=VERDIGRIS_DARK)
        draw.point((7, 11), fill=VERDIGRIS_LIGHT)
        draw.line((5, 15, 10, 15), fill=RUST_DARK)
        return image

    if raised:
        draw.rectangle((5, 0, 10, 12), fill=IRON_MID, outline=OUTLINE)
        draw.line((6, 0, 9, 0), fill=RUST_DARK)
        draw.rectangle((6, 2, 9, 6), fill=STONE_SHADOW)
        draw.line((6, 2, 9, 2), fill=IRON_LIGHT)
        draw.point((7, 4), fill=BRASS_LIGHT)
        draw.point((9, 5), fill=EMBER_MID)
        draw.line((4, 5, 4, 12), fill=RUST_MID)
        draw.point((4, 5), fill=BRASS_MID)
        draw.rectangle((5, 13, 6, 15), fill=IRON_DARK)
        draw.rectangle((9, 13, 10, 15), fill=IRON_DARK)
        return image

    draw.rectangle((4, 5, 11, 13), fill=IRON_MID, outline=OUTLINE)
    draw.line((5, 5, 10, 5), fill=IRON_LIGHT)
    draw.rectangle((5, 7, 10, 10), fill=STONE_SHADOW)
    draw.point((6, 8), fill=BRASS_LIGHT)
    draw.point((9, 9), fill=EMBER_MID)
    draw.line((4, 7, 3, 7), fill=RUST_MID)
    draw.line((3, 7, 3, 12), fill=RUST_DARK)
    draw.rectangle((5, 14, 6, 15), fill=IRON_DARK)
    draw.rectangle((9, 14, 10, 15), fill=IRON_DARK)
    return image


def draw_collapsed_rack(
    background: Image.Image | None,
    raised: bool = False,
    overhang: bool = False,
) -> Image.Image:
    image, draw = local_draw(None if overhang else background)
    if overhang:
        draw.line((4, 5, 4, 15), fill=IRON_DARK)
        draw.line((11, 7, 11, 15), fill=RUST_DARK)
        draw.line((4, 8, 11, 10), fill=IRON_MID)
        draw.line((5, 7, 10, 9), fill=IRON_LIGHT)
        draw.rectangle((5, 11, 7, 13), fill=BRASS_DARK)
        draw.rectangle((9, 12, 11, 14), fill=VERDIGRIS_DARK)
        draw.point((10, 12), fill=VERDIGRIS_LIGHT)
        return image

    if raised:
        draw.line((4, 0, 3, 15), fill=IRON_DARK)
        draw.line((11, 0, 10, 15), fill=RUST_DARK)
        draw.line((4, 3, 11, 5), fill=IRON_MID)
        draw.line((3, 9, 10, 11), fill=IRON_LIGHT)
        draw.rectangle((5, 6, 8, 9), fill=RUST_MID, outline=OUTLINE)
        draw.rectangle((3, 14, 4, 15), fill=IRON_DARK)
        draw.rectangle((10, 14, 11, 15), fill=IRON_DARK)
        return image

    draw.line((3, 4, 4, 14), fill=IRON_DARK)
    draw.line((12, 6, 10, 14), fill=RUST_DARK)
    draw.line((4, 7, 11, 9), fill=IRON_LIGHT)
    draw.line((4, 11, 10, 12), fill=IRON_MID)
    draw.rectangle((5, 8, 7, 10), fill=BRASS_DARK)
    draw.rectangle((8, 11, 11, 13), fill=RUST_MID, outline=OUTLINE)
    draw.point((9, 11), fill=VERDIGRIS_MID)
    draw.rectangle((3, 14, 4, 15), fill=IRON_DARK)
    draw.rectangle((9, 14, 10, 15), fill=IRON_DARK)
    return image


def draw_automaton(
    background: Image.Image | None,
    service_floor: bool = False,
    body_only: bool = False,
    overhang: bool = False,
) -> Image.Image:
    image, draw = local_draw(None if overhang else background)
    offset = 1 if service_floor else 0
    if overhang:
        draw.rectangle((5 + offset, 8, 10 + offset, 13), fill=IRON_MID, outline=OUTLINE)
        draw.line((6 + offset, 8, 9 + offset, 8), fill=IRON_LIGHT)
        draw.point((6 + offset, 10), fill=VERDIGRIS_LIGHT)
        draw.point((9 + offset, 10), fill=BRASS_LIGHT)
        draw.rectangle((6 + offset, 14, 9 + offset, 15), fill=RUST_DARK)
        return image

    if body_only:
        draw.rectangle((6 + offset, 0, 9 + offset, 8), fill=IRON_MID, outline=OUTLINE)
        draw.line((7 + offset, 0, 8 + offset, 0), fill=RUST_DARK)
        draw.rectangle((4 + offset, 2, 5 + offset, 9), fill=RUST_MID)
        if service_floor:
            draw.line((10 + offset, 2, 12 + offset, 6), fill=BRASS_DARK)
            draw.point((12 + offset, 6), fill=VERDIGRIS_MID)
        else:
            draw.rectangle((10 + offset, 2, 11 + offset, 9), fill=IRON_DARK)
        draw.line((6 + offset, 9, 5 + offset, 15), fill=IRON_DARK)
        draw.line((9 + offset, 9, 10 + offset, 15), fill=IRON_DARK)
        draw.line((4 + offset, 15, 6 + offset, 15), fill=RUST_DARK)
        draw.line((9 + offset, 15, 11 + offset, 15), fill=RUST_DARK)
        draw.point((8 + offset, 4), fill=EMBER_DARK)
        return image

    draw.rectangle((5 + offset, 2, 10 + offset, 6), fill=IRON_MID, outline=OUTLINE)
    draw.point((6 + offset, 4), fill=VERDIGRIS_LIGHT)
    draw.point((9 + offset, 4), fill=BRASS_LIGHT)
    draw.rectangle((6 + offset, 7, 9 + offset, 11), fill=RUST_MID, outline=OUTLINE)
    draw.line((4 + offset, 8, 5 + offset, 12), fill=IRON_DARK)
    draw.line((10 + offset, 8, 11 + offset, 12), fill=IRON_DARK)
    draw.line((6 + offset, 12, 5 + offset, 15), fill=IRON_DARK)
    draw.line((9 + offset, 12, 10 + offset, 15), fill=IRON_DARK)
    draw.point((8 + offset, 9), fill=EMBER_DARK)
    return image


def draw_flywheel(
    background: Image.Image | None,
    body_only: bool = False,
    overhang: bool = False,
) -> Image.Image:
    image, draw = local_draw(None if overhang else background)
    if overhang:
        draw.ellipse((3, 5, 12, 14), fill=IRON_DARK, outline=OUTLINE)
        draw.ellipse((5, 7, 10, 12), fill=TRANSPARENT, outline=RUST_MID)
        draw.line((7, 7, 8, 14), fill=BRASS_DARK)
        draw.line((5, 10, 10, 10), fill=IRON_LIGHT)
        draw.line((5, 15, 10, 15), fill=RUST_DARK)
        return image

    if body_only:
        draw.ellipse((3, -6, 12, 4), fill=IRON_DARK, outline=OUTLINE)
        draw.line((5, 0, 10, 0), fill=RUST_DARK)
        draw.rectangle((7, 1, 8, 10), fill=BRASS_DARK)
        draw.rectangle((4, 10, 11, 12), fill=IRON_MID, outline=OUTLINE)
        draw.rectangle((4, 13, 5, 15), fill=IRON_DARK)
        draw.rectangle((10, 13, 11, 15), fill=IRON_DARK)
        draw.point((8, 2), fill=VERDIGRIS_MID)
        return image

    draw.ellipse((3, 2, 12, 11), fill=IRON_DARK, outline=OUTLINE)
    draw.ellipse((5, 4, 10, 9), fill=STONE_SHADOW, outline=RUST_MID)
    draw.line((7, 3, 8, 10), fill=BRASS_DARK)
    draw.line((4, 6, 11, 6), fill=IRON_LIGHT)
    draw.rectangle((4, 12, 11, 13), fill=IRON_MID, outline=OUTLINE)
    draw.rectangle((4, 14, 5, 15), fill=IRON_DARK)
    draw.rectangle((10, 14, 11, 15), fill=IRON_DARK)
    draw.point((9, 5), fill=VERDIGRIS_MID)
    return image


def draw_gearbox(
    background: Image.Image | None,
    body_only: bool = False,
    overhang: bool = False,
) -> Image.Image:
    image, draw = local_draw(None if overhang else background)
    if overhang:
        draw.rectangle((4, 7, 11, 15), fill=IRON_MID, outline=OUTLINE)
        draw.line((5, 7, 10, 7), fill=IRON_LIGHT)
        draw.ellipse((5, 9, 9, 13), fill=RUST_DARK, outline=BRASS_DARK)
        draw.point((7, 11), fill=OUTLINE)
        draw.rectangle((10, 10, 11, 13), fill=VERDIGRIS_DARK)
        draw.line((5, 15, 10, 15), fill=RUST_DARK)
        return image

    if body_only:
        draw.rectangle((4, 0, 11, 11), fill=IRON_MID, outline=OUTLINE)
        draw.line((5, 0, 10, 0), fill=RUST_DARK)
        draw.ellipse((5, 2, 9, 6), fill=RUST_DARK, outline=BRASS_MID)
        draw.point((7, 4), fill=OUTLINE)
        draw.rectangle((9, 5, 12, 7), fill=IRON_DARK)
        draw.line((12, 6, 13, 6), fill=BRASS_DARK)
        draw.rectangle((5, 12, 6, 15), fill=IRON_DARK)
        draw.rectangle((10, 12, 11, 15), fill=IRON_DARK)
        draw.point((10, 9), fill=VERDIGRIS_MID)
        return image

    draw.rectangle((3, 5, 12, 12), fill=IRON_MID, outline=OUTLINE)
    draw.line((4, 5, 11, 5), fill=IRON_LIGHT)
    draw.ellipse((4, 7, 8, 11), fill=RUST_DARK, outline=BRASS_MID)
    draw.point((6, 9), fill=OUTLINE)
    draw.rectangle((9, 7, 11, 10), fill=STONE_SHADOW)
    draw.point((10, 8), fill=VERDIGRIS_MID)
    draw.line((12, 8, 14, 8), fill=BRASS_DARK)
    draw.rectangle((4, 13, 5, 15), fill=IRON_DARK)
    draw.rectangle((10, 13, 11, 15), fill=IRON_DARK)
    return image


def draw_metal_stock(
    background: Image.Image | None,
    seed: int,
    raised: bool = False,
    overhang: bool = False,
) -> Image.Image:
    variant = seed % 3
    image, draw = local_draw(None if overhang else background)
    if overhang:
        if variant == 0:
            draw.rectangle((5, 8, 10, 15), fill=IRON_DARK, outline=OUTLINE)
            for y, color in ((9, IRON_LIGHT), (11, RUST_MID), (13, BRASS_DARK)):
                draw.line((6, y, 9, y), fill=color)
        elif variant == 1:
            draw.ellipse((4, 7, 11, 14), fill=RUST_DARK, outline=OUTLINE)
            draw.ellipse((6, 9, 9, 12), fill=STONE_SHADOW, outline=BRASS_DARK)
            draw.line((6, 15, 9, 15), fill=RUST_DARK)
            draw.point((10, 9), fill=VERDIGRIS_DARK)
        else:
            draw.polygon(((4, 15), (6, 8), (11, 10), (10, 15)), fill=IRON_DARK)
            draw.line((6, 9, 10, 11), fill=IRON_LIGHT)
            draw.line((5, 13, 10, 14), fill=RUST_MID)
            draw.line((5, 15, 10, 15), fill=BRASS_DARK)
        return image

    if raised:
        if variant == 0:
            draw.rectangle((5, 0, 10, 11), fill=IRON_DARK, outline=OUTLINE)
            for y, color in ((2, IRON_LIGHT), (5, RUST_MID), (8, BRASS_DARK)):
                draw.line((6, y, 9, y), fill=color)
            draw.rectangle((5, 12, 6, 15), fill=IRON_DARK)
            draw.rectangle((9, 12, 10, 15), fill=IRON_DARK)
        elif variant == 1:
            draw.ellipse((4, -4, 11, 4), fill=RUST_DARK, outline=OUTLINE)
            draw.line((6, 0, 9, 0), fill=RUST_DARK)
            draw.ellipse((5, 3, 10, 8), fill=IRON_MID, outline=BRASS_DARK)
            draw.rectangle((4, 9, 11, 12), fill=IRON_DARK, outline=OUTLINE)
            draw.rectangle((5, 13, 6, 15), fill=IRON_DARK)
            draw.rectangle((9, 13, 10, 15), fill=IRON_DARK)
            draw.point((9, 5), fill=VERDIGRIS_DARK)
        else:
            draw.polygon(((5, 0), (10, 0), (11, 10), (4, 10)), fill=IRON_DARK)
            draw.line((6, 0, 9, 0), fill=BRASS_DARK)
            draw.line((5, 4, 10, 4), fill=IRON_LIGHT)
            draw.line((5, 8, 10, 8), fill=RUST_MID)
            draw.rectangle((5, 11, 6, 15), fill=IRON_DARK)
            draw.rectangle((9, 11, 10, 15), fill=IRON_DARK)
        return image

    if variant == 0:
        draw.rectangle((4, 6, 11, 13), fill=IRON_DARK, outline=OUTLINE)
        for y, color in ((7, IRON_LIGHT), (9, RUST_MID), (11, BRASS_DARK)):
            draw.line((5, y, 10, y), fill=color)
    elif variant == 1:
        draw.ellipse((3, 5, 10, 12), fill=RUST_DARK, outline=OUTLINE)
        draw.ellipse((5, 7, 8, 10), fill=STONE_SHADOW, outline=BRASS_DARK)
        draw.ellipse((8, 9, 13, 14), fill=IRON_MID, outline=OUTLINE)
        draw.point((11, 11), fill=VERDIGRIS_DARK)
    else:
        draw.polygon(((3, 13), (5, 5), (11, 7), (13, 13)), fill=IRON_DARK)
        draw.line((5, 6, 11, 8), fill=IRON_LIGHT)
        draw.line((4, 10, 12, 11), fill=RUST_MID)
        draw.line((4, 13, 11, 13), fill=BRASS_DARK)
    draw.rectangle((5, 14, 6, 15), fill=IRON_DARK)
    draw.rectangle((9, 14, 10, 15), fill=IRON_DARK)
    return image


def build_water_texture() -> Image.Image:
    period = 31
    rng = random.Random(SEED + 900)
    core = Image.new("RGBA", (period, period), COOLANT_MID)

    def torus_distance(
        left: tuple[int, int],
        right: tuple[int, int],
    ) -> int:
        dx = min(abs(left[0] - right[0]), period - abs(left[0] - right[0]))
        dy = min(abs(left[1] - right[1]), period - abs(left[1] - right[1]))
        return max(dx, dy)

    def scattered_positions(count: int, spacing: int) -> tuple[tuple[int, int], ...]:
        candidates = [(x, y) for y in range(period) for x in range(period)]
        rng.shuffle(candidates)
        positions: list[tuple[int, int]] = []
        for candidate in candidates:
            if all(torus_distance(candidate, placed) >= spacing for placed in positions):
                positions.append(candidate)
                if len(positions) == count:
                    return tuple(positions)
        raise RuntimeError(f"cannot place {count} coolant marks with spacing {spacing}")

    profiles = ((0, 0, 0), (0, 0, 1), (1, 0, 0), (0, -1, 0))
    for wave, (left, y) in enumerate(scattered_positions(28, 3)):
        length = rng.choice((1, 2, 2, 3))
        profile = profiles[rng.randrange(len(profiles))]
        color = COOLANT_DARK if wave % 7 == 0 else COOLANT_RIPPLE
        for offset in range(length):
            core.putpixel(
                ((left + offset) % period, (y + profile[offset]) % period),
                color,
            )

    glint_positions = scattered_positions(13, 5)
    for glint, (left, y) in enumerate(glint_positions):
        length = 1 if glint % 3 == 0 else 2
        color = COOLANT_GLINT if glint % 4 == 0 else COOLANT_LIGHT
        for offset in range(length):
            core.putpixel(((left + offset) % period, y), color)

    oil_positions = [
        position
        for position in scattered_positions(16, 5)
        if all(torus_distance(position, glint) >= 3 for glint in glint_positions)
    ][:10]
    if len(oil_positions) < 10:
        raise RuntimeError("cannot distribute coolant oil marks")
    for x, y in oil_positions:
        core.putpixel((x, y), OIL_DARK)

    image = Image.new("RGBA", (32, 32), COOLANT_MID)
    image.paste(core, (0, 0))
    for x in range(32):
        image.putpixel((x, 31), core.getpixel((x % period, 0)))
    for y in range(32):
        image.putpixel((31, y), core.getpixel((0, y % period)))
    return image


def draw_water_transition(
    reference: Image.Image,
    index: int,
    floor: Image.Image,
    water: Image.Image,
) -> Image.Image:
    source = tile(reference, index)
    result = Image.new("RGBA", (T, T), TRANSPARENT)
    for y in range(T):
        for x in range(T):
            alpha = source.getpixel((x, y))[3]
            if alpha == 0:
                continue
            if alpha == 128:
                red, green, blue, _ = water.getpixel((x, y))
            elif alpha == 255:
                red, green, blue, _ = floor.getpixel((x, y))
            else:
                raise ValueError(
                    f"unexpected shoreline alpha {alpha} at slot {index}, {(x, y)}"
                )
            result.putpixel((x, y), (red, green, blue, alpha))
    return result


def paint_masked_pixels(
    image: Image.Image,
    mask_source: Image.Image,
    coordinates: tuple[tuple[int, int], ...] | list[tuple[int, int]],
    color: tuple[int, int, int, int],
) -> None:
    """Paint details without changing a reference tile's alpha topology."""
    for x, y in coordinates:
        if not (0 <= x < T and 0 <= y < T):
            continue
        alpha = mask_source.getpixel((x, y))[3]
        if alpha:
            image.putpixel((x, y), color[:3] + (alpha,))


def masked_line(
    image: Image.Image,
    mask_source: Image.Image,
    start: tuple[int, int],
    end: tuple[int, int],
    color: tuple[int, int, int, int],
) -> None:
    x0, y0 = start
    x1, y1 = end
    if x0 == x1:
        coordinates = [(x0, y) for y in range(min(y0, y1), max(y0, y1) + 1)]
    elif y0 == y1:
        coordinates = [(x, y0) for x in range(min(x0, x1), max(x0, x1) + 1)]
    else:
        raise ValueError("masked_line only supports horizontal or vertical lines")
    paint_masked_pixels(image, mask_source, coordinates, color)


def draw_cast_wall(
    reference: Image.Image,
    index: int,
    variant: int = 0,
    overhang: bool = False,
) -> Image.Image:
    source = tile(reference, index)
    image = recolor_structure(source, CAST_STONE_RAMP)

    if index in (48, 52) and not overhang:
        wall_layouts = {
            48: (
                ((4, ((0, 5), (8, 13))), (11, ((2, 8), (12, 15)))),
                3,
                ((1, 8), (10, 2), (14, 13)),
            ),
            52: (
                ((6, ((1, 7), (10, 15))), (13, ((0, 3), (6, 12)))),
                11,
                ((2, 3), (7, 10), (13, 5)),
            ),
        }
        seam_specs, brace_x, accents = wall_layouts[index]
        for y, segments in seam_specs:
            for left, right in segments:
                masked_line(image, source, (left, y), (right, y), STONE_SHADOW)
                if y < 15 and left + 1 <= right - 1:
                    masked_line(
                        image,
                        source,
                        (left + 1, y + 1),
                        (right - 1, y + 1),
                        STONE_DARK,
                    )
        brace_segments = ((0, 7), (9, 15)) if index == 48 else ((1, 5), (8, 14))
        for top, bottom in brace_segments:
            masked_line(image, source, (brace_x, top), (brace_x, bottom), IRON_DARK)
            if brace_x < 15:
                masked_line(image, source, (brace_x + 1, top), (brace_x + 1, bottom), IRON_MID)
    else:
        seam_rows = ((5, 11), (4, 10), (6, 12))[variant % 3]
        for row, y in enumerate(seam_rows):
            masked_line(image, source, (0, y), (15, y), STONE_SHADOW)
            if y < 15:
                offset = (variant * 5 + row * 7) % 12 + 2
                masked_line(image, source, (offset, y + 1), (offset, min(15, y + 3)), STONE_DARK)

        brace_x = (2, 12, 7, 4, 10)[variant % 5]
        if variant % 4 != 2:
            masked_line(image, source, (brace_x, 0), (brace_x, 15), IRON_DARK)
            if brace_x < 15:
                masked_line(image, source, (brace_x + 1, 0), (brace_x + 1, 15), IRON_MID)

        accent_sets = (
            ((1, 3), (11, 13)),
            ((13, 2), (4, 9)),
            ((3, 7), (14, 12)),
            ((9, 1), (2, 14)),
        )
        accents = accent_sets[variant % len(accent_sets)]
    paint_masked_pixels(image, source, list(accents), RUST_MID)
    paint_masked_pixels(image, source, [accents[variant % 2]], BRASS_DARK)
    if variant % 5 == 1:
        paint_masked_pixels(image, source, [(14, 6), (13, 6), (14, 7)], VERDIGRIS_DARK)

    if overhang:
        masked_line(image, source, (0, 13), (15, 13), OUTLINE)
        masked_line(image, source, (0, 14), (15, 14), IRON_DARK)
    return image


def draw_cabinet(
    reference: Image.Image,
    index: int,
    material: str,
    variant: int = 0,
    connectivity: int | None = None,
    overhang: bool = False,
) -> Image.Image:
    source = tile(reference, index)
    connectivity = variant & 0b11 if connectivity is None else connectivity & 0b11
    if material == "wood":
        image = recolor_structure(source, WOOD_CABINET_RAMP)
        frame_dark, frame_light = DRY_DARK, DRY_LIGHT
        panel, handle = RUST_DARK, BRASS_MID
    elif material == "iron":
        image = recolor_structure(source, IRON_CABINET_RAMP)
        frame_dark, frame_light = IRON_DARK, IRON_LIGHT
        panel, handle = STONE_SHADOW, BRASS_DARK
    else:
        raise ValueError(f"unknown cabinet material: {material}")

    shelf_rows = (10, 13) if overhang else (4, 9, 14)
    for y in shelf_rows:
        masked_line(image, source, (1, y), (14, y), frame_dark)
        if y < 15:
            masked_line(image, source, (2, y + 1), (13, y + 1), frame_light)

    top = 8 if overhang else 1
    bottom = 15 if overhang else 14
    open_right = bool(connectivity & 0b01)
    open_left = bool(connectivity & 0b10)
    if not open_left:
        masked_line(image, source, (1, top), (1, bottom), frame_dark)
        masked_line(image, source, (2, top), (2, bottom), frame_light)
    if not open_right:
        masked_line(image, source, (14, top), (14, bottom), frame_dark)
        masked_line(image, source, (13, top), (13, bottom), frame_light)

    stile_x = 7 + (variant % 2)
    masked_line(image, source, (stile_x, top), (stile_x, bottom), frame_dark)
    handle_y = 12 if overhang else 6
    paint_masked_pixels(
        image,
        source,
        [(7 + variant % 2, handle_y), (8 - variant % 2, min(15, handle_y + 3))],
        handle,
    )
    panel_points = [(4, 12), (11, 9)] if overhang else [(4, 13), (11, 3)]
    paint_masked_pixels(image, source, panel_points, panel)
    if material == "iron" and variant % 3 == 1:
        paint_masked_pixels(image, source, [(12, 12), (13, 12)], RUST_MID)
    return image


def draw_wall_deco(
    reference: Image.Image,
    index: int,
    variant: int = 0,
    overhang: bool = False,
) -> Image.Image:
    source = tile(reference, index)
    image = draw_cast_wall(reference, index, variant, overhang=overhang)
    pipe_x = 3 if variant % 2 == 0 else 11
    masked_line(image, source, (pipe_x, 1), (pipe_x, 14), BRASS_DARK)
    masked_line(image, source, (pipe_x + 1, 1), (pipe_x + 1, 14), RUST_DARK)

    center = (10, 6) if variant % 2 == 0 else (5, 10)
    cx, cy = center
    valve = [
        (cx - 1, cy - 1), (cx, cy - 1), (cx + 1, cy - 1),
        (cx - 1, cy), (cx, cy), (cx + 1, cy),
        (cx - 1, cy + 1), (cx, cy + 1), (cx + 1, cy + 1),
    ]
    paint_masked_pixels(image, source, valve, IRON_DARK)
    paint_masked_pixels(image, source, [(cx, cy - 1), (cx, cy + 1)], IRON_LIGHT)
    paint_masked_pixels(image, source, [(cx - 1, cy), (cx + 1, cy)], VERDIGRIS_MID)
    paint_masked_pixels(image, source, [(cx, cy)], OUTLINE)
    return image


def draw_door(
    reference: Image.Image,
    index: int,
    kind: str,
    sideways: bool = False,
    overhang: bool = False,
) -> Image.Image:
    source = tile(reference, index)
    ramp = OPEN_DOOR_RAMP if kind == "open" else IRON_CABINET_RAMP
    image = recolor_structure(source, ramp)

    if kind == "open":
        if sideways:
            for y in range(5, 11):
                masked_line(image, source, (2, y), (13, y), VOID)
        else:
            for x in range(5, 11):
                masked_line(image, source, (x, 2), (x, 14), VOID)
    elif sideways:
        masked_line(image, source, (1, 7), (14, 7), OUTLINE)
        masked_line(image, source, (1, 8), (14, 8), IRON_LIGHT)
    else:
        masked_line(image, source, (7, 1), (7, 14), OUTLINE)
        masked_line(image, source, (8, 1), (8, 14), IRON_LIGHT)

    if kind in {"locked", "locked_exit"}:
        cx, cy = ((8, 8) if not sideways else (7, 8))
        lock_pixels = [
            (cx, cy),
            (cx - 1, cy), (cx + 1, cy),
            (cx, cy - 1), (cx, cy + 1),
        ]
        paint_masked_pixels(image, source, lock_pixels, BRASS_MID)
        paint_masked_pixels(image, source, [(cx, cy)], OUTLINE)
        paint_masked_pixels(image, source, [(cx + 1, cy - 1)], EMBER_DARK)
    elif kind == "pressure":
        if overhang and sideways:
            masked_line(image, source, (3, 9), (12, 9), VERDIGRIS_DARK)
            masked_line(image, source, (4, 12), (11, 12), VERDIGRIS_LIGHT)
            paint_masked_pixels(image, source, [(4, 10), (11, 13)], COOLANT_GLINT)
        elif overhang:
            masked_line(image, source, (2, 12), (13, 12), VERDIGRIS_DARK)
            masked_line(image, source, (3, 14), (12, 14), VERDIGRIS_LIGHT)
            paint_masked_pixels(image, source, [(4, 13), (11, 15)], COOLANT_GLINT)
        elif sideways:
            masked_line(image, source, (3, 6), (12, 6), VERDIGRIS_DARK)
            masked_line(image, source, (4, 9), (11, 9), VERDIGRIS_LIGHT)
            paint_masked_pixels(image, source, [(4, 4), (11, 11)], COOLANT_GLINT)
        else:
            masked_line(image, source, (6, 3), (6, 12), VERDIGRIS_DARK)
            masked_line(image, source, (9, 4), (9, 11), VERDIGRIS_LIGHT)
            paint_masked_pixels(image, source, [(4, 4), (11, 11)], COOLANT_GLINT)
    elif kind == "exit":
        signal = [(3, 3), (3, 4), (12, 11), (12, 12)]
        paint_masked_pixels(image, source, signal, BRASS_LIGHT)

    if overhang:
        masked_line(image, source, (1, 14), (14, 14), OUTLINE)
        masked_line(image, source, (2, 15), (13, 15), IRON_DARK)
    return image


def draw_exit_underhang(reference: Image.Image, index: int = 230) -> Image.Image:
    source = tile(reference, index)
    image = recolor_structure(source, IRON_CABINET_RAMP)
    masked_line(image, source, (0, 0), (15, 0), IRON_LIGHT)
    paint_masked_pixels(image, source, [(0, 0), (15, 0)], OUTLINE)
    masked_line(image, source, (0, 1), (0, 3), RUST_DARK)
    masked_line(image, source, (15, 1), (15, 3), RUST_DARK)
    paint_masked_pixels(image, source, [(1, 1), (14, 1)], BRASS_DARK)
    return image


def draw_door_threshold(
    reference: Image.Image,
    index: int,
    background: Image.Image,
) -> Image.Image:
    """Draw the floor visible behind a north/south doorway."""
    source = tile(reference, index)
    image = Image.new("RGBA", (T, T), TRANSPARENT)
    for y in range(T):
        for x in range(T):
            alpha = source.getpixel((x, y))[3]
            if not alpha:
                continue
            red, green, blue, _ = background.getpixel((x, y))
            image.putpixel((x, y), (red, green, blue, alpha))

    masked_line(image, source, (1, 7), (14, 7), IRON_DARK)
    masked_line(image, source, (1, 8), (14, 8), IRON_LIGHT)
    paint_masked_pixels(image, source, [(1, 7), (14, 7)], RUST_DARK)
    paint_masked_pixels(image, source, [(4, 8), (11, 8)], BRASS_DARK)
    return image


def raised_wall_material(index: int) -> str:
    if 92 <= index <= 95:
        return "wood"
    if 108 <= index <= 111:
        return "iron"
    return "wall"


def door_kind(index: int) -> str:
    flat_kinds = {
        56: "closed",
        57: "open",
        58: "locked",
        59: "pressure",
        60: "exit",
        61: "locked_exit",
    }
    raised_kinds = {
        112: "closed",
        113: "open",
        114: "locked",
        115: "pressure",
        116: "threshold",
    }
    overhang_kinds = (
        "closed",
        "open",
        "pressure",
        "closed",
        "locked",
        "pressure",
        "exit",
    )
    if index in flat_kinds:
        return flat_kinds[index]
    if index in raised_kinds:
        return raised_kinds[index]
    if 224 <= index <= 230:
        return overhang_kinds[index - 224]
    raise ValueError(f"unsupported door slot: {index}")


def build_tileset() -> Image.Image:
    with Image.open(ENV / "tiles_sewers.png") as source:
        sewers = source.convert("RGBA")
    with Image.open(ENV / "tiles_halls.png") as source:
        halls = source.convert("RGBA")

    atlas = Image.new("RGBA", (ATLAS_SIZE, ATLAS_SIZE), TRANSPARENT)
    floor = draw_iron_floor(SEED, 0)
    floor_alt_1 = draw_iron_floor(SEED + 6, 1)
    floor_alt_2 = draw_iron_floor(SEED + 12, 2)
    service_floor = draw_service_floor(SEED + 4)

    floor_slots = {
        0: floor,
        1: draw_floor_deco(floor, SEED + 1),
        2: draw_low_grass(floor, SEED + 2),
        3: draw_embers(floor, SEED + 3),
        4: service_floor,
        6: floor_alt_1,
        7: draw_floor_deco(floor_alt_1, SEED + 7, alt=True),
        8: draw_low_grass(floor, SEED + 8, alt=True),
        9: draw_embers(floor_alt_1, SEED + 9, alt=True),
        10: draw_service_floor(SEED + 10, alt=True),
        12: floor_alt_2,
    }
    for index, floor_tile in floor_slots.items():
        replace_tile(atlas, floor_tile, index)

    replace_tile(
        atlas,
        draw_stair(sewers, 17, 0, floor, IRON_STAIR_RAMP),
        16,
    )
    replace_tile(
        atlas,
        draw_stair(sewers, 16, 0, floor, IRON_STAIR_RAMP),
        17,
    )
    paste_tile(atlas, sewers, 18, 18)
    paste_tile(atlas, sewers, 19, 19)
    replace_tile(atlas, draw_bearing_pedestal(floor), 20)
    replace_tile(
        atlas,
        draw_stair(sewers, 17, 0, service_floor, WOOD_STAIR_RAMP),
        22,
    )

    edge_kinds = (
        "plain",
        "iron",
        "service",
        "stone",
        "coolant",
        "plain",
        "iron",
        "service",
    )
    for offset, edge_kind in enumerate(edge_kinds):
        replace_tile(
            atlas,
            draw_chasm(SEED + 100 + offset, edge_kind),
            24 + offset,
        )

    water = build_water_texture()
    for index in range(33, 48):
        replace_tile(
            atlas,
            draw_water_transition(sewers, index, floor, water),
            index,
        )

    flat_wall_slots = {
        48: draw_cast_wall(halls, 48, 0),
        49: draw_wall_deco(halls, 49, 0),
        50: draw_cabinet(halls, 50, "wood", 0),
        52: draw_cast_wall(halls, 52, 1),
        53: draw_wall_deco(halls, 53, 1),
        54: draw_cabinet(halls, 54, "iron", 1),
    }
    for index, wall_tile in flat_wall_slots.items():
        replace_tile(atlas, wall_tile, index)

    for index in range(56, 62):
        replace_tile(atlas, draw_door(halls, index, door_kind(index)), index)

    grass_variants = (
        (66, 67, SEED + 66, False),
        (69, 70, SEED + 69, True),
    )
    for live_index, dry_index, seed, alt in grass_variants:
        replace_tile(
            atlas,
            draw_flat_tall_grass(floor, seed, dry=False, alt=alt),
            live_index,
        )
        replace_tile(
            atlas,
            draw_flat_tall_grass(floor, seed, dry=True, alt=alt),
            dry_index,
        )

    flat_machinery = {
        64: draw_pressure_console(floor),
        65: draw_collapsed_rack(floor),
        72: draw_automaton(floor),
        73: draw_automaton(service_floor, service_floor=True),
        74: draw_flywheel(floor),
        75: draw_gearbox(service_floor),
        76: draw_metal_stock(floor, 0),
        77: draw_metal_stock(service_floor, 1),
        78: draw_metal_stock(floor, 2),
    }
    for index, machinery_tile in flat_machinery.items():
        replace_tile(atlas, machinery_tile, index)

    for index in range(80, 112):
        material = raised_wall_material(index)
        variant = index % 7
        if material == "wall":
            wall_tile = draw_cast_wall(halls, index, variant)
        else:
            wall_tile = draw_cabinet(
                halls,
                index,
                material,
                variant,
                connectivity=index & 0b11,
            )
        replace_tile(atlas, wall_tile, index)

    for index in range(112, 116):
        replace_tile(
            atlas,
            draw_door(
                halls,
                index,
                door_kind(index),
            ),
            index,
        )
    replace_tile(atlas, draw_door_threshold(halls, 116, floor), 116)

    raised_machinery = {
        120: draw_pressure_console(floor, raised=True),
        121: draw_collapsed_rack(floor, raised=True),
        128: draw_automaton(floor, body_only=True),
        129: draw_automaton(service_floor, service_floor=True, body_only=True),
        130: draw_flywheel(floor, body_only=True),
        131: draw_gearbox(service_floor, body_only=True),
        132: draw_metal_stock(floor, 0, raised=True),
        133: draw_metal_stock(floor, 1, raised=True),
        134: draw_metal_stock(floor, 2, raised=True),
    }
    for index, machinery_tile in raised_machinery.items():
        replace_tile(atlas, machinery_tile, index)

    raised_grass_variants = (
        (122, 123, SEED + 66, False),
        (125, 126, SEED + 69, True),
    )
    for live_index, dry_index, seed, alt in raised_grass_variants:
        replace_tile(
            atlas,
            draw_raised_tall_grass(floor, seed, dry=False, alt=alt),
            live_index,
        )
        replace_tile(
            atlas,
            draw_raised_tall_grass(floor, seed, dry=True, alt=alt),
            dry_index,
        )

    for index in range(144, 192):
        if index < 160:
            wall_tile = draw_cast_wall(halls, index, index - 144)
        elif index < 176:
            wall_tile = draw_wall_deco(halls, index, index - 160)
        else:
            mask = index - 176
            connectivity = (mask & 0b0001) | ((mask & 0b1000) >> 2)
            wall_tile = draw_cabinet(
                halls,
                index,
                "wood",
                variant=mask // 4,
                connectivity=connectivity,
            )
        replace_tile(atlas, wall_tile, index)

    for index in range(192, 196):
        replace_tile(atlas, draw_cast_wall(halls, index, index - 192, True), index)
    for index in range(196, 200):
        replace_tile(atlas, draw_wall_deco(halls, index, index - 196, True), index)
    for index in range(200, 204):
        replace_tile(
            atlas,
            draw_cabinet(
                halls,
                index,
                "wood",
                connectivity=index - 200,
                overhang=True,
            ),
            index,
        )

    sideways_overhang_groups = (
        (range(208, 212), "open"),
        (range(212, 216), "closed"),
        (range(216, 220), "locked"),
        (range(220, 224), "pressure"),
    )
    for indices, kind in sideways_overhang_groups:
        for index in indices:
            replace_tile(
                atlas,
                draw_door(halls, index, kind, sideways=True, overhang=True),
                index,
            )

    for index in range(224, 231):
        if index == 230:
            replace_tile(atlas, draw_exit_underhang(halls), index)
            continue
        replace_tile(
            atlas,
            draw_door(
                halls,
                index,
                door_kind(index),
                sideways=227 <= index <= 229,
                overhang=True,
            ),
            index,
        )

    machinery_overhangs = {
        232: draw_pressure_console(None, raised=True, overhang=True),
        233: draw_collapsed_rack(None, raised=True, overhang=True),
        240: draw_automaton(None, body_only=True, overhang=True),
        241: draw_automaton(None, service_floor=True, body_only=True, overhang=True),
        242: draw_flywheel(None, body_only=True, overhang=True),
        243: draw_gearbox(None, body_only=True, overhang=True),
        244: draw_metal_stock(None, 0, raised=True, overhang=True),
        245: draw_metal_stock(None, 1, raised=True, overhang=True),
        246: draw_metal_stock(None, 2, raised=True, overhang=True),
    }
    for index, machinery_tile in machinery_overhangs.items():
        replace_tile(atlas, machinery_tile, index)

    layered_grass_variants = (
        (234, 235, 250, 251, SEED + 66, False),
        (237, 238, 253, 254, SEED + 69, True),
    )
    for live_overhang, dry_overhang, live_underhang, dry_underhang, seed, alt in layered_grass_variants:
        replace_tile(
            atlas,
            draw_tall_grass_overhang(seed, dry=False, alt=alt),
            live_overhang,
        )
        replace_tile(
            atlas,
            draw_tall_grass_overhang(seed, dry=True, alt=alt),
            dry_overhang,
        )
        replace_tile(
            atlas,
            draw_tall_grass_underhang(seed, dry=False, alt=alt),
            live_underhang,
        )
        replace_tile(
            atlas,
            draw_tall_grass_underhang(seed, dry=True, alt=alt),
            dry_underhang,
        )

    unused_opaque_slots = {
        5, 11, 13, 14, 15, 21, 23,
        51, 55, 62, 63, 68, 71, 79,
        124, 127, 135, 136, 137, 138, 139, 140, 141, 142, 143,
    }
    for index in unused_opaque_slots:
        replace_tile(atlas, service_floor if index % 4 == 3 else floor, index)
    return atlas


def generate() -> tuple[Path, Path]:
    atlas = build_tileset()
    water = build_water_texture()
    OUT.parent.mkdir(parents=True, exist_ok=True)
    atlas.save(OUT)
    water.save(WATER_OUT)
    return OUT, WATER_OUT


def main() -> None:
    for output_path in generate():
        print(output_path)


if __name__ == "__main__":
    main()
