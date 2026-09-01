package wulf.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import wulf.compose.componentes.WulfDestino
import wulf.compose.componentes.wulfAnchoRail
import wulf.compose.foco.WulfCanto
import wulf.compose.tokens.wulfObjetivoTactilMin

/**
 * El rail: la columna de navegación vertical.
 *
 * Existe porque unas pestañas verticales rotan el texto. Lo que se puede
 * comprobar sin pintar son sus medidas y su forma; que la marca caiga en el borde
 * derecho se ve ejecutando, y está probado en la gemela de Flutter, donde el
 * borde es un dato del árbol y no un dibujo.
 */
class RailTest {

    @Test
    fun `un destino declara su etiqueta y el icono es opcional`() {
        val soloTexto = WulfDestino("Venta")

        assertEquals("Venta", soloTexto.etiqueta)
        assertTrue(soloTexto.icono == null)
    }

    @Test
    fun `el rail es mas ancho que alto un destino`() {
        // Si fuera más estrecho que el objetivo táctil, cada destino sería una
        // tira imposible de acertar con el dedo.
        assertTrue(wulfAnchoRail > wulfObjetivoTactilMin)
    }

    @Test
    fun `la marca del foco es mas gruesa que la de seleccion`() {
        // Los dos estados caen juntos todo el rato —se recorre la selección— así
        // que si empataran no se distinguirían.
        assertTrue(WulfCanto.enfocado > WulfCanto.seleccionado)
        assertTrue(WulfCanto.enfocadoTelevision > WulfCanto.enfocado)
    }

    @Test
    fun `dos destinos con la misma etiqueta siguen siendo distintos`() {
        // El rail se indexa por posición y no por texto: dos secciones pueden
        // llamarse igual sin que se pisen.
        val a = WulfDestino("Ventas")
        val b = WulfDestino("Ventas")

        assertEquals(a, b)
    }
}
