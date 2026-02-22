package com.blackpirateapps.urlshortener

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.blackpirateapps.urlshortener.data.ApiService
import com.blackpirateapps.urlshortener.data.PreferencesManager
import com.blackpirateapps.urlshortener.data.UrlShortenerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Transparent activity that receives shared URLs, shortens them in the background,
 * copies the result to clipboard, shows a toast, and finishes without showing any UI.
 */
class ShareReceiverActivity : ComponentActivity() {

    private val repository = UrlShortenerRepository(ApiService())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                handleSharedUrl(sharedText)
            } else {
                Toast.makeText(this, "No URL received", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            finish()
        }
    }

    private fun handleSharedUrl(rawText: String) {
        // Extract URL from shared text (some apps share "Title - URL" format)
        val url = extractUrl(rawText)

        if (url == null) {
            Toast.makeText(this, "No valid URL found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val preferencesManager = PreferencesManager(applicationContext)

        CoroutineScope(Dispatchers.Main).launch {
            val baseUrl = preferencesManager.apiBaseUrl.first()
            val password = preferencesManager.apiPassword.first()
            val hostname = preferencesManager.defaultHostname.first()

            if (baseUrl.isBlank() || password.isBlank()) {
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "Please configure API in BlackPirate URL Shortener settings first",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }

            if (hostname.isBlank()) {
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "Please set a default hostname in settings first",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }

            Toast.makeText(this@ShareReceiverActivity, "Shortening...", Toast.LENGTH_SHORT).show()

            repository.shortenUrl(
                baseUrl = baseUrl,
                password = password,
                url = url,
                hostname = hostname
            ).fold(
                onSuccess = { result ->
                    // Copy shortened URL to clipboard
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Shortened URL", result.shortUrl)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(
                        this@ShareReceiverActivity,
                        "Copied: ${result.shortUrl}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                },
                onFailure = { error ->
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        "Failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            )
        }
    }

    private fun extractUrl(text: String): String? {
        // Try to find a URL pattern in the shared text
        val urlRegex = Regex("https?://[\\S]+")
        val match = urlRegex.find(text)
        return match?.value?.trimEnd('.', ',', ')', ']', '>')
    }
}
