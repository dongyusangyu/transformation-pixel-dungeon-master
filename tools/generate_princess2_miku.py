"""Generate a Miku-inspired princess skin from scratch.

The game uses 12x15 HeroSprite frames. This script authors every active
character frame with native-size pixel primitives and only borrows the shared
death animation from a canonical hero sheet. It never reads princess2.png.
"""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw


FRAME_WIDTH = 12
FRAME_HEIGHT = 15
SHEET_WIDTH = 256
SHEET_HEIGHT = 128
ACTIVE_ROWS = range(7)
DEATH_FRAMES = range(8, 13)

OUTLINE = (8, 17, 28, 255)
HAIR_DARK = (0, 67, 83, 255)
HAIR = (0, 151, 167, 255)
HAIR_LIGHT = (55, 222, 214, 255)
SKIN_SHADOW = (210, 132, 111, 255)
SKIN = (255, 218, 184, 255)
SKIN_LIGHT = (255, 236, 211, 255)
EYE = (20, 29, 42, 255)
GRAPHITE = (27, 34, 45, 255)
CLOTH = (59, 68, 78, 255)
CLOTH_LIGHT = (151, 165, 173, 255)
TEAL_TRIM = (19, 191, 185, 255)
PINK_TIE = (240, 48, 104, 255)
PINK_LIGHT = (255, 112, 156, 255)
WHITE = (226, 239, 239, 255)


def put(draw: ImageDraw.ImageDraw, ox: int, oy: int, x: int, y: int, color) -> None:
    if 0 <= x < FRAME_WIDTH and 0 <= y < FRAME_HEIGHT:
        draw.point((ox + x, oy + y), fill=color)


def rect(draw: ImageDraw.ImageDraw, ox: int, oy: int, left: int, top: int, right: int, bottom: int, color) -> None:
    draw.rectangle((ox + left, oy + top, ox + right, oy + bottom), fill=color)


def paint_twin_tails(draw: ImageDraw.ImageDraw, ox: int, oy: int, shift: int = 0) -> None:
    """Paint long stepped twin-tails without soft scaling or interpolation."""
    rect(draw, ox, oy, 0, 2 + shift, 2, 10 + shift, HAIR_DARK)
    rect(draw, ox, oy, 0, 4 + shift, 1, 10 + shift, HAIR)
    put(draw, ox, oy, 1, 2 + shift, HAIR)
    put(draw, ox, oy, 0, 3 + shift, HAIR_LIGHT)
    put(draw, ox, oy, 0, 9 + shift, HAIR_LIGHT)
    put(draw, ox, oy, 1, 10 + shift, HAIR_DARK)
    put(draw, ox, oy, 0, 11 + shift, HAIR_DARK)

    rect(draw, ox, oy, 9, 2 - shift, 11, 10 - shift, HAIR_DARK)
    rect(draw, ox, oy, 10, 3 - shift, 11, 9 - shift, HAIR)
    put(draw, ox, oy, 10, 2 - shift, HAIR_LIGHT)
    put(draw, ox, oy, 11, 4 - shift, HAIR_LIGHT)
    put(draw, ox, oy, 11, 9 - shift, HAIR_LIGHT)
    put(draw, ox, oy, 10, 10 - shift, HAIR_DARK)
    put(draw, ox, oy, 11, 11 - shift, HAIR_DARK)


def paint_side_head(draw: ImageDraw.ImageDraw, ox: int, oy: int, phase: int) -> None:
    paint_twin_tails(draw, ox, oy, shift=phase & 1)
    # Profile faces right: the nose and single visible eye are pushed one pixel right.
    rect(draw, ox, oy, 3, 0, 7, 1, OUTLINE)
    rect(draw, ox, oy, 2, 1, 8, 3, HAIR_DARK)
    rect(draw, ox, oy, 3, 1, 7, 2, HAIR)
    put(draw, ox, oy, 4, 1, HAIR_LIGHT)
    put(draw, ox, oy, 3, 2, HAIR_LIGHT)
    rect(draw, ox, oy, 4, 3, 8, 7, OUTLINE)
    rect(draw, ox, oy, 5, 3, 7, 6, SKIN)
    put(draw, ox, oy, 8, 5, SKIN_LIGHT)
    put(draw, ox, oy, 6, 5, EYE)
    put(draw, ox, oy, 6, 6, SKIN_LIGHT)
    put(draw, ox, oy, 7, 3, HAIR)
    put(draw, ox, oy, 8, 4, HAIR_DARK)


