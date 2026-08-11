"""Generate the upright 16x16, 13-frame mimic crocodile sprite sheet."""

from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "core/src/main/assets/sprites/mimic_crocodile.png"
PREVIEW = ROOT / "docs/pixel-art/mimic-crocodile/mimic_crocodile_preview.png"

FRAME_SIZE = 16
FRAME_COUNT = 13

TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (11, 16, 14, 255)
SCALE_SHADOW = (31, 39, 25, 255)
SCUTE = (53, 56, 32, 255)
OLIVE_DARK = (50, 66, 38, 255)
OLIVE = (78, 94, 50, 255)
OLIVE_LIGHT = (111, 122, 68, 255)
BELLY = (101, 88, 48, 255)
CYAN_DARK = (18, 93, 101, 255)
CYAN = (48, 183, 188, 255)
TOOTH = (205, 197, 139, 255)
BLOOD = (105, 33, 29, 255)


def draw_tail(draw, lift=0):
	draw.polygon(
		[(0, 14 - lift), (3, 12 - lift), (6, 10), (9, 9),
		 (10, 11), (7, 13), (3, 15), (0, 15)],
		fill=OUTLINE,
	)
	draw.polygon(
		[(1, 14 - lift), (4, 12 - lift), (7, 11), (9, 10),
		 (9, 11), (6, 12), (3, 14)],
		fill=OLIVE_DARK,
	)
	draw.point((4, 11 - lift), fill=SCUTE)
	draw.point((6, 10), fill=SCUTE)


def draw_legs(draw, step=0):
	# Three distinct planted silhouettes provide a readable heavy bipedal gait.
	if step == 1:
		back = [(7, 10), (9, 10), (8, 13), (6, 15), (3, 15), (5, 14), (6, 11)]
		front = [(9, 10), (11, 10), (12, 13), (15, 14), (15, 15), (11, 15), (9, 12)]
	elif step == 2:
		back = [(7, 10), (9, 10), (9, 13), (11, 15), (8, 15), (7, 13)]
		front = [(9, 10), (11, 10), (10, 13), (7, 15), (4, 15), (6, 14), (8, 11)]
	else:
		back = [(7, 10), (9, 10), (9, 13), (8, 15), (5, 15), (6, 13)]
		front = [(9, 10), (11, 10), (12, 14), (14, 14), (14, 15), (10, 15), (9, 12)]

	for leg in (back, front):
		draw.polygon(leg, fill=OUTLINE)
	inner_back = [(7, 11), (8, 11), (8, 13), (7, 14), (6, 14)]
	inner_front = [(9, 11), (10, 11), (11, 14), (12, 14), (10, 14)]
	draw.polygon(inner_back, fill=OLIVE_DARK)
	draw.polygon(inner_front, fill=OLIVE)


def upright_pose(step=0, alert=False, breathe=False, tail_lift=0):
	frame = Image.new("RGBA", (FRAME_SIZE, FRAME_SIZE), TRANSPARENT)
	draw = ImageDraw.Draw(frame)
	draw_tail(draw, tail_lift)
	draw_legs(draw, step)

	# Thick upright torso, forward shoulders, and a pale armored belly.
	draw.polygon(
		[(6, 4), (9, 3), (11, 5), (11, 9), (10, 12),
		 (7, 12), (6, 9)], fill=OUTLINE)
	draw.polygon(
		[(7, 5), (9, 4), (10, 5), (10, 9), (9, 11),
		 (8, 11), (7, 9)], fill=OLIVE)
	draw.polygon([(9, 5), (10, 6), (10, 10), (9, 11), (8, 9)], fill=BELLY)

	# Broad crocodile muzzle prevents the compact kaiju from reading as a lizard-man.
	head_y = 0 if alert else (2 if breathe else 1)
	draw.polygon(
		[(6, 4), (8, 2 + head_y), (10, head_y), (14, 1 + head_y),
		 (15, 2 + head_y), (15, 5 + head_y), (12, 6 + head_y),
		 (8, 5 + head_y)], fill=OUTLINE)
	draw.polygon(
		[(8, 3 + head_y), (10, 1 + head_y), (14, 2 + head_y),
		 (14, 4 + head_y), (12, 5 + head_y), (9, 4 + head_y)],
		fill=OLIVE,
	)
	draw.line([(11, 4 + head_y), (15, 4 + head_y)], fill=SCALE_SHADOW)
	draw.point((13, 2 + head_y), fill=CYAN if alert else OLIVE_LIGHT)
	draw.point((14, 5 + head_y), fill=TOOTH)

	# Short clawed forearms, dorsal plates, and sparse cyan mimic markings.
	draw.polygon([(10, 7), (12, 8), (14, 8), (13, 10), (11, 9)], fill=OUTLINE)
	draw.point((12, 8), fill=OLIVE_LIGHT)
	draw.point((14, 9), fill=TOOTH)
	for x, y in ((6, 3), (5, 5), (5, 7), (6, 9)):
		draw.point((x, y), fill=SCUTE)
		draw.point((x + 1, y + 1), fill=OLIVE_LIGHT)
	draw.point((8, 6), fill=CYAN_DARK)
	draw.point((9, 7), fill=CYAN)
	draw.point((8, 9), fill=CYAN_DARK)
	return frame


