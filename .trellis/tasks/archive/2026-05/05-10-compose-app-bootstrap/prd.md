# 周期表 App Compose 客户端规划

## Goal

基于 `design/` 下的 4 张高保真设计图（design_system / components / patterns / screens），规划并搭建一款使用 Jetpack Compose 的 Android 客户端，**MVP 覆盖 5 屏：Home / Periodic Table / Element Detail / Compare / Favorites**，并把 Trellis spec 从 web 模板改造成 Android Compose 规范，为后续迭代铺路。

---

## What I already know

### 设计图解析

| 设计图 | 内容 |
|------|------|
| **01 Design System** | 紫色主色 + Element Category 多色板；8pt Grid；多字阶 Typography；Elevation 4 层（Shadow1/Shadow2/Floating/Modal）；**Glass Blur 毛玻璃**；多档 Radius；图标系统 |
| **02 Components** | Buttons / Element Cards / Chips & Tags / Inputs / Card System / Navigation / Modal & Overlays |
| **03 Patterns** | Property Grid / Feed / Dashboard / Details / Comparison / 3D Viewer / Wizard / Empty & Error |
| **04 Screens** | Home / Periodic Table / Element Detail / Category / Discover / Lab / Compare / Learn / Favorites / Profile（共 10 屏） |

### 项目状态

- 仓库当前**仅有 `design/` 与 `.trellis/`**，无任何 Android 工程代码，从零开始。
- 现有 Trellis spec（`.trellis/spec/frontend/*`）是 React/Web 模板，与 Android Compose 范式不匹配。
- 已存在 `00-bootstrap-guidelines` 任务（in_progress），原本引导填 web 通用 spec；本任务接管，将 spec 改造为 Android Compose 版本，让 00 任务在新方向下达成。
- 当前 git 分支 `master`，无提交历史。

---

## Requirements

### MVP 屏幕（5 屏 + 增项）

| 屏幕 | 关键交互 |
|------|---------|
| **Home** | 元素亮点入口、推荐 / 最近浏览、**搜索框入口** |
| **Periodic Table** | 118 元素 18×9 网格；分类筛选（Chips）；**点击详情、长按收藏、多选→Compare**；**顶部搜索** |
| **Element Detail** | 头部 3D 区域（Canvas 2D 原子动画占位）+ Property Grid + 化学/物理属性 + 收藏按钮 |
| **Compare** | 多元素属性对比表；从 Tab 进或从 Periodic Table 多选进 |
| **Favorites** | 收藏列表（DataStore 持久化）；列表项 swipe 操作；空状态 |

### 跨屏 / 全局功能

- **搜索功能**：按名称 / 符号 / 原子序号模糊查（Home + Table 顶部）
- **深浅色主题切换**：默认跟随系统，可手动覆盖（写入 DataStore Preferences）
- **多个收藏入口**：Detail 收藏按钮 + Periodic Table 长按 + Favorites 列表 swipe
- **多端适配**：手机 / 平板 / 横屏，使用 `WindowSizeClass` + `NavigationSuiteScaffold`，BottomBar / NavRail / NavDrawer 自动切换

### 设计系统落地

- 色板（Primary/Secondary/Element Category 多色） → `Color.kt` + Material 3 ColorScheme
- 字阶 → `Typography.kt`
- Spacing 8pt Grid → `Dimensions.kt` 常量
- Radius → `Shapes.kt`
- Elevation 4 层 → 自定义 `Elevation` token
- **Glass Blur** → 在 API 31+ 用 `Modifier.blur(...)` + `RenderEffect`；考虑 Haze 库做跨版本兜底
- 02 Components 全部抽成可复用 Composable，放在 `ui/components/`

### 数据层

- **静态数据集**：118 元素 JSON 放 `app/src/main/assets/elements.json`；启动时一次性加载 → `ElementRepository` 内存持有
- **持久化**：DataStore Preferences 存 `favorite_ids: Set<Int>` 与 `theme_mode: ThemeMode`
- **无后端**：MVP 完全离线

