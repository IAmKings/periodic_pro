# Category 屏

## Goal

实现 Category 屏：按元素分类浏览入口 + 分类详情列表。

## 父任务

`.trellis/tasks/05-12-design-audit`

## 前置依赖

- `05-12-design-foundation`（地基重构完成）

## Requirements

### Category 屏 (`feature/category/`)

- MVI 模式：Screen + ViewModel + UiState + Intent
- 分类入口 Grid（10 个分类卡片）
- 点击分类 → 分类详情（该分类下所有元素列表）
- 元素列表用 ElementCard
- 点击元素 → 导航 Detail 屏
- Koin 注册 CategoryViewModel
- 路由 `Routes.CATEGORY` + `Routes.CATEGORY_DETAIL(categoryId)`

### UI 布局
- 分类入口：2 列 Grid，每项分类色条 + 图标 + 名称 + 计数
- 分类详情：TopAppBar（分类名 + 返回）+ LazyColumn
- 复用 ElementCard、PropertyChip

## Acceptance Criteria

- [ ] Category 屏 MVI 完整
- [ ] 10 分类入口 Grid 正确
- [ ] 分类详情元素列表正确筛选
- [ ] 元素卡片点击跳转 Detail
- [ ] Koin 注册 CategoryViewModel
