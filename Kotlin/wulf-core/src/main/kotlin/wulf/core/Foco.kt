package wulf.core

/**
 * Grosor del canto según el estado, en dp. Es la regla 12 en números.
 *
 * El foco no estrena una señal propia: usa la que el sistema ya tiene para decir
 * "este" —el canto— subida un escalón.
 *
 * | estado | canto | relleno |
 * |---|---|---|
 * | reposo | cresta (metal apagado) | pozo |
 * | seleccionado | metal pleno | pozo |
 * | enfocado | metal pleno, más grueso | pozo aclarado |
 */
object WulfCanto {
    const val REPOSO = 1f
    const val SELECCIONADO = 2f

    /**
     * Enfocado por mando o teclado.
     *
     * Tres y no dos: a distancia de televisor un escalón de 1 dp no existe. Es el
     * mismo motivo por el que el objetivo táctil es de 48 y no de 32.
     */
    const val ENFOCADO = 3f

    /**
     * En un televisor engorda otro escalón.
     *
     * A tres metros lo que en la mano se distingue perfectamente deja de verse, y
     * el foco es lo único que dice dónde está uno cuando se navega con mando.
     */
    const val ENFOCADO_TELEVISION = 4f

    /** El canto de foco que le toca a un tamaño. */
    fun enfocadoDe(ancho: WulfAncho): Float =
        if (ancho.esLejano) ENFOCADO_TELEVISION else ENFOCADO
}

/**
 * Cuánto se aclara el pozo enfocado.
 *
 * Coincide a propósito con la separación del tono del toque: el aparato dice
 * "acá estás" con el mismo material se llegue con el dedo, con el mando o con el
 * teclado.
 */
const val WULF_SEPARACION_FOCO = 0.10f
