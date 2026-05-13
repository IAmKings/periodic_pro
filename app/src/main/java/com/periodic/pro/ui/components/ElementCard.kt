package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.Elevation
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.forCategory

/**
 * 元素卡片组件。
 *
 * 顶部 4dp 高 Category 色条 + 原子序号 + 符号(titleLarge) + 中文名(bodyMedium)。
 * 圆角 shapeMedium(12dp)，L1 阴影(Elevation.Shadow1)。
 * 通过 [LocalCategoryColors] 获取元素分类色值。
 *
 * @param element 元素数据
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param zhName 元素中文名（可选）
 */
@Composable
fun ElementCard(
    element: Element,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    zhName: String? = null,
) {
    val categoryColor = LocalCategoryColors.current.forCategory(element.category)
    val shape = MaterialTheme.shapes.medium

    Card(
        onClick = onClick,
        modifier = modifier
            .width(88.dp)
            .height(100.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Shadow1),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column {
            // 顶部 4dp 色条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.Dp4)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(categoryColor),
            )
            // 内容区
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.Dp12),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 原子序号
                Text(
                    text = "${element.atomicNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                // 符号
                Text(
                    text = element.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                // 中文名
                if (zhName != null) {
                    Text(
                        text = zhName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private val sampleHydrogen = Element(
    atomicNumber = 1,
    symbol = "H",
    name = "Hydrogen",
    atomicMass = 1.008,
    category = Category.NONMETAL,
    group = 1,
    period = 1,
)

private val sampleIron = Element(
    atomicNumber = 26,
    symbol = "Fe",
    name = "Iron",
    atomicMass = 55.845,
    category = Category.TRANSITION_METAL,
    group = 8,
    period = 4,
)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ElementCardHydrogenPreview() {
    PeriodicProTheme {
        ElementCard(
            element = sampleHydrogen,
            onClick = {},
            zhName = "氢",
        )
    }
}

@Preview(name = "Light - Fe", showBackground = true)
@Preview(name = "Dark - Fe", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ElementCardIronPreview() {
    PeriodicProTheme {
        ElementCard(
            element = sampleIron,
            onClick = {},
            zhName = "铁",
        )
    }
}
