package com.example.texttoimage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.texttoimage.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject

data class Flashcard(val imageUri: Uri, val description: String)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val flashcards = mutableListOf<Flashcard>()
    private var currentCardIndex = 0
    private var isFlipped = false

    private var tempImageUri: Uri? = null
    private var dialogImageView: ImageView? = null

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, flag)
                    tempImageUri = uri
                    dialogImageView?.setImageURI(uri)
                } catch (e: SecurityException) {
                    Toast.makeText(this, "Nelze získat oprávnění k obrázku.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCards()
        updateUI()

        binding.fabAddCard.setOnClickListener {
            showAddCardDialog()
        }

        binding.cardContainer.setOnClickListener {
            isFlipped = !isFlipped
            updateCardView()
        }

        binding.btnNext.setOnClickListener {
            if (flashcards.isNotEmpty()) {
                currentCardIndex = (currentCardIndex + 1) % flashcards.size
                isFlipped = false
                updateUI()
            }
        }

        binding.btnPrev.setOnClickListener {
            if (flashcards.isNotEmpty()) {
                currentCardIndex = (currentCardIndex - 1 + flashcards.size) % flashcards.size
                isFlipped = false
                updateUI()
            }
        }
    }

    private fun updateUI() {
        if (flashcards.isEmpty()) {
            binding.tvCardCounter.text = "0 / 0"
            binding.cardContainer.visibility = View.GONE
        } else {
            binding.cardContainer.visibility = View.VISIBLE
            binding.tvCardCounter.text = "${currentCardIndex + 1} / ${flashcards.size}"
            updateCardView()
        }
    }

    private fun updateCardView() {
        if (flashcards.isEmpty()) return

        val card = flashcards[currentCardIndex]
        if (isFlipped) {
            binding.ivCardFront.visibility = View.GONE
            binding.tvCardBack.visibility = View.VISIBLE
            binding.tvCardBack.text = card.description
        } else {
            binding.ivCardFront.visibility = View.VISIBLE
            binding.tvCardBack.visibility = View.GONE
            try {
                binding.ivCardFront.setImageURI(card.imageUri)
            } catch (e: SecurityException) {
                binding.ivCardFront.setImageResource(android.R.drawable.stat_notify_error)
                Toast.makeText(this, "Chyba: Oprávnění k obrázku bylo ztraceno.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddCardDialog() {
        tempImageUri = null
        dialogImageView = null

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_card, null)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescriptionDialog)
        dialogImageView = dialogView.findViewById(R.id.ivPreviewDialog)

        dialogView.findViewById<Button>(R.id.btnPickImageDialog).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle("Přidat novou kartu")
            .setView(dialogView)
            .setPositiveButton("Uložit") { _, _ ->
                val description = etDescription.text.toString().trim()
                if (description.isNotEmpty() && tempImageUri != null) {
                    flashcards.add(Flashcard(tempImageUri!!, description))
                    saveCards()
                    currentCardIndex = flashcards.size - 1
                    isFlipped = false
                    updateUI()
                } else {
                    Toast.makeText(this, "Vyber obrázek a zadej popisek.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Zrušit", null)
            .setOnDismissListener {
                dialogImageView = null
                tempImageUri = null
            }
            .show()
    }

    private fun saveCards() {
        val jsonArray = JSONArray()
        flashcards.forEach { card ->
            val jsonObject = JSONObject()
            jsonObject.put("imageUri", card.imageUri.toString())
            jsonObject.put("description", card.description)
            jsonArray.put(jsonObject)
        }
        val sharedPrefs = getSharedPreferences("flashcards_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("cards", jsonArray.toString()).apply()
    }

    private fun loadCards() {
        val sharedPrefs = getSharedPreferences("flashcards_prefs", Context.MODE_PRIVATE)
        val jsonString = sharedPrefs.getString("cards", null)
        if (jsonString != null) {
            flashcards.clear()
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                try {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val uri = Uri.parse(jsonObject.getString("imageUri"))
                    val description = jsonObject.getString("description")
                    
                    val hasPermission = contentResolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
                    if(hasPermission) {
                         flashcards.add(Flashcard(uri, description))
                    }
                } catch(e: Exception) {
                    // Ignore cards that fail to load
                }
            }
        }
    }
}