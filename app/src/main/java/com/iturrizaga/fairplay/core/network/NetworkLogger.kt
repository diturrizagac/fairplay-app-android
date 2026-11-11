package com.iturrizaga.fairplay.core.network

import android.util.Log
import com.iturrizaga.fairplay.BuildConfig
import org.json.JSONObject

private const val TAG = "NetworkLogger"
private val SENSITIVE_KEYS = setOf("password", "confirm_password")

object NetworkLogger {

    fun logRequest(method: String, url: String, headers: Map<String, String>, body: String?) {
        if (!BuildConfig.DEBUG) return

        val sb = StringBuilder("→ $method $url\n")

        headers
            .filterKeys { it != "Authorization" }
            .forEach { (k, v) -> sb.append("  $k: $v\n") }

        if (body != null) {
            sb.append(prettyJson(sanitize(body)))
        }

        Log.d(TAG, sb.toString().trimEnd())
    }

    fun logResponse(statusCode: Int, url: String, body: String) {
        if (!BuildConfig.DEBUG) return

        val sb = StringBuilder("← $statusCode $url\n")
        if (body.isNotEmpty()) sb.append(prettyJson(body))

        Log.d(TAG, sb.toString().trimEnd())
    }

    private fun sanitize(body: String): String {
        return try {
            val json = JSONObject(body)
            SENSITIVE_KEYS.forEach { key -> if (json.has(key)) json.put(key, "***") }
            json.toString()
        } catch (_: Exception) {
            body
        }
    }

    private fun prettyJson(raw: String): String {
        return try {
            JSONObject(raw).toString(2)
        } catch (_: Exception) {
            raw
        }
    }
}
