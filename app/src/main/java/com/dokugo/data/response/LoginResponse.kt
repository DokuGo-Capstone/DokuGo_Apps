package com.dokugo.data.response

data class LoginResponse(
	val message: String,
	val token: String,
	val error: Boolean
)