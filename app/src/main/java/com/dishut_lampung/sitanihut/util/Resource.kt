package com.dishut_lampung.sitanihut.util

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    data class Success<T>(val successData: T) : Resource<T>(data = successData)
    data class Error<T>(val errorMessage: String, val errorData: T? = null) : Resource<T>(data = errorData, message = errorMessage)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}