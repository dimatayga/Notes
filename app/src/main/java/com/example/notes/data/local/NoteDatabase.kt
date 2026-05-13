package com.example.notes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notes.domain.model.Note
import com.example.notes.domain.model.NoteDraft

@Database(
    entities = [Note::class, NoteDraft::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class NoteDatabase: RoomDatabase() {

    abstract val noteDao: NoteDao

    companion object {
        const val DATABASE_NAME = "notes_db"
    }
}
