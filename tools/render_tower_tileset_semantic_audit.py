from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
TILE = 16
SCALE = 4

THEMES = (
    ("SEWERS", "sewers"),
    ("HALLS", "halls"),
    ("CHINESE", "chinese_hall"),
    ("GOTHIC", "gothic_castle"),
    ("ASTRAL", "astral_library"),
    ("GEARWORKS", "tower_core_gearworks"),
    ("GREENHOUSE", "sky_alchemy_greenhouse"),
    ("FROST", "frost_archive"),
)

GROUPS = (
    ("FLAT DOORS", (56, 57, 58, 59)),
    ("RAISED DOORS", (112, 113, 114, 115, 116)),
    ("DOOR TOPS", (224, 225, 226, 227, 228, 229)),
    ("POT / WELL", (64, 120, 232, 18, 19)),
    ("GRASS BODY", (66, 67, 69, 70, 122, 123, 125, 126)),
    ("GRASS LAYERS", (234, 235, 237, 238, 250, 251, 253, 254)),
    ("WATER EDGES", tuple(range(32, 48))),
    ("BOOKSHELVES", (50, 54, 92, 93, 94, 95, 108, 109, 110, 111, 176, 179, 188, 191, 200, 203)),
    ("SOLID OBJECTS", (65, 72, 73, 74, 75, 76, 77, 78)),
    ("OBJECT LAYERS", (121, 128, 129, 130, 131, 132, 133, 134, 233, 240, 241, 242, 243, 244, 245, 246)),
)


def tile(atlas: Image.Image, index: int) -> Image.Image:
    x = index % 16 * TILE
    y = index // 16 * TILE
    return atlas.crop((x, y, x + TILE, y + TILE))


def render(output: Path) -> None:
    label_width = 100
    group_widths = [len(indices) * TILE * SCALE + 16 for _, indices in GROUPS]
    width = label_width + sum(group_widths)
    row_height = TILE * SCALE + 24
    height = 30 + len(THEMES) * row_height
    board = Image.new("RGB", (width, height), (25, 27, 29))
    draw = ImageDraw.Draw(board)

    x = label_width
    for (title, _indices), group_width in zip(GROUPS, group_widths):
        draw.text((x + 4, 8), title, fill=(215, 197, 119))
        x += group_width

    for row, (label, stem) in enumerate(THEMES):
        atlas = Image.open(ENV / f"tiles_{stem}.png").convert("RGBA")
        y = 30 + row * row_height
        draw.text((8, y + 24), label, fill=(220, 224, 226))
        x = label_width
        for _title, indices in GROUPS:
            for offset, index in enumerate(indices):
                checker = Image.new("RGB", (TILE, TILE), (45, 48, 51))
                cell = tile(atlas, index)
                checker.paste(cell, (0, 0), cell)
                checker = checker.resize((TILE * SCALE, TILE * SCALE), Image.Resampling.NEAREST)
                board.paste(checker, (x + offset * TILE * SCALE, y))
                draw.text((x + offset * TILE * SCALE + 2, y + TILE * SCALE + 2), str(index), fill=(145, 151, 154))
            x += len(indices) * TILE * SCALE + 16

    output.parent.mkdir(parents=True, exist_ok=True)
    board.save(output)


def render_vertical_door_composites(output: Path) -> None:
    pairs = (("CLOSED", 224, 112), ("LOCKED", 224, 114), ("CRYSTAL", 226, 115))
    cell_width = TILE * SCALE + 24
    cell_height = TILE * SCALE * 2 + 34
    label_width = 100
    board = Image.new(
        "RGB",
        (label_width + len(pairs) * cell_width, 30 + len(THEMES) * cell_height),
        (25, 27, 29),
    )
    draw = ImageDraw.Draw(board)
    for column, (label, _upper, _lower) in enumerate(pairs):
        draw.text((label_width + column * cell_width + 4, 8), label, fill=(215, 197, 119))

    for row, (label, stem) in enumerate(THEMES):
        atlas = Image.open(ENV / f"tiles_{stem}.png").convert("RGBA")
        y = 30 + row * cell_height
        draw.text((8, y + TILE * SCALE - 3), label, fill=(220, 224, 226))
        for column, (_state, upper_index, lower_index) in enumerate(pairs):
            composite = Image.new("RGB", (TILE, TILE * 2), (45, 48, 51))
            upper = tile(atlas, upper_index)
            lower = tile(atlas, lower_index)
            composite.paste(upper, (0, 0), upper)
            composite.paste(lower, (0, TILE), lower)
            composite = composite.resize(
                (TILE * SCALE, TILE * SCALE * 2),
                Image.Resampling.NEAREST,
            )
            x = label_width + column * cell_width
            board.paste(composite, (x, y))
            draw.text(
                (x + 2, y + TILE * SCALE * 2 + 2),
                f"{upper_index}+{lower_index}",
                fill=(145, 151, 154),
            )

    board.save(output.with_name(f"{output.stem}_door_composites.png"))


