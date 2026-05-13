# Learn屏 + Lab屏: 最后2屏收尾

## Goal

实现最后两屏：Learn（学习）和 Lab（实验），完成 10 屏全量落地。

## What I already know

* 8 屏已完成（Home / Table / Detail / Compare / Favorites / Category / Profile / Discover）
* MVI 模式、Category 枚举、NavigationSuite 6 tab 全部就位
* 设计稿对这两屏描述极简：
  - **Learn**：元素学习资料
  - **Lab**：化学反应小实验模拟
* 两屏的核心挑战：**内容数据量**（Learn）和**逻辑复杂度**（Lab）

## Learn 屏分析

### 内容结构（每个元素）
```
发现历史（1-2 段） + 实际应用（2-3 条） + 趣味知识点（1-2 条）
```

### 数据规模选项

| 方案 | 覆盖 | 工作量 |
|------|------|--------|
| A | 30-40 个重点元素（H, He, C, O, Fe, Au, U 等） | 中（每元素 200-300 字） |
| B | 118 全元素 | 高（每元素 100-150 字简版） |
| C | 先用 20 个核心元素 + 占位内容 | 低（快出 MVP） |

### UI 布局
- 元素选择入口：按分类浏览或搜索
- 详情页：发现历史 + 应用场景 + 趣味知识
- 可收藏/分享

## Lab 屏分析

### 复杂度选项

| 方案 | 描述 | 工程复杂度 |
|------|------|:--:|
| A | **简单方程式展示**：选 2 个元素，查出化学反应方程式，图文展示 | 低（需要反应数据库） |
| B | **可视化动画**：原子模型碰撞动画 + 产物展示 | 中（Canvas 动画） |
| C | **完整交互模拟**：自由搭配元素+化合物，配平方程式，计算产物 | 高（化学引擎） |

### 化学反应数据
- 需要预置反应数据库（`reactions.json`）
- 每条：反应物 [元素ID] + 产物 [化合物] + 方程式 + 反应条件 + 描述
- 预估 15-20 个经典反应

### UI 布局
- 反应物选择区（从周期表选元素或从列表选）
- 反应方程式展示区
- 产物/描述区
- 简单动画（A→B 过渡）

## Decision (ADR-lite)

### Learn 屏：三阶段分层递进

| 阶段 | 等级 | 数量 | 内容深度 | 验收 |
|:--:|------|------|------|:--:|
| **1** | 🔴 核心 | 20 元素 | 发现历史(150-200字) + 应用(2-3条) + 趣味知识(1-2条) | 先验收 |
| **2** | 🟡 重点 | 30-40 元素 | 发现历史(100-150字) + 应用(1-2条) + 趣味知识(1条) | 通过后继续 |
| **3** | 🟢 普通 | ≈58-68 元素 | 简短介绍(50-80字) + 应用(1条) | 全量收尾 |

**等级字段**：`LearnItem.level: "core" | "important" | "normal"`

**阶段 1 核心元素**（20个）：H, He, C, N, O, F, Na, Mg, Al, Si, P, S, Cl, K, Ca, Fe, Cu, Zn, Ag, Au

### Lab 屏：初中 + 高中全部覆盖

| 阶段 | 来源 | 数量 | 特点 |
|:--:|------|------|------|
| **初中** | 初三化学 | 15-20 个 | 基础反应、现象直观、入门友好 |
| **高中** | 高一~高三 | 25 个 | 覆盖化合/分解/置换/复分解/氧化还原 |

**总计 40-45 个反应**，按 `level: "junior" | "senior"` 区分

## Requirements

### 子任务 1: Learn 屏 Phase 1 — 20 核心元素 (`learn-phase1-core`)

- 新建 `feature/learn/` MVI（Screen + ViewModel + UiState + Intent）
- 新建 `data/learn/model/LearnItem.kt` — 含 `level: String` 字段
- 新建 `data/learn/LearnRepository.kt`
- `assets/learn.json` — 20 个核心元素完整内容
- UI：元素列表入口 + 学习详情页（发现历史 / 应用 / 趣味知识）
- 元素列表按等级分组显示（核心 / 重点 / 普通三个 Section）
- 列表卡片显示等级标签（核心=红 / 重点=黄 / 普通=绿）
- 详情页底部"查看元素详情"按钮 → 跳转 Detail
- NavigationSuite 新增 Learn tab（第 7 个）
- Koin 注册 LearnRepository + LearnViewModel

### 子任务 2: Learn 屏 Phase 2 — 30-40 重点元素 (`learn-phase2-important`)

- 扩展 `assets/learn.json` 增加 30-40 个重点元素（中等深度）
- 更新 LearnRepository 兼容增量数据
- 验收后进入 Phase 3

