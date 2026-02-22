package com.blackpirateapps.urlshortener.data

import kotlinx.coroutines.delay

data class ShortenedUrl(
    val id: String,
    val originalUrl: String,
    val shortUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)

class UrlShortenerRepository {

    private val history = mutableListOf<ShortenedUrl>()
    private var counter = 0

    /**
     * Stub implementation — returns a mock shortened URL.
     * Replace this with actual API call when backend is ready.
     */
    suspend fun shortenUrl(url: String): Result<ShortenedUrl> {
        return try {
            // Simulate network delay
            delay(800)

            counter++
            val shortened = ShortenedUrl(
                id = counter.toString(),
                originalUrl = url,
                shortUrl = "https://bprt.link/${generateShortCode()}"
            )
            history.add(0, shortened)
            Result.success(shortened)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getHistory(): List<ShortenedUrl> = history.toList()

    fun deleteFromHistory(id: String) {
        history.removeAll { it.id == id }
    }

    fun clearHistory() {
        history.clear()
    }

    private fun generateShortCode(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
