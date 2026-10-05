package com.smooth.translate.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smooth.translate.data.model.TranslationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smooth_translate_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _history = MutableStateFlow<List<TranslationRecord>>(emptyList())
    val history: StateFlow<List<TranslationRecord>> = _history.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val json = prefs.getString(KEY_HISTORY, null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<TranslationRecord>>() {}.type
                val list: List<TranslationRecord> = gson.fromJson(json, type) ?: emptyList()
                _history.value = list
            } catch (e: Exception) {
                _history.value = emptyList()
            }
        }
    }

    private fun saveHistory(list: List<TranslationRecord>) {
        _history.value = list
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }

    fun addRecord(record: TranslationRecord) {
        val current = _history.value.toMutableList()
        // remove existing duplicate if same text & languages
        current.removeAll {
            it.sourceText.trim() == record.sourceText.trim() &&
                    it.sourceLang == record.sourceLang &&
                    it.targetLang == record.targetLang
        }
        current.add(0, record)
        if (current.size > MAX_HISTORY) {
            current.removeAt(current.lastIndex)
        }
        saveHistory(current)
    }

    fun toggleFavorite(id: String) {
        val current = _history.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        saveHistory(current)
    }

    fun deleteRecord(id: String) {
        val current = _history.value.filter { it.id != id }
        saveHistory(current)
    }

    fun clearHistory() {
        saveHistory(emptyList())
    }

    var autoSpeak: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SPEAK, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SPEAK, value).apply()

    var wifiOnlyDownload: Boolean
        get() = prefs.getBoolean(KEY_WIFI_ONLY, false)
        set(value) = prefs.edit().putBoolean(KEY_WIFI_ONLY, value).apply()

    companion object {
        private const val KEY_HISTORY = "key_translation_history"
        private const val KEY_AUTO_SPEAK = "key_auto_speak"
        private const val KEY_WIFI_ONLY = "key_wifi_only"
        private const val MAX_HISTORY = 100
    }
}
