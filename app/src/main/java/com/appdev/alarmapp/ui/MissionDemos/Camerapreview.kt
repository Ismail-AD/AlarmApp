package com.appdev.alarmapp.ui.MissionDemos


import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

@Composable
fun CameraPreview(modifier: Modifier, cameraController: LifecycleCameraController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(factory = {
        val pv = PreviewView(it).apply {
//            this.scaleType = PreviewView.ScaleType.FIT_CENTER
            this.controller = cameraController
            cameraController.bindToLifecycle(lifecycleOwner)
        }
//        val resolutionSelector = ResolutionSelector.Builder()
//            .setAspectRatioStrategy(
//                AspectRatioStrategy(
//                    AspectRatio.RATIO_4_3,
//                    AspectRatioStrategy.FALLBACK_RULE_AUTO
//                )
//            )
//            .build()
//
//        val imageCapture = ImageCapture.Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
//            .setResolutionSelector(resolutionSelector)
//            .build()
//
//
//        val previewUseCase = Preview.Builder().setResolutionSelector(resolutionSelector).build()
//        previewUseCase.setSurfaceProvider(pv.surfaceProvider)
//
//        val listenableFuture = ProcessCameraProvider.getInstance(it)
//        listenableFuture.addListener({
//            val cameraProvider = listenableFuture.get()
//            cameraProvider.unbindAll()
//            cameraProvider.bindToLifecycle(
//                lifecycleOwner,
//                CameraSelector.DEFAULT_BACK_CAMERA,
//                previewUseCase, imageCapture
//            )
//        }, ContextCompat.getMainExecutor(it))

        pv
    }, modifier = modifier)
}