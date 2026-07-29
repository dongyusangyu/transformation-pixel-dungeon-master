"""Generate the hand-authored 16x16 Corrosive Swarm sprite sheet."""

from pathlib import Path

from PIL import Image, ImageDraw


FRAME_WIDTH = 16
FRAME_HEIGHT = 16
FRAME_COUNT = 17

PALETTE = {
    "outline": (20, 16, 27, 255),
    "shadow": (38, 25, 50, 255),
    "body": (67, 40, 82, 255),
    "body_light": (111, 68, 123, 255),
    "core": (211, 111, 221, 255),
    "wing_dark": (73, 80, 88, 255),
    "wing": (137, 147, 151, 255),
    "eye": (224, 91, 38, 255),
    "amber": (244, 155, 45, 255),
    "acid_dark": (82, 103, 35, 255),
    "acid": (158, 186, 57, 255),
}


def frame():
    return Image.new("RGBA", (FRAME_WIDTH, FRAME_HEIGHT))


def point(draw, color, x, y):
    if 0 <= x < FRAME_WIDTH and 0 <= y < FRAME_HEIGHT:
        draw.point((x, y), fill=PALETTE[color])


def line(draw, color, points, width=1):
    draw.line(points, fill=PALETTE[color], width=width)


def polygon(draw, color, points):
    draw.polygon(points, fill=PALETTE[color])


def draw_wings(draw, bob, phase, attack_shift=0):
    y = bob
    if phase % 2 == 0:
        wings = [
            [(5 + attack_shift, 7 + y), (2 + attack_shift, 3 + y),
             (1 + attack_shift, 4 + y), (3 + attack_shift, 7 + y)],
            [(10 + attack_shift, 7 + y), (13 + attack_shift, 3 + y),
             (14 + attack_shift, 4 + y), (12 + attack_shift, 7 + y)],
            [(5 + attack_shift, 9 + y), (2 + attack_shift, 12 + y),
             (3 + attack_shift, 13 + y), (7 + attack_shift, 10 + y)],
            [(10 + attack_shift, 9 + y), (13 + attack_shift, 12 + y),
             (12 + attack_shift, 13 + y), (8 + attack_shift, 10 + y)],
        ]
    else:
        wings = [
            [(5 + attack_shift, 7 + y), (2 + attack_shift, 5 + y),
             (1 + attack_shift, 6 + y), (4 + attack_shift, 8 + y)],
            [(10 + attack_shift, 7 + y), (13 + attack_shift, 5 + y),
             (14 + attack_shift, 6 + y), (11 + attack_shift, 8 + y)],
            [(5 + attack_shift, 9 + y), (2 + attack_shift, 10 + y),
             (3 + attack_shift, 12 + y), (7 + attack_shift, 10 + y)],
            [(10 + attack_shift, 9 + y), (13 + attack_shift, 10 + y),
             (12 + attack_shift, 12 + y), (8 + attack_shift, 10 + y)],
        ]
    for shape in wings:
        polygon(draw, "outline", shape)
        inner = shape[1:]
        polygon(draw, "wing_dark", inner)
        point(draw, "wing", inner[0][0], inner[0][1])


def draw_core(draw, bob=0, attack_shift=0, swollen=False):
    x = attack_shift
    if swollen:
        outer = [(5 + x, 4 + bob), (10 + x, 4 + bob), (12 + x, 7 + bob),
                 (11 + x, 11 + bob), (8 + x, 13 + bob), (4 + x, 11 + bob),
                 (3 + x, 7 + bob)]
        inner = [(6 + x, 5 + bob), (9 + x, 5 + bob), (11 + x, 7 + bob),
                 (10 + x, 10 + bob), (8 + x, 12 + bob), (5 + x, 10 + bob),
                 (4 + x, 7 + bob)]
    else:
        outer = [(5 + x, 5 + bob), (10 + x, 5 + bob), (12 + x, 7 + bob),
                 (11 + x, 10 + bob), (8 + x, 12 + bob), (4 + x, 10 + bob),
                 (3 + x, 7 + bob)]
        inner = [(6 + x, 6 + bob), (9 + x, 6 + bob), (11 + x, 7 + bob),
                 (10 + x, 9 + bob), (8 + x, 11 + bob), (5 + x, 9 + bob),
                 (4 + x, 7 + bob)]
    polygon(draw, "outline", outer)
    polygon(draw, "body", inner)
    polygon(draw, "shadow", [(5 + x, 8 + bob), (7 + x, 6 + bob),
                             (8 + x, 10 + bob), (6 + x, 10 + bob)])
    polygon(draw, "body_light", [(8 + x, 6 + bob), (10 + x, 7 + bob),
                                 (9 + x, 9 + bob), (8 + x, 8 + bob)])
    point(draw, "core", 7 + x, 7 + bob)
    point(draw, "eye", 10 + x, 7 + bob)
    point(draw, "amber", 11 + x, 8 + bob)


