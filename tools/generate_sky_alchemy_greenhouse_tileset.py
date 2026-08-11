from __future__ import annotations

import hashlib
import random
from pathlib import Path

from PIL import Image, ImageDraw

try:
    from tools import generate_astral_library_tileset as base
except ImportError:
    import generate_astral_library_tileset as base


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
OUT = ENV / "tiles_sky_alchemy_greenhouse.png"
WATER_OUT = ENV / "water_sky_alchemy_greenhouse.png"

T = 16
COLS = 16
ATLAS_SIZE = T * COLS
TRANSPARENT = (0, 0, 0, 0)

OUTLINE = (19, 29, 30, 255)
SKY_DARK = (27, 54, 72, 255)
SKY_MID = (50, 91, 112, 255)
SKY_LIGHT = (103, 151, 158, 255)
CLOUD = (179, 199, 187, 255)

STONE_SHADOW = (50, 62, 58, 255)
STONE_DARK = (70, 82, 74, 255)
STONE_MID = (94, 105, 91, 255)
STONE_LIGHT = (124, 133, 109, 255)
STONE_EDGE = (158, 163, 130, 255)

BRASS_DARK = (91, 69, 34, 255)
BRASS_MID = (137, 103, 47, 255)
BRASS_LIGHT = (188, 151, 70, 255)
WOOD_DARK = (64, 43, 31, 255)
WOOD_MID = (94, 61, 38, 255)
WOOD_LIGHT = (135, 88, 49, 255)

GLASS_DARK = (35, 82, 88, 255)
GLASS_MID = (52, 119, 119, 255)
GLASS_LIGHT = (92, 169, 154, 255)
GLASS_GLINT = (160, 218, 190, 255)

LEAF_DARK = (35, 76, 43, 255)
LEAF_MID = (51, 111, 54, 255)
LEAF_LIGHT = (82, 147, 67, 255)
LEAF_GLOW = (132, 181, 83, 255)
SOIL_DARK = (64, 50, 37, 255)
SOIL_MID = (91, 70, 43, 255)

DRY_DARK = (79, 62, 38, 255)
DRY_MID = (116, 88, 48, 255)
DRY_LIGHT = (157, 123, 66, 255)

PETAL_PINK = (206, 116, 131, 255)
PETAL_WHITE = (219, 218, 172, 255)
ALCHEMY_BLUE = (67, 151, 158, 255)
ALCHEMY_GOLD = (210, 174, 75, 255)

WATER_DARK = (24, 78, 84, 255)
WATER_MID = (35, 105, 107, 255)
WATER_LIGHT = (55, 137, 128, 255)
WATER_GLINT = (108, 190, 158, 255)

STONE_RAMP = (
    OUTLINE, STONE_SHADOW, STONE_DARK, STONE_MID, STONE_LIGHT, STONE_EDGE
)
BRASS_RAMP = (
    OUTLINE, WOOD_DARK, BRASS_DARK, BRASS_MID, BRASS_LIGHT, GLASS_LIGHT
)


def tile_rect(index: int) -> tuple[int, int, int, int]:
    if not 0 <= index < 256:
        raise ValueError(f"tile index out of range: {index}")
    left = index % COLS * T
    top = index // COLS * T
    return left, top, left + T, top + T


def tile(image: Image.Image, index: int) -> Image.Image:
    return image.crop(tile_rect(index)).convert("RGBA")


def replace_tile(atlas: Image.Image, image: Image.Image, index: int) -> None:
    atlas.paste(image.convert("RGBA"), tile_rect(index))


def local_draw(background: Image.Image | None = None) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = background.copy() if background is not None else Image.new("RGBA", (T, T), TRANSPARENT)
    return image, ImageDraw.Draw(image)


