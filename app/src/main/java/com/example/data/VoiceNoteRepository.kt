package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VoiceNoteRepository(private val dao: VoiceNoteDao) {
    val allNotes: Flow<List<VoiceNote>> = dao.getAllNotes()
    val allReminders: Flow<List<Reminder>> = dao.getAllReminders()

    fun getUpcomingReminders(now: Long): Flow<List<Reminder>> {
        return dao.getUpcomingReminders(now)
    }

    fun getNoteById(id: String): Flow<VoiceNote?> {
        return dao.getNoteById(id)
    }

    suspend fun getNoteByIdSync(id: String): VoiceNote? {
        return dao.getNoteByIdSync(id)
    }

    suspend fun insertNote(note: VoiceNote) {
        dao.insertNote(note)
    }

    suspend fun updateNote(note: VoiceNote) {
        dao.updateNote(note)
    }

    suspend fun updateFavorite(id: String, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    suspend fun deleteNote(id: String) {
        dao.deleteNote(id)
    }

    // Reminders
    fun getRemindersForNote(noteId: String): Flow<List<Reminder>> {
        return dao.getRemindersForNote(noteId)
    }

    suspend fun insertReminder(reminder: Reminder) {
        dao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        dao.updateReminder(reminder)
    }

    suspend fun updateReminderCompleted(id: String, isCompleted: Boolean) {
        dao.updateReminderCompleted(id, isCompleted)
    }

    suspend fun deleteReminder(id: String) {
        dao.deleteReminder(id)
    }

    suspend fun deleteRemindersForNote(noteId: String) {
        dao.deleteRemindersForNote(noteId)
    }

    suspend fun getReminderById(id: String): Reminder? {
        return dao.getReminderById(id)
    }
}
