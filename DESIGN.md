# 周期表 Pro · DESIGN.md

> **Spectra** — 设计语言 v1.0
> 「仪器美学」：像一台精密的科学仪器，而不是一个模板应用。
>
> 版本：v1.0 · 2026-11 · 适用范围：全部 UI 表现层（不涉及功能变更）

---

## 0. 设计定位与参考

### 0.1 一句话定位

> **中性石墨表面 + 语义光谱色 + 仪器级排印。**
> 颜色只用于表达数据（元素分类），界面本体保持克制的中性色；
> 数字是第一等公民，用等宽数字与清晰层级呈现，像实验报告一样可信。

### 0.2 参考对象（设计特征提炼）

| 参考对象 | 借鉴点 |
|---|---|
| Wolfram\|Alpha | 计算结果卡片化：清晰小节标题 + 标签/数值对 + 大量留白 |
| RSC 交互式周期表 | 分类色即数据：色相环全覆盖、明度统一、可辨识优先 |
| NIST WebBook 等数据工具 | 表格排印：等宽数字、单位弱化、缺失值统一 `—` |
| Apple 健康/天气 | 大数字层级 + 分层卡片表面 + 顺滑克制的动效 |
| Linear / Raycast 等现代专业工具 | 中性表面 + 发丝线描边分层（而非阴影）+ tonal 选中态 |

### 0.3 现状问题清单（本次迭代要解决的）

| # | 问题 | 根因 |
|---|---|---|
| P1 | 整体像 M3 默认模板，无品牌辨识度 | 默认紫色色板 + 紫调表面 |
| P2 | 白底白卡靠阴影分层，层次弱且显廉价 | 无描边、阴影滥用 |
| P3 | 元素分类色中 卤素/稀有气体/镧系/锕系 四色挤在粉紫区，不可辨识 | 色板未经色相环设计 |
| P4 | 数据数值用默认比例数字，对不齐、显业余 | 未启用 tabular numerals |
| P5 | 化学式/方程式与正文同字体 | 未用等宽字体 |
| P6 | 首页下半屏大面积空白，视觉失衡 | 内容层级无 Hero 无节奏 |
| P7 | 卡片"顶部色条"样式陈旧且语义不明 | 装饰性用色 ≠ 数据性用色 |
| P8 | 组件样式各自为政（圆角/内边距/字阶不统一） | 缺少统一 token 约束 |

---

## 1. 设计原则（Principles）

1. **颜色即数据（Color = Data）**
   唯一大面积使用彩色的地方是元素分类语义色。界面 chrome（导航、卡片、
   分隔、按钮）一律中性色。删除一切"装饰性色条"。

2. **表面分层靠明度与发丝线，不靠阴影（Tonal + Hairline）**
   卡片 = 白面 + 1dp `outlineVariant` 发丝描边，无阴影；
   阴影只属于"漂浮层"（对话框、BottomSheet、FAB）。

3. **数字是第一等公民（Numbers First）**
   一切测量值/属性值/统计值使用 **等宽数字（tabular numerals）**；
   化学方程式、电子排布使用 **等宽字体（Monospace）**。

4. **克制的层级（Restrained Hierarchy）**
   每屏一个视觉锚点（Hero/大数字），其余用 3 级文字层级：
   标题（SemiBold）→ 标签（Medium 小字）→ 数值/正文（Regular）。

5. **状态必须可见（Visible States）**
   选中 = 分类色环 + tonal 填充；筛选中 = chip 实心；多选 = 顶栏切换为
   上下文模式。任何状态变化都有即时视觉反馈（涟漪/缩放/弹簧）。

6. **8pt 网格（8pt Grid）**
   所有间距为 4 的整数倍、以 8 为主；4dp 仅用于发丝线与微调。

---

## 2. 色彩系统（Color）

### 2.1 中性表面「石墨」（Graphite Surfaces）

界面本体色。冷调中性（不再带紫调），深色模式为真正的深色。

