# Composable 组件规范

> 所有可复用 UI 单元都是 `@Composable` 函数，放在 `ui/components/`（细粒度）或 `ui/pattern/`（粗粒度复合）。本文档约束**如何写一个合格的 Composable**。

---

## 1. 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 公共组件 | PascalCase 名词 / 动名词 | `ElementCard`, `CategoryChip`, `GlassSurface` |
| 屏幕入口 | `<Feature>Screen` | `TableScreen` |
| 屏幕内容 | `<Feature>Content` | `TableContent` |
| 私有辅助 | 文件内 `private fun`，无前缀 | `private fun CellSpacer()` |
| Slot 参数 | 使用 `content` / `leading` / `trailing` / `header` | `header: @Composable () -> Unit` |
| 回调参数 | `on<Action>` | `onClick`, `onCategoryToggle`, `onLongPress` |
| 状态参数 | 直接传值（hoisted） | `isSelected: Boolean`, `query: String` |

**禁止**：以 `render` / `build` / `create` 开头（这是 React/Web 范式），Composable 函数本身就是「声明 UI」。

---

## 2. 参数顺序铁律

```kotlin
@Composable
fun ElementCard(
    element: Element,                                  // 1. 业务必填参数（数据）
    onClick: () -> Unit,                                // 2. 必填回调
    modifier: Modifier = Modifier,                      // 3. ★ Modifier 必须是第一个可选参数 ★
    isSelected: Boolean = false,                        // 4. 其他可选状态
    onLongPress: (() -> Unit)? = null,                  // 5. 可选回调
    content: @Composable (() -> Unit)? = null,         // 6. 末尾的 trailing lambda（如有）
) { /* ... */ }
```

铁律理由：调用方按位置传入 `Modifier.padding(8.dp)` 是 Compose 最高频用法；Modifier 必须是第一个可选参数，使调用方能用具名参数链式覆写。

---

## 3. State Hoisting（状态提升）

> 组件本身**无状态**或**仅持有 UI 私有状态**（如展开/折叠动画）。所有业务态由调用方注入。

❌ 反例：

```kotlin
@Composable
fun BadSearchBar() {
    var query by remember { mutableStateOf("") }  // 业务态写死在组件里，外部无法读
    TextField(value = query, onValueChange = { query = it })
}
```

✅ 正例：

```kotlin
@Composable
fun PeriodicSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.search_placeholder),
    onSubmit: () -> Unit = {},
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        singleLine = true,
    )
}
```

仅当状态**纯属 UI 私有且无外部读取必要**（如菜单是否展开、动画进度），才在组件内 `remember{}`。

---

## 4. Slot API（插槽）

复合组件优先用 Slot 而非 enum 配置：

```kotlin
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
    shape: Shape = RoundedCornerShape(20.dp),
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,   // ★ Slot：把内容控制权交给调用方
) {
    val hazeState = LocalHazeState.current
    val baseModifier = modifier.clip(shape)
    val finalModifier = when {
        !enabled || hazeState == null -> baseModifier.background(tint, shape)
        else -> baseModifier.hazeEffect(state = hazeState) {
            this.blurRadius = blurRadius
            backgroundColor = MaterialTheme.colorScheme.surface
            tints = listOf(HazeTint(tint))
        }
    }
    Box(modifier = finalModifier, content = content)
}
```

---

## 5. `@Preview` 强制要求

每个公开 Composable **至少 1 个 `@Preview`**；UI 组件应额外给深色态 Preview。

```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ElementCardPreview() {
    PeriodicProTheme {
        ElementCard(
            element = SampleElements.hydrogen,
            onClick = {},
        )
    }
}
```

> `SampleElements` 放在 `ui/components/preview/SampleElements.kt`，仅用于 Preview，不打包到 release（用 `debugImplementation` 或 `@Suppress("UnusedPrivateMember")` 标记）。

---

## 6. 重组优化与稳定性

- **使用稳定参数**：data class 默认稳定；`List<T>` 不稳定，需用 `ImmutableList`（kotlinx.collections.immutable）或 `@Stable` 标注包装类
- 复杂参数尽量传 `data class` 而非多个原始参数
- 避免在 Composable 里创建 lambda 临时对象——优先用方法引用：`onClick = vm::onElementTap`

---

## 7. Glass Blur 红线（依据 `research/glass-blur-compose.md`）

**毛玻璃锁版本：`dev.chrisbanes.haze:haze:1.7.2`**，禁止升级到 2.0 alpha。

| 必须 | 禁止 |
|------|------|
| 在 AppRoot 通过 `LocalHazeState` 下发 `HazeState`，业务侧用 `GlassSurface(...)` | **禁止**在 LazyColumn / LazyVerticalGrid / 自定义网格的**每个子项**里套 `GlassSurface`（118 元素各开离屏 layer，必卡） |
| 同屏 GlassSurface 数量 ≤ 5（顶栏、底栏、FAB、详情卡） | **禁止**嵌套 GlassSurface（GlassSurface 套 GlassSurface） |
| API 24-30 自动降级 scrim（半透明 + tint），无需手动处理 | **禁止**直接用已 deprecated 的 RenderScript |
| 省电模式可通过 `enabled = false` 短路 | **禁止**用 `Modifier.blur` 期望「模糊下层」——它只模糊自身子树 |

---

## 8. 周期表 CategoryColors 注入（依据 `research/compose-periodic-table.md`）

10 种 element category 颜色**不要**塞进 Material 3 ColorScheme（会污染语义槽）。改用 `staticCompositionLocalOf`：

```kotlin
// theme/CategoryColors.kt
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
        ElementCategory.AlkalineEarthMetal -> alkalineEarthMetal
        ElementCategory.TransitionMetal -> transitionMetal
        ElementCategory.PostTransitionMetal -> postTransitionMetal
        ElementCategory.Metalloid -> metalloid
        ElementCategory.ReactiveNonmetal -> reactiveNonmetal
        ElementCategory.NobleGas -> nobleGas
        ElementCategory.Lanthanide -> lanthanide
        ElementCategory.Actinide -> actinide
        ElementCategory.Unknown -> unknown
    }
}

val LocalCategoryColors = staticCompositionLocalOf<CategoryColors> {
    error("CategoryColors not provided. Wrap UI in PeriodicProTheme.")
}
```

调用方：`val bg = LocalCategoryColors.current.forCategory(element.category)`。深色模式下应用降饱和度调色板（tone 30 vs light tone 90）。

---

## 9. 完整示例：ElementCard

```kotlin
// ui/components/ElementCard.kt
package app.periodic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.periodic.data.element.model.Element
import app.periodic.theme.LocalCategoryColors

@Composable
fun ElementCard(
    element: Element,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    inSelectionMode: Boolean = false,
    onLongPress: (() -> Unit)? = null,
) {
    val bg = LocalCategoryColors.current.forCategory(element.category)
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape,
            )
            .alpha(if (inSelectionMode && !isSelected) 0.5f else 1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${element.z}", style = MaterialTheme.typography.labelSmall)
            Text(
                element.symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(14.dp),
            )
        }
    }
}
```

---

## Anti-patterns

1. **不要**让 `modifier` 不是第一个可选参数；这是 Compose 全社区的强约定，违反会让调用方写得很别扭
2. **不要**在公共组件内部 `viewModel()` 或注入 Repository——组件必须 stateless，业务态从外部 hoist 进来
3. **不要**在 18×9 周期表的每个 Cell 上叠 `Modifier.shadow()` 或 `GlassSurface`——118 个离屏 layer 会让中端机掉到 < 30 fps
