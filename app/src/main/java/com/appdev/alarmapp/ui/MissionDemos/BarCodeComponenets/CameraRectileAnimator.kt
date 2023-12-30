import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke


@Composable
fun CameraReticleAnimator(
    graphicOverlay: DrawScope,
    color: Color = Color.White
) {
    var rippleAlpha by remember { mutableStateOf(0f) }
    var rippleSize by remember { mutableStateOf(0f) }
    var rippleStrokeWidth by remember { mutableStateOf(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    val rippleFadeIn = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = DURATION_RIPPLE_FADE_IN_MS, easing = LinearEasing)
        ), label = ""
    )

    val rippleFadeOut = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = DURATION_RIPPLE_FADE_OUT_MS,
                easing = LinearEasing,
                delayMillis = START_DELAY_RIPPLE_FADE_OUT_MS
            ),
        ), label = ""
    )

    val rippleExpand = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = DURATION_RIPPLE_EXPAND_MS,
                easing = FastOutSlowInEasing,
                delayMillis = START_DELAY_RIPPLE_EXPAND_MS
            ),
        ), label = ""
    )

    val rippleStrokeWidthShrink = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = DURATION_RIPPLE_STROKE_WIDTH_SHRINK_MS,
                easing = FastOutSlowInEasing,
                delayMillis = START_DELAY_RIPPLE_STROKE_WIDTH_SHRINK_MS
            ),
        ), label = ""
    )

    rippleAlpha = rippleFadeIn.value * rippleFadeOut.value
    rippleSize = rippleExpand.value
    rippleStrokeWidth = rippleStrokeWidthShrink.value

    graphicOverlay.drawCircle(
        color = color,
        alpha = rippleAlpha,
        radius = rippleSize * 50f * rippleStrokeWidth, // Scale radius for stroke width
        center = Offset.Zero, // Assuming you want the circle centered
        style = Stroke(),  // Draw only the stroke
        colorFilter = null,
        blendMode = BlendMode.SrcOver
    )
}



// Constants (adjust as needed)
private const val DURATION_RIPPLE_FADE_IN_MS = 333
private const val DURATION_RIPPLE_FADE_OUT_MS = 500
private const val DURATION_RIPPLE_EXPAND_MS = 833
private const val DURATION_RIPPLE_STROKE_WIDTH_SHRINK_MS = 833
private const val START_DELAY_RIPPLE_FADE_OUT_MS = 667
private const val START_DELAY_RIPPLE_EXPAND_MS = 333
private const val START_DELAY_RIPPLE_STROKE_WIDTH_SHRINK_MS = 333