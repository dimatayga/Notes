package com.example.notes

import android.app.Application
import androidx.room.Room
import com.example.notes.data.local.NoteDatabase
import com.example.notes.data.repository.NoteRepositoryImpl
import com.example.notes.domain.repository.NoteRepository
import com.example.notes.data.preferences.PreferencesManager

class NoteApplication : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
    }

    private val database by lazy {
        Room.databaseBuilder(
            this,
            NoteDatabase::class.java,
            NoteDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration(true)
            .build()
    }

    val repository: NoteRepository by lazy {
        NoteRepositoryImpl(database.noteDao)
    }
}
