package com.example.notes.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteDraft(
    @PrimaryKey val id: Int = 1, // Only one draft for "new note"
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val imageUri: String? = null
)
