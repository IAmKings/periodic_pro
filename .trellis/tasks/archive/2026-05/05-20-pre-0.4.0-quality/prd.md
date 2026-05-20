# 代码质量梳理：spec更新 + 技术债务 + 0.4.0准备

## Goal

在 0.4.0 版本开发前，对代码进行一轮系统性的质量梳理：修复架构违规、清除死代码、消除 lint 警告、更新过时 spec。

## 探查发现的全部问题

### 🔴 P0 — 架构违规（影响稳定性/可维护性）

| # | 问题 | 文件 |
|---|------|------|
| 1 | **QuizScreen 无 ViewModel** — 585行单体，直接注入 Repository，10+ state 变量 inline `remember` | `feature/quiz/QuizScreen.kt` |
| 2 | **TableScreen 手动构建 ViewModel** — `remember { TableViewModel(...) }` 而非 `koinViewModel()`，丢失生命周期 | `feature/table/TableScreen.kt:91-93` |
| 3 | **路由常量与实际注册不匹配** — `LEARN_DETAIL = "learn/{atomicNumber}"` 但实际注册为 `"learn?atomicNumber={atomicNumber}"`（查询参数 vs 路径参数），若使用会 crash | `Routes.kt:25,28` |

### 🟡 P1 — 死代码（增加维护成本）

| # | 问题 | 文件 |
|---|------|------|
| 4 | `SearchElementsUseCase` 从未被引用 | `domain/usecase/SearchElementsUseCase.kt` |
| 5 | 权限子系统全部 3 个文件无消费者（PermissionDef + PermissionDialog + PermissionsManager + Koin 注册） | `data/permission/*.kt`, `di/AppModule.kt` |
| 6 | `CompareIntent.NavigateBack` 从未 dispatch | `feature/compare/CompareIntent.kt:8` |
| 7 | `DetailIntent.NavigateBack` + `DetailIntent.SaveScroll` 从未 dispatch | `feature/detail/DetailIntent.kt:5-6` |
| 8 | 8 个未使用的 string 资源（`tab_compare`, `tab_category`, `tab_learn`, `tab_lab`, `screen_home`, `screen_detail`, `home_dashboard_table`, `home_dashboard_more`） | `strings.xml` |

### 🟢 P2 — 代码规范（lint 警告 + spec 不一致）

| # | 问题 | 文件 |
|---|------|------|
| 9 | **14 个 ModifierParameter 警告** — `modifier` 不是第一个可选参数 | 14 个 Composable 文件 |
| 10 | **2 个 AutoboxingStateCreation** — `mutableStateOf` 应改为 `mutableIntStateOf` | `DetailScreen.kt:230`, `PeriodicSearchBar.kt:39` |
| 11 | **硬编码中文字符串** — UpdateDialog, DetailScreen, ProfileScreen, PermissionDialog, QuizScreen 中约 15+ 处用户可见文本 | 5 个文件 |
| 12 | **formatDouble 重复** — 完全相同的函数出现在 CompareScreen + DetailScreen | 两个文件 |
| 13 | `collectAsState()` 在 PeriodicProTheme 中（应为 `collectAsStateWithLifecycle`） | `theme/PeriodicProTheme.kt:66` |
| 14 | README 版本号过时（v0.3.1 vs 实际 0.3.2） | `README.md` |
| 15 | UpdateDialog `modifier` 参数在末尾而非第一个 | `ui/components/UpdateDialog.kt:52` |
| 16 | PermissionDialog 无 @Preview | `ui/components/PermissionDialog.kt` |
| 17 | `WindowWidthSizeClass.EXPANDED` 枚举已 deprecated | `PeriodicNavSuite.kt:86` |

### ⚪ P3 — Spec 文档更新

| # | 问题 | 文件 |
|---|------|------|
| 18 | spec 仍描述"单层 NavHost"，实际已是双层 | `.trellis/spec/frontend/directory-structure.md` |
| 19 | spec 功能包列表缺 category/discover/learn/lab/quiz/profile | 同上 |
| 20 | spec data 包列表缺 discover/lab/learn/permission/update | 同上 |
| 21 | spec 要求 `@Serializable` type-safe 路由，实际用字符串（已知差异） | `index.md` |
| 22 | Haze 版本 spec 写 1.7.2，实际 1.6.0 | `index.md` |
| 23 | `ComparisonTable.kt` 在 spec 中列出但不存在 | `directory-structure.md` |

## Requirements

### P0（必须改）

- [ ] QuizScreen 重构为 MVVM：新增 QuizViewModel + QuizUiState + QuizIntent
- [ ] TableScreen 改用 `koinViewModel()` 替代手动 `remember { TableViewModel(...) }`
- [ ] 删除或修正 `Routes.LEARN_DETAIL` / `Routes.LAB_DETAIL`（与实际注册不一致）

### P1（强烈建议）

- [ ] 移除 SearchElementsUseCase
- [ ] 移除权限子系统（3 文件 + Koin 注册 + PermissionDef model）
- [ ] 清理未使用的 Intent 变体
- [ ] 清理 8 个未使用的 string 资源

### P2（规范对齐）

- [ ] 修复 14 个 ModifierParameter（modifier 提前到第一个可选参数）
- [ ] 2 个 AutoboxingStateCreation 修复
- [ ] 提取 formatDouble 到 `util/FormatUtils.kt`
- [ ] README 版本号更新
- [ ] 修复 `collectAsState()` → `collectAsStateWithLifecycle()`
- [ ] UpdateDialog modifier 参数顺序修正
- [ ] PermissionDialog 添加 @Preview

### P3（Spec 更新）

- [ ] directory-structure.md 更新为实际目录结构 + 双层 NavHost
- [ ] index.md 版本号/依赖版本同步

## Out of Scope

- Gradle 依赖升级（AGP 9.x / Kotlin 2.1.x / Compose 1.11.x 需单独评估兼容性）
- `@Serializable` type-safe 路由迁移（需先升 Navigation 2.8+）
- `WindowWidthSizeClass` → `isWidthAtLeastBreakpoint` 迁移（需先升 window-core 1.4+）
- ProGuard/R8 开启
- 大型 Composable 拆分（后续单独做）

## Acceptance Criteria

- [ ] QuizScreen 有 ViewModel + UiState + Intent，通过 koinViewModel 注入
- [ ] TableScreen 使用 koinViewModel()
- [ ] 所有死代码已移除（use case + 权限 + 未用 Intent + 未用 string）
- [ ] 14 个 ModifierParameter lint 警告清零
- [ ] 2 个 AutoboxingStateCreation 警告清零
- [ ] formatDouble 提取到共享 util
- [ ] spec 文档反映实际代码架构
- [ ] lint 警告数从 88 → 0（或接近 0）
- [ ] 编译通过
