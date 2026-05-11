# Stage 3: 数据层与全局组件

## Goal

制作 118 元素静态数据集（中英双语）+ 三层 Repository + Koin DI 模块 + 全局可复用 Composable 组件库（Button / ElementCard / CategoryChip / SearchBar / GlassSurface 等），为 Stage 4-7 各屏实施提供数据与 UI 基础。

## 父任务

`.trellis/tasks/05-10-compose-app-bootstrap`（Stage 2 已完成：工程骨架）

## Requirements

### 1. 元素数据集

**数据源**：Bowserinator/Periodic-Table-JSON (CC BY-SA 3.0)，裁剪后 ~90KB

**主数据文件** `app/src/main/assets/elements.json`：
118 元素 JSON 数组，每个元素至少包含：
```json
{
  "atomicNumber": 1,
  "symbol": "H",
  "name": "Hydrogen",
  "atomicMass": 1.008,
  "category": "nonmetal",
  "electronConfiguration": "1s1",
  "electronegativity": 2.2,
  "atomicRadius": 120,
  "ionizationEnergy": 13.598,
  "density": 0.00008988,
  "meltingPoint": 13.99,
  "boilingPoint": 20.271,
  "group": 1,
  "period": 1,
  "discoveredBy": "Henry Cavendish",
  "yearDiscovered": 1766
}
```
- category 枚举值：`alkali-metal` / `alkaline-earth-metal` / `transition-metal` / `post-transition-metal` / `metalloid` / `nonmetal` / `halogen` / `noble-gas` / `lanthanide` / `actinide`
- 超重元素 (Z≥104)：缺失字段填 `null`（JSON null）
- 署名要求：CC BY-SA 3.0 → 在「关于」页署名 Wikipedia（Stage 后续补）

**中文数据** `app/src/main/assets/elements_zh.json`：
```json
[
  { "atomicNumber": 1, "nameZh": "氢", "pinyin": "qīng" },
  ...
]
```
- 生僻字（鉨/鏌/鉝/鿫）确保正确 Unicode

### 2. 数据层 (`app/src/main/java/com/periodic/pro/data/`)

#### Element 模型
```kotlin
// data/element/model/Element.kt
@Serializable
data class Element(
    val atomicNumber: Int,
    val symbol: String,
    val name: String,
    val atomicMass: Double?,
    val category: String,
    val electronConfiguration: String?,
    val electronegativity: Double?,
    val atomicRadius: Double?,
    val ionizationEnergy: Double?,
    val density: Double?,
    val meltingPoint: Double?,
    val boilingPoint: Double?,
    val group: Int?,
    val period: Int?,
    val discoveredBy: String?,
    val yearDiscovered: Int?,
)
```

#### Element 中文模型
```kotlin
@Serializable
data class ElementZh(
    val atomicNumber: Int,
    val nameZh: String,
    val pinyin: String,
)
```

#### ElementRepository
- 启动时从 `assets/elements.json` + `assets/elements_zh.json` 一次性加载
- 内存中持有 `Map<Int, Element>` + `Map<Int, ElementZh>`
- 提供 `getByNumber(Int): Element?` / `getAll(): List<Element>` / `search(query: String): List<Element>`（按名称/符号/序号模糊查）
- 解析失败抛异常让 App 崩溃（数据完整性底线）

#### FavoritesRepository
- DataStore Preferences 持久化 `favorite_ids: Set<Int>`
- `isFavorite(Int): Flow<Boolean>` / `toggle(Int)` / `getAll(): Flow<Set<Int>>`

#### ThemePreferenceRepository
- DataStore Preferences 持久化 `theme_mode: String`（`system` / `light` / `dark`）
- `themeMode: Flow<ThemeMode>`

### 3. Koin DI (`app/src/main/java/com/periodic/pro/di/AppModule.kt`)
```kotlin
val appModule = module {
    single { ElementRepository(context = androidContext()) }
    single { FavoritesRepository(context = androidContext()) }
    single { ThemePreferenceRepository(context = androidContext()) }
}
```

### 4. 全局 Composable 组件库 (`app/src/main/java/com/periodic/pro/ui/components/`)

基于 `design/components.PNG` 设计稿 + `design/README.md` 第 3 章组件清单：

| 组件 | 文件 | 关键设计点 |
|------|------|-----------|
| **PeriodicButton** | `PeriodicButton.kt` | 紫色填充，圆角 `shapeMedium`(12dp)，高度 48dp，含 `enabled`/`loading` 态 |
| **PeriodicOutlinedButton** | `PeriodicOutlinedButton.kt` | 紫色描边 1dp，透明背景 |
| **PeriodicTextButton** | `PeriodicTextButton.kt` | 无边框无背景，紫色文字 |
| **ElementCard** | `ElementCard.kt` | 顶部 Category 色条(4dp高) + 原子序号 + 符号(titleLarge 22sp) + 中文名(bodyMedium)；`shapeMedium`(12dp)；L1 阴影；`modifier` 第一参数 |
| **CategoryChip** | `CategoryChip.kt` | 筛选 Chip，选中态填充 Category Color；`shapeExtraSmall`(4dp)；含 "全部" 选项 |
| **PeriodicSearchBar** | `PeriodicSearchBar.kt` | M3 `SearchBar` 或 `OutlinedTextField`；圆角 `shapeSmall`(8dp)；搜索 icon + clear 按钮 |
| **GlassSurface** | `GlassSurface.kt` | 已存在（Stage 2），验证可用 |

**组件规范（继承 component-guidelines.md）：**
- 每个 Composable 第一参数 `modifier: Modifier = Modifier`
- 每个组件带 `@Preview`（至少 1 个亮色 + 1 个暗色）
- ElementCard 通过 `LocalCategoryColors.current` 获取分类色
- 所有可点击元素最小 48dp 点击区
- 中文字符串通过参数传入（不硬编码）

---

## Acceptance Criteria

- [ ] `app/src/main/assets/elements.json` 118 元素完整，JSON 格式合法
- [ ] `app/src/main/assets/elements_zh.json` 118 元素中文名 + 拼音完整
- [ ] `Element` / `ElementZh` data class 定义完整，`@Serializable`
- [ ] `ElementRepository` 加载 + 查询 + 搜索可用
- [ ] `FavoritesRepository` DataStore 读写通过
- [ ] `ThemePreferenceRepository` DataStore 读写通过
- [ ] Koin `appModule` 注册 3 个 Repository
- [ ] 7 个 Composable 组件全部实现 + `@Preview`
- [ ] `./gradlew assembleDebug` 通过
- [ ] 组件遵循 `component-guidelines.md`（modifier 第一参数、State Hoisting、Slot API）

## Definition of Done

- `./gradlew lint` 通过（无新增警告）
- 组件可独立 Preview 渲染
- 数据集 JSON 可被 `kotlinx.serialization` 正确解析

## Out of Scope

- 周期表 18×9 网格布局（Stage 4）
- 5 屏真实 UI（Stage 4-7）
- 单测（Stage 8）
- 搜索 UseCase（本 Stage 只做 Repository 层搜索，UseCase 放 Home 屏 Stage 7）

## Technical Notes

- 数据集来源：`.trellis/tasks/05-10-compose-app-bootstrap/research/elements-dataset.md`
- 组件设计参考：`design/README.md` 第 3 章
- 组件规范：`.trellis/spec/frontend/component-guidelines.md`
- ElementCard 色条映射见 `design/README.md` 2.1 节 10 色分类表
