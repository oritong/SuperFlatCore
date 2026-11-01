# 员工陪玩手机贴图

本版重绘参考 [Blockbench Minecraft Style Guide](https://blockbench.net/wiki/guides/minecraft-style-guide/) 的物品贴图、色阶、光照和像素簇原则。游戏资源为原生 16×16 RGBA，背景透明。

`employee_companion.png` 使用原生 16×16 网格、RGBA 透明背景和硬像素边缘绘制。

当前版本按用户选择组合：保留 Minecraft 风格重绘版的外壳、顶部听筒与镜头、底部导航条，屏幕恢复此前的青色全息员工头像和绿色、琥珀色应用图标。屏幕区域为 x=5..10、y=3..11，逐像素保留 `employee_companion.before-style-guide.png` 的内容和颜色；其余区域逐像素保留 `employee_companion.style-guide.png` 的新版外框。

绘图直接使用 16 行字符矩阵。先定轮廓、外壳和屏幕，再安排左上亮面与右下暗面。保留有限的材质色阶和连贯像素簇，不使用模糊、半透明边缘、缩小高分辨率图、随机噪点或抖色。屏幕左上方的青色块表示玻璃反光，不沿整个屏幕边缘描亮。

参考图板 `reference-board.png` 实际查看了用户提供的本地素材：GTM 终端与显示器的灰色金属色阶、AE2 无线终端的深色框架与屏幕明暗、Mekanism 能量板的亮屏与机身分区，以及当前项目的原贴图和进气盖。新图在 16×16 网格上原创绘制。图板的完整来源见 `reference-board.png.sources.json`。

## 文件

- `pixels.json`：可编辑的调色板与 16 行字符像素源。
- `render.py`：只生成这一件物品的贴图和预览，依赖 Python 与 Pillow。
- `employee_companion.before.png`：修改前的原图备份。
- `employee_companion.v1.png` 与 `pixels.v1.json`：首次手机重绘的贴图与像素源备份。
- `employee_companion.before-style-guide.png` 与 `pixels.before-style-guide.json`：本次重绘前的游戏贴图与可编辑源备份。
- `employee_companion.style-guide.png` 与 `pixels.style-guide.json`：新版边框与方块员工头像的完整备份。
- `preview.png`：新版手机与组合版本对比，同时包含原尺寸与 16 倍最近邻放大。棋盘格是预览背景，不在游戏 PNG 内。
- `employee_companion.enlarged.png`：透明背景的 20 倍最近邻放大。

在项目根目录执行：

```powershell
python assets/employee_companion/render.py
```

游戏资源写入 `src/main/resources/assets/superflatcore/textures/item/employee_companion.png`。模型现有 `layer0` 引用即指向这张图，无需修改。

验收范围：检查 PNG 为 16×16 RGBA、透明度仅为 0/255、像素源与输出一致、现有模型引用正确；实际查看原尺寸与最近邻放大图，检查手机轮廓、头像、光照和材质分区。脚本检查不代表美术规则的自动认证。本次未运行 Minecraft 客户端，也未构建或复制模组包。
