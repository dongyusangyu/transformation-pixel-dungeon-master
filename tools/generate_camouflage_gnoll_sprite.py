"""Generate the hand-authored 12x16 Camouflage Gnoll sprite sheet."""

from pathlib import Path

from PIL import Image, ImageDraw


FRAME_WIDTH = 12
FRAME_HEIGHT = 16
FRAME_COUNT = 13

PALETTE = {
    "outline": (24, 25, 20, 255),
    "deep": (43, 40, 28, 255),
    "brown": (84, 68, 45, 255),
    "tan": (157, 133, 88, 255),
    "light": (204, 184, 126, 255),
    "moss_dark": (50, 62, 31, 255),
    "moss": (83, 101, 53, 255),
    "moss_light": (121, 136, 72, 255),
    "poison": (139, 70, 164, 255),
    "steel": (194, 194, 159, 255),
    "eye": (229, 177, 38, 255),
}


def polygon(draw, color, points):
    draw.polygon(points, fill=PALETTE[color])


def line(draw, color, points, width=1):
    draw.line(points, fill=PALETTE[color], width=width)


def point(draw, color, x, y):
    draw.point((x, y), fill=PALETTE[color])


def draw_legs(draw, gait):
    poses = {
        "idle": (
            [(4, 9), (6, 9), (6, 12), (5, 14), (3, 14), (4, 11)],
            [(6, 9), (8, 9), (9, 13), (10, 14), (7, 14), (6, 11)],
        ),
        "run_a": (
            [(4, 9), (6, 9), (5, 11), (2, 13), (1, 13), (4, 10)],
            [(6, 9), (8, 9), (9, 12), (11, 13), (9, 14), (7, 11)],
        ),
        "run_b": (
            [(4, 9), (6, 9), (5, 12), (4, 14), (2, 14), (4, 11)],
            [(6, 9), (8, 9), (8, 12), (7, 14), (5, 14), (6, 11)],
        ),
        "run_c": (
            [(4, 9), (6, 9), (3, 12), (1, 14), (3, 14), (5, 11)],
            [(6, 9), (8, 9), (9, 11), (11, 12), (10, 13), (7, 11)],
        ),
        "run_d": (
            [(4, 9), (6, 9), (6, 12), (7, 14), (5, 14), (5, 11)],
            [(6, 9), (8, 9), (7, 12), (5, 14), (3, 14), (6, 11)],
        ),
    }
    for shape in poses[gait]:
        polygon(draw, "outline", shape)
        inner = [(x, max(0, y - 1)) for x, y in shape[1:-1]]
        if len(inner) >= 3:
            polygon(draw, "brown", inner)
    point(draw, "tan", 5, 11)
    point(draw, "tan", 7, 11)


def draw_tail(draw, tail):
    paths = {
        "low": [(4, 8), (3, 9), (1, 10), (0, 9)],
        "up": [(4, 8), (2, 7), (1, 8), (0, 7)],
        "back": [(4, 8), (2, 9), (0, 8)],
        "down": [(4, 8), (3, 10), (2, 12), (1, 12)],
    }
    line(draw, "outline", paths[tail], 3)
    line(draw, "brown", paths[tail], 1)


def draw_body(draw, bob=0):
    polygon(draw, "outline", [(3, 5 + bob), (7, 4 + bob), (9, 7 + bob),
                              (8, 10 + bob), (5, 11 + bob), (2, 8 + bob)])
    polygon(draw, "brown", [(4, 6 + bob), (7, 5 + bob), (8, 7 + bob),
                            (7, 9 + bob), (5, 10 + bob), (3, 8 + bob)])
    point(draw, "tan", 7, 7 + bob)
    point(draw, "deep", 5, 9 + bob)


def draw_cloak(draw, bob=0, bright=False):
    polygon(draw, "outline", [(2, 4 + bob), (5, 3 + bob), (8, 5 + bob),
                              (6, 8 + bob), (3, 9 + bob), (1, 7 + bob)])
    polygon(draw, "moss_dark", [(3, 4 + bob), (5, 4 + bob), (7, 5 + bob),
                                (5, 7 + bob), (3, 8 + bob), (2, 6 + bob)])
    for x, y in [(2, 5), (4, 4), (6, 5), (3, 7), (5, 7), (2, 8)]:
        point(draw, "moss", x, y + bob)
    for x, y in [(3, 5), (5, 4), (4, 7)]:
        point(draw, "moss_light" if bright else "moss", x, y + bob)


