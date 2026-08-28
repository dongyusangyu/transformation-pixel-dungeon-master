"""Draw the original hard-edged 16x16 Death Butterfly animation sheet."""

from pathlib import Path

from PIL import Image, ImageDraw


FRAME_WIDTH = 16
FRAME_HEIGHT = 16
FRAME_COUNT = 15

PALETTE = {
    "outline": (15, 15, 29, 255),
    "deep": (31, 30, 58, 255),
    "wing_dark": (43, 55, 91, 255),
    "wing": (61, 92, 137, 255),
    "glow": (78, 169, 194, 255),
    "crystal": (145, 231, 225, 255),
    "bone_shadow": (139, 147, 158, 255),
    "bone": (220, 225, 216, 255),
    "violet": (132, 70, 164, 255),
    "curse": (232, 83, 153, 255),
}


def blank_frame():
    return Image.new("RGBA", (FRAME_WIDTH, FRAME_HEIGHT), (0, 0, 0, 0))


def point(draw, color, x, y):
    if 0 <= x < FRAME_WIDTH and 0 <= y < FRAME_HEIGHT:
        draw.point((x, y), fill=PALETTE[color])


def polygon(draw, color, points):
    clipped = [(x, y) for x, y in points if 0 <= x < FRAME_WIDTH and 0 <= y < FRAME_HEIGHT]
    if len(clipped) >= 3:
        draw.polygon(clipped, fill=PALETTE[color])


def draw_wing(draw, points, inner_points, highlight_points):
    polygon(draw, "outline", points)
    polygon(draw, "wing_dark", inner_points)
    for x, y in highlight_points:
        point(draw, "wing", x, y)


def draw_butterfly(draw, shift=0, bob=0, wing_phase=0, skull=True):
    cx = 7 + shift
    cy = 8 + bob

    if wing_phase == 0:
        left = [(cx - 1, cy), (cx - 5, cy - 5), (cx - 7, cy - 4),
                (cx - 6, cy - 1), (cx - 5, cy + 3), (cx - 2, cy + 2)]
        right = [(cx + 2, cy), (cx + 6, cy - 5), (cx + 7, cy - 4),
                 (cx + 7, cy - 1), (cx + 6, cy + 3), (cx + 3, cy + 2)]
        left_inner = [(cx - 2, cy), (cx - 5, cy - 4), (cx - 6, cy - 3),
                      (cx - 5, cy), (cx - 4, cy + 2)]
        right_inner = [(cx + 2, cy), (cx + 5, cy - 4), (cx + 6, cy - 3),
                       (cx + 6, cy), (cx + 5, cy + 2)]
        left_high = [(cx - 4, cy - 3), (cx - 5, cy - 2), (cx - 4, cy + 1)]
        right_high = [(cx + 5, cy - 3), (cx + 6, cy - 2), (cx + 5, cy + 1)]
    elif wing_phase == 1:
        left = [(cx - 1, cy), (cx - 5, cy - 3), (cx - 6, cy - 1),
                (cx - 5, cy + 2), (cx - 2, cy + 2)]
        right = [(cx + 2, cy), (cx + 6, cy - 3), (cx + 7, cy - 1),
                 (cx + 6, cy + 2), (cx + 3, cy + 2)]
        left_inner = [(cx - 2, cy), (cx - 5, cy - 2), (cx - 5, cy),
                      (cx - 4, cy + 1)]
        right_inner = [(cx + 2, cy), (cx + 5, cy - 2), (cx + 6, cy),
                       (cx + 5, cy + 1)]
        left_high = [(cx - 4, cy - 1), (cx - 4, cy + 1)]
        right_high = [(cx + 5, cy - 1), (cx + 5, cy + 1)]
    else:
        left = [(cx - 1, cy), (cx - 3, cy - 4), (cx - 4, cy - 2),
                (cx - 3, cy + 3), (cx - 1, cy + 2)]
        right = [(cx + 2, cy), (cx + 4, cy - 4), (cx + 5, cy - 2),
                 (cx + 4, cy + 3), (cx + 2, cy + 2)]
        left_inner = [(cx - 2, cy), (cx - 3, cy - 3), (cx - 3, cy + 2)]
        right_inner = [(cx + 2, cy), (cx + 4, cy - 3), (cx + 4, cy + 2)]
        left_high = [(cx - 3, cy - 1)]
        right_high = [(cx + 4, cy - 1)]

    draw_wing(draw, left, left_inner, left_high)
    draw_wing(draw, right, right_inner, right_high)

    polygon(draw, "outline", [(cx, cy - 4), (cx + 2, cy - 3),
                              (cx + 2, cy + 3), (cx + 1, cy + 5),
                              (cx - 1, cy + 3), (cx - 1, cy - 3)])
    polygon(draw, "deep", [(cx, cy - 3), (cx + 1, cy - 2),
                           (cx + 1, cy + 3), (cx, cy + 4),
                           (cx, cy + 1)])
    point(draw, "violet", cx, cy + 1)
    point(draw, "glow", cx + 1, cy + 2)
    point(draw, "crystal", cx, cy - 3)

    if skull:
        point(draw, "bone_shadow", cx - 1, cy - 2)
        point(draw, "bone", cx, cy - 2)
        point(draw, "bone", cx + 1, cy - 2)
        point(draw, "bone", cx, cy - 1)
        point(draw, "outline", cx - 1, cy - 1)
        point(draw, "curse", cx + 1, cy - 1)


