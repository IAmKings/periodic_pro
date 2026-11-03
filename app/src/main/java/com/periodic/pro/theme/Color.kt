package com.periodic.pro.theme

import androidx.compose.ui.graphics.Color
import com.periodic.pro.data.element.model.Category

// =====================================================================
// Spectra Design Language v1.0 — Color Tokens
// 事实源：/DESIGN.md §2
// 结构：
//   1) 石墨中性表面（Graphite）   — 界面本体，冷调中性，深浅两套
//   2) 品牌靛蓝（Brand Indigo）   — 主操作色
//   3) 光谱十色（Spectral Ten）   — 元素分类语义色，色相环均分
//   4) 语义辅助色（Semantic）     — 正确/错误/学习等级/难度
// =====================================================================

// ===== 1) 石墨中性表面 — 浅色 =====
val LightBackground = Color(0xFFF6F7F9)
val LightOnBackground = Color(0xFF1A1C20)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A1C20)
val LightSurfaceVariant = Color(0xFFEDEEF2)
val LightOnSurfaceVariant = Color(0xFF5A5D68)
val LightSurfaceDim = Color(0xFFE9EAEF)
val LightOutline = Color(0xFF8A8D98)
val LightOutlineVariant = Color(0xFFE3E4EA)

// ===== 1) 石墨中性表面 — 深色 =====
val DarkBackground = Color(0xFF0E0F13)
val DarkOnBackground = Color(0xFFE6E7EC)
val DarkSurface = Color(0xFF15161B)
val DarkOnSurface = Color(0xFFE6E7EC)
val DarkSurfaceVariant = Color(0xFF232430)
val DarkOnSurfaceVariant = Color(0xFF9DA0AC)
val DarkSurfaceDim = Color(0xFF1A1B21)
val DarkOutline = Color(0xFF4B4E5A)
val DarkOutlineVariant = Color(0xFF2A2C36)

// ===== 2) 品牌靛蓝 — 浅色 =====
val LightPrimary = Color(0xFF4E56CA)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE4E5FB)
val LightOnPrimaryContainer = Color(0xFF171A4D)
val LightSecondary = Color(0xFF5B616E)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE6E8EE)
val LightOnSecondaryContainer = Color(0xFF1F232B)
val LightTertiary = Color(0xFF0F7B72)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFD3F5F1)
val LightOnTertiaryContainer = Color(0xFF043B37)
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// ===== 2) 品牌靛蓝 — 深色 =====
val DarkPrimary = Color(0xFFBCC2FF)
val DarkOnPrimary = Color(0xFF1A1D66)
val DarkPrimaryContainer = Color(0xFF3B41A0)
val DarkOnPrimaryContainer = Color(0xFFDFE1FF)
val DarkSecondary = Color(0xFFC4C7D3)
val DarkOnSecondary = Color(0xFF1D2026)
val DarkSecondaryContainer = Color(0xFF3A3E4B)
val DarkOnSecondaryContainer = Color(0xFFDDE0E9)
val DarkTertiary = Color(0xFF54D8CC)
val DarkOnTertiary = Color(0xFF003733)
val DarkTertiaryContainer = Color(0xFF0C5A54)
val DarkOnTertiaryContainer = Color(0xFFC4F2EC)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// ===== 3) 光谱十色 — 元素分类色板 =====
// 独立于 M3 ColorScheme，通过 staticCompositionLocalOf 注入（见 PeriodicProTheme.kt）。
// 设计：色相环 10 等分（红/橙/琥珀/绿/青/天蓝/靛/紫/品红/玫红），
//       浅色统一明度饱和度、深色提亮降饱和，保证任意两色可辨。

/** 分类色透明度 Token（DESIGN.md §2.4） */
object CategoryAlpha {
    /** 周期表单元格底色 — 浅色模式 */
    const val CELL = 0.16f
    /** 周期表单元格底色 — 深色模式 */
    const val CELL_DARK = 0.22f
    /** 单元格选中态底色加深 */
    const val CELL_SELECTED = 0.35f
    /** 图标圆底 / badge 底 */
    const val BADGE = 0.12f
    /** Chip 选中态底 */
    const val CHIP = 0.14f
    /** Hero 渐变起始 — 浅色 */
    const val HERO = 0.18f
    /** Hero 渐变起始 — 深色 */
    const val HERO_DARK = 0.24f
}

