package com.example.vanocni_appka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vanocni_appka.data.AppDatabase
import com.example.vanocni_appka.data.Gift
import com.example.vanocni_appka.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: GiftAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupCountdown()
        observeGifts()

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddGiftActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = GiftAdapter(
            onEdit = { gift ->
                val intent = Intent(this, AddGiftActivity::class.java)
                intent.putExtra("GIFT_ID", gift.id)
                startActivity(intent)
            },
            onDelete = { gift ->
                deleteGift(gift)
            }
        )
        binding.rvGifts.layoutManager = LinearLayoutManager(this)
        binding.rvGifts.adapter = adapter
    }

    private fun deleteGift(gift: Gift) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MainActivity)
            db.giftDao().deleteGift(gift)
            
            Snackbar.make(binding.root, "Dárek '${gift.name}' byl smazán", Snackbar.LENGTH_LONG)
                .setAction("ZPĚT") {
                    lifecycleScope.launch {
                        db.giftDao().insertGift(gift)
                    }
                }.show()
        }
    }

    private fun observeGifts() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.giftDao().getAllGifts().collectLatest { gifts ->
                adapter.submitList(gifts)
            }
        }
    }

    private fun setupCountdown() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        val christmas = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 24)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }

        if (calendar.after(christmas)) {
            christmas.set(Calendar.YEAR, currentYear + 1)
        }

        val diff = christmas.timeInMillis - calendar.timeInMillis
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        binding.tvCountdown.text = if (days == 0L) "Štědrý den je DNES!" else "$days dní"
    }
}
