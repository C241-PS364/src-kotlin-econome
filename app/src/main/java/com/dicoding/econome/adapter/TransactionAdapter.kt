package com.dicoding.econome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.econome.R
import com.dicoding.econome.database.entity.Transaction

class TransactionAdapter(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.label)
        val amount: TextView = view.findViewById(R.id.amount)
        val categoryIcon: ImageView = view.findViewById(R.id.category_icon)
        val date: TextView = view.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.amount.context

        if(transaction.amount >= 0){
            holder.amount.text = "+ Rp%.0f".format(transaction.amount)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green))
            holder.categoryIcon.setImageResource(R.drawable.ic_income_new) // Set icon to ic_money for income
        }else{
            holder.amount.text = "- Rp%.0f".format(Math.abs(transaction.amount))
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))

            val categoryIconRes = when (transaction.category) {
                "Entertainment" -> R.drawable.ic_entertainment_new
                "Food" -> R.drawable.ic_food_new
                "Health" -> R.drawable.ic_health
                "Housing" -> R.drawable.ic_housing_new
                "Other" -> R.drawable.ic_other_new
                "Transportation" -> R.drawable.ic_transportation_new
                else -> R.drawable.ic_other_new
            }
            holder.categoryIcon.setImageResource(categoryIconRes)
        }

        holder.label.text = transaction.label
        holder.date.text = transaction.date
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    fun setData(transactions: List<Transaction>){
        this.transactions = transactions
        notifyDataSetChanged()
    }
}