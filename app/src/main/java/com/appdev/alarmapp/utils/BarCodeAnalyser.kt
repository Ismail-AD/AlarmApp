package com.appdev.alarmapp.utils

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.core.graphics.toRectF
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning

import com.google.mlkit.vision.common.InputImage
import kotlin.math.max

@ExperimentalGetImage
class QrCodeAnalyzer(
    private val mainViewModel: MainViewModel,
    private val targetRect: Rect,
    private val previewView: PreviewView,
    private val onQrCodeDetected: (String) -> Unit,
): ImageAnalysis.Analyzer {

    private val previewWidth = previewView.width.toFloat()
    private val previewHeight = previewView.height.toFloat()
    private var scaleFactor = 0f

    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(scannerOptions)

    override fun analyze(image: ImageProxy) {
        if (mainViewModel.detectedQrCodeState.startProcess && image.image != null) {
            val frameWidth = image.width
            val frameHeight = image.height
            scaleFactor = max(
                previewHeight / frameHeight,
                previewWidth / frameWidth
            )
            val scaledFrameHeight = frameHeight * scaleFactor
            val scaledFrameWidth = frameWidth * scaleFactor
            val marginY = (scaledFrameHeight - previewHeight) / 2
            val marginX = (scaledFrameWidth - previewWidth) / 2

            val inputImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result.let { barcodes ->
                            barcodes.forEach { barcode ->
                                barcode.boundingBox?.transform { scaledBound ->
                                    scaledBound.offset(-marginX, -marginY)
                                    if (targetRect.toRectF().contains(scaledBound)) {
                                        barcode.rawValue?.let {
                                            Log.d("CURC","PREP OF BAR CODE RESULT WHICH IS : $it")
                                            Log.d("CURC","RECEIVED IS : ${barcode.displayValue}")
                                            mainViewModel.updateDetectedString(MainViewModel.ProcessingState(qrCode = it))
                                            onQrCodeDetected(it)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        it.exception?.let { exception ->
                            Log.e("QR Analyzer", exception.message.orEmpty())
                        }
                    }
                    Log.d("CURC","IMAGE CLOSED")
                    image.close()
                }
        } else {
            Log.d("CURC","IMAGE CLOSED")
            image.close()
        }
    }

    private fun transformX(x: Float) = x * scaleFactor
    private fun transformY(y: Float) = y * scaleFactor

    private fun Rect.transform(block: (RectF) -> Unit) {
        val org = this
        block(
            toRectF().apply {
                left = transformX(org.left.toFloat())
                top = transformY(org.top.toFloat())
                right = transformX(org.right.toFloat())
                bottom = transformY(org.bottom.toFloat())
            }
        )
    }
}



