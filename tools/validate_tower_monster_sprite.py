"""Validate small, hard-edged tower monster sprite sheets."""

import argparse
import json
from pathlib import Path
from typing import Dict, Optional

from PIL import Image


class SpriteValidationError(ValueError):
    """Raised when a sprite sheet violates the tower monster art contract."""


def validate_sprite(
    image_path: Path,
    frame_width: int,
    frame_height: int,
    expected_frames: Optional[int] = None,
    max_colors: int = 12,
) -> Dict[str, int]:
    image_path = Path(image_path)
    if not image_path.is_file():
        raise SpriteValidationError(f"sprite does not exist: {image_path}")
    if frame_width <= 0 or frame_height <= 0:
        raise SpriteValidationError("frame dimensions must be positive")
    if frame_width > 16 or frame_height > 16:
        raise SpriteValidationError("each frame must fit within 16x16 pixels")
    if max_colors <= 0:
        raise SpriteValidationError("max_colors must be positive")

    with Image.open(image_path) as source:
        if source.format != "PNG":
            raise SpriteValidationError("sprite sheet must be a PNG")
        if source.mode != "RGBA":
            raise SpriteValidationError("sprite sheet must use RGBA mode")
        image = source.copy()

    width, height = image.size
    if width % frame_width != 0 or height % frame_height != 0:
        raise SpriteValidationError(
            f"sheet {width}x{height} is not divisible by frame "
            f"{frame_width}x{frame_height}"
        )

    columns = width // frame_width
    rows = height // frame_height
    frame_count = columns * rows
    if expected_frames is not None and frame_count != expected_frames:
        raise SpriteValidationError(
            f"expected {expected_frames} frames, found {frame_count}"
        )

    visible_colors = set()
    semi_transparent = 0
    hidden_rgb = 0
    for red, green, blue, alpha in image.getdata():
        if alpha not in (0, 255):
            semi_transparent += 1
        elif alpha == 0 and (red or green or blue):
            hidden_rgb += 1
        elif alpha == 255:
            visible_colors.add((red, green, blue))

    if semi_transparent:
        raise SpriteValidationError(
            f"alpha must be hard-edged; found {semi_transparent} semitransparent pixels"
        )
    if hidden_rgb:
        raise SpriteValidationError(
            f"transparent pixels must have zero RGB to avoid halos; found {hidden_rgb}"
        )
    if len(visible_colors) > max_colors:
        raise SpriteValidationError(
            f"visible palette uses {len(visible_colors)} colors; maximum is {max_colors}"
        )

    for row in range(rows):
        for column in range(columns):
            frame_index = row * columns + column
            frame = image.crop(
                (
                    column * frame_width,
                    row * frame_height,
                    (column + 1) * frame_width,
                    (row + 1) * frame_height,
                )
            )
            alpha = frame.getchannel("A")
            if alpha.getbbox() is None:
                raise SpriteValidationError(f"frame {frame_index} is empty")

    return {
        "width": width,
        "height": height,
        "frame_width": frame_width,
        "frame_height": frame_height,
        "frame_count": frame_count,
        "visible_colors": len(visible_colors),
    }


def parse_args():
    parser = argparse.ArgumentParser(
        description="Validate a hard-edged tower monster PNG sprite sheet."
    )
    parser.add_argument("image", type=Path)
    parser.add_argument("--frame-width", type=int, required=True)
    parser.add_argument("--frame-height", type=int, required=True)
    parser.add_argument("--expected-frames", type=int)
    parser.add_argument("--max-colors", type=int, default=12)
    return parser.parse_args()


def main():
    args = parse_args()
    try:
        report = validate_sprite(
            args.image,
            args.frame_width,
            args.frame_height,
            expected_frames=args.expected_frames,
            max_colors=args.max_colors,
        )
    except SpriteValidationError as error:
        raise SystemExit(f"FAIL: {error}")
    print("PASS")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
