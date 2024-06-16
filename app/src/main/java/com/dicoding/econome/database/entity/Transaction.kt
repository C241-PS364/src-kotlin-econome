package com.dicoding.econome.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val label: String,
    val amount: Double,
    val category: String = "",
    val date: String,
    val incomeId: Int? = null,
    val expenseId: Int? = null
) {

}
