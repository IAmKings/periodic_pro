# Research: Jetpack Compose 实现 Glass Blur 毛玻璃效果

- **Query**: 在 Jetpack Compose 中实现 Glass Blur 毛玻璃效果（背景模糊 + 半透明 tint），支持深 / 浅色主题，需兼顾 API 24-30 兜底
- **Scope**: external（官方文档 + Haze 库 + 社区方案）
- **Date**: 2026-05-10

## TL;DR（结论先行）

| 选项 | 适用场景 | 推荐度 |
|---|---|---|
| **Haze 1.7.x（稳定）** | MVP 主推，Android 12+ 真模糊，Android 11- 自动 scrim 兜底，可选 RenderScript 实验性兜底 | ⭐⭐⭐⭐⭐ |
| Compose 原生 `Modifier.blur` | 仅"自身内容模糊"（前景模糊），无法看到底层动态内容 | ⭐⭐ 单点用 |
| 自写 `graphicsLayer { renderEffect = … }` | 需要完全自控渲染管线时再考虑，复杂度高 | ⭐ |
| Cloudy（skydoves） | 想全自研、要 KMP + iOS 一致性的备选库 | ⭐⭐⭐ |

**MVP 推荐**：直接引入 **Haze 1.7.2**，封装一个 `GlassSurface` Composable；API 31+ 走 RenderEffect，API 24-30 自动降级为半透明 tint scrim（视觉差异可接受）。后续如设计同学坚持要求老设备也有真模糊，再开启 Haze 实验性 RenderScript 兜底或迁移到 Haze 2.0 的 `haze-blur` 模块。

---

## 1. API 31+ 原生方案

### 1.1 `Modifier.blur`

