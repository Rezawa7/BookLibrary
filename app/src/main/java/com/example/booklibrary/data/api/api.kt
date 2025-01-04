package com.example.booklibrary.data.api

import com.example.booklibrary.data.models.*
import retrofit2.http.*

interface LibraryApi {
    @GET("api/books")
    suspend fun getAllBooks(): List<Book>

    @GET("api/books/search")
    suspend fun searchBooks(@Query("query") query: String): List<Book>

    @GET("api/books/{id}")
    suspend fun getBook(@Path("id") id: String): BookWithLoan

    @POST("api/books")
    suspend fun createBook(@Body book: Book): Book

    @PUT("api/books/{id}")
    suspend fun updateBook(@Path("id") id: String, @Body book: Book): Book

    @DELETE("api/books/{id}")
    suspend fun deleteBook(@Path("id") id: String)

    @POST("api/loans")
    suspend fun createLoan(@Body loan: Loan): Loan
}