package com.example.booklibrary.ui.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booklibrary.data.models.BookWithLoan
import com.example.booklibrary.data.models.Loan
import com.example.booklibrary.data.models.LoanRequest
import com.example.booklibrary.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {
    private val _activeLoans = MutableStateFlow<List<BookWithLoan>>(emptyList())
    val activeLoans: StateFlow<List<BookWithLoan>> = _activeLoans.asStateFlow()

    private val _allLoans = MutableStateFlow<List<Loan>>(emptyList())
    val allLoans: StateFlow<List<Loan>> = _allLoans.asStateFlow()

    private val _selectedBookWithLoan = MutableStateFlow<BookWithLoan?>(null)
    val selectedBookWithLoan: StateFlow<BookWithLoan?> = _selectedBookWithLoan.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()


    fun loadActiveLoans() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _activeLoans.value = repository.getActiveLoans()
            } catch (e: Exception) {
                _error.value = "Failed to load active loans: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllLoans() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allLoans.value = repository.getAllLoans()
                println("Loaded active loans: $_allLoans")
            } catch (e: Exception) {
                _error.value = "Failed to load loan history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createLoan(bookId: String, bookName:String ,borrowerName: String, borrowerEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loanRequest = LoanRequest(
                    book_id = bookId,
                    book_name = bookName,
                    borrower_name = borrowerName,
                    borrower_email = borrowerEmail,
                    borrow_date = LocalDateTime.now().toString(),
                    return_date = LocalDateTime.now().plusDays(30).toString(),
                    status = "ACTIVE"
                )
                repository.createLoan(loanRequest)
                loadActiveLoans() // Refresh the active loans list
            } catch (e: Exception) {
                _error.value = "Failed to create loan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun returnBook(loanId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.returnBook(loanId)
                loadActiveLoans() // Refresh the active loans list
                loadAllLoans() // Refresh the loan history
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
}

