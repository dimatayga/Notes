package com.example.notes.presentation.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.domain.model.Note

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.example.notes.ui.theme.NotesTheme
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onAddNoteClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory)
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    NotesContent(
        notes = notes,
        searchQuery = searchQuery,
        allTags = allTags,
        selectedTag = selectedTag,
        onAddNoteClick = onAddNoteClick,
        onNoteClick = onNoteClick,
        onSettingsClick = onSettingsClick,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onTagSelected = viewModel::onTagSelected,
        onDeleteNote = viewModel::deleteNote,
        onDeleteAllNotes = viewModel::deleteAllNotes
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesContent(
    notes: List<Note>,
    searchQuery: String,
    allTags: List<String>,
    selectedTag: String?,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onSettingsClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onTagSelected: (String?) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onDeleteAllNotes: () -> Unit
) {
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Удалить заметку") },
            text = { Text("Вы уверены, что хотите удалить эту заметку?") },
            confirmButton = {
                TextButton(onClick = {
                    noteToDelete?.let { onDeleteNote(it) }
                    noteToDelete = null
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Удалить все заметки") },
            text = { Text("Вы уверены, что хотите удалить все заметки?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAllNotes()
                    showDeleteAllDialog = false
                }) {
                    Text("Удалить все")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Найти...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            onSearchQueryChanged("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Заметки") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        if (notes.isNotEmpty()) {
                            IconButton(onClick = { showDeleteAllDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Delete all notes"
                                )
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (allTags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { onTagSelected(null) },
                            label = { Text("All") }
                        )
                    }
                    items(allTags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { onTagSelected(tag) },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()
                        ) "Пока нет заметок. Добавьте свою первую заметку!"
                        else "Никаких заметок найдено не было."
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notes) { note ->
                        NoteItem(
                            note = note,
                            onClick = { onNoteClick(note) },
                            onDeleteClick = { noteToDelete = note }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (note.imageUri != null) {
                AsyncImage(
                    model = note.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.tags.isNotEmpty() || note.reminderTime != null) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (note.reminderTime != null) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            note.tags.take(3).forEach { tag ->
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            tag,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete note",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    NotesTheme {
        NotesContent(
            notes = listOf(
                Note(
                    id = 1,
                    title = "First Note",
                    content = "Content of the first note",
                    timestamp = System.currentTimeMillis(),
                    tags = listOf("Personal", "Work")
                ),
                Note(
                    id = 2,
                    title = "Second Note",
                    content = "Content of the second note",
                    timestamp = System.currentTimeMillis(),
                    tags = listOf("Study")
                )
            ),
            searchQuery = "",
            allTags = listOf("Personal", "Work", "Study"),
            selectedTag = null,
            onAddNoteClick = {},
            onNoteClick = {},
            onSettingsClick = {},
            onSearchQueryChanged = {},
            onTagSelected = {},
            onDeleteNote = {},
            onDeleteAllNotes = {}
        )
    }
}
