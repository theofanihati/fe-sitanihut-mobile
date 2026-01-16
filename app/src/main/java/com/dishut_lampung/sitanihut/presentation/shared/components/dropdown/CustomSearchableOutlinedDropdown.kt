package com.dishut_lampung.sitanihut.presentation.shared.components.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.dishut_lampung.sitanihut.domain.model.Commodity
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomSearchableOutlinedDropdown(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onOptionSelected: (T) -> Unit,
    options: List<T>,
    itemLabel: (T) -> String = { it.toString() },
    label: String,
    placeholder: String = "Pilih Opsi",
    isError: Boolean = false,
    errorMessage: String? = null,
    asteriskAtEnd: Boolean = false,
    rounded: Int = 40,
    enabled: Boolean = true,
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    bgColor: Color = Color.White,
    debounceTime: Long = 500L
) {
    var expanded by remember { mutableStateOf(false) }
    val isErrorState = isError || errorMessage != null
    var filteredOptions by remember { mutableStateOf(options) }

    LaunchedEffect(value, options) {
        if (value.isEmpty()) {
            filteredOptions = options
        } else {
            delay(debounceTime)

            filteredOptions = options.filter {
                itemLabel(it).contains(value, ignoreCase = true)
            }
        }
    }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    if (enabled) {
                        onValueChange(it)
                        expanded = true
                    }
                },
                enabled = enabled,
                readOnly = false,
                label = { Text(buildAnnotatedString {
                    append(label)
                    if (asteriskAtEnd) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                            append("*")
                        }
                    }
                }) },
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                isError = isErrorState,
                supportingText = {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                shape = RoundedCornerShape(rounded),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = borderColor,
                    focusedBorderColor = borderColor,
                    unfocusedContainerColor = bgColor,
                    focusedContainerColor = bgColor,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    errorSupportingTextColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = enabled)
                    .fillMaxWidth()
            )

            if (filteredOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(bgColor)
                ) {
                    filteredOptions.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = itemLabel(item),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onOptionSelected(item)
//                                onValueChange(itemLabel(item))
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}