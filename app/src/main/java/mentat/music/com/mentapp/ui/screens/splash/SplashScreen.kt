package mentat.music.com.mentapp.ui.screens.splash

// --- IMPORTS PARA MODO INMERSIVO ---
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
// --- OTROS IMPORTS ---
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import mentat.music.com.mentapp.R
import mentat.music.com.mentapp.ui.navigation.AppScreens

// --- Definición de la fuente ---
private val verdanaFontFamily = FontFamily(
    Font(R.font.verdana_regular, FontWeight.Normal),
    Font(R.font.verdana_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.verdana_bold, FontWeight.Bold),
    Font(R.font.verdana_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

/**
 * La pantalla de Splash "cinematográfica" (Strobe/Glitch).
 * Usa una IMAGEN ESTÁTICA de fondo para ser 100% compatible y rápida.
 */
@Composable
fun SplashScreen(
    navController: NavController
) {
    // --- MODO INMERSIVO (PARA PANTALLA COMPLETA) ---
    val view = LocalView.current
    val window = (view.context as Activity).window

    // Usamos un LaunchedEffect *separado* para el modo inmersivo
    // para que se ejecute inmediatamente y no dependa del "Unit"
    LaunchedEffect(key1 = window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    // -----------------------------------------------

    // 1. Estado para controlar el "flash" (tu coreografía)
    var isTextVisible by remember { mutableStateOf(false) }

    // 2. La Coreografía (tu coreografía)
    // Usamos 'Unit' como clave para que se ejecute solo una vez
    LaunchedEffect(key1 = Unit) {
        // Fase 1: Fondo (2s)
        isTextVisible = false
        delay(2000)

        // Fase 2: Corte 1 (Flash rápido)
        isTextVisible = true
        delay(150)

        // Fase 3: Fondo 2 (Flash rápido)
        isTextVisible = false
        delay(200)

        // Fase 4: Corte 2 (Flash rápido)
        isTextVisible = true
        delay(150)

        // Fase 5: Aceleración
        isTextVisible = false
        delay(100)
        isTextVisible = true
        delay(100)
        isTextVisible = false
        delay(50)
        isTextVisible = true
        delay(50)

        // Fase 6: Navegar a Home
        navController.navigate(AppScreens.HomeScreen.route) {
            // Borra el Splash del historial
            popUpTo(AppScreens.SplashScreen.route) {
                inclusive = true
            }
        }
    }

    // 3. El Escenario (Las 2 capas apiladas)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Capa 1: El Fondo (Imagen estática)
        Image(
            painter = painterResource(id = R.drawable.ic_fondo_fijo), // ¡TU CAPTURA!
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Capa 2: El Corte a Negro (Tu "glitch")
        AnimatedVisibility(
            visible = isTextVisible,
            enter = fadeIn(animationSpec = tween(0)),
            exit = fadeOut(animationSpec = tween(0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MENTAPP",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontFamily = verdanaFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}