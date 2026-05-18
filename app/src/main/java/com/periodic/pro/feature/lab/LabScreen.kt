@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
)

package com.periodic.pro.feature.lab

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.lab.model.ChemicalReaction
import com.periodic.pro.data.lab.model.ReactionLevel
import com.periodic.pro.data.lab.model.ReactionType
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.Elevation
import com.periodic.pro.theme.LabColors
import com.periodic.pro.theme.PeriodicProTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Lab 屏入口。
 *
 * 展示化学反应列表，支持按年级/类型筛选，点击查看反应详情。
 *
 * @param onNavigateToDetail 导航到元素详情
 * @param modifier Modifier
 */
@Composable
fun LabScreen(
    onNavigateToDetail: (Int) -> Unit,
    initialReactionId: String? = null,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    vm: LabViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(initialReactionId) {
        val id = initialReactionId ?: return@LaunchedEffect
        vm.handle(LabIntent.SelectReaction(id))
    }

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is LabEffect.NavigateToDetail -> onNavigateToDetail(effect.atomicNumber)
            }
        }
    }

    when (state.navMode) {
        LabNavMode.LIST -> LabListContent(
            state = state,
            onIntent = vm::handle,
            modifier = modifier,
        )
        LabNavMode.DETAIL -> LabDetailContent(
            reaction = state.selectedReaction,
            symbolMap = state.symbolMap,
            onBack = {
                if (initialReactionId != null) onNavigateBack()
                else vm.handle(LabIntent.BackToList)
            },
            onNavigateToDetail = { atomicNumber ->
                vm.handle(LabIntent.NavigateToDetail(atomicNumber))
            },
            modifier = modifier,
        )
    }
}

