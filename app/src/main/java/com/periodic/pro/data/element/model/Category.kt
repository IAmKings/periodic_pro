package com.periodic.pro.data.element.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 元素分类枚举。
 *
 * @param id 分类标识（与 JSON 数据中的字段值一致）
 * @param displayName 中文显示名
 */
enum class Category(val id: String, val displayName: String) {
    ALKALI_METAL("alkali-metal", "碱金属"),
    ALKALINE_EARTH("alkaline-earth", "碱土金属"),
    TRANSITION_METAL("transition-metal", "过渡金属"),
    POST_TRANSITION("post-transition", "后过渡金属"),
    METALLOID("metalloid", "类金属"),
    NONMETAL("nonmetal", "非金属"),
    HALOGEN("halogen", "卤素"),
    NOBLE_GAS("noble-gas", "稀有气体"),
    LANTHANIDE("lanthanide", "镧系"),
    ACTINIDE("actinide", "锕系"),
}

/**
 * 自定义 Kotlinx Serialization 序列化器。
 *
 * elements.json 中的 category 字段使用英文标识（如 "alkaline-earth-metal"），
 * 而枚举的 id 使用的是精简形式（如 "alkaline-earth"），因此需要显式映射。
 */
object CategorySerializer : KSerializer<Category> {
    override val descriptor = PrimitiveSerialDescriptor("Category", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Category) {
        encoder.encodeString(
            when (value) {
                Category.ALKALI_METAL -> "alkali-metal"
                Category.ALKALINE_EARTH -> "alkaline-earth-metal"
                Category.TRANSITION_METAL -> "transition-metal"
                Category.POST_TRANSITION -> "post-transition-metal"
                Category.METALLOID -> "metalloid"
                Category.NONMETAL -> "nonmetal"
                Category.HALOGEN -> "halogen"
                Category.NOBLE_GAS -> "noble-gas"
                Category.LANTHANIDE -> "lanthanide"
                Category.ACTINIDE -> "actinide"
            },
        )
    }

    override fun deserialize(decoder: Decoder): Category {
        val value = decoder.decodeString()
        return when (value) {
            "alkali-metal" -> Category.ALKALI_METAL
            "alkaline-earth-metal" -> Category.ALKALINE_EARTH
            "transition-metal" -> Category.TRANSITION_METAL
            "post-transition-metal" -> Category.POST_TRANSITION
            "metalloid" -> Category.METALLOID
            "nonmetal" -> Category.NONMETAL
            "halogen" -> Category.HALOGEN
            "noble-gas" -> Category.NOBLE_GAS
            "lanthanide" -> Category.LANTHANIDE
            "actinide" -> Category.ACTINIDE
            else -> throw IllegalArgumentException("Unknown category value: $value")
        }
    }
}