data class CategoryColors(
    val alkali: Color = Color(0xFFE5484D),        // 碱金属 — 红 355°
    val alkalineEarth: Color = Color(0xFFF5872E), // 碱土金属 — 橙 25°
    val transition: Color = Color(0xFFF5A623),    // 过渡金属 — 琥珀 42°
    val postTransition: Color = Color(0xFF3FA45B),// 后过渡金属 — 绿 145°
    val metalloid: Color = Color(0xFF12A094),     // 类金属 — 青 172°
    val nonmetal: Color = Color(0xFF2D8FE0),      // 非金属 — 天蓝 205°
    val nobleGas: Color = Color(0xFF5A6ACF),      // 稀有气体 — 靛 245°
    val halogen: Color = Color(0xFF8E4EC6),       // 卤素 — 紫 280°
    val lanthanide: Color = Color(0xFFD6409F),    // 镧系 — 品红 318°
    val actinide: Color = Color(0xFFE93D82),      // 锕系 — 玫红 340°
)

/** 深色模式：整体提亮（L+15）降饱和（S−15） */
val DarkCategoryColors = CategoryColors(
    alkali = Color(0xFFFF7A80),
    alkalineEarth = Color(0xFFFFA05C),
    transition = Color(0xFFFFC24D),
    postTransition = Color(0xFF6ECF87),
    metalloid = Color(0xFF3ECFC2),
    nonmetal = Color(0xFF7AB5F5),
    nobleGas = Color(0xFF97A0EE),
    halogen = Color(0xFFBA8BE8),
    lanthanide = Color(0xFFED7CC1),
    actinide = Color(0xFFF9799F),
)

fun CategoryColors.forCategory(category: Category): Color = when (category) {
    Category.ALKALI_METAL -> alkali
    Category.ALKALINE_EARTH -> alkalineEarth
    Category.TRANSITION_METAL -> transition
    Category.POST_TRANSITION -> postTransition
    Category.METALLOID -> metalloid
    Category.NONMETAL -> nonmetal
    Category.HALOGEN -> halogen
    Category.NOBLE_GAS -> nobleGas
    Category.LANTHANIDE -> lanthanide
    Category.ACTINIDE -> actinide
}

// ===== 4) 语义辅助色（DESIGN.md §2.5）=====
// 语义色以「前景/容器」成对定义，深浅模式各自成套，经 CompositionLocal 注入。

data class SemanticColors(
    val correct: Color = Color(0xFF2E7D43),
    val correctContainer: Color = Color(0xFFD8F3DC),
    val wrong: Color = Color(0xFFBA1A1A),
    val wrongContainer: Color = Color(0xFFFFDAD6),
    val levelCore: Color = Color(0xFFC92A2A),
    val levelCoreContainer: Color = Color(0xFFFFE3E3),
    val levelImportant: Color = Color(0xFFD9480F),
    val levelImportantContainer: Color = Color(0xFFFFE8CC),
    val levelNormal: Color = Color(0xFF2B8A3E),
    val levelNormalContainer: Color = Color(0xFFD3F9D8),
    val junior: Color = Color(0xFF1C5FA8),
    val juniorContainer: Color = Color(0xFFDCEBFB),
    val senior: Color = Color(0xFFC2410C),
    val seniorContainer: Color = Color(0xFFFFE8CC),
    // 化学反应类型（DESIGN.md §8.11：badge = 类型色 12% 底 + 全值文字）
    val rxCombination: Color = Color(0xFF2E7D43),
    val rxDecomposition: Color = Color(0xFFBA1A1A),
    val rxDisplacement: Color = Color(0xFF1C5FA8),
    val rxDoubleDisplacement: Color = Color(0xFF8E4EC6),
    val rxRedox: Color = Color(0xFFD9480F),
    val rxOther: Color = Color(0xFF5A5D68),
)

val DarkSemanticColors = SemanticColors(
    correct = Color(0xFF6BCB77),
    correctContainer = Color(0xFF1D4A2A),
    wrong = Color(0xFFFFB4AB),
    wrongContainer = Color(0xFF5C1A1E),
    levelCore = Color(0xFFFF8787),
    levelCoreContainer = Color(0xFF4A2020),
    levelImportant = Color(0xFFFFA94D),
    levelImportantContainer = Color(0xFF4A2E14),
    levelNormal = Color(0xFF69DB7C),
    levelNormalContainer = Color(0xFF1E4028),
    junior = Color(0xFF74C0FC),
    juniorContainer = Color(0xFF173A5E),
    senior = Color(0xFFFFA94D),
    seniorContainer = Color(0xFF4A2E14),
    rxCombination = Color(0xFF6BCB77),
    rxDecomposition = Color(0xFFFFB4AB),
    rxDisplacement = Color(0xFF74C0FC),
    rxDoubleDisplacement = Color(0xFFBA8BE8),
    rxRedox = Color(0xFFFFA94D),
    rxOther = Color(0xFF9DA0AC),
)

// ===== AtomCanvas — 原子模型电子颜色 =====
val AtomElectronColor: Color = Color(0xFF4FC3F7)
