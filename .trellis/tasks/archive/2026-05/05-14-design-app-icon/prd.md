# 设计元素周期表主题应用图标

## Goal

为"周期表 Pro"应用设计并生成一套以元素周期表为主题的 Android 应用图标，放到项目 `icon/` 目录，并替换现有 app 的 launcher icon。

## Requirements

- **图标概念**：字母"P"元素化风格
  - 大写 **P** 作为核心视觉（类似元素符号），居中放置
  - 右下角小数字 **15**（磷的原子序号，与 P 完美对应）
  - 整体风格现代、简洁、Material 3 风格
- **色彩规范**（符合 Design System）：
  - 浅色模式前景：Primary `#6650A4`
  - 浅色模式背景：Primary Container `#EADDFF`
  - 深色模式前景：`#D0BCFF`
  - 深色模式背景：`#4F378B`
- **输出格式**：矢量 XML（Android Adaptive Icon 规范）
  - `ic_launcher_foreground.xml` — 前景层（P + 15）
  - `ic_launcher_background.xml` — 背景层
  - `ic_launcher_monochrome.xml` — Themed Icon（Android 13+）
- **尺寸**：Adaptive Icon 标准 108dp × 108dp，安全区域 66dp × 66dp
- **输出目录**：
  - `icon/` — 作为设计源文件备份
  - `app/src/main/res/drawable/` — 实际应用替换现有图标
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` — 已存在，确认引用正确
- **Themed Icon**：monochrome 版，仅使用单一颜色，跟随系统主题色

## Acceptance Criteria

- [ ] `ic_launcher_foreground.xml` — P 字母 + 右下角 15，居中在安全区域内
- [ ] `ic_launcher_background.xml` — 纯色背景，浅色 `#EADDFF` / 深色 `#4F378B`
- [ ] `ic_launcher_monochrome.xml` — 单色版本，仅形状无填充色
- [ ] `icon/` 目录包含所有生成的矢量图标源文件
- [ ] 现有 app launcher icon 被替换
- [ ] 应用编译通过，lint 无报错
- [ ] 浅色/深色模式下图标均美观清晰

## Definition of Done

- 所有图标资源生成并放置到位
- 替换现有 app launcher icon
- 编译构建通过
- 图标在浅色/深色模式下均清晰可辨

## Out of Scope

- Play Store feature graphic / 宣传图
- 动态图标 / 动画效果
- iOS 图标格式
- PNG 位图输出（仅矢量 XML）

## Technical Notes

- 当前图标文件：`app/src/main/res/drawable/ic_launcher_foreground.xml`、`ic_launcher_background.xml`
- Adaptive icon 定义：`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Design System 主色：Primary `#6650A4`，Primary Container `#EADDFF`
- 深色模式：Primary `#D0BCFF`，Surface `#1C1B1F`
- Adaptive Icon 安全区域：66dp 直径圆内（中心点 54,54，半径 33）
- 标准 mipmap 目录仍需保留对应的 PNG fallback（但本次不修改，保持原样或后续可移除）
