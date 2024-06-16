package com.dicoding.econome.expense

import com.google.gson.annotations.SerializedName

object ExpenseRequests {
    data class AddExpenseRequest(
        @SerializedName("date") val date: String,
        @SerializedName("title") val title: String,
        @SerializedName("category_id") val categoryId: Int,
        @SerializedName("amount") val amount: Int
    )
}