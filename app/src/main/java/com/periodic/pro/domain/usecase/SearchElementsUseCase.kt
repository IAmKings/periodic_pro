package com.periodic.pro.domain.usecase

import com.periodic.pro.data.element.model.Element
import com.periodic.pro.data.element.model.ElementZh

/**
 * 全局搜索 UseCase。
 *
 * 根据 query 匹配元素名称（中英）、符号、原子序号、拼音。
 * 大小写不敏感。
 */
class SearchElementsUseCase {

    /**
     * 搜索元素。
     *
     * @param query 搜索关键词
     * @param elements 全部元素列表
     * @param zhMap 中文名映射 { atomicNumber -> ElementZh }
     * @return 匹配的元素列表；query 为空时返回全部
     */
    operator fun invoke(
        query: String,
        elements: List<Element>,
        zhMap: Map<Int, ElementZh>,
    ): List<Element> {
        if (query.isBlank()) return elements

        val q = query.trim().lowercase()

        return elements.filter { e ->
            // 按英文名匹配
            if (e.name.lowercase().contains(q)) return@filter true
            // 按符号匹配
            if (e.symbol.lowercase().contains(q)) return@filter true
            // 按原子序号精确匹配
            if (e.atomicNumber.toString() == q) return@filter true
            // 按中文名匹配
            val zh = zhMap[e.atomicNumber]
            if (zh != null) {
                if (zh.nameZh.contains(query)) return@filter true
                if (zh.pinyin.lowercase().contains(q)) return@filter true
            }
            false
        }
    }
}
