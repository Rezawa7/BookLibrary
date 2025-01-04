package com.example.booklibrary.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booklibrary.data.models.Book
import com.example.booklibrary.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()


    init {
        loadBooks()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun executeSearch() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _books.value = repository.searchBooks(_searchQuery.value)
            } catch (e: Exception) {
                _error.value = "Failed to search books: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _books.value = repository.getAllBooks()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.deleteBook(id)
                loadBooks()  // Refresh the list
            } catch (e: Exception) {
                _error.value = "Failed to delete book: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    fun clearError() {
        _error.value = null
    }
}
