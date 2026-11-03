package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.forCategory
import com.periodic.pro.theme.pressScale

/**
 * 元素磁贴 — Spectra 设计语言核心列表组件（DESIGN.md §8.2）。
 *
 * 表面 = surface + 1dp 发丝描边 + 左侧 3dp 分类色竖条（inset）；
 * 符号用分类色，序号 tnum；按压缩放反馈。
 *
 * @param element 元素数据
 * @param onClick 点击回调
 * @param modifier Modifier（横滑场景配合 [elementTileRowSize]，列表场景通栏）
 * @param zhName 元素中文名（可选）
 * @param trailingIcon 右上角小图标（如收藏标记）
 */
@Composable
fun ElementTile(
    element: Element,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    zhName: String? = null,
    trailingIcon: ImageVector? = null,
) {
    val categoryColor = LocalCategoryColors.current.forCategory(element.category)
    val shape = MaterialTheme.shapes.medium
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .pressScale(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
            ) { onClick() },
    ) {
        // 左侧 3dp 分类色竖条（上下 inset）
        Box(
            modifier = Modifier
                .padding(vertical = Dimensions.Dp8)
                .padding(start = Dimensions.Dp8)
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(categoryColor),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = Dimensions.Dp12,
                    end = Dimensions.Dp12,
                    top = Dimensions.Dp12,
                    bottom = Dimensions.Dp12,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 原子序号（tnum）
                Text(
                    text = "${element.atomicNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.Dp4))
            // 符号 + 中文名（同一行，符号用分类色）
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = element.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    color = categoryColor,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(Dimensions.Dp8))
                Column(modifier = Modifier.padding(bottom = 1.dp)) {
                    if (zhName != null) {
                        Text(
                            text = zhName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = element.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** 横滑场景固定尺寸：128dp 宽 × 88dp 高 */
fun Modifier.elementTileRowSize(): Modifier = this
    .width(128.dp)
    .height(88.dp)

// ===== Previews =====

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

@Preview(name = "Light - H", showBackground = true)
@Preview(name = "Dark - H", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ElementTileHydrogenPreview() {
    PeriodicProTheme {
        ElementTile(
            element = sampleHydrogen,
            onClick = {},
            zhName = "氢",
            modifier = Modifier
                .elementTileRowSize()
                .padding(8.dp),
        )
    }
}

@Preview(name = "Light - Fe Wide", showBackground = true)
@Preview(name = "Dark - Fe Wide", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ElementTileIronWidePreview() {
    PeriodicProTheme {
        ElementTile(
            element = sampleIron,
            onClick = {},
            zhName = "铁",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }
}
