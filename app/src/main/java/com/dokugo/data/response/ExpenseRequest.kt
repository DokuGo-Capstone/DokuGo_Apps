package com.dokugo.data.response

data class ExpenseRequest(
    val id: String,
    val amount: Double,
    val category: Int,
    val date: String,
    val notes: String
)

data class ExpenseResponse(
    val success: Boolean,
    val message: String
)