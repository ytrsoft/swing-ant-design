#!/usr/bin/env python3
"""
从一张源 PNG 图片生成所有 app-icon 和 tray-icon 尺寸。

用法:
  python generate_icons.py <source.png>

输出目录:
  src/main/resources/com/antdesign/swing/icons/app/
"""

import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("需要 Pillow: pip install Pillow")
    sys.exit(1)

# AppIconProvider 中定义的尺寸
APP_ICON_SIZES = [16, 20, 24, 32, 48, 64, 128, 256, 512]
TRAY_ICON_SIZE = 64

OUTPUT_DIR = Path(__file__).parent / "src/main/resources/com/antdesign/swing/icons/app"


def generate(source_path: str):
    src = Image.open(source_path).convert("RGBA")
    print(f"源图片: {source_path} ({src.width}x{src.height})")

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    for size in APP_ICON_SIZES:
        out = OUTPUT_DIR / f"app-icon-{size}.png"
        resized = src.resize((size, size), Image.LANCZOS)
        resized.save(out, "PNG")
        print(f"  -> {out}")

    # tray-icon
    tray_out = OUTPUT_DIR / "tray-icon.png"
    tray = src.resize((TRAY_ICON_SIZE, TRAY_ICON_SIZE), Image.LANCZOS)
    tray.save(tray_out, "PNG")
    print(f"  -> {tray_out}")

    print(f"\n共生成 {len(APP_ICON_SIZES) + 1} 个图标文件")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"用法: python {Path(__file__).name} <source.png>")
        sys.exit(1)
    generate(sys.argv[1])
