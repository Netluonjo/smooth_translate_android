package com.smooth.translate.data.repository

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class TranslationRepository {

    private val modelManager = RemoteModelManager.getInstance()
    private val translatorCache = ConcurrentHashMap<String, Translator>()

    private val _downloadedModels = MutableStateFlow<Set<String>>(emptySet())
    val downloadedModels: StateFlow<Set<String>> = _downloadedModels.asStateFlow()

    private val _downloadingModels = MutableStateFlow<Set<String>>(emptySet())
    val downloadingModels: StateFlow<Set<String>> = _downloadingModels.asStateFlow()

    suspend fun refreshDownloadedModels() = withContext(Dispatchers.IO) {
        try {
            val models = modelManager.getDownloadedModels(TranslateRemoteModel::class.java).await()
            val codes = models.map { it.language }.toSet()
            _downloadedModels.value = codes
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun isModelDownloaded(langCode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val model = TranslateRemoteModel.Builder(langCode).build()
            modelManager.isModelDownloaded(model).await()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun downloadModel(langCode: String, requireWifi: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _downloadingModels.value = _downloadingModels.value + langCode
            val model = TranslateRemoteModel.Builder(langCode).build()
            val conditionsBuilder = DownloadConditions.Builder()
            if (requireWifi) {
                conditionsBuilder.requireWifi()
            }
            modelManager.download(model, conditionsBuilder.build()).await()
            refreshDownloadedModels()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _downloadingModels.value = _downloadingModels.value - langCode
        }
    }

    suspend fun deleteModel(langCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val model = TranslateRemoteModel.Builder(langCode).build()
            modelManager.deleteDownloadedModel(model).await()
            // clear cached translator for this language
            translatorCache.entries.removeIf { it.key.contains(langCode) }
            refreshDownloadedModels()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        requireWifiForDownload: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext Result.success("")
        if (sourceLang == targetLang) return@withContext Result.success(text)

        try {
            val pairKey = "${sourceLang}_$targetLang"
            val translator = translatorCache.getOrPut(pairKey) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build()
                Translation.getClient(options)
            }

            val conditions = DownloadConditions.Builder().apply {
                if (requireWifiForDownload) requireWifi()
            }.build()

            // Download model if not yet available
            translator.downloadModelIfNeeded(conditions).await()
            refreshDownloadedModels()

            val translated = translator.translate(text).await()
            Result.success(translated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearCache() {
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
    }
}