def draw_motes(draw, phase, shift=0, bob=0):
    motes = [
        (2 + shift, 3 + bob + phase % 2, "crystal"),
        (13 + shift, 12 + bob - phase % 2, "violet"),
        (3 + shift, 13 + bob - (phase + 1) % 2, "glow"),
    ]
    for index, (x, y, color) in enumerate(motes):
        point(draw, color, x, y)
        if (phase + index) % 2 == 0:
            point(draw, "wing_dark", x - 1, y)
            point(draw, "wing_dark", x + 1, y)


def hover_frame(index):
    image = blank_frame()
    draw = ImageDraw.Draw(image)
    phases = (0, 1, 2, 1, 0, 1)
    bobs = (0, -1, -1, 0, 1, 0)
    draw_butterfly(draw, bob=bobs[index], wing_phase=phases[index])
    draw_motes(draw, index, bob=bobs[index])
    return image


def attack_frame(stage):
    image = blank_frame()
    draw = ImageDraw.Draw(image)
    if stage == 0:
        draw_butterfly(draw, shift=-1, wing_phase=2)
        draw_motes(draw, stage, shift=-1)
    elif stage == 1:
        draw_butterfly(draw, shift=1, wing_phase=1)
        for x, y in [(1, 7), (3, 6), (4, 9)]:
            point(draw, "violet", x, y)
    elif stage == 2:
        draw_butterfly(draw, shift=3, wing_phase=2)
        for x, y in [(1, 8), (3, 7), (5, 9), (7, 8)]:
            point(draw, "glow", x, y)
    else:
        draw_butterfly(draw, wing_phase=1)
        draw_motes(draw, stage)
    return image


def death_frame(stage):
    image = blank_frame()
    draw = ImageDraw.Draw(image)
    if stage == 0:
        draw_butterfly(draw, bob=1, wing_phase=0)
        draw_motes(draw, stage, bob=1)
    elif stage == 1:
        draw_butterfly(draw, bob=2, wing_phase=2, skull=False)
        for x, y, color in [(2, 4, "bone"), (13, 5, "crystal"),
                            (1, 11, "wing"), (14, 12, "violet")]:
            point(draw, color, x, y)
    elif stage == 2:
        polygon(draw, "outline", [(4, 10), (7, 8), (11, 10), (10, 13), (5, 13)])
        polygon(draw, "deep", [(5, 10), (7, 9), (10, 10), (9, 12), (6, 12)])
        for x, y, color in [(2, 6, "bone"), (5, 5, "wing"), (11, 6, "crystal"),
                            (14, 8, "violet"), (3, 13, "glow"), (12, 14, "curse")]:
            point(draw, color, x, y)
    elif stage == 3:
        for x, y, color in [(1, 7, "wing_dark"), (4, 9, "bone_shadow"),
                            (7, 10, "deep"), (10, 9, "crystal"),
                            (13, 11, "violet"), (3, 14, "glow"),
                            (9, 14, "outline")]:
            point(draw, color, x, y)
    else:
        for x, y, color in [(2, 12, "wing_dark"), (5, 14, "bone_shadow"),
                            (8, 13, "deep"), (11, 14, "violet")]:
            point(draw, color, x, y)
    return image


def build_frames():
    return (
        [hover_frame(index) for index in range(6)]
        + [attack_frame(index) for index in range(4)]
        + [death_frame(index) for index in range(5)]
    )


def main():
    root = Path(__file__).resolve().parents[1]
    asset_path = root / "core/src/main/assets/sprites/death_butterfly.png"
    preview_path = root / "docs/pixel-art/death-butterfly/death_butterfly_preview.png"
    asset_path.parent.mkdir(parents=True, exist_ok=True)
    preview_path.parent.mkdir(parents=True, exist_ok=True)

    frames = build_frames()
    if len(frames) != FRAME_COUNT:
        raise ValueError(f"expected {FRAME_COUNT} frames, got {len(frames)}")
    sheet = Image.new(
        "RGBA", (FRAME_WIDTH * FRAME_COUNT, FRAME_HEIGHT), (0, 0, 0, 0)
    )
    for index, sprite_frame in enumerate(frames):
        sheet.alpha_composite(sprite_frame, (index * FRAME_WIDTH, 0))

    sheet.save(asset_path)
    sheet.resize(
        (sheet.width * 8, sheet.height * 8), Image.Resampling.NEAREST
    ).save(preview_path)
    print(f"wrote {asset_path}")
    print(f"wrote {preview_path}")


if __name__ == "__main__":
    main()
