package com.blackpirateapps.urlshortener.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blackpirateapps.urlshortener.data.ApiService
import com.blackpirateapps.urlshortener.data.PreferencesManager
import com.blackpirateapps.urlshortener.data.ShortenedUrl
import com.blackpirateapps.urlshortener.data.UrlShortenerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UrlUiState(
    val inputUrl: String = "",
    val hostname: String = "",
    val shortenedResult: ShortenedUrl? = null,
    val history: List<ShortenedUrl> = emptyList(),
    val domains: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val showCopiedToast: Boolean = false,
    val darkMode: Boolean = false,
    val apiBaseUrl: String = "",
    val apiPassword: String = "",
    val isConfigured: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.NOT_CONFIGURED
)

enum class ConnectionStatus {
    NOT_CONFIGURED,
    TESTING,
    CONNECTED,
    ERROR
}

class UrlViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val repository = UrlShortenerRepository(ApiService())

    private val _uiState = MutableStateFlow(UrlUiState())
    val uiState: StateFlow<UrlUiState> = _uiState.asStateFlow()

    init {
        loadSavedSettings()
    }

    private fun loadSavedSettings() {
        viewModelScope.launch {
            val baseUrl = preferencesManager.apiBaseUrl.first()
            val password = preferencesManager.apiPassword.first()
            val hostname = preferencesManager.defaultHostname.first()
            val darkMode = preferencesManager.darkMode.first()
            val isConfigured = baseUrl.isNotBlank() && password.isNotBlank()

            _uiState.value = _uiState.value.copy(
                apiBaseUrl = baseUrl,
                apiPassword = password,
                hostname = hostname,
                darkMode = darkMode,
                isConfigured = isConfigured,
                connectionStatus = if (isConfigured) ConnectionStatus.CONNECTED else ConnectionStatus.NOT_CONFIGURED
            )

            if (isConfigured) {
                fetchDomains()
                fetchLinks()
            }
        }
    }

    fun updateInputUrl(url: String) {
        _uiState.value = _uiState.value.copy(inputUrl = url, error = null)
    }

    fun updateHostname(hostname: String) {
        _uiState.value = _uiState.value.copy(hostname = hostname)
        viewModelScope.launch {
            preferencesManager.setDefaultHostname(hostname)
        }
    }

    fun shortenUrl() {
        val state = _uiState.value
        val url = state.inputUrl.trim()

        if (url.isEmpty()) {
            _uiState.value = state.copy(error = "Please enter a URL")
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _uiState.value = state.copy(error = "URL must start with http:// or https://")
            return
        }
        if (!state.isConfigured) {
            _uiState.value = state.copy(error = "Configure API URL and password in Settings first")
            return
        }
        if (state.hostname.isBlank()) {
            _uiState.value = state.copy(error = "Please enter or select a hostname")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.shortenUrl(
                baseUrl = state.apiBaseUrl,
                password = state.apiPassword,
                url = url,
                hostname = state.hostname
            ).fold(
                onSuccess = { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shortenedResult = result,
                        inputUrl = ""
                    )
                    // Refresh the links list
                    fetchLinks()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to shorten URL"
                    )
                }
            )
        }
    }

    fun fetchLinks() {
        val state = _uiState.value
        if (!state.isConfigured) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            repository.getLinks(state.apiBaseUrl, state.apiPassword).fold(
                onSuccess = { links ->
                    _uiState.value = _uiState.value.copy(
                        history = links,
                        isRefreshing = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
            )
        }
    }

    private fun fetchDomains() {
        val state = _uiState.value
        if (!state.isConfigured) return

        viewModelScope.launch {
            repository.getDomains(state.apiBaseUrl, state.apiPassword).fold(
                onSuccess = { domains ->
                    _uiState.value = _uiState.value.copy(domains = domains)
                    // If no hostname is set and domains exist, use the first one
                    if (_uiState.value.hostname.isBlank() && domains.isNotEmpty()) {
                        updateHostname(domains.first())
                    }
                },
                onFailure = { /* silently fail */ }
            )
        }
    }

    fun deleteFromHistory(slug: String) {
        val state = _uiState.value
        if (!state.isConfigured) return

        viewModelScope.launch {
            repository.deleteLink(state.apiBaseUrl, state.apiPassword, slug).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        history = _uiState.value.history.filter { it.slug != slug }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete link"
                    )
                }
            )
        }
    }

    fun clearHistory() {
        // Delete all links one by one from API
        val state = _uiState.value
        if (!state.isConfigured) return

        viewModelScope.launch {
            state.history.forEach { link ->
                repository.deleteLink(state.apiBaseUrl, state.apiPassword, link.slug)
            }
            _uiState.value = _uiState.value.copy(history = emptyList())
        }
    }

    fun showCopiedToast() {
        _uiState.value = _uiState.value.copy(showCopiedToast = true)
        viewModelScope.launch {
            delay(2000)
            _uiState.value = _uiState.value.copy(showCopiedToast = false)
        }
    }

    fun toggleDarkMode() {
        val newDarkMode = !_uiState.value.darkMode
        _uiState.value = _uiState.value.copy(darkMode = newDarkMode)
        viewModelScope.launch {
            preferencesManager.setDarkMode(newDarkMode)
        }
    }

    fun updateApiBaseUrl(url: String) {
        val trimmed = url.trim().trimEnd('/')
        _uiState.value = _uiState.value.copy(
            apiBaseUrl = trimmed,
            isConfigured = trimmed.isNotBlank() && _uiState.value.apiPassword.isNotBlank(),
            connectionStatus = ConnectionStatus.NOT_CONFIGURED
        )
        viewModelScope.launch {
            preferencesManager.setApiBaseUrl(trimmed)
        }
    }

    fun updateApiPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            apiPassword = password,
            isConfigured = _uiState.value.apiBaseUrl.isNotBlank() && password.isNotBlank(),
            connectionStatus = ConnectionStatus.NOT_CONFIGURED
        )
        viewModelScope.launch {
            preferencesManager.setApiPassword(password)
        }
    }

    fun testConnection() {
        val state = _uiState.value
        if (state.apiBaseUrl.isBlank() || state.apiPassword.isBlank()) {
            _uiState.value = state.copy(
                error = "Please enter both API URL and password",
                connectionStatus = ConnectionStatus.NOT_CONFIGURED
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.TESTING)

            repository.testConnection(state.apiBaseUrl, state.apiPassword).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        connectionStatus = ConnectionStatus.CONNECTED,
                        isConfigured = true
                    )
                    fetchDomains()
                    fetchLinks()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        connectionStatus = ConnectionStatus.ERROR,
                        error = error.message ?: "Connection failed"
                    )
                }
            )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(shortenedResult = null)
    }
}
