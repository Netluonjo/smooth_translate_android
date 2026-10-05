package com.smooth.translate

import android.app.Application
import com.smooth.translate.data.repository.HistoryRepository
import com.smooth.translate.data.repository.TranslationRepository
import com.smooth.translate.service.SpeechRecognizerHelper
import com.smooth.translate.service.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmoothTranslateApp : Application() {

    lateinit var translationRepository: TranslationRepository
        private set

    lateinit var historyRepository: HistoryRepository
        private set

    lateinit var ttsManager: TtsManager
        private set

    lateinit var speechRecognizerHelper: SpeechRecognizerHelper
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this

        translationRepository = TranslationRepository()
        historyRepository = HistoryRepository(this)
        ttsManager = TtsManager(this)
        speechRecognizerHelper = SpeechRecognizerHelper(this)

        applicationScope.launch {
            translationRepository.refreshDownloadedModels()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        ttsManager.shutdown()
        translationRepository.clearCache()
    }

    companion object {
        lateinit var instance: SmoothTranslateApp
            private set
    }
}
