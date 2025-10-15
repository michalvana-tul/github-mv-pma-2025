package com.example.myapp004

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_main)


        val btnSecondAct = findViewById<Button>(id = R.id.btnSecondAct)
        val etNickname = findViewById<EditText>(id = R.id.etNickname)
        btnSecondAct.setOnClickListener {
            val nickname = etNickname.text.toString()
            val intent = Intent(this, btnSecondAct::class.java)
            intent.putExtra("NICK_NAME", value = nickname)
            startActivity(intent)
        }
    }
}