from pathlib import Path
from PIL import Image
import tempfile

ROOT = Path(__file__).resolve().parents[2]
source = Path(r"D:\桌面\绅士精灵设计资料\醉意 昂扬 守护.png")
wine = Path(r"D:\桌面\绅士精灵设计资料\精灵酒.png")
fruit = Path(r"D:\桌面\绅士精灵设计资料\绿光果实.png")
aroma = Path(r"D:\桌面\绅士精灵设计资料\酸味的酒香.png")
assert Image.open(source).size == (48, 23)
for p in (wine, fruit, aroma): assert Image.open(p).size == (16, 16)

def paste_checked(path, targets, crops, cols, size):
    original = Image.open(path).convert("RGBA")
    image = original.copy()
    for index, crop in zip(targets, crops):
        x, y = (index % cols) * size, (index // cols) * size
        if image.crop((x, y, x + size, y + size)).getbbox() is not None:
            raise SystemExit(f"refusing to overwrite non-empty cell {index} in {path}")
        image.alpha_composite(crop, (x, y))
    with tempfile.NamedTemporaryFile(delete=False, suffix=".png", dir=path.parent) as f:
        temp = Path(f.name)
    image.save(temp)
    temp.replace(path)

buff = Image.open(source).convert("RGBA")
paste_checked(ROOT / "core/src/main/assets/interfaces/buffs.png",
              [149, 150], [buff.crop((0, 0, 7, 7)), buff.crop((7, 0, 14, 7))], 18, 7)
paste_checked(ROOT / "core/src/main/assets/interfaces/large_buffs.png",
              [149, 150], [buff.crop((0, 7, 16, 23)), buff.crop((16, 7, 32, 23))], 16, 16)
paste_checked(ROOT / "core/src/main/assets/sprites/ex_items.png", [20, 21, 22],
              [Image.open(wine).convert("RGBA"), Image.open(fruit).convert("RGBA"), Image.open(aroma).convert("RGBA")], 16, 16)
print("inserted gentleman elf buff and item assets")
