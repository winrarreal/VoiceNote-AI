package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "notes",
    indices = [Index(value = ["createdAt"])]
)
data class VoiceNote(
    @PrimaryKey val id: String,
    val title: String,
    val transcription: String,
    val originalLanguage: String,
    val audioUri: String?,
    val audioDuration: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: String, // Comma separated (e.g. "Work,Strategy")
    val color: String, // "violet", "teal", "coral", "amber", "sky"
    val isFavorite: Boolean = false,
    
    // AI Metadata Fields (Extracted from analysis)
    val confidence: Float = 1.0f,
    val suggestedTitle: String? = null,
    val detectedNames: String = "", // Comma separated (e.g. "Alexander,David")
    val detectedDates: String = "", // Format: "rawText|resolvedTimestamp|startIndex|endIndex;..."
    val detectedActionItems: String = "", // Newline separated (e.g. "Call the team\nBuy coffee")
    val suggestedReminderDate: Long? = null
)

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = VoiceNote::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["noteId"]),
        Index(value = ["triggerDate"])
    ]
)
data class Reminder(
    @PrimaryKey val id: String,
    val noteId: String,
    val triggerDate: Long,
    val title: String,
    val message: String,
    val isEmailEnabled: Boolean = false,
    val isPushEnabled: Boolean = true,
    val isCompleted: Boolean = false,
    val localNotificationId: String? = null
)
