# 设计系统实现规范（Spectra Design Language）

> 依据：仓库根 `DESIGN.md`（v1.0）— 设计唯一事实源。
> 本文件是**实现层强制规则**：写任何 UI 代码前必读，与 DESIGN.md 冲突时以 DESIGN.md 为准。

---

## 1. Token 使用铁律

### 1.1 颜色

| 规则 | 说明 |
|---|---|
| ✅ 只用 `MaterialTheme.colorScheme.*` | 界面 chrome 颜色禁止手写 hex |
| ✅ 分类色只从 `LocalCategoryColors.current.forCategory(cat)` 取 | 分类色板独立注入，不进 ColorScheme |
| ✅ 卡片描边一律 `colorScheme.outlineVariant`，宽 1dp | 不用阴影分层 |
| ✅ tint 底用 `.copy(alpha = …)` 且 alpha 用 DESIGN.md §2.4 规定值 | 0.12 圆底 / 0.14 chip / 0.16~0.22 单元格 / 0.18~0.24 Hero 渐变 |
| ❌ 禁止装饰性彩色 | 彩色只表达分类/语义（正确/错误/等级） |
| ❌ 禁止 `Color.White`/`Color.Black` 硬编码文字色 | 走 `onSurface` / `onError` 等 |

### 1.2 排印

| 规则 | 说明 |
|---|---|
| ✅ 数值/统计/序号用 `PeriodicTextStyles.numeric*`（tnum 等宽） | 或 `labelSmall`（已带 tnum） |
| ✅ 方程式/电子排布用 `PeriodicTextStyles.equation`（Monospace） | 不用默认字体 |
| ✅ 中文标题最重 SemiBold（600） | Bold 仅限元素符号（拉丁字母） |
| ❌ 禁止在 Composable 里散写 `fontSize = X.sp` | 走 Typography 槽位或 PeriodicTextStyles |

### 1.3 形状 / 间距 / 高度

- 圆角走 `MaterialTheme.shapes.*`（6/8/12/16/20），不手写 `RoundedCornerShape(N.dp)`
  （周期表单元格等特例集中在定义处一次）。
- 间距走 `Dimensions.*`（8pt 网格）；页边距 16dp；卡片内边距 16/12；列表 gap 12dp；Section 间 24dp。
- 卡片无阴影：`tonalElevation = 0`，`border = 1dp outlineVariant`；
  只有 Dialog/Sheet/FAB 用 `Elevation.*`。

### 1.4 分类色板（Spectral Ten）

`CategoryColors`（浅）与 `DarkCategoryColors`（深）的值以 `theme/Color.kt` 为准，
十个色相环均分（红→橙→琥珀→绿→青→天蓝→靛→紫→品红→玫红）。
**修改任何一色必须同时核对**：浅色文字对比 ≥3:1、深色模式版本、与相邻色相可辨。

---

## 2. 组件使用规则

| 需求 | 用 | 禁 |
|---|---|---|
| 列表元素卡 | `ElementTile` | 自写卡片、旧"顶部色条"样式 |
| 小节标题 | `SectionHeader` | 裸 Text + 手写间距 |
| 图标强调位 | `IconBadge` | 彩色圆形背景手写 |
| 搜索 | `PeriodicSearchBar`（填充式） | OutlinedTextField 裸用 |
| 筛选 | `CategoryFilterChip`（圆点+tonal 选中） | AssistChip 默认样式 |
| 数据行 | `PropertyRow`（label→value tnum→unit） | 裸 Row+Text |
| 可点击卡片按压 | `Modifier.pressScale()` | 手写 scale 动画 |
| 空状态 | `EmptyState` | 裸居中文本 |

## 3. 状态可见性

- 多选模式：TopBar 切 `primaryContainer` 上下文栏（计数 tnum）。
- 筛选淘汰/未命中元素：`alpha 0.12`，不用 0.2（旧值对比不足）。
- 选中单元格：2dp 分类色描边 + tint 加深到 0.35。
- 左滑删除：`errorContainer` 底 + `onErrorContainer` 图标。

## 4. 深色模式

- 表面体系由 ColorScheme 提供，禁止用 `isSystemInDarkTheme()` 在业务组件里
  切换样式（该判断只允许出现在 theme 层与 AtomCanvas 等绘制层）。
- 分类色深色版由 `PeriodicProTheme` 统一切换，组件不感知。

## 5. 验收门（UI PR 必过）

- [ ] 浅色 + 深色双主题下截图无对比度问题（正文 ≥4.5:1）
- [ ] 所有数值列对齐（tnum 生效）
- [ ] 卡片为描边分层、无阴影（Dialog/Sheet 除外）
- [ ] 分类色仅在语义位出现
- [ ] `./gradlew lint test` 通过；每个改动屏在浅/深模式各跑一次预览/截图
