package com.dishut_lampung.sitanihut.presentation.components.dropdown

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.dishut_lampung.sitanihut.domain.model.Commodity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchableOutlinedDropdown(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onOptionSelected: (Commodity) -> Unit,
    options: List<Commodity>,
    label: String,
    placeholder: String = "Pilih Komoditas",
    isError: Boolean = false,
    errorMessage: String? = null,
    rounded: Int = 40,
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    bgColor: Color = Color.White
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = options.filter {
        it.name.contains(value, ignoreCase = true)
    }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    expanded = true
                },
                readOnly = false,
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                isError = isError,
                supportingText = if (errorMessage != null) {
                    { Text(text = errorMessage, color = MaterialTheme.colorScheme.error) }
                } else null,
                shape = RoundedCornerShape(rounded),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = borderColor,
                    focusedBorderColor = borderColor,
                    unfocusedContainerColor = bgColor,
                    focusedContainerColor = bgColor
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            if (filteredOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(bgColor)
                ) {
                    filteredOptions.forEach { commodity ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = commodity.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onOptionSelected(commodity)
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