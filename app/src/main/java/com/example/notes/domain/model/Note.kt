package com.example.notes.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val title: String,
    val content: String,
    val timestamp: Long,
    val tags: List<String> = emptyList(),
    val imageUri: String? = null,
    val reminderTime: Long? = null
)
