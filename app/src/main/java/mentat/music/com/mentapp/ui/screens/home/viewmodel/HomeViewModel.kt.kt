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
import kotlinx.coroutines.async // ¡Importante para llamadas paralelas!
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// import kotlinx.serialization.SerialName // Ya no se usa
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
// import kotlinx.serialization.modules.SerializersModule // Ya no se usa
// import kotlinx.serialization.modules.polymorphic // Ya no se usa
// import kotlinx.serialization.modules.subclass // Ya no se usa

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
// --- ARQUITECTURA DE DATOS SIMPLIFICADA ---

// 1. El 'CarouselItem' (para GUZZ, Spotify, etc. - sin cambios)
@Serializable
data class CarouselItem(
    val imageUrl: String? = null, // <-- ¡HECHO NULO!
    val targetUrl: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val appPackageName: String? = null
)

// 2. 'ConceptBlock' (¡ELIMINADO!)
//    (Tu 'ConceptItem.kt' que creaste por separado
//    se usará para la llamada de red)

// 3. La "Super-Clase" (¡MODIFICADA!)
@Serializable
data class AppData(
    val GUZZ: List<CarouselItem>? = null,
    val Spotify: List<CarouselItem>? = null,
    val Bandcamp: List<CarouselItem>? = null,
    val Soundcloud: List<CarouselItem>? = null,
    val YouTube: List<CarouselItem>? = null,
    // ¡¡ESTA LÍNEA HA CAMBIADO!!
    val Concepto: List<CarouselItem>? = null
)

// 4. El nuevo estado de la UI (sin cambios)
sealed class AppState {
    object Loading : AppState()
    data class Success(val data: AppData) : AppState()
    data class Error(val message: String) : AppState()
}

// 5. El "traductor" de JSON (¡ELIMINADO!)
//    (private val jsonSerializerModule = ... se ha borrado)

// --- FIN DE LA MODIFICACIÓN DE ARQUITECTURA ---


// --- EL VIEWMODEL ---
class HomeViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- (URL del JSON principal - sin cambios) ---
    private val JSON_URL = "https://www.mentat-music.com/mentapp/mentat_data_DEF.json"

    // --- ¡URL DEL JSON DE CONCEPTO AÑADIDA! ---
    private val CONCEPTO_JSON_URL = "https://www.mentat-music.com/mentapp/concepto.json"


    // --- (Lógica de Ktor - ¡SIMPLIFICADA!) ---
    private val ktorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                // ¡La línea de 'serializersModule' se ha borrado!
            })
        }
    }

    // --- (El estado ahora es 'AppState' - sin cambios) ---
    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        // Renombrado para más claridad
        loadAllData()
    }

    // --- ¡¡¡FUNCIÓN 'fetchData' REESCRITA!!! ---
    private fun loadAllData() {
        viewModelScope.launch {
            _appState.value = AppState.Loading
            try {
                // 1. Lanzamos AMBAS llamadas de red en paralelo
                val localDataJob = async {
                    // Carga el JSON principal (GUZZ, Spotify, etc.)
                    // (que incluye un 'Concepto' vacío o nulo)
                    ktorClient.get(JSON_URL).body<AppData>()
                }
                val remoteConceptJob = async {
                    // Carga tu 'concepto.json' (el array de 'ConceptItem')
                    ktorClient.get(CONCEPTO_JSON_URL).body<List<ConceptItem>>()
                }

                // 2. Esperamos a que ambas terminen
                val localData = localDataJob.await()
                val remoteConceptItems = remoteConceptJob.await()

                // 3. ¡LA TRADUCCIÓN!
                // Convertimos tu 'List<ConceptItem>' (de concepto.json)
                // en la 'List<CarouselItem>' (que usa la UI)
                val translatedConceptData = remoteConceptItems.map { conceptItem ->
                    CarouselItem(
                        title = conceptItem.title,
                        artist = conceptItem.artist,
                        imageUrl = conceptItem.imageUrl,
                        targetUrl = conceptItem.url_embed, // Mapeamos 'url_embed' a 'targetUrl'
                        appPackageName = null // Lo dejamos null
                    )
                }

                // 4. ¡LA FUSIÓN!
                // Creamos el objeto final, reemplazando el 'Concepto' vacío
                // del JSON principal por nuestra lista traducida.
                val finalData = localData.copy(
                    Concepto = translatedConceptData
                )

                // 5. ¡Éxito!
                _appState.value = AppState.Success(finalData)

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar datos (local o remoto)", e)
                _appState.value = AppState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // --- ¡AÑADIDO! Limpieza de Ktor ---
    override fun onCleared() {
        super.onCleared()
        ktorClient.close()
    }


    // --- (Lógica de Rotación - sin cambios) ---
    private val ROTATION_KEY = "rotationAngle"
    val rotationAngle: StateFlow<Float> = savedStateHandle.getStateFlow(ROTATION_KEY, BANDCAMP_START_ANGLE)
    fun updateRotationAngle(angle: Float) {
        savedStateHandle[ROTATION_KEY] = angle
    }

    // --- (Lógica de Estado de UI - sin cambios) ---
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