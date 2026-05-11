# 状态管理规范

> 本项目状态架构：**MVVM + StateFlow + UDF（单向数据流）+ sealed Intent**。每个业务屏一个 ViewModel，由 Koin 注入。

---

## 1. 分层与职责

```
┌─────────────────────────────────────────────────────┐
│  Composable Screen (UI 层)                          │
│    val state by vm.state.collectAsStateWithLifecycle()
│    XxxContent(state, onIntent = vm::handle)         │
└──────────────────┬──────────────────────────────────┘
                   │ Intent (用户意图)
                   ▼
┌─────────────────────────────────────────────────────┐
│  ViewModel                                          │
│    private val _state = MutableStateFlow(UiState()) │
│    val state: StateFlow<UiState> = _state.asStateFlow()
│    private val _effect = Channel<Effect>()          │
│    fun handle(intent: Intent) { reduce → update }   │
└──────────────────┬──────────────────────────────────┘
                   │ 调用
                   ▼
┌─────────────────────────────────────────────────────┐
│  Repository / UseCase                               │
│    suspend fun load(): Result<T>                    │
│    fun observe(): Flow<T>                           │
└─────────────────────────────────────────────────────┘
```

| 层 | 持有 | 不能持有 |
|----|------|----------|
| UI Composable | state（只读快照） | ViewModel 引用以外的业务对象、可变状态 |
| ViewModel | StateFlow、Effect Channel、Repository（注入） | Context、View、Composable 引用 |
| Repository | DataSource、内存缓存 | ViewModel、UI 状态 |

---

## 2. 标准模板（必抄）

每个 feature 包按 4 件套实现：

### 2.1 UiState（`data class`，不可变）

```kotlin
// feature/table/TableUiState.kt
package app.periodic.feature.table

import app.periodic.data.element.model.Element
import app.periodic.data.element.model.ElementCategory

data class TableUiState(
    val elements: List<Element> = emptyList(),
    val query: String = "",
    val filterCategory: ElementCategory? = null,
    val selected: Set<Int> = emptySet(),           // 多选模式选中的原子序号
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val inSelectionMode: Boolean get() = selected.isNotEmpty()
}
```

### 2.2 Intent（`sealed interface`）

```kotlin
// feature/table/TableIntent.kt
package app.periodic.feature.table

import app.periodic.data.element.model.ElementCategory

sealed interface TableIntent {
    data class QueryChanged(val text: String) : TableIntent
    data class CategoryToggled(val category: ElementCategory?) : TableIntent
    data class CellTapped(val z: Int) : TableIntent
    data class CellLongPressed(val z: Int) : TableIntent
    data object ClearSelection : TableIntent
    data object CompareSelected : TableIntent
}
```

### 2.3 Effect（一次性副作用，导航 / Toast）

```kotlin
// feature/table/TableEffect.kt
sealed interface TableEffect {
    data class NavigateToDetail(val z: Int) : TableEffect
    data class NavigateToCompare(val ids: List<Int>) : TableEffect
    data class ShowError(val message: String) : TableEffect
}
```

### 2.4 ViewModel

```kotlin
// feature/table/TableViewModel.kt
package app.periodic.feature.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.periodic.data.element.ElementRepository
import app.periodic.data.favorites.FavoritesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TableViewModel(
    private val elements: ElementRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TableUiState(isLoading = true))
    val state: StateFlow<TableUiState> = _state.asStateFlow()

    private val _effect = Channel<TableEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            elements.loadAll()
                .onSuccess { list -> _state.update { it.copy(elements = list, isLoading = false) } }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                    _effect.send(TableEffect.ShowError(e.message ?: "Load failed"))
                }
        }
    }

    fun handle(intent: TableIntent) {
        when (intent) {
            is TableIntent.QueryChanged ->
                _state.update { it.copy(query = intent.text) }

            is TableIntent.CategoryToggled ->
                _state.update { it.copy(filterCategory = intent.category) }

            is TableIntent.CellTapped -> {
                val cur = _state.value
                if (cur.inSelectionMode) {
                    _state.update { it.copy(selected = it.selected.toggle(intent.z)) }
                } else {
                    viewModelScope.launch { _effect.send(TableEffect.NavigateToDetail(intent.z)) }
                }
            }

            is TableIntent.CellLongPressed ->
                _state.update { it.copy(selected = it.selected + intent.z) }

            TableIntent.ClearSelection ->
                _state.update { it.copy(selected = emptySet()) }

            TableIntent.CompareSelected -> {
                val ids = _state.value.selected.toList()
                viewModelScope.launch { _effect.send(TableEffect.NavigateToCompare(ids)) }
            }
        }
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> =
    if (contains(item)) this - item else this + item
```

