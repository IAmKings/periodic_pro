package com.periodic.pro.ui.navigation

import android.net.Uri

/**
 * 路由路径常量（Navigation Compose 2.7.7 字符串路由）。
 *
 * 计划：升级到 Navigation 2.8+ 后迁移到 @Serializable type-safe 路由
 *       (navigation-compose 2.8.x 需要 Compose BOM 2024.12+ 或更高)。
 */
object Routes {
    const val HOME = "home"
    const val TABLE = "table"
    const val DETAIL = "detail/{atomicNumber}"
    const val COMPARE = "compare?ids={ids}"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val DISCOVER = "discover"
    const val CATEGORY = "category"
    const val CATEGORY_DETAIL = "category/{categoryId}"

    fun detail(atomicNumber: Int) = "detail/$atomicNumber"
    fun compare(ids: List<Int>) = "compare?ids=${ids.joinToString(",")}"
    const val LEARN = "learn"
    const val LEARN_DETAIL = "learn/{atomicNumber}"

    const val LAB = "lab"
    const val LAB_DETAIL = "lab/{reactionId}"

    fun categoryDetail(categoryId: String) = "category/$categoryId"
    fun learnDetail(atomicNumber: Int) = "learn/$atomicNumber"
    fun labDetail(reactionId: String) = "lab/$reactionId"

    /**
     * 构建带搜索 query 的 table 路由。
     * query 为空时使用基本路由以支持底部导航匹配。
     */
    fun table(query: String = ""): String {
        if (query.isBlank()) return TABLE
        return "table?query=${Uri.encode(query)}"
    }
}
