# Kotlin 类型安全与 idiom 规范

> 本项目用 **Kotlin 2.x**，启用 `-Xexplicit-api=strict` 推荐（公共 API 必须显式可见性与返回类型）。本文档约束 Kotlin 类型层面的实践。

---

## 1. `sealed interface` / `sealed class`

用于**封闭层级**：UI Intent、网络结果、UI 状态等"已知的全集"。优先用 `sealed interface`（更灵活，可多实现）。

```kotlin
sealed interface DetailIntent {
    data class Load(val z: Int) : DetailIntent
    data object ToggleFavorite : DetailIntent
    data object Back : DetailIntent
}

// 配合 when 表达式穷尽，编译器保证不漏分支
fun reduce(intent: DetailIntent): DetailUiState = when (intent) {
    is DetailIntent.Load -> /* ... */
    DetailIntent.ToggleFavorite -> /* ... */
    DetailIntent.Back -> /* ... */
}
```

---

## 2. `data class`：不可变状态载体

- UiState、网络 DTO、领域模型一律 `data class`
- 字段使用 `val`，**不允许 `var`**
- 派生字段用计算属性（`val foo get() = ...`），不要存储

```kotlin
data class DetailUiState(
    val element: Element? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
) {
    val isReady: Boolean get() = element != null && !isLoading
}
```

---

## 3. `@JvmInline value class`：防 ID 混用

不同语义的 `Int`/`String` 包成 value class，编译时就能拦住调错参数。

```kotlin
@JvmInline
value class AtomicNumber(val value: Int) {
    init { require(value in 1..118) { "Atomic number must be 1..118, got $value" } }
}

@JvmInline
value class ElementSymbol(val value: String) {
    init { require(value.length in 1..3) { "Invalid symbol: $value" } }
}

// 强类型签名，无法把 Symbol 当 AtomicNumber 传错
fun findByAtomicNumber(z: AtomicNumber): Element? { /* ... */ }
fun findBySymbol(s: ElementSymbol): Element? { /* ... */ }
```

⚠️ value class 与 `@Serializable` 协作有些坑（默认无法直接序列化为对象字段的原始值），需要时给一个 `KSerializer`，或者在边界处用原始 `Int`，业务层再包装。

---

## 4. Coroutines 基础

| 场景 | 用法 |
|------|------|
| ViewModel 启动业务 | `viewModelScope.launch { ... }` |
| Repository 暴露挂起函数 | `suspend fun load(): Result<List<Element>>` |
| Repository 暴露持续观察 | `fun observe(): Flow<...>` |
| 把回调改造为挂起 | `suspendCancellableCoroutine` |
| 数据流操作符 | `map / filter / combine / flatMapLatest`（按需） |
| 切线程 | Repository 内 `withContext(Dispatchers.IO) { ... }`，**ViewModel 不显式切线程** |

```kotlin
class ElementRepository(private val ds: ElementDataSource) {
    private var cached: List<Element>? = null

    suspend fun loadAll(): Result<List<Element>> = withContext(Dispatchers.IO) {
        runCatching {
            cached ?: ds.readJson().also { cached = it }
        }
    }

    fun observeBySymbol(symbol: String): Flow<Element?> = flow {
        emit(loadAll().getOrNull()?.firstOrNull { it.symbol == symbol })
    }
}
```

---

## 5. `Result<T>` 错误模型

Repository 返回 `Result<T>`（kotlin stdlib），ViewModel 用 `.onSuccess` / `.onFailure` 处理。

```kotlin
viewModelScope.launch {
    elements.loadAll()
        .onSuccess { list -> _state.update { it.copy(elements = list, isLoading = false) } }
        .onFailure { e ->
            _state.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
}
```

> 不引入第三方 `Either` / `Outcome` 类型——`Result` 标准库已够用且与 coroutines 良好兼容。

---

## 6. `@Serializable` 路由（type-safe Navigation）

依据研究 `navigation-suite-scaffold.md`，使用 **Navigation Compose 2.8+ 的 type-safe 路由**：

```kotlin
// ui/navigation/Routes.kt
package app.periodic.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object PeriodicTableGraph
@Serializable object PeriodicTableHome
@Serializable data class ElementDetailRoute(val z: Int)
@Serializable data class CompareRoute(val ids: List<Int>)

@Serializable object MoleculeGraph
@Serializable object SettingsGraph
@Serializable object FavoritesRoute
@Serializable object HomeRoute
```

NavHost：

```kotlin
NavHost(navController, startDestination = HomeRoute) {
    composable<HomeRoute> { HomeScreen(/* ... */) }

    navigation<PeriodicTableGraph>(startDestination = PeriodicTableHome) {
        composable<PeriodicTableHome> { TableScreen(/* ... */) }
        composable<ElementDetailRoute> { entry ->
            val args: ElementDetailRoute = entry.toRoute()
            DetailScreen(z = args.z)
        }
        composable<CompareRoute> { entry ->
            val args: CompareRoute = entry.toRoute()
            CompareScreen(ids = args.ids)
        }
    }
}

// 跳转
navController.navigate(ElementDetailRoute(z = 6))
```

**禁止**使用旧的字符串路由（`"detail/{z}"` + `navArgument(...)`）。

---

## 7. Null 安全

- 公共 API 默认非空；可空时**显式** `?`
- 使用 `requireNotNull(x) { "msg" }` / `checkNotNull` 在边界处校验
- **禁止** `!!` 强制解包（lint 应警告），破坏 null 安全
- `Element?` 等可空数据流，UI 层用 `state.element?.let { ... } ?: EmptyState()`

---

## 8. 集合不可变性

| 用 | 不用 |
|------|------|
| `List<T>` (read-only) | `MutableList<T>` 作为函数返回值 |
| `Set<T>` | `MutableSet<T>` 跨层传递 |
| `kotlinx.collections.immutable.ImmutableList<T>`（重组优化） | 频繁重组场景的普通 `List` |

```kotlin
data class TableUiState(
    val elements: ImmutableList<Element> = persistentListOf(),
)
```

---

## 9. `enum class` + 自定义 `KSerializer`

当 JSON 字段值为字符串（如 `"alkali-metal"`）需要映射为强类型枚举时，使用自定义 `KSerializer`：

```kotlin
// data/element/model/Category.kt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class Category(val id: String, val displayName: String) {
    ALKALI_METAL("alkali-metal", "碱金属"),
    ALKALINE_EARTH("alkaline-earth", "碱土金属"),
    // ... 10 values total
}

object CategorySerializer : KSerializer<Category> {
    override val descriptor = PrimitiveSerialDescriptor("Category", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Category) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: Decoder): Category {
        val id = decoder.decodeString()
        return Category.entries.firstOrNull { it.id == id }
            ?: error("Unknown category: $id")
    }
}

// 使用
@Serializable
data class Element(
    @Serializable(with = CategorySerializer::class)
    val category: Category,
)
```

**规则**：
- 枚举 `id` 必须与 JSON 数据一致
- `deserialize` 中找不到匹配时抛异常（数据不合法就该炸）
- `displayName` 直接用于 UI，不需要额外的 `stringResource` 映射表

---

## 10. Anti-patterns

1. **不要** 用 `any?` 等价物 `Any?`/`*` 在公共 API；类型是文档
2. **不要** 用 `lateinit var` 在 ViewModel 中替代 `MutableStateFlow` 初始值——丢失初始态语义
3. **不要** 在 `@Serializable` 路由 data class 里塞复杂对象（`Element` 实例等）；只放原始类型与 ID。复杂对象通过 Repository / ViewModel 再加载
