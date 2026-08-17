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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import wulf.compose.componentes.WulfAcciones
import wulf.compose.componentes.WulfAvisoHost
import wulf.compose.componentes.WulfBarraBusqueda
import wulf.compose.componentes.WulfBarraProgreso
import wulf.compose.componentes.WulfBoton
import wulf.compose.componentes.WulfBotonPlano
import wulf.compose.componentes.WulfBotonPrimario
import wulf.compose.componentes.WulfCampo
import wulf.compose.componentes.WulfEnlace
import wulf.compose.componentes.WulfInterruptor
import wulf.compose.componentes.avisos
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
import wulf.compose.foco.wulfFocoInicial
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
        // El anfitrión de avisos envuelve la pantalla una vez. Sin él, pedir un
        // aviso revienta con un mensaje claro en vez de tragárselo en silencio.
        WulfAvisoHost {
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
                    Reproductor()
                    Reticula()
                    Tallado()
                    Controles()
                    Acciones()
                }
            }
        }
    }
}

/**
 * La barra de búsqueda, que es la pieza central de un reproductor.
 *
 * Es lo primero que recibe el foco al abrir: en un televisor, la pantalla tiene
 * que decir de entrada qué se puede hacer. Con el mando enfocada, izquierda y
 * derecha mueven la posición.
 */
@Composable
private fun Reproductor() {
    var posicion by remember { mutableStateOf(0.35f) }

    WulfPozo(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
            Text("Reproductor", style = MaterialTheme.typography.titleLarge)
            WulfBarraBusqueda(
                valor = posicion,
                onCambio = { posicion = it },
                // Lo cargado va por delante de la posición: es lo que distingue
                // "va por el minuto 3" de "tiene hasta el 8 en memoria".
                secundario = (posicion + 0.2f).coerceAtMost(1f),
                modifier = Modifier.wulfFocoInicial(),
            )
            Text(
                "${(posicion * 100).toInt()} %",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Progreso (no se enfoca)", style = MaterialTheme.typography.bodySmall)
            WulfBarraProgreso(valor = 0.7f)
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
                        WulfBotonPrimario(metal.name, onClick = { onAcento(metal) })
                    } else {
                        WulfBoton(metal.name, onClick = { onAcento(metal) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
                WulfBoton(if (oscuro) "Modo oscuro" else "Modo claro", onClick = onModo)
                WulfBoton(
                    if (relieve) "Relieve activo" else "Relieve apagado",
                    onClick = onRelieve,
                )
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

/**
 * Los controles del sistema, y el aviso.
 *
 * Recorrer esta sección con un teclado o un mando es la forma de ver la regla
 * 12: el canto de lo enfocado sube a 3 dp y el relleno se aclara.
 */
@Composable
private fun Controles() {
    var texto by remember { mutableStateOf("") }
    var encendido by remember { mutableStateOf(true) }
    val aviso = avisos()

    WulfPozo(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(WulfSpacing.unidad)) {
            Text("Controles", style = MaterialTheme.typography.titleLarge)
            WulfCampo(
                valor = texto,
                onValor = { texto = it },
                etiqueta = "Nombre",
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(WulfSpacing.unidad),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WulfInterruptor(encendido = encendido, onCambio = { encendido = it })
                Text("Sincronizar", style = MaterialTheme.typography.bodyMedium)
            }
            WulfBoton("Mostrar un aviso", onClick = { aviso.mostrar("Guardado") })
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
                    { WulfBotonPlano("Cancelar", onClick = {}) },
                    { WulfBotonPlano("Eliminar", onClick = {}) },
                ),
                derecha = listOf(
                    { WulfBotonPrimario("Guardar", onClick = {}) },
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
