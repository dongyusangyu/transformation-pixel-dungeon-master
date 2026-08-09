"""Generate Roast Lamb Warlock art from the original dwarf warlock sheet."""

from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "core/src/main/assets/sprites/warlock.png"
OUTPUT = ROOT / "core/src/main/assets/sprites/roast_lamb_warlock.png"
PREVIEW = (
    ROOT
    / "docs/pixel-art/roast-lamb-warlock/roast_lamb_warlock_sprite_preview.png"
)

FRAME_WIDTH = 12
FRAME_HEIGHT = 15
GAMEPLAY_FRAMES = 11

TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (16, 12, 10, 255)
CHARCOAL = (34, 29, 25, 255)
ROBE_SHADOW = (58, 25, 20, 255)
ROBE_DARK = (91, 31, 26, 255)
ROBE_MID = (130, 43, 31, 255)
ROBE_HIGHLIGHT = (165, 59, 35, 255)
BROWN = (92, 58, 35, 255)
WOOL_SHADOW = (143, 127, 100, 255)
WOOL = (205, 197, 163, 255)
WOOL_HIGHLIGHT = (238, 226, 184, 255)
FIRE = (225, 71, 22, 255)
FIRE_BRIGHT = (255, 184, 46, 255)

PALETTE = {
    (0, 0, 0, 255): OUTLINE,
    (31, 13, 3, 255): CHARCOAL,
    (61, 22, 2, 255): BROWN,
    (125, 75, 25, 255): BROWN,
    (31, 6, 64, 255): ROBE_SHADOW,
    (41, 8, 82, 255): ROBE_SHADOW,
    (59, 17, 108, 255): ROBE_DARK,
    (89, 40, 128, 255): ROBE_MID,
    (102, 45, 145, 255): ROBE_HIGHLIGHT,
    (82, 119, 92, 255): CHARCOAL,
    (120, 152, 127, 255): WOOL_SHADOW,
    (145, 174, 153, 255): WOOL_SHADOW,
    (166, 192, 171, 255): WOOL,
    (175, 197, 180, 255): WOOL,
    (200, 213, 200, 255): WOOL,
    (206, 221, 209, 255): WOOL_HIGHLIGHT,
    (163, 163, 163, 255): WOOL_SHADOW,
    (206, 206, 206, 255): WOOL,
    (226, 226, 222, 255): WOOL_HIGHLIGHT,
    (229, 229, 229, 255): WOOL_HIGHLIGHT,
    (255, 255, 255, 255): WOOL_HIGHLIGHT,
}


def frame_origin(frame):
    return frame * FRAME_WIDTH


def opaque_positions(image, frame, y_min=0, y_max=FRAME_HEIGHT - 1):
    left = frame_origin(frame)
    return [
        (x, y)
        for y in range(y_min, y_max + 1)
        for x in range(FRAME_WIDTH)
        if image.getpixel((left + x, y))[3] == 255
    ]


def put(image, frame, x, y, color):
    absolute_x = frame_origin(frame) + x
    if image.getpixel((absolute_x, y))[3] == 255:
        image.putpixel((absolute_x, y), color)


def add_ram_and_wool_details(image):
    for frame in range(GAMEPLAY_FRAMES):
        head = [
            point
            for point in opaque_positions(image, frame, 1, 5)
            if 2 <= point[0] <= 9
        ]
        if head:
            left_horn = min(head, key=lambda point: (point[0], point[1]))
            right_horn = max(head, key=lambda point: (point[0], -point[1]))
            put(image, frame, *left_horn, BROWN)
            put(image, frame, *right_horn, BROWN)

        collar = [
            point
            for point in opaque_positions(image, frame, 5, 8)
            if 2 <= point[0] <= 9
        ]
        if collar:
            put(image, frame, *min(collar, key=lambda point: point[0]), WOOL_HIGHLIGHT)
            put(image, frame, *max(collar, key=lambda point: point[0]), WOOL_HIGHLIGHT)


def add_roasting_flame_details(image):
    for frame in (5, 6):
        candidates = opaque_positions(image, frame, 2, 10)
        if not candidates:
            continue
        tip = max(candidates, key=lambda point: (point[0], -abs(point[1] - 5)))
        put(image, frame, *tip, FIRE)

        nearby = [
            point
            for point in candidates
            if abs(point[0] - tip[0]) <= 1 and abs(point[1] - tip[1]) <= 2
            and point != tip
        ]
        if nearby:
            spark = min(
                nearby,
                key=lambda point: (
                    abs(point[0] - tip[0]) + abs(point[1] - tip[1]),
                    point[1],
                ),
            )
            put(image, frame, *spark, FIRE_BRIGHT)


def recolor(source):
    output = Image.new("RGBA", source.size, TRANSPARENT)
    for y in range(source.height):
        for x in range(source.width):
            pixel = source.getpixel((x, y))
            if pixel[3] == 0:
                output.putpixel((x, y), TRANSPARENT)
            else:
                output.putpixel((x, y), PALETTE[pixel])
    return output


def generate():
    source = Image.open(SOURCE).convert("RGBA")
    if source.size != (256, 16):
        raise ValueError(f"Unexpected warlock sprite size: {source.size}")

    output = recolor(source)
    add_ram_and_wool_details(output)
    add_roasting_flame_details(output)

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    output.save(OUTPUT, optimize=False)

    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    output.resize(
        (output.width * 8, output.height * 8),
        resample=Image.Resampling.NEAREST,
    ).save(PREVIEW, optimize=False)


if __name__ == "__main__":
    generate()
