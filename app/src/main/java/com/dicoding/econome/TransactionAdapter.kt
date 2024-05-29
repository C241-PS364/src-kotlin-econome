package com.dicoding.econome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

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
        }else{
            holder.amount.text = "- Rp%.0f".format(Math.abs(transaction.amount))
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

        holder.label.text = transaction.label

        val categoryIconRes = when (transaction.category) {
            "Entertainment" -> R.drawable.ic_entertainment
            "Food" -> R.drawable.ic_food
            "Health and Beauty" -> R.drawable.ic_health
            "Housing" -> R.drawable.ic_housing
            "Other" -> R.drawable.ic_other
            "Transportation" -> R.drawable.ic_transportation
            else -> R.drawable.ic_other
        }
        holder.categoryIcon.setImageResource(categoryIconRes)
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