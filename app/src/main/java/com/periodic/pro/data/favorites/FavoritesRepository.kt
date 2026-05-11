package com.periodic.pro.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favoritesDataStore by preferencesDataStore(name = "favorites")

class FavoritesRepository(private val context: Context) {

    companion object {
        private val FAVORITE_IDS_KEY = stringSetPreferencesKey("favorite_ids")
    }

    /**
     * 收藏的元素原子序号集合，持续可观察。
     */
    val favoritesFlow: Flow<Set<Int>> = context.favoritesDataStore.data
        .map { prefs ->
            prefs[FAVORITE_IDS_KEY]
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet() ?: emptySet()
        }

    /**
     * 检查指定元素是否被收藏。
     */
    fun isFavorite(atomicNumber: Int): Flow<Boolean> = favoritesFlow.map { it.contains(atomicNumber) }

    /**
     * 获取所有收藏元素 ID 的 Flow。
     */
    fun getAll(): Flow<Set<Int>> = favoritesFlow

    /**
     * 切换收藏状态：如果已收藏则移除，否则添加。
     */
    suspend fun toggle(atomicNumber: Int) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[FAVORITE_IDS_KEY] ?: emptySet()
            val updated = if (current.contains(atomicNumber.toString())) {
                current - atomicNumber.toString()
            } else {
                current + atomicNumber.toString()
            }
            prefs[FAVORITE_IDS_KEY] = updated
        }
    }
}
