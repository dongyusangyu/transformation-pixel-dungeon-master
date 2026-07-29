from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
OUT = ENV / "tiles_surface_lush.png"
WATER_OUT = ENV / "water_surface_lush.png"

T = 16
W = 16


def tile_rect(index: int) -> tuple[int, int, int, int]:
    x = index % W * T
    y = index // W * T
    return x, y, x + T, y + T


def paste_tile(dst: Image.Image, src: Image.Image, src_index: int, dst_index: int) -> None:
    dst.paste(src.crop(tile_rect(src_index)), tile_rect(dst_index))


def composite_reference_object(
        dst: Image.Image,
        reference: Image.Image,
        object_index: int,
        reference_floor_index: int,
        dst_index: int) -> None:
    """Transfer an object and its shadow without carrying over its source floor."""
    obj = reference.crop(tile_rect(object_index)).convert("RGBA")
    source_floor = reference.crop(tile_rect(reference_floor_index)).convert("RGBA")
    target = dst.crop(tile_rect(dst_index)).convert("RGBA")
    overlay = Image.new("RGBA", (T, T), (0, 0, 0, 0))

    for y in range(T):
        for x in range(T):
            pixel = obj.getpixel((x, y))
            if pixel != source_floor.getpixel((x, y)):
                overlay.putpixel((x, y), pixel)

    dst.paste(Image.alpha_composite(target, overlay), tile_rect(dst_index))


def composite_reference_inset(
        dst: Image.Image,
        reference: Image.Image,
        object_index: int,
        dst_index: int,
        margin: int = 2) -> None:
    """Keep a reference object's central silhouette while exposing the new floor at its edge."""
    src = reference.crop(tile_rect(object_index)).convert("RGBA")
    target = dst.crop(tile_rect(dst_index)).convert("RGBA")
    target.paste(src.crop((margin, margin, T - margin, T - margin)), (margin, margin))
    dst.paste(target, tile_rect(dst_index))


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

    # Sparse, directional grass tufts create larger shapes than single-pixel noise.
    # They remain away from tile borders, so neighboring tiles do not gain a grid seam.
    for _ in range(3 if dense else 1):
        x = x0 + rng.randrange(3, 13)
        y = y0 + rng.randrange(7, 14)
        shade = rng.choice([(45, 101, 41, 255), (82, 151, 57, 255), (107, 169, 67, 255)])
        draw.line((x, y, x - 1, y - 2), fill=shade)
        draw.line((x + 1, y, x + 1, y - 3), fill=shade)

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


