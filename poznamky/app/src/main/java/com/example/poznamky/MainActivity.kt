package com.example.poznamky

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.poznamky.data.Note
import com.example.poznamky.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as NotesApplication).repository)
    }

    private val addNoteActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val name = data.getStringExtra(AddNoteActivity.EXTRA_NAME)
                val description = data.getStringExtra(AddNoteActivity.EXTRA_DESCRIPTION)
                if (name != null && description != null) {
                    noteViewModel.insert(Note(name = name, description = description))
                }
            }
        } else {
            Toast.makeText(
                applicationContext,
                "Note not saved because it was empty.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = NoteListAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            noteViewModel.allNotes.collect { notes ->
                notes?.let { adapter.submitList(it) }
            }
        }

        binding.fabAddNote.setOnClickListener {
            val intent = Intent(this@MainActivity, AddNoteActivity::class.java)
            addNoteActivityResultLauncher.launch(intent)
        }
    }
}