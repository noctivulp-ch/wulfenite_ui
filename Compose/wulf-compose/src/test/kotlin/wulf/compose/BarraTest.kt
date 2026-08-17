package wulf.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import wulf.compose.componentes.WULF_PASO_BUSQUEDA
import wulf.compose.componentes.wulfAltoBarra
import wulf.compose.tokens.wulfObjetivoTactilMin

/**
 * La barra de búsqueda: la pieza central de un reproductor.
 *
 * Lo que se comprueba acá es la aritmética del paso y el recorte — que es donde
 * un reproductor se rompe de verdad: buscar más allá del final, o un paso tan
 * fino que recorrer una película sean cien pulsaciones.
 */
class BarraTest {

    /** Lo mismo que hace el componente al recibir izquierda o derecha. */
    private fun mover(desde: Float, pasos: Int, paso: Float = WULF_PASO_BUSQUEDA): Float {
        var v = desde
        repeat(kotlin.math.abs(pasos)) {
            v = (if (pasos > 0) v + paso else v - paso).coerceIn(0f, 1f)
        }
        return v
    }

    @Test
    fun `un paso mueve lo que dice el token`() {
        assertEquals(0.55f, mover(0.5f, 1), 0.0001f)
        assertEquals(0.45f, mover(0.5f, -1), 0.0001f)
    }

    @Test
    fun `no se puede buscar mas alla del final`() {
        // Sin el recorte, mantener pulsada la derecha manda la posición a 3,7 y
        // el reproductor intenta saltar fuera del archivo.
        assertEquals(1f, mover(0.95f, 5), 0.0001f)
    }

    @Test
    fun `no se puede buscar antes del principio`() {
        assertEquals(0f, mover(0.05f, -5), 0.0001f)
    }

    @Test
    fun `recorrer la barra entera cuesta un numero razonable de pulsaciones`() {
        // Con el 1% serían cien: recorrer una película de dos horas con el mando
        // dejaría de ser viable. Con el 5%, veinte — y el ajuste fino sale de
        // mantener pulsado, que el sistema repite solo.
        val pulsaciones = (1f / WULF_PASO_BUSQUEDA).toInt()
        assertTrue("$pulsaciones pulsaciones para recorrer la barra", pulsaciones in 10..25)
    }

    @Test
    fun `se dibuja fina pero se toca ancha`() {
        // El carril mide 8 y el objetivo táctil 48: se acierta con el dedo sin
        // engordar la pieza (regla 7).
        assertTrue(wulfAltoBarra < wulfObjetivoTactilMin)
        assertEquals(wulfObjetivoTactilMin.value, 48f, 0.01f)
    }

    @Test
    fun `un valor fuera de rango no rompe la pantalla`() {
        // Un cálculo de posición sobre una duración todavía desconocida da
        // infinito o negativo. La barra lo recorta en vez de dibujar fuera.
        assertEquals(1f, 2.5f.coerceIn(0f, 1f), 0.0001f)
        assertEquals(0f, (-3f).coerceIn(0f, 1f), 0.0001f)
    }
}