def render_wall_sandwiched_door_composites(output: Path) -> None:
    pairs = (("CLOSED", 227, 116), ("LOCKED", 228, 117), ("CRYSTAL", 229, 118))
    cell_width = TILE * SCALE + 24
    cell_height = TILE * SCALE * 2 + 34
    label_width = 100
    board = Image.new(
        "RGB",
        (label_width + len(pairs) * cell_width, 30 + len(THEMES) * cell_height),
        (25, 27, 29),
    )
    draw = ImageDraw.Draw(board)
    for column, (label, _upper, _lower) in enumerate(pairs):
        draw.text((label_width + column * cell_width + 4, 8), label, fill=(215, 197, 119))

    for row, (label, stem) in enumerate(THEMES):
        atlas = Image.open(ENV / f"tiles_{stem}.png").convert("RGBA")
        y = 30 + row * cell_height
        draw.text((8, y + TILE * SCALE - 3), label, fill=(220, 224, 226))
        for column, (_state, upper_index, lower_index) in enumerate(pairs):
            pair = Image.new("RGB", (TILE, TILE * 2), (45, 48, 51))
            upper = tile(atlas, upper_index)
            lower = tile(atlas, lower_index)
            pair.paste(upper, (0, 0), upper)
            pair.paste(lower, (0, TILE), lower)
            pair = pair.resize((TILE * SCALE, TILE * SCALE * 2), Image.Resampling.NEAREST)
            x = label_width + column * cell_width
            board.paste(pair, (x, y))
            draw.text(
                (x + 2, y + TILE * SCALE * 2 + 2),
                f"{upper_index}+{lower_index}",
                fill=(145, 151, 154),
            )

    board.save(output.with_name(f"{output.stem}_wall_sandwiched_doors.png"))


def render_full_wall_sandwiched_door_composites(output: Path) -> None:
    states = (
        ("CLOSED", 227, 212),
        ("LOCKED", 228, 216),
        ("CRYSTAL", 229, 220),
    )
    cell_width = TILE * SCALE + 24
    cell_height = TILE * SCALE * 3 + 34
    label_width = 100
    board = Image.new(
        "RGB",
        (label_width + len(states) * cell_width, 30 + len(THEMES) * cell_height),
        (25, 27, 29),
    )
    draw = ImageDraw.Draw(board)
    for column, (label, _top, _overlay) in enumerate(states):
        draw.text((label_width + column * cell_width + 4, 8), label, fill=(215, 197, 119))

    for row, (label, stem) in enumerate(THEMES):
        atlas = Image.open(ENV / f"tiles_{stem}.png").convert("RGBA")
        y = 30 + row * cell_height
        draw.text((8, y + TILE * SCALE - 3), label, fill=(220, 224, 226))
        for column, (_state, top_index, overlay_index) in enumerate(states):
            composite = Image.new("RGB", (TILE, TILE * 3), (45, 48, 51))
            top = tile(atlas, top_index)
            middle = tile(atlas, 116)
            overlay = tile(atlas, overlay_index)
            bottom = tile(atlas, 80)
            composite.paste(top, (0, 0), top)
            composite.paste(middle, (0, TILE), middle)
            composite.paste(overlay, (0, TILE), overlay)
            composite.paste(bottom, (0, TILE * 2), bottom)
            composite = composite.resize(
                (TILE * SCALE, TILE * SCALE * 3),
                Image.Resampling.NEAREST,
            )
            x = label_width + column * cell_width
            board.paste(composite, (x, y))
            draw.text(
                (x + 2, y + TILE * SCALE * 3 + 2),
                f"{top_index} / 116+{overlay_index} / 80",
                fill=(145, 151, 154),
            )

    board.save(output.with_name(f"{output.stem}_full_wall_sandwiched_doors.png"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("output", type=Path)
    output = parser.parse_args().output
    render(output)
    render_vertical_door_composites(output)
    render_wall_sandwiched_door_composites(output)
    render_full_wall_sandwiched_door_composites(output)
