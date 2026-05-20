# 首页新增快捷入口 —— 元素知识测试

## Goal

参照 QuizMode.kt 实现，在首页新增"测试"快捷入口，用户可随机答题测试元素知识。

## Reference

https://github.com/IAmKings/PeriodicTableOfElements/blob/master/mobile/androidApp/src/main/java/com/periodictable/ui/components/QuizMode.kt

## Requirements

### 题型（4种随机）
- **符号题**：给出中文名 → 选元素符号
- **名称题**：给出元素符号 → 选中文名
- **分类题**：给出元素 → 选所属分类
- **序号题**：给出元素 → 选原子序数

### 交互
- 每题4个选项，2列布局
- 点击选项立即反馈：绿色（正确）/ 红色（错误）
- 正确+1分+连击计数，错误连击归零
- 连击>1时显示连击动画标签
- 自动出下一题（或"下一题"按钮）
- 顶部显示：分数 + 连击 + 重新开始按钮

### 视觉
- 元素卡片用 CategoryColors 着色
- 题目中被问的信息用 `?` 替代
- 适配浅色/深色主题
- Material 3 风格，与项目一致

### 架构
- 全屏二级页面（root NavHost `quiz` 路由，滑动动画）
- 首页 Dashboard 加"测试"卡片（替换原有空位）
- QuizScreen 独立 Composable，不依赖 ViewModel

## Acceptance Criteria

- [ ] 首页显示"测试"快捷入口卡片
- [ ] 点击进入全屏测试页面，有返回按钮
- [ ] 4种题型随机出题
- [ ] 选项2列布局，正确/错误颜色反馈
- [ ] 分数+连击显示，重新开始按钮
- [ ] 浅色/深色主题适配

## Out of Scope

- 排行榜/历史记录
- 限时答题
- 难度选择

## Technical Notes

- QuizMode.kt 参考：数据驱动、本地 state、无 ViewModel
- 元素数据：ElementRepository.getAll()
- 分类色：LocalCategoryColors.current.forCategory()
- 路由：root NavHost `quiz`
- HomeScreen Dashboard 卡片