### 配套文档（Trellis Spec 改造）

将 `.trellis/spec/frontend/*` 内容全部替换为 Android Compose 规范（保留目录名以免动 Trellis 配置）：

| 现文件 | 改后内容 |
|------|---------|
| `directory-structure.md` | 单 module + 包分层（`ui/data/domain/theme/feature/<name>/`） |
| `component-guidelines.md` | Composable 命名、参数顺序（modifier 第一可选）、State Hoisting、Slot API |
| `hook-guidelines.md` → 重命名思路 | Composable 局部状态：`remember{}` / `derivedStateOf` / `produceState` 何时用 |
| `state-management.md` | ViewModel + StateFlow + UDF + sealed Intent 模板 |
| `type-safety.md` → Kotlin 约定 | Kotlin idioms：sealed / data class / value class / coroutines 用法 |
| `quality-guidelines.md` | Lint / detekt / 单测覆盖（ViewModel / Repository） |

可能新增：`theme-guidelines.md`、`navigation-guidelines.md`、`build-guidelines.md`（待 Phase 2 决定，单文件不上限）。

### 项目骨架

- 单 module（`app/`），仓库根即 Android 工程根
- Gradle 8+ / Kotlin 2.x / Compose Compiler Plugin（不再用 KSP composer）
- minSdk 24 / targetSdk 35 / compileSdk 35
- Java 17

---

## 本任务交付范围（Stage 1 only）

> ⚠️ **重要**：本任务（`05-10-compose-app-bootstrap`）只交付 **Stage 1：Trellis Spec 改造** 和与之配套的规划产出。Stage 2-8（Android 工程骨架、数据层、5 屏实施、抛光）将各自通过 `task.py create --parent <本任务>` 创建独立子任务。implement / check 子代理只需聚焦 Stage 1。

**本任务实际交付物**：

1. PRD（含 ADR-lite、研究索引、分阶段实施计划）—— 已完成
2. 4 份研究文件（`research/*.md`）—— 已完成
3. `.trellis/spec/frontend/` 7 份文件改写为 Android Compose 规范
4. `00-bootstrap-guidelines` archive

---

## Acceptance Criteria（本任务）

- [x] PRD `Goal / Requirements / Out of Scope / Technical Approach / Decision (ADR-lite) / Implementation Plan / Research References` 全部写完
- [x] 用户已在 PRD 上签字
- [x] 4 份研究文件落盘（dataset / table / blur / nav）
- [ ] `.trellis/spec/frontend/index.md` 改写为 Android Compose 总入口（含 Pre-Development Checklist + 规范导航）
- [ ] `.trellis/spec/frontend/directory-structure.md` 改写为 Android 单 module 包分层（`ui/data/domain/theme/feature/<name>/`），含 5 屏对应的真实 Composable 命名映射
- [ ] `.trellis/spec/frontend/component-guidelines.md` 改写为 Composable 规范（命名、`modifier: Modifier = Modifier` 参数顺序、State Hoisting、Slot API、Preview 注解）
- [ ] `.trellis/spec/frontend/hook-guidelines.md` 改写为 Composable 局部状态规范（`remember{}` / `derivedStateOf` / `produceState` / `collectAsStateWithLifecycle()` 使用时机）
- [ ] `.trellis/spec/frontend/state-management.md` 改写为 ViewModel + StateFlow + UDF + sealed Intent 模板（含 `koinViewModel()` 注入、Channel/SharedFlow 副作用）
- [ ] `.trellis/spec/frontend/type-safety.md` 改写为 Kotlin idioms（sealed interface/class、data class、value class、coroutines、Result）
- [ ] `.trellis/spec/frontend/quality-guidelines.md` 改写为 Lint + detekt + 单测（JUnit4 + MockK + turbine + compose-ui-test）规范
- [ ] 每份 spec 至少包含 1 个真实可用的 Compose 代码示例
- [ ] `00-bootstrap-guidelines` 已 archive

