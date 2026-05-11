# 周期表 App - 高保真设计稿解析

> 4 张设计图：Design System / Components / Patterns / Screens
> 目标平台：Android Jetpack Compose (Material 3)
> 解析日期：2026-05-11

---

## 1. 设计总览

| 文件 | 内容 | 核心产出 |
|------|------|---------|
| `design_system.PNG` | 设计 Token（色板/字体/间距/圆角/阴影/毛玻璃/图标） | 映射到 `theme/` 目录：Color.kt / Typography.kt / Shapes.kt / Dimensions.kt / Elevation.kt |
| `components.PNG` | 基础组件库（按钮/卡片/Chip/输入/导航/弹窗） | 映射到 `ui/components/`，每个组件一个 Composable |
| `patterns.PNG` | 页面模式（属性网格/Feed/Dashboard/详情/对比/3D Viewer/向导/空状态） | 映射到 `ui/pattern/`，可复用页面布局骨架 |
| `screens.PNG` | 10 个高保真屏面（Home/周期表/元素详情/分类/发现/Lab/对比/学习/收藏/个人） | 映射到 `feature/`，MVP 先做 5 屏 |

---

## 2. Design System Token 拆解

### 2.1 色板 (Color Palette)

**主色系**：紫色（Primary），Material 3 Dynamic Color 可覆盖

**元素分类多色板**（Category Colors，**不与 M3 ColorScheme 混**，独立 `staticCompositionLocalOf` 注入）：

| 分类 | 预估色值 | 用途 |
|------|---------|------|
| 碱金属 (Alkali) | `#FF6B6B` 红 | ElementCard 顶部色条 + 标签 |
| 碱土金属 (Alkaline Earth) | `#FFA94D` 橙 | 同上 |
| 过渡金属 (Transition) | `#FFD43B` 黄 | 同上 |
| 后过渡金属 (Post-Transition) | `#69DB7C` 绿 | 同上 |
| 类金属 (Metalloid) | `#4DABF7` 蓝 | 同上 |
| 非金属 (Nonmetal) | `#748FFC` 靛 | 同上 |
| 卤素 (Halogen) | `#DA77F2` 紫 | 同上 |
| 稀有气体 (Noble Gas) | `#9775FA` 深紫 | 同上 |
| 镧系 (Lanthanide) | `#F783AC` 粉 | 同上 |
| 锕系 (Actinide) | `#E599F7` 浅紫 | 同上 |

**深色模式变化**：饱和度下降 20%-30%，背景色从 `#FFFFFF` → `#1A1B1E`（M3 Dark Surface）

**毛玻璃叠加色**：`rgba(255,255,255,0.15)` 浅色 / `rgba(0,0,0,0.25)` 深色

### 2.2 字阶 (Typography)

Material 3 标准字阶映射：

| Token | M3 Style | 预估字号 | 用途 |
|-------|----------|---------|------|
| `displayLarge` | Display Large | 57sp | 元素原子序号大数字 |
| `headlineMedium` | Headline Medium | 28sp | 屏面标题 |
| `titleLarge` | Title Large | 22sp | 元素符号卡片 |
| `titleMedium` | Title Medium | 16sp | Section Header（如"属性"分类名） |
| `bodyLarge` | Body Large | 16sp | 属性值 |
| `bodyMedium` | Body Medium | 14sp | 属性标签、列表项 |
| `labelMedium` | Label Medium | 12sp | Chips、标签、辅助文字 |
| `labelSmall` | Label Small | 11sp | 角标、微标注 |

**字体**：系统默认 (Roboto / Noto Sans)，中文字符需 Noto Sans CJK（特别是 Z≥113 生僻字）

### 2.3 8pt Grid 间距系统

