package wulf.compose.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import wulf.compose.foco.WULF_SEPARACION_FOCO
import wulf.compose.foco.WulfCanto
import wulf.compose.foco.recordarEnfocado
import wulf.compose.superficies.tonoDeToque
import wulf.compose.superficies.wulfEsTelevision
import wulf.compose.texto.WulfEtiquetaAjustada
import wulf.compose.texto.isLight
import wulf.compose.tokens.WulfSpacing
import wulf.compose.tokens.WulfTheme
import wulf.compose.tokens.wulfObjetivoTactilMin

/** Un destino del rail. */
data class WulfDestino(
    val etiqueta: String,
    val icono: (@Composable () -> Unit)? = null,
)

/** Ancho por defecto del rail. Cabe una palabra corta sin encoger. */
val wulfAnchoRail: Dp = 88.dp

/**
 * La columna de navegación: los destinos de la app, en vertical.
 *
 * **Es la respuesta del sistema a la navegación lateral**, y existe en vez de
 * unas pestañas verticales por un motivo concreto: las pestañas verticales
 * **rotan el texto**, y leer de lado es incómodo. Acá el texto va horizontal
 * siempre.
 *
 * En Qt esa rotación además no tiene arreglo con el tema puesto —Qt descarta el
 * estilo delegado en cuanto una hoja de estilo toca las pestañas— así que el rail
 * es la única forma de tener navegación vertical con el acabado del sistema en
 * las tres plataformas. Ver `doc/paridad.md`.
 *
 * Reglas de la pieza:
 * - **Vive en la bandeja, no en un pozo.** Es cromo, no contenido. El pozo es lo
 *   que hay al lado.
 * - **La marca del activo va en el borde que toca al contenido** — el derecho—,
 *   con el metal y el grosor de selección. Es la misma regla que en las pestañas:
 *   la marca señala hacia lo que se está mirando.
 * - **Cada destino se enfoca** y lo muestra como todo lo demás (regla 12).
 * - **48 dp de alto mínimo**, y el texto encoge antes que partirse.
 */
@Composable
fun WulfRail(
    destinos: List<WulfDestino>,
    seleccionado: Int,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    ancho: Dp = wulfAnchoRail,
) {
    val laca = WulfTheme.laca
    Column(
        modifier
            .width(ancho)
            .background(laca.bandeja)
            .padding(vertical = WulfSpacing.unidad),
        verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad / 2),
    ) {
        destinos.forEachIndexed { indice, destino ->
            DestinoDelRail(
                destino = destino,
                activo = indice == seleccionado,
                onElegir = { onSeleccionar(indice) },
            )
        }
    }
}

@Composable
private fun DestinoDelRail(
    destino: WulfDestino,
    activo: Boolean,
    onElegir: () -> Unit,
) {
    val laca = WulfTheme.laca
    val esquema = MaterialTheme.colorScheme
    val interacciones = remember { MutableInteractionSource() }
    val enfocado by recordarEnfocado(interacciones)
    val ancho = if (wulfEsTelevision()) WulfCanto.enfocadoTelevision else WulfCanto.enfocado

    val fondo = if (enfocado) {
        tonoDeToque(laca.bandeja, !esquema.isLight(), WULF_SEPARACION_FOCO)
    } else {
        laca.bandeja
    }

    Box(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = wulfObjetivoTactilMin)
            .background(fondo)
            .drawBehind {
                // La marca en el borde DERECHO: el que toca al contenido. Es la
                // misma regla que en las pestañas — la marca señala hacia lo que
                // se está mirando, no hacia el vacío.
                val grosor = when {
                    enfocado -> ancho.toPx()
                    activo -> WulfCanto.seleccionado.toPx()
                    else -> 0f
                }
                if (grosor > 0f) {
                    drawLine(
                        color = esquema.primary,
                        start = Offset(size.width - grosor / 2, 0f),
                        end = Offset(size.width - grosor / 2, size.height),
                        strokeWidth = grosor,
                    )
                }
            }
            .selectable(
                selected = activo,
                interactionSource = interacciones,
                indication = LocalIndication.current,
                onClick = onElegir,
            )
            .padding(horizontal = WulfSpacing.unidad, vertical = WulfSpacing.unidad),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad / 2),
        ) {
            destino.icono?.invoke()
            WulfEtiquetaAjustada(
                texto = destino.etiqueta,
                estilo = MaterialTheme.typography.bodyMedium.copy(
                    // Activo en texto pleno, el resto atenuado: el mismo reparto
                    // que en las pestañas.
                    color = if (activo) laca.textoEnBandeja else laca.textoAtenuadoEnBandeja,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}
