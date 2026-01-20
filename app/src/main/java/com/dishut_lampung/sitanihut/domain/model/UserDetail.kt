package com.dishut_lampung.sitanihut.domain.model

data class UserDetail(
    val id: String,
    val email: String? = null,
    val roleId: String? = null,
    val role: String,
    val kphId: String? = null,
    val kphName: String? = null,
    val kthId: String? = null,
    val kthName: String? = null,
    val name: String,
    val identityNumber: String? = null,
    val gender: String,
    val profilePictureUrl: String? = null,
    val address: String? = null,
    val whatsAppNumber: String? = null,
    val lastEducation: String? = null,
    val sideJob: String? = null,
    val landArea: Double? = null,
    val position: String? = null
)

data class CreateUserInput(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val roleId: String = "",
    val role: String = "",
    val name: String = "",
    val identityNumber: String = "",
    val gender: String = "",
    val address: String= "",
    val whatsAppNumber: String = "",

    val lastEducation: String = "",
    val sideJob: String = "",
    val landArea: String = "",

//    val position: String = "",

    val kphId: String = "",
    val kphName: String = "",
    val kthId: String = "",
    val kthName: String = "",
    val isEditMode: Boolean = false,
)