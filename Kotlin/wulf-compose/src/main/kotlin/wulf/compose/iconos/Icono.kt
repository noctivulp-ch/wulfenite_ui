package wulf.compose.iconos

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import wulf.compose.superficies.wulfEsTelevision
import wulf.compose.tokens.WulfTheme

/** Tamaños del sistema, en el tipo de Compose. */
object WulfIconoTamano {
    val chico: Dp = wulf.core.WulfIconoTamano.CHICO.dp
    val normal: Dp = wulf.core.WulfIconoTamano.NORMAL.dp
    val grande: Dp = wulf.core.WulfIconoTamano.GRANDE.dp
    val television: Dp = wulf.core.WulfIconoTamano.TELEVISION.dp
}

/** Aire entre un icono y el texto que lo acompaña. */
val wulfGapIcono: Dp = wulf.core.WULF_GAP_ICONO.dp

/**
 * Un icono del juego, teñido y a su tamaño.
 *
 * Los archivos son de **un solo color**, que no es el color final: se retiñen al
 * dibujar. Es lo que permite que el mismo archivo sirva para el estado inactivo
 * y para el activo sin mantener dos copias que acaban desincronizadas.
 *
 * ```kotlin
 * WulfIcono(R.drawable.wulf_buscar, "Buscar")
 * WulfIcono(R.drawable.wulf_reportes, "Reportes", activo = true)
 * ```
 *
 * **En un televisor crece solo**: a tres metros el tamaño normal se queda corto,
 * por el mismo motivo que el labio de la bandeja y el canto del foco.
 *
 * @param descripcion lo que oye un lector de pantalla. **Nulo solo si el icono es
 *   decorativo** — si acompaña a un texto que ya dice lo mismo, repetirlo hace
 *   que se lea dos veces.
 * @param activo con el metal en vez del texto atenuado. Es la misma señal que en
 *   el resto del sistema: el acento marca lo elegido.
 */
@Composable
fun WulfIcono(
    @DrawableRes recurso: Int,
    descripcion: String?,
    modifier: Modifier = Modifier,
    activo: Boolean = false,
    tamano: Dp = WulfIconoTamano.normal,
    color: Color? = null,
) {
    val laca = WulfTheme.laca
    // En televisor nunca por debajo del tamaño de lejos, pero sin encoger uno que
    // ya se pidió más grande.
    val medida = if (wulfEsTelevision()) maxOf(tamano, WulfIconoTamano.television) else tamano

    Icon(
        painter = painterResource(recurso),
        contentDescription = descripcion,
        modifier = modifier.size(medida),
        tint = color
            ?: if (activo) MaterialTheme.colorScheme.primary else laca.textoAtenuadoEnBandeja,
    )
}
