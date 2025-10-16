package com.example.myapp007afragments

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapp007afragments.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFragment1.setOnClickListener {
            replaceFragment(Fragment1())
            binding.btnFragment1.setBackgroundColor(Color.BLUE)
            binding.btnFragment2.setBackgroundColor(Color.GRAY)
            binding.btnFragment3.setBackgroundColor(Color.GRAY)
        }

        binding.btnFragment2.setOnClickListener {
            replaceFragment(Fragment2())
            binding.btnFragment2.setBackgroundColor(Color.BLUE)
            binding.btnFragment1.setBackgroundColor(Color.GRAY)
            binding.btnFragment3.setBackgroundColor(Color.GRAY)
        }

        binding.btnFragment3.setOnClickListener {
            replaceFragment(Fragment3())
            binding.btnFragment3.setBackgroundColor(Color.BLUE)
            binding.btnFragment1.setBackgroundColor(Color.GRAY)
            binding.btnFragment2.setBackgroundColor(Color.GRAY)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}