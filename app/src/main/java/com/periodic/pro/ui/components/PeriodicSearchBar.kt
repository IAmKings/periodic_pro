package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme

/**
 * 搜索栏 — 填充式（DESIGN.md §8.3）。
 *
 * surfaceVariant 底 + 8dp 圆角 + 52dp 高；
 * 聚焦时 2dp primary 描边、底色转 surface；有内容时尾部 Clear。
 */
@Composable
fun PeriodicSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    onSubmit: () -> Unit = {},
    searchContentDescription: String? = null,
    clearContentDescription: String? = null,
    cursorAtEndTrigger: Int = 0,
) {
    var textState by remember { mutableStateOf(TextFieldValue(query)) }
    var lastTrigger by remember { mutableIntStateOf(cursorAtEndTrigger) }
    var isFocused by remember { mutableStateOf(false) }

    // cursorAtEndTrigger 自增时置光标到末尾（仅一次）
    if (cursorAtEndTrigger != lastTrigger) {
        lastTrigger = cursorAtEndTrigger
        textState = TextFieldValue(query, TextRange(query.length))
    } else if (textState.text != query) {
        // 外部 query 变化（非用户输入）→ 同步文本但保留光标
        textState = textState.copy(text = query)
    }

    val containerColor by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(150),
        label = "searchContainer",
    )

    OutlinedTextField(
        value = textState,
        onValueChange = {
            textState = it
            onQueryChange(it.text)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .onFocusChanged { isFocused = it.isFocused },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        shape = MaterialTheme.shapes.small,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = searchContentDescription,
                tint = if (isFocused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = clearContentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Preview(name = "Light Empty", showBackground = true)
@Preview(name = "Dark Empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PeriodicSearchBarEmptyPreview() {
    PeriodicProTheme {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(Dimensions.Dp16)) {
            PeriodicSearchBar(query = "", onQueryChange = {}, placeholder = "搜索元素 / Fe / 26 / 铁")
        }
    }
}

@Preview(name = "Light Filled", showBackground = true)
@Preview(name = "Dark Filled", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PeriodicSearchBarFilledPreview() {
    PeriodicProTheme {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(Dimensions.Dp16)) {
            PeriodicSearchBar(query = "H", onQueryChange = {}, placeholder = "搜索元素 / Fe / 26 / 铁")
        }
    }
}
