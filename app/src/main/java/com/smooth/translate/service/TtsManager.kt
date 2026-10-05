package com.smooth.translate.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun speak(text: String, langCode: String) {
        if (!isInitialized || text.isBlank()) return
        val locale = getLocaleForCode(langCode)
        tts?.language = locale
        val utteranceId = System.currentTimeMillis().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun getLocaleForCode(code: String): Locale {
        return when (code.lowercase()) {
            "en" -> Locale.US
            "vi" -> Locale("vi", "VN")
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRANCE
            "de" -> Locale.GERMANY
            "ja" -> Locale.JAPAN
            "ko" -> Locale.KOREA
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "it" -> Locale.ITALY
            "pt" -> Locale("pt", "PT")
            "ru" -> Locale("ru", "RU")
            "ar" -> Locale("ar", "SA")
            "hi" -> Locale("hi", "IN")
            "th" -> Locale("th", "TH")
            "id" -> Locale("id", "ID")
            "tr" -> Locale("tr", "TR")
            else -> Locale(code)
        }
    }
}