def draw_satellites(draw, bob, phase, spread=0):
    coords = [
        (2 - spread, 8 + bob, phase),
        (13 + spread, 8 + bob, phase + 1),
        (5 - spread, 3 + bob, phase + 1),
        (11 + spread, 12 + bob, phase),
    ]
    for x, y, wing_phase in coords:
        point(draw, "wing_dark", x - 1, y - (wing_phase % 2))
        point(draw, "wing", x + 1, y - ((wing_phase + 1) % 2))
        point(draw, "outline", x, y)
        point(draw, "eye", x, y + 1)


def draw_acid(draw, bob=0, phase=0, spread=0):
    drops = [
        (4 - spread, 11 + bob),
        (12 + spread, 10 + bob),
        (3 - spread, 6 + bob),
        (9 + spread, 13 + bob),
    ]
    for index, (x, y) in enumerate(drops):
        point(draw, "acid_dark", x, y)
        if (index + phase) % 2 == 0:
            point(draw, "acid", x, y - 1)


def hover_frame(index):
    image = frame()
    draw = ImageDraw.Draw(image)
    bob = 1 if index in (1, 2, 4) else 0
    draw_wings(draw, bob, index)
    draw_core(draw, bob)
    draw_satellites(draw, bob, index)
    draw_acid(draw, bob, index)
    return image


def attack_frame(stage):
    image = frame()
    draw = ImageDraw.Draw(image)
    if stage == 0:
        draw_wings(draw, 1, 1)
        draw_core(draw, 1, swollen=True)
        draw_satellites(draw, 1, 1)
        draw_acid(draw, 1, 1)
    elif stage == 1:
        polygon(draw, "outline", [(2, 6), (9, 5), (14, 7), (15, 9),
                                  (10, 10), (3, 10), (0, 8)])
        polygon(draw, "body", [(3, 7), (9, 6), (13, 7), (14, 8),
                               (9, 9), (3, 9), (1, 8)])
        line(draw, "core", [(7, 7), (11, 7)], 1)
        point(draw, "eye", 14, 7)
        for x, y in [(1, 5), (3, 4), (2, 11), (6, 12)]:
            point(draw, "wing", x, y)
        for x, y in [(12, 11), (14, 10), (15, 12)]:
            point(draw, "acid", x, y)
    elif stage == 2:
        polygon(draw, "outline", [(4, 5), (10, 5), (15, 7), (15, 9),
                                  (10, 11), (4, 10), (1, 8)])
        polygon(draw, "body", [(5, 6), (10, 6), (14, 7), (14, 8),
                               (9, 10), (4, 9), (2, 8)])
        point(draw, "core", 10, 7)
        point(draw, "eye", 14, 7)
        line(draw, "acid", [(12, 10), (15, 11)], 1)
        for x, y in [(3, 4), (6, 3), (3, 11), (7, 12)]:
            point(draw, "wing", x, y)
    else:
        draw_wings(draw, 0, 0)
        draw_core(draw, 0)
        draw_satellites(draw, 0, 0)
        draw_acid(draw, 0, 0)
    return image


