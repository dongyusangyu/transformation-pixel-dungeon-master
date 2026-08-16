from __future__ import annotations

from collections.abc import Iterable

from PIL import Image


TILE_SIZE = 16
SHEET_COLUMNS = 16
FIXED_OBJECT_SLOTS = (18, 19, 64, 120, 232)
SOLID_OBJECT_GROUPS = tuple(zip(range(65, 79), range(121, 135), range(233, 247)))
SOLID_OBJECT_GROUPS = tuple(
    group for group in SOLID_OBJECT_GROUPS
    if group[0] not in (66, 67, 68, 69, 70, 71)
)
BOOKSHELF_PAIRS = (
    (50, 48),
    (54, 52),
    *((92 + offset, 80 + offset) for offset in range(4)),
    *((108 + offset, 96 + offset) for offset in range(4)),
    *((176 + offset, 144 + offset) for offset in range(16)),
    *((200 + offset, 192 + offset) for offset in range(4)),
)


def tile(image: Image.Image, index: int) -> Image.Image:
    left = index % SHEET_COLUMNS * TILE_SIZE
    top = index // SHEET_COLUMNS * TILE_SIZE
    return image.crop((left, top, left + TILE_SIZE, top + TILE_SIZE)).convert("RGBA")


def replace_tile(atlas: Image.Image, image: Image.Image, index: int) -> None:
    left = index % SHEET_COLUMNS * TILE_SIZE
    top = index // SHEET_COLUMNS * TILE_SIZE
    atlas.paste(image, (left, top))


def _luminance(color: tuple[int, int, int, int]) -> float:
    return 0.2126 * color[0] + 0.7152 * color[1] + 0.0722 * color[2]


def _color_distance(first: tuple[int, ...], second: tuple[int, ...]) -> float:
    return sum((first[channel] - second[channel]) ** 2 for channel in range(3)) ** 0.5


def _blend(
        first: tuple[int, int, int, int],
        second: tuple[int, int, int, int],
        amount: float) -> tuple[int, int, int, int]:
    return tuple(
        round(first[channel] * (1 - amount) + second[channel] * amount)
        for channel in range(3)
    ) + (first[3],)


def _separate_color(
        color: tuple[int, int, int, int],
        background: tuple[int, int, int, int],
        minimum: float) -> tuple[int, int, int, int]:
    if _color_distance(color, background) >= minimum:
        return color
    black = (8, 10, 12, color[3])
    white = (230, 224, 203, color[3])
    candidates = [color]
    for anchor in (black, white):
        candidates.extend(_blend(color, anchor, amount) for amount in (0.25, 0.4, 0.55, 0.7))
    qualifying = [candidate for candidate in candidates if _color_distance(candidate, background) >= minimum]
    if qualifying:
        return min(qualifying, key=lambda candidate: _color_distance(candidate, color))
    return max(candidates, key=lambda candidate: _color_distance(candidate, background))


def _best_background(subject: Image.Image, candidates: Iterable[Image.Image]) -> Image.Image:
    return min(
        candidates,
        key=lambda background: sum(
            subject.getpixel((x, y)) != background.getpixel((x, y))
            for y in range(TILE_SIZE)
            for x in range(TILE_SIZE)
        ),
    )


def _foreground_positions(subject: Image.Image, background: Image.Image) -> list[tuple[int, int]]:
    return [
        (x, y)
        for y in range(TILE_SIZE)
        for x in range(TILE_SIZE)
        if subject.getpixel((x, y)) != background.getpixel((x, y))
    ]


def _theme_palette(subject: Image.Image, background: Image.Image) -> list[tuple[int, int, int, int]]:
    colors = {
        subject.getpixel((x, y))
        for y in range(TILE_SIZE)
        for x in range(TILE_SIZE)
        if subject.getpixel((x, y))[3] and subject.getpixel((x, y)) != background.getpixel((x, y))
    }
    if not colors:
        colors = {pixel for pixel in subject.getdata() if pixel[3]}
    return sorted(colors, key=_luminance)


def _crystal_material_palette(atlas: Image.Image, floor: Image.Image) -> list[tuple[int, int, int, int]]:
    crystal = set(_theme_palette(tile(atlas, 59), floor))
    ordinary: set[tuple[int, int, int, int]] = set()
    for index in (56, 57, 58):
        ordinary.update(_theme_palette(tile(atlas, index), floor))
    specific = [
        color
        for color in crystal - ordinary
        if color[1] + color[2] >= color[0] * 2 + 12
    ]
    if not specific:
        specific = list(crystal - ordinary) or list(crystal)
    specific = sorted(specific, key=_luminance)
    if len(specific) >= 3:
        return specific

    base = specific[-1]
    generated = {
        _blend(base, (8, 14, 18, 255), 0.38),
        base,
        _blend(base, (205, 242, 239, 255), 0.34),
    }
    return sorted(generated, key=_luminance)


