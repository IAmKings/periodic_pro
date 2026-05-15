package com.periodic.pro.data.update

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.updatePreferencesStore by preferencesDataStore(name = "update_preferences")

private val KEY_SKIPPED_VERSION = stringPreferencesKey("skipped_version")
private val KEY_LAST_PROMPT_DATE = stringPreferencesKey("last_prompt_date")

/**
 * 更新偏好持久化仓库。
 *
 * 使用 DataStore 存储跳过版本号和上次弹窗日期，
 * 供 UpdateService 查询，实现"跳过此版本"和"稍后提示"逻辑。
 */
class UpdatePreferences(
    private val context: Context,
) {

    /** 已永久跳过的版本号，为空表示未跳过 */
    val skippedVersion: Flow<String?> = context.updatePreferencesStore.data.map { prefs ->
        prefs[KEY_SKIPPED_VERSION]
    }

    /** 上次弹窗日期（yyyy-MM-dd），为空表示从未弹窗 */
    val lastPromptDate: Flow<String?> = context.updatePreferencesStore.data.map { prefs ->
        prefs[KEY_LAST_PROMPT_DATE]
    }

    /** 记录跳过版本：该版本号永不弹窗 */
    suspend fun setSkippedVersion(version: String) {
        context.updatePreferencesStore.edit { prefs ->
            prefs[KEY_SKIPPED_VERSION] = version
        }
    }

    /** 记录今日已弹窗：同一天不再弹 */
    suspend fun setLastPromptDate(date: String) {
        context.updatePreferencesStore.edit { prefs ->
            prefs[KEY_LAST_PROMPT_DATE] = date
        }
    }
}
