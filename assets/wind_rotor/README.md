# 风力发电机替换转子

本版重绘参考 [Blockbench Minecraft Style Guide](https://blockbench.net/wiki/guides/minecraft-style-guide/) 的物品贴图、轮廓、左上光源和像素簇原则。

最终游戏贴图直接使用原生 **16×16 RGBA**，背景透明。所有像素的 R、G、B 相等，透明度仅为 0 或 255。使用 6 级灰度，供 GTM 材料颜色乘色使用。灰度约束按用户要求保留，未向此底图引入带色相的金属阴影。

设计：风力发电机替换转子 → 三片带旋向的金属叶片 → 中央轮毂和安装轴孔 → 明确的深色外轮廓、连续亮面和背面暗边。先构建中间色剪影，再加入左上光源；减少细碎灰阶，让叶片在 16×16 下仍有完整块面。参考实际查看的 Create 螺旋桨、GTM shiny/dull 转子和项目原贴图，不复制参考图标。

注册使用 `windRotor` 材料图标类型，GTM 材质模型引用 `gtceu:item/material_sets/dull/wind_rotor`。因此以下两个资源由同一像素源生成：

- `src/main/resources/assets/superflatcore/textures/item/wind_rotor.png`
- `src/main/resources/assets/gtceu/textures/item/material_sets/dull/wind_rotor.png`

## 编辑与生成

`pixels.json` 保存可编辑调色板和 16 行像素矩阵。`render.py` 检查调色板是否为纯灰度，生成贴图和最近邻预览，依赖 Python 与 Pillow。在项目根目录运行：

```powershell
python assets/wind_rotor/render.py
```

仅生成原生稿与预览，不写游戏资源：

```powershell
python assets/wind_rotor/render.py --preview-only
```

## 辅助文件

- `wind_rotor.native.png`：最终原生 16×16 稿，与游戏资源像素一致。
- `wind_rotor.before.png`：原 superflatcore 贴图备份，128×128。
- `wind_rotor.gtceu.before.png`：原 GTM 材质贴图备份，128×128。
- `wind_rotor.v1.png`、`pixels.v1.json`：首次三叶重绘的 16×16 转子与像素源备份。
- `wind_rotor.v2.png`、`pixels.v2.json`：作为当前造型基础的曲面细化稿与像素源备份。
- `wind_rotor.v3.png`、`pixels.v3.json`：本次调整前的贴图与像素源备份。
- `wind_rotor.before-style-guide.png`、`wind_rotor.gtceu.before-style-guide.png` 与 `pixels.before-style-guide.json`：本次重绘前的两张游戏资源和可编辑像素源。
- `preview.png`：本次重绘前后对比，包含原尺寸和最近邻放大。棋盘格只用于预览，不在游戏 PNG 内。
- `tint-preview.png`：灰度底图及铜色、蓝灰色的逐像素乘色示意。彩色图片仅为预览，游戏资源始终保持灰度。
- `reference-board.png`、`current-textures.png`：实际查看的参考图板，旁边的 `.sources.json` 记录完整来源。

验收：实际查看原尺寸、最近邻放大、乘色示意；验证两张游戏 PNG 均为 16×16 RGBA、R=G=B、二值透明度、同一像素源输出，以及现有模型引用。脚本检查不代表美术规则的自动认证。本次未运行 Minecraft 客户端，也未构建或复制模组包。
