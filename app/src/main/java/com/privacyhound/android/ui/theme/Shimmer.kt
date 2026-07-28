package com.privacyhound.android.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

fun Modifier.goldShimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            GoldDark.copy(alpha = 0.0f),
            GoldPrimary.copy(alpha = 0.4f),
            GoldLight.copy(alpha = 0.6f),
            GoldPrimary.copy(alpha = 0.4f),
            GoldDark.copy(alpha = 0.0f)
        ),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f)
    )

    this.background(shimmerBrush)
}

@Composable
fun GoldShimmerBar(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmerBar")
    val translateX by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerBarTranslate"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GoldDark.copy(alpha = 0.0f),
                        GoldPrimary.copy(alpha = 0.8f),
                        GoldLight,
                        GoldPrimary.copy(alpha = 0.8f),
                        GoldDark.copy(alpha = 0.0f)
                    ),
                    start = Offset(translateX, 0f),
                    end = Offset(translateX + 400f, 0f)
                )
            )
    )
}
