// DataStoreManager.kt
package com.example.stopwatchmetrics

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log

// ───────────────────────────────
// Extension property on Context
// ───────────────────────────────
val Context.dataStore by preferencesDataStore(name = "settings")

// ───────────────────────────────
// Time Format Key & Helpers (already existed)
// ───────────────────────────────
val TIME_FORMAT_KEY = booleanPreferencesKey("time_format_setting")

data class TimeFormatSetting(
    val useShortFormat: Boolean = false
)

suspend fun saveTimeFormatSetting(context: Context, useShortFormat: Boolean) {
    context.dataStore.edit { settings ->
        settings[TIME_FORMAT_KEY] = useShortFormat
        Log.d("DataStore", "Time format setting saved: $useShortFormat")
    }
}

fun readTimeFormatSetting(context: Context): Flow<Boolean> {
    return context.dataStore.data.map { preferences ->
        val value = preferences[TIME_FORMAT_KEY] ?: false
        Log.d("DataStore", "Loaded time format setting: $value")
        value
    }
}

// ───────────────────────────────
// “Show Tips on Start” Key (already existed)
// ───────────────────────────────
val SHOW_START_TIPS = booleanPreferencesKey("show_start_tips")

suspend fun saveShowTips(context: Context, value: Boolean) {
    context.dataStore.edit { it[SHOW_START_TIPS] = value }
}

fun readShowTips(context: Context): Flow<Boolean> =
    context.dataStore.data
        .map { prefs -> prefs[SHOW_START_TIPS] ?: true }


// ───────────────────────────────
// SheetSettings Keys & Helpers (already existed)
// ───────────────────────────────
private val SHOW_POINT_KEY      = booleanPreferencesKey("show_point")
private val SHOW_TIME_KEY       = booleanPreferencesKey("show_time")
private val SHOW_TMU_KEY        = booleanPreferencesKey("show_tmu")
private val SHOW_START_TIME_KEY = booleanPreferencesKey("show_start_time")
private val SHOW_COMMENT_KEY    = booleanPreferencesKey("show_comment")
private val SHOW_IMAGE_KEY      = booleanPreferencesKey("show_image")

data class SheetSettings(
    val showPoint: Boolean = true,
    val showTime: Boolean = true,
    val showTMU: Boolean = false,
    val showStartTime: Boolean = true,
    val showComment: Boolean = true,
    val showImage: Boolean = true
)

suspend fun saveSheetSettings(context: Context, sheetSettings: SheetSettings) {
    context.dataStore.edit { prefs ->
        prefs[SHOW_POINT_KEY]       = sheetSettings.showPoint
        prefs[SHOW_TIME_KEY]        = sheetSettings.showTime
        prefs[SHOW_TMU_KEY]         = sheetSettings.showTMU
        prefs[SHOW_START_TIME_KEY]  = sheetSettings.showStartTime
        prefs[SHOW_COMMENT_KEY]     = sheetSettings.showComment
        prefs[SHOW_IMAGE_KEY]       = sheetSettings.showImage
    }
}

fun readSheetSettings(context: Context): Flow<SheetSettings> =
    context.dataStore.data.map { prefs ->
        SheetSettings(
            showPoint     = prefs[SHOW_POINT_KEY]       ?: true,
            showTime      = prefs[SHOW_TIME_KEY]        ?: true,
            showTMU       = prefs[SHOW_TMU_KEY]         ?: false,
            showStartTime = prefs[SHOW_START_TIME_KEY]  ?: true,
            showComment   = prefs[SHOW_COMMENT_KEY]     ?: true,
            showImage     = prefs[SHOW_IMAGE_KEY]       ?: true
        )
    }

// ───────────────────────────────
// FastComments Settings (already existed)
// ───────────────────────────────


private val FAST_COMMENTS_ENABLED_KEY = booleanPreferencesKey("fast_comments_enabled")
private val FAST_COMMENTS_LIST_KEY    = stringSetPreferencesKey("fast_comments_list")

