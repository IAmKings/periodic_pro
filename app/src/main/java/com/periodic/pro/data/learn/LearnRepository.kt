package com.periodic.pro.data.learn

import android.content.Context
import com.periodic.pro.data.learn.model.LearnItem
import kotlinx.serialization.json.Json

/**
 * 元素学习资料数据仓库。
 *
 * 从 [assets/learn.json] 加载元素学习内容。
 */
class LearnRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedItems: List<LearnItem>? = null

    /**
     * 加载所有元素学习资料。
     * 内部缓存，多次调用只加载一次 JSON。
     */
    suspend fun loadAll(): List<LearnItem> {
        return cachedItems ?: run {
            val jsonText = context.assets.open("learn.json")
                .bufferedReader()
                .use { it.readText() }
            val items = json.decodeFromString<List<LearnItem>>(jsonText)
            cachedItems = items
            items
        }
    }

    /**
     * 根据原子序号获取单个元素的学习资料。
     */
    suspend fun getByAtomicNumber(atomicNumber: Int): LearnItem? {
        return loadAll().find { it.atomicNumber == atomicNumber }
    }

    /**
     * 根据等级获取元素学习资料列表。
     */
    suspend fun getByLevel(level: String): List<LearnItem> {
        return loadAll().filter { it.level == level }
    }
}
