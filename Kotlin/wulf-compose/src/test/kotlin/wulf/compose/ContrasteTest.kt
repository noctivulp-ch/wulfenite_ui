package wulf.compose

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import wulf.compose.tokens.WulfAcento
import wulf.compose.tokens.contraste
import wulf.compose.tokens.esquemaDe
import wulf.compose.tokens.lacaDe
import wulf.compose.tokens.ribeteDe
import wulf.compose.tokens.textoEnPozoClaro
import wulf.compose.tokens.textoEnPozoOscuro
import wulf.compose.tokens.textoSecundarioEnPozoClaro
import wulf.compose.tokens.textoSecundarioEnPozoOscuro

/**
 * Los números que sostienen el sistema, recalculados.
 *
 * No están para "tener pruebas": están porque las cifras de contraste son lo que
 * hace que esta paleta sea usable bajo sol directo, y son exactamente lo que se
 * degrada sin que nadie lo note al retocar un tono. Un color que se acerca medio
 * paso a su fondo se ve idéntico en el monitor y deja de leerse en la calle.
 *
 * Los pisos son los de WCAG 2.1: 4,5:1 para texto normal (1.4.3) y 3:1 para
 * texto grande y para elementos no textuales (1.4.11).
 */
class ContrasteTest {

    private val modos = listOf(false, true) // claro, oscuro
    private val acentos = WulfAcento.entries

    private fun nombre(acento: WulfAcento, oscuro: Boolean) =
        "$acento ${if (oscuro) "oscuro" else "claro"}"

    private fun exigir(razon: Double, piso: Double, que: String) {
        assertTrue(
            "$que da %.2f:1 y el piso es %.1f:1".format(razon, piso),
            razon >= piso,
        )
    }

    @Test
    fun `el texto sobre el pozo pasa AA`() {
        for (oscuro in modos) {
            val laca = lacaDe(WulfAcento.ORO, oscuro)
            val texto = if (oscuro) textoEnPozoOscuro else textoEnPozoClaro
            exigir(contraste(texto, laca.pozo), 4.5, "texto en pozo ${if (oscuro) "oscuro" else "claro"}")
        }
    }

    @Test
    fun `el texto secundario sobre el pozo pasa AA`() {
        for (oscuro in modos) {
            val laca = lacaDe(WulfAcento.ORO, oscuro)
            val texto = if (oscuro) textoSecundarioEnPozoOscuro else textoSecundarioEnPozoClaro
            exigir(contraste(texto, laca.pozo), 4.5, "texto secundario en pozo")
        }
    }

    @Test
    fun `el texto sobre la bandeja pasa AA en los dos modos`() {
        for (oscuro in modos) {
            val laca = lacaDe(WulfAcento.ORO, oscuro)
            exigir(contraste(laca.textoEnBandeja, laca.bandeja), 4.5, "texto en bandeja")
            exigir(
                contraste(laca.textoAtenuadoEnBandeja, laca.bandeja),
                4.5,
                "texto atenuado en bandeja",
            )
        }
    }

    /**
     * La regla 1 en forma de número, y la razón de que exista la cresta.
     *
     * En modo oscuro es **imposible** llegar a 3:1 entre bandeja y pozo — el
     * rango de luminancia está comprimido y da 1,43:1. Por eso el tallado no
     * puede depender del escalón de valor entre las dos superficies, y la señal
     * primaria tiene que ser la cresta.
     */
    @Test
    fun `la cresta llega a 3 a 1 contra la bandeja en los 3 metales y los 2 modos`() {
        for (acento in acentos) {
            for (oscuro in modos) {
                val laca = lacaDe(acento, oscuro)
                exigir(
                    contraste(laca.paredCresta, laca.bandeja),
                    3.0,
                    "cresta contra bandeja en ${nombre(acento, oscuro)}",
                )
            }
        }
    }

    /**
     * El canto se apaga hacia el POZO, no hacia la bandeja.
     *
     * Este es el fallo concreto que la prueba de paridad cazó al portar a
     * Python: mezclado hacia la bandeja, el canto se le acerca demasiado y cae
     * por debajo de 3:1 contra ella — justo lo que existe para cumplir. La
     * prueba de arriba lo detectaría, pero esta dice *por qué* falla.
     */
    @Test
    fun `el canto se apaga hacia el pozo`() {
        for (acento in acentos) {
            for (oscuro in modos) {
                val laca = lacaDe(acento, oscuro)
                val ribete = ribeteDe(acento, oscuro)
                assertTrue(
                    "el canto de ${nombre(acento, oscuro)} debería estar más cerca del pozo",
                    contraste(ribete, laca.pozo) < contraste(ribete, laca.bandeja),
                )
            }
        }
    }

    @Test
    fun `el metal como texto sobre el pozo pasa AA en los 3 metales`() {
        for (acento in acentos) {
            for (oscuro in modos) {
                val esquema = esquemaDe(acento, oscuro)
                exigir(
                    contraste(esquema.primary, esquema.surface),
                    4.5,
                    "metal sobre pozo en ${nombre(acento, oscuro)}",
                )
            }
        }
    }

    @Test
    fun `el texto sobre el metal relleno pasa AA en los 3 metales`() {
        for (acento in acentos) {
            for (oscuro in modos) {
                val esquema = esquemaDe(acento, oscuro)
                exigir(
                    contraste(esquema.onPrimary, esquema.primary),
                    4.5,
                    "texto sobre metal en ${nombre(acento, oscuro)}",
                )
            }
        }
    }

    @Test
    fun `el error y su texto pasan AA`() {
        for (oscuro in modos) {
            val esquema = esquemaDe(WulfAcento.ORO, oscuro)
            exigir(contraste(esquema.error, esquema.surface), 4.5, "error sobre pozo")
            exigir(contraste(esquema.onError, esquema.error), 4.5, "texto sobre error")
        }
    }

    /** La fórmula, contra los dos extremos conocidos. */
    @Test
    fun `la formula WCAG da 21 a 1 entre blanco y negro y 1 a 1 consigo mismo`() {
        assertTrue(contraste(Color.White, Color.Black) > 20.9)
        assertTrue(contraste(Color.White, Color.Black) < 21.1)
        assertTrue(contraste(Color.White, Color.White) < 1.001)
    }
}
