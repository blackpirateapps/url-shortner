package com.blackpirateapps.urlshortener.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackpirateapps.urlshortener.data.ShortenedUrl
import com.blackpirateapps.urlshortener.data.UrlShortenerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UrlUiState(
    val inputUrl: String = "",
    val shortenedResult: ShortenedUrl? = null,
    val history: List<ShortenedUrl> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCopiedToast: Boolean = false,
    val darkMode: Boolean = false,
    val apiBaseUrl: String = "https://api.example.com"
)

class UrlViewModel : ViewModel() {

    private val repository = UrlShortenerRepository()

    private val _uiState = MutableStateFlow(UrlUiState())
    val uiState: StateFlow<UrlUiState> = _uiState.asStateFlow()

    fun updateInputUrl(url: String) {
        _uiState.value = _uiState.value.copy(inputUrl = url, error = null)
    }

    fun shortenUrl() {
        val url = _uiState.value.inputUrl.trim()
        if (url.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a URL")
            return
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _uiState.value = _uiState.value.copy(error = "URL must start with http:// or https://")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.shortenUrl(url).fold(
                onSuccess = { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shortenedResult = result,
                        history = repository.getHistory(),
                        inputUrl = ""
                    )
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

    fun deleteFromHistory(id: String) {
        repository.deleteFromHistory(id)
        _uiState.value = _uiState.value.copy(history = repository.getHistory())
    }

    fun clearHistory() {
        repository.clearHistory()
        _uiState.value = _uiState.value.copy(history = emptyList())
    }

    fun showCopiedToast() {
        _uiState.value = _uiState.value.copy(showCopiedToast = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(showCopiedToast = false)
        }
    }

    fun toggleDarkMode() {
        _uiState.value = _uiState.value.copy(darkMode = !_uiState.value.darkMode)
    }

    fun updateApiBaseUrl(url: String) {
        _uiState.value = _uiState.value.copy(apiBaseUrl = url)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(shortenedResult = null)
    }
}