def draw_head(draw, bob=0, lowered=False):
    y = bob + (2 if lowered else 0)
    polygon(draw, "outline", [(5, 2 + y), (6, 0 + y), (7, 2 + y),
                              (8, 1 + y), (9, 2 + y), (10, 3 + y),
                              (11, 4 + y), (11, 6 + y), (9, 6 + y),
                              (8, 7 + y), (6, 6 + y), (5, 4 + y)])
    polygon(draw, "tan", [(6, 2 + y), (7, 2 + y), (8, 2 + y),
                          (9, 3 + y), (10, 4 + y), (10, 5 + y),
                          (8, 6 + y), (6, 5 + y)])
    point(draw, "light", 7, 3 + y)
    point(draw, "light", 9, 4 + y)
    point(draw, "eye", 9, 3 + y)
    point(draw, "deep", 11, 5 + y)


def draw_arm_and_dagger(draw, pose, bob=0):
    if pose == "down":
        line(draw, "outline", [(7, 7 + bob), (9, 9 + bob)], 3)
        line(draw, "tan", [(7, 7 + bob), (9, 9 + bob)], 1)
        line(draw, "outline", [(9, 9 + bob), (10, 11 + bob)], 3)
        line(draw, "steel", [(9, 9 + bob), (10, 11 + bob)], 1)
        point(draw, "poison", 10, 11 + bob)
    elif pose == "windup":
        line(draw, "outline", [(7, 7 + bob), (8, 5 + bob)], 3)
        line(draw, "tan", [(7, 7 + bob), (8, 5 + bob)], 1)
        line(draw, "outline", [(8, 5 + bob), (9, 2 + bob)], 3)
        line(draw, "steel", [(8, 5 + bob), (9, 2 + bob)], 1)
        point(draw, "poison", 9, 2 + bob)
    elif pose == "strike":
        line(draw, "outline", [(7, 7 + bob), (9, 7 + bob)], 3)
        line(draw, "tan", [(7, 7 + bob), (9, 7 + bob)], 1)
        line(draw, "outline", [(9, 7 + bob), (11, 7 + bob)], 3)
        line(draw, "steel", [(9, 7 + bob), (11, 7 + bob)], 1)
        point(draw, "poison", 11, 7 + bob)


def standing_frame(gait="idle", tail="low", arm="down", bob=0, bright=False):
    image = Image.new("RGBA", (FRAME_WIDTH, FRAME_HEIGHT))
    draw = ImageDraw.Draw(image)
    draw_tail(draw, tail)
    draw_legs(draw, gait)
    draw_body(draw, bob)
    draw_cloak(draw, bob, bright)
    draw_head(draw, bob)
    draw_arm_and_dagger(draw, arm, bob)
    return image


def death_frame(stage):
    image = Image.new("RGBA", (FRAME_WIDTH, FRAME_HEIGHT))
    draw = ImageDraw.Draw(image)
    if stage == 0:
        draw_tail(draw, "down")
        draw_legs(draw, "idle")
        draw_body(draw, 1)
        draw_cloak(draw, 1)
        draw_head(draw, 1, lowered=True)
        line(draw, "outline", [(7, 9), (9, 11)], 3)
        point(draw, "poison", 10, 13)
    elif stage == 1:
        polygon(draw, "outline", [(2, 10), (4, 6), (8, 7), (10, 10),
                                  (9, 13), (3, 14), (1, 12)])
        polygon(draw, "brown", [(3, 10), (5, 7), (7, 8), (9, 10),
                                (8, 12), (4, 13), (2, 12)])
        polygon(draw, "moss_dark", [(2, 9), (4, 6), (7, 8), (5, 11)])
        point(draw, "moss", 4, 8)
        polygon(draw, "outline", [(7, 8), (9, 8), (11, 10), (10, 12), (7, 11)])
        polygon(draw, "tan", [(8, 9), (9, 9), (10, 10), (9, 11), (8, 10)])
        point(draw, "deep", 10, 10)
        line(draw, "outline", [(7, 13), (10, 14)], 2)
        point(draw, "poison", 10, 14)
    else:
        polygon(draw, "outline", [(0, 12), (2, 10), (6, 10), (8, 11),
                                  (11, 12), (11, 14), (8, 15), (2, 15)])
        polygon(draw, "brown", [(1, 12), (3, 11), (6, 11), (8, 12),
                                (10, 12), (10, 13), (8, 14), (2, 14)])
        polygon(draw, "moss_dark", [(2, 10), (6, 10), (8, 12), (5, 13), (1, 12)])
        for x, y in [(2, 11), (4, 10), (6, 11), (4, 12)]:
            point(draw, "moss", x, y)
        polygon(draw, "outline", [(8, 11), (10, 11), (11, 12), (10, 14), (8, 13)])
        polygon(draw, "tan", [(9, 12), (10, 12), (10, 13), (9, 13)])
        point(draw, "deep", 11, 13)
    return image


