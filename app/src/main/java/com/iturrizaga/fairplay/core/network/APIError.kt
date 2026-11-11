package com.iturrizaga.fairplay.core.network

sealed class APIError(message: String) : Exception(message) {
    class Unauthorized : APIError("Sesión expirada. Inicia sesión nuevamente.")
    class NotFound : APIError("El recurso no fue encontrado.")
    class Conflict(message: String) : APIError(message)
    class ServerError(code: Int) : APIError("Error del servidor ($code).")
    class NetworkFailure(cause: Exception) : APIError(cause.message ?: "Error de red.")
    class DecodingFailed : APIError("Error al procesar la respuesta del servidor.")
}
