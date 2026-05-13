package com.example.notes.presentation.add_edit_note

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.NoteApplication
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    onSaveNote: () -> Unit,
    noteId: Int,
    repository: com.example.notes.domain.repository.NoteRepository
) {
    val context = LocalContext.current
    val viewModel: AddEditNoteViewModel = viewModel(
        factory = AddEditNoteViewModel.provideFactory(
            repository = repository,
            application = context.applicationContext as NoteApplication,
            savedStateHandle = SavedStateHandle(mapOf("noteId" to noteId))
        )
    )

    val titleState = viewModel.noteTitle.value
    val contentState = viewModel.noteContent.value
    val tagsState = viewModel.noteTags.value
    val imageUriState = viewModel.imageUri.value
    val reminderTimeState = viewModel.reminderTime.value

    var showTagDialog by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.onImageSelected(it.toString())
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    viewModel.onReminderTimeSet(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditNoteViewModel.UiEvent.SaveNote -> {
                    onSaveNote()
                }
            }
        }
    }

    BackHandler {
        viewModel.saveDraft()
        onSaveNote()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == -1) "Добавить заметку" else "Редактировать заметку") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveDraft()
                        onSaveNote()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Set reminder"
                        )
                    }
                    IconButton(onClick = { photoPickerLauncher.launch(arrayOf("image/*")) }) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Add image")
                    }
                    IconButton(onClick = { showTagDialog = true }) {
                        Icon(imageVector = Icons.Default.Tag, contentDescription = "Add tag")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveNote() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (imageUriState != null) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    AsyncImage(
                        model = imageUriState,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.onImageSelected(null) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove image",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (reminderTimeState != null) {
                val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
                SuggestionChip(
                    onClick = { viewModel.onReminderTimeSet(null) },
                    label = { Text("Reminder: ${sdf.format(Date(reminderTimeState))}") },
                    icon = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (tagsState.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tagsState) { tag ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.onTagRemoved(tag) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            TextField(
                value = titleState,
                onValueChange = { viewModel.onTitleChanged(it) },
                placeholder = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = contentState,
                onValueChange = { viewModel.onContentChanged(it) },
                placeholder = { Text("Описание заметки") },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Добавить Тег") },
            text = {
                TextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onTagAdded(tagInput)
                    tagInput = ""
                    showTagDialog = false
                }) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
