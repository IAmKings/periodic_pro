# Stage 5: 元素详情屏（Element Detail Screen）

## Goal

实现元素详情页：Canvas 2D 原子动画头部 + PropertyGrid 属性网格 + 化学/物理属性区块 + 收藏按钮。路由参数：原子序号。

## 父任务

`.trellis/tasks/05-10-compose-app-bootstrap`（Stage 4 已完成：周期表屏）

## Requirements

### 1. Feature 包结构

```
feature/detail/
├── DetailScreen.kt        // 主 Composable（原子动画 + 属性网格 + 收藏 FAB）
├── DetailViewModel.kt     // ViewModel（加载元素 + 收藏状态）
├── DetailUiState.kt       // data class
└── DetailIntent.kt        // sealed interface
```

### 2. 页面布局（从上到下）

#### 2.1 头部：Canvas 2D 原子动画占位 (`ui/pattern/AtomCanvas.kt`)
- Canvas 绘制：
  - 中心原子核圆球（质子数 = 原子序号，按比例缩放）
  - 电子层轨道圆环（周期数 = 层数）
  - 电子小球在轨道上匀速转动
- `rememberInfiniteTransition` 驱动旋转动画
- 高度约 240dp
- 原子核颜色 = 该元素 Category 色

#### 2.2 元素基本信息区
- 原子序号（displayLarge 57sp）
- 元素符号（headlineMedium 28sp）
- 中文名（titleMedium 16sp）
- 英文名（bodyMedium 14sp）
- 分类标签（CategoryChip，选中态）
- 收藏按钮（`IconButton` + 心形 Icon，已收藏填充/未收藏空心）

#### 2.3 PropertyGrid（`ui/pattern/PropertyGrid.kt`）
2 列网格展示属性：

| 属性名 | 字段 | 单位 |
|--------|------|------|
| 原子量 | atomicMass | u |
| 密度 | density | g/cm³ |
| 熔点 | meltingPoint | K |
| 沸点 | boilingPoint | K |
| 电负性 | electronegativity | Pauling |
| 原子半径 | atomicRadius | pm |
| 电离能 | ionizationEnergy | eV |
| 电子排布 | electronConfiguration | — |

- 2 列布局：`LazyVerticalGrid(columns = GridCells.Fixed(2))`
- 每项：`PropertyItem(name, value, unit)`
- null 值显示 "—"

#### 2.4 额外信息区
- 发现者 (`discoveredBy`)
- 发现年份 (`yearDiscovered`)
- Group / Period 编号

### 3. 交互

- 顶部返回箭头（`TopAppBar` + `navigationIcon`）
- 收藏按钮（调用 `FavoritesRepository.toggle()`）
- null 属性值显示 "—" 不外漏

### 4. DetailViewModel
```kotlin
class DetailViewModel(
    private val elementRepo: ElementRepository,
    private val favoritesRepo: FavoritesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val atomicNumber: Int = savedStateHandle.get<Int>("atomicNumber")!!
    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadElement()
        observeFavorite()
    }
    fun handle(intent: DetailIntent) { ... }
}
```

### 5. Navigation
- 路由：`detail/{atomicNumber}`
- 从 Table 屏点击 → `navController.navigate("detail/$atomicNumber")`
- SavedStateHandle 获取参数

---

## Acceptance Criteria

- [ ] Canvas 2D 原子动画渲染（原子核 + 电子轨道 + 旋转电子）
- [ ] PropertyGrid 2 列展示 8 个属性，null 值显示 "—"
- [ ] 收藏按钮切换（填充/空心），调用 FavoritesRepository
- [ ] 顶部返回箭头可用
- [ ] Category 色在原子动画头部和分类标签中正确显示
- [ ] `./gradlew assembleDebug` 通过

## Definition of Done

- UDF 范式：DetailViewModel + sealed Intent + StateFlow
- 所有 null 值安全兜底
- `./gradlew lint` 通过

## Out of Scope

- 真实 3D Viewer（SceneView/Filament）
- 编辑/分享功能
- 动画性能优化（后续抛光）
