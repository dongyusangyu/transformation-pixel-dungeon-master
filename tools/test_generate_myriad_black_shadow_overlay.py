"""Compatibility entry point for the Myriad Black Shadow overlay contract tests."""

from pathlib import Path
import runpy


if __name__ == "__main__":
    runpy.run_path(
        str(Path(__file__).with_name("test_myriad_black_shadow_overlay.py")),
        run_name="__main__",
    )
