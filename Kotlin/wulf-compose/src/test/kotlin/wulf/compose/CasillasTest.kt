package wulf.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import org.junit.Assert.assertNotEquals
import org.junit.Test
import wulf.compose.tokens.WulfAcento
import wulf.compose.tokens.esquemaDe

/**
 * Que ningún rol del esquema se quede en el valor de fábrica de Material 3.
 *
 * Esta prueba nace de dos averías de la misma familia, con años y lenguajes de
 * por medio:
 *
 * - En Compose salió una **píldora azul violácea** en mitad de la laca. Era
 *   `secondaryContainer` sin mapear: el componente lo pidió y Material entregó
 *   su morado de fábrica.
 * - En Qt, una casilla y un radio salieron con la MISMA forma, y lo encontró
 *   alguien usando la librería.
 *
 * En los dos casos el fallo es el mismo: un componente que nadie escribió acá
 * lee algo que acá no se decidió. Aquí no hay pieza propia para casillas ni para
 * radios —Material los dibuja bien, cuadrada la una y redondo el otro, y eso no
 * se puede temar— así que lo único que pueden traerse de fuera es el COLOR. Si
 * todos los roles están mapeados, no hay de dónde sacar el morado.
 *
 * Por eso la prueba no mira una casilla: mira el agujero por el que se colaría.
 */
class CasillasTest {

    private val roles: List<Pair<String, (ColorScheme) -> Long>> = listOf(
        "primary" to { it.primary.value.toLong() },
        "onPrimary" to { it.onPrimary.value.toLong() },
        "primaryContainer" to { it.primaryContainer.value.toLong() },
        "onPrimaryContainer" to { it.onPrimaryContainer.value.toLong() },
        "inversePrimary" to { it.inversePrimary.value.toLong() },
        "secondary" to { it.secondary.value.toLong() },
        "onSecondary" to { it.onSecondary.value.toLong() },
        "secondaryContainer" to { it.secondaryContainer.value.toLong() },
        "onSecondaryContainer" to { it.onSecondaryContainer.value.toLong() },
        "tertiary" to { it.tertiary.value.toLong() },
        "onTertiary" to { it.onTertiary.value.toLong() },
        "tertiaryContainer" to { it.tertiaryContainer.value.toLong() },
        "onTertiaryContainer" to { it.onTertiaryContainer.value.toLong() },
        "surface" to { it.surface.value.toLong() },
        "onSurface" to { it.onSurface.value.toLong() },
        "surfaceVariant" to { it.surfaceVariant.value.toLong() },
        // El que usa el contorno de una casilla SIN marcar y el aro de un radio
        // sin elegir: si se queda de fábrica, el control vacío se ve gris de
        // Material sobre la laca.
        "onSurfaceVariant" to { it.onSurfaceVariant.value.toLong() },
        "outline" to { it.outline.value.toLong() },
        "outlineVariant" to { it.outlineVariant.value.toLong() },
        "error" to { it.error.value.toLong() },
        "onError" to { it.onError.value.toLong() },
        "errorContainer" to { it.errorContainer.value.toLong() },
        "onErrorContainer" to { it.onErrorContainer.value.toLong() },
    )

    /** Blanco y negro puros son los extremos de CUALQUIER paleta.
     *
     * Que `onPrimary` claro coincida con el de fábrica no significa que se haya
     * olvidado: significa que el texto sobre el metal es blanco en los dos
     * sitios, y blanco es blanco. Lo que esta prueba busca es el morado de
     * Material colándose, y el morado no es ninguno de estos dos. */
    private val extremos = setOf(0xFFFFFFFFL, 0xFF000000L)

    private fun ColorScheme.crudo(leer: (ColorScheme) -> Long): Long =
        leer(this) ushr 32 and 0xFFFFFFFFL

    @Test
    fun `ningun rol se queda en el morado de fabrica`() {
        for (acento in WulfAcento.entries) {
            for (oscuro in listOf(false, true)) {
                val nuestro = esquemaDe(acento, oscuro)
                val fabrica = if (oscuro) darkColorScheme() else lightColorScheme()
                for ((nombre, leer) in roles) {
                    if (nuestro.crudo(leer) in extremos) continue
                    assertNotEquals(
                        "$nombre sigue en el valor de fábrica con $acento " +
                            (if (oscuro) "oscuro" else "claro") +
                            ": cualquier componente que lo pida filtra el morado de Material",
                        leer(fabrica),
                        leer(nuestro),
                    )
                }
            }
        }
    }
}
