package com.smooth.translate.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import com.smooth.translate.data.model.LanguageCatalog
import com.smooth.translate.data.model.LanguageItem
import com.smooth.translate.data.repository.TranslationRepository
import com.smooth.translate.service.SpeechRecognizerHelper
import com.smooth.translate.service.TtsManager
import com.smooth.translate.ui.components.LanguagePickerSheet
import com.smooth.translate.ui.theme.DarkText
import com.smooth.translate.ui.theme.YellowDark
import com.smooth.translate.ui.theme.YellowPrimary
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val senderIsTop: Boolean,
    val originalText: String,
    val translatedText: String
)

@Composable
fun FaceToFaceScreen(
    translationRepo: TranslationRepository,
    ttsManager: TtsManager,
    speechRecognizer: SpeechRecognizerHelper,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var topLang by remember { mutableStateOf(LanguageCatalog.getByCode(TranslateLanguage.JAPANESE)) }
    var bottomLang by remember { mutableStateOf(LanguageCatalog.getByCode(TranslateLanguage.ENGLISH)) }

    val downloadedModels by translationRepo.downloadedModels.collectAsState()
    val downloadingModels by translationRepo.downloadingModels.collectAsState()
    val isListening by speechRecognizer.isListening.collectAsState()

    var activeListeningSide by remember { mutableStateOf<Boolean?>(null) } // true: top, false: bottom
    var isTranslating by remember { mutableStateOf(false) }

    val messages = remember { mutableStateListOf<ChatMessage>() }

    var showTopPicker by remember { mutableStateOf(false) }
    var showBottomPicker by remember { mutableStateOf(false) }

    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Microphone permission required for face-to-face chat", Toast.LENGTH_SHORT).show()
        }
    }

    val startSideListening = { isTop: Boolean ->
        if (isListening && activeListeningSide == isTop) {
            speechRecognizer.stopListening()
            activeListeningSide = null
        } else {
            if (!hasAudioPermission) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                activeListeningSide = isTop
                val src = if (isTop) topLang.code else bottomLang.code
                val tgt = if (isTop) bottomLang.code else topLang.code

                speechRecognizer.startListening(
                    langCode = src,
                    onResult = { recognized ->
                        isTranslating = true
                        activeListeningSide = null
                        scope.launch {
                            val res = translationRepo.translate(
                                text = recognized,
                                sourceLang = src,
                                targetLang = tgt
                            )
                            isTranslating = false
                            res.onSuccess { translated ->
                                messages.add(
                                    ChatMessage(
                                        senderIsTop = isTop,
                                        originalText = recognized,
                                        translatedText = translated
                                    )
                                )
                                ttsManager.speak(translated, tgt)
                            }.onFailure {
                                Toast.makeText(context, "Translation error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onError = {
                        activeListeningSide = null
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // TOP HALF (Rotated 180° for opposite person matching Screen 4)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF60A5FA))
                    .rotate(180f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top person header: Language pill + mic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language pill button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { showTopPicker = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = topLang.flagEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = topLang.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        // Top person mic button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, CircleShape)
                                .background(
                                    if (isListening && activeListeningSide == true) Color.Red else Color.White,
                                    CircleShape
                                )
                                .clickable { startSideListening(true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening && activeListeningSide == true) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Top Mic",
                                tint = if (isListening && activeListeningSide == true) Color.White else DarkText,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Conversation cards for top person
                    val topListState = rememberLazyListState()
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) topListState.animateScrollToItem(messages.size - 1)
                    }

                    LazyColumn(
                        state = topListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            val isMe = msg.senderIsTop
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .shadow(2.dp, RoundedCornerShape(16.dp))
                                        .background(
                                            if (isMe) Color.White.copy(alpha = 0.9f) else Color.White,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = if (isMe) msg.originalText else msg.translatedText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = DarkText
                                        )
                                        Text(
                                            text = if (isMe) msg.translatedText else msg.originalText,
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM HALF (Facing current user matching Screen 4)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(YellowPrimary)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Conversation cards for bottom person
                    val bottomListState = rememberLazyListState()
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) bottomListState.animateScrollToItem(messages.size - 1)
                    }

                    LazyColumn(
                        state = bottomListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            val isMe = !msg.senderIsTop
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .shadow(2.dp, RoundedCornerShape(16.dp))
                                        .background(
                                            if (isMe) Color.White else Color(0xFFFEF9C3),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = if (isMe) msg.originalText else msg.translatedText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = DarkText
                                        )
                                        Text(
                                            text = if (isMe) msg.translatedText else msg.originalText,
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom person controls: Language pill + mic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language pill button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .clickable { showBottomPicker = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = bottomLang.flagEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = bottomLang.name,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                fontSize = 15.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = DarkText
                            )
                        }

                        // Bottom person mic button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, CircleShape)
                                .background(
                                    if (isListening && activeListeningSide == false) Color.Red else DarkText,
                                    CircleShape
                                )
                                .clickable { startSideListening(false) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening && activeListeningSide == false) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Bottom Mic",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating Back Button at top left
        IconButton(
            onClick = {
                speechRecognizer.stopListening()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 36.dp, start = 16.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        // Translating loader
        if (isTranslating) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = YellowDark, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Translating...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showTopPicker) {
        LanguagePickerSheet(
            title = "Top Side Language",
            selectedCode = topLang.code,
            downloadedCodes = downloadedModels,
            downloadingCodes = downloadingModels,
            onSelect = { topLang = it },
            onDownloadModel = { code ->
                scope.launch { translationRepo.downloadModel(code) }
            },
            onDismiss = { showTopPicker = false }
        )
    }

    if (showBottomPicker) {
        LanguagePickerSheet(
            title = "Your Language",
            selectedCode = bottomLang.code,
            downloadedCodes = downloadedModels,
            downloadingCodes = downloadingModels,
            onSelect = { bottomLang = it },
            onDownloadModel = { code ->
                scope.launch { translationRepo.downloadModel(code) }
            },
            onDismiss = { showBottomPicker = false }
        )
    }
}
