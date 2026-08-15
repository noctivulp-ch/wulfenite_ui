package wulf.compose.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import wulf.compose.superficies.wulfInsetSuperior
import wulf.compose.tokens.WulfRadius
import wulf.compose.tokens.WulfSpacing
import wulf.compose.tokens.WulfTheme

/** Cuánto se queda un aviso en pantalla antes de irse solo. */
private const val VISIBLE_MS = 2000L

/** Lo que tarda en entrar empujando. */
private const val ENTRADA_MS = 220

/**
 * Al tocarlo se va MÁS RÁPIDO de lo que entró: tocar un aviso es decir "ya lo
 * vi", y hacerlo esperar la misma animación se siente pegajoso.
 */
private const val SALIDA_POR_TOQUE_MS = 120

/** Quién sabe mostrar avisos. Se obtiene con [avisos] dentro de un [WulfAvisoHost]. */
class WulfAvisos internal constructor(
    private val mostrarInterno: (String) -> Unit,
) {
    /** Muestra [mensaje]. Reemplaza al aviso anterior si todavía estaba. */
    fun mostrar(mensaje: String) = mostrarInterno(mensaje)
}

private val LocalWulfAvisos = staticCompositionLocalOf<WulfAvisos?> { null }

/**
 * El avisador vigente.
 *
 * Revienta con un mensaje claro si no hay [WulfAvisoHost] arriba, en vez de
 * tragarse los avisos en silencio: un aviso que no aparece se descubre en
 * producción, cuando alguien dice "guardé y no me dijo nada".
 */
@Composable
fun avisos(): WulfAvisos = LocalWulfAvisos.current
    ?: error("No hay WulfAvisoHost en el árbol. Envolvé el contenido de la pantalla en uno.")

/**
 * El aviso de la app. **Uno solo, para todo**: lo guardado, lo exportado, y
 * también lo que salió mal cuando no amerita ocupar la pantalla.
 *
 * Sustituye al `Snackbar` de Material, que ocupa el ancho completo y se sienta
 * abajo — justo donde suelen estar el buscador y los botones de acción.
 *
 * Reglas de la pieza:
 * - **Un rectángulo**, con el fondo del pozo y el metal por borde. Es el mismo
 *   canto que marca un pozo seleccionado: el aviso se lee como una pieza de la
 *   app, no como un cartel del sistema. Sin sombra — la profundidad la carga el
 *   canto (regla 1).
 * - **Entra y sale empujando desde la derecha**, no con un fundido: el
 *   movimiento dice de dónde viene y a dónde se va, y hacia el lado corto —
 *   entrar desde arriba lo cruzaría por delante del contenido que se está
 *   mirando.
 * - **Dos segundos**, y se va solo. Un toque lo saca antes.
 * - **Tope de media pantalla**: pasado eso baja de línea en vez de estirarse.
 *   Una tira larguísima de una sola línea no se lee de un vistazo, que es lo
 *   único que un aviso tiene que conseguir.
 *
 * Se pone UNA vez, envolviendo el contenido de la pantalla:
 *
 * ```kotlin
 * WulfTheme {
 *     WulfAvisoHost {
 *         val aviso = avisos()
 *         WulfBandeja { Button(onClick = { aviso.mostrar("Guardado") }) { … } }
 *     }
 * }
 * ```
 */
@Composable
fun WulfAvisoHost(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    // Uno a la vez: dos avisos encimados se tapan entre sí y ninguno se lee. El
    // nuevo reemplaza al anterior, que es lo que pasa cuando alguien encadena
    // acciones rápido. El contador fuerza el reinicio del reloj aunque el texto
    // sea idéntico.
    var mensaje by remember { mutableStateOf<String?>(null) }
    var turno by remember { mutableStateOf(0) }
    var porToque by remember { mutableStateOf(false) }

    val avisador = remember {
        WulfAvisos { texto ->
            mensaje = texto
            porToque = false
            turno++
        }
    }

    Box(modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalWulfAvisos provides avisador) { content() }

        AvisoFlotante(
            mensaje = mensaje,
            turno = turno,
            salidaRapida = porToque,
            onTocar = { porToque = true; mensaje = null },
            onExpirar = { mensaje = null },
        )
    }
}

@Composable
private fun BoxScope.AvisoFlotante(
    mensaje: String?,
    turno: Int,
    salidaRapida: Boolean,
    onTocar: () -> Unit,
    onExpirar: () -> Unit,
) {
    val laca = WulfTheme.laca
    val acento = MaterialTheme.colorScheme.primary

    // Se relanza con `turno` y no solo con el texto: dos avisos iguales seguidos
    // tienen que reiniciar los dos segundos, no compartirlos.
    LaunchedEffect(turno) {
        if (mensaje != null) {
            delay(VISIBLE_MS)
            onExpirar()
        }
    }

    BoxWithConstraints(
        Modifier
            .align(Alignment.TopEnd)
            // Arriba a la derecha y por debajo de la zona reservada del borde
            // superior: ahí es donde caen la muesca de la cámara y las
            // notificaciones del sistema.
            .padding(
                top = wulfInsetSuperior(),
                start = WulfSpacing.gapGrupo,
                end = WulfSpacing.gapGrupo,
            ),
    ) {
        val topeAncho = maxWidth / 2

        AnimatedVisibility(
            visible = mensaje != null,
            enter = slideInHorizontally(
                animationSpec = tween(ENTRADA_MS, easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)),
                initialOffsetX = { ancho -> (ancho * 1.4f).toInt() },
            ),
            exit = slideOutHorizontally(
                animationSpec = tween(if (salidaRapida) SALIDA_POR_TOQUE_MS else ENTRADA_MS),
                targetOffsetX = { ancho -> (ancho * 1.4f).toInt() },
            ),
        ) {
            Box(
                Modifier
                    .widthIn(max = topeAncho)
                    .clip(RoundedCornerShape(WulfRadius.pozo))
                    .background(laca.pozo)
                    .border(1.dp, acento, RoundedCornerShape(WulfRadius.pozo))
                    .clickable(onClick = onTocar)
                    .padding(
                        horizontal = WulfSpacing.paddingCelda,
                        vertical = WulfSpacing.unidad,
                    ),
            ) {
                Text(
                    // Sin `maxLines`: el aviso crece las líneas que haga falta,
                    // que es lo que permite que el texto entre entero.
                    text = mensaje.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
