package com.periodic.pro.feature.compare

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.forCategory
import com.periodic.pro.util.formatDouble
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

// ===== Constants =====

/** 左列（属性名）宽度 */
private val LABEL_COL_WIDTH = 88.dp

/** 每列元素宽度 */
private val ELEMENT_COL_WIDTH = 120.dp

/** 表头区域高度（4dp 色条 + 符号 + 名称） */
private val HEADER_HEIGHT = 72.dp

/** 每行数据行高度 */
private val ROW_HEIGHT = 40.dp

/** 色条高度 */
private val COLOR_BAR_HEIGHT = 4.dp

// ===== Public Screen =====

/**
 * 元素对比屏入口。
 *
 * @param ids 待对比元素的原子序号列表
 * @param onNavigateBack 返回回调
 * @param onNavigateToTable 导航到周期表
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    ids: List<Int>,
    onNavigateBack: () -> Unit,
    onNavigateToTable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CompareViewModel = koinViewModel { parametersOf(ids) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    CompareContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToTable = onNavigateToTable,
        modifier = modifier,
    )
}

// ===== Content =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompareContent(
    state: CompareUiState,
    onNavigateBack: () -> Unit,
    onNavigateToTable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_compare),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isEmpty -> EmptyCompareContent(onNavigateToTable = onNavigateToTable)
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> CompareTableContent(
                    elements = state.elements,
                    zhMap = state.zhMap,
                )
            }
        }
    }
}

// ===== Empty State =====

@Composable
private fun EmptyCompareContent(
    onNavigateToTable: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.Dp32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = Dimensions.Dp16)
                .height(Dimensions.Dp48)
                .width(Dimensions.Dp48),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Text(
            text = stringResource(R.string.compare_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp24))

        Button(onClick = onNavigateToTable) {
            Text(
                text = stringResource(R.string.compare_empty_action),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

// ===== Comparison Table =====

/**
 * 属性定义：提取 Element 中某属性的值并格式化为显示文本。
 */
private data class PropertyDef(
    val name: String,
    val formatValue: (Element) -> String?,
)

/**
 * 构建属性定义列表。
 * 在 Composable 中调用以使用 stringResource 解析中文属性名。
 */
@Composable
private fun buildPropertyDefs(
    labelCategory: String = stringResource(R.string.property_category),
    labelAtomicMass: String = stringResource(R.string.property_atomic_mass),
    labelDensity: String = stringResource(R.string.property_density),
    labelMeltingPoint: String = stringResource(R.string.property_melting_point),
    labelBoilingPoint: String = stringResource(R.string.property_boiling_point),
    labelElectronegativity: String = stringResource(R.string.property_electronegativity),
    labelAtomicRadius: String = stringResource(R.string.property_atomic_radius),
    labelIonizationEnergy: String = stringResource(R.string.property_ionization_energy),
    labelElectronConfig: String = stringResource(R.string.property_electron_configuration),
    labelPeriod: String = stringResource(R.string.detail_period),
    labelGroup: String = stringResource(R.string.detail_group),
): List<PropertyDef> {
    // Pre-resolve category display names from string resources
    val categoryNames = mapOf(
        Category.ALKALI_METAL to stringResource(R.string.category_alkali_metal),
        Category.ALKALINE_EARTH to stringResource(R.string.category_alkaline_earth_metal),
        Category.TRANSITION_METAL to stringResource(R.string.category_transition_metal),
        Category.POST_TRANSITION to stringResource(R.string.category_post_transition_metal),
        Category.METALLOID to stringResource(R.string.category_metalloid),
        Category.NONMETAL to stringResource(R.string.category_nonmetal),
        Category.HALOGEN to stringResource(R.string.category_halogen),
        Category.NOBLE_GAS to stringResource(R.string.category_noble_gas),
        Category.LANTHANIDE to stringResource(R.string.category_lanthanide),
        Category.ACTINIDE to stringResource(R.string.category_actinide),
    )

    return listOf(
        PropertyDef(labelAtomicMass) { formatDouble(it.atomicMass) },
        PropertyDef(labelDensity) { formatDouble(it.density) },
        PropertyDef(labelMeltingPoint) { formatDouble(it.meltingPoint) },
        PropertyDef(labelBoilingPoint) { formatDouble(it.boilingPoint) },
        PropertyDef(labelElectronegativity) { formatDouble(it.electronegativity) },
        PropertyDef(labelAtomicRadius) { formatDouble(it.atomicRadius) },
        PropertyDef(labelIonizationEnergy) { formatDouble(it.ionizationEnergy) },
        PropertyDef(labelElectronConfig) { it.electronConfiguration },
        PropertyDef(labelCategory) { categoryNames[it.category] ?: it.category.displayName },
        PropertyDef(labelPeriod) { it.period?.toString() },
        PropertyDef(labelGroup) { it.group?.toString() },
    )
}