来源：`androidx.compose.ui:ui` —— [官方 API 参考](https://developer.android.com/reference/kotlin/androidx/compose/ui/draw/blur.modifier)

```kotlin
fun Modifier.blur(
    radius: Dp,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle
): Modifier
```

关键事实：

- **仅 Android 12 (API 31) 及以上生效**；旧版本调用会被 **静默忽略**（不会崩，也不会有效果）。
- 会把内容渲染到独立 `GraphicsLayer`，并 clip 到内容边界。圆形/异形需配合 `BlurredEdgeTreatment.Unbounded` + `clip(Shape)`。
- **它模糊的是自身的子树**，不是底层；适用于"前景模糊"（modal/overlay 自己有图，要把自己变模糊），**不适合 Glass Surface 这种"看到下层模糊背景"的场景**。

### 1.2 `graphicsLayer { renderEffect = BlurEffect(...) }`

来源：`androidx.compose.ui.graphics.BlurEffect` / `RenderEffect`。

```kotlin
Modifier.graphicsLayer(
    renderEffect = android.graphics.RenderEffect
        .createBlurEffect(25f, 25f, Shader.TileMode.MIRROR)
        .asComposeRenderEffect()
)
```

特性：

- 同样 **要求 API 31+**（`@RequiresApi(31)`）。
- 比 `Modifier.blur` 更底层，可以与 `OffsetEffect`、`compositingStrategy` 自由组合。
- 仍是"对自身内容模糊"，**无法天然拿到下层渲染结果**。要做"内容感知"的 Glass Blur，需要把底层内容显式画到一个 `GraphicsLayer`，再让 GlassSurface 节点把这个 layer 重画一遍并加 blur —— 这正是 Haze 在做的事。

### 1.3 内容感知的核心难点

参见 StackOverflow [How to Achieve a Glassmorphic Background](https://stackoverflow.com/questions/78740780/)：

> 把 `Modifier.blur` 加到 Box 上会把 **整个子树**（包含 overlay 上的文字/图标）也模糊掉，这通常不是想要的。

要"模糊下层但保留上层清晰"，必须把渲染流分两份：

1. 下层 → 离屏 `GraphicsLayer`（`rememberGraphicsLayer()` + `Modifier.drawWithContent { layer.record { drawContent() }; drawLayer(layer) }`）。
2. GlassSurface 节点 → 取这个 layer，应用 BlurEffect 重绘到自己的位置上，再叠 tint 与子内容。

这套机制的难点是 **坐标对齐 + 失效驱动 + 多源**，自己写很容易踩坑（见 Haze 关于 `HazePositionStrategy`、`ViewTreeObserver` 预绘事件的讨论）。所以 MVP 阶段不建议自研。

---

## 2. API 24-30 兜底方案

### 2.1 RenderScript（已 deprecated）

- `ScriptIntrinsicBlur` 在 **API 31 deprecated**，AGP 7.2 起开始警告，未来 AGP 会移除支持（[官方迁移指南](https://developer.android.com/guide/topics/renderscript/migrate)）。
- 不推荐新项目直接用。

### 2.2 RenderScript Intrinsics Replacement Toolkit

- Google 官方维护的接力库：[android/renderscript-intrinsics-replacement-toolkit](https://github.com/android/renderscript-intrinsics-replacement-toolkit) (v0.8 BETA)。
- CPU 实现，速度比原 RS 快 ~2x，但只能处理 `Bitmap`/`ByteArray`，对于 Compose 动态内容必须先把内容 draw 到 Bitmap → blur → 再贴回，开销大。

### 2.3 Haze 自带兜底（推荐）

来自 Haze 文档 [Platforms](https://chrisbanes.github.io/haze/latest/platforms/)：

| 平台 | 默认行为 |
|---|---|
| Android 12+ (SDK 32+) | RenderEffect，真模糊 |
| Android 12 (SDK 31) | 1.6.0 起默认开启（之前因 RenderNode bug 关闭） |
| Android 11 及以下 (SDK 30-) | 默认 **scrim**（半透明 + tint），不模糊 |
| Android 11 及以下，可选 | 1.6 引入 **RenderScript 实验性方案**，背景线程处理，会比真模糊掉帧/落后一帧，需手动 opt-in |

> "On Android 11 and below, blurring by default is disabled. When disabled, a scrim (translucent overlay) implementation is used."

对 MVP，scrim 兜底视觉上接受度足够（iOS 风毛玻璃在低端设备本来就要让步），且性能 **零额外开销**。

### 2.4 Cloudy（备选）

[skydoves/Cloudy](https://github.com/skydoves/Cloudy)：

| 平台 | 实现 |
|---|---|
| Android 31+ | RenderEffect (GPU) |
| Android 30- | 自研 NEON/SIMD CPU blur |
| iOS / Desktop / Wasm | Skia BlurEffect |

如果未来要做 KMP，且需要老安卓也有真模糊，Cloudy 比 Haze 1.x 在低版本 Android 上更激进。但是它 API 更"原始"（更接近 `Modifier.cloudy(radius)`），不专门为"看到下层"做的。

---

## 3. Haze 库详解

### 3.1 概览

- 仓库：[chrisbanes/haze](https://github.com/chrisbanes/haze)（2.1k+ ⭐）
- License：Apache 2.0
- **当前稳定版**：`1.7.2`（2026-02-10），依赖 Kotlin 2.2.20、Compose Multiplatform 1.9.3、Jetpack Compose 1.9.4。
- **2.0.0-alpha01**：拆分为 `haze`（核心）+ `haze-blur`（blur 模块），API 更模块化，但仍 alpha，MVP 不上。
- 支持 Android / Desktop JVM / iOS / Wasm / JS-Canvas，作者是 Tivi 作者 Chris Banes（Compose 早期社区核心贡献者）。

### 3.2 依赖（MVP 推荐 1.7.x）

```kotlin
dependencies {
    implementation("dev.chrisbanes.haze:haze:1.7.2")
    // 可选：预制 Material 风格 (HazeMaterials.thin/regular/thick/ultraThin)
    // 视情况引入；当前 haze:1.7 已自带 Materials
}
```

### 3.3 核心 API（1.5+ 现行命名）

两个 Modifier + 一个 State：

```kotlin
val hazeState = rememberHazeState()  // 1.6+；之前是 remember { HazeState() }

// 1) 标记"被模糊源"——下层背景
Modifier.hazeSource(state = hazeState, zIndex = 0f)

// 2) 标记"玻璃"——会在自己位置画出底层模糊
Modifier.hazeEffect(state = hazeState) {
    // HazeEffectScope 内可动态调参
    blurRadius = 24.dp                     // 1.7+ 使用 blurEffect {} 包裹也可
    backgroundColor = MaterialTheme.colorScheme.surface
    tints = listOf(HazeTint(Color.White.copy(alpha = 0.3f)))
    noiseFactor = 0.05f
    style = HazeMaterials.thin()           // 也可用预制材质
}
```

> 注意：1.4 之前是 `Modifier.haze` + `Modifier.hazeChild`，1.5 重命名为 `hazeSource` + `hazeEffect`。Haze 2.0 引入 `blurEffect { ... }` 嵌套块（v2 才强制嵌套）。**MVP 用 1.7.x，可两种写法都接受**。

### 3.4 工作机制（"内容感知"是怎么做的）

1. `hazeSource` 节点把内容渲染时同步写入一个共享的 `GraphicsLayer`。
2. `hazeEffect` 节点在 draw 阶段读取这些 layer，按自身坐标裁剪出对应区域，应用 `RenderEffect.createBlurEffect`。
3. Android 12 上为了规避 `RenderNode.setPosition` 的位置失效 bug（issue #77），Haze 通过 `ViewTreeObserver` 预绘监听器手动 invalidate。
4. `HazePositionStrategy` 解决分屏/多窗口下绝对坐标错位。
5. 1.6 之后老平台支持基于 RenderScript 的离线 blur（背景线程，单帧节流，落后 1+ 帧）。

### 3.5 前景模糊（无 hazeSource）

如果只想模糊"自己"，不需要 HazeState：

```kotlin
Modifier.hazeEffect {
    blurEffect { style = HazeMaterials.thin() }
}
```

适合：MVP 中某些 modal/dialog 不在意看到底层时，简化用法。

---

## 4. 性能 / 电量

### 4.1 数据点

来自 [Haze 1.0 release notes](https://chrisbanes.me/posts/haze-1.0/) 在 Pixel 6 上的基准（P50 帧时长）：

| 场景 | 相对成本 |
|---|---|
| 普通 hazeEffect（API 31+） | 1.0x（基线） |
| Progressive blur (SDK 33+ runtime shader) | 1.25x |
| Progressive blur (SDK 32 多层) | 2.0x |
| Mask blur | 1.05x |

结论：**单个常驻 GlassSurface 在中端机（Pixel 6 级别）几乎无感**；多个堆叠 + progressive 才会出现压力。

### 4.2 周期表 + Detail 屏幕的实务建议

- **周期表主页**：建议把毛玻璃只用在固定 chrome（顶栏 / 底部抽屉 / FAB 卡）。元素格子本身 **不要每个都套 GlassSurface**——118 个全是毛玻璃 = 118 个离屏 layer，必崩。
- **Detail 页**：单张大卡片 + 顶部图，毛玻璃强度可以拉满，性能没问题。
- **滚动列表**：`hazeSource` 套在 `LazyColumn` 上，`hazeEffect` 套在固定 chrome 上是 Haze 推荐姿势，benchmark 已优化。
- **降级策略**：
  - API <= 30：自动 scrim，无成本。
  - 用户开了"省电模式" / `Settings.Global.LOW_POWER_MODE`：可在 `blurEnabled = { false }` lambda 里返回 false。
  - 屏幕动画频繁帧（开屏过场）期间临时关 blur，过场后再开。
- **过度绘制**：[Android 官方建议](https://developer.android.com/topic/performance/rendering/overdraw) 透明层和模糊都加重 overdraw，Glass Surface 不要嵌 Glass Surface。

---

## 5. GlassSurface Composable 推荐 API

### 5.1 设计目标

- 上层调用方 **不感知** API 31 / Haze 内部细节；只暴露 `blurRadius / tint / shape / content`。
- 单一 `HazeState` 在 Scaffold 层 provide，`GlassSurface` 通过 CompositionLocal 拿。
- 主题感知：默认 `tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)`，深浅色自动切换。
- 支持"前景模糊"短路（无 hazeSource 时退化为 `Modifier.blur`，仍有 fallback）。

### 5.2 骨架代码

```kotlin
// ---------- 1) 应用根：提供 HazeState ----------
val LocalHazeState = compositionLocalOf<HazeState?> { null }

@Composable
fun AppRoot(content: @Composable () -> Unit) {
    val hazeState = rememberHazeState()
    CompositionLocalProvider(LocalHazeState provides hazeState) {
        // 注意：Scaffold 的"被模糊源"必须挂 hazeSource
        Box(Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 0f)) {
            content()
        }
    }
}

// ---------- 2) 通用 GlassSurface ----------
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
    shape: Shape = RoundedCornerShape(20.dp),
    border: BorderStroke? = BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    ),
    enabled: Boolean = true, // 业务侧可按省电/低端机关闭
    content: @Composable BoxScope.() -> Unit,
) {
    val hazeState = LocalHazeState.current
    val baseModifier = modifier
        .clip(shape)
        .then(if (border != null) Modifier.border(border, shape) else Modifier)

    val blurredModifier = when {
        !enabled || hazeState == null -> {
            // 兜底：无 HazeState 或被禁用 → 纯 tint scrim
            baseModifier.background(tint, shape)
        }
        else -> {
            baseModifier
                .hazeEffect(state = hazeState) {
                    this.blurRadius = blurRadius
                    backgroundColor = MaterialTheme.colorScheme.surface
                    tints = listOf(HazeTint(tint))
                    // 可选：noiseFactor = 0.05f
                }
        }
    }

    Box(modifier = blurredModifier, content = content)
}

// ---------- 3) 调用方 ----------
@Composable
fun PeriodicTopBar() {
    GlassSurface(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RectangleShape,
    ) {
        // TopBar 内容（搜索框、标题、操作按钮）
    }
}
```

### 5.3 备选简化版（不需要"看到下层"的场景）

```kotlin
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    // 仅前景模糊，无需 HazeState
    Box(
        modifier = modifier
            .clip(shape)
            .hazeEffect { blurEffect { this.blurRadius = blurRadius } }
            .background(tint, shape),
        content = content,
    )
}
```

---

## 6. 最终推荐方案

### 6.1 MVP 阶段

1. **依赖**：`dev.chrisbanes.haze:haze:1.7.2`。
2. **架构**：根 Scaffold/Box 挂 `hazeSource`，通过 `LocalHazeState` 下发 → 业务侧只用 `GlassSurface(...)`。
3. **降级**：
   - Android 12+：Haze 真模糊。
   - Android 7-11：Haze 默认 scrim（半透明 tint + 边框 + 轻微 noise），视觉可接受。
   - 用户禁用动画 / 省电：`enabled = false` 返回纯 scrim。
4. **使用约束**：
   - 同屏 GlassSurface 数量上限 ~3-5 个（顶栏、底栏、FAB、详情卡）。
   - 不要在 LazyColumn 子项里每项都套 GlassSurface。
   - 滚动期间避免 progressive blur（保留为后续迭代）。

### 6.2 后续可演进

- 升级 Haze 2.x（拆分 `haze-blur`），支持自定义 `VisualEffect`（颜色着色、彩色玻璃等）。
- 在低端 Android 11- 用户多的情况下，开启 Haze 的实验性 RenderScript 兜底，或评估 Cloudy 的 NEON/CPU blur。
- 进一步引入 progressive blur / mask blur 做沉浸式头图。

---

## External References

- [Modifier.blur 官方 API](https://developer.android.com/reference/kotlin/androidx/compose/ui/draw/blur.modifier)
- [BlurEffect / RenderEffect](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/BlurEffect)
- [Haze 官网](https://chrisbanes.github.io/haze/latest/)
- [Haze Blur Usage 文档](https://chrisbanes.github.io/haze/latest/usage/)
- [Haze Platforms 兼容矩阵](https://chrisbanes.github.io/haze/latest/platforms/)
- [Haze 1.0 性能基准](https://chrisbanes.me/posts/haze-1.0/)
- [Haze 2.0 架构介绍](https://chrisbanes.me/posts/haze-2.0/)
- [Haze CHANGELOG](https://github.com/chrisbanes/haze/blob/main/CHANGELOG.md)
- [Haze Maven Central](https://central.sonatype.com/artifact/dev.chrisbanes.haze/haze)
- [skydoves/Cloudy](https://github.com/skydoves/Cloudy)
- [RenderScript 迁移指南（官方）](https://developer.android.com/guide/topics/renderscript/migrate)
- [RenderScript Intrinsics Replacement Toolkit](https://github.com/android/renderscript-intrinsics-replacement-toolkit)
- [StackOverflow: Glassmorphic Background in Compose](https://stackoverflow.com/questions/78740780/)
- [Haze Issue #77 — API 31 RenderNode bug](https://github.com/chrisbanes/haze/issues/77)

## Caveats / Not Found

- **未实测**：本调研未在真实 Android 7-11 设备上跑过 Haze 的 scrim/RenderScript 兜底；视觉接受度需要 UI/QA 在落地后确认。
- **Haze 2.0 alpha** 的 API 与 1.7 不完全兼容（必须 `blurEffect {}` 包裹 + 拆 `haze-blur` 依赖）。MVP 锁 1.7.x 即可，等 2.0 stable 再迁移。
- **省电模式自动检测代码**未写出，建议在 `GlassSurface` 外层用 `PowerManager.isPowerSaveMode` 注入 `enabled` 开关。
- **GlassSurface 与 Material 3 Surface 协同**（如 elevation tonal overlay）未深入研究，落地时需要确认 `MaterialTheme.colorScheme.surface` 在深色模式下的实际显示。
