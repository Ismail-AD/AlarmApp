package com.appdev.alarmapp.ui.MissionDemos

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.utils.QrCodeAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext

typealias AndroidSize = android.util.Size

@Composable
@ExperimentalGetImage
fun BarCodeCameraPreview(
    viewModel: MainViewModel, onDetect: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val preview = Preview.Builder().build()
    val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(
            AndroidSize(previewView.width, previewView.height)
        )
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val targetRect by remember { derivedStateOf { uiState.targetRect } }

    LaunchedEffect(targetRect) {
        imageAnalysis.setAnalyzer(
            Dispatchers.Default.asExecutor(),
            QrCodeAnalyzer(
                mainViewModel = viewModel,
                targetRect = targetRect.toAndroidRect(),
                previewView = previewView,
            ) { result ->
                viewModel.onQrCodeDetected(result)
            }
        )
    }

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(uiState.lensFacing)
        .build()
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(uiState.lensFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context)
        val cameraProviderFuture: ProcessCameraProvider =
            withContext(Dispatchers.IO) {
                cameraProvider.get()
            }
        try {
            cameraProviderFuture.unbindAll()
            camera = withContext(Dispatchers.IO) {
                cameraProvider.get()
            }.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
            preview.setSurfaceProvider(previewView.surfaceProvider)
            camera?.cameraControl?.enableTorch(viewModel.flashLight)

        } catch (e: Exception) {

        }

    }

    Scaffold { paddingValues ->
        Content(
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            previewView = previewView,
            onTargetPositioned = viewModel::onTargetPositioned
        ) {
            onDetect()
        }
    }
}


@Composable
private fun Content(
    viewModel: MainViewModel,
    modifier: Modifier,
    previewView: PreviewView,
    uiState: MainViewModel.QrScanUIState,
    onTargetPositioned: (Rect) -> Unit, onDetect: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = {
                previewView
            }
        )
        val widthInPx: Float
        val heightInPx: Float
        val radiusInPx: Float
        with(LocalDensity.current) {
            widthInPx = 250.dp.toPx()
            heightInPx = 250.dp.toPx()
            radiusInPx = 16.dp.toPx()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .5f)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .size(250.dp)
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .onGloballyPositioned {
                        onTargetPositioned(it.boundsInRoot())
                    }
            ) {
                val offset = Offset(
                    x = (size.width - widthInPx) / 2,
                    y = (size.height - heightInPx) / 2,
                )
                val cutoutRect = Rect(offset, Size(widthInPx, heightInPx))
                // Source
                drawRoundRect(
                    topLeft = cutoutRect.topLeft,
                    size = cutoutRect.size,
                    cornerRadius = CornerRadius(radiusInPx, radiusInPx),
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear
                )
            }
        }
        Text(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 155.dp, start = 50.dp, end = 50.dp),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Center,
            text = "Place a QR/Barcode inside the box",
        )
        Log.d("CHKBC","Start Process ${viewModel.detectedQrCodeState.startProcess}")
        Log.d("CHKBC","Qr code state ${viewModel.detectedQrCodeState.qrCode.isNotEmpty()}")

        if (viewModel.detectedQrCodeState.qrCode.isNotEmpty() && viewModel.detectedQrCodeState.startProcess) {
            Log.d("CHKBC","On Detect Called")
            onDetect()
        }
    }
}