def _lock_material_palette(atlas: Image.Image, floor: Image.Image) -> list[tuple[int, int, int, int]]:
    locked = set(_theme_palette(tile(atlas, 58), floor))
    ordinary: set[tuple[int, int, int, int]] = set()
    for index in (56, 57, 59):
        ordinary.update(_theme_palette(tile(atlas, index), floor))
    specific = sorted(locked - ordinary, key=_luminance)
    if not specific:
        specific = sorted(locked, key=_luminance)
    if len(specific) > 6:
        specific = [specific[round(index * (len(specific) - 1) / 5)] for index in range(6)]
    if len(specific) >= 3:
        return specific

    base = specific[-1]
    generated = {
        *specific,
        _blend(base, (20, 14, 9, 255), 0.38),
        _blend(base, (244, 218, 139, 255), 0.32),
    }
    return sorted(generated, key=_luminance)


def _recolor_reference(
        reference: Image.Image,
        palette: list[tuple[int, int, int, int]],
        background: Image.Image | None = None,
        reference_background: Image.Image | None = None) -> Image.Image:
    result = background.copy() if background is not None else Image.new("RGBA", reference.size, (0, 0, 0, 0))
    visible = [pixel for pixel in reference.getdata() if pixel[3]]
    source_luminances = [_luminance(pixel) for pixel in visible]
    low = min(source_luminances, default=0)
    high = max(source_luminances, default=255)
    span = max(1, high - low)

    for y in range(TILE_SIZE):
        for x in range(TILE_SIZE):
            source = reference.getpixel((x, y))
            if source[3] == 0:
                continue
            if reference_background is not None and source == reference_background.getpixel((x, y)):
                continue
            rank = (_luminance(source) - low) / span
            palette_index = round(rank * (len(palette) - 1))
            color = palette[palette_index]
            if background is not None and color == background.getpixel((x, y)):
                color = next(
                    (candidate for candidate in palette if candidate != background.getpixel((x, y))),
                    color,
                )
            result.putpixel((x, y), (color[0], color[1], color[2], source[3]))
    return result


def _paint_reference_pixels(
        result: Image.Image,
        reference: Image.Image,
        positions: list[tuple[int, int]],
        palette: list[tuple[int, int, int, int]],
        background: Image.Image) -> None:
    luminances = [_luminance(reference.getpixel(position)) for position in positions]
    low = min(luminances, default=0)
    high = max(luminances, default=255)
    span = max(1, high - low)
    for position in positions:
        source = reference.getpixel(position)
        rank = (_luminance(source) - low) / span
        color = palette[round(rank * (len(palette) - 1))]
        if color == background.getpixel(position):
            color = next(
                (candidate for candidate in palette if candidate != background.getpixel(position)),
                color,
            )
        result.putpixel(position, (color[0], color[1], color[2], source[3]))


def _recolor_raised_door(
        references: list[Image.Image],
        state: int,
        reference_floor: Image.Image,
        floor: Image.Image,
        wall_palette: list[tuple[int, int, int, int]],
        door_palette: list[tuple[int, int, int, int]]) -> Image.Image:
    reference = references[state]
    wall_positions: list[tuple[int, int]] = []
    door_positions: list[tuple[int, int]] = []
    for y in range(TILE_SIZE):
        for x in range(TILE_SIZE):
            position = (x, y)
            source = reference.getpixel(position)
            if source == reference_floor.getpixel(position):
                continue
            if all(other.getpixel(position) == source for other in references):
                wall_positions.append(position)
            else:
                door_positions.append(position)

    result = floor.copy()
    _paint_reference_pixels(result, reference, wall_positions, wall_palette, floor)
    _paint_reference_pixels(result, reference, door_positions, door_palette, floor)
    return result


