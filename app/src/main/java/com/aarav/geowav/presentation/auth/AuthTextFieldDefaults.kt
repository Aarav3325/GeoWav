package com.aarav.geowav.presentation.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorLabelColor = MaterialTheme.colorScheme.error,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorLeadingIconColor = MaterialTheme.colorScheme.error,
    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorTrailingIconColor = MaterialTheme.colorScheme.error,
    cursorColor = MaterialTheme.colorScheme.primary,
    errorCursorColor = MaterialTheme.colorScheme.error
)
