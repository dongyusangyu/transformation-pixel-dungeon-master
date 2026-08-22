from __future__ import annotations

import json
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont
from scipy import ndimage


ROOT = Path(__file__).resolve().parent
CONCEPT = ROOT / "concepts" / "gentleman_elf_action_sheet.png"
OUT_DIR = ROOT / "outputs"
FRAME = 32
FRAMES = 25

TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (5, 17, 20, 255)
DEEP = (8, 51, 47, 255)
SHADOW = (14, 91, 75, 255)
MID = (22, 139, 101, 255)
GREEN = (38, 190, 132, 255)
LIGHT = (91, 231, 167, 255)
SPEC = (176, 255, 220, 255)
WING_DARK = (29, 151, 164, 255)
WING = (62, 218, 220, 255)
WING_LIGHT = (190, 255, 248, 255)
WHITE = (239, 255, 249, 255)
TIE = (7, 14, 20, 255)
TONGUE = (28, 118, 91, 255)

PALETTE = np.array([
    OUTLINE[:3], DEEP[:3], SHADOW[:3], MID[:3], GREEN[:3], LIGHT[:3], SPEC[:3],
    WING_DARK[:3], WING[:3], WING_LIGHT[:3], WHITE[:3], TIE[:3], TONGUE[:3],
], dtype=np.int32)


def foreground_mask(cell: Image.Image) -> np.ndarray:
    rgb = np.asarray(cell.convert("RGB"), dtype=np.int16)
    chroma = rgb.max(axis=2) - rgb.min(axis=2)
    mean = rgb.mean(axis=2)
    mask = (chroma > 19) | (mean < 48) | (mean > 175)
    mask = ndimage.binary_opening(mask, iterations=1)
    mask = ndimage.binary_closing(mask, iterations=2)
    labels, count = ndimage.label(mask)
    if count:
        sizes = ndimage.sum(mask, labels, range(1, count + 1))
        keep = np.zeros(count + 1, dtype=bool)
        keep[1:] = sizes >= 45
        mask = keep[labels]
    return mask


def crop_pose(sheet: Image.Image, index: int) -> Image.Image:
    cols, rows = 5, 2
    col, row = index % cols, index // cols
    x0 = round(col * sheet.width / cols)
    x1 = round((col + 1) * sheet.width / cols)
    y0 = round(row * sheet.height / rows)
    y1 = round((row + 1) * sheet.height / rows)
    cell = sheet.crop((x0, y0, x1, y1)).convert("RGBA")
    mask = foreground_mask(cell)
    ys, xs = np.where(mask)
    if not len(xs):
        raise RuntimeError(f"No foreground detected in concept pose {index}")
    pad = 3
    left, right = max(0, xs.min() - pad), min(cell.width, xs.max() + pad + 1)
    top, bottom = max(0, ys.min() - pad), min(cell.height, ys.max() + pad + 1)
    rgba = np.asarray(cell, dtype=np.uint8).copy()
    rgba[:, :, 3] = np.where(mask, 255, 0).astype(np.uint8)
    rgba[~mask, :3] = 0
    return Image.fromarray(rgba, "RGBA").crop((left, top, right, bottom))


def quantize_rgba(image: Image.Image) -> Image.Image:
    arr = np.asarray(image.convert("RGBA"), dtype=np.uint8).copy()
    visible = arr[:, :, 3] >= 64
    pixels = arr[:, :, :3].astype(np.int32)
    distances = ((pixels[:, :, None, :] - PALETTE[None, None, :, :]) ** 2).sum(axis=3)
    nearest = PALETTE[distances.argmin(axis=2)].astype(np.uint8)
    arr[:, :, :3] = np.where(visible[:, :, None], nearest, 0)
    arr[:, :, 3] = np.where(visible, 255, 0).astype(np.uint8)
    return Image.fromarray(arr, "RGBA")


