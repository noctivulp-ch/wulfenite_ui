package wulf.compose.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import wulf.compose.tokens.WulfSpacing
import wulf.compose.tokens.wulfObjetivoTactilMin

/**
 * Un enlace, con la pinta de un enlace.
 *
 * Va en el **color del acento** y **subrayado**. Las dos cosas juntas y no una:
 * el color solo no basta —quien no distingue ese tono ve texto normal— y el
 * subrayado es lo que hace la pista independiente del color. Es la regla de
 * siempre de accesibilidad, y acá además el acento cambia según el metal que
 * tenga puesto el aparato.
 *
 * El acento es `primary`, que es el metal. Medido contra el pozo en los tres
 * metales y las dos luminosidades: el peor caso da 5,28:1, por encima del 4,5:1
 * que pide un texto. Hay una prueba que lo vigila, porque es justo lo que se
 * rompe al añadir un metal nuevo.
 *
 * El objetivo táctil llega a 48 dp aunque el texto sea más bajo: un enlace de una
 * línea de texto pequeño mide unos 16 dp, y eso no se acierta con el dedo.
 *
 * @param onAbrir quién lo abre, y si pudo. Se recibe en vez de llamar al sistema
 *   acá adentro para no atar un componente de diseño a una forma concreta de
 *   lanzar intents — y para que la app decida qué hacer cuando no hay navegador.
 */
@Composable
fun WulfEnlace(
    texto: String,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier,
    estilo: TextStyle = LocalTextStyle.current,
    descripcion: String? = null,
) {
    val acento = MaterialTheme.colorScheme.primary
    val deEnlace = estilo.copy(
        color = acento,
        textDecoration = TextDecoration.Underline,
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = wulfObjetivoTactilMin)
            .clickable(role = Role.Button, onClick = onAbrir)
            .semantics {
                // Sin esto se anuncia el texto tal cual, y un nombre propio no
                // dice que al tocarlo se abre una página.
                contentDescription = descripcion ?: "Enlace: $texto"
            }
            // Aire vertical para que el subrayado no toque lo de abajo.
            .padding(vertical = WulfSpacing.unidad / 2),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(texto, style = deEnlace)
    }
}
