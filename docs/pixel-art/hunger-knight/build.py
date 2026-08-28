#!/usr/bin/env python3
"""Deterministically builds the Hunger Knight's 30-frame 32px sprite sheet."""
from pathlib import Path
import json
import sys

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
STUDIO = Path(r"G:\CodexData\.codex\skills\pixel-art-studio\scripts")
sys.path.insert(0, str(STUDIO))
from pixelstudio import Sprite

OUT = Path(__file__).resolve().parent
REF = OUT / "reference"
RUNTIME = ROOT / "core" / "src" / "main" / "assets" / "sprites" / "hunger_knight.png"

P = {
    "ink": "#17151a", "ink_light": "#28252b",
    "iron_shadow": "#353238", "iron": "#4c4949", "iron_light": "#777068",
    "cloth_shadow": "#382920", "cloth": "#604536", "cloth_light": "#8a6749",
    "bone_shadow": "#9d947d", "bone": "#d7cbaa",
    "amber": "#d79a2d", "rust": "#8f402e"
}
PAL = list(P.values())


def pooled_reference():
    src = Image.open(REF / "hunger_knight_concept.png").convert("RGBA")
    corner = src.getpixel((0, 0))
    px = src.load()
    for y in range(src.height):
        for x in range(src.width):
            c = px[x, y]
            if sum(abs(c[i] - corner[i]) for i in range(3)) < 42:
                px[x, y] = (0, 0, 0, 0)
    box = src.getbbox()
    crop = src.crop(box) if box else src
    crop.thumbnail((30, 30), Image.Resampling.BOX)
    pooled = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    pooled.alpha_composite(crop, ((32 - crop.width) // 2, 31 - crop.height))
    pooled.save(REF / "hunger_knight_pool_32.png")


def polearm(s, kind, phase, bob):
    ink, iron, light, rust = P["ink"], P["iron"], P["iron_light"], P["rust"]
    if kind == "attack":
        lines = [((5, 24), (26, 19)), ((7, 26), (26, 11)),
                 ((8, 26), (24, 5)), ((7, 18), (27, 18)), ((6, 21), (27, 24))]
    elif kind == "charge":
        lines = [((6, 24), (25, 12)), ((7, 25), (24, 8)),
                 ((9, 26), (22, 6)), ((8, 25), (25, 11))]
    elif kind == "thrust":
        lines = [((5, 23), (26, 19)), ((7, 21), (27, 17)),
                 ((8, 18), (28, 16)), ((8, 17), (28, 16)), ((7, 20), (27, 19))]
    elif kind == "walk":
        lines = [((5, 21), (28, 25)), ((6, 20), (29, 24)),
                 ((6, 19), (29, 23)), ((5, 21), (28, 25)),
                 ((4, 22), (27, 26)), ((5, 21), (28, 25))]
    else:
        lines = [((5, 21), (28, 25)), ((5, 20), (28, 24)),
                 ((5, 21), (28, 25)), ((5, 20), (28, 24))]
    a, b = lines[phase]
    a = (a[0], a[1] + bob); b = (b[0], b[1] + bob)
    s.line(a[0], a[1], b[0], b[1], ink)
    s.line(a[0], a[1] - 1, b[0], b[1] - 1, iron)
    tx, ty = b
    if kind == "thrust" and phase >= 1:
        s.polygon([(tx - 3, ty - 3), (30, ty - 1), (tx - 1, ty + 3),
                   (tx - 6, ty + 1), (tx - 5, ty - 1)], ink)
        s.polygon([(tx - 2, ty - 2), (29, ty - 1), (tx - 1, ty + 1),
                   (tx - 5, ty)], iron)
        s.line(tx - 2, ty - 2, 29, ty - 1, light)
    else:
        direction = 1 if tx >= a[0] else -1
        s.polygon([(tx - 2, ty - 3), (min(30, tx + 3 * direction), ty - 2),
                   (min(30, tx + 2 * direction), ty + 3), (tx - 3, ty + 2)], ink)
        s.polygon([(tx - 1, ty - 2), (min(29, tx + 2 * direction), ty - 1),
                   (tx + direction, ty + 2), (tx - 2, ty + 1)], iron)
        s.px(tx, ty - 1, light)
    if phase % 2:
        s.px(max(0, tx - 2), min(31, ty + 2), rust)


def upright(s, kind="idle", phase=0):
    if kind == "idle":
        bob = [0, -1, 0, 0][phase]
        near_dx, far_dx = 0, 0
    elif kind == "walk":
        bob = [0, -1, -1, 0, -1, -1][phase]
        near_dx = [2, 1, 0, -2, -1, 0][phase]
        far_dx = [-2, -1, 0, 2, 1, 0][phase]
    elif kind == "charge":
        bob = [1, 2, 2, 1][phase]
        near_dx, far_dx = 1, -1
    elif kind == "thrust":
        bob = [0, 1, 1, 0, 0][phase]
        near_dx, far_dx = [0, 1, 2, 2, 1][phase], [-1, -1, -2, -2, -1][phase]
    else:
        bob = [0, 0, 1, 0, 0][phase]
        near_dx, far_dx = [0, -1, -1, 1, 0][phase], [0, 1, 1, -1, 0][phase]

    polearm(s, kind, phase, bob)

    # Torn grain-sack cape behind the body.
    s.polygon([(9, 9 + bob), (15, 7 + bob), (17, 20 + bob),
               (13, 27 + bob), (11, 24 + bob), (8, 28 + bob), (7, 17 + bob)], P["ink"])
    s.polygon([(10, 10 + bob), (14, 9 + bob), (15, 19 + bob),
               (12, 25 + bob), (10, 23 + bob), (9, 26 + bob), (9, 16 + bob)], P["cloth"])
    s.line(10, 11 + bob, 9, 20 + bob, P["cloth_light"])

    # Far and near legs are deliberately thick and separately readable.
    s.polygon([(11 + far_dx, 21 + bob), (15 + far_dx, 21 + bob),
               (15 + far_dx, 29), (9 + far_dx, 29), (10 + far_dx, 26 + bob)], P["ink"])
    s.rect(11 + far_dx, 22 + bob, 14 + far_dx, 27, P["iron_shadow"])
    s.rect(9 + far_dx, 28, 15 + far_dx, 29, P["iron"])
    s.polygon([(16 + near_dx, 20 + bob), (21 + near_dx, 21 + bob),
               (21 + near_dx, 29), (15 + near_dx, 29), (16 + near_dx, 25 + bob)], P["ink"])
    s.rect(17 + near_dx, 22 + bob, 20 + near_dx, 27, P["iron"])
    s.rect(15 + near_dx, 28, 22 + near_dx, 29, P["iron_light"])

    # Rib-cage armor and asymmetric shoulder mass.
    s.polygon([(10, 10 + bob), (14, 7 + bob), (20, 9 + bob), (23, 16 + bob),
               (20, 23 + bob), (12, 23 + bob), (9, 17 + bob)], P["ink"])
    s.polygon([(11, 11 + bob), (15, 9 + bob), (19, 10 + bob), (21, 16 + bob),
               (19, 21 + bob), (13, 21 + bob), (11, 17 + bob)], P["iron_shadow"])
    s.polygon([(9, 9 + bob), (14, 7 + bob), (16, 10 + bob), (13, 13 + bob),
               (8, 12 + bob)], P["iron"])
    s.line(10, 9 + bob, 14, 8 + bob, P["iron_light"])
    for y, width in ((12, 6), (15, 7), (18, 6)):
        s.line(13, y + bob, 13 + width, y + bob, P["bone_shadow"])
        s.px(13, y - 1 + bob, P["bone"])
    s.rect(12, 20 + bob, 20, 22 + bob, P["cloth_shadow"])
    s.px(19, 21 + bob, P["rust"])

    # Narrow right-facing pale mask and amber slit.
    s.polygon([(14, 3 + bob), (19, 4 + bob), (23, 7 + bob),
               (21, 13 + bob), (16, 12 + bob), (13, 7 + bob)], P["ink"])
    s.polygon([(16, 4 + bob), (19, 5 + bob), (22, 7 + bob),
               (20, 12 + bob), (17, 10 + bob), (15, 7 + bob)], P["bone_shadow"])
    s.polygon([(16, 4 + bob), (19, 5 + bob), (21, 7 + bob),
               (17, 7 + bob)], P["bone"])
    s.line(18, 8 + bob, 21, 8 + bob, P["amber"])

    # Arms grip the weapon; near hand shifts with action emphasis.
    hand_x = 20 + (2 if kind == "thrust" and phase >= 1 else 0)
    s.polygon([(19, 13 + bob), (23, 14 + bob), (hand_x + 2, 20 + bob),
               (hand_x, 22 + bob), (17, 18 + bob)], P["ink"])
    s.line(20, 14 + bob, hand_x + 1, 20 + bob, P["iron"])
    s.rect(hand_x, 19 + bob, hand_x + 2, 21 + bob, P["iron_light"])

    # Broken balance chain: sparse amber pixels remain legible at 1x.
    chain_x = 12 + (phase % 2 if kind == "idle" else 0)
    s.line(chain_x, 21 + bob, chain_x - 1, 25 + bob, P["amber"])
    s.line(chain_x - 3, 25 + bob, chain_x + 1, 25 + bob, P["amber"])
    s.px(chain_x + 2, 23 + bob, P["amber"])


def death_pose(s, phase):
    if phase == 0:
        upright(s, "idle", 0)
        s.px(23, 8, P["rust"])
        return
    if phase == 1:
        upright(s, "charge", 1)
        return
    if phase == 2:
        # Kneeling silhouette.
        s.polygon([(8, 14), (15, 8), (22, 12), (23, 22), (18, 27),
                   (10, 27), (7, 22)], P["ink"])
        s.polygon([(10, 14), (15, 10), (20, 13), (20, 21), (16, 25),
                   (11, 24), (9, 20)], P["iron_shadow"])
        s.polygon([(15, 7), (20, 9), (22, 13), (18, 15), (14, 12)], P["bone_shadow"])
        s.px(20, 11, P["amber"])
        s.rect(6, 27, 17, 29, P["iron"])
        s.line(5, 23, 29, 27, P["iron_light"])
        return
    # Explicitly redrawn collapse; no rotated or interpolated pixels.
    widths = [(7, 25), (5, 27), (4, 28)]
    left, right = widths[phase - 3]
    y = 24 + (phase - 3)
    s.polygon([(left, y - 5), (12, y - 9), (20, y - 7), (right, y - 3),
               (right - 2, 29), (left + 1, 29)], P["ink"])
    s.polygon([(left + 2, y - 4), (13, y - 7), (20, y - 5), (right - 2, y - 2),
               (right - 3, 28), (left + 2, 28)], P["cloth"])
    s.polygon([(17, y - 9), (22, y - 7), (24, y - 5), (20, y - 3), (16, y - 5)], P["bone_shadow"])
    if phase < 5: s.px(22, y - 6, P["amber"])
    s.line(3, 29, 29, 29, P["iron_shadow"])
    s.line(7, y - 2, 30, y - 1, P["iron_light"])


def build():
    s = Sprite(32, 32, palette=PAL)
    recipes = ([('idle', i) for i in range(4)]
               + [('walk', i) for i in range(6)]
               + [('attack', i) for i in range(5)]
               + [('charge', i) for i in range(4)]
               + [('thrust', i) for i in range(5)]
               + [('die', i) for i in range(6)])
    for frame, (kind, phase) in enumerate(recipes):
        if frame: s.add_frame(copy=False)
        if kind == 'die': death_pose(s, phase)
        else: upright(s, kind, phase)
        s.set_duration(120 if kind not in ('idle', 'die') else 180)
    s.tag("idle", 1, 4, "pingpong")
    s.tag("run", 5, 10, "forward")
    s.tag("attack", 11, 15, "forward")
    s.tag("charge", 16, 19, "pingpong")
    s.tag("thrust", 20, 24, "forward")
    s.tag("die", 25, 30, "forward")

    RUNTIME.parent.mkdir(parents=True, exist_ok=True)
    sheet = Image.new("RGBA", (32 * 30, 32), (0, 0, 0, 0))
    for i in range(1, 31): sheet.alpha_composite(s.composite(i), ((i - 1) * 32, 0))
    sheet.save(RUNTIME)
    s.preview(str(OUT / "hunger_knight_preview.png"), scale=6, cols=10, grid=True, labels=True)
    s.save_silhouette(str(OUT / "hunger_knight_silhouette.png"))
    s.save_gif(str(OUT / "hunger_knight_idle.gif"), scale=8, tag="idle", bg="#d6cfbd")
    s.save_gif(str(OUT / "hunger_knight_run.gif"), scale=8, tag="run", bg="#26242b")
    qa_backgrounds(s.composite(1))
    s.stats()
    return s, sheet


def qa_backgrounds(frame):
    backgrounds = [
        ("light stone", "#c8bd9f", "#8f806c"),
        ("dark stone", "#29272d", "#403b43"),
        ("grass", "#52623a", "#73804c"),
        ("water", "#31596a", "#4c7c83"),
        ("warning", "#8e342e", "#d06a3d")
    ]
    panel = Image.new("RGBA", (32 * len(backgrounds), 32), (0, 0, 0, 255))
    for i, (_, base, accent) in enumerate(backgrounds):
        tile = Image.new("RGBA", (32, 32), base)
        tp = tile.load()
        for y in range(0, 32, 4):
            for x in range((y // 4) % 2, 32, 4):
                tp[x, y] = tuple(bytes.fromhex(accent[1:])) + (255,)
        tile.alpha_composite(frame)
        panel.alpha_composite(tile, (i * 32, 0))
    panel.resize((panel.width * 6, panel.height * 6), Image.Resampling.NEAREST).save(
        OUT / "hunger_knight_background_qa.png")


def validate(s, sheet):
    colors = sorted({sheet.getpixel((x, y)) for y in range(32) for x in range(sheet.width)
                     if sheet.getpixel((x, y))[3]})
    boxes = [s.composite(i).getbbox() for i in range(1, 31)]
    report = {
        "canvas": [32, 32], "frames": 30, "sheet": [sheet.width, sheet.height],
        "opaque_colors": len(colors), "semi_alpha_pixels": sum(
            1 for p in sheet.getdata() if 0 < p[3] < 255),
        "empty_frames": [i for i, box in enumerate(boxes) if box is None],
        "edge_touches": [i for i, box in enumerate(boxes)
                         if box and (box[0] == 0 or box[1] == 0 or box[2] == 32 or box[3] == 32)]
    }
    (OUT / "hunger_knight_validation.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))
    if report["opaque_colors"] > 16 or report["semi_alpha_pixels"] or report["empty_frames"]:
        raise SystemExit("sprite validation failed")


if __name__ == "__main__":
    pooled_reference()
    sprite, runtime_sheet = build()
    validate(sprite, runtime_sheet)
