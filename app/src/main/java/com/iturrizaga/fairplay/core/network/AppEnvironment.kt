package com.iturrizaga.fairplay.core.network

enum class AppEnvironment(val baseUrl: String) {
    LOCAL("http://10.0.2.2:8000/api/v1"),
    DEV("https://api.dev.fairplay.com/api/v1"),
    PROD("https://api.fairplay.com/api/v1");

    companion object {
        val current: AppEnvironment = LOCAL
    }
}
