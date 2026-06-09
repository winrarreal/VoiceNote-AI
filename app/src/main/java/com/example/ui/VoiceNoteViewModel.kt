package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class VoiceNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = VoiceNoteRepository(database.voiceNoteDao())

    // All Voice notes from Room, reactive
    val allNotes: StateFlow<List<VoiceNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Reminders
    val allReminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _activeRecordingState = MutableStateFlow("idle") // idle, recording, processing, paused
    val activeRecordingState: StateFlow<String> = _activeRecordingState.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _waveformData = MutableStateFlow<List<Float>>(List(15) { 0.2f })
    val waveformData: StateFlow<List<Float>> = _waveformData.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English") // English (UK), Türkçe, Turkmen, Russian
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _activeFilter = MutableStateFlow("all") // all, upcoming, week
    val activeFilter: StateFlow<String> = _activeFilter.asStateFlow()

    // Email Flow States
    private val _userEmail = MutableStateFlow("alexander.vance@example.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(true)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private val _userName = MutableStateFlow("Alexander Vance")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Alarm settings
    private val _alertLeadTime = MutableStateFlow("15 min") // 15 min, 30 min, 1 hour
    val alertLeadTime: StateFlow<String> = _alertLeadTime.asStateFlow()

    // Calendar selected date
    private val _selectedCalendarDate = MutableStateFlow("2023-11-10") // yyyy-MM-dd
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    // Temporary Note details for edits
    private val _transcriptionInProgressText = MutableStateFlow("")
    val transcriptionInProgressText: StateFlow<String> = _transcriptionInProgressText.asStateFlow()

    init {
        // Seed default database values if it's empty
        viewModelScope.launch {
            repository.allNotes.first().let { currentList ->
                if (currentList.isEmpty()) {
                    seedDefaultNotes()
                }
            }
        }
    }

    private var recordingJob: Job? = null

    fun startRecording() {
        _activeRecordingState.value = "recording"
        _recordingDurationMs.value = 0L
        
        recordingJob = viewModelScope.launch(Dispatchers.Default) {
            val startTime = System.currentTimeMillis()
            while (_activeRecordingState.value == "recording") {
                val elapsed = System.currentTimeMillis() - startTime
                _recordingDurationMs.value = elapsed
                
                // Pulsing dynamic waveform simulation
                _waveformData.value = List(15) {
                    (0.1f + Math.random().toFloat() * 0.9f)
                }
                delay(100)
            }
        }
    }

    fun pauseRecording() {
        if (_activeRecordingState.value == "recording") {
            _activeRecordingState.value = "paused"
            recordingJob?.cancel()
        } else if (_activeRecordingState.value == "paused") {
            // resume
            startRecording()
        }
    }

    fun cancelRecording() {
        _activeRecordingState.value = "idle"
        _recordingDurationMs.value = 0L
        recordingJob?.cancel()
    }

    fun stopAndProcessRecording(onProcessed: (String) -> Unit) {
        _activeRecordingState.value = "processing"
        recordingJob?.cancel()

        viewModelScope.launch(Dispatchers.Default) {
            // Simulated transcription based on standard demo text for realism
            val elapsedSecs = _recordingDurationMs.value / 1000
            delay(1500) // Simulate voice model processing latency

            val simulatedText = "Hey, just wanted to record a quick thought before I forget. Let's schedule a meeting on Jan 8 to discuss the new project timeline with Alexander. Also, I need to remember to call the team later today to confirm everyone is aligned on the deliverables for Q1. The audio quality seems pretty good right now, so hopefully, the AI catches all these details perfectly. End of note."
            
            // Run Gemini structured extraction
            val analysis = GeminiAnalysisService.analyzeTranscription(simulatedText, _selectedLanguage.value)
            
            val noteId = UUID.randomUUID().toString()
            val newNote = VoiceNote(
                id = noteId,
                title = analysis.suggestedTitle,
                transcription = simulatedText,
                originalLanguage = _selectedLanguage.value,
                audioUri = "simulated_audio_$noteId.m4a",
                audioDuration = elapsedSecs.toInt().coerceAtLeast(5),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                tags = analysis.suggestedTags,
                color = "violet",
                isFavorite = false,
                confidence = analysis.confidence,
                suggestedTitle = analysis.suggestedTitle,
                detectedNames = analysis.detectedNames,
                detectedDates = analysis.detectedDates,
                detectedActionItems = analysis.detectedActionItems,
                suggestedReminderDate = analysis.suggestedReminderDate
            )
            
            repository.insertNote(newNote)

            // If a reminder date is suggested, auto-create a reminder
            analysis.suggestedReminderDate?.let { reminderTime ->
                val r = Reminder(
                    id = UUID.randomUUID().toString(),
                    noteId = noteId,
                    triggerDate = reminderTime,
                    title = "Reminder for: ${analysis.suggestedTitle}",
                    message = "Based on voice notes: \"${analysis.detectedActionItems.take(40)}\"",
                    isEmailEnabled = _isEmailVerified.value,
                    isPushEnabled = true,
                    isCompleted = false
                )
                repository.insertReminder(r)
            }

            _activeRecordingState.value = "idle"
            _recordingDurationMs.value = 0
            
            launch(Dispatchers.Main) {
                onProcessed(noteId)
            }
        }
    }

    fun updateFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateFavorite(id, isFavorite)
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun saveNoteChanges(note: VoiceNote) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    // Language change
    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    // Active filter change
    fun setFilter(filter: String) {
        _activeFilter.value = filter
    }

    // Settings edits
    fun updateProfile(name: String, email: String) {
        _userName.value = name
        _userEmail.value = email
    }

    fun toggleEmailVerification() {
        _isEmailVerified.value = !_isEmailVerified.value
    }

    fun setAlertLeadTime(time: String) {
        _alertLeadTime.value = time
    }

    fun selectCalendarDay(day: String) {
        _selectedCalendarDate.value = day
    }

    // Add tag to note
    fun addTagToNote(note: VoiceNote, newTag: String) {
        if (newTag.isBlank()) return
        val currentTags = note.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        val formattedTag = newTag.trim().removePrefix("#")
        if (!currentTags.contains(formattedTag)) {
            currentTags.add(formattedTag)
            val updated = note.copy(tags = currentTags.joinToString(","))
            saveNoteChanges(updated)
        }
    }

    // Remove tag
    fun removeTagFromNote(note: VoiceNote, tagToRemove: String) {
        val currentTags = note.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        if (currentTags.remove(tagToRemove)) {
            val updated = note.copy(tags = currentTags.joinToString(","))
            saveNoteChanges(updated)
        }
    }

    // Clear all notes + reminders
    fun clearAllData() {
        viewModelScope.launch {
            allNotes.value.forEach { note ->
                repository.deleteNote(note.id)
            }
        }
    }

    private suspend fun seedDefaultNotes() {
        // Oct 24, 2023 10:42 AM
        val oct24 = 1698144120000L
        val note1 = VoiceNote(
            id = "seed-1",
            title = "Project Alpha Ideas",
            transcription = "The main takeaway from the meeting is that we need to pivot the architecture to microservices before Q3. Also, remind Sarah about the design system updates.",
            originalLanguage = "English",
            audioUri = "sample_audio_1.m4a",
            audioDuration = 84,
            createdAt = oct24,
            updatedAt = oct24,
            tags = "work,strategy,Q3",
            color = "violet",
            isFavorite = true,
            confidence = 0.98f,
            suggestedTitle = "Project Alpha Ideas",
            detectedNames = "Sarah",
            detectedDates = "before Q3|1698796800000|84|93",
            detectedActionItems = "Pivot architecture to microservices\nRemind Sarah about design system updates",
            suggestedReminderDate = oct24 + 1000 * 60 * 60 * 24
        )

        // Yesterday from current local time June 8, 2026
        val yesterday = System.currentTimeMillis() - 1000 * 60 * 60 * 24
        val note2 = VoiceNote(
            id = "seed-2",
            title = "Grocery List & Errands",
            transcription = "Need to pick up almond milk, coffee beans, and drop off the dry cleaning on the way back from the gym.",
            originalLanguage = "English",
            audioUri = "sample_audio_2.m4a",
            audioDuration = 45,
            createdAt = yesterday,
            updatedAt = yesterday,
            tags = "personal,grocery",
            color = "amber",
            isFavorite = false,
            confidence = 0.96f,
            suggestedTitle = "Grocery List & Errands",
            detectedNames = "",
            detectedDates = "today|$yesterday|0|5",
            detectedActionItems = "Pick up almond milk & coffee beans\nDrop off dry cleaning near gym",
            suggestedReminderDate = null
        )

        // Selected Calendar Note: Friday, Nov 10, 2023
        val nov10 = 1699627200000L // Nov 10, 2023 2:15 PM
        val note3 = VoiceNote(
            id = "seed-3",
            title = "Q3 Strategy Alignment Ideas",
            transcription = "Brainstorming session for the upcoming quarter. Key focus areas should include user retention loops and exploring new glassmorphism UI patterns for the dashboard...",
            originalLanguage = "English",
            audioUri = "sample_audio_3.m4a",
            audioDuration = 120,
            createdAt = nov10 - 1000 * 60 * 60 * 4, // 10:45 AM
            updatedAt = nov10 - 1000 * 60 * 60 * 4,
            tags = "work,strategy",
            color = "violet",
            isFavorite = false,
            confidence = 0.99f,
            suggestedTitle = "Q3 Strategy Alignment Ideas",
            detectedNames = "",
            detectedDates = "",
            detectedActionItems = "Explore retention loops\nDesign glassmorphism patterns",
            suggestedReminderDate = null
        )

        val note4 = VoiceNote(
            id = "seed-4",
            title = "Call David re: Design System",
            transcription = "Review the new color tokens and ensure the backdrop blur values are consistent across all mobile components.",
            originalLanguage = "English",
            audioUri = "sample_audio_4.m4a",
            audioDuration = 19,
            createdAt = nov10 + 1000 * 60 * 105, // 4:00 PM
            updatedAt = nov10 + 1000 * 60 * 105,
            tags = "design,system",
            color = "coral",
            isFavorite = false,
            confidence = 0.95f,
            suggestedTitle = "Call David re: Design System",
            detectedNames = "David",
            detectedDates = "",
            detectedActionItems = "Review color tokens\nVerify consistent backdrop blurs",
            suggestedReminderDate = nov10 + 1000 * 60 * 105
        )

        repository.insertNote(note1)
        repository.insertNote(note2)
        repository.insertNote(note3)
        repository.insertNote(note4)

        // Seed reminders
        repository.insertReminder(
            Reminder(
                id = "rem-1",
                noteId = "seed-1",
                triggerDate = oct24 + 1000 * 60 * 60 * 24,
                title = "SARAH: Design Updates",
                message = "Remind Sarah about the design system updates.",
                isCompleted = false
            )
        )
        repository.insertReminder(
            Reminder(
                id = "rem-4",
                noteId = "seed-4",
                triggerDate = nov10 + 1000 * 60 * 105,
                title = "DAVID: Design System System",
                message = "Review the new color tokens with David.",
                isCompleted = false
            )
        )
    }
}
