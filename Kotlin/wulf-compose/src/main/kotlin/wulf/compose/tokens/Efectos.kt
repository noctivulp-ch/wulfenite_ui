package wulf.compose.tokens

/**
 * Efectos visuales que la app puede apagar desde sus propios ajustes.
 *
 * Acá va lo que **cuesta pintar y no cambia lo que la app hace**: adorno, no
 * función. En un aparato de gama baja la diferencia entre un desplazamiento
 * fluido y uno con tirones puede ser un solo efecto.
 *
 * Para agregar uno nuevo: una constante acá, respetarla donde se pinta, y una
 * entrada en el panel de ajustes de la app. Nada más — el resto lo lleva el
 * tema.
 */
enum class WulfEfectoPesado {
    /**
     * El relieve grabado de los números y los iconos: pinta el mismo glifo tres
     * veces (sombra, luz y glifo) para que se lea hundido en la superficie.
     *
     * Medido en el teléfono de gama baja donde nació el sistema: apagarlo baja
     * el raster al desplazar un catálogo de 12,9 ms a 7,9 y los frames fuera de
     * presupuesto de 50 a 4. Es, por lejos, el efecto más caro del sistema.
     */
    TALLADO;

    /** Nombre corto para mostrar en un panel de ajustes. */
    val etiqueta: String
        get() = when (this) {
            TALLADO -> "Relieve grabado"
        }

    /** Qué gana quien lo apaga, en una frase. */
    val explicacion: String
        get() = when (this) {
            TALLADO ->
                "Los números y los iconos se ven hundidos en la superficie. Apagarlo es " +
                    "lo que más alivia el desplazamiento en teléfonos lentos."
        }
}

/**
 * Qué efectos están apagados, disponible en cualquier punto del árbol.
 *
 * Va por el TEMA y no por una librería de estado a propósito: los componentes
 * del sistema no conocen la app que los usa, así que el tallado no necesita
 * saber que existe una pantalla de ajustes. La app traduce su preferencia a esto
 * y la pasa al tema.
 */
@JvmInline
value class WulfEfectos(val apagados: Set<WulfEfectoPesado> = emptySet()) {
    /** Si [efecto] se puede pintar. Es lo que consultan los componentes. */
    fun activo(efecto: WulfEfectoPesado): Boolean = efecto !in apagados
}