| Token | 浅色 | 深色 | 用途 |
|---|---|---|---|
| `background` | `#F6F7F9` | `#0E0F13` | 页面最底层 |
| `surface` | `#FFFFFF` | `#15161B` | 卡片、顶栏、底栏 |
| `surfaceVariant` | `#EDEEF2` | `#232430` | 沉降面（代码块、输入框底） |
| `surfaceDim` | `#E9EAEF` | `#1A1B21` | 次级卡片、hover 面 |
| `onSurface` | `#1A1C20` | `#E6E7EC` | 主文字 |
| `onSurfaceVariant` | `#5A5D68` | `#9DA0AC` | 次文字、标签 |
| `outline` | `#8A8D98` | `#4B4E5A` | 输入框描边、强调分隔 |
| `outlineVariant` | `#E3E4EA` | `#2A2C36` | **发丝线（卡片描边、分隔线）** |

> 关键变化：`outlineVariant` 成为全应用最高频使用的 token —— 所有卡片描边、
> 列表分隔线统一走它，形成"仪器面板"的精密质感。

### 2.2 品牌主色「靛蓝」（Brand Indigo）

延续既有靛紫品牌基因，但提纯为更冷静、对比更强的靛蓝。

| Token | 浅色 | 深色 | 用途 |
|---|---|---|---|
| `primary` | `#4E56CA` | `#BCC2FF` | 主按钮、选中态、链接 |
| `onPrimary` | `#FFFFFF` | `#1A1D66` | 主按钮文字 |
| `primaryContainer` | `#E4E5FB` | `#3B41A0` | 选中背景、tonal chip |
| `onPrimaryContainer` | `#171A4D` | `#DFE1FF` | tonal chip 文字 |
| `secondary` | `#5B616E` | `#C4C7D3` | 次级操作 |
| `secondaryContainer` | `#E6E8EE` | `#3A3E4B` | 次级选中背景 |
| `onSecondaryContainer` | `#1F232B` | `#DDE0E9` | 次级文字 |
| `tertiary` | `#0F7B72` | `#54D8CC` | 科学数据强调（图表、化学计量） |
| `tertiaryContainer` | `#D3F5F1` | `#0C5A54` | 数据强调背景 |
| `onTertiaryContainer` | `#043B37` | `#C4F2EC` | — |
| `error` | `#BA1A1A` | `#FFB4AB` | 错误、删除 |
| `errorContainer` | `#FFDAD6` | `#93000A` | 删除滑动背景 |

### 2.3 元素分类色「光谱十色」（Spectral Ten）

设计方法：10 个分类 = 色相环 10 等分（约每 34°一档），**统一饱和度与明度**
（浅色 S≈70 L≈55；深色整体提亮降饱和），保证任意两色可辨、任意色在
深浅两模式下均可用作文字色。

| 分类 | 色相 | 浅色 | 深色 |
|---|---|---|---|
| 碱金属 Alkali | 红 355° | `#E5484D` | `#FF7A80` |
| 碱土金属 Alkaline Earth | 橙 25° | `#F5872E` | `#FFA05C` |
| 过渡金属 Transition | 琥珀 42° | `#F5A623` | `#FFC24D` |
| 后过渡金属 Post-transition | 绿 145° | `#3FA45B` | `#6ECF87` |
| 类金属 Metalloid | 青 172° | `#12A094` | `#3ECFC2` |
| 非金属 Nonmetal | 天蓝 205° | `#2D8FE0` | `#7AB5F5` |
| 稀有气体 Noble Gas | 靛 245° | `#5A6ACF` | `#97A0EE` |
| 卤素 Halogen | 紫 280° | `#8E4EC6` | `#BA8BE8` |
| 镧系 Lanthanide | 品红 318° | `#D6409F` | `#ED7CC1` |
| 锕系 Actinide | 玫红 340° | `#E93D82` | `#F9799F` |

> 变化说明：修复了旧色板中 卤素/稀有气体/镧系/锕系 四色挤在粉紫区的问题；
> 镧系与锕系保持"邻近色相的 f-block 亲缘感"，但明度/色相可区分。

### 2.4 分类色的使用规则

