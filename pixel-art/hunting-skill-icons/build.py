"""Build six Hunting Technique icons from existing project talent art."""

from __future__ import annotations

from collections import Counter
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps


ROOT = Path(__file__).resolve().parent
REPO_ROOT = ROOT.parents[1]
SOURCE_PATH = REPO_ROOT / "core/src/main/assets/interfaces/Talent_icon.png"
TILE_SIZE = 16
SHEET_COLUMNS = 32

# Existing Huntress and boss talents used as source material.
SOURCE_IDS = (98, 104, 106, 107, 109, 113, 114, 115, 116, 119, 121, 166)


def talent_icon(sheet: Image.Image, index: int) -> Image.Image:
    x = index % SHEET_COLUMNS * TILE_SIZE
    y = index // SHEET_COLUMNS * TILE_SIZE
    icon = sheet.crop((x, y, x + TILE_SIZE, y + TILE_SIZE)).convert("RGBA")

    # Some atlas pixels retain hidden RGB values under fully transparent alpha.
    clean = Image.new("RGBA", icon.size, (0, 0, 0, 0))
    clean.alpha_composite(icon)
    return clean


def source_color(icons: dict[int, Image.Image], predicate) -> tuple[int, int, int, int]:
    colors: Counter[tuple[int, int, int, int]] = Counter()
    for icon in icons.values():
        colors.update(pixel for pixel in icon.getdata() if pixel[3] == 255 and predicate(pixel))
    if not colors:
        raise AssertionError("required source color was not found")
    return colors.most_common(1)[0][0]


def composite(*layers: Image.Image) -> Image.Image:
    result = Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 0))
    for layer in layers:
        result.alpha_composite(layer)
    return result


def translated(layer: Image.Image, x: int, y: int) -> Image.Image:
    result = Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 0))
    result.alpha_composite(layer, (x, y))
    return result


def remove_connected_background(icon: Image.Image) -> Image.Image:
    """Makes the dominant flat class-color field transparent."""
    result = icon.copy()
    pixels = result.load()
    opaque = Counter(pixel for pixel in result.getdata() if pixel[3] == 255)
    background = opaque.most_common(1)[0][0]
    for y in range(TILE_SIZE):
        for x in range(TILE_SIZE):
            if pixels[x, y] == background:
                pixels[x, y] = (0, 0, 0, 0)
    return result


def draw_proc_mark(
        icon: Image.Image,
        center: tuple[int, int],
        outline: tuple[int, int, int, int],
        yellow: tuple[int, int, int, int],
        red: tuple[int, int, int, int],
) -> None:
    """Small two-stage spark matching the compact marks in existing talent art."""
    x, y = center
    draw = ImageDraw.Draw(icon)
    draw.point(((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)), fill=outline)
    draw.point(((x, y - 1), (x + 1, y)), fill=yellow)
    draw.point((x, y), fill=red)


def candidate_01(icons, colors) -> Image.Image:
    """Follow-up sword crossed by the durable projectile."""
    projectile = ImageOps.mirror(remove_connected_background(icons[104]))
    result = composite(icons[98], projectile)
    draw_proc_mark(result, (8, 8), colors["outline"], colors["yellow"], colors["red"])
    return result


def candidate_02(icons, colors) -> Image.Image:
    """Farsight reticle with a projectile and a compact proc center."""
    result = composite(icons[107], remove_connected_background(icons[104]))
    draw_proc_mark(result, (8, 8), colors["outline"], colors["yellow"], colors["red"])
    return result


def candidate_03(icons, colors) -> Image.Image:
    """Spirit hawk carrying a small melee blade."""
    result = icons[119].copy()
    draw = ImageDraw.Draw(result)
    draw.line((10, 13, 14, 9), fill=colors["outline"], width=2)
    draw.line((11, 12, 14, 9), fill=colors["white"])
    draw.point(((9, 13), (10, 14)), fill=colors["brown"])
    draw.point(((10, 10), (11, 9)), fill=colors["yellow"])
    return result


def candidate_04(icons, colors) -> Image.Image:
    """Fan of Blades with the two-channel trigger at its center."""
    result = icons[113].copy()
    draw_proc_mark(result, (9, 8), colors["outline"], colors["yellow"], colors["red"])
    return result


def candidate_05(icons, colors) -> Image.Image:
    """Nature's bow launching the durable projectile."""
    projectile = translated(remove_connected_background(icons[104]), 2, -1)
    result = composite(icons[116], projectile)
    draw_proc_mark(result, (7, 9), colors["outline"], colors["yellow"], colors["red"])
    return result


def candidate_06(icons, colors) -> Image.Image:
    """Spirit blade marked by Seer Shot's purple trajectory."""
    result = composite(icons[106], remove_connected_background(icons[115]))
    draw_proc_mark(result, (10, 9), colors["outline"], colors["yellow"], colors["red"])
    return result


CANDIDATES = (
    ("01_crossed_arrow_blade", candidate_01),
    ("02_half_target", candidate_02),
    ("03_hooded_hunter", candidate_03),
    ("04_dual_proc_impact", candidate_04),
    ("05_bow_wrapped_blade", candidate_05),
    ("06_hawkeye_mark", candidate_06),
)


