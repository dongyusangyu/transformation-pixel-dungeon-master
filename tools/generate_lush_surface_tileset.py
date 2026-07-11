from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
OUT = ENV / "tiles_surface_lush.png"
WATER_OUT = ENV / "water_surface_lush.png"
PREVIEW = ROOT / "tiles_surface_lush_preview.png"
SCENE_PREVIEW = ROOT / "tiles_surface_lush_scene_preview.png"

T = 16
W = 16


def tile_rect(index: int) -> tuple[int, int, int, int]:
    x = index % W * T
    y = index // W * T
    return x, y, x + T, y + T


def paste_tile(dst: Image.Image, src: Image.Image, src_index: int, dst_index: int) -> None:
    dst.paste(src.crop(tile_rect(src_index)), tile_rect(dst_index))


def draw_grass(draw: ImageDraw.ImageDraw, index: int, seed: int, dense: bool = False, flowers: bool = False) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    # Keep the edge pixels low-contrast so repeated 16x16 tiles do not form a visible grid.
    base = (65 + rng.randrange(4), 124 + rng.randrange(5), 51 + rng.randrange(4), 255)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=base)

    for _ in range(36 if dense else 22):
        x = x0 + rng.randrange(16)
        y = y0 + rng.randrange(16)
        c = rng.choice(
            [
                (55, 111, 44, 255),
                (74, 142, 56, 255),
                (91, 158, 64, 255),
                (48, 99, 43, 255),
            ]
        )
        if dense and rng.random() < 0.55:
            draw.line((x, y, x + rng.choice([-1, 0, 1]), max(y0, y - rng.randrange(2, 5))), fill=c)
        else:
            draw.point((x, y), fill=c)

    for _ in range(5 if flowers else 2):
        x = x0 + rng.randrange(3, 13)
        y = y0 + rng.randrange(3, 13)
        petal = rng.choice([(244, 226, 93, 255), (245, 168, 210, 255), (210, 238, 255, 255)])
        draw.point((x, y), fill=petal)
        if rng.random() < 0.4:
            draw.point((x + 1, y), fill=petal)

    # Lightly normalize the border pixels to reduce both vertical and horizontal seams.
    edge = (66, 126, 52, 255)
    draw.line((x0, y0, x1 - 1, y0), fill=edge)
    draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=edge)
    draw.line((x0, y0, x0, y1 - 1), fill=edge)
    draw.line((x1 - 1, y0, x1 - 1, y1 - 1), fill=edge)


def draw_wood_floor(draw: ImageDraw.ImageDraw, index: int, seed: int) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=(116, 80, 48, 255))
    for y in (y0 + 4, y0 + 9, y0 + 14):
        draw.line((x0, y, x1 - 1, y), fill=(66, 47, 35, 255))
    for y in (y0, y0 + 5, y0 + 10):
        offset = rng.randrange(2, 8)
        draw.line((x0 + offset, y, x0 + offset, min(y + 4, y1 - 1)), fill=(70, 49, 34, 255))
        draw.line((x0 + offset + 7, y, x0 + offset + 7, min(y + 4, y1 - 1)), fill=(70, 49, 34, 255))
    for _ in range(12):
        x = x0 + rng.randrange(16)
        y = y0 + rng.randrange(16)
        draw.point((x, y), fill=rng.choice([(151, 103, 58, 255), (86, 59, 39, 255)]))


def draw_wooden_upstairs(draw: ImageDraw.ImageDraw, index: int, seed: int) -> None:
    rng = random.Random(seed)
    x0, y0, _, _ = tile_rect(index)
    draw_grass(draw, index, seed + 3100, dense=False, flowers=False)

    # Dark recess behind the stairs.
    draw.rectangle((x0 + 3, y0 + 3, x0 + 12, y0 + 12), fill=(38, 31, 24, 255))
    draw.line((x0 + 3, y0 + 3, x0 + 12, y0 + 3), fill=(142, 107, 62, 255))
    draw.line((x0 + 3, y0 + 3, x0 + 3, y0 + 12), fill=(77, 55, 38, 255))

    # Wooden ascending steps, matching the classic stair silhouette.
    step_cols = [(156, 107, 58, 255), (128, 86, 50, 255), (92, 61, 40, 255)]
    for i in range(5):
        y = y0 + 11 - i * 2
        x = x0 + 4 + i
        draw.line((x, y, x0 + 12, y), fill=step_cols[i % len(step_cols)])
        draw.line((x, y + 1, x0 + 11, y + 1), fill=(71, 48, 34, 255))

    for _ in range(5):
        x = x0 + rng.randrange(4, 12)
        y = y0 + rng.randrange(4, 12)
        draw.point((x, y), fill=(181, 132, 70, 255))