| 场景 | 用法 | 公式 |
|---|---|---|
| 周期表单元格底色 | 分类色 × 低透明度 | `category.copy(alpha = 0.16)` 浅色 / `0.22` 深色 |
| 单元格符号/描边 | 分类色全值 | 直接使用 |
| 元素磁贴（ElementTile） | 左侧 3dp 竖条 + 符号着色 | 竖条全值，符号全值 |
| 图标圆底（IconBadge） | 分类色 12% 圆底 + 分类色图标 | `copy(alpha = 0.12)` |
| Chip 选中态 | 分类色 14% 底 + 分类色文字与圆点 | `copy(alpha = 0.14)` |
| 详情页 Hero 渐变 | 分类色 18% → 透明 线性渐变 | 角度 160° |

**禁止**：装饰性渐变（非语义）、彩色阴影、彩色边框（除选中态）。

### 2.5 语义辅助色

| 用途 | 浅色 | 深色 |
|---|---|---|
| 正确（测验/成功） | `#2E7D43` / 底 `#D8F3DC` | `#6BCB77` / 底 `#1D4A2A` |
| 错误（测验） | `#BA1A1A` / 底 `#FFDAD6` | `#FFB4AB` / 底 `#5C1A1E` |
| 学习等级·核心 | `#C92A2A` / 底 `#FFE3E3` | `#FF8787` / 底 `#4A2020` |
| 学习等级·重点 | `#D9480F` / 底 `#FFE8CC` | `#FFA94D` / 底 `#4A2E14` |
| 学习等级·了解 | `#2B8A3E` / 底 `#D3F9D8` | `#69DB7C` / 底 `#1E4028` |
| 初中 badge | `#1C5FA8` / 底 `#DCEBFB` | `#74C0FC` / 底 `#173A5E` |
| 高中 badge | `#C2410C` / 底 `#FFE8CC` | `#FFA94D` / 底 `#4A2E14` |

---

## 3. 排印系统（Typography）

### 3.1 三条铁律

1. **数值一律等宽**：`fontFeatureSettings = "tnum"`（属性值、统计、序号、对比表）。
2. **公式一律等宽**：化学方程式、电子排布 → `FontFamily.Monospace`。
3. **中文标题不加粗到 800+**：最重 `SemiBold(600)`，靠字号与颜色分层。

### 3.2 字阶（覆盖 M3 十五槽位）

| Token | 字号/行高 | 字重 | 关键特征 | 用途 |
|---|---|---|---|---|
| `displayLarge` | 64/72 | Light | tnum, LS −1.0 | 元素详情原子序号 Hero |
| `displayMedium` | 45/52 | Light | tnum | 测验得分大数字 |
| `displaySmall` | 36/44 | Light | tnum | 大统计数字 |
| `headlineLarge` | 32/40 | SemiBold | — | 页面大标题（少用） |
| `headlineMedium` | 28/36 | SemiBold | — | 二级页标题 |
| `headlineSmall` | 24/32 | SemiBold | LS −0.2 | 各屏 TopBar 标题 |
| `titleLarge` | 22/28 | Bold | 元素符号 | 元素符号大字 |
| `titleMedium` | 16/24 | SemiBold | LS +0.1 | Section 标题 |
| `titleSmall` | 14/20 | SemiBold | — | 卡片标题 |
| `bodyLarge` | 16/24 | Regular | LS +0.3 | 正文主文字 |
| `bodyMedium` | 14/20 | Regular | LS +0.2 | 列表项、描述 |
| `bodySmall` | 12/16 | Regular | LS +0.3 | 辅助说明 |
| `labelLarge` | 14/20 | SemiBold | LS +0.1 | 按钮文字 |
| `labelMedium` | 12/16 | Medium | LS +0.4 | Chip、badge、标签 |
| `labelSmall` | 11/14 | Medium | tnum | 角标、原子序号小字 |

数值等宽通过统一扩展实现（`Modifier`/`TextStyle` + `fontFeatureSettings`），
避免每处手写。

### 3.3 专用文本样式（代码内提供 `PeriodicTextStyles`）

| 名称 | 定义 | 用途 |
|---|---|---|
| `numericValue` | bodyLarge + tnum + onSurface | 属性值 |
| `numericUnit` | bodySmall + onSurfaceVariant | 单位（紧跟数值，弱化） |
| `equation` | bodyMedium + Monospace + onSurface | 化学方程式 |
| `electronConfig` | bodySmall + Monospace | 电子排布 |

