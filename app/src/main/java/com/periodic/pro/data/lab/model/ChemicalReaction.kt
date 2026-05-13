package com.periodic.pro.data.lab.model

import kotlinx.serialization.Serializable

/**
 * 化学反应类型枚举。
 */
@Serializable
enum class ReactionType {
    /** 化合反应 A + B → C */
    COMBINATION,

    /** 分解反应 A → B + C */
    DECOMPOSITION,

    /** 置换反应 A + BC → AC + B */
    DISPLACEMENT,

    /** 复分解反应 AB + CD → AD + CB */
    DOUBLE_DISPLACEMENT,

    /** 氧化还原反应 */
    REDOX,

    /** 其他（络合反应等） */
    OTHER,
}

/**
 * 反应年级等级。
 */
@Serializable
enum class ReactionLevel {
    /** 初中 */
    JUNIOR,

    /** 高中 */
    SENIOR,
}

/**
 * 化学反应数据模型。
 *
 * @param id 唯一标识，如 "r001"
 * @param name 反应名称，如 "氢气燃烧"
 * @param reactants 反应物列表，如 ["H₂", "O₂"]
 * @param products 产物列表，如 ["H₂O"]
 * @param equation 化学方程式，如 "2H₂ + O₂ → 2H₂O"
 * @param conditions 反应条件，如 "点燃"；无条件时为 null
 * @param type 反应类型
 * @param level 年级等级
 * @param phenomenon 实验现象描述
 * @param involvedElements 涉及元素的原子序数列表
 * @param note 补充说明（可选）
 */
@Serializable
data class ChemicalReaction(
    val id: String,
    val name: String,
    val reactants: List<String>,
    val products: List<String>,
    val equation: String,
    val conditions: String?,
    val type: ReactionType,
    val level: ReactionLevel,
    val phenomenon: String,
    val involvedElements: List<Int>,
    val note: String? = null,
)
