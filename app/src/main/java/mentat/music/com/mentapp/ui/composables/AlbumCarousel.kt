package mentat.music.com.mentapp.ui.composables

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.request.RequestOptions
import mentat.music.com.mentapp.R
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
    items: List<CarouselItem>
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- (Pager - sin cambios) ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) { pageIndex ->
            val item = items[pageIndex]
            AlbumCard(item = item)
        }

        Spacer(Modifier.height(24.dp))

        // --- (Indicador de puntos - sin cambios) ---
        HorizontalPagerIndicator(
            pagerState = pagerState,
            pageCount = items.size
        )
    }
}

/**
 * La tarjeta individual
 * ¡MODIFICADA con altura fija de texto y Spacer elástico!
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumCard(
    item: CarouselItem
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val isClickable = item.targetUrl != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // --- ¡¡¡MODIFICACIÓN AQUÍ!!! ---
        // 'Arrangement.Center' es demasiado simple y causa el bug
        // 'Arrangement.Top' empuja la imagen hacia arriba
        verticalArrangement = Arrangement.Top
    ) {
        // --- 1. LA IMAGEN (La tarjeta visual) ---
        GlideImage(
            model = item.imageUrl,
            contentDescription = item.title ?: "Portada",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
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
                    if (item.appPackageName != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.targetUrl))
                        intent.setPackage(item.appPackageName)
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            uriHandler.openUri(item.targetUrl)
                        }
                    } else {
                        uriHandler.openUri(item.targetUrl)
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

        // --- ¡¡¡MODIFICACIÓN AQUÍ!!! ---
        // --- 2. EL ESPACIADOR (Ahora es elástico) ---
        // Este 'Spacer' elástico empuja el texto hacia abajo
        // y ocupa todo el espacio sobrante.
        Spacer(Modifier.weight(1f))

        // --- 3. LOS TEXTOS (Con altura fija) ---
        item.title?.let { title ->
            Text(
                text = title.uppercase(),
                fontSize = 16.sp, // Tu ajuste
                fontWeight = FontWeight.Normal, // Tu ajuste
                color = Color.White,
                textAlign = TextAlign.Center,
                fontFamily = verdanaFontFamily,
                maxLines = 2, // Permite 2 líneas
                overflow = TextOverflow.Ellipsis, // Pone "..." si es más largo
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Que no toque los bordes
                    .height(42.dp) // ¡Altura fija para 2 líneas!
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
                maxLines = 1, // El artista solo 1 línea
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(20.dp) // ¡Altura fija para 1 línea!
            )
        }
    } // --- FIN DE LA COLUMN (TARJETA) ---
}


// --- (HorizontalPagerIndicator - sin cambios) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    pageCount: Int,
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
        repeat(pageCount) { iteration ->
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