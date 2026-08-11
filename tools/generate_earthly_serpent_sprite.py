"""Generate the Earthly Serpent sprite from the original sewer snake sheet."""

from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "core/src/main/assets/sprites/snake.png"
OUTPUT = ROOT / "core/src/main/assets/sprites/earthly_serpent.png"
PREVIEW = ROOT / "docs/pixel-art/earthly-serpent/earthly_serpent_preview.png"

FRAME_WIDTH = 12
FRAME_HEIGHT = 11

TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (0, 0, 0, 255)
EARTH_SHADOW = (45, 34, 25, 255)
EARTH_DARK = (96, 70, 42, 255)
EARTH_MID = (145, 107, 58, 255)
BRASS = (184, 145, 70, 255)
SAND = (222, 190, 98, 255)
ACID = (166, 211, 54, 255)
ACID_BRIGHT = (221, 242, 106, 255)

PALETTE = {
    (0, 0, 0, 255): OUTLINE,
    (56, 104, 0, 255): EARTH_SHADOW,
    (103, 181, 0, 255): EARTH_DARK,
    (123, 217, 0, 255): EARTH_MID,
    (204, 183, 81, 255): BRASS,
    (255, 229, 101, 255): SAND,
    (255, 0, 0, 255): ACID,
}


def frame_box(index):
    left = index * FRAME_WIDTH
    return left, 0, left + FRAME_WIDTH, FRAME_HEIGHT


def paste_frame(sheet, source, source_index, target_index):
    sheet.paste(source.crop(frame_box(source_index)), (target_index * FRAME_WIDTH, 0))


def put(sheet, frame, x, y, color):
    sheet.putpixel((frame * FRAME_WIDTH + x, y), color)


def add_base_details(sheet):
    throat_pixels = {
        0: [(6, 4)],
        1: [(6, 3)],
        2: [(6, 3)],
        3: [(6, 3)],
        4: [(8, 5)],
        5: [(9, 5)],
        6: [(9, 4)],
        7: [(8, 5)],
        8: [(4, 3)],
        9: [(6, 3)],
        10: [(9, 4)],
        11: [(6, 4)],
    }
    for frame, pixels in throat_pixels.items():
        for x, y in pixels:
            put(sheet, frame, x, y, ACID_BRIGHT)

    # Sparse dorsal sacs keep the original silhouette while reading as acid-filled.
    for frame, x, y in [(0, 6, 7), (4, 7, 8), (7, 7, 8), (8, 5, 6), (11, 7, 7)]:
        put(sheet, frame, x, y, ACID)


def add_spit_frames(sheet, recolored_source):
    for source, target in [(8, 14), (9, 15), (10, 16)]:
        paste_frame(sheet, recolored_source, source, target)

    put(sheet, 14, 4, 3, ACID)
    put(sheet, 14, 4, 4, ACID_BRIGHT)

    put(sheet, 15, 6, 3, ACID_BRIGHT)
    put(sheet, 15, 6, 4, ACID)
    put(sheet, 15, 7, 4, ACID_BRIGHT)

    put(sheet, 16, 9, 4, ACID_BRIGHT)
    put(sheet, 16, 10, 4, ACID)
    put(sheet, 16, 11, 4, ACID_BRIGHT)


def add_pull_frames(sheet, recolored_source):
    for source, target in [(0, 17), (8, 18), (10, 19), (1, 20)]:
        paste_frame(sheet, recolored_source, source, target)

    put(sheet, 17, 6, 4, ACID)
    put(sheet, 17, 6, 7, ACID_BRIGHT)

    put(sheet, 18, 4, 3, ACID_BRIGHT)
    put(sheet, 18, 5, 6, ACID)

    put(sheet, 19, 9, 4, ACID_BRIGHT)
    put(sheet, 19, 7, 7, ACID)

    put(sheet, 20, 6, 3, ACID_BRIGHT)
    put(sheet, 20, 6, 5, ACID)
    put(sheet, 20, 7, 7, ACID_BRIGHT)


def generate():
    source = Image.open(SOURCE).convert("RGBA")
    if source.size != (256, 16):
        raise ValueError(f"Unexpected snake sprite size: {source.size}")

    recolored = Image.new("RGBA", source.size, TRANSPARENT)
    for y in range(source.height):
        for x in range(source.width):
            pixel = source.getpixel((x, y))
            if pixel[3] == 0:
                recolored.putpixel((x, y), TRANSPARENT)
            else:
                recolored.putpixel((x, y), PALETTE[pixel])

    output = recolored.copy()
    add_base_details(output)
    add_spit_frames(output, recolored)
    add_pull_frames(output, recolored)

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    output.save(OUTPUT, optimize=False)

    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    output.resize(
        (output.width * 8, output.height * 8),
        resample=Image.Resampling.NEAREST,
    ).save(PREVIEW, optimize=False)


if __name__ == "__main__":
    generate()
