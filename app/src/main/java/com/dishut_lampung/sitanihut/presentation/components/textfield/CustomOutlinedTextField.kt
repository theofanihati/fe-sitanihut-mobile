package com.dishut_lampung.sitanihut.presentation.components.textfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Suppress("t")
@Composable
fun CustomOutlinedTextField(
    modifier : Modifier = Modifier,
    asteriskAtEnd : Boolean = false,
    value : String,
    onValueChange : (String) -> Unit,
    label : String? = "",
    placeholder : String? = "isi data",
    isPassword : Boolean = false,
    isPasswordVisible : Boolean = false,
    onPasswordToggleClick: () -> Unit = {},
    leadingIcon : @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType : KeyboardType = KeyboardType.Text,
    error : String? = null,
    rounded : Int = 40,
    multiLine : Boolean = false,
    maxLines : Int = 1,
    isEnabled : Boolean = true,
    readOnly : Boolean = false,
    isFocused : Boolean? = null,
    onFocusChange : (Boolean) -> Unit = {},
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    borderColor : Color = MaterialTheme.colorScheme.tertiary,
    bgColor : Color = Color.White,
    focusedBorderColor : Color = MaterialTheme.colorScheme.tertiary,
    focusedLabelColor : Color = MaterialTheme.colorScheme.tertiary,
    labelDefaultColor : Color = MaterialTheme.colorScheme.onSurface,
    cursorColor : Color = MaterialTheme.colorScheme.tertiary,

) {

    val focusRequester = remember { FocusRequester() }
    var isFocusedDefault by remember { mutableStateOf(false) }

    val actualIsFocus = isFocused ?: isFocusedDefault

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused.let {
                        if (it != null) {
                            onFocusChange(focusState.isFocused)
                        } else {
                            isFocusedDefault = focusState.isFocused
                        }
                    }
                },
            singleLine = ! multiLine,
            maxLines = if (multiLine) maxLines else 1,
            shape = RoundedCornerShape(rounded),
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (isPassword) {
                    PasswordVisibilityIcon(
                        isVisible = isPasswordVisible,
                        onClick = onPasswordToggleClick
                    )
                } else {
                    trailingIcon?.invoke()
                }
            },
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            label = {
                if (label != null) {
                    Text(buildAnnotatedString {
                        append(label)
                        if (asteriskAtEnd) {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) { // Anda bisa ganti warnanya jika mau
                                append("*")
                            }
                        }
                    })
                }
            },
            placeholder = {
                if (placeholder != null) {
                    if (placeholder != null) {
                        Text(placeholder)
                    }
                }
            },
            textStyle = textStyle,
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
            colors = getTextFieldColors(
                borderColor = borderColor,
                borderFocusedColor = focusedBorderColor,
                bgColor = bgColor,
                labelFocusedColor = focusedLabelColor,
                labelDefaultColor = labelDefaultColor,
                isError = error != null
            ),
            enabled = isEnabled,
            readOnly = readOnly,
            )

//        AnimatedVisibility(visible = error != null) {
//            error?.let {
//                ErrorMessageTextField(it)
//            }
//        }
    }
}

@Composable
private fun PasswordVisibilityIcon(
    isVisible : Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = if (isVisible) "Sembunyikan Password" else "Tampilkan Password"
        )
    }
}

@Composable
private fun getTextFieldColors(
    borderColor : Color,
    borderFocusedColor : Color,
    bgColor : Color,
    labelFocusedColor : Color,
    labelDefaultColor : Color,
    isError : Boolean = false
) : TextFieldColors {

    val focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else labelFocusedColor

    return OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = borderColor,
        focusedBorderColor = borderFocusedColor,
        disabledBorderColor = borderColor,
        unfocusedContainerColor = bgColor,
        focusedContainerColor = bgColor,
        disabledContainerColor = bgColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = labelDefaultColor,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorLeadingIconColor = MaterialTheme.colorScheme.error,
        errorTrailingIconColor = MaterialTheme.colorScheme.error
    )
}