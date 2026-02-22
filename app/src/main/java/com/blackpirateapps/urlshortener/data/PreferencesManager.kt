package com.blackpirateapps.urlshortener.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val API_BASE_URL = stringPreferencesKey("api_base_url")
        private val API_PASSWORD = stringPreferencesKey("api_password")
        private val DEFAULT_HOSTNAME = stringPreferencesKey("default_hostname")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val apiBaseUrl: Flow<String> = context.dataStore.data.map { it[API_BASE_URL] ?: "" }
    val apiPassword: Flow<String> = context.dataStore.data.map { it[API_PASSWORD] ?: "" }
    val defaultHostname: Flow<String> = context.dataStore.data.map { it[DEFAULT_HOSTNAME] ?: "" }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }

    suspend fun setApiBaseUrl(url: String) {
        context.dataStore.edit { it[API_BASE_URL] = url }
    }

    suspend fun setApiPassword(password: String) {
        context.dataStore.edit { it[API_PASSWORD] = password }
    }

    suspend fun setDefaultHostname(hostname: String) {
        context.dataStore.edit { it[DEFAULT_HOSTNAME] = hostname }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }
}
