package wulf.compose.ejemplo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import wulf.compose.componentes.WulfAcciones
import wulf.compose.componentes.WulfEnlace
import wulf.compose.reticula.WulfCelda
import wulf.compose.reticula.WulfCuadricula
import wulf.compose.superficies.WulfBandeja
import wulf.compose.superficies.WulfPozo
import wulf.compose.texto.TalladoNivel
import wulf.compose.texto.WulfEtiquetaAjustada
import wulf.compose.texto.WulfTextoTallado
import wulf.compose.tokens.WulfAcento
import wulf.compose.tokens.WulfEfectoPesado
import wulf.compose.tokens.WulfEfectos
import wulf.compose.tokens.WulfSpacing
import wulf.compose.tokens.WulfTheme

/**
 * El banco de estilo: una app pequeña que toca **todas** las piezas del sistema.
 *
 * Sirve para dos cosas a la vez, que es lo que la hace buen ejemplo:
 *
 * - **Validar la librería.** Si el banco se puede escribir usando solo
 *   `wulf-compose`, la librería está completa. Si hay que salirse de ella, falta
 *   algo. Este archivo no importa ni un color ni una medida sueltos: todo sale
 *   de los tokens.
 * - **Enseñarla.** Quien la use tiene una app funcional donde ver cada pieza en
 *   su sitio, en vez de una lista de nombres de constantes.
 */
class BancoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Banco() }
    }
}

@Composable
fun Banco() {
    var acento by remember { mutableStateOf(WulfAcento.ORO) }
    var oscuro by remember { mutableStateOf(true) }
    var relieve by remember { mutableStateOf(true) }

    val efectos = if (relieve) WulfEfectos() else WulfEfectos(setOf(WulfEfectoPesado.TALLADO))

    WulfTheme(acento = acento, oscuro = oscuro, efectos = efectos) {
        WulfBandeja(Modifier.fillMaxSize()) {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(WulfSpacing.pared),
            ) {
                Mandos(
                    acento = acento,
                    oscuro = oscuro,
                    relieve = relieve,
                    onAcento = { acento = it },
                    onModo = { oscuro = !oscuro },
                    onRelieve = { relieve = !relieve },
                )
                Reticula()
                Tallado()
                Acciones()
            }
        }
    }
}

/** Los tres ejes que el sistema deja mover: metal, modo y adorno caro. */
@Composable
private fun Mandos(
    acento: WulfAcento,
    oscuro: Boolean,
    relieve: Boolean,
    onAcento: (WulfAcento) -> Unit,
    onModo: () -> Unit,
    onRelieve: () -> Unit,
) {
    WulfPozo(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
            Text("Metal, modo y relieve", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
                WulfAcento.entries.forEach { metal ->
                    if (metal == acento) {
                        FilledTonalButton(onClick = { onAcento(metal) }) { Text(metal.name) }
                    } else {
                        OutlinedButton(onClick = { onAcento(metal) }) { Text(metal.name) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
                OutlinedButton(onClick = onModo) {
                    Text(if (oscuro) "Modo oscuro" else "Modo claro")
                }
                OutlinedButton(onClick = onRelieve) {
                    Text(if (relieve) "Relieve activo" else "Relieve apagado")
                }
            }
        }
    }
}

/**
 * La retícula con spans distintos por tamaño. Girando el aparato se ve lo que
 * hace: el hero pasa de ocupar la fila entera a compartirla con los tres tramos.
 */
@Composable
private fun Reticula() {
    val laca = WulfTheme.laca
    WulfCuadricula(
        celdas = listOf(
            WulfCelda(compacto = 6, medio = 6, amplio = 3) {
                WulfPozo(Modifier.fillMaxWidth()) {
                    Column {
                        Text("Total del día", style = MaterialTheme.typography.bodyMedium)
                        WulfTextoTallado(
                            texto = "₡128 400",
                            sobre = laca.pozo,
                            estilo = MaterialTheme.typography.displaySmall,
                            nivel = TalladoNivel.MARCADO,
                        )
                    }
                }
            },
            *List(3) { i ->
                WulfCelda(compacto = 2, medio = 2, amplio = 1) {
                    WulfPozo(Modifier.fillMaxWidth()) {
                        Column {
                            // La etiqueta encoge antes que partirse: con tres por
                            // fila a 360 dp, "Madrugada" no entra a tamaño pleno.
                            WulfEtiquetaAjustada(
                                listOf("Mañana", "Tarde", "Madrugada")[i],
                                estilo = MaterialTheme.typography.bodyMedium,
                            )
                            // También el monto: a tres por fila en un teléfono
                            // "₡71 200" no entra, y partido en dos líneas deja
                            // de leerse como una cifra.
                            WulfEtiquetaAjustada(
                                listOf("₡42 000", "₡71 200", "₡15 200")[i],
                                estilo = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }.toTypedArray(),
        ),
    )
}

/** Los dos niveles de tallado, uno al lado del otro: es la única forma de elegir. */
@Composable
private fun Tallado() {
    val laca = WulfTheme.laca
    WulfPozo(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
            Text("Tallado", style = MaterialTheme.typography.titleLarge)
            WulfTextoTallado(
                texto = "Sutil — para leer",
                sobre = laca.pozo,
                estilo = MaterialTheme.typography.bodyLarge,
                nivel = TalladoNivel.SUTIL,
            )
            WulfTextoTallado(
                texto = "Marcado — 24sp",
                sobre = laca.pozo,
                estilo = MaterialTheme.typography.headlineSmall,
                nivel = TalladoNivel.MARCADO,
            )
            WulfEnlace(texto = "Un enlace se ve como un enlace", onAbrir = {})
        }
    }
}

/** Confirmar a la derecha, y lo destructivo lejos del pulgar que confirma. */
@Composable
private fun Acciones() {
    WulfPozo(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
            Text("Acciones", style = MaterialTheme.typography.titleLarge)
            WulfAcciones(
                izquierda = listOf(
                    { TextButton(onClick = {}) { Text("Cancelar") } },
                    { TextButton(onClick = {}) { Text("Eliminar") } },
                ),
                derecha = listOf(
                    { FilledTonalButton(onClick = {}) { Text("Guardar") } },
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun BancoEnTelefono() = Banco()

@Preview(showBackground = true, widthDp = 1024, heightDp = 800)
@Composable
private fun BancoEnEscritorio() = Banco()
