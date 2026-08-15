package wulf.compose.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import wulf.compose.foco.WulfCanto
import wulf.compose.foco.recordarEnfocado
import wulf.compose.tokens.WulfRadius
import wulf.compose.tokens.WulfSpacing
import wulf.compose.tokens.WulfTheme
import wulf.compose.tokens.wulfObjetivoTactilMin

/**
 * Los controles con la forma del sistema.
 *
 * En Flutter y en Qt esto lo resuelve el tema: uno declara cómo son los botones
 * y los campos una vez, y todos salen así. Compose no tiene ese punto — sus
 * componentes leen el `ColorScheme` pero **no** las formas, los tamaños ni los
 * bordes. Por eso acá son componentes y no configuración.
 *
 * Que se note lo que aportan sobre los de Material:
 * - **Radio de pozo (4 dp)**, no la píldora de Material. El interior de una
 *   bandeja es rectilíneo (regla 3).
 * - **48 dp de alto mínimo** siempre (regla 7).
 * - **El contorno es el METAL, no el canto del pozo.** Un botón ES acción, y el
 *   acento marca acción (regla 4). Material los dibuja con `outline`, que acá es
 *   material de bandeja, y entonces un botón no se distingue de un hueco.
 * - **El foco se ve** (regla 12): el contorno sube a 3 dp. Sin esto un mando de
 *   televisor recorre los botones sin que nada lo diga.
 */

/** La forma de todo control del sistema. */
private val formaControl @Composable get() = RoundedCornerShape(WulfRadius.pozo)

/**
 * La acción primaria: metal relleno. Una por pantalla, o deja de ser primaria.
 */
@Composable
fun WulfBotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
) {
    val interacciones = remember { MutableInteractionSource() }
    val enfocado by recordarEnfocado(interacciones)

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = wulfObjetivoTactilMin),
        enabled = habilitado,
        shape = formaControl,
        interactionSource = interacciones,
        border = if (enfocado) {
            BorderStroke(WulfCanto.enfocado, MaterialTheme.colorScheme.onPrimary)
        } else {
            null
        },
        contentPadding = PaddingValues(
            horizontal = WulfSpacing.paddingCeldaGrande,
            vertical = WulfSpacing.paddingCelda,
        ),
    ) { Text(texto) }
}

/**
 * La acción normal: contorno de metal sobre el pozo.
 *
 * El contorno es `primary` y no `outline`, que es la diferencia con el de
 * Material: con `outline` el botón lleva el canto del pozo y se lee como un
 * hueco más, no como algo que se pulsa.
 */
@Composable
fun WulfBoton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
) {
    val interacciones = remember { MutableInteractionSource() }
    val enfocado by recordarEnfocado(interacciones)
    val acento = MaterialTheme.colorScheme.primary

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = wulfObjetivoTactilMin),
        enabled = habilitado,
        shape = formaControl,
        interactionSource = interacciones,
        border = BorderStroke(
            width = if (enfocado) WulfCanto.enfocado else WulfCanto.reposo,
            color = acento,
        ),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = acento),
    ) { Text(texto) }
}

/** La acción que se puede ignorar: solo texto. Cancelar, o "más tarde". */
@Composable
fun WulfBotonPlano(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
) {
    val interacciones = remember { MutableInteractionSource() }
    val enfocado by recordarEnfocado(interacciones)

    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = wulfObjetivoTactilMin),
        enabled = habilitado,
        shape = formaControl,
        interactionSource = interacciones,
        // Plano no lleva contorno en reposo — es lo que lo hace plano—, pero
        // enfocado tiene que verse igual que los demás o el mando lo salta sin
        // que nadie note que pasó por él.
        border = if (enfocado) {
            BorderStroke(WulfCanto.enfocado, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) { Text(texto) }
}

/**
 * Un campo de texto.
 *
 * En reposo el campo es un **hueco** y lleva el canto del pozo; al enfocarse
 * pasa a ser acción y toma el metal, engordando a 3 dp. Es la misma lógica que
 * los botones, y hace que enfocar se vea igual en toda la app.
 */
@Composable
fun WulfCampo(
    valor: String,
    onValor: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    soloNumeros: Boolean = false,
    oculto: Boolean = false,
    error: String? = null,
) {
    val laca = WulfTheme.laca
    val esquema = MaterialTheme.colorScheme

    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        modifier = modifier.defaultMinSize(minHeight = wulfObjetivoTactilMin),
        enabled = habilitado,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        shape = formaControl,
        visualTransformation =
            if (oculto) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (soloNumeros) KeyboardType.Number else KeyboardType.Text,
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = laca.pozo,
            unfocusedContainerColor = laca.pozo,
            disabledContainerColor = laca.pozo,
            errorContainerColor = laca.pozo,
            focusedIndicatorColor = esquema.primary,
            unfocusedIndicatorColor = laca.paredCresta,
            focusedLabelColor = esquema.primary,
            unfocusedLabelColor = esquema.onSurfaceVariant,
        ),
    )
}

/**
 * Un interruptor.
 *
 * Encendido es metal, apagado es canto de pozo: el acento significa lo mismo acá
 * que en el resto del sistema — esto está activo — en vez de estrenar un verde
 * de "correcto" que no está en la paleta.
 */
@Composable
fun WulfInterruptor(
    encendido: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
) {
    val laca = WulfTheme.laca
    val esquema = MaterialTheme.colorScheme

    Switch(
        checked = encendido,
        onCheckedChange = onCambio,
        modifier = modifier,
        enabled = habilitado,
        colors = SwitchDefaults.colors(
            checkedThumbColor = esquema.onPrimary,
            checkedTrackColor = esquema.primary,
            checkedBorderColor = esquema.primary,
            uncheckedThumbColor = laca.paredCresta,
            uncheckedTrackColor = laca.pozo,
            uncheckedBorderColor = laca.paredCresta,
        ),
    )
}
