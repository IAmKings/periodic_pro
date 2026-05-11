# Research: Material 3 NavigationSuiteScaffold + WindowSizeClass 自适应导航

- **Query**: NavigationSuiteScaffold + WindowSizeClass 在手机/平板/横屏折叠下的自适应导航最佳实践
- **Scope**: external（官方文档 + Now in Android 源码 + 社区方案）
- **Date**: 2026-05-10
- **Output**: `.trellis/tasks/05-10-compose-app-bootstrap/research/navigation-suite-scaffold.md`

---

## 1. 依赖引入

### 1.1 NavigationSuiteScaffold 库

```kotlin
// libs.versions.toml / build.gradle.kts
implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
```

注意 artifact 名称：库归属 `androidx.compose.material3` 组，但 package 是 `androidx.compose.material3.adaptive.navigationsuite`。文档（developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation）使用此坐标，1.4.0 起 `rememberNavigationSuiteScaffoldState` 可用。

### 1.2 WindowSizeClass 引入方式（重要：推荐方案已变化）

| 方案 | Package | 状态 |
|---|---|---|
| 旧方案 A | `androidx.compose.material3.windowsizeclass.calculateWindowSizeClass()` | 仍可用，但需要 `@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)`，且 `WindowWidthSizeClass.COMPACT/MEDIUM/EXPANDED` 在 androidx.window 1.4.0 中被标记为 deprecated |
| 旧方案 B | `androidx.window.core.layout.WindowSizeClass.compute()` | 1.4.0 deprecated（替代为 `computeWindowSizeClass`），且 `WindowWidthSizeClass`/`WindowHeightSizeClass` 弃用 |
| **推荐方案** | `androidx.compose.material3.adaptive.currentWindowAdaptiveInfo()` → `WindowAdaptiveInfo.windowSizeClass`（类型为 `androidx.window.core.layout.WindowSizeClass`，但用 `isWidthAtLeastBreakpoint(...)` 等新 API） | 当前 Google 官方在所有 adaptive 文档中使用此入口 |

```kotlin
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

val adaptiveInfo = currentWindowAdaptiveInfo()
val sizeClass = adaptiveInfo.windowSizeClass
val isExpanded = sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
val isMedium = sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
```

WindowManager 1.5 还引入了 Large（≥1200dp）和 Extra-large breakpoint，需通过 `currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)` 显式启用（参考：Jetpack WindowManager 1.5 stable 博客）。

---

## 2. NavigationSuiteScaffold API

### 2.1 默认行为映射

| WindowSizeClass | 默认 NavigationSuiteType |
|---|---|
| width compact 或 height compact（含桌面手机/横屏折叠主屏） | `NavigationBar` (BottomBar) |
| width medium / 不属于 compact 也不属于 expanded | `NavigationRail` |
| width expanded（≥840dp 横屏平板 / 大屏折叠展开） | 默认仍为 `NavigationRail`；**如果想要 PermanentDrawer 必须显式覆写** |

> 关键点：默认行为不会自动给 expanded 用 Drawer。Now in Android（PR #942）就是手动用 `customNavSuiteType` 在 ≥1240dp 时切换为 `NavigationDrawer`。Material 文档示例同样使用覆写：

```kotlin
val customNavSuiteType = with(currentWindowAdaptiveInfo()) {
    if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        NavigationSuiteType.NavigationDrawer
    } else {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(this)
    }
}
```

`NavigationSuiteScaffoldDefaults` 提供两个工具函数：
- `calculateFromAdaptiveInfo(adaptiveInfo)` — 标准映射（含上述默认表）。
- `navigationSuiteType(adaptiveInfo)` — 推荐使用，扩展支持 `WideNavigationRailCollapsed/Expanded`、`ShortNavigationBarMedium` 等新 type（Material spec）。

### 2.2 主入口 API

```kotlin
@Composable
fun NavigationSuiteScaffold(
    navigationItems: NavigationSuiteScope.() -> Unit,        // 推荐参数（新 API）
    modifier: Modifier = Modifier,
    navigationSuiteType: NavigationSuiteType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo()),
    navigationSuiteColors: NavigationSuiteColors = NavigationSuiteDefaults.colors(),
    containerColor: Color = NavigationSuiteScaffoldDefaults.containerColor,
    contentColor: Color = NavigationSuiteScaffoldDefaults.contentColor,
    state: NavigationSuiteScaffoldState = rememberNavigationSuiteScaffoldState(),
    primaryActionContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
)
```

