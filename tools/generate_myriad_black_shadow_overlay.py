"""Generate the deterministic 4-frame Myriad Black Shadow overlay."""

from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "core/src/main/assets/sprites/myriad_black_shadow_overlay.png"

FRAME_SIZE = 16
PALETTE = {
    "transparent": (0, 0, 0, 0),
    "black": (0x05, 0x05, 0x07, 0xFF),
    "deep": (0x10, 0x0B, 0x16, 0xFF),
    "mid": (0x21, 0x13, 0x2E, 0xFF),
    "purple": (0x3A, 0x1B, 0x4C, 0xFF),
    "violet": (0x6B, 0x2A, 0x76, 0xFF),
}

GLYPH_COLORS = {
    ".": PALETTE["transparent"],
    "#": PALETTE["black"],
    "d": PALETTE["deep"],
    "m": PALETTE["mid"],
    "p": PALETTE["purple"],
    "v": PALETTE["violet"],
}

# Every character is one authored source pixel. Frames are, in order:
# idle (horn/claw/tail), run (raised tail), attack (split jaw/eyes/claw), die.
FRAME_GLYPHS = (
    (
        "................",
        "...#............",
        "..#d#...........",
        "...m#...........",
        "....#...........",
        "............#p#.",
        "...........#pm##",
        "............#m#.",
        "..............#.",
        "................",
        ".............###",
        "............#m##",
        "............##..",
        "................",
        "................",
        "................",
    ),
    (
        "................",
        "...#............",
        "..#d#...........",
        ".#dm#...........",
        "#dmp#...........",
        ".#m#.........##.",
        "..##........#p##",
        "...........#pm##",
        "............####",
        "..............##",
        "................",
        ".........##.....",
        "........#m#.....",
        "........##......",
        "................",
        "................",
    ),
    (
        "................",
        "...##....##.....",
        "..#d#....#d#....",
        "...#v....v#.....",
        "...##....##.#...",
        "...........#####",
        "..........#pmm##",
        "..........#pmm##",
        ".###.......#####",
        "#dm#........####",
        ".###.........###",
        "..#.........####",
        "................",
        "................",
        "................",
        "................",
    ),
    (
        "................",
        "................",
        "..#.........##..",
        "................",
        "................",
        "......#m#.......",
        "................",
        "................",
        ".#p#.........#..",
        "................",
        "................",
        "....###...#m#...",
        "................",
        ".#............#.",
        "................",
        "................",
    ),
)


def build_frames():
    frames = []
    for rows in FRAME_GLYPHS:
        if len(rows) != FRAME_SIZE or any(len(row) != FRAME_SIZE for row in rows):
            raise ValueError("Every overlay frame must be exactly 16x16 pixels")
        frame = Image.new("RGBA", (FRAME_SIZE, FRAME_SIZE), PALETTE["transparent"])
        pixels = frame.load()
        for y, row in enumerate(rows):
            for x, glyph in enumerate(row):
                pixels[x, y] = GLYPH_COLORS[glyph]
        frames.append(frame)
    return frames


def build_sheet():
    sheet = Image.new("RGBA", (FRAME_SIZE * 4, FRAME_SIZE), PALETTE["transparent"])
    for index, frame in enumerate(build_frames()):
        sheet.paste(frame, (index * FRAME_SIZE, 0))
    return sheet


def main():
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    build_sheet().save(OUTPUT)
    print(f"wrote {OUTPUT} (64x16 RGBA, 4 hard-edge frames)")


if __name__ == "__main__":
    main()
