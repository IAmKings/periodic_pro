package com.periodic.pro.util

import java.util.Locale

/**
 * 格式化 Double? 值：整数不显示小数，小数最多 2 位，去除尾部零。
 * null 返回 null（显示 "—"）。
 */
fun formatDouble(value: Double?): String? {
    if (value == null) return null
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}
