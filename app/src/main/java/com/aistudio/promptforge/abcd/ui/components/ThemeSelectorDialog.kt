package com.aistudio.promptforge.abcd.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.ui.theme.AppThemeMode
import com.aistudio.promptforge.abcd.ui.theme.AppThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.CyberpunkThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.DarkThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.LightThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.LocalAppThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.NeonThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager

@Composable
fun ThemeSelectorDialog(
    themeManager: ThemeManager,
    onDismiss: () -> Unit
) {
    val currentMode by themeManager.themeMode.collectAsState()
    val glowEnabled by themeManager.glowEffectsEnabled.collectAsState()
    val tokens = LocalAppThemeTokens.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tokens.accentPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Palette,
                        contentDescription = "Theme System",
                        tint = tokens.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Workspace Appearance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Shared tokenized design system",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Choose an appearance mode. AutoFlow preserves strict high-contrast readability across all themes:",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )

                // Theme Mode Cards
                ThemeOptionCard(
                    mode = AppThemeMode.DARK,
                    isSelected = currentMode == AppThemeMode.DARK,
                    tokens = DarkThemeTokens,
                    icon = Icons.Filled.DarkMode,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.DARK) },
                    testTag = "theme_option_dark"
                )

                ThemeOptionCard(
                    mode = AppThemeMode.LIGHT,
                    isSelected = currentMode == AppThemeMode.LIGHT,
                    tokens = LightThemeTokens,
                    icon = Icons.Filled.LightMode,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.LIGHT) },
                    testTag = "theme_option_light"
                )

                ThemeOptionCard(
                    mode = AppThemeMode.NEON,
                    isSelected = currentMode == AppThemeMode.NEON,
                    tokens = NeonThemeTokens,
                    icon = Icons.Filled.FlashOn,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.NEON) },
                    testTag = "theme_option_neon"
                )

                ThemeOptionCard(
                    mode = AppThemeMode.CYBERPUNK,
                    isSelected = currentMode == AppThemeMode.CYBERPUNK,
                    tokens = CyberpunkThemeTokens,
                    icon = Icons.Filled.AutoAwesome,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.CYBERPUNK) },
                    testTag = "theme_option_cyberpunk"
                )

                ThemeOptionCard(
                    mode = AppThemeMode.SYSTEM,
                    isSelected = currentMode == AppThemeMode.SYSTEM,
                    tokens = DarkThemeTokens, // Reference preview
                    icon = Icons.Filled.BrightnessAuto,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.SYSTEM) },
                    testTag = "theme_option_system"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // State Glow Toggle
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = tokens.surfaceElevated
                    ),
                    border = BorderStroke(1.dp, tokens.border),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Workflow State Glow",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = tokens.textPrimary
                            )
                            Text(
                                "Glow indicates active running & generation states (never pure decoration)",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = tokens.textSecondary
                            )
                        }
                        Switch(
                            checked = glowEnabled,
                            onCheckedChange = { themeManager.setGlowEffectsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = tokens.accentPrimary,
                                checkedTrackColor = tokens.accentPrimary.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("theme_glow_toggle")
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("theme_dialog_close")
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            if (currentMode != AppThemeMode.SYSTEM) {
                OutlinedButton(
                    onClick = { themeManager.resetToSystemDefault() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.testTag("theme_reset_system_button")
                ) {
                    Icon(
                        Icons.Filled.SettingsBrightness,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Use System Setting", fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
private fun ThemeOptionCard(
    mode: AppThemeMode,
    isSelected: Boolean,
    tokens: AppThemeTokens,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit,
    testTag: String
) {
    val currentTheme = LocalAppThemeTokens.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                currentTheme.accentPrimary.copy(alpha = 0.12f)
            } else {
                currentTheme.surfaceElevated
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = if (isSelected) currentTheme.accentPrimary else currentTheme.border
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) currentTheme.accentPrimary else currentTheme.surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else currentTheme.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            mode.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = currentTheme.textPrimary
                        )
                        Text(
                            mode.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) currentTheme.accentPrimary else currentTheme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(currentTheme.accentPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                mode.description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = currentTheme.textSecondary
            )

            // Swatch Chips Preview
            if (mode != AppThemeMode.SYSTEM) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorSwatch(color = tokens.background, label = "Bg", textColor = tokens.textPrimary)
                    ColorSwatch(color = tokens.surface, label = "Surface", textColor = tokens.textPrimary)
                    ColorSwatch(color = tokens.accentPrimary, label = "Primary", textColor = Color.White)
                    ColorSwatch(color = tokens.accentSecondary, label = "Secondary", textColor = Color.Black)
                    if (tokens.isGlowEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(tokens.accentPrimary.copy(alpha = 0.2f))
                                .border(1.dp, tokens.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("State Glow", fontSize = 9.sp, color = tokens.accentPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 9.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}
