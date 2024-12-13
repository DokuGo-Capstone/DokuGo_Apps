package com.dokugo.data.response

data class IncomeRequest(
    val id: String,
    val amount: Double,
    val category: Int,
    val date: String,
    val notes: String
)

data class IncomeResponse(
    val success: Boolean,
    val message: String
)