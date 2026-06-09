package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// --- MAIN NAV ROUTER COMPOSABLE ---
@Composable
fun VoiceNoteAppContent(viewModel: VoiceNoteViewModel, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Soft backdrop purple glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlowAccent.copy(alpha = 0.12f), Color.Transparent),
                            radius = size.width * 0.8f
                        ),
                        center = center
                    )
                }
        )
    }
}

// --- WELCOME ONBOARDING SCREEN ---
@Composable
fun WelcomeScreen(navController: NavController, viewModel: VoiceNoteViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        // Accent Background Ambient Light
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlowAccent.copy(alpha = 0.15f), Color.Transparent),
                            radius = size.width * 0.7f
                        ),
                        center = center
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Logo Visual Element
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .background(ObsidianCard, CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Microphone",
                        tint = ElectricIndigo,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "VoiceNote AI",
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.02).sp,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Speak. Remember. Never Forget.",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Actions & Progress Indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Horizontal Slide Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(width = 32.dp, height = 6.dp).background(ElectricIndigo, RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.size(6.dp).background(TextTertiary, CircleShape))
                    Box(modifier = Modifier.size(6.dp).background(TextTertiary, CircleShape))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Get Started Button
                Button(
                    onClick = { navController.navigate("home") },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("get_started_button"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(text = "Get Started", color = ObsidianBg, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.testTag("maybe_later_button")
                ) {
                    Text(text = "Maybe later", color = TextSecondary, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// --- HOME FEEDS SCREEN ---
@Composable
fun HomeScreen(navController: NavController, viewModel: VoiceNoteViewModel) {
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()

    val filteredNotes = remember(notes, activeFilter) {
        val now = System.currentTimeMillis()
        when (activeFilter) {
            "upcoming" -> notes.filter { it.suggestedReminderDate != null && it.suggestedReminderDate > now }
            "week" -> notes.filter { now - it.createdAt < 7 * 24 * 60 * 60 * 1000L }
            else -> notes
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("recording") },
                containerColor = GlowAccent,
                contentColor = ObsidianBg,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("floating_recording_trigger")
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Voice Note", modifier = Modifier.size(28.dp))
            }
        },
        bottomBar = {
            BottomNavigationBar(currentRoute = "home", navController = navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Custom TopBar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Good morning, ${userName.split(" ").firstOrNull() ?: "Alexander"}",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.02).sp
                        )
                        Text(
                            text = "October 24, 2023",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    // Notifications Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .background(ObsidianCard, CircleShape)
                                .border(1.dp, BorderSubtle, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = TextPrimary)
                        }
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .background(ObsidianCard, CircleShape)
                                .border(1.dp, BorderSubtle, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                        }
                    }
                }
            }

            // Hero Record Button Panel
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clickable { navController.navigate("recording") }
                            .drawBehind {
                                // Pulsing Ambient glow outer ring
                                drawCircle(
                                    color = ElectricIndigo.copy(alpha = 0.08f),
                                    radius = size.width * 0.9f
                                )
                                drawCircle(
                                    color = ElectricIndigo.copy(alpha = 0.05f),
                                    radius = size.width * 1.1f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(GlowAccent, Purple40)
                                    ),
                                    shape = CircleShape
                                )
                                .testTag("mic_hub_hero"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Record microphone trigger",
                                tint = ObsidianBg,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tap to record",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Small Waveform simulated visualization lines
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(32.dp)
                    ) {
                        val heights = listOf(8.dp, 16.dp, 24.dp, 20.dp, 28.dp, 12.dp, 24.dp, 8.dp)
                        heights.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h)
                                    .background(ElectricIndigo, RoundedCornerShape(1.5.dp))
                            )
                        }
                    }
                }
            }

            // Interactive Quick Stats Pills horizontal block
            item {
                val totalNotes = notes.size
                val upcomingCount = notes.filter { it.suggestedReminderDate != null }.size
                val thisWeekCount = notes.filter { System.currentTimeMillis() - it.createdAt < 7 * 24 * 60 * 60 * 1000L }.size

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatPill(
                        icon = Icons.Filled.Description,
                        label = "$totalNotes notes",
                        isActive = activeFilter == "all",
                        onClick = { viewModel.setFilter("all") },
                        modifier = Modifier.testTag("notes_count_pill").weight(1f)
                    )
                    StatPill(
                        icon = Icons.Filled.Event,
                        label = "$upcomingCount upcoming",
                        isActive = activeFilter == "upcoming",
                        onClick = { viewModel.setFilter("upcoming") },
                        modifier = Modifier.testTag("upcoming_count_pill").weight(1.1f)
                    )
                    StatPill(
                        icon = Icons.Filled.CalendarMonth,
                        label = "$thisWeekCount this week",
                        isActive = activeFilter == "week",
                        onClick = { viewModel.setFilter("week") },
                        modifier = Modifier.testTag("this_week_pill").weight(1.1f)
                    )
                }
            }

            // Recent Notes Title Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Recent Notes",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        color = ElectricIndigo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { }
                    )
                }
            }

            // Audio Notes List feed
            if (filteredNotes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Filled.Description, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "No notes found", color = TextSecondary, fontSize = 15.sp)
                    }
                }
            } else {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onNoteClick = { navController.navigate("detail/${note.id}") },
                        onFavoriteToggle = { viewModel.updateFavorite(note.id, !note.isFavorite) },
                        modifier = Modifier.testTag("note_item_card_${note.id}")
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) GlowAccent.copy(alpha = 0.25f) else ObsidianCard)
            .border(
                width = 1.dp,
                color = if (isActive) GlowAccent else BorderSubtle,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) ElectricIndigo else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = if (isActive) TextPrimary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NoteCard(
    note: VoiceNote,
    onNoteClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bulletColor = when (note.color) {
        "violet" -> ElectricIndigo
        "teal" -> TealNeon
        "coral" -> CoralNeon
        "amber" -> AmberNeon
        "sky" -> SkyNeon
        else -> ElectricIndigo
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onNoteClick)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header: Title / Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(bulletColor, CircleShape)
                    )
                    Text(
                        text = note.title,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Time label
                val df = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeStr = df.format(Date(note.createdAt))
                Text(
                    text = timeStr,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Excerpt Summary Transcription Block
            Text(
                text = note.transcription,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Tags row representation
            if (note.tags.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    note.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(3).forEach { t ->
                        Box(
                            modifier = Modifier
                                .background(BorderSubtle, RoundedCornerShape(4.dp))
                                .padding(vertical = 2.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = "#$t",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- FULL SCREEN RECORDING DIALOG OVERLAY ---
@Composable
fun RecordingOverlayScreen(navController: NavController, viewModel: VoiceNoteViewModel) {
    val durationMs by viewModel.recordingDurationMs.collectAsStateWithLifecycle()
    val recordingState by viewModel.activeRecordingState.collectAsStateWithLifecycle()
    val waveformData by viewModel.waveformData.collectAsStateWithLifecycle()
    val detectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    // High precision milliseconds formatting: 00:23.00
    val totalSeconds = durationMs / 1000
    val fractionMs = (durationMs % 1000) / 10
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    val durationText = String.format("%02d:%02d.%02d", mins, secs, fractionMs)

    // Automatically trigger voice recorder start on display launch
    LaunchedEffect(Unit) {
        if (recordingState == "idle") {
            viewModel.startRecording()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.95f))
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Status Badging Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ObsidianCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Red pulsing circle
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CoralNeon, CircleShape)
                        )
                        Text(
                            text = if (recordingState == "processing") "TRANSCRIPTION DISPATCHING..." else "RECORDING IN PROGRESS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderSubtle)
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🇺🇸", fontSize = 14.sp)
                        Text(text = "$detectedLanguage Detected", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Center: Bouncing Visual Waveform + Digital stopwatch
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                if (recordingState == "processing") {
                    CircularProgressIndicator(
                        color = ElectricIndigo,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "AI is thinking…",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    // Live Sound Waveform representation
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(horizontal = 32.dp)
                    ) {
                        waveformData.forEach { ampl ->
                            val dynamicHeight = (ampl * 100).dp.coerceIn(4.dp, 100.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dynamicHeight)
                                    .background(ElectricIndigo, RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = durationText,
                        color = TextPrimary,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Bottom Actions Control Hub
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Speak naturally — AI will handle the rest",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    // Pause/Resume button
                    IconButton(
                        onClick = { viewModel.pauseRecording() },
                        modifier = Modifier
                            .size(56.dp)
                            .background(ObsidianCard, CircleShape)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .testTag("pause_recording_button")
                    ) {
                        Icon(
                            imageVector = if (recordingState == "paused") Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = "Pause button",
                            tint = TextPrimary
                        )
                    }

                    // Huge red Stop and Process Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .drawBehind {
                                drawCircle(
                                    color = CoralNeon.copy(alpha = 0.15f),
                                    radius = size.width * 0.65f
                                )
                            }
                            .clickable {
                                viewModel.stopAndProcessRecording { noteId ->
                                    navController.navigate("detail/$noteId") {
                                        popUpTo("home")
                                    }
                                }
                            }
                            .testTag("stop_recording_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(CoralNeon, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "Stop voice note trigger",
                                tint = ObsidianBg,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Cancel button
                    IconButton(
                        onClick = {
                            viewModel.cancelRecording()
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(ObsidianCard, CircleShape)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .testTag("cancel_recording_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancel recording",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

// --- NOTE DETAIL EDIT SCREEN ---
@Composable
fun NoteDetailScreen(navController: NavController, noteId: String, viewModel: VoiceNoteViewModel) {
    var noteState by remember { mutableStateOf<VoiceNote?>(null) }
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    
    // Track new tags addition UI
    var showTagInput by remember { mutableStateOf(false) }
    var tagInputText by remember { mutableStateOf("") }
    
    // Reminder Toggle values
    var isPushEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(noteId, allNotes) {
        allNotes.firstOrNull { it.id == noteId }?.let {
            noteState = it
        }
    }

    val note = noteState
    if (note == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ElectricIndigo)
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go back", tint = TextPrimary)
                }
                Text(text = "New Note", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit title", tint = TextPrimary)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                
                // Confidence badge indicator
                item {
                    val confidencePercentage = (note.confidence * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TealNeon.copy(alpha = 0.08f))
                                .border(1.dp, TealNeon.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = TealNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Transcribed with $confidencePercentage% confidence",
                                    color = TealNeon,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Audio compact representations panel
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ObsidianCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(ElectricIndigo, CircleShape)
                            ) {
                                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = ObsidianBg)
                            }
                            
                            // Waveform bars representation
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val barHeights = listOf(12.dp, 24.dp, 36.dp, 16.dp, 28.dp, 40.dp, 20.dp, 12.dp, 32.dp, 24.dp, 40.dp, 16.dp, 12.dp)
                                barHeights.forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(h)
                                            .background(ElectricIndigo.copy(alpha = 0.3f), RoundedCornerShape(1.5.dp))
                                    )
                                }
                            }

                            val durationSecs = note.audioDuration
                            val durMin = durationSecs / 60
                            val durSec = durationSecs % 60
                            Text(
                                text = String.format("%02d:%02d", durMin, durSec),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Transcribed annotated text layout (Colored entity highlighting)
                item {
                    val annotatedTranscript = buildAnnotatedString {
                        val text = note.transcription
                        val namesList = note.detectedNames.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val datesHeuristics = listOf("meeting on Jan 8", "later today")
                        val actionWords = listOf("call the team", "pick up", "drop off")

                        var currentWordIndex = 0
                        val words = text.split(" ")
                        
                        words.forEachIndexed { idx, word ->
                            val cleanWord = word.trim(',', '.', '!', '?', '"')
                            
                            // Check match
                            when {
                                namesList.any { it.equals(cleanWord, ignoreCase = true) } -> {
                                    withStyle(style = SpanStyle(color = TealNeon, background = TealNeon.copy(alpha = 0.15f), fontWeight = FontWeight.Bold)) {
                                        append(word)
                                    }
                                }
                                text.substring(0.coerceAtLeast(text.indexOf(word) - 10).coerceAtLeast(0), (text.indexOf(word) + 20).coerceAtMost(text.length)).contains("meeting on Jan 8") && word.contains("Jan") || word.contains("8") || word.contains("meeting") -> {
                                    withStyle(style = SpanStyle(color = ElectricIndigo, background = ElectricIndigo.copy(alpha = 0.15f), textDecoration = TextDecoration.Underline)) {
                                        append(word)
                                    }
                                }
                                actionWords.any { text.substring(0.coerceAtLeast(text.indexOf(word) - 15).coerceAtLeast(0), (text.indexOf(word) + 20).coerceAtMost(text.length)).contains(it) } -> {
                                    withStyle(style = SpanStyle(color = CoralNeon, background = CoralNeon.copy(alpha = 0.12f))) {
                                        append(word)
                                    }
                                }
                                else -> {
                                    withStyle(style = SpanStyle(color = TextPrimary)) {
                                        append(word)
                                    }
                                }
                            }
                            if (idx < words.size - 1) append(" ")
                        }
                    }

                    Text(
                        text = annotatedTranscript,
                        fontSize = 17.sp,
                        lineHeight = 26.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Detected Insights (Action Item, Person, Calendar Reminders)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "DETECTED INSIGHTS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        // Action Item Row info
                        if (note.detectedActionItems.isNotBlank()) {
                            InsightBentoCard(
                                icon = Icons.Filled.Assignment,
                                label = "Action Item",
                                iconColor = CoralNeon,
                                content = note.detectedActionItems.split("\n").firstOrNull() ?: "Call the team",
                                showChevron = true
                            )
                        }

                        // Entity mentioned card
                        if (note.detectedNames.isNotBlank()) {
                            InsightBentoCard(
                                icon = Icons.Filled.Person,
                                label = "Mentioned",
                                iconColor = TealNeon,
                                content = note.detectedNames,
                                showChevron = true
                            )
                        }

                        // Calendar alert panel
                        InsightReminderCard(
                            reminderTitle = note.title,
                            isPushEnabled = isPushEnabled,
                            onToggle = { isPushEnabled = it }
                        )
                    }
                }

                // Editable Chip Tags section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "TAGS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activeTagsList = note.tags.split(",").map { it.trim() }.filter { t -> t.isNotEmpty() }
                            activeTagsList.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(BorderSubtle, RoundedCornerShape(20.dp))
                                        .clickable { viewModel.removeTagFromNote(note, tag) }
                                        .padding(vertical = 8.dp, horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Trigger Tag Input
                            IconButton(
                                onClick = { showTagInput = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.dp, BorderSubtle, CircleShape)
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add tag", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (showTagInput) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = tagInputText,
                                    onValueChange = { tagInputText = it },
                                    placeholder = { Text("newtag", color = TextTertiary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = ObsidianCard,
                                        unfocusedContainerColor = ObsidianCard,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = ElectricIndigo,
                                        unfocusedBorderColor = BorderSubtle
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                )
                                Button(
                                    onClick = {
                                        viewModel.addTagToNote(note, tagInputText)
                                        tagInputText = ""
                                        showTagInput = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text("Add", color = ObsidianBg)
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Save Note Sticky control at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("home") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_note_button"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = ObsidianBg)
                        Text(text = "Save Note", color = ObsidianBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InsightBentoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconColor: Color,
    content: String,
    showChevron: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                    Text(text = label, color = iconColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
                Text(text = content, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            if (showChevron) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
            }
        }
    }
}

@Composable
fun InsightReminderCard(
    reminderTitle: String,
    isPushEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Event, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(16.dp))
                        Text(text = "Event Detected", color = ElectricIndigo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "Meeting on Jan 8", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

                // Reminder inline toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "Set Reminder", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isPushEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBg,
                            checkedTrackColor = ElectricIndigo,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = ObsidianCard
                        ),
                        modifier = Modifier.scaleScaleFactorFixed()
                    )
                }
            }
        }
    }
}

@Composable
fun Modifier.scaleScaleFactorFixed() = this.height(24.dp).width(40.dp)


// --- CALENDAR AGENDA FEED SCREEN ---
@Composable
fun CalendarScreen(navController: NavController, viewModel: VoiceNoteViewModel) {
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()

    // Notes filtered specifically for selected November date
    val dayNotes = remember(notes, selectedDay) {
        notes.filter { note ->
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            df.format(Date(note.createdAt)) == selectedDay
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        bottomBar = {
            BottomNavigationBar(currentRoute = "calendar", navController = navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Calendar", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            // Central Calendar Block View (November 2023 Grid format)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        
                        // Month selector row header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Text(text = "November 2023", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }

                        // Days of Week labels
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
                            daysOfWeek.forEach { d ->
                                Text(
                                    text = d,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Month Grid Blocks
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Week Row 1
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                CalendarDayItem("30", isCurrentMonth = false, isSelected = false) {}
                                CalendarDayItem("31", isCurrentMonth = false, isSelected = false) {}
                                CalendarDayItem("1", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-01") }
                                CalendarDayItem("2", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-02") }
                                CalendarDayItem("3", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-03") }
                                CalendarDayItem("4", isCurrentMonth = true, isSelected = selectedDay == "2023-11-04") { viewModel.selectCalendarDay("2023-11-04") }
                                CalendarDayItem("5", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-05") }
                            }
                            // Week Row 2
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                CalendarDayItem("6", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-06") }
                                CalendarDayItem("7", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-07") }
                                CalendarDayItem("8", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-08") }
                                CalendarDayItem("9", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-09") }
                                CalendarDayItem("10", isCurrentMonth = true, isSelected = selectedDay == "2023-11-10", hasPrimaryHighlight = true) { viewModel.selectCalendarDay("2023-11-10") }
                                CalendarDayItem("11", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-11") }
                                CalendarDayItem("12", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-12") }
                            }
                            // Week Row 3
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                CalendarDayItem("13", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-13") }
                                CalendarDayItem("14", isCurrentMonth = true, isSelected = selectedDay == "2023-11-14") { viewModel.selectCalendarDay("2023-11-14") }
                                CalendarDayItem("15", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-15") }
                                CalendarDayItem("16", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-16") }
                                CalendarDayItem("17", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-17") }
                                CalendarDayItem("18", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-18") }
                                CalendarDayItem("19", isCurrentMonth = true, isSelected = false) { viewModel.selectCalendarDay("2023-11-19") }
                            }
                        }

                        // Grid Indicators Legend
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(ElectricIndigo, CircleShape))
                                Text(text = "Notes", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(AmberNeon, CircleShape))
                                Text(text = "Reminders", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Selected Day agenda list
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedSelected = when(selectedDay) {
                        "2023-11-10" -> "Friday, Nov 10"
                        "2023-11-04" -> "Saturday, Nov 4"
                        "2023-11-14" -> "Tuesday, Nov 14"
                        else -> "Selected Day"
                    }
                    Text(text = formattedSelected, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background(BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(vertical = 4.dp, horizontal = 10.dp)
                    ) {
                        Text(text = "${dayNotes.size} Items", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (dayNotes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "No recorded items on this day", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                items(dayNotes, key = { it.id }) { note ->
                    AgendaDetailCard(
                        note = note,
                        onNoteClick = { navController.navigate("detail/${note.id}") },
                        modifier = Modifier.testTag("calendar_note_item_${note.id}")
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    day: String,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    hasPrimaryHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    hasPrimaryHighlight -> GlowAccent
                    isSelected -> ElectricIndigo.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day,
                color = when {
                    hasPrimaryHighlight -> ObsidianBg
                    isSelected -> ElectricIndigo
                    isCurrentMonth -> TextPrimary
                    else -> TextTertiary
                },
                fontSize = 13.sp,
                fontWeight = if (hasPrimaryHighlight || isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun AgendaDetailCard(
    note: VoiceNote,
    onNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blockColor = when (note.color) {
        "violet" -> ElectricIndigo
        "teal" -> TealNeon
        "coral" -> CoralNeon
        "amber" -> AmberNeon
        "sky" -> SkyNeon
        else -> ElectricIndigo
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onNoteClick)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Colored status bar left border indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .background(blockColor, RoundedCornerShape(2.dp))
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val df = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeStr = df.format(Date(note.createdAt))
                Text(text = timeStr, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = note.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = note.transcription,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- SETTINGS PREFERENCES SCREEN ---
@Composable
fun SettingsScreen(navController: NavController, viewModel: VoiceNoteViewModel) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val alertLeadTime by viewModel.alertLeadTime.collectAsStateWithLifecycle()
    val activeLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isEmailEnabled by viewModel.isEmailVerified.collectAsStateWithLifecycle()

    var showProfileEditDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(userName) }
    var editEmailInput by remember { mutableStateOf(userEmail) }

    var showLanguageSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        bottomBar = {
            BottomNavigationBar(currentRoute = "settings", navController = navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Settings", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            // Profile Card Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "Profile", color = ElectricIndigo, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Large beautiful circle avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(GlowAccent.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "A",
                                    color = ElectricIndigo,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = userName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(text = userEmail, color = TextSecondary, fontSize = 14.sp)
                            }
                        }

                        Button(
                            onClick = {
                                editNameInput = userName
                                editEmailInput = userEmail
                                showProfileEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderSubtle),
                            modifier = Modifier.fillMaxWidth().testTag("save_profile_button")
                        ) {
                            Text(text = "Edit Profile Info", color = TextPrimary)
                        }
                    }
                }
            }

            // Notifications lead alert choices
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "Notifications", color = ElectricIndigo, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        
                        ToggleRow(
                            title = "Push Notifications",
                            description = "Receive alerts on this device for critical events.",
                            checked = isEmailEnabled,
                            onCheckedChange = { viewModel.toggleEmailVerification() }
                        )

                        Text(text = "Alert Lead Time", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("15 min", "30 min", "1 hour").forEach { time ->
                                val isSelected = alertLeadTime == time
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ObsidianCard else Color.Transparent)
                                        .clickable { viewModel.setAlertLeadTime(time) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = time,
                                        color = if (isSelected) ElectricIndigo else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Language Selector block item
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .clickable { showLanguageSheet = true }
                        .padding(20.dp)
                        .testTag("change_language_trigger")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "🇺🇸", fontSize = 24.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "$activeLanguage (UK)", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Transcription & Interface", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = TextTertiary)
                    }
                }
            }

            // App Preferences
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "App Preferences", color = ElectricIndigo, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        
                        ToggleRow(
                            title = "Dark Mode",
                            description = "Force dark theme everywhere.",
                            checked = true,
                            onCheckedChange = {}
                        )

                        ToggleRow(
                            title = "Auto-save Drafts",
                            description = "Unsaved drafts persist automatically.",
                            checked = true,
                            onCheckedChange = {}
                        )
                    }
                }
            }

            // Danger Zone Clear All Data
            item {
                Button(
                    onClick = { viewModel.clearAllData() },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralNeon.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, CoralNeon.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                        .testTag("clear_all_data_button"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CoralNeon, modifier = Modifier.size(16.dp))
                        Text(text = "Clear All Data", color = CoralNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Modal Sheet mock for language selector
    if (showLanguageSheet) {
        AlertDialog(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = ObsidianCard,
            title = { Text("Select Language", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val langs = listOf("English", "Türkçe", "Türkmençe", "Русский")
                    langs.forEach { lang ->
                        val isSel = activeLanguage == lang || (lang == "Türkmençe" && activeLanguage == "Turkmen")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(if (lang == "Türkmençe") "Turkmen" else lang)
                                    showLanguageSheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = lang, color = if (isSel) ElectricIndigo else TextPrimary)
                            if (isSel) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ElectricIndigo)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Profile Editing alert dialog dialog
    if (showProfileEditDialog) {
        AlertDialog(
            onDismissRequest = { showProfileEditDialog = false },
            containerColor = ObsidianCard,
            title = { Text("Edit Profile Info", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Full Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ObsidianBg,
                            unfocusedContainerColor = ObsidianBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmailInput,
                        onValueChange = { editEmailInput = it },
                        label = { Text("Email", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ObsidianBg,
                            unfocusedContainerColor = ObsidianBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editNameInput, editEmailInput)
                        showProfileEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                ) {
                    Text("Save", color = ObsidianBg)
                }
            }
        )
    }
}

@Composable
fun ToggleRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (description != null) {
                Text(text = description, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBg,
                checkedTrackColor = ElectricIndigo,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = ObsidianCard
            ),
            modifier = Modifier.scaleScaleFactorFixed()
        )
    }
}


// --- REUSABLE NAVIGATION BAR FOR ALL SCREENS ---
@Composable
fun BottomNavigationBar(currentRoute: String, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavBarItem(
                imageVector = Icons.Default.Home,
                label = "Home",
                isActive = currentRoute == "home",
                onClick = {
                    if (currentRoute != "home") navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
            NavBarItem(
                imageVector = Icons.Default.Description,
                label = "Notes",
                isActive = currentRoute == "notes",
                onClick = { }
            )
            NavBarItem(
                imageVector = Icons.Default.CalendarMonth,
                label = "Calendar",
                isActive = currentRoute == "calendar",
                onClick = {
                    if (currentRoute != "calendar") navController.navigate("calendar") {
                        popUpTo("home")
                    }
                }
            )
            NavBarItem(
                imageVector = Icons.Default.Notifications,
                label = "Alerts",
                isActive = currentRoute == "alerts",
                onClick = { }
            )
            NavBarItem(
                imageVector = Icons.Default.Settings,
                label = "Settings",
                isActive = currentRoute == "settings",
                onClick = {
                    if (currentRoute != "settings") navController.navigate("settings") {
                        popUpTo("home")
                    }
                }
            )
        }
    }
}

@Composable
fun NavBarItem(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = label,
            tint = if (isActive) ElectricIndigo else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = if (isActive) ElectricIndigo else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}
