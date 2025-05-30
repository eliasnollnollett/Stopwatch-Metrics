package com.example.stopwatchmetrics

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log
import androidx.datastore.preferences.core.stringSetPreferencesKey

// Extension property on Context—place this at the file level.
val Context.dataStore by preferencesDataStore(name = "settings")
// Time format key (already exists)
val TIME_FORMAT_KEY = booleanPreferencesKey("time_format_setting")



// Data class along with companion object for loading from DataStore.
data class TimeFormatSetting(
    val useShortFormat: Boolean = false  // default to false
)

// DataStore‐keys.kt  (or wherever you keep the other keys)
val SHOW_START_TIPS = booleanPreferencesKey("show_start_tips")

suspend fun saveShowTips(context: Context, value: Boolean) {
    context.dataStore.edit { it[SHOW_START_TIPS] = value }
}

fun readShowTips(context: Context) = context.dataStore.data
    .map { prefs -> prefs[SHOW_START_TIPS] ?: true }   // default = true

private val SHOW_POINT_KEY                = booleanPreferencesKey("show_point")
private val SHOW_TIME_KEY                 = booleanPreferencesKey("show_time")
private val SHOW_TMU_KEY                  = booleanPreferencesKey("show_tmu")
private val SHOW_START_TIME_KEY           = booleanPreferencesKey("show_start_time")
private val SHOW_COMMENT_KEY              = booleanPreferencesKey("show_comment")
private val SHOW_IMAGE_KEY                = booleanPreferencesKey("show_image")
//private val SHOW_EMPTY_IMAGE_KEY          = booleanPreferencesKey("show_empty_image")
//private val SHOW_INSTRUCTIONS_KEY         = booleanPreferencesKey("show_instructions")
//private val SHOW_GRAPH_KEY                = booleanPreferencesKey("show_graph")

private val FAST_COMMENTS_ENABLED_KEY = booleanPreferencesKey("fast_comments_enabled")
private val FAST_COMMENTS_LIST_KEY    = stringSetPreferencesKey("fast_comments_list")

// --- Fast Comments Keys ---

val FAST_COMMENT_1_KEY = stringPreferencesKey("fast_comment_1")
val FAST_COMMENT_2_KEY = stringPreferencesKey("fast_comment_2")
val FAST_COMMENT_3_KEY = stringPreferencesKey("fast_comment_3")
val FAST_COMMENT_4_KEY = stringPreferencesKey("fast_comment_4")
val FAST_COMMENT_5_KEY = stringPreferencesKey("fast_comment_5")
val FAST_COMMENT_6_KEY = stringPreferencesKey("fast_comment_6")



// Save the time format setting.
suspend fun saveTimeFormatSetting(context: Context, useShortFormat: Boolean) {
    context.dataStore.edit { settings ->
        settings[TIME_FORMAT_KEY] = useShortFormat
        Log.d("DataStore", "Time format setting saved: $useShortFormat")
    }
}

// Read the time format setting as a Flow.
fun readTimeFormatSetting(context: Context): Flow<Boolean> {
    return context.dataStore.data.map { preferences ->
        val value = preferences[TIME_FORMAT_KEY] ?: false
        Log.d("DataStore", "Loaded time format setting: $value")
        value
    }
}

// Save Fast Comments Settings.
suspend fun saveFastCommentsSettings(context: Context, settings: FastCommentsSettings) {
    context.dataStore.edit { prefs ->
        prefs[FAST_COMMENTS_ENABLED_KEY] = settings.enabled
        prefs[FAST_COMMENTS_LIST_KEY]    = settings.comments.toSet()
    }
}

fun readFastCommentsSettings(context: Context): Flow<FastCommentsSettings> =
    context.dataStore.data
        .map { prefs ->
            val enabled = prefs[FAST_COMMENTS_ENABLED_KEY] ?: false
            // if user never saved, fall back to the six defaults
            val savedSet = prefs[FAST_COMMENTS_LIST_KEY]
            val list     = savedSet?.toList()
                ?: FastCommentsSettings().comments
            FastCommentsSettings(enabled = enabled, comments = list)
        }

// Save SheetSettings.
suspend fun saveSheetSettings(context: Context, sheetSettings: SheetSettings) {
    context.dataStore.edit { prefs ->
        prefs[SHOW_POINT_KEY]         = sheetSettings.showPoint
        prefs[SHOW_TIME_KEY]          = sheetSettings.showTime
        prefs[SHOW_TMU_KEY]           = sheetSettings.showTMU
        prefs[SHOW_START_TIME_KEY]    = sheetSettings.showStartTime
        prefs[SHOW_COMMENT_KEY]       = sheetSettings.showComment
        prefs[SHOW_IMAGE_KEY]         = sheetSettings.showImage

        // replaced hide… with showEmpty…
       // prefs[SHOW_EMPTY_COMMENT_KEY] = sheetSettings.showEmptyComment
       // prefs[SHOW_EMPTY_IMAGE_KEY]   = sheetSettings.showEmptyImage

        // replaced hideInstructions with showInstructions
      //  prefs[SHOW_INSTRUCTIONS_KEY]  = sheetSettings.showInstructions

      //  prefs[SHOW_GRAPH_KEY]         = sheetSettings.showGraph
    }
}


// Read SheetSettings as a Flow.
fun readSheetSettings(context: Context): Flow<SheetSettings> =
    context.dataStore.data
        .map { prefs ->
            SheetSettings(
                showPoint         = prefs[SHOW_POINT_KEY]         ?: true,
                showTime          = prefs[SHOW_TIME_KEY]          ?: true,
                showTMU           = prefs[SHOW_TMU_KEY]           ?: false,
                showStartTime     = prefs[SHOW_START_TIME_KEY]    ?: true,
                showComment       = prefs[SHOW_COMMENT_KEY]       ?: true,
                showImage         = prefs[SHOW_IMAGE_KEY]         ?: true,

                // new defaults: false means “don’t force‐show empty”
              //  showEmptyComment  = prefs[SHOW_EMPTY_COMMENT_KEY] ?: false,
              //  showEmptyImage    = prefs[SHOW_EMPTY_IMAGE_KEY]   ?: false,
              //  showInstructions  = prefs[SHOW_INSTRUCTIONS_KEY]  ?: true,

              //  showGraph         = prefs[SHOW_GRAPH_KEY]         ?: false
            )
        }

private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")

suspend fun saveDarkModeSetting(context: Context, enabled: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[DARK_MODE_KEY] = enabled
    }
}

fun readDarkModeSetting(context: Context): Flow<Boolean> =
    context.dataStore.data
        .map { prefs -> prefs[DARK_MODE_KEY] ?: true } // default = true (dark)