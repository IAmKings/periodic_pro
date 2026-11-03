package com.periodic.pro.feature.detail

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.lab.model.ChemicalReaction
import com.periodic.pro.data.element.model.Category
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.periodic.pro.data.element.ElectronShells
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.CategoryAlpha
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.PeriodicTextStyles
import com.periodic.pro.theme.forCategory
import com.periodic.pro.ui.components.PeriodicOutlinedButton
import com.periodic.pro.util.formatDouble
import com.periodic.pro.ui.components.PeriodicTextButton
import com.periodic.pro.ui.components.PeriodicTopBar
import com.periodic.pro.ui.components.PropertyChip
import com.periodic.pro.ui.components.SectionHeader
import com.periodic.pro.ui.pattern.AtomCanvas
import com.periodic.pro.ui.pattern.PropertyGrid
import com.periodic.pro.ui.pattern.PropertyItem
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * 元素详情屏 — Spectra「实验报告」布局（DESIGN.md §9.3）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    atomicNumber: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToLearn: (Int) -> Unit = {},
    onNavigateToDiscover: () -> Unit = {},
    onNavigateToLab: (Int) -> Unit = {},
    onNavigateToLabDetail: (String) -> Unit = {},
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
            PeriodicTopBar(
                title = state.element?.symbol ?: "",
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
                        // 收藏心形 — 点击弹跳（DESIGN.md §7）
                        val scale by animateFloatAsState(
                            targetValue = if (state.isFavorite) 1.15f else 1f,
                            animationSpec = com.periodic.pro.theme.Motion.BounceSpring,
                            label = "favoriteScale",
                        )
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
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                            )
                        }
                    }
                },
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
                    zhDescription = state.zhDescription,
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
 * 元素详情主内容：Hero → 电子壳层 → 属性卡 → 档案卡 → 相关反应。
 */
@Composable
private fun ElementDetailContent(
    element: Element,
    zhName: String?,
    zhDescription: String?,
    reactions: List<ChemicalReaction>,
    onNavigateToLearn: (Int) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToLab: (Int) -> Unit,
    onNavigateToLabDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = LocalCategoryColors.current.forCategory(element.category)
    val scrollState = rememberScrollState()
    var savedPosition by rememberSaveable { mutableIntStateOf(0) }
    var showCelsiusState by rememberSaveable { mutableStateOf(true) }

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
        // === 1. Hero：分类色渐变 + 紧凑原子模型 + 元素标识 ===
        DetailHero(
            element = element,
            zhName = zhName,
            zhDescription = zhDescription,
            categoryColor = categoryColor,
        )

        // 电子壳层标签（tnum 小 chip 行）
        val shells = remember(element.atomicNumber) {
            ElectronShells.getElectronShells(element.atomicNumber)
        }
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Dp16),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
        ) {
            shells.forEachIndexed { index, count ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                ) {
                    Text(
                        text = "${ElectronShells.shellNames.getOrElse(index) { "?" }}$count",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Dimensions.Dp12, vertical = Dimensions.Dp6),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp24))

        // === 2. 属性卡 ===
        Column(modifier = Modifier.padding(horizontal = Dimensions.Dp16)) {
            SectionHeader(
                title = stringResource(R.string.detail_properties_section),
                trailing = {
                    TemperatureToggle(
                        showCelsius = showCelsiusState,
                        onToggle = { showCelsiusState = !showCelsiusState },
                    )
                },
            )
        }
        Spacer(modifier = Modifier.height(Dimensions.Dp12))
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
                showCelsius = showCelsiusState,
                onToggleTemp = { showCelsiusState = !showCelsiusState },
            ),
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp24))

        // === 3. 档案卡 ===
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

/** °C/K 分段切换（紧凑型） */
@Composable
private fun TemperatureToggle(
    showCelsius: Boolean,
    onToggle: () -> Unit,
) {
    val options = listOf("\u2103", "K")
    val currentIndex = if (showCelsius) 0 else 1
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == currentIndex
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onToggle() }
                    .padding(horizontal = Dimensions.Dp12, vertical = Dimensions.Dp4),
            )
        }
    }
}

/**
 * Hero 区：分类色渐变底 + 左原子模型 / 右元素标识。
 */
