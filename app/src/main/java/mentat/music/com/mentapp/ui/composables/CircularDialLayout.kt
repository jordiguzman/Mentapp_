package mentat.music.com.mentapp.ui.composables

import android.os.Build
import androidx.annotation.DrawableRes
// import androidx.annotation.RequiresApi // <-- ¡ELIMINADO!
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow // <-- ¡AÑADIDO!
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mentat.music.com.mentapp.R
import mentat.music.com.mentapp.ui.navigation.AppScreens
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
// ¡NUEVA IMPORTACIÓN!
import mentat.music.com.mentapp.ui.composables.TRANSITION_DURATION

// --- (data class MenuItem y menuItems - sin cambios) ---
data class MenuItem(
    val name: String,
    @DrawableRes val iconResId: Int,
    val route: String,
    val brandColor: Color
)
val menuItems = listOf(
    MenuItem("GUZZ", R.drawable.ic_menu_guzz, "guzz_screen", Color.White),
    MenuItem("Spotify", R.drawable.ic_menu_streams, "spotify_screen", Color(0xFF1DB954)),
    MenuItem("Social", R.drawable.ic_menu_social, "https://bsky.app/profile/juanmentat.bsky.social", Color(0xFF0085FF)),
    MenuItem("YouTube", R.drawable.ic_menu_youtube, "youtube_screen", Color(0xFFFF0000)),
    MenuItem("Concepto", R.drawable.ic_menu_concept, "https://www.mentat-music.com/site/concepto/", Color(0xFF8A2BE2)),
    MenuItem("Bandcamp", R.drawable.ic_menu_bandcamp, "bandcamp_screen", Color(0xFF629AA9)),
    MenuItem("Soundcloud", R.drawable.ic_menu_soundcloud, "soundcloud_screen", Color(0xFFFF5500))
)
val angleStep = (2 * Math.PI.toFloat() / menuItems.size)
val targetAngleRad = (Math.PI.toFloat() / 2.0f)


// @RequiresApi(Build.VERSION_CODES.S) // <-- ¡ELIMINADO!
@Composable
fun CircularDialLayout(
    modifier: Modifier = Modifier,
    currentRotation: Float,
    iconPathRadius: Dp,
    isAnimatingOut: Boolean,
    clickedIconIndex: Int,
    isExpansionFinished: Boolean,
    onIconClick: (route: String, index: Int) -> Unit,
    contentFor: @Composable (
        item: MenuItem,
        isClickedIcon: Boolean,
        isExpansionFinished: Boolean,
        isActive: Boolean
    ) -> Unit
) {
    val radiusPx = with(LocalDensity.current) { iconPathRadius.toPx() }

    Layout(
        modifier = modifier,
        content = {
            menuItems.forEachIndexed { index, item ->

                // --- (Lógica de ángulo y estado - sin cambios) ---
                val angle = (angleStep * index) + currentRotation
                val normalizedAngle = (angle % (2 * Math.PI.toFloat()) + 2 * Math.PI.toFloat()) % (2 * Math.PI.toFloat())
                val targetAngleNorm = (targetAngleRad % (2 * Math.PI.toFloat()) + 2 * Math.PI.toFloat()) % (2 * Math.PI.toFloat())
                val diff = abs(normalizedAngle - targetAngleNorm)
                val isActive = (diff < 0.05f || abs(diff - 2 * Math.PI.toFloat()) < 0.05f)
                val isClickedIcon = (index == clickedIconIndex)

                // --- (Lógica de animación de tamaño - 76.dp - sin cambios) ---
                val targetSize = when {
                    isAnimatingOut && isClickedIcon -> 1000.dp
                    isAnimatingOut && !isClickedIcon -> 48.dp
                    isActive -> 72.dp // <-- ¡Tu tamaño grande!
                    else -> 48.dp
                }
                // --- (Animación de tamaño - sin cambios) ---
                val animatedSize by animateDpAsState(
                    targetValue = targetSize,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION), // <-- ¡TIEMPO NUEVO!
                    label = "sizeAnimation"
                )

                // --- (Animación de alfa - sin cambios) ---
                val containerTargetAlpha = when {
                    isAnimatingOut && !isClickedIcon -> 0.0f
                    else -> 1.0f
                }
                val containerAnimatedAlpha by animateFloatAsState(
                    targetValue = containerTargetAlpha,
                    animationSpec = tween(durationMillis = TRANSITION_DURATION), // <-- ¡TIEMPO NUEVO!
                    label = "containerAlpha"
                )

                // --- El Contenedor Box ---
                Box(
                    modifier = Modifier
                        .alpha(containerAnimatedAlpha)
                        .size(animatedSize)
                        .then(if (isActive && !isAnimatingOut) Modifier.clickable {
                            onIconClick(item.route, index)
                        } else Modifier),
                    contentAlignment = Alignment.Center
                ) {

                    // ==========================================================
                    // --- ¡¡¡INICIO DEL CAMBIO DE COMPATIBILIDAD!!! ---
                    // ==========================================================
                    if (!isAnimatingOut) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.9f)
                                .offset(x = 6.dp, y = 6.dp) // <-- Tu ajuste
                                // Comprobamos la versión del SDK
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        // --- API 31+ (Android 12): Tu "sombra a la brava" ---
                                        Modifier
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.2f)) // <-- Tu ajuste
                                            .blur(24.dp) // <-- Tu ajuste
                                    } else {
                                        // --- API < 31 (Android 11 o menos): Fallback "glow" ---
                                        Modifier.shadow(
                                            elevation = 16.dp,
                                            shape = CircleShape,
                                            clip = false, // 'clip = false' para que el "glow" se extienda
                                            // Aplicamos la transparencia al color de la sombra
                                            spotColor = Color.Black.copy(alpha = 0.3f),
                                            ambientColor = Color.Black.copy(alpha = 0.3f)
                                        )
                                    }
                                )
                        )
                    }
                    // ==========================================================
                    // --- ¡¡¡FIN DEL CAMBIO DE COMPATIBILIDAD!!! ---
                    // ==========================================================


                    // --- (Contenido - sin cambios) ---
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    ) {
                        contentFor(
                            item,
                            isClickedIcon,
                            isExpansionFinished,
                            isActive
                        )
                    }
                }
            }
        }
    ) { measurables, constraints ->
        // --- (Lógica de Layout y 'placeRelative' - sin cambios) ---
        val placables = measurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val centerX = layoutWidth / 2
        val centerY = layoutHeight / 2

        layout(layoutWidth, layoutHeight) {
            placables.forEachIndexed { index, placable ->
                // ¡LÓGICA SIMPLIFICADA! (Arreglo de "Inconsistencia")
                // Todos los iconos se quedan en su sitio en el dial.
                val angle = (angleStep * index) + currentRotation
                val x = (centerX + radiusPx * cos(angle.toDouble())).toInt() - (placable.width / 2)
                val y = (centerY + radiusPx * sin(angle.toDouble())).toInt() - (placable.height / 2)
                placable.placeRelative(x, y)
            }
        }
    }
}