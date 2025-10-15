package com.example.myapp005

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapp005.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.io.root

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnShowToast.setOnClickListener {
            val toast = Toast.makeText(this, "Nazdar - MÁM HLAD", Toast.LENGTH_SHORT)
            toast.show()
        }

        binding.btnShowScnackbar.setOnClickListener {
            Snackbar.make(binding.root, "Jsem Svačinář", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.parseColor(colorString = #FFCC50))
            .s
        }

    }
}