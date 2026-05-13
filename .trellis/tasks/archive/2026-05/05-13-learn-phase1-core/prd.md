# Learn Phase 1: 20 核心元素

## Goal

实现 Learn 屏基础架构 + 20 个核心元素学习内容。

## 父任务

`.trellis/tasks/05-13-learn-lab-final`

## Requirements

### 数据层
- `data/learn/model/LearnItem.kt`: @Serializable, `level` 字段 ("core"/"important"/"normal")
- `data/learn/LearnRepository.kt`: load `assets/learn.json`
- `assets/learn.json`: 20 核心元素，每元素包含 discoveryStory(150-200字) + applications(2-3条) + funFacts(1-2条) + level("core")

### Feature 层
- `feature/learn/LearnScreen.kt`: 元素列表(按等级分组 Section) + 学习详情页
- `feature/learn/LearnViewModel.kt`
- `feature/learn/LearnUiState.kt`
- `feature/learn/LearnIntent.kt`

### 导航
- `Routes.LEARN` + PeriodicNav + NavigationSuite 第 7 tab

### Koin
- `LearnRepository` + `LearnViewModel`

## Acceptance Criteria

- [ ] 20 核心元素学习内容完整
- [ ] 列表按等级分组显示（核心/重点/普通）
- [ ] 等级标签色区分
- [ ] 详情页：发现历史 + 应用 + 趣味知识
- [ ] "查看元素详情"按钮跳转 Detail
- [ ] 编译/lint/detekt 通过
