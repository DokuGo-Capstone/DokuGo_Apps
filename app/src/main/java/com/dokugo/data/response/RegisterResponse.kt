package com.dokugo.data.response

data class RegisterResponse(
	val message: String,
	val token: String,
	val error: Boolean
)