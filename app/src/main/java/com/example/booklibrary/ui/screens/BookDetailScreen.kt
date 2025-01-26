package com.example.booklibrary.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booklibrary.ui.viewmodels.BookDetailViewModel
import com.example.booklibrary.ui.viewmodels.BookDetailViewModel.LoanCreationStatus
import com.example.booklibrary.data.models.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String?,
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val selectedBook by viewModel.selectedBook.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val loanCreationStatus by viewModel.loanCreationStatus.collectAsState()

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var publishYear by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var loanName by remember { mutableStateOf("") }
    var loanDate by remember { mutableStateOf("") }
    var loanStatus by remember { mutableStateOf("") }

    LaunchedEffect(bookId) {
        if (bookId != null) {
            viewModel.loadBookById(bookId)

        }
    }

    LaunchedEffect(selectedBook) {
        selectedBook?.let { bookWithLoan ->
            title = bookWithLoan.book.title
            author = bookWithLoan.book.author
            isbn = bookWithLoan.book.isbn.toString()
            publishYear = bookWithLoan.book.publishYear.toString()
            description = bookWithLoan.book.description.toString()
            status = bookWithLoan.book.status
            loanStatus = bookWithLoan.active_loan?.status.toString()
            loanName = bookWithLoan.active_loan?.borrowerName.toString()
            loanDate = bookWithLoan.active_loan?.borrowDate.toString()

        }
    }

    // Handle loan creation status
    LaunchedEffect(loanCreationStatus) {
        when (loanCreationStatus) {
            is LoanCreationStatus.Success -> {
                // Show success snackbar or navigate
                viewModel.clearLoanCreationStatus()
            }
            is LoanCreationStatus.Error -> {
                // Error is already handled by the error state
                viewModel.clearLoanCreationStatus()
            }
            null -> { /* Initial state */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (bookId == null) "Add Book" else "Edit Book") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = isbn,
                        onValueChange = { isbn = it },
                        label = { Text("ISBN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = publishYear,
                        onValueChange = { publishYear = it },
                        label = { Text("Publish Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Loan Status Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Book Status: $status",
                                style = MaterialTheme.typography.titleMedium
                            )

                            // Conditional rendering based on book status and active loan
                            when {
                                // If book is available and no active loan, show Loan Book button
                                status == "AVAILABLE" -> {
                                    if (bookId != null) {
                                        LoanBookButton(
                                            bookId = bookId,
                                            viewModel = viewModel,
                                            bookName = title,
                                            isEnabled = true
                                        )
                                    }
                                }

                                // If book has an active loan, show loan details and Return Book button
                                selectedBook?.active_loan != null -> {
                                    val loan = selectedBook!!.active_loan!!
                                    Text(
                                        text = "Loan Status: ${loanStatus}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Currently borrowed by: ${loanName}")
                                    Text("Borrowed on: ${loanDate}")
                                    Button(
                                        onClick = {
                                            viewModel.returnBook(loan.id, bookId ?: "")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Return Book")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val book = Book(
                                id = bookId ?: "",
                                title = title,
                                author = author,
                                isbn = isbn,
                                publishYear = publishYear.toIntOrNull() ?: 0,
                                description = description,
                                status = status
                            )
                            viewModel.saveBook(book)
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save")
                    }
                }
            }

            error?.let {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(it) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoanBookButton(
    bookId: String,
    bookName:String,
    viewModel: BookDetailViewModel,
    isEnabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        enabled = isEnabled
    ) {
        Text(if (isEnabled) "Loan Book" else "Book Unavailable")
    }

    if (showDialog) {
        LoanBookDialog(
            onDismiss = { showDialog = false },
            onConfirm = { borrowerName, borrowerEmail ->
                viewModel.createLoanForBook(
                    bookId = bookId,
                    borrowerName = borrowerName,
                    bookName = bookName,
                    borrowerEmail = borrowerEmail
                )
                showDialog = false
            }
        )
    }
}

@Composable
fun LoanBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (borrowerName: String, borrowerEmail: String) -> Unit
) {
    var borrowerName by remember { mutableStateOf("") }
    var borrowerEmail by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Loan Book") },
        text = {
            Column {
                OutlinedTextField(
                    value = borrowerName,
                    onValueChange = {
                        borrowerName = it
                        showError = false
                    },
                    label = { Text("Borrower Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = borrowerEmail,
                    onValueChange = {
                        borrowerEmail = it
                        showError = false
                    },
                    label = { Text("Borrower Email") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Text(
                        text = "Please enter valid name and email.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(borrowerEmail).matches()
                        && borrowerName.isNotBlank()
                    ) {
                        onConfirm(borrowerName, borrowerEmail)
                    } else {
                        showError = true
                    }
                },
                enabled = borrowerName.isNotBlank() && borrowerEmail.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}