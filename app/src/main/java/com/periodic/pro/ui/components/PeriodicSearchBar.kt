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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.periodic.pro.theme.PeriodicProTheme

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
    var lastTrigger by remember { mutableStateOf(cursorAtEndTrigger) }

    // cursorAtEndTrigger自增时置光标到末尾（仅一次）
    if (cursorAtEndTrigger != lastTrigger) {
        lastTrigger = cursorAtEndTrigger
        textState = TextFieldValue(query, TextRange(query.length))
    } else if (textState.text != query) {
        // 外部query变化（非用户输入）→ 同步文本但保留光标
        textState = textState.copy(text = query)
    }

    OutlinedTextField(
        value = textState,
        onValueChange = {
            textState = it
            onQueryChange(it.text)
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        shape = MaterialTheme.shapes.small,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = searchContentDescription)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = clearContentDescription)
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
