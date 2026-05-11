# Composable 局部状态规范

> 本项目**不使用 React Hooks**——文件名保留 `hook-guidelines.md` 仅为兼容 Trellis 目录结构。实际内容讲 **Compose 局部状态原语**：`remember{}` / `rememberSaveable{}` / `derivedStateOf` / `produceState` / `collectAsStateWithLifecycle()`。

业务屏级状态见 [state-management.md](./state-management.md)，本文档只覆盖 Composable 内部的轻量状态。

---

## 决策速查

| 场景 | 用什么 |
|------|--------|
| Composable 内部短暂 UI 状态（菜单展开、动画进度） | `remember { mutableStateOf(...) }` |
| 同上但需要在屏幕旋转 / 进程死亡后恢复 | `rememberSaveable { mutableStateOf(...) }` |
| 派生 / 过滤计算（依赖其他 state，避免每次重组都重算） | `remember { derivedStateOf { ... } }` |
| 从挂起 / 回调 / 非 Compose 数据源生产 State | `produceState(initialValue) { ... }` |
| 从 ViewModel 的 `StateFlow` 收集 | `collectAsStateWithLifecycle()`（**强制**，不要用 `collectAsState()`） |
| 副作用启动一次 | `LaunchedEffect(key) { ... }` |
| Composable 销毁时清理 | `DisposableEffect(key) { onDispose { ... } }` |

---

## 1. `remember { mutableStateOf(...) }`

最常用，保存在 Composition 中，重组保留，**配置变化（旋转）丢失**。

```kotlin
@Composable
fun CategoryFilterChips(
    selected: ElementCategory?,
    onSelect: (ElementCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // UI 私有：是否折叠为 "更多" 按钮
    var expanded by remember { mutableStateOf(false) }

    Row(modifier = modifier) {
        // ...
    }
}
```

---

## 2. `rememberSaveable`

需要跨配置变化保留时用。仅支持可放进 `Bundle` 的类型，自定义类型需提供 `Saver`。

```kotlin
@Composable
fun PeriodicTableScreen(/* ... */) {
    // 多选模式集合：旋转屏幕后保留
    var selected by rememberSaveable(
        stateSaver = SetIntSaver,
    ) { mutableStateOf(emptySet<Int>()) }

    val inSelectionMode = selected.isNotEmpty()
    // ...
}

val SetIntSaver = listSaver<MutableState<Set<Int>>, Int>(
    save = { it.value.toList() },
    restore = { mutableStateOf(it.toSet()) },
)
```

> **业务态优先放 ViewModel + SavedStateHandle**。`rememberSaveable` 只用于纯 UI 状态。

---

## 3. `derivedStateOf`

当一个 state **依赖于**另一些 state 的派生计算，且派生值变化频率远低于依赖值时使用——避免每次重组都重算。

```kotlin
@Composable
fun TableContent(state: TableUiState, onIntent: (TableIntent) -> Unit) {
    // 元素过滤计算依赖 query 与 filterCategory
    val visibleElements by remember(state.elements) {
        derivedStateOf {
            state.elements.filter { e ->
                (state.filterCategory == null || e.category == state.filterCategory) &&
                    (state.query.isBlank() || e.matches(state.query))
            }
        }
    }
    PeriodicTableLayout(elements = visibleElements, /* ... */)
}
```

**何时不要用**：派生只是简单 `state.x + state.y`，没必要——直接读，开销可忽略。

---

## 4. `produceState`

从挂起函数 / 回调 / 非 Compose 数据源生产 `State<T>`，并自动绑定 Composition 生命周期。

```kotlin
@Composable
fun rememberElementBitmap(z: Int): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, key1 = z) {
        value = withContext(Dispatchers.IO) {
            loadBohrModelBitmap(context, z)
        }
    }
}
```

> 业务屏的数据流应在 ViewModel 里完成，不要在 Composable 用 `produceState` 直接读 Repository——会破坏 UDF。

---

## 5. `collectAsStateWithLifecycle()` 强制使用

```kotlin
// ❌ 错误：后台/锁屏期间仍持续收集，浪费电
@Composable
fun TableScreen(vm: TableViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    // ...
}

// ✅ 正确：仅在 STARTED 时收集
@Composable
fun TableScreen(vm: TableViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    TableContent(state = state, onIntent = vm::handle)
}
```

依赖：`androidx.lifecycle:lifecycle-runtime-compose`。

---

## 6. `LaunchedEffect` 与 `DisposableEffect`

**`LaunchedEffect(key)`**：在进入组合或 `key` 变化时启动协程；离开组合自动取消。

```kotlin
@Composable
fun DetailScreen(vm: DetailViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    // 监听一次性副作用：导航/Snackbar
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is DetailEffect.ShowError -> snackbar.showSnackbar(effect.message)
            }
        }
    }
    // ...
}
```

**`DisposableEffect`**：需要在离开组合时清理（解绑监听、停止定时器）。

```kotlin
@Composable
fun AtomCanvas(z: Int, modifier: Modifier = Modifier) {
    DisposableEffect(z) {
        val animator = AtomAnimator(z).also { it.start() }
        onDispose { animator.stop() }
    }
    // 绘制...
}
```

---

## 7. Key 选择陷阱

`LaunchedEffect(key)`、`remember(key)` 的 key 决定**何时重新启动 / 重新计算**：

- `Unit` 或 `true` —— 进入组合一次，永不重启
- 业务 ID（如 `elementId`）—— ID 变化时重启
- **不要**用对象引用作 key（如整个 `state`），每次重组都是新引用，会无限循环

---

## Anti-patterns

1. **不要**在 Composable 顶层直接 `remember { ViewModel() }` 或 `remember { Repository() }`——业务对象通过 Koin 注入 ViewModel，再由 Composable `koinViewModel()` 拿
2. **不要**在 `LaunchedEffect(Unit)` 里发起一次性查询然后写回本地 state——这是把业务逻辑藏在 UI 层。改放 ViewModel `init {}`
3. **不要**用 `collectAsState()` 而非 `collectAsStateWithLifecycle()`——前者在 App 退到后台时仍在收集，电量与无谓重组双输
