package wulf.compose.foco

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Grosor del canto según el estado. Es la regla 12 en forma de número.
 *
 * El foco NO estrena una señal propia: usa la que el sistema ya tiene para decir
 * "este", el canto de la regla 1, subida un escalón más. Inventarle un halo o
 * una sombra sería contradecir la regla 1 justo donde más se nota — un mando a
 * tres metros.
 *
 * | estado | canto | relleno |
 * |---|---|---|
 * | reposo | cresta (metal apagado), 1 dp | pozo |
 * | seleccionado | metal pleno, 2 dp | pozo |
 * | enfocado | metal pleno, 3 dp | pozo aclarado |
 *
 * Que enfocado lleve **además** el relleno separado es lo que lo distingue de
 * seleccionado cuando los dos caen a la vez, que es el caso normal navegando una
 * lista con el mando: se recorre la selección.
 */
object WulfCanto {
    /** En reposo. La cresta, que es material de bandeja. */
    val reposo: Dp = 1.dp

    /** Seleccionado: metal pleno. */
    val seleccionado: Dp = 2.dp

    /**
     * Enfocado por mando o teclado.
     *
     * Tres y no dos: en televisión se mira a tres metros, y un escalón de 1 dp
     * que en la mano se distingue perfectamente, ahí no existe. Es el mismo
     * motivo por el que el objetivo táctil es de 48 dp y no de 32.
     */
    val enfocado: Dp = 3.dp

    /**
     * En un televisor engorda otro escalón.
     *
     * A tres metros lo que en la mano se distingue perfectamente deja de verse, y
     * el foco es lo único que dice dónde está uno navegando con mando.
     */
    val enfocadoTelevision: Dp = 4.dp

    /** El canto de foco que le toca a un tamaño. */
    fun enfocadoDe(ancho: wulf.core.WulfAncho): Dp =
        if (ancho.esLejano) enfocadoTelevision else enfocado
}

/**
 * Cuánto se aclara el pozo enfocado, en la misma escala que el tono del toque.
 *
 * Coincide a propósito con la separación de la onda: el aparato dice "acá estás"
 * con el mismo material en los dos casos, se llegue con el dedo o con el mando.
 */
const val WULF_SEPARACION_FOCO = 0.10f

/**
 * Si este elemento tiene el foco ahora mismo.
 *
 * Se pasa la MISMA fuente de interacciones que se le da al `clickable` o al
 * `focusable`; si se crean dos, el componente se pinta mirando una y el sistema
 * mueve el foco en la otra, y el canto no se entera nunca. Es el fallo típico de
 * esta pieza.
 */
@Composable
fun recordarEnfocado(interacciones: MutableInteractionSource): State<Boolean> =
    interacciones.collectIsFocusedAsState()

/**
 * Pide el foco para este elemento cuando la pantalla aparece.
 *
 * **Una pantalla sin foco inicial deja al mando sin punto de partida**: la
 * primera pulsación se gasta en que el sistema elija por su cuenta, y suele
 * elegir mal — lo primero del árbol, que casi nunca es lo que se quiere hacer.
 *
 * Va sobre el elemento que la pantalla quiere que sea el destino de entrada, y
 * es una línea:
 *
 * ```kotlin
 * WulfPozo(onTap = ::reproducir, modifier = Modifier.wulfFocoInicial()) { … }
 * ```
 *
 * Tiene que ir **antes** del `focusable`/`clickable` en la cadena de
 * modificadores, que es donde lo pone este ayudante por su cuenta al
 * encadenarse: el requester tiene que envolver al nodo que recibe el foco, no
 * ir después.
 *
 * @param activo en falso no pide nada. Sirve para que la pantalla decida en
 *   caliente cuál de sus piezas arranca enfocada —la lista si hay elementos, el
 *   mensaje de vacío si no— sin mover los modificadores de sitio.
 */
@Composable
fun Modifier.wulfFocoInicial(activo: Boolean = true): Modifier {
    val solicitante = remember { FocusRequester() }

    LaunchedEffect(activo) {
        if (activo) {
            // `runCatching`: si el elemento se fue del árbol entre que se pidió
            // y que llegó el efecto, pedir el foco lanza. Un arranque de
            // pantalla no puede reventar por eso.
            runCatching { solicitante.requestFocus() }
        }
    }

    return focusRequester(solicitante)
}
