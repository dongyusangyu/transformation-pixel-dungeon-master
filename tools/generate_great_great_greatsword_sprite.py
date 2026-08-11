from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SHEET_PATH = ROOT / "core/src/main/assets/sprites/ex_items.png"
CELL_X = 0
CELL_Y = 144
CELL_SIZE = 16

OUTLINE = (24, 28, 43, 255)
BLADE_SHADOW = (78, 96, 116, 255)
BLADE_MID = (151, 173, 188, 255)
BLADE_LIGHT = (228, 242, 244, 255)
GOLD_SHADOW = (116, 70, 27, 255)
GOLD = (205, 139, 42, 255)
GOLD_LIGHT = (255, 220, 105, 255)
GRIP = (91, 45, 42, 255)


def draw_sprite(sheet: Image.Image) -> None:
    before = sheet.copy()
    draw = ImageDraw.Draw(sheet)
    draw.rectangle(
        (CELL_X, CELL_Y, CELL_X + CELL_SIZE - 1, CELL_Y + CELL_SIZE - 1),
        fill=(0, 0, 0, 0),
    )

    def points(coords):
        return [(CELL_X + x, CELL_Y + y) for x, y in coords]

    # Broad blade silhouette, angled like the existing weapon icons.
    draw.polygon(
        points([(15, 0), (15, 4), (10, 9), (10, 11), (8, 13),
                (5, 10), (7, 8), (12, 3), (12, 1)]),
        fill=OUTLINE,
    )
    draw.polygon(
        points([(14, 1), (14, 3), (9, 8), (9, 10), (8, 11),
                (7, 10), (8, 8), (13, 3), (13, 1)]),
        fill=BLADE_MID,
    )
    draw.polygon(
        points([(14, 3), (9, 8), (9, 10), (8, 11), (7, 10), (13, 4)]),
        fill=BLADE_SHADOW,
    )
    draw.line(points([(14, 1), (8, 7)]), fill=BLADE_LIGHT, width=1)
    draw.line(points([(13, 2), (8, 8)]), fill=BLADE_LIGHT, width=1)

    # Oversized crossguard, perpendicular to the blade.
    draw.polygon(points([(3, 7), (4, 6), (11, 12), (9, 14)]), fill=OUTLINE)
    draw.line(points([(4, 7), (10, 13)]), fill=GOLD_SHADOW, width=2)
    draw.line(points([(4, 7), (9, 12)]), fill=GOLD, width=1)
    draw.point((CELL_X + 4, CELL_Y + 7), fill=GOLD_LIGHT)
    draw.point((CELL_X + 10, CELL_Y + 12), fill=GOLD_LIGHT)

    # Long wrapped grip and heavy pommel fill the lower-left corner.
    draw.polygon(points([(5, 9), (8, 12), (3, 15), (0, 15), (0, 14)]), fill=OUTLINE)
    draw.line(points([(6, 11), (2, 15)]), fill=GRIP, width=2)
    draw.point((CELL_X + 4, CELL_Y + 13), fill=GOLD_SHADOW)
    draw.polygon(points([(0, 13), (3, 15), (2, 15), (0, 15)]), fill=OUTLINE)
    draw.line(points([(1, 14), (2, 15)]), fill=GOLD, width=1)
    draw.point((CELL_X, CELL_Y + 15), fill=GOLD_LIGHT)

    for y in range(sheet.height):
        for x in range(sheet.width):
            inside = CELL_X <= x < CELL_X + CELL_SIZE and CELL_Y <= y < CELL_Y + CELL_SIZE
            if not inside and sheet.getpixel((x, y)) != before.getpixel((x, y)):
                raise AssertionError(f"pixel outside index 144 changed at {x},{y}")


def validate(sheet: Image.Image) -> None:
    colors = set()
    bounds = [CELL_SIZE, CELL_SIZE, -1, -1]
    for y in range(CELL_SIZE):
        for x in range(CELL_SIZE):
            pixel = sheet.getpixel((CELL_X + x, CELL_Y + y))
            if pixel[3] not in (0, 255):
                raise AssertionError(f"partial alpha at {x},{y}: {pixel[3]}")
            if pixel[3] == 255:
                colors.add(pixel)
                bounds[0] = min(bounds[0], x)
                bounds[1] = min(bounds[1], y)
                bounds[2] = max(bounds[2], x)
                bounds[3] = max(bounds[3], y)

    if bounds != [0, 0, 15, 15]:
        raise AssertionError(f"unexpected sprite bounds: {bounds}")
    if len(colors) > 8:
        raise AssertionError(f"palette contains {len(colors)} opaque colors")

    for y in range(CELL_Y, CELL_Y + CELL_SIZE):
        for x in range(CELL_SIZE, sheet.width):
            if sheet.getpixel((x, y))[3] != 0:
                raise AssertionError(f"reserved tier-six pixel is occupied at {x},{y}")


def main() -> None:
    sheet = Image.open(SHEET_PATH).convert("RGBA")
    draw_sprite(sheet)
    validate(sheet)
    sheet.save(SHEET_PATH, format="PNG", optimize=True)


if __name__ == "__main__":
    main()
