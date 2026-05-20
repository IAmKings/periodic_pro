# 目录结构规范

> Periodic Pro 是**单 module Android 工程**：仓库根即 Android 工程根，仅有一个 `app/` module，按**包**而非 Gradle module 分层。未来如需要再拆 module，先重构包结构再动 Gradle。

---

## 顶层结构

```
periodic-pro/
├── app/                                # 唯一 module
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/<pkg>/             # Kotlin 源码（见下方包分层）
│       │   ├── assets/
│       │   │   ├── elements.json       # 118 元素英文主表（来源 Bowserinator，CC BY-SA 3.0）
│       │   │   └── elements_zh.json    # 118 元素中文映射 {number, name_zh, pinyin}
│       │   └── res/
│       │       ├── values/strings.xml  # 文案集中存放（不写硬编码字符串到 Composable）
│       │       └── drawable/           # 图标 / 字体
│       ├── test/                       # JUnit4 + MockK + turbine 单测
│       └── androidTest/                # Compose UI Test
├── design/                             # 设计稿（4 张 PNG，只读参考）
├── .trellis/                           # 任务与 spec
├── gradle/libs.versions.toml           # Version Catalog
├── settings.gradle.kts
└── build.gradle.kts
```

> **assets 双文件设计**依据：研究 `elements-dataset.md` 结论——开源数据集均不带中文，必须自建一份小映射文件，由 Repository 启动时合并。

---

## 包分层（`app/src/main/java/<pkg>/`）

```
<pkg>/
├── App.kt                       # Application 子类，注册 Koin
├── MainActivity.kt              # 单 Activity；setContent { PeriodicProTheme { PeriodicProApp() } }
│
├── di/                          # Koin 依赖注入模块
│   └── AppModule.kt             # module { ... } 单例 / viewModel 注册
│
├── theme/                       # 设计系统层（与设计图 01 Design System 一一对应）
│   ├── Color.kt                 # Primary/Secondary + CategoryPalette（10 色 element category）
│   ├── Typography.kt            # 字阶
│   ├── Shapes.kt                # M3 Shape 三档 + RoundedCornerShape token
│   ├── Dimensions.kt            # 8pt Grid 间距常量
│   ├── Elevation.kt             # 4 层 Elevation token（Shadow1/Shadow2/Floating/Modal）
│   └── PeriodicProTheme.kt      # 深浅色 ColorScheme + ThemeProvider，注入 CategoryColors / Dimensions / Elevation
│
├── data/                        # 数据层
│   ├── element/
│   │   ├── ElementDataSource.kt # 读 assets/elements.json + elements_zh.json
│   │   ├── ElementRepository.kt # 启动加载到内存；按 z / symbol / name 查询
│   │   └── model/
│   │       ├── Element.kt       # data class（含 AtomicNumber value class）
│   │       └── ElementCategory.kt  # enum（10 类）
│   ├── favorites/
│   │   ├── FavoritesDataStore.kt    # DataStore Preferences，key="favorite_ids"
│   │   └── FavoritesRepository.kt   # Flow<Set<AtomicNumber>>
│   ├── discover/
│   │   ├── DiscoverRepository.kt
│   │   └── model/
│   ├── lab/
│   │   ├── LabRepository.kt
│   │   └── model/
│   ├── learn/
│   │   ├── LearnRepository.kt
│   │   └── model/
│   ├── update/
│   │   ├── UpdateRepository.kt
│   │   ├── UpdatePreferences.kt
│   │   ├── UpdateService.kt
│   │   ├── GitHubRelease.kt
│   │   └── ApkInstaller.kt
│   └── theme/
│       └── ThemePreferenceRepository.kt  # Flow<ThemeMode> { System, Light, Dark }
│
├── ui/
│   ├── components/              # 02 Components（细粒度可复用 Composable）
│   │   ├── PeriodicButton.kt
│   │   ├── ElementCard.kt
│   │   ├── CategoryChip.kt
│   │   ├── PeriodicSearchBar.kt
│   │   └── GlassSurface.kt      # Haze 1.6.x 包装（见 component-guidelines）
│   ├── pattern/                 # 03 Patterns（粗粒度复合 UI）
│   │   ├── PropertyGrid.kt
│   │   ├── AtomCanvas.kt        # Canvas 2D 原子动画
│   │   └── EmptyState.kt
│   └── navigation/              # 全局导航
│       ├── Routes.kt            # @Serializable 路由对象 / data class（type-safe）
│       ├── PeriodicNav.kt       # 内层 NavHost；Tab 内子路由
│       └── PeriodicNavSuite.kt  # NavigationSuiteScaffold 包装，使用 currentWindowAdaptiveInfo()
│
└── feature/                     # 业务屏（每屏 4 件套 + Effect）
    ├── home/
    │   ├── HomeScreen.kt        # @Composable HomeScreen(vm = koinViewModel()) + HomeContent(state, onIntent)
    │   ├── HomeViewModel.kt
    │   ├── HomeUiState.kt       # data class
    │   └── HomeIntent.kt        # sealed interface
    ├── table/
    │   ├── TableScreen.kt
    │   ├── TableViewModel.kt
    │   ├── TableUiState.kt
    │   └── TableIntent.kt
    ├── detail/
    │   ├── DetailScreen.kt
    │   ├── DetailViewModel.kt
    │   ├── DetailUiState.kt
    │   └── DetailIntent.kt
    ├── compare/
    │   ├── CompareScreen.kt
    │   ├── CompareViewModel.kt
    │   ├── CompareUiState.kt
    │   ├── CompareIntent.kt
    │   └── CompareEffect.kt
    ├── favorites/
    │   ├── FavoritesScreen.kt
    │   ├── FavoritesViewModel.kt
    │   ├── FavoritesUiState.kt
    │   └── FavoritesIntent.kt
    ├── category/
    │   ├── CategoryScreen.kt
    │   ├── CategoryViewModel.kt
    │   ├── CategoryUiState.kt
    │   └── CategoryIntent.kt
    ├── discover/
    │   ├── DiscoverScreen.kt
    │   ├── DiscoverViewModel.kt
    │   ├── DiscoverUiState.kt
    │   ├── DiscoverIntent.kt
    │   └── DiscoverEffect.kt
    ├── learn/
    │   ├── LearnScreen.kt
    │   ├── LearnViewModel.kt
    │   ├── LearnUiState.kt
    │   ├── LearnIntent.kt
    │   └── LearnEffect.kt
    ├── lab/
    │   ├── LabScreen.kt
    │   ├── LabViewModel.kt
    │   ├── LabUiState.kt
    │   ├── LabIntent.kt
    │   └── LabEffect.kt
    ├── quiz/
    │   ├── QuizScreen.kt
    │   ├── QuizViewModel.kt
    │   ├── QuizUiState.kt
    │   └── QuizIntent.kt
    └── profile/
        ├── ProfileScreen.kt
        ├── ProfileViewModel.kt
        ├── ProfileUiState.kt
        └── ProfileIntent.kt
```

