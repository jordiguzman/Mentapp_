package mentat.music.com.mentapp.ui.screens.home

// (Imports de RuntimeShader y drawWithCache eliminados)
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mentat.music.com.mentapp.R
import mentat.music.com.mentapp.ui.composables.AlbumCarousel
import mentat.music.com.mentapp.ui.composables.AttractorBackground
import mentat.music.com.mentapp.ui.composables.CircularDialLayout
// import mentat.music.com.mentapp.ui.composables.ConceptScreen // <-- ¡ELIMINADO!
import mentat.music.com.mentapp.ui.composables.TRANSITION_DURATION
import mentat.music.com.mentapp.ui.composables.VideoBackground
import mentat.music.com.mentapp.ui.composables.angleStep
import mentat.music.com.mentapp.ui.composables.menuItems
import mentat.music.com.mentapp.ui.composables.targetAngleRad
import mentat.music.com.mentapp.ui.screens.home.viewmodel.AppData
import mentat.music.com.mentapp.ui.screens.home.viewmodel.AppState
// import mentat.music.com.mentapp.ui.screens.home.viewmodel.ConceptBlock // <-- ¡ELIMINADO!
import mentat.music.com.mentapp.ui.screens.home.viewmodel.HomeViewModel
import kotlin.math.atan2
import kotlin.math.roundToInt
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

