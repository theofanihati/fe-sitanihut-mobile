package com.dishut_lampung.sitanihut.domain.model

data class UserDetail(
    val id: String,
    val email: String,
    val roleId: String,
    val role: String? = "",
    val kphId: String? = "",
    val kphName: String? = "",
    val kthId: String? = "",
    val kthName: String? = "",
    val name: String,
    val identityNumber: String,
    val gender: String,
    val profilePictureUrl: String? = "",
    val address: String? = "",
    val whatsAppNumber: String? = "",
    val lastEducation: String? = "",
    val sideJob: String? = "",
    val landArea: Double? = null,
    val position: String? = ""
)