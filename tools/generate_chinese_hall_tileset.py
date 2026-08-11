from __future__ import annotations

import hashlib
import random
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
OUT = ENV / "tiles_chinese_hall.png"
WATER_OUT = ENV / "water_chinese_hall.png"

T = 16
COLS = 16
ATLAS_SIZE = T * COLS

TRANSPARENT = (0, 0, 0, 0)
INK = (27, 27, 25, 255)
SHADOW = (42, 39, 35, 255)
BRICK_DARK = (87, 87, 79, 255)
BRICK = (112, 108, 94, 255)
BRICK_LIGHT = (129, 123, 105, 255)
WOOD_DARK = (58, 37, 29, 255)
WOOD = (104, 64, 43, 255)
WOOD_LIGHT = (137, 83, 50, 255)
VERMILION_DARK = (103, 32, 29, 255)
VERMILION = (146, 49, 38, 255)
VERMILION_LIGHT = (178, 69, 47, 255)
BRONZE_DARK = (105, 80, 39, 255)
BRONZE = (177, 132, 61, 255)
BRONZE_LIGHT = (209, 179, 92, 255)
JADE_DARK = (49, 93, 89, 255)
JADE = (102, 141, 121, 255)
JADE_LIGHT = (148, 179, 143, 255)
EMBER = (217, 154, 63, 255)

WATER_DARK = (27, 48, 55, 255)
WATER_MID = (43, 75, 82, 255)
WATER_LIGHT = (72, 109, 111, 255)
WATER_GLINT = (111, 151, 145, 255)
MOSS_SHADOW = (35, 61, 42, 255)
MOSS_DARK = (47, 82, 49, 255)
MOSS = (66, 104, 57, 255)
MOSS_LIGHT = (102, 132, 72, 255)
WEED_DARK = (31, 67, 39, 255)
WEED = (52, 98, 48, 255)
WEED_LIGHT = (91, 132, 61, 255)
FLOWER = (177, 105, 111, 255)
FLOWER_LIGHT = (214, 175, 111, 255)


def tile_rect(index: int) -> tuple[int, int, int, int]:
    if not 0 <= index < 256:
        raise ValueError(f"tile index out of range: {index}")
    x = index % COLS * T
    y = index // COLS * T
    return x, y, x + T, y + T


def tile(image: Image.Image, index: int) -> Image.Image:
    return image.crop(tile_rect(index)).convert("RGBA")


def paste_tile(atlas: Image.Image, source: Image.Image, source_index: int, target_index: int) -> None:
    atlas.paste(tile(source, source_index), tile_rect(target_index))


def replace_tile(atlas: Image.Image, source: Image.Image, index: int) -> None:
    atlas.paste(source.convert("RGBA"), tile_rect(index))


def local_draw() -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = Image.new("RGBA", (T, T), TRANSPARENT)
    return image, ImageDraw.Draw(image)


def draw_brick_floor(seed: int, variant: int = 0) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    base = (100 + variant * 2, 99 + variant, 88 - variant, 255)
    draw.rectangle((0, 0, 15, 15), fill=base)
    courses = {
        0: (3, 7, 11, 15),
        1: (4, 8, 12),
        2: (2, 6, 11, 15),
    }[variant]
    for y in courses:
        draw.line((0, y, 15, y), fill=BRICK_DARK)
        if y < 15:
            draw.line((0, y + 1, 15, y + 1), fill=(111, 108, 94, 255))
    starts = (0,) + tuple(y + 1 for y in courses if y < 15)
    for row, y in enumerate(starts):
        offset = (2 + row * 5 + variant * 3) % 8
        for x in range(offset, 16, 8):
            next_course = next((course for course in courses if course >= y), 15)
            draw.line((x, y, x, min(next_course - 1, y + 2)), fill=BRICK_DARK)
    for _ in range(8):
        x, y = rng.randrange(1, 15), rng.randrange(1, 15)
        draw.point((x, y), fill=rng.choice((BRICK, BRICK_LIGHT, (79, 80, 75, 255))))
    if variant == 2:
        draw.line((10, 5, 8, 7), fill=(70, 71, 67, 255))
        draw.point((7, 7), fill=(70, 71, 67, 255))
    return image


def draw_runner(seed: int, variant: int = 0) -> Image.Image:
    image = draw_brick_floor(seed, variant)
    draw = ImageDraw.Draw(image)
    draw.rectangle((1, 0, 14, 15), fill=VERMILION_DARK)
    draw.rectangle((3, 0, 12, 15), fill=(120, 37, 31, 255))
    edge = BRONZE if variant == 0 else BRONZE_DARK
    for y in (1, 6, 11):
        draw.point((2, y), fill=edge)
        draw.point((13, y + 2), fill=edge)
    # Stepped cloud-like corners, deliberately abstract and non-linguistic.
    draw.line((4, 3, 6, 3), fill=VERMILION_LIGHT)
    draw.line((6, 3, 6, 4), fill=VERMILION_LIGHT)
    draw.line((9, 11, 11, 11), fill=WOOD_DARK)
    draw.line((9, 10, 9, 11), fill=WOOD_DARK)
    return image


