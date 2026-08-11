from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SHEET_PATH = ROOT / "core/src/main/assets/sprites/ex_items.png"
CELL_SIZE = 16
ROW_Y = 144


def draw_blade(sheet: Image.Image, cell_x: int, palette: dict[str, tuple[int, int, int, int]],
               corrupted: bool) -> None:
    draw = ImageDraw.Draw(sheet)
    draw.rectangle((cell_x, ROW_Y, cell_x + 15, ROW_Y + 15), fill=(0, 0, 0, 0))

    def pts(coords):
        return [(cell_x + x, ROW_Y + y) for x, y in coords]

    # A slim, slightly curved tachi silhouette. The exposed bounds are 15x16.
    draw.polygon(
        pts([(14, 0), (14, 2), (13, 3), (12, 5), (10, 6), (9, 8),
             (7, 10), (5, 10), (6, 8), (8, 7), (9, 5), (11, 4),
             (12, 2), (13, 0)]),
        fill=palette["outline"],
    )
    draw.line(pts([(13, 1), (12, 3), (11, 4), (9, 6), (8, 8), (6, 9)]),
              fill=palette["blade_mid"], width=2)
    draw.line(pts([(13, 1), (12, 3), (10, 5), (9, 6), (7, 8)]),
              fill=palette["blade_light"], width=1)
    draw.line(pts([(12, 4), (10, 6), (9, 8), (7, 9)]),
              fill=palette["blade_shadow"], width=1)

    # Compact flower-shaped guard and collar.
    draw.polygon(pts([(4, 8), (6, 8), (9, 11), (8, 13), (6, 12), (3, 10)]),
                 fill=palette["outline"])
    draw.polygon(pts([(5, 9), (6, 9), (8, 11), (7, 12), (5, 11), (4, 10)]),
                 fill=palette["guard"])
    draw.point((cell_x + 6, ROW_Y + 10), fill=palette["accent"])
    draw.point((cell_x + 7, ROW_Y + 11), fill=palette["accent_light"])

    # Wrapped handle and pointed pommel.
    draw.polygon(pts([(5, 10), (7, 12), (2, 15), (0, 15), (0, 14)]),
                 fill=palette["outline"])
    draw.line(pts([(5, 11), (1, 15)]), fill=palette["guard"], width=2)
    draw.point((cell_x + 4, ROW_Y + 12), fill=palette["accent"])
    draw.point((cell_x + 2, ROW_Y + 14), fill=palette["accent_light"])

    if corrupted:
        # The evolved form keeps the silhouette but gains a blood-red ridge and pale knot.
        draw.line(pts([(13, 2), (11, 4), (10, 6), (8, 8), (7, 9)]),
                  fill=palette["accent"], width=1)
        draw.point((cell_x + 4, ROW_Y + 9), fill=palette["accent_light"])
        draw.point((cell_x + 5, ROW_Y + 8), fill=palette["accent_light"])
        draw.point((cell_x + 8, ROW_Y + 12), fill=palette["accent_light"])
    else:
        # Two detached sakura pixels make the base form read as blossom-themed at 1x scale.
        draw.point((cell_x + 10, ROW_Y + 2), fill=palette["accent"])
        draw.point((cell_x + 11, ROW_Y + 1), fill=palette["accent_light"])


def validate_cell(sheet: Image.Image, cell_x: int, max_colors: int = 8) -> None:
    colors = set()
    bounds = [CELL_SIZE, CELL_SIZE, -1, -1]
    for y in range(CELL_SIZE):
        for x in range(CELL_SIZE):
            pixel = sheet.getpixel((cell_x + x, ROW_Y + y))
            if pixel[3] not in (0, 255):
                raise AssertionError(f"partial alpha at {cell_x + x},{ROW_Y + y}")
            if pixel[3] == 255:
                colors.add(pixel)
                bounds[0] = min(bounds[0], x)
                bounds[1] = min(bounds[1], y)
                bounds[2] = max(bounds[2], x)
                bounds[3] = max(bounds[3], y)
    if bounds != [0, 0, 14, 15]:
        raise AssertionError(f"unexpected bounds for cell x={cell_x}: {bounds}")
    if len(colors) > max_colors:
        raise AssertionError(f"cell x={cell_x} uses {len(colors)} opaque colors")


def main() -> None:
    sheet = Image.open(SHEET_PATH).convert("RGBA")
    before = sheet.copy()

    spirit = {
        "outline": (35, 25, 42, 255),
        "blade_shadow": (137, 104, 131, 255),
        "blade_mid": (220, 207, 218, 255),
        "blade_light": (255, 248, 246, 255),
        "guard": (80, 42, 61, 255),
        "accent": (225, 91, 142, 255),
        "accent_light": (255, 184, 207, 255),
    }
    demon = {
        "outline": (28, 13, 23, 255),
        "blade_shadow": (90, 8, 26, 255),
        "blade_mid": (171, 18, 45, 255),
        "blade_light": (255, 112, 75, 255),
        "guard": (69, 28, 40, 255),
        "accent": (235, 35, 52, 255),
        "accent_light": (241, 224, 216, 255),
    }

    draw_blade(sheet, 16, spirit, corrupted=False)
    draw_blade(sheet, 32, demon, corrupted=True)
    validate_cell(sheet, 16)
    validate_cell(sheet, 32)

    for y in range(sheet.height):
        for x in range(sheet.width):
            inside_new_cells = ROW_Y <= y < ROW_Y + 16 and 16 <= x < 48
            if not inside_new_cells and sheet.getpixel((x, y)) != before.getpixel((x, y)):
                raise AssertionError(f"pixel outside indices 145/146 changed at {x},{y}")

    sheet.save(SHEET_PATH, format="PNG", optimize=True)


if __name__ == "__main__":
    main()
