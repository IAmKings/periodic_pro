package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.periodic.pro.theme.PeriodicProTheme

/**
 * 搜索框组件。
 *
 * M3 OutlinedTextField 包装，圆角 shapeSmall(8dp)，
 * 搜索 icon + clear 按钮。
 *
 * @param query 当前搜索文本
 * @param onQueryChange 搜索文本变化回调
 * @param modifier Modifier
 * @param placeholder 占位提示文字
 * @param searchContentDescription 搜索图标的无障碍描述
 * @param clearContentDescription 清除图标的无障碍描述
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
    cursorAtEnd: Boolean = false,
) {
    if (cursorAtEnd) {
        val tfv = remember(query) { TextFieldValue(query, TextRange(query.length)) }
        OutlinedTextField(
            value = tfv,
            onValueChange = { onQueryChange(it.text) },
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            shape = MaterialTheme.shapes.small,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = searchContentDescription) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = clearContentDescription)
                    }
                }
            },
        )
    } else {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        shape = MaterialTheme.shapes.small,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSubmit() },
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = searchContentDescription,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = clearContentDescription,
                    )
                }
            }
        },
    )
}

@Preview(name = "Light Empty", showBackground = true)
@Preview(name = "Dark Empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PeriodicSearchBarEmptyPreview() {
    PeriodicProTheme {
        PeriodicSearchBar(query = "", onQueryChange = {}, placeholder = "Search elements...")
    }
}

@Preview(name = "Light Filled", showBackground = true)
@Preview(name = "Dark Filled", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PeriodicSearchBarFilledPreview() {
    PeriodicProTheme {
        PeriodicSearchBar(query = "H", onQueryChange = {}, placeholder = "Search elements...")
    }
}
