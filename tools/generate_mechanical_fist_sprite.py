"""Generate a technological tower fist from the original rusted YOG fist row."""

from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "core/src/main/assets/sprites/yog_fists.png"
OUTPUT = ROOT / "core/src/main/assets/sprites/mechanical_fist.png"
PREVIEW = ROOT / "docs/pixel-art/mechanical-fist/mechanical_fist_preview.png"

FRAME_WIDTH = 24
FRAME_HEIGHT = 17
FRAME_COUNT = 10
ACTIVE_FRAME_COUNT = 7
RUSTED_ROW_TOP = FRAME_HEIGHT * 3

TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (10, 13, 17, 255)
GUNMETAL_SHADOW = (24, 30, 36, 255)
GUNMETAL_DARK = (42, 50, 58, 255)
GUNMETAL = (64, 75, 84, 255)
GUNMETAL_LIGHT = (92, 104, 112, 255)
STEEL_HIGHLIGHT = (132, 145, 151, 255)
BRASS_DARK = (91, 63, 29, 255)
BRASS = (142, 102, 48, 255)
BRASS_LIGHT = (192, 151, 73, 255)
CYAN_DARK = (18, 88, 96, 255)
CYAN = (34, 163, 174, 255)
CYAN_LIGHT = (109, 224, 222, 255)


def gunmetal_for_luminance(luminance):
	if luminance < 16:
		return OUTLINE
	if luminance < 40:
		return GUNMETAL_SHADOW
	if luminance < 68:
		return GUNMETAL_DARK
	if luminance < 92:
		return GUNMETAL
	if luminance < 118:
		return GUNMETAL_LIGHT
	return STEEL_HIGHLIGHT


def recolor(source_row):
	output = Image.new("RGBA", source_row.size, TRANSPARENT)
	for y in range(source_row.height):
		for x in range(source_row.width):
			red, green, blue, alpha = source_row.getpixel((x, y))
			if alpha == 0:
				output.putpixel((x, y), TRANSPARENT)
			else:
				output.putpixel((x, y), gunmetal_for_luminance((red + green + blue) // 3))
	return output


def paint_if_opaque(image, frame, x, y, color):
	absolute_x = frame * FRAME_WIDTH + x
	if image.getpixel((absolute_x, y))[3] == 255:
		image.putpixel((absolute_x, y), color)


def add_technology_details(image):
	for frame in range(ACTIVE_FRAME_COUNT):
		# Compact energy core and conduits remain readable at native scale.
		for x, y, color in (
				(10, 5, CYAN_DARK), (11, 5, CYAN), (12, 5, CYAN_DARK),
				(10, 6, CYAN), (11, 6, CYAN_LIGHT), (12, 6, CYAN),
				(9, 8, CYAN_DARK), (10, 8, CYAN), (13, 8, CYAN),
				(14, 8, CYAN_DARK)):
			paint_if_opaque(image, frame, x, y, color)

		# Brass knuckle joints and armor fasteners break up the gunmetal mass.
		for x in (5, 9, 14, 18):
			paint_if_opaque(image, frame, x, 11, BRASS_DARK)
			paint_if_opaque(image, frame, x, 12, BRASS)
			paint_if_opaque(image, frame, x, 13, BRASS_LIGHT)
		for x, y in ((6, 4), (17, 5), (6, 14), (16, 14)):
			paint_if_opaque(image, frame, x, y, BRASS_LIGHT)

		# Attack/casting poses expose brighter piston rails.
		if frame in (5, 6):
			for x in range(7, 17):
				paint_if_opaque(
					image,
					frame,
					x,
					9,
					CYAN_LIGHT if x in (10, 13) else CYAN_DARK,
				)


def generate():
	source = Image.open(SOURCE).convert("RGBA")
	if source.size != (256, 128):
		raise ValueError(f"Unexpected YOG fist sprite size: {source.size}")

	source_row = source.crop(
		(0, RUSTED_ROW_TOP, FRAME_WIDTH * FRAME_COUNT, RUSTED_ROW_TOP + FRAME_HEIGHT)
	)
	output = recolor(source_row)
	add_technology_details(output)

	OUTPUT.parent.mkdir(parents=True, exist_ok=True)
	output.save(OUTPUT, optimize=False)

	PREVIEW.parent.mkdir(parents=True, exist_ok=True)
	output.resize(
		(output.width * 8, output.height * 8),
		resample=Image.Resampling.NEAREST,
	).save(PREVIEW, optimize=False)


if __name__ == "__main__":
	generate()
