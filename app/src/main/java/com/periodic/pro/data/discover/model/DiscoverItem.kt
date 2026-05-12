package com.periodic.pro.data.discover.model

import kotlinx.serialization.Serializable

/**
 * 发现页知识点数据模型。
 *
 * @param atomicNumber 关联元素的原子序号
 * @param title 知识点标题（如"最轻的金属"）
 * @param description 知识点详细描述
 * @param funFact 额外的趣味小贴士（可选）
 */
@Serializable
data class DiscoverItem(
    val atomicNumber: Int,
    val title: String,
    val description: String,
    val funFact: String? = null,
)
