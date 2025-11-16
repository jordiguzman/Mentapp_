package mentat.music.com.mentapp.ui.composables

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
// --- ¡¡¡IMPORTS NUEVOS PARA EL SCROLL!!! ---
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
// -----------------------------------------
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.request.RequestOptions
import mentat.music.com.mentapp.R
import mentat.music.com.mentapp.ui.navigation.AppScreens
import mentat.music.com.mentapp.ui.screens.home.viewmodel.CarouselItem

// --- (Definición de la fuente - sin cambios) ---
private val verdanaFontFamily = FontFamily(
    Font(R.font.verdana_regular, FontWeight.Normal),
    Font(R.font.verdana_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.verdana_bold, FontWeight.Bold),
    Font(R.font.verdana_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun AlbumCarousel(
    modifier: Modifier = Modifier,
    items: List<CarouselItem>,
    navController: NavController,
    isConceptMode: Boolean // <-- ¡¡¡AÑADIDO!!!
) {
    val pagerState = rememberPagerState { items.size }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) { pageIndex ->
            val item = items[pageIndex]
            AlbumCard(
                item = item,
                navController = navController,
                isConceptMode = isConceptMode // <-- ¡¡¡AÑADIDO!!!
            )
        }

        Spacer(Modifier.height(24.dp))

        HorizontalPagerIndicator(
            pagerState = pagerState
        )
    }
}

/**
 * La tarjeta individual
 * (Ahora con "Modo Concepto" y scroll)
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumCard(
    item: CarouselItem,
    navController: NavController,
    isConceptMode: Boolean // <-- ¡¡¡AÑADIDO!!!
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val isClickable = item.targetUrl != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // (Esto está bien para ambos modos)
    ) {

        // --- ¡¡¡MODIFICACIÓN DE TAMAÑO DE IMAGEN!!! ---
        // 1. Decidimos el aspect ratio
        val imageAspectRatio = if (isConceptMode) 1.5f else 1f // 1.5f es rectangular

        // --- 1. LA IMAGEN ---
        if (item.imageUrl != null) {
            GlideImage(
                model = item.imageUrl,
                contentDescription = item.title ?: "Portada",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(imageAspectRatio) // <-- ¡Aplicamos el ratio dinámico!
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        ambientColor = Color.White.copy(alpha = 0.7f),
                        spotColor = Color.White.copy(alpha = 0.7f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(enabled = isClickable) {
                        if (item.targetUrl == null) return@clickable

                        // 1. Lógica "App-First" (Bandcamp, etc.)
                        if (item.appPackageName != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.targetUrl))
                            intent.setPackage(item.appPackageName)
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                uriHandler.openUri(item.targetUrl)
                            }
                        }
                        // 2. Lógica "Concepto" (WebView)
                        else {
                            navController.navigate(
                                AppScreens.WebViewScreen.createRoute(item.targetUrl)
                            )
                        }
                    },
                contentScale = ContentScale.Crop,
                requestBuilderTransform = {
                    it.apply(
                        RequestOptions()
                            .override(600)
                            .skipMemoryCache(true)
                    )
                }
            ) // --- FIN DE GLIDEIMAGE ---
        }

        // --- ¡¡¡MODIFICACIÓN DE LAYOUT DE TEXTO!!! ---
        if (isConceptMode) {

            // --- MODO CONCEPTO (Scrollable) ---
            Spacer(Modifier.height(16.dp)) // Espacio entre imagen y título

            item.title?.let { title ->
                Text(
                    text = title.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = verdanaFontFamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Spacer(Modifier.height(8.dp)) // Espacio entre título y texto

            item.artist?.let { artistText ->
                // ¡El contenedor con Scroll!
                Column(
                    modifier = Modifier
                        .fillMaxSize() // Ocupa el resto del espacio
                        .padding(horizontal = 12.dp) // Un poco de padding
                        .verticalScroll(rememberScrollState()) // ¡AQUÍ ESTÁ EL SCROLL!
                ) {
                    Text(
                        text = artistText, // El texto largo
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start, // El texto largo mejor alineado al inicio
                        fontFamily = verdanaFontFamily,
                        // Sin 'maxLines' ni 'height'
                    )
                    Spacer(Modifier.height(16.dp)) // Un poco de espacio al final
                }
            }

        } else {

            // --- MODO DISCO (Original, texto fijo abajo) ---
            Spacer(Modifier.weight(1f)) // Espaciador elástico

            item.title?.let { title ->
                Text(
                    text = title.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = verdanaFontFamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(42.dp) // Altura fija
                )
            }
            item.artist?.let { artist ->
                Text(
                    text = artist,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontFamily = verdanaFontFamily,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(20.dp) // Altura fija
                )
            }
        } // --- FIN DEL IF/ELSE ---

    } // --- FIN DE LA COLUMN (TARJETA) ---
}


// --- (HorizontalPagerIndicator - sin cambios) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.5f)
) {
    Row(
        modifier = modifier
            .height(30.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) activeColor else inactiveColor
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}