package com.dokugo.data.response

data class ExpenseGetResponse(
    val transaction_id: Int,
    val id: String,
    val amount: Double,
    val category: Int,
    val date: String,
    val notes: String,
    val created_at: String,
    val updated_at: String
)