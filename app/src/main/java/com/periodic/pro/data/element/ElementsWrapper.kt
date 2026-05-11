package com.periodic.pro.data.element

import com.periodic.pro.data.element.model.Element
import kotlinx.serialization.Serializable

/**
 * elements.json 的顶层包装。
 * 文件格式: { "elements": [...], "_source": "..." }
 */
@Serializable
data class ElementsWrapper(
    val elements: List<Element>,
)