---

## 命名约定

| 类别 | 规则 | 示例 |
|------|------|------|
| 文件名 | PascalCase + `.kt` | `ElementCard.kt` |
| Composable | PascalCase + 名词或动名词 | `ElementCard`, `GlassSurface`, `PeriodicTableLayout` |
| ViewModel | `<Feature>ViewModel` | `TableViewModel` |
| UiState | `<Feature>UiState` data class | `TableUiState` |
| Intent | `<Feature>Intent` sealed interface | `TableIntent` |
| Repository | `<Domain>Repository` | `ElementRepository` |
| Route | `<Screen>Route` 或独立 `Routes.kt` 内 object/data class | `ElementDetailRoute(val z: Int)` |
| Resource | snake_case（Android 强制） | `R.string.tab_table`, `R.drawable.ic_atom` |

---

## Feature 包内 4 件套硬约定

**每个 feature 包必须有且只有 4 个核心 Kotlin 文件**：

1. `<Feature>Screen.kt` —— 入口 Composable + 纯 UI Content（`<Feature>Content(state, onIntent)`）；可以有 `@Preview`
2. `<Feature>ViewModel.kt` —— ViewModel 实现，Koin 注入
3. `<Feature>UiState.kt` —— `data class`，纯不可变
4. `<Feature>Intent.kt` —— `sealed interface`，列出该屏所有用户意图

**禁止**在 feature 包内放可复用 Composable，必须抽到 `ui/components/` 或 `ui/pattern/`。

> 详细模板见 `state-management.md`。

---

## 导航相关（`ui/navigation/`）

依据研究 `navigation-suite-scaffold.md`：

- 双层 `NavHost`：rootNav（MainActivity）管理全屏二级页面（detail / learn / lab / quiz / compare）；内层 `PeriodicNav` 管理 Tab 子路由（home / table / favorites / discover / category / profile）
- 路由全部用 `@Serializable` 对象 / data class，禁止字符串路由
- 适配代码统一用 `currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)`，**禁止**使用已 deprecated 的 `WindowWidthSizeClass.COMPACT/MEDIUM/EXPANDED` 枚举比较

---

## 代码示例：典型 Element data 模型

```kotlin
// data/element/model/Element.kt
package app.periodic.data.element.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
value class AtomicNumber(val value: Int) {
    init { require(value in 1..118) { "Atomic number out of range: $value" } }
}

@Serializable
data class Element(
    @SerialName("number") val z: Int,           // 反序列化为原始 Int，业务层再包成 AtomicNumber
    val symbol: String,
    val name: String,
    @SerialName("name_zh") val nameZh: String?,
    @SerialName("atomic_mass") val atomicMass: Double?,
    val period: Int,
    val group: Int?,                            // 镧/锕系无 group
    val category: ElementCategory,
    @SerialName("electron_configuration") val electronConfiguration: String?,
    val density: Double?,
    val melt: Double?,
    val boil: Double?,
    @SerialName("electronegativity_pauling") val electronegativity: Double?,
    @SerialName("ionization_energies") val ionizationEnergies: List<Double> = emptyList(),
    @SerialName("discovered_by") val discoveredBy: String?,
) {
    val atomicNumber: AtomicNumber get() = AtomicNumber(z)
}
```

---

## Anti-patterns

1. **不要**新建 Gradle module（如 `:core`、`:feature-table`）。MVP 维持单 module，编译快、起步轻
2. **不要**在 `ui/components/` 放业务态（ViewModel、Repository 引用）；组件必须 stateless，状态由 feature 屏 hoist 进来
3. **不要**把 `elements.json` 放进 `res/raw/`；它是大数据资产，应在 `assets/` 下，由 Repository 通过 `AssetManager.open()` 读取