// --- (Definición de la fuente - sin cambios) ---
private val verdanaFontFamily = FontFamily(
    Font(R.font.verdana_regular, FontWeight.Normal),
    Font(R.font.verdana_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.verdana_bold, FontWeight.Bold),
    Font(R.font.verdana_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

@SuppressLint("LocalContextResourcesRead")
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {

    // --- (Lógica de estado del VM - sin cambios) ---
    val savedRotationAngle by homeViewModel.rotationAngle.collectAsState()
    val appState by homeViewModel.appState.collectAsState()
    val isAnimatingOut by homeViewModel.isAnimatingOut.collectAsState()
    val clickedIconIndex by homeViewModel.clickedIconIndex.collectAsState()
    val isExpansionFinished by homeViewModel.isExpansionFinished.collectAsState()

    // --- (Lógica de estado local - sin cambios) ---
    val rotationAngle = remember { Animatable(savedRotationAngle) }
    val scope = rememberCoroutineScope()
    var isDialIdle by remember { mutableStateOf(true) }
    var isFrozen by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val dialScale = remember { Animatable(1.0f) }

    // --- (Parámetros "para toquetear" - sin cambios) ---
    val bounceDamping = 0.5f
    val bounceStiffness = 150f
    val bounceStartScale = 1.1f

    val bounceSpec = spring<Float>(
        dampingRatio = bounceDamping,
        stiffness = bounceStiffness
    )

    // --- (Rebote Inicial - sin cambios) ---
    LaunchedEffect(Unit) {
        scope.launch {
            delay(100)
            dialScale.snapTo(bounceStartScale)
            dialScale.animateTo(
                targetValue = 1.0f,
                animationSpec = bounceSpec
            )
        }
    }
    // --- CÓDIGO PARA MOSTRAR LAS BARRAS DEL SISTEMA ---
    val view = LocalView.current
    val window = (view.context as Activity).window

    // Este LaunchedEffect se ejecuta una vez cuando HomeScreen aparece
    LaunchedEffect(key1 = window) {
        // 1. Le decimos a Windows que vuelva a dibujar "normal"
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // 2. Pedimos al controlador que MUESTRE las barras
        val controller = WindowCompat.getInsetsController(window, view)
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
    // ---------------------------------------------------

    // --- (Alfas - sin cambios) ---
    val dialSceneAlpha by animateFloatAsState(
        targetValue = if (isExpansionFinished) 0f else 1f,
        animationSpec = tween(durationMillis = TRANSITION_DURATION),
        label = "dialSceneAlpha"
    )
    val arrowsAlpha by animateFloatAsState(
        targetValue = if (isDialIdle && !isAnimatingOut) 0.4f else 0.0f,
        animationSpec = tween(300), label = "arrowsAlpha"
    )
    // ... (resto de lógica de donut - sin cambios) ...
    val iconPathRadius = 140.dp
    val donutPadding = 8.dp
    val donutThickness = 76.dp + (donutPadding * 2)
    val donutRadius = iconPathRadius
    val radiusPx = with(LocalDensity.current) { donutRadius.toPx() }
    val thicknessPx = with(LocalDensity.current) { donutThickness.toPx() }
    val arrowsYOffset = iconPathRadius

    // --- (Lógica de 'time' - ¡SE QUEDA AQUÍ!) ---
    val infiniteTransition = rememberInfiniteTransition(label = "shader time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 600000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "time"
    )
    var frozenTime by remember { mutableStateOf(0f) }

    // --- (Lógica de BackHandler - sin cambios) ---
    BackHandler(enabled = isAnimatingOut || isExpansionFinished) {
        scope.launch {
            homeViewModel.updateIsExpansionFinished(false)
            homeViewModel.updateIsAnimatingOut(false)
            delay(TRANSITION_DURATION.toLong())
            scope.launch {
                dialScale.snapTo(bounceStartScale)
                dialScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = bounceSpec
                )
            }
            homeViewModel.updateClickedIconIndex(-1)
            isFrozen = false
            rotationAngle.snapTo(homeViewModel.rotationAngle.value)
            isDialIdle = true
        }
    }

    // --- (Filtro para "apagar" - sin cambios) ---
    val desaturationFilter: ColorFilter = remember {
        val matrix = ColorMatrix()
        matrix.setToSaturation(0.5f)
        ColorFilter.colorMatrix(matrix)
    }


    Box( // El Box principal que contiene todo
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        // --- (CAPA 1: EL FONDO - Arreglado) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AttractorBackground(
                modifier = Modifier.fillMaxSize(),
                isFrozen = isAnimatingOut || isExpansionFinished, // <-- ¡Arreglado!
                frozenTime = frozenTime
            )
        } else {
            VideoBackground(modifier = Modifier.fillMaxSize())
        }


        // --- (BoxWithConstraints - sin cambios) ---
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val isPortrait = maxWidth < maxHeight

            // --- (ESCENA DEL DIAL - sin cambios) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(dialSceneAlpha)
                    .pointerInput(Unit) {
                        // ... (toda tu lógica de 'pointerInput' sin cambios) ...
                        if (isAnimatingOut || isExpansionFinished) return@pointerInput
                        var centerX = 0f
                        var centerY = 0f
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDialIdle = false
                                centerX = size.width / 2f
                                centerY = size.height / 2f
                                scope.launch { rotationAngle.stop() }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val startAngle = atan2(
                                    y = change.previousPosition.y - centerY,
                                    x = change.previousPosition.x - centerX
                                )
                                val endAngle = atan2(
                                    y = change.position.y - centerY,
                                    x = change.position.x - centerX
                                )
                                val angleDifference = endAngle - startAngle
                                scope.launch {
                                    rotationAngle.snapTo(rotationAngle.value + angleDifference)
                                }
                            },
                            onDragEnd = {
                                val currentAngle = rotationAngle.value
                                val currentOffset = currentAngle - targetAngleRad
                                val nearestIconIndex = -(currentOffset / angleStep).roundToInt()
                                val targetSnapAngle = targetAngleRad - (angleStep * nearestIconIndex)
                                scope.launch {
                                    rotationAngle.animateTo(
                                        targetValue = targetSnapAngle,
                                        animationSpec = spring(
                                            dampingRatio = 0.7f,
                                            stiffness = 100f
                                        )
                                    )
                                    homeViewModel.updateRotationAngle(targetSnapAngle)
                                    isDialIdle = true
                                    isFrozen = false
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // --- HIJO 1: El Título (Estático - sin cambios) ---
                Text(
                    text = "MENTAPP",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 22.sp,
                    fontFamily = verdanaFontFamily,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(if (isPortrait) Alignment.TopCenter else Alignment.TopStart)
                        .padding(32.dp)
                )

                // --- HIJO 2: El "Contenedor del Dial" (El que rebota - sin cambios) ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(dialScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    // (Donut, Flechas y DialLayout AHORA VIVEN AQUÍ DENTRO)
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // ... (toda tu lógica del Canvas Donut - sin cambios) ...
                        val gradientColors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.4f),
                            Color.Gray.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.95f)
                        )
                        val brush = Brush.sweepGradient(colors = gradientColors, center = this.center)
                        drawCircle(brush = brush, radius = radiusPx, style = Stroke(width = thicknessPx))
                        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = radiusPx - (thicknessPx / 2), style = Stroke(width = 1.5.dp.toPx()))
                        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = radiusPx + (thicknessPx / 2), style = Stroke(width = 2.dp.toPx()))
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = arrowsYOffset)
                            .alpha(arrowsAlpha),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ... (lógica de Flechas - sin cambios) ...
                        Image(painter = painterResource(id = R.drawable.outline_line_start_arrow_notch_24), contentDescription = "Girar izquierda", colorFilter = ColorFilter.tint(Color.Black), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(80.dp))
                        Image(painter = painterResource(id = R.drawable.outline_line_end_arrow_notch_24), contentDescription = "Girar derecha", colorFilter = ColorFilter.tint(Color.Black), modifier = Modifier.size(24.dp))
                    }

                    // (El Dial - sin cambios)
                    CircularDialLayout(
                        modifier = Modifier.fillMaxSize(),
                        currentRotation = rotationAngle.value,
                        iconPathRadius = iconPathRadius,
                        isAnimatingOut = isAnimatingOut,
                        clickedIconIndex = clickedIconIndex,
                        isExpansionFinished = isExpansionFinished,

                        onIconClick = { route, index ->
                            // ... (toda tu lógica de onIconClick - sin cambios) ...
                            if (!isAnimatingOut) {
                                scope.launch {
                                    val clickedItemName = menuItems[index].name
                                    val carruselItems = listOf(
                                        "GUZZ", "Spotify", "YouTube",
                                        "Concepto", "Bandcamp", "Soundcloud"
                                    )
                                    if (clickedItemName in carruselItems) {
                                        isFrozen = true
                                        frozenTime = time // <-- Capturamos el 'time'
                                        isDialIdle = false
                                        homeViewModel.updateIsAnimatingOut(true)
                                        homeViewModel.updateClickedIconIndex(index)
                                        homeViewModel.updateIsExpansionFinished(true)
                                        isFrozen = false
                                    } else {
                                        isFrozen = true
                                        frozenTime = time // <-- CapturJamos el 'time'
                                        delay(100)
                                        uriHandler.openUri(route)
                                        delay(500)
                                        isFrozen = false
                                    }
                                }
                            }
                        },

                        // --- ¡¡¡INICIO DEL CÓDIGO RESTAURADO!!! ---
                        // (Este es el código que te daba error)
                        contentFor = { item, isClickedIcon, isExpansionFinished, isActive ->
                            val iconTargetAlpha = when {
                                isClickedIcon && isAnimatingOut -> 0f
                                else -> 1f
                            }
                            val iconAnimatedAlpha by animateFloatAsState(
                                targetValue = iconTargetAlpha,
                                animationSpec = tween(durationMillis = TRANSITION_DURATION),
                                label = "iconContentAlpha"
                            )
                            Image(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.name,
                                contentScale = ContentScale.Fit,
                                colorFilter = if (isActive || isAnimatingOut) null else desaturationFilter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(iconAnimatedAlpha)
                            )
                        }
                        // --- ¡¡¡FIN DEL CÓDIGO RESTAURADO!!! ---

                    )
                } // --- FIN DEL "Contenedor del Dial" (que rebota) ---
            } // --- FIN DE LA ESCENA DEL DIAL ---


            // --- (CAPA 5: EL CARRUSEL) ---

            // --- ¡¡¡INICIO DE LA MODIFICACIÓN!!! ---

            // 1. (Lógica de AppData - sin cambios)
            val appData: AppData? = remember(appState) {
                when (val state = appState) {
                    is AppState.Success -> state.data
                    is AppState.Loading -> null
                    is AppState.Error -> {
                        Log.e("HomeScreen", "Error al cargar JSON: ${state.message}")
                        null
                    }
                }
            }
            val clickedItemName = if (clickedIconIndex != -1) menuItems[clickedIconIndex].name else null

            // 2. (¡LÓGICA DE DATOS MODIFICADA!)
            //    Ahora 'conceptData' es 'conceptDataAsCarousel'
            var carouselData: List<mentat.music.com.mentapp.ui.screens.home.viewmodel.CarouselItem>? = null
            var conceptDataAsCarousel: List<mentat.music.com.mentapp.ui.screens.home.viewmodel.CarouselItem>? = null // <-- ¡RENOMBRADO!

            if (appData != null && clickedItemName != null) {
                when (clickedItemName) {
                    "GUZZ" -> carouselData = appData.GUZZ
                    "Spotify" -> carouselData = appData.Spotify
                    "Bandcamp" -> carouselData = appData.Bandcamp
                    "Soundcloud" -> carouselData = appData.Soundcloud
                    "YouTube" -> {
                        carouselData = appData.YouTube?.map { item ->
                            item.copy(
                                imageUrl = "https://img.youtube.com/vi/${item.imageUrl}/0.jpg"
                            )
                        }
                    }
                    "Concepto" -> {
                        // ¡Ahora carga en la nueva variable!
                        conceptDataAsCarousel = appData.Concepto
                    }
                }
            }

            // 3. (¡LÓGICA DE ALFA MODIFICADA!)
            //    Ahora comprueba 'conceptDataAsCarousel'
            val carouselLayerTargetAlpha = when {
                isExpansionFinished && (appState is AppState.Loading || carouselData != null || conceptDataAsCarousel != null) -> 1f
                else -> 0f
            }
            // --- (FIN DE LA MODIFICACIÓN DE LÓGICA DE DATOS) ---


            val carouselLayerAnimatedAlpha by animateFloatAsState(
                targetValue = carouselLayerTargetAlpha,
                animationSpec = tween(durationMillis = TRANSITION_DURATION),
                label = "carouselLayerAlpha"
            )
            val brandColor = if (clickedIconIndex != -1) {
                menuItems[clickedIconIndex].brandColor
            } else {
                Color.Transparent
            }
            val isConceptMode = (clickedItemName == "Concepto")

            val carouselBoxModifier = if (isPortrait) {
                // Modo Retrato (Portrait)
                if (isConceptMode) {
                    // MODO CONCEPTO: Rectangular y alto
                    Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f) // 80% del alto
                } else {
                    // MODO DISCO: Cuadrado (como antes)
                    Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1f)
                }
            } else {
                // Modo Apaisado (Landscape)
                if (isConceptMode) {
                    // MODO CONCEPTO: Rectangular y ancho
                    Modifier
                        .fillMaxHeight(0.9f)
                        .fillMaxWidth(0.7f) // 70% del ancho
                } else {
                    // MODO DISCO: Cuadrado (como antes)
                    Modifier
                        .fillMaxHeight(0.9f)
                        .aspectRatio(1f)
                }
            }