def paint_front_head(draw: ImageDraw.ImageDraw, ox: int, oy: int, phase: int) -> None:
    paint_twin_tails(draw, ox, oy, shift=phase & 1)
    rect(draw, ox, oy, 2, 0, 9, 1, OUTLINE)
    rect(draw, ox, oy, 2, 1, 9, 3, HAIR_DARK)
    rect(draw, ox, oy, 3, 1, 8, 2, HAIR)
    put(draw, ox, oy, 4, 1, HAIR_LIGHT)
    put(draw, ox, oy, 7, 1, HAIR_LIGHT)
    rect(draw, ox, oy, 3, 3, 8, 7, OUTLINE)
    rect(draw, ox, oy, 4, 3, 7, 6, SKIN)
    rect(draw, ox, oy, 4, 6, 7, 7, SKIN_SHADOW)
    put(draw, ox, oy, 4, 3, HAIR_LIGHT)
    put(draw, ox, oy, 5, 3, HAIR_LIGHT)
    put(draw, ox, oy, 6, 3, HAIR)
    put(draw, ox, oy, 7, 3, HAIR_DARK)
    put(draw, ox, oy, 4, 5, EYE)
    put(draw, ox, oy, 7, 5, EYE)
    put(draw, ox, oy, 5, 6, SKIN_LIGHT)
    put(draw, ox, oy, 6, 6, SKIN_LIGHT)


def paint_side_body(draw: ImageDraw.ImageDraw, ox: int, oy: int, pose: str, phase: int, trim) -> None:
    paint_side_head(draw, ox, oy, phase)
    rect(draw, ox, oy, 5, 7, 7, 8, OUTLINE)
    put(draw, ox, oy, 6, 7, SKIN_LIGHT)
    put(draw, ox, oy, 6, 8, PINK_TIE)
    rect(draw, ox, oy, 4, 8, 8, 10, GRAPHITE)
    rect(draw, ox, oy, 5, 8, 7, 9, CLOTH)
    put(draw, ox, oy, 5, 9, trim)
    put(draw, ox, oy, 7, 9, CLOTH_LIGHT)

    if pose == "attack":
        rect(draw, ox, oy, 7, 8, 10, 9, OUTLINE)
        put(draw, ox, oy, 9, 8, SKIN_LIGHT)
        put(draw, ox, oy, 10, 8, PINK_LIGHT)
    else:
        rect(draw, ox, oy, 3, 9, 5, 11, OUTLINE)
        put(draw, ox, oy, 4, 9, trim)
        rect(draw, ox, oy, 8, 9, 9, 11, OUTLINE)
        put(draw, ox, oy, 8, 9, trim)

    rect(draw, ox, oy, 3, 10, 8, 11, OUTLINE)
    rect(draw, ox, oy, 4, 10, 7, 11, CLOTH)
    put(draw, ox, oy, 5, 10, trim)
    put(draw, ox, oy, 7, 10, trim)
    if pose == "run":
        left_leg = 3 if phase % 2 == 0 else 4
        right_leg = 7 if phase % 2 == 0 else 8
        rect(draw, ox, oy, left_leg, 12, left_leg + 1, 13, OUTLINE)
        rect(draw, ox, oy, right_leg, 12, right_leg + 1, 13, OUTLINE)
    elif pose == "fly":
        put(draw, ox, oy, 3, 13, HAIR_LIGHT)
        put(draw, ox, oy, 8, 13, HAIR_LIGHT)
    else:
        rect(draw, ox, oy, 4, 12, 5, 13, OUTLINE)
        rect(draw, ox, oy, 7, 12, 8, 13, OUTLINE)
    rect(draw, ox, oy, 3, 14, 5, 14, OUTLINE)
    rect(draw, ox, oy, 7, 14, 9, 14, OUTLINE)
    put(draw, ox, oy, 4, 14, trim)
    put(draw, ox, oy, 8, 14, trim)


def extract_scroll_palette(reference: Image.Image) -> tuple:
    """Extract the canonical parchment tones from HeroSprite's open-scroll frame."""
    frame_left = 20 * FRAME_WIDTH
    return (
        reference.getpixel((frame_left + 5, 11)),  # rolled edge / outline
        reference.getpixel((frame_left + 5, 6)),   # parchment shadow
        reference.getpixel((frame_left + 6, 7)),   # parchment body
    )