---

## Definition of Done（本任务）

- 7 份 spec 文件改写完成，内容反映 ADR-lite 第 1-10 项决策
- 每份 spec 都有具体代码示例（不是占位伪代码）
- spec 内容与 4 份研究决策一致（特别是 Haze 锁版本、Telephoto 手势、CategoryColors、currentWindowAdaptiveInfo）
- trellis-check 子代理通过

---

## 整体 MVP 验收（跨子任务，本任务不验）

> 以下条目由 Stage 2-8 各子任务分别完成与验收，本任务不再校验：
>
> - Android 工程骨架可编译：`./gradlew assembleDebug` 通过（Stage 2）
> - Theme 系统支持深 / 浅色切换并能渲染色板、字阶、Spacing、Elevation、Glass Blur（Stage 2-3）
> - Home + Periodic Table 两屏完成设计还原（Stage 4 + 7）
> - Element Detail / Compare / Favorites 三屏路由可达 + UI 主体完成（Stage 5 / 6 / 7）
> - Favorites 持久化生效（关 App 重开仍在）（Stage 7）
> - 关键 ViewModel + Repository 单测（Stage 8）
> - README 说明：运行方式、关键依赖与版本、目录约定（Stage 8）
> - 编译 / Lint / detekt 通过（Stage 8）
> - 设计图 ↔ 实现可追溯（Composable 命名映射到设计编号）（Stage 4-7 各自负责）

---

## Out of Scope

- **i18n（国际化）**：MVP 仅中文，但 strings.xml 占位（不写硬编码字符串到 Composable 里）
- **剩余 5 屏**：Lab / Learn / Discover / Profile / Category 不在 MVP
- **真实 3D Viewer**：Detail 头部用 Canvas 2D 原子动画占位，不接 SceneView/Filament
- **后端 API**
- **iOS / Web / 桌面端 / KMP**
- **商店上架、签名、CI/CD**
- **跨账号同步收藏**（无登录）

---

## Technical Approach

### 包结构（单 module 内）

```
app/src/main/java/<pkg>/
├── App.kt                   // Application(KoinApp 注册)
├── MainActivity.kt          // setContent + Theme + Nav
├── theme/                   // 主题层
│   ├── Color.kt
│   ├── Typography.kt
│   ├── Shapes.kt
│   ├── Dimensions.kt
│   ├── Elevation.kt
│   └── PeriodicProTheme.kt  // 深/浅色 ColorScheme + ThemeProvider
├── data/                    // 数据层
│   ├── element/
│   │   ├── ElementDataSource.kt   // 读 assets/elements.json
│   │   ├── ElementRepository.kt
│   │   └── model/Element.kt
│   ├── favorites/
│   │   ├── FavoritesDataStore.kt
│   │   └── FavoritesRepository.kt
│   └── theme/
│       └── ThemePreferenceRepository.kt
├── domain/                  // 领域层（如需要）
│   └── usecase/             // SearchElementsUseCase 等
├── ui/
│   ├── components/          // 02 Components
│   │   ├── PeriodicButton.kt
│   │   ├── ElementCard.kt
│   │   ├── CategoryChip.kt
│   │   ├── PeriodicSearchBar.kt
│   │   ├── GlassSurface.kt   // Glass Blur 包装
│   │   └── ...
│   ├── pattern/             // 03 Patterns
│   │   ├── PropertyGrid.kt
│   │   ├── ComparisonTable.kt
│   │   ├── AtomCanvas.kt    // 2D 原子动画占位
│   │   └── EmptyState.kt
│   └── navigation/
│       ├── PeriodicNav.kt   // NavHost
│       ├── Routes.kt        // @Serializable 路由
│       └── PeriodicNavSuite.kt  // Bottom/Rail/Drawer 切换
└── feature/
    ├── home/{HomeScreen.kt, HomeViewModel.kt, HomeUiState.kt, HomeIntent.kt}
    ├── table/{TableScreen.kt, TableViewModel.kt, ...}
    ├── detail/{DetailScreen.kt, DetailViewModel.kt, ...}
    ├── compare/{CompareScreen.kt, CompareViewModel.kt, ...}
    └── favorites/{FavoritesScreen.kt, FavoritesViewModel.kt, ...}

app/src/main/assets/elements.json    // 118 元素静态数据
app/src/test/                         // 单元测试
app/src/androidTest/                  // UI 测试（可选 MVP 不强制）
```

