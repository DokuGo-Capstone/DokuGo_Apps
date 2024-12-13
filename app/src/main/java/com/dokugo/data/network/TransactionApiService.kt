package com.dokugo.data.network

import com.dokugo.data.response.ExpenseGetResponse
import com.dokugo.data.response.ExpenseRequest
import com.dokugo.data.response.ExpenseResponse
import com.dokugo.data.response.IncomeRequest
import com.dokugo.data.response.IncomeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TransactionApiService {
    @POST("/transactions/expenses")
    suspend fun postExpense(
        @Header("Authorization") token: String,
        @Body expense: ExpenseRequest
    ): ExpenseResponse

    @GET("/transactions/expenses")
    suspend fun getExpenses(
        @Header("Authorization") token: String
    ): List<ExpenseGetResponse>

    @POST("/transactions/income")
    suspend fun postIncome(
        @Header("Authorization") token: String,
        @Body incomeRequest: IncomeRequest
    ): IncomeResponse
}