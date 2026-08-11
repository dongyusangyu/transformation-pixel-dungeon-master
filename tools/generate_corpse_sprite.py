"""Recolor the Warrior cloth-armor frames into the tower Corpse sprite."""

from pathlib import Path

from PIL import Image


FRAME_WIDTH = 12
FRAME_HEIGHT = 15
FRAME_COUNT = 16

PALETTE = {
    "outline": (31, 24, 38, 255),
    "hair_shadow": (51, 35, 42, 255),
    "hair": (79, 55, 51, 255),
    "hair_light": (111, 81, 65, 255),
    "skin_shadow": (67, 82, 62, 255),
    "skin": (99, 118, 83, 255),
    "skin_light": (147, 157, 113, 255),
    "cloth_shadow": (66, 58, 54, 255),
    "cloth": (105, 94, 79, 255),
    "cloth_light": (151, 136, 108, 255),
    "wound": (91, 54, 88, 255),
}

HAIR_DARK = {
    (117, 43, 14),
    (126, 34, 11),
    (151, 49, 13),
}
HAIR_MID = {
    (173, 105, 60),
    (190, 57, 12),
    (196, 112, 58),
}
HAIR_LIGHT = {
    (220, 121, 55),
}
SKIN_DARK = {
    (184, 151, 120),
    (190, 127, 89),
}
SKIN_MID = {
    (220, 180, 151),
    (255, 186, 143),
}
SKIN_LIGHT = {
    (255, 218, 191),
}
CLOTH_DARK = {
    (126, 126, 126),
    (133, 89, 56),
    (163, 163, 163),
}
CLOTH_MID = {
    (179, 179, 179),
    (204, 204, 204),
    (206, 206, 206),
}
CLOTH_LIGHT = {
    (229, 229, 229),
    (255, 255, 255),
}


def mapped_color(rgb, death_frame):
    if rgb == (0, 0, 0):
        return PALETTE["outline"]
    if death_frame and rgb in CLOTH_DARK:
        return PALETTE["skin_shadow"]
    if death_frame and rgb in CLOTH_MID:
        return PALETTE["skin"]
    if death_frame and rgb in CLOTH_LIGHT:
        return PALETTE["skin_light"]
    if rgb in HAIR_DARK:
        return PALETTE["hair_shadow"]
    if rgb in HAIR_MID:
        return PALETTE["hair"]
    if rgb in HAIR_LIGHT:
        return PALETTE["hair_light"]
    if rgb in SKIN_DARK:
        return PALETTE["skin_shadow"]
    if rgb in SKIN_MID:
        return PALETTE["skin"]
    if rgb in SKIN_LIGHT:
        return PALETTE["skin_light"]
    if rgb in CLOTH_DARK:
        return PALETTE["cloth_shadow"]
    if rgb in CLOTH_MID:
        return PALETTE["cloth"]
    if rgb in CLOTH_LIGHT:
        return PALETTE["cloth_light"]
    raise ValueError(f"unmapped Warrior color: {rgb}")


def recolor_frame(source, frame_index):
    left = frame_index * FRAME_WIDTH
    original = source.crop((left, 0, left + FRAME_WIDTH, FRAME_HEIGHT))
    result = Image.new("RGBA", (FRAME_WIDTH, FRAME_HEIGHT))
    source_pixels = original.load()
    result_pixels = result.load()
    death_frame = 8 <= frame_index <= 12

    for y in range(FRAME_HEIGHT):
        for x in range(FRAME_WIDTH):
            red, green, blue, alpha = source_pixels[x, y]
            if alpha > 0:
                result_pixels[x, y] = mapped_color(
                    (red, green, blue), death_frame
                )

    add_corpse_details(result, frame_index)
    return result


def replace_if_opaque(image, x, y, color):
    if 0 <= x < FRAME_WIDTH and 0 <= y < FRAME_HEIGHT:
        if image.getpixel((x, y))[3] == 255:
            image.putpixel((x, y), PALETTE[color])


def add_corpse_details(image, frame_index):
    bounds = image.getchannel("A").getbbox()
    if bounds is None:
        return

    left, top, right, bottom = bounds
    center_x = (left + right - 1) // 2

    if 8 <= frame_index <= 12:
        replace_if_opaque(image, center_x + 1, top + 2, "wound")
        replace_if_opaque(image, center_x - 1, top + 3, "outline")
        return

    # The Warrior faces right. Keep the socket on the same side of the head
    # while its exact cell follows the original animation's head position.
    replace_if_opaque(image, center_x + 1, top + 3, "outline")
    replace_if_opaque(image, center_x + 2, top + 4, "wound")

    torso_y = min(bottom - 1, top + 7)
    replace_if_opaque(image, center_x, torso_y, "wound")

    leg_y = min(bottom - 1, top + 11)
    replace_if_opaque(
        image,
        max(left, center_x - 2 + frame_index % 2),
        leg_y,
        "skin_shadow",
    )


def main():
    root = Path(__file__).resolve().parents[1]
    source_path = root / "core/src/main/assets/sprites/warrior.png"
    asset_path = root / "core/src/main/assets/sprites/corpse.png"
    preview_path = root / "docs/pixel-art/corpse/corpse_preview.png"

    with Image.open(source_path) as source:
        source = source.convert("RGBA")
        if source.size != (256, 128):
            raise ValueError(f"unexpected Warrior sheet size: {source.size}")
        frames = [
            recolor_frame(source, frame_index)
            for frame_index in range(FRAME_COUNT)
        ]

    sheet = Image.new(
        "RGBA", (FRAME_WIDTH * FRAME_COUNT, FRAME_HEIGHT)
    )
    for index, sprite_frame in enumerate(frames):
        if sprite_frame.getbbox() is None:
            raise ValueError(f"frame {index} is empty")
        sheet.alpha_composite(sprite_frame, (index * FRAME_WIDTH, 0))

    asset_path.parent.mkdir(parents=True, exist_ok=True)
    preview_path.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(asset_path)
    sheet.resize(
        (sheet.width * 8, sheet.height * 8),
        Image.Resampling.NEAREST,
    ).save(preview_path)

    print(f"wrote {asset_path}")
    print(f"wrote {preview_path}")


if __name__ == "__main__":
    main()
