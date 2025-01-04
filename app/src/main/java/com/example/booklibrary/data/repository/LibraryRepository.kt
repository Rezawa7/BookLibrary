package com.example.booklibrary.data.repository

import com.example.booklibrary.data.api.LibraryApi
import com.example.booklibrary.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val api: LibraryApi
) {
    suspend fun getAllBooks() = api.getAllBooks()
    suspend fun searchBooks(query: String) = api.searchBooks(query)
    suspend fun getBook(id: String) = api.getBook(id)
    suspend fun createBook(book: Book): Book = api.createBook(book)
    suspend fun updateBook(id: String, book: Book): Book = api.updateBook(id, book)
    suspend fun deleteBook(id: String) = api.deleteBook(id)
    suspend fun createLoan(loan: Loan) = api.createLoan(loan)
}



