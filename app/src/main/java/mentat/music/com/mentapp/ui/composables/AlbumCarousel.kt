package mentat.music.com.mentapp.ui.composables

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import mentat.music.com.mentapp.ui.VibrationHelper
import mentat.music.com.mentapp.ui.navigation.AppScreens
import mentat.music.com.mentapp.ui.rememberVibrator
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
    isConceptMode: Boolean,
    initialPage: Int, // <-- Estado de control del ViewModel (Lectura)
    onPageChanged: (Int) -> Unit // <-- Callback para informar al ViewModel (Escritura)
) {
    val sidePadding = if (isConceptMode) 16.dp else 48.dp
    val vibrator = rememberVibrator() // Asumo que esta función está disponible

    // 1. Inicializa el PagerState. Usa initialPage solo en la primera composición
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { items.size }
    )

    // 2. Control (UI -> VM): Notifica los cambios de la UI al ViewModel (Esto es correcto)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { pageIndex ->
            // vibrator.vibrateTick()
            onPageChanged(pageIndex)
        }
    }

    // 3. CONTROL (VM -> UI): ¡EL FIX! Sincronización manual.
    // Si 'initialPage' (el estado del ViewModel) cambia, forzamos el scroll.
    LaunchedEffect(initialPage) {
        // Usamos scrollToPage para forzar un cambio inmediato al estado del VM.
        // Se ejecuta cada vez que initialPage cambia (ej. al volver del 'back').
        if (pagerState.currentPage != initialPage) {
            pagerState.scrollToPage(initialPage)
        }
    }
    // -------------------------------

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
            contentPadding = PaddingValues(horizontal = sidePadding),
            pageSpacing = if (isConceptMode) 16.dp else 0.dp
        ) { pageIndex ->
            val item = items[pageIndex]
            AlbumCard(
                item = item,
                navController = navController,
                isConceptMode = isConceptMode
            )
        }

        Spacer(Modifier.height(24.dp))

        HorizontalPagerIndicator(
            pagerState = pagerState
        )
    }
}

/**
 * La tarjeta individual (sin cambios en su lógica interna)
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumCard(
    item: CarouselItem,
    navController: NavController,
    isConceptMode: Boolean
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val vibrator = rememberVibrator()
    val isClickable = item.targetUrl != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        // --- 1. LA IMAGEN ---
        val imageAspectRatio = if (isConceptMode) 1.5f else 1f

        if (item.imageUrl != null) {
            GlideImage(
                model = item.imageUrl,
                contentDescription = item.title ?: "Portada",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(imageAspectRatio)
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

                        vibrator.vibrateClick() // <-- VIBRACIÓN DE CLIC

                        if (item.appPackageName != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.targetUrl))
                            intent.setPackage(item.appPackageName)
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                uriHandler.openUri(item.targetUrl)
                            }
                        } else {
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
            )
        }

        // --- 2. TEXTOS ---
        if (isConceptMode) {
            // ... (Lógica de Scroll sin cambios) ...
            Spacer(Modifier.height(16.dp))

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
            Spacer(Modifier.height(8.dp))
            item.artist?.let { artistText ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = artistText,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start,
                        fontFamily = verdanaFontFamily,
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

        } else {
            // --- MODO DISCO (Lógica de Disco sin cambios) ---
            Spacer(Modifier.weight(1f))
            item.title?.let { title ->
                Text(
                    text = title.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = verdanaFontFamily,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            Spacer(Modifier.height(4.dp))
            item.artist?.let { artist ->
                Text(
                    text = artist,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontFamily = verdanaFontFamily,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
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