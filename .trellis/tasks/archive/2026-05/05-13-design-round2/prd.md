# design续期: Discover屏 + Learn屏 + Lab屏

## Goal

对照 `design/README.md`，补齐最后 3 个缺失屏面：Discover（发现）、Learn（学习）、Lab（实验），完成设计稿 10 屏全量落地。

## What I already know

* 7 屏已完成（Home / Table / Detail / Compare / Favorites / Profile / Category）
* MVI 模式、Category 枚举、Theme 接线、NavigationSuite 5 tab 全部就位
* 设计稿对最后 3 屏描述极简：
  - **Discover**：发现页，元素趣味知识
  - **Learn**：元素学习资料
  - **Lab**：化学反应小实验模拟
* 这 3 屏与之前 7 屏的核心差异：**需要内容数据**（趣味知识、学习资料、反应方程式），不是纯 UI

## Assumptions (temporary)

* Discover 和 Learn 可能需要内置 JSON 数据集（类似 elements.json）
* Lab 屏化学反应模拟可能复杂度较高，MVP 可考虑简化
* 3 屏可能不需要全部做完，取决于内容和复杂度

## Decision (ADR-lite)

**本轮范围**：Discover 屏（仅此一屏）

**Learn / Lab 屏**：Out of Scope，后续迭代

## Open Questions

（已全部解决）

## Requirements

### Discover 屏 (`feature/discover/`)

**内容策略**：32 条精选知识点（热门/有趣元素），JSON 数据集 `assets/discover.json`

**展示模式**：
- **每日推荐 Hero Card**：顶部大卡片，随机展示当日推荐元素，含元素符号 + 趣味标题 + 简短描述 + 点击跳详情
- **Feed 流**：下方 LazyColumn，卡片列表展示全部知识点
- 点击任意卡片 → 跳转元素详情 (Routes.detail)
- 复用 ElementCard + 新建 DiscoverCard / DiscoverHeroCard

**导航入口**：NavigationSuite 新增 Discover tab（第 6 个 tab）

**数据模型**：
```kotlin
// data/discover/model/DiscoverItem.kt
@Serializable
data class DiscoverItem(
    val atomicNumber: Int,
    val title: String,       // "最轻的金属"
    val description: String, // "锂是密度最小的金属..."
    val funFact: String?,    // 额外的趣味小贴士
)

// data/discover/DiscoverRepository.kt
class DiscoverRepository(private val context: Context) {
    suspend fun loadAll(): Result<List<DiscoverItem>>
    fun getDailyRecommend(): DiscoverItem  // 基于当天日期随机
}
```

**Discover JSON 样例**（`assets/discover.json`）：
```json
[
  {
    "atomicNumber": 3,
    "title": "最轻的金属",
    "description": "锂是密度最小的金属元素，密度仅为 0.534 g/cm³，可以浮在水面上（但会剧烈反应）。",
    "funFact": "锂离子电池让现代手机、笔记本和电动车成为可能，2019 年诺贝尔化学奖授予锂离子电池的发明者。"
  },
  ...
]
```

**MVI 架构**：
- `DiscoverScreen.kt` + `DiscoverViewModel.kt` + `DiscoverUiState.kt` + `DiscoverIntent.kt`
- Koin 注册 `DiscoverViewModel`
- 导航路由 `Routes.DISCOVER`
- NavigationSuite 新增 Discover tab（6th tab）

### Learn 屏 → Out of Scope
### Lab 屏 → Out of Scope

## Acceptance Criteria

* [ ] `assets/discover.json` 包含 ≥ 32 条知识点
* [ ] Discover 屏 MVI 完整（Screen + ViewModel + UiState + Intent）
* [ ] 每日推荐 Hero Card + Feed 流展示
* [ ] 点击卡片跳转元素详情
* [ ] NavigationSuite 新增 Discover tab（第 6 个）
* [ ] 路由 `Routes.DISCOVER` + PeriodicNav 注册
* [ ] Koin 注册 DiscoverViewModel + DiscoverRepository
* [ ] `./gradlew assembleDebug lint` 通过
* [ ] `./gradlew detekt` 通过

## Definition of Done

* 新增 feature 屏含完整的 MVI（Screen + ViewModel + UiState + Intent）
* Koin 注册新 ViewModel
* 代码通过 lint + detekt

## Out of Scope (explicit)

* 真 3D 渲染
* 后端 API（数据全本地）

## Technical Notes

* 设计规范：`design/README.md`
* 7 屏代码基：`feature/` 下 7 个包
* 数据模式参考：`data/element/model/Element.kt`, `assets/elements.json`
