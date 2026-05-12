# Profile/Settings 屏

## Goal

实现 Profile/Settings 屏，提供主题切换、关于信息、数据署名。

## 父任务

`.trellis/tasks/05-12-design-audit`

## 前置依赖

- `05-12-design-foundation`（地基重构完成）

## Requirements

### Profile 屏 (`feature/profile/`)

- MVI 模式：Screen + ViewModel + UiState + Intent
- 主题三段切换：System / Light / Dark，即时生效
- 关于区：App 名称图标 + 版本号 + 技术栈标签
- 数据署名区：CC BY-SA 3.0 + Wikipedia 链接
- NavigationSuite 新增 Settings tab
- 路由 `Routes.PROFILE`

### UI 布局
- Scaffold + TopAppBar（"设置"）
- 主题区：3 个 RadioButton / 卡片选择
- 关于区：List 项
- 署名区：文本

## Acceptance Criteria

- [ ] Profile 屏 MVI 完整
- [ ] 主题切换三段选择，即时生效
- [ ] 关于 + 数据署名显示正确
- [ ] NavigationSuite 显示 Settings tab
- [ ] Koin 注册 ProfileViewModel
