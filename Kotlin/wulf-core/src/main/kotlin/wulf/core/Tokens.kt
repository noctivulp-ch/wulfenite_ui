package wulf.core

/**
 * Los tokens del sistema, en dp como número.
 *
 * Sin `Dp` ni ningún tipo de interfaz: es lo que permite que estos mismos
 * números los use una capa de Compose, una de escritorio, o una prueba que no
 * dibuja nada.
 */
object WulfEspaciado {
    /** La unidad base. Todo lo demás es un múltiplo suyo. */
    const val UNIDAD = 8f
    const val GAP_CELDA = 16f
    const val GAP_GRUPO = 24f
    const val PADDING_CELDA = 16f
    const val PADDING_CELDA_GRANDE = 24f

    /**
     * Pared compartida entre dos pozos. NO es "el fondo asomándose": es material
     * de bandeja, así que dos pozos vecinos comparten UNA sola pared.
     */
    const val PARED = 12f

    /** Labio exterior de la bandeja. */
    const val RIM = 24f

    /**
     * El labio en un televisor, que es el doble.
     *
     * Un televisor recorta los bordes de la imagen (overscan) y la guía de
     * Android TV pide dejar libre un 5 % por lado. Con el labio normal, a 1080p
     * quedan 48 px de 1920 — la mitad de lo que hace falta, medido en el
     * emulador. Lo que se pierde ahí es la cresta de los pozos de los extremos,
     * que es la señal primaria de profundidad.
     */
    const val RIM_TELEVISION = 48f

    /** El labio que le toca a un tamaño. */
    fun rimDe(ancho: WulfAncho): Float =
        if (ancho.esLejano) RIM_TELEVISION else RIM
}

/** Los dos únicos radios: interior rectilíneo, canto exterior suave. */
object WulfRadios {
    const val POZO = 4f
    const val BANDEJA = 20f
}

/**
 * Objetivo táctil mínimo. Coincide con la guía de Android, así que no cuesta
 * nada respetarlo.
 */
const val WULF_OBJETIVO_TACTIL_MIN = 48f

/**
 * Lado de una miniatura dentro de una fila.
 *
 * Deliberadamente por DEBAJO del objetivo táctil: no es un objetivo, es un hueco
 * decorativo dentro de una fila que ya es tocable entera. Existe como token
 * porque escrito a mano invita a "corregirlo".
 */
const val WULF_MINIATURA = 44f

/** Duración del feedback táctil, en milisegundos. */
const val WULF_FEEDBACK_TACTIL_MS = 261

/**
 * Metal del acento. El MODO decide la laca completa; el metal es ortogonal,
 * igual en claro y en oscuro.
 */
enum class WulfAcento { ORO, JADE, PELTRE }

/**
 * Tamaños a los que se dibuja un icono, en dp.
 *
 * Tres y no una escala libre: un icono a 22 junto a otro a 24 se ve descuadrado
 * sin que nadie sepa decir por qué, y con tres nombres nadie tiene que elegir un
 * número.
 *
 * **Ninguno es el objetivo táctil.** Un icono de 24 dentro de un botón sigue
 * necesitando sus 48 alrededor — el dibujo es lo que se ve, no lo que se toca.
 */
object WulfIconoTamano {
    /** En línea con un texto: junto a una etiqueta, dentro de un campo. */
    const val CHICO = 20f

    /** El de siempre. Es el tamaño de la grilla, así que no se reescala. */
    const val NORMAL = 24f

    /** Protagonista: un destino del rail, un estado vacío, un mando grande. */
    const val GRANDE = 32f

    /**
     * En un televisor, el normal se queda corto.
     *
     * Es la misma razón que el labio y el canto del foco: a tres metros lo que en
     * la mano se lee perfectamente deja de leerse.
     */
    const val TELEVISION = 36f

    /** El tamaño que le toca a un ancho de pantalla. */
    fun de(ancho: WulfAncho, base: Float = NORMAL): Float =
        if (ancho.esLejano) maxOf(base, TELEVISION) else base
}

/**
 * Aire entre un icono y el texto que lo acompaña.
 *
 * La mitad de la unidad: con la unidad entera el par se lee como dos cosas
 * sueltas en vez de como una etiqueta con su icono.
 */
const val WULF_GAP_ICONO = 4f