### 关键库选型

| 类别 | 选型 | 版本（写时） |
|------|------|--------------|
| Kotlin | Kotlin 2.x | 2.0+ |
| Build | Gradle Kotlin DSL + Version Catalog | 8.7+ |
| Compose BOM | androidx.compose:compose-bom | 2024.10+ |
| Material | Material 3 + adaptive-navigation-suite | 1.3+ |
| Adaptive | androidx.compose.material3.adaptive（含 NavigableListDetailPaneScaffold） | 1.0+ |
| Navigation | androidx.navigation.compose | 2.8+ (type-safe) |
| Lifecycle | lifecycle-runtime-compose | 2.8+ |
| DI | Koin Compose | 4.0+ |
| Persistence | DataStore Preferences | 1.1+ |
| 序列化 | kotlinx-serialization-json | 1.7+ |
| 协程 | kotlinx-coroutines | 1.9+ |
| **手势缩放** | **me.saket.telephoto:zoomable**（周期表） | 0.13+ |
| **毛玻璃** | **dev.chrisbanes.haze:haze**（锁 1.7.x，不上 2.0 alpha） | 1.7.2 |
| 测试 | JUnit4 + MockK + turbine + compose-ui-test | latest |

> 备选：`cheonjaeung/gridlayout-compose`（BoxGrid 简化自定义 Layout）；若不想引第三方手势库可退化到 `Modifier.transformable()`。

### 状态管理范式（每个 feature 包内统一）

```
ViewModel(repo: SomeRepository) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun handle(intent: Intent) { /* reduce → _state.update */ }
}

@Composable
fun XxxScreen(vm: XxxViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    XxxContent(state = state, onIntent = vm::handle)
}

@Composable
fun XxxContent(state: UiState, onIntent: (Intent) -> Unit) { /* 纯 UI */ }
```

UiState 用 `data class`；Intent 用 `sealed class` / `sealed interface`；副作用（导航、Toast）通过 `Channel` 或 `SharedFlow` 暴露。

### 多端适配范式

- 顶层 `MainActivity` 拿 `WindowSizeClass`
- `NavigationSuiteScaffold` 自动在 BottomBar（compact 手机）/ NavRail（medium 折叠/横屏）/ NavDrawer（expanded 平板）切换
- 每屏内部布局可按 `WindowWidthSizeClass` 决定单列 / 双列（如 Compare 在平板上多列展开）

### Glass Blur 实施

- API 31+：`Modifier.graphicsLayer { renderEffect = BlurEffect(...) }` 或 Compose 自带 `Modifier.blur(...)`
- API 24-30：降级为半透明 + tint（不做真实 blur）
- 抽成 `GlassSurface(modifier, blurRadius, tint, content)` 复用

---

## Decision (ADR-lite)

