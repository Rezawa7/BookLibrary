package com.example.booklibrary.data.models

import com.google.gson.annotations.SerializedName

data class Loan(
    @SerializedName("_id") val id: String = "", // Or ObjectId if you have a corresponding Kotlin type
    val bookId: String, // Or ObjectId if you have a corresponding Kotlin type
    @SerializedName("book_name")val bookName: String,
    @SerializedName("borrower_name") val borrowerName: String,
    @SerializedName("borrower_email") val borrowerEmail: String,
    @SerializedName("borrow_date")val borrowDate: String, // Consider using Date/DateTime type
    @SerializedName("return_date")val returnDate: String?, // Optional return date
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
    val active_loan: Loan?
)

data class LoanRequest(
    val book_id: String,
    val book_name: String,
    val borrower_name: String,
    val borrower_email: String,
    val borrow_date: String,
    val return_date: String,
    val status: String
)

data class ApiResponse(
    val message: String
)

