# Stage 7: Home 屏 + 收藏屏 + 全局搜索

## Goal

实现 Home 屏（欢迎区 + 推荐元素 + 搜索入口）+ Favorites 屏（收藏列表 + swipe 删除 + 空状态 + DataStore 持久化）+ 全局搜索 UseCase。MVP 5 屏合龙。

## 父任务

`.trellis/tasks/05-10-compose-app-bootstrap`（Stage 6 已完成：对比屏）

## Requirements

### A. Home 屏 (`feature/home/`)

#### 布局（从上到下）
1. **欢迎区**：App Logo/标题 "周期表 Pro" + 副标题 "探索 118 种化学元素"
2. **搜索框**：`PeriodicSearchBar`，输入跳转到 Table 屏并带搜索 query
3. **推荐元素**：横向 `LazyRow` 展示 5-8 个热门元素（H/He/C/O/Fe/Cu/Au/Hg），每项 `ElementCard`
4. **快捷入口卡片矩阵**（Dashboard 2×2）：
   - 周期表 → Table 屏
   - 元素对比 → Compare 屏
   - 我的收藏 → Favorites 屏
   - (预留第四格，显示"更多功能"占位)

#### HomeViewModel
- 加载推荐元素列表
- 搜索 query 传递

### B. Favorites 屏 (`feature/favorites/`)

#### 布局
1. TopAppBar: "我的收藏"
2. 收藏列表：`LazyColumn` + `FavoriteElementCard`（基于 ElementCard + 右上角心形图标）
3. **Swipe-to-delete**：`SwipeToDismissBox` 左滑删除 → 调 `favoritesRepo.toggle()`
4. **空状态**：图标 + "还没有收藏任何元素" + "去周期表看看"按钮
5. DataStore 持久化联动：收藏/取消收藏实时刷新列表

#### FavoritesViewModel
- 监听 `favoritesRepo.favoritesFlow` → `combine` with `elementRepo.getAll()`
- 过滤出收藏的元素列表

### C. 全局搜索 UseCase (`domain/usecase/SearchElementsUseCase.kt`)
- 输入 `query: String` + `elements: List<Element>` + `zhMap: Map<Int, ElementZh>`
- 返回匹配的元素列表（按名称/符号/序号/中文名/拼音）
- 大小写不敏感

### D. Navigation 集成

- Home 搜索框 → 跳到 Table 屏 + 传递搜索 query
- Dashboard 卡片 → 对应屏面导航
- Favorites 列表项点击 → Detail 屏
- Favorites → 空状态按钮 → Table 屏

---

## Acceptance Criteria

- [ ] Home 屏：欢迎区 + 搜索框 + 推荐 LazyRow + Dashboard 2×2 卡片
- [ ] Favorites 屏：列表 + swipe 删除 + 空状态 + DataStore 联动
- [ ] 收藏持久化：关 App 重新打开，收藏仍在
- [ ] 搜索 UseCase 可用
- [ ] `./gradlew assembleDebug` 通过
- [ ] 5 屏互相导航可达（Home ↔ Table ↔ Detail ↔ Compare ↔ Favorites）

## Out of Scope
- Profile/Settings 屏（后续迭代）
- 搜索历史记录
- 推荐算法（本 Stage 硬编码热门元素列表）
