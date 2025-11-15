package mentat.music.com.mentapp.ui.screens.home.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

// --- (Constantes de ángulo - sin cambios) ---
private const val NUM_ITEMS = 7
private val angleStep = (2 * Math.PI.toFloat() / NUM_ITEMS)
private val targetAngleRad = (Math.PI.toFloat() / 2.0f)
private val BANDCAMP_START_ANGLE = targetAngleRad - (angleStep * 5)

// --- (Lógica de Estado de UI - sin cambios) ---
private const val ANIMATING_OUT_KEY = "isAnimatingOut"
private const val CLICKED_INDEX_KEY = "clickedIconIndex"
private const val EXPANSION_FINISHED_KEY = "isExpansionFinished"

// --- (Fin Constantes) ---


// --- ¡¡¡MODIFICACIÓN AQUÍ!!! ---
// --- NUEVA ARQUITECTURA DE DATOS ---

// 1. El 'CarouselItem' (para GUZZ, Spotify, etc.)
@Serializable
data class CarouselItem(
    val imageUrl: String,
    val targetUrl: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val appPackageName: String? = null // ¡Campo para "refinar" URLs!
)

// 2. Las nuevas clases para "Concepto" (polimorfismo)
@Serializable
sealed interface ConceptBlock {
    @Serializable
    @SerialName("header") // Coincide con tu JSON: "type": "header"
    data class HeaderBlock(val text: String) : ConceptBlock

    @Serializable
    @SerialName("paragraph") // "type": "paragraph"
    data class ParagraphBlock(val text: String) : ConceptBlock

    @Serializable
    @SerialName("image") // "type": "image"
    data class ImageBlock(val imageUrl: String) : ConceptBlock

    @Serializable
    @SerialName("link") // "type": "link"
    data class LinkBlock(val title: String, val targetUrl: String) : ConceptBlock

    // (Añadiremos 'embed' aquí cuando lo implementemos)
}

// 3. La "Super-Clase" que representa el JSON entero
@Serializable
data class AppData(
    val GUZZ: List<CarouselItem>? = null,
    val Spotify: List<CarouselItem>? = null,
    val Bandcamp: List<CarouselItem>? = null,
    val Soundcloud: List<CarouselItem>? = null,
    val YouTube: List<CarouselItem>? = null,
    val Concepto: List<ConceptBlock>? = null
)

// 4. El nuevo estado de la UI (renombrado)
sealed class AppState {
    object Loading : AppState()
    data class Success(val data: AppData) : AppState() // ¡Ahora contiene AppData!
    data class Error(val message: String) : AppState()
}

// 5. El "traductor" de JSON ahora necesita saber sobre el polimorfismo
private val jsonSerializerModule = SerializersModule {
    polymorphic(ConceptBlock::class) {
        subclass(ConceptBlock.HeaderBlock::class)
        subclass(ConceptBlock.ParagraphBlock::class)
        subclass(ConceptBlock.ImageBlock::class)
        subclass(ConceptBlock.LinkBlock::class)
    }
}
// --- FIN DE LA MODIFICACIÓN DE ARQUITECTURA ---


// --- EL VIEWMODEL ---
class HomeViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- (URL del JSON - ¡TU URL REAL!) ---
    private val JSON_URL = "https://www.mentat-music.com/mentapp/mentat_data_DEF.json"

    // --- (Lógica de Ktor - ¡CON ARREGLO!) ---
    private val ktorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true // ¡Importante!
                serializersModule = jsonSerializerModule // ¡Le enseñamos "Concepto"!
            })
        }
    }

    // --- (El estado ahora es 'AppState') ---
    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _appState.value = AppState.Loading
            try {
                // ¡Ahora pedimos un objeto 'AppData' (la super-clase)!
                val data = ktorClient.get(JSON_URL).body<AppData>()
                _appState.value = AppState.Success(data)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar JSON", e)
                _appState.value = AppState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // --- (Lógica de Rotación - sin cambios) ---
    private val ROTATION_KEY = "rotationAngle"
    val rotationAngle: StateFlow<Float> = savedStateHandle.getStateFlow(ROTATION_KEY, BANDCAMP_START_ANGLE)
    fun updateRotationAngle(angle: Float) {
        savedStateHandle[ROTATION_KEY] = angle
    }


    val isAnimatingOut: StateFlow<Boolean> = savedStateHandle.getStateFlow(ANIMATING_OUT_KEY, false)
    val clickedIconIndex: StateFlow<Int> = savedStateHandle.getStateFlow(CLICKED_INDEX_KEY, -1)
    val isExpansionFinished: StateFlow<Boolean> = savedStateHandle.getStateFlow(EXPANSION_FINISHED_KEY, false)

    fun updateIsAnimatingOut(isAnimating: Boolean) {
        savedStateHandle[ANIMATING_OUT_KEY] = isAnimating
    }
    fun updateClickedIconIndex(index: Int) {
        savedStateHandle[CLICKED_INDEX_KEY] = index
    }
    fun updateIsExpansionFinished(isFinished: Boolean) {
        savedStateHandle[EXPANSION_FINISHED_KEY] = isFinished
    }
}