```kotlin
// Dimensions.kt 常量定义
val Dp0 = 0.dp
val Dp4 = 4.dp       // micro
val Dp8 = 8.dp       // small → 基准
val Dp12 = 12.dp     // medium-small
val Dp16 = 16.dp     // medium → 内边距
val Dp20 = 20.dp     // medium-large
val Dp24 = 24.dp     // large → 卡片间距
val Dp32 = 32.dp     // xlarge → Section 间距
val Dp40 = 40.dp     // xxlarge
val Dp48 = 48.dp     // 最小点击区域
val Dp56 = 56.dp     // TopAppBar 标准高度
val Dp64 = 64.dp     // BottomBar/NavRail item 高度
```

**强制规则**：所有间距、内边距、尺寸必须为 `8.dp` 的整数倍（`Dp4` 仅限极细线条/分割线）。

### 2.4 圆角 (Radius / Shapes)

| Token | 值 | 用途 |
|-------|---|------|
| `shapeExtraSmall` | 4.dp | Chips、小标签 |
| `shapeSmall` | 8.dp | 小卡片、输入框 |
| `shapeMedium` | 12.dp | 标准卡片（ElementCard、Property Card） |
| `shapeLarge` | 16.dp | 大卡片、Modal、BottomSheet |
| `shapeExtraLarge` | 24.dp | 圆角容器、Dashboard 区块 |

### 2.5 Elevation 4 层体系

| 层级 | 名称 | 阴影描述 | 系统映射 |
|------|------|---------|---------|
| L1 | Shadow 1 | `elevation=1dp`，微弱投影 | M3 `CardDefaults.elevatedCardElevation(1.dp)` |
| L2 | Shadow 2 | `elevation=3dp`，标准悬浮 | M3 `elevation=3.dp` |
| L3 | Floating | `elevation=6dp`，浮层（BottomSheet、Tooltip） | M3 `elevation=6.dp` |
| L4 | Modal | `elevation=8dp`，模态弹窗 + 背景 Scrim | M3 `elevation=8.dp` |

**注意**：M3 的 `TonalElevation` 不适用，本设计用的是传统 Shadow Elevation（`ShadowElevation`）。

### 2.6 Glass Blur 毛玻璃

- **适用场景**：BottomBar 背景、TopAppBar 滚动后效果、上下文菜单、卡片叠加
- **实现**：`dev.chrisbanes.haze:haze:1.7.2`（锁版本，不上 2.0 alpha）
  - API 31+ → RenderEffect 真模糊
  - API 24-30 → 半透明 Scrim 兜底
- **性能红线**：
  - 禁止在 LazyColumn / 网格子项中使用（每个 item 开离屏 layer = 卡爆）
  - 同屏 ≤ 5 个 GlassSurface 实例
  - 通过 `LocalHazeState` 全局下发 `HazeState`

### 2.7 图标系统

- Material Icons Extended（`androidx.compose.material:material-icons-extended`）
- 统一 24dp 基准尺寸
- 点击区最小 48dp（`Dp48`）

---

## 3. Components 组件清单

### 3.1 Buttons

| 组件 | Composable | 关键设计点 |
|------|-----------|-----------|
| Primary Button | `PeriodicButton` | 紫色填充，圆角 `shapeMedium`(12dp)，高度 48dp |
| Secondary Button | `PeriodicOutlinedButton` | 紫色描边，透明背景 |
| Text Button | `PeriodicTextButton` | 无边框无背景，紫色文字 |
| Icon Button | 标准 M3 `IconButton` | 48dp 点击区 |
| FAB | 标准 M3 `FloatingActionButton` | 右下角 16dp 间距 |

### 3.2 Element Cards

| 组件 | Composable | 关键设计点 |
|------|-----------|-----------|
| 元素小卡片 | `ElementCard` | 顶部 Category 色条(4dp高) + 原子序号 + 符号(大字) + 名称；圆角 `shapeMedium`；L1 阴影 |
| 元素大卡片 | `ElementDetailCard` | 同上 + 原子量 + 分类 Chip；圆角 `shapeLarge` |
| 收藏卡片 | `FavoriteElementCard` | 基于 `ElementCard`，右上角收藏图标，swipe-to-delete |

