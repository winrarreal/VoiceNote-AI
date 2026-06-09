package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<VoiceNote>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: String): Flow<VoiceNote?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdSync(id: String): VoiceNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: VoiceNote)

    @Update
    suspend fun updateNote(note: VoiceNote)

    @Query("UPDATE notes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    // Reminders
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): Reminder?

    @Query("SELECT * FROM reminders ORDER BY triggerDate ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE noteId = :noteId")
    fun getRemindersForNote(noteId: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE triggerDate >= :now AND isCompleted = 0 ORDER BY triggerDate ASC")
    fun getUpcomingReminders(now: Long): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateReminderCompleted(id: String, isCompleted: Boolean)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteRemindersForNote(noteId: String)
}
