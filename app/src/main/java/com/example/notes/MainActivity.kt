package com.example.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notes.presentation.add_edit_note.AddEditNoteScreen
import com.example.notes.presentation.notes.NotesScreen
import com.example.notes.presentation.settings.SettingsScreen
import com.example.notes.ui.theme.NotesTheme
import com.example.notes.ui.util.Screen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as NoteApplication).repository
        val preferencesManager = (application as NoteApplication).preferencesManager
        setContent {
            val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = null)

            NotesTheme(
                darkTheme = isDarkMode ?: androidx.compose.foundation.isSystemInDarkTheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.NotesScreen.route
                    ) {
                        composable(route = Screen.NotesScreen.route) {
                            NotesScreen(
                                onAddNoteClick = {
                                    navController
                                        .navigate(
                                            Screen
                                                .AddEditNoteScreen.route + "?noteId=-1"
                                        )
                                },
                                onNoteClick = { note ->
                                    navController.navigate(
                                        Screen
                                            .AddEditNoteScreen.route + "?noteId=${note.id}"
                                    )
                                },
                                onSettingsClick = {
                                    navController.navigate(Screen.SettingsScreen.route)
                                }
                            )
                        }
                        composable(
                            route = Screen.AddEditNoteScreen.route + "?noteId={noteId}",
                            arguments = listOf(
                                navArgument(name = "noteId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ) {
                            val noteId = it.arguments?.getInt("noteId") ?: -1
                            AddEditNoteScreen(
                                onSaveNote = {
                                    navController.navigateUp()
                                },
                                noteId = noteId,
                                repository = repository
                            )
                        }
                        composable(route = Screen.SettingsScreen.route) {
                            SettingsScreen(
                                onBackClick = {
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
