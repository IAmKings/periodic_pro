package com.periodic.pro.data.learn.model

import kotlinx.serialization.Serializable

/**
 * 元素学习资料数据模型。
 *
 * @param atomicNumber 原子序号
 * @param level 内容深度等级: "core" | "important" | "normal"
 * @param discoveryStory 发现历史（150-200 字）
 * @param applications 实际应用列表（2-3 条）
 * @param funFacts 趣味知识点列表（1-2 条）
 */
@Serializable
data class LearnItem(
    val atomicNumber: Int,
    val level: String,
    val discoveryStory: String,
    val applications: List<String>,
    val funFacts: List<String>,
)
