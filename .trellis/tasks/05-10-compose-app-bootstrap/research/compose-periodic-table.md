# Research: Jetpack Compose 周期表 18×9 布局实现方案调研

- **Query**: 为 Android Compose App 找到 118 元素 18×9 周期表的最佳实现方案（不规则布局 + 缩放手势 + 多分类着色 + 多选交互 + 响应式）
- **Scope**: 外部为主（GitHub / 官方文档 / 博客）
- **Date**: 2026-05-10

---

## 1. 社区现成 Compose 周期表实现

| 仓库 | Stars | 技术栈 | 思路简介 |
|---|---|---|---|
| [JLindemann42/Atomic-Periodic-Table.Android](https://github.com/JLindemann42/Atomic-Periodic-Table.Android) | 220 | Kotlin (73%) + Python（数据生成）；尚为 View System / RecyclerView 时代为主 | 老牌开源，特点是数据集 (JSON) + 多语言字符串。布局并非纯 Compose，但 element 数据模型与 category 颜色映射可直接借鉴。GPL-3.0。 |
| [FurtherSecrets24680/chemsearch-android](https://github.com/FurtherSecrets24680/chemsearch-android) | 6 | Kotlin + Jetpack Compose + Material 3 + Retrofit + Coil + 自研 Canvas 3D | 最新（2026-04 push）的纯 Compose 化学应用，技术栈与本项目完全对齐：MVVM + StateFlow + Material 3。MIT。 |
| [Frozen-Developers/android-periodic-table](https://github.com/Frozen-Developers/android-periodic-table) | - | Kotlin + Holo（旧）+ Wikipedia 数据库 | 元素数据库构建脚本（`build_db.py`）值得参考，离线数据来源思路完整。 |
| [jeanbarrossilva/Period](https://github.com/jeanbarrossilva/Period) | 0 | Kotlin + XML View + 多模块（:ui/:model/:extensions） | 模块拆分思路：`:model`（数据/sealed class）、`:ui`、`:extensions`。可参考其 module 结构。 |
| 商业模板 [Periodic Table Explorer](https://prowebthemes.com/plugins/periodic-table-explorer-complete-periodic-table-app-with-quiz-search-and-admob-monetization/61504969) | 付费 | Compose + Material 3 + MVVM + DataStore | 含完整 118 元素 + 测验 + 详情页，UI 思路（overview/properties/history Tab）值得抄。 |

> 没有找到 star 上千的、纯 Compose 周期表参考实现 —— 这是个机会点。建议把 chemsearch-android 作为主要参考。

---

## 2. 网格布局技术选型对比

周期表 18×9 是**已知大小、不规则、需要任意 (row, col) 寻址**的网格，**不是同质大列表**。Google 官方文档（[Grid | Jetpack Compose](https://developer.android.com/develop/ui/compose/layouts/adaptive/grid)）明确指出：

> `LazyVerticalGrid`, `LazyStaggeredGrid`, `LazyHorizontalGrid` are mainly for visualization of large, homogeneous data sets... These components are NOT designed for the structural layout of a screen or complex component.

| 方案 | 不规则布局 | 缩放手势 | 性能 | 复杂度 | 是否推荐 |
|---|---|---|---|---|---|
| **`LazyVerticalGrid` + `GridItemSpan`** | 用 `span` 留白可行，但 1×8 空白格 + 镧锕系单独行处理麻烦 | Lazy 容器与 graphicsLayer 缩放兼容差，缩放时 viewport 计算异常 | 118 个项目本就不大，Lazy 收益有限 | 中 | ❌ |
| **`LazyHorizontalGrid`** | 行数固定（9），但宽度方向 lazy 对手机横屏滚动友好 | 同上 | OK | 中 | ⚠ 备选 |
| **`Row` × `Column` 嵌套** | 需为每个空位塞 `Spacer`，可读性差 | 可，整体加 graphicsLayer | 118 个非懒加载也无压力 | 低 | ⚠ 仅原型 |
| **自定义 `Layout` 测量** | ✅ 任意 `(row, col)` 直接定位；镧锕系作为单独 sub-grid 拼接 | ✅ 配合 graphicsLayer 完美 | ✅ 一次测量 118 个 measurable | 高 | ✅✅ |
| **`BoxWithConstraints` + `offset`** | 用约束计算 cellSize，每个 Cell 用 `Modifier.offset(x = col * cellSize, y = row * cellSize)` | ✅ | OK | 中 | ✅ |
| **第三方 [`cheonjaeung/gridlayout-compose`](https://github.com/cheonjaeung/gridlayout-compose) 的 `BoxGrid`** | ✅ 原生支持 `Modifier.position(row, column)`，9×18 一行声明 | ✅（外层加 graphicsLayer） | OK | 低 | ✅✅ |
| **新版 `Grid` (`androidx.compose.foundation.layout.Grid`)** | ✅ 二维布局原生 API，专为屏幕级结构设计 | ✅ | OK | 低 | ✅（如最低 SDK 允许） |

**结论**：放弃 Lazy 系，采用 **自定义 `Layout`** 或 **`BoxGrid` (gridlayout-compose)** 或 **官方新 `Grid`** 之一。镧系/锕系单独再放一个 18 列 sub-grid，与主表用 `Column` 拼接（中间加 8dp 间隔）。

---

## 3. 缩放 + 平移手势

| 方案 | 适用场景 | 优缺点 |
|---|---|---|
| **`Modifier.transformable(state)`** + `graphicsLayer` | 官方推荐，简单缩放/平移/旋转 | ✅ 内置 `rememberTransformableState`，回调里手动 clamp scale (0.5..3) 与 offset 边界。✅ 与 `combinedClickable` 兼容。⚠ 不带 fling、不带双击缩放、不带边界回弹。 |
| **`pointerInput { detectTransformGestures }`** | 需要更细控制（双指 vs 单指逻辑分离） | ✅ 灵活；⚠ 必须自己处理 panZoomLock、consume |
| **[`me.saket.telephoto:zoomable`](https://saket.github.io/telephoto/zoomable/) `Modifier.zoomable()`** | 高品质媒体级体验 | ✅ 双击缩放、单指 double-tap-hold 缩放、fling、边界回弹、键鼠快捷键、状态保存。⚠ **会消费所有 tap**，与 `Modifier.clickable / combinedClickable` 不兼容，必须用其 `onClick` / `onLongClick` 参数。⚠ 多了个依赖。 |

**推荐**：**Telephoto `Modifier.zoomable()`**。理由：
1. 周期表手机竖屏 UX 最痛点是 "缩放看 symbol" "双指放大区域" "双击放大某元素" —— Telephoto 全部内置；
2. 它的 `onClick` / `onLongClick` 钩子正好对应"点击元素详情/长按进多选"两种交互，不冲突；
3. 缩放后 viewport 会自动夹紧，不会拖飞。

仅当不想引入第三方依赖时退化到 `Modifier.transformable()`。

---

## 4. 颜色分类映射（10 种 element category）

### 4.1 标准颜色参考

[Wikipedia 元素分类配色表](https://en.wikipedia.org/wiki/Help:Periodic_table_classifications_in_the_English_Wikipedia) 给出常见 10 类：
Alkali metal · Alkaline earth metal · Lanthanide · Actinide · Transition metal · Post-transition metal · Metalloid · Reactive nonmetal · Noble gas · Unknown。

> Wikipedia 自 2021 起改用 block (s/p/d/f) 配色（更中立）；具体 block 色：
> `s-block #ffe6dd` · `p-block #fdff8c` · `d-block #d9eff5` · `f-block #aaffad`。

### 4.2 Material 3 ColorScheme 是否够用？

**不够。** M3 ColorScheme 只有 primary/secondary/tertiary/error 4 组语义槽，最多覆盖 4 种 category。强行映射会破坏 M3 的语义（让 primary 变成"碱金属"色）。

### 4.3 推荐：自建 token + CompositionLocal 扩展

参考 [PCSalt - Compose Theming Material3](https://pcsalt.com/compose/compose-theming-material3/) 与 [SO 79627092](https://stackoverflow.com/questions/79627092)，标准做法：

```kotlin
// CategoryColors.kt
@Immutable
data class CategoryColors(
    val alkaliMetal: Color,
    val alkalineEarthMetal: Color,
    val transitionMetal: Color,
    val postTransitionMetal: Color,
    val metalloid: Color,
    val reactiveNonmetal: Color,
    val nobleGas: Color,
    val lanthanide: Color,
    val actinide: Color,
    val unknown: Color,
) {
    fun forCategory(c: ElementCategory): Color = when (c) {
        ElementCategory.AlkaliMetal -> alkaliMetal
        // ...
    }
}

val LocalCategoryColors = staticCompositionLocalOf<CategoryColors> {
    error("CategoryColors not provided")
}

// 在 AppTheme 内 CompositionLocalProvider 注入 light/dark 两套
```

注意：**深色模式下要降低饱和度**（HSL L 值降到 30~40%），否则 10 种鲜艳色块在黑底上会非常刺眼。建议用 [Material Theme Builder](https://m3.material.io/theme-builder) 为每个 category 生成 tonal palette，取 light=tone 90 / dark=tone 30。

---

## 5. 多选模式 UX 范式

参考 [Medium - Multi-List Item Selection in Jetpack Compose](https://medium.com/make-apps-simple/multi-list-item-selection-in-jetpack-compose-301fcf375a6c) 与 [jordond/drag-select-compose](https://github.com/jordond/drag-select-compose)：

| 关注点 | 推荐做法 |
|---|---|
| 进入多选 | 长按任一 cell 触发 → `inSelectionMode = true`，同时把该 cell 加入选中集合 |
| 退出多选 | 选中集合为空时 `LaunchedEffect` 自动退出；BackHandler 拦截返回键清空 |
| 单击行为 | 普通模式 → 跳详情；多选模式 → toggle 选中 |
| 长按行为 | `combinedClickable(onLongClick = ...)` 或自写 `pointerInput { detectTapGestures(onLongPress = ...) }`（必要，因为 zoomable 会消费 click） |
| 视觉反馈 | 选中：边框 2dp `MaterialTheme.colorScheme.primary` + scale 0.92 (animateFloatAsState) + 角标 ✓ icon。未选中：alpha 0.5（仅在 inSelectionMode 时） |
| TopAppBar | 进入多选切到 ContextualTopAppBar（显示已选数量 + 删除/分享/导出 actions） |
| 状态管理 | `var selected by rememberSaveable(mutableStateOf(emptySet<Int>()))`；ViewModel hold 更稳 |
| 进阶 | 集成 [drag-select-compose](https://github.com/jordond/drag-select-compose)：长按后拖动可框选范围 + auto-scroll + haptics（仅适配 Lazy 网格，自定义 Layout 需自己实现） |

> ⚠ **与 Telephoto zoomable 协同**：因 zoomable 消费 tap，需用其 `onClick`/`onLongClick` 参数把事件转发给 cell。在 cell 上不能再用 `combinedClickable`。Cell 内只做视觉绘制 + 点击区域 hit-test 由父 `zoomable` 通过坐标→(row,col) 反查。

---

## 6. 响应式布局（WindowSizeClass）

依据 [Use window size classes](https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes) 与 [Canonical layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive/canonical-layouts)：

| WindowWidthSizeClass | 典型设备 | 布局策略 |
|---|---|---|
| **Compact** (<600dp) | 手机竖屏 | 周期表整体宽度 = 18 × cellSize（cellSize ≈ 56dp，总宽 ≈ 1008dp）→ **强制水平滚动** + 默认初始 `scale = viewportWidth / tableWidth` 让首屏看全；用户可 pinch zoom 进。详情页用 BottomSheet。 |
| **Compact-tall / Medium-W landscape (height compact)** | 手机横屏 | 表格仍按 cellSize 56dp 渲染，宽度可能恰好放下；详情页用 BottomSheet 或半屏 dialog（避免 ListDetailPaneScaffold 在矮高度下挤压）。 |
| **Medium** (600~840dp) | 平板竖屏 / 折叠机内屏竖屏 | cellSize 自适应：`cellSize = (maxWidth - padding) / 18`，整表一屏铺满；缩放仍可用但默认 1×。详情页 BottomSheet。 |
| **Expanded** (≥840dp) | 平板横屏 / 桌面 / Chromebook | **`ListDetailPaneScaffold` 双面板**：左侧周期表（占 60~70%），右侧元素详情。点击元素更新 detail pane 而非新 Activity。 |

实现要点：
```kotlin
val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
if (isExpanded) ListDetailPaneScaffold(...) else SinglePaneWithBottomSheet(...)
```

---

## 7. 推荐技术栈与实现思路（综合方案）

### 7.1 技术栈

- **网格布局**：自定义 `Layout` （或 `cheonjaeung/gridlayout-compose` 的 `BoxGrid`）— 完全控制 (period, group) 定位
- **手势**：`me.saket.telephoto:zoomable` `Modifier.zoomable()` — 含 fling、双击缩放、边界回弹、`onClick`/`onLongClick`
- **颜色**：自建 `CategoryColors` data class + `staticCompositionLocalOf` 注入；light/dark 两套 tonal palette
- **多选**：`rememberSaveable` 持有 `Set<Int>` + `inSelectionMode` 派生状态；Topbar 切换 ContextualTopAppBar；BackHandler
- **响应式**：`currentWindowAdaptiveInfo().windowSizeClass` + Compact/Medium/Expanded 三档分支；Expanded 用 `ListDetailPaneScaffold`
- **状态层**：ViewModel + StateFlow（与 chemsearch-android 一致）

### 7.2 简化代码骨架

```kotlin
// ---------- 数据 ----------
data class Element(
    val z: Int,           // 原子序数 1..118
    val symbol: String,
    val name: String,
    val period: Int,      // 1..7（镧锕系记 8/9 用于绘制行）
    val group: Int,       // 1..18
    val category: ElementCategory,
)

enum class ElementCategory { AlkaliMetal, AlkalineEarthMetal, TransitionMetal,
    PostTransitionMetal, Metalloid, ReactiveNonmetal, NobleGas,
    Lanthanide, Actinide, Unknown }

// ---------- 自定义 Layout ----------
@Composable
fun PeriodicTableLayout(
    elements: List<Element>,
    modifier: Modifier = Modifier,
    cellSize: Dp,
    cellSpacing: Dp = 2.dp,
    content: @Composable (Element) -> Unit,
) {
    Layout(
        modifier = modifier,
        content = { elements.forEach { e -> Box(Modifier.layoutId(e.z)) { content(e) } } }
    ) { measurables, constraints ->
        val cellPx = cellSize.roundToPx()
        val gapPx = cellSpacing.roundToPx()
        val cellConstraints = Constraints.fixed(cellPx, cellPx)
        val totalCols = 18
        val totalRows = 9   // 1..7 主表 + 镧系第8行 + 锕系第9行
        val width = totalCols * cellPx + (totalCols - 1) * gapPx
        val height = totalRows * cellPx + (totalRows - 1) * gapPx + 16.dp.roundToPx() // 主表与 f-block 间隔

        layout(width, height) {
            measurables.forEach { m ->
                val z = m.layoutId as Int
                val e = elements.first { it.z == z }
                val placeable = m.measure(cellConstraints)
                val (row, col) = mapToGrid(e)  // 处理镧锕系单独行
                val x = (col - 1) * (cellPx + gapPx)
                val extraY = if (row >= 8) 16.dp.roundToPx() else 0
                val y = (row - 1) * (cellPx + gapPx) + extraY
                placeable.placeRelative(x, y)
            }
        }
    }
}

private fun mapToGrid(e: Element): Pair<Int, Int> = when {
    e.category == ElementCategory.Lanthanide -> 8 to (e.z - 57 + 3)  // La..Lu → row8 col3..17
    e.category == ElementCategory.Actinide   -> 9 to (e.z - 89 + 3)
    else -> e.period to e.group
}

// ---------- 屏幕组合 ----------
@Composable
fun PeriodicTableScreen(viewModel: PeriodicViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val isExpanded = windowSize.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val zoomable = rememberZoomableState(
        zoomSpec = ZoomSpec(maxZoomFactor = 3f),
    )

    BackHandler(enabled = state.inSelectionMode) { viewModel.clearSelection() }

    Scaffold(topBar = {
        if (state.inSelectionMode) SelectionTopBar(state.selected.size, viewModel::clearSelection)
        else NormalTopBar()
    }) { padding ->
        val tableContent: @Composable BoxScope.() -> Unit = {
            PeriodicTableLayout(
                elements = state.elements,
                cellSize = if (isExpanded) 64.dp else 56.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .zoomable(
                        state = zoomable,
                        onClick = { offset -> viewModel.onCellTap(offset, isExpanded) },
                        onLongClick = { offset -> viewModel.onCellLongPress(offset) },
                    )
            ) { element ->
                ElementCell(
                    element = element,
                    isSelected = element.z in state.selected,
                    inSelectionMode = state.inSelectionMode,
                )
            }
        }
        if (isExpanded) {
            ListDetailPaneScaffold(
                listPane = { Box(Modifier.fillMaxSize(), content = tableContent) },
                detailPane = { ElementDetailPane(state.focused) },
            )
        } else {
            Box(Modifier.fillMaxSize().padding(padding), content = tableContent)
            if (state.focused != null) ElementDetailBottomSheet(state.focused, viewModel::dismiss)
        }
    }
}

// ---------- Cell ----------
@Composable
fun ElementCell(element: Element, isSelected: Boolean, inSelectionMode: Boolean) {
    val bg = LocalCategoryColors.current.forCategory(element.category)
    val scale by animateFloatAsState(if (isSelected) 0.92f else 1f, label = "cellScale")
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(bg, RoundedCornerShape(4.dp))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp),
            )
            .alpha(if (inSelectionMode && !isSelected) 0.5f else 1f),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${element.z}", style = MaterialTheme.typography.labelSmall)
            Text(element.symbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (isSelected) Icon(Icons.Default.Check, null, Modifier.align(Alignment.TopEnd).size(14.dp))
    }
}
```

### 7.3 实施顺序建议

1. **先静态**：用 `Row × Column + Spacer` 把 18×9 跑通，验证数据/分类着色（一天内）
2. **抽离自定义 Layout**：替换为 `PeriodicTableLayout`
3. **接 zoomable**：加 Telephoto，处理 onClick/onLongClick 坐标 → cell 反查
4. **多选与详情页**
5. **WindowSizeClass 分档**
6. **暗色模式调色 / 无障碍**

---

## 关键外部参考

- [Grid | Jetpack Compose - 官方](https://developer.android.com/develop/ui/compose/layouts/adaptive/grid)
- [Multitouch: Panning, zooming, rotating | Jetpack Compose](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/multi-touch)
- [Telephoto - saket.github.io](https://saket.github.io/telephoto/zoomable/)
- [cheonjaeung/gridlayout-compose - BoxGrid 任意位置](https://github.com/cheonjaeung/gridlayout-compose)
- [jordond/drag-select-compose](https://github.com/jordond/drag-select-compose)
- [Use window size classes](https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes)
- [Canonical layouts (ListDetailPaneScaffold)](https://developer.android.com/develop/ui/compose/layouts/adaptive/canonical-layouts)
- [Wikipedia 元素分类配色](https://en.wikipedia.org/wiki/Help:Periodic_table_classifications_in_the_English_Wikipedia)
- [Multi-List Item Selection in Jetpack Compose - Medium](https://medium.com/make-apps-simple/multi-list-item-selection-in-jetpack-compose-301fcf375a6c)
- [chemsearch-android（最新参考实现）](https://github.com/FurtherSecrets24680/chemsearch-android)

---

## Caveats / Not Found

- 没有找到 star 数较多、纯 Compose 的开源周期表实现，技术风险点（自定义 Layout + zoomable + 多选三者协同）需要做 PoC 验证
- Telephoto 的 `onClick(Offset)` 回调中的坐标系是 content 坐标还是 viewport 坐标，需写代码确认（影响命中检测的反查公式）
- Compose `androidx.compose.foundation.layout.Grid`（不同于 LazyVerticalGrid）的最低版本要求未明确，使用前需查 release notes
- `cheonjaeung/gridlayout-compose` 是否仍在维护、是否兼容 Compose Multiplatform 最新版需在引入前确认
- Wikipedia category 着色仅作参考，最终配色需 UI 设计师定稿；推荐通过 Material Theme Builder 生成 light/dark 双套
