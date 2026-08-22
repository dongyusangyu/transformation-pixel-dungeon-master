from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "core/src/main/assets/sprites/gentleman_elf.png"
PREVIEW = ROOT / "docs/art/gentleman_elf/gentleman_elf_sprite_preview.png"

TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (10, 31, 31, 255)
DEEP = (14, 67, 54, 255)
SHADOW = (18, 105, 73, 255)
MID = (24, 151, 96, 255)
LIGHT = (48, 197, 126, 255)
HIGHLIGHT = (133, 241, 174, 255)
TIE = (7, 14, 20, 255)
COLLAR = (224, 245, 226, 255)
WING_DARK = (23, 113, 122, 255)
WING = (67, 205, 194, 255)
GLASS = (191, 244, 230, 255)
WINE = (153, 222, 83, 255)
HURT = (230, 79, 90, 255)


def new_frame():
    image = Image.new("RGBA", (32, 32), TRANSPARENT)
    return image, ImageDraw.Draw(image)


def wing(draw, points):
    draw.polygon(points, fill=OUTLINE)
    inner = [(x + (1 if x < 10 else -1), y) for x, y in points]
    draw.polygon(inner, fill=WING_DARK)
    draw.line(points[0:2], fill=WING, width=1)


def leg(draw, x, y, forward=0, tucked=False):
    if tucked:
        draw.ellipse((x - 1, y, x + 5, y + 4), fill=OUTLINE)
        draw.rectangle((x, y, x + 4, y + 2), fill=SHADOW)
        return
    draw.polygon([(x, y - 6), (x + 5, y - 5), (x + 5 + forward, y),
                  (x + 1 + forward, y + 1), (x - 1, y - 1)], fill=OUTLINE)
    draw.polygon([(x + 1, y - 5), (x + 4, y - 4), (x + 4 + forward, y - 1),
                  (x + 1 + forward, y), (x, y - 1)], fill=MID)
    draw.line((x + 1 + forward, y, x + 5 + forward, y), fill=HIGHLIGHT, width=1)


def wine_glass(draw, x, y, tilted=False):
    if tilted:
        draw.polygon([(x, y), (x + 4, y + 1), (x + 3, y + 4), (x + 1, y + 3)], fill=GLASS)
        draw.line((x + 2, y + 3, x + 1, y + 6), fill=GLASS, width=1)
        draw.line((x, y + 6, x + 3, y + 6), fill=GLASS, width=1)
        draw.line((x + 1, y + 1, x + 3, y + 2), fill=WINE, width=1)
    else:
        draw.polygon([(x, y), (x + 4, y), (x + 3, y + 4), (x + 1, y + 4)], fill=GLASS)
        draw.rectangle((x + 1, y + 1, x + 3, y + 2), fill=WINE)
        draw.line((x + 2, y + 4, x + 2, y + 6), fill=GLASS, width=1)
        draw.line((x, y + 6, x + 4, y + 6), fill=GLASS, width=1)


def draw_wings(draw, dx, dy, compact=False):
    if compact:
        wing(draw, [(9 + dx, 11 + dy), (3 + dx, 12 + dy), (7 + dx, 16 + dy), (11 + dx, 15 + dy)])
        wing(draw, [(9 + dx, 15 + dy), (4 + dx, 18 + dy), (9 + dx, 20 + dy), (12 + dx, 17 + dy)])
    else:
        wing(draw, [(10 + dx, 10 + dy), (3 + dx, 9 + dy), (6 + dx, 14 + dy), (12 + dx, 14 + dy)])
        wing(draw, [(9 + dx, 14 + dy), (2 + dx, 17 + dy), (8 + dx, 20 + dy), (13 + dx, 17 + dy)])


