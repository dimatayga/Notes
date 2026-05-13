package com.example.notes.data.repository

import com.example.notes.data.local.NoteDao
import com.example.notes.domain.model.Note
import com.example.notes.domain.model.NoteDraft
import com.example.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(
    private val dao: NoteDao,
) : NoteRepository {

    override fun getNotes(query: String): Flow<List<Note>> {
        return dao.getNotes(query)
    }

    override suspend fun getNoteById(id: Int): Note? {
        return dao.getNoteById(id)
    }

    override suspend fun insertNote(note: Note): Long {
        return dao.insertNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        dao.deleteNote(note)
    }

    override suspend fun deleteAllNotes() {
        dao.deleteAllNotes()
    }

    override suspend fun getDraft(): NoteDraft? {
        return dao.getDraft()
    }

    override suspend fun saveDraft(draft: NoteDraft) {
        dao.insertDraft(draft)
    }

    override suspend fun deleteDraft() {
        dao.deleteDraft()
    }
}
