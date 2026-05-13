package com.example.notes.presentation.add_edit_note

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notes.NoteApplication
import com.example.notes.domain.model.Note
import com.example.notes.domain.model.NoteDraft
import com.example.notes.domain.repository.NoteRepository
import com.example.notes.util.ReminderScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AddEditNoteViewModel(
    private val repository: NoteRepository,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _noteTitle = mutableStateOf("")
    val noteTitle: State<String> = _noteTitle

    private val _noteContent = mutableStateOf("")
    val noteContent: State<String> = _noteContent

    private val _noteTags = mutableStateOf<List<String>>(emptyList())
    val noteTags: State<List<String>> = _noteTags

    private val _imageUri = mutableStateOf<String?>(null)
    val imageUri: State<String?> = _imageUri

    private val _reminderTime = mutableStateOf<Long?>(null)
    val reminderTime: State<Long?> = _reminderTime

    private var currentNoteId: Int? = null

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if (noteId != -1) {
                viewModelScope.launch {
                    repository.getNoteById(noteId)?.also { note ->
                        currentNoteId = note.id
                        _noteTitle.value = note.title
                        _noteContent.value = note.content
                        _noteTags.value = note.tags
                        _imageUri.value = note.imageUri
                        _reminderTime.value = note.reminderTime
                    }
                }
            } else {
                // Load draft for new note
                viewModelScope.launch {
                    repository.getDraft()?.also { draft ->
                        _noteTitle.value = draft.title
                        _noteContent.value = draft.content
                        _noteTags.value = draft.tags
                        _imageUri.value = draft.imageUri
                    }
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _noteTitle.value = title
    }

    fun onContentChanged(content: String) {
        _noteContent.value = content
    }

    fun onImageSelected(uri: String?) {
        _imageUri.value = uri
    }

    fun onReminderTimeSet(time: Long?) {
        _reminderTime.value = time
    }

    fun onTagAdded(tag: String) {
        if (tag.isNotBlank() && !_noteTags.value.contains(tag)) {
            _noteTags.value = _noteTags.value + tag
        }
    }

    fun onTagRemoved(tag: String) {
        _noteTags.value = _noteTags.value - tag
    }

    fun saveNote() {
        viewModelScope.launch {
            val note = Note(
                title = noteTitle.value,
                content = noteContent.value,
                timestamp = System.currentTimeMillis(),
                id = currentNoteId,
                tags = noteTags.value,
                imageUri = imageUri.value,
                reminderTime = reminderTime.value
            )
            val id = repository.insertNote(note)
            
            val savedNote = note.copy(id = id.toInt())
            if (savedNote.reminderTime != null) {
                ReminderScheduler.schedule(application, savedNote)
            } else {
                ReminderScheduler.cancel(application, savedNote)
            }

            if (currentNoteId == null) {
                repository.deleteDraft()
            }
            _eventFlow.emit(UiEvent.SaveNote)
        }
    }

    fun saveDraft() {
        if (currentNoteId == null && (noteTitle.value.isNotBlank() || noteContent.value.isNotBlank())) {
            viewModelScope.launch {
                repository.saveDraft(
                    NoteDraft(
                        title = noteTitle.value,
                        content = noteContent.value,
                        tags = noteTags.value,
                        imageUri = imageUri.value
                    )
                )
            }
        }
    }

    sealed class UiEvent {
        object SaveNote: UiEvent()
    }

    companion object {
        fun provideFactory(
            repository: NoteRepository,
            application: Application,
            savedStateHandle: SavedStateHandle
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddEditNoteViewModel(repository, application, savedStateHandle)
            }
        }
    }
}