| # | Context | Decision | Consequences |
|---|---------|---------|-------------|
| 1 | 5 屏 vs 10 屏 vs 骨架 | **中等 5 屏 MVP**（Home/Table/Detail/Compare/Favorites） | 工期 3-4 周；剩余 5 屏迭代 |
| 2 | 架构范式 | **MVVM + StateFlow + UDF**，sealed Intent | 与 Compose 天然契合；不引入 MVI 框架样板 |
| 3 | 工程结构 | **单 module + 包分层** | 编译快、起步轻；未来按 feature 拆 module 时再重构 |
| 4 | 数据层 | **静态 JSON + DataStore** | 完全离线；不引入 Room / KSP |
| 5 | 依赖注入 | **Koin** | 运行时 DI，DSL 直观，无 KSP 编译开销；代价是缺失依赖运行时才暴露 |
| 6 | 导航 | **官方 Navigation Compose 2.8+ type-safe** | Google 官方栈、`@Serializable` 路由；无需额外库 |
| 7 | Spec 改造 | **保留 frontend/ 目录名仅改内容** | 不动 Trellis 配置；目录名通用 |
| 8 | 设备形态 | **全适配（手机+平板+横屏）** | 工作量上去；获得自适应导航 + 平板布局 |
| 9 | 3D Viewer 占位 | **Canvas 2D 原子动画** | 视觉不死板；未来可平替 SceneView |
| 10 | 增项纳入 MVP | 搜索 / 深浅色切换 / 多收藏入口 / Table 多选→Compare | 提升真实可用性；i18n 与剩余 5 屏延后 |

---

## Implementation Plan（分阶段 PR / 子任务）

> 每个阶段建议作为一个独立子任务（`task.py create ... --parent <本任务>`），完成后单独提交。

### 阶段 1：Trellis Spec 改造（子任务）
- 改写 `.trellis/spec/frontend/*` 为 Android Compose 规范
- 关闭 `00-bootstrap-guidelines` 任务
- **产出**：6+ 份 spec 文件 + 真实 Compose 代码示例引用

### 阶段 2：Android 工程骨架（子任务）
- `gradle init` 模板 / 手动搭建 Gradle KTS + Version Catalog
- Compose BOM、Material 3、Navigation、Koin、DataStore、Serialization 全部接入
- 主题骨架：Color/Typography/Shapes/Dimensions/Elevation + 深浅切换 + GlassSurface 包装
- App.kt + MainActivity.kt + 空 NavHost 跑通 Hello Compose
- **产出**：`./gradlew assembleDebug` 通过

### 阶段 3：数据层与全局组件（子任务）
- 制作 `assets/elements.json`（118 元素，至少包含：编号、符号、名称、原子量、分类、电子排布、密度、熔/沸点、原子半径、电负性、IE、发现年份/人）
- `ElementRepository` + `FavoritesRepository` + `ThemePreferenceRepository` + Koin module
- 02 Components 库：Button / ElementCard / CategoryChip / SearchBar / GlassSurface 等
- **产出**：可复用组件 + 数据可读

### 阶段 4：Periodic Table 屏（子任务，最复杂）
- 18×9 网格布局（横向可滑、可缩放）
- 分类筛选 Chips、点击进 Detail、长按收藏、多选模式 → Compare
- 多端适配：手机紧凑 / 平板舒展
- **产出**：Periodic Table 还原度 ≥ 90%

### 阶段 5：Element Detail 屏（子任务）
- Canvas 2D 原子动画头部 + 属性 PropertyGrid + 收藏按钮
- 路由参数：原子序号
- **产出**：Detail 屏完成

### 阶段 6：Compare 屏（子任务）
- 接收元素 ID 列表（来自 Tab / Table 多选）
- ComparisonTable Pattern 实现
- **产出**：Compare 屏完成

### 阶段 7：Home + Favorites 屏（子任务）
- Home：欢迎区 + 推荐元素 + 搜索框
- Favorites：列表 + swipe 操作 + 空状态
- 全局搜索 UseCase
- **产出**：剩余 2 屏完成 + MVP 合龙

### 阶段 8：抛光与质量（子任务）
- Lint / detekt 配置 + 修复
- 关键 ViewModel / Repository 单测
- README 落地
- **产出**：MVP 收尾，可发布 v0.1.0

---

## Technical Notes

- 设计资产：`design/{design_system,components,patterns,screens}.PNG`
- Trellis spec：`.trellis/spec/{backend,frontend,guides}/`
- 老王偏好：KISS / DRY / SOLID，反对过度设计与无依据的扩展点
- 后续每个阶段产出建议都进 spec 库做经验沉淀

---

## Research References