def _paint_lock_emblem(
        door: Image.Image,
        palette: list[tuple[int, int, int, int]]) -> Image.Image:
    result = door.copy()
    dark, mid, light = palette[0], palette[len(palette) // 2], palette[-1]
    dark_pixels = {
        (6, 4), (7, 3), (8, 3), (9, 4), (6, 5), (9, 5),
        *((x, 6) for x in range(5, 11)),
        *((x, 10) for x in range(5, 11)),
        *((5, y) for y in range(7, 10)),
        *((10, y) for y in range(7, 10)),
        (7, 8), (8, 8), (8, 9),
    }
    mid_pixels = {
        (7, 4), (8, 4), (7, 5), (8, 5),
        *((x, y) for y in range(7, 10) for x in range(6, 10)),
    } - dark_pixels
    for position in dark_pixels:
        result.putpixel(position, dark)
    for position in mid_pixels:
        result.putpixel(position, mid)
    result.putpixel((6, 7), light)
    result.putpixel((9, 7), light)
    return result


def _shared_color_map(
        references: Iterable[Image.Image],
        palette: list[tuple[int, int, int, int]]) -> dict[tuple[int, int, int], tuple[int, int, int, int]]:
    colors = sorted(
        {
            pixel[:3]
            for reference in references
            for pixel in reference.getdata()
            if pixel[3]
        },
        key=lambda color: _luminance((*color, 255)),
    )
    if not colors:
        return {}
    return {
        color: palette[round(index * (len(palette) - 1) / max(1, len(colors) - 1))]
        for index, color in enumerate(colors)
    }


def _recolor_with_shared_map(
        reference: Image.Image,
        color_map: dict[tuple[int, int, int], tuple[int, int, int, int]]) -> Image.Image:
    result = Image.new("RGBA", reference.size, (0, 0, 0, 0))
    for y in range(TILE_SIZE):
        for x in range(TILE_SIZE):
            source = reference.getpixel((x, y))
            if source[3]:
                target = color_map[source[:3]]
                result.putpixel((x, y), (target[0], target[1], target[2], source[3]))
    return result


def _combined_palette(atlas: Image.Image, indices: Iterable[int]) -> list[tuple[int, int, int, int]]:
    colors = {
        pixel
        for index in indices
        for pixel in tile(atlas, index).getdata()
        if pixel[3]
    }
    return sorted(colors, key=_luminance)


def _normalize_wall_overhangs(
        atlas: Image.Image,
        halls: Image.Image,
        door_maps: dict[str, dict[tuple[int, int, int], tuple[int, int, int, int]]]) -> None:
    wall_families = (
        (range(192, 196), _combined_palette(atlas, range(80, 84))),
        (range(196, 200), _combined_palette(atlas, range(84, 88))),
        (range(200, 204), _combined_palette(atlas, range(92, 96))),
    )
    for indices, palette in wall_families:
        references = [tile(halls, index) for index in indices]
        color_map = _shared_color_map(references, palette)
        for index, reference in zip(indices, references):
            replace_tile(atlas, _recolor_with_shared_map(reference, color_map), index)

    transparent = Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 0))
    for index in range(204, 208):
        replace_tile(atlas, transparent, index)

    for state, start in (("open", 208), ("closed", 212), ("locked", 216), ("crystal", 220)):
        for variant in range(4):
            reference_wall = tile(halls, 192 + variant)
            reference_door = tile(halls, start + variant)
            target_wall = tile(atlas, 192 + variant)
            result = Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 0))
            for y in range(TILE_SIZE):
                for x in range(TILE_SIZE):
                    source_wall = reference_wall.getpixel((x, y))
                    source_door = reference_door.getpixel((x, y))
                    if source_door == source_wall:
                        result.putpixel((x, y), target_wall.getpixel((x, y)))
                    elif source_door[3]:
                        target = door_maps[state][source_door[:3]]
                        result.putpixel((x, y), (target[0], target[1], target[2], source_door[3]))
            replace_tile(atlas, result, start + variant)


def _normalize_fixed_objects(atlas: Image.Image, sewers: Image.Image) -> None:
    for index in FIXED_OBJECT_SLOTS:
        replace_tile(atlas, tile(sewers, index), index)


def _bookshelf_palette(atlas: Image.Image) -> list[tuple[int, int, int, int]]:
    colors: set[tuple[int, int, int, int]] = set()
    for shelf_index, wall_index in ((50, 48), (54, 52)):
        shelf = tile(atlas, shelf_index)
        wall = tile(atlas, wall_index)
        colors.update(
            shelf.getpixel((x, y))
            for y in range(TILE_SIZE)
            for x in range(TILE_SIZE)
            if shelf.getpixel((x, y))[3] and shelf.getpixel((x, y)) != wall.getpixel((x, y))
        )
    if len(colors) < 4:
        colors.update(pixel for pixel in atlas.getdata() if pixel[3])
    ordered = sorted(colors, key=_luminance)
    if len(ordered) > 8:
        ordered = [ordered[round(index * (len(ordered) - 1) / 7)] for index in range(8)]
    return ordered


