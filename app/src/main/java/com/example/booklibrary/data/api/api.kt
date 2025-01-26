package com.example.booklibrary.data.api

import com.example.booklibrary.data.models.*
import retrofit2.http.*

interface LibraryApi {
    // Existing book endpoints
    @GET("api/books")
    suspend fun getAllBooks(): List<Book>

    @GET("api/books/search")
    suspend fun searchBooks(@Query("query") query: String): List<Book>

    @GET("api/books/{bookId}")
    suspend fun getBook(@Path("bookId") bookId: String): BookWithLoan

    @GET("api/loans")
    suspend fun getAllLoans(): List<Loan>

    @GET("api/loans/active")
    suspend fun getActiveLoans(): List<BookWithLoan>

    @POST("/api/loans")
    suspend fun createLoan(@Body loan: LoanRequest): Loan

    @PUT("api/loans/{loanId}/return")
    suspend fun returnBook(@Path("loanId") loanId: String): ApiResponse

    @POST("api/books")
    suspend fun createBook(@Body book: Book): Book

    @PUT("api/books/{id}")
    suspend fun updateBook(@Path("id") id: String, @Body book: Book): Book

    @DELETE("api/books/{id}")
    suspend fun deleteBook(@Path("id") id: String)

}