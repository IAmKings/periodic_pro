# 地基重构: Category枚举 + ThemePreference + PropertyChip + DetailVM

## Goal

夯实设计审计前的技术地基，为 Profile 屏和 Category 屏铺路。

## 父任务

`.trellis/tasks/05-12-design-audit`

## Requirements

### 1. Category 枚举
- 新建 `data/element/model/Category.kt`，定义 `enum class Category`
- 10 个枚举值，含 `id: String` 和 `displayName: String`
- `Element.kt` 的 `category` 字段从 `String` 改为 `Category`（自定义 Serializer）
- 替换全项目 String 字面量：`Color.kt`、`TableScreen.kt`、`DetailScreen.kt`、`CompareScreen.kt`
- `CategoryChip.kt` 签名更新

### 2. ThemePreferenceRepository 接线
- `PeriodicProTheme.kt` 读取 `ThemePreferenceRepository.themeMode: Flow<ThemeMode>`
- `ThemeMode.SYSTEM` → `isSystemInDarkTheme()`
- `ThemeMode.LIGHT` → false
- `ThemeMode.DARK` → true

### 3. PropertyChip 组件
- 新建 `ui/components/PropertyChip.kt`
- 选中/非选中两态，参考 `CategoryChip` 样式
- 含 @Preview

### 4. DetailViewModel Koin 注册
- `AppModule.kt` 添加 DetailViewModel 注册
- `DetailScreen.kt` 改用 `koinViewModel`

## Acceptance Criteria

- [ ] Category 枚举生效，全项目零 String 字面量残留
- [ ] 编译通过
- [ ] 现有 5 屏功能不变
- [ ] ThemeMode 三段切换生效
- [ ] PropertyChip @Preview 正确
- [ ] DetailViewModel Koin 注册
