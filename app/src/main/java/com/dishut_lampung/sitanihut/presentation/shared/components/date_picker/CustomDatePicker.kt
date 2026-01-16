package com.dishut_lampung.sitanihut.presentation.shared.components.date_picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    modifier: Modifier = Modifier,
    value: String, // Format: yyyy-MM-dd
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Pilih Tanggal",
    asteriskAtEnd: Boolean = false,
    error: String? = null,
    rounded: Int = 40,
    isEnabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    bgColor: Color = Color.White,
    focusedBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    focusedLabelColor: Color = MaterialTheme.colorScheme.tertiary,
    labelDefaultColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var showDialog by remember { mutableStateOf(false) }
    val initialMillis = remember(value) {
        if (value.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.parse(value)?.time
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(rounded.dp),
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
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    IconButton(onClick = { if (isEnabled) showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pilih Tanggal",
                            tint = if (error != null) MaterialTheme.colorScheme.error else borderColor
                        )
                    }
                },
                supportingText = if (error != null) {
                    {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else null,
                isError = error != null,
                colors = getDatePickerColors(
                    borderColor = borderColor,
                    borderFocusedColor = focusedBorderColor,
                    bgColor = bgColor,
                    labelFocusedColor = focusedLabelColor,
                    labelDefaultColor = labelDefaultColor,
                    isError = error != null
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(rounded.dp))
                    .clickable(enabled = isEnabled) { showDialog = true }
            )
        }

        if (showDialog) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            datePickerState.selectedDateMillis?.let { millis ->
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
                                sdf.timeZone = TimeZone.getTimeZone("UTC")
                                val formattedDate = sdf.format(Date(millis))
                                onValueChange(formattedDate)
                            }
                        },
                        enabled = confirmEnabled.value
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Batal")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun getDatePickerColors(
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