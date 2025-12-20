package com.dishut_lampung.sitanihut.domain.model

data class Penyuluh(
    val id: String,
    val name: String,
    val identityNumber: String,
    val position: String,
    val gender: String,
    val kphId: String,
    val kphName: String,
    val whatsAppNumber: String? = null
)