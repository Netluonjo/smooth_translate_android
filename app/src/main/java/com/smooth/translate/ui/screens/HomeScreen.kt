package com.smooth.translate.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smooth.translate.R
import com.smooth.translate.data.repository.TranslationRepository
import com.smooth.translate.ui.components.ToolCard
import com.smooth.translate.ui.navigation.Screen
import com.smooth.translate.ui.theme.CardBlueEnd
import com.smooth.translate.ui.theme.CardBlueStart
import com.smooth.translate.ui.theme.CardGreenEnd
import com.smooth.translate.ui.theme.CardGreenStart
import com.smooth.translate.ui.theme.CardOrangeEnd
import com.smooth.translate.ui.theme.CardOrangeStart
import com.smooth.translate.ui.theme.CardPurpleEnd
import com.smooth.translate.ui.theme.CardPurpleStart
import com.smooth.translate.ui.theme.DarkText
import com.smooth.translate.ui.theme.LightBg
import com.smooth.translate.ui.theme.SubtitleText
import com.smooth.translate.ui.theme.SuccessGreen
import com.smooth.translate.ui.theme.YellowDark
import com.smooth.translate.ui.theme.YellowLight
import com.smooth.translate.ui.theme.YellowPrimary

@Composable
fun HomeScreen(
    translationRepo: TranslationRepository,
    onNavigate: (Screen) -> Unit
) {
    val downloadedModels by translationRepo.downloadedModels.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Header matching Screen 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Translate",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText
                )
            }

            IconButton(
                onClick = { onNavigate(Screen.Settings) },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White, CircleShape)
                    .shadow(1.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = DarkText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Offline Translation Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(YellowLight, RoundedCornerShape(20.dp))
                .clickable { onNavigate(Screen.OfflineModels) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(YellowPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Offline Data",
                    tint = DarkText,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Offline On-Device Engine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DarkText
                )
                Text(
                    text = if (downloadedModels.isEmpty()) {
                        "Download language packs for 100% offline use"
                    } else {
                        "${downloadedModels.size} language pack(s) ready offline"
                    },
                    fontSize = 12.sp,
                    color = SubtitleText
                )
            }
            Icon(
                imageVector = Icons.Default.NavigateNext,
                contentDescription = null,
                tint = SubtitleText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2x2 Grid of Main Tools matching Screen 2
        // Row 1: Text & Photo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToolCard(
                title = "Text",
                drawableResId = R.drawable.ic_tool_text,
                gradientColors = listOf(CardGreenStart, CardGreenEnd),
                onClick = { onNavigate(Screen.TextTranslate) },
                modifier = Modifier.weight(1f)
            )

            ToolCard(
                title = "Photo",
                drawableResId = R.drawable.ic_tool_photo,
                gradientColors = listOf(CardBlueStart, CardBlueEnd),
                onClick = { onNavigate(Screen.CameraTranslate) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 2: Voice & Chat (Face-To-Face)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToolCard(
                title = "Voice",
                drawableResId = R.drawable.ic_tool_voice,
                gradientColors = listOf(CardOrangeStart, CardOrangeEnd),
                onClick = { onNavigate(Screen.VoiceTranslate) },
                modifier = Modifier.weight(1f)
            )

            ToolCard(
                title = "Chat",
                drawableResId = R.drawable.ic_tool_chat,
                gradientColors = listOf(CardPurpleStart, CardPurpleEnd),
                onClick = { onNavigate(Screen.FaceToFace) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Offline Storage Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "100% Free Offline Data",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkText
                    )
                    Text(
                        text = "Manage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = YellowDark,
                        modifier = Modifier.clickable { onNavigate(Screen.OfflineModels) }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Powered by Google ML Kit On-Device Translation. No server required, unlimited characters, completely private on your phone.",
                    fontSize = 12.sp,
                    color = SubtitleText,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
