package mentat.music.com.mentapp.ui.navigation

sealed class AppScreens(val route: String) {

    // --- ¡AÑADIDO OTRA VEZ! ---
    object SplashScreen : AppScreens("splash_screen")
    // -------------------------
    object HomeScreen : AppScreens("home_screen")
    object MusicOverviewScreen : AppScreens("music_overview_screen")

    // Ruta para el detalle, necesita un "argumento" (el ID del álbum)
    object AlbumDetailScreen : AppScreens("album_detail_screen/{albumId}") {
        // Función de ayuda para construir la ruta con el ID
        fun createRoute(albumId: String) = "album_detail_screen/$albumId"
    }

    // Ruta para la web, necesita un "argumento" (la URL)
    object WebViewScreen : AppScreens("webview_screen/{url}") {
        // Función de ayuda para construir la ruta con la URL
        // (Necesitamos codificar la URL para que sea segura)
        fun createRoute(url: String): String {
            val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
            return "webview_screen/$encodedUrl"
        }
    }
}