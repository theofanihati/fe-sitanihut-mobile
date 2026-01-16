package com.dishut_lampung.sitanihut.presentation.shared.components.textfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CustomOutlinedTextArea(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Isi catatan...",
    error: String? = null,
    rounded: Int = 16,
    isEnabled: Boolean = true,
    minLines: Int = 3,
    maxLines: Int = 5,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    bgColor: Color = Color.White,
    focusedBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    focusedLabelColor: Color = MaterialTheme.colorScheme.tertiary,
    labelDefaultColor: Color = MaterialTheme.colorScheme.onSurface,
    cursorColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled,
            textStyle = textStyle,
            shape = RoundedCornerShape(rounded.dp),

            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,

            label = { Text(label) },
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),

            isError = error != null,
            supportingText = if (error != null) {
                {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else null,

            colors = getTextAreaColors(
                borderColor = borderColor,
                borderFocusedColor = focusedBorderColor,
                bgColor = bgColor,
                labelFocusedColor = focusedLabelColor,
                labelDefaultColor = labelDefaultColor,
                isError = error != null,
                cursorColor = cursorColor
            )
        )
    }
}

@Composable
private fun getTextAreaColors(
    borderColor: Color,
    borderFocusedColor: Color,
    bgColor: Color,
    labelFocusedColor: Color,
    labelDefaultColor: Color,
    cursorColor: Color,
    isError: Boolean = false
): TextFieldColors {
    val focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else labelFocusedColor

    return OutlinedTextFieldDefaults.colors(
        cursorColor = cursorColor,
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