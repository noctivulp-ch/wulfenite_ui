package wulf.core

/**
 * Cuánta jerarquía tiene el glifo, y con ella cuánto contraste se puede gastar.
 *
 * El tallado se consigue acercando el glifo al color de su superficie, así que es
 * literalmente contraste gastado en estética. Gastarlo donde el tamaño lo
 * compensa —números grandes, iconos— y no donde hay que leer.
 */
enum class TalladoNivel { SUTIL, MARCADO }

/** Desplazamiento de las capas de luz y sombra. */
const val WULF_TALLADO_PROFUNDIDAD = 1.25f

const val WULF_LUZ_INTENSIDAD = 0.70f
const val WULF_SOMBRA_INTENSIDAD = 0.65f

/**
 * Cuánto se funde el glifo con su superficie, por nivel: `(claro, oscuro)`.
 *
 * No puede ser un solo número: la paleta clara tiene mucho menos margen de
 * luminancia, y la misma fusión que en oscuro queda al borde de 4,5:1 ahí cae a
 * 2,83:1.
 */
val FUSION: Map<TalladoNivel, Pair<Float, Float>> = mapOf(
    TalladoNivel.SUTIL to (0.28f to 0.42f),
    TalladoNivel.MARCADO to (0.46f to 0.62f),
)

/** Color de glifo tallado: el color base acercado a su superficie. */
fun colorTallado(
    base: WulfColor,
    sobre: WulfColor,
    oscuro: Boolean,
    nivel: TalladoNivel = TalladoNivel.SUTIL,
): WulfColor {
    val (claro, fosco) = FUSION.getValue(nivel)
    return wulfLerp(base, sobre, if (oscuro) fosco else claro)
}
