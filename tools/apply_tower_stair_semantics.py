from __future__ import annotations

import importlib
import sys
from pathlib import Path

from PIL import Image, ImageDraw

try:
    from tools.tower_tileset_semantics import normalize_tower_tileset
except ImportError:
    from tower_tileset_semantics import normalize_tower_tileset


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


def recolor_vegetation_layer(
        subject: Image.Image,
        source_background: Image.Image | None,
        target_background: Image.Image | None,
        palette: tuple[tuple[int, int, int, int], ...]) -> Image.Image:
    result = (
        target_background.copy()
        if target_background is not None
        else Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 0))
    )
    pixels = []
    for y in range(TILE_SIZE):
        for x in range(TILE_SIZE):
            color = subject.getpixel((x, y))
            if color[3] and (source_background is None or color != source_background.getpixel((x, y))):
                pixels.append((x, y, color))
    luminances = [0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2] for _x, _y, c in pixels]
    low = min(luminances, default=0)
    high = max(luminances, default=255)
    span = max(1, high - low)
    for x, y, color in pixels:
        luminance = 0.2126 * color[0] + 0.7152 * color[1] + 0.0722 * color[2]
        rank = (luminance - low) / span
        replacement = palette[round(rank * (len(palette) - 1))]
        result.putpixel((x, y), replacement)
    return result


def apply_gothic_castle_vegetation(atlas: Image.Image) -> None:
    source = Image.open(ENV / "tiles_chinese_hall.png").convert("RGBA")
    source_floor = tile(source, 0)
    target_floor = tile(atlas, 0)
    live_palette = (
        (25, 39, 31, 255),
        (42, 63, 43, 255),
        (67, 88, 55, 255),
        (91, 112, 67, 255),
    )
    dry_palette = (
        (42, 38, 35, 255),
        (67, 57, 47, 255),
        (91, 76, 58, 255),
        (116, 98, 72, 255),
    )
    groups = (
        ((66, 69, 122, 125), live_palette),
        ((67, 70, 123, 126), dry_palette),
    )
    for indices, palette in groups:
        for index in indices:
            replace_tile(
                atlas,
                recolor_vegetation_layer(tile(source, index), source_floor, target_floor, palette),
                index,
            )
        for index in (indices[0] + 168, indices[1] + 168, indices[0] + 184, indices[1] + 184):
            replace_tile(
                atlas,
                recolor_vegetation_layer(tile(source, index), None, None, palette),
                index,
            )


def draw_gothic_low_obstacle(background: Image.Image, variant: int, raised: bool = False) -> Image.Image:
    image = background.copy()
    draw = ImageDraw.Draw(image)
    offset = -3 if raised else 0
    outline = (18, 17, 25, 255)
    stone_dark = (50, 53, 61, 255)
    stone_mid = (88, 92, 101, 255)
    stone_light = (139, 142, 148, 255)
    iron = (74, 78, 87, 255)
    rust = (112, 43, 48, 255)
    wood = (65, 42, 38, 255)

    if variant == 0:
        draw.ellipse((2, 10 + offset, 13, 15), fill=outline)
        draw.polygon(((3, 13 + offset), (5, 8 + offset), (9, 9 + offset), (12, 13 + offset)), fill=stone_dark)
        draw.polygon(((5, 11 + offset), (7, 6 + offset), (10, 11 + offset)), fill=stone_mid)
        draw.line((7, 7 + offset, 7, 11 + offset), fill=stone_light)
        draw.point((10, 12 + offset), fill=rust)
    elif variant == 1:
        draw.ellipse((1, 11 + offset, 14, 15), fill=outline)
        draw.rectangle((3, 10 + offset, 7, 14 + offset), fill=stone_dark)
        draw.rectangle((8, 8 + offset, 12, 14 + offset), fill=stone_mid)
        draw.line((2, 12 + offset, 13, 10 + offset), fill=iron, width=2)
        draw.point((5, 11 + offset), fill=rust)
        draw.point((10, 10 + offset), fill=rust)
    else:
        draw.line((1, 15, 14, 15), fill=outline, width=2)
        for x, height in ((3, 6), (7, 9), (11, 7)):
            top = 15 - height + offset
            draw.polygon(((x - 2, 14), (x, top), (x + 2, 14)), fill=wood)
            draw.line((x, top + 1, x, 13), fill=iron)
        draw.line((2, 12 + offset, 13, 12 + offset), fill=rust)
    return image


def apply_gothic_castle_objects(atlas: Image.Image) -> None:
    floor = tile(atlas, 0)
    floor_sp = tile(atlas, 4)
    for variant, (flat_index, raised_index) in enumerate(zip(range(76, 79), range(132, 135))):
        background = floor_sp if variant == 1 else floor
        replace_tile(atlas, draw_gothic_low_obstacle(background, variant), flat_index)
        replace_tile(atlas, draw_gothic_low_obstacle(background, variant, raised=True), raised_index)
        replace_tile(
            atlas,
            Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 0)),
            244 + variant,
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
    apply_gothic_castle_vegetation(atlas)
    apply_gothic_castle_objects(atlas)
    atlas = normalize_tower_tileset(
        atlas,
        Image.open(ENV / "tiles_sewers.png"),
        reference,
        Image.open(ENV / "water_gothic_castle.png"),
    )
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