旧重载使用 `navigationSuiteItems`（NavigationSuiteScope.item DSL）+ `layoutType` 参数；新重载使用 `navigationItems`（接受 `NavigationSuiteItem` composable）+ `navigationSuiteType` 参数。**官方推荐迁移到 `NavigationSuiteItem` + `navigationItems`**（见 NavigationSuiteScaffold API 参考的 deprecation note）。

### 2.3 NavigationSuiteScope DSL（旧 API，仍广泛使用）

```kotlin
NavigationSuiteScaffold(
    navigationSuiteItems = {
        AppDestinations.entries.forEach { dest ->
            item(
                selected = dest == currentDestination,
                onClick = { /* navigate */ },
                icon = { Icon(dest.icon, contentDescription = stringResource(dest.label)) },
                label = { Text(stringResource(dest.label)) },
                badge = { /* optional Badge */ },
            )
        }
    },
    layoutType = customNavSuiteType,
) {
    // content slot
}
```

### 2.4 NavigationSuiteScaffoldState（控制显隐）

```kotlin
val state = rememberNavigationSuiteScaffoldState() // initialValue = Visible
scope.launch { state.toggle() }   // 隐藏/显示
scope.launch { state.hide() }
scope.launch { state.show() }
```

适用于全屏沉浸场景（视频播放页）希望临时隐藏导航。

---

## 3. 与 Navigation Compose 协同

### 3.1 推荐结构（Now in Android 模式）

content slot 内放 `NavHost`，selected 状态通过 `navController.currentBackStackEntryAsState()` 推导：

```kotlin
val navController = rememberNavController()
val currentBackStackEntry by navController.currentBackStackEntryAsState()
val currentDestination = currentBackStackEntry?.destination

NavigationSuiteScaffold(
    navigationSuiteItems = {
        topLevelDestinations.forEach { dest ->
            val selected = currentDestination?.hierarchy?.any { it.hasRoute(dest.route::class) } == true
            item(
                selected = selected,
                onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(dest.icon, null) },
                label = { Text(stringResource(dest.label)) },
            )
        }
    },
    layoutType = customNavSuiteType,
) {
    NavHost(
        navController = navController,
        startDestination = PeriodicTableRoute,
    ) {
        composable<PeriodicTableRoute> { /* ... */ }
        composable<DetailRoute> { /* ... */ }
        // …
    }
}
```

`hierarchy.any { it.hasRoute(...) }` 模式是官方导航文档与 NiA 项目的标准写法，确保进入 Tab 内部子页面时父 Tab 仍高亮。

### 3.2 多 Back Stack（嵌套 Tab 内独立栈）

底部 Tab 切换时，**必须**同时设置 `popUpTo + saveState + launchSingleTop + restoreState`，由 Navigation Compose 自动维护每个 Tab 的独立 back stack：

```kotlin
navController.navigate(targetTabRoute) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true            // 保存当前 Tab 整条栈
    }
    launchSingleTop = true          // 同 Tab 重复点击不重复入栈
    restoreState = true             // 恢复目标 Tab 之前保存的栈
}
```

参考：`developer.android.com/guide/navigation/backstack/multi-back-stacks`。这是当前 Navigation 2.x 的官方多栈方案。**不要**使用 LaunchedEffect + selectedTab index 触发 navigate（那种写法会丢失 back 行为，是常见反模式）。

### 3.3 嵌套图（每个 Tab 一个 nested graph）

每个 Tab 用 `navigation { ... }` 块定义自己的子图，子图内部包含该 Tab 所有页面（list/detail/settings）：

```kotlin
NavHost(navController, startDestination = PeriodicTableGraph) {
    navigation<PeriodicTableGraph>(startDestination = PeriodicTableHome) {
        composable<PeriodicTableHome> { /* … */ }
        composable<ElementDetail> { /* … */ }
    }
    navigation<MoleculeGraph>(startDestination = MoleculeHome) { /* … */ }
    navigation<SettingsGraph>(startDestination = SettingsHome) { /* … */ }
}
```

NiA 的 `interestsNavGraph`、`forYouNavGraph`、`bookmarksNavGraph` 都遵循此模式（main 分支：`feature/<name>/src/main/kotlin/.../navigation/<Name>Navigation.kt`）。

### 3.4 selected 判定

```kotlin
fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any { it.hasRoute(route) } == true

val selected = currentDestination.isRouteInHierarchy(dest.baseRoute)
```

