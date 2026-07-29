"""Extract canonical HeroSprite reference pixels without resampling.

The generated PNG files are documentation/reference assets. They are not loaded
by the game. Every native-size output pixel is copied directly from the source
sprite sheet or selected by exact RGBA consensus across existing hero sheets.
"""

from __future__ import annotations

import hashlib
import json
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SPRITE_DIR = ROOT / "core" / "src" / "main" / "assets" / "sprites"
OUTPUT_DIR = ROOT / "docs" / "pixel-art" / "reference-assets"

FRAME_WIDTH = 12
FRAME_HEIGHT = 15
ACTIVE_TIERS = range(7)
SCROLL_FRAMES = (19, 20)
DEATH_FRAMES = (8, 9, 10, 11, 12)

CONSENSUS_SHEETS = (
    "warrior.png",
    "mage.png",
    "rogue.png",
    "huntress.png",
    "duelist.png",
    "cleric.png",
    "freeman.png",
    "ninja.png",
    "princess.png",
    "friar.png",
)

EXPECTED_SCROLL_PIXELS = {
    19: {
        (5, 9): "#988B63FF",
        (6, 9): "#988B63FF",
        (7, 9): "#988B63FF",
        (6, 10): "#C5BC9FFF",
        (7, 10): "#C5BC9FFF",
        (6, 11): "#988B63FF",
        (7, 11): "#988B63FF",
        (8, 11): "#988B63FF",
    },
    20: {
        (5, 7): "#988B63FF",
        (5, 8): "#988B63FF",
        (6, 8): "#C5BC9FFF",
        (7, 8): "#C5BC9FFF",
        (8, 8): "#988B63FF",
        (5, 9): "#988B63FF",
        (6, 9): "#C5BC9FFF",
        (7, 9): "#C5BC9FFF",
        (8, 9): "#988B63FF",
        (6, 10): "#C5BC9FFF",
        (7, 10): "#C5BC9FFF",
        (8, 10): "#988B63FF",
        (6, 11): "#988B63FF",
        (7, 11): "#988B63FF",
        (8, 11): "#988B63FF",
    },
}


def rgba_hex(pixel: tuple[int, int, int, int]) -> str:
    return "#" + "".join(f"{channel:02X}" for channel in pixel)


def crop_frame(image: Image.Image, frame_index: int, tier: int) -> Image.Image:
    left = frame_index * FRAME_WIDTH
    top = tier * FRAME_HEIGHT
    return image.crop(
        (left, top, left + FRAME_WIDTH, top + FRAME_HEIGHT)
    ).convert("RGBA")


def sha256_pixels(image: Image.Image) -> str:
    return hashlib.sha256(image.tobytes()).hexdigest()


def save_png(image: Image.Image, path: Path) -> None:
    image.save(path, format="PNG", optimize=True, compress_level=9)
    decoded = Image.open(path).convert("RGBA")
    if decoded.size != image.size or decoded.tobytes() != image.convert("RGBA").tobytes():
        raise RuntimeError(f"PNG round-trip changed pixels: {path}")


def nearest_preview(image: Image.Image, scale: int = 12) -> Image.Image:
    return image.resize(
        (image.width * scale, image.height * scale),
        resample=Image.Resampling.NEAREST,
    )


def exact_scroll_consensus(images: list[Image.Image], frame_index: int) -> Image.Image:
    samples = [
        crop_frame(image, frame_index, tier)
        for image in images
        for tier in ACTIVE_TIERS
    ]
    result = Image.new("RGBA", (FRAME_WIDTH, FRAME_HEIGHT), (0, 0, 0, 0))
    for y in range(FRAME_HEIGHT):
        for x in range(FRAME_WIDTH):
            pixels = [sample.getpixel((x, y)) for sample in samples]
            if pixels[0][3] == 255 and all(pixel == pixels[0] for pixel in pixels[1:]):
                result.putpixel((x, y), pixels[0])
    return result


def pixel_map(image: Image.Image) -> dict[tuple[int, int], str]:
    return {
        (x, y): rgba_hex(image.getpixel((x, y)))
        for y in range(image.height)
        for x in range(image.width)
        if image.getpixel((x, y))[3] != 0
    }


