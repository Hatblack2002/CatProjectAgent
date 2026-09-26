package com.example.ui.cat.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CatAmberPrimary
import com.example.ui.theme.CatBorder
import com.example.ui.theme.CatSurface
import com.example.ui.theme.ToyMousePink
import com.example.ui.theme.ToyMouseSilver
import com.example.ui.theme.ToyMouseWhite

/**
 * Animación insignia de CatProjectAgent:
 * El ratón blanco de juguete mecánico corre hacia adelante con la llave girando velozmente,
 * y el gato negro pixel art corre detrás con mirada curiosa y juguetona.
 * Representa: el sistema poniéndose en movimiento.
 */
@Composable
fun CatRunningMouseAnimation(
    modifier: Modifier = Modifier,
    useBannerArt: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "cat_mouse_loop")

    // Rotation of the mechanical winding key
    val keyRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "key_rotation"
    )

    // Running bounce & cycle
    val bounceY by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Forward and back playful sprint
    val runOffset by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sprint"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CatSurface)
            .border(1.dp, CatBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (useBannerArt) {
            // Render the high-contrast pixel art banner
            Image(
                painter = painterResource(id = R.drawable.cat_chasing_mouse_banner),
                contentDescription = "Gato persiguiendo al ratón de cuerda",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic overlay of the animated winding key over the toy mouse
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mouseKeyX = size.width * 0.76f + (runOffset * 0.6f)
                val mouseKeyY = size.height * 0.48f + bounceY

                // Draw rotating silver winding key over the toy mouse
                rotate(degrees = keyRotation, pivot = Offset(mouseKeyX, mouseKeyY)) {
                    // Stem
                    drawRect(
                        color = ToyMouseSilver,
                        topLeft = Offset(mouseKeyX - 2.5f, mouseKeyY - 14f),
                        size = Size(5f, 16f)
                    )
                    // Oval wing left
                    drawOval(
                        color = ToyMouseSilver,
                        topLeft = Offset(mouseKeyX - 16f, mouseKeyY - 20f),
                        size = Size(12f, 9f),
                        style = Stroke(width = 3.5f)
                    )
                    // Oval wing right
                    drawOval(
                        color = ToyMouseSilver,
                        topLeft = Offset(mouseKeyX + 4f, mouseKeyY - 20f),
                        size = Size(12f, 9f),
                        style = Stroke(width = 3.5f)
                    )
                }
            }
        } else {
            // Pure procedural vector pixel animation canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val groundY = h * 0.72f

                // Subtle starry grid
                drawCircle(CatBorder, radius = 2f, center = Offset(w * 0.2f, h * 0.25f))
                drawCircle(CatBorder, radius = 1.5f, center = Offset(w * 0.5f, h * 0.18f))
                drawCircle(CatBorder, radius = 2f, center = Offset(w * 0.85f, h * 0.3f))

                // Ground line
                drawLine(
                    color = CatBorder,
                    start = Offset(0f, groundY + 14f),
                    end = Offset(w, groundY + 14f),
                    strokeWidth = 2f
                )

                // 1. BLACK CAT (RUNNING)
                val catX = w * 0.30f + runOffset
                val catY = groundY + bounceY

                // Cat Body (sleek pixel-like curves)
                drawRoundRect(
                    color = Color(0xFF14131A),
                    topLeft = Offset(catX - 35f, catY - 24f),
                    size = Size(50f, 28f),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Cat Head
                drawCircle(
                    color = Color(0xFF14131A),
                    radius = 18f,
                    center = Offset(catX + 22f, catY - 18f)
                )

                // Cat Ears
                val leftEar = Path().apply {
                    moveTo(catX + 10f, catY - 28f)
                    lineTo(catX + 16f, catY - 42f)
                    lineTo(catX + 24f, catY - 28f)
                    close()
                }
                drawPath(leftEar, color = Color(0xFF14131A))
                drawPath(leftEar, color = ToyMousePink.copy(alpha = 0.5f), style = Stroke(width = 2f))

                val rightEar = Path().apply {
                    moveTo(catX + 24f, catY - 28f)
                    lineTo(catX + 30f, catY - 42f)
                    lineTo(catX + 36f, catY - 28f)
                    close()
                }
                drawPath(rightEar, color = Color(0xFF14131A))
                drawPath(rightEar, color = ToyMousePink.copy(alpha = 0.5f), style = Stroke(width = 2f))

                // Cat Eyes (Large, expressive, curious golden amber!)
                drawOval(
                    color = CatAmberPrimary,
                    topLeft = Offset(catX + 22f, catY - 22f),
                    size = Size(8f, 10f)
                )
                drawCircle(
                    color = Color(0xFF121212),
                    radius = 3f,
                    center = Offset(catX + 26f, catY - 17f)
                )

                // Tail (animated flick)
                val tailPath = Path().apply {
                    moveTo(catX - 35f, catY - 12f)
                    quadraticTo(catX - 52f, catY - 30f + bounceY, catX - 44f, catY - 42f)
                }
                drawPath(tailPath, color = Color(0xFF14131A), style = Stroke(width = 6f))

                // 2. WHITE TOY MOUSE (RUNNING AHEAD)
                val mouseX = w * 0.72f + (runOffset * 0.8f)
                val mouseY = groundY + (bounceY * 0.8f)

                // Mechanical Winding Key on its back (rotates continuously)
                rotate(degrees = keyRotation, pivot = Offset(mouseX, mouseY - 20f)) {
                    drawRect(
                        color = ToyMouseSilver,
                        topLeft = Offset(mouseX - 2f, mouseY - 28f),
                        size = Size(4f, 12f)
                    )
                    drawOval(
                        color = ToyMouseSilver,
                        topLeft = Offset(mouseX - 12f, mouseY - 34f),
                        size = Size(10f, 7f),
                        style = Stroke(width = 2.5f)
                    )
                    drawOval(
                        color = ToyMouseSilver,
                        topLeft = Offset(mouseX + 2f, mouseY - 34f),
                        size = Size(10f, 7f),
                        style = Stroke(width = 2.5f)
                    )
                }

                // Mouse Body (White, smooth rounded toy shape)
                drawOval(
                    color = ToyMouseWhite,
                    topLeft = Offset(mouseX - 18f, mouseY - 14f),
                    size = Size(36f, 22f)
                )

                // Mouse Nose
                drawCircle(
                    color = ToyMousePink,
                    radius = 3f,
                    center = Offset(mouseX + 19f, mouseY - 2f)
                )

                // Mouse Ear
                drawCircle(
                    color = ToyMousePink,
                    radius = 5.5f,
                    center = Offset(mouseX + 2f, mouseY - 12f)
                )

                // Toy Wheels (front and back)
                drawCircle(
                    color = ToyMouseSilver,
                    radius = 4.5f,
                    center = Offset(mouseX - 10f, mouseY + 8f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 2f,
                    center = Offset(mouseX - 10f, mouseY + 8f)
                )

                drawCircle(
                    color = ToyMouseSilver,
                    radius = 4.5f,
                    center = Offset(mouseX + 8f, mouseY + 8f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 2f,
                    center = Offset(mouseX + 8f, mouseY + 8f)
                )

                // Little spring tail
                val mouseTail = Path().apply {
                    moveTo(mouseX - 18f, mouseY - 2f)
                    cubicTo(
                        mouseX - 26f, mouseY - 8f,
                        mouseX - 28f, mouseY + 6f,
                        mouseX - 36f, mouseY + 2f
                    )
                }
                drawPath(mouseTail, color = ToyMousePink, style = Stroke(width = 2.5f))
            }
        }
    }
}
