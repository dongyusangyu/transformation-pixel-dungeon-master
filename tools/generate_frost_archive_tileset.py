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
OUT = ENV / "tiles_frost_archive.png"
WATER_OUT = ENV / "water_frost_archive.png"

T = 16
COLS = 16
ATLAS_SIZE = T * COLS
TRANSPARENT = (0, 0, 0, 0)

OUTLINE = (15, 22, 31, 255)
VOID = (8, 15, 25, 255)
FOG_DARK = (25, 42, 56, 255)
FOG_MID = (53, 75, 87, 255)
FOG_LIGHT = (99, 122, 128, 255)

STONE_SHADOW = (37, 47, 59, 255)
STONE_DARK = (51, 62, 75, 255)
STONE_MID = (69, 82, 94, 255)
STONE_LIGHT = (91, 105, 116, 255)
STONE_EDGE = (118, 132, 139, 255)

METAL_DARK = (38, 49, 62, 255)
METAL_MID = (57, 70, 84, 255)
METAL_LIGHT = (82, 96, 108, 255)

ICE_DARK = (28, 71, 99, 255)
ICE_MID = (45, 105, 137, 255)
ICE_LIGHT = (85, 158, 186, 255)
ICE_PALE = (142, 205, 216, 255)
ICE_GLINT = (205, 239, 235, 255)

FROST_SHADOW = (85, 101, 111, 255)
FROST_MID = (126, 145, 151, 255)
FROST_LIGHT = (173, 193, 192, 255)

AMBER_DARK = (91, 55, 26, 255)
AMBER_MID = (145, 89, 31, 255)
AMBER_LIGHT = (214, 150, 54, 255)

EXHAUSTED_DARK = (57, 66, 73, 255)
EXHAUSTED_MID = (83, 91, 96, 255)
EXHAUSTED_LIGHT = (116, 124, 126, 255)

COOLANT_DARK = (17, 50, 78, 255)
COOLANT_MID = (25, 75, 108, 255)
COOLANT_LIGHT = (45, 112, 145, 255)
COOLANT_GLINT = (91, 174, 190, 255)

STONE_RAMP = (OUTLINE, STONE_SHADOW, STONE_DARK, STONE_MID, STONE_LIGHT, STONE_EDGE)
METAL_RAMP = (OUTLINE, METAL_DARK, STONE_DARK, METAL_MID, METAL_LIGHT, ICE_LIGHT)


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
        base.VOID: VOID,
        base.STONE_SHADOW: STONE_SHADOW,
        base.STONE_DARK: STONE_DARK,
        base.STONE: STONE_MID,
        base.STONE_LIGHT: STONE_LIGHT,
        base.STONE_EDGE: STONE_EDGE,
        base.WOOD_DARK: METAL_DARK,
        base.WOOD: METAL_MID,
        base.WOOD_LIGHT: METAL_LIGHT,
        base.BRASS_DARK: AMBER_DARK,
        base.BRASS: AMBER_MID,
        base.BRASS_LIGHT: AMBER_LIGHT,
        base.CYAN_DARK: ICE_DARK,
        base.CYAN: ICE_MID,
        base.CYAN_LIGHT: ICE_LIGHT,
        base.STAR_WHITE: ICE_GLINT,
        base.VIOLET: FROST_SHADOW,
        base.VIOLET_LIGHT: FROST_LIGHT,
        base.PARCHMENT_DARK: EXHAUSTED_DARK,
        base.PARCHMENT: EXHAUSTED_MID,
        base.PARCHMENT_LIGHT: EXHAUSTED_LIGHT,
        base.WATER_DARK: COOLANT_DARK,
        base.WATER_MID: COOLANT_MID,
        base.WATER_LIGHT: COOLANT_LIGHT,
        base.WATER_GLINT: COOLANT_GLINT,
    }
    if pixel in exact:
        return exact[pixel]
    value = (red * 3 + green * 5 + blue * 2) // 10
    if blue >= red + 14:
        ramp = (ICE_DARK, ICE_MID, ICE_LIGHT, ICE_PALE)
    elif red >= blue + 24:
        ramp = (AMBER_DARK, AMBER_MID, AMBER_LIGHT)
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
        draw.line((0, y + 1, 15, y + 1), fill=STONE_LIGHT)
    offsets = ((3, 11, 7), (8, 2, 13), (12, 5, 2))
    for row, start_y in enumerate((0, 6, 12)):
        x = offsets[variant % 3][row]
        draw.line((x, start_y, x, min(15, start_y + 4)), fill=STONE_SHADOW)
    frost_marks = ((1, 2, 5, 2), (9, 8, 13, 8), (4, 14, 7, 14))
    left, y, right, _ = frost_marks[variant % 3]
    draw.line((left, y, right, y), fill=ICE_LIGHT)
    draw.point((right + 1, y), fill=ICE_PALE)
    for _ in range(4):
        x, y = rng.randrange(1, 15), rng.randrange(1, 15)
        draw.point((x, y), fill=rng.choice((STONE_DARK, FROST_MID, ICE_DARK)))
    return image