def serializable_pixels(image: Image.Image) -> list[dict[str, object]]:
    return [
        {"x": x, "y": y, "rgba": color}
        for (x, y), color in sorted(pixel_map(image).items(), key=lambda item: (item[0][1], item[0][0]))
    ]


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    source_images = [
        Image.open(SPRITE_DIR / name).convert("RGBA") for name in CONSENSUS_SHEETS
    ]
    warrior = source_images[0]

    scroll_frames: dict[int, Image.Image] = {}
    scroll_sheet = Image.new(
        "RGBA", (len(SCROLL_FRAMES) * FRAME_WIDTH, FRAME_HEIGHT), (0, 0, 0, 0)
    )
    for output_index, frame_index in enumerate(SCROLL_FRAMES):
        scroll = exact_scroll_consensus(source_images, frame_index)
        actual = pixel_map(scroll)
        expected = {
            coordinate: color for coordinate, color in EXPECTED_SCROLL_PIXELS[frame_index].items()
        }
        if actual != expected:
            raise RuntimeError(
                f"Scroll frame {frame_index} no longer matches the verified pixel mask"
            )
        scroll_frames[frame_index] = scroll
        scroll_sheet.alpha_composite(scroll, (output_index * FRAME_WIDTH, 0))

        tight_box = scroll.getbbox()
        if tight_box is None:
            raise RuntimeError(f"Scroll frame {frame_index} is unexpectedly empty")
        tight_name = "closed" if frame_index == 19 else "open"
        save_png(scroll.crop(tight_box), OUTPUT_DIR / f"parchment_scroll_{tight_name}.png")

    save_png(scroll_sheet, OUTPUT_DIR / "parchment_scroll_read_frames.png")
    save_png(
        nearest_preview(scroll_sheet),
        OUTPUT_DIR / "parchment_scroll_read_frames_preview_12x.png",
    )

    death_sheet = Image.new(
        "RGBA", (len(DEATH_FRAMES) * FRAME_WIDTH, FRAME_HEIGHT), (0, 0, 0, 0)
    )
    death_hashes: dict[int, str] = {}
    for output_index, frame_index in enumerate(DEATH_FRAMES):
        skull_frame = crop_frame(warrior, frame_index, tier=0)
        for tier in ACTIVE_TIERS:
            if crop_frame(warrior, frame_index, tier).tobytes() != skull_frame.tobytes():
                raise RuntimeError(
                    f"Warrior death frame {frame_index} differs at armor tier {tier}"
                )
        death_sheet.alpha_composite(skull_frame, (output_index * FRAME_WIDTH, 0))
        death_hashes[frame_index] = sha256_pixels(skull_frame)

    canonical_skull_frame = crop_frame(warrior, frame_index=8, tier=0)
    skull_box = canonical_skull_frame.getbbox()
    if skull_box != (3, 1, 11, 8):
        raise RuntimeError(f"Unexpected canonical skull bounds: {skull_box}")
    canonical_skull = canonical_skull_frame.crop(skull_box)

    save_png(canonical_skull, OUTPUT_DIR / "human_death_skull.png")
    save_png(death_sheet, OUTPUT_DIR / "human_death_skull_frames.png")
    save_png(
        nearest_preview(death_sheet),
        OUTPUT_DIR / "human_death_skull_frames_preview_12x.png",
    )

    manifest = {
        "format": "RGBA PNG, native square pixels, no resampling",
        "frame_size": {"width": FRAME_WIDTH, "height": FRAME_HEIGHT},
        "scroll": {
            "consensus_sources": list(CONSENSUS_SHEETS),
            "tiers_sampled": list(ACTIVE_TIERS),
            "sheet_file": "parchment_scroll_read_frames.png",
            "sheet_frame_order": list(SCROLL_FRAMES),
            "palette": ["#988B63FF", "#C5BC9FFF"],
            "frames": {
                str(frame_index): {
                    "bbox_in_12x15_frame": list(scroll_frames[frame_index].getbbox()),
                    "pixels_in_12x15_frame": serializable_pixels(scroll_frames[frame_index]),
                }
                for frame_index in SCROLL_FRAMES
            },
        },
        "human_death_skull": {
            "source": "warrior.png",
            "source_tier": 0,
            "sheet_file": "human_death_skull_frames.png",
            "sheet_frame_order": list(DEATH_FRAMES),
            "runtime_sequence": [8, 9, 10, 11, 12, 11],
            "canonical_tight_file": "human_death_skull.png",
            "canonical_bbox_in_frame_8": list(skull_box),
            "palette": sorted(set(pixel_map(canonical_skull).values())),
            "canonical_pixels": serializable_pixels(canonical_skull),
            "source_frame_pixel_hashes": {
                str(frame_index): digest for frame_index, digest in death_hashes.items()
            },
        },
    }
    (OUTPUT_DIR / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )

    print(f"Exported exact reference pixels to {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
