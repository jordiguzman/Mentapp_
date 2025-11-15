package mentat.music.com.mentapp.ui.screens.detail

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

// Acepta el NavController y el ID del álbum
@Composable
fun AlbumDetailScreen(navController: NavController, albumId: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Estás en AlbumDetailScreen\nÁlbum ID: $albumId\n\nTocar para volver",
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier.clickable {
                navController.popBackStack() // El comando para "volver atrás"
            }
        )
    }
}