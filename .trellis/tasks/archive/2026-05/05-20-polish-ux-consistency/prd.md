# 应用打磨：UX体验 + 一致性 + 工程完善

## Goal

基于代码审查发现的问题，对应用的 UX 体验断点、交互一致性、工程整洁度进行一轮集中打磨。

## 子任务列表

### P0 — 用户体验断点

#### 1. 测验：错题回顾 + 再测一次

**现状**：Quiz 结束只显示分数，无后续交互。
**目标**：
- 测验结束页面新增「错题回顾」按钮，展示错题列表（题目 + 正确答案）
- 新增「再测一次」按钮，重新开始出题
- 满分时显示 🎉 庆祝提示

**涉及文件**：`feature/quiz/QuizScreen.kt`、`feature/quiz/QuizViewModel.kt`

#### 2. 搜索无结果提示

**现状**：周期表搜索输入不存在的元素，列表区域一片空白。
**目标**：元素列表为空时显示「没有找到匹配的元素」空状态，建议调整关键词。

**涉及文件**：`feature/table/TableScreen.kt` 或 `feature/table/PeriodicTableGrid.kt`

#### 3. 收藏空状态引导

**现状**：收藏列表为空时仅文字提示。
**目标**：空状态增加跳转周期表的 CTA 按钮「去探索元素」。

**涉及文件**：`feature/favorites/FavoritesScreen.kt`

### P1 — 交互一致性

#### 4. 移除 Telephoto 依赖

**现状**：Gradle 仍有 `libs.telephoto.zoomable` 依赖，但周期表手势已改用原生 `horizontalScroll + combinedClickable`。
**目标**：彻底移除 Telephoto 库依赖，清理 `build.gradle.kts` 和 `libs.versions.toml`。

**涉及文件**：`app/build.gradle.kts`、`gradle/libs.versions.toml`

#### 5. 二级页面返回行为统一

**现状**：Compare → `popBackStack("main")`，Detail → `popBackStack()`，Learn/Lab 根据入口不同走 `onNavigateBack` 或 `BackToList`。
**目标**：所有二级全屏页面（Detail/Compare/Learn/Lab/Quiz）统一返回行为为 `popBackStack()`——回到上一页。

**涉及文件**：`MainActivity.kt`、`feature/learn/LearnScreen.kt`、`feature/lab/LabScreen.kt`

### P2 — 工程整洁

#### 6. 深色模式分类颜色对比度优化

**现状**：`CATEGORY_CELL_ALPHA = 0.85f`，浅色分类（如 nonmetal 黄绿）在深色背景上对比度不足。
**目标**：`isSystemInDarkTheme()` 时 cell alpha 提升到 0.92f 或换用更亮的变体色。

**涉及文件**：`theme/Color.kt`、`feature/table/PeriodicTableGrid.kt`

#### 7. 移除未使用的 detekt 配置

**现状**：CI 已移除 detekt，但 `detekt.yml` 和 Gradle plugin 仍在。
**目标**：移除 detekt plugin 和配置文件（或保留 plugin 仅本地开发用，不强制 CI）。

**涉及文件**：`app/build.gradle.kts`、`config/detekt/detekt.yml`、`.github/workflows/ci.yml`

## Acceptance Criteria

- [ ] 测验结束后可查看错题并重测
- [ ] 周期表搜索无结果时有文字提示
- [ ] 收藏空状态有引导跳转按钮
- [ ] Telephoto 依赖完全移除，编译通过
- [ ] 二级页面返回键行为统一为 popBackStack()
- [ ] 深色模式浅色分类 alpha 提升
- [ ] detekt 配置清理（plugin + yml）

## Out of Scope

- 单元测试补充
- 同位素信息
- 分享功能
- 国际化字符串提取