def camouflage_frame(alternate=False):
    image = Image.new("RGBA", (FRAME_WIDTH, FRAME_HEIGHT))
    draw = ImageDraw.Draw(image)
    grass_y = 13 if alternate else 14
    for x in range(12):
        if x % 2 == 0:
            line(draw, "moss_dark", [(x, 15), (x, grass_y)], 1)
        if x % 3 == 1:
            point(draw, "moss_light", x, 14)
    polygon(draw, "outline", [(1, 11), (3, 7), (7, 7), (10, 10),
                              (10, 13), (7, 14), (2, 14), (0, 13)])
    polygon(draw, "moss_dark", [(2, 11), (4, 8), (7, 8), (9, 10),
                                (9, 12), (6, 13), (2, 13), (1, 12)])
    for x, y in [(2, 10), (3, 8), (5, 9), (7, 8), (8, 10), (5, 12), (2, 12)]:
        point(draw, "moss", x, y)
    for x, y in [(3, 10), (6, 8), (7, 11)]:
        point(draw, "moss_light", x, y + (1 if alternate and x == 3 else 0))
    polygon(draw, "outline", [(7, 9), (9, 8), (11, 9), (11, 11), (9, 12), (7, 11)])
    polygon(draw, "tan", [(8, 9), (9, 9), (10, 9), (10, 10), (9, 11), (8, 10)])
    point(draw, "eye", 10, 9)
    point(draw, "deep", 11, 10)
    point(draw, "poison", 7, 13)
    return image


def build_frames():
    return [
        standing_frame("idle", "low", "down"),
        standing_frame("idle", "up", "down", bob=1, bright=True),
        standing_frame("idle", "back", "windup"),
        standing_frame("idle", "back", "strike", bob=1),
        standing_frame("run_a", "up", "down"),
        standing_frame("run_b", "back", "down", bob=1),
        standing_frame("run_c", "low", "down"),
        standing_frame("run_d", "back", "down", bob=1),
        death_frame(0),
        death_frame(1),
        death_frame(2),
        camouflage_frame(False),
        camouflage_frame(True),
    ]


def main():
    root = Path(__file__).resolve().parents[1]
    asset_path = root / "core/src/main/assets/sprites/camouflage_gnoll.png"
    preview_path = root / "docs/pixel-art/camouflage-gnoll/camouflage_gnoll_preview.png"
    asset_path.parent.mkdir(parents=True, exist_ok=True)
    preview_path.parent.mkdir(parents=True, exist_ok=True)

    frames = build_frames()
    assert len(frames) == FRAME_COUNT
    sheet = Image.new("RGBA", (FRAME_WIDTH * FRAME_COUNT, FRAME_HEIGHT))
    for index, frame in enumerate(frames):
        assert frame.size == (FRAME_WIDTH, FRAME_HEIGHT)
        assert frame.getbbox() is not None
        sheet.alpha_composite(frame, (index * FRAME_WIDTH, 0))

    sheet.save(asset_path)
    sheet.resize((sheet.width * 8, sheet.height * 8), Image.Resampling.NEAREST).save(preview_path)
    print(f"wrote {asset_path}")
    print(f"wrote {preview_path}")


if __name__ == "__main__":
    main()
