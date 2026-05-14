# 周期表 Pro — 应用图标设计

## 设计理念

以**元素周期表**为核心视觉语言：
- **大写 P** 作为元素符号，居中放置，参考 App 内 ElementCard 的元素符号风格
- **右下角数字 15** 作为原子序号 — 15 = 磷 = P，完美对应品牌首字母
- 整体风格简洁现代，Material 3 设计语言

## 文件清单

| 文件 | 用途 |
|------|------|
| `ic_launcher_foreground.xml` | 浅色模式前景层（P + 15，Primary `#6650A4`） |
| `ic_launcher_background.xml` | 浅色模式背景层（Primary Container `#EADDFF`） |
| `ic_launcher_monochrome.xml` | Themed Icon 单色版（Android 13+） |

## 深色模式

深色模式资源位于 `app/src/main/res/drawable-night/`：
- 前景：`#D0BCFF`
- 背景：`#4F378B`

## 技术规格

- 画布：108dp × 108dp（Android Adaptive Icon 标准）
- 安全区域：66dp 直径圆（中心 54,54，半径 33）
- P 字母使用 evenOdd fillType 实现右上碗部挖空
- 所有路径为纯矢量，无位图依赖
