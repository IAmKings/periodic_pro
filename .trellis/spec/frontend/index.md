# Android Compose 客户端规范总入口

> 本目录是 **Periodic Pro** Android 客户端的开发规范。技术栈：**Kotlin 2.x + Jetpack Compose + Material 3 + MVVM + StateFlow + Koin + 双层 NavHost + Navigation Compose + DataStore**，单 module 包分层。

---

## 规范定位

- 适用范围：`app/` 单 module 下的所有 Kotlin / Compose 代码
- 不适用：Gradle 构建脚本（见 build-guidelines.md，待补）；Android 原生 View 系统（本项目不使用）
- 语言：所有规范用简体中文撰写；代码示例为 Kotlin

---

## 文件导航与阅读顺序

按以下顺序阅读，新成员一小时内即可对齐：

| # | 文件 | 内容 | 必读 |
|---|------|------|------|
| 1 | [directory-structure.md](./directory-structure.md) | 单 module 包分层、feature 结构、assets 约定 | ✅ |
| 2 | [component-guidelines.md](./component-guidelines.md) | Composable 命名、参数顺序、State Hoisting、Slot API、`@Preview`、Glass Blur 红线 | ✅ |
| 3 | [state-management.md](./state-management.md) | ViewModel + StateFlow + UDF + sealed Intent 模板、Koin 注入、副作用 Channel | ✅ |
| 4 | [hook-guidelines.md](./hook-guidelines.md) | `remember` / `rememberSaveable` / `derivedStateOf` / `produceState` / `collectAsStateWithLifecycle` 用法 | ✅ |
| 5 | [type-safety.md](./type-safety.md) | Kotlin idioms：sealed、data class、value class、Result、`@Serializable` 路由 | ✅ |
| 6 | [quality-guidelines.md](./quality-guidelines.md) | Lint / detekt / 单测 / Compose UI 测试 / PR 质量门 | ✅ |

---

## Pre-Development Checklist

提交 PR 前自查：

- [ ] 已读 directory-structure.md，确认新增文件落点正确（feature 内 4 件套 / ui/components / ui/pattern / data）
- [ ] 所有 Composable 的 `modifier: Modifier = Modifier` 是第一个可选参数
- [ ] 每个公开 Composable 至少 1 个 `@Preview`
- [ ] 业务屏遵循 `ViewModel → UiState (data class) → Intent (sealed interface) → handle(intent)` 模板
- [ ] 使用 `collectAsStateWithLifecycle()` 而非 `collectAsState()`
- [ ] Navigation 路由用 `@Serializable` 对象 / data class（type-safe），不用字符串
- [ ] 没有在 LazyColumn / Grid 子项里套 `GlassSurface`（同屏 ≤ 5 个）
- [ ] CategoryColors 通过 `staticCompositionLocalOf` 注入，不塞进 M3 ColorScheme
- [ ] 适配代码使用 `currentWindowAdaptiveInfo()`，**不要**使用已 deprecated 的 `calculateWindowSizeClass(activity)`
- [ ] 关键 ViewModel / Repository 已补单测（turbine + MockK + runTest）
- [ ] `./gradlew lint detekt test` 全部通过

---

## 关键决策速查（ADR-lite）

| 决策 | 选型 |
|------|------|
| 架构范式 | MVVM + StateFlow + UDF（sealed Intent） |
| 工程结构 | 单 module + 包分层（按 feature 包；不拆 Gradle module） |
| DI | **Koin**（不用 Hilt，避免 KSP 编译开销） |
| 导航 | **双层 NavHost**（root 全屏二级页面 + inner Tab）+ Navigation Compose |
| 持久化 | DataStore Preferences（不引 Room） |
| 数据源 | `assets/elements.json`（英文主表）+ `assets/elements_zh.json`（中文映射） |
| 毛玻璃 | **`dev.chrisbanes.haze:haze:1.6.0`**（锁版本，不上 2.0 alpha） |
| 周期表手势 | 原生 Compose（`horizontalScroll` + `combinedClickable`） |
| 适配 | `currentWindowAdaptiveInfo()` + `NavigationSuiteScaffold` |

详细论证见任务 `.trellis/tasks/05-10-compose-app-bootstrap/prd.md`「Decision (ADR-lite)」与 `research/*.md` 4 份调研。

---

## Anti-patterns（全局红线）

1. **不要**在 Composable 里直接 `import androidx.compose.runtime.collectAsState` 收集 ViewModel 的 Flow——必须用 `collectAsStateWithLifecycle()`，避免后台收集耗电
2. **不要**新建任何 React/TSX/Web 痕迹的术语（useState/useEffect/hook/component prop drilling），这是 Android Compose 项目
3. **不要**在公共 Composable 里写硬编码字符串，所有可见文本走 `stringResource(R.string.xxx)`（即便 MVP 仅中文）
