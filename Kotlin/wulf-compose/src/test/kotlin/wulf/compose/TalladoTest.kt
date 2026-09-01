package wulf.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import wulf.compose.texto.TalladoNivel
import wulf.compose.texto.colorTallado
import wulf.compose.tokens.WulfAcento
import wulf.compose.tokens.contraste
import wulf.compose.tokens.esquemaDe
import wulf.compose.tokens.lacaDe

/**
 * El tallado gasta contraste a propósito, y por eso lleva techo.
 *
 * Acercar el glifo al color de su superficie es literalmente lo que produce el
 * relieve — y también lo que lo hace peligroso: pasado de fusión, el número
 * queda bonito y deja de leerse. Estas pruebas son el techo.
 *
 * Los dos niveles no son un capricho: la fusión se calibró mirando modo oscuro,
 * donde queda al borde de 4,5:1, pero la paleta clara tiene mucho menos margen
 * de luminancia y la misma fusión cae a 2,83:1 ahí. De ahí que sea por modo Y
 * por nivel.
 */
class TalladoTest {

    private val modos = listOf(false, true)

    @Test
    fun `el nivel sutil se queda por encima de 4,5 a 1 en los 2 modos`() {
        for (oscuro in modos) {
            val laca = lacaDe(WulfAcento.ORO, oscuro)
            val esquema = esquemaDe(WulfAcento.ORO, oscuro)
            val tallado = colorTallado(esquema.onSurface, laca.pozo, oscuro, TalladoNivel.SUTIL)
            val razon = contraste(tallado, laca.pozo)
            assertTrue(
                "sutil en ${if (oscuro) "oscuro" else "claro"} da %.2f:1".format(razon),
                razon >= 4.5,
            )
        }
    }

    @Test
    fun `el nivel marcado se queda por encima de 3 a 1 en los 2 modos`() {
        // 3:1 es lo que WCAG permite para texto grande (1.4.3) y para elementos
        // no textuales (1.4.11), que es exactamente donde se usa este nivel.
        for (oscuro in modos) {
            val laca = lacaDe(WulfAcento.ORO, oscuro)
            val esquema = esquemaDe(WulfAcento.ORO, oscuro)
            val tallado = colorTallado(esquema.onSurface, laca.pozo, oscuro, TalladoNivel.MARCADO)
            val razon = contraste(tallado, laca.pozo)
            assertTrue(
                "marcado en ${if (oscuro) "oscuro" else "claro"} da %.2f:1".format(razon),
                razon >= 3.0,
            )
        }
    }

    @Test
    fun `marcado se funde mas que sutil, que es de lo que se trata`() {
        for (oscuro in modos) {
            val laca = lacaDe(WulfAcento.ORO, oscuro)
            val esquema = esquemaDe(WulfAcento.ORO, oscuro)
            val sutil = contraste(
                colorTallado(esquema.onSurface, laca.pozo, oscuro, TalladoNivel.SUTIL),
                laca.pozo,
            )
            val marcado = contraste(
                colorTallado(esquema.onSurface, laca.pozo, oscuro, TalladoNivel.MARCADO),
                laca.pozo,
            )
            assertTrue("marcado debería gastar más contraste que sutil", marcado < sutil)
        }
    }

    @Test
    fun `fundir del todo devuelve la superficie`() {
        val laca = lacaDe(WulfAcento.ORO, oscuro = false)
        // Sanidad de la interpolación: es la operación de la que dependen los
        // dos niveles y las dos capas de relieve.
        assertEquals(laca.pozo, wulf.compose.tokens.wulfLerp(laca.textoEnBandeja, laca.pozo, 1f))
    }
}
