# design-spec 全量合规审计

## Goal

对照 `design/design-spec.md` UI 一致性强制规则，扫描全部代码，标记违规项。

## 审计结果

### ✅ 合规项

| 规则 | 状态 |
|------|:--:|
| Color.kt / Typography.kt / Shapes.kt / Dimensions.kt / Elevation.kt 定义文件 | ✅ |
| ElementCard 使用 Elevation.Shadow1 | ✅ |
| HomeScreen DashboardCard 使用 Elevation.Shadow2 | ✅ |
| 大部分间距正确使用 Dimensions 常量 | ✅ |

### 🔴 硬编码颜色（Color(0xFF...)）— 7 处违规

| 文件 | 行 | 内容 | 应改用 |
|------|:--:|------|--------|
| `LabScreen.kt` | 71-86 | 12 个反应类型色 + 初中/高中标签色 | Theme 提取至 Color.kt |
| `LearnScreen.kt` | 59-65 | 核心/重点/其他 6 个色值 | Theme 提取至 Color.kt |
| `DiscoverScreen.kt` | 196,298,307 | `Color(0xFF4DABF7)` 硬编码 | CategoryColors.metalloid |
| `PeriodicTableGrid.kt` | 324 | `Color(0xFFE0E0E0)` 硬编码 | MaterialTheme.colorScheme.surfaceVariant |
| `AtomCanvas.kt` | 93 | `Color(0xFF4FC3F7)` 电子颜色 | 提取常量 |
| `PropertyChip.kt` | 80,94 | Preview 硬编码 | 保留（Preview） |

### 🟡 硬编码 dp（未用 Dimensions 常量）— 12 处违规

| 文件 | 行 | 值 | 应改用 |
|------|:--:|----|--------|
| `ElementCard.kt` | 56-57 | `88.dp, 100.dp` | `Dimensions` 或设计卡片尺寸常量 |
| `ElementCard.kt` | 69 | `height(4.dp)` | `Dimensions.Dp4` |
| `ElementCard.kt` | 77 | `padding(12.dp)` | `Dimensions.Dp12` |
| `EmptyState.kt` | 58 | `size(64.dp)` | `Dimensions.Dp64` |
| `ProfileScreen.kt` | 269 | `size(56.dp)` | `Dimensions.Dp56` |
| `HomeScreen.kt` | 241 | `height(100.dp)` | 不标准（非 8 倍数） |
| `DiscoverScreen.kt` | 212 | `height(6.dp)` | 非 8pt |
| `LabScreen.kt` | 411,611 | `6.dp, 2.dp` | 非 8pt |
| `LearnScreen.kt` | 345,672,696 | `6.dp, 2.dp` | 非 8pt |
| `LearnScreen.kt` | 310,296 | `44.dp, 22.dp` | 非 8 倍数 |
| `DiscoverScreen.kt` | 296 | `size(44.dp)` | 非 8 倍数 |
| `GlassSurface.kt` | 84,124,128,135 | 各类硬编码 | 语义化常量 |

### 🟡 硬编码 elevation（未用 Elevation 常量）— 4 处

| 文件 | 行 | 值 | 应改用 |
|------|:--:|----|--------|
| `DiscoverScreen.kt` | 202 | `3.dp` | `Elevation.Shadow2` |
| `DiscoverScreen.kt` | 282 | `1.dp` | `Elevation.Shadow1` |
| `LabScreen.kt` | 378 | `1.dp` | `Elevation.Shadow1` |
| `LearnScreen.kt` | 296 | `1.dp` | `Elevation.Shadow1` |
| `CategoryScreen.kt` | 203 | `1.dp` | `Elevation.Shadow1` |

### 🟡 硬编码 fontSize — 1 处（PeriodicTableGrid）

| 文件 | 行 | 值 | 应改用 |
|------|:--:|----|--------|
| `PeriodicTableGrid.kt` | 278 | `fontSize = 9.sp` | `MaterialTheme.typography.labelSmall` |
| `PeriodicTableGrid.kt` | 287 | `fontSize = 12.sp` | `MaterialTheme.typography.labelMedium` |
| `PeriodicTableGrid.kt` | 298 | `fontSize = 8.sp` | `MaterialTheme.typography.labelSmall` 或更小 |

### 🟡 硬编码 RoundedCornerShape — 6 处

| 文件 | 行 | 值 | 应改用 |
|------|:--:|----|--------|
| `PeriodicTableGrid.kt` | 250,318 | `4.dp` | `MaterialTheme.shapes.extraSmall` |
| `LabScreen.kt` | 355,670,694 | `4.dp` | `MaterialTheme.shapes.extraSmall` |
| `LearnScreen.kt` | 253,343 | `4.dp` | `MaterialTheme.shapes.extraSmall` |
| `LearnScreen.kt` | 313 | `22.dp` | 非标准圆角 |
| `DiscoverScreen.kt` | 299 | `22.dp` | 非标准圆角 |

### 🟢 轻量违规（Divider / Chip 微间距）

| 文件 | 行 | 内容 | 判定 |
|------|:--:|------|------|
| `PeriodicTableGrid.kt` | 254,273,322 | `1.dp, 2.dp` 微间距 | 网格内部微调，可接受 |
| `CategoryChip.kt` | 57 | `width = 1.dp` 描边 | 标准 Chip 描边，可接受 |
| `PropertyChip.kt` | 46 | `width = 1.dp` 描边 | 同上 |
| 各屏 Divider | 多文件 | `thickness = 0.5.dp` | 分割线标准值，可接受 |

## 修复优先级

### 🔴 P0 — 必须修复
1. LabScreen 12 个反应类型色 → 提取至 Color.kt（LabColorScheme）
2. LearnScreen 6 个等级色 → 提取至 Color.kt（LearnBadgeColors）
3. DiscoverScreen 硬编码 `Color(0xFF4DABF7)` → 改用 CategoryColors
4. PeriodicTableGrid 硬编码色 → 改用 theme

### 🟡 P1 — 建议修复
5. 硬编码 elevation → 改用 Elevation 常量 (5 处)
6. 硬编码 dp → 改用 Dimensions 常量 (12 处)
7. 硬编码 fontSize → 改用 Typography (3 处)

### 🟢 P2 — 可选
8. 硬编码 RoundedCornerShape → 改用 Shapes (6 处)
9. 微间距/Divider 标准化 (保持)

## Acceptance Criteria

- [ ] P0: 所有 feature 文件零硬编码 Color
- [ ] P1: 所有 elevation 使用 Elevation 常量
- [ ] P1: 所有间距使用 Dimensions 常量（8pt Grid）
- [ ] P1: 所有 fontSize 使用 Typography 字阶
- [ ] `./gradlew assembleDebug lint` 通过