NiA `NiaApp.kt` 直接使用此扩展函数（见 NiaApp main 分支早期版本）。

---

## 4. 状态保留（旋转、折叠、平板）

| 状态层级 | 推荐 API | 说明 |
|---|---|---|
| Composable 局部 UI 状态（滚动位置、selectedTab、对话框开关） | `rememberSaveable { ... }` 或 `rememberSerializable`（带 KTX serialization） | 自动用 Bundle 跨配置变化与进程死亡 |
| 屏幕业务状态（选中元素 id、查询条件） | `ViewModel` + `SavedStateHandle` | 配置变化由 ViewModel 直接保留；进程死亡靠 SavedStateHandle 复活 |
| 多 Tab 栈状态 | Navigation Compose 多栈 API（saveState / restoreState） | 见 §3.2 |
| ViewModel 作用域 | 默认 NavBackStackEntry → 该 destination 销毁即清；想跨 destination 共享则 scope 到 nested graph 的 NavBackStackEntry | `hiltViewModel(navController.getBackStackEntry(GraphRoute))` |
| 折叠展开（layout pass 重新执行，非配置变化） | 同上；额外注意 `MovableContent` 用于在不同布局位置间迁移已记忆值 | 见 "State lifespans in Compose" |

关键陷阱：**改变 composable 在层级中的位置会丢失 remember 值**。Adaptive 布局应"改逻辑而不是改位置"，必要时用 `MovableContent` 包裹有状态子树（官方 state-lifespans 文档明确警告）。

---

## 5. 平板双面板（list-detail）

### 5.1 库分层

```kotlin
implementation("androidx.compose.material3.adaptive:adaptive")             // HingeInfo, Posture
implementation("androidx.compose.material3.adaptive:adaptive-layout")      // ListDetailPaneScaffold
implementation("androidx.compose.material3.adaptive:adaptive-navigation")  // NavigableListDetailPaneScaffold
```

### 5.2 何时启用双面板

依据 `ListDetailSceneStrategy` / `NavigableListDetailPaneScaffold` 的内部 directive：默认在 `WIDTH_DP_MEDIUM_LOWER_BOUND` 即可启用 list+detail 并排（具体阈值由 `PaneScaffoldDirective` 控制，可自定义 `calculatePaneScaffoldDirective`）。

### 5.3 推荐 API：`NavigableListDetailPaneScaffold`

不要手写。`NavigableListDetailPaneScaffold` 自动处理：
- 双面板/单面板切换
- 谓测式后退动画（predictive back）
- ThreePaneScaffoldNavigator 的列表/详情切换

```kotlin
val navigator = rememberListDetailPaneScaffoldNavigator<ElementId>()
NavigableListDetailPaneScaffold(
    navigator = navigator,
    listPane = { AnimatedPane { ListContent(onItemClick = { id ->
        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id) }
    }) } },
    detailPane = { AnimatedPane { DetailContent(navigator.currentDestination?.contentKey) } },
)
```

### 5.4 与外层 NavigationSuiteScaffold 嵌套

外层 `NavigationSuiteScaffold` 选 Tab，Tab 内某个 destination（如 PeriodicTable）可使用 `NavigableListDetailPaneScaffold` 在 medium/expanded 宽度展示 list-detail。NiA PR #1380 揭露过坑：嵌套 NavHost 与 `AnimatedPane` 协同时，list-only 状态下不能直接 navigate，要靠 `key()` 重建嵌套 NavHost，或者直接使用 `NavigableListDetailPaneScaffold` 内置的导航器避免该问题。

### 5.5 Navigation 3 + ListDetailSceneStrategy（前瞻）

NiA 已迁移到 Navigation 3（PR #2003），用 `rememberListDetailSceneStrategy<NavKey>()` + `NavDisplay`，元数据 `listPane()/detailPane()/extraPane()` 标记 NavEntry 归属。当前若团队用稳定的 Navigation Compose 2.x，仍走 `NavigableListDetailPaneScaffold` 路线。

---

## 6. Now in Android 参考文件清单

主分支已迁到 Navigation 3（commits 自 2025-11-19 起），但 PR #1373/#942/#1380 commit 上仍可看到 NavigationSuiteScaffold 在 Navigation 2 时代的成熟接入。建议钉住下列引用点：

