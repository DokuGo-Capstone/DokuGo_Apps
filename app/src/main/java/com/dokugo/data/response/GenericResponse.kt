package com.dokugo.data.response

data class GenericResponse(
    val message: String,
    val error: Boolean,
    val resetToken: String? = null
)