def draw_mossy_downstairs(draw: ImageDraw.ImageDraw, index: int, seed: int) -> None:
    rng = random.Random(seed)
    x0, y0, _, _ = tile_rect(index)
    draw_grass(draw, index, seed + 3200, dense=False, flowers=True)

    # Stone-lined stair mouth.
    draw.ellipse((x0 + 2, y0 + 3, x0 + 13, y0 + 13), fill=(63, 76, 65, 255), outline=(168, 179, 151, 255))
    draw.ellipse((x0 + 4, y0 + 5, x0 + 11, y0 + 12), fill=(20, 29, 26, 255))
    draw.arc((x0 + 3, y0 + 4, x0 + 12, y0 + 13), 190, 345, fill=(96, 106, 91, 255))

    for i in range(3):
        y = y0 + 7 + i * 2
        draw.line((x0 + 5 - i, y, x0 + 10 + i, y), fill=(78, 84, 72, 255))

    # Moss creeping over the rim.
    for _ in range(13):
        x = x0 + rng.randrange(3, 13)
        y = y0 + rng.randrange(3, 9)
        draw.point((x, y), fill=rng.choice([(73, 136, 55, 255), (102, 163, 66, 255), (48, 98, 44, 255)]))


def draw_meadow_object(draw: ImageDraw.ImageDraw, index: int, seed: int, kind: str) -> None:
    draw_grass(draw, index, seed, dense=True, flowers=True)
    rng = random.Random(seed + 73)
    x0, y0, _, _ = tile_rect(index)
    if kind == "well":
        draw.ellipse((x0 + 3, y0 + 5, x0 + 12, y0 + 13), fill=(42, 79, 78, 255), outline=(185, 192, 154, 255))
        draw.ellipse((x0 + 5, y0 + 7, x0 + 10, y0 + 11), fill=(24, 54, 62, 255))
    elif kind == "pedestal":
        draw.rectangle((x0 + 5, y0 + 6, x0 + 10, y0 + 12), fill=(180, 184, 148, 255))
        draw.rectangle((x0 + 4, y0 + 11, x0 + 11, y0 + 13), fill=(126, 134, 104, 255))
        draw.point((x0 + 7, y0 + 5), fill=(255, 237, 113, 255))
    elif kind == "entrance":
        draw_wooden_upstairs(draw, index, seed)
    elif kind == "exit":
        draw_mossy_downstairs(draw, index, seed)


def draw_chasm(draw: ImageDraw.ImageDraw, index: int, seed: int, edge: str = "plain") -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=(8, 18, 13, 255))
    for _ in range(14):
        draw.point((x0 + rng.randrange(16), y0 + rng.randrange(16)), fill=(18, 38, 29, 255))
    if edge != "plain":
        for x in range(x0, x1):
            if rng.random() < 0.75:
                draw.point((x, y0), fill=(72, 129, 57, 255))
                draw.point((x, y0 + 1), fill=(42, 86, 40, 255))