**卡片色条映射**：顶部 4dp 高度用 Category Color → 必须在 `ElementCard` 内部通过 `staticCompositionLocalOf<CategoryColors>` 获取分类色值。

### 3.3 Chips & Tags

| 组件 | Composable | 用途 |
|------|-----------|------|
| 分类筛选 Chip | `CategoryChip` | Periodic Table 顶部分类筛选，选中态填充 Category Color |
| 属性标签 | `PropertyChip` | Detail 页属性分类标签 |
| 多选计数 Chip | `SelectionChip` | 多选模式下显示已选数量 |

### 3.4 Inputs

| 组件 | Composable | 关键设计点 |
|------|-----------|-----------|
| 搜索框 | `PeriodicSearchBar` | 圆角 `shapeSmall`(8dp)，带搜索 icon，支持 clear 按钮 |
| 搜索输入 | `SearchTextField` | Material 3 `SearchBar` 或 `TextField` + `OutlinedTextField` |

### 3.5 Card System

- 标准信息卡片 → 内边距 16dp，L1 阴影
- 可展开卡片 → 带展开/折叠箭头动画
- 交互卡片 → 点击态 ripple + 轻微缩放（`animateScaleBy`）

### 3.6 Navigation

| 屏幕形态 | 组件 | Material 3 对应 |
|---------|------|----------------|
| 手机竖屏 | Bottom Navigation Bar | `NavigationBar` |
| 折叠/横屏 | Navigation Rail | `NavigationRail` |
| 平板 | Navigation Drawer | `PermanentNavigationDrawer` |

**自动切换**：`NavigationSuiteScaffold` + `WindowSizeClass` 根据屏幕宽度自动选择。

### 3.7 Modal & Overlays

| 组件 | 用途 | 设计点 |
|------|------|--------|
| Bottom Sheet | 元素快捷预览 | 毛玻璃背景，圆角 `shapeLarge`(16dp)，L3 Floating |
| Dialog | 确认删除收藏、主题切换 | L4 Modal，背景 Scrim `rgba(0,0,0,0.4)` |
| Dropdown Menu | 排序/筛选选项 | 标准 M3 `DropdownMenu` |

---

## 4. Patterns 页面模式

### 4.1 Property Grid（属性网格）
- 2 列网格展示元素属性（原子量、密度、熔点、沸点等）
- 每项：属性名 `labelMedium` + 属性值 `bodyLarge` + 单位 `labelSmall`
- 分隔线 `Dp8` 间距

### 4.2 Feed（信息流）
- Home 屏"推荐元素"区域使用
- 横向滑动 `LazyRow`，每项为 `ElementCard` + `Dp16` 间距

### 4.3 Dashboard（仪表盘）
- Home 屏主区域，卡片矩阵展示入口
- 圆角 `shapeExtraLarge`(24dp)，L2 阴影

### 4.4 Details（详情）
- Element Detail 屏主模式
- 顶部 2D 原子动画占位（Canvas）+ PropertyGrid + 描述文本

### 4.5 Comparison（对比）
- Compare 屏使用
- 多列属性对比表，表头固定，内容横向可滑
- 每列一个元素，行按属性分类分组

### 4.6 3D Viewer（3D 查看器）
- MVP 不实现真 3D，用 Canvas 2D 原子动画占位
- Canvas 画原子核（质子/中子小球）+ 电子层轨道

### 4.7 Wizard（向导）
- Lab 屏用，MVP 不做

### 4.8 Empty & Error States
- 空收藏列表 → 图标 + "还没有收藏任何元素" + "去周期表看看"按钮
- 搜索无结果 → 图标 + "未找到相关元素" + 建议表情
- 加载/错误 → 标准 M3 `CircularProgressIndicator` / 重试按钮

---

## 5. Screens 屏面清单（10 屏）