def open_bite_pose():
	frame = upright_pose(step=1, alert=True, tail_lift=1)
	draw = ImageDraw.Draw(frame)
	draw.rectangle((7, 0, 15, 8), fill=TRANSPARENT)

	# Neck and separated jaws create a strong vertical bite silhouette.
	draw.polygon([(6, 5), (8, 3), (11, 4), (11, 8), (8, 9), (6, 8)], fill=OUTLINE)
	draw.polygon([(7, 5), (9, 4), (10, 5), (10, 7), (8, 8), (7, 7)], fill=OLIVE)
	draw.polygon([(8, 4), (10, 1), (14, 0), (15, 1), (15, 3), (11, 4)], fill=OUTLINE)
	draw.polygon([(10, 2), (14, 1), (14, 2), (11, 3)], fill=OLIVE_LIGHT)
	draw.polygon([(9, 6), (12, 7), (15, 7), (15, 10), (12, 9), (9, 8)], fill=OUTLINE)
	draw.polygon([(11, 7), (14, 8), (14, 9), (12, 8)], fill=BELLY)
	draw.point((12, 2), fill=CYAN)
	for x, y in ((10, 4), (12, 4), (10, 6), (13, 7)):
		draw.point((x, y), fill=TOOTH)
	draw.point((6, 4), fill=SCUTE)
	draw.point((6, 6), fill=SCUTE)
	return frame


def kneeling_pose():
	frame = Image.new("RGBA", (FRAME_SIZE, FRAME_SIZE), TRANSPARENT)
	draw = ImageDraw.Draw(frame)
	draw_tail(draw)
	draw.polygon([(6, 7), (9, 5), (12, 7), (12, 12), (9, 14), (6, 12)], fill=OUTLINE)
	draw.polygon([(8, 7), (9, 6), (11, 8), (11, 11), (9, 12), (7, 11)], fill=OLIVE_DARK)
	draw.polygon([(9, 8), (11, 8), (14, 9), (15, 10), (14, 12), (10, 11)], fill=OUTLINE)
	draw.polygon([(11, 9), (14, 10), (13, 11), (10, 10)], fill=OLIVE)
	draw.polygon([(7, 11), (10, 11), (12, 14), (15, 14), (15, 15), (10, 15), (8, 13)], fill=OUTLINE)
	draw.point((13, 10), fill=CYAN_DARK)
	draw.point((6, 6), fill=SCUTE)
	draw.point((6, 8), fill=SCUTE)
	return frame


def fallen_pose():
	frame = Image.new("RGBA", (FRAME_SIZE, FRAME_SIZE), TRANSPARENT)
	draw = ImageDraw.Draw(frame)
	draw.polygon(
		[(0, 13), (4, 11), (7, 9), (11, 10), (12, 11), (15, 11),
		 (15, 14), (11, 15), (5, 14), (2, 15)], fill=OUTLINE)
	draw.polygon(
		[(2, 13), (5, 12), (8, 10), (11, 11), (14, 12),
		 (14, 13), (10, 14), (5, 13)], fill=OLIVE_DARK)
	for x, y in ((6, 10), (8, 9), (10, 10)):
		draw.point((x, y), fill=SCUTE)
	draw.point((13, 12), fill=SCALE_SHADOW)
	draw.point((10, 15), fill=BLOOD)
	return frame


def generate():
	frames = [
		upright_pose(step=0, alert=True),
		upright_pose(step=0, breathe=True),
		upright_pose(step=0, alert=True),
		upright_pose(step=1, tail_lift=1),
		upright_pose(step=0, breathe=True),
		upright_pose(step=2, alert=True),
		upright_pose(step=0, breathe=True),
		upright_pose(step=1, tail_lift=1),
		upright_pose(step=2, alert=True, tail_lift=1),
		open_bite_pose(),
		upright_pose(step=1, alert=True),
		kneeling_pose(),
		fallen_pose(),
	]

	sheet = Image.new("RGBA", (FRAME_SIZE * FRAME_COUNT, FRAME_SIZE), TRANSPARENT)
	for index, frame in enumerate(frames):
		sheet.paste(frame, (index * FRAME_SIZE, 0))

	OUTPUT.parent.mkdir(parents=True, exist_ok=True)
	sheet.save(OUTPUT, optimize=False)
	PREVIEW.parent.mkdir(parents=True, exist_ok=True)
	sheet.resize(
		(sheet.width * 8, sheet.height * 8),
		resample=Image.Resampling.NEAREST,
	).save(PREVIEW, optimize=False)


if __name__ == "__main__":
	generate()