def draw_water_stitch_from_template(
        out: Image.Image,
        template: Image.Image,
        water_texture: Image.Image,
        index: int,
        seed: int) -> None:
    rng = random.Random(seed)
    tile = Image.new("RGBA", (T, T), (0, 0, 0, 0))
    local = ImageDraw.Draw(tile)
    draw_grass(local, 0, seed + 4100, dense=False, flowers=False)
    src = template.crop(tile_rect(index)).convert("RGBA")
    water_colors = [
        water_texture.getpixel((x, y))
        for y in range(water_texture.height)
        for x in range(water_texture.width)
    ]
    avg_water = tuple(sum(c[i] for c in water_colors) // len(water_colors) for i in range(3))

    water_mask = [[False for _ in range(T)] for _ in range(T)]
    for y in range(T):
        for x in range(T):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            mx = max(r, g, b)
            mn = min(r, g, b)
            saturation = mx - mn
            water_mask[y][x] = (g > r + 8 and g >= b - 12 and mx > 38) or (
                    g >= r and g >= b - 18 and mx <= 70 and saturation > 10)

    for y in range(T):
        for x in range(T):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue

            mx = max(r, g, b)
            if water_mask[y][x] or mx < 35:
                # Water-side pixels must be transparent; the animated water layer is underneath.
                tile.putpixel((x, y), (0, 0, 0, 0))
            else:
                # Sewer stone/floor pixels become grass while preserving the original transition shape.
                shade = (r + g + b) // 3
                jitter = rng.randrange(-3, 4)
                water_dist = 99
                for yy in range(max(0, y - 4), min(T, y + 5)):
                    for xx in range(max(0, x - 4), min(T, x + 5)):
                        if water_mask[yy][xx]:
                            water_dist = min(water_dist, abs(yy - y) + abs(xx - x))
                near_water = water_dist <= 3
                blend = max(0, 4 - water_dist) if near_water else 0
                grass = (
                    max(42, min(88, 62 + (shade - 90) // 10 + jitter - blend)),
                    max(96, min(154, 122 + (shade - 90) // 8 + jitter + blend * 2)),
                    max(42, min(86, 52 + (shade - 90) // 14 + jitter + blend * 3)),
                    a,
                )
                tile.putpixel((x, y), grass)

    shore = (
        max(45, min(130, avg_water[0] + 8)),
        max(90, min(175, avg_water[1] + 20)),
        max(110, min(205, avg_water[2] + 28)),
        230,
    )
    foam = (
        max(80, min(180, avg_water[0] + 46)),
        max(135, min(220, avg_water[1] + 58)),
        max(150, min(235, avg_water[2] + 58)),
        190,
    )
    for y in range(T):
        for x in range(T):
            if not water_mask[y][x]:
                continue
            touches_land = False
            for dy, dx in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                yy = y + dy
                xx = x + dx
                if 0 <= yy < T and 0 <= xx < T and not water_mask[yy][xx]:
                    rr, gg, bb, aa = src.getpixel((xx, yy))
                    if aa > 0 and max(rr, gg, bb) >= 35:
                        touches_land = True
                        break
            if touches_land:
                tile.putpixel((x, y), shore)

    # A few tiny bright grass/flower specks on the land side make the transition match the meadow.
    candidates = []
    for y in range(2, T - 2):
        for x in range(2, T - 2):
            if water_mask[y][x] or tile.getpixel((x, y))[3] == 0:
                continue
            if any(water_mask[yy][xx]
                   for yy in range(max(0, y - 3), min(T, y + 4))
                   for xx in range(max(0, x - 3), min(T, x + 4))):
                candidates.append((x, y))
    for _ in range(min(3, len(candidates))):
        x, y = rng.choice(candidates)
        candidates.remove((x, y))
        r, g, b, a = tile.getpixel((x, y))
        if rng.random() < 0.35:
            tile.putpixel((x, y), foam)
        else:
            tile.putpixel((x, y), rng.choice([
                (242, 226, 94, a),
                (235, 156, 204, a),
                (204, 237, 248, a),
            ]))

    # Replace the whole tile, including transparent pixels. Using the tile as a mask
    # leaves opaque pixels from the base atlas behind in water-side transparent areas.
    out.paste(tile, tile_rect(index))


def draw_stone_base(draw: ImageDraw.ImageDraw, index: int, seed: int, moss: bool = False) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw_grass(draw, index, seed + 2000, dense=False, flowers=False)
    pts = [
        (x0 + 2, y0 + 5),
        (x0 + 8, y0 + 2),
        (x0 + 14, y0 + 5),
        (x0 + 15, y0 + 11),
        (x0 + 11, y0 + 15),
        (x0 + 4, y0 + 15),
        (x0 + 1, y0 + 11),
    ]
    draw.polygon(pts, fill=(142, 151, 132, 255), outline=(70, 79, 71, 255))
    draw.line((x0 + 4, y0 + 6, x0 + 10, y0 + 4), fill=(206, 214, 190, 255))
    draw.line((x0 + 5, y0 + 13, x0 + 12, y0 + 13), fill=(95, 105, 93, 255))
    draw.point((x0 + 8, y0 + 9), fill=(94, 104, 92, 255))
    draw.point((x0 + 12, y0 + 10), fill=(94, 104, 92, 255))
    if moss:
        for _ in range(12):
            x = x0 + rng.randrange(3, 14)
            y = y0 + rng.randrange(5, 14)
            draw.point((x, y), fill=rng.choice([(72, 129, 57, 255), (94, 151, 65, 255)]))


def draw_stone_lower(draw: ImageDraw.ImageDraw, index: int, seed: int, moss: bool = False) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw_grass(draw, index, seed + 2100, dense=False, flowers=False)
    if moss:
        rocks = [
            [(x0 + 1, y0 + 1), (x0 + 8, y0), (x0 + 15, y0 + 2), (x0 + 15, y0 + 12), (x0 + 11, y0 + 15), (x0 + 3, y0 + 15), (x0, y0 + 8)],
            [(x0 + 4, y0 + 7), (x0 + 10, y0 + 5), (x0 + 15, y0 + 8), (x0 + 14, y0 + 15), (x0 + 6, y0 + 15), (x0 + 2, y0 + 12)],
        ]
    else:
        rocks = [
            [(x0, y0 + 1), (x0 + 7, y0), (x0 + 15, y0 + 2), (x0 + 15, y0 + 13), (x0 + 10, y0 + 15), (x0 + 2, y0 + 15), (x0, y0 + 8)],
        ]
    for pts in rocks:
        draw.polygon(pts, fill=(138, 147, 130, 255), outline=(66, 74, 68, 255))
    draw.line((x0 + 4, y0 + 4, x0 + 11, y0 + 2), fill=(207, 213, 191, 255))
    draw.line((x0 + 4, y0 + 13, x0 + 12, y0 + 14), fill=(88, 97, 88, 255))
    if moss:
        for _ in range(18):
            x = x0 + rng.randrange(2, 15)
            y = y0 + rng.randrange(2, 15)
            draw.point((x, y), fill=rng.choice([(67, 125, 54, 255), (95, 155, 66, 255), (47, 94, 44, 255)]))


def draw_stone_overhang(draw: ImageDraw.ImageDraw, index: int, seed: int, moss: bool = False) -> None:
    x0, y0, x1, y1 = tile_rect(index)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=(0, 0, 0, 0))


def draw_water_texture(size: int = 32) -> Image.Image:
    src = Image.open(ENV / "water0.png").convert("RGBA")
    water = Image.new("RGBA", src.size)
    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            # Shift sewer green water toward a cleaner pond blue while retaining the source animation texture.
            nr = int(r * 0.72 + 24)
            ng = int(g * 0.92 + 18)
            nb = int(b * 1.18 + 34)
            water.putpixel((x, y), (min(nr, 255), min(ng, 255), min(nb, 255), a))
    return water


def draw_tree_wall(draw: ImageDraw.ImageDraw, index: int, seed: int, variant: str = "canopy") -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=(23, 55, 30, 255))

    if variant in {"trunk", "raised"}:
        draw.rectangle((x0 + 5, y0 + 2, x0 + 10, y1 - 1), fill=(86, 64, 42, 255))
        draw.line((x0 + 6, y0 + 3, x0 + 6, y1 - 2), fill=(126, 91, 54, 255))
        draw.line((x0 + 9, y0 + 4, x0 + 9, y1 - 2), fill=(48, 38, 30, 255))
    else:
        for _ in range(22):
            cx = x0 + rng.randrange(16)
            cy = y0 + rng.randrange(2, 15)
            c = rng.choice(
                [
                    (30, 76, 34, 255),
                    (49, 105, 43, 255),
                    (72, 137, 54, 255),
                    (92, 158, 62, 255),
                ]
            )
            draw.rectangle((cx, cy, min(cx + 1, x1 - 1), min(cy + 1, y1 - 1)), fill=c)
        draw.rectangle((x0 + 6, y0 + 8, x0 + 9, y1 - 1), fill=(82, 58, 38, 255))

    if variant == "deco":
        for _ in range(5):
            x = x0 + rng.randrange(2, 14)
            y = y0 + rng.randrange(2, 11)
            draw.point((x, y), fill=(246, 220, 95, 255))

    draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=(12, 31, 20, 255))


def draw_leaf_overhang(draw: ImageDraw.ImageDraw, index: int, seed: int, prison_hint: bool = False) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=(0, 0, 0, 255))
    for _ in range(36):
        x = x0 + rng.randrange(16)
        y = y0 + rng.randrange(1, 12)
        draw.point((x, y), fill=rng.choice([(35, 88, 36, 255), (75, 142, 54, 255), (111, 176, 70, 255)]))
    draw.rectangle((x0, y0 + 12, x1 - 1, y1 - 1), fill=(28, 38, 27, 255))
    if prison_hint:
        draw.rectangle((x0 + 1, y0 + 13, x1 - 2, y1 - 1), fill=(112, 105, 90, 255))
        for x in range(x0 + 2, x1 - 1, 4):
            draw.line((x, y0 + 13, x, y1 - 1), fill=(71, 68, 61, 255))


def main() -> None:
    prison = Image.open(ENV / "tiles_prison.png").convert("RGBA")
    sewers = Image.open(ENV / "tiles_sewers.png").convert("RGBA")
    caves = Image.open(ENV / "tiles_caves.png").convert("RGBA")
    out = prison.copy()
    draw = ImageDraw.Draw(out)
    water = draw_water_texture()

    # Ground row: outdoor floor slots become grass; special floor slots become indoor wood.
    for idx, seed in [(0, 1), (1, 2), (2, 3), (3, 4), (6, 6), (7, 7), (8, 8), (9, 9), (12, 12)]:
        draw_grass(draw, idx, seed, dense=idx in {2, 8}, flowers=idx in {1, 4, 7, 10, 12})
    for idx, seed in [(4, 104), (10, 110)]:
        draw_wood_floor(draw, idx, seed)
    paste_tile(out, sewers, 16, 16)
    paste_tile(out, sewers, 17, 17)
    draw_meadow_object(draw, 18, 18, "well")
    draw_meadow_object(draw, 19, 19, "well")
    draw_meadow_object(draw, 20, 20, "pedestal")
    paste_tile(out, sewers, 22, 22)

    for i in range(8):
        draw_chasm(draw, 24 + i, 100 + i, edge="moss")

    # Water stitching slots reuse the sewer shoreline silhouettes, but remap the land side to grass.
    for idx in range(32, 48):
        draw_water_stitch_from_template(out, sewers, water, idx, 200 + idx)

    # Wall and door slots intentionally stay prison-stone, so houses read as buildings.
    for idx in range(48, 64):
        paste_tile(out, prison, idx, idx)

    # Other flat objects.
    draw_meadow_object(draw, 64, 64, "well")
    paste_tile(out, sewers, 65, 65)
    for idx in [66, 67, 69, 70]:
        draw_grass(draw, idx, 400 + idx, dense=True, flowers=idx in {66, 69})
    for idx in [72, 73]:
        paste_tile(out, prison, idx, idx)
    draw_stone_base(draw, 74, 474, moss=False)
    draw_stone_base(draw, 75, 475, moss=True)
    for idx in [76, 77, 78]:
        paste_tile(out, prison, idx, idx)

    # Raised walls, doors, and wall overhangs stay compatible prison masonry.
    for idx in range(80, 144):
        paste_tile(out, prison, idx, idx)
    for idx in [120, 121, 122, 123, 125, 126]:
        draw_grass(draw, idx, 540 + idx, dense=True, flowers=True)
    draw_stone_lower(draw, 130, 690, moss=False)
    draw_stone_lower(draw, 131, 691, moss=True)
    for idx in [132, 133, 134]:
        paste_tile(out, caves, idx, idx)

    for idx in range(144, 192):
        paste_tile(out, prison, idx, idx)
    for idx in range(192, 224):
        paste_tile(out, prison, idx, idx)

    for idx in range(224, 256):
        paste_tile(out, prison, idx, idx)
    draw_stone_overhang(draw, 242, 1142, moss=False)
    draw_stone_overhang(draw, 243, 1143, moss=True)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    out.save(OUT)

    # Preview: 3x nearest-neighbor scale with a simple grid.
    scale = 3
    preview = out.resize((out.width * scale, out.height * scale), Image.Resampling.NEAREST)
    pdraw = ImageDraw.Draw(preview)
    grid = (255, 255, 255, 38)
    for x in range(0, preview.width + 1, T * scale):
        pdraw.line((x, 0, x, preview.height), fill=grid)
    for y in range(0, preview.height + 1, T * scale):
        pdraw.line((0, y, preview.width, y), fill=grid)
    preview.save(PREVIEW)

    water.save(WATER_OUT)

    # A small semantic preview: not used by the game, only for judging the tiles together.
    scene_tiles = [
        [48, 48, 52, 48, 48, 48, 52, 48, 48, 48, 48, 48],
        [48, 0, 1, 2, 0, 33, 34, 0, 1, 2, 20, 48],
        [52, 1, 2, 0, 32, 33, 34, 35, 0, 1, 17, 48],
        [48, 2, 0, 64, 36, 37, 38, 39, 1, 2, 0, 52],
        [48, 1, 2, 0, 40, 41, 42, 43, 0, 1, 2, 48],
        [48, 0, 1, 66, 0, 74, 75, 0, 67, 2, 0, 48],
        [52, 48, 56, 48, 48, 48, 52, 48, 48, 56, 48, 52],
    ]
    scene = Image.new("RGBA", (len(scene_tiles[0]) * T, len(scene_tiles) * T), (0, 0, 0, 0))
    for y, row in enumerate(scene_tiles):
        for x, tile in enumerate(row):
            if 32 <= tile < 48:
                scene.paste(water.crop((0, 0, T, T)), (x * T, y * T))
            else:
                scene.paste(out.crop(tile_rect(tile)), (x * T, y * T))
    scene = scene.resize((scene.width * 4, scene.height * 4), Image.Resampling.NEAREST)
    scene.save(SCENE_PREVIEW)

    print(OUT)
    print(WATER_OUT)
    print(PREVIEW)
    print(SCENE_PREVIEW)


if __name__ == "__main__":
    main()
