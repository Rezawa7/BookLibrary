package com.example.booklibrary.data.models

import com.google.gson.annotations.SerializedName

data class Loan(
    val _id: String,
    val bookId: String,
    val borrowerName: String,
    val borrowerEmail: String,
    val borrowDate: String,
    val returnDate: String,
    val status: String
)

data class Book(
    @SerializedName("_id") val id: String = "",
    val title: String,
    val author: String,
    val isbn: String? = null,
    @SerializedName("publish_year") val publishYear: Int? = null, // Correct mapping
    val description: String? = null,
    val status: String = "AVAILABLE"
)

data class BookWithLoan(
    val book: Book,
    val activeLoan: Loan?
)

data class LoanRequest(
    val bookId: String,
    val borrowerName: String,
    val borrowerEmail: String,
    val borrowDate: String,
    val dueDate: String,
    val status: String
)

data class ApiResponse(
    val message: String
)

