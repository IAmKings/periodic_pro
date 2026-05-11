# Stage 4: 周期表屏（Periodic Table Screen）

## Goal

实现 18×9 标准周期表网格布局（含镧系/锕系独立行），集成 Telephoto 缩放/平移手势、分类 CategoryChip 筛选、点击进 Detail、长按收藏、多选模式 → Compare，手机/平板/横屏自适应。

## 父任务

`.trellis/tasks/05-10-compose-app-bootstrap`（Stage 3 已完成：数据层 + 组件）

## Requirements

### 1. 网格布局

- **自定义 `Layout` Composable**（非 LazyVerticalGrid），实现标准周期表 18 列 × 7 行（Period 1-7）+ 镧系/锕系独立 2 行
- 每个元素格最小 48dp × 48dp 点击区
- 格子内容：原子序号(labelSmall) + 符号(titleLarge) + 可选中文名(labelSmall)
- 顶部 4dp 高 Category 色条（通过 `LocalCategoryColors.current.forCategory()` 获取）
- 空位（如第 1 周期只有 H/He）渲染为透明占位

**周期表标准布局映射：**
```
Period 1: [H,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ..., He]
Period 2: [Li, Be,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ..., B,  C,  N,  O,  F,  Ne]
Period 3: [Na, Mg,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ...,  ..., Al, Si, P,  S,  Cl, Ar]
Period 4: [K,  Ca, Sc, Ti, V,  Cr, Mn, Fe, Co, Ni, Cu, Zn, Ga, Ge, As, Se, Br, Kr]
Period 5: [Rb, Sr, Y,  Zr, Nb, Mo, Tc, Ru, Rh, Pd, Ag, Cd, In, Sn, Sb, Te, I,  Xe]
Period 6: [Cs, Ba, *,  Hf, Ta, W,  Re, Os, Ir, Pt, Au, Hg, Tl, Pb, Bi, Po, At, Rn]
Period 7: [Fr, Ra, **, Rf, Db, Sg, Bh, Hs, Mt, Ds, Rg, Cn, Nh, Fl, Mc, Lv, Ts, Og]
Lanthanide: [La, Ce, Pr, Nd, Pm, Sm, Eu, Gd, Tb, Dy, Ho, Er, Tm, Yb, Lu]
Actinide:   [Ac, Th, Pa, U,  Np, Pu, Am, Cm, Bk, Cf, Es, Fm, Md, No, Lr]
```
- Period 6/7 的 `*` / `**` 占位点击跳转到镧系/锕系行

### 2. 手势交互

- **Telephoto `Modifier.zoomable()`** 缩放/平移（含 fling、双击缩放、边界回弹）
- `onClick`：点击元素 → 导航到 `DetailPage(atomicNumber)`
- `onLongClick`：长按元素 → 进入多选模式（同时收藏该元素）
- 缩放时自动隐藏小文字（labelSmall），只保留符号

### 3. 分类筛选

- 顶部 `LazyRow` 排列 `CategoryChip`（10 个分类 + "全部"）
- 选中态过滤显示（非选中分类的元素 alpha=0.3 或完全隐藏）
- "全部" = 恢复全部显示

### 4. 多选模式

- 长按进入多选，顶部切换为 `ContextualTopAppBar`（显示已选数量 + 取消 + 确认按钮）
- 确认按钮 → 导航到 `ComparePage`（传递 `Set<Int>` 选中元素 ID）
- 多选状态用 `rememberSaveable<Set<Int>>` 管理
- BackHandler 按返回退出多选模式

### 5. 搜索功能

- 顶部 `PeriodicSearchBar`（已在 Stage 3 实现）
- 输入时过滤格子（匹配的元素高亮/非匹配降低 alpha），搜索结果实时展示

### 6. 响应式适配

| 屏幕宽度 | 布局策略 |
|---------|---------|
| Compact（手机竖屏） | 横向可滑（`horizontalScroll`），默认拟合宽度 |
| Medium（折叠/横屏） | 自适应 cellSize，每个格子稍大 |
| Expanded（平板） | 嵌入 `ListDetailPaneScaffold` 双面板（左：周期表，右：快捷预览） |

### 7. Feature 包结构

```
feature/table/
├── TableScreen.kt       // 主 Composable（搜索 + Chips + 网格 + 多选）
├── TableViewModel.kt    // ViewModel（过滤逻辑 + 多选状态）
├── TableUiState.kt      // data class（searchQuery / selectedCategory / isMultiSelect / selectedIds / zoomScale）
├── TableIntent.kt       // sealed interface（Search / FilterCategory / ToggleElement / EnterMultiSelect / ExitMultiSelect / ConfirmCompare）
└── PeriodicTableGrid.kt // 核心自定义 Layout（18×9 网格 + Telephoto zoomable）
```

遵循 `state-management.md` UDF 范式：TableViewModel 持有 StateFlow<TableUiState>，TableScreen 通过 `collectAsStateWithLifecycle()` 收集。

---

## Acceptance Criteria

- [ ] 周期表 18×9 网格 + 镧系/锕系独立行正确渲染
- [ ] 空位占位正确（第 1 周期仅 H/He）
- [ ] Telephoto 缩放/平移可用（双击缩放 + fling + 边界回弹）
- [ ] 点击元素 → 导航到 Detail 屏（传 atomicNumber）
- [ ] 长按元素 → 进入多选 + 收藏该元素
- [ ] CategoryChip 筛选生效（10 分类 + "全部"）
- [ ] PeriodicSearchBar 输入过滤实时生效
- [ ] 多选模式：ContextualTopAppBar + 已选计数 + 确认→Compare + 取消 + BackHandler
- [ ] 手机竖屏/横屏/平板 3 种布局可用
- [ ] 缩放时小文字隐藏
- [ ] `./gradlew assembleDebug` 通过

## Definition of Done

- 遵循 `component-guidelines.md` 全部规范
- `feature/table/` 内 5 文件 UDF 范式正确
- `./gradlew lint` 通过（无新增警告）

## Out of Scope

- Detail 屏真实 UI（Stage 5）
- Compare 屏真实 UI（Stage 6）
- Favorites 持久化回调（长按收藏调用 FavoritesRepository.toggle，但 Stage 4 只做调用不做 UI 反馈）
- 平板双面板 `ListDetailPaneScaffold`（本 Stage 先做 `BoxWithConstraints` 判断宽度 + `isWidthAtLeastBreakpoint`，平板布局骨架即可）

## Technical Approach

参考研究：
- 网格布局：`.trellis/tasks/05-10-compose-app-bootstrap/research/compose-periodic-table.md`
- 导航规范：`.trellis/tasks/05-10-compose-app-bootstrap/research/navigation-suite-scaffold.md`

**关键实现要点：**
1. Telephoto `Modifier.zoomable()` 的 `onClick`/`onLongClick` 槽位替代外层 `combinedClickable`（否则手势冲突）
2. Category 色通过 `LocalCategoryColors.current.forCategory(element.category)` 获取
3. 多选 `ContextualTopAppBar` 通过 `BackHandler` + `AnimatedVisibility` 切换
4. 空位用 `Spacer(Modifier.size(cellSize))` 占位
