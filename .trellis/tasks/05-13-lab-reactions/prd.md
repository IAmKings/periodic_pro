# Lab 屏: 40+ 经典化学反应（初中 + 高中）

## Goal

实现 Lab 屏 + 40-45 个初高中经典化学反应。

## 父任务

`.trellis/tasks/05-13-learn-lab-final`

## Requirements

### 数据层
- `data/lab/model/ChemicalReaction.kt`: @Serializable, 含 `ReactionType` 枚举 + `ReactionLevel` 枚举
- `data/lab/LabRepository.kt`
- `assets/reactions.json`: 40-45 个反应（初中 15-20 + 高中 25）

### Feature 层
- `feature/lab/LabScreen.kt`: 按类型分组列表 + 反应详情
- `feature/lab/LabViewModel.kt`
- `feature/lab/LabUiState.kt`
- `feature/lab/LabIntent.kt`

### 导航
- `Routes.LAB` + `Routes.LAB_DETAIL(reactionId)` + PeriodicNav + NavigationSuite 第 8 tab

### Koin
- `LabRepository` + `LabViewModel`

### 反应数据覆盖
- 化合反应 (combination): ≥5
- 分解反应 (decomposition): ≥5
- 置换反应 (displacement): ≥5
- 复分解反应 (double_displacement): ≥5
- 氧化还原反应 (redox): ≥5
- 其他 (other): ≥3
- 初中标记 (junior): 15-20
- 高中标记 (senior): 25

## Acceptance Criteria

- [ ] 40-45 个反应数据完整
- [ ] 反应列表按类型分组(StickyHeader)
- [ ] 反应详情：方程式 + 条件 + 现象 + 涉及元素
- [ ] 涉及元素可点击跳转 Detail
- [ ] 初中/高中标签区分
- [ ] 编译/lint/detekt 通过
