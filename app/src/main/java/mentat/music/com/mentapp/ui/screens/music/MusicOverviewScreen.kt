package mentat.music.com.mentapp.ui.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mentat.music.com.mentapp.ui.navigation.AppScreens

// Acepta el NavController para poder navegar "hacia adelante"
@Composable
fun MusicOverviewScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Estás en MusicOverviewScreen\n(El Carrusel irá aquí)\n\nTocar para ir al Detalle",
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier.clickable {
                // Navegamos al detalle, pasando un ID de prueba
                navController.navigate(AppScreens.AlbumDetailScreen.createRoute("solar_system"))
            }
        )
    }
}