package wulf.compose.superficies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import wulf.compose.tokens.WulfTheme

/**
 * Margen propio por encima del inset del sistema.
 *
 * La zona alta de la pantalla es territorio ajeno: ahí caen la muesca de la
 * cámara y las notificaciones que bajan. Nada importante puede vivir en ese
 * borde, aunque el sistema diga que el inset "termina" antes.
 */
val wulfMargenSuperior: Dp = 24.dp

/** Cuánto hay que dejar libre arriba: el inset real del aparato más el margen propio. */
@Composable
fun wulfInsetSuperior(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + wulfMargenSuperior

/**
 * Franja degradada en el borde superior, del color de la bandeja a transparente.
 *
 * No corta el contenido: lo deja pasar POR DEBAJO. Empujar todo hacia abajo con
 * un margen seguro deja un borde muerto; con el velo, una lista que sube se
 * desvanece contra el canto de la bandeja en vez de cortarse en seco.
 *
 * El color es el de la bandeja y no negro fijo: en modo oscuro la bandeja ya es
 * casi negra, y en modo claro un velo negro sería una mancha. Lo que se quiere
 * decir es "el contenido se mete bajo el canto de la bandeja", y eso lo dice el
 * color de la bandeja en los dos modos.
 *
 * Es decoración: nunca se come un toque, porque no declara ninguno.
 */
@Composable
fun WulfVeloSuperior(modifier: Modifier = Modifier) {
    val bandeja = WulfTheme.laca.bandeja
    Box(
        modifier
            .fillMaxWidth()
            .height(wulfInsetSuperior())
            .background(
                // Opaco arriba y transparente abajo, con la mayor parte del
                // desvanecido en el último tramo: si el degradado empezara a
                // aclararse enseguida, el texto que pasa por debajo se leería a
                // medias justo donde está la muesca.
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to bandeja,
                        0.55f to bandeja,
                        1f to bandeja.copy(alpha = 0f),
                    ),
                ),
            ),
    )
}
