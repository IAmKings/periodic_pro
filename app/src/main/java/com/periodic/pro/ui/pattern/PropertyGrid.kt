package com.periodic.pro.ui.pattern

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.FONT_FEATURE_TNUM
import com.periodic.pro.theme.PeriodicProTheme

/**
 * 属性卡 — 2 列数据网格（DESIGN.md §8.6）。
 *
 * surface + 1dp 发丝描边卡片；行间发丝分隔线；
 * 标签 labelMedium / 数值 bodyLarge(tnum) + 单位 bodySmall 弱化；
 * null 值统一 "—"（弱化色）。
 *
 * @param properties 属性列表
 * @param modifier Modifier
 */
@Composable
fun PropertyGrid(
    properties: List<PropertyItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            properties.chunked(2).forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Dimensions.Dp16),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.Dp16, vertical = Dimensions.Dp12),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp16),
                ) {
                    row.forEach { prop ->
                        PropertyCell(
                            prop = prop,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // 最后一行只有 1 项时补位
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 属性条目，包含名称、值和可选单位。
 */
data class PropertyItem(
    val name: String,
    val value: String?,
    val unit: String? = null,
    val valueAnnotated: AnnotatedString? = null,
    val onClick: (() -> Unit)? = null,
)

@Composable
private fun PropertyCell(
    prop: PropertyItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.then(
            if (prop.onClick != null) Modifier.clickable { prop.onClick() } else Modifier
        )
    ) {
        Text(
            text = prop.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dimensions.Dp4))
        Row(verticalAlignment = Alignment.Bottom) {
            if (prop.valueAnnotated != null) {
                Text(
                    text = prop.valueAnnotated,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFeatureSettings = FONT_FEATURE_TNUM,
                    ),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Text(
                    text = prop.value ?: "\u2014",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFeatureSettings = FONT_FEATURE_TNUM,
                    ),
                    fontWeight = FontWeight.Medium,
                    color = if (prop.value == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            if (prop.value != null && prop.unit != null) {
                Spacer(modifier = Modifier.width(Dimensions.Dp4))
                Text(
                    text = prop.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 1.dp),
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PropertyGridPreview() {
    PeriodicProTheme {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            PropertyGrid(
                properties = listOf(
                    PropertyItem(name = "原子量", value = "1.008", unit = "u"),
                    PropertyItem(name = "密度", value = "0.00009", unit = "g/cm\u00B3"),
                    PropertyItem(name = "熔点", value = "14.01", unit = "K"),
                    PropertyItem(name = "沸点", value = "20.28", unit = "K"),
                    PropertyItem(name = "电负性", value = "2.20", unit = "Pauling"),
                    PropertyItem(name = "原子半径", value = "25", unit = "pm"),
                    PropertyItem(name = "电离能", value = "13.6", unit = "eV"),
                    PropertyItem(name = "电子排布", value = "1s1"),
                ),
            )
        }
    }
}
