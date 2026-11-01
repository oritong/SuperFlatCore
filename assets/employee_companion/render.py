"""Render the editable 16x16 phone texture and a nearest-neighbour preview."""
import argparse
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).resolve().parent
TEXTURE = HERE.parents[1] / "src/main/resources/assets/superflatcore/textures/item/employee_companion.png"


def render(source):
    size = source["size"]
    rows = source["sprites"]["employee_companion"]["pixels"]
    assert size == 16 and len(rows) == size
    assert all(len(row) == size for row in rows)
    palette = {key: tuple(bytes.fromhex(value.lstrip("#"))) + (255,)
               for key, value in source["palette"].items()}
    image = Image.new("RGBA", (size, size))
    for y, row in enumerate(rows):
        for x, token in enumerate(row):
            if token != ".":
                image.putpixel((x, y), palette[token])
    return image


def preview(image):
    board = Image.new("RGB", (672, 400), "#eeeeec")
    draw = ImageDraw.Draw(board)
    font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 18)
    small = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 14)
    before = HERE / "employee_companion.style-guide.png"
    with Image.open(before) as original:
        pictures = [original.convert("RGBA"), image]
    for index, picture in enumerate(pictures):
        x = 16 + index * 336
        draw.text((x + 12, 14), ["新版边框与头像", "新版边框 + 原屏幕"][index], font=font, fill="#33383b")
        for gy in range(16):
            for gx in range(16):
                color = "#c8c8c5" if (gx // 2 + gy // 2) % 2 else "#d1d1ce"
                left, top = x + 16 + gx * 16, 50 + gy * 16
                draw.rectangle((left, top, left + 15, top + 15), fill=color)
        enlarged = picture.resize((256, 256), Image.Resampling.NEAREST)
        board.paste(enlarged, (x + 16, 50), enlarged)
        board.paste(picture, (x + 16, 338), picture)
        draw.text((x + 48, 336), "16×16 原尺寸 / 最近邻放大", font=small, fill="#555b60")
    board.save(HERE / "preview.png")
    image.resize((320, 320), Image.Resampling.NEAREST).save(HERE / "employee_companion.enlarged.png")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=TEXTURE)
    args = parser.parse_args()
    source = json.loads((HERE / "pixels.json").read_text(encoding="utf-8"))
    image = render(source)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    image.save(args.output)
    preview(image)
    print(f"Saved {args.output.resolve()} (16x16 RGBA)")
