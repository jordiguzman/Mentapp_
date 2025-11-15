package mentat.music.com.mentapp.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import mentat.music.com.mentapp.R
import mentat.music.com.mentapp.ui.screens.home.viewmodel.ConceptBlock

// --- (Definimos la fuente, la necesitamos para los textos) ---
private val verdanaFontFamily = FontFamily(
    Font(R.font.verdana_regular, FontWeight.Normal),
    Font(R.font.verdana_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.verdana_bold, FontWeight.Bold),
    Font(R.font.verdana_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

/**
 * Un Composable que renderiza una lista de 'ConceptBlock'
 * (los datos de "Concepto" del JSON).
 */
@Composable
fun ConceptScreen(
    modifier: Modifier = Modifier,
    blocks: List<ConceptBlock>
) {
    val uriHandler = LocalUriHandler.current

    // Usamos una LazyColumn para que sea scrolleable
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre bloques
    ) {
        items(blocks) { block ->
            // Miramos qué 'type' de bloque es y lo dibujamos
            when (block) {
                is ConceptBlock.HeaderBlock -> {
                    Text(
                        text = block.text.uppercase(),
                        fontFamily = verdanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                is ConceptBlock.ParagraphBlock -> {
                    Text(
                        text = block.text,
                        fontFamily = verdanaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                is ConceptBlock.ImageBlock -> {
                    ImageBlockView(imageUrl = block.imageUrl)
                }
                is ConceptBlock.LinkBlock -> {
                    // Un bloque clicable para las "Sorpresas"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable {
                                uriHandler.openUri(block.targetUrl)
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = block.title,
                            fontFamily = verdanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Un Composable separado para mostrar las imágenes de "Concepto"
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ImageBlockView(imageUrl: String) {
    GlideImage(
        model = imageUrl,
        contentDescription = "Imagen de Concepto",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f) // Ratio 16:9
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}