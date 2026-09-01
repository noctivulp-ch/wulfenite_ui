package wulf.compose.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import wulf.compose.foco.WULF_SEPARACION_FOCO
import wulf.compose.foco.WulfCanto
import wulf.compose.foco.recordarEnfocado
import wulf.compose.superficies.tonoDeToque
import wulf.compose.texto.isLight
import wulf.compose.tokens.WulfTheme
import wulf.compose.tokens.wulfObjetivoTactilMin

/**
 * Alto del carril. Fino a propósito: la barra es un pozo alargado, no una pieza
 * con volumen propio.
 */
val wulfAltoBarra: Dp = 8.dp

/**
 * Cuánto avanza un paso del mando, en tanto por uno.
 *
 * Cinco por ciento y no uno: con el 1% recorrer una película de dos horas serían
 * cien pulsaciones. Con el 5%, veinte — y el ajuste fino se hace manteniendo
 * pulsado, que el sistema repite solo.
 */
const val WULF_PASO_DESLIZADOR = 0.05f

/**
 * Una barra de progreso: cuánto va de algo que avanza solo.
 *
 * **No se puede enfocar**, y es deliberado: no hay nada que hacer con ella. Un
 * destino del mando en el que no se puede actuar convierte el recorrido en un
 * laberinto (regla 12).
 *
 * Para lo que SÍ se mueve —la posición de un vídeo, un volumen— está
 * [WulfDeslizador].
 *
 * @param valor de 0 a 1. Se recorta, así que un cálculo que se pase no rompe la
 *   pantalla.
 * @param secundario lo cargado por delante de la posición, si lo hay. Es lo que
 *   distingue "va por el minuto 3" de "tiene hasta el minuto 8 en memoria".
 */
@Composable
fun WulfBarraProgreso(
    valor: Float,
    modifier: Modifier = Modifier,
    secundario: Float? = null,
) {
    val laca = WulfTheme.laca
    val acento = MaterialTheme.colorScheme.primary
    val v = valor.coerceIn(0f, 1f)

    Canvas(
        modifier
            .fillMaxWidth()
            .height(wulfAltoBarra)
            // Se anuncia como barra de progreso aunque sea un dibujo: sin esto,
            // un lector de pantalla ve un rectángulo sin significado.
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(v, 0f..1f) },
    ) {
        dibujarBarra(
            avance = v,
            secundario = secundario?.coerceIn(0f, 1f),
            carril = laca.pozo,
            cargado = laca.paredCresta,
            relleno = acento,
            canto = laca.paredCresta,
            grosorCanto = WulfCanto.reposo.toPx(),
        )
    }
}

/**
 * Una barra que se MUEVE: la posición de un vídeo, un volumen.
 *
 * Es la pieza central de un reproductor, y por eso lo importante acá es que
 * **funcione con el mando**: izquierda y derecha mueven de a [paso], y el foco se
 * ve con el canto del sistema (regla 12). Sin esto, en un televisor no hay forma
 * de mover la posición.
 *
 * El objetivo táctil es de 48 dp aunque el carril mida 8: se dibuja fino y se
 * toca ancho, que es lo que permite acertarle con el dedo sin engordar la pieza.
 *
 * @param onCambio recibe el valor nuevo ya recortado a 0..1.
 * @param paso cuánto mueve una pulsación del mando.
 */
@Composable
fun WulfDeslizador(
    valor: Float,
    onCambio: (Float) -> Unit,
    modifier: Modifier = Modifier,
    secundario: Float? = null,
    habilitado: Boolean = true,
    paso: Float = WULF_PASO_DESLIZADOR,
) {
    val laca = WulfTheme.laca
    val esquema = MaterialTheme.colorScheme
    val interacciones = remember { MutableInteractionSource() }
    val enfocado by recordarEnfocado(interacciones)
    val v = valor.coerceIn(0f, 1f)

    val carril = if (enfocado) {
        tonoDeToque(laca.pozo, !esquema.isLight(), WULF_SEPARACION_FOCO)
    } else {
        laca.pozo
    }

    Canvas(
        modifier
            .fillMaxWidth()
            // Fino de dibujo, ancho de toque.
            .height(wulfObjetivoTactilMin)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(v, 0f..1f) }
            .onKeyEvent { evento ->
                // Solo al pulsar: atender también el soltar movería dos pasos
                // por cada pulsación.
                if (!habilitado || evento.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (evento.key) {
                    Key.DirectionLeft -> {
                        onCambio((v - paso).coerceIn(0f, 1f))
                        true
                    }

                    Key.DirectionRight -> {
                        onCambio((v + paso).coerceIn(0f, 1f))
                        true
                    }
                    // Arriba y abajo NO se consumen: son lo que saca el foco de
                    // la barra. Tragárselos dejaría al mando encerrado acá.
                    else -> false
                }
            }
            .focusable(enabled = habilitado, interactionSource = interacciones),
    ) {
        dibujarBarra(
            avance = v,
            secundario = secundario?.coerceIn(0f, 1f),
            carril = carril,
            cargado = laca.paredCresta,
            relleno = if (habilitado) esquema.primary else laca.paredCresta,
            canto = if (enfocado) esquema.primary else laca.paredCresta,
            grosorCanto = if (enfocado) WulfCanto.enfocado.toPx() else WulfCanto.reposo.toPx(),
        )
    }
}

/**
 * El dibujo, compartido por las dos.
 *
 * La barra es un **pozo alargado**: carril hundido con su canto de 1 px y el
 * avance en metal. Ni sombra ni degradado de volumen — la profundidad la carga
 * el canto (regla 1), también acá.
 */
private fun DrawScope.dibujarBarra(
    avance: Float,
    secundario: Float?,
    carril: androidx.compose.ui.graphics.Color,
    cargado: androidx.compose.ui.graphics.Color,
    relleno: androidx.compose.ui.graphics.Color,
    canto: androidx.compose.ui.graphics.Color,
    grosorCanto: Float,
) {
    val alto = wulfAltoBarra.toPx()
    val arriba = (size.height - alto) / 2f
    val radio = CornerRadius(alto / 2f)

    fun tramo(hasta: Float, color: androidx.compose.ui.graphics.Color) {
        if (hasta <= 0f) return
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, arriba),
            size = Size(size.width * hasta, alto),
            cornerRadius = radio,
        )
    }

    tramo(1f, carril)
    // Lo cargado se pinta con el CANTO, que es el metal apagado: "metal, pero
    // más callado", que es exactamente lo que significa. Con el tono de contacto
    // —que es más oscuro que el carril— el tramo cargado se veía como un agujero
    // en mitad de la barra en vez de como avance.
    //
    // Y va DEBAJO del avance: pintado encima taparía la posición justo cuando la
    // memoria va por delante, que es siempre.
    secundario?.let { tramo(it, cargado) }
    tramo(avance, relleno)

    drawRoundRect(
        color = canto,
        topLeft = Offset(0f, arriba),
        size = Size(size.width, alto),
        cornerRadius = radio,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = grosorCanto),
    )
}
