package com.dishut_lampung.sitanihut.domain.model

data class Kth (
    val id : String,
    val name : String,
    val desa : String,
    val kecamatan: String? = null,
    val kabupaten : String,
    val coordinator: String? = null,
    val whatsappNumber: String? = null,
    val kphName : String,
)