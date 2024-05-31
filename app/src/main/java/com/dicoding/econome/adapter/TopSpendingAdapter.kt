package com.dicoding.econome.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.econome.R
import com.dicoding.econome.model.TopSpending

class TopSpendingAdapter(private val topSpendings: List<TopSpending>) :
    RecyclerView.Adapter<TopSpendingAdapter.TopSpendingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopSpendingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_spending, parent, false)
        return TopSpendingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopSpendingViewHolder, position: Int) {
        val topSpending = topSpendings[position]
        holder.bind(topSpending)
    }

    override fun getItemCount(): Int = topSpendings.size

    inner class TopSpendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val itemCount: TextView = itemView.findViewById(R.id.item_count)
        private val totalExpense: TextView = itemView.findViewById(R.id.total_expense)

        fun bind(topSpending: TopSpending) {
            icon.setImageResource(topSpending.iconRes)
            category.text = topSpending.category
            itemCount.text = itemView.context.getString(R.string.item_count, topSpending.itemCount)
            totalExpense.text = itemView.context.getString(R.string.total_expense, topSpending.totalExpense)
        }
    }
}