def draw_body(draw, dx, dy, lean=0, squat=0):
    draw.ellipse((7 + dx, 8 + dy + squat, 25 + dx + lean, 27 + dy), fill=OUTLINE)
    draw.ellipse((8 + dx, 9 + dy + squat, 24 + dx + lean, 26 + dy), fill=MID)
    draw.polygon([(9 + dx, 18 + dy), (10 + dx, 26 + dy), (12 + dx, 29 + dy),
                  (14 + dx, 25 + dy), (17 + dx, 29 + dy), (19 + dx, 25 + dy),
                  (22 + dx, 28 + dy), (24 + dx, 22 + dy)], fill=SHADOW)
    draw.ellipse((10 + dx, 10 + dy + squat, 15 + dx, 15 + dy + squat), fill=LIGHT)
    draw.rectangle((9 + dx, 13 + dy + squat, 10 + dx, 18 + dy), fill=HIGHLIGHT)


def draw_head_and_tie(draw, dx, dy, lean=0, tie_swing=0):
    hx = dx + lean
    draw.polygon([(18 + hx, 5 + dy), (10 + hx, 4 + dy), (16 + hx, 10 + dy)], fill=OUTLINE)
    draw.polygon([(17 + hx, 6 + dy), (12 + hx, 5 + dy), (17 + hx, 9 + dy)], fill=LIGHT)
    draw.ellipse((15 + hx, 4 + dy, 27 + hx, 15 + dy), fill=OUTLINE)
    draw.ellipse((16 + hx, 5 + dy, 26 + hx, 14 + dy), fill=LIGHT)
    draw.polygon([(24 + hx, 8 + dy), (29 + hx, 10 + dy), (24 + hx, 12 + dy)], fill=OUTLINE)
    draw.polygon([(24 + hx, 9 + dy), (27 + hx, 10 + dy), (24 + hx, 11 + dy)], fill=MID)
    draw.polygon([(16 + hx, 8 + dy), (13 + hx, 10 + dy), (14 + hx, 20 + dy),
                  (17 + hx, 17 + dy)], fill=OUTLINE)
    draw.polygon([(16 + hx, 9 + dy), (14 + hx, 11 + dy), (15 + hx, 18 + dy),
                  (17 + hx, 16 + dy)], fill=SHADOW)
    draw.point((23 + hx, 8 + dy), fill=TIE)
    draw.line((23 + hx, 12 + dy, 27 + hx, 12 + dy), fill=TIE, width=1)
    draw.point((25 + hx, 11 + dy), fill=COLLAR)
    draw.polygon([(17 + dx, 13 + dy), (21 + dx, 15 + dy), (18 + dx, 18 + dy),
                  (15 + dx, 14 + dy)], fill=COLLAR)
    draw.polygon([(21 + dx, 15 + dy), (24 + dx, 13 + dy), (25 + dx, 17 + dy),
                  (23 + dx, 18 + dy)], fill=COLLAR)
    draw.polygon([(20 + dx, 15 + dy), (23 + dx, 16 + dy),
                  (24 + dx + tie_swing, 25 + dy), (20 + dx + tie_swing, 27 + dy),
                  (19 + dx, 18 + dy)], fill=OUTLINE)
    draw.polygon([(21 + dx, 16 + dy), (22 + dx, 17 + dy),
                  (22 + dx + tie_swing, 24 + dy), (21 + dx + tie_swing, 25 + dy)], fill=TIE)


def draw_back_arm(draw, dx, dy, lifted=False):
    if lifted:
        draw.polygon([(10 + dx, 12 + dy), (7 + dx, 8 + dy), (4 + dx, 10 + dy),
                      (7 + dx, 17 + dy), (12 + dx, 18 + dy)], fill=OUTLINE)
        draw.polygon([(10 + dx, 13 + dy), (7 + dx, 10 + dy), (6 + dx, 11 + dy),
                      (8 + dx, 16 + dy), (11 + dx, 17 + dy)], fill=SHADOW)
    else:
        draw.ellipse((6 + dx, 12 + dy, 12 + dx, 25 + dy), fill=OUTLINE)
        draw.ellipse((7 + dx, 13 + dy, 11 + dx, 23 + dy), fill=SHADOW)
        draw.line((8 + dx, 15 + dy, 8 + dx, 20 + dy), fill=HIGHLIGHT, width=1)


