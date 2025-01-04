package com.example.booklibrary.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booklibrary.data.models.*
import com.example.booklibrary.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _selectedBook = MutableStateFlow<BookWithLoan?>(null)
    val selectedBook: StateFlow<BookWithLoan?> = _selectedBook.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _books.value = repository.getAllBooks()
            } catch (e: Exception) {
                _error.value = "Failed to load books: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBookById(_id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bookWithLoan = repository.getBook(_id)
                _selectedBook.value = bookWithLoan // Assuming BookWithLoan has a 'book' property
                println("Load book: $bookWithLoan")
            } catch (e: Exception) {
                _error.value = "Failed to load book: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Log the book object
                println("Saving book: $book")

                if (book.id.isEmpty()) {
                    repository.createBook(book)
                } else {
                    repository.updateBook(book.id, book)
                }
                loadBooks()  // Refresh the list
            } catch (e: Exception) {
                _error.value = "Failed to save book: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun clearError() {
        _error.value = null
    }
}