---

## 4. 形状（Shape）

| Token | 圆角 | 用途 |
|---|---|---|
| `extraSmall` | 6dp | 周期表单元格、小 badge |
| `small` | 8dp | Chip、输入框、列表子项 |
| `medium` | 12dp | 列表卡片、属性卡 |
| `large` | 16dp | 标准卡片、对话框 |
| `extraLarge` | 20dp | Hero 卡、BottomSheet |
| `full` | 50% | 圆形（IconBadge、圆点） |

**一致性规则**：同一层级的组件圆角一致；嵌套时内层比外层小 4dp；
全屏页面容器不圆角。

---

## 5. 间距与网格（Spacing）

沿用 8pt 网格，语义化命名：

| Token | 值 | 用途 |
|---|---|---|
| `Dp2` | 2dp | 发丝偏移 |
| `Dp4` | 4dp | 微间距、分隔线 |
| `Dp8` | 8dp | 紧凑内边距 |
| `Dp12` | 12dp | 卡片内边距（紧凑）、栅格间隙 |
| `Dp16` | 16dp | **标准页边距 / 标准卡片内边距** |
| `Dp20` | 20dp | 卡片内边距（宽松） |
| `Dp24` | 24dp | Section 间距 |
| `Dp32` | 32dp | 大分节 |
| `Dp48+` | 48dp+ | 特殊留白 |

列表：`Arrangement.spacedBy(12dp)`；Section 间 `24dp`；页底留白 `24dp`。

---

## 6. 高度与描边（Elevation & Border）

### 6.1 分层策略（核心变化）

| 层级 | 表现 | 阴影 |
|---|---|---|
| L0 页面背景 | `background` | 无 |
| L1 卡片 | `surface` + 1dp `outlineVariant` 描边 | **无** |
| L2 沉降面 | `surfaceVariant`（代码块、输入底） | 无 |
| L3 顶栏/底栏 | `surface` + 底部发丝线 | 无（滚动时可 + Shadow1） |
| L4 漂浮层 | 对话框 / BottomSheet | `Modal` 8dp 阴影 |

`Elevation` token：`Shadow1=1dp`（顶栏滚动态）、`Shadow2=3dp`（FAB）、
`Floating=6dp`、`Modal=8dp`。

### 6.2 按压反馈

所有可点击卡片/磁贴：`Modifier.pressScale()`（按下 0.97 弹簧缩放）+
涟漪。周期表单元格只保留涟漪（密度高，缩放会显乱）。

---

## 7. 动效（Motion）

| 场景 | 规格 |
|---|---|
| 页面转场 | 沿用水平滑动（已实现） |
| 按压 | `spring(dampingRatio=0.7, stiffness=600)` scale 0.97 |
| 选中态变化 | `animateColorAsState` 150ms |
| 数字出现 | 无（保持可靠感，不做数字滚动） |
| 收藏心形 | scale 弹跳 1→1.2→1 |
| 局部展开/收起 | `animateContentSize` 200ms |

原则：动效服务反馈，不做纯装饰动画；原子模型动画保留（品牌资产）。

---

## 8. 组件规范（Components）

### 8.1 周期表单元格 PeriodicTableCell

- 圆角 6dp；间距 2dp；底色 = 分类色 × alpha(0.16/0.22)
- 内容：序号（labelSmall tnum，onSurfaceVariant）/ 符号（labelMedium Bold，
  onSurface）/ 中文名（9sp，onSurfaceVariant）
- 状态：
  - 筛选淘汰 → 整体 `alpha 0.12`，饱和度去色
  - 搜索命中 → 正常 + 其余 0.12
  - 选中（多选）→ 2dp 分类色描边 + 底色 alpha 提到 0.35
- f-block 占位格：`surfaceVariant` 底 + `57–71` 数字号

### 8.2 元素磁贴 ElementTile（原 ElementCard 重设计）

列表/收藏/首页推荐使用。替代"顶部色条"：