### MVP 必做（Stage 4-7）

| # | 屏面 | Feature 包 | 核心交互 | MVP |
|---|------|-----------|---------|:---:|
| 1 | **Home** | `feature/home/` | 欢迎区 + 推荐元素 `LazyRow` + 搜索框入口 + Dashboard 卡片矩阵 | ✅ |
| 2 | **Periodic Table** | `feature/table/` | 18×9 自定义 Layout + 分类 Chips 筛选 + 缩放/平移 + 长按收藏 + 多选→Compare + 搜索 | ✅ |
| 3 | **Element Detail** | `feature/detail/` | Canvas 2D 原子动画头部 + PropertyGrid + 属性区块 + 收藏按钮 | ✅ |
| 4 | **Compare** | `feature/compare/` | 多列对比表 + 从 Table 多选进入 / Tab 进入 | ✅ |
| 5 | **Favorites** | `feature/favorites/` | 收藏列表 + swipe 删除 + 空状态 + DataStore 持久化 | ✅ |

### 迭代补全（后续子任务）

| # | 屏面 | Feature 包 | 核心交互 | MVP |
|---|------|-----------|---------|:---:|
| 6 | Category | `feature/category/` | 按分类浏览元素列表 | ❌ |
| 7 | Discover | `feature/discover/` | 发现页，元素趣味知识 | ❌ |
| 8 | Lab | `feature/lab/` | 化学反应小实验模拟 | ❌ |
| 9 | Learn | `feature/learn/` | 元素学习资料 | ❌ |
| 10 | Profile | `feature/profile/` | 个人设置（主题、关于、数据来源署名） | ❌ |

---

## 6. 设计 → 工程映射速查

### 6.1 Design Token → Kotlin 文件

```
design_system.PNG            →  theme/
├── 色板                      →  Color.kt (Primary/Secondary/CategoryColors)
├── 字阶                      →  Typography.kt (M3 Typography)
├── 圆角                      →  Shapes.kt (M3 Shapes)
├── 8pt Grid 间距             →  Dimensions.kt (Dp常量)
├── Elevation 4层             →  Elevation.kt (自定义token)
├── Glass Blur                →  ui/components/GlassSurface.kt
└── 图标                      →  (Material Icons Extended 库)
```

### 6.2 Components → Composable 文件

```
components.PNG               →  ui/components/
├── Buttons                   →  PeriodicButton.kt
├── Element Cards             →  ElementCard.kt (含大小变体)
├── Chips & Tags              →  CategoryChip.kt / PropertyChip.kt
├── Inputs                    →  PeriodicSearchBar.kt
├── Card System               →  (M3 Card + 自定义包装)
├── Navigation                →  ui/navigation/PeriodicNavSuite.kt
└── Modal & Overlays          →  (M3 原生为主，Glass 包装)
```

### 6.3 Patterns → Composable 文件

```
patterns.PNG                 →  ui/pattern/
├── Property Grid             →  PropertyGrid.kt
├── Feed                      →  (LazyRow 标准模式)
├── Dashboard                 →  DashboardGrid.kt
├── Details                   →  (Detail 屏内联)
├── Comparison                →  ComparisonTable.kt
├── 3D Viewer (占位)          →  AtomCanvas.kt (Canvas 2D)
├── Wizard                    →  (后续迭代)
└── Empty & Error             →  EmptyState.kt
```

### 6.4 Screens → Feature 包

```
screens.PNG                  →  feature/
├── Home                      →  feature/home/HomeScreen.kt
├── Periodic Table            →  feature/table/TableScreen.kt
├── Element Detail            →  feature/detail/DetailScreen.kt
├── Compare                   →  feature/compare/CompareScreen.kt
├── Favorites                 →  feature/favorites/FavoritesScreen.kt
├── Category                  →  (后续)
├── Discover                  →  (后续)
├── Lab                       →  (后续)
├── Learn                     →  (后续)
└── Profile                   →  (后续)
```

