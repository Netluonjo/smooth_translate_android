package com.smooth.translate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smooth.translate.data.model.LanguageCatalog
import com.smooth.translate.data.model.LanguageItem
import com.smooth.translate.data.model.TranslationRecord
import com.smooth.translate.data.repository.HistoryRepository
import com.smooth.translate.data.repository.TranslationRepository
import com.smooth.translate.service.TtsManager
import com.smooth.translate.ui.components.LanguagePickerSheet
import com.smooth.translate.ui.components.LanguageSelectorBar
import com.smooth.translate.ui.theme.DarkText
import com.smooth.translate.ui.theme.LightBg
import com.smooth.translate.ui.theme.SubtitleText
import com.smooth.translate.ui.theme.SurfaceWhite
import com.smooth.translate.ui.theme.YellowDark
import com.smooth.translate.ui.theme.YellowLight
import com.smooth.translate.ui.theme.YellowPrimary
import kotlinx.coroutines.launch

@Composable
fun TextTranslateScreen(
    translationRepo: TranslationRepository,
    historyRepo: HistoryRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceLang by remember { mutableStateOf(LanguageCatalog.getByCode(LanguageCatalog.DEFAULT_SOURCE_CODE)) }
    var targetLang by remember { mutableStateOf(LanguageCatalog.getByCode(LanguageCatalog.DEFAULT_TARGET_CODE)) }

    var inputText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    val downloadedModels by translationRepo.downloadedModels.collectAsState()
    val downloadingModels by translationRepo.downloadingModels.collectAsState()
    val historyList by historyRepo.history.collectAsState()
    val isSpeaking by ttsManager.isSpeaking.collectAsState()

    var showSourcePicker by remember { mutableStateOf(false) }
    var showTargetPicker by remember { mutableStateOf(false) }

    val performTranslate = {
        if (inputText.isNotBlank()) {
            isTranslating = true
            scope.launch {
                val result = translationRepo.translate(
                    text = inputText,
                    sourceLang = sourceLang.code,
                    targetLang = targetLang.code,
                    requireWifiForDownload = historyRepo.wifiOnlyDownload
                )
                isTranslating = false
                result.onSuccess { output ->
                    translatedText = output
                    if (output.isNotBlank()) {
                        historyRepo.addRecord(
                            TranslationRecord(
                                sourceText = inputText,
                                translatedText = output,
                                sourceLang = sourceLang.code,
                                targetLang = targetLang.code
                            )
                        )
                        if (historyRepo.autoSpeak) {
                            ttsManager.speak(output, targetLang.code)
                        }
                    }
                }.onFailure { err ->
                    Toast.makeText(context, "Translation failed: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Navigation Bar
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
                text = "Text Translate",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Selector Bar matching Screen 5
        LanguageSelectorBar(
            sourceLang = sourceLang,
            targetLang = targetLang,
            onSourceClick = { showSourcePicker = true },
            onTargetClick = { showTargetPicker = true },
            onSwapClick = {
                val temp = sourceLang
                sourceLang = targetLang
                targetLang = temp
                val tempText = inputText
                inputText = translatedText
                translatedText = tempText
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(24.dp))
                .background(SurfaceWhite, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Enter text please...", color = SubtitleText, fontSize = 16.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = DarkText
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${inputText.length} chars",
                        fontSize = 12.sp,
                        color = SubtitleText
                    )

                    Row {
                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    inputText = ""
                                    translatedText = ""
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = SubtitleText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    inputText = clip.getItemAt(0).text.toString()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = SubtitleText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vibrant Yellow Translate CTA Button matching Screen 5
        Button(
            onClick = { performTranslate() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(4.dp, RoundedCornerShape(27.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowPrimary,
                contentColor = DarkText
            ),
            shape = RoundedCornerShape(27.dp),
            enabled = inputText.isNotBlank() && !isTranslating
        ) {
            if (isTranslating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = DarkText,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Translating...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Text(text = "Translate", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Translation Result Card
        AnimatedVisibility(
            visible = translatedText.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .background(SurfaceWhite, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = targetLang.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = YellowDark
                        )
                        Row {
                            IconButton(
                                onClick = { ttsManager.speak(translatedText, targetLang.code) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = if (isSpeaking) YellowDark else DarkText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Translated Text", translatedText))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = DarkText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, translatedText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Translation"))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = DarkText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = translatedText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Translations History
        if (historyList.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Translations",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DarkText
                )
                Text(
                    text = "Clear All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubtitleText,
                    modifier = Modifier.clickable { historyRepo.clearHistory() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(historyList, key = { it.id }) { record ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .shadow(1.dp, RoundedCornerShape(16.dp))
                            .background(SurfaceWhite, RoundedCornerShape(16.dp))
                            .clickable {
                                inputText = record.sourceText
                                translatedText = record.translatedText
                                sourceLang = LanguageCatalog.getByCode(record.sourceLang)
                                targetLang = LanguageCatalog.getByCode(record.targetLang)
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.sourceText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = SubtitleText,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = record.translatedText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkText,
                                    maxLines = 2
                                )
                            }

                            IconButton(
                                onClick = { historyRepo.toggleFavorite(record.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (record.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (record.isFavorite) Color.Red else SubtitleText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { historyRepo.deleteRecord(record.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = SubtitleText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // Source Picker Sheet
    if (showSourcePicker) {
        LanguagePickerSheet(
            title = "Source Language",
            selectedCode = sourceLang.code,
            downloadedCodes = downloadedModels,
            downloadingCodes = downloadingModels,
            onSelect = { sourceLang = it },
            onDownloadModel = { code ->
                scope.launch {
                    translationRepo.downloadModel(code, historyRepo.wifiOnlyDownload)
                }
            },
            onDismiss = { showSourcePicker = false }
        )
    }

    // Target Picker Sheet
    if (showTargetPicker) {
        LanguagePickerSheet(
            title = "Target Language",
            selectedCode = targetLang.code,
            downloadedCodes = downloadedModels,
            downloadingCodes = downloadingModels,
            onSelect = { targetLang = it },
            onDownloadModel = { code ->
                scope.launch {
                    translationRepo.downloadModel(code, historyRepo.wifiOnlyDownload)
                }
            },
            onDismiss = { showTargetPicker = false }
        )
    }
}
