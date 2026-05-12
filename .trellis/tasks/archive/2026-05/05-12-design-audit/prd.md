# design 验收：Profile 屏 + Category 屏 + 地基重构

## Goal

对照 `design/README.md` 设计规范，补齐：
1. **地基重构**：Category 枚举 + ThemePreference 接线 + PropertyChip + DetailViewModel Koin
2. **Profile/Settings 屏**：主题切换 + 关于 + 数据署名
3. **Category 屏**：按分类浏览元素列表

## Decision (ADR-lite)

**本轮范围**：地基重构 → Profile 屏 → Category 屏

**Discover / Lab / Learn 屏**：Out of Scope，后续迭代

**DashboardGrid / ComparisonTable 提取**：Out of Scope，不与新屏耦合

---

## 子任务 1: 地基重构

### 1.1 Category 枚举 (`data/element/model/Category.kt`)

```kotlin
enum class Category(val id: String, val displayName: String) {
    ALKALI_METAL("alkali-metal", "碱金属"),
    ALKALINE_EARTH("alkaline-earth", "碱土金属"),
    TRANSITION_METAL("transition-metal", "过渡金属"),
    POST_TRANSITION("post-transition", "后过渡金属"),
    METALLOID("metalloid", "类金属"),
    NONMETAL("nonmetal", "非金属"),
    HALOGEN("halogen", "卤素"),
    NOBLE_GAS("noble-gas", "稀有气体"),
    LANTHANIDE("lanthanide", "镧系"),
    ACTINIDE("actinide", "锕系")
}
```

**影响文件**（全项目 String → 枚举替换）：
- `Color.kt` — `forCategory(category: String)` → `forCategory(category: Category)`
- `Element.kt` — `category: String` → `category: Category`（自定义 Serializer）
- `TableScreen.kt` — `categoryEntries` 改用 `Category.entries`
- `DetailScreen.kt` — `categoryStringRes()` → `category.displayName`
- `CompareScreen.kt` — `categoryNames` 映射
- `AppModule.kt` — 无需改

### 1.2 ThemePreferenceRepository 接线

`PeriodicProTheme.kt` 读取 `ThemePreferenceRepository.themeMode` Flow：
- `ThemeMode.SYSTEM` → `isSystemInDarkTheme()`
- `ThemeMode.LIGHT` → false
- `ThemeMode.DARK` → true

### 1.3 PropertyChip 组件 (`ui/components/PropertyChip.kt`)

属性标签 Chip，用于 Detail 页属性分类标签和 Category 页标签：
- 非选中态：透明背景 + 描边 + category 色文字
- 选中态：category 色填充 + 白色文字
- 参考 `CategoryChip` 的样式逻辑

### 1.4 DetailViewModel Koin 注册

`AppModule.kt` 添加 `viewModel { params -> DetailViewModel(get(), get(), params.get()) }`
`DetailScreen.kt` 改用 `koinViewModel { params -> ... }`

---

## 子任务 2: Profile/Settings 屏 (`feature/profile/`)

### 功能
- **主题切换**：System / Light / Dark 三段选择，即时生效
- **关于**：App 版本、技术栈说明
- **数据署名**：CC BY-SA 3.0，元素数据来源 Wikipedia
- **右上角入口**：从 Home 屏 Dashboard 进入，或 NavigationSuite 新增第 5 个 tab

### 技术实现
- MVI 模式：`ProfileScreen.kt` + `ProfileViewModel.kt` + `ProfileUiState.kt` + `ProfileIntent.kt`
- Koin 注册 `ProfileViewModel`
- 导航路由 `Routes.PROFILE`
- NavigationSuite 新增 Settings tab（Material Icons: `settings`）

### UI 布局
- 主题切换区：3 个 RadioButton / SegmentedButton（跟随系统 / 浅色 / 深色）
- 关于区：App 图标 + 版本号 + 技术栈标签
- 数据署名区：CC BY-SA 3.0 + Wikipedia 链接
- 标准 M3 Scaffold + TopAppBar

---

## 子任务 3: Category 屏 (`feature/category/`)

### 功能
- 显示 10 个分类入口（Grid 布局，每项显示分类色条 + 分类名 + 元素数量）
- 点击某个分类 → 跳转到分类详情（该分类下所有元素列表）
- 元素列表项：ElementCard 小卡片
- 列表中点击元素 → 跳转 Detail 屏
- 空状态处理

### 技术实现
- MVI 模式：`CategoryScreen.kt` + `CategoryViewModel.kt` + `CategoryUiState.kt` + `CategoryIntent.kt`
- Koin 注册 `CategoryViewModel`
- 导航路由 `Routes.CATEGORY` + `Routes.CATEGORY_DETAIL(categoryId)`
- 复用 `ElementCard`、`CategoryChip`、`PropertyChip`

### UI 布局
- 分类入口 Grid：2 列或 3 列，每个卡片顶部 Category 色条 + 图标 + 名称 + 元素计数
- 分类详情：TopAppBar（分类名 + 返回）+ LazyColumn（ElementCard 列表）
- 空状态：无

---

## Acceptance Criteria

- [ ] Category 枚举替代全项目 String 字面量，编译通过
- [ ] ThemePreferenceRepository 接线，主题切换即时生效
- [ ] PropertyChip 组件实现，含 @Preview
- [ ] DetailViewModel Koin 注册
- [ ] Profile 屏 MVI 完整，主题三段切换 + 关于 + 数据署名
- [ ] Category 屏 MVI 完整，分类入口 + 分类详情列表
- [ ] NavigationSuite 新增 Profile tab
- [ ] 新路由 + 导航参数正确
- [ ] `./gradlew assembleDebug lint` 通过
- [ ] `./gradlew detekt` 通过

## Definition of Done

* 新增 feature 屏含完整的 MVI（Screen + ViewModel + UiState + Intent）
* Koin 注册新 ViewModel
* 新组件含 @Preview
* 代码通过 lint + detekt
* 地基重构不破坏现有 5 屏功能

## Out of Scope

* Discover / Lab / Learn 屏（后续迭代）
* DashboardGrid / ComparisonTable 提取（后续迭代）
* 3D Viewer（真 3D）
* Wizard 向导模式
* CI/CD 配置

## Technical Notes

* 设计规范：`design/README.md`
* 相关 spec：`.trellis/spec/frontend/state-management.md`、`.trellis/spec/frontend/index.md`
* 所有 5 屏 MVP 已提交（`94e9df5`）
