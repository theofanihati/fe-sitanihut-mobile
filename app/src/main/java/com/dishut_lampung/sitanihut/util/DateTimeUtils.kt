package com.dishut_lampung.sitanihut.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

fun changeDateFormat(inputDate: String): String {
    if (inputDate.isBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val date = inputFormat.parse(inputDate)
        outputFormat.format(date ?: return inputDate)
    } catch (e: Exception) {
        inputDate
    }
}

fun String.parseIndonesianNumber(): Double {
    if (this.isBlank()) return 0.0
    val cleanString = this.replace(".", "").replace(",", ".")
    return cleanString.toDoubleOrNull() ?: 0.0
}

fun formatRupiah(amount: Double): String {
    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    numberFormat.maximumFractionDigits = 0
    return numberFormat.format(amount)
}

fun String.formatApiToUiString(): String {
    val doubleVal = this.toDoubleOrNull() ?: return this
    return java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(doubleVal)
}

fun convertUiDateToApiDate(uiDate: String): String {
    return try {
        if (uiDate.contains("/")) {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(uiDate)
            outputFormat.format(date ?: return uiDate)
        } else {
            uiDate
        }
    } catch (e: Exception) {
        uiDate
    }
}