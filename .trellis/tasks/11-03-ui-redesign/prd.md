# UI 全面重设计 — 专业科学工具风格

## 背景

当前应用 UI 基于默认 Material 3 紫色模板，视觉上"合格但缺乏记忆点"：
表面系统带紫色偏色、依赖阴影分层、元素分类色板中粉紫系四色难以区分、
数据展示未使用等宽数字、卡片样式陈旧（顶部色条）、首页下半屏空白失衡。

## 目标

参考专业科学工具类应用（Wolfram|Alpha、RSC 周期表、NIST 数据工具、
现代专业工具应用的表面/排印系统），完整迭代一次 UI，让应用更**专业、可靠、美观**。

**不新增任何功能** — 纯表现层迭代，导航结构、数据层、业务逻辑不动。

## 交付物

1. `DESIGN.md`（仓库根）— 完整设计语言文档：设计原则、色彩、排印、形状、
   间距、高度、动效、组件规范、逐屏规范。
2. `.trellis/spec/frontend/design-system.md` — UI 实现规范（供后续开发注入的
   强制规则），并在 `spec/frontend/index.md` 挂载。
3. 主题层重构：`theme/` 全部文件（Color/Typography/Shapes/Dimensions/
   Elevation/PeriodicProTheme + 新增 Motion）。
4. 组件层升级：`ui/components/`（ElementCard、SearchBar、Chip、Buttons、
   新增 SectionHeader/StatTile 等）+ `ui/pattern/`（PropertyGrid、EmptyState）。
5. 逐屏改造：Home / Table(+Grid) / Detail / Discover / Favorites / Profile /
   Learn / Lab / Quiz / Compare / Category / UpdateDialog / 导航 Suite。
6. 验证：`./gradlew assembleDebug` 通过、全部单元测试通过、模拟器截图
   浅色+深色逐屏视觉验收。

## 非目标

- 不新增功能/页面/数据字段
- 不改导航信息架构（Tab 结构、路由）
- 不改 ViewModel/Repository/DataStore 逻辑