| 关注点 | 路径 | 说明 |
|---|---|---|
| App 入口 / NavigationSuiteScaffold 装配 | `app/src/main/kotlin/com/google/samples/apps/nowinandroid/ui/NiaApp.kt` | `NiaNavigationSuiteScaffold` + items + content slot 接入 |
| AppState（NavController + WindowAdaptiveInfo + topLevel 状态） | `app/src/main/kotlin/com/google/samples/apps/nowinandroid/ui/NiaAppState.kt` | `currentTopLevelDestination`、`navigateToTopLevelDestination`（saveState/restoreState）、`isRouteInHierarchy` |
| 设计系统封装的 NavigationSuiteScaffold | `core/designsystem/src/main/kotlin/com/google/samples/apps/nowinandroid/core/designsystem/component/Navigation.kt` | NiA 自定义颜色、主题包装的 `NiaNavigationSuiteScaffold`，可作品牌化模板 |
| TopLevelDestination 枚举 | `app/src/main/kotlin/com/google/samples/apps/nowinandroid/navigation/TopLevelDestination.kt` | route + iconPair + label 的标准定义 |
| NavHost 主图 | `app/src/main/kotlin/com/google/samples/apps/nowinandroid/navigation/NiaNavHost.kt` | 顶层 NavHost + 各 feature 的 nested graph 拼装 |
| Feature 子图样例 | `feature/interests/src/main/kotlin/.../navigation/InterestsNavigation.kt` | 包含 `NavigableListDetailPaneScaffold` 接入（PR #1380 后） |
| WindowSizeClass 接入位置 | `MainActivity.kt` 调用 `currentWindowAdaptiveInfo()` 传入 `NiaApp` | Activity 不再用 `calculateWindowSizeClass(activity)` |

> 直接参考 main 之前最后一个 Nav2 提交：`e997459f292058ade39368f9ecbfd4ed917e55b1`（即 PR #1373 落地后），路径 `app/src/main/java/com/google/samples/apps/nowinandroid/ui/NiaApp.kt`。

---

## 7. 完整代码骨架

```kotlin
// PeriodicProApp.kt
package app.periodic.ui

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

// 1. 顶层目的地
enum class TopLevelDestination(
    val baseRoute: KClass<*>,
    val startRoute: Any,
    val iconRes: Int,
    val labelRes: Int,
) {
    PeriodicTable(PeriodicTableGraph::class, PeriodicTableHome, R.drawable.ic_table, R.string.tab_table),
    Molecules(MoleculeGraph::class, MoleculeHome, R.drawable.ic_molecule, R.string.tab_molecule),
    Settings(SettingsGraph::class, SettingsHome, R.drawable.ic_settings, R.string.tab_settings),
}

// 2. Routes（Navigation Compose type-safe）
@Serializable object PeriodicTableGraph
@Serializable object PeriodicTableHome
@Serializable data class ElementDetail(val z: Int)

@Serializable object MoleculeGraph
@Serializable object MoleculeHome

@Serializable object SettingsGraph
@Serializable object SettingsHome

// 3. App 入口
@Composable
fun PeriodicProApp(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = currentBackStackEntry?.destination

    val customNavSuiteType = with(windowAdaptiveInfo) {
        if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            NavigationSuiteType.NavigationDrawer
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(this)
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { dest ->
                val selected = currentDestination
                    ?.hierarchy
                    ?.any { it.hasRoute(dest.baseRoute) } == true
                item(
                    selected = selected,
                    onClick = {
                        navController.navigate(dest.startRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(painterResource(dest.iconRes), contentDescription = null) },
                    label = { Text(stringResource(dest.labelRes)) },
                )
            }
        },
        layoutType = customNavSuiteType,
    ) {
        NavHost(
            navController = navController,
            startDestination = PeriodicTableGraph,
        ) {
            navigation<PeriodicTableGraph>(startDestination = PeriodicTableHome) {
                composable<PeriodicTableHome> { /* PeriodicTableScreen(...) */ }
                composable<ElementDetail> { entry ->
                    val args = entry.toRoute<ElementDetail>()
                    /* ElementDetailScreen(args.z) */
                }
            }
            navigation<MoleculeGraph>(startDestination = MoleculeHome) {
                composable<MoleculeHome> { /* … */ }
            }
            navigation<SettingsGraph>(startDestination = SettingsHome) {
                composable<SettingsHome> { /* … */ }
            }
        }
    }
}
```

要点回顾：
- `currentWindowAdaptiveInfo()` 取代旧的 `calculateWindowSizeClass(activity)`。
- 显式覆写 `layoutType` 让 expanded 走 Drawer。
- 选中态用 `hierarchy.any { hasRoute(...) }`，子页面内仍能高亮 Tab。
- Tab 切换的 4 件套：`popUpTo(start) { saveState }` + `launchSingleTop` + `restoreState`。
- 每个 Tab 一个 nested `navigation { }` 子图，独立 back stack。

