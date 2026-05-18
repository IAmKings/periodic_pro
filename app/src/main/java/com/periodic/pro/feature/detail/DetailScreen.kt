package com.periodic.pro.feature.detail

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import com.periodic.pro.R
import com.periodic.pro.data.lab.LabRepository
import com.periodic.pro.data.lab.model.ChemicalReaction
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.forCategory
import com.periodic.pro.ui.components.PeriodicOutlinedButton
import com.periodic.pro.ui.components.PeriodicTextButton
import com.periodic.pro.ui.components.PropertyChip
import com.periodic.pro.ui.pattern.AtomCanvas
import com.periodic.pro.ui.pattern.PropertyGrid
import com.periodic.pro.ui.pattern.PropertyItem
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale

/**
 * 元素详情屏入口。
 *
 * @param atomicNumber 原子序号
 * @param onNavigateBack 返回回调
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    atomicNumber: Int,
    onNavigateBack: () -> Unit,
    onNavigateToLearn: (Int) -> Unit = {},
    onNavigateToDiscover: () -> Unit = {},
    onNavigateToLab: (Int) -> Unit = {},
    onNavigateToLabDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: DetailViewModel = koinViewModel { parametersOf(atomicNumber) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailContent(
        state = state,
        onIntent = viewModel::handle,
        onNavigateBack = onNavigateBack,
        onNavigateToLearn = onNavigateToLearn,
        onNavigateToDiscover = onNavigateToDiscover,
        onNavigateToLab = onNavigateToLab,
        onNavigateToLabDetail = onNavigateToLabDetail,
        modifier = modifier,
    )
}

/**
 * 纯 UI 层。用于 Preview 和 Screen 整合。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    state: DetailUiState,
    onIntent: (DetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLearn: (Int) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToLab: (Int) -> Unit,
    onNavigateToLabDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.element?.symbol ?: "",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
                actions = {
                    if (state.isReady) {
                        IconButton(onClick = { onIntent(DetailIntent.ToggleFavorite) }) {
                            Icon(
                                imageVector = if (state.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = stringResource(R.string.detail_favorite),
                                tint = if (state.isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            val element = state.element
            if (element == null) {
                // Element not found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.detail_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                ElementDetailContent(
                    element = element,
                    zhName = state.zhName,
                    reactions = state.reactions,
                    onNavigateToLearn = onNavigateToLearn,
                    onNavigateToDiscover = onNavigateToDiscover,
                    onNavigateToLab = onNavigateToLab,
                    onNavigateToLabDetail = onNavigateToLabDetail,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

/**
 * 元素详情主内容。
 *
 * 从上到下：AtomCanvas、基本信息区、属性网格、额外信息。
 */
@Composable
private fun ElementDetailContent(
    element: Element,
    zhName: String?,
    reactions: List<ChemicalReaction>,
    onNavigateToLearn: (Int) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToLab: (Int) -> Unit,
    onNavigateToLabDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = LocalCategoryColors.current.forCategory(element.category)
    val scrollState = rememberScrollState()
    var savedPosition by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.maxValue }
            .first { it > 0 }
        if (savedPosition > 0) {
            scrollState.scrollTo(savedPosition.coerceAtMost(scrollState.maxValue))
        }
        snapshotFlow { scrollState.value }
            .drop(1)
            .collect { savedPosition = it }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        // === 1. AtomCanvas（原子动画头部） ===
        AtomCanvas(
            atomicNumber = element.atomicNumber,
            period = element.period ?: 1,
            categoryColor = categoryColor,
            symbol = element.symbol,
        )

        // 电子壳层标签
        val shells = remember(element.atomicNumber) {
            ElectronShells.getElectronShells(element.atomicNumber)
        }
        androidx.compose.foundation.layout.ExperimentalLayoutApi
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Dp16),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp6),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Dp4),
        ) {
            shells.forEachIndexed { index, count ->
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = Dimensions.Dp8, vertical = Dimensions.Dp4),
                ) {
                    Text(
                        text = "${ElectronShells.shellNames.getOrElse(index) { "?" }} $count",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // === 2. 元素基本信息区 ===
        ElementInfoSection(
            element = element,
            zhName = zhName,
            categoryColor = categoryColor,
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
            thickness = 0.5.dp,
        )

        // === 3. 属性网格 ===
        PropertyGrid(
            properties = buildProperties(
                element = element,
                labelAtomicMass = stringResource(R.string.property_atomic_mass),
                labelDensity = stringResource(R.string.property_density),
                labelMeltingPoint = stringResource(R.string.property_melting_point),
                labelBoilingPoint = stringResource(R.string.property_boiling_point),
                labelElectronegativity = stringResource(R.string.property_electronegativity),
                labelAtomicRadius = stringResource(R.string.property_atomic_radius),
                labelIonizationEnergy = stringResource(R.string.property_ionization_energy),
                labelElectronConfig = stringResource(R.string.property_electron_configuration),
            ),
            modifier = Modifier.padding(vertical = Dimensions.Dp8),
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
            thickness = 0.5.dp,
        )

        // === 4. 额外信息区 ===
        ExtraInfoSection(
            element = element,
            reactions = reactions,
            onNavigateToLearn = onNavigateToLearn,
            onNavigateToDiscover = onNavigateToDiscover,
            onNavigateToLab = onNavigateToLab,
            onNavigateToLabDetail = onNavigateToLabDetail,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp32))
    }
}