def death_frame(stage):
    image = frame()
    draw = ImageDraw.Draw(image)
    if stage == 0:
        draw_wings(draw, 2, 1)
        draw_core(draw, 2)
        draw_satellites(draw, 2, 1)
        draw_acid(draw, 2, 1)
    elif stage == 1:
        polygon(draw, "outline", [(3, 8), (7, 6), (11, 7), (13, 10),
                                  (10, 13), (5, 13), (2, 11)])
        polygon(draw, "body", [(4, 8), (7, 7), (10, 8), (12, 10),
                               (9, 12), (5, 12), (3, 10)])
        point(draw, "core", 7, 9)
        for x, y in [(1, 7), (4, 5), (12, 6), (14, 9), (3, 14), (11, 14)]:
            point(draw, "wing_dark", x, y)
        for x, y in [(2, 10), (9, 13), (13, 12)]:
            point(draw, "acid", x, y)
    elif stage == 2:
        polygon(draw, "outline", [(2, 10), (5, 8), (10, 9), (13, 11),
                                  (11, 14), (4, 14), (1, 12)])
        polygon(draw, "shadow", [(3, 10), (6, 9), (10, 10), (12, 11),
                                 (10, 13), (4, 13), (2, 12)])
        for x, y, color in [(2, 8, "wing"), (5, 7, "body"), (12, 8, "wing_dark"),
                            (14, 10, "body"), (3, 14, "acid"), (10, 14, "acid")]:
            point(draw, color, x, y)
    elif stage == 3:
        line(draw, "outline", [(1, 13), (13, 13)], 2)
        line(draw, "shadow", [(3, 12), (10, 12)], 1)
        for x, y, color in [(1, 10, "wing_dark"), (4, 9, "body"),
                            (8, 10, "core"), (12, 9, "wing"), (14, 12, "acid")]:
            point(draw, color, x, y)
    else:
        for x, y, color in [(1, 13, "wing_dark"), (3, 12, "shadow"),
                            (5, 13, "body"), (8, 12, "core"),
                            (10, 13, "body"), (13, 12, "acid_dark"),
                            (14, 13, "acid")]:
            point(draw, color, x, y)
        line(draw, "outline", [(2, 14), (12, 14)], 1)
    return image


def burst_frame(stage):
    image = frame()
    draw = ImageDraw.Draw(image)
    if stage == 0:
        draw_wings(draw, 0, 0)
        draw_core(draw, 0, swollen=True)
        draw_satellites(draw, 0, 0, spread=1)
        for x, y in [(7, 3), (12, 5), (13, 10), (8, 14), (3, 11), (2, 5)]:
            point(draw, "acid", x, y)
        for x, y in [(7, 4), (11, 6), (11, 10), (7, 12), (4, 10), (4, 6)]:
            point(draw, "core", x, y)
    else:
        polygon(draw, "acid_dark", [(7, 1), (8, 1), (8, 3), (11, 3),
                                     (11, 4), (13, 4), (13, 6), (15, 6),
                                     (15, 9), (13, 9), (13, 11), (11, 11),
                                     (11, 13), (8, 13), (8, 15), (6, 15),
                                     (6, 13), (3, 13), (3, 11), (1, 11),
                                     (1, 9), (0, 9), (0, 6), (2, 6),
                                     (2, 4), (4, 4), (4, 2), (6, 2)])
        for x, y in [(7, 1), (12, 4), (15, 8), (11, 13),
                     (7, 15), (2, 11), (0, 7), (4, 3)]:
            point(draw, "acid", x, y)
        for x, y in [(5, 6), (10, 6), (11, 9), (8, 11), (4, 10), (3, 7)]:
            point(draw, "core", x, y)
        for x, y in [(2, 3), (13, 2), (14, 12), (1, 13)]:
            point(draw, "wing", x, y)
        for x, y in [(6, 7), (8, 6), (9, 9), (6, 10)]:
            point(draw, "outline", x, y)
    return image


def build_frames():
    return (
        [hover_frame(index) for index in range(6)]
        + [attack_frame(index) for index in range(4)]
        + [death_frame(index) for index in range(5)]
        + [burst_frame(index) for index in range(2)]
    )


def main():
    root = Path(__file__).resolve().parents[1]
    asset_path = root / "core/src/main/assets/sprites/corrosive_swarm.png"
    preview_path = root / "docs/pixel-art/corrosive-swarm/corrosive_swarm_preview.png"
    asset_path.parent.mkdir(parents=True, exist_ok=True)
    preview_path.parent.mkdir(parents=True, exist_ok=True)

    frames = build_frames()
    assert len(frames) == FRAME_COUNT
    sheet = Image.new("RGBA", (FRAME_WIDTH * FRAME_COUNT, FRAME_HEIGHT))
    for index, sprite_frame in enumerate(frames):
        assert sprite_frame.size == (FRAME_WIDTH, FRAME_HEIGHT)
        assert sprite_frame.getbbox() is not None
        sheet.alpha_composite(sprite_frame, (index * FRAME_WIDTH, 0))

    sheet.save(asset_path)
    sheet.resize(
        (sheet.width * 8, sheet.height * 8),
        Image.Resampling.NEAREST,
    ).save(preview_path)
    print(f"wrote {asset_path}")
    print(f"wrote {preview_path}")


if __name__ == "__main__":
    main()
