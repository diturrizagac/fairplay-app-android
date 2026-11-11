package com.iturrizaga.fairplay.core.network

import com.iturrizaga.fairplay.BuildConfig

enum class AppEnvironment(val baseUrl: String) {
    LOCAL("http://10.0.2.2:8000/api/v1"),
    DEV("https://fairplay-api-dev-development.up.railway.app/api/v1"),
    PROD("https://fairplay-api-production-01fc.up.railway.app/api/v1");

    companion object {
        /** Release builds always use PROD; see FAIRPLAY_ENV in app/build.gradle.kts. */
        val current: AppEnvironment = fromName(BuildConfig.FAIRPLAY_ENV)

        fun fromName(name: String): AppEnvironment =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: LOCAL
    }
}
