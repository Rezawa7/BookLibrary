package com.example.booklibrary.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booklibrary.data.models.*
import com.example.booklibrary.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

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

    private val _loanCreationStatus = MutableStateFlow<LoanCreationStatus?>(null)
    val loanCreationStatus: StateFlow<LoanCreationStatus?> = _loanCreationStatus.asStateFlow()

    sealed class LoanCreationStatus {
        object Success : LoanCreationStatus()
        data class Error(val message: String) : LoanCreationStatus()
    }

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
                _selectedBook.value = bookWithLoan
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
                println("Saving book: $book")

                if (book.id.isEmpty()) {
                    repository.createBook(book)
                } else {
                    repository.updateBook(book.id, book)
                }
                loadBooks()
            } catch (e: Exception) {
                _error.value = "Failed to save book: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createLoanForBook(
        bookId: String,
        bookName:String,
        borrowerName: String,
        borrowerEmail: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val now = LocalDateTime.now()
                val dueDate = now.plusDays(30) // Set due date to 30 days from now

                val loanRequest = LoanRequest(
                    book_id = bookId,
                    book_name = bookName,
                    borrower_name = borrowerName,
                    borrower_email = borrowerEmail,
                    borrow_date = now.toString(),
                    return_date = dueDate.toString(),
                    status = "ACTIVE"
                )

                println("Complete loan request:")
                println(loanRequest.toString())

                val response = repository.createLoan(loanRequest)
                println("Loan creation successful: $response")

                loadBookById(bookId)
                _loanCreationStatus.value = LoanCreationStatus.Success
            } catch (e: Exception) {
                println("Loan creation failed:")
                println("Error message: ${e.message}")
                println("Error cause: ${e.cause}")
                e.printStackTrace()

                _error.value = "Failed to create loan: ${e.message}"
                _loanCreationStatus.value = LoanCreationStatus.Error(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun returnBook(loanId: String, bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.returnBook(loanId)
                // Refresh the selected book to show updated loan status
                loadBookById(bookId)
            } catch (e: Exception) {
                _error.value = "Failed to return book: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearLoanCreationStatus() {
        _loanCreationStatus.value = null
    }
}