---

## 8. Caveats / 待确认

- `NavigationSuiteScaffold` 在 1.4.x 起推荐使用 `navigationItems` + `NavigationSuiteItem` 组合，而旧的 `navigationSuiteItems = { item(...) }` DSL 仍工作但被弱化。新项目可优先用新 API。
- `WindowWidthSizeClass.COMPACT/MEDIUM/EXPANDED` 在 androidx.window 1.4.0 已 deprecated，请避免再使用枚举比较；统一用 `isWidthAtLeastBreakpoint(...)` / `isAtLeastBreakpoint(...)`。
- Now in Android `main` 分支自 2025-11 起迁移到 Navigation 3（`NavDisplay` + `rememberNavBackStack`），NavigationSuiteScaffold 仍保留但内部已无 NavController。**若本项目仍用 Navigation Compose 2.x，请参考 NiA commit `e997459` 时代的写法**，不要照抄当前 main。
- `NavigableListDetailPaneScaffold` 与外层 NavHost 嵌套有过崩溃问题（PR #1380），稳妥做法：把 list-detail 这一组路由整段塞进 list-detail 的内部 navigator，外层 NavHost 只持有"该 Tab 的入口路由"。
- Compose Material3 1.4.0 的 `NavigationSuiteType` 新增了 `WideNavigationRailCollapsed/Expanded`、`ShortNavigationBarMedium` 等 type；如果团队需要这些，应使用 `NavigationSuiteScaffoldDefaults.navigationSuiteType(...)` 而不是 `calculateFromAdaptiveInfo(...)`。
- 平板 list-detail 模式下，detail 端的 ViewModel 作用域要小心：通常推荐 scope 到 detail destination 的 NavBackStackEntry，避免列表 ViewModel 与详情 ViewModel 互相污染。
- 折叠设备 tabletop posture 在默认行为里被识别为 "compact"（走 BottomBar），如果产品想在 tabletop 显示 NavigationRail，需要自定义 `customNavSuiteType` 检查 `adaptiveInfo.windowPosture`。

---

## 9. External References

- Build adaptive navigation（官方）— https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation
- NavigationSuiteScaffold API — https://developer.android.com/reference/kotlin/androidx/compose/material3/adaptive/navigationsuite/NavigationSuiteScaffold
- NavigationSuiteScaffoldDefaults — https://developer.android.com/reference/kotlin/androidx/compose/material3/adaptive/navigationsuite/NavigationSuiteScaffoldDefaults
- Sample 源码（AOSP） — https://android.googlesource.com/platform/frameworks/support/+/HEAD/compose/material3/material3-adaptive-navigation-suite/samples/src/main/java/androidx/compose/material3/adaptive/navigationsuite/samples/NavigationSuiteScaffoldSamples.kt
- Multiple back stacks — https://developer.android.com/guide/navigation/backstack/multi-back-stacks
- Navigation with Compose — https://developer.android.com/develop/ui/compose/navigation
- Build a list-detail layout — https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail
- NavigableListDetailPaneScaffold API — https://developer.android.com/reference/kotlin/androidx/compose/material3/adaptive/navigation/NavigableListDetailPaneScaffold
- WindowSizeClass（androidx.window）— https://developer.android.com/reference/androidx/window/core/layout/WindowSizeClass
- Jetpack WindowManager 1.5 stable 博客 — https://developer.android.com/blog/posts/jetpack-window-manager-1-5-is-stable
- State lifespans in Compose — https://developer.android.com/develop/ui/compose/state-lifespans
- NiA PR #942（首次接入 NavigationSuiteScaffold）— https://github.com/android/nowinandroid/pull/942
- NiA PR #1373（重启 NavigationSuiteScaffold）— https://github.com/android/nowinandroid/pull/1373
- NiA PR #1380（嵌套 nav + AnimatedPane 修复）— https://github.com/android/nowinandroid/pull/1380
- NiA PR #2003（迁移到 Navigation 3，前瞻参考）— https://github.com/android/nowinandroid/pull/2003
- NiAApp.kt（Nav2 时代快照）— https://github.com/android/nowinandroid/blob/e997459f292058ade39368f9ecbfd4ed917e55b1/app/src/main/java/com/google/samples/apps/nowinandroid/ui/NiaApp.kt
