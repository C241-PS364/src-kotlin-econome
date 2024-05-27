package com.dicoding.econome

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.room.Room
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

        binding.addTransactionButton.setOnClickListener {
            val label = binding.labelInput.text.toString()
            var amount = binding.amountInput.text.toString().toDoubleOrNull()
            if (label.isEmpty())
                binding.labelLayout.error = "Label cannot be empty"
            else if (amount == null)
                binding.amountLayout.error = "Please enter a valid amount"
            else{
                amount = -Math.abs(amount)
                val transaction = Transaction(0,label,amount)
                insert(transaction)
            }
        }
        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    private fun insert(transaction: Transaction) {
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions")
            .build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}