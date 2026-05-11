# 质量规范

> 本项目质量保障体系：**Lint + detekt 静态分析 + JUnit4/MockK/turbine 单元测试 + Compose UI 测试 + PR 质量门**。每个 PR 必须在 CI 上通过全部检查，或至少在本地 `./gradlew lint detekt test` 全部通过。

---

## 1. Lint 配置

### 1.1 Android Lint

通过 `app/build.gradle.kts` 配置：

```kotlin
android {
    lint {
        // 严重错误级别阻止构建
        isAbortOnError = true
        // 不检查过时翻译（MVP 仅中文）
        disable += "MissingTranslation"
        // 确保 Compose 可组合函数遵守命名与参数规范
        enable += "ComposableModifier"          // 强制 modifier 参数
        enable += "ComposableNaming"            // 强制 PascalCase 命名
        enable += "ComposableLambdaParameterNaming"
        // 禁止旧 API
        warning += "Deprecated"
    }
}
```

| 规则 | 说明 |
|------|------|
| `MissingTranslation` | 关闭（MVP 仅中文） |
| `ComposableModifier` | 要求所有公共 Composable 的 `modifier: Modifier` 是第一个可选参数 |
| `ComposableNaming` | 要求 Composable 函数名遵循 PascalCase |
| `Deprecated` | 对 deprecated API 给出 warning |
| `ContentDescription` | 对无描述的 Image/Icon 给出 warning（无障碍） |

### 1.2 detekt 集成

**依赖**：`io.gitlab.arturbosch.detekt` 插件（大于 1.23+），配合 `detekt-formatting` 子插件。

```kotlin
// build.gradle.kts (root)
plugins {
    id("io.gitlab.arturbosch.detekt").version("1.23.7")
}

// app/build.gradle.kts
detekt {
    config = files("${rootDir}/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    parallel = true
}
```

**推荐规则**：

| 规则集 | 关键规则 | 行为 |
|--------|---------|------|
| `comments` | `CommentOverPrivateFunction`, `UndocumentedPublicFunction` | 公共 API 必须有 KDoc 注释 |
| `complexity` | `ComplexCondition`, `CyclomaticComplexMethod(max = 15)`, `LongMethod(max = 30)` | 控制复杂度 |
| `coroutines` | `GlobalScope`, `RedundantSuspendModifier`, `SuspendFunSwallowedCancellation` | 协程安全 |
| `exceptions` | `TooGenericExceptionCaught`, `TooGenericExceptionThrown` | 异常粒度 |
| `naming` | `FunctionNaming`, `VariableNaming`, `MemberNameEqualsClassName` | 命名一致性 |
| `performance` | `SpreadOperator`, `UnnecessaryTemporary` | 性能感性 |
| `potential-bugs` | `CastToNullableType`, `DontDowncastCollectionType`, `UnreachableCode` | 防 bug |
| `style` | `ForbiddenComment`, `UnusedImports`, `UseCheckOrError`, `UseRequire` | 编码风格 |
| `formatting`（detekt-formatting） | `Indentation`, `TrailingComma`, `ParameterListWrapping`, `SpacingBetweenDeclarationsWithAnnotations` | Kotlin 格式化一致性 |

### 1.3 Pre-commit Hook（推荐）

建议通过 `gradle preCommit` task 在本地 commit 前运行：

```bash
# .husky/pre-commit 或 .git/hooks/pre-commit
#!/bin/sh
./gradlew lint detekt
if [ $? -ne 0 ]; then
  echo "Lint/Detekt 未通过，请修复后提交"
  exit 1
fi
```

---

## 2. 单元测试规范

### 2.1 测试依赖

```kotlin
// libs.versions.toml
[versions]
junit = "4.13.2"
mockk = "1.13.12"
turbine = "1.1.0"
kotlin-coroutines-test = "1.9.0"

[libraries]
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
kotlin-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlin-coroutines-test" }

// app/build.gradle.kts
dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlin.coroutines.test)
}
```

### 2.2 ViewModel 测试模板

使用 **turbine** 验证 `StateFlow` 的状态变迁，使用 **MockK** mock Repository，使用 **runTest** 控制协程。