def paint_front_body(draw: ImageDraw.ImageDraw, ox: int, oy: int, action: str, phase: int, trim, scroll_palette: tuple) -> None:
    paint_front_head(draw, ox, oy, phase)
    edge, paper_shadow, paper = scroll_palette
    rect(draw, ox, oy, 4, 7, 7, 8, OUTLINE)
    rect(draw, ox, oy, 5, 7, 6, 8, SKIN_LIGHT)
    rect(draw, ox, oy, 3, 8, 8, 10, GRAPHITE)
    rect(draw, ox, oy, 4, 8, 7, 9, CLOTH)
    put(draw, ox, oy, 5, 8, PINK_TIE)
    put(draw, ox, oy, 6, 8, PINK_LIGHT)
    put(draw, ox, oy, 3, 9, trim)
    put(draw, ox, oy, 8, 9, trim)

    if action == "operate":
        rect(draw, ox, oy, 1, 8, 3, 10, OUTLINE)
        put(draw, ox, oy, 2, 8, trim)
        rect(draw, ox, oy, 8, 8, 10, 10, OUTLINE)
        put(draw, ox, oy, 9, 8, trim)
    elif action == "read_closed":
        rect(draw, ox, oy, 2, 9, 4, 10, OUTLINE)
        rect(draw, ox, oy, 7, 9, 9, 10, OUTLINE)
        rect(draw, ox, oy, 5, 8, 6, 11, edge)
        put(draw, ox, oy, 5, 9, paper_shadow)
        put(draw, ox, oy, 6, 10, paper)
    else:
        rect(draw, ox, oy, 2, 9, 4, 10, OUTLINE)
        rect(draw, ox, oy, 7, 9, 9, 10, OUTLINE)
        rect(draw, ox, oy, 5, 6, 7, 11, edge)
        rect(draw, ox, oy, 6, 6, 7, 10, paper_shadow)
        put(draw, ox, oy, 6, 7, paper)
        put(draw, ox, oy, 7, 8, paper)

    rect(draw, ox, oy, 3, 10, 8, 11, OUTLINE)
    rect(draw, ox, oy, 4, 10, 7, 11, CLOTH)
    put(draw, ox, oy, 5, 10, trim)
    put(draw, ox, oy, 6, 10, trim)
    rect(draw, ox, oy, 4, 12, 5, 13, OUTLINE)
    rect(draw, ox, oy, 6, 12, 7, 13, OUTLINE)
    rect(draw, ox, oy, 3, 14, 5, 14, OUTLINE)
    rect(draw, ox, oy, 6, 14, 8, 14, OUTLINE)
    put(draw, ox, oy, 4, 14, trim)
    put(draw, ox, oy, 7, 14, trim)


def make_sheet(death_source: Image.Image) -> Image.Image:
    sheet = Image.new("RGBA", (SHEET_WIDTH, SHEET_HEIGHT), (0, 0, 0, 0))
    draw = ImageDraw.Draw(sheet)
    death_source = death_source.convert("RGBA")
    if death_source.size != (SHEET_WIDTH, SHEET_HEIGHT):
        raise ValueError(f"unexpected death source size: {death_source.size}")
    scroll_palette = extract_scroll_palette(death_source)
    poses = {
        0: "idle", 1: "idle",
        2: "run", 3: "run", 4: "run", 5: "run", 6: "run", 7: "run",
        13: "attack", 14: "attack", 15: "attack",
        16: "operate", 17: "operate", 18: "fly", 19: "read", 20: "read",
    }
    for row in ACTIVE_ROWS:
        for frame, pose in poses.items():
            ox = frame * FRAME_WIDTH
            oy = row * FRAME_HEIGHT
            trim = TEAL_TRIM if row % 2 == 0 else HAIR_LIGHT
            if pose == "operate":
                paint_front_body(draw, ox, oy, "operate", frame + row, trim, scroll_palette)
            elif pose == "read":
                action = "read_closed" if frame == 19 else "read_open"
                paint_front_body(draw, ox, oy, action, frame + row, trim, scroll_palette)
            else:
                paint_side_body(draw, ox, oy, pose, frame + row, trim)

    for row in ACTIVE_ROWS:
        for frame in DEATH_FRAMES:
            box = (frame * FRAME_WIDTH, row * FRAME_HEIGHT,
                   frame * FRAME_WIDTH + FRAME_WIDTH, row * FRAME_HEIGHT + FRAME_HEIGHT)
            sheet.paste(death_source.crop(box), box)
    return sheet


def verify(sheet: Image.Image, death_source: Image.Image) -> None:
    if sheet.size != (SHEET_WIDTH, SHEET_HEIGHT):
        raise AssertionError(f"unexpected output size: {sheet.size}")
    if sheet.getchannel("A").getbbox() is None:
        raise AssertionError("generated sheet is fully transparent")
    for row in ACTIVE_ROWS:
        for frame in DEATH_FRAMES:
            box = (frame * FRAME_WIDTH, row * FRAME_HEIGHT,
                   frame * FRAME_WIDTH + FRAME_WIDTH, row * FRAME_HEIGHT + FRAME_HEIGHT)
            if sheet.crop(box).tobytes() != death_source.crop(box).convert("RGBA").tobytes():
                raise AssertionError(f"death frame mismatch at row={row}, frame={frame}")
    for row in ACTIVE_ROWS:
        for frame in (0, 1, 2, 7, 13, 16, 18, 19, 20):
            box = (frame * FRAME_WIDTH, row * FRAME_HEIGHT,
                   frame * FRAME_WIDTH + FRAME_WIDTH, row * FRAME_HEIGHT + FRAME_HEIGHT)
            if sheet.crop(box).getbbox() is None:
                raise AssertionError(f"empty active frame at row={row}, frame={frame}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("death_source", type=Path, help="canonical hero sheet, e.g. cleric.png")
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    death_source = Image.open(args.death_source).convert("RGBA")
    sheet = make_sheet(death_source)
    verify(sheet, death_source)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(args.output, format="PNG", optimize=True, compress_level=9)
    round_trip = Image.open(args.output).convert("RGBA")
    if round_trip.tobytes() != sheet.tobytes():
        raise AssertionError("PNG round-trip changed pixels")
    print(f"generated {args.output} ({sheet.width}x{sheet.height})")


if __name__ == "__main__":
    main()
