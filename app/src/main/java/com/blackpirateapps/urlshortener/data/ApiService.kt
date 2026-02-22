package com.blackpirateapps.urlshortener.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * POST /api/shorten
     */
    suspend fun shortenUrl(
        baseUrl: String,
        password: String,
        url: String,
        hostname: String,
        slug: String? = null,
        linkPassword: String? = null
    ): Result<String> = apiCall {
        val body = JSONObject().apply {
            put("url", url)
            put("hostname", hostname)
            slug?.let { put("slug", it) }
            linkPassword?.let { put("password", it) }
        }
        val request = buildRequest(baseUrl, "/api/shorten", password)
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val error = try { JSONObject(responseBody).optString("error", "Request failed") } catch (_: Exception) { "Request failed (${response.code})" }
            throw ApiException(response.code, error)
        }

        JSONObject(responseBody).getString("shortUrl")
    }

    /**
     * GET /api/links
     */
    suspend fun getLinks(baseUrl: String, password: String): Result<List<LinkItem>> = apiCall {
        val request = buildRequest(baseUrl, "/api/links", password)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val error = try { JSONObject(responseBody).optString("error", "Request failed") } catch (_: Exception) { "Request failed (${response.code})" }
            throw ApiException(response.code, error)
        }

        val array = JSONArray(responseBody)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            LinkItem(
                slug = obj.getString("slug"),
                url = obj.getString("url"),
                hostname = obj.getString("hostname"),
                hasPassword = !obj.isNull("password"),
                clickCount = obj.optInt("click_count", 0),
                createdAt = obj.optString("created_at", "")
            )
        }
    }

    /**
     * DELETE /api/links
     */
    suspend fun deleteLink(baseUrl: String, password: String, slug: String): Result<Unit> = apiCall {
        val body = JSONObject().apply { put("slug", slug) }
        val request = buildRequest(baseUrl, "/api/links", password)
            .delete(body.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val error = try { JSONObject(responseBody).optString("error", "Request failed") } catch (_: Exception) { "Request failed (${response.code})" }
            throw ApiException(response.code, error)
        }
    }

    /**
     * GET /api/domains
     */
    suspend fun getDomains(baseUrl: String, password: String): Result<List<String>> = apiCall {
        val request = buildRequest(baseUrl, "/api/domains", password)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val error = try { JSONObject(responseBody).optString("error", "Request failed") } catch (_: Exception) { "Request failed (${response.code})" }
            throw ApiException(response.code, error)
        }

        val array = JSONArray(responseBody)
        (0 until array.length()).map { i ->
            array.getJSONObject(i).getString("hostname")
        }
    }

    /**
     * GET /api/link-details?slug=...
     */
    suspend fun getLinkDetails(baseUrl: String, password: String, slug: String): Result<List<ClickAnalytics>> = apiCall {
        val url = baseUrl.trimEnd('/') + "/api/link-details?slug=$slug"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $password")
            .addHeader("Content-Type", "application/json")
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val error = try { JSONObject(responseBody).optString("error", "Request failed") } catch (_: Exception) { "Request failed (${response.code})" }
            throw ApiException(response.code, error)
        }

        val obj = JSONObject(responseBody)
        val clicksArray = obj.optJSONArray("clicks") ?: JSONArray()
        (0 until clicksArray.length()).map { i ->
            val click = clicksArray.getJSONObject(i)
            ClickAnalytics(
                clickedAt = click.optString("clicked_at", ""),
                userAgent = click.optString("user_agent", ""),
                referrer = click.optString("referrer", ""),
                ipAddress = click.optString("ip_address", "")
            )
        }
    }

    /**
     * Test connection by attempting to fetch domains
     */
    suspend fun testConnection(baseUrl: String, password: String): Result<Boolean> = apiCall {
        val result = getDomains(baseUrl, password)
        result.getOrThrow()
        true
    }

    private fun buildRequest(baseUrl: String, path: String, password: String): Request.Builder {
        val url = baseUrl.trimEnd('/') + path
        return Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $password")
            .addHeader("Content-Type", "application/json")
    }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(block())
            } catch (e: ApiException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(ApiException(0, e.message ?: "Network error"))
            }
        }
    }
}

data class LinkItem(
    val slug: String,
    val url: String,
    val hostname: String,
    val hasPassword: Boolean,
    val clickCount: Int,
    val createdAt: String
)

data class ClickAnalytics(
    val clickedAt: String,
    val userAgent: String,
    val referrer: String,
    val ipAddress: String
)

class ApiException(val code: Int, message: String) : Exception(message)