def recolor_pixel(pixel: tuple[int, int, int, int]) -> tuple[int, int, int, int]:
    red, green, blue, alpha = pixel
    if alpha == 0:
        return TRANSPARENT

    exact = {
        base.INK: OUTLINE,
        base.VOID: SKY_DARK,
        base.STONE_SHADOW: STONE_SHADOW,
        base.STONE_DARK: STONE_DARK,
        base.STONE: STONE_MID,
        base.STONE_LIGHT: STONE_LIGHT,
        base.STONE_EDGE: STONE_EDGE,
        base.WOOD_DARK: WOOD_DARK,
        base.WOOD: WOOD_MID,
        base.WOOD_LIGHT: WOOD_LIGHT,
        base.BRASS_DARK: BRASS_DARK,
        base.BRASS: BRASS_MID,
        base.BRASS_LIGHT: BRASS_LIGHT,
        base.CYAN_DARK: GLASS_DARK,
        base.CYAN: GLASS_MID,
        base.CYAN_LIGHT: GLASS_LIGHT,
        base.STAR_WHITE: GLASS_GLINT,
        base.VIOLET: LEAF_DARK,
        base.VIOLET_LIGHT: LEAF_LIGHT,
        base.PARCHMENT_DARK: SOIL_DARK,
        base.PARCHMENT: LEAF_MID,
        base.PARCHMENT_LIGHT: LEAF_GLOW,
        base.WATER_DARK: WATER_DARK,
        base.WATER_MID: WATER_MID,
        base.WATER_LIGHT: WATER_LIGHT,
        base.WATER_GLINT: WATER_GLINT,
    }
    if pixel in exact:
        return exact[pixel]

    value = (red * 3 + green * 5 + blue * 2) // 10
    if blue >= red + 16 and green >= red + 8:
        ramp = (GLASS_DARK, GLASS_MID, GLASS_LIGHT, GLASS_GLINT)
    elif green >= red + 10:
        ramp = (LEAF_DARK, LEAF_MID, LEAF_LIGHT, LEAF_GLOW)
    elif red >= blue + 20:
        ramp = (WOOD_DARK, BRASS_DARK, BRASS_MID, BRASS_LIGHT)
    else:
        ramp = (STONE_SHADOW, STONE_DARK, STONE_MID, STONE_LIGHT, STONE_EDGE)
    color = ramp[min(len(ramp) - 1, value * len(ramp) // 256)]
    return color[0], color[1], color[2], alpha


def recolor_image(image: Image.Image) -> Image.Image:
    source = image.convert("RGBA")
    result = Image.new("RGBA", source.size, TRANSPARENT)
    result.putdata([recolor_pixel(pixel) for pixel in source.getdata()])
    return result


def draw_floor(seed: int, variant: int = 0) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=STONE_MID)
    layouts = ((5, 11), (4, 10), (6, 12))
    for y in layouts[variant % 3]:
        draw.line((0, y, 15, y), fill=STONE_DARK)
        if y < 15:
            draw.line((0, y + 1, 15, y + 1), fill=STONE_LIGHT)
    offsets = ((4, 12, 7), (8, 2, 13), (11, 5, 2))
    for row, start_y in enumerate((0, 6, 12)):
        x = offsets[variant % 3][row]
        draw.line((x, start_y, x, min(15, start_y + 4)), fill=STONE_SHADOW)
    for _ in range(4):
        x, y = rng.randrange(1, 15), rng.randrange(1, 15)
        draw.point((x, y), fill=rng.choice((STONE_DARK, STONE_LIGHT, LEAF_DARK)))
    return image


def draw_garden_floor(background: Image.Image, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    if alt:
        draw.rectangle((1, 1, 5, 14), fill=SOIL_MID)
        draw.line((6, 1, 6, 14), fill=BRASS_DARK)
        plants = ((2, 4), (4, 8), (2, 12))
    else:
        draw.rectangle((1, 9, 14, 14), fill=SOIL_MID)
        draw.line((1, 8, 14, 8), fill=BRASS_DARK)
        plants = ((3, 11), (7, 13), (11, 10))
    for x, y in plants:
        draw.point((x, y), fill=LEAF_LIGHT)
        draw.point((min(15, x + 1), max(0, y - 1)), fill=LEAF_GLOW)
    draw.point((13, 3), fill=PETAL_PINK)
    draw.point((9, 5), fill=PETAL_WHITE)
    return image


def draw_low_herbs(background: Image.Image, seed: int, alt: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw(background)
    anchors = ((3, 12), (7, 10), (11, 13), (13, 8)) if not alt else ((2, 8), (5, 13), (9, 11), (13, 13))
    for order, (x, y) in enumerate(anchors):
        height = 2 + (order + int(alt)) % 3
        draw.line((x, y, x, y - height), fill=LEAF_DARK)
        draw.point((x - 1, y - height + 1), fill=LEAF_MID)
        draw.point((x + 1, y - height), fill=LEAF_LIGHT)
        if order % 2 == 0:
            draw.point((x, y - height - 1), fill=PETAL_PINK if alt else PETAL_WHITE)
    for _ in range(3):
        draw.point((rng.randrange(2, 14), rng.randrange(2, 14)), fill=LEAF_GLOW)
    return image


def draw_spores(background: Image.Image, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    points = ((3, 12), (6, 8), (9, 11), (12, 5), (13, 13))
    for order, (x, y) in enumerate(points):
        color = ALCHEMY_GOLD if (order + int(alt)) % 3 == 0 else LEAF_GLOW
        draw.point((x, y), fill=color)
        if order % 2:
            draw.point((x, max(0, y - 1)), fill=GLASS_GLINT)
    return image


def draw_grate_floor(background: Image.Image, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    for x in range(1, 16, 4):
        draw.line((x, 1, x, 14), fill=BRASS_DARK)
        draw.line((x + 1, 1, x + 1, 14), fill=BRASS_LIGHT)
    y = 5 if not alt else 10
    draw.line((0, y, 15, y), fill=OUTLINE)
    draw.line((0, y + 1, 15, y + 1), fill=BRASS_MID)
    return image


def draw_chasm(seed: int, edge: str) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=SKY_DARK)
    draw.rectangle((0, 4, 15, 15), fill=SKY_MID)
    for left, y, right in ((1, 7, 6), (9, 11, 15), (3, 14, 10)):
        draw.line((left, y, right, y), fill=CLOUD)
        draw.point((min(15, right + 1), y), fill=SKY_LIGHT)
    for _ in range(5):
        draw.point((rng.randrange(16), rng.randrange(3, 15)), fill=SKY_LIGHT)
    if edge == "stone":
        draw.line((0, 0, 15, 0), fill=STONE_EDGE)
        draw.line((0, 1, 15, 1), fill=STONE_DARK)
    elif edge == "brass":
        draw.line((0, 0, 15, 0), fill=BRASS_LIGHT)
        draw.line((0, 1, 15, 1), fill=BRASS_DARK)
    elif edge == "wall":
        draw.rectangle((0, 0, 15, 3), fill=STONE_DARK)
        draw.line((0, 0, 15, 0), fill=STONE_EDGE)
    elif edge == "water":
        draw.line((0, 0, 15, 0), fill=WATER_GLINT)
        draw.line((0, 1, 15, 1), fill=WATER_DARK)
    return image


def draw_glass_wall(deco: bool = False, cabinet: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=STONE_DARK)
    draw.rectangle((1, 1, 14, 9), fill=GLASS_DARK)
    draw.rectangle((2, 2, 13, 8), fill=GLASS_MID)
    frame_x = 5 if alt else 8
    draw.line((frame_x, 1, frame_x, 9), fill=BRASS_MID)
    draw.line((1, 5, 14, 5), fill=BRASS_DARK)
    draw.line((2, 2, 5, 2), fill=GLASS_GLINT)
    draw.rectangle((0, 10, 15, 15), fill=STONE_MID)
    draw.line((0, 10, 15, 10), fill=STONE_EDGE)
    draw.line((0, 14, 15, 14), fill=STONE_DARK)
    if deco:
        draw.line((3, 14, 4, 5), fill=LEAF_DARK)
        for x, y in ((3, 12), (5, 10), (3, 8), (5, 6)):
            draw.point((x, y), fill=LEAF_LIGHT)
            draw.point((x + 1, y - 1), fill=LEAF_MID)
    if cabinet:
        draw.rectangle((2, 4, 13, 14), fill=WOOD_DARK)
        draw.rectangle((3, 5, 12, 13), fill=WOOD_MID)
        for y in (7, 10):
            draw.line((3, y, 12, y), fill=BRASS_DARK)
        for x, y, color in ((4, 6, PETAL_WHITE), (7, 9, LEAF_GLOW), (10, 6, PETAL_PINK), (11, 12, GLASS_LIGHT)):
            draw.point((x, y), fill=color)
        draw.line((3, 5, 8, 5), fill=WOOD_LIGHT)
    return image


def draw_alchemy_table(background: Image.Image, raised: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    top = 7 if not raised else 4
    draw.rectangle((2, top, 13, top + 2), fill=WOOD_DARK)
    draw.line((2, top, 13, top), fill=BRASS_LIGHT)
    draw.rectangle((3, top + 3, 4, 14), fill=WOOD_MID)
    draw.rectangle((11, top + 3, 12, 14), fill=WOOD_MID)
    draw.ellipse((4, top - 4, 7, top), fill=GLASS_DARK, outline=GLASS_LIGHT)
    draw.ellipse((9, top - 3, 12, top), fill=LEAF_DARK, outline=LEAF_GLOW)
    draw.point((5, top - 4), fill=GLASS_GLINT)
    draw.line((8, top - 5, 8, top), fill=BRASS_MID)
    return image


def draw_broken_planter(background: Image.Image, raised: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    top = 8 if not raised else 5
    draw.rectangle((2, top, 12, top + 4), fill=STONE_DARK)
    draw.line((2, top, 12, top), fill=STONE_EDGE)
    draw.rectangle((4, top - 2, 10, top), fill=SOIL_MID)
    draw.line((8, top, 13, top + 5), fill=BRASS_DARK)
    draw.line((5, top - 1, 4, top - 5), fill=DRY_DARK)
    draw.point((3, top - 4), fill=DRY_MID)
    return image


def herb_stems(alt: bool = False) -> tuple[tuple[int, int, int], ...]:
    return ((3, 8, -1), (6, 11, 1), (9, 9, -1), (12, 12, 1)) if not alt else ((2, 10, 1), (5, 8, -1), (9, 12, 1), (13, 9, -1))


def draw_tall_herbs(background: Image.Image, dry: bool = False, alt: bool = False, raised: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    dark, mid, light = (DRY_DARK, DRY_MID, DRY_LIGHT) if dry else (LEAF_DARK, LEAF_MID, LEAF_LIGHT)
    for order, (x, height, bend) in enumerate(herb_stems(alt)):
        bottom = 15
        top = max(1, bottom - height + (3 if raised else 0))
        draw.line((x, bottom, x, top + 2), fill=dark)
        draw.line((x, top + 2, x + bend, top), fill=mid)
        for y in range(top + 3, bottom, 3):
            side = bend if (y + order) % 2 else -bend
            draw.point((x + side, y), fill=light)
            draw.point((x + side * 2, max(top, y - 1)), fill=mid)
        if not dry and order % 2 == 0:
            draw.point((x + bend, max(0, top - 1)), fill=PETAL_WHITE if alt else PETAL_PINK)
    return image


def draw_herb_overhang(dry: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    dark, mid, light = (DRY_DARK, DRY_MID, DRY_LIGHT) if dry else (LEAF_DARK, LEAF_MID, LEAF_LIGHT)
    for order, (x, _, bend) in enumerate(herb_stems(alt)):
        top = 7 + order % 2
        draw.line((x, 15, x, top + 2), fill=dark)
        draw.line((x, top + 2, x + bend, top), fill=mid)
        draw.point((x - bend, top + 3), fill=light)
        draw.point((x + bend * 2, top + 1), fill=mid)
        if not dry and order % 2 == 0:
            draw.point((x + bend, top - 1), fill=PETAL_WHITE)
    return image


def draw_herb_underhang(dry: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    color = DRY_MID if dry else LEAF_MID
    for order, (x, _, bend) in enumerate(herb_stems(alt)):
        draw.line((x, 0, x + bend, 2 + order % 2), fill=color)
    return image


def draw_sun_prism(background: Image.Image, body_only: bool = False, flowering: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((4, 10, 11, 14), fill=STONE_DARK)
    draw.line((3, 10, 12, 10), fill=STONE_EDGE)
    draw.rectangle((7, 5, 8, 10), fill=BRASS_MID)
    if not body_only:
        if flowering:
            for x, y in ((5, 5), (8, 3), (11, 5), (8, 7)):
                draw.point((x, y), fill=PETAL_PINK)
                draw.point((8, 5), fill=LEAF_GLOW)
        else:
            draw.polygon(((8, 1), (12, 5), (8, 9), (4, 5)), fill=GLASS_DARK)
            draw.line((8, 1, 8, 9), fill=GLASS_LIGHT)
            draw.line((4, 5, 12, 5), fill=BRASS_LIGHT)
            draw.point((7, 3), fill=GLASS_GLINT)
    return image


def draw_growing_vat(background: Image.Image, body_only: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((4, 7, 11, 14), fill=BRASS_DARK)
    draw.rectangle((5, 7, 10, 13), fill=GLASS_DARK)
    draw.rectangle((6, 8, 9, 12), fill=GLASS_MID)
    draw.line((4, 14, 11, 14), fill=BRASS_LIGHT)
    if not body_only:
        draw.line((7, 8, 7, 3), fill=LEAF_DARK)
        draw.line((8, 8, 9, 2), fill=LEAF_MID)
        draw.point((6, 5), fill=LEAF_LIGHT)
        draw.point((10, 4), fill=LEAF_GLOW)
        draw.point((9, 1), fill=PETAL_WHITE if alt else PETAL_PINK)
    return image


def draw_trellis(background: Image.Image, body_only: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    for x in (3, 8, 12):
        draw.line((x, 4, x, 15), fill=BRASS_DARK)
    for y in (6, 10):
        draw.line((2, y, 13, y), fill=BRASS_MID)
    if not body_only:
        for order, (x, y) in enumerate(((4, 12), (7, 8), (10, 5), (12, 11), (5, 4))):
            draw.point((x, y), fill=LEAF_LIGHT)
            draw.point((min(15, x + 1), max(0, y - 1)), fill=LEAF_MID)
            if order % 2 == int(alt):
                draw.point((x, max(0, y - 2)), fill=PETAL_PINK)
    return image


def draw_object_overhang(kind: str, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    if kind == "alchemy":
        draw.line((8, 15, 8, 8), fill=BRASS_MID)
        draw.ellipse((5, 7, 10, 12), fill=GLASS_DARK, outline=GLASS_LIGHT)
        draw.point((6, 8), fill=GLASS_GLINT)
    elif kind == "planter":
        draw.line((6, 15, 5, 10), fill=DRY_DARK)
        draw.point((4, 10), fill=DRY_LIGHT)
    elif kind == "prism":
        color = PETAL_PINK if alt else GLASS_LIGHT
        draw.polygon(((8, 5), (12, 10), (8, 15), (4, 10)), fill=GLASS_DARK)
        draw.line((4, 10, 12, 10), fill=color)
    elif kind == "vat":
        draw.line((7, 15, 7, 8), fill=LEAF_DARK)
        draw.line((8, 15, 10, 6), fill=LEAF_MID)
        draw.point((6, 10), fill=LEAF_LIGHT)
        draw.point((10, 5), fill=PETAL_WHITE if alt else PETAL_PINK)
    elif kind == "trellis":
        for x in (3, 8, 12):
            draw.line((x, 15, x, 5), fill=BRASS_DARK)
        for x, y in ((4, 12), (7, 8), (11, 10)):
            draw.point((x, y), fill=LEAF_LIGHT)
            draw.point((x + 1, y - 1), fill=PETAL_PINK if alt else LEAF_MID)
    return image


def build_water_texture() -> Image.Image:
    image = Image.new("RGBA", (32, 32), WATER_MID)
    draw = ImageDraw.Draw(image)
    rng = random.Random(928411)
    for left, y, right, color in (
        (2, 5, 11, WATER_LIGHT),
        (16, 8, 27, WATER_GLINT),
        (5, 15, 14, WATER_DARK),
        (18, 20, 29, WATER_LIGHT),
        (3, 27, 10, WATER_GLINT),
    ):
        draw.line((left, y, right, y), fill=color)
    for _ in range(22):
        x, y = rng.randrange(1, 31), rng.randrange(1, 31)
        draw.point((x, y), fill=rng.choice((GLASS_DARK, GLASS_LIGHT, LEAF_GLOW, ALCHEMY_GOLD)))
    for x in range(32):
        image.putpixel((x, 31), image.getpixel((x, 0)))
    for y in range(32):
        image.putpixel((31, y), image.getpixel((0, y)))
    return image


def build_tileset() -> Image.Image:
    sewers = Image.open(ENV / "tiles_sewers.png").convert("RGBA")
    atlas = recolor_image(base.build_tileset())

    floor = draw_floor(928400, 0)
    floor_alt_1 = draw_floor(928406, 1)
    floor_alt_2 = draw_floor(928412, 2)
    grate = draw_grate_floor(floor)
    grate_alt = draw_grate_floor(floor_alt_1, alt=True)
    floors = {
        0: floor,
        1: draw_garden_floor(floor),
        2: draw_low_herbs(floor, 928402),
        3: draw_spores(floor),
        4: grate,
        6: floor_alt_1,
        7: draw_garden_floor(floor_alt_1, alt=True),
        8: draw_low_herbs(floor_alt_2, 928408, alt=True),
        9: draw_spores(floor_alt_1, alt=True),
        10: grate_alt,
        12: floor_alt_2,
    }
    for index in range(24):
        replace_tile(atlas, floors.get(index, floor), index)

    replace_tile(atlas, base.draw_stair(sewers, 17, floor, STONE_RAMP), 16)
    replace_tile(atlas, base.draw_stair(sewers, 16, floor, STONE_RAMP), 17)
    replace_tile(atlas, tile(sewers, 18), 18)
    replace_tile(atlas, tile(sewers, 19), 19)
    replace_tile(atlas, draw_sun_prism(floor, flowering=True), 20)
    replace_tile(atlas, base.draw_stair(sewers, 17, grate, BRASS_RAMP), 22)

    edges = ("plain", "stone", "brass", "wall", "water", "plain", "stone", "brass")
    for offset, edge in enumerate(edges):
        replace_tile(atlas, draw_chasm(929000 + offset, edge), 24 + offset)

    water = build_water_texture()
    for index in range(32, 48):
        replace_tile(atlas, base.draw_water_transition(sewers, index, floor, water), index)

    for index, image in {
        48: draw_glass_wall(),
        49: draw_glass_wall(deco=True),
        50: draw_glass_wall(cabinet=True),
        52: draw_glass_wall(alt=True),
        53: draw_glass_wall(deco=True, alt=True),
        54: draw_glass_wall(cabinet=True, alt=True),
        62: draw_glass_wall(deco=True, alt=True),
        63: draw_glass_wall(cabinet=True),
    }.items():
        replace_tile(atlas, image, index)

    flat_objects = {
        64: draw_alchemy_table(floor),
        65: draw_broken_planter(floor),
        66: draw_tall_herbs(floor),
        67: draw_tall_herbs(floor, dry=True),
        69: draw_tall_herbs(floor, alt=True),
        70: draw_tall_herbs(floor, dry=True, alt=True),
        72: draw_sun_prism(floor),
        73: draw_sun_prism(grate, flowering=True),
        74: draw_growing_vat(floor),
        75: draw_trellis(grate),
        76: draw_growing_vat(floor, alt=True),
        77: draw_trellis(floor, alt=True),
        78: draw_sun_prism(floor, flowering=True),
    }
    for index, image in flat_objects.items():
        replace_tile(atlas, image, index)

    raised_objects = {
        120: draw_alchemy_table(floor, raised=True),
        121: draw_broken_planter(floor, raised=True),
        122: draw_tall_herbs(floor, raised=True),
        123: draw_tall_herbs(floor, dry=True, raised=True),
        125: draw_tall_herbs(floor, alt=True, raised=True),
        126: draw_tall_herbs(floor, dry=True, alt=True, raised=True),
        128: draw_sun_prism(floor, body_only=True),
        129: draw_sun_prism(grate, body_only=True, flowering=True),
        130: draw_growing_vat(floor, body_only=True),
        131: draw_trellis(grate, body_only=True),
        132: draw_growing_vat(floor, body_only=True, alt=True),
        133: draw_trellis(floor, body_only=True, alt=True),
        134: draw_sun_prism(floor, body_only=True, flowering=True),
    }
    for index, image in raised_objects.items():
        replace_tile(atlas, image, index)

    overhangs = {
        232: draw_object_overhang("alchemy"),
        233: draw_object_overhang("planter"),
        234: draw_herb_overhang(),
        235: draw_herb_overhang(dry=True),
        237: draw_herb_overhang(alt=True),
        238: draw_herb_overhang(dry=True, alt=True),
        240: draw_object_overhang("prism"),
        241: draw_object_overhang("prism", alt=True),
        242: draw_object_overhang("vat"),
        243: draw_object_overhang("trellis"),
        244: draw_object_overhang("vat", alt=True),
        245: draw_object_overhang("trellis", alt=True),
        246: draw_object_overhang("prism", alt=True),
        250: draw_herb_underhang(),
        251: draw_herb_underhang(dry=True),
        253: draw_herb_underhang(alt=True),
        254: draw_herb_underhang(dry=True, alt=True),
    }
    for index, image in overhangs.items():
        replace_tile(atlas, image, index)

    return atlas


def generate() -> tuple[Path, Path]:
    atlas = build_tileset()
    water = build_water_texture()
    OUT.parent.mkdir(parents=True, exist_ok=True)
    atlas.save(OUT)
    water.save(WATER_OUT)
    return OUT, WATER_OUT


def image_digest(image: Image.Image) -> str:
    return hashlib.sha256(image.tobytes()).hexdigest()


def main() -> None:
    atlas_path, water_path = generate()
    atlas = Image.open(atlas_path).convert("RGBA")
    water = Image.open(water_path).convert("RGBA")
    print(f"{atlas_path} {atlas.size} {image_digest(atlas)}")
    print(f"{water_path} {water.size} {image_digest(water)}")


if __name__ == "__main__":
    main()
