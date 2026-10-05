package com.smooth.translate.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.smooth.translate.data.model.LanguageCatalog
import com.smooth.translate.data.model.LanguageItem
import com.smooth.translate.data.repository.TranslationRepository
import com.smooth.translate.service.TtsManager
import com.smooth.translate.ui.components.LanguagePickerSheet
import com.smooth.translate.ui.components.LanguageSelectorBar
import com.smooth.translate.ui.theme.DarkText
import com.smooth.translate.ui.theme.SubtitleText
import com.smooth.translate.ui.theme.SurfaceWhite
import com.smooth.translate.ui.theme.YellowDark
import com.smooth.translate.ui.theme.YellowPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

@Composable
fun CameraTranslateScreen(
    translationRepo: TranslationRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var sourceLang by remember { mutableStateOf(LanguageCatalog.getByCode(LanguageCatalog.DEFAULT_SOURCE_CODE)) }
    var targetLang by remember { mutableStateOf(LanguageCatalog.getByCode(LanguageCatalog.DEFAULT_TARGET_CODE)) }

    val downloadedModels by translationRepo.downloadedModels.collectAsState()
    val downloadingModels by translationRepo.downloadingModels.collectAsState()

    var showSourcePicker by remember { mutableStateOf(false) }
    var showTargetPicker by remember { mutableStateOf(false) }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }

    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val processImage: (InputImage) -> Unit = { inputImage ->
        isProcessing = true
        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text.trim()
                if (rawText.isNotBlank()) {
                    recognizedText = rawText
                    scope.launch {
                        val result = translationRepo.translate(
                            text = rawText,
                            sourceLang = sourceLang.code,
                            targetLang = targetLang.code
                        )
                        isProcessing = false
                        result.onSuccess {
                            translatedText = it
                        }.onFailure {
                            Toast.makeText(context, "Translation error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    isProcessing = false
                    Toast.makeText(context, "No readable text found in image", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                isProcessing = false
                Toast.makeText(context, "Text recognition failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputImage = InputImage.fromFilePath(context, it)
                processImage(inputImage)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview
        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera permission is required", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    IconButton(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.background(YellowPrimary, CircleShape)
                    ) {
                        Text("Grant", fontWeight = FontWeight.Bold, color = DarkText)
                    }
                }
            }
        }

        // Top Overlay: Back button + Title + Language Selector Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkText
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Camera Translate",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LanguageSelectorBar(
                sourceLang = sourceLang,
                targetLang = targetLang,
                onSourceClick = { showSourcePicker = true },
                onTargetClick = { showTargetPicker = true },
                onSwapClick = {
                    val temp = sourceLang
                    sourceLang = targetLang
                    targetLang = temp
                },
                backgroundColor = Color.White.copy(alpha = 0.9f)
            )
        }

        // Translation Result Callout Overlay matching Screen 3 ("Arrivals" -> "到着する乗客")
        AnimatedVisibility(
            visible = translatedText.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .background(SurfaceWhite, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detected (${sourceLang.name})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SubtitleText
                        )
                        IconButton(
                            onClick = {
                                translatedText = ""
                                recognizedText = ""
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DarkText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = recognizedText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = DarkText,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Translation (${targetLang.name})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = translatedText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { ttsManager.speak(translatedText, targetLang.code) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak",
                                tint = DarkText
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Camera Translation", translatedText))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = DarkText
                            )
                        }
                    }
                }
            }
        }

        // Loading spinner
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = YellowPrimary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Recognizing & Translating...", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Bottom Controls matching Screen 3 (Large circular Shutter button + Gallery button)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 30.dp, end = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(48.dp)) // balance left

            // Center Camera Shutter Button (Screen 3 style)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(5.dp)
                    .background(Color.White, CircleShape)
                    .clickable {
                        val capture = imageCapture
                        if (capture != null) {
                            isProcessing = true
                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val bitmap = imageProxyToBitmap(imageProxy)
                                        imageProxy.close()
                                        if (bitmap != null) {
                                            val inputImage = InputImage.fromBitmap(bitmap, 0)
                                            processImage(inputImage)
                                        } else {
                                            isProcessing = false
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isProcessing = false
                                        Toast.makeText(context, "Capture error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
            )

            // Right Gallery Picker Button
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Pick Image",
                    tint = DarkText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showSourcePicker) {
        LanguagePickerSheet(
            title = "Source Language",
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
            title = "Target Language",
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

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val planeProxy = imageProxy.planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