def outline_and_repair(image: Image.Image, death: bool = False, repair_identity: bool = True) -> Image.Image:
    arr = np.asarray(image.convert("RGBA"), dtype=np.uint8).copy()
    mask = arr[:, :, 3] == 255
    edge = mask & ~ndimage.binary_erosion(mask)
    arr[edge] = OUTLINE
    repaired = Image.fromarray(arr, "RGBA")
    d = ImageDraw.Draw(repaired)
    bbox = repaired.getbbox()
    if not bbox:
        return repaired
    left, top, right, bottom = bbox
    if not death and repair_identity:
        # These are identity repairs, not source sampling: a crisp hat/collar/tie survives at 1x.
        hx = min(26, max(12, left + (right - left) * 3 // 5))
        hy = max(1, top)
        d.rectangle((hx, hy, hx + 3, hy + 1), fill=OUTLINE)
        d.rectangle((hx, max(0, hy - 1), hx + 2, hy), fill=WHITE)
        tx = min(24, max(12, left + (right - left) * 3 // 5))
        ty = min(14, max(8, top + (bottom - top) // 3))
        d.polygon([(tx - 2, ty), (tx, ty + 1), (tx + 2, ty)], fill=WHITE)
        d.polygon([(tx, ty + 1), (tx + 2, ty + 8), (tx, ty + 10), (tx - 1, ty + 3)], fill=TIE)
    return repaired


def fit_pose(pose: Image.Image, max_w: int = 30, max_h: int = 30, y_bias: int = 1) -> Image.Image:
    scale = min(max_w / pose.width, max_h / pose.height)
    size = (max(1, round(pose.width * scale)), max(1, round(pose.height * scale)))
    pooled = pose.resize(size, Image.Resampling.BOX)
    canvas = Image.new("RGBA", (FRAME, FRAME), TRANSPARENT)
    x = (FRAME - size[0]) // 2
    y = min(FRAME - size[1], max(0, FRAME - size[1] - y_bias))
    canvas.alpha_composite(pooled, (x, y))
    return quantize_rgba(canvas)


def translate(image: Image.Image, x: int = 0, y: int = 0) -> Image.Image:
    out = Image.new("RGBA", image.size, TRANSPARENT)
    out.alpha_composite(image, (x, y))
    return out


def stretch(image: Image.Image, width: int, height: int, x: int, y: int) -> Image.Image:
    bbox = image.getbbox()
    if not bbox:
        return image.copy()
    crop = image.crop(bbox).resize((width, height), Image.Resampling.NEAREST)
    out = Image.new("RGBA", image.size, TRANSPARENT)
    out.alpha_composite(crop, (x, y))
    return out


def repair_horizontal_identity(image: Image.Image) -> Image.Image:
    repaired = image.copy()
    d = ImageDraw.Draw(repaired)
    # The head leads at the right edge in the pooled leap/lunge concept.
    d.rectangle((22, 5, 27, 7), fill=OUTLINE)
    d.rectangle((23, 4, 26, 6), fill=WHITE)
    d.polygon([(18, 12), (21, 13), (23, 12)], fill=WHITE)
    d.polygon([(20, 13), (23, 19), (21, 22), (18, 15)], fill=TIE)
    return repaired


def route_a_frames() -> list[Image.Image]:
    sheet = Image.open(CONCEPT).convert("RGBA")
    base = [outline_and_repair(fit_pose(crop_pose(sheet, i)), death=(i == 9),
                               repair_identity=(i <= 6 or i == 8))
            for i in range(10)]
    base[7] = repair_horizontal_identity(base[7])
    # Concept indices: idle, high idle, contact, pass, toast, windup, impact, leap, hurt, death.
    frames = [
        base[0], translate(base[1], 0, -1), base[0],
        base[2], translate(base[3], 0, -1), translate(base[2], 1, 0), base[3],
        base[5], stretch(base[5], 29, 29, 1, 2), base[6],
        base[0], translate(base[4], 0, -1), base[4],
        base[5], stretch(base[6], 30, 27, 1, 4), base[6],
        translate(base[7], -1, 2), translate(base[7], 0, -2), translate(base[7], 1, 1),
        base[8],
        base[8], stretch(base[8], 30, 24, 1, 8), stretch(base[9], 30, 20, 1, 12),
        translate(base[9], 0, 2), stretch(base[9], 31, 13, 0, 19),
    ]
    return [outline_and_repair(quantize_rgba(frame), death=i >= 20, repair_identity=False)
            for i, frame in enumerate(frames)]


def outlined_ellipse(d: ImageDraw.ImageDraw, box, fill, outline=OUTLINE):
    d.ellipse(box, fill=outline)
    x0, y0, x1, y1 = box
    if x1 - x0 > 2 and y1 - y0 > 2:
        d.ellipse((x0 + 1, y0 + 1, x1 - 1, y1 - 1), fill=fill)


def outlined_polygon(d: ImageDraw.ImageDraw, points, fill, outline=OUTLINE):
    d.polygon(points, fill=fill)
    # PIL's one-pixel line keeps diagonals crisp and deliberate.
    d.line(points + [points[0]], fill=outline, width=1)


def draw_wings(d: ImageDraw.ImageDraw, y: int, compact: bool = False):
    if compact:
        upper = [(4, y + 6), (11, y + 4), (13, y + 7), (6, y + 10)]
        lower = [(3, y + 11), (11, y + 9), (13, y + 12), (5, y + 15)]
    else:
        upper = [(1, y + 5), (11, y + 3), (14, y + 7), (4, y + 10)]
        lower = [(1, y + 12), (11, y + 9), (13, y + 13), (3, y + 17)]
    for points in (upper, lower):
        outlined_polygon(d, points, WING)
        d.line((points[0][0] + 2, points[0][1] + 1, points[2][0] - 2, points[2][1] - 1), fill=WING_LIGHT)


def draw_identity(d: ImageDraw.ImageDraw, y: int, x_shift: int = 0, hat: bool = True):
    # Right-facing upper face, long ears, collar and a deliberately oversized tie.
    outlined_ellipse(d, (15 + x_shift, 4 + y, 28 + x_shift, 14 + y), GREEN)
    d.rectangle((24 + x_shift, 8 + y, 29 + x_shift, 11 + y), fill=OUTLINE)
    d.rectangle((25 + x_shift, 8 + y, 28 + x_shift, 9 + y), fill=LIGHT)
    d.point((25 + x_shift, 7 + y), fill=TIE)
    outlined_polygon(d, [(17 + x_shift, 7 + y), (21 + x_shift, 8 + y),
                         (18 + x_shift, 19 + y), (15 + x_shift, 18 + y)], MID)
    d.polygon([(18 + x_shift, 12 + y), (21 + x_shift, 13 + y), (24 + x_shift, 12 + y)], fill=WHITE)
    d.polygon([(21 + x_shift, 13 + y), (24 + x_shift, 21 + y),
               (21 + x_shift, 23 + y), (18 + x_shift, 14 + y)], fill=TIE)
    if hat:
        d.rectangle((20 + x_shift, 2 + y, 26 + x_shift, 4 + y), fill=OUTLINE)
        d.rectangle((21 + x_shift, 1 + y, 24 + x_shift, 3 + y), fill=WHITE)


def draw_belly_features(d: ImageDraw.ImageDraw, y: int, x_shift: int = 0, open_mouth: bool = True):
    if open_mouth:
        d.polygon([(13 + x_shift, 19 + y), (28 + x_shift, 18 + y),
                   (30 + x_shift, 22 + y), (14 + x_shift, 24 + y)], fill=OUTLINE)
        for x in range(16 + x_shift, 28 + x_shift, 3):
            d.rectangle((x, 19 + y, x + 1, 20 + y), fill=WHITE)
        outlined_ellipse(d, (20 + x_shift, 22 + y, 27 + x_shift, 29 + y), TONGUE)
        d.line((22 + x_shift, 23 + y, 24 + x_shift, 27 + y), fill=LIGHT)


def draw_regular(kind: str, variant: int = 0) -> Image.Image:
    image = Image.new("RGBA", (FRAME, FRAME), TRANSPARENT)
    d = ImageDraw.Draw(image)
    bob = -1 if kind == "idle" and variant == 1 else 0
    if kind == "walk":
        bob = -1 if variant % 2 else 0
    draw_wings(d, bob, compact=True)
    outlined_ellipse(d, (7, 7 + bob, 28, 29 + bob), MID)
    d.ellipse((9, 8 + bob, 25, 23 + bob), fill=GREEN)
    d.polygon([(9, 19 + bob), (7, 28 + bob), (12, 26 + bob)], fill=SHADOW)
    d.polygon([(8, 24 + bob), (10, 31), (12, 25 + bob)], fill=MID)
    d.polygon([(17, 25 + bob), (18, 30), (20, 25 + bob)], fill=GREEN)
    # Large hanging arm is the main side-view silhouette anchor.
    arm_y = 12 + bob + (1 if kind == "walk" and variant % 2 else 0)
    outlined_ellipse(d, (23, arm_y, 31, 26 + bob), MID)
    d.ellipse((25, arm_y + 1, 29, 21 + bob), fill=GREEN)
    draw_identity(d, bob)
    draw_belly_features(d, bob)
    # Highlights follow the same top-left light source in every frame.
    d.rectangle((10, 10 + bob, 12, 12 + bob), fill=LIGHT)
    d.rectangle((12, 9 + bob, 13, 10 + bob), fill=SPEC)
    if kind == "walk":
        if variant % 2 == 0:
            d.rectangle((11, 28 + bob, 15, 31), fill=OUTLINE)
            d.rectangle((21, 27 + bob, 24, 30), fill=OUTLINE)
        else:
            d.rectangle((12, 27 + bob, 15, 30), fill=OUTLINE)
            d.rectangle((20, 28 + bob, 25, 31), fill=OUTLINE)
    else:
        d.rectangle((12, 27 + bob, 15, 31), fill=OUTLINE)
        d.rectangle((21, 28 + bob, 24, 31), fill=OUTLINE)
    return image


def draw_attack(stage: int) -> Image.Image:
    image = Image.new("RGBA", (FRAME, FRAME), TRANSPARENT)
    d = ImageDraw.Draw(image)
    draw_wings(d, 0, compact=True)
    if stage == 0:
        outlined_ellipse(d, (8, 7, 28, 29), MID)
        outlined_ellipse(d, (3, 11, 15, 22), GREEN)
        d.rectangle((3, 14, 8, 19), fill=LIGHT)
        draw_identity(d, 0)
        draw_belly_features(d, 0)
    elif stage == 1:
        outlined_ellipse(d, (6, 8, 27, 28), MID)
        outlined_polygon(d, [(20, 13), (31, 12), (31, 20), (20, 23)], GREEN)
        d.rectangle((26, 13, 30, 16), fill=LIGHT)
        draw_identity(d, 1, -1)
        draw_belly_features(d, 1, -1)
    else:
        outlined_ellipse(d, (3, 9, 26, 28), MID)
        outlined_polygon(d, [(18, 13), (31, 10), (31, 21), (18, 24)], GREEN)
        d.rectangle((27, 11, 30, 15), fill=LIGHT)
        draw_identity(d, 2, -3)
        draw_belly_features(d, 2, -3)
    d.rectangle((11, 27, 15, 31), fill=OUTLINE)
    d.rectangle((20, 28, 24, 31), fill=OUTLINE)
    return image


def draw_cast(stage: int) -> Image.Image:
    image = draw_regular("idle", min(stage, 1))
    d = ImageDraw.Draw(image)
    # Raised arm and one-pixel-stem goblet; staged height improves action readability.
    top = 9 - stage * 2
    outlined_polygon(d, [(23, 15), (26, top + 4), (30, top + 7), (27, 19)], GREEN)
    d.rectangle((28, top, 31, top + 3), fill=OUTLINE)
    d.rectangle((29, top, 30, top + 2), fill=WING_LIGHT)
    d.point((29, top + 1), fill=GREEN)
    return image


def draw_horizontal(kind: str, stage: int) -> Image.Image:
    image = Image.new("RGBA", (FRAME, FRAME), TRANSPARENT)
    d = ImageDraw.Draw(image)
    if kind == "leap":
        y = [11, 7, 10][stage]
    else:
        y = [10, 11, 12][stage]
    outlined_polygon(d, [(1, y + 4), (7, y), (24, y), (31, y + 6),
                         (28, y + 14), (8, y + 15), (1, y + 11)], MID)
    d.polygon([(5, y + 3), (23, y + 2), (28, y + 6), (10, y + 8)], fill=GREEN)
    # Wings trail left; the head and fist lead right.
    outlined_polygon(d, [(1, y + 2), (10, y - 2), (13, y + 2), (5, y + 6)], WING)
    outlined_polygon(d, [(2, y + 11), (11, y + 8), (14, y + 11), (5, y + 14)], WING)
    outlined_ellipse(d, (20, y, 30, y + 10), GREEN)
    d.rectangle((27, y + 4, 31, y + 7), fill=OUTLINE)
    d.point((26, y + 3), fill=TIE)
    outlined_polygon(d, [(19, y + 5), (22, y + 6), (19, y + 13), (17, y + 10)], MID)
    d.polygon([(18, y + 6), (22, y + 8), (22, y + 14), (19, y + 13)], fill=TIE)
    d.rectangle((22, y - 2, 26, y), fill=OUTLINE)
    d.rectangle((23, y - 3, 25, y - 1), fill=WHITE)
    if kind == "dash":
        outlined_polygon(d, [(23, y + 9), (31, y + 8), (31, y + 14), (22, y + 14)], GREEN)
    else:
        d.polygon([(9, y + 10), (19, y + 9), (21, y + 13), (10, y + 14)], fill=OUTLINE)
        d.rectangle((13, y + 10, 14, y + 11), fill=WHITE)
    return image


def draw_hurt() -> Image.Image:
    image = Image.new("RGBA", (FRAME, FRAME), TRANSPARENT)
    d = ImageDraw.Draw(image)
    draw_wings(d, 4, compact=True)
    outlined_ellipse(d, (5, 10, 29, 30), SHADOW)
    d.ellipse((8, 11, 25, 25), fill=MID)
    draw_identity(d, 5, -1)
    draw_belly_features(d, 5, -1)
    d.line((28, 7, 31, 4), fill=WING_LIGHT)
    d.line((29, 8, 31, 8), fill=WING_LIGHT)
    return image


def draw_death(stage: int) -> Image.Image:
    if stage == 0:
        return draw_hurt()
    image = Image.new("RGBA", (FRAME, FRAME), TRANSPARENT)
    d = ImageDraw.Draw(image)
    top = [0, 12, 16, 19, 22][stage]
    bottom = 31
    width = [0, 26, 29, 31, 32][stage]
    left = (32 - width) // 2
    outlined_ellipse(d, (left, top, left + width - 1, bottom), SHADOW if stage < 3 else MID)
    d.ellipse((left + 2, top + 1, left + width - 4, bottom - 2), fill=MID)
    if stage <= 2:
        d.rectangle((17, top + 1, 22, top + 3), fill=OUTLINE)
        d.rectangle((18, top, 21, top + 1), fill=WHITE)
        d.polygon([(15, top + 4), (18, top + 5), (18, min(bottom, top + 11)), (16, top + 8)], fill=TIE)
        d.polygon([(9, bottom - 7), (25, bottom - 8), (26, bottom - 4), (10, bottom - 3)], fill=OUTLINE)
        d.rectangle((14, bottom - 7, 15, bottom - 6), fill=WHITE)
    if stage >= 3:
        d.rectangle((3, 29, 28, 31), fill=MID)
        d.point((1, 31), fill=GREEN)
        d.point((30, 30), fill=GREEN)
    return image


def route_b_frames() -> list[Image.Image]:
    frames = [
        draw_regular("idle", 0), draw_regular("idle", 1), draw_regular("idle", 0),
        draw_regular("walk", 0), draw_regular("walk", 1), draw_regular("walk", 2), draw_regular("walk", 3),
        draw_attack(0), draw_attack(1), draw_attack(2),
        draw_cast(0), draw_cast(1), draw_cast(2),
        draw_horizontal("dash", 0), draw_horizontal("dash", 1), draw_horizontal("dash", 2),
        draw_horizontal("leap", 0), draw_horizontal("leap", 1), draw_horizontal("leap", 2),
        draw_hurt(),
        draw_death(0), draw_death(1), draw_death(2), draw_death(3), draw_death(4),
    ]
    return [outline_and_repair(quantize_rgba(frame), death=i >= 20, repair_identity=False)
            for i, frame in enumerate(frames)]


def save_sheet(frames: list[Image.Image], path: Path) -> Image.Image:
    sheet = Image.new("RGBA", (FRAME * len(frames), FRAME), TRANSPARENT)
    for index, frame in enumerate(frames):
        sheet.alpha_composite(frame, (index * FRAME, 0))
    sheet.save(path, optimize=True)
    return sheet


def checkerboard(size: tuple[int, int], cell: int = 8) -> Image.Image:
    image = Image.new("RGB", size, (31, 35, 39))
    d = ImageDraw.Draw(image)
    for y in range(0, size[1], cell):
        for x in range(0, size[0], cell):
            if (x // cell + y // cell) % 2:
                d.rectangle((x, y, x + cell - 1, y + cell - 1), fill=(44, 50, 55))
    return image


def composite_on_checker(sheet: Image.Image, scale: int = 5) -> Image.Image:
    scaled = sheet.resize((sheet.width * scale, sheet.height * scale), Image.Resampling.NEAREST)
    bg = checkerboard(scaled.size, 16)
    bg.paste(scaled, (0, 0), scaled)
    return bg


def save_gif(frames: list[Image.Image], path: Path):
    preview = []
    for frame in frames:
        scaled = frame.resize((256, 256), Image.Resampling.NEAREST)
        bg = checkerboard(scaled.size, 32)
        bg.paste(scaled, (0, 0), scaled)
        preview.append(bg)
    durations = [260] * 3 + [120] * 4 + [140] * 3 + [160] * 3 + [110] * 3 + [110] * 3 + [180] + [170] * 5
    preview[0].save(path, save_all=True, append_images=preview[1:], duration=durations, loop=0, optimize=False)


def metrics(sheet: Image.Image) -> dict:
    arr = np.asarray(sheet.convert("RGBA"))
    alphas = arr[:, :, 3]
    colors = {tuple(pixel) for pixel in arr[:, :, :3][alphas == 255]}
    silhouettes = set()
    visible_by_frame = []
    tie_by_frame = []
    for index in range(FRAMES):
        frame = arr[:, index * FRAME:(index + 1) * FRAME]
        mask = frame[:, :, 3] == 255
        silhouettes.add(mask.tobytes())
        visible_by_frame.append(int(mask.sum()))
        tie_by_frame.append(int(np.all(frame[:, :, :3] == np.array(TIE[:3]), axis=2).sum()))
    return {
        "size": list(sheet.size),
        "unique_opaque_colors": len(colors),
        "partial_alpha_pixels": int(((alphas > 0) & (alphas < 255)).sum()),
        "distinct_silhouettes": len(silhouettes),
        "minimum_visible_pixels": min(visible_by_frame),
        "maximum_visible_pixels": max(visible_by_frame),
        "minimum_tie_pixels_frames_0_19": min(tie_by_frame[:20]),
    }


def validate_metrics(name: str, report: dict):
    assert report["size"] == [800, 32], f"{name}: incompatible sheet size"
    assert report["partial_alpha_pixels"] == 0, f"{name}: soft alpha is not pixel-safe"
    assert report["unique_opaque_colors"] <= 16, f"{name}: palette exceeds 16 colors"
    assert report["distinct_silhouettes"] >= 20, f"{name}: actions are not distinct enough"
    assert report["minimum_visible_pixels"] >= 20, f"{name}: contains an empty frame"
    assert report["minimum_tie_pixels_frames_0_19"] >= 17, f"{name}: tie loses readability"


def save_comparison(a: Image.Image, b: Image.Image, path: Path):
    scale = 4
    pa = composite_on_checker(a, scale)
    pb = composite_on_checker(b, scale)
    label_h = 24
    canvas = Image.new("RGB", (pa.width, label_h * 2 + pa.height + pb.height), (18, 20, 23))
    d = ImageDraw.Draw(canvas)
    font = ImageFont.load_default()
    d.text((8, 7), "A - concept pooling + pixel repair", fill=(232, 239, 240), font=font)
    canvas.paste(pa, (0, label_h))
    y = label_h + pa.height
    d.text((8, y + 7), "B - hand-drawn from concept silhouettes", fill=(232, 239, 240), font=font)
    canvas.paste(pb, (0, y + label_h))
    canvas.save(path, optimize=True)


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    frames_a = route_a_frames()
    frames_b = route_b_frames()
    sheet_a = save_sheet(frames_a, OUT_DIR / "gentleman_elf_route_a_pooled.png")
    sheet_b = save_sheet(frames_b, OUT_DIR / "gentleman_elf_route_b_handdrawn.png")
    composite_on_checker(sheet_a).save(OUT_DIR / "gentleman_elf_route_a_preview_5x.png", optimize=True)
    composite_on_checker(sheet_b).save(OUT_DIR / "gentleman_elf_route_b_preview_5x.png", optimize=True)
    save_gif(frames_a, OUT_DIR / "gentleman_elf_route_a_animation.gif")
    save_gif(frames_b, OUT_DIR / "gentleman_elf_route_b_animation.gif")
    save_comparison(sheet_a, sheet_b, OUT_DIR / "gentleman_elf_route_comparison.png")
    report = {"route_a": metrics(sheet_a), "route_b": metrics(sheet_b)}
    for name, values in report.items():
        validate_metrics(name, values)
    (OUT_DIR / "validation.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
