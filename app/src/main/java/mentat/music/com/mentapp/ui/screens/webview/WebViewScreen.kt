package mentat.music.com.mentapp.ui.screens.webview

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import java.net.URLDecoder

@Composable
fun WebViewScreen(navController: NavController, url: String?) {
    val decodedUrl = remember(url) {
        if (url != null) URLDecoder.decode(url, "UTF-8") else null
    }

    // --- ¡CAMBIO AQUÍ! ---
    // 1. La Columna ahora ocupa toda la pantalla y es negra
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Un botón "Volver" un poco más visible
        Text(
            text = "< Volver",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.popBackStack() }
                .padding(16.dp)
        )

        if (decodedUrl != null) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        // Configuración del WebView
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        // Para que el fondo de la web sea transparente si puede
                        setBackgroundColor(0x00000000)

                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        loadUrl(decodedUrl)
                    }
                },
                // 2. El WebView "pesa" 1f, ocupando todo el espacio restante
                modifier = Modifier.weight(1f)
            )
        } else {
            // ... (el texto de error)
        }
    }
}