def draw_village_path(draw: ImageDraw.ImageDraw, index: int, seed: int) -> None:
    """Draw seamless packed earth with sparse stones and grass encroachment."""
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    earth = (119, 102, 70, 255)
    earth_dark = (91, 80, 59, 255)
    earth_light = (151, 130, 85, 255)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=earth)

    for _ in range(30):
        x = x0 + rng.randrange(T)
        y = y0 + rng.randrange(T)
        draw.point((x, y), fill=rng.choice([earth_dark, earth, earth_light]))

    stones = [
        (rng.randrange(2, 7), rng.randrange(2, 7)),
        (rng.randrange(9, 14), rng.randrange(4, 10)),
        (rng.randrange(3, 12), rng.randrange(11, 15)),
    ]
    for x, y in stones:
        px, py = x0 + x, y0 + y
        draw.point((px, py), fill=(185, 177, 143, 255))
        draw.point((min(px + 1, x1 - 1), py), fill=(128, 125, 103, 255))
        if rng.random() < 0.5:
            draw.point((px, min(py + 1, y1 - 1)), fill=(78, 76, 66, 255))

    # Broken edge tufts suggest that the meadow is reclaiming the path without creating tile seams.
    grass_dark = (48, 99, 43, 255)
    grass_mid = (75, 140, 55, 255)
    for _ in range(5):
        side = rng.randrange(4)
        if side < 2:
            x = x0 + rng.randrange(1, 15)
            y = y0 + (rng.randrange(3) if side == 0 else 13 + rng.randrange(3))
        else:
            x = x0 + (rng.randrange(3) if side == 2 else 13 + rng.randrange(3))
            y = y0 + rng.randrange(1, 15)
        draw.point((x, y), fill=rng.choice([grass_dark, grass_mid]))


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
    x0, y0, _, _ = tile_rect(index)
    if kind == "pedestal":
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
    src = template.crop(tile_rect(index)).convert("RGBA")
    water_colors = [
        water_texture.getpixel((x, y))
        for y in range(water_texture.height)
        for x in range(water_texture.width)
    ]
    avg_water = tuple(sum(c[i] for c in water_colors) // len(water_colors) for i in range(3))

    water_mask = [[False for _ in range(T)] for _ in range(T)]
    edge_mask = [[False for _ in range(T)] for _ in range(T)]
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
            edge_mask[y][x] = water_mask[y][x] or mx < 35

    for y in range(T):
        for x in range(T):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                # Transparent template pixels are the animated-water window.
                continue

            if edge_mask[y][x]:
                wr, wg, wb, _ = water_texture.getpixel(
                    (x % water_texture.width, y % water_texture.height)
                )
                if not water_mask[y][x]:
                    wr, wg, wb = max(0, wr - 24), max(0, wg - 24), max(0, wb - 18)
                tile.putpixel((x, y), (wr, wg, wb, a))
            else:
                # Sewer stone/floor pixels become grass while preserving the original transition shape.
                shade = (r + g + b) // 3
                jitter = rng.randrange(-3, 4)
                water_dist = 99
                for yy in range(max(0, y - 4), min(T, y + 5)):
                    for xx in range(max(0, x - 4), min(T, x + 5)):
                        if edge_mask[yy][xx] or src.getpixel((xx, yy))[3] == 0:
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
                _, _, _, a = src.getpixel((x, y))
                tile.putpixel((x, y), (shore[0], shore[1], shore[2], a))

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
            tile.putpixel((x, y), (foam[0], foam[1], foam[2], a))
        else:
            tile.putpixel((x, y), rng.choice([
                (242, 226, 94, a),
                (235, 156, 204, a),
                (204, 237, 248, a),
            ]))

    # Replace the whole tile, including transparent pixels. Using the tile as a mask
    # leaves opaque pixels from the base atlas behind in water-side transparent areas.
    out.paste(tile, tile_rect(index))


def draw_tree_root_base(draw: ImageDraw.ImageDraw, index: int, seed: int, old_tree: bool = False) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw_grass(draw, index, seed + 2000, dense=True, flowers=old_tree)
    bark_dark = (55, 43, 31, 255)
    bark_mid = (95, 65, 38, 255)
    bark_light = (143, 96, 51, 255)
    # Roots are a separate, walkable visual base; the collision trunk is in the raised slot.
    root_lines = [
        (x0 + 7, y0 + 9, x0 + 3, y1 - 2),
        (x0 + 8, y0 + 10, x0 + 12, y1 - 2),
        (x0 + 7, y0 + 12, x0 + 1, y1 - 1),
    ]
    if old_tree:
        root_lines.extend([(x0 + 9, y0 + 9, x1 - 1, y0 + 13), (x0 + 6, y0 + 10, x0 + 4, y1 - 1)])
    for x1r, y1r, x2r, y2r in root_lines:
        draw.line((x1r, y1r, x2r, y2r), fill=bark_dark, width=2)
        draw.line((x1r, y1r, x2r, y2r - 1), fill=bark_mid)
    draw.line((x0 + 7, y0 + 10, x0 + 8, y1 - 2), fill=bark_light)
    for _ in range(5 if old_tree else 3):
        draw.point((x0 + rng.randrange(2, 14), y0 + rng.randrange(11, 16)), fill=(75, 133, 53, 255))


def draw_tree_trunk(draw: ImageDraw.ImageDraw, index: int, seed: int, old_tree: bool = False) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw_grass(draw, index, seed + 2100, dense=False, flowers=False)
    bark_dark = (48, 39, 29, 255)
    bark_mid = (93, 63, 37, 255)
    bark_light = (151, 101, 54, 255)
    trunk_width = 6 if old_tree else 4
    trunk_left = x0 + (T - trunk_width) // 2
    trunk_right = trunk_left + trunk_width - 1
    trunk_start = y0 + (6 if old_tree else 5)
    # The exposed trunk begins below the upper leaf mass; only its lower half remains fully visible.
    draw.rectangle((trunk_left, trunk_start, trunk_right, y1 - 1), fill=bark_dark)
    draw.rectangle((trunk_left + 1, trunk_start + 1, trunk_right - 1, y1 - 2), fill=bark_mid)
    draw.line((trunk_left + 1, trunk_start + 2, trunk_left + 1, y1 - 3), fill=bark_light)
    draw.line((trunk_right, trunk_start + 3, trunk_right, y1 - 2), fill=(63, 44, 31, 255))
    for _ in range(5 if old_tree else 3):
        x = rng.randrange(trunk_left + 1, trunk_right - 1)
        y = rng.randrange(trunk_start + 1, y1 - 2)
        draw.line((x, y, x - 1, min(y + 2, y1 - 2)), fill=(56, 43, 31, 255))

    leaf_dark = (27, 72, 35, 255)
    leaf_mid = (48, 111, 44, 255)
    leaf_light = (88, 151, 57, 255)
    # Upper foliage overlaps the trunk and extends toward both tile edges, allowing forest clusters to join.
    leaf_shapes = [
        [(x0 + 2, y0 + 5), (x0 + 3, y0 + 2), (x0 + 7, y0), (x0 + 12, y0 + 2), (x0 + 13, y0 + 5), (x0 + 11, y0 + 8), (x0 + 4, y0 + 8)],
        [(x0 + 1, y0 + 7), (x0 + 4, y0 + 4), (x0 + 10, y0 + 4), (x0 + 14, y0 + 7), (x0 + 12, y0 + 9), (x0 + 3, y0 + 9)],
    ]
    if old_tree:
        leaf_shapes.append([(x0 + 1, y0 + 4), (x0 + 4, y0 + 1), (x0 + 12, y0 + 1), (x0 + 14, y0 + 5), (x0 + 14, y0 + 9), (x0 + 1, y0 + 9)])
    for shape in leaf_shapes:
        draw.polygon(shape, fill=leaf_dark)
    for _ in range(48 if old_tree else 36):
        x = x0 + rng.randrange(1, 15)
        y = y0 + rng.randrange(1, 9)
        if rng.random() < 0.7:
            draw.point((x, y), fill=rng.choice([leaf_mid, leaf_mid, leaf_light, leaf_dark]))
    if old_tree:
        for _ in range(12):
            x = rng.randrange(trunk_left, trunk_right)
            y = rng.randrange(y0 + 3, y1 - 1)
            draw.point((x, y), fill=rng.choice([(63, 117, 49, 255), (91, 151, 62, 255)]))


def draw_tree_canopy(draw: ImageDraw.ImageDraw, index: int, seed: int, old_tree: bool = False) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=(0, 0, 0, 0))

    dark = (25, 65, 33, 255)
    mid = (47, 108, 45, 255)
    light = (85, 151, 58, 255)
    sun = (122, 178, 69, 255)
    # Broad leaf masses overlap at both side edges, so adjacent trees visually join into forest canopy.
    clusters = [(0, 6, 8, 15), (2, 2, 14, 12), (8, 5, 16, 15), (5, 0, 12, 9)]
    if old_tree:
        clusters.extend([(0, 2, 9, 14), (7, 1, 16, 14), (2, 0, 14, 9)])
    for left, top, right, bottom in clusters:
        for y in range(y0 + top, y0 + bottom):
            for x in range(x0 + left, x0 + right):
                if rng.random() < 0.06:
                    continue
                colour = dark if y > y0 + 9 else rng.choice([mid, light, sun])
                draw.point((x, y), fill=colour)
    # The connector is exactly the raised-trunk width, so the two layers read as a single tree.
    trunk_width = 6 if old_tree else 4
    trunk_left = x0 + (T - trunk_width) // 2
    trunk_right = trunk_left + trunk_width - 1
    draw.rectangle((trunk_left, y0 + 10, trunk_right, y1 - 1), fill=(63, 45, 31, 255))
    draw.line((trunk_left + 1, y0 + 10, trunk_left + 1, y1 - 1), fill=(114, 75, 42, 255))
    draw.line((trunk_left + 1, y0 + 10, x0 + 3, y0 + 7), fill=(70, 49, 32, 255))
    if old_tree:
        draw.line((trunk_right - 1, y0 + 11, x0 + 13, y0 + 8), fill=(70, 49, 32, 255))
    # Lower leaf fringe breaks the straight connector and visually merges into the upper trunk foliage.
    for x in range(x0 + 1, x1 - 1):
        if rng.random() < (0.7 if old_tree else 0.55):
            draw.point((x, y0 + rng.choice([12, 13, 14])), fill=rng.choice([dark, mid, light]))
    for _ in range(5 if old_tree else 3):
        x = x0 + rng.randrange(2, 14)
        y = y0 + rng.randrange(3, 12)
        if draw._image.getpixel((x, y))[3] > 0:
            draw.point((x, y), fill=(239, 214, 91, 255))
    # Keep deliberately stepped transparent corners: dense foliage still needs a readable pixel-art silhouette.
    for left, top, right, bottom in ((0, 0, 2, 2), (13, 0, 15, 2), (0, 13, 2, 15), (13, 13, 15, 15)):
        draw.rectangle((x0 + left, y0 + top, x0 + right, y0 + bottom), fill=(0, 0, 0, 0))


