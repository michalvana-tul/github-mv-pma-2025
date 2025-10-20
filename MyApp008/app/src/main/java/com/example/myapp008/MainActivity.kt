package com.example.myapp008

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.content.Context
import androidx.core.view.WindowInsetsCompat
import com.example.myapp008.databinding.ActivityMainBinding
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val ageString = binding.etAge.text.toString().trim()

            if (ageString.isBlank()) {
                Toast.makeText(this, "Zadej věk", Toast.LENGTH_LONG).show()
            } else {
                val age = ageString.toInt()
                val isAdult = binding.cbAdult.isChecked

                if ((age < 18 && isAdult) || (age >= 18 && !isAdult)) {
                    Toast.makeText(this, "Věk a status dospělosti si neodpovídají.", Toast.LENGTH_LONG).show()
                } else {
                    editor.apply {
                        putString("name", name)
                        putInt("age", age)
                        putBoolean("isAdult", isAdult)
                        apply()
                    }
                    Toast.makeText(this, "Data uložena.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnLoad.setOnClickListener { 
            val name = sharedPref.getString("name", "")
            val age = sharedPref.getInt("age", 0)
            val isAdult = sharedPref.getBoolean("isAdult", false)

            binding.etName.setText(name)
            binding.etAge.setText(age.toString())
            binding.cbAdult.isChecked = isAdult

            binding.tvLoadedData.text = "Jméno: $name\nVěk: $age\nDospělý: $isAdult"
        }
    } 
}
