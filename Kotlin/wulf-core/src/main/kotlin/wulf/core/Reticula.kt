package wulf.core

/**
 * Los cuatro tamaños de pantalla, definidos UNA vez.
 *
 * Tres salen del ancho. El cuarto **no**, y esa es la parte que importa:
 *
 * **[TELEVISION] no es un ancho, es una distancia.** Un televisor de 1080p
 * reporta unos 960 dp lógicos —medido en el emulador de Android TV— y por ancho
 * caería en [MEDIO], que es el reparto de una tablet que se mira a cuarenta
 * centímetros. A tres metros esa composición no se lee: hacen falta menos piezas
 * y más grandes, y el canto del foco tiene que engordar.
 *
 * Por eso no se deduce del tamaño. **Se le pregunta al sistema**, que es el único
 * que distingue un monitor de 1920 de un televisor de 1920. Donde la plataforma
 * lo dice —Android, por el `uiMode`— se resuelve solo; donde no lo expone, lo
 * declara la app.
 */
enum class WulfAncho {
    /** Teléfono. 360 dp es el caso de diseño. */
    COMPACTO,

    /** Tablet. */
    MEDIO,

    /** Escritorio. */
    AMPLIO,

    /** Televisor: se mira de lejos y se maneja con mando. */
    TELEVISION;

    /** Atajo para el caso que más se consulta: si hay que componer para teléfono. */
    val esCompacto: Boolean get() = this == COMPACTO

    /**
     * Si se mira de lejos. Lo que cambia acá no es el ancho sino el tamaño de
     * todo: el labio de la bandeja, el canto del foco y cuántas piezas caben
     * antes de que deje de leerse.
     */
    val esLejano: Boolean get() = this == TELEVISION

    companion object {
        const val CORTE_MEDIO = 600
        const val CORTE_AMPLIO = 1024

        /**
         * El tamaño que le toca a un ancho dado, en dp.
         *
         * Se decide a partir del ancho de una CAJA y no del de la ventana: un
         * panel lateral angosto dentro de un escritorio ancho necesita
         * componerse como compacto.
         *
         * Nunca devuelve [TELEVISION]: eso no se deduce del ancho. Ver [de].
         */
        fun deAncho(anchoDp: Int): WulfAncho = when {
            anchoDp >= CORTE_AMPLIO -> AMPLIO
            anchoDp >= CORTE_MEDIO -> MEDIO
            else -> COMPACTO
        }

        /**
         * El tamaño, contando lo que dice la plataforma.
         *
         * [esTelevision] gana sobre el ancho, porque la distancia de lectura no
         * se puede medir en píxeles.
         */
        fun de(anchoDp: Int, esTelevision: Boolean): WulfAncho =
            if (esTelevision) TELEVISION else deAncho(anchoDp)
    }
}

/**
 * Cuántas columnas tiene la retícula. Es el mismo número en los cuatro tamaños:
 * lo que cambia es cuántas ocupa cada pieza.
 *
 * Seis porque se divide en 2 y en 3, que son los repartos que piden estas
 * pantallas. Con cuatro los tercios no cierran; con doce, a 360 dp cada columna
 * queda en unos 21 dp y el número deja de significar nada.
 */
const val WULF_COLUMNAS = 6

/**
 * Cuántos elementos entran por fila si cada uno ocupa el span indicado.
 *
 * Para listas largas no se puede construir la retícula entera de una: eso pide
 * un contenedor perezoso con el número de columnas EXPLÍCITO, y este ayudante es
 * el que hace que ese número salga del mismo vocabulario de spans que el resto.
 */
fun wulfPorFila(ancho: WulfAncho, compacto: Int, medio: Int, amplio: Int, television: Int = amplio): Int {
    val span = when (ancho) {
        WulfAncho.COMPACTO -> compacto
        WulfAncho.MEDIO -> medio
        WulfAncho.AMPLIO -> amplio
        WulfAncho.TELEVISION -> television
    }
    return WULF_COLUMNAS / span
}

/**
 * Empaqueta los spans en filas sin partir ninguno: uno que no entra en lo que
 * queda de la fila abre la siguiente.
 *
 * Es aritmética pura a propósito: el reparto es la parte de la retícula donde de
 * verdad se puede equivocar uno, y así se comprueba sin pintar nada.
 */
fun repartirEnFilas(spans: List<Int>): List<List<Int>> {
    val filas = mutableListOf<List<Int>>()
    var actual = mutableListOf<Int>()
    var ocupadas = 0

    for (span in spans) {
        if (ocupadas + span > WULF_COLUMNAS && actual.isNotEmpty()) {
            filas += actual
            actual = mutableListOf()
            ocupadas = 0
        }
        actual += span
        ocupadas += span
    }
    if (actual.isNotEmpty()) filas += actual
    return filas
}
