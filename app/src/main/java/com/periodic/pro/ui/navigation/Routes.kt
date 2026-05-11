package com.periodic.pro.ui.navigation

/**
 * 路由路径常量（Navigation Compose 2.7.7 字符串路由）。
 * TODO: 升级到 Navigation 2.8+ 后迁移到 @Serializable type-safe 路由
 *       (navigation-compose 2.8.x 需要 Compose BOM 2024.12+ 或更高)。
 */
object Routes {
    const val HOME = "home"
    const val TABLE = "table"
    const val DETAIL = "detail/{atomicNumber}"
    const val COMPARE = "compare"
    const val FAVORITES = "favorites"

    fun detail(atomicNumber: Int) = "detail/$atomicNumber"
}
