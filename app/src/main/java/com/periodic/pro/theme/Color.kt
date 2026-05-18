package com.periodic.pro.theme

import androidx.compose.ui.graphics.Color
import com.periodic.pro.data.element.model.Category

// ===== Material 3 主色系 =====
// 浅色模式
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)
val LightSurface = Color(0xFFFFFBFE)
val LightOnSurface = Color(0xFF1C1B1F)
val LightSurfaceVariant = Color(0xFFE7E0EC)
val LightOnSurfaceVariant = Color(0xFF49454F)
val LightOutline = Color(0xFF79747E)
val LightPrimaryContainer = Color(0xFFEADDFF)
val LightSecondaryContainer = Color(0xFFE8DEF8)
val LightBackground = Color(0xFFFFFBFE)
val LightOnBackground = Color(0xFF1C1B1F)
val LightError = Color(0xFFB3261E)
val LightOnError = Color(0xFFFFFFFF)

// 深色模式
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val DarkSurface = Color(0xFF1C1B1F)
val DarkOnSurface = Color(0xFFE6E1E5)
val DarkSurfaceVariant = Color(0xFF49454F)
val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
val DarkOutline = Color(0xFF938F99)
val DarkPrimaryContainer = Color(0xFF4F378B)
val DarkSecondaryContainer = Color(0xFF4A4458)
val DarkBackground = Color(0xFF1C1B1F)
val DarkOnBackground = Color(0xFFE6E1E5)
val DarkError = Color(0xFFF2B8B5)
val DarkOnError = Color(0xFF601410)

// ===== CategoryColors — 元素分类色板 =====
// 独立于 M3 ColorScheme，通过 staticCompositionLocalOf 注入
// 浅色模式：10 种元素分类色
data class CategoryColors(
    val alkali: Color = Color(0xFFFF6B6B),
    val alkalineEarth: Color = Color(0xFFFFA94D),
    val transition: Color = Color(0xFFFFD43B),
    val postTransition: Color = Color(0xFF69DB7C),
    val metalloid: Color = Color(0xFF4DABF7),
    val nonmetal: Color = Color(0xFF748FFC),
    val halogen: Color = Color(0xFFDA77F2),
    val nobleGas: Color = Color(0xFF9775FA),
    val lanthanide: Color = Color(0xFFF783AC),
    val actinide: Color = Color(0xFFE599F7),
)

// ===== 分类颜色透明度 Token =====
/** 元素卡片背景（周期表单元格） */
const val CATEGORY_CELL_ALPHA = 0.85f
/** 元素圆形标记背景（学习/发现列表） */
const val CATEGORY_CIRCLE_ALPHA = 0.15f

// 添加 forCategory 方法到 CategoryColors（扩展函数）
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

// ===== LabColors — 化学反应类型色板 =====
object LabColors {
    val Combination = Color(0xFF2E7D32)
    val Decomposition = Color(0xFFD32F2F)
    val Displacement = Color(0xFF1565C0)
    val DoubleDisplacement = Color(0xFF6A1B9A)
    val Redox = Color(0xFFE65100)
    val Other = Color(0xFF616161)
    val CombinationBg = Color(0xFFE8F5E9)
    val DecompositionBg = Color(0xFFFFEBEE)
    val DisplacementBg = Color(0xFFE3F2FD)
    val DoubleDisplacementBg = Color(0xFFF3E5F5)
    val RedoxBg = Color(0xFFFFF3E0)
    val OtherBg = Color(0xFFF5F5F5)
    val JuniorBadge = Color(0xFF1565C0)
    val SeniorBadge = Color(0xFFE65100)
}

// ===== LearnBadgeColors — 学习等级标签色板 =====
object LearnBadgeColors {
    val Core = Color(0xFFE03131)
    val Important = Color(0xFFF08C00)
    val Other = Color(0xFF2F9E44)
    val CoreBg = Color(0xFFFFE8E8)
    val ImportantBg = Color(0xFFFFF3D6)
    val OtherBg = Color(0xFFE6F9E8)
}

// ===== AtomCanvas — 原子模型电子颜色 =====
val AtomElectronColor: Color = Color(0xFF4FC3F7)

// 深色模式降饱和版（降低 20-30% 饱和度，适配暗色背景）
val DarkCategoryColors = CategoryColors(
    alkali = Color(0xFFCC5555),
    alkalineEarth = Color(0xFFCC8844),
    transition = Color(0xFFCCAA33),
    postTransition = Color(0xFF55AA66),
    metalloid = Color(0xFF4488CC),
    nonmetal = Color(0xFF5566CC),
    halogen = Color(0xFFAA55CC),
    nobleGas = Color(0xFF7755CC),
    lanthanide = Color(0xFFCC6699),
    actinide = Color(0xFFBB77CC),
)
