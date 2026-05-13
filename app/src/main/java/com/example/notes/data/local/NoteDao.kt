package com.example.notes.data.local

import androidx.room.*
import com.example.notes.domain.model.Note
import com.example.notes.domain.model.NoteDraft
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM Note WHERE title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun getNotes(query: String = ""): Flow<List<Note>>

    @Query("SELECT * FROM Note WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Delete
    suspend fun deleteNote(note: Note): Unit

    @Query("DELETE FROM Note")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM NoteDraft WHERE id = 1")
    suspend fun getDraft(): NoteDraft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: NoteDraft)

    @Query("DELETE FROM NoteDraft WHERE id = 1")
    suspend fun deleteDraft()
}
