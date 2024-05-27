package com.dicoding.econome

import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.dicoding.econome.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.amountInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        binding.labelInput.doOnTextChanged { text, _, _, _ ->
            if (text != null && text.count() > 0)
                binding.labelLayout.error = null
        }

        binding.amountInput.doOnTextChanged { text, _, _, _ ->
            if (text != null && text.count() > 0)
                binding.amountLayout.error = null
        }

        val categories = arrayOf("Entertainment", "Food", "Health and Beauty", "Housing", "Investment", "Miscellaneous", "Transportation")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.categoryInput.setAdapter(adapter)

        binding.addTransactionButton.setOnClickListener {
            val label = binding.labelInput.text.toString()
            var amount = binding.amountInput.text.toString().toDoubleOrNull()
            val category = binding.categoryInput.text.toString()
            if (category.isEmpty())
                Toast.makeText(this,"Category cannot be empty", Toast.LENGTH_SHORT).show()
            else if (!categories.contains(category))
                Toast.makeText(this,"Please select a valid category", Toast.LENGTH_SHORT).show()
            else if (amount == null)
                Toast.makeText(this,"Please enter a valid amount", Toast.LENGTH_SHORT).show()
            else {
                amount = -Math.abs(amount!!)
                val transaction = Transaction(0,label,amount, category)
                insert(transaction)
            }
        }
        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    private fun insert(transaction: Transaction) {
        val db = AppDatabase.getDatabase(this)

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}