def _normalize_bookshelves(atlas: Image.Image, halls: Image.Image) -> None:
    palette = _bookshelf_palette(atlas)
    for shelf_index, wall_index in BOOKSHELF_PAIRS:
        subject = tile(atlas, shelf_index)
        reference = tile(halls, shelf_index)
        reference_wall = tile(halls, wall_index)
        wall = tile(atlas, wall_index)
        positions = _foreground_positions(subject, wall)
        source = subject
        if not positions and _foreground_positions(reference, reference_wall):
            positions = _foreground_positions(reference, reference_wall)
            source = reference
        source_luminances = [_luminance(source.getpixel(position)) for position in positions]
        low = min(source_luminances, default=0)
        high = max(source_luminances, default=255)
        span = max(1, high - low)
        mean_distance = sum(
            _color_distance(source.getpixel(position), wall.getpixel(position))
            for position in positions
        ) / max(1, len(positions))
        if source is subject and mean_distance >= 42:
            continue
        result = subject.copy()
        for position in positions:
            color = source.getpixel(position)
            if source is reference:
                rank = (_luminance(color) - low) / span
                color = palette[round(rank * (len(palette) - 1))]
            color = _separate_color(color, wall.getpixel(position), 42)
            result.putpixel(position, color)
        replace_tile(atlas, result, shelf_index)


def _theme_object_palette(atlas: Image.Image) -> list[tuple[int, int, int, int]]:
    floor_candidates = (tile(atlas, 0), tile(atlas, 4))
    colors: set[tuple[int, int, int, int]] = set()
    for index in (65, 72, 73, 74, 75, 76, 77, 78):
        subject = tile(atlas, index)
        background = _best_background(subject, floor_candidates)
        positions = _foreground_positions(subject, background)
        if 24 <= len(positions) <= 200:
            colors.update(subject.getpixel(position) for position in positions)
    if len(colors) < 4:
        colors.update(_bookshelf_palette(atlas))
    return sorted(colors, key=_luminance)


def _rebuild_object_from_reference(
        reference: Image.Image,
        reference_background: Image.Image,
        background: Image.Image,
        palette: list[tuple[int, int, int, int]]) -> Image.Image:
    result = background.copy()
    positions = _foreground_positions(reference, reference_background)
    luminances = [_luminance(reference.getpixel(position)) for position in positions]
    low = min(luminances, default=0)
    high = max(luminances, default=255)
    span = max(1, high - low)
    for position in positions:
        rank = (_luminance(reference.getpixel(position)) - low) / span
        color = palette[round(rank * (len(palette) - 1))]
        result.putpixel(position, _separate_color(color, background.getpixel(position), 50))
    return result


def _enhance_object(subject: Image.Image, background: Image.Image) -> Image.Image:
    positions = _foreground_positions(subject, background)
    if not positions:
        return subject
    mean_distance = sum(
        _color_distance(subject.getpixel(position), background.getpixel(position))
        for position in positions
    ) / len(positions)
    if mean_distance >= 50:
        return subject
    result = subject.copy()
    for position in positions:
        result.putpixel(
            position,
            _separate_color(subject.getpixel(position), background.getpixel(position), 52),
        )
    return result


def _normalize_solid_objects(atlas: Image.Image, halls: Image.Image) -> None:
    floor_candidates = (tile(atlas, 0), tile(atlas, 4))
    reference_floor_candidates = (tile(halls, 0), tile(halls, 4))
    palette = _theme_object_palette(atlas)
    for flat_index, raised_index, overhang_index in SOLID_OBJECT_GROUPS:
        subject = tile(atlas, flat_index)
        background = _best_background(subject, floor_candidates)
        positions = _foreground_positions(subject, background)
        rebuild_family = len(positions) > 220 or len(positions) < 24
        if rebuild_family:
            reference = tile(halls, flat_index)
            reference_background = _best_background(reference, reference_floor_candidates)
            subject = _rebuild_object_from_reference(reference, reference_background, background, palette)
        subject = _enhance_object(subject, background)
        replace_tile(atlas, subject, flat_index)

        raised = tile(atlas, raised_index)
        raised_background = _best_background(raised, floor_candidates)
        raised_positions = _foreground_positions(raised, raised_background)
        if rebuild_family or len(raised_positions) > 220 or len(raised_positions) < 12:
            reference = tile(halls, raised_index)
            reference_background = _best_background(reference, reference_floor_candidates)
            raised = _rebuild_object_from_reference(reference, reference_background, raised_background, palette)
        replace_tile(atlas, _enhance_object(raised, raised_background), raised_index)

        overhang = tile(atlas, overhang_index)
        if rebuild_family or overhang.getchannel("A").getbbox() is None:
            reference = tile(halls, overhang_index)
            overhang = _recolor_reference(reference, palette)
        replace_tile(atlas, overhang, overhang_index)


