# Stage 6: 对比屏（Compare Screen）

## Goal

实现多元素属性对比表。从周期表多选进入或 Tab 进入，多列横向对比展示关键属性。

## 父任务

`.trellis/tasks/05-10-compose-app-bootstrap`（Stage 5 已完成：元素详情屏）

## Requirements

### 1. Feature 包结构

```
feature/compare/
├── CompareScreen.kt      // 主 Composable
├── CompareViewModel.kt   // ViewModel
├── CompareUiState.kt     // data class
└── CompareIntent.kt      // sealed interface
```

### 2. 页面布局

- TopAppBar：标题 + 返回箭头
- 横向可滑的对比表：
  - 表头行（固定）：元素符号 + 分类色条
  - 属性行（每属性一行）：属性名（左列固定）+ 各元素值（可横向滚动）
- 支持 2-5 个元素对比
- 属性列表：原子量 / 密度 / 熔点 / 沸点 / 电负性 / 原子半径 / 电离能 / 电子排布 / 分类 / 周期 / 族
- null 值显示 "—"
- 每列顶部 4dp Category 色条

### 3. CompareViewModel

- 接收 `elementIds: Set<Int>`（从 Navigation 参数或 SavedStateHandle 获取）
- 加载元素列表 `elementRepo.getByNumber()` × N
- 如果 elementIds 为空 → UiState.isEmpty = true

### 4. Navigation
- 路由：`compare?ids=1,2,3`（逗号分隔原子序号）
- 从 Table 多选确认 → 导航到 Compare
- 从 BottomNav Tab 进入 → 空状态提示"从周期表选择元素进行对比"

---

## Acceptance Criteria

- [ ] 对比表横向可滑（属性名列固定左列 + 元素列可滚动）
- [ ] 每列顶部 Category 色条
- [ ] null 值显示 "—"
- [ ] 空状态：ids 为空时提示"从周期表多选元素开始对比"
- [ ] `./gradlew assembleDebug` 通过

## Out of Scope
- 添加/删除对比元素（本 Stage 只展示传入的 ids）