def draw_front_arm(draw, dx, dy, mode):
    if mode == "idle":
        draw.polygon([(22 + dx, 14 + dy), (27 + dx, 16 + dy), (29 + dx, 20 + dy),
                      (26 + dx, 22 + dy), (22 + dx, 19 + dy)], fill=OUTLINE)
        draw.polygon([(23 + dx, 15 + dy), (26 + dx, 17 + dy), (28 + dx, 19 + dy),
                      (26 + dx, 20 + dy), (23 + dx, 18 + dy)], fill=LIGHT)
        wine_glass(draw, 27 + dx, 12 + dy)
    elif mode == "low":
        draw.polygon([(22 + dx, 16 + dy), (28 + dx, 20 + dy), (27 + dx, 24 + dy),
                      (22 + dx, 21 + dy)], fill=OUTLINE)
        draw.polygon([(23 + dx, 17 + dy), (27 + dx, 20 + dy), (26 + dx, 22 + dy),
                      (23 + dx, 20 + dy)], fill=LIGHT)
        wine_glass(draw, 26 + dx, 18 + dy, tilted=True)
    elif mode == "windup":
        draw.polygon([(20 + dx, 14 + dy), (13 + dx, 12 + dy), (9 + dx, 15 + dy),
                      (13 + dx, 18 + dy), (22 + dx, 18 + dy)], fill=OUTLINE)
        draw.polygon([(20 + dx, 15 + dy), (14 + dx, 13 + dy), (11 + dx, 15 + dy),
                      (14 + dx, 17 + dy), (21 + dx, 17 + dy)], fill=LIGHT)
        wine_glass(draw, 7 + dx, 11 + dy, tilted=True)
    elif mode == "punch":
        draw.polygon([(21 + dx, 13 + dy), (30 + dx, 10 + dy), (32 + dx, 13 + dy),
                      (30 + dx, 16 + dy), (22 + dx, 18 + dy)], fill=OUTLINE)
        draw.polygon([(22 + dx, 14 + dy), (29 + dx, 11 + dy), (31 + dx, 13 + dy),
                      (29 + dx, 15 + dy), (23 + dx, 17 + dy)], fill=LIGHT)
        draw.rectangle((30 + dx, 11 + dy, 31 + dx, 14 + dy), fill=HIGHLIGHT)
    elif mode == "toast":
        draw.polygon([(21 + dx, 15 + dy), (23 + dx, 7 + dy), (26 + dx, 3 + dy),
                      (29 + dx, 5 + dy), (27 + dx, 10 + dy), (25 + dx, 18 + dy)], fill=OUTLINE)
        draw.polygon([(22 + dx, 15 + dy), (24 + dx, 8 + dy), (26 + dx, 5 + dy),
                      (28 + dx, 6 + dy), (26 + dx, 10 + dy), (24 + dx, 17 + dy)], fill=LIGHT)
        wine_glass(draw, 25 + dx, 0 + dy)
    elif mode == "cast":
        draw.polygon([(21 + dx, 14 + dy), (28 + dx, 9 + dy), (32 + dx, 10 + dy),
                      (31 + dx, 14 + dy), (23 + dx, 18 + dy)], fill=OUTLINE)
        draw.polygon([(22 + dx, 15 + dy), (28 + dx, 10 + dy), (31 + dx, 11 + dy),
                      (30 + dx, 13 + dy), (23 + dx, 17 + dy)], fill=LIGHT)
        wine_glass(draw, 27 + dx, 5 + dy, tilted=True)
        draw.point((31 + dx, 6 + dy), fill=WINE)
        draw.point((30 + dx, 4 + dy), fill=WINE)


