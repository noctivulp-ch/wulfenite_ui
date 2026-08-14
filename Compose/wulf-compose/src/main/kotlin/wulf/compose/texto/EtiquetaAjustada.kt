package wulf.compose.texto

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

/**
 * Cuánto se puede encoger una etiqueta antes de que dejar de leerse sea peor que
 * partirla. Por debajo de esto no baja.
 */
const val WULF_ETIQUETA_MINIMA_SP = 9f

/**
 * Una etiqueta corta que **encoge la tipografía antes que partirse**.
 *
 * El caso que la trajo: una palabra larga —"Optimización"— en la rejilla de
 * ajustes. El hueco deja ~69 dp de texto a 360 dp de ancho, la palabra no entra,
 * y lo que hace el sistema por su cuenta es cortarla a mitad —"Optimizaci /
 * ón"—, porque partir por carácter es lo que toca cuando una palabra sola es más
 * ancha que la línea.
 *
 * Una línea siempre, encogida lo justo para entrar. Lo que ya entra **no se
 * toca**: solo reduce, nunca agranda.
 *
 * En Compose esto se resuelve midiendo: se dibuja, y si el resultado desbordó se
 * baja el tamaño y se vuelve a intentar. Es lo mismo que hace `BoxFit.scaleDown`
 * en la gemela de Flutter, pero acá hay que escribirlo — y por eso hay un suelo
 * ([WULF_ETIQUETA_MINIMA_SP]): sin él, una caja de 2 dp haría bajar el tamaño
 * hasta cero probando.
 */
@Composable
fun WulfEtiquetaAjustada(
    texto: String,
    modifier: Modifier = Modifier,
    estilo: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign = TextAlign.Center,
    minimo: TextUnit = WULF_ETIQUETA_MINIMA_SP.sp,
) {
    val partida = if (estilo.fontSize.isUnspecified) 14.sp else estilo.fontSize
    // La clave incluye el texto y el tamaño de partida: si cambia cualquiera de
    // los dos hay que volver a medir desde arriba, no seguir desde el tamaño al
    // que había quedado la etiqueta anterior.
    var tamano by remember(texto, partida) { mutableStateOf(partida) }
    var listo by remember(texto, partida) { mutableStateOf(false) }

    Text(
        text = texto,
        modifier = modifier,
        style = estilo.copy(fontSize = tamano),
        textAlign = textAlign,
        // Una línea y sin ceder: es lo que obliga a que el desbordamiento se
        // note. Con el ajuste de línea puesto, el texto se partiría antes de que
        // hubiera nada que encoger.
        maxLines = 1,
        softWrap = false,
        onTextLayout = { resultado ->
            if (!listo && resultado.hasVisualOverflow && tamano > minimo) {
                // Medio punto por paso: bajar de a uno da saltos visibles entre
                // etiquetas vecinas de la misma rejilla.
                tamano = (tamano.value - 0.5f).coerceAtLeast(minimo.value).sp
            } else {
                listo = true
            }
        },
    )
}
