"""Render a native 16x16 grayscale wind rotor and tint previews."""
import argparse
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
TARGETS = [
    ROOT / "src/main/resources/assets/superflatcore/textures/item/wind_rotor.png",
    ROOT / "src/main/resources/assets/gtceu/textures/item/material_sets/dull/wind_rotor.png",
]


def render(source):
    size = source["size"]
    rows = source["sprites"]["wind_rotor"]["pixels"]
    if size != 16 or len(rows) != size or any(len(row) != size for row in rows):
        raise ValueError("Expected a native 16x16 pixel matrix")
    palette = {}
    for token, hex_color in source["palette"].items():
        color = tuple(bytes.fromhex(hex_color.lstrip("#")))
        if len(color) != 3 or color[0] != color[1] or color[1] != color[2]:
            raise ValueError(f"{token}: tintable texture must be strictly grayscale")
        palette[token] = color + (255,)
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, token in enumerate(row):
            if token != ".":
                image.putpixel((x, y), palette[token])
    return image


def tint(image, color):
    result = image.copy()
    for y in range(image.height):
        for x in range(image.width):
            gray, _, _, alpha = image.getpixel((x, y))
            result.putpixel((x, y), tuple(gray * channel // 255 for channel in color) + (alpha,))
    return result


def board(pictures, labels, output):
    width = 336
    preview = Image.new("RGB", (width * len(pictures), 390), "#eeeeec")
    draw = ImageDraw.Draw(preview)
    font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 18)
    small = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 13)
    for index, (picture, label) in enumerate(zip(pictures, labels)):
        left = index * width + 40
        draw.text((left - 16, 14), label, font=font, fill="#33383b")
        for gy in range(16):
            for gx in range(16):
                color = "#c8c8c5" if (gx // 2 + gy // 2) % 2 else "#d1d1ce"
                x, y = left + gx * 16, 52 + gy * 16
                draw.rectangle((x, y, x + 15, y + 15), fill=color)
        enlarged = picture.resize((256, 256), Image.Resampling.NEAREST)
        preview.paste(enlarged, (left, 52), enlarged)
        preview.paste(picture, (left, 332), picture)
        draw.text((left + 28, 330), "16×16 原尺寸 / 最近邻放大", font=small, fill="#555b60")
        draw.text((left, 356), "纯灰度 / 透明背景", font=small, fill="#666b70")
    preview.save(output)


def preview(image):
    with Image.open(HERE / "wind_rotor.before-style-guide.png") as original:
        before = original.convert("RGBA").resize((16, 16), Image.Resampling.NEAREST)
    board([before, image], ["重绘前", "Minecraft 风格重绘"], HERE / "preview.png")
    board([image, tint(image, (219, 159, 99)), tint(image, (151, 192, 220))],
          ["纯灰度底图", "乘色示意 · 铜色", "乘色示意 · 蓝灰色"], HERE / "tint-preview.png")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--preview-only", action="store_true")
    args = parser.parse_args()
    source = json.loads((HERE / "pixels.json").read_text(encoding="utf-8"))
    image = render(source)
    image.save(HERE / "wind_rotor.native.png")
    preview(image)
    if not args.preview_only:
        for path in TARGETS:
            image.save(path)
            print(f"Saved {path} (16x16 RGBA, strictly grayscale)")
