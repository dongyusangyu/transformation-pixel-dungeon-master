"""Generate 16x16 sprite sheets for the Obscura and its Wild Dread."""

from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SPRITES = ROOT / "core/src/main/assets/sprites"
ART = ROOT / "docs/pixel-art/obscura"
SIZE = 16
SCALE = 8

T = (0, 0, 0, 0)
OUTLINE = (12, 11, 30, 255)
SHADOW = (27, 24, 58, 255)
INDIGO = (38, 42, 83, 255)
VIOLET = (62, 49, 103, 255)
TEAL_DARK = (13, 83, 91, 255)
TEAL = (19, 145, 132, 255)
GLOW = (63, 220, 159, 255)
BONE_DARK = (133, 135, 126, 255)
BONE = (213, 211, 180, 255)
PALE = (225, 255, 211, 255)


def canvas():
	image = Image.new("RGBA", (SIZE, SIZE), T)
	return image, ImageDraw.Draw(image)


def obscura_base(shift=0, tail=0, lean=0, core=GLOW):
	image, draw = canvas()
	# Curled spectral tail and a narrow upright torso retain a readable 1x silhouette.
	draw.polygon([(7 + lean, 8 + shift), (11, 9 + shift), (12, 12), (10, 15),
				  (5 + tail, 15), (3 + tail, 13), (5 + tail, 12), (8, 13),
				  (9, 11), (6 + lean, 10 + shift)], fill=OUTLINE)
	draw.polygon([(7 + lean, 9 + shift), (10, 10 + shift), (11, 12), (9, 14),
				  (6 + tail, 14), (5 + tail, 13), (8, 13), (9, 11),
				  (6 + lean, 11 + shift)], fill=INDIGO)
	draw.polygon([(6 + lean, 4 + shift), (9 + lean, 3 + shift), (11, 5 + shift),
				  (10, 10 + shift), (6, 11 + shift), (4 + lean, 7 + shift)], fill=OUTLINE)
	draw.polygon([(7 + lean, 5 + shift), (9 + lean, 5 + shift), (10, 6 + shift),
				  (9, 9 + shift), (6, 9 + shift), (5 + lean, 7 + shift)], fill=VIOLET)
	draw.polygon([(7, 7 + shift), (9, 7 + shift), (9, 9 + shift),
				  (8, 10 + shift), (6, 8 + shift)], fill=TEAL_DARK)
	draw.rectangle((7, 8 + shift, 8, 9 + shift), fill=core)
	# Pale asymmetric mask.
	draw.polygon([(6 + lean, 1 + shift), (10 + lean, 2 + shift), (11 + lean, 4 + shift),
				  (9 + lean, 7 + shift), (6 + lean, 6 + shift), (5 + lean, 3 + shift)], fill=OUTLINE)
	draw.polygon([(7 + lean, 2 + shift), (9 + lean, 3 + shift), (10 + lean, 4 + shift),
				  (9 + lean, 6 + shift), (7 + lean, 5 + shift), (6 + lean, 3 + shift)], fill=BONE)
	draw.point((8 + lean, 4 + shift), fill=GLOW)
	# Four restrained tendrils; single-pixel tips keep the outline crisp.
	draw.line([(6 + lean, 5 + shift), (3 + lean, 3 + shift), (2 + lean, 1 + shift)], fill=OUTLINE, width=2)
	draw.line([(5 + lean, 6 + shift), (2 + lean, 6 + shift), (1 + lean, 8 + shift)], fill=OUTLINE, width=2)
	draw.line([(10 + lean, 6 + shift), (13 + lean, 4 + shift), (13 + lean, 2 + shift)], fill=OUTLINE, width=2)
	draw.line([(10, 8 + shift), (13, 9 + shift), (14, 11 + shift)], fill=OUTLINE, width=2)
	draw.point((3 + lean, 3 + shift), fill=VIOLET)
	draw.point((12 + lean, 4 + shift), fill=TEAL)
	return image


