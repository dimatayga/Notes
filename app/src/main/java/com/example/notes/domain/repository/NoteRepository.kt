package com.example.notes.domain.repository

import com.example.notes.domain.model.Note
import com.example.notes.domain.model.NoteDraft
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getNotes(query: String = ""): Flow<List<Note>>

    suspend fun getNoteById(id: Int): Note?

    suspend fun insertNote(note: Note): Long

    suspend fun deleteNote(note: Note)

    suspend fun deleteAllNotes()

    suspend fun getDraft(): NoteDraft?

    suspend fun saveDraft(draft: NoteDraft)

    suspend fun deleteDraft()
}
