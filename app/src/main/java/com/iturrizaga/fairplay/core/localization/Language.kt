package com.iturrizaga.fairplay.core.localization

import java.util.Locale

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español");

    val locale: Locale get() = Locale(code)

    companion object {
        fun fromCode(code: String): Language =
            entries.firstOrNull { it.code == code } ?: ENGLISH

        val system: Language
            get() = fromCode(Locale.getDefault().language)
    }
}