// --- ¡¡¡FIN DE LA MODIFICACIÓN DE TAMAÑO!!! ---

            if (isExpansionFinished) {
                Box(
                    modifier = Modifier
                        .alpha(carouselLayerAnimatedAlpha)
                        .then(carouselBoxModifier),
                    contentAlignment = Alignment.Center
                ) {

                    // (HIJO 1: La Tarjeta "Box Padre" - sin cambios)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        brandColor.copy(alpha = 0.6f),
                                        brandColor.copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color.Gray.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.9f)
                                    )
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        // --- ¡¡¡INICIO DE LA MODIFICACIÓN DE CONTENIDO!!! ---

                        // 1. Unimos las dos listas en una sola
                        val itemsToShow = carouselData ?: conceptDataAsCarousel

                        // 2. Comprobamos si hay algo que mostrar
                        if (itemsToShow != null) {
                            // ¡Llamamos SIEMPRE a AlbumCarousel!
                            // Y le pasamos el navController
                            AlbumCarousel(
                                items = itemsToShow,
                                navController = navController,
                                isConceptMode = isConceptMode // <-- ¡LE PASAMOS LA BANDERA!
                            )
                        }
                        // (¡HEMOS BORRADO 'else if (conceptData != null)')
                        else if (appState is AppState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White.copy(alpha = 0.7f),
                                strokeWidth = 3.dp,
                                strokeCap = StrokeCap.Round
                            )
                        } else if (appState is AppState.Error) {
                            Text(
                                text = "Error al cargar datos.\nInténtalo de nuevo.",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontFamily = verdanaFontFamily
                            )
                        }
                        // --- ¡¡¡FIN DE LA MODIFICACIÓN DE CONTENIDO!!! ---
                    }

                    // (HIJO 2: Icono Flotante - sin cambios)
                    if (clickedIconIndex != -1) {
                        Image(
                            painter = painterResource(id = menuItems[clickedIconIndex].iconResId),
                            contentDescription = "Context Icon",
                            modifier = Modifier
                                .align(if (isPortrait) Alignment.TopCenter else Alignment.CenterStart)
                                .fillMaxWidth(0.15f)
                                .aspectRatio(1f)
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    val xOffset = -(placeable.width * 1.4f).roundToInt()
                                    val yOffset = -(placeable.height * 1.4f).roundToInt()

                                    layout(placeable.width, placeable.height) {
                                        if (isPortrait) {
                                            placeable.placeRelative(x = 0, y = yOffset) // Vertical
                                        } else {
                                            placeable.placeRelative(x = xOffset, y = 0) // Horizontal
                                        }
                                    }
                                }
                                .alpha(0.5f) // Tu valor
                        )
                    }
                } // Fin del "Stack"
            } // Fin de if (isExpansionFinished)
        } // --- FIN DE CAPA 5 (BoxWithConstraints) ---
    }
}


// --- (Vista Previa - sin cambios) ---
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navController = rememberNavController()
    )
}