def draw_cooling_channel(background: Image.Image, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    if alt:
        draw.rectangle((2, 0, 6, 15), fill=METAL_DARK)
        draw.line((3, 0, 3, 15), fill=COOLANT_LIGHT)
        draw.line((5, 0, 5, 15), fill=ICE_PALE)
    else:
        draw.rectangle((0, 9, 15, 13), fill=METAL_DARK)
        draw.line((0, 10, 15, 10), fill=COOLANT_LIGHT)
        draw.line((0, 12, 15, 12), fill=ICE_PALE)
    draw.point((12, 3), fill=AMBER_LIGHT)
    draw.point((13, 3), fill=AMBER_DARK)
    return image


def draw_low_crystals(background: Image.Image, seed: int, alt: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw(background)
    clusters = ((2, 13, 4), (6, 11, 6), (10, 14, 5), (13, 10, 4)) if not alt else ((1, 10, 5), (5, 14, 4), (9, 11, 7), (13, 14, 6))
    for order, (x, bottom, height) in enumerate(clusters):
        color = ICE_LIGHT if order % 2 else ICE_MID
        draw.polygon(((x, bottom), (x + 1, bottom - height), (x + 2, bottom)), fill=color)
        draw.line((x + 1, bottom - height, x + 1, bottom - 1), fill=ICE_PALE)
    for _ in range(3):
        draw.point((rng.randrange(2, 14), rng.randrange(2, 14)), fill=ICE_GLINT)
    return image


def draw_frost_motes(background: Image.Image, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    for order, (x, y) in enumerate(((3, 12), (6, 7), (9, 10), (12, 4), (14, 13))):
        draw.point((x, y), fill=ICE_GLINT if order % 2 else ICE_PALE)
        if (order + int(alt)) % 2:
            draw.point((x, max(0, y - 1)), fill=ICE_LIGHT)
    return image


def draw_metal_floor(background: Image.Image, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((0, 0, 15, 15), fill=METAL_MID)
    for x in range(0, 16, 4):
        draw.line((x, 0, x, 15), fill=METAL_DARK)
        if x < 15:
            draw.line((x + 1, 0, x + 1, 15), fill=METAL_LIGHT)
    y = 5 if not alt else 11
    draw.line((0, y, 15, y), fill=OUTLINE)
    draw.point((13, 2 if not alt else 8), fill=AMBER_LIGHT)
    return image


def draw_cold_shaft(seed: int, edge: str) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=VOID)
    draw.rectangle((1, 4, 14, 15), fill=FOG_DARK)
    for left, y, right in ((2, 7, 9), (7, 11, 14), (1, 14, 6)):
        draw.line((left, y, right, y), fill=FOG_MID)
        draw.point((right, y - 1), fill=FOG_LIGHT)
    for _ in range(4):
        draw.point((rng.randrange(2, 14), rng.randrange(4, 15)), fill=ICE_DARK)
    if edge == "stone":
        draw.line((0, 0, 15, 0), fill=STONE_EDGE)
        draw.line((0, 1, 15, 1), fill=STONE_DARK)
    elif edge == "metal":
        draw.line((0, 0, 15, 0), fill=METAL_LIGHT)
        draw.line((0, 1, 15, 1), fill=METAL_DARK)
    elif edge == "wall":
        draw.rectangle((0, 0, 15, 3), fill=STONE_DARK)
        draw.line((0, 0, 15, 0), fill=STONE_EDGE)
    elif edge == "water":
        draw.line((0, 0, 15, 0), fill=COOLANT_GLINT)
        draw.line((0, 1, 15, 1), fill=COOLANT_DARK)
    return image


def draw_archive_wall(deco: bool = False, cabinet: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=STONE_SHADOW)
    draw.rectangle((1, 1, 14, 9), fill=METAL_DARK)
    draw.rectangle((2, 2, 13, 8), fill=ICE_DARK)
    draw.rectangle((3, 3, 12, 7), fill=ICE_MID)
    frame_x = 6 if alt else 9
    draw.line((frame_x, 1, frame_x, 9), fill=METAL_LIGHT)
    draw.line((2, 2, 6, 2), fill=ICE_PALE)
    draw.rectangle((0, 10, 15, 15), fill=STONE_MID)
    draw.line((0, 10, 15, 10), fill=STONE_EDGE)
    draw.point((13, 12), fill=AMBER_LIGHT)
    draw.point((12, 12), fill=AMBER_LIGHT)
    draw.point((11, 12), fill=AMBER_DARK)
    if deco:
        draw.line((2, 9, 5, 3), fill=FROST_LIGHT)
        draw.line((5, 3, 8, 1), fill=ICE_GLINT)
        draw.line((10, 8, 13, 5), fill=ICE_PALE)
    if cabinet:
        draw.rectangle((2, 3, 13, 14), fill=METAL_DARK)
        draw.rectangle((3, 4, 12, 13), fill=METAL_MID)
        for y in (6, 9, 12):
            draw.line((3, y, 12, y), fill=OUTLINE)
        for x in (6, 9):
            draw.line((x, 4, x, 13), fill=METAL_DARK)
        for x, y in ((5, 5), (8, 8), (11, 11)):
            draw.point((x, y), fill=ICE_LIGHT)
        draw.point((11, 5), fill=AMBER_LIGHT)
        draw.point((10, 5), fill=AMBER_DARK)
    return image


def composite_base_object(source: Image.Image, source_floor: Image.Image, background: Image.Image) -> Image.Image:
    result = background.copy()
    recolored = recolor_image(source)
    for y in range(T):
        for x in range(T):
            if source.getpixel((x, y)) != source_floor.getpixel((x, y)):
                result.putpixel((x, y), recolored.getpixel((x, y)))
    return result


def draw_door(background: Image.Image, kind: str, sideways: bool = False) -> Image.Image:
    source_floor = base.draw_slate_floor(1)
    source = base.draw_door(source_floor, kind, sideways=sideways)
    return composite_base_object(source, source_floor, background)


def draw_locked_exit(background: Image.Image, locked: bool = False) -> Image.Image:
    source_floor = base.draw_slate_floor(1)
    source = base.draw_locked_exit(source_floor, locked=locked)
    return composite_base_object(source, source_floor, background)


def draw_cryo_table(background: Image.Image, raised: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    top = 7 if not raised else 4
    draw.rectangle((2, top, 13, top + 2), fill=METAL_DARK)
    draw.line((2, top, 13, top), fill=METAL_LIGHT)
    draw.rectangle((3, top + 3, 4, 14), fill=METAL_MID)
    draw.rectangle((11, top + 3, 12, 14), fill=METAL_MID)
    draw.ellipse((4, top - 4, 8, top), fill=ICE_DARK, outline=ICE_PALE)
    draw.line((9, top - 5, 9, top), fill=METAL_LIGHT)
    draw.line((9, top - 4, 12, top - 4), fill=COOLANT_LIGHT)
    draw.point((5, top - 3), fill=ICE_GLINT)
    draw.point((12, top - 2), fill=AMBER_LIGHT)
    draw.point((11, top - 2), fill=AMBER_LIGHT)
    draw.point((10, top - 2), fill=AMBER_DARK)
    return image


def draw_broken_cabinet(background: Image.Image, raised: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    top = 7 if not raised else 4
    draw.rectangle((2, top, 12, 14), fill=METAL_DARK)
    draw.rectangle((3, top + 1, 10, 12), fill=METAL_MID)
    draw.line((6, top + 1, 6, 12), fill=OUTLINE)
    draw.line((10, top + 1, 14, top - 2), fill=METAL_LIGHT)
    draw.line((4, top, 7, top - 4), fill=ICE_LIGHT)
    draw.point((5, top - 3), fill=ICE_GLINT)
    draw.point((4, 13), fill=AMBER_LIGHT)
    draw.point((3, 13), fill=AMBER_DARK)
    return image


def crystal_shapes(alt: bool = False) -> tuple[tuple[int, int, int], ...]:
    return ((2, 15, 8), (5, 15, 12), (8, 15, 9), (11, 15, 13), (14, 15, 7)) if not alt else ((1, 15, 10), (4, 15, 7), (7, 15, 13), (10, 15, 8), (13, 15, 11))


def draw_crystal_growth(background: Image.Image, exhausted: bool = False, alt: bool = False, raised: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    dark, mid, light = ((EXHAUSTED_DARK, EXHAUSTED_MID, EXHAUSTED_LIGHT)
                        if exhausted else (ICE_DARK, ICE_LIGHT, ICE_PALE))
    for order, (x, bottom, height) in enumerate(crystal_shapes(alt)):
        adjusted_height = max(4, height - (3 if raised else 0))
        top = bottom - adjusted_height
        left = max(0, x - 1)
        right = min(15, x + 1)
        draw.polygon(((left, bottom), (x, top), (right, bottom)), fill=mid)
        draw.line((x, top, x, bottom - 1), fill=light)
        draw.point((left, bottom - 1), fill=dark)
        if not exhausted and order % 2 == 0:
            draw.point((x, max(0, top - 1)), fill=ICE_GLINT)
    return image


def draw_crystal_overhang(exhausted: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    dark, mid, light = ((EXHAUSTED_DARK, EXHAUSTED_MID, EXHAUSTED_LIGHT)
                        if exhausted else (ICE_DARK, ICE_LIGHT, ICE_PALE))
    for order, (x, _, height) in enumerate(crystal_shapes(alt)):
        top = max(2, 15 - height + order % 2)
        draw.polygon(((max(0, x - 1), 15), (x, top), (min(15, x + 1), 15)), fill=mid)
        draw.line((x, top, x, 14), fill=light)
        draw.point((max(0, x - 1), 14), fill=dark)
    return image


def draw_crystal_underhang(exhausted: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    color = EXHAUSTED_MID if exhausted else ICE_LIGHT
    for order, (x, _, _) in enumerate(crystal_shapes(alt)):
        draw.line((x, 0, x + (1 if order % 2 else -1), 2), fill=color)
    return image


def draw_frozen_specimen(background: Image.Image, body_only: bool = False, core: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((4, 10, 11, 14), fill=METAL_DARK)
    draw.line((3, 10, 12, 10), fill=METAL_LIGHT)
    draw.point((10, 13), fill=AMBER_LIGHT)
    draw.point((9, 13), fill=AMBER_LIGHT)
    draw.point((8, 13), fill=AMBER_DARK)
    if not body_only:
        draw.polygon(((8, 1), (12, 6), (10, 10), (5, 10), (3, 6)), fill=ICE_DARK)
        draw.polygon(((8, 2), (10, 6), (8, 9), (5, 6)), fill=ICE_MID)
        draw.line((7, 2, 7, 8), fill=ICE_PALE)
        if core:
            draw.rectangle((7, 5, 8, 7), fill=AMBER_LIGHT)
        else:
            draw.line((8, 5, 8, 8), fill=FROST_SHADOW)
            draw.point((7, 4), fill=FROST_LIGHT)
    return image


def draw_preservation_pod(background: Image.Image, body_only: bool = False, cracked: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((4, 5, 11, 14), fill=METAL_DARK)
    draw.rectangle((5, 5, 10, 13), fill=ICE_DARK)
    draw.rectangle((6, 6, 9, 12), fill=COOLANT_MID)
    draw.line((4, 14, 11, 14), fill=METAL_LIGHT)
    draw.point((10, 13), fill=AMBER_LIGHT)
    draw.point((9, 13), fill=AMBER_DARK)
    if not body_only:
        draw.ellipse((5, 2, 10, 7), fill=ICE_MID, outline=ICE_PALE)
        draw.point((6, 3), fill=ICE_GLINT)
        if cracked:
            draw.line((8, 3, 7, 6), fill=ICE_GLINT)
            draw.line((7, 6, 9, 8), fill=ICE_GLINT)
    return image


def draw_coolant_pump(background: Image.Image, body_only: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((3, 7, 12, 14), fill=METAL_DARK)
    draw.rectangle((4, 8, 11, 13), fill=METAL_MID)
    draw.ellipse((5, 9, 9, 13), fill=COOLANT_DARK, outline=COOLANT_LIGHT)
    draw.point((10, 9), fill=AMBER_LIGHT)
    draw.point((11, 9), fill=AMBER_DARK)
    if not body_only:
        draw.line((4, 7, 4, 3), fill=METAL_LIGHT)
        draw.line((4, 3, 11, 3), fill=COOLANT_LIGHT)
        draw.line((11, 3, 11, 7), fill=METAL_LIGHT)
        if alt:
            draw.line((7, 3, 7, 0), fill=ICE_PALE)
    return image


def draw_object_overhang(kind: str, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    if kind == "table":
        draw.line((8, 15, 8, 8), fill=METAL_LIGHT)
        draw.ellipse((5, 7, 10, 12), fill=ICE_DARK, outline=ICE_PALE)
        draw.point((6, 8), fill=ICE_GLINT)
        draw.point((11, 12), fill=AMBER_LIGHT)
    elif kind == "cabinet":
        draw.line((5, 15, 7, 9), fill=METAL_LIGHT)
        draw.line((7, 9, 10, 7), fill=ICE_PALE)
        draw.point((4, 13), fill=AMBER_LIGHT)
    elif kind == "specimen":
        draw.polygon(((8, 4), (12, 10), (9, 15), (5, 15), (3, 10)), fill=ICE_DARK)
        draw.line((7, 5, 7, 14), fill=ICE_PALE)
        draw.point((9, 10), fill=AMBER_LIGHT if alt else ICE_GLINT)
    elif kind == "pod":
        draw.ellipse((5, 5, 10, 13), fill=ICE_MID, outline=ICE_PALE)
        draw.line((5, 13, 5, 15), fill=METAL_LIGHT)
        draw.line((10, 13, 10, 15), fill=METAL_LIGHT)
        draw.point((6, 6), fill=ICE_GLINT)
    elif kind == "pump":
        draw.line((4, 15, 4, 7), fill=METAL_LIGHT)
        draw.line((4, 7, 11, 7), fill=COOLANT_LIGHT)
        draw.line((11, 7, 11, 15), fill=METAL_LIGHT)
        if alt:
            draw.line((7, 7, 7, 3), fill=ICE_PALE)
    return image


def build_water_texture() -> Image.Image:
    image = Image.new("RGBA", (32, 32), COOLANT_MID)
    draw = ImageDraw.Draw(image)
    rng = random.Random(614207)
    for left, y, right, color in (
        (2, 5, 11, COOLANT_LIGHT),
        (16, 8, 27, COOLANT_GLINT),
        (5, 15, 14, COOLANT_DARK),
        (18, 21, 29, COOLANT_LIGHT),
        (3, 27, 10, ICE_PALE),
    ):
        draw.line((left, y, right, y), fill=color)
    for _ in range(20):
        x, y = rng.randrange(1, 31), rng.randrange(1, 31)
        draw.point((x, y), fill=rng.choice((ICE_DARK, ICE_LIGHT, ICE_PALE, FROST_LIGHT)))
    for x in range(32):
        image.putpixel((x, 31), image.getpixel((x, 0)))
    for y in range(32):
        image.putpixel((31, y), image.getpixel((0, y)))
    return image


def build_tileset() -> Image.Image:
    sewers = Image.open(ENV / "tiles_sewers.png").convert("RGBA")
    atlas = recolor_image(base.build_tileset())
    floor = draw_floor(614200, 0)
    floor_alt_1 = draw_floor(614206, 1)
    floor_alt_2 = draw_floor(614212, 2)
    metal = draw_metal_floor(floor)
    metal_alt = draw_metal_floor(floor_alt_1, alt=True)

    floors = {
        0: floor,
        1: draw_cooling_channel(floor),
        2: draw_low_crystals(floor, 614202),
        3: draw_frost_motes(floor),
        4: metal,
        6: floor_alt_1,
        7: draw_cooling_channel(floor_alt_1, alt=True),
        8: draw_low_crystals(floor_alt_2, 614208, alt=True),
        9: draw_frost_motes(floor_alt_1, alt=True),
        10: metal_alt,
        12: floor_alt_2,
    }
    for index in range(24):
        replace_tile(atlas, floors.get(index, floor), index)

    replace_tile(atlas, base.draw_stair(sewers, 17, floor, STONE_RAMP), 16)
    replace_tile(atlas, base.draw_stair(sewers, 16, floor, STONE_RAMP), 17)
    replace_tile(atlas, tile(sewers, 18), 18)
    replace_tile(atlas, tile(sewers, 19), 19)
    replace_tile(atlas, draw_frozen_specimen(floor, core=True), 20)
    replace_tile(atlas, base.draw_stair(sewers, 17, metal, METAL_RAMP), 22)

    for offset, edge in enumerate(("plain", "stone", "metal", "wall", "water", "plain", "stone", "metal")):
        replace_tile(atlas, draw_cold_shaft(615000 + offset, edge), 24 + offset)

    water = build_water_texture()
    for index in range(32, 48):
        replace_tile(atlas, base.draw_water_transition(sewers, index, floor, water), index)

    for index, image in {
        48: draw_archive_wall(),
        49: draw_archive_wall(deco=True),
        50: draw_archive_wall(cabinet=True),
        52: draw_archive_wall(alt=True),
        53: draw_archive_wall(deco=True, alt=True),
        54: draw_archive_wall(cabinet=True, alt=True),
        62: draw_archive_wall(deco=True, alt=True),
        63: draw_archive_wall(cabinet=True),
    }.items():
        replace_tile(atlas, image, index)

    for index, kind in {56: "closed", 57: "open", 58: "locked", 59: "crystal"}.items():
        replace_tile(atlas, draw_door(floor, kind), index)
    replace_tile(atlas, draw_locked_exit(floor), 60)
    replace_tile(atlas, draw_locked_exit(floor, locked=True), 61)

    flat_objects = {
        64: draw_cryo_table(floor),
        65: draw_broken_cabinet(floor),
        66: draw_crystal_growth(floor),
        67: draw_crystal_growth(floor, exhausted=True),
        69: draw_crystal_growth(floor, alt=True),
        70: draw_crystal_growth(floor, exhausted=True, alt=True),
        72: draw_frozen_specimen(floor),
        73: draw_frozen_specimen(metal, core=True),
        74: draw_preservation_pod(floor),
        75: draw_coolant_pump(metal),
        76: draw_preservation_pod(floor, cracked=True),
        77: draw_coolant_pump(floor, alt=True),
        78: draw_frozen_specimen(floor, core=True),
    }
    for index, image in flat_objects.items():
        replace_tile(atlas, image, index)

    for index, kind in {
        112: "closed", 113: "open", 114: "locked", 115: "crystal", 116: "closed"
    }.items():
        replace_tile(atlas, draw_door(floor, kind, sideways=index == 116), index)

    raised_objects = {
        120: draw_cryo_table(floor, raised=True),
        121: draw_broken_cabinet(floor, raised=True),
        122: draw_crystal_growth(floor, raised=True),
        123: draw_crystal_growth(floor, exhausted=True, raised=True),
        125: draw_crystal_growth(floor, alt=True, raised=True),
        126: draw_crystal_growth(floor, exhausted=True, alt=True, raised=True),
        128: draw_frozen_specimen(floor, body_only=True),
        129: draw_frozen_specimen(metal, body_only=True, core=True),
        130: draw_preservation_pod(floor, body_only=True),
        131: draw_coolant_pump(metal, body_only=True),
        132: draw_preservation_pod(floor, body_only=True, cracked=True),
        133: draw_coolant_pump(floor, body_only=True, alt=True),
        134: draw_frozen_specimen(floor, body_only=True, core=True),
    }
    for index, image in raised_objects.items():
        replace_tile(atlas, image, index)

    for index, args in {
        224: ("closed", False),
        225: ("open", False),
        226: ("crystal", False),
        227: ("open", True),
        228: ("locked", True),
        229: ("crystal", True),
    }.items():
        kind, sideways = args
        replace_tile(atlas, recolor_image(base.draw_door(floor, kind, sideways=sideways, overhang=True)), index)
    replace_tile(atlas, recolor_image(base.draw_exit_underhang()), 230)

    overhangs = {
        232: draw_object_overhang("table"),
        233: draw_object_overhang("cabinet"),
        234: draw_crystal_overhang(),
        235: draw_crystal_overhang(exhausted=True),
        237: draw_crystal_overhang(alt=True),
        238: draw_crystal_overhang(exhausted=True, alt=True),
        240: draw_object_overhang("specimen"),
        241: draw_object_overhang("specimen", alt=True),
        242: draw_object_overhang("pod"),
        243: draw_object_overhang("pump"),
        244: draw_object_overhang("pod", alt=True),
        245: draw_object_overhang("pump", alt=True),
        246: draw_object_overhang("specimen", alt=True),
        250: draw_crystal_underhang(),
        251: draw_crystal_underhang(exhausted=True),
        253: draw_crystal_underhang(alt=True),
        254: draw_crystal_underhang(exhausted=True, alt=True),
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
