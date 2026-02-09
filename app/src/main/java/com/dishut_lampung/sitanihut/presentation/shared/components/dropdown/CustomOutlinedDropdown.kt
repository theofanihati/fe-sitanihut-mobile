package com.dishut_lampung.sitanihut.presentation.shared.components.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedDropdown(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Pilih",
    options: List<String>,
    asteriskAtEnd: Boolean = false,
    error: String? = null,
    rounded: Int = 40,
    isEnabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    bgColor: Color = MaterialTheme.colorScheme.background,
    focusedBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    focusedLabelColor: Color = MaterialTheme.colorScheme.tertiary,
    labelDefaultColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (isEnabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = isEnabled,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(rounded),
                textStyle = textStyle,
                label = {
                    Text(buildAnnotatedString {
                        append(label)
                        if (asteriskAtEnd) {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                                append("*")
                            }
                        }
                    })
                },
                placeholder = {
                    Text(placeholder)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                supportingText = {
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                isError = error != null,
                colors = getDropdownColors(
                    borderColor = borderColor,
                    borderFocusedColor = focusedBorderColor,
                    bgColor = bgColor,
                    labelFocusedColor = focusedLabelColor,
                    labelDefaultColor = labelDefaultColor,
                    isError = error != null
                ),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(bgColor)
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = selectionOption,
                                style = textStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onValueChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun getDropdownColors(
    borderColor: Color,
    borderFocusedColor: Color,
    bgColor: Color,
    labelFocusedColor: Color,
    labelDefaultColor: Color,
    isError: Boolean = false
): TextFieldColors {
    val focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else labelFocusedColor

    return OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = borderColor,
        focusedBorderColor = borderFocusedColor,
        disabledBorderColor = borderColor.copy(alpha = 0.5f),
        unfocusedContainerColor = bgColor,
        focusedContainerColor = bgColor,
        disabledContainerColor = bgColor.copy(alpha = 0.5f),
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = labelDefaultColor,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorLeadingIconColor = MaterialTheme.colorScheme.error,
        errorTrailingIconColor = MaterialTheme.colorScheme.error
    )
}