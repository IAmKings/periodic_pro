package com.periodic.pro.data.discover

import android.content.Context
import com.periodic.pro.data.discover.model.DiscoverItem
import kotlinx.serialization.json.Json

/**
 * 发现页数据仓库。
 *
 * 从 [assets/discover.json] 加载知识点列表。
 * 提供基于当天日期的每日推荐。
 */
class DiscoverRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedItems: List<DiscoverItem>? = null

    /**
     * 加载所有知识点。
     * 内部缓存，多次调用只加载一次 JSON。
     */
    suspend fun loadAll(): List<DiscoverItem> {
        return cachedItems ?: run {
            val jsonText = context.assets.open("discover.json")
                .bufferedReader()
                .use { it.readText() }
            val items = json.decodeFromString<List<DiscoverItem>>(jsonText)
            cachedItems = items
            items
        }
    }

    /**
     * 基于当天日期获取每日推荐知识点。
     *
     * 算法：使用当天日期（年+月+日）的哈希值在列表中取模索引。
     * 这样每天推荐不同的知识点，且对所有人一致。
     */
    suspend fun getDailyRecommend(): DiscoverItem {
        val items = loadAll()
        if (items.isEmpty()) error("Discover items list is empty")

        val todaySeed = java.util.Calendar.getInstance().let { cal ->
            cal.get(java.util.Calendar.YEAR) * 10000 +
                (cal.get(java.util.Calendar.MONTH) + 1) * 100 +
                cal.get(java.util.Calendar.DAY_OF_MONTH)
        }
        val index = (todaySeed % items.size).toInt()
        return items[index]
    }
}
