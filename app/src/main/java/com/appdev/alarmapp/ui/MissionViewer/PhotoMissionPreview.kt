package com.appdev.alarmapp.ui.MissionViewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ThumbnailUtils
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.AlarmManagement.DismissCallback
import com.appdev.alarmapp.R
import com.appdev.alarmapp.navigation.Routes
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.appdev.alarmapp.ui.MissionDemos.CameraPreview
import com.appdev.alarmapp.ui.theme.backColor
import com.appdev.alarmapp.utils.Helper
import com.appdev.alarmapp.utils.ImageData
import com.appdev.alarmapp.utils.MissionDataHandler
import com.appdev.alarmapp.utils.convertStringToSet
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun PhotoMissionScreen(
    mainViewModel: MainViewModel,
    controller: NavHostController,
    dismissCallback: DismissCallback
) {

    val dismissSettings by mainViewModel.dismissSettings.collectAsStateWithLifecycle()
    if (dismissSettings.muteTone) {
        Helper.stopStream()
    }
    var loading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(1f) }
    var isFlashOn by remember { mutableStateOf(false) }

    var openCamera by remember { mutableStateOf(false) }
    var isMatched by remember { mutableStateOf<Boolean?>(null) }


    val context = LocalContext.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    var bitmapImage by remember {
        mutableStateOf<ImageData?>(null)
    }

    LaunchedEffect(key1 = mainViewModel.selectedImage) {
        if (mainViewModel.missionDetails.imageId > 1) {
            bitmapImage = mainViewModel.selectedImage
        }
    }

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    ).value

    LaunchedEffect(animatedProgress) {
        var elapsedTime = 0L
        val duration = dismissSettings.missionTime * 1000
        while (elapsedTime < duration && progress > 0.00100f) {
            val deltaTime = min(10, duration - elapsedTime)
            elapsedTime += deltaTime
            delay(deltaTime)
            progress -= deltaTime.toFloat() / duration // Decrement the progress
        }
    }
    LaunchedEffect(key1 = progress) {
        if (progress < 0.00100f) {
            Helper.playStream(context, R.raw.alarmsound)
            controller.navigate(Routes.PreviewAlarm.route) {
                popUpTo(controller.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }
    LaunchedEffect(key1 = isMatched) {
        if (isMatched == false) {
            loading = false
            openCamera = false
            delay(3000)
            progress = 1f
            isMatched = null
        }
        if (isMatched == true) {
            loading = false
            openCamera = false
            delay(2000)
            if (mainViewModel.isRealAlarm) {
                val mutableList = mainViewModel.dummyMissionList.toMutableList()
                mutableList.removeFirst()
                mainViewModel.dummyMissionList = mutableList
                if (mainViewModel.dummyMissionList.isNotEmpty()) {
                    val singleMission = mainViewModel.dummyMissionList.first()

                    mainViewModel.missionData(
                        MissionDataHandler.AddCompleteMission(
                            missionId = singleMission.missionID,
                            repeat = singleMission.repeatTimes,
                            repeatProgress = singleMission.repeatProgress,
                            missionLevel = singleMission.missionLevel,
                            missionName = singleMission.missionName,
                            isSelected = singleMission.isSelected,
                            setOfSentences = convertStringToSet(singleMission.selectedSentences),
                            imageId = singleMission.imageId,
                            codeId = singleMission.codeId
                        )
                    )
                    when (mainViewModel.missionDetails.missionName) {
                        "Memory" -> {
                            controller.navigate(Routes.MissionScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                        "Shake" -> {
                            controller.navigate(Routes.MissionShakeScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                        "Math" -> {
                            controller.navigate(Routes.MissionMathScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                        "Typing" -> {
                            controller.navigate(Routes.TypingPreviewScreen.route) {
                                popUpTo(Routes.PreviewAlarm.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }

                        "Photo" -> {
                            controller.navigate(Routes.PhotoMissionPreviewScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "Step" -> {
                            controller.navigate(Routes.StepDetectorScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        "Squat" -> {
                            controller.navigate(Routes.SquatMissionScreen.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                        "QR/Barcode" -> {
                            controller.navigate(Routes.BarCodePreviewAlarmScreen.route) {
                                popUpTo(Routes.PreviewAlarm.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }

                        else -> {
                            dismissCallback.onDismissClicked()
                        }
                    }
                } else {
                    dismissCallback.onDismissClicked()
                }
            } else {
                controller.navigate(Routes.CameraRoutineScreen.route) {
                    popUpTo(Routes.PreviewAlarm.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff121315)),
        contentAlignment = Alignment.TopCenter
    ) {
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {

                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Dialog(onDismissRequest = { /*TODO*/ }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Processing...",
                                color = Color.LightGray,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.W400,
                                modifier = Modifier.padding(start = 15.dp)
                            )
                        }
                    }

                }
            }
        }
        when (isMatched) {
            false -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wrong),
                            contentDescription = "",
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "Image doesn't matched! Please try again...",
                            color = Color(0xffF44336),
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 30.dp),
                            lineHeight = 35.sp
                        )

                    }
                }
            }

            true -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.angel),
                            contentDescription = "",
                            modifier = Modifier.size(95.dp)
                        )
                        Text(
                            text = "Have a nice day :)",
                            color = Color.White,
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 30.dp),
                            lineHeight = 35.sp
                        )

                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            trackColor = backColor,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp), progress = animatedProgress
                        )

                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            controller.navigate(Routes.PreviewAlarm.route) {
                                popUpTo(controller.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIos,
                                contentDescription = "",
                                tint = Color.White, modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    if (!openCamera) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.8f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Get ready to take the photo",
                                color = Color.White,
                                fontSize = 25.sp, textAlign = TextAlign.Center,
                                fontWeight = FontWeight.W400,
                                modifier = Modifier.padding(horizontal = 80.dp), lineHeight = 35.sp
                            )
                            Spacer(modifier = Modifier.height(25.dp))
                            if (mainViewModel.missionDetails.imageId > 1) {
                                mainViewModel.getImageById(mainViewModel.missionDetails.imageId)
                                bitmapImage?.bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "",
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    )
                                }
                            } else {
                                mainViewModel.selectedImage.bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "",
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(35.dp))
                            CustomButton(
                                onClick = {
                                    progress = 1f
                                    openCamera = true
                                },
                                text = "I'm ready",
                                width = 0.8f,
                                backgroundColor = Color(0xff7B70FF),
                                textColor = Color.White
                            )

                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 80.dp)
                                .background(backColor)
                        ) {
                            CameraPreview(
                                modifier = Modifier
                                    .aspectRatio(1f / 1f)
                                    .clipToBounds()
                                    .align(Alignment.TopCenter),
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
                                    loading = true
                                    takePhoto(
                                        cameraController,
                                        context
                                    ) { receivedBitmap ->
                                        receivedBitmap?.let { bm ->
                                            isMatched =
                                                mainViewModel.selectedImage.bitmap?.let {
                                                    areImagesSimilar(
                                                        it,
                                                        bm,
                                                        when (mainViewModel.dismissSettings.value.photoSensitivity) {
                                                            "High(hard to turn off)" -> 0.50
                                                            "Normal" -> 0.45
                                                            "Low(easy to turn off)" -> 0.38
                                                            else -> {
                                                                0.45
                                                            }
                                                        }
                                                    )
                                                }
                                        }
                                    }
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

                }
            }
        }

    }
}

fun areImagesSimilar(bmp1: Bitmap, bmp2: Bitmap, threshold: Double): Boolean {
    val similarity = calSimilarity(bmp1, bmp2)
    return similarity > threshold
}

fun calSimilarity(bmp1: Bitmap, bmp2: Bitmap): Double {
    var bmp1 = toGrayscale(bmp1)
    var bmp2 = toGrayscale(bmp2)

    bmp1 = ThumbnailUtils.extractThumbnail(bmp1, 32, 32)
    bmp2 = ThumbnailUtils.extractThumbnail(bmp2, 32, 32)

    val pixels1 = IntArray(bmp1.width * bmp1.height)
    val pixels2 = IntArray(bmp2.width * bmp2.height)

    bmp1.getPixels(pixels1, 0, bmp1.width, 0, 0, bmp1.width, bmp1.height)
    bmp2.getPixels(pixels2, 0, bmp2.width, 0, 0, bmp2.width, bmp2.height)

    val averageColor1 = getAverageOfPixelArray(pixels1)
    val averageColor2 = getAverageOfPixelArray(pixels2)

    val p1 = getPixelDeviateWeightsArray(pixels1, averageColor1)
    val p2 = getPixelDeviateWeightsArray(pixels2, averageColor2)

    val hammingDistance = getHammingDistance(p1, p2)
    return calSimilarity(hammingDistance)
}

private fun calSimilarity(hammingDistance: Int): Double {
    val length = 32 * 32
    var similarity = (length - hammingDistance) / length.toDouble()

    similarity = Math.pow(similarity, 2.0)
    return similarity
}

private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
    val width = bmpOriginal.width
    val height = bmpOriginal.height

    val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    val c = Canvas(bmpGrayscale)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.setSaturation(0f)
    val f = ColorMatrixColorFilter(cm)
    paint.colorFilter = f
    c.drawBitmap(bmpOriginal, 0f, 0f, paint)
    return bmpGrayscale
}

private fun getAverageOfPixelArray(pixels: IntArray): Int {
    var sumRed: Long = 0
    for (i in pixels.indices) {
        sumRed += android.graphics.Color.red(pixels[i]).toLong()
    }
    return (sumRed / pixels.size).toInt()
}

private fun getPixelDeviateWeightsArray(pixels: IntArray, averageColor: Int): IntArray {
    val dest = IntArray(pixels.size)
    for (i in pixels.indices) {
        dest[i] = if (android.graphics.Color.red(pixels[i]) - averageColor > 0) 1 else 0
    }
    return dest
}

private fun getHammingDistance(a: IntArray, b: IntArray): Int {
    var sum = 0
    for (i in a.indices) {
        sum += if (a[i] == b[i]) 0 else 1
    }
    return sum
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

                val cropImage = cropBitmapToAspectRatio(bitmap, 1f / 1f)
                val resizedBitmap = resizeBitmapWithMaxDimensions(cropImage, 1500)
                val compressedImage = compressAndReduceQuality(resizedBitmap, 40)

                onPhotoCaptured(compressedImage)

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