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
import android.view.TextureView
import androidx.media3.common.C // ¡Este es clave!

/**
 * Un Composable que reproduce un vídeo en bucle desde res/raw.
 * Está diseñado para silenciar el vídeo y quitar todos los controles.
 * Este es el "fallback" para APIs < 33.
 */
// (Imports: Asegúrate de tener TextureView y androidx.media3.common.C)
// (La anotación @OptIn se queda donde estaba, encima de la función)

@OptIn(UnstableApi::class)
@Composable
fun VideoBackground(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 1. Creamos la "pantalla tonta" (TextureView)
    //    Usamos 'remember' para que sobreviva a las recomposiciones.
    val textureView = remember {
        TextureView(context)
    }

    // 2. Creamos el "cerebro" (ExoPlayer)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // (La configuración del vídeo es la misma)
            val mediaItem = MediaItem.fromUri(
                Uri.parse("android.resource://${context.packageName}/${R.raw.captura_animacion}")
            )
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f

            // --- ¡¡¡LA NUEVA LÓGICA DE ESCALADO!!! ---
            // Le decimos AL PLAYER (no a la vista) que haga ZOOM (Crop).
            // Esta es la versión de "ExoPlayer" del RESIZE_MODE_ZOOM.
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }
    }

    // 3. Controlamos el Ciclo de Vida
    DisposableEffect(Unit) {
        // Cuando el Composable APARECE, conectamos el "cerebro" a la "pantalla"
        exoPlayer.setVideoTextureView(textureView)

        onDispose {
            // Cuando el Composable DESAPARECE, los desconectamos y liberamos
            exoPlayer.clearVideoTextureView(textureView)
            exoPlayer.release()
        }
    }

    // 4. Mostramos la "pantalla" (TextureView) en Compose
    AndroidView(
        factory = {
            // El factory ahora solo devuelve la "pantalla tonta"
            // que ya hemos creado y recordado.
            textureView
        },
        modifier = modifier // <-- Este sigue siendo tu .fillMaxSize()
    )
}