四份研究已完成，结论以本节为准（实施时具体代码示例 / 备选方案见对应 md 文件）：

- [x] [`research/elements-dataset.md`](research/elements-dataset.md)
  - **首选**：Bowserinator/Periodic-Table-JSON（CC BY-SA 3.0，字段最全，~90 KB 裁剪后），需在「关于」页署名 Wikipedia
  - **备选**：sjf8203/periodic-table-data（MIT，~35 KB）规避 ShareAlike
  - **中文必须自建**：主 `elements.json`（英文）+ `elements_zh.json`（仅 `{number, name_zh, pinyin}` 118 项）
  - **风险兜底**：超重元素 Z≥104 多个字段为 `null`，UI 必须空值兜底；113/115/117/118 中文含生僻字，需 Noto Sans CJK 字体

- [x] [`research/compose-periodic-table.md`](research/compose-periodic-table.md)
  - **网格布局**：放弃 LazyVerticalGrid（不适合不规则布局），采用 **自定义 `Layout`** 或 `cheonjaeung/gridlayout-compose` 的 `BoxGrid`
  - **缩放/平移手势**：**Telephoto `Modifier.zoomable()`**（含 fling、双击缩放、边界回弹、`onClick`/`onLongClick`）
  - **颜色映射**：自建 `CategoryColors` data class + `staticCompositionLocalOf` 注入，**不要塞进 M3 ColorScheme**；深色模式降饱和度
  - **多选模式**：`rememberSaveable<Set<Int>>` + `ContextualTopAppBar` 切换 + BackHandler；与 zoomable 协同通过其 `onLongClick` 转发
  - **响应式**：Compact 强制水平滚动 + 默认拟合；Medium 自适应 cellSize；Expanded 用 `ListDetailPaneScaffold` 双面板
  - **社区参考**：FurtherSecrets24680/chemsearch-android 最相近（Compose + M3 + MVVM + StateFlow）

- [x] [`research/glass-blur-compose.md`](research/glass-blur-compose.md)
  - **MVP 锁 `dev.chrisbanes.haze:haze:1.7.2`**（不上 2.0 alpha）：API 31+ 真模糊（RenderEffect），API 24-30 自动 scrim 兜底
  - 在 AppRoot 通过 `LocalHazeState` 下发 `HazeState`，业务侧统一用 **`GlassSurface(modifier, blurRadius, tint, shape, enabled, content)`** 包装
  - **关键禁忌**：禁止在 LazyColumn / 网格子项里逐项套 GlassSurface（118 元素各开离屏 layer = 卡爆），同屏控制在 **3-5 个**
  - `Modifier.blur` / `graphicsLayer { renderEffect }` 只模糊自身子树，做不到"看到下层"，所以"内容感知 Glass"必须用 Haze
  - RenderScript 已 deprecated，禁止新代码直接用

- [x] [`research/navigation-suite-scaffold.md`](research/navigation-suite-scaffold.md)
  - **WindowSizeClass 新入口**：用 `currentWindowAdaptiveInfo().windowSizeClass`（旧的 `calculateWindowSizeClass()` 已 deprecated）；通过 `isWidthAtLeastBreakpoint(WIDTH_DP_*_LOWER_BOUND)` 判断
  - **NavigationSuiteScaffold** 默认 Compact→BottomBar / Medium→NavRail / Expanded→NavDrawer 自动切换
  - **嵌套结构**：单 NavHost（路由全局唯一）+ NavigationSuiteScaffold 包裹，避免多 NavHost 状态丢失
  - **多 Back Stack**：用 `popUpTo(graph.findStartDestination().id) { saveState = true }` + `restoreState = true` 保 Tab 内栈
  - **平板双面板**：Expanded 嵌入 `NavigableListDetailPaneScaffold`，与外层 NavigationSuiteScaffold 嵌套使用
  - **参考实现**：Now in Android 项目的 `MainActivity.kt` + `NiaApp.kt` + `NiaNavHost.kt`