/**
 * 元素基本信息区：原子序号、符号、中文名、英文名、分类标签、收藏按钮。
 */
@Composable
private fun ElementInfoSection(
    element: Element,
    zhName: String?,
    categoryColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.Dp16),
    ) {
        // 原子序号大数字
        Text(
            text = "${element.atomicNumber}",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 元素符号
                Text(
                    text = element.symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // 中文名
                if (zhName != null) {
                    Text(
                        text = zhName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // 英文名
                Text(
                    text = element.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        }

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        // 分类标签
        PropertyChip(
            text = element.category.displayName,
            selected = true,
            onClick = {},
            categoryColor = categoryColor,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp16))
    }
}

/**
 * 额外信息区：发现者、发现年份、族、周期。
 */
@Composable
private fun ExtraInfoSection(
    element: Element,
    reactions: List<ChemicalReaction>,
    onNavigateToLearn: (Int) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToLab: (Int) -> Unit,
    onNavigateToLabDetail: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.Dp16, vertical = Dimensions.Dp16),
    ) {
        Text(
            text = stringResource(R.string.detail_more_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        ExtraInfoRow(
            label = stringResource(R.string.detail_discovered_by),
            value = element.discoveredBy ?: "\u2014",
        )

        if (element.yearDiscovered != null) {
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            ExtraInfoRow(
                label = stringResource(R.string.detail_year),
                value = "${element.yearDiscovered}",
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp8))
        Row(modifier = Modifier.fillMaxWidth()) {
            ExtraInfoRow(
                label = stringResource(R.string.detail_group),
                value = element.group?.toString() ?: "\u2014",
                modifier = Modifier.weight(1f),
            )
            ExtraInfoRow(
                label = stringResource(R.string.detail_period),
                value = element.period?.toString() ?: "\u2014",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp16))
        PeriodicOutlinedButton(
            onClick = { onNavigateToLearn(element.atomicNumber) },
            text = "学习资料",
            modifier = Modifier.fillMaxWidth(),
        )

        // === 相关化学反应 ===
        if (reactions.isNotEmpty()) {
            var expanded by rememberSaveable { mutableStateOf(false) }
            val displayReactions = if (expanded) reactions else reactions.take(5)

            Spacer(modifier = Modifier.height(Dimensions.Dp16))
            HorizontalDivider(modifier = Modifier.padding(horizontal = Dimensions.Dp16))
            Spacer(modifier = Modifier.height(Dimensions.Dp12))
            Text(
                text = "相关化学反应",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Dimensions.Dp4),
            )
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            displayReactions.forEach { reaction ->
                Card(
                    onClick = { onNavigateToLabDetail(reaction.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                ) {
                    Column(modifier = Modifier.padding(Dimensions.Dp12)) {
                        Text(
                            text = reaction.equation,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = reaction.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.Dp4))
            }
            if (reactions.size > 5) {
                PeriodicTextButton(
                    onClick = { expanded = !expanded },
                    text = if (expanded) "收起 ↑" else "查看全部 ${reactions.size} 个反应 ↓",
                )
            }
        }
    }
}

/**
 * 额外信息行：标签 + 值。
 */
@Composable
private fun ExtraInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp),
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

// ========== 属性构建 ==========

/**
 * 根据 Element 数据构建 PropertyGrid 所需的属性列表。
 *
 * 通过参数传入已解析的中文属性名，避免在非 Composable 函数中调用 stringResource。
 */
private fun buildProperties(
    element: Element,
    labelAtomicMass: String,
    labelDensity: String,
    labelMeltingPoint: String,
    labelBoilingPoint: String,
    labelElectronegativity: String,
    labelAtomicRadius: String,
    labelIonizationEnergy: String,
    labelElectronConfig: String,
): List<PropertyItem> {
    return listOf(
        PropertyItem(
            name = labelAtomicMass,
            value = formatDouble(element.atomicMass),
            unit = "u",
        ),
        PropertyItem(
            name = labelDensity,
            value = formatDouble(element.density),
            unit = "g/cm\u00B3",
        ),
        PropertyItem(
            name = labelMeltingPoint,
            value = formatDouble(element.meltingPoint),
            unit = "K",
        ),
        PropertyItem(
            name = labelBoilingPoint,
            value = formatDouble(element.boilingPoint),
            unit = "K",
        ),
        PropertyItem(
            name = labelElectronegativity,
            value = formatDouble(element.electronegativity),
            unit = "Pauling",
        ),
        PropertyItem(
            name = labelAtomicRadius,
            value = formatDouble(element.atomicRadius),
            unit = "pm",
        ),
        PropertyItem(
            name = labelIonizationEnergy,
            value = formatDouble(element.ionizationEnergy),
            unit = "eV",
        ),
        PropertyItem(
            name = labelElectronConfig,
            value = element.electronConfiguration,
        ),
    )
}

/**
 * 格式化 Double? 值：最多2位小数，去除尾部零。
 * null 返回 null，由 PropertyGrid 显示 "—"。
 */
private fun formatDouble(value: Double?): String? {
    if (value == null) return null
    return if (value == value.toLong().toDouble()) {
        // 整数值（如 atomicRadius）
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}

// ========== Preview ==========

@Preview(name = "Light - H", showBackground = true)
@Preview(
    name = "Dark - H",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DetailContentHydrogenPreview() {
    PeriodicProTheme {
        DetailContent(
            state = DetailUiState(
                element = Element(
                    atomicNumber = 1,
                    symbol = "H",
                    name = "Hydrogen",
                    atomicMass = 1.008,
                    category = Category.NONMETAL,
                    electronConfiguration = "1s1",
                    electronegativity = 2.20,
                    atomicRadius = 25.0,
                    ionizationEnergy = 13.598,
                    density = 0.0000899,
                    meltingPoint = 14.01,
                    boilingPoint = 20.28,
                    period = 1,
                    group = 1,
                    discoveredBy = "Henry Cavendish",
                    yearDiscovered = 1766,
                ),
                zhName = "\u6C22",
                isFavorite = true,
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
            onNavigateToLearn = {},
            onNavigateToDiscover = {},
            onNavigateToLab = {},
            onNavigateToLabDetail = {},
        )
    }
}

@Preview(name = "Light - Fe", showBackground = true)
@Preview(
    name = "Dark - Fe",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DetailContentIronPreview() {
    PeriodicProTheme {
        DetailContent(
            state = DetailUiState(
                element = Element(
                    atomicNumber = 26,
                    symbol = "Fe",
                    name = "Iron",
                    atomicMass = 55.845,
                    category = Category.TRANSITION_METAL,
                    electronConfiguration = "[Ar] 3d6 4s2",
                    electronegativity = 1.83,
                    atomicRadius = 126.0,
                    ionizationEnergy = 7.902,
                    density = 7.874,
                    meltingPoint = 1811.0,
                    boilingPoint = 3134.0,
                    period = 4,
                    group = 8,
                    discoveredBy = "Known since ancient times",
                    yearDiscovered = null,
                ),
                zhName = "\u94C1",
                isFavorite = false,
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
            onNavigateToLearn = {},
            onNavigateToDiscover = {},
            onNavigateToLab = {},
            onNavigateToLabDetail = {},
        )
    }
}
