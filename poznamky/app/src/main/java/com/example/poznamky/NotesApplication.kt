package com.example.poznamky

import android.app.Application
import com.example.poznamky.data.NoteDatabase
import com.example.poznamky.data.NoteRepository

class NotesApplication : Application() {
    // Using by lazy so the database and repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { NoteDatabase.getDatabase(this) }
    val repository by lazy { NoteRepository(database.noteDao()) }
}