@Composable
private fun CompareTableContent(
    elements: List<Element>,
    zhMap: Map<Int, String>,
) {
    val properties = buildPropertyDefs()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // === Header Row ===
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left fixed: blank corner cell
            Box(modifier = Modifier.width(LABEL_COL_WIDTH).height(HEADER_HEIGHT))

            // Right scrollable: element column headers
            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                elements.forEach { element ->
                    ElementColumnHeader(
                        element = element,
                        zhName = zhMap[element.atomicNumber],
                        modifier = Modifier.width(ELEMENT_COL_WIDTH),
                    )
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp)

        // === Data Rows ===
        properties.forEachIndexed { index, prop ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ROW_HEIGHT),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left fixed: property name
                PropertyLabelCell(
                    name = prop.name,
                    modifier = Modifier.width(LABEL_COL_WIDTH),
                )

                // Right scrollable: values for each element
                Row(
                    modifier = Modifier.horizontalScroll(horizontalScrollState),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    elements.forEach { element ->
                        PropertyValueCell(
                            value = prop.formatValue(element),
                            modifier = Modifier
                                .width(ELEMENT_COL_WIDTH)
                                .height(ROW_HEIGHT),
                        )
                    }
                }
            }

            if (index < properties.lastIndex) {
                HorizontalDivider(thickness = 0.5.dp)
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp16))
    }
}

// ===== Element Column Header =====

@Composable
private fun ElementColumnHeader(
    element: Element,
    zhName: String?,
    modifier: Modifier = Modifier,
) {
    val categoryColor = LocalCategoryColors.current.forCategory(element.category)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 4dp Category color bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(COLOR_BAR_HEIGHT)
                .background(categoryColor),
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp4))

        // Element symbol
        Text(
            text = element.symbol,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )

        // Element name (zh if available, else en)
        val displayName = zhName ?: element.name
        Text(
            text = displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp4))
    }
}

// ===== Property Label Cell (Fixed Left Column) =====

@Composable
private fun PropertyLabelCell(
    name: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(start = Dimensions.Dp12, end = Dimensions.Dp8),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ===== Property Value Cell =====

@Composable
private fun PropertyValueCell(
    value: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = Dimensions.Dp8),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value ?: "\u2014",
            style = MaterialTheme.typography.bodyMedium,
            color = if (value != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ===== Helpers =====

// ===== Preview Data =====

private val previewHydrogen = Element(
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
)

private val previewHelium = Element(
    atomicNumber = 2,
    symbol = "He",
    name = "Helium",
    atomicMass = 4.0026,
    category = Category.NOBLE_GAS,
    electronConfiguration = "1s2",
    electronegativity = null,
    atomicRadius = 31.0,
    ionizationEnergy = 24.587,
    density = 0.0001785,
    meltingPoint = null,
    boilingPoint = 4.22,
    period = 1,
    group = 18,
)

// ===== Previews =====

@Preview(name = "Light - 2 Elements", showBackground = true)
@Preview(
    name = "Dark - 2 Elements",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CompareContentWithElementsPreview() {
    PeriodicProTheme {
        CompareContent(
            state = CompareUiState(
                elements = listOf(previewHydrogen, previewHelium),
                zhMap = mapOf(1 to "\u6C22", 2 to "\u6C19"),
                isLoading = false,
                isEmpty = false,
            ),
            onNavigateBack = {},
            onNavigateToTable = {},
        )
    }
}

@Preview(name = "Empty State - Light", showBackground = true)
@Preview(
    name = "Empty State - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CompareContentEmptyPreview() {
    PeriodicProTheme {
        CompareContent(
            state = CompareUiState(
                isEmpty = true,
                isLoading = false,
            ),
            onNavigateBack = {},
            onNavigateToTable = {},
        )
    }
}
