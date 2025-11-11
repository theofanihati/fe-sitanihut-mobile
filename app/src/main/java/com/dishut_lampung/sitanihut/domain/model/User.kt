package com.dishut_lampung.sitanihut.domain.model

data class User(
    val id: String,
    val name: String,
    val token: String,
    val role:String,
    val email:String,
)