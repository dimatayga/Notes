package com.example.notes.presentation.notes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.domain.model.Note
import com.example.notes.domain.repository.NoteRepository
import com.example.notes.util.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val application: Application
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = combine(
        _notes, _searchQuery, _selectedTag
    ) { notes, query, tag ->
        notes.filter { note ->
            (note.title.contains(query, ignoreCase = true) || note.content.contains(
                query, ignoreCase = true
            )) &&
                    (tag == null || note.tags.contains(tag))
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(
            5000
        ), emptyList()
    )

    val allTags: StateFlow<List<String>> = _notes.map { notes ->
        notes.flatMap { it.tags }.distinct().sorted()
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(
            5000
        ), emptyList()
    )

    init {
        repository.getNotes().onEach { notes ->
            _notes.value = notes
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTagSelected(tag: String?) {
        _selectedTag.value = tag
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            ReminderScheduler.cancel(application, note)
            repository.deleteNote(note)
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            _notes.value.forEach { note ->
                ReminderScheduler.cancel(application, note)
            }
            repository.deleteAllNotes()
        }
    }
}
