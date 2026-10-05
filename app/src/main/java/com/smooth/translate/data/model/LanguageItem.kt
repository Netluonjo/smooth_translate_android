package com.smooth.translate.data.model

import com.google.mlkit.nl.translate.TranslateLanguage

data class LanguageItem(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f
)

object LanguageCatalog {
    val ALL_LANGUAGES = listOf(
        LanguageItem(TranslateLanguage.ENGLISH, "English", "English", "🇺🇸"),
        LanguageItem(TranslateLanguage.VIETNAMESE, "Vietnamese", "Tiếng Việt", "🇻🇳"),
        LanguageItem(TranslateLanguage.SPANISH, "Spanish", "Español", "🇪🇸"),
        LanguageItem(TranslateLanguage.FRENCH, "French", "Français", "🇫🇷"),
        LanguageItem(TranslateLanguage.GERMAN, "German", "Deutsch", "🇩🇪"),
        LanguageItem(TranslateLanguage.JAPANESE, "Japanese", "日本語", "🇯🇵"),
        LanguageItem(TranslateLanguage.KOREAN, "Korean", "한국어", "🇰🇷"),
        LanguageItem(TranslateLanguage.CHINESE, "Chinese", "中文", "🇨🇳"),
        LanguageItem(TranslateLanguage.ITALIAN, "Italian", "Italiano", "🇮🇹"),
        LanguageItem(TranslateLanguage.PORTUGUESE, "Portuguese", "Português", "🇵🇹"),
        LanguageItem(TranslateLanguage.RUSSIAN, "Russian", "Русский", "🇷🇺"),
        LanguageItem(TranslateLanguage.ARABIC, "Arabic", "العربية", "🇸🇦"),
        LanguageItem(TranslateLanguage.HINDI, "Hindi", "हिन्दी", "🇮🇳"),
        LanguageItem(TranslateLanguage.INDONESIAN, "Indonesian", "Bahasa Indonesia", "🇮🇩"),
        LanguageItem(TranslateLanguage.THAI, "Thai", "ไทย", "🇹🇭"),
        LanguageItem(TranslateLanguage.TURKISH, "Turkish", "Türkçe", "🇹🇷"),
        LanguageItem(TranslateLanguage.POLISH, "Polish", "Polski", "🇵🇱"),
        LanguageItem(TranslateLanguage.DUTCH, "Dutch", "Nederlands", "🇳🇱"),
        LanguageItem(TranslateLanguage.SWEDISH, "Swedish", "Svenska", "🇸🇪"),
        LanguageItem(TranslateLanguage.GREEK, "Greek", "Ελληνικά", "🇬🇷"),
        LanguageItem(TranslateLanguage.CZECH, "Czech", "Čeština", "🇨🇿"),
        LanguageItem(TranslateLanguage.DANISH, "Danish", "Dansk", "🇩🇰"),
        LanguageItem(TranslateLanguage.FINNISH, "Finnish", "Suomi", "🇫🇮"),
        LanguageItem(TranslateLanguage.NORWEGIAN, "Norwegian", "Norsk", "🇳🇴"),
        LanguageItem(TranslateLanguage.UKRAINIAN, "Ukrainian", "Українська", "🇺🇦"),
        LanguageItem(TranslateLanguage.HEBREW, "Hebrew", "עברית", "🇮🇱"),
        LanguageItem(TranslateLanguage.HUNGARIAN, "Hungarian", "Magyar", "🇭🇺"),
        LanguageItem(TranslateLanguage.ROMANIAN, "Romanian", "Română", "🇷🇴"),
        LanguageItem(TranslateLanguage.SLOVAK, "Slovak", "Slovenčina", "🇸🇰"),
        LanguageItem(TranslateLanguage.CATALAN, "Catalan", "Català", "🇦🇩"),
        LanguageItem(TranslateLanguage.CROATIAN, "Croatian", "Hrvatski", "🇭🇷"),
        LanguageItem(TranslateLanguage.MALAY, "Malay", "Bahasa Melayu", "🇲🇾"),
        LanguageItem(TranslateLanguage.TAGALOG, "Tagalog", "Filipino", "🇵🇭"),
        LanguageItem(TranslateLanguage.BENGALI, "Bengali", "বাংলা", "🇧🇩"),
        LanguageItem(TranslateLanguage.URDU, "Urdu", "اردو", "🇵🇰"),
        LanguageItem(TranslateLanguage.PERSIAN, "Persian", "فارسی", "🇮🇷")
    )

    fun getByCode(code: String): LanguageItem {
        return ALL_LANGUAGES.find { it.code.equals(code, ignoreCase = true) }
            ?: LanguageItem(code, code.uppercase(), code.uppercase(), "🌐")
    }

    const val DEFAULT_SOURCE_CODE = TranslateLanguage.ENGLISH
    const val DEFAULT_TARGET_CODE = TranslateLanguage.VIETNAMESE
}