def enlarge(icon: Image.Image, scale: int) -> Image.Image:
    return icon.resize((TILE_SIZE * scale, TILE_SIZE * scale), Image.Resampling.NEAREST)


def silhouette(icon: Image.Image, scale: int = 8) -> Image.Image:
    result = Image.new("RGBA", icon.size, (0, 0, 0, 0))
    result.paste((255, 255, 255, 255), mask=icon.getchannel("A"))
    return enlarge(result, scale)


def contact_sheet(icons: list[Image.Image]) -> Image.Image:
    scale = 10
    cell_w = TILE_SIZE * scale
    cell_h = TILE_SIZE * scale + 24
    sheet = Image.new("RGBA", (cell_w * 3, cell_h * 2), (28, 30, 34, 255))
    draw = ImageDraw.Draw(sheet)
    for number, icon in enumerate(icons, 1):
        x = (number - 1) % 3 * cell_w
        y = (number - 1) // 3 * cell_h
        sheet.alpha_composite(enlarge(icon, scale), (x, y))
        draw.text((x + 5, y + TILE_SIZE * scale + 4), f"{number:02d}", fill="white")
    return sheet


def source_reference(icons: dict[int, Image.Image]) -> Image.Image:
    scale = 6
    cell_w = TILE_SIZE * scale
    cell_h = TILE_SIZE * scale + 16
    result = Image.new("RGBA", (cell_w * 6, cell_h * 2), (28, 30, 34, 255))
    draw = ImageDraw.Draw(result)
    for number, index in enumerate(SOURCE_IDS):
        x = number % 6 * cell_w
        y = number // 6 * cell_h
        result.alpha_composite(enlarge(icons[index], scale), (x, y))
        draw.text((x + 3, y + TILE_SIZE * scale + 2), str(index), fill="white")
    return result


def style_comparison(icons: dict[int, Image.Image], candidates: list[Image.Image]) -> Image.Image:
    scale = 8
    cell = TILE_SIZE * scale
    label_h = 18
    references = (98, 107, 119, 113, 116, 115)
    result = Image.new("RGBA", (cell * 6, (cell + label_h) * 2), (28, 30, 34, 255))
    draw = ImageDraw.Draw(result)
    for column, index in enumerate(references):
        result.alpha_composite(enlarge(icons[index], scale), (column * cell, 0))
        draw.text((column * cell + 3, cell + 2), f"source {index}", fill="white")
    y = cell + label_h
    for column, icon in enumerate(candidates):
        result.alpha_composite(enlarge(icon, scale), (column * cell, y))
        draw.text((column * cell + 3, y + cell + 2), f"candidate {column + 1}", fill="white")
    return result


def palette_swatch(colors: set[tuple[int, int, int, int]]) -> Image.Image:
    ordered = sorted(colors, key=lambda color: (sum(color[:3]), color))
    cell = 16
    swatch = Image.new("RGBA", (cell * len(ordered), cell), (0, 0, 0, 0))
    draw = ImageDraw.Draw(swatch)
    for x, color in enumerate(ordered):
        draw.rectangle((x * cell, 0, x * cell + cell - 1, cell - 1), fill=color)
    return swatch


def main() -> None:
    sheet = Image.open(SOURCE_PATH).convert("RGBA")
    icons = {index: talent_icon(sheet, index) for index in SOURCE_IDS}
    source_palette = {
        pixel
        for icon in icons.values()
        for pixel in icon.getdata()
        if pixel[3] == 255
    }
    colors = {
        "outline": source_color(icons, lambda p: max(p[:3]) < 70),
        "white": source_color(icons, lambda p: min(p[:3]) > 180),
        "yellow": source_color(icons, lambda p: p[0] > 180 and p[1] > 150 and p[2] < 100),
        "red": source_color(icons, lambda p: p[0] > 150 and p[1] < 100 and p[2] < 100),
        "brown": source_color(icons, lambda p: p[0] > 70 and p[1] > 50 and p[2] < 80),
    }

    outputs: list[Image.Image] = []
    for name, factory in CANDIDATES:
        icon = factory(icons, colors)
        alpha_values = set(icon.getchannel("A").getdata())
        used_colors = {pixel for pixel in icon.getdata() if pixel[3] == 255}
        if not alpha_values.issubset({0, 255}):
            raise AssertionError(f"{name}: semi-transparent pixels detected")
        if not used_colors.issubset(source_palette):
            raise AssertionError(f"{name}: contains colors outside referenced project icons")

        icon.save(ROOT / f"hunting_skill_{name}.png")
        enlarge(icon, 8).save(ROOT / f"hunting_skill_{name}_preview_8x.png")
        silhouette(icon).save(ROOT / f"hunting_skill_{name}_silhouette.png")
        outputs.append(icon)

    contact_sheet(outputs).save(ROOT / "hunting_skill_candidates_preview.png")
    source_reference(icons).save(ROOT / "hunting_skill_source_reference.png")
    style_comparison(icons, outputs).save(ROOT / "hunting_skill_style_comparison.png")
    palette_swatch(source_palette).save(ROOT / "hunting_skill_palette.png")
    print(f"generated {len(outputs)} source-derived Hunting Technique candidates")


if __name__ == "__main__":
    main()
