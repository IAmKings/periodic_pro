package com.periodic.pro.data.element.model

import kotlinx.serialization.Serializable

@Serializable
data class ElementZh(
    val atomicNumber: Int,
    val nameZh: String,
    val pinyin: String,
)