def draw_wood_floor(seed: int, variant: int = 0) -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=(91 + variant * 3, 55 + variant * 2, 38, 255))
    courses = (4, 9, 14) if variant == 0 else (3, 7, 12)
    for y in courses:
        draw.line((0, y, 15, y), fill=WOOD_DARK)
        if y < 15:
            draw.line((0, y + 1, 15, y + 1), fill=WOOD_LIGHT)
    starts = (0,) + tuple(y + 1 for y in courses)
    for row, y in enumerate(starts):
        x = (row * 7 + seed) % 13 + 1
        next_course = next((course for course in courses if course >= y), 15)
        draw.line((x, y, x, min(next_course - 1, y + 3)), fill=WOOD_DARK)
    for _ in range(5):
        x, y = rng.randrange(2, 14), rng.randrange(1, 15)
        draw.point((x, y), fill=rng.choice((WOOD, WOOD_LIGHT, WOOD_DARK)))
    return image


def draw_moss_floor(background: Image.Image, seed: int, alt: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image = background.copy()
    draw = ImageDraw.Draw(image)
    anchors = (
        ((2, 4), (6, 11), (11, 6), (13, 13))
        if not alt
        else ((3, 12), (7, 5), (10, 10), (13, 3))
    )
    for x, y in anchors:
        dark = rng.choice((MOSS_SHADOW, MOSS_DARK))
        draw.point((x, y), fill=dark)
        draw.point((x + 1, y), fill=MOSS)
        if (x > 1 and rng.randrange(2)):
            draw.point((x - 1, y + 1), fill=MOSS_DARK)
        if y < 14:
            draw.point((x, y + 1), fill=MOSS_LIGHT)
    for _ in range(5):
        x, y = rng.randrange(2, 14), rng.randrange(2, 14)
        draw.point((x, y), fill=rng.choice((MOSS_DARK, MOSS, MOSS_LIGHT)))
    if alt:
        draw.point((5, 8), fill=FLOWER_LIGHT)
    return image


def weed_stems(seed: int, trampled: bool = False) -> tuple[tuple[int, int, int], ...]:
    rng = random.Random(seed)
    xs = (2, 4, 6, 8, 10, 12, 14)
    stems = []
    for order, x in enumerate(xs):
        height = rng.randrange(3, 6) if trampled else rng.randrange(8, 14)
        bend = (-1, 0, 1)[(order + seed) % 3]
        stems.append((x, height, bend))
    return tuple(stems)


def draw_flat_weeds(
        background: Image.Image,
        seed: int,
        trampled: bool = False,
        alt: bool = False) -> Image.Image:
    image = draw_moss_floor(background, seed + 100, alt)
    draw = ImageDraw.Draw(image)
    for order, (x, height, bend) in enumerate(weed_stems(seed, trampled)):
        bottom = 14
        top = max(2, bottom - height)
        color = WEED if (order + int(alt)) % 2 else WEED_DARK
        draw.line((x, bottom, x, top + 2), fill=color)
        draw.line((x, top + 2, x + bend, top), fill=WEED_LIGHT)
        leaf_y = top + max(2, height // 2)
        draw.point((max(0, x - 1), leaf_y), fill=WEED)
        draw.point((min(15, x + 1), min(15, leaf_y + 1)), fill=WEED_LIGHT)
        if not trampled and order in ((1, 5) if alt else (2, 4)):
            draw.point((x + bend, max(1, top - 1)), fill=FLOWER if alt else FLOWER_LIGHT)
    if trampled:
        for x, direction in ((3, 1), (8, -1), (12, 1)):
            draw.line((x, 13, x + direction * 3, 11), fill=WEED_DARK)
            draw.point((x + direction * 3, 10), fill=WEED_LIGHT)
    else:
        draw.line((1, 14, 14, 14), fill=MOSS_DARK)
    return image


def draw_raised_weeds(
        background: Image.Image,
        seed: int,
        trampled: bool = False,
        alt: bool = False) -> Image.Image:
    image = draw_moss_floor(background, seed + 200, alt)
    draw = ImageDraw.Draw(image)
    stems = weed_stems(seed, trampled)
    for order, (x, height, bend) in enumerate(stems):
        color = WEED if (order + int(alt)) % 2 else WEED_DARK
        if trampled:
            draw.line((x, 0, x, 3), fill=color)
            draw.line((x, 3, max(0, min(15, x + bend * 3)), 5), fill=WEED_LIGHT)
        else:
            draw.line((x, 13, x, 0), fill=color)
            draw.point((max(0, x - 1), 5 + order % 3), fill=WEED)
            draw.point((min(15, x + 1), 7 + order % 2), fill=WEED_LIGHT)
    if not trampled:
        draw.line((1, 13, 14, 13), fill=MOSS_DARK)
    return image


def draw_weed_overhang(seed: int, trampled: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    stems = weed_stems(seed, trampled)
    start_y = 13 if trampled else 7
    for order, (x, _height, bend) in enumerate(stems):
        color = WEED if (order + int(alt)) % 2 else WEED_DARK
        top = start_y + (order % 3 if not trampled else order % 2)
        draw.line((x, 15, x, top + 2), fill=color)
        draw.line((x, top + 2, max(0, min(15, x + bend)), top), fill=WEED_LIGHT)
        if not trampled:
            draw.point((max(0, x - 1), min(15, top + 3)), fill=WEED)
            draw.point((min(15, x + 1), min(15, top + 5)), fill=WEED_LIGHT)
            if order in ((1, 5) if alt else (2, 4)):
                draw.point((max(0, min(15, x + bend)), max(5, top - 1)),
                           fill=FLOWER if alt else FLOWER_LIGHT)
    return image


def draw_weed_underhang(seed: int, trampled: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    rng = random.Random(seed + 300)
    upper = 5 if trampled else 9
    for x in (2, 4, 6, 8, 10, 12, 14):
        top = rng.randrange(0, 3)
        bottom = rng.randrange(max(top + 2, upper - 3), upper + 1)
        draw.line((x, top, x, bottom), fill=WEED_DARK if x % 4 else WEED)
        direction = -1 if (x + int(alt)) % 3 else 1
        draw.point((max(0, min(15, x + direction)), max(top, bottom - 2)), fill=WEED_LIGHT)
    return image


def draw_brazier_floor(seed: int, variant: int = 0) -> Image.Image:
    image = draw_brick_floor(seed, variant)
    draw = ImageDraw.Draw(image)
    draw.ellipse((4, 7, 11, 13), fill=INK, outline=BRONZE_DARK)
    draw.rectangle((5, 8, 10, 10), fill=(91, 42, 28, 255))
    draw.point((6, 8), fill=EMBER)
    draw.point((8, 9), fill=(235, 116, 42, 255))
    draw.point((9, 7), fill=BRONZE_LIGHT)
    return image


def draw_pedestal(background: Image.Image) -> Image.Image:
    image = background.copy()
    draw = ImageDraw.Draw(image)
    draw.rectangle((4, 6, 11, 12), fill=WOOD_DARK)
    draw.rectangle((3, 5, 12, 7), fill=WOOD)
    draw.line((4, 5, 11, 5), fill=WOOD_LIGHT)
    draw.rectangle((5, 8, 10, 10), fill=VERMILION_DARK)
    draw.point((7, 8), fill=BRONZE_LIGHT)
    return image


def draw_exit(background: Image.Image, locked: bool = False, underhang: bool = False) -> Image.Image:
    image = Image.new("RGBA", (T, T), TRANSPARENT) if underhang else background.copy()
    draw = ImageDraw.Draw(image)
    if underhang:
        draw.rectangle((2, 0, 13, 4), fill=BRICK_DARK)
        draw.line((3, 0, 12, 0), fill=BRICK_LIGHT)
        draw.rectangle((4, 1, 11, 5), fill=INK)
        draw.line((5, 2, 10, 2), fill=BRICK)
        return image
    draw.rectangle((2, 4, 13, 14), fill=BRICK_DARK)
    draw.rectangle((4, 5, 11, 13), fill=INK)
    for y, inset in ((7, 0), (9, 1), (11, 2), (13, 3)):
        draw.line((4 + inset, y, 11 - inset, y), fill=BRICK_LIGHT)
        if y < 13:
            draw.line((4 + inset, y + 1, 11 - inset, y + 1), fill=SHADOW)
    draw.line((2, 4, 13, 4), fill=BRICK_LIGHT)
    if locked:
        draw.rectangle((3, 8, 12, 9), fill=BRONZE_DARK)
        draw.rectangle((6, 8, 9, 12), fill=BRONZE)
        draw.point((7, 9), fill=BRONZE_LIGHT)
    return image


def draw_chasm(seed: int, edge: str = "plain") -> Image.Image:
    rng = random.Random(seed)
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=(12, 14, 14, 255))
    for _ in range(14):
        draw.point((rng.randrange(16), rng.randrange(16)), fill=rng.choice((INK, (34, 39, 39, 255))))
    if edge == "brick":
        draw.line((0, 0, 15, 0), fill=BRICK_LIGHT)
        draw.line((0, 1, 15, 1), fill=BRICK_DARK)
    elif edge == "wood":
        draw.line((0, 0, 15, 0), fill=WOOD_LIGHT)
        draw.line((0, 1, 15, 1), fill=WOOD_DARK)
    elif edge == "wall":
        draw.rectangle((0, 0, 15, 3), fill=BRICK_DARK)
        draw.line((0, 0, 15, 0), fill=BRICK_LIGHT)
    elif edge == "water":
        draw.line((0, 0, 15, 0), fill=WATER_LIGHT)
        draw.line((0, 1, 15, 1), fill=WATER_DARK)
    return image


def build_water_texture() -> Image.Image:
    image = Image.new("RGBA", (32, 32), WATER_MID)
    draw = ImageDraw.Draw(image)
    rng = random.Random(8102)
    for y in range(32):
        for x in range(32):
            if (x * 3 + y * 5) % 17 == 0:
                draw.point((x, y), fill=WATER_DARK)
    strokes = (
        (2, 4, 9, WATER_LIGHT),
        (15, 7, 24, WATER_GLINT),
        (6, 14, 14, WATER_DARK),
        (19, 18, 29, WATER_LIGHT),
        (1, 25, 8, WATER_GLINT),
        (12, 29, 22, WATER_DARK),
    )
    for left, y, right, color in strokes:
        draw.line((left, y, right, y), fill=color)
        if color == WATER_GLINT:
            draw.point((right + 1, y), fill=WATER_LIGHT)
    for _ in range(20):
        x, y = rng.randrange(32), rng.randrange(32)
        draw.point((x, y), fill=rng.choice((WATER_DARK, WATER_MID, WATER_LIGHT)))
    # Match opposite edges exactly for a seamless 32x32 loop.
    for x in range(32):
        image.putpixel((x, 31), image.getpixel((x, 0)))
    for y in range(32):
        image.putpixel((31, y), image.getpixel((0, y)))
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
            r, g, b, alpha = source.getpixel((x, y))
            if alpha == 0:
                continue
            maximum = max(r, g, b)
            minimum = min(r, g, b)
            saturation = maximum - minimum
            water_edge = (
                g > r + 8 and g >= b - 12 and maximum > 38
            ) or (
                g >= r and g >= b - 18 and maximum <= 70 and saturation > 10
            )
            if water_edge:
                wr, wg, wb, _ = water.getpixel((x, y))
                result.putpixel((x, y), (max(0, wr - 9), max(0, wg - 9), max(0, wb - 7), alpha))
            else:
                fr, fg, fb, _ = floor.getpixel((x, y))
                result.putpixel((x, y), (fr, fg, fb, alpha))
    return result


def recolor_structure(source: Image.Image, ramp: tuple[tuple[int, int, int, int], ...]) -> Image.Image:
    source = source.convert("RGBA")
    result = Image.new("RGBA", source.size, TRANSPARENT)
    for y in range(source.height):
        for x in range(source.width):
            r, g, b, a = source.getpixel((x, y))
            if a == 0:
                continue
            value = (r * 3 + g * 5 + b * 2) // 10
            level = min(len(ramp) - 1, value * len(ramp) // 256)
            rr, gg, bb, _ = ramp[level]
            result.putpixel((x, y), (rr, gg, bb, a))
    return result


STONE_RAMP = (INK, SHADOW, BRICK_DARK, BRICK, BRICK_LIGHT)
WOOD_RAMP = (INK, WOOD_DARK, WOOD, WOOD_LIGHT, BRONZE_DARK)


def draw_flat_wall(seed: int, deco: bool = False, shelf: bool = False, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    draw.rectangle((0, 0, 15, 15), fill=SHADOW)
    draw.rectangle((0, 2, 15, 14), fill=BRICK_DARK)
    for y in (5, 10, 14):
        draw.line((0, y, 15, y), fill=INK)
        draw.line((0, y - 1, 15, y - 1), fill=BRICK)
    for y, offset in ((2, 3 if alt else 0), (6, 7 if alt else 4), (11, 1 if alt else 6)):
        for x in range(offset, 16, 8):
            draw.line((x, y, x, min(14, y + 3)), fill=SHADOW)
    draw.line((0, 1, 15, 1), fill=WOOD_DARK)
    draw.line((0, 0, 15, 0), fill=WOOD_LIGHT)
    if deco:
        draw.rectangle((3, 4, 12, 12), fill=WOOD_DARK)
        draw.rectangle((4, 5, 11, 11), outline=VERMILION_DARK)
        draw.line((5, 8, 7, 6), fill=BRONZE_DARK)
        draw.line((7, 6, 10, 8), fill=BRONZE_DARK)
        draw.line((10, 8, 7, 10), fill=BRONZE_DARK)
        draw.line((7, 10, 5, 8), fill=BRONZE_DARK)
    if shelf:
        draw.rectangle((1, 3, 14, 14), fill=WOOD_DARK)
        for y in (7, 11):
            draw.line((1, y, 14, y), fill=WOOD_LIGHT)
        draw.rectangle((2, 4, 7, 5), fill=BRICK_LIGHT)
        draw.point((2, 4), fill=BRONZE)
        draw.point((7, 5), fill=BRONZE)
        draw.rectangle((9, 5, 13, 6), fill=(157, 139, 103, 255))
        draw.point((9, 5), fill=VERMILION_DARK)
        draw.line((2, 9, 12, 9), fill=BRICK_LIGHT)
        draw.point((1, 9), fill=BRONZE_LIGHT)
        draw.line((10, 8, 13, 10), fill=BRONZE_DARK)
        draw.rectangle((3, 12, 8, 13), fill=WOOD)
        draw.point((3, 12), fill=BRONZE)
    return image


def door_background(wood: bool = False) -> Image.Image:
    return draw_wood_floor(920, 0) if wood else draw_brick_floor(920, 0)


def draw_door(kind: str, sideways: bool = False, overhang: bool = False) -> Image.Image:
    image = Image.new("RGBA", (T, T), TRANSPARENT) if overhang else door_background()
    draw = ImageDraw.Draw(image)
    if sideways:
        if overhang:
            draw.rectangle((1, 7, 14, 15), fill=WOOD_DARK)
            draw.line((2, 7, 13, 7), fill=WOOD_LIGHT)
            if kind == "locked":
                draw.rectangle((6, 11, 10, 14), fill=BRONZE)
            elif kind == "jade":
                draw.rectangle((5, 10, 10, 14), fill=JADE)
            return image
        draw.rectangle((1, 3, 14, 13), fill=WOOD_DARK)
        if kind == "open":
            draw.rectangle((3, 5, 12, 13), fill=INK)
            draw.rectangle((1, 5, 3, 13), fill=VERMILION_DARK)
            draw.rectangle((12, 5, 14, 13), fill=VERMILION_DARK)
        else:
            draw.rectangle((3, 4, 12, 13), fill=VERMILION_DARK)
            draw.line((7, 4, 7, 13), fill=WOOD_DARK)
        return image
    if overhang:
        draw.rectangle((2, 8, 13, 15), fill=WOOD_DARK)
        draw.line((2, 8, 13, 8), fill=WOOD_LIGHT)
        if kind == "open":
            draw.rectangle((3, 10, 5, 15), fill=VERMILION_DARK)
            draw.rectangle((10, 10, 12, 15), fill=VERMILION_DARK)
            draw.line((4, 10, 4, 15), fill=VERMILION)
            draw.line((11, 10, 11, 15), fill=SHADOW)
            draw.rectangle((6, 10, 9, 15), fill=TRANSPARENT)
        else:
            draw.rectangle((3, 10, 12, 15), fill=JADE_DARK if kind == "jade" else VERMILION_DARK)
        return image
    draw.rectangle((2, 2, 13, 14), fill=WOOD_DARK)
    if kind == "open":
        draw.rectangle((4, 4, 11, 14), fill=INK)
        draw.rectangle((2, 4, 4, 14), fill=VERMILION_DARK)
        draw.rectangle((11, 4, 13, 14), fill=VERMILION_DARK)
    else:
        fill = JADE_DARK if kind == "jade" else VERMILION_DARK
        draw.rectangle((4, 3, 11, 14), fill=fill)
        draw.line((7, 3, 7, 14), fill=WOOD_DARK)
        for y in (6, 10):
            draw.point((5, y), fill=BRONZE)
            draw.point((10, y), fill=BRONZE)
    draw.line((2, 2, 13, 2), fill=WOOD_LIGHT)
    if kind == "locked":
        draw.rectangle((3, 8, 12, 9), fill=BRONZE_DARK)
        draw.rectangle((6, 8, 9, 12), fill=BRONZE)
        draw.point((7, 9), fill=BRONZE_LIGHT)
    elif kind == "jade":
        draw.rectangle((6, 5, 9, 11), fill=JADE)
        draw.point((7, 6), fill=JADE_LIGHT)
    return image


def draw_screen(
        background: Image.Image,
        broken: bool = False,
        alt: bool = False,
        raised: bool = False) -> Image.Image:
    image = background.copy()
    draw = ImageDraw.Draw(image)
    if broken:
        for box in ((2, 9, 6, 11), (8, 7, 13, 9), (5, 12, 10, 13)):
            draw.rectangle(box, fill=WOOD_DARK)
            draw.line((box[0], box[1], box[2], box[1]), fill=WOOD_LIGHT)
        draw.point((12, 12), fill=VERMILION_DARK)
        return image
    top = 0 if raised else 3
    draw.rectangle((2, top, 13, 14), fill=WOOD_DARK)
    draw.rectangle((3, top, 12, 12), fill=JADE_DARK if alt else VERMILION_DARK)
    draw.line((7, top, 7, 13), fill=BRONZE_DARK)
    motif_y = 6 if raised else 8
    draw.line((4, motif_y, 5, motif_y - 2), fill=BRONZE)
    draw.line((5, motif_y - 2, 6, motif_y), fill=BRONZE)
    draw.line((6, motif_y, 5, motif_y + 2), fill=BRONZE)
    draw.line((5, motif_y + 2, 4, motif_y), fill=BRONZE)
    draw.line((9, motif_y, 10, motif_y - 2), fill=BRONZE)
    draw.line((10, motif_y - 2, 11, motif_y), fill=BRONZE)
    draw.line((11, motif_y, 10, motif_y + 2), fill=BRONZE)
    draw.line((10, motif_y + 2, 9, motif_y), fill=BRONZE)
    return image


def draw_barricade(background: Image.Image, raised: bool = False) -> Image.Image:
    image = background.copy()
    draw = ImageDraw.Draw(image)
    if raised:
        draw.line((2, 0, 13, 7), fill=WOOD_LIGHT, width=2)
        draw.line((13, 0, 4, 8), fill=WOOD, width=2)
        draw.line((2, 0, 13, 0), fill=WOOD_DARK)
        draw.rectangle((1, 5, 5, 12), outline=VERMILION_DARK)
        return image
    draw.rectangle((2, 8, 13, 11), fill=WOOD_DARK)
    draw.line((3, 7, 12, 13), fill=WOOD_LIGHT, width=2)
    draw.line((12, 6, 4, 13), fill=WOOD, width=2)
    draw.rectangle((1, 10, 5, 14), outline=VERMILION_DARK)
    return image


def draw_cauldron(background: Image.Image, raised: bool = False) -> Image.Image:
    image = background.copy()
    draw = ImageDraw.Draw(image)
    if raised:
        draw.ellipse((3, -5, 12, 7), fill=BRONZE_DARK, outline=BRONZE_LIGHT)
        draw.rectangle((4, 0, 11, 2), fill=INK)
        draw.line((5, 1, 10, 1), fill=JADE)
        draw.line((4, 6, 3, 13), fill=BRONZE_DARK)
        draw.line((11, 6, 12, 13), fill=BRONZE_DARK)
        return image
    draw.ellipse((3, 5, 12, 12), fill=BRONZE_DARK, outline=BRONZE_LIGHT)
    draw.rectangle((4, 5, 11, 8), fill=INK)
    draw.line((5, 6, 10, 6), fill=JADE)
    draw.line((4, 12, 3, 14), fill=BRONZE_DARK)
    draw.line((11, 12, 12, 14), fill=BRONZE_DARK)
    draw.point((5, 4), fill=WATER_GLINT)
    return image


def draw_lion(background: Image.Image, body_only: bool = False) -> Image.Image:
    image = background.copy()
    draw = ImageDraw.Draw(image)
    if not body_only:
        draw.ellipse((3, 2, 12, 9), fill=BRICK_DARK, outline=SHADOW)
        draw.rectangle((4, 3, 11, 8), fill=BRICK)
        draw.point((3, 3), fill=BRICK_LIGHT)
        draw.point((12, 4), fill=BRICK_LIGHT)
        draw.point((5, 5), fill=INK)
        draw.point((10, 5), fill=INK)
        draw.rectangle((6, 7, 10, 9), fill=BRICK_LIGHT)
        draw.point((9, 8), fill=INK)
        draw.rectangle((4, 9, 11, 12), fill=BRICK_DARK)
        draw.rectangle((4, 11, 6, 13), fill=BRICK)
        draw.rectangle((9, 11, 11, 13), fill=BRICK)
    else:
        draw.rectangle((4, 0, 11, 10), fill=BRICK_DARK)
        draw.rectangle((4, 7, 6, 12), fill=BRICK)
        draw.rectangle((9, 7, 11, 12), fill=BRICK)
    draw.rectangle((3, 13, 12, 14), fill=SHADOW)
    draw.line((4, 13, 11, 13), fill=BRICK_LIGHT)
    return image


def draw_drum(background: Image.Image, flat: bool = False) -> Image.Image:
    image = background.copy()
    draw = ImageDraw.Draw(image)
    if flat:
        draw.line((3, 5, 3, 14), fill=WOOD_DARK)
        draw.line((12, 5, 12, 14), fill=WOOD_DARK)
        draw.line((3, 13, 12, 13), fill=WOOD)
        draw.ellipse((3, 4, 12, 13), fill=VERMILION_DARK, outline=BRONZE_DARK)
        draw.ellipse((5, 6, 10, 11), fill=VERMILION)
        draw.point((6, 6), fill=VERMILION_LIGHT)
        for point in ((4, 6), (11, 7), (5, 11), (10, 12)):
            draw.point(point, fill=BRONZE_LIGHT)
    else:
        # This is the lower half of a cross-tile drum whose crown lives in slot 242.
        draw.ellipse((3, -6, 12, 9), fill=VERMILION_DARK, outline=BRONZE_DARK)
        draw.ellipse((5, -4, 10, 7), fill=VERMILION)
        draw.point((10, 2), fill=BRONZE_LIGHT)
        draw.line((3, 4, 3, 14), fill=WOOD_DARK)
        draw.line((12, 4, 12, 14), fill=WOOD_DARK)
        draw.line((3, 13, 12, 13), fill=WOOD)
        draw.point((4, 13), fill=WOOD_LIGHT)
    return image


def draw_ding(background: Image.Image, flat: bool = False) -> Image.Image:
    image = background.copy()
    draw = ImageDraw.Draw(image)
    if flat:
        draw.rectangle((3, 6, 12, 7), fill=BRONZE_DARK)
        draw.rectangle((1, 6, 3, 10), fill=BRONZE_DARK)
        draw.rectangle((12, 6, 14, 10), fill=BRONZE_DARK)
        draw.ellipse((2, 7, 13, 14), fill=BRONZE_DARK, outline=SHADOW)
        draw.rectangle((4, 8, 11, 11), fill=(143, 105, 50, 255))
        draw.rectangle((5, 7, 10, 8), fill=INK)
        draw.line((5, 13, 4, 15), fill=SHADOW, width=2)
        draw.line((10, 13, 11, 15), fill=SHADOW, width=2)
        draw.point((5, 9), fill=BRONZE)
    else:
        # The body starts above this tile; only the lower belly and feet belong here.
        draw.ellipse((2, -4, 13, 8), fill=BRONZE_DARK, outline=SHADOW)
        draw.rectangle((4, 0, 11, 5), fill=(143, 105, 50, 255))
        draw.point((5, 0), fill=BRONZE)
        draw.line((5, 6, 4, 13), fill=SHADOW, width=2)
        draw.line((10, 6, 11, 13), fill=SHADOW, width=2)
    return image


def draw_mineral(
        background: Image.Image,
        seed: int,
        jade: bool,
        raised: bool = False) -> Image.Image:
    rng = random.Random(seed)
    image = background.copy()
    draw = ImageDraw.Draw(image)
    colors = (JADE_DARK, JADE, JADE_LIGHT) if jade else (SHADOW, BRICK_DARK, BRICK_LIGHT)
    points = (
        [(2, 13), (5, 3), (7, 8), (10, 0), (14, 13)]
        if raised
        else [(2, 13), (4, 7), (7, 10), (9, 4), (13, 13)]
    )
    draw.polygon(points, fill=colors[0])
    draw.polygon([(4, 12), (5, 8), (8, 11), (9, 6), (12, 13)], fill=colors[1])
    for _ in range(4):
        draw.point((rng.randrange(4, 12), rng.randrange(6, 13)), fill=colors[2])
    return image


def draw_object_overhang(kind: str, alt: bool = False) -> Image.Image:
    image, draw = local_draw()
    if kind == "cauldron":
        draw.arc((3, 9, 12, 17), 180, 350, fill=BRONZE_LIGHT)
        draw.point((6, 12), fill=WATER_GLINT)
        draw.line((4, 15, 11, 15), fill=BRONZE_DARK)
    elif kind == "barricade":
        draw.line((2, 11, 13, 15), fill=WOOD_DARK, width=2)
    elif kind == "screen":
        draw.rectangle((2, 10, 13, 15), fill=WOOD_DARK)
        draw.rectangle((3, 11, 12, 15), fill=JADE_DARK if alt else VERMILION_DARK)
        draw.line((7, 11, 7, 15), fill=BRONZE_DARK)
    elif kind == "lion":
        draw.ellipse((3, 8, 12, 15), fill=BRICK_DARK, outline=SHADOW)
        draw.rectangle((4, 10, 11, 15), fill=BRICK)
        draw.point((3, 9), fill=BRICK_LIGHT)
        draw.point((12, 10), fill=BRICK_LIGHT)
        draw.point((5, 12), fill=INK)
        draw.point((10, 12), fill=INK)
        draw.rectangle((6, 14, 10, 15), fill=BRICK_LIGHT)
    elif kind == "drum":
        draw.line((2, 15, 13, 15), fill=WOOD)
        draw.point((2, 14), fill=WOOD_LIGHT)
        draw.point((13, 14), fill=WOOD_DARK)
        draw.ellipse((3, 10, 12, 25), fill=VERMILION_DARK, outline=BRONZE_DARK)
        draw.ellipse((5, 12, 10, 23), fill=VERMILION)
        draw.point((6, 12), fill=VERMILION_LIGHT)
        draw.point((4, 13), fill=BRONZE_LIGHT)
        draw.point((11, 14), fill=BRONZE_LIGHT)
    elif kind == "ding":
        draw.rectangle((1, 11, 3, 15), fill=BRONZE_DARK)
        draw.rectangle((12, 11, 14, 15), fill=BRONZE_DARK)
        draw.rectangle((4, 14, 11, 15), fill=BRONZE_DARK)
        draw.line((5, 13, 10, 13), fill=INK)
        draw.line((6, 12, 9, 12), fill=BRONZE)
    elif kind == "mineral":
        colors = (JADE_DARK, JADE, JADE_LIGHT) if not alt else (SHADOW, BRICK_DARK, BRICK_LIGHT)
        draw.polygon([(2, 15), (5, 10), (7, 15), (10, 8), (14, 15)], fill=colors[0])
        draw.line((10, 9, 10, 14), fill=colors[2])
    elif kind == "fragments":
        draw.line((2, 11, 7, 14), fill=WOOD)
        draw.line((9, 12, 13, 15), fill=VERMILION_DARK)
    return image


def add_hall_wall_detail(image: Image.Image, seed: int) -> Image.Image:
    result = image.copy()
    rng = random.Random(seed)
    if result.getbbox() is None:
        return result

    # Keep bitmask silhouettes untouched: details only recolor already-opaque pixels.
    accents = (
        (WOOD_DARK, ((3, 7), (12, 10))),
        (BRONZE_DARK, ((5, 6), (10, 11))),
        (JADE_DARK, ((4, 9), (11, 7))),
        (VERMILION_DARK, ((7, 8), (8, 8))),
    )
    color, points = accents[seed % len(accents)]
    for x, y in points:
        if result.getpixel((x, y))[3]:
            result.putpixel((x, y), color)
            if rng.randrange(2) and y + 1 < T and result.getpixel((x, y + 1))[3]:
                result.putpixel((x, y + 1), color)
    return result


def build_tileset() -> Image.Image:
    prison = Image.open(ENV / "tiles_prison.png").convert("RGBA")
    sewers = Image.open(ENV / "tiles_sewers.png").convert("RGBA")
    atlas = Image.new("RGBA", (ATLAS_SIZE, ATLAS_SIZE), TRANSPARENT)

    floors = {
        0: draw_brick_floor(100, 0),
        1: draw_runner(101, 0),
        2: draw_moss_floor(draw_brick_floor(102, 1), 302),
        3: draw_brazier_floor(103, 0),
        4: draw_wood_floor(104, 0),
        6: draw_brick_floor(106, 1),
        7: draw_runner(107, 1),
        8: draw_moss_floor(draw_brick_floor(108, 2), 308, alt=True),
        9: draw_brazier_floor(109, 1),
        10: draw_wood_floor(110, 1),
        12: draw_brick_floor(112, 2),
    }
    for index in range(24):
        replace_tile(atlas, floors.get(index, floors[0]), index)
    # Preserve the project's established stair silhouettes and only change material.
    replace_tile(atlas, recolor_structure(tile(sewers, 17), STONE_RAMP), 16)
    replace_tile(atlas, recolor_structure(tile(sewers, 16), STONE_RAMP), 17)
    paste_tile(atlas, sewers, 18, 18)
    paste_tile(atlas, sewers, 19, 19)
    replace_tile(atlas, draw_pedestal(floors[0]), 20)
    replace_tile(atlas, recolor_structure(tile(sewers, 17), WOOD_RAMP), 22)

    chasm_edges = ("plain", "brick", "wood", "wall", "water", "plain", "brick", "wood")
    for offset, edge in enumerate(chasm_edges):
        replace_tile(atlas, draw_chasm(200 + offset, edge), 24 + offset)

    water = build_water_texture()
    for index in range(32, 48):
        transition = draw_water_transition(
            sewers,
            index,
            floors[0],
            water,
        )
        replace_tile(atlas, transition, index)

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

    for index, kind in {
        56: "closed",
        57: "open",
        58: "locked",
        59: "jade",
    }.items():
        replace_tile(atlas, draw_door(kind), index)
    replace_tile(atlas, draw_exit(floors[0], locked=False), 60)
    replace_tile(atlas, draw_exit(floors[0], locked=True), 61)
    replace_tile(atlas, draw_flat_wall(62, deco=True), 62)
    replace_tile(atlas, draw_flat_wall(63, deco=True, alt=True), 63)

    flat_objects = {
        64: draw_cauldron(floors[0]),
        65: draw_barricade(floors[0]),
        66: draw_flat_weeds(floors[0], 466),
        67: draw_flat_weeds(floors[0], 467, trampled=True),
        69: draw_flat_weeds(floors[0], 469, alt=True),
        70: draw_flat_weeds(floors[0], 470, trampled=True, alt=True),
        72: draw_lion(floors[0]),
        73: draw_lion(floors[4]),
        74: draw_drum(floors[0], flat=True),
        75: draw_ding(floors[4], flat=True),
        76: draw_mineral(floors[0], 76, True),
        77: draw_mineral(floors[0], 77, True),
        78: draw_mineral(floors[0], 78, True),
    }
    for index in range(64, 80):
        replace_tile(atlas, flat_objects.get(index, floors[0]), index)

    for index in range(80, 112):
        ramp = WOOD_RAMP if index in range(92, 96) or index in range(108, 112) else STONE_RAMP
        source = recolor_structure(tile(prison, index), ramp)
        replace_tile(atlas, add_hall_wall_detail(source, 400 + index), index)

    for index, kind in {
        112: "closed",
        113: "open",
        114: "locked",
        115: "jade",
        116: "closed",
    }.items():
        replace_tile(atlas, draw_door(kind, sideways=index == 116), index)
    for index in range(117, 120):
        replace_tile(atlas, recolor_structure(tile(prison, index), STONE_RAMP), index)

    raised_objects = {
        120: draw_cauldron(floors[0], raised=True),
        121: draw_barricade(floors[0], raised=True),
        122: draw_raised_weeds(floors[0], 466),
        123: draw_raised_weeds(floors[0], 467, trampled=True),
        125: draw_raised_weeds(floors[0], 469, alt=True),
        126: draw_raised_weeds(floors[0], 470, trampled=True, alt=True),
        128: draw_lion(floors[0], body_only=True),
        129: draw_lion(floors[4], body_only=True),
        130: draw_drum(floors[0]),
        131: draw_ding(floors[4]),
        132: draw_mineral(floors[0], 132, True, raised=True),
        133: draw_mineral(floors[0], 133, True, raised=True),
        134: draw_mineral(floors[0], 134, True, raised=True),
    }
    for index in range(120, 144):
        replace_tile(atlas, raised_objects.get(index, floors[0]), index)

    for index in range(144, 192):
        ramp = WOOD_RAMP if index >= 176 else STONE_RAMP
        source = recolor_structure(tile(prison, index), ramp)
        replace_tile(atlas, add_hall_wall_detail(source, 600 + index), index)

    for index in range(192, 224):
        ramp = WOOD_RAMP if 200 <= index < 204 else STONE_RAMP
        source = recolor_structure(tile(prison, index), ramp)
        replace_tile(atlas, add_hall_wall_detail(source, 800 + index), index)

    door_overhangs = {
        224: draw_door("closed", overhang=True),
        225: draw_door("open", overhang=True),
        226: draw_door("jade", overhang=True),
        227: draw_door("open", sideways=True, overhang=True),
        228: draw_door("locked", sideways=True, overhang=True),
        229: draw_door("jade", sideways=True, overhang=True),
        230: draw_exit(floors[0], underhang=True),
    }
    for index in range(224, 232):
        replace_tile(
            atlas,
            door_overhangs.get(index, recolor_structure(tile(prison, index), WOOD_RAMP)),
            index,
        )

    overhangs = {
        232: draw_object_overhang("cauldron"),
        233: draw_object_overhang("barricade"),
        234: draw_weed_overhang(466),
        235: draw_weed_overhang(467, trampled=True),
        237: draw_weed_overhang(469, alt=True),
        238: draw_weed_overhang(470, trampled=True, alt=True),
        240: draw_object_overhang("lion"),
        241: draw_object_overhang("lion"),
        242: draw_object_overhang("drum"),
        243: draw_object_overhang("ding"),
        244: draw_object_overhang("mineral"),
        245: draw_object_overhang("mineral"),
        246: draw_object_overhang("mineral"),
        250: draw_weed_underhang(466),
        251: draw_weed_underhang(467, trampled=True),
        253: draw_weed_underhang(469, alt=True),
        254: draw_weed_underhang(470, trampled=True, alt=True),
    }
    for index in range(232, 256):
        replace_tile(atlas, overhangs.get(index, Image.new("RGBA", (T, T), TRANSPARENT)), index)

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
    print(f"{atlas_path} {Image.open(atlas_path).size} {image_digest(Image.open(atlas_path))}")
    print(f"{water_path} {Image.open(water_path).size} {image_digest(Image.open(water_path))}")


if __name__ == "__main__":
    main()
