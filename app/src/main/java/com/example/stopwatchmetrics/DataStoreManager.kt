// DataStoreManager.kt
package com.example.stopwatchmetrics

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json



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

data class FastCommentsSettings(
    val enabled: Boolean = false,
    val comments: List<String> = listOf(
        "In",
        "Transport",
        "Out",
        "Return",
        "Wait In",
        "Wait Out"
    )
)

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

/* --------------  PRESET CYCLES -------------- */

@Serializable
data class PresetCycle(val name: String, val steps: List<String>)

/** ONE key – it holds a Set<String> */
private val PRESET_CYCLES_KEY = stringSetPreferencesKey("preset_cycles")

private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

/** Save the whole list */
suspend fun saveAllPresetCycles(ctx: Context, list: List<PresetCycle>) {
    val set: Set<String> = list.map { json.encodeToString(it) }.toSet()
    ctx.dataStore.edit { it[PRESET_CYCLES_KEY] = set }
}

/** Load the list (Flow) */
fun readAllPresetCycles(ctx: Context): Flow<List<PresetCycle>> =
    ctx.dataStore.data.map { prefs ->
        prefs[PRESET_CYCLES_KEY]
            ?.mapNotNull { runCatching { json.decodeFromString<PresetCycle>(it) }.getOrNull() }
            ?: emptyList()
    }


/* ────────────────────────────────────────────── *
 *  PRESET‑CYCLE  JSON  IMPORT / EXPORT HELPERS   *
 * ────────────────────────────────────────────── */

private val jsonPretty = Json { prettyPrint = true }

fun List<PresetCycle>.toJsonBytes(): ByteArray =
    jsonPretty.encodeToString(this).encodeToByteArray()

fun decodePresetCycles(bytes: ByteArray): List<PresetCycle>? =
    runCatching { jsonPretty.decodeFromString<List<PresetCycle>>(bytes.decodeToString()) }
        .getOrNull()