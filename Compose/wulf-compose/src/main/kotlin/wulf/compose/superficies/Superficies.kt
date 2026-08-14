package wulf.compose.superficies

import androidx.compose.foundation.background
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import wulf.compose.tokens.WulfRadius
import wulf.compose.tokens.WulfSpacing
import wulf.compose.tokens.WulfTheme
import wulf.compose.tokens.wulfObjetivoTactilMin

/**
 * El plano de la bandeja: un único contenedor continuo por pantalla, con el
 * labio exterior alrededor.
 *
 * Los pozos van dentro, separados entre sí por [WulfSpacing.pared] — ese hueco
 * NO es fondo de página asomándose, es material de bandeja (`doc/reglas.md`,
 * regla 2).
 *
 * Pinta el fondo de la bandeja además del labio, porque en Android no hay un
 * `scaffoldBackgroundColor` que lo haga por su cuenta: si esto no lo pintara, el
 * fondo sería el `surface` de Material — que acá es el POZO— y la pantalla
 * entera se vería del color del contenido.
 */
@Composable
fun WulfBandeja(
    modifier: Modifier = Modifier,
    rim: Boolean = true,
    fondo: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val laca = WulfTheme.laca
    // El texto que cae sobre la bandeja toma su color, no el del pozo. Sin
    // proveerlo acá, un `Text` suelto hereda el negro por defecto de Compose y
    // desaparece sobre la bandeja oscura — que es exactamente lo que pasó la
    // primera vez que se ejecutó el ejemplo.
    CompositionLocalProvider(
        LocalContentColor provides laca.textoEnBandeja,
        LocalTextStyle provides LocalTextStyle.current.copy(color = laca.textoEnBandeja),
    ) {
        Box(
            modifier = modifier
                .then(if (fondo) Modifier.background(laca.bandeja) else Modifier)
                .then(if (rim) Modifier.padding(WulfSpacing.rim) else Modifier),
            content = content,
        )
    }
}

/**
 * Un compartimento tallado en la bandeja.
 *
 * Tres señales, en orden de importancia, y ninguna es una sombra proyectada:
 * 1. La cresta como contorno de 1 dp — la pared atrapando la luz. Es la señal
 *    PRIMARIA porque es la única que sobrevive a luz ambiente fuerte y la única
 *    que funciona en modo oscuro, donde el escalón de valor bandeja/pozo no
 *    llega a 3:1.
 * 2. El contacto como degradado corto y duro en el borde superior interno — la
 *    sombra que la pared proyecta hacia dentro del hueco.
 * 3. El salto de valor entre bandeja y pozo, que en modo claro es enorme (13:1)
 *    y en oscuro es un apoyo secundario (1,43:1).
 *
 * Radio 4 y no 12: el interior de una bandeja es rectilíneo. El radio grande
 * vive solo en el canto exterior.
 *
 * Con [onTap] u [onLongPress] el pozo se vuelve tocable y garantiza el objetivo
 * táctil mínimo, así que no hace falta envolverlo en un botón.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WulfPozo(
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    seleccionado: Boolean = false,
    padding: PaddingValues = PaddingValues(WulfSpacing.paddingCelda),
    radio: Dp = WulfRadius.pozo,
    content: @Composable BoxScope.() -> Unit,
) {
    val laca = WulfTheme.laca
    val acento = MaterialTheme.colorScheme.primary
    val forma = RoundedCornerShape(radio)
    val interacciones = remember { MutableInteractionSource() }

    val tocable = onTap != null || onLongPress != null

    Box(
        modifier = modifier
            .clip(forma)
            .background(
                // La sombra de contacto ocupa solo la primera franja del pozo:
                // es el canto de la pared, no un degradado decorativo de arriba
                // a abajo. En 7% del alto, igual que en las otras dos gemelas.
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to laca.pozoContacto,
                        0.07f to laca.pozo,
                        1f to laca.pozo,
                    ),
                ),
            )
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) acento else laca.paredCresta,
                shape = forma,
            )
            .then(
                when {
                    !tocable -> Modifier
                    onLongPress != null -> Modifier
                        .defaultMinSize(minHeight = wulfObjetivoTactilMin)
                        .combinedClickable(
                            interactionSource = interacciones,
                            indication = LocalIndication.current,
                            onClick = { onTap?.invoke() },
                            onLongClick = onLongPress,
                        )

                    else -> Modifier
                        .defaultMinSize(minHeight = wulfObjetivoTactilMin)
                        .clickable(
                            interactionSource = interacciones,
                            indication = LocalIndication.current,
                            onClick = { onTap?.invoke() },
                        )
                },
            )
            .padding(padding),
    ) {
        // Dentro del pozo manda el color de contenido, no el de bandeja: es la
        // otra mitad de lo mismo, y lo que hace que un `Text` sin estilo caiga
        // bien puesto en las dos superficies.
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            LocalTextStyle provides
                LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
        ) {
            content()
        }
    }
}
