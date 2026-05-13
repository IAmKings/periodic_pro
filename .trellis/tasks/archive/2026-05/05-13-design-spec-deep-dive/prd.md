# 高保真设计稿深度解析修复

## Goal

根据 `design/design-spec.md` 中识别的偏差，按优先级修复所有 UI 一致性问题，确保代码 100% 对齐设计稿。

## 修复清单

### 🔴 P0 — 必须修复（影响设计一致性）

1. **GlassSurface tint 默认值** — 修正为 `0.15`/`0.25`
   - 文件：`ui/components/GlassSurface.kt`
   - 当前：`surface.copy(alpha=0.3f)`
   - 目标：浅色 `rgba(255,255,255,0.15)`，深色 `rgba(0,0,0,0.25)`

2. **Typography 补齐缺失字阶** — 补充 8 个缺失字阶
   - 文件：`theme/Typography.kt`
   - 缺失：Display Medium(45sp)、Display Small(36sp)、Headline Large(32sp)、Headline Small(24sp)、Title Small(14sp)、Body Small(12sp)、Label Large(14sp)
   - 保留现有 7 个字阶不变

3. **Color.kt 补齐 M3 完整 Token** — 补充 4 个缺失 Token
   - 文件：`theme/Color.kt`
   - 缺失：Primary Container、Secondary Container、On Surface Variant、Outline
   - 同步更新 `PeriodicProTheme.kt` 中的 `lightColorScheme`/`darkColorScheme`

4. **Shapes 补充 20.dp** — `radiusXXLarge`
   - 文件：`theme/Shapes.kt`
   - 新增：`extraExtraLarge = RoundedCornerShape(20.dp)`

### 🟡 P1 — 建议修复（提升一致性）

5. **ElementCard 字号/内边距核对**
   - 文件：`ui/components/ElementCard.kt`
   - 原子序号 → Label Small(11sp)
   - 元素符号 → Title Large(22sp)
   - 内边距 → 12.dp
   - 顶部色条 → 4.dp（已有，确认）

6. **CategoryChip 非选中态描边色**
   - 文件：`ui/components/CategoryChip.kt`
   - 非选中态边框 → `Outline` 色（当前可能是 `On Surface Variant`）

7. **PropertyGrid 分隔线样式**
   - 文件：`ui/pattern/PropertyGrid.kt`
   - 分隔线 → 0.5dp，`Outline` 色

8. **Detail 页分类标签改用 PropertyChip**
   - 文件：`feature/detail/DetailScreen.kt`
   - 当前使用 `CategoryChip` 或直接文本，应改用 `PropertyChip`

### 🟢 P2 — 可选优化（提升可维护性）

9. **DashboardGrid 提取为独立 Pattern** — `ui/pattern/DashboardGrid.kt`
10. **ComparisonTable 提取为独立 Pattern** — `ui/pattern/ComparisonTable.kt`
11. **ElementCard 大卡片变体** — `ui/components/ElementCard.kt` 增加 `size: ElementCardSize` 参数
12. **FavoriteElementCard（swipe-to-delete）** — `ui/components/FavoriteElementCard.kt`

## Decision (ADR-lite)

**本轮范围**：P0（4 项）+ P1（4 项），共 8 项

**P2（4 项）**：Out of Scope，后续迭代

## Acceptance Criteria

- [ ] GlassSurface tint 精确匹配设计稿（浅色 0.15 / 深色 0.25）
- [ ] Typography 补齐 7 个缺失字阶（共 14 阶完整 M3 字阶）
- [ ] Color.kt 补齐 4 个缺失 M3 Token
- [ ] Shapes 新增 20.dp extraExtraLarge
- [ ] ElementCard 字号/内边距与设计稿一致
- [ ] CategoryChip 非选中态描边为 Outline 色
- [ ] PropertyGrid 分隔线 0.5dp Outline 色
- [ ] Detail 页分类标签使用 PropertyChip
- [ ] 所有修改后的组件 @Preview 正确（Light + Dark）
- [ ] `./gradlew assembleDebug lint` 通过
- [ ] `./gradlew detekt` 通过

## Definition of Done

* 修改后的组件含 @Preview（Light + Dark 双预览）
* 代码通过 lint + detekt
* 不破坏现有 8 屏功能

## Out of Scope

* P2 项（DashboardGrid / ComparisonTable 提取、ElementCard 大卡片、FavoriteElementCard）
* Lab / Learn 屏开发
* 3D Viewer 真 3D

## Technical Notes

* 设计规范：`design/design-spec.md`
* 参考设计稿：`design/design_system.PNG`、`design/components.PNG`
* 所有修改必须在现有 MVI 架构下兼容
