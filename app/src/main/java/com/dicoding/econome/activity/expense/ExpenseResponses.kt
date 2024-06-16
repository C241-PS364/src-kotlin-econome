package com.dicoding.econome.activity.expense

import com.google.gson.annotations.SerializedName

object ExpenseResponses {
    data class ExpenseData(
        @SerializedName("id") val id: Int,
        @SerializedName("date") val date: String,
        @SerializedName("title") val title: String,
        @SerializedName("category_id") val categoryId: Int,
        @SerializedName("amount") val amount: Int
    )

    data class AddExpenseResponse(
        @SerializedName("error") val error: Boolean,
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: ExpenseData
    )

    data class DeleteExpenseResponse(
        @SerializedName("error") val error: Boolean,
        @SerializedName("message") val message: String
    )
}