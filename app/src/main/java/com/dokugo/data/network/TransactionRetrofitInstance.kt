// TransactionRetrofitInstance.kt
package com.dokugo.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TransactionRetrofitInstance {
    private const val BASE_URL = "https://dokugo-transaction-715591776189.asia-southeast2.run.app/"

    val api: TransactionApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(TransactionApiService::class.java)
    }
}