# 周期表 Pro — 高保真设计稿深度解析与 UI 开发规范

> 基于 4 张高保真设计图（Design System / Components / Patterns / Screens）逐像素提取的开发规范。
> 用途：保证任何后续开发都能 100% 还原设计稿，保持 UI 一致性。

---

## 目录

1. [Design System 深度规范](#1-design-system-深度规范)
2. [Components 组件规范](#2-components-组件规范)
3. [Patterns 页面模式规范](#3-patterns-页面模式规范)
4. [Screens 屏面开发清单](#4-screens-屏面开发清单)
5. [UI 一致性强制规则](#5-ui-一致性强制规则)
6. [实现状态矩阵](#6-实现状态矩阵)

---

## 1. Design System 深度规范

### 1.1 色板 Color System

#### 1.1.1 Material 3 主色系

| Token | 浅色值 | 深色值 | 用途 |
|-------|--------|--------|------|
| Primary | `#6650A4` | `#D0BCFF` | 按钮填充、选中态、强调色 |
| Primary Container | `#EADDFF` | `#4F378B` | 选中背景、Chip 填充 |
| Secondary | `#625B71` | `#CCC2DC` | 次级按钮、辅助文字 |
| Secondary Container | `#E8DEF8` | `#4A4458` | 次级选中背景 |
| Surface | `#FFFBFE` | `#1C1B1F` | 页面背景 |
| Surface Variant | `#E7E0EC` | `#49454F` | 卡片背景、输入框背景 |
| On Surface | `#1C1B1F` | `#E6E1E5` | 主文字 |
| On Surface Variant | `#49454F` | `#CAC4D0` | 次要文字、标签 |
| Outline | `#79747E` | `#938F99` | 描边、分割线 |
| Error | `#B3261E` | `#F2B8B5` | 错误态 |

> **与代码现状对比**：当前 `Color.kt` 已定义这些值，但 **缺少 Primary Container / Secondary Container / On Surface Variant / Outline 等 M3 标准 Token**。应补齐完整 M3 ColorScheme。

#### 1.1.2 元素分类色板（Category Colors）

**浅色模式**（与当前代码一致 ✅）：

| 分类 | 色值 | Token |
|------|------|-------|
| 碱金属 | `#FF6B6B` | `categoryAlkali` |
| 碱土金属 | `#FFA94D` | `categoryAlkalineEarth` |
| 过渡金属 | `#FFD43B` | `categoryTransition` |
| 后过渡金属 | `#69DB7C` | `categoryPostTransition` |
| 类金属 | `#4DABF7` | `categoryMetalloid` |
| 非金属 | `#748FFC` | `categoryNonmetal` |
| 卤素 | `#DA77F2` | `categoryHalogen` |
| 稀有气体 | `#9775FA` | `categoryNobleGas` |
| 镧系 | `#F783AC` | `categoryLanthanide` |
| 锕系 | `#E599F7` | `categoryActinide` |

**深色模式**（当前代码已定义，但需确认是否精确匹配设计稿降饱和要求）：

设计稿深色模式下 Category 色应在原色基础上降低饱和度约 20-30%。当前 `DarkCategoryColors` 的实现基本正确，但需逐色与设计稿对比确认。

#### 1.1.3 毛玻璃色（Glass Blur）

| 模式 | 叠加色 | 背景模糊半径 |
|------|--------|-------------|
| 浅色 | `rgba(255,255,255,0.15)` | 24dp |
| 深色 | `rgba(0,0,0,0.25)` | 24dp |

> **代码现状**：`GlassSurface.kt` 已实现，但 `tint` 默认值是 `surface.copy(alpha=0.3f)`，与设计稿 `0.15`/`0.25` 有偏差。**需修正**。

### 1.2 字阶 Typography System

设计稿中字阶比当前代码更丰富，当前代码缺少部分层级：

| Token | 字号 | 字重 | 行高 | 字间距 | 用途 |
|-------|------|------|------|--------|------|
| **Display Large** | 57sp | Regular (400) | 64sp | -0.25 | 元素原子序号大数字 |
| **Display Medium** | 45sp | Regular (400) | 52sp | 0 | — |
| **Display Small** | 36sp | Regular (400) | 44sp | 0 | — |
| **Headline Large** | 32sp | Regular (400) | 40sp | 0 | — |
| **Headline Medium** | 28sp | Regular (400) | 36sp | 0 | **屏面标题** |
| **Headline Small** | 24sp | Regular (400) | 32sp | 0 | — |
| **Title Large** | 22sp | Medium (500) | 28sp | 0 | **元素符号卡片** |
| **Title Medium** | 16sp | Medium (500) | 24sp | 0.15 | **Section Header** |
| **Title Small** | 14sp | Medium (500) | 20sp | 0.1 | 卡片标题 |
| **Body Large** | 16sp | Regular (400) | 24sp | 0.5 | **属性值** |
| **Body Medium** | 14sp | Regular (400) | 20sp | 0.25 | **属性标签、列表项** |
| **Body Small** | 12sp | Regular (400) | 16sp | 0.4 | 辅助文字 |
| **Label Large** | 14sp | Medium (500) | 20sp | 0.1 | 按钮文字 |
| **Label Medium** | 12sp | Medium (500) | 16sp | 0.5 | **Chips、标签** |
| **Label Small** | 11sp | Medium (500) | 16sp | 0.5 | **角标、微标注** |

> **与代码现状对比**：当前 `Typography.kt` 定义了 7 种字阶，**缺少 Display Medium/Small、Headline Large/Small、Title Small、Body Small、Label Large**。虽然部分未使用，但为了设计一致性，应补齐完整 M3 字阶。

> **字体**：系统默认 Roboto + Noto Sans CJK（中文）。**生僻字 Z≥113**（鉨/鏌/鉝/鿫）必须 Noto Sans CJK，否则显示豆腐块。

### 1.3 间距 Spacing（8pt Grid）

| Token | 值 | 用途 |
|-------|---|------|
| `space0` | 0.dp | — |
| `space1` | 4.dp | 极细线、分割线 |
| `space2` | 8.dp | 小间距、Chip 内边距 |
| `space3` | 12.dp | 卡片内紧凑间距 |
| `space4` | 16.dp | 标准内边距 |
| `space5` | 20.dp | 中等间距 |
| `space6` | 24.dp | 卡片间距、Section 间距 |
| `space7` | 32.dp | 大间距 |
| `space8` | 40.dp | Section 分隔 |
| `space9` | 48.dp | **最小点击区域** |

> **与代码现状对比**：`Dimensions.kt` 使用了 `Dp0`/`Dp4`/`Dp8`... 的命名，但设计稿对应的是 `space1`/`space2`/`space3`... 的语义命名。**建议保留现有命名或对齐为 spaceN 语义化命名**。

> **强制规则**：所有间距必须为 `8.dp` 的整数倍，`4.dp` 仅限极细线条/分割线。

### 1.4 圆角 Radius

| Token | 值 | 用途 |
|-------|---|------|
| `radiusSmall` | 4.dp | Chips、小标签 |
| `radiusMedium` | 8.dp | 小卡片、输入框 |
| `radiusLarge` | 12.dp | **标准卡片**（ElementCard、Property Card） |
| `radiusXLarge` | 16.dp | 大卡片、Modal、BottomSheet |
| `radiusXXLarge` | 20.dp | 圆角容器 |
| `radiusMax` | 24.dp | Dashboard 区块 |

> **与代码现状对比**：`Shapes.kt` 定义了 extraSmall(4)/small(8)/medium(12)/large(16)/extraLarge(24)，**缺少 20.dp 的 radiusXXLarge**。应补充。

### 1.5 阴影 Elevation（4 层体系）

| 层级 | 名称 | 阴影描述 | 值 |
|------|------|---------|----|
| L1 | Shadow 1 | 微弱投影 | 1.dp |
| L2 | Shadow 2 | 标准悬浮 | 3.dp |
| L3 | Floating | 浮层 | 6.dp |
| L4 | Modal | 模态弹窗 + Scrim | 8.dp |

> **与代码现状对比**：`Elevation.kt` 已定义 Shadow1/Shadow2/Floating/Modal，值一致 ✅。

### 1.6 图标系统 Iconography

- 基准尺寸：**24dp**
- 点击区最小：**48dp × 48dp**（`Dimensions.Dp48`）
- 图标库：`androidx.compose.material:material-icons-extended`
- 图标颜色：默认 `On Surface Variant`，选中态 `Primary`

---

## 2. Components 组件规范

### 2.1 Button 按钮

#### Primary Button（主按钮）
```
样式：紫色填充（Primary）
圆角：12.dp（radiusLarge / shapes.medium）
高度：48.dp（最小点击区）
内边距：水平 24.dp，垂直 12.dp
文字：Label Large（14sp Medium），白色（On Primary）
点击态：轻微缩放 + ripple
禁用态：背景透明度 38%
```

> **代码现状**：`PeriodicButton.kt` 已实现，但需确认高度是否为 48.dp。

#### Secondary Button（次按钮）
```
样式：紫色描边（Primary），透明背景
圆角：12.dp
高度：48.dp
文字：Label Large，紫色（Primary）
```

#### Text Button（文字按钮）
```
样式：无边框无背景
文字：Label Large，紫色（Primary）
```

#### FAB（悬浮按钮）
```
样式：圆形，Primary Container 色
尺寸：56.dp × 56.dp
位置：屏幕右下角，距边缘 16.dp
图标：24.dp，On Primary Container 色
```

### 2.2 Element Cards 元素卡片

#### 元素小卡片（ElementCard）
```
尺寸：88dp × 100dp（设计稿中显示为小正方形）
顶部：4.dp 分类色条（Category Color）
内容：原子序号（Label Small）+ 元素符号（Title Large）+ 中文名（Body Small，可选）
圆角：12.dp（radiusLarge）
阴影：L1（Shadow1，1.dp）
背景：Surface Variant
内边距：12.dp
```

> **与代码现状对比**：当前 `ElementCard.kt` 为 88×100dp，顶部 4dp 色条 ✅。但需确认：
> - 原子序数字号是否为 Label Small（11sp）
> - 元素符号字号是否为 Title Large（22sp）
> - 内边距是否为 12.dp

#### 元素大卡片（ElementDetailCard）
```
尺寸：更宽，约 160dp × 120dp
顶部：4.dp 分类色条
内容：原子序号 + 符号（更大）+ 原子量 + 分类 Chip
圆角：16.dp（radiusXLarge）
阴影：L2（Shadow2，3.dp）
```

#### 深色模式卡片
```
背景：Dark Surface Variant（`#49454F`）
色条：使用 DarkCategoryColors（降饱和）
文字：Dark On Surface
```

### 2.3 Chips & Tags

#### CategoryChip（分类筛选 Chip）
```
选中态：
  - 背景：Category Color 填充
  - 文字：白色
  - 圆角：4.dp（radiusSmall）

非选中态：
  - 背景：透明
  - 边框：1.dp Outline 色描边
  - 文字：On Surface Variant

内边距：垂直 4.dp，水平 12.dp
文字：Label Medium（12sp）
```

> **与代码现状对比**：`CategoryChip.kt` 已实现，但需确认非选中态是否为透明+Outline描边（当前可能是透明+灰色描边）。

#### PropertyChip（属性标签 Chip）
```
选中态：Category Color 填充 + 白色文字
非选中态：透明 + Category Color 描边 + Category Color 文字
圆角：4.dp
文字：Label Medium
```

> **代码现状**：`PropertyChip.kt` 刚实现，需对照上述规范逐项检查。

### 2.4 Inputs 输入框

#### Search Bar（搜索框）
```
样式：OutlinedTextField 变体
圆角：8.dp（radiusMedium）
前置图标：Search（24dp，On Surface Variant）
后置图标：Clear（有文字时显示）
占位文字：On Surface Variant，Body Medium
背景：Surface Variant
内边距：水平 16.dp，垂直 12.dp
IME Action：Search
```

### 2.5 Glass Surface（毛玻璃）

```
组件：GlassSurface.kt
依赖：dev.chrisbanes.haze:haze:1.6.0

浅色模式：
  - 模糊半径：24.dp
  - 叠加色：rgba(255,255,255,0.15)
  - 圆角：20.dp（默认）

深色模式：
  - 模糊半径：24.dp
  - 叠加色：rgba(0,0,0,0.25)
  - 圆角：20.dp（默认）

性能红线：
  - ❌ 禁止在 LazyColumn / 网格子项中使用
  - ❌ 同屏 ≤ 5 个 GlassSurface 实例
  - ✅ 通过 LocalHazeState 全局下发
```

> **代码现状**：`GlassSurface.kt` 已实现，但 `tint` 默认值需修正为 `0.15`/`0.25`。

---

## 3. Patterns 页面模式规范

### 3.1 Property Grid（属性网格）

```
布局：2 列网格
每项结构：
  - 属性名：Label Medium（12sp），On Surface Variant
  - 属性值：Body Large（16sp），On Surface
  - 单位：Label Small（11sp），On Surface Variant

行间距：8.dp
列间距：16.dp
分隔线：每行底部 0.5dp 分割线（Outline 色）
空值显示："—"（破折号）
```

> **代码现状**：`PropertyGrid.kt` 已实现，但需确认分隔线是否为 0.5dp Outline 色。

### 3.2 Feed（信息流）

```
布局：LazyRow 横向滑动
每项：ElementCard + 16.dp 间距
用途：Home 屏"推荐元素"区域
```

### 3.3 Dashboard（仪表盘）

```
布局：2×2 或自适应网格
卡片：圆角 24.dp（radiusMax），L2 阴影
内容：图标 + 标题 + 描述
点击态：轻微缩放 + ripple
```

> **代码现状**：HomeScreen 中内联实现，未提取为独立 `DashboardGrid` Pattern。

### 3.4 Details（详情页模式）

```
结构：
  1. 顶部 Hero 区（Canvas 2D 原子动画）
  2. 元素基本信息（原子序号、符号、中文名、英文名）
  3. 分类 Chip
  4. 收藏按钮
  5. Property Grid
  6. Extra Info Section（发现者、年份、族、周期）

滚动行为：TopAppBar 滚动时背景变为毛玻璃
```

### 3.5 Comparison（对比模式）

```
布局：固定左侧标签列 + 可横向滚动元素列
表头：4dp 分类色条 + 符号 + 名称
属性行：11 个属性，每行一个 HorizontalDivider
空状态：EmptyState 模式
```

> **代码现状**：内联在 `CompareScreen.kt`，未提取为独立 `ComparisonTable` Pattern。

### 3.6 3D Viewer（3D 查看器）

```
MVP 占位：Canvas 2D 原子动画
真实 3D：后续迭代

Canvas 2D 内容：
  - 原子核：质子/中子小球，径向渐变
  - 电子层：虚线轨道环，最多 7 层
  - 电子小球：外层轨道匀速旋转，3 秒周期
```

> **代码现状**：`AtomCanvas.kt` 已实现 Canvas 2D 占位 ✅。

### 3.7 Empty & Error States

```
布局：居中 Column
内容：
  - 图标：64.dp，半透明（On Surface Variant 38%）
  - 标题：Title Medium（16sp）
  - 副标题：Body Medium（14sp），可选
  - 按钮：PeriodicButton，可选
```

> **代码现状**：`EmptyState.kt` 已实现 ✅。

---

## 4. Screens 屏面开发清单

### 4.1 Home 屏

| 区域 | 组件 | 状态 |
|------|------|:--:|
| 标题区 | "周期表 Pro" + 副标题 | ✅ |
| 搜索框 | PeriodicSearchBar | ✅ |
| 推荐元素 | LazyRow + ElementCard | ✅ |
| Dashboard | 2×2 卡片网格 | ✅ |
| Dashboard 卡片 | "周期表"/"对比"/"收藏"/"更多" | ✅（"更多"已禁用） |

**缺失**：
- Dashboard 卡片应提取为独立 `DashboardGrid` Pattern

### 4.2 Periodic Table 屏

| 区域 | 组件 | 状态 |
|------|------|:--:|
| 搜索框 | PeriodicSearchBar | ✅ |
| 分类筛选 | LazyRow + CategoryChip（11 项） | ✅ |
| 周期表网格 | 自定义 Layout，18×9 | ✅ |
| 缩放/平移 | Telephoto zoomable（最大 3x） | ✅ |
| 长按收藏 | 多选模式 | ✅ |
| 多选→Compare | 两阶段对比流程 | ✅ |

### 4.3 Element Detail 屏

| 区域 | 组件 | 状态 |
|------|------|:--:|
| TopAppBar | 返回 + 收藏 | ✅ |
| Canvas 2D | AtomCanvas 原子动画 | ✅ |
| 元素信息 | 原子序号 + 符号 + 名称 | ✅ |
| 分类 Chip | PropertyChip | ⚠️ 使用 `CategoryChip` 替代，应改用 `PropertyChip` |
| Property Grid | 8 个属性 | ✅ |
| Extra Info | 发现者/年份/族/周期 | ✅ |

**待修正**：
- Detail 页的分类标签应使用 `PropertyChip`（当前可能使用 `CategoryChip`）

### 4.4 Category 屏

| 区域 | 组件 | 状态 |
|------|------|:--:|
| 分类入口 Grid | 2 列 Grid，10 分类 | ✅ |
| 分类卡片 | 色条 + 图标 + 名称 + 计数 | ✅ |
| 分类详情 | TopAppBar + LazyColumn | ✅ |
| 元素列表 | ElementCard | ✅ |

### 4.5 Compare 屏

| 区域 | 组件 | 状态 |
|------|------|:--:|
| 对比表格 | 固定标签列 + 滚动元素列 | ✅ |
| 表头 | 色条 + 符号 + 名称 | ✅ |
| 属性行 | 11 属性 + Divider | ✅ |
| 空状态 | EmptyState | ✅ |

**待提取**：`ComparisonTable` 应提取为独立 Pattern

### 4.6 Discover 屏

| 区域 | 组件 | 状态 |
|------|------|:--:|
| TopAppBar | "发现" | ✅ |
| 每日推荐 | Hero Card（大尺寸） | ✅ |
| Feed 流 | LazyColumn + DiscoverCard | ✅ |
| 跳转详情 | 点击卡片 → Detail | ✅ |

### 4.7 Favorites 屏

| 区域 | 组件 | 状态 |
|------|------|:--:|
| 收藏列表 | LazyColumn + SwipeToDismissBox | ✅ |
| 空状态 | EmptyState | ✅ |
| 实时更新 | favoritesFlow | ✅ |

### 4.8 Profile/Settings 屏

| 区域 | 组件 | 状态 |
|------|------|:--:|
| 主题切换 | System/Light/Dark 三段选择 | ✅ |
| 关于 | App 图标 + 版本 + 技术栈 | ✅ |
| 数据署名 | CC BY-SA 3.0 | ✅ |

### 4.9 Lab 屏（待开发）

```
设计稿描述：化学反应小实验模拟
预估功能：
- 选择反应物（元素/化合物）
- 显示反应方程式
- 简单动画演示（分子碰撞、产物生成）
- 反应条件选择（温度、催化剂等）

复杂度：高（需要化学反应数据库 + 动画逻辑）
```

### 4.10 Learn 屏（待开发）

```
设计稿描述：元素学习资料
预估功能：
- 按元素展示学习资料
- 历史发现故事
- 实际应用场景
- 趣味问答（可选）

复杂度：中（需要大量文本内容数据）
```

---

## 5. UI 一致性强制规则

### 5.1 间距规则（违反即视为 bug）

```kotlin
// ✅ 正确：使用 Dimensions 常量
Modifier.padding(Dimensions.Dp16)

// ❌ 错误：硬编码 dp
Modifier.padding(16.dp)

// ❌ 错误：非 8 的倍数
Modifier.padding(14.dp)
```

### 5.2 颜色规则

```kotlin
// ✅ 正确：使用 MaterialTheme 或 CategoryColors
MaterialTheme.colorScheme.primary
LocalCategoryColors.current.forCategory(element.category)

// ❌ 错误：硬编码颜色
Color(0xFF6650A4)
```

### 5.3 字号规则

```kotlin
// ✅ 正确：使用 MaterialTheme Typography
MaterialTheme.typography.titleLarge  // 22sp，元素符号
MaterialTheme.typography.bodyLarge   // 16sp，属性值

// ❌ 错误：硬编码 sp
Text(text = "H", fontSize = 22.sp)
```

### 5.4 圆角规则

```kotlin
// ✅ 正确：使用 Shapes
MaterialTheme.shapes.medium  // 12.dp，标准卡片

// ❌ 错误：硬编码
RoundedCornerShape(12.dp)
```

### 5.5 阴影规则

```kotlin
// ✅ 正确：使用 Elevation 常量
Card(elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Shadow1))

// ❌ 错误：硬编码
cardElevation(1.dp)
```

### 5.6 点击区域规则

```kotlin
// ✅ 正确：最小 48.dp
Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)

// ❌ 错误：过小
IconButton(modifier = Modifier.size(24.dp)) { }
```

### 5.7 图标规则

```kotlin
// ✅ 正确：24.dp 基准 + Material Icons Extended
Icon(imageVector = Icons.Default.Search, contentDescription = ..., modifier = Modifier.size(24.dp))

// ❌ 错误：非标尺寸
Icon(..., modifier = Modifier.size(20.dp))
```

### 5.8 预览规则

```kotlin
// ✅ 正确：每个独立 Composable 都有 @Preview
@Preview
@Composable
private fun ElementCardPreview() { }

// ✅ 正确：Light + Dark 双预览
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
```

### 5.9 无障碍规则

```kotlin
// ✅ 正确：所有图标和交互元素都有 contentDescription
Icon(imageVector = ..., contentDescription = stringResource(R.string.search_icon))

// ✅ 正确：语义描述
Modifier.semantics { contentDescription = "${element.nameZh}元素卡片" }
```

---

## 6. 实现状态矩阵

### 6.1 Design System

| 组件 | 文件 | 状态 | 偏差 |
|------|------|:--:|:--|
| Color（M3 完整色板） | `theme/Color.kt` | ⚠️ | 缺少 Primary Container 等 M3 Token |
| CategoryColors | `theme/Color.kt` | ✅ | 精确匹配设计稿 |
| DarkCategoryColors | `theme/Color.kt` | ⚠️ | 需逐色与设计稿对比确认 |
| Typography（完整 15 阶） | `theme/Typography.kt` | ⚠️ | 缺少 8 个字阶 |
| Shapes（完整 6 级） | `theme/Shapes.kt` | ⚠️ | 缺少 20.dp |
| Dimensions（8pt Grid） | `theme/Dimensions.kt` | ✅ | 精确匹配 |
| Elevation（4 层） | `theme/Elevation.kt` | ✅ | 精确匹配 |
| GlassSurface | `ui/components/GlassSurface.kt` | ⚠️ | tint 默认值偏差 |

### 6.2 Components

| 组件 | 文件 | 状态 | 偏差 |
|------|------|:--:|:--|
| PeriodicButton | `ui/components/PeriodicButton.kt` | ✅ | — |
| PeriodicOutlinedButton | `ui/components/PeriodicOutlinedButton.kt` | ✅ | — |
| PeriodicTextButton | `ui/components/PeriodicTextButton.kt` | ✅ | — |
| ElementCard（小） | `ui/components/ElementCard.kt` | ⚠️ | 需核对字号/内边距 |
| ElementCard（大） | ❌ | ❌ | **缺失** |
| FavoriteElementCard | ❌ | ❌ | **缺失**（swipe-to-delete） |
| CategoryChip | `ui/components/CategoryChip.kt` | ⚠️ | 需核对非选中态描边色 |
| PropertyChip | `ui/components/PropertyChip.kt` | ⚠️ | 刚实现，需逐项检查 |
| PeriodicSearchBar | `ui/components/PeriodicSearchBar.kt` | ✅ | — |
| GlassSurface | `ui/components/GlassSurface.kt` | ⚠️ | tint 修正 |
| EmptyState | `ui/pattern/EmptyState.kt` | ✅ | — |

### 6.3 Patterns

| Pattern | 文件 | 状态 | 偏差 |
|---------|------|:--:|:--|
| PropertyGrid | `ui/pattern/PropertyGrid.kt` | ⚠️ | 需核对分隔线样式 |
| AtomCanvas | `ui/pattern/AtomCanvas.kt` | ✅ | — |
| EmptyState | `ui/pattern/EmptyState.kt` | ✅ | — |
| DashboardGrid | ❌ | ❌ | **缺失**（内联在 HomeScreen） |
| ComparisonTable | ❌ | ❌ | **缺失**（内联在 CompareScreen） |

### 6.4 Screens

| 屏面 | Feature 包 | 状态 | 备注 |
|------|-----------|:--:|:--|
| Home | `feature/home/` | ✅ | DashboardGrid 未提取 |
| Periodic Table | `feature/table/` | ✅ | — |
| Element Detail | `feature/detail/` | ✅ | PropertyChip 未使用 |
| Category | `feature/category/` | ✅ | — |
| Compare | `feature/compare/` | ✅ | ComparisonTable 未提取 |
| Discover | `feature/discover/` | ✅ | — |
| Favorites | `feature/favorites/` | ✅ | — |
| Profile | `feature/profile/` | ✅ | — |
| Lab | ❌ | ❌ | **待开发** |
| Learn | ❌ | ❌ | **待开发** |

---

## 7. 修复清单（按优先级）

### 🔴 P0 — 必须修复（影响设计一致性）

1. **GlassSurface tint 默认值** — 修正为 `0.15`/`0.25`
2. **Typography 补齐缺失字阶** — 补充 Display Medium/Small、Headline Large/Small、Title Small、Body Small、Label Large
3. **Color.kt 补齐 M3 完整 Token** — Primary Container、Secondary Container、On Surface Variant、Outline 等
4. **Shapes 补充 20.dp** — `radiusXXLarge`

### 🟡 P1 — 建议修复（提升一致性）

5. **ElementCard 字号/内边距核对** — 对照设计稿逐项确认
6. **CategoryChip 非选中态描边色** — 确认为 Outline 色
7. **PropertyGrid 分隔线样式** — 0.5dp Outline 色
8. **Detail 页分类标签改用 PropertyChip**

### 🟢 P2 — 可选优化（提升可维护性）

9. **DashboardGrid 提取为独立 Pattern**
10. **ComparisonTable 提取为独立 Pattern**
11. **ElementCard 大卡片变体**
12. **FavoriteElementCard（swipe-to-delete）**

---

> **文档版本**：v1.0
> **基于设计稿**：design_system.PNG / components.PNG / patterns.PNG / screens.PNG
> **生成日期**：2026-05-13
> **适用范围**：所有后续 UI 开发必须参照此文档，任何偏离需经过设计审查
