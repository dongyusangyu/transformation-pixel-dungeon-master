from __future__ import annotations

import importlib
import sys
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / "core" / "src" / "main" / "assets" / "environment"
TILE_SIZE = 16
SHEET_COLUMNS = 16

if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

GENERATORS = (
    "tools.generate_chinese_hall_tileset",
    "tools.generate_astral_library_tileset",
    "tools.generate_tower_core_gearworks_tileset",
    "tools.generate_sky_alchemy_greenhouse_tileset",
    "tools.generate_frost_archive_tileset",
)


def tile(image: Image.Image, index: int) -> Image.Image:
    left = index % SHEET_COLUMNS * TILE_SIZE
    top = index // SHEET_COLUMNS * TILE_SIZE
    return image.crop((left, top, left + TILE_SIZE, top + TILE_SIZE)).convert("RGBA")


def replace_tile(atlas: Image.Image, image: Image.Image, index: int) -> None:
    left = index % SHEET_COLUMNS * TILE_SIZE
    top = index // SHEET_COLUMNS * TILE_SIZE
    atlas.paste(image, (left, top))


def foreground_mask(subject: Image.Image, background: Image.Image) -> tuple[bool, ...]:
    return tuple(
        subject.getpixel((x, y)) != background.getpixel((x, y))
        for y in range(TILE_SIZE)
        for x in range(TILE_SIZE)
    )


def apply_gothic_castle_stairs() -> Path:
    atlas_path = ENV / "tiles_gothic_castle.png"
    reference_path = ENV / "tiles_halls.png"
    atlas = Image.open(atlas_path).convert("RGBA")
    reference = Image.open(reference_path).convert("RGBA")

    upward_mask = foreground_mask(tile(reference, 16), tile(reference, 0))
    downward_mask = foreground_mask(tile(reference, 17), tile(reference, 0))
    entrance_mask = foreground_mask(tile(atlas, 16), tile(atlas, 0))
    exit_mask = foreground_mask(tile(atlas, 17), tile(atlas, 0))

    if entrance_mask == downward_mask and exit_mask == upward_mask:
        downward_stair = tile(atlas, 16)
    elif entrance_mask == upward_mask and exit_mask == downward_mask:
        upward_stair = tile(atlas, 16)
        downward_stair = tile(atlas, 17)
        replace_tile(atlas, downward_stair, 16)
        replace_tile(atlas, upward_stair, 17)
    else:
        raise ValueError("gothic castle stair silhouettes do not match the halls contract")

    replace_tile(atlas, downward_stair, 22)
    atlas.save(atlas_path)
    return atlas_path


def generate() -> tuple[Path, ...]:
    outputs = []
    for module_name in GENERATORS:
        module = importlib.import_module(module_name)
        generated = module.generate()
        outputs.extend(Path(path) for path in generated)
    outputs.append(apply_gothic_castle_stairs())
    return tuple(outputs)


if __name__ == "__main__":
    for output in generate():
        print(output)
