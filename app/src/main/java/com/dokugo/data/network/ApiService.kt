package com.dokugo.data.network

import com.dokugo.data.response.ExpenseRequest
import com.dokugo.data.response.ExpenseResponse
import com.dokugo.data.response.GenericResponse
import com.dokugo.data.response.LoginResponse
import com.dokugo.data.response.ProfileResponse
import com.dokugo.data.response.RegisterResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    suspend fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @FormUrlEncoded
    @POST("register")
    suspend fun registerUser(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("phone_number") phoneNumber: String
    ): RegisterResponse

    @GET("profile")
    suspend fun getProfile(@Header("Authorization") token: String): ProfileResponse

    @PUT("profile/edit")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body profile: Map<String, String>
    ): GenericResponse

    @DELETE("profile/delete")
    suspend fun deleteProfile(@Header("Authorization") token: String): GenericResponse

    @PATCH("profile/photo")
    suspend fun updateProfilePhotoUrl(
        @Header("Authorization") token: String,
        @Body profile: Map<String, String>
    ): GenericResponse

    @POST("forgotPassword")
    suspend fun sendOtp(@Body email: Map<String, String>): GenericResponse

    @POST("verify-otp")
    suspend fun verifyOtp(@Body otpData: Map<String, String>): GenericResponse

    @POST("resetPassword")
    suspend fun resetPassword(@Body resetData: Map<String, String>): GenericResponse
}