def upright_frame(bob=0, step_left=0, step_right=0, arm="idle", lean=0,
                  tie_swing=0, compact_wings=False, tucked=False):
    image, draw = new_frame()
    dx = -1 if lean < 0 else 0
    dy = bob
    draw_wings(draw, dx, dy, compact=compact_wings)
    leg(draw, 10 + dx, 29 + dy, step_left, tucked=tucked)
    leg(draw, 18 + dx, 29 + dy, step_right, tucked=tucked)
    draw_back_arm(draw, dx, dy, lifted=arm in ("windup", "punch"))
    draw_body(draw, dx, dy, lean=max(0, lean), squat=1 if arm == "punch" else 0)
    draw_head_and_tie(draw, dx, dy, lean=max(0, lean), tie_swing=tie_swing)
    draw_front_arm(draw, dx, dy, arm)
    return image


def dash_frame(stage):
    image, draw = new_frame()
    y = (4, 3, 2)[stage]
    draw_wings(draw, 1, y, compact=True)
    draw.ellipse((5, 11 + y, 27, 26 + y), fill=OUTLINE)
    draw.ellipse((6, 12 + y, 26, 25 + y), fill=MID)
    draw.polygon([(18, 8 + y), (28, 10 + y), (31, 13 + y), (25, 16 + y),
                  (17, 14 + y)], fill=OUTLINE)
    draw.polygon([(19, 9 + y), (27, 11 + y), (29, 13 + y), (24, 15 + y),
                  (18, 13 + y)], fill=LIGHT)
    draw.point((27, 11 + y), fill=TIE)
    draw.line((27, 14 + y, 30, 14 + y), fill=TIE, width=1)
    draw.polygon([(18, 15 + y), (21, 16 + y), (27, 23 + y), (22, 24 + y)], fill=TIE)
    draw.polygon([(19, 14 + y), (22, 16 + y), (20, 18 + y), (17, 15 + y)], fill=COLLAR)
    draw.polygon([(23, 14 + y), (26, 13 + y), (27, 16 + y), (25, 17 + y)], fill=COLLAR)
    draw.polygon([(21, 16 + y), (32, 15 + y), (32, 19 + y), (22, 21 + y)], fill=OUTLINE)
    draw.polygon([(22, 17 + y), (31, 16 + y), (31, 18 + y), (23, 20 + y)], fill=HIGHLIGHT)
    draw.line((2, 23 + y, 8, 23 + y), fill=SHADOW, width=2)
    draw.point((0, 24 + y), fill=LIGHT)
    return image


def hurt_frame():
    image = upright_frame(bob=0, arm="low", lean=-1, tie_swing=-1, compact_wings=True)
    draw = ImageDraw.Draw(image)
    draw.line((27, 7, 30, 4), fill=HURT, width=1)
    draw.line((28, 5, 31, 7), fill=HURT, width=1)
    return image


def death_frame(stage):
    image, draw = new_frame()
    if stage == 0:
        return hurt_frame()
    if stage == 1:
        draw_wings(draw, -1, 5, compact=True)
        draw.ellipse((6, 12, 27, 29), fill=OUTLINE)
        draw.ellipse((7, 13, 26, 28), fill=MID)
        draw.ellipse((18, 9, 30, 19), fill=OUTLINE)
        draw.ellipse((19, 10, 29, 18), fill=LIGHT)
        draw.polygon([(19, 17), (23, 17), (27, 28), (21, 29)], fill=TIE)
        draw.polygon([(17, 16), (21, 18), (18, 20), (15, 17)], fill=COLLAR)
    elif stage == 2:
        draw.polygon([(3, 21), (8, 14), (21, 13), (30, 20), (28, 29),
                      (8, 30)], fill=OUTLINE)
        draw.polygon([(5, 21), (9, 15), (21, 15), (28, 20), (26, 28),
                      (8, 28)], fill=MID)
        draw.ellipse((20, 16, 31, 24), fill=LIGHT)
        draw.polygon([(17, 18), (21, 19), (27, 28), (20, 29)], fill=TIE)
        draw.polygon([(15, 17), (20, 19), (17, 21), (13, 18)], fill=COLLAR)
    elif stage == 3:
        draw.ellipse((2, 23, 30, 31), fill=OUTLINE)
        draw.ellipse((3, 24, 29, 30), fill=MID)
        draw.ellipse((18, 21, 31, 28), fill=LIGHT)
        draw.polygon([(14, 23), (19, 23), (26, 30), (18, 31)], fill=TIE)
        draw.polygon([(11, 22), (16, 23), (13, 25), (9, 23)], fill=COLLAR)
    else:
        draw.ellipse((1, 25, 29, 31), fill=OUTLINE)
        draw.ellipse((3, 26, 27, 30), fill=SHADOW)
        draw.polygon([(15, 25), (20, 25), (27, 30), (19, 31)], fill=TIE)
        wine_glass(draw, 26, 21, tilted=True)
        draw.point((5, 24), fill=LIGHT)
        draw.point((30, 29), fill=MID)
    return image


