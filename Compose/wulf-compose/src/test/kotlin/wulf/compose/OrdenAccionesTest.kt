package wulf.compose

import org.junit.Assert.assertEquals
import org.junit.Test
import wulf.compose.componentes.wulfEmparejarAcciones

/**
 * Confirmar a la derecha, cancelar a su izquierda, en la misma fila.
 *
 * Es la regla 9, y la que más barata sale de romper: al agregar un botón lo
 * natural es ponerlo donde quepa. El coste de romperla no es estético — con el
 * orden invertido, crear algo al vuelo significa pulsar Cancelar creyendo
 * guardar, porque a la derecha se pulsa sin mirar.
 *
 * Lo que se comprueba acá es el emparejado: que la primera fila lleve una acción
 * de cada columna, y que las acciones sobre el objeto bajen a las filas
 * siguientes de la izquierda — lejos de donde llega el pulgar a confirmar, que
 * es justo donde no se quiere un borrado.
 */
class OrdenAccionesTest {

    @Test
    fun `cancelar y confirmar comparten la primera fila`() {
        // izquierda = [Cancelar], derecha = [Guardar]
        assertEquals(listOf(0 to 0), wulfEmparejarAcciones(izquierda = 1, derecha = 1))
    }

    @Test
    fun `un dialogo de una sola salida la deja a la derecha`() {
        // Sin nada que confirmar no hay confusión posible, y la derecha es donde
        // llega el dedo.
        assertEquals(listOf<Pair<Int?, Int?>>(null to 0), wulfEmparejarAcciones(izquierda = 0, derecha = 1))
    }

    @Test
    fun `las acciones sobre el objeto bajan a filas siguientes de la izquierda`() {
        // izquierda = [Cancelar, Eliminar, Reactivar], derecha = [Guardar]
        assertEquals(
            listOf<Pair<Int?, Int?>>(0 to 0, 1 to null, 2 to null),
            wulfEmparejarAcciones(izquierda = 3, derecha = 1),
        )
    }

    @Test
    fun `nunca hay una fila vacia`() {
        for (izq in 0..4) {
            for (der in 0..4) {
                wulfEmparejarAcciones(izq, der).forEach { (a, b) ->
                    assert(a != null || b != null) { "fila vacía con izq=$izq der=$der" }
                }
            }
        }
    }

    @Test
    fun `hay tantas filas como la columna mas larga`() {
        for (izq in 0..4) {
            for (der in 0..4) {
                assertEquals(maxOf(izq, der), wulfEmparejarAcciones(izq, der).size)
            }
        }
    }

    @Test
    fun `ninguna accion se pierde`() {
        val filas = wulfEmparejarAcciones(izquierda = 3, derecha = 2)
        assertEquals(listOf(0, 1, 2), filas.mapNotNull { it.first })
        assertEquals(listOf(0, 1), filas.mapNotNull { it.second })
    }
}
