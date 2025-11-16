package mentat.music.com.mentapp.ui.screens.home.viewmodel

import kotlinx.serialization.Serializable

/**
 * Representa la estructura de un objeto
 * en el archivo 'concepto.json' remoto.
 */
@Serializable
data class ConceptItem(
    val title: String,
    val artist: String, // Usamos 'artist' para el subtítulo
    val imageUrl: String,
    val url_embed: String? // Nullable
)