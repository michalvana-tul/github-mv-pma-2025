package com.example.semestralka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.semestralka.data.Book
import com.example.semestralka.data.BookDao
import com.example.semestralka.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel(
    private val bookDao: BookDao,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val allBooks: StateFlow<List<Book>> = bookDao.getAllBooks()
        .combine(preferencesRepository.hideReadBooksFlow) { books, hideRead ->
            if (hideRead) {
                books.filter { !it.isRead }
            } else {
                books
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertBook(book: Book) {
        viewModelScope.launch {
            bookDao.insertBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            bookDao.updateBook(book)
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            bookDao.deleteBook(book)
        }
    }

    suspend fun getBookById(id: Int): Book? {
        return bookDao.getBookById(id)
    }

    fun updateHideReadBooks(hide: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHideReadBooks(hide)
        }
    }
    
    val hideReadBooks = preferencesRepository.hideReadBooksFlow
}

class BookViewModelFactory(
    private val bookDao: BookDao,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(bookDao, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
