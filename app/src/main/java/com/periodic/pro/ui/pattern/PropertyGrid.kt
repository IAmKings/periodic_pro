package com.periodic.pro.ui.pattern

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import android.content.res.Configuration

/**
 * 属性条目，包含名称、值和可选单位。
 */
data class PropertyItem(
    val name: String,
    val value: String?,
    val unit: String? = null,
)

/**
 * 2 列属性网格。
 *
 * 每行展示 2 个属性（属性名 + 属性值 + 单位），null 值显示 "—"。
 * 各项之间用分隔线隔开。
 *
 * @param properties 属性列表
 * @param modifier Modifier
 */
@Composable
fun PropertyGrid(
    properties: List<PropertyItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        properties.chunked(2).forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Dimensions.Dp16),
                    thickness = 0.5.dp,
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
                // Fill remaining space if only 1 item in the last row
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PropertyCell(
    prop: PropertyItem,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = prop.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dimensions.Dp4))
        Text(
            text = prop.value ?: "\u2014",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (prop.value != null && prop.unit != null) {
            Text(
                text = prop.unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
