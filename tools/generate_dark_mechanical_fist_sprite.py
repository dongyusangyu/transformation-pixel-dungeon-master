from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "core/src/main/assets/sprites/mechanical_fist.png"
OUTPUT = ROOT / "core/src/main/assets/sprites/dark_mechanical_fist.png"
PREVIEW = ROOT / "docs/pixel-art/dark-mechanical-fist/dark_mechanical_fist_preview.png"


def recolor(source: Image.Image) -> Image.Image:
    source = source.convert("RGBA")
    palette = {
        (10, 13, 17, 255): (16, 10, 24, 255),
        (24, 30, 36, 255): (29, 18, 42, 255),
        (42, 50, 58, 255): (47, 28, 63, 255),
        (64, 75, 84, 255): (67, 42, 82, 255),
        (91, 63, 29, 255): (75, 29, 47, 255),
        (92, 104, 112, 255): (91, 55, 101, 255),
        (132, 145, 151, 255): (132, 80, 123, 255),
        (142, 102, 48, 255): (75, 29, 47, 255),
        (192, 151, 73, 255): (160, 64, 83, 255),
        (18, 88, 96, 255): (91, 24, 66, 255),
        (34, 163, 174, 255): (151, 43, 112, 255),
        (109, 224, 222, 255): (221, 112, 178, 255),
    }
    output = Image.new("RGBA", source.size, (0, 0, 0, 0))
    pixels = output.load()
    for y in range(source.height):
        for x in range(source.width):
            pixel = source.getpixel((x, y))
            if pixel[3] == 0:
                pixels[x, y] = (0, 0, 0, 0)
            else:
                pixels[x, y] = palette.get(pixel, (47, 28, 63, 255))

    eye_socket = (16, 10, 24, 255)
    eye_white = (221, 112, 178, 255)
    eye_pupil = (255, 220, 244, 255)
    for frame in range(7):
        x0 = frame * 24
        for dx, dy in ((0, 0), (1, 0), (2, 0), (0, 1), (2, 1), (0, 2), (1, 2), (2, 2)):
            pixels[x0 + 10 + dx, 5 + dy] = eye_socket
        pixels[x0 + 11, 6] = eye_white
        pixels[x0 + 11, 7 if frame in (5, 6) else 6] = eye_pupil

    return output


def main() -> None:
    output = recolor(Image.open(SOURCE))
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    output.save(OUTPUT)
    output.resize((output.width * 8, output.height * 8), Image.Resampling.NEAREST).save(PREVIEW)


if __name__ == "__main__":
    main()
