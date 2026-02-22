package com.blackpirateapps.urlshortener.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.blackpirateapps.urlshortener.ui.components.CupertinoButton
import com.blackpirateapps.urlshortener.ui.components.CupertinoCard
import com.blackpirateapps.urlshortener.ui.components.CupertinoNavigationBar
import com.blackpirateapps.urlshortener.viewmodel.ConnectionStatus
import com.blackpirateapps.urlshortener.viewmodel.UrlViewModel

@Composable
fun SettingsScreen(
    viewModel: UrlViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var showApiUrlDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showHostnameDialog by remember { mutableStateOf(false) }
    var editingValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        CupertinoNavigationBar(title = "Settings")

        Spacer(modifier = Modifier.height(8.dp))

        // API Configuration Section
        Text(
            text = "API CONFIGURATION",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
        )

        CupertinoCard {
            Column {
                SettingsRow(
                    icon = Icons.Outlined.Link,
                    title = "API Base URL",
                    subtitle = uiState.apiBaseUrl.ifBlank { "Not set — tap to configure" },
                    onClick = {
                        editingValue = uiState.apiBaseUrl
                        showApiUrlDialog = true
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 56.dp)
                )
                SettingsRow(
                    icon = Icons.Outlined.Key,
                    title = "API Password",
                    subtitle = if (uiState.apiPassword.isNotBlank()) "••••••••" else "Not set — tap to configure",
                    onClick = {
                        editingValue = uiState.apiPassword
                        showPasswordDialog = true
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 56.dp)
                )
                SettingsRow(
                    icon = Icons.Outlined.Language,
                    title = "Default Hostname",
                    subtitle = uiState.hostname.ifBlank { "Not set — auto-detected from API" },
                    onClick = {
                        editingValue = uiState.hostname
                        showHostnameDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Connection Status + Test Button
        CupertinoCard {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (statusIcon, statusColor, statusText) = when (uiState.connectionStatus) {
                            ConnectionStatus.NOT_CONFIGURED -> Triple(
                                Icons.Outlined.Info,
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                "Not configured"
                            )
                            ConnectionStatus.TESTING -> Triple(
                                Icons.Outlined.Info,
                                MaterialTheme.colorScheme.primary,
                                "Testing..."
                            )
                            ConnectionStatus.CONNECTED -> Triple(
                                Icons.Outlined.CheckCircle,
                                Color(0xFF34C759),
                                "Connected"
                            )
                            ConnectionStatus.ERROR -> Triple(
                                Icons.Outlined.Error,
                                MaterialTheme.colorScheme.error,
                                "Connection failed"
                            )
                        }
                        if (uiState.connectionStatus == ConnectionStatus.TESTING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = statusColor
                            )
                        } else {
                            Icon(
                                statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                CupertinoButton(
                    text = "Test Connection",
                    onClick = { viewModel.testConnection() },
                    enabled = uiState.apiBaseUrl.isNotBlank() && uiState.apiPassword.isNotBlank()
                            && uiState.connectionStatus != ConnectionStatus.TESTING
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Appearance Section
        Text(
            text = "APPEARANCE",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
        )

        CupertinoCard {
            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = "Dark Mode",
                isChecked = uiState.darkMode,
                onToggle = { viewModel.toggleDarkMode() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        Text(
            text = "ABOUT",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
        )

        CupertinoCard {
            Column {
                SettingsInfoRow(title = "Version", subtitle = "1.1.0")
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 16.dp)
                )
                SettingsInfoRow(title = "Developer", subtitle = "BlackPirate Apps")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BlackPirate URL Shortener",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // API URL Dialog
    if (showApiUrlDialog) {
        EditDialog(
            title = "API Base URL",
            value = editingValue,
            onValueChange = { editingValue = it },
            placeholder = "https://your-instance.vercel.app",
            onDismiss = { showApiUrlDialog = false },
            onConfirm = {
                viewModel.updateApiBaseUrl(editingValue)
                showApiUrlDialog = false
            }
        )
    }

    // Password Dialog
    if (showPasswordDialog) {
        EditDialog(
            title = "API Password",
            value = editingValue,
            onValueChange = { editingValue = it },
            placeholder = "Your dashboard password",
            isPassword = true,
            onDismiss = { showPasswordDialog = false },
            onConfirm = {
                viewModel.updateApiPassword(editingValue)
                showPasswordDialog = false
            }
        )
    }

    // Hostname Dialog
    if (showHostnameDialog) {
        EditDialog(
            title = "Default Hostname",
            value = editingValue,
            onValueChange = { editingValue = it },
            placeholder = "short.example.com",
            onDismiss = { showHostnameDialog = false },
            onConfirm = {
                viewModel.updateHostname(editingValue)
                showHostnameDialog = false
            }
        )
    }
}

@Composable
private fun EditDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                },
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Save",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.padding(start = 16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.padding(start = 16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