def build_frames():
    return [
        upright_frame(bob=0, arm="idle"),
        upright_frame(bob=-1, arm="idle"),
        upright_frame(bob=0, arm="low", tie_swing=1),
        upright_frame(bob=0, step_left=2, step_right=-1, arm="idle", tie_swing=1),
        upright_frame(bob=-1, step_left=0, step_right=0, arm="low"),
        upright_frame(bob=0, step_left=-1, step_right=2, arm="idle", tie_swing=-1),
        upright_frame(bob=-1, step_left=0, step_right=0, arm="low"),
        upright_frame(bob=0, arm="windup", lean=-1, tie_swing=-1),
        upright_frame(bob=0, arm="punch", lean=1, tie_swing=2, compact_wings=True),
        upright_frame(bob=0, arm="low", lean=1, tie_swing=1),
        upright_frame(bob=0, arm="idle"),
        upright_frame(bob=0, arm="toast", tie_swing=0),
        upright_frame(bob=0, arm="cast", lean=1, tie_swing=1),
        dash_frame(0), dash_frame(1), dash_frame(2),
        upright_frame(bob=1, arm="low", compact_wings=True),
        upright_frame(bob=-5, arm="idle", tie_swing=1, compact_wings=True, tucked=True),
        upright_frame(bob=1, arm="low", compact_wings=True),
        hurt_frame(),
        death_frame(0), death_frame(1), death_frame(2), death_frame(3), death_frame(4),
    ]


def validate(frames):
    assert len(frames) == 25
    for index, frame in enumerate(frames):
        assert frame.size == (32, 32)
        alpha = frame.getchannel("A")
        assert alpha.getbbox(), f"empty frame {index}"
        assert set(alpha.getdata()).issubset({0, 255}), f"soft alpha in frame {index}"
        colors = {pixel for pixel in frame.getdata() if pixel[3]}
        assert len(colors) <= 16, f"palette overflow in frame {index}: {len(colors)}"


def save_preview(frames):
    scale = 4
    gap = 4
    width = 5 * 32 * scale + 4 * gap
    height = 5 * 32 * scale + 4 * gap
    preview = Image.new("RGBA", (width, height), (35, 39, 43, 255))
    for index, frame in enumerate(frames):
        large = frame.resize((32 * scale, 32 * scale), Image.Resampling.NEAREST)
        x = (index % 5) * (32 * scale + gap)
        y = (index // 5) * (32 * scale + gap)
        preview.alpha_composite(large, (x, y))
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    preview.save(PREVIEW)


def main():
    frames = build_frames()
    validate(frames)
    sheet = Image.new("RGBA", (32 * len(frames), 32), TRANSPARENT)
    for index, frame in enumerate(frames):
        sheet.alpha_composite(frame, (index * 32, 0))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT)
    save_preview(frames)
    print(f"wrote {OUT} ({sheet.width}x{sheet.height}, {len(frames)} frames)")
    print(f"wrote {PREVIEW} (4x nearest-neighbor preview)")


if __name__ == "__main__":
    main()