def obscura_attack(stage):
	image = obscura_base(shift=0, tail=stage % 2, lean=stage - 1)
	draw = ImageDraw.Draw(image)
	if stage == 0:
		draw.line([(10, 8), (12, 7), (13, 6)], fill=OUTLINE, width=2)
	elif stage == 1:
		draw.polygon([(9, 7), (15, 6), (15, 8), (11, 9)], fill=OUTLINE)
		draw.line([(10, 7), (14, 7)], fill=TEAL, width=1)
		draw.point((15, 7), fill=PALE)
	else:
		draw.line([(9, 8), (13, 10), (14, 9)], fill=OUTLINE, width=2)
	return image


def obscura_summon(stage):
	image = obscura_base(shift=0 if stage < 3 else 1, core=PALE if stage >= 1 else GLOW)
	draw = ImageDraw.Draw(image)
	# Raised arms and a compact summoning ring read even at 16x16.
	draw.line([(5, 7), (3, 5 - min(stage, 2)), (2, 3 - min(stage, 1))], fill=OUTLINE, width=2)
	draw.line([(10, 7), (12, 5 - min(stage, 2)), (13, 3 - min(stage, 1))], fill=OUTLINE, width=2)
	if stage >= 1:
		draw.line([(3, 13), (5, 12), (10, 12), (12, 13), (10, 14), (5, 14), (3, 13)], fill=TEAL)
	if stage >= 2:
		draw.point((6, 13), fill=GLOW)
		draw.point((8, 12), fill=PALE)
		draw.point((10, 13), fill=GLOW)
	if stage == 3:
		draw.rectangle((7, 11, 8, 13), fill=PALE)
	return image


def obscura_death(stage):
	image, draw = canvas()
	if stage == 0:
		return obscura_base(shift=1, tail=1, lean=1, core=TEAL)
	if stage == 1:
		draw.polygon([(3, 10), (7, 7), (12, 9), (14, 12), (11, 15), (4, 15), (1, 13)], fill=OUTLINE)
		draw.polygon([(4, 10), (8, 8), (11, 10), (12, 13), (9, 14), (4, 14), (2, 13)], fill=INDIGO)
		draw.polygon([(8, 8), (11, 9), (10, 12), (7, 11)], fill=BONE)
		draw.point((8, 12), fill=GLOW)
	elif stage == 2:
		draw.polygon([(2, 12), (6, 10), (12, 11), (14, 14), (11, 15), (3, 15)], fill=OUTLINE)
		draw.polygon([(4, 12), (8, 11), (12, 12), (12, 14), (5, 14)], fill=SHADOW)
		draw.rectangle((7, 12, 9, 13), fill=TEAL_DARK)
	else:
		draw.line([(4, 14), (11, 14)], fill=SHADOW, width=2)
		draw.point((7, 13), fill=TEAL_DARK)
		draw.point((9, 13), fill=GLOW)
	return image


def wild_base(y=0, lean=0, core=GLOW):
	image, draw = canvas()
	draw.polygon([(3 + lean, 7 + y), (6 + lean, 4 + y), (11 + lean, 5 + y),
				  (14 + lean, 8 + y), (13 + lean, 12 + y), (10, 14 + y),
				  (4, 14 + y), (1 + lean, 11 + y)], fill=OUTLINE)
	draw.polygon([(4 + lean, 8 + y), (7 + lean, 5 + y), (10 + lean, 6 + y),
				  (12 + lean, 8 + y), (11, 11 + y), (8, 13 + y),
				  (4, 12 + y), (2 + lean, 10 + y)], fill=VIOLET)
	draw.polygon([(6 + lean, 6 + y), (10 + lean, 6 + y), (11 + lean, 8 + y),
				  (9 + lean, 10 + y), (6 + lean, 9 + y), (5 + lean, 7 + y)], fill=BONE_DARK)
	draw.polygon([(7 + lean, 6 + y), (9 + lean, 7 + y), (9 + lean, 9 + y),
				  (7 + lean, 8 + y)], fill=BONE)
	draw.point((8 + lean, 7 + y), fill=core)
	draw.rectangle((6, 10 + y, 9, 12 + y), fill=TEAL_DARK)
	draw.point((7, 11 + y), fill=core)
	# Claws anchor the squat phantom to the floor.
	draw.line([(4, 12 + y), (2, 15 + y)], fill=BONE, width=2)
	draw.line([(6, 13 + y), (5, 15 + y)], fill=BONE, width=2)
	draw.line([(10, 13 + y), (11, 15 + y)], fill=BONE, width=2)
	draw.line([(12, 12 + y), (14, 15 + y)], fill=BONE, width=2)
	return image