suspend fun saveFastCommentsSettings(context: Context, settings: FastCommentsSettings) {
    context.dataStore.edit { prefs ->
        prefs[FAST_COMMENTS_ENABLED_KEY] = settings.enabled
        prefs[FAST_COMMENTS_LIST_KEY]    = settings.comments.toSet()
    }
}

fun readFastCommentsSettings(context: Context): Flow<FastCommentsSettings> =
    context.dataStore.data.map { prefs ->
        val enabled = prefs[FAST_COMMENTS_ENABLED_KEY] ?: false
        val savedSet = prefs[FAST_COMMENTS_LIST_KEY]
        val list = savedSet?.toList() ?: FastCommentsSettings().comments
        FastCommentsSettings(enabled = enabled, comments = list)
    }

// ───────────────────────────────
// Dark Mode Key & Helpers (already existed)
// ───────────────────────────────
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")

suspend fun saveDarkModeSetting(context: Context, enabled: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[DARK_MODE_KEY] = enabled
    }
}

fun readDarkModeSetting(context: Context): Flow<Boolean> =
    context.dataStore.data.map { prefs -> prefs[DARK_MODE_KEY] ?: true }


// ───────────────────────────────
// NEW: PresetCycle Persistance
// ───────────────────────────────
//
// We map each PresetCycle(name, steps) ↔ a single String: "name::step1|step2|step3".
// We then store the entire List<PresetCycle> as a StringSet under key PRESETS_KEY.


/**
 * Represents one user‐defined cycle.  (This mirrors your in‐memory data class.)
 */
data class PresetCycle(
    val name: String,
    val steps: List<String>
)

/**
 * Usage format for serializing a single PresetCycle into a String:
 *
 *    "<presetName>::<step1>|<step2>|<step3>|…"
 *
 * Then we put all of those Strings into a Set<String> and store under PRESETS_KEY.
 * On read‐back, we split by "::" and then split the "steps" portion by "|" to rehydrate.
 *
 * Note: We assume that the literal substrings "::" and "|" never appear inside any presetName or step.
 * If you need to support arbitrary characters, you’d have to either escape them or switch to a JSON library.
 */

private val PRESETS_KEY = stringSetPreferencesKey("preset_cycles")

/**
 * Save the entire list of PresetCycle into DataStore.
 */
suspend fun saveAllPresetCycles(context: Context, allCycles: List<PresetCycle>) {
    // Convert each PresetCycle → single string
    val serializedSet: Set<String> = allCycles.map { cycle ->
        // Join steps by "|"
        val stepsJoined = cycle.steps.joinToString(separator = "|")
        // Format: "name::step1|step2|step3"
        "${cycle.name}::${stepsJoined}"
    }.toSet()

    context.dataStore.edit { prefs ->
        prefs[PRESETS_KEY] = serializedSet
        Log.d("DataStore", "Saved ${serializedSet.size} presets")
    }
}

/**
 * Read the Flow<List<PresetCycle>> from DataStore.
 * If nothing is stored yet, returns emptyList().
 */
fun readAllPresetCycles(context: Context): Flow<List<PresetCycle>> =
    context.dataStore.data
        .map { prefs ->
            val storedSet: Set<String>? = prefs[PRESETS_KEY]
            if (storedSet == null || storedSet.isEmpty()) {
                emptyList()
            } else {
                storedSet.mapNotNull { serialized ->
                    // serialized form = "name::step1|step2|step3"
                    val parts = serialized.split("::", limit = 2)
                    if (parts.size != 2) return@mapNotNull null
                    val name = parts[0]
                    val stepsList = if (parts[1].isBlank()) {
                        emptyList()
                    } else {
                        parts[1].split("|")
                    }
                    PresetCycle(name = name, steps = stepsList)
                }
            }
        }