def decorate_living_masonry(draw: ImageDraw.ImageDraw, index: int, seed: int, vine: bool = False) -> None:
    rng = random.Random(seed)
    x0, y0, x1, y1 = tile_rect(index)
    # Touch only a corner and the lower mortar line; the prison silhouette remains readable as a house wall.
    for _ in range(8):
        x = x0 + rng.randrange(1, 15)
        y = y0 + rng.choice([rng.randrange(1, 4), rng.randrange(12, 15)])
        draw.point((x, y), fill=rng.choice([(99, 136, 74, 255), (74, 113, 61, 255), (165, 169, 137, 255)]))
    if vine:
        x = x0 + rng.choice([2, 13])
        draw.line((x, y0 + 1, x - 1, y0 + 9), fill=(45, 94, 43, 255))
        for y in range(y0 + 3, y0 + 10, 3):
            draw.point((x + rng.choice([-1, 1]), y), fill=(91, 151, 61, 255))


def decorate_locked_door(draw: ImageDraw.ImageDraw, index: int, raised: bool) -> None:
    """Add a compact bar and padlock while retaining the prison door silhouette."""
    x0, y0, _, _ = tile_rect(index)
    bar_y = y0 + (8 if raised else 9)
    draw.rectangle((x0 + 3, bar_y - 1, x0 + 12, bar_y + 1), fill=(45, 43, 39, 255))
    draw.line((x0 + 4, bar_y - 1, x0 + 11, bar_y - 1), fill=(154, 151, 132, 255))
    draw.line((x0 + 4, bar_y, x0 + 11, bar_y), fill=(91, 92, 84, 255))

    lock_x = x0 + 7
    draw.rectangle((lock_x - 2, bar_y, lock_x + 2, bar_y + 4), fill=(55, 45, 25, 255))
    draw.rectangle((lock_x - 1, bar_y + 1, lock_x + 1, bar_y + 3), fill=(205, 166, 62, 255))
    draw.line((lock_x - 1, bar_y - 2, lock_x + 1, bar_y - 2), fill=(229, 197, 91, 255))
    draw.point((lock_x - 2, bar_y - 1), fill=(229, 197, 91, 255))
    draw.point((lock_x + 2, bar_y - 1), fill=(229, 197, 91, 255))


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
    crystal_caves = Image.open(ENV / "tiles_caves_crystal.png").convert("RGBA")
    out = prison.copy()
    draw = ImageDraw.Draw(out)
    water = draw_water_texture()

    # Ground row: outdoor floor slots become grass; special floor slots become indoor wood.
    for idx, seed in [(0, 1), (2, 3), (3, 4), (6, 6), (8, 8), (9, 9), (12, 12)]:
        draw_grass(draw, idx, seed, dense=idx in {2, 8}, flowers=idx in {1, 4, 7, 10, 12})
    draw_village_path(draw, 1, 201)
    draw_village_path(draw, 7, 207)
    for idx, seed in [(4, 104), (10, 110)]:
        draw_wood_floor(draw, idx, seed)
    # Floor 0 reverses the usual travel direction: the manor's up-stair exit is indoors,
    # while the non-interactive down-stair arrival marker sits on the village grass.
    paste_tile(out, out, 4, 16)
    composite_reference_object(out, sewers, 16, 0, 16)
    paste_tile(out, out, 0, 17)
    composite_reference_inset(out, sewers, 17, 17, margin=2)
    paste_tile(out, sewers, 18, 18)
    paste_tile(out, sewers, 19, 19)
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
    for idx in (48, 49, 50, 52, 53, 54, 56, 57):
        decorate_living_masonry(draw, idx, 900 + idx, vine=idx in {49, 53, 57})
    decorate_locked_door(draw, 58, raised=False)

    # Other flat objects.
    paste_tile(out, sewers, 64, 64)
    paste_tile(out, sewers, 65, 65)
    for idx in [66, 67, 69, 70]:
        draw_grass(draw, idx, 400 + idx, dense=True, flowers=idx in {66, 69})
    # Rat-statue flat visuals: normal terrain uses meadow, special terrain uses indoor wood.
    paste_tile(out, out, 0, 72)
    paste_tile(out, out, 4, 73)
    composite_reference_object(out, crystal_caves, 72, 0, 72)
    composite_reference_object(out, crystal_caves, 73, 4, 73)
    # Flat decoration variants are plain meadow; tree roots are intentionally not drawn.
    draw_grass(draw, 74, 474, dense=True, flowers=False)
    draw_grass(draw, 75, 475, dense=True, flowers=True)
    for idx in [76, 77, 78]:
        paste_tile(out, prison, idx, idx)

    # Raised walls, doors, and wall overhangs stay compatible prison masonry.
    for idx in range(80, 144):
        paste_tile(out, prison, idx, idx)
    for idx in (80, 81, 82, 84, 85, 86, 96, 97, 98):
        decorate_living_masonry(draw, idx, 1000 + idx, vine=idx in {81, 85, 97})
    decorate_locked_door(draw, 114, raised=True)
    for idx in [120, 121, 122, 123, 125, 126]:
        draw_grass(draw, idx, 540 + idx, dense=True, flowers=True)
    # Raised statue bodies use the same backgrounds as their flat counterparts.
    paste_tile(out, out, 0, 128)
    paste_tile(out, out, 4, 129)
    composite_reference_object(out, crystal_caves, 128, 0, 128)
    composite_reference_object(out, crystal_caves, 129, 4, 129)
    draw_tree_trunk(draw, 130, 690, old_tree=False)
    draw_tree_trunk(draw, 131, 691, old_tree=True)
    for idx in [132, 133, 134]:
        paste_tile(out, caves, idx, idx)

    for idx in range(144, 192):
        paste_tile(out, prison, idx, idx)
    for idx in range(192, 224):
        paste_tile(out, prison, idx, idx)

    for idx in range(224, 256):
        paste_tile(out, prison, idx, idx)
    paste_tile(out, crystal_caves, 240, 240)
    paste_tile(out, crystal_caves, 241, 241)
    draw_tree_canopy(draw, 242, 1142, old_tree=False)
    draw_tree_canopy(draw, 243, 1143, old_tree=True)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    out.save(OUT)
    water.save(WATER_OUT)

    print(OUT)
    print(WATER_OUT)


if __name__ == "__main__":
    main()