// ===== List view =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabListContent(
    state: LabUiState,
    onIntent: (LabIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_lab),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.loading),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = Dimensions.Dp16,
                    vertical = Dimensions.Dp8,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
            ) {
                // Filter chips
                item(key = "filter_chips") {
                    FilterChipsRow(
                        state = state,
                        onLevelFilter = { level ->
                            onIntent(LabIntent.FilterByLevel(level))
                        },
                        onTypeFilter = { type ->
                            onIntent(LabIntent.FilterByType(type))
                        },
                    )
                }

                item(key = "filter_spacer") {
                    Spacer(modifier = Modifier.height(Dimensions.Dp4))
                }

                if (state.filteredReactions.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimensions.Dp48),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.lab_no_reactions),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    // Group reactions by type
                    val grouped = state.filteredReactions.groupBy { it.type }
                    val typeOrder = listOf(
                        ReactionType.COMBINATION,
                        ReactionType.DECOMPOSITION,
                        ReactionType.DISPLACEMENT,
                        ReactionType.DOUBLE_DISPLACEMENT,
                        ReactionType.REDOX,
                        ReactionType.OTHER,
                    )

                    typeOrder.forEach { type ->
                        val reactions = grouped[type].orEmpty()
                        if (reactions.isNotEmpty()) {
                            // Sticky header
                            item(key = "header_${type.name}") {
                                ReactionTypeHeader(type = type)
                            }
                            items(reactions, key = { "reaction_${it.id}" }) { reaction ->
                                ReactionListItem(
                                    reaction = reaction,
                                    onClick = {
                                        onIntent(LabIntent.SelectReaction(reaction.id))
                                    },
                                )
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(Dimensions.Dp16))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChipsRow(
    state: LabUiState,
    onLevelFilter: (ReactionLevel?) -> Unit,
    onTypeFilter: (ReactionType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Level filter chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Dp4),
        ) {
            // All
            FilterChip(
                selected = !state.hasActiveFilter,
                onClick = {
                    onLevelFilter(null)
                    onTypeFilter(null)
                },
                label = { Text(stringResource(R.string.lab_filter_all)) },
            )
            // Junior
            FilterChip(
                selected = state.filter.activeLevel == ReactionLevel.JUNIOR &&
                    state.filter.activeType == null,
                onClick = {
                    onLevelFilter(ReactionLevel.JUNIOR)
                    onTypeFilter(null)
                },
                label = { Text(stringResource(R.string.lab_level_junior)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LabColors.JuniorBadge.copy(alpha = 0.15f),
                ),
            )
            // Senior
            FilterChip(
                selected = state.filter.activeLevel == ReactionLevel.SENIOR &&
                    state.filter.activeType == null,
                onClick = {
                    onLevelFilter(ReactionLevel.SENIOR)
                    onTypeFilter(null)
                },
                label = { Text(stringResource(R.string.lab_level_senior)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LabColors.SeniorBadge.copy(alpha = 0.15f),
                ),
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp4))

        // Type filter chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Dp4),
        ) {
            ReactionType.entries.forEach { type ->
                val isSelected = state.filter.activeType == type &&
                    state.filter.activeLevel == null
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onTypeFilter(if (isSelected) null else type)
                        onLevelFilter(null)
                    },
                    label = { Text(stringResource(typeLabelRes(type))) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = typeColor(type).copy(alpha = 0.15f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun ReactionTypeHeader(
    type: ReactionType,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Dp8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(typeColor(type)),
        )
        Spacer(modifier = Modifier.width(Dimensions.Dp8))
        Text(
            text = stringResource(typeLabelRes(type)),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ReactionListItem(
    reaction: ChemicalReaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Shadow1),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Dp12),
        ) {
            // Equation (large)
            Text(
                text = reaction.equation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(Dimensions.Dp4))

            // Name + badges
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reaction.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(Dimensions.Dp8))

                // Type badge
                TypeBadge(type = reaction.type)
                Spacer(modifier = Modifier.width(6.dp))

                // Level badge
                LevelBadge(level = reaction.level)
            }

            // Conditions + phenomenon (compact)
            val details = buildList {
                reaction.conditions?.let { add(it) }
                add(reaction.phenomenon)
            }
            if (details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.Dp4))
                Text(
                    text = details.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

// ===== Detail view =====

@Composable
private fun LabDetailContent(
    reaction: ChemicalReaction?,
    symbolMap: Map<Int, String>,
    onBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (reaction == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.lab_detail_not_found),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = reaction.name,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Equation section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(Dimensions.Dp12),
                    )
                    .padding(Dimensions.Dp24),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = reaction.equation,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 36.sp,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp16))

            // Meta info cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.Dp16),
                verticalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
            ) {
                // Type + Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
                ) {
                    MetaChip(
                        label = stringResource(R.string.lab_reaction_type),
                        value = stringResource(typeLabelRes(reaction.type)),
                        badgeColor = typeColor(reaction.type),
                    )
                    MetaChip(
                        label = stringResource(R.string.lab_reaction_level),
                        value = stringResource(levelLabelRes(reaction.level)),
                        badgeColor = when (reaction.level) {
                            ReactionLevel.JUNIOR -> LabColors.JuniorBadge
                            ReactionLevel.SENIOR -> LabColors.SeniorBadge
                        },
                    )
                }

                // Conditions
                if (reaction.conditions != null) {
                    DetailInfoRow(
                        label = stringResource(R.string.lab_conditions),
                        value = reaction.conditions,
                    )
                }

                HorizontalDivider(thickness = 0.5.dp)

                // Phenomenon
                DetailSectionTitle(text = stringResource(R.string.lab_phenomenon))
                Text(
                    text = reaction.phenomenon,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )

                HorizontalDivider(thickness = 0.5.dp)

                // Involved elements
                DetailSectionTitle(text = stringResource(R.string.lab_involved_elements))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
                ) {
                    reaction.involvedElements.forEach { atomicNumber ->
                        ElementChip(
                            atomicNumber = atomicNumber,
                            symbol = symbolMap[atomicNumber] ?: "?",
                            onClick = { onNavigateToDetail(atomicNumber) },
                        )
                    }
                }

                // Note
                if (reaction.note != null) {
                    HorizontalDivider(thickness = 0.5.dp)
                    DetailSectionTitle(text = stringResource(R.string.lab_note))
                    Text(
                        text = reaction.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp32))
        }
    }
}

@Composable
private fun MetaChip(
    label: String,
    value: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.Dp8))
            .background(badgeColor.copy(alpha = 0.1f))
            .padding(horizontal = Dimensions.Dp12, vertical = Dimensions.Dp8),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = badgeColor,
        )
    }
}

