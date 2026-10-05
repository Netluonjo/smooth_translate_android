package com.smooth.translate.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.smooth.translate.data.model.LanguageCatalog
import com.smooth.translate.data.model.LanguageItem
import com.smooth.translate.data.repository.TranslationRepository
import com.smooth.translate.service.SpeechRecognizerHelper
import com.smooth.translate.service.TtsManager
import com.smooth.translate.ui.components.LanguagePickerSheet
import com.smooth.translate.ui.components.LanguageSelectorBar
import com.smooth.translate.ui.components.SoundwaveVisualizer
import com.smooth.translate.ui.theme.DarkText
import com.smooth.translate.ui.theme.LightBg
import com.smooth.translate.ui.theme.SubtitleText
import com.smooth.translate.ui.theme.SurfaceWhite
import com.smooth.translate.ui.theme.YellowDark
import com.smooth.translate.ui.theme.YellowLight
import com.smooth.translate.ui.theme.YellowPrimary
import kotlinx.coroutines.launch

@Composable
fun VoiceTranslateScreen(
    translationRepo: TranslationRepository,
    ttsManager: TtsManager,
    speechRecognizer: SpeechRecognizerHelper,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceLang by remember { mutableStateOf(LanguageCatalog.getByCode(LanguageCatalog.DEFAULT_SOURCE_CODE)) }
    var targetLang by remember { mutableStateOf(LanguageCatalog.getByCode(LanguageCatalog.DEFAULT_TARGET_CODE)) }

    val downloadedModels by translationRepo.downloadedModels.collectAsState()
    val downloadingModels by translationRepo.downloadingModels.collectAsState()
    val isListening by speechRecognizer.isListening.collectAsState()
    val recognizedSpeech by speechRecognizer.recognizedText.collectAsState()

    var spokenText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    var showSourcePicker by remember { mutableStateOf(false) }
    var showTargetPicker by remember { mutableStateOf(false) }

    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Microphone permission is needed for voice translation", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    speechRecognizer.stopListening()
                    onBack()
                },
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
                text = "Voice Translate",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Selector Bar
        LanguageSelectorBar(
            sourceLang = sourceLang,
            targetLang = targetLang,
            onSourceClick = { showSourcePicker = true },
            onTargetClick = { showTargetPicker = true },
            onSwapClick = {
                val temp = sourceLang
                sourceLang = targetLang
                targetLang = temp
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Center Voice Animated Waves & Big Mic Button matching Screen 1
        Box(
            modifier = Modifier
                .size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isListening) {
                // Outer glowing animated pulse ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .background(YellowPrimary.copy(alpha = 0.25f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(YellowPrimary.copy(alpha = 0.4f), CircleShape)
                )
            }

            // Central Mic Button
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(8.dp, CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(YellowPrimary, YellowDark)
                        ),
                        CircleShape
                    )
                    .clickable {
                        if (isListening) {
                            speechRecognizer.stopListening()
                        } else {
                            if (!hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                return@clickable
                            }
                            spokenText = ""
                            translatedText = ""
                            speechRecognizer.startListening(
                                langCode = sourceLang.code,
                                onResult = { text ->
                                    spokenText = text
                                    isTranslating = true
                                    scope.launch {
                                        val res = translationRepo.translate(
                                            text = text,
                                            sourceLang = sourceLang.code,
                                            targetLang = targetLang.code
                                        )
                                        isTranslating = false
                                        res.onSuccess { tr ->
                                            translatedText = tr
                                            ttsManager.speak(tr, targetLang.code)
                                        }.onFailure {
                                            Toast.makeText(context, "Translation error: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = DarkText,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isListening) "Listening... speak now" else if (isTranslating) "Translating..." else "Tap microphone to speak",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (isListening) YellowDark else DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Pulsating Soundwave Visualizer matching Screen 1
        SoundwaveVisualizer(
            isListening = isListening,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dialogue Results Cards matching Screen 1 style (White speech bubble for User, Blue speech bubble for Translation!)
        AnimatedVisibility(
            visible = spokenText.isNotBlank() || translatedText.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Source Speech Bubble
                if (spokenText.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .shadow(3.dp, RoundedCornerShape(20.dp))
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = sourceLang.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubtitleText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = spokenText,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkText
                                )
                            }
                        }
                    }
                }

                // Translated Speech Bubble (Vibrant Blue matching Screen 1 "Bonjour" card)
                if (translatedText.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .shadow(4.dp, RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))
                                    ),
                                    RoundedCornerShape(20.dp)
                                )
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
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Row {
                                        IconButton(
                                            onClick = { ttsManager.speak(translatedText, targetLang.code) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Speak",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Voice Translation", translatedText))
                                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = translatedText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showSourcePicker) {
        LanguagePickerSheet(
            title = "Voice Language",
            selectedCode = sourceLang.code,
            downloadedCodes = downloadedModels,
            downloadingCodes = downloadingModels,
            onSelect = { sourceLang = it },
            onDownloadModel = { code ->
                scope.launch { translationRepo.downloadModel(code) }
            },
            onDismiss = { showSourcePicker = false }
        )
    }

    if (showTargetPicker) {
        LanguagePickerSheet(
            title = "Translation Language",
            selectedCode = targetLang.code,
            downloadedCodes = downloadedModels,
            downloadingCodes = downloadingModels,
            onSelect = { targetLang = it },
            onDownloadModel = { code ->
                scope.launch { translationRepo.downloadModel(code) }
            },
            onDismiss = { showTargetPicker = false }
        )
    }
}
