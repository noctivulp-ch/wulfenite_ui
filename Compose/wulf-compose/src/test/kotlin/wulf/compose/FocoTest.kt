package wulf.compose

import org.junit.Assert.assertTrue
import org.junit.Test
import wulf.compose.foco.WULF_SEPARACION_FOCO
import wulf.compose.foco.WulfCanto
import wulf.compose.superficies.tonoDeToque
import wulf.compose.tokens.WulfAcento
import wulf.compose.tokens.contraste
import wulf.compose.tokens.lacaDe

/**
 * La regla 12, en lo que de ella se puede comprobar sin pintar.
 *
 * Que el foco **exista** solo se ve ejecutando; que sea **distinguible** es
 * aritmética, y es lo que se rompe sin querer al retocar un token. Sin un foco
 * visible una app no se puede usar con mando — no es una mejora de
 * accesibilidad, es que el mando mueve algo y la pantalla no lo dice.
 */
class FocoTest {

    private val modos = listOf(false, true)

    @Test
    fun `el canto crece en cada escalon`() {
        // Si dos estados comparten grosor, el de más arriba deja de leerse.
        assertTrue(WulfCanto.reposo < WulfCanto.seleccionado)
        assertTrue(WulfCanto.seleccionado < WulfCanto.enfocado)
    }

    @Test
    fun `el canto enfocado dobla al de reposo`() {
        // A tres metros un escalón de 1 dp no existe. El de foco tiene que ser
        // al menos el doble del de reposo para leerse desde el sofá.
        assertTrue(
            "enfocado (${WulfCanto.enfocado}) debería doblar a reposo (${WulfCanto.reposo})",
            WulfCanto.enfocado.value >= WulfCanto.reposo.value * 2,
        )
    }

    @Test
    fun `el pozo enfocado se separa del pozo en reposo`() {
        // Es la mitad de lo que distingue enfocado de seleccionado. Si la
        // separación se quedara en nada, los dos estados se verían iguales en
        // cuanto coinciden — que es lo normal navegando una lista.
        for (oscuro in modos) {
            val laca = lacaDe(WulfAcento.ORO, oscuro)
            val enfocado = tonoDeToque(laca.pozo, oscuro, WULF_SEPARACION_FOCO)
            assertTrue(
                "el pozo enfocado en ${if (oscuro) "oscuro" else "claro"} es idéntico al de reposo",
                enfocado != laca.pozo,
            )
        }
    }

    @Test
    fun `el pozo enfocado no se aleja tanto que rompa el texto`() {
        // La otra cara: aclarar el pozo gasta contraste contra el texto que
        // lleva encima. La señal de foco no puede costar la legibilidad de lo
        // que se está enfocando.
        for (oscuro in modos) {
            val laca = lacaDe(WulfAcento.ORO, oscuro)
            val esquema = wulf.compose.tokens.esquemaDe(WulfAcento.ORO, oscuro)
            val enfocado = tonoDeToque(laca.pozo, oscuro, WULF_SEPARACION_FOCO)
            val razon = contraste(esquema.onSurface, enfocado)
            assertTrue(
                "texto sobre pozo enfocado en ${if (oscuro) "oscuro" else "claro"} da %.2f:1"
                    .format(razon),
                razon >= 4.5,
            )
        }
    }

    @Test
    fun `el canto de foco pasa 3 a 1 contra el pozo enfocado en los 3 metales`() {
        // El canto es la señal PRIMARIA (regla 1), así que tiene que cumplir el
        // piso de elemento no textual contra el fondo que le toca — que cuando
        // hay foco NO es el pozo en reposo, sino el aclarado.
        for (acento in WulfAcento.entries) {
            for (oscuro in modos) {
                val laca = lacaDe(acento, oscuro)
                val esquema = wulf.compose.tokens.esquemaDe(acento, oscuro)
                val fondo = tonoDeToque(laca.pozo, oscuro, WULF_SEPARACION_FOCO)
                val razon = contraste(esquema.primary, fondo)
                assertTrue(
                    "canto de foco de $acento ${if (oscuro) "oscuro" else "claro"} da %.2f:1"
                        .format(razon),
                    razon >= 3.0,
                )
            }
        }
    }

    @Test
    fun `la separacion del foco es la misma que la del toque`() {
        // A propósito: el aparato dice "acá estás" con el mismo material se
        // llegue con el dedo o con el mando. Si alguien las separa, que sea
        // porque lo decidió.
        assertTrue(WULF_SEPARACION_FOCO == wulf.compose.superficies.WulfToqueReceta().separacion)
    }
}
