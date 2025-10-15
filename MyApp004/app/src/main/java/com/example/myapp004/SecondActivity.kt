package com.example.myapp004

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_second)
        val twInfo = findViewById<TextView>(id = R.id.twInfo)

        val nickname = intent.getStringExtra(name = "NICK_NAME")
        twInfo = "Data z první aktivity. Přezdívk $nickname"

        val btnClose = findViewById<Button>(id = R.id.btnClose)
        btnClose.setOnClickListener {
            finish()
        }
    }
}