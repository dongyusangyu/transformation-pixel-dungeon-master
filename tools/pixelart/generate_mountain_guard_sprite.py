from pathlib import Path
from PIL import Image


TARGET_INDEX = 160
CELL = 16
SHEET_COLUMNS = 16
ROWS = (
    ".....OOOOOO.....",
    "...OOOMMMMOOO...",
    "..OOMMMMMMMMOO..",
    ".OOMMLLSSLLMMOO.",
    ".OMMLLSSSSLLMMO.",
    "OMMMLLMMMMLLMMMO",
    "OMMMMMMCCMMMMMMO",
    "OMMMMCCCCCCMMMMO",
    "OMMCCMMCCMMCCMMO",
    ".OMCMMMMMMMMCMO.",
    ".OMMCMDDDDMCMMO.",
    "..OMMCDDDDCMMO..",
    "..OOMMCCCCMMOO..",
    "...OOMMMMMMOO...",
    "....OOMMMMOO....",
    "......OOOO......",
)
PALETTE = {
    ".": (0, 0, 0, 0),
    "O": (43, 39, 49, 255),
    "D": (59, 68, 75, 255),
    "M": (91, 105, 112, 255),
    "L": (132, 148, 151, 255),
    "S": (218, 232, 229, 255),
    "C": (89, 211, 220, 255),
}


def main() -> None:
    repo = Path(__file__).resolve().parents[2]
    sheet_path = repo / "core/src/main/assets/sprites/ex_items.png"
    with Image.open(sheet_path) as source:
        sheet = source.convert("RGBA")
    if sheet.size != (256, 512):
        raise ValueError(f"expected 256x512 EX item sheet, got {sheet.size}")
    if len(ROWS) != CELL or any(len(row) != CELL for row in ROWS):
        raise ValueError("mountain guard matrix must be exactly 16x16")
    if any(pixel not in PALETTE for row in ROWS for pixel in row):
        raise ValueError("mountain guard matrix contains an unknown palette symbol")

    original = tuple(sheet.getdata())
    cell_x = TARGET_INDEX % SHEET_COLUMNS * CELL
    cell_y = TARGET_INDEX // SHEET_COLUMNS * CELL
    for y, row in enumerate(ROWS):
        for x, symbol in enumerate(row):
            sheet.putpixel((cell_x + x, cell_y + y), PALETTE[symbol])
    sheet.save(sheet_path)

    written = tuple(sheet.getdata())
    for y in range(sheet.height):
        for x in range(sheet.width):
            in_target = cell_x <= x < cell_x + CELL and cell_y <= y < cell_y + CELL
            offset = y * sheet.width + x
            if not in_target and written[offset] != original[offset]:
                raise AssertionError(f"non-target pixel changed at {x},{y}")


if __name__ == "__main__":
    main()
