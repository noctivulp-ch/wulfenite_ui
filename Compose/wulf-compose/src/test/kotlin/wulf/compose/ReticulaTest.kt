package wulf.compose

import org.junit.Assert.assertEquals
import org.junit.Test
import wulf.compose.reticula.WULF_COLUMNAS
import wulf.compose.reticula.WulfAncho
import wulf.compose.reticula.repartirEnFilas
import wulf.compose.reticula.wulfPorFila

/**
 * El reparto en filas y los cortes de tamaño.
 *
 * Es la parte de la retícula donde de verdad se puede uno equivocar: una celda
 * que se parte entre dos filas, o una que se cuela en una fila ya llena, sale en
 * pantalla como una composición torcida y no como un error.
 */
class ReticulaTest {

    @Test
    fun `una celda que no entra abre la siguiente fila, nunca se parte`() {
        // 3 + 3 llenan la fila; el siguiente 3 tiene que bajar.
        assertEquals(listOf(listOf(3, 3), listOf(3)), repartirEnFilas(listOf(3, 3, 3)))
    }

    @Test
    fun `las celdas que caben comparten fila`() {
        assertEquals(listOf(listOf(2, 2, 2)), repartirEnFilas(listOf(2, 2, 2)))
    }

    @Test
    fun `una celda de ancho completo se queda sola`() {
        assertEquals(
            listOf(listOf(6), listOf(3, 3)),
            repartirEnFilas(listOf(6, 3, 3)),
        )
    }

    @Test
    fun `el hero de 3 con tres turnos de 1 llena una fila exacta`() {
        // El caso real que motivó las seis columnas: un total grande y tres
        // tramos del día al lado, todo en una fila.
        assertEquals(listOf(listOf(3, 1, 1, 1)), repartirEnFilas(listOf(3, 1, 1, 1)))
    }

    @Test
    fun `sin celdas no hay filas`() {
        assertEquals(emptyList<List<Int>>(), repartirEnFilas(emptyList()))
    }

    @Test
    fun `ninguna fila suma mas que las columnas disponibles`() {
        val spans = listOf(4, 3, 2, 2, 1, 6, 5, 1, 3)
        repartirEnFilas(spans).forEach { fila ->
            assert(fila.sum() <= WULF_COLUMNAS) { "fila de ${fila.sum()} columnas: $fila" }
        }
    }

    @Test
    fun `no se pierde ni se duplica ninguna celda`() {
        val spans = listOf(4, 3, 2, 2, 1, 6, 5, 1, 3)
        assertEquals(spans, repartirEnFilas(spans).flatten())
    }

    @Test
    fun `los cortes de tamano caen donde dicen`() {
        assertEquals(WulfAncho.COMPACTO, WulfAncho.deAncho(360))
        assertEquals(WulfAncho.COMPACTO, WulfAncho.deAncho(599))
        assertEquals(WulfAncho.MEDIO, WulfAncho.deAncho(600))
        assertEquals(WulfAncho.MEDIO, WulfAncho.deAncho(1023))
        assertEquals(WulfAncho.AMPLIO, WulfAncho.deAncho(1024))
    }

    @Test
    fun `el telefono de diseno es compacto`() {
        assert(WulfAncho.deAncho(360).esCompacto)
    }

    @Test
    fun `porFila traduce spans a elementos por fila`() {
        // Un producto de 3 columnas en teléfono son 2 por fila; de 1 en
        // escritorio, 6.
        assertEquals(2, wulfPorFila(WulfAncho.COMPACTO, compacto = 3, medio = 2, amplio = 1))
        assertEquals(3, wulfPorFila(WulfAncho.MEDIO, compacto = 3, medio = 2, amplio = 1))
        assertEquals(6, wulfPorFila(WulfAncho.AMPLIO, compacto = 3, medio = 2, amplio = 1))
    }
}