### 2.5 Screen（Composable 入口）

```kotlin
// feature/table/TableScreen.kt
package app.periodic.feature.table

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun TableScreen(
    onNavigateDetail: (Int) -> Unit,
    onNavigateCompare: (List<Int>) -> Unit,
    vm: TableViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.inSelectionMode) { vm.handle(TableIntent.ClearSelection) }

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is TableEffect.NavigateToDetail -> onNavigateDetail(effect.z)
                is TableEffect.NavigateToCompare -> onNavigateCompare(effect.ids)
                is TableEffect.ShowError -> { /* Snackbar */ }
            }
        }
    }

    TableContent(state = state, onIntent = vm::handle)
}

@Composable
private fun TableContent(state: TableUiState, onIntent: (TableIntent) -> Unit) {
    // 纯 UI：调用 PeriodicTableLayout / CategoryFilterChips / ContextualTopAppBar 等
}
```

---

## 3. Koin 注入（不用 Hilt）

```kotlin
// di/AppModule.kt
val appModule = module {
    single { ElementDataSource(get()) }
    single { ElementRepository(get()) }
    single { FavoritesDataStore(androidContext()) }
    single { FavoritesRepository(get()) }
    single { ThemePreferenceRepository(androidContext()) }

    viewModel { HomeViewModel(get(), get()) }
    viewModel { TableViewModel(get(), get()) }
    viewModel { (z: Int) -> DetailViewModel(z, get(), get()) }  // 带参数
    viewModel { (ids: List<Int>) -> CompareViewModel(ids, get()) }
    viewModel { FavoritesViewModel(get(), get()) }
}
```

Composable 中：

```kotlin
val vm: DetailViewModel = koinViewModel { parametersOf(atomicNumber) }
```

---

## 4. 副作用（Effect）：Channel vs SharedFlow

| 选择 | 适用 |
|------|------|
| **`Channel` + `receiveAsFlow`** | 一次性事件（导航、Snackbar、Toast）。每个事件被恰好一个收集者消费 |
| `MutableSharedFlow(replay = 0)` | 多订阅者需要看到事件 |
| ❌ `StateFlow<Event?>` | **禁止**——读取后必须置空，业务复杂时容易丢事件或重复消费 |

收集方必须在 `LaunchedEffect` 中（生命周期内），不能在普通 lambda。

---

## 5. 多选模式（依据 `research/compose-periodic-table.md`）

- 选中集合通过 `UiState.selected: Set<Int>` 持有，**ViewModel 层主存**（不要塞 Composable 内的 `rememberSaveable`）；这样多选状态可被 ViewModel 配合 `SavedStateHandle` 持久化
- `inSelectionMode` 通过 `derivedStateOf` 派生
- TopAppBar 切换为 `ContextualTopAppBar`（展示选中数量 + Compare / Share / Clear actions）
- `BackHandler(enabled = state.inSelectionMode)` 拦截返回键清空选择

---

## 6. 跨屏数据传递

| 场景 | 方法 |
|------|------|
| 简单 ID | Navigation type-safe 路由参数（`@Serializable data class ElementDetailRoute(val z: Int)`） |
| 列表（如 Compare 的 ids） | 路由 data class 携带 `List<Int>`（kotlinx.serialization 支持）或独立 Repository 缓存 |
| 跨 feature 共享业务态 | 提升到 Repository 层（如 `FavoritesRepository.observe()`），各 ViewModel 订阅 |

**禁止**用 `SharedViewModel`（Activity 范围共享 ViewModel）——会破坏 feature 隔离。

---

## Anti-patterns

1. **不要**让 ViewModel 持有 `MutableState` 或调用 `mutableStateOf`——业务层只用 `StateFlow`，UI 层才用 Compose state 原语
2. **不要**用 `viewModelScope.launch(Dispatchers.Main)` 之类显式切线程；Repository 内部用 `withContext(Dispatchers.IO)` 包好，ViewModel 不感知线程
3. **不要**在 Composable 里直接调用 `Repository.load()`；必须经 ViewModel，否则 UDF 单向就不成立了
