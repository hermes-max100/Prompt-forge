package com.aistudio.promptforge.abcd.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.ErrorSeverity

@Composable
fun ErrorBanner(
    error: AppError?,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onSwitchToLocal: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        if (error == null) return@AnimatedVisibility

        var showDetails by remember { mutableStateOf(false) }

        val isWarning = error.severity == ErrorSeverity.WARNING || error.severity == ErrorSeverity.INFO
        val containerColor = if (isWarning) {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f)
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
        }

        val contentColor = if (isWarning) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }

        val borderColor = if (isWarning) {
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        }

        val icon = when (error.severity) {
            ErrorSeverity.INFO -> Icons.Filled.Info
            ErrorSeverity.WARNING -> Icons.Filled.WarningAmber
            else -> Icons.Filled.ErrorOutline
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .testTag("error_banner"),
            color = containerColor,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            )

                            if (error.httpCode != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = contentColor.copy(alpha = 0.18f),
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Text(
                                        text = "HTTP ${error.httpCode}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            color = contentColor
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = error.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = contentColor.copy(alpha = 0.9f)
                            )
                        )

                        if (!error.suggestedAction.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "💡 Tip: ${error.suggestedAction}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = contentColor.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        if (!error.technicalDetails.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showDetails = !showDetails }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (showDetails) "Hide technical details" else "View technical details",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = contentColor.copy(alpha = 0.75f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(Modifier.width(2.dp))
                                Icon(
                                    if (showDetails) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = contentColor.copy(alpha = 0.75f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            AnimatedVisibility(visible = showDetails) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.25f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = error.technicalDetails,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = contentColor
                                        ),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }

                        // Action Buttons Row (Retry, Local Fallback, Settings, Dismiss)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (onSwitchToLocal != null) {
                                OutlinedButton(
                                    onClick = onSwitchToLocal,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = contentColor
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("error_switch_local_button")
                                ) {
                                    Icon(
                                        Icons.Filled.FlashOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Autonomous Local", style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(Modifier.width(6.dp))
                            }

                            if (onOpenSettings != null) {
                                OutlinedButton(
                                    onClick = onOpenSettings,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = contentColor
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("error_settings_button")
                                ) {
                                    Icon(
                                        Icons.Filled.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Settings", style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(Modifier.width(6.dp))
                            }

                            if (error.isRetryable && onRetry != null) {
                                OutlinedButton(
                                    onClick = onRetry,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = contentColor
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("error_retry_button")
                                ) {
                                    Icon(
                                        Icons.Filled.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Retry", style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(Modifier.width(8.dp))
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("error_dismiss_button")
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Dismiss error",
                                    tint = contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
