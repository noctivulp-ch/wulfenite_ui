package wulf.compose.reticula

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import wulf.core.WULF_COLUMNAS
import wulf.core.repartirEnFilas
import wulf.compose.superficies.wulfEsTelevision
import wulf.compose.tokens.WulfSpacing

// Los tamaños, las columnas y el reparto viven en `wulf-core`: son aritmética y
// no tienen por qué saber de Compose. Acá solo se re-exportan para que quien use
// la librería no tenga que importar de dos sitios.
typealias WulfAncho = wulf.core.WulfAncho



/**
 * Una pieza de la retícula, con cuántas columnas ocupa en cada tamaño.
 *
 * Declarar los tres valores es obligatorio a propósito. Un valor por defecto del
 * estilo "si no decís nada, ocupa todo" esconde justamente la decisión que este
 * sistema existe para hacer explícita.
 */
class WulfCelda(
    /** Columnas que ocupa en teléfono. */
    val compacto: Int,
    /** Columnas que ocupa en tablet. */
    val medio: Int,
    /** Columnas que ocupa en escritorio. */
    val amplio: Int,
    /**
     * Columnas en televisor. Por defecto hereda el de escritorio, que es lo
     * razonable de partida — pero se puede pedir otro: de lejos suelen caber
     * menos piezas por fila, no más.
     */
    val television: Int = amplio,
    /** El contenido de la celda. */
    val contenido: @Composable () -> Unit,
) {
    init {
        require(compacto in 1..WULF_COLUMNAS) { "compacto fuera de 1..$WULF_COLUMNAS: $compacto" }
        require(medio in 1..WULF_COLUMNAS) { "medio fuera de 1..$WULF_COLUMNAS: $medio" }
        require(amplio in 1..WULF_COLUMNAS) { "amplio fuera de 1..$WULF_COLUMNAS: $amplio" }
        require(television in 1..WULF_COLUMNAS) { "television fuera de 1..$WULF_COLUMNAS: $television" }
    }

    /** Cuántas columnas ocupa en un [ancho] concreto. */
    fun columnasEn(ancho: WulfAncho): Int = when (ancho) {
        WulfAncho.COMPACTO -> compacto
        WulfAncho.MEDIO -> medio
        WulfAncho.AMPLIO -> amplio
        WulfAncho.TELEVISION -> television
    }
}

/** Una celda que ocupa lo mismo en los tres tamaños. */
fun wulfCeldaFija(columnas: Int, contenido: @Composable () -> Unit): WulfCelda =
    WulfCelda(columnas, columnas, columnas, columnas, contenido)


/**
 * La retícula wulf: reparte sus celdas en filas de [WULF_COLUMNAS] columnas.
 *
 * Todas las celdas de una misma fila comparten altura. Eso no es un detalle
 * estético: es lo que hace que el resultado se lea como una caja wulf y no como
 * columnas sueltas de distinto largo.
 *
 * No usa `LazyVerticalGrid` por tres razones:
 * - reparte por tamaño de celda, o sea que el número de columnas termina siendo
 *   un efecto secundario del ancho disponible en vez de una decisión — que es
 *   exactamente el problema que este componente viene a resolver;
 * - su separación es un margen, y acá el hueco entre pozos es MATERIAL de
 *   bandeja (`doc/reglas.md`, regla 2), no fondo asomándose;
 * - construye perezosamente, y para una pantalla de pocas piezas eso solo añade
 *   complicación. Para listas largas se usa el grid perezoso con [wulfPorFila].
 *
 * @param llenarAlto las filas se reparten el alto disponible en vez de medir su
 *   contenido. Hace falta cuando las celdas contienen algo que SCROLLEA:
 *   `IntrinsicSize.Min` —que es como se comparte altura en el modo normal— pide
 *   a sus hijos que declaren su alto natural, y una lista perezosa no sabe
 *   hacerlo. Exige que el padre acote el alto.
 */
@Composable
fun WulfCuadricula(
    celdas: List<WulfCelda>,
    modifier: Modifier = Modifier,
    separacion: Dp = WulfSpacing.pared,
    anchoForzado: WulfAncho? = null,
    llenarAlto: Boolean = false,
) {
    BoxWithConstraints(modifier) {
        // `de` y no `deAncho`: un televisor de 1080p reporta unos 960 dp y por
        // ancho caería en MEDIO — el reparto de una tablet que se mira a cuarenta
        // centímetros. La distancia de lectura no se mide en píxeles, así que se
        // le pregunta al sistema.
        val ancho = anchoForzado ?: WulfAncho.de(maxWidth.value.toInt(), wulfEsTelevision())
        val spans = celdas.map { it.columnasEn(ancho) }
        val filas = repartirEnFilas(spans)

        Column(verticalArrangement = Arrangement.spacedBy(separacion)) {
            var indice = 0
            filas.forEach { fila ->
                val desde = indice
                indice += fila.size
                FilaDeCuadricula(
                    celdas = celdas.subList(desde, indice),
                    ancho = ancho,
                    separacion = separacion,
                    llenarAlto = llenarAlto,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FilaDeCuadricula(
    celdas: List<WulfCelda>,
    ancho: WulfAncho,
    separacion: Dp,
    llenarAlto: Boolean,
) {
    val ocupadas = celdas.sumOf { it.columnasEn(ancho) }

    // Sin `llenarAlto`, `IntrinsicSize.Min` es lo que le da a la fila una altura
    // finita real antes de repartirla entre las celdas. Con `llenarAlto` el
    // padre ya acotó el alto, y meter la altura intrínseca ahí rompería con
    // cualquier hijo que scrollee.
    val modificador = Modifier
        .fillMaxWidth()
        .then(if (llenarAlto) Modifier.weight(1f) else Modifier.height(IntrinsicSize.Min))

    Row(
        modifier = modificador,
        horizontalArrangement = Arrangement.spacedBy(separacion),
        verticalAlignment = Alignment.Top,
    ) {
        celdas.forEach { celda ->
            CeldaEstirada(peso = celda.columnasEn(ancho).toFloat()) { celda.contenido() }
        }
        // Una fila a medio llenar mantiene el ancho de sus celdas en vez de
        // estirarlas: si la última fila tuviera celdas más anchas que las de
        // arriba, la retícula dejaría de leerse como una retícula.
        if (ocupadas < WULF_COLUMNAS) {
            Spacer(Modifier.weight((WULF_COLUMNAS - ocupadas).toFloat()))
        }
    }
}

@Composable
private fun RowScope.CeldaEstirada(peso: Float, contenido: @Composable () -> Unit) {
    // `fillMaxHeight` dentro de una fila de altura intrínseca es lo que hace que
    // todas las celdas queden del alto de la más alta. Sin esto cada una mide su
    // propio contenido y la fila se lee como columnas sueltas.
    Box(Modifier.weight(peso).fillMaxHeight()) { contenido() }
}

/** El hueco de una pared, para separar a mano fuera de la retícula. */
@Composable
fun WulfPared(horizontal: Boolean = false) {
    if (horizontal) Spacer(Modifier.width(WulfSpacing.pared))
    else Spacer(Modifier.height(WulfSpacing.pared))
}
