package com.dishut_lampung.sitanihut.domain.model

data class Petani(
    val id: String,
    val name: String,
    val identityNumber: String,
    val gender: String? = "",
    val address: String? = "",
    val whatsAppNumber: String? = "",
    val lastEducation: String? = "",
    val sideJob: String? = "",
    val landArea: Double? = null,
    val kphId: String? = "",
    val kphName: String,
    val kthId: String? = "",
    val kthName: String,
)

data class CreatePetaniInput(
    val name: String = "",
    val identityNumber: String = "",
    val gender: String = "",
    val address: String = "",
    val whatsAppNumber: String = "",
    val lastEducation: String = "",
    val sideJob: String = "",
    val landArea: String = "",
    val kphId: String = "",
    val kthId: String = "",
)
