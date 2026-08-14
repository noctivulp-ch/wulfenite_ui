package wulf.compose.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import wulf.compose.superficies.WulfPozo
import wulf.compose.tokens.WulfSpacing

/**
 * Qué se muestra cuando algo no se pudo cargar.
 *
 * Nace de un caso real: una pantalla de reportes falló en un teléfono y lo que
 * apareció fue una excepción de base de datos con el SQL completo, a pantalla
 * llena, delante de quien atendía el mostrador. El error estaba capturado — pero
 * **capturar no es lo mismo que contar**.
 *
 * Dos audiencias distintas, y por eso dos niveles:
 * - quien usa la app lee una frase en su idioma y un botón para reintentar;
 * - quien la desarrolla ve el detalle técnico, pero **solo si se le pide**,
 *   porque en el mostrador no le sirve a nadie y encima puede filtrar nombres de
 *   tablas y consultas.
 *
 * Lo que NO hace: tragarse el error. El fallo se sigue viendo, y sigue siendo
 * evidente que algo está mal — un cero silencioso dejaría al negocio creyendo
 * que no vendió nada.
 *
 * @param mostrarDetalle por defecto `false`. La app lo pone a `BuildConfig.DEBUG`
 *   — la librería no puede leer el `BuildConfig` de quien la usa.
 */
@Composable
fun WulfFalla(
    mensaje: String,
    modifier: Modifier = Modifier,
    error: Any? = null,
    onReintentar: (() -> Unit)? = null,
    mostrarDetalle: Boolean = false,
    textoReintentar: String = "Reintentar",
) {
    var detalleAbierto by remember { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WulfSpacing.pared),
        contentAlignment = Alignment.Center,
    ) {
        WulfPozo {
            Column(verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
                Text(mensaje, style = MaterialTheme.typography.titleSmall)

                // Nulo esconde el botón: sin nada que reintentar, ofrecerlo es
                // prometer algo que no pasa.
                if (onReintentar != null) {
                    FilledTonalButton(onClick = onReintentar) { Text(textoReintentar) }
                }

                if (mostrarDetalle && error != null) {
                    // Plegado: el detalle es para depurar, no para mirar de
                    // frente.
                    TextButton(onClick = { detalleAbierto = !detalleAbierto }) {
                        Text(
                            if (detalleAbierto) "Ocultar detalle técnico" else "Detalle técnico",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (detalleAbierto) {
                        Text("$error", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
