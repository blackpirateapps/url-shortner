package com.blackpirateapps.urlshortener.data

data class ShortenedUrl(
    val id: String,
    val slug: String,
    val originalUrl: String,
    val shortUrl: String,
    val hostname: String = "",
    val clickCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

class UrlShortenerRepository(private val apiService: ApiService) {

    suspend fun shortenUrl(
        baseUrl: String,
        password: String,
        url: String,
        hostname: String
    ): Result<ShortenedUrl> {
        val result = apiService.shortenUrl(baseUrl, password, url, hostname)
        return result.map { shortUrl ->
            ShortenedUrl(
                id = shortUrl,
                slug = shortUrl.substringAfterLast("/"),
                originalUrl = url,
                shortUrl = shortUrl,
                hostname = hostname
            )
        }
    }

    suspend fun getLinks(baseUrl: String, password: String): Result<List<ShortenedUrl>> {
        val result = apiService.getLinks(baseUrl, password)
        return result.map { links ->
            links.map { link ->
                ShortenedUrl(
                    id = link.slug,
                    slug = link.slug,
                    originalUrl = link.url,
                    shortUrl = "https://${link.hostname}/${link.slug}",
                    hostname = link.hostname,
                    clickCount = link.clickCount,
                    createdAt = parseTimestamp(link.createdAt)
                )
            }
        }
    }

    suspend fun deleteLink(baseUrl: String, password: String, slug: String): Result<Unit> {
        return apiService.deleteLink(baseUrl, password, slug)
    }

    suspend fun getDomains(baseUrl: String, password: String): Result<List<String>> {
        return apiService.getDomains(baseUrl, password)
    }

    suspend fun testConnection(baseUrl: String, password: String): Result<Boolean> {
        return apiService.testConnection(baseUrl, password)
    }

    suspend fun getLinkDetails(baseUrl: String, password: String, slug: String): Result<List<com.blackpirateapps.urlshortener.data.ClickAnalytics>> {
        return apiService.getLinkDetails(baseUrl, password, slug)
    }

    private fun parseTimestamp(isoString: String): Long {
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(isoString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
