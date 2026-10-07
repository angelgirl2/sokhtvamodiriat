package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Size
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraQrScannerDialog(
    onDismiss: () -> Unit,
    onReceiptScanned: (liters: Double, pricePerLiter: Long, stationName: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camera_qr_scanner_dialog"),
                color = Color.Black
            ) {
            if (cameraPermissionState.status.isGranted) {
                // RENDER CAMERAX PREVIEW
                CameraScannerContent(
                    onDismiss = onDismiss,
                    onQrCodeScanned = { rawText ->
                        // Parse scanned QR data
                        val parsedData = parseReceiptQrData(rawText)
                        onReceiptScanned(
                            parsedData.liters,
                            parsedData.pricePerLiter,
                            parsedData.stationName,
                            parsedData.notes
                        )
                        com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("🧾 رسید سوخت‌گیری با موفقیت اسکن شد!")
                        onDismiss()
                    }
                )
            } else {
                // RENDER FRIENDLY PERMISSION REQUEST
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "نیاز به دسترسی به دوربین",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "برای اسکن رسید سوخت‌گیری فیزیکی جایگاه بنزین و استخراج مشخصات آن به صورت هوشمند، نیاز به فعال‌سازی دسترسی دوربین داریم.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("اعطای دسترسی دوربین 📷", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun CameraScannerContent(
    onDismiss: () -> Unit,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var isScanningActive by remember { mutableStateOf(true) }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    // Floating Laser Scanner line animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. CameraX Android View
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor, BarcodeAnalyzer { rawText ->
                        if (isScanningActive) {
                            isScanningActive = false // Debounce scanners
                            onQrCodeScanned(rawText)
                        }
                    })

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl
                    } catch (e: Exception) {
                        com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("خطا در بالاآوردن دوربین: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Cyberpunk Scanner Viewfinder Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Scanner box dimensions (240dp square)
            val boxSize = 250.dp.toPx()
            val left = (w - boxSize) / 2
            val top = (h - boxSize) / 2

            // Dim outer area
            drawRect(
                color = Color.Black.copy(alpha = 0.65f)
            )
            // Clear viewfinder box
            drawRect(
                color = Color.Transparent,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )

            // Draw bounding corners
            drawRect(
                color = Color(0xFF0284C7),
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Dynamic moving scanner Laser line
            val laserY = top + (boxSize * laserYRatio)
            drawLine(
                color = Color(0xFFEF4444),
                start = androidx.compose.ui.geometry.Offset(left + 10f, laserY),
                end = androidx.compose.ui.geometry.Offset(left + boxSize - 10f, laserY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // 3. User Controls Layer (Exit, Flashlight, Guide Texts)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Exit & Flashlight Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = Color.White
                    )
                }

                Text(
                    text = "اسکنر رسید سوخت",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                IconButton(
                    onClick = {
                        flashEnabled = !flashEnabled
                        cameraControl?.enableTorch(flashEnabled)
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        Icons.Default.FlashlightOn,
                        contentDescription = "فلش",
                        tint = if (flashEnabled) Color(0xFFFACC15) else Color.White
                    )
                }
            }

            // Viewfinder Guide Text
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "بارکد یا QR رسید فیزیکی پمپ بنزین را در کادر قرار دهید",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Clean up Executor on dispose
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

// Custom Barcode analyzer bounding to Google ML Kit Barcode Scan
@androidx.annotation.OptIn(ExperimentalGetImage::class)
private class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            onBarcodeDetected(rawValue)
                            break
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

// Smart Receipt QR Code Parser with Regular Expression to handle various formats
private data class ParsedReceiptData(
    val liters: Double,
    val pricePerLiter: Long,
    val stationName: String,
    val notes: String
)

private fun parseReceiptQrData(rawText: String): ParsedReceiptData {
    // Standard Iranian Refueling Station Receipts often contain keys like "liters", "price", "amount", or are simple URLs with parameters
    // Let's implement robust regex lookups
    var lit = 30.0
    var prc = 1500L
    var station = "جایگاه سوخت شتاب"
    var note = "اسکن‌شده از رسید سوخت‌گیری فیزیکی"

    try {
        // Try reading query parameters if QR is a URL (e.g., http://niopdc.ir/receipt?liters=35&price=3000)
        if (rawText.contains("?") || rawText.contains("&")) {
            val params = rawText.split("?").lastOrNull()?.split("&")
            params?.forEach { p ->
                val pair = p.split("=")
                if (pair.size == 2) {
                    val key = pair[0].lowercase()
                    val value = pair[1]
                    if (key.contains("liter") || key == "l") {
                        lit = value.toDoubleOrNull() ?: lit
                    } else if (key.contains("price") || key == "p") {
                        prc = value.toLongOrNull() ?: prc
                    } else if (key.contains("station") || key == "s") {
                        station = value.replace("%20", " ")
                    }
                }
            }
        } else {
            // General text format lookup
            // Match any numbers for liters (usually 10 to 60)
            val litersPattern = "\\b([1-5][0-9])\\b".toRegex()
            val matchLit = litersPattern.find(rawText)
            if (matchLit != null) {
                lit = matchLit.groupValues[1].toDoubleOrNull() ?: lit
            }

            // Match price (1500 or 3000)
            if (rawText.contains("3000") || rawText.contains("۳۰۰۰")) {
                prc = 3000L
            } else if (rawText.contains("1500") || rawText.contains("۱۵۰۰")) {
                prc = 1500L
            }
        }
    } catch (_: Exception) {}

    return ParsedReceiptData(
        liters = lit,
        pricePerLiter = prc,
        stationName = station,
        notes = "$note | محتوای خام بارکد: $rawText"
    )
}