---

## 7. 实施重点与隐患

### 7.1 必须死扣的设计细节

| 优先级 | 细节 | 原因 |
|--------|------|------|
| 🔴 P0 | ElementCard 顶部 Category 色条 4dp | 118 个元素卡片的第一视觉识别，错了全完 |
| 🔴 P0 | Glass Blur 性能红线 | 118 个卡片每个套 Glass = 用户手机会冒烟 |
| 🔴 P0 | 周期表 18×9 网格 + 镧系/锕系独立行 | 化学周期表的标准布局，不能变形 |
| 🟡 P1 | 8pt Grid 强制 | 所有间距设计稿都按 8dp 基准走，不一致会视觉崩 |
| 🟡 P1 | CategoryColors ≠ M3 ColorScheme | 元素分类色是业务色彩，塞进 M3 ColorScheme 会污染整个主题 |
| 🟡 P1 | 深色模式下 Category 色降饱和 | 高饱和荧光色在暗背景下刺眼 |
| 🟢 P2 | 中文字体生僻字 (Z≥113) | 元素名 鉨/鏌/鉝/鿫 需要 Noto Sans CJK，否则显示豆腐块 |
| 🟢 P2 | 毛玻璃 API 分级 | API < 31 没有 RenderEffect，只能半透明兜底 |

### 7.2 技术风险

| 风险 | 影响 | 缓解 |
|------|------|------|
| Telephoto zoomable + 长按/点击冲突 | 手势冲突难调 | 用 `zoomable` 的 `onClick`/`onLongClick` 槽位，不要用外层包裹的 `combinedClickable` |
| 自定义 Layout 复杂度 | 镧系/锕系错位 | 先用 `BoxGrid` (gridlayout-compose) 快速验证，后续优化时换自定义 `Layout` |
| WindowSizeClass 在折叠屏上频繁切换 | 导航架构抖动 | `NavigationSuiteScaffold` 自带`animateLayoutChanges`，设定 `WindowWidthSizeClass` 断点阈值 |
| DataStore Preferences 类型安全 | 收藏列表数据丢失 | `Set<Int>` 序列化/反序列化用 `stringSetPreferencesKey` + 明确的迁移逻辑 |

### 7.3 KISS 原则优先

- **不要为未来 10 屏预留抽象接口**（YAGNI），5 屏 MVP 直接用具体实现
- **不要引入 Room 数据库**（YAGNI），118 条静态数据用 JSON + 内存持有完全够
- **不要拆多 module**（YAGNI），单 module + 包分层编译快、调试爽

---

## 8. 设计资产来源

| 文件 | 尺寸 | 内容 |
|------|------|------|
| `design_system.PNG` | ~1.4 MB | 完整 Design Token 清单 |
| `components.PNG` | ~1.7 MB | 全部基础组件设计 |
| `patterns.PNG` | ~1.6 MB | 8 种页面模式 |
| `screens.PNG` | ~1.7 MB | 10 个高保真屏面 |

---

## 9. 相关文档索引

- PRD（需求 + ADR 决策 + 实施计划）：`.trellis/tasks/05-10-compose-app-bootstrap/prd.md`
- 元素数据集研究：`.trellis/tasks/05-10-compose-app-bootstrap/research/elements-dataset.md`
- 周期表布局研究：`.trellis/tasks/05-10-compose-app-bootstrap/research/compose-periodic-table.md`
- Glass Blur 研究：`.trellis/tasks/05-10-compose-app-bootstrap/research/glass-blur-compose.md`
- 导航与自适应研究：`.trellis/tasks/05-10-compose-app-bootstrap/research/navigation-suite-scaffold.md`

---

> **老王注**：设计稿画得不错，但这 4 张图要是再不上 Compose 落地，就是在耍流氓。色板、间距、字体、阴影、毛玻璃 5 大系统必须一个像素不差地映射到代码里。开干。