```kotlin
// app/src/test/java/app/periodic/feature/table/TableViewModelTest.kt
package app.periodic.feature.table

import app.cash.turbine.test
import app.periodic.data.element.ElementRepository
import app.periodic.data.element.model.Element
import app.periodic.data.element.model.ElementCategory
import app.periodic.data.favorites.FavoritesRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TableViewModelTest {

    @MockK
    private lateinit var elementRepository: ElementRepository

    @MockK
    private lateinit var favoritesRepository: FavoritesRepository

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var viewModel: TableViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = testScope.runTest {
        // Arrange
        coEvery { elementRepository.loadAll() } returns Result.success(emptyList())

        // Act
        viewModel = TableViewModel(elementRepository, favoritesRepository)

        // Assert：初始状态应为 isLoading = true
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(true, initialState.isLoading)
        }
    }

    @Test
    fun `load success updates state with elements`() = testScope.runTest {
        // Arrange
        val sampleElement = Element(
            z = 1,
            symbol = "H",
            name = "Hydrogen",
            nameZh = "氢",
            atomicMass = 1.008,
            period = 1,
            group = 1,
            category = ElementCategory.ReactiveNonmetal,
            electronConfiguration = "1s1",
            density = 8.99e-5,
            melt = 14.01,
            boil = 20.28,
            electronegativity = 2.20,
            ionizationEnergies = listOf(13.5984),
            discoveredBy = "Henry Cavendish",
        )
        coEvery { elementRepository.loadAll() } returns Result.success(listOf(sampleElement))

        // Act
        viewModel = TableViewModel(elementRepository, favoritesRepository)

        // Assert
        viewModel.state.test {
            // 跳过初始 loading 态
            awaitItem()
            // 验证加载完成后的状态
            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertEquals(1, loadedState.elements.size)
            assertEquals("H", loadedState.elements[0].symbol)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `query changed updates query in state`() = testScope.runTest {
        // Arrange
        coEvery { elementRepository.loadAll() } returns Result.success(emptyList())
        viewModel = TableViewModel(elementRepository, favoritesRepository)

        // Act
        viewModel.handle(TableIntent.QueryChanged("He"))

        // Assert
        assertEquals("He", viewModel.state.value.query)
    }

    @Test
    fun `cell tapped in normal mode sends navigation effect`() = testScope.runTest {
        // Arrange
        coEvery { elementRepository.loadAll() } returns Result.success(emptyList())
        viewModel = TableViewModel(elementRepository, favoritesRepository)

        // Act & Assert
        viewModel.effect.test {
            viewModel.handle(TableIntent.CellTapped(z = 6))
            val effect = awaitItem()
            assertEquals(
                TableEffect.NavigateToDetail(z = 6),
                effect,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cell tapped in selection mode toggles selection`() = testScope.runTest {
        // Arrange
        coEvery { elementRepository.loadAll() } returns Result.success(emptyList())
        viewModel = TableViewModel(elementRepository, favoritesRepository)

        // Act：先进入多选模式（长按触发）
        viewModel.handle(TableIntent.CellLongPressed(z = 1))
        // 再点另一个元素
        viewModel.handle(TableIntent.CellTapped(z = 2))

        // Assert
        assertEquals(setOf(1, 2), viewModel.state.value.selected)
        assertEquals(true, viewModel.state.value.inSelectionMode)
    }
}
```

### 2.3 Repository 测试模板

Repository 测试重点：数据源合并逻辑、缓存命中/过期、错误传递。

```kotlin
// app/src/test/java/app/periodic/data/element/ElementRepositoryTest.kt
package app.periodic.data.element

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ElementRepositoryTest {

    @MockK
    private lateinit var dataSource: ElementDataSource

    private lateinit var repository: ElementRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = ElementRepository(dataSource)
    }

    @Test
    fun `loadAll returns elements on success`() = runTest {
        // Arrange
        coEvery { dataSource.readElements() } returns Result.success(/* sample list */)

        // Act
        val result = repository.loadAll()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `loadAll propagates error from dataSource`() = runTest {
        // Arrange
        coEvery { dataSource.readElements() } returns Result.failure(IOException("File not found"))

        // Act
        val result = repository.loadAll()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("File not found", result.exceptionOrNull()?.message)
    }
}
```

### 2.4 测试技术选型

| 关注点 | 技术选型 | 理由 |
|--------|---------|------|
| 测试框架 | JUnit4（Android 标准） | Compose 测试框架默认集成 |
| Mock 框架 | MockK | Kotlin 原生支持、协程友好、可 mock `suspend` 函数 |
| StateFlow 验证 | Turbine | 最简洁的 Flow 断言库；`awaitItem()` / `awaitComplete()` 清晰表达预期 |
| 协程测试 | `kotlinx-coroutines-test` + `runTest` + `StandardTestDispatcher` | `StandardTestDispatcher` 确保可预测执行顺序 |
| Compose UI 测试 | `compose-ui-test-junit4` + `createComposeRule()` | Google 官方测试 API |
| 参数化测试 | JUnit4 `Parameterized` 或 `@RunWith(Parameterized::class)` | 适合 element 分类/过滤的边界测试 |

---

## 3. Compose UI 测试

### 3.1 依赖

```kotlin
// app/build.gradle.kts
dependencies {
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

### 3.2 Composable 快照测试

验证 Composable 的首次渲染内容：

```kotlin
// app/src/androidTest/java/app/periodic/ui/components/ElementCardTest.kt
package app.periodic.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import app.periodic.data.element.model.Element
import app.periodic.data.element.model.ElementCategory
import app.periodic.theme.PeriodicProTheme
import org.junit.Rule
import org.junit.Test

class ElementCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun elementCard_displays_symbol_and_number() {
        val hydrogen = Element(
            z = 1, symbol = "H", name = "Hydrogen", nameZh = "氢",
            atomicMass = 1.008, period = 1, group = 1,
            category = ElementCategory.ReactiveNonmetal,
            electronConfiguration = null, density = null, melt = null, boil = null,
            electronegativity = null, ionizationEnergies = emptyList(), discoveredBy = null,
        )

        composeTestRule.setContent {
            PeriodicProTheme {
                ElementCard(element = hydrogen, onClick = {})
            }
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("H").assertIsDisplayed()
    }
}
```

### 3.3 交互测试

验证点击、输入等交互行为：

```kotlin
@Test
fun periodicSearchBar_onTextChange_triggersCallback() {
    var capturedText = ""
    composeTestRule.setContent {
        PeriodicProTheme {
            PeriodicSearchBar(
                query = "",
                onQueryChange = { capturedText = it },
            )
        }
    }

    composeTestRule.onNodeWithTag("search_text_field").performTextInput("He")
    assertEquals("He", capturedText)
}
```

### 3.4 UI 测试注意事项

- **不要**依赖精确像素值（各设备 dpi 不同）；使用语义匹配器（`hasText`、`hasContentDescription`）
- **不要**在 UI 测试中 mock ViewModel；使用真实测试数据通过 Composable 参数传入
- **使用** `semantics { contentDescription = ... }` 或 `testTag` 标记 Composable，提高测试稳健性

---

## 4. 覆盖率目标

| 层级 | 目标覆盖率（行覆盖率） | 说明 |
|------|----------------------|------|
| ViewModel 业务逻辑 | >= 80% | 所有 `handle(intent)` 分支 + `init` 数据加载 + Effect 发射 |
| Repository | >= 70% | 数据源调用 + 缓存 + 错误传播 |
| DataSource | >= 60% | 文件解析、JSON 反序列化（网络/IO 密集型可放宽） |
| UI Composable | 快照测试 100% 公共组件 | 每个公开 Composable 至少 1 个渲染测试，交互路径按需 |
| UseCase | >= 80% | 核心过滤/搜索/排序逻辑 |

> 覆盖率指南：不追求 100%，追求**关键分支全覆盖**（空值、边界值、错误路径、正常路径）。ViewModel 中的 `when (intent)` 所有分支必须覆盖。

---

## 5. PR 质量门

### 5.1 CI 检查清单

每次 PR 提交时 CI（GitHub Actions / GitLab CI）必须执行：

```yaml
# .github/workflows/quality.yml 示例
jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: 17
      - uses: gradle/actions/setup-gradle@v3
      - run: ./gradlew lint detekt
      - run: ./gradlew testDebugUnitTest
      - run: ./gradlew connectedDebugAndroidTest  # 可选，需模拟器
      - run: ./gradlew assembleDebug               # 确保编译通过
```

**通过条件**：

1. `./gradlew lint` 零 Error（Warning 不超过 10 个允许通过，但需在 PR 内说明）
2. `./gradlew detekt` 零 Error（warning 需在 PR 内说明或标注 `@Suppress` 并附理由）
3. `./gradlew testDebugUnitTest` 全部 Green（零失败 Zero Failure）
4. `./gradlew assembleDebug` 编译通过

### 5.2 Code Review Checklist

Reviewer 逐条检查：

- [ ] 代码遵循 `directory-structure.md` 包分层（新文件落点正确）
- [ ] Composable 的 `modifier: Modifier = Modifier` 是第一个可选参数
- [ ] 每个公开 Composable 至少有 1 个 `@Preview`
- [ ] ViewModel 使用 `MutableStateFlow` + `asStateFlow()`，无 `mutableStateOf`
- [ ] Intent 使用 `sealed interface`，UiState 使用 `data class`
- [ ] Flow 收集使用 `collectAsStateWithLifecycle()`，非 `collectAsState()`
- [ ] Navigation 路由使用 `@Serializable`，无字符串路由
- [ ] 没有在 LazyColumn / Grid 子项中使用 `GlassSurface`
- [ ] 适配代码使用 `currentWindowAdaptiveInfo()`，无已 deprecated 的 `calculateWindowSizeClass()`
- [ ] 无硬编码字符串（全部走 `stringResource(R.string.xxx)`）
- [ ] 无 `!!` 强制解包
- [ ] 无 `TODO` / `FIXME` / `HACK` 遗留（`ForbiddenComment` detekt 规则）
- [ ] 对应屏的 ViewModel 关键分支已补单元测试
- [ ] 新增的公共 Composable 已补快照测试（至少 1 个渲染测试）

---

## 6. 禁止模式（Forbidden Patterns）

| 模式 | 原因 | 替代方案 |
|------|------|---------|
| `collectAsState()` 替代 `collectAsStateWithLifecycle()` | 后台持续收集，浪费电量 | 强制使用 `collectAsStateWithLifecycle()` |
| `remember { ViewModel() }` 或 `remember { Repository() }` | 绕过 Koin DI，手动管理生命周期 | 使用 `koinViewModel()` |
| `var` 字段在 `data class` 中 | 破坏不可变性 | 全部用 `val`；派生值用计算属性 |
| `!!` 强制解包 | 破坏 null safety | 使用 `?.let {}` / `?:` / `requireNotNull()` |
| `String` 路由（`"detail/{z}"`） | 类型不安全，易拼错 | 使用 `@Serializable` data class 路由 |
| `calculateWindowSizeClass(activity)` | 已 deprecated，枚举比较也 deprecated | 使用 `currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(...)` |
| 在 ViewModel 中使用 `mutableStateOf()` | ViewModel 层应使用 `StateFlow`，非 Compose state | `MutableStateFlow` + `asStateFlow()` |
| `Dispatchers.Main.immediate` 硬编码在 ViewModel | 调度策略应封装在 Repository | Repository 内部使用 `withContext(Dispatchers.IO)` |
| 在 Composable 中直接调用 `Repository.loadAll()` | 破坏 UDF 单向数据流 | 经 ViewModel `handle(intent)` → ViewModel 内部调用 |
| 单元测试使用 `GlobalScope` 或 `Dispatchers.Unconfined` | 不可控，测试不稳定 | 使用 `runTest` + `StandardTestDispatcher` |
| `Modifier.blur` 期望"模糊下层" | `Modifier.blur` 只模糊自身子树 | 使用 `GlassSurface`（基于 Haze 库）|

---

## 7. 必要模式（Required Patterns）

| 模式 | 强制程度 |
|------|---------|
| 每个公开 Composable 至少 1 个 `@Preview` | 强制 |
| `modifier: Modifier = Modifier` 作为第一个可选参数 | 强制 |
| ViewModel 构造函数注入 Repository（Koin） | 强制 |
| `collectAsStateWithLifecycle()` 收集 ViewModel 的 StateFlow | 强制 |
| Intent 用 `sealed interface` | 强制 |
| UiState 用 `data class`，全部 `val` | 强制 |
| 路由用 `@Serializable` 对象 / data class | 强制 |
| 可见文本走 `stringResource(R.string.xxx)` | 强制 |
| 适配用 `currentWindowAdaptiveInfo()` | 强制 |
| CategoryColors 用 `staticCompositionLocalOf` 注入 | 推荐 |
| 关键 ViewModel / Repository 补单测 | 强制（新代码） |
| Kotlin 公共 API 显式声明返回类型 | 推荐 |

---

## Anti-patterns

1. **不要**把 lint/detekt 的 warning 当作可忽略项——至少要在 PR 中说明理由或在代码中加 `@Suppress` 标注
2. **不要**在单元测试里测试 Compose 布局细节（像素位置、颜色值）；单元测试测逻辑，UI 测试测渲染
3. **不要**为了凑覆盖率写只调不测的"空心测试"（empty test / 只调方法不 assert）；每个测试至少 assert 一个关键行为或状态变化
