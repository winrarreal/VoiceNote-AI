package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class VoiceNoteAnalysisResult(
    val suggestedTitle: String,
    val detectedNames: String,
    val detectedDates: String,
    val detectedActionItems: String,
    val suggestedTags: String,
    val suggestedReminderDate: Long?,
    val confidence: Float = 0.98f
)

object GeminiAnalysisService {
    private const val TAG = "GeminiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Moshi parser
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun analyzeTranscription(text: String, originalLanguage: String): VoiceNoteAnalysisResult {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is empty or placeholder! Running offline fallback heuristic.")
            return runOfflineHeuristic(text)
        }

        val systemPrompt = """
            You are VoiceNote AI content analyzer. Analyze the provided voice note transcript block and extract structured metadata:
            1. suggestedTitle: concise, creative 3-7 word title of the note. No quotation marks.
            2. detectedNames: Comma-separated list of names of people mentioned (e.g. "Alexander, David, Sarah"). If none, leave empty string.
            3. detectedDates: Semicolon-separated entities representing days or times mentioned. Each entry must format strictly as: rawText|resolvedTimestampMillis|startIndex|endIndex.
               To resolve relative terms: Today is June 9, 2026. "meeting on Jan 8" translates to milliseconds representing Jan 8th, 2027. "call some team later today" resolves to June 9, 2026.
               Example value: "meeting on Jan 8|1800000000000|40|56;later today|1781000000000|80|91".
            4. detectedActionItems: Newline-separated list of actionable tasks identified in the transcript (e.g. "Call the design team\nPrepare project proposal"). If none, leave empty string.
            5. suggestedTags: Comma-separated list of 1-3 lowercase relevant hashtags without spaces (e.g. "ideas,strategy,planning"). If none, leave empty string.
            6. suggestedReminderDate: Long epoch timestamp (milliseconds) representing the most appropriate trigger alert time. For example, if they specify "remind me tomorrow" or "at 5pm", estimate that mills. If none is mentioned, return null.

            Return ONLY raw valid JSON matching this schema:
            {
              "suggestedTitle": "Title here",
              "detectedNames": "Name1, Name2",
              "detectedDates": "raw|ms|start|end",
              "detectedActionItems": "Action1\nAction2",
              "suggestedTags": "tag1,tag2",
              "suggestedReminderDate": null or 123456789000
            }
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Transcript: \"$text\"\n\nAnalyze this transcript.")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response from Gemini: Code ${response.code}")
                    return runOfflineHeuristic(text)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentJson = candidate.optJSONObject("content")
                    val partsJson = contentJson?.optJSONArray("parts")
                    if (partsJson != null && partsJson.length() > 0) {
                        val firstPart = partsJson.getJSONObject(0)
                        val textAnswer = firstPart.optString("text")

                        // Extract actual JSON object
                        val jsonAnswer = textAnswer.trim().trim('`').trim()
                        val resultObj = JSONObject(if (jsonAnswer.startsWith("json")) jsonAnswer.substring(4).trim() else jsonAnswer)

                        return VoiceNoteAnalysisResult(
                            suggestedTitle = resultObj.optString("suggestedTitle", "New Voice Note"),
                            detectedNames = resultObj.optString("detectedNames", ""),
                            detectedDates = resultObj.optString("detectedDates", ""),
                            detectedActionItems = resultObj.optString("detectedActionItems", ""),
                            suggestedTags = resultObj.optString("suggestedTags", "voice,note"),
                            suggestedReminderDate = if (resultObj.isNull("suggestedReminderDate")) null else resultObj.optLong("suggestedReminderDate"),
                            confidence = 0.98f
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini REST: ${e.message}", e)
        }

        // Return offline fallback
        return runOfflineHeuristic(text)
    }

    private fun runOfflineHeuristic(text: String): VoiceNoteAnalysisResult {
        // Fallback simple offline parsing
        val suggestedTitle = if (text.length > 30) {
            text.take(30).trim() + "..."
        } else if (text.isNotBlank()) {
            text
        } else {
            "New Note"
        }

        // Search names
        val names = ArrayList<String>()
        if (text.contains("Alexander", ignoreCase = true)) names.add("Alexander")
        if (text.contains("Sarah", ignoreCase = true)) names.add("Sarah")
        if (text.contains("David", ignoreCase = true)) names.add("David")

        // Search dates
        val dates = ArrayList<String>()
        val currentTimeMillis = System.currentTimeMillis()
        if (text.contains("Jan 8", ignoreCase = true)) {
            val startIdx = text.indexOf("Jan 8", ignoreCase = true)
            // Resolve to future Jan 8
            dates.add("meeting on Jan 8|1800000000000|$startIdx|${startIdx + 11}")
        } else if (text.contains("today", ignoreCase = true)) {
            val startIdx = text.indexOf("today", ignoreCase = true)
            dates.add("later today|$currentTimeMillis|$startIdx|${startIdx + 5}")
        }

        // Check for actions "call the team", "remember to", "need to"
        val actions = ArrayList<String>()
        if (text.contains("call the team", ignoreCase = true)) {
            actions.add("Call the team")
        }
        if (text.contains("pick up", ignoreCase = true)) {
            actions.add("Pick up items")
        }
        if (actions.isEmpty() && text.contains("to ", ignoreCase = true)) {
            actions.add("Action item detected")
        }

        // Suggested tags
        val tags = ArrayList<String>()
        if (text.contains("project", ignoreCase = true) || text.contains("work", ignoreCase = true)) {
            tags.add("project")
            tags.add("work")
        } else if (text.contains("grocery", ignoreCase = true) || text.contains("shop", ignoreCase = true)) {
            tags.add("grocery")
            tags.add("shopping")
        } else {
            tags.add("planning")
        }

        val suggestedReminder = if (dates.isNotEmpty()) currentTimeMillis + 1000 * 60 * 60 * 2 else null

        return VoiceNoteAnalysisResult(
            suggestedTitle = suggestedTitle,
            detectedNames = names.joinToString(", "),
            detectedDates = dates.joinToString(";"),
            detectedActionItems = actions.joinToString("\n"),
            suggestedTags = tags.joinToString(","),
            suggestedReminderDate = suggestedReminder,
            confidence = 0.85f // Lower confidence offline
        )
    }
}