### 子任务 3: Learn 屏 Phase 3 — 118 全量 (`learn-phase3-full`)

- 扩展 `assets/learn.json` 到全部 118 元素
- 普通元素为简短介绍（50-80 字）
- 三种等级完整覆盖

### 子任务 4: Lab 屏 — 40+ 经典反应 (`lab-reactions`)

- 新建 `feature/lab/` MVI
- 新建 `data/lab/model/ChemicalReaction.kt` + 枚举（`ReactionType`, `ReactionLevel`）
- 新建 `data/lab/LabRepository.kt`
- `assets/reactions.json` — 40-45 个反应（初中 15-20 + 高中 25）
- UI：反应列表（按类型分组 + StickyHeader）+ 反应详情
- 详情页：方程式(大字) + 条件 + 现象 + 涉及元素（点击跳 Detail）
- 反应类型标签色
- NavigationSuite 新增 Lab tab（第 8 个）
- Koin 注册 LabRepository + LabViewModel

**初中经典反应**（15-20个）：
- 加热高锰酸钾制氧气：2KMnO₄→K₂MnO₄+MnO₂+O₂↑
- 过氧化氢分解：2H₂O₂→2H₂O+O₂↑
- 氯酸钾制氧气：2KClO₃→2KCl+3O₂↑
- 电解水：2H₂O→2H₂↑+O₂↑
- 镁条燃烧：2Mg+O₂→2MgO
- 铁丝在氧气中燃烧：3Fe+2O₂→Fe₃O₄
- 红磷燃烧：4P+5O₂→2P₂O₅
- 木炭燃烧：C+O₂→CO₂
- 硫燃烧：S+O₂→SO₂
- 氢气燃烧：2H₂+O₂→2H₂O
- 锌与稀硫酸：Zn+H₂SO₄→ZnSO₄+H₂↑
- 铁与硫酸铜：Fe+CuSO₄→FeSO₄+Cu
- 二氧化碳通入石灰水：CO₂+Ca(OH)₂→CaCO₃↓+H₂O
- 碳酸钠与盐酸：Na₂CO₃+2HCl→2NaCl+H₂O+CO₂↑
- 一氧化碳还原氧化铜：CO+CuO→Cu+CO₂
- 氢气还原氧化铜：H₂+CuO→Cu+H₂O
- 生石灰与水：CaO+H₂O→Ca(OH)₂
- 甲烷燃烧：CH₄+2O₂→CO₂+2H₂O

**高中补充反应**（25个，见原方案基础上调整，避免与初中重复）：已在上方列出

## Acceptance Criteria

* [ ] Learn Phase 1: 20 核心元素，`level` 字段正确，UI 三等级分组
* [ ] Learn Phase 2: 30-40 重点元素追加
* [ ] Learn Phase 3: 118 全量覆盖
* [ ] Lab: 40-45 个反应，初中+高中完整
* [ ] NavigationSuite 8 tab（Home/Table/Compare/Favorites/Category/Discover/Learn/Lab/Profile）
* [ ] 全部编译/lint/detekt 通过
* [ ] 每个阶段独立可验收

## Acceptance Criteria

* [ ] Learn 屏 MVI 完整（Screen + ViewModel + UiState + Intent）
* [ ] `assets/learn.json` ≥ 20 个元素学习内容
* [ ] Lab 屏 MVI 完整（Screen + ViewModel + UiState + Intent）
* [ ] `assets/reactions.json` ≥ 25 个经典反应
* [ ] NavigationSuite 新增 Learn + Lab 两个 tab（第 7、8 个）
* [ ] 路由 `Routes.LEARN` + `Routes.LAB` + `Routes.LAB_DETAIL(reactionId)`
* [ ] Koin 注册 LearnViewModel + LabViewModel + LabRepository
* [ ] 点击 Learn 元素 → 跳转 Detail 屏
* [ ] 点击 Lab 反应 → 反应详情
* [ ] `./gradlew assembleDebug lint` 通过
* [ ] `./gradlew detekt` 通过

## Definition of Done

* 两屏 MVI 完整
* Koin 注册所有新类
* @Preview 含 Light + Dark
* lint + detekt 绿

## Out of Scope (explicit)

* 真 3D 渲染
* 化学反应动画（Canvas 动态模拟）
* 后端 API
* 方程式自动配平引擎

## Definition of Done

* 新增 feature 屏含完整的 MVI（Screen + ViewModel + UiState + Intent）
* Koin 注册新 ViewModel
* 代码通过 lint + detekt

## Out of Scope (explicit)

* 真 3D 渲染
* 后端 API

## Technical Notes

* 设计规范：`design/design-spec.md`、`design/README.md`
* 现有 8 屏代码基参考
