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
import com.example.notes.domain.model.Note
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onAddNoteClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
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

@Composable
private fun DeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Удалить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchToggle: (Boolean) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDeleteAllClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasNotes: Boolean
) {
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
                    onSearchToggle(false)
                    onSearchQueryChanged("")
                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            }
        )
    } else {
        TopAppBar(
            title = { Text("Заметки") },
            actions = {
                IconButton(onClick = { onSearchToggle(true) }) {
                    Icon(
                        Icons.Default.Search,
                        "Search"
                    )
                }
                if (hasNotes) {
                    IconButton(onClick = onDeleteAllClick) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            "Delete all"
                        )
                    }
                }
                IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, "Settings") }
            }
        )
    }
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

    noteToDelete?.let { note ->
        DeleteDialog(
            title = "Удалить заметку",
            text = "Вы уверены, что хотите удалить эту заметку?",
            onConfirm = { onDeleteNote(note); noteToDelete = null },
            onDismiss = { noteToDelete = null }
        )
    }

    if (showDeleteAllDialog) {
        DeleteDialog(
            title = "Удалить все заметки",
            text = "Вы уверены, что хотите удалить все заметки?",
            onConfirm = { onDeleteAllNotes(); showDeleteAllDialog = false },
            onDismiss = { showDeleteAllDialog = false }
        )
    }

    Scaffold(
        topBar = {
            NotesTopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchToggle = { isSearchActive = it },
                onSearchQueryChanged = onSearchQueryChanged,
                onDeleteAllClick = { showDeleteAllDialog = true },
                onSettingsClick = onSettingsClick,
                hasNotes = notes.isNotEmpty()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, "Add note") }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (allTags.isNotEmpty()) {
                LazyRow(
                    Modifier
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
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        if (searchQuery.isEmpty()) "Заметок нет...   Добавьте!"
                        else "Ничего не найдено"
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(
                        items = notes,
                        key = { note -> note.content}
                    ) { note ->
//                        NoteItem(note, { onNoteClick(note) }, { noteToDelete = note })
                        NoteItem(
                            modifier = Modifier.animateItem(),
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
    modifier: Modifier = Modifier,
    note: Note,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
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