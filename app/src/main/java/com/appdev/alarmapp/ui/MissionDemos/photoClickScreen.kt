package com.appdev.alarmapp.ui.MissionDemos

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.IconButton
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.ImageData
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Locale

@Composable
fun PhotoClickScreen(controller: NavHostController, mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    var isFlashOn by remember { mutableStateOf(false) }

    val imagesList by mainViewModel.imagesList.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(imagesList) {
        loading = imagesList.isEmpty()
    }

    BackHandler(enabled = true) {
        controller.navigate(Routes.CameraRoutineScreen.route) {
            popUpTo(controller.graph.startDestinationId)
            launchSingleTop = true
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor)
    ) {
//        if (loading) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//            ) {
//                Column(
//                    modifier = Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Dialog(onDismissRequest = { /*TODO*/ }) {
//                        CircularProgressIndicator()
//                    }
//                }
//            }
//        }
        CameraPreview(
            modifier = Modifier
                .aspectRatio(1f / 1f)
                .clipToBounds()
                .align(Alignment.Center),
            cameraController
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = {
                cameraController.cameraSelector =
                    if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
            }) {
                Icon(
                    imageVector = Icons.Filled.Cameraswitch,
                    contentDescription = "camera Switch", tint = Color.Gray
                )
            }
            IconButton(onClick = {
                takePhoto(
                    cameraController,
                    context,
                    onPhotoCaptured = { receivedBitmap ->
                        receivedBitmap?.let { bm ->
                            val newList = imagesList.toMutableList()
                            newList.add(ImageData(id = System.currentTimeMillis(), bitmap = bm))
                            mainViewModel.insertImage(newList)
                            controller.navigate(Routes.CameraRoutineScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    })
            }) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Click To Capture", tint = Color.Gray
                )
            }
            IconButton(
                onClick = {
                    isFlashOn = !isFlashOn
                    cameraController.enableTorch(isFlashOn)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = if (isFlashOn) "Flash On" else "Flash Off",
                    tint = Color.Gray
                )
            }
        }
    }
}

fun takePhoto(
    controller: LifecycleCameraController,
    context: Context,
    onPhotoCaptured: (Bitmap?) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                    if (controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                        postScale(-1f, 1f)
                    }
                }
                val bitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                val cropImage = cropBitmapToAspectRatio(bitmap,1f/1f)
                val resizedBitmap = resizeBitmapWithMaxDimensions(cropImage, 1500)
                val compressedImage = compressAndReduceQuality(resizedBitmap,40)

                onPhotoCaptured(compressedImage)
//                onPhotoCaptured(bitmap.toUri(context = context))
                Toast.makeText(context, "Photo Saved Successfully", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Toast.makeText(context, "Something Went Wrong ! Try Again", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    )
}

fun cropBitmapToAspectRatio(originalBitmap: Bitmap, targetAspectRatio: Float): Bitmap {
    // Calculate the target dimensions based on the aspect ratio
    val originalWidth = originalBitmap.width
    val originalHeight = originalBitmap.height
    val targetWidth: Int
    val targetHeight: Int

    if (originalWidth / originalHeight > targetAspectRatio) {
        targetWidth = (originalHeight * targetAspectRatio).toInt()
        targetHeight = originalHeight
    } else {
        targetWidth = originalWidth
        targetHeight = (originalWidth / targetAspectRatio).toInt()
    }

    // Calculate the coordinates for cropping
    val left = (originalWidth - targetWidth) / 2
    val top = (originalHeight - targetHeight) / 2

    // Create a new bitmap with the cropped region

    return Bitmap.createBitmap(originalBitmap, left, top, targetWidth, targetHeight)
}

fun resizeBitmapWithMaxDimensions(originalBitmap: Bitmap, maxDimension: Int): Bitmap {
    val originalWidth = originalBitmap.width
    val originalHeight = originalBitmap.height

    // Calculate the new dimensions while preserving the original aspect ratio
    val newWidth: Int
    val newHeight: Int
    if (originalWidth > originalHeight) {
        newWidth = maxDimension
        newHeight = (originalHeight * (maxDimension.toFloat() / originalWidth)).toInt()
    } else {
        newHeight = maxDimension
        newWidth = (originalWidth * (maxDimension.toFloat() / originalHeight)).toInt()
    }

    // Resize the bitmap
    return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
}

fun compressAndReduceQuality(originalBitmap: Bitmap, quality: Int): Bitmap {
    // Step 1: Compress the bitmap
    val outputStream = ByteArrayOutputStream()
    originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

    // Step 2: Decode the compressed byte array back into a Bitmap
    return BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))

}

