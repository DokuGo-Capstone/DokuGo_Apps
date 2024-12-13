package com.dokugo.data.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    val user: User
)

data class User(
    val id: String,
    val username: String,
    val email: String,
    @SerializedName("avatarUrl") val avatarUrl: String,
    @SerializedName("phone_number") val phoneNumber: String?
)