package com.periodic.pro.data.lab

import android.content.Context
import com.periodic.pro.data.lab.model.ChemicalReaction
import com.periodic.pro.data.lab.model.ReactionLevel
import com.periodic.pro.data.lab.model.ReactionType
import kotlinx.serialization.json.Json

/**
 * 化学实验室数据仓库。
 *
 * 从 [assets/reactions.json] 加载化学反应数据。
 */
class LabRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedReactions: List<ChemicalReaction>? = null

    /**
     * 加载所有化学反应数据。
     * 内部缓存，多次调用只加载一次 JSON。
     */
    suspend fun loadAll(): List<ChemicalReaction> {
        return cachedReactions ?: run {
            val jsonText = context.assets.open("reactions.json")
                .bufferedReader()
                .use { it.readText() }
            val reactions = json.decodeFromString<List<ChemicalReaction>>(jsonText)
            cachedReactions = reactions
            reactions
        }
    }

    /**
     * 根据 ID 获取单个反应。
     */
    suspend fun getById(id: String): ChemicalReaction? {
        return loadAll().find { it.id == id }
    }

    /**
     * 根据反应类型获取反应列表。
     */
    suspend fun getByType(type: ReactionType): List<ChemicalReaction> {
        return loadAll().filter { it.type == type }
    }

    /**
     * 根据年级等级获取反应列表。
     */
    suspend fun getByLevel(level: ReactionLevel): List<ChemicalReaction> {
        return loadAll().filter { it.level == level }
    }
}
