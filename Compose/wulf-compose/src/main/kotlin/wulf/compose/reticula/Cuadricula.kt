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
import wulf.compose.tokens.WulfSpacing

/**
 * Los tres tamaños de pantalla, definidos UNA vez.
 *
 * Con el layout repartido en varios archivos, cada uno eligiendo sus propios
 * umbrales, una app deja de leerse como una sola cosa. Los cortes salen del
 * parque real de aparatos: un teléfono barato son 360 dp de ancho lógico; una
 * tablet ronda los 800; el escritorio va de 1024 para arriba.
 */
enum class WulfAncho {
    /** Teléfono. 360 dp es el caso de diseño. */
    COMPACTO,

    /** Tablet. */
    MEDIO,

    /** Escritorio. */
    AMPLIO;

    /** Atajo para el caso que más se consulta: si hay que componer para teléfono. */
    val esCompacto: Boolean get() = this == COMPACTO

    companion object {
        const val CORTE_MEDIO = 600
        const val CORTE_AMPLIO = 1024

        /**
         * El tamaño que le toca a un ancho dado, en dp.
         *
         * Se decide a partir del ancho de una CAJA y no del de la ventana: un
         * panel lateral angosto dentro de un escritorio ancho necesita
         * componerse como compacto.
         */
        fun deAncho(anchoDp: Int): WulfAncho = when {
            anchoDp >= CORTE_AMPLIO -> AMPLIO
            anchoDp >= CORTE_MEDIO -> MEDIO
            else -> COMPACTO
        }
    }
}

/**
 * Cuántas columnas tiene la retícula. Es el mismo número en los tres tamaños: lo
 * que cambia es cuántas ocupa cada pieza.
 *
 * Seis porque se divide en 2 y en 3, que son los repartos que piden estas
 * pantallas. Con cuatro los tercios no cierran; con doce, a 360 dp cada columna
 * queda en unos 21 dp y el número deja de significar nada.
 */
const val WULF_COLUMNAS = 6

/**
 * Cuántos elementos entran por fila si cada uno ocupa el span indicado.
 *
 * Para las listas LARGAS no se puede usar [WulfCuadricula]: construye todas sus
 * celdas de una, y una lista de miles tiene que ser perezosa. Esas se arman con
 * un `LazyVerticalGrid` de `GridCells.Fixed` EXPLÍCITO, y este ayudante es el
 * que hace que ese número salga del mismo vocabulario de spans que el resto de
 * la app en vez de ser un tamaño máximo que reparte por ancho disponible.
 */
fun wulfPorFila(ancho: WulfAncho, compacto: Int, medio: Int, amplio: Int): Int {
    val span = when (ancho) {
        WulfAncho.COMPACTO -> compacto
        WulfAncho.MEDIO -> medio
        WulfAncho.AMPLIO -> amplio
    }
    return WULF_COLUMNAS / span
}

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
    /** El contenido de la celda. */
    val contenido: @Composable () -> Unit,
) {
    init {
        require(compacto in 1..WULF_COLUMNAS) { "compacto fuera de 1..$WULF_COLUMNAS: $compacto" }
        require(medio in 1..WULF_COLUMNAS) { "medio fuera de 1..$WULF_COLUMNAS: $medio" }
        require(amplio in 1..WULF_COLUMNAS) { "amplio fuera de 1..$WULF_COLUMNAS: $amplio" }
    }

    /** Cuántas columnas ocupa en un [ancho] concreto. */
    fun columnasEn(ancho: WulfAncho): Int = when (ancho) {
        WulfAncho.COMPACTO -> compacto
        WulfAncho.MEDIO -> medio
        WulfAncho.AMPLIO -> amplio
    }
}

/** Una celda que ocupa lo mismo en los tres tamaños. */
fun wulfCeldaFija(columnas: Int, contenido: @Composable () -> Unit): WulfCelda =
    WulfCelda(columnas, columnas, columnas, contenido)

/**
 * Empaqueta las celdas en filas sin partir ninguna: una celda que no entra en lo
 * que queda de la fila abre la siguiente.
 *
 * Está fuera del composable y trabaja sobre los spans y nada más, que es lo que
 * permite probarla en la JVM sin emulador — el reparto es la parte de la
 * retícula donde de verdad se puede equivocar uno.
 */
fun repartirEnFilas(spans: List<Int>): List<List<Int>> {
    val filas = mutableListOf<List<Int>>()
    var actual = mutableListOf<Int>()
    var ocupadas = 0

    for (span in spans) {
        if (ocupadas + span > WULF_COLUMNAS && actual.isNotEmpty()) {
            filas += actual
            actual = mutableListOf()
            ocupadas = 0
        }
        actual += span
        ocupadas += span
    }
    if (actual.isNotEmpty()) filas += actual
    return filas
}

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
        val ancho = anchoForzado ?: WulfAncho.deAncho(maxWidth.value.toInt())
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
