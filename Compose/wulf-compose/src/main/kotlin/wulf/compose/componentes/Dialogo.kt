package wulf.compose.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import wulf.compose.tokens.WulfSpacing

/**
 * Por debajo de este alto la pantalla se considera acostada.
 *
 * Es ALTO y no orientación a propósito: lo que escasea acostado es el alto, y
 * una tablet apaisada de 800 dp no tiene el problema que sí tiene un teléfono de
 * 360.
 */
val wulfAltoAcostado: Dp = 520.dp

/** Si la ventana está acostada: poco alto y ancho de sobra. */
@Composable
fun wulfEstaAcostado(): Boolean =
    LocalConfiguration.current.screenHeightDp.dp < wulfAltoAcostado

/**
 * Las acciones de un diálogo en dos columnas, fila por fila.
 *
 * **La acción que confirma va a la DERECHA y Cancelar a su izquierda, en la
 * misma fila.** No es una preferencia estética: para guardar se pulsa a la
 * derecha sin mirar, y con el orden invertido —la acción a la izquierda,
 * Cancelar a la derecha— crear algo al vuelo significa pulsar Cancelar creyendo
 * guardar. Es un fallo que se comete con el dedo, no con los ojos.
 *
 * Las acciones **sobre el objeto** (reactivar, eliminar, cámara, galería) van en
 * las filas siguientes de la columna izquierda: quedan lejos del sitio al que va
 * el pulgar a confirmar, que es justo donde no se quiere un borrado.
 *
 * Un diálogo cuya única acción es salir deja ese botón a la derecha: ahí no hay
 * nada que confirmar, así que no hay confusión posible y es donde llega el dedo.
 *
 * Dos columnas y no una fila con todos los botones: en 360 dp cuatro no entran
 * de ancho, y al no entrar se apilan alineados a la derecha, perdiendo de qué
 * habla cada uno.
 *
 * Hay una prueba que lo vigila ([wulfEmparejarAcciones]) porque es una
 * convención que se rompe sola: al agregar un botón, lo natural es ponerlo donde
 * quepa.
 */
@Composable
fun WulfAcciones(
    derecha: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    izquierda: List<@Composable () -> Unit> = emptyList(),
) {
    val filas = wulfEmparejarAcciones(izquierda.size, derecha.size)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
        filas.forEach { (iIzq, iDer) ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WulfSpacing.gapCelda),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Dos columnas del MISMO ancho, no dos botones repartidos a los
                // extremos: así cada uno se coloca según su propio largo y las
                // columnas quedan descuadradas entre filas.
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    iIzq?.let { izquierda[it]() }
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    iDer?.let { derecha[it]() }
                }
            }
        }
    }
}

/**
 * Qué índice de cada columna va en cada fila. `null` es un hueco.
 *
 * Está fuera del composable para poder probarlo sin dibujar nada: lo que la
 * prueba tiene que vigilar es que **la primera fila lleva la última acción de
 * cada columna emparejada**, o sea confirmar a la derecha de cancelar.
 */
fun wulfEmparejarAcciones(izquierda: Int, derecha: Int): List<Pair<Int?, Int?>> {
    val filas = maxOf(izquierda, derecha)
    return (0 until filas).map { i ->
        (if (i < izquierda) i else null) to (if (i < derecha) i else null)
    }
}

/**
 * El diálogo del sistema: mismo cuerpo en las dos orientaciones, con las
 * acciones DEBAJO de pie y AL COSTADO acostado.
 *
 * Acostado el alto es el recurso escaso —los campos ya se lo comen— así que las
 * acciones se van a donde sobra espacio. Dejarlas debajo es exactamente lo que
 * no entra: un formulario con varios campos desborda por eso.
 *
 * El desplazamiento lo pone este componente DENTRO del contenido. Acostado
 * envuelve la fila entera, contenido y acciones juntos: envolviendo solo el
 * contenido, con el teclado abierto la columna de acciones sigue pidiendo su
 * alto y desborda igual. Lo que tiene que ceder acostado es el bloque completo.
 */
@Composable
fun WulfDialogo(
    onDismiss: () -> Unit,
    titulo: @Composable () -> Unit,
    acciones: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    anchoAcostado: Dp = 560.dp,
    contenido: @Composable () -> Unit,
) {
    val acostado = wulfEstaAcostado()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = titulo,
        text = {
            if (acostado) {
                Row(
                    Modifier
                        // `widthIn` y no un ancho fijo: acostado la pantalla
                        // siempre da más de 560, pero fijarlo obligaría al
                        // diálogo a ese ancho aun cuando la ventana sea menor.
                        .widthIn(max = anchoAcostado)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(WulfSpacing.pared),
                    // `Top` y no estirado: dentro de un scroll el alto es
                    // ilimitado, y estirar a un infinito no significa nada.
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(Modifier.weight(3f)) { contenido() }
                    Box(Modifier.weight(2f)) { acciones() }
                }
            } else {
                Box(Modifier.verticalScroll(rememberScrollState())) { contenido() }
            }
        },
        // Acostado las acciones ya viajan dentro del cuerpo, así que acá no van:
        // repetirlas las dibujaría dos veces.
        confirmButton = { if (!acostado) acciones() },
    )
}

/**
 * Un paso de un panel embebido: título, algo de contenido y sus acciones.
 *
 * Misma regla que [WulfDialogo] y por el mismo motivo, pero para lo que NO es un
 * diálogo: un panel de ajustes que apila sus botones debajo del texto es lo que
 * no entra acostado. Acá van al costado.
 *
 * La columna de acciones se lleva 2 de 5 partes en vez de medir su contenido:
 * estos botones llevan frases enteras y medirlos les daría media pantalla.
 */
@Composable
fun WulfPanel(
    titulo: String,
    acciones: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    contenido: (@Composable () -> Unit)? = null,
) {
    val cuerpo: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(WulfSpacing.gapCelda)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido?.invoke()
        }
    }
    val botones: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
            acciones.forEach { it() }
        }
    }

    if (!wulfEstaAcostado()) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(WulfSpacing.gapCelda)) {
            cuerpo()
            botones()
        }
    } else {
        Row(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(WulfSpacing.pared),
            verticalAlignment = Alignment.Top,
        ) {
            Box(Modifier.weight(3f)) { cuerpo() }
            Box(Modifier.weight(2f)) { botones() }
        }
    }
}