def wild_attack(stage):
	if stage == 0:
		return wild_base(y=-1, lean=-1, core=TEAL)
	image, draw = canvas()
	if stage == 1:
		draw.polygon([(2, 8), (6, 5), (12, 6), (15, 10), (13, 13), (4, 13)], fill=OUTLINE)
		draw.polygon([(4, 8), (7, 6), (11, 7), (13, 10), (11, 12), (5, 12)], fill=VIOLET)
		draw.rectangle((7, 8, 9, 10), fill=GLOW)
		draw.line([(4, 11), (1, 15)], fill=BONE, width=2)
		draw.line([(12, 11), (15, 15)], fill=BONE, width=2)
	else:
		draw.polygon([(1, 10), (5, 7), (12, 8), (15, 12), (13, 14), (3, 14)], fill=OUTLINE)
		draw.polygon([(4, 10), (7, 8), (11, 9), (13, 12), (10, 13), (4, 13)], fill=INDIGO)
		draw.rectangle((7, 10, 9, 12), fill=TEAL)
		draw.line([(3, 13), (1, 15)], fill=BONE, width=2)
		draw.line([(12, 13), (15, 15)], fill=BONE, width=2)
	return image


def wild_down(stage):
	image, draw = canvas()
	if stage == 0:
		draw.polygon([(2, 10), (6, 7), (12, 8), (15, 12), (13, 15), (3, 15)], fill=OUTLINE)
		draw.polygon([(4, 10), (7, 8), (11, 9), (13, 12), (11, 14), (4, 14)], fill=VIOLET)
		draw.rectangle((7, 11, 9, 13), fill=GLOW)
	elif stage == 1:
		draw.polygon([(2, 12), (6, 10), (12, 11), (14, 14), (12, 15), (3, 15)], fill=OUTLINE)
		draw.polygon([(5, 12), (8, 11), (11, 12), (12, 14), (4, 14)], fill=SHADOW)
		draw.rectangle((7, 12, 9, 13), fill=TEAL)
	else:
		draw.line([(4, 14), (11, 14)], fill=SHADOW, width=2)
		draw.point((7, 13), fill=TEAL_DARK)
		draw.point((8, 13), fill=GLOW)
	return image


def save_sheet(name, frames):
	SPRITES.mkdir(parents=True, exist_ok=True)
	ART.mkdir(parents=True, exist_ok=True)
	sheet = Image.new("RGBA", (len(frames) * SIZE, SIZE), T)
	for index, frame in enumerate(frames):
		sheet.paste(frame, (index * SIZE, 0))
	sheet.save(SPRITES / f"{name}.png")
	preview = sheet.resize((sheet.width * SCALE, sheet.height * SCALE), Image.Resampling.NEAREST)
	preview.save(ART / f"{name}_preview.png")


def main():
	obscura = [
		obscura_base(core=GLOW),
		obscura_base(shift=0, tail=1, core=PALE),
		obscura_base(shift=0, tail=-1, lean=-1),
		obscura_base(shift=-1, tail=0),
		obscura_base(shift=0, tail=1, lean=1),
		obscura_base(shift=1, tail=0, core=TEAL),
		*(obscura_attack(i) for i in range(3)),
		*(obscura_summon(i) for i in range(4)),
		*(obscura_death(i) for i in range(4)),
	]
	wild = [
		wild_base(core=GLOW),
		wild_base(core=PALE),
		wild_base(y=0, lean=-1),
		wild_base(y=-1, lean=0),
		wild_base(y=0, lean=1, core=TEAL),
		wild_base(y=-1, lean=0, core=PALE),
		*(wild_attack(i) for i in range(3)),
		*(wild_down(i) for i in range(3)),
	]
	save_sheet("obscura", obscura)
	save_sheet("wild_dread", wild)


if __name__ == "__main__":
	main()
