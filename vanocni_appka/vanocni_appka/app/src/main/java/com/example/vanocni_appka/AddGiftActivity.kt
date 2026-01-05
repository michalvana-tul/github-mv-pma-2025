package com.example.vanocni_appka

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vanocni_appka.data.AppDatabase
import com.example.vanocni_appka.data.Gift
import com.example.vanocni_appka.databinding.ActivityAddGiftBinding
import kotlinx.coroutines.launch

class AddGiftActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddGiftBinding
    private var selectedImageUri: Uri? = null
    private var editGiftId: Int = -1

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedImageUri = it
            binding.ivPreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editGiftId = intent.getIntExtra("GIFT_ID", -1)
        if (editGiftId != -1) {
            loadGiftData()
            binding.tvHeader.text = "Upravit dárek"
        }

        binding.btnPickImage.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        binding.btnSave.setOnClickListener {
            saveGift()
        }
    }

    private fun loadGiftData() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddGiftActivity)
            db.giftDao().getGiftById(editGiftId)?.let { gift ->
                binding.etName.setText(gift.name)
                binding.etPerson.setText(gift.person)
                binding.etDescription.setText(gift.description)
                binding.cbBought.isChecked = gift.isBought
                gift.imageUri?.let {
                    selectedImageUri = Uri.parse(it)
                    binding.ivPreview.setImageURI(selectedImageUri)
                }
            }
        }
    }

    private fun saveGift() {
        val name = binding.etName.text.toString()
        val person = binding.etPerson.text.toString()
        val desc = binding.etDescription.text.toString()
        val isBought = binding.cbBought.isChecked

        if (name.isBlank() || person.isBlank()) {
            Toast.makeText(this, "Vyplňte prosím název a jméno", Toast.LENGTH_SHORT).show()
            return
        }

        val gift = Gift(
            id = if (editGiftId != -1) editGiftId else 0,
            name = name,
            person = person,
            description = desc,
            isBought = isBought,
            imageUri = selectedImageUri?.toString()
        )

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddGiftActivity)
            if (editGiftId != -1) {
                db.giftDao().updateGift(gift)
            } else {
                db.giftDao().insertGift(gift)
            }
            Toast.makeText(this@AddGiftActivity, "Uloženo!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
