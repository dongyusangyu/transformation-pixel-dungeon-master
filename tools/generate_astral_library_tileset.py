from __future__ import annotations

import hashlib
import random
from pathlib import Path

from PIL import Image, ImageDraw

try:
    from tools.tower_tileset_semantics import normalize_tower_tileset
except ImportError:
    from tower_tileset_semantics import normalize_tower_tileset


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
OUT = ENV / "tiles_astral_library.png"
WATER_OUT = ENV / "water_astral_library.png"

T = 16
COLS = 16
ATLAS_SIZE = T * COLS

TRANSPARENT = (0, 0, 0, 0)
INK = (17, 18, 31, 255)
VOID = (10, 12, 24, 255)

STONE_SHADOW = (30, 30, 48, 255)
STONE_DARK = (39, 39, 59, 255)
STONE = (53, 52, 76, 255)
STONE_LIGHT = (72, 70, 96, 255)
STONE_EDGE = (91, 87, 112, 255)

WOOD_DARK = (45, 29, 36, 255)
WOOD = (76, 45, 45, 255)
WOOD_LIGHT = (112, 68, 52, 255)

BRASS_DARK = (89, 64, 35, 255)
BRASS = (146, 108, 51, 255)
BRASS_LIGHT = (204, 166, 79, 255)

CYAN_DARK = (27, 67, 87, 255)
CYAN = (49, 125, 146, 255)
CYAN_LIGHT = (103, 192, 191, 255)
STAR_WHITE = (185, 224, 213, 255)
VIOLET = (94, 69, 132, 255)
VIOLET_LIGHT = (136, 101, 167, 255)

PARCHMENT_DARK = (103, 82, 62, 255)
PARCHMENT = (165, 139, 96, 255)
PARCHMENT_LIGHT = (211, 189, 135, 255)

WATER_DARK = (13, 30, 56, 255)
WATER_MID = (20, 49, 82, 255)
WATER_LIGHT = (35, 82, 116, 255)
WATER_GLINT = (72, 153, 163, 255)

STONE_RAMP = (INK, STONE_SHADOW, STONE_DARK, STONE, STONE_LIGHT, STONE_EDGE)
WOOD_RAMP = (INK, WOOD_DARK, WOOD, WOOD_LIGHT, BRASS_DARK, BRASS)
BRASS_RAMP = (INK, BRASS_DARK, BRASS, BRASS_LIGHT, CYAN, CYAN_LIGHT)


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


def paste_tile(atlas: Image.Image, source: Image.Image, source_index: int, index: int) -> None:
    replace_tile(atlas, tile(source, source_index), index)


def local_draw(background: Image.Image | None = None) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = background.copy() if background is not None else Image.new("RGBA", (T, T), TRANSPARENT)
    return image, ImageDraw.Draw(image)


def draw_slate_floor(seed: int, variant: int = 0) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=STONE)
    courses = {
        0: (5, 11),
        1: (4, 10),
        2: (6, 12),
    }[variant]
    for y in courses:
        draw.line((0, y, 15, y), fill=STONE_SHADOW)
        if y < 15:
            draw.line((0, y + 1, 15, y + 1), fill=STONE_LIGHT)
    starts = (0,) + tuple(y + 1 for y in courses)
    for row, y in enumerate(starts):
        bottom = next((course - 1 for course in courses if course >= y), 15)
        x = (3 + row * 6 + variant * 3) % 13 + 1
        draw.line((x, y, x, min(bottom, y + 4)), fill=STONE_DARK)
    for _ in range(7):
        x, y = rng.randrange(1, 15), rng.randrange(1, 15)
        draw.point((x, y), fill=rng.choice((STONE_DARK, STONE_LIGHT, STONE_EDGE)))
    if variant == 2:
        draw.line((11, 2, 9, 4), fill=STONE_SHADOW)
        draw.point((8, 4), fill=STONE_SHADOW)
    return image


def draw_constellation_floor(background: Image.Image, seed: int, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((1, 0, 14, 15), fill=(35, 31, 59, 255))
    draw.line((1, 0, 1, 15), fill=BRASS_DARK)
    draw.line((14, 0, 14, 15), fill=BRASS_DARK)
    points = (
        ((3, 3), (7, 5), (11, 2), (12, 9), (6, 12), (3, 9))
        if not alt
        else ((4, 2), (10, 4), (12, 8), (9, 12), (4, 13), (2, 7))
    )
    for left, right in zip(points, points[1:]):
        draw.line((left[0], left[1], right[0], right[1]), fill=BRASS_DARK)
    for order, point in enumerate(points):
        draw.point(point, fill=CYAN_LIGHT if order % 2 else STAR_WHITE)
        accent_x = min(15, point[0] + (1 if order % 2 else -1))
        draw.point((accent_x, point[1]), fill=BRASS_LIGHT)
    return image


def draw_pages_floor(background: Image.Image, seed: int, alt: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw(background)
    pages = (
        ((2, 4, 5, 5), (9, 3, 12, 4), (5, 11, 8, 12), (11, 13, 13, 14))
        if not alt
        else ((3, 2, 6, 3), (10, 6, 13, 7), (2, 12, 4, 13), (7, 10, 10, 11))
    )
    for order, box in enumerate(pages):
        draw.rectangle(box, fill=PARCHMENT_DARK)
        draw.line((box[0], box[1], box[2] - 1, box[1]), fill=PARCHMENT_LIGHT)
        if order % 2:
            draw.point((box[2], box[3]), fill=BRASS)
    for _ in range(5):
        x, y = rng.randrange(2, 14), rng.randrange(2, 14)
        draw.point((x, y), fill=rng.choice((CYAN_DARK, CYAN, CYAN_LIGHT)))
    return image


def draw_starfire_floor(background: Image.Image, alt: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.ellipse((4, 8, 11, 13), fill=INK, outline=BRASS_DARK)
    draw.rectangle((5, 9, 10, 11), fill=CYAN_DARK)
    flame = CYAN_LIGHT if not alt else VIOLET_LIGHT
    draw.point((6, 8), fill=flame)
    draw.line((8, 5, 8, 9), fill=flame)
    draw.point((9, 7), fill=STAR_WHITE)
    draw.point((7, 6), fill=CYAN)
    return image


def draw_wood_floor(seed: int, alt: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=WOOD)
    courses = (4, 9, 14) if not alt else (3, 8, 13)
    for y in courses:
        draw.line((0, y, 15, y), fill=WOOD_DARK)
        if y < 15:
            draw.line((0, y + 1, 15, y + 1), fill=WOOD_LIGHT)
    for row, start in enumerate((0,) + tuple(y + 1 for y in courses)):
        x = (seed + row * 7) % 13 + 1
        draw.line((x, start, x, min(15, start + 3)), fill=WOOD_DARK)
    for _ in range(4):
        draw.point((rng.randrange(2, 14), rng.randrange(1, 15)), fill=WOOD_LIGHT)
    return image


def recolor_structure(
        source: Image.Image,
        ramp: tuple[tuple[int, int, int, int], ...]) -> Image.Image:
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


def draw_stair(
        sewers: Image.Image,
        source_index: int,
        background: Image.Image,
        ramp: tuple[tuple[int, int, int, int], ...]) -> Image.Image:
    source = tile(sewers, source_index)
    source_floor = tile(sewers, 0)
    image = background.copy()
    recolored = recolor_structure(source, ramp)
    for y in range(T):
        for x in range(T):
            if source.getpixel((x, y)) != source_floor.getpixel((x, y)):
                pixel = recolored.getpixel((x, y))
                if pixel == image.getpixel((x, y)):
                    pixel = ramp[0]
                image.putpixel((x, y), pixel)
    return image


def draw_pedestal(background: Image.Image) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((4, 8, 11, 13), fill=STONE_DARK)
    draw.rectangle((3, 7, 12, 9), fill=BRASS_DARK)
    draw.line((4, 7, 11, 7), fill=BRASS_LIGHT)
    draw.ellipse((6, 4, 9, 7), fill=CYAN_DARK, outline=CYAN_LIGHT)
    draw.point((7, 4), fill=STAR_WHITE)
    return image


def draw_chasm(seed: int, edge: str = "plain") -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=VOID)
    for _ in range(10):
        draw.point(
            (rng.randrange(16), rng.randrange(16)),
            fill=rng.choice((INK, CYAN_DARK, VIOLET)),
        )
    if edge == "slate":
        draw.line((0, 0, 15, 0), fill=STONE_LIGHT)
        draw.line((0, 1, 15, 1), fill=STONE_DARK)
    elif edge == "wood":
        draw.line((0, 0, 15, 0), fill=WOOD_LIGHT)
        draw.line((0, 1, 15, 1), fill=WOOD_DARK)
    elif edge == "wall":
        draw.rectangle((0, 0, 15, 3), fill=STONE_DARK)
        draw.line((0, 0, 15, 0), fill=STONE_EDGE)
    elif edge == "water":
        draw.line((0, 0, 15, 0), fill=WATER_GLINT)
        draw.line((0, 1, 15, 1), fill=WATER_DARK)
    return image


def build_water_texture() -> Image.Image:
    image = Image.new("RGBA", (32, 32), WATER_MID)
    draw = ImageDraw.Draw(image)
    rng = random.Random(471103)
    for y in range(1, 31):
        for x in range(1, 31):
            if (x * 5 + y * 7) % 19 == 0:
                draw.point((x, y), fill=WATER_DARK)
    strokes = (
        (2, 5, 10, WATER_LIGHT),
        (15, 8, 25, WATER_GLINT),
        (5, 15, 13, WATER_DARK),
        (18, 20, 29, WATER_LIGHT),
        (3, 26, 9, WATER_GLINT),
        (12, 29, 22, WATER_DARK),
    )
    for left, y, right, color in strokes:
        draw.line((left, y, right, y), fill=color)
        if color == WATER_GLINT:
            draw.point((right + 1, y), fill=STAR_WHITE)
    for _ in range(18):
        x, y = rng.randrange(1, 31), rng.randrange(1, 31)
        draw.point((x, y), fill=rng.choice((CYAN_DARK, CYAN, CYAN_LIGHT)))
    for x in range(32):
        image.putpixel((x, 31), image.getpixel((x, 0)))
    for y in range(32):
        image.putpixel((31, y), image.getpixel((0, y)))
    return image


def draw_water_transition(
        reference: Image.Image,
        index: int,
        floor: Image.Image,
        water: Image.Image) -> Image.Image:
    source = tile(reference, index)
    result = Image.new("RGBA", (T, T), TRANSPARENT)
    for y in range(T):
        for x in range(T):
            red, green, blue, alpha = source.getpixel((x, y))
            if alpha == 0:
                continue
            maximum = max(red, green, blue)
            minimum = min(red, green, blue)
            saturation = maximum - minimum
            water_edge = (
                green > red + 8 and green >= blue - 12 and maximum > 38
            ) or (
                green >= red and green >= blue - 18 and maximum <= 70 and saturation > 10
            )
            if water_edge:
                wr, wg, wb, _ = water.getpixel((x, y))
                result.putpixel((x, y), (wr, wg, wb, alpha))
            else:
                fr, fg, fb, _ = floor.getpixel((x, y))
                result.putpixel((x, y), (fr, fg, fb, alpha))
    return result


def draw_flat_wall(
        seed: int,
        deco: bool = False,
        shelf: bool = False,
        alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=STONE_SHADOW)
    draw.rectangle((0, 2, 15, 14), fill=STONE_DARK)
    for y in (5, 10, 14):
        draw.line((0, y, 15, y), fill=INK)
        draw.line((0, y - 1, 15, y - 1), fill=STONE)
    for row, y in enumerate((2, 6, 11)):
        offset = (seed + row * 5 + int(alt) * 3) % 8
        for x in range(offset, 16, 8):
            draw.line((x, y, x, min(14, y + 3)), fill=STONE_SHADOW)
    draw.line((0, 0, 15, 0), fill=BRASS_DARK)
    draw.line((0, 1, 15, 1), fill=WOOD_DARK)
    if deco:
        draw.rectangle((3, 4, 12, 12), fill=INK)
        draw.rectangle((4, 5, 11, 11), outline=BRASS_DARK)
        stars = ((5, 9), (7, 6), (10, 8), (9, 11))
        for first, second in zip(stars, stars[1:]):
            draw.line((first[0], first[1], second[0], second[1]), fill=VIOLET)
        for point in stars:
            draw.point(point, fill=CYAN_LIGHT)
    if shelf:
        draw.rectangle((1, 3, 14, 14), fill=WOOD_DARK)
        for y in (7, 11):
            draw.line((1, y, 14, y), fill=WOOD_LIGHT)
        book_colors = (PARCHMENT, WOOD, VIOLET, CYAN_DARK, BRASS)
        for row, y in enumerate((4, 8, 12)):
            for x in range(2 + row % 2, 14, 3):
                color = book_colors[(x + y + seed) % len(book_colors)]
                draw.rectangle((x, y, min(13, x + 1), min(14, y + 2)), fill=color)
        draw.point((12, 5), fill=CYAN_LIGHT)
    return image


def draw_door(
        background: Image.Image,
        kind: str,
        sideways: bool = False,
        overhang: bool = False) -> Image.Image:
    image, draw = local_draw(None if overhang else background)
    if overhang:
        if sideways:
            draw.rectangle((1, 8, 14, 15), fill=WOOD_DARK)
            draw.line((2, 8, 13, 8), fill=BRASS_DARK)
            if kind == "locked":
                draw.rectangle((6, 12, 9, 15), fill=BRASS)
            elif kind == "crystal":
                draw.rectangle((6, 11, 9, 15), fill=CYAN)
            return image
        draw.rectangle((2, 8, 13, 15), fill=WOOD_DARK)
        draw.line((2, 8, 13, 8), fill=BRASS_DARK)
        if kind == "open":
            draw.rectangle((3, 10, 5, 15), fill=WOOD)
            draw.rectangle((10, 10, 12, 15), fill=WOOD)
        elif kind == "crystal":
            draw.rectangle((5, 11, 10, 15), fill=CYAN_DARK)
        return image
    if sideways:
        draw.rectangle((1, 3, 14, 13), fill=WOOD_DARK)
        draw.line((1, 3, 14, 3), fill=BRASS_DARK)
        if kind == "open":
            draw.rectangle((3, 5, 12, 13), fill=INK)
        else:
            draw.rectangle((3, 5, 12, 13), fill=WOOD)
        return image
    draw.rectangle((2, 2, 13, 14), fill=WOOD_DARK)
    draw.line((2, 2, 13, 2), fill=BRASS_DARK)
    if kind == "open":
        draw.rectangle((4, 4, 11, 14), fill=INK)
        draw.rectangle((2, 4, 4, 14), fill=WOOD)
        draw.rectangle((11, 4, 13, 14), fill=WOOD)
    else:
        fill = CYAN_DARK if kind == "crystal" else WOOD
        draw.rectangle((4, 3, 11, 14), fill=fill)
        draw.line((7, 3, 7, 14), fill=WOOD_DARK)
        for y in (6, 10):
            draw.point((5, y), fill=BRASS)
            draw.point((10, y), fill=BRASS)
    if kind == "locked":
        draw.rectangle((3, 8, 12, 9), fill=BRASS_DARK)
        draw.rectangle((6, 8, 9, 12), fill=BRASS)
        draw.point((7, 9), fill=BRASS_LIGHT)
    elif kind == "crystal":
        draw.polygon(((7, 5), (9, 8), (7, 12), (5, 8)), fill=CYAN)
        draw.point((7, 6), fill=CYAN_LIGHT)
    return image


def draw_locked_exit(background: Image.Image, locked: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    draw.rectangle((2, 4, 13, 14), fill=STONE_DARK)
    draw.rectangle((4, 5, 11, 13), fill=INK)
    for y, inset in ((7, 0), (9, 1), (11, 2), (13, 3)):
        draw.line((4 + inset, y, 11 - inset, y), fill=STONE_LIGHT)
    draw.line((2, 4, 13, 4), fill=BRASS_DARK)
    if locked:
        draw.rectangle((4, 8, 11, 9), fill=BRASS)
        draw.polygon(((7, 8), (9, 10), (7, 13), (5, 10)), fill=CYAN)
    return image


def draw_exit_underhang() -> Image.Image:
    image, draw = local_draw()
    draw.rectangle((2, 0, 13, 4), fill=STONE_DARK)
    draw.line((3, 0, 12, 0), fill=BRASS_DARK)
    draw.rectangle((4, 1, 11, 5), fill=INK)
    draw.line((5, 2, 10, 2), fill=STONE_LIGHT)
    return image


def draw_observatory_table(background: Image.Image, raised: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    if raised:
        draw.line((3, 0, 3, 13), fill=WOOD_DARK)
        draw.line((12, 0, 12, 13), fill=WOOD_DARK)
        draw.rectangle((3, 0, 12, 4), fill=WOOD)
        draw.line((4, 0, 11, 0), fill=BRASS)
        draw.ellipse((5, -4, 10, 3), fill=CYAN_DARK, outline=BRASS_DARK)
        draw.point((7, 0), fill=CYAN_LIGHT)
    else:
        draw.rectangle((3, 8, 12, 12), fill=WOOD)
        draw.line((3, 8, 12, 8), fill=BRASS)
        draw.line((4, 12, 3, 15), fill=WOOD_DARK)
        draw.line((11, 12, 12, 15), fill=WOOD_DARK)
        draw.ellipse((5, 4, 10, 9), fill=CYAN_DARK, outline=BRASS_DARK)
        draw.point((7, 5), fill=CYAN_LIGHT)
        draw.line((5, 10, 10, 10), fill=PARCHMENT)
    return image


def draw_fallen_shelf(background: Image.Image, raised: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    if raised:
        draw.line((2, 0, 13, 7), fill=WOOD_LIGHT, width=2)
        draw.line((13, 0, 4, 8), fill=WOOD, width=2)
        draw.line((2, 0, 13, 0), fill=WOOD_DARK)
        draw.rectangle((2, 7, 5, 10), fill=PARCHMENT)
        draw.point((11, 5), fill=CYAN)
    else:
        draw.rectangle((2, 8, 13, 11), fill=WOOD_DARK)
        draw.line((3, 7, 12, 13), fill=WOOD_LIGHT, width=2)
        draw.line((12, 6, 4, 13), fill=WOOD, width=2)
        draw.rectangle((2, 11, 5, 13), fill=PARCHMENT)
        draw.point((11, 12), fill=CYAN)
    return image


def page_stems(seed: int, scattered: bool = False) -> tuple[tuple[int, int, int], ...]:
    rng = random.Random(seed)
    result = []
    positions = (3, 8, 13) if scattered else (2, 5, 8, 11, 14)
    for order, x in enumerate(positions):
        height = rng.randrange(3, 7) if scattered else rng.randrange(7, 13)
        drift = (-1, 0, 1)[(seed + order) % 3]
        result.append((x, height, drift))
    return tuple(result)


def draw_page_pile(
        background: Image.Image,
        seed: int,
        scattered: bool = False,
        alt: bool = False,
        raised: bool = False) -> Image.Image:
    image = draw_pages_floor(background, seed + 90, alt)
    draw = ImageDraw.Draw(image)
    for order, (x, height, drift) in enumerate(page_stems(seed, scattered)):
        if raised:
            page_y = min(12, order * (4 if scattered else 3))
        else:
            page_y = max(1, 13 - height + order % 3)
        width = 3 if scattered else 4
        left = max(0, min(15 - width, x + drift - 1))
        color = PARCHMENT_LIGHT if (order + int(alt)) % 2 else PARCHMENT
        draw.rectangle((left, page_y, left + width, min(15, page_y + 1)), fill=color)
        if not scattered and page_y + 2 < T:
            draw.line((left + 1, page_y + 2, left + width - 1, page_y + 2),
                      fill=PARCHMENT_DARK)
        sparkle_y = max(0, page_y - 1) if order % 2 else min(15, page_y + 2)
        draw.point((x, sparkle_y), fill=CYAN_LIGHT if order % 2 else CYAN)
    return image


def draw_page_overhang(seed: int, scattered: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    for order, (x, _height, drift) in enumerate(page_stems(seed, scattered)):
        if order == 0:
            top = 14
        elif scattered:
            top = 9 + order * 3
        else:
            top = 2 + order * 3
        top = min(14, top)
        width = 3 if scattered else 4
        left = max(0, min(15 - width, x + drift - 1))
        color = PARCHMENT_LIGHT if (order + int(alt)) % 2 else PARCHMENT
        draw.rectangle((left, top, left + width, min(15, top + 1)), fill=color)
        if top < 14:
            draw.point((x, top + 2), fill=CYAN_DARK)
        draw.point((min(15, left + width), max(0, top - 1)), fill=CYAN_LIGHT)
    return image


def draw_page_underhang(seed: int, scattered: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    rng = random.Random(seed + 200)
    limit = 6 if scattered else 10
    for order, x in enumerate((2, 5, 8, 11, 14)):
        y = rng.randrange(1, max(2, limit - 2))
        color = PARCHMENT if (order + int(alt)) % 2 else PARCHMENT_LIGHT
        draw.rectangle((max(0, x - 1), y, min(15, x + 1), y + 1), fill=color)
        draw.point((x, min(15, y + 2)), fill=CYAN_DARK)
    return image


def draw_scholar_statue(background: Image.Image, body_only: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    if body_only:
        draw.rectangle((4, 0, 11, 10), fill=STONE_DARK)
        draw.polygon(((4, 0), (7, 4), (11, 0), (11, 10), (4, 10)), fill=STONE)
        draw.rectangle((5, 8, 10, 12), fill=STONE_DARK)
        draw.line((6, 9, 9, 9), fill=PARCHMENT)
    else:
        draw.polygon(((4, 3), (7, 1), (11, 3), (12, 8), (10, 11), (5, 11), (3, 8)),
                     fill=STONE_DARK)
        draw.rectangle((5, 4, 10, 9), fill=STONE)
        draw.point((6, 5), fill=CYAN_DARK)
        draw.point((9, 5), fill=CYAN_DARK)
        draw.line((5, 9, 10, 9), fill=PARCHMENT)
    draw.rectangle((3, 13, 12, 14), fill=STONE_SHADOW)
    draw.line((4, 13, 11, 13), fill=STONE_EDGE)
    return image


def draw_armillary(background: Image.Image, body_only: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    if body_only:
        draw.ellipse((3, -6, 12, 6), outline=BRASS, width=2)
        draw.line((7, 0, 7, 13), fill=BRASS_DARK)
        draw.line((4, 5, 11, 5), fill=BRASS)
        draw.rectangle((5, 12, 10, 14), fill=STONE_DARK)
    else:
        draw.ellipse((3, 2, 12, 11), outline=BRASS, width=2)
        draw.ellipse((5, 3, 10, 10), outline=BRASS_DARK)
        draw.line((7, 2, 7, 13), fill=BRASS_LIGHT)
        draw.line((3, 7, 12, 7), fill=BRASS)
        draw.ellipse((6, 5, 9, 8), fill=CYAN_DARK, outline=CYAN_LIGHT)
        draw.rectangle((5, 12, 10, 14), fill=STONE_DARK)
    return image


def draw_telescope(background: Image.Image, body_only: bool = False) -> Image.Image:
    image, draw = local_draw(background)
    if body_only:
        draw.line((5, 0, 11, 4), fill=BRASS, width=2)
        draw.rectangle((10, 2, 13, 5), fill=CYAN_DARK)
        draw.line((8, 4, 5, 14), fill=WOOD_DARK, width=2)
        draw.line((8, 4, 12, 14), fill=WOOD_DARK, width=2)
    else:
        draw.line((3, 5, 11, 8), fill=BRASS, width=2)
        draw.rectangle((10, 6, 13, 9), fill=CYAN_DARK)
        draw.point((13, 7), fill=CYAN_LIGHT)
        draw.line((8, 8, 5, 15), fill=WOOD_DARK, width=2)
        draw.line((8, 8, 12, 15), fill=WOOD_DARK, width=2)
    return image


def draw_crystal(background: Image.Image, seed: int, raised: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw(background)
    points = (
        ((2, 13), (5, 3), (7, 9), (10, 0), (14, 13))
        if raised
        else ((2, 13), (5, 7), (7, 11), (10, 4), (14, 13))
    )
    draw.polygon(points, fill=CYAN_DARK)
    draw.polygon(((4, 12), (5, 8), (8, 11), (10, 6), (12, 13)), fill=CYAN)
    for _ in range(4):
        draw.point((rng.randrange(4, 12), rng.randrange(6, 13)), fill=CYAN_LIGHT)
    return image


def draw_object_overhang(kind: str, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    if kind == "table":
        draw.ellipse((5, 10, 10, 17), fill=CYAN_DARK, outline=BRASS)
        draw.point((7, 11), fill=CYAN_LIGHT)
        draw.line((3, 15, 12, 15), fill=WOOD)
    elif kind == "shelf":
        draw.line((2, 12, 13, 15), fill=WOOD_DARK, width=2)
        draw.rectangle((3, 11, 6, 13), fill=PARCHMENT)
    elif kind == "statue":
        draw.polygon(((4, 15), (5, 10), (7, 8), (10, 10), (12, 15)), fill=STONE_DARK)
        draw.rectangle((5, 12, 10, 15), fill=STONE)
        draw.point((6, 13), fill=CYAN_DARK)
        draw.point((9, 13), fill=CYAN_DARK)
    elif kind == "armillary":
        draw.ellipse((3, 6, 12, 17), outline=BRASS, width=2)
        draw.ellipse((5, 8, 10, 16), outline=BRASS_DARK)
        draw.line((7, 7, 7, 15), fill=BRASS_LIGHT)
        draw.ellipse((6, 11, 9, 14), fill=CYAN_DARK, outline=CYAN_LIGHT)
    elif kind == "telescope":
        draw.line((3, 10, 11, 15), fill=BRASS, width=2)
        draw.rectangle((10, 12, 14, 15), fill=CYAN_DARK)
        draw.point((13, 13), fill=CYAN_LIGHT)
        draw.line((4, 12, 5, 15), fill=WOOD_DARK, width=2)
    elif kind == "crystal":
        draw.polygon(((2, 15), (5, 9), (7, 15), (10, 7), (14, 15)), fill=CYAN_DARK)
        draw.line((10, 8, 10, 14), fill=CYAN_LIGHT)
    return image


def add_wall_accents(image: Image.Image, seed: int) -> Image.Image:
    result = image.copy()
    accents = (
        (BRASS_DARK, ((4, 7), (11, 10))),
        (CYAN_DARK, ((5, 9), (10, 6))),
        (WOOD_DARK, ((3, 8), (12, 8))),
        (VIOLET, ((7, 6), (8, 10))),
    )
    color, points = accents[seed % len(accents)]
    for x, y in points:
        if result.getpixel((x, y))[3]:
            result.putpixel((x, y), color)
            if y + 1 < T and result.getpixel((x, y + 1))[3] and seed % 2:
                result.putpixel((x, y + 1), color)
    return result


def build_tileset() -> Image.Image:
    sewers = Image.open(ENV / "tiles_sewers.png").convert("RGBA")
    halls = Image.open(ENV / "tiles_halls.png").convert("RGBA")
    atlas = Image.new("RGBA", (ATLAS_SIZE, ATLAS_SIZE), TRANSPARENT)

    floor = draw_slate_floor(8100, 0)
    floor_alt_1 = draw_slate_floor(8106, 1)
    floor_alt_2 = draw_slate_floor(8112, 2)
    wood = draw_wood_floor(8104)
    wood_alt = draw_wood_floor(8110, alt=True)

    floors = {
        0: floor,
        1: draw_constellation_floor(floor, 8101),
        2: draw_pages_floor(floor_alt_1, 8102),
        3: draw_starfire_floor(floor),
        4: wood,
        6: floor_alt_1,
        7: draw_constellation_floor(floor_alt_1, 8107, alt=True),
        8: draw_pages_floor(floor_alt_2, 8108, alt=True),
        9: draw_starfire_floor(floor_alt_1, alt=True),
        10: wood_alt,
        12: floor_alt_2,
    }
    for index in range(24):
        replace_tile(atlas, floors.get(index, floor), index)

    replace_tile(atlas, draw_stair(sewers, 17, floor, STONE_RAMP), 16)
    replace_tile(atlas, draw_stair(sewers, 16, floor, STONE_RAMP), 17)
    paste_tile(atlas, sewers, 18, 18)
    paste_tile(atlas, sewers, 19, 19)
    replace_tile(atlas, draw_pedestal(floor), 20)
    replace_tile(atlas, draw_stair(sewers, 17, wood, BRASS_RAMP), 22)

    for offset, edge in enumerate(("plain", "slate", "wood", "wall", "water", "plain", "slate", "wood")):
        replace_tile(atlas, draw_chasm(8200 + offset, edge), 24 + offset)

    water = build_water_texture()
    for index in range(32, 48):
        replace_tile(atlas, draw_water_transition(sewers, index, floor, water), index)

    wall_slots = {
        48: draw_flat_wall(48),
        49: draw_flat_wall(49, deco=True),
        50: draw_flat_wall(50, shelf=True),
        52: draw_flat_wall(52, alt=True),
        53: draw_flat_wall(53, deco=True, alt=True),
        54: draw_flat_wall(54, shelf=True, alt=True),
    }
    for index in range(48, 56):
        replace_tile(atlas, wall_slots.get(index, wall_slots[48]), index)

    for index, kind in {56: "closed", 57: "open", 58: "locked", 59: "crystal"}.items():
        replace_tile(atlas, draw_door(floor, kind), index)
    replace_tile(atlas, draw_locked_exit(floor), 60)
    replace_tile(atlas, draw_locked_exit(floor, locked=True), 61)
    replace_tile(atlas, draw_flat_wall(62, deco=True), 62)
    replace_tile(atlas, draw_flat_wall(63, shelf=True, alt=True), 63)

    flat_objects = {
        64: draw_observatory_table(floor),
        65: draw_fallen_shelf(floor),
        66: draw_page_pile(floor, 8466),
        67: draw_page_pile(floor, 8467, scattered=True),
        69: draw_page_pile(floor, 8469, alt=True),
        70: draw_page_pile(floor, 8470, scattered=True, alt=True),
        72: draw_scholar_statue(floor),
        73: draw_scholar_statue(wood),
        74: draw_armillary(floor),
        75: draw_telescope(wood),
        76: draw_crystal(floor, 76),
        77: draw_crystal(floor, 77),
        78: draw_crystal(floor, 78),
    }
    for index in range(64, 80):
        replace_tile(atlas, flat_objects.get(index, floor), index)

    for index in range(80, 112):
        ramp = WOOD_RAMP if index in range(92, 96) or index in range(108, 112) else STONE_RAMP
        replace_tile(
            atlas,
            add_wall_accents(recolor_structure(tile(halls, index), ramp), 400 + index),
            index,
        )

    for index, kind in {
        112: "closed",
        113: "open",
        114: "locked",
        115: "crystal",
        116: "closed",
    }.items():
        replace_tile(atlas, draw_door(floor, kind, sideways=index == 116), index)
    for index in range(117, 120):
        replace_tile(atlas, recolor_structure(tile(halls, index), STONE_RAMP), index)

    raised_objects = {
        120: draw_observatory_table(floor, raised=True),
        121: draw_fallen_shelf(floor, raised=True),
        122: draw_page_pile(floor, 8466, raised=True),
        123: draw_page_pile(floor, 8467, scattered=True, raised=True),
        125: draw_page_pile(floor, 8469, alt=True, raised=True),
        126: draw_page_pile(floor, 8470, scattered=True, alt=True, raised=True),
        128: draw_scholar_statue(floor, body_only=True),
        129: draw_scholar_statue(wood, body_only=True),
        130: draw_armillary(floor, body_only=True),
        131: draw_telescope(wood, body_only=True),
        132: draw_crystal(floor, 132, raised=True),
        133: draw_crystal(floor, 133, raised=True),
        134: draw_crystal(floor, 134, raised=True),
    }
    for index in range(120, 144):
        replace_tile(atlas, raised_objects.get(index, floor), index)

    for index in range(144, 192):
        ramp = WOOD_RAMP if index >= 176 else STONE_RAMP
        replace_tile(
            atlas,
            add_wall_accents(recolor_structure(tile(halls, index), ramp), 600 + index),
            index,
        )

    for index in range(192, 224):
        ramp = WOOD_RAMP if 200 <= index < 204 else STONE_RAMP
        replace_tile(
            atlas,
            add_wall_accents(recolor_structure(tile(halls, index), ramp), 800 + index),
            index,
        )

    door_overhangs = {
        224: draw_door(floor, "closed", overhang=True),
        225: draw_door(floor, "open", overhang=True),
        226: draw_door(floor, "crystal", overhang=True),
        227: draw_door(floor, "open", sideways=True, overhang=True),
        228: draw_door(floor, "locked", sideways=True, overhang=True),
        229: draw_door(floor, "crystal", sideways=True, overhang=True),
        230: draw_exit_underhang(),
    }
    for index in range(224, 232):
        replace_tile(atlas, door_overhangs.get(index, Image.new("RGBA", (T, T), TRANSPARENT)), index)

    overhangs = {
        232: draw_object_overhang("table"),
        233: draw_object_overhang("shelf"),
        234: draw_page_overhang(8466),
        235: draw_page_overhang(8467, scattered=True),
        237: draw_page_overhang(8469, alt=True),
        238: draw_page_overhang(8470, scattered=True, alt=True),
        240: draw_object_overhang("statue"),
        241: draw_object_overhang("statue", alt=True),
        242: draw_object_overhang("armillary"),
        243: draw_object_overhang("telescope"),
        244: draw_object_overhang("crystal"),
        245: draw_object_overhang("crystal", alt=True),
        246: draw_object_overhang("crystal"),
        250: draw_page_underhang(8466),
        251: draw_page_underhang(8467, scattered=True),
        253: draw_page_underhang(8469, alt=True),
        254: draw_page_underhang(8470, scattered=True, alt=True),
    }
    for index in range(232, 256):
        replace_tile(atlas, overhangs.get(index, Image.new("RGBA", (T, T), TRANSPARENT)), index)

    return normalize_tower_tileset(atlas, sewers, halls, build_water_texture())


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
