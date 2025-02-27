package com.example.booklibrary.ui.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booklibrary.data.models.Book
import com.example.booklibrary.data.models.BookWithLoan
import com.example.booklibrary.data.models.Loan
import com.example.booklibrary.ui.viewmodels.LoanViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    viewModel: LoanViewModel = hiltViewModel(),
    onNavigateBack: (String) -> Unit
) {
    val activeLoans by viewModel.activeLoans.collectAsState()
    val allLoans by viewModel.allLoans.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showLoanDialog by remember { mutableStateOf(false) }
    var selectedBookForLoan by remember { mutableStateOf<Book?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadActiveLoans()
        viewModel.loadAllLoans()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Management") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Active Loans") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Loan History") }
                )
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    error != null -> {
                        ErrorMessage(
                            error = error!!,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                    else -> {
                        when (selectedTabIndex) {
                            0 -> ActiveLoansContent(
                                activeLoans = activeLoans,
                                onReturnBook = { loanId ->
                                    scope.launch {
                                        viewModel.returnBook(loanId)
                                    }
                                },
                                onBookClick = onNavigateBack
                            )
                            1 -> LoanHistoryContent(
                                allLoans = allLoans,
                                onBookClick = onNavigateBack
                            )
                        }
                    }
                }
            }
        }
    }

    // Loan Dialog
    if (showLoanDialog && selectedBookForLoan != null) {
        CreateLoanDialog(
            book = selectedBookForLoan!!,
            onDismiss = { showLoanDialog = false },
            onCreateLoan = { name, email, bookName ->
                scope.launch {
                    viewModel.createLoan(
                        selectedBookForLoan!!.id,
                        bookName,
                        name,
                        email
                    )
                    showLoanDialog = false
                }
            }
        )
    }
}

@Composable
fun ActiveLoansContent(
    activeLoans: List<BookWithLoan>,
    onReturnBook: (String) -> Unit,
    onBookClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(activeLoans) { bookWithLoan ->
            ActiveLoanCard(
                bookWithLoan = bookWithLoan,
                onReturnBook = onReturnBook,
                onBookClick = onBookClick
            )
        }
    }
}

@Composable
fun ActiveLoanCard(
    bookWithLoan: BookWithLoan,
    onReturnBook: (String) -> Unit,
    onBookClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = bookWithLoan.book.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "By ${bookWithLoan.book.author}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            bookWithLoan.active_loan?.let { loan ->
                Text(
                    text = "Borrowed by: ${bookWithLoan.active_loan.borrowerName}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Borrow Date: ${bookWithLoan.active_loan.borrowDate}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Status: ${bookWithLoan.active_loan.status}",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onReturnBook(loan.id) }
                    ) {
                        Text("Return Book")
                    }
                }
            }
        }
    }
}

@Composable
fun LoanHistoryContent(
    allLoans: List<Loan>,
    onBookClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(allLoans) { loan ->
            LoanHistoryCard(loan = loan, onBookClick = onBookClick)
        }
    }
}

@Composable
fun LoanHistoryCard(
    loan: Loan,
    onBookClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Borrowed Book: ${loan.bookName}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Borrower: ${loan.borrowerName}",
                style = MaterialTheme.typography.bodyLarge
            )
            loan.returnDate?.let { returnDate ->
                Text(
                    text = "Return Date: $returnDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Status: ${loan.status}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Loan ID: ${loan.id}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CreateLoanDialog(
    book: Book,
    onDismiss: () -> Unit,
    onCreateLoan: (name: String, email: String, bookName:String) -> Unit
) {
    var borrowerName by remember { mutableStateOf("") }
    var borrowerEmail by remember { mutableStateOf("") }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Loan")
        },
        text = {
            Column {
                Text(
                    text = "Book: ${book.title}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = borrowerName,
                    onValueChange = { borrowerName = it },
                    label = { Text("Borrower Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = borrowerEmail,
                    onValueChange = { borrowerEmail = it },
                    label = { Text("Borrower Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateLoan(borrowerName, borrowerEmail, book.title)
                },
                enabled = borrowerName.isNotBlank() && borrowerEmail.isNotBlank()
            ) {
                Text("Create Loan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ErrorMessage(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error"
                )
            }
        }
    }
}