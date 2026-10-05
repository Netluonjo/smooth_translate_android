package com.smooth.translate.data.model

import java.util.UUID

data class TranslationRecord(
    val id: String = UUID.randomUUID().toString(),
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