@Composable
private fun DetailHero(
    element: Element,
    zhName: String?,
    zhDescription: String?,
    categoryColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = CategoryAlpha.HERO),
                        Color.Transparent,
                    ),
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimensions.Dp8,
                    end = Dimensions.Dp16,
                    top = Dimensions.Dp8,
                    bottom = Dimensions.Dp8,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 紧凑原子模型
            AtomCanvas(
                atomicNumber = element.atomicNumber,
                period = element.period ?: 1,
                categoryColor = categoryColor,
                symbol = element.symbol,
                scale = 0.62f,
                modifier = Modifier.width(160.dp),
            )

            Spacer(modifier = Modifier.width(Dimensions.Dp8))

            Column(modifier = Modifier.weight(1f)) {
                // 原子序号大数字（displayLarge tnum）
                Text(
                    text = "${element.atomicNumber}",
                    style = MaterialTheme.typography.displayLarge,
                    color = categoryColor,
                )
                Text(
                    text = element.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (zhName != null) {
                    Text(
                        text = "$zhName · ${element.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.Dp8))
                PropertyChip(
                    text = element.category.displayName,
                    selected = true,
                    onClick = {},
                    categoryColor = categoryColor,
                )
                if (zhDescription != null) {
                    Spacer(modifier = Modifier.height(Dimensions.Dp8))
                    Text(
                        text = zhDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                    )
                }
            }
        }
    }
}

/**
 * 档案卡：发现者/年份/族/周期 + 学习资料入口 + 相关反应。
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
            .padding(horizontal = Dimensions.Dp16),
    ) {
        SectionHeader(title = stringResource(R.string.detail_more_info))
        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(modifier = Modifier.padding(Dimensions.Dp16)) {
                ExtraInfoRow(
                    label = stringResource(R.string.detail_discovered_by),
                    value = element.discoveredBy ?: "\u2014",
                )
                if (element.yearDiscovered != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Dimensions.Dp8),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    ExtraInfoRow(
                        label = stringResource(R.string.detail_year),
                        value = "${element.yearDiscovered}",
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimensions.Dp8),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
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
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp16))
        PeriodicOutlinedButton(
            onClick = { onNavigateToLearn(element.atomicNumber) },
            text = stringResource(R.string.detail_learning_material),
            modifier = Modifier.fillMaxWidth(),
        )

        // === 相关化学反应 ===
        if (reactions.isNotEmpty()) {
            var expanded by rememberSaveable { mutableStateOf(false) }
            val displayReactions = if (expanded) reactions else reactions.take(5)

            Spacer(modifier = Modifier.height(Dimensions.Dp24))
            SectionHeader(title = stringResource(R.string.detail_related_reactions))
            Spacer(modifier = Modifier.height(Dimensions.Dp12))
            displayReactions.forEach { reaction ->
                ReactionRow(
                    reaction = reaction,
                    onClick = { onNavigateToLabDetail(reaction.id) },
                )
                Spacer(modifier = Modifier.height(Dimensions.Dp8))
            }
            if (reactions.size > 5) {
                PeriodicTextButton(
                    onClick = { expanded = !expanded },
                    text = if (expanded) {
                        stringResource(R.string.detail_collapse)
                    } else {
                        stringResource(R.string.detail_expand_reactions, reactions.size)
                    },
                )
            }
        }
    }
}

/**
 * 反应行卡：等宽方程式 + 名称，发丝描边。
 */
@Composable
private fun ReactionRow(
    reaction: ChemicalReaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.small
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        interactionSource = interaction,
    ) {
        Column(modifier = Modifier.padding(Dimensions.Dp12)) {
            Text(
                text = reaction.equation,
                style = PeriodicTextStyles.Equation,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimensions.Dp4))
            Text(
                text = reaction.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

/**
 * 档案行：标签 + 值（发丝分隔线由调用方控制）。
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
            style = MaterialTheme.typography.labelMedium,
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
    showCelsius: Boolean,
    onToggleTemp: () -> Unit,
): List<PropertyItem> {
    val tempUnit = if (showCelsius) "\u2103" else "K"
    val tempConvert: (Double?) -> Double? = if (showCelsius) { { it?.minus(273.15) } } else { { it } }
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
            value = tempConvert(element.meltingPoint)?.let { formatDouble(it) },
            unit = tempUnit,
            onClick = onToggleTemp,
        ),
        PropertyItem(
            name = labelBoilingPoint,
            value = tempConvert(element.boilingPoint)?.let { formatDouble(it) },
            unit = tempUnit,
            onClick = onToggleTemp,
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
            valueAnnotated = element.electronConfiguration?.let { electronConfigToAnnotated(it) },
        ),
    )
}

/**
 * 将电子排布字符串转为上标数字的 AnnotatedString。
 * "[Rn] 5f14 6d10 7s2 7p5" → 数字上标
 */
private fun electronConfigToAnnotated(config: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < config.length) {
            val ch = config[i]
            if (ch.isDigit() && (i == 0 || !config[i - 1].isDigit())) {
                val prev = if (i > 0) config[i - 1] else ' '
                if (prev.isLetter() && prev != '[' && prev != ']') {
                    val start = i
                    while (i < config.length && config[i].isDigit()) i++
                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = androidx.compose.ui.unit.TextUnit(
                                11f,
                                androidx.compose.ui.unit.TextUnitType.Sp,
                            ),
                        )
                    ) { append(config.substring(start, i)) }
                    continue
                }
            }
            append(ch)
            i++
        }
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
