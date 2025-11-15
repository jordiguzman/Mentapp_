package mentat.music.com.mentapp.ui.composables

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import mentat.music.com.mentapp.R // ¡Importante para el R.raw...!
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import android.view.ViewGroup

/**
 * Un Composable que reproduce un vídeo en bucle desde res/raw.
 * Está diseñado para silenciar el vídeo y quitar todos los controles.
 * Este es el "fallback" para APIs < 33.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoBackground(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 1. Recordamos el ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Construimos el MediaItem desde nuestro recurso en res/raw
            val mediaItem = MediaItem.fromUri(
                "android.resource://${context.packageName}/${R.raw.captura_animacion}".toUri()
            )
            setMediaItem(mediaItem)
            prepare()

            // Configuración clave:
            playWhenReady = true      // Empieza a sonar solo
            repeatMode = Player.REPEAT_MODE_ONE // ¡Bucle infinito!
            volume = 0f               // Lo silenciamos
        }
    }

    // 2. Controlamos el ciclo de vida
    DisposableEffect(Unit) {
        onDispose {
            // Cuando el Composable desaparece, liberamos el reproductor
            exoPlayer.release()
        }
    }

    // 3. Usamos AndroidView para mostrar el PlayerView
    AndroidView(
        factory = {
            PlayerView(it).apply {
                // --- ¡¡¡EL ARREGLO ESTÁ AQUÍ!!! ---

                // 1. Preparamos el "escenario" (la vista)
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                player = exoPlayer

            }
        },
        modifier = modifier
    )
}