```
┌────────────────────┐

│ 08        ●        │   ← 序号(tnum) / 收藏点(可选)
│      O             │   ← 符号 titleLarge Bold，分类色
│      氧  Oxygen    │   ← 中文 + 英文
└────────────────────┘
```

- 白面 + 发丝描边 + 12dp 圆角 + 左侧 3dp 分类色竖条（inset）
- 按压缩放反馈；宽度自适应（LazyRow 定宽 96dp，列表通栏）

### 8.3 搜索栏 PeriodicSearchBar

- 填充式：`surfaceVariant` 底、8dp 圆角、高 52dp
- 前置 Search 图标；有内容时尾部 Clear 按钮
- 聚焦：描边 2dp `primary` + 底色转 `surface`

### 8.4 筛选 Chip CategoryFilterChip

- 未选：`surface` 底 + 发丝描边 + 分类色圆点 + onSurface 文字
- 选中：分类色 × 0.14 底 + 分类色文字 + 圆点放大
- 高 36dp、圆角 8dp（full 圆角视觉过软，不用）

### 8.5 SectionHeader

`titleMedium SemiBold` + 可选 `trailingAction`（文字按钮）+
底部 24dp 后接内容。全应用统一的小节标题。

### 8.6 属性卡 PropertyCard（详情页）

- 卡片内两列数据行；行间发丝分隔线
- 行结构：`labelMedium 标签`（onSurfaceVariant）→ `numericValue 数值`
  + `numericUnit 单位`（右对齐）
- 温度单位切换：行内小 SegmentedToggle（°C | K），不做整行点击
- 缺失值统一 `—`（onSurfaceVariant × 0.6）

### 8.7 IconBadge

- 分类色/主色 12% 圆底（40dp）+ 20dp 图标（全值色）
- 用于 Dashboard 快捷入口、发现页 Feed、设置组图标

### 8.8 按钮

| 类型 | 样式 | 高度 |
|---|---|---|
| Primary | `primary` 底 / `onPrimary` 字 | 48dp，圆角 12 |
| Tonal | `primaryContainer` / `onPrimaryContainer` | 48dp |
| Outlined | 白底 + 1.5dp `outline` | 48dp |
| Text | `primary` 文字按钮 | 40dp |

### 8.9 空状态 EmptyState

- 图标置于 56dp IconBadge（primaryContainer 底）
- 标题 `titleMedium` + 副文 `bodyMedium` + Primary 按钮
- 整体居中，上下留白 ≥ 64dp

### 8.10 顶栏 PeriodicTopBar

- 标题统一 `headlineSmall SemiBold`；容器 `surface` + 底部发丝线
- 多选上下文模式：容器 `primaryContainer`，标题"已选 N 项"（tnum），
  关闭图标 + 确认按钮（Primary）
- 全屏二级页：返回箭头 + 标题 + 右侧动作（收藏等）

### 8.11 化学反应卡 ReactionCard

- 等宽字体方程式（`equation` 样式，可横向滚动）
- 类型 badge（语义色 12% 底）+ 难度 badge
- 涉及元素以小号 ElementTile 链呈现

---

## 9. 逐屏规范（Screens）

### 9.1 首页 Home ——「仪表盘」

```
[周期表 Pro]  标题行（headlineSmall SemiBold + 副标题）
[搜索框........................]  52dp 填充式
[今日聚焦 Hero]  分类色渐变 18%→透明 + 大符号 + 描述 + 进入箭头
[精选元素 ———  横滑 ElementTile × N，尾部渐隐提示]
[快捷入口]  2×2 IconBadge 卡（学习/测验/实验室/对比）
[元素分类]  分类色圆点 + 名称 的双列图例行 → 跳周期表
```

- Hero 取 featured 首个元素（纯展示复用现有数据）
- 修复下半屏空白：图例节 + 更充裕的 Section 间距

### 9.2 周期表 Table ——「仪器主屏」

- TopBar：标题；多选时切上下文栏
- 搜索 + 分类 Chip 行（LazyRow）
- 网格：单元格 6dp 圆角 + 分类色 tint 底；f-block 双行间距 8dp
- 图例：Chip 行本身即图例（圆点=分类色），不再单独图例区

### 9.3 元素详情 Detail ——「实验报告」

```
TopBar（返回 / 符号 / 收藏）
[Hero 区]  分类色 18%→透明 渐变底
   左：AtomCanvas（缩小至 140dp）
   右：序号 displayLarge(tnum) + 符号 titleLarge + 中英文名
[电子壳层]  小 chip 行 K2 L8 …（tnum，等宽）
[属性 PropertyCard]  8 项数据 + °C/K 切换
[档案卡]  发现者/年份/族/周期 行式 + 发丝线
[相关反应 ReactionCard × N]  可展开
```

- 原子模型区从 1/3 屏压缩为紧凑 Hero，属性前置（信息效率）

### 9.4 发现 Discover

- 每日推荐 Hero：分类色渐变底 + 大符号 + 标题 + 摘要 + "查看"
- Feed 卡：IconBadge（元素序号）+ 标题 + 2 行摘要 + 分类色小字

### 9.5 收藏 Favorites

- TopBar 副标题：N 个元素（tnum）
- 列表用通栏 ElementTile；左滑删除底改 `errorContainer` + 图标 `onErrorContainer`

### 9.6 设置 Profile

- 分组卡：`profile_theme` / `profile_about` / `profile_check_update`
- 组内行：IconBadge(32dp) + 标题 + 说明 + 右侧控件（主题用三段 Segmented）
- 主题选择：跟随系统 / 浅色 / 深色 分段控件，选中 `primaryContainer`

### 9.7 学习资料 Learn

- 搜索 + 等级 Chip（核心/重点/了解 语义色）
- 列表卡：ElementTile 式左块 + 知识点标题 + 等级 badge

### 9.8 实验室 Lab

- 类型 Chip 行（反应类型语义色圆点）
- ReactionCard 列表；详情页方程式大号居中 + 条件/现象分组卡

### 9.9 测验 Quiz

- 题目卡：题号 tnum + 进度条（primary）
- 选项：通栏可选行，选中 `primaryContainer`；答对/答错 用语义色即时反馈
- 结算：displayMedium 大分数 + 正确率 + 重开按钮

### 9.10 对比 Compare

- 首行元素符号列（分类色），冻结列
- 属性行：标签列 + N 值列，全部 tnum；斑马纹 `surface`/`surfaceDim`
- 横向可滚动 + 顶栏说明已选 N 个

### 9.11 更新对话框 UpdateDialog

- `large` 卡 + 版本号 tnum + Release Notes 等宽小字区
- 主按钮"立即更新" + 文字按钮"稍后/跳过"；下载中 LinearProgress

---

## 10. 深色模式策略

- 表面：真·深色（`#0E0F13`），不做纯黑（OLED 纯黑 + 彩色 tint 会显脏）
- 分类色：整体提亮（L+15）降饱和（S−15），保证深底可读
- 发丝线：`#2A2C36`，比深色表面亮一档即可，不要更亮
- 渐变 Hero：alpha 从 0.18（浅）提到 0.24（深）
- 禁止：深色模式使用浅色模式的 tint 直接叠加（对比度不足）

## 11. 无障碍（Accessibility）

- 正文/标签对比度 ≥ 4.5:1；大号文字 ≥ 3:1
- 分类色 **不单独**承载信息：Chip 有文字、单元格有序号+符号
- 所有 IconButton 有 `contentDescription`（沿用现有 string 资源）
- 按压目标 ≥ 48dp（周期表单元格最小 48dp 已保证）

## 12. 落地清单

| 阶段 | 内容 |
|---|---|
| T1 主题 | `theme/` 六文件重构 + `Motion`/`Pressable` 扩展 |
| T2 组件 | components + pattern 全部对齐新 token |
| T3 屏幕 | 11 屏 + UpdateDialog 逐屏改造 |
| T4 验证 | 构建 / 单测 / 模拟器浅深双主题截图验收 |

> 本文档是唯一设计事实源（Single Source of Truth）。
> 实现与本文冲突时，以本文为准；本文未尽事宜遵循 Material 3 规范。
