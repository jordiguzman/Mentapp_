package mentat.music.com.mentapp.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * El "Especialista en Temblores".
 * Centraliza toda la lógica de vibración para tener control total.
 */
class VibrationHelper(context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * EL "PUM": Golpe seco, pesado y autoritario.
     * Para confirmar acciones (Clics, Entradas).
     */
    fun vibrateClick() {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 80ms de duración, 255 de potencia (MÁXIMA)
                vibrator.vibrate(VibrationEffect.createOneShot(80, 255))
            } else {
                // Fallback para móviles muy viejos
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        }
    }

    /**
     * EL "TICK": Golpe metálico, corto y preciso.
     * Para el giro del dial o paso de cartas.
     */
    fun vibrateTick() {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 15ms de duración, 100 de potencia (MEDIA-BAJA)
                // Lo suficientemente corto para sentirse "mecánico"
                vibrator.vibrate(VibrationEffect.createOneShot(15, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        }
    }
}

/**
 * Función "fábrica" para usarlo fácilmente en Compose.
 * Solo tienes que llamar a `val vibrator = rememberVibrator()`
 */
@Composable
fun rememberVibrator(): VibrationHelper {
    val context = LocalContext.current
    return remember(context) {
        VibrationHelper(context)
    }
}