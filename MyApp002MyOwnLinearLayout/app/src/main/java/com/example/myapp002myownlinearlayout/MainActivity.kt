package com.example.myapp002myownlinearlayout

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp002myownlinearlayout.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonAdd.setOnClickListener { calculate('+') }
        binding.buttonSubtract.setOnClickListener { calculate('-') }
        binding.buttonMultiply.setOnClickListener { calculate('*') }
        binding.buttonDivide.setOnClickListener { calculate('/') }
    }

    private fun calculate(operator: Char) {
        val number1String = binding.number1Input.text.toString()
        val number2String = binding.number2Input.text.toString()

        if (number1String.isNotEmpty() && number2String.isNotEmpty()) {
            try {
                val number1 = number1String.toDouble()
                val number2 = number2String.toDouble()
                var result: Double? = null

                when (operator) {
                    '+' -> result = number1 + number2
                    '-' -> result = number1 - number2
                    '*' -> result = number1 * number2
                    '/' -> {
                        if (number2 != 0.0) {
                            result = number1 / number2
                        } else {
                            Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show()
                            binding.resultText.text = "Error: Division by zero"
                        }
                    }
                }

                result?.let {
                    if (it % 1.0 == 0.0) {
                        binding.resultText.text = "Result: ${it.toLong()}"
                    } else {
                        binding.resultText.text = "Result: %.2f".format(it)
                    }
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show()
                binding.resultText.text = "Error: Invalid input"
            }
        } else {
            Toast.makeText(this, "Please enter both numbers", Toast.LENGTH_SHORT).show()
            binding.resultText.text = "Please enter both numbers"
        }
    }
}
