package com.smooth.translate.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smooth.translate.data.repository.HistoryRepository
import com.smooth.translate.ui.navigation.Screen
import com.smooth.translate.ui.theme.DarkText
import com.smooth.translate.ui.theme.LightBg
import com.smooth.translate.ui.theme.SubtitleText
import com.smooth.translate.ui.theme.SurfaceWhite
import com.smooth.translate.ui.theme.YellowDark
import com.smooth.translate.ui.theme.YellowLight
import com.smooth.translate.ui.theme.YellowPrimary

@Composable
fun SettingsScreen(
    historyRepo: HistoryRepository,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var autoSpeak by remember { mutableStateOf(historyRepo.autoSpeak) }
    var wifiOnly by remember { mutableStateOf(historyRepo.wifiOnlyDownload) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .shadow(1.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkText
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Offline Translation
        Text(
            text = "OFFLINE ENGINE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SubtitleText,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(SurfaceWhite, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.CloudDone,
                    title = "Offline Language Models",
                    subtitle = "Manage on-device translation packages (~30MB)",
                    onClick = { onNavigate(Screen.OfflineModels) }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(YellowLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = YellowDark, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Download over Wi-Fi only", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DarkText)
                            Text(text = "Prevent cellular data consumption", fontSize = 12.sp, color = SubtitleText)
                        }
                    }

                    Switch(
                        checked = wifiOnly,
                        onCheckedChange = {
                            wifiOnly = it
                            historyRepo.wifiOnlyDownload = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = YellowPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Audio & Playback
        Text(
            text = "VOICE & SPEECH",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SubtitleText,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(SurfaceWhite, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(YellowLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = YellowDark, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Auto-Pronounce", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DarkText)
                        Text(text = "Read translation aloud automatically", fontSize = 12.sp, color = SubtitleText)
                    }
                }

                Switch(
                    checked = autoSpeak,
                    onCheckedChange = {
                        autoSpeak = it
                        historyRepo.autoSpeak = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = YellowPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: History
        Text(
            text = "STORAGE & DATA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SubtitleText,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(SurfaceWhite, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.DeleteSweep,
                title = "Clear History",
                subtitle = "Delete all stored translation logs",
                onClick = {
                    historyRepo.clearHistory()
                    Toast.makeText(context, "Translation history cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: About & Privacy
        Text(
            text = "ABOUT & PRIVACY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SubtitleText,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(SurfaceWhite, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(YellowLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = YellowDark, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "100% Private & Free", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkText)
                        Text(
                            text = "All text, voice, and photo processing runs on-device using Google ML Kit. Zero cloud telemetry, no character limits.",
                            fontSize = 12.sp,
                            color = SubtitleText,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Smooth Translate", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = DarkText)
                    Text(text = "v1.0.0", fontSize = 14.sp, color = SubtitleText)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(YellowLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = YellowDark, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DarkText)
                Text(text = subtitle, fontSize = 12.sp, color = SubtitleText)
            }
        }
        Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null, tint = SubtitleText)
    }
}
