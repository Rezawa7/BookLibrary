package com.example.booklibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.booklibrary.ui.screens.*
import com.example.booklibrary.ui.theme.BookLibraryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookLibraryTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    LibraryApp()
                }
            }
        }
    }
}

@Composable
fun LibraryApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "bookList") {
        composable("bookList") {
            BookListScreen(
                onBookClick = { bookId ->
                    navController.navigate("bookDetail/$bookId")
                },
                onAddClick = {
                    navController.navigate("bookDetail/new")
                }
            )
        }
        composable(
            route = "bookDetail/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            BookDetailScreen(
                bookId = if (bookId == "new") null else bookId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}