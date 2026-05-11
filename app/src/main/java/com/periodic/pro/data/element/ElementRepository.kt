package com.periodic.pro.data.element

import android.content.Context
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.data.element.model.ElementZh
import kotlinx.serialization.json.Json

class ElementRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val elements: Map<Int, Element> by lazy { loadElements() }
    private val zhMap: Map<Int, ElementZh> by lazy { loadZh() }

    fun getByNumber(number: Int): Element? = elements[number]

    fun getAll(): List<Element> = elements.values.toList()

    fun getZh(number: Int): ElementZh? = zhMap[number]

    /**
     * 模糊搜索：按元素名称（中英）、符号、原子序号匹配。
     */
    fun search(query: String): List<Element> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        return elements.values.filter { el ->
            // 按符号匹配
            if (el.symbol.lowercase().contains(q)) return@filter true
            // 按英文名匹配
            if (el.name.lowercase().contains(q)) return@filter true
            // 按原子序号匹配（精确匹配或前缀）
            if (q.toIntOrNull() != null && el.atomicNumber.toString().contains(q)) return@filter true
            // 按中文名匹配
            val zh = zhMap[el.atomicNumber]
            if (zh != null && zh.nameZh.contains(q)) return@filter true
            if (zh != null && zh.pinyin.contains(q)) return@filter true
            false
        }
    }

    /**
     * 按英文名为没有中文名的元素获取显示名。
     */
    fun getDisplayName(atomicNumber: Int): String {
        val zh = zhMap[atomicNumber]
        if (zh != null) return zh.nameZh
        return elements[atomicNumber]?.name ?: "?"
    }

    private fun loadElements(): Map<Int, Element> {
        val jsonText = context.assets.open("elements.json")
            .bufferedReader()
            .use { it.readText() }
        // elements.json 顶层包含 "elements" 数组
        val wrapper = json.decodeFromString<ElementsWrapper>(jsonText)
        return wrapper.elements.associateBy { it.atomicNumber }
    }

    private fun loadZh(): Map<Int, ElementZh> {
        val jsonText = context.assets.open("elements_zh.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString<List<ElementZh>>(jsonText)
            .associateBy { it.atomicNumber }
    }
}