@Composable
private fun DetailInfoRow(
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
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DetailSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun TypeBadge(
    type: ReactionType,
    modifier: Modifier = Modifier,
) {
    val color = typeColor(type)
    val bg = typeBgColor(type)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = stringResource(typeShortLabelRes(type)),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun LevelBadge(
    level: ReactionLevel,
    modifier: Modifier = Modifier,
) {
    val (color, bg) = when (level) {
        ReactionLevel.JUNIOR -> LabColors.JuniorBadge to LabColors.JuniorBadge.copy(alpha = 0.12f)
        ReactionLevel.SENIOR -> LabColors.SeniorBadge to LabColors.SeniorBadge.copy(alpha = 0.12f)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = stringResource(levelLabelRes(level)),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ElementChip(
    atomicNumber: Int,
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.Dp8))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimensions.Dp12, vertical = 6.dp),
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

// ===== Helper functions =====

private fun typeColor(type: ReactionType): Color = when (type) {
    ReactionType.COMBINATION -> LabColors.Combination
    ReactionType.DECOMPOSITION -> LabColors.Decomposition
    ReactionType.DISPLACEMENT -> LabColors.Displacement
    ReactionType.DOUBLE_DISPLACEMENT -> LabColors.DoubleDisplacement
    ReactionType.REDOX -> LabColors.Redox
    ReactionType.OTHER -> LabColors.Other
}

private fun typeBgColor(type: ReactionType): Color = when (type) {
    ReactionType.COMBINATION -> LabColors.CombinationBg
    ReactionType.DECOMPOSITION -> LabColors.DecompositionBg
    ReactionType.DISPLACEMENT -> LabColors.DisplacementBg
    ReactionType.DOUBLE_DISPLACEMENT -> LabColors.DoubleDisplacementBg
    ReactionType.REDOX -> LabColors.RedoxBg
    ReactionType.OTHER -> LabColors.OtherBg
}

private fun typeLabelRes(type: ReactionType): Int = when (type) {
    ReactionType.COMBINATION -> R.string.lab_type_combination
    ReactionType.DECOMPOSITION -> R.string.lab_type_decomposition
    ReactionType.DISPLACEMENT -> R.string.lab_type_displacement
    ReactionType.DOUBLE_DISPLACEMENT -> R.string.lab_type_double_displacement
    ReactionType.REDOX -> R.string.lab_type_redox
    ReactionType.OTHER -> R.string.lab_type_other
}

private fun typeShortLabelRes(type: ReactionType): Int = when (type) {
    ReactionType.COMBINATION -> R.string.lab_type_combination_short
    ReactionType.DECOMPOSITION -> R.string.lab_type_decomposition_short
    ReactionType.DISPLACEMENT -> R.string.lab_type_displacement_short
    ReactionType.DOUBLE_DISPLACEMENT -> R.string.lab_type_double_displacement_short
    ReactionType.REDOX -> R.string.lab_type_redox_short
    ReactionType.OTHER -> R.string.lab_type_other_short
}

private fun levelLabelRes(level: ReactionLevel): Int = when (level) {
    ReactionLevel.JUNIOR -> R.string.lab_level_junior
    ReactionLevel.SENIOR -> R.string.lab_level_senior
}

// ===== Preview =====

private val previewReactions = listOf(
    ChemicalReaction(
        id = "r001",
        name = "氢气燃烧",
        reactants = listOf("H₂", "O₂"),
        products = listOf("H₂O"),
        equation = "2H₂ + O₂ → 2H₂O",
        conditions = "点燃",
        type = ReactionType.COMBINATION,
        level = ReactionLevel.JUNIOR,
        phenomenon = "产生淡蓝色火焰，烧杯壁出现水珠，放出大量热",
        involvedElements = listOf(1, 8),
        note = "氢气是最清洁的燃料",
    ),
    ChemicalReaction(
        id = "r005",
        name = "镁条燃烧",
        reactants = listOf("Mg", "O₂"),
        products = listOf("MgO"),
        equation = "2Mg + O₂ → 2MgO",
        conditions = "点燃",
        type = ReactionType.COMBINATION,
        level = ReactionLevel.JUNIOR,
        phenomenon = "发出耀眼白光，放出大量热，生成白色固体",
        involvedElements = listOf(12, 8),
    ),
    ChemicalReaction(
        id = "r021",
        name = "铜与浓硫酸反应",
        reactants = listOf("Cu", "H₂SO₄(浓)"),
        products = listOf("CuSO₄", "SO₂", "H₂O"),
        equation = "Cu + 2H₂SO₄(浓) → CuSO₄ + SO₂↑ + 2H₂O",
        conditions = "加热",
        type = ReactionType.REDOX,
        level = ReactionLevel.SENIOR,
        phenomenon = "红色固体逐渐溶解，溶液变为蓝色，产生刺激性气味气体",
        involvedElements = listOf(29, 1, 16, 8),
    ),
)

@Preview(name = "Light - List", showBackground = true)
@Preview(
    name = "Dark - List",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LabListPreview() {
    PeriodicProTheme {
        LabListContent(
            state = LabUiState(
                allReactions = previewReactions,
                filteredReactions = previewReactions,
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Light - Detail", showBackground = true)
@Preview(
    name = "Dark - Detail",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LabDetailPreview() {
    val previewReaction = ChemicalReaction(
        id = "r010",
        name = "氢气燃烧",
        reactants = listOf("H₂", "O₂"),
        products = listOf("H₂O"),
        equation = "2H₂ + O₂ → 2H₂O",
        conditions = "点燃",
        type = ReactionType.COMBINATION,
        level = ReactionLevel.JUNIOR,
        phenomenon = "产生淡蓝色火焰，烧杯壁出现水珠，放出大量热",
        involvedElements = listOf(1, 8),
        note = "氢气是最清洁的燃料，燃烧产物只有水",
    )
    PeriodicProTheme {
        LabDetailContent(
            reaction = previewReaction,
            symbolMap = emptyMap(),
            onBack = {},
            onNavigateToDetail = {},
        )
    }
}
