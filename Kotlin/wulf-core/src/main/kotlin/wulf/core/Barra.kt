package wulf.core

/** Alto del carril de una barra. Fina: es un pozo alargado, no una pieza con volumen. */
const val WULF_ALTO_BARRA = 8f

/**
 * Cuánto avanza un paso del mando, en tanto por uno.
 *
 * Cinco por ciento y no uno: con el 1 % recorrer una película de dos horas serían
 * cien pulsaciones. Con el 5 %, veinte — y el ajuste fino sale de mantener
 * pulsado, que el sistema repite solo.
 */
const val WULF_PASO_DESLIZADOR = 0.05f

/** Mueve una posición de 0..1 el número de pasos indicado, sin salirse. */
fun moverPasos(desde: Float, pasos: Int, paso: Float = WULF_PASO_DESLIZADOR): Float =
    (desde + pasos * paso).coerceIn(0f, 1f)