def _normalize_water_transitions(atlas: Image.Image, sewers: Image.Image, water: Image.Image) -> None:
    floor = tile(atlas, 0)
    water = water.convert("RGBA")
    for index in range(32, 48):
        reference = tile(sewers, index)
        transition = Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 0))
        for y in range(TILE_SIZE):
            for x in range(TILE_SIZE):
                alpha = reference.getpixel((x, y))[3]
                if alpha == 255:
                    transition.putpixel((x, y), floor.getpixel((x, y)))
                elif alpha:
                    color = water.getpixel((x % water.width, y % water.height))
                    transition.putpixel((x, y), (color[0], color[1], color[2], alpha))
        replace_tile(atlas, transition, index)


def _normalize_doors(
        atlas: Image.Image,
        halls: Image.Image) -> dict[str, dict[tuple[int, int, int], tuple[int, int, int, int]]]:
    floor = tile(atlas, 0)
    reference_floor = tile(halls, 0)
    flat_palette_by_state = {
        "closed": _theme_palette(tile(atlas, 56), floor),
        "open": _theme_palette(tile(atlas, 57), floor),
        "locked": _theme_palette(tile(atlas, 58), floor),
        "crystal": _theme_palette(tile(atlas, 59), floor),
    }
    palette_by_state = dict(flat_palette_by_state)

    palette_by_state["crystal"] = _crystal_material_palette(atlas, floor)
    lock_palette = _lock_material_palette(atlas, floor)

    wall_palette = _theme_palette(tile(atlas, 80), floor)
    raised_references = [tile(halls, index) for index in range(112, 116)]

    for state_index, (index, state) in enumerate(zip(range(112, 116), ("closed", "open", "locked", "crystal"))):
        source_state = 0 if state == "locked" else state_index
        normalized = _recolor_raised_door(
            raised_references,
            source_state,
            reference_floor,
            floor,
            wall_palette,
            palette_by_state["closed"] if state == "locked" else palette_by_state[state],
        )
        if state == "locked":
            normalized = _paint_lock_emblem(normalized, lock_palette)
        replace_tile(atlas, normalized, index)
    sideways_lower_reference = tile(halls, 116)
    replace_tile(
        atlas,
        _recolor_reference(
            sideways_lower_reference,
            palette_by_state["closed"],
            background=floor,
            reference_background=reference_floor,
        ),
        116,
    )
    transparent = Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 0))
    replace_tile(atlas, transparent, 117)
    replace_tile(atlas, transparent, 118)

    reference_indices = {
        "open": (225, *range(208, 212)),
        "closed": (224, 227, *range(212, 216)),
        "locked": (228, *range(216, 220)),
        "crystal": (226, 229, *range(220, 224)),
    }
    door_maps = {
        state: _shared_color_map(
            [tile(halls, index) for index in indices],
            flat_palette_by_state[state],
        )
        for state, indices in reference_indices.items()
    }
    overhang_states: tuple[tuple[int, str], ...] = (
        (224, "closed"),
        (225, "open"),
        (226, "crystal"),
        (227, "closed"),
        (228, "locked"),
        (229, "crystal"),
    )
    for index, state in overhang_states:
        replace_tile(atlas, _recolor_with_shared_map(tile(halls, index), door_maps[state]), index)

    for lower_index, upper_index in ((112, 224), (114, 224), (115, 226)):
        lower = tile(atlas, lower_index)
        upper = tile(atlas, upper_index)
        for x in range(2, TILE_SIZE - 2):
            color = upper.getpixel((x, TILE_SIZE - 1))
            if color == floor.getpixel((x, 0)):
                color = lower.getpixel((x, 0))
                upper.putpixel((x, TILE_SIZE - 1), color)
            lower.putpixel((x, 0), color)
        replace_tile(atlas, upper, upper_index)
        replace_tile(atlas, lower, lower_index)
    return door_maps


def normalize_tower_tileset(
        atlas: Image.Image,
        sewers: Image.Image,
        halls: Image.Image,
        water: Image.Image) -> Image.Image:
    atlas = atlas.convert("RGBA")
    _normalize_fixed_objects(atlas, sewers.convert("RGBA"))
    _normalize_water_transitions(atlas, sewers.convert("RGBA"), water)
    halls = halls.convert("RGBA")
    door_maps = _normalize_doors(atlas, halls)
    _normalize_bookshelves(atlas, halls)
    _normalize_solid_objects(atlas, halls)
    _normalize_wall_overhangs(atlas, halls, door_maps)
    return atlas
