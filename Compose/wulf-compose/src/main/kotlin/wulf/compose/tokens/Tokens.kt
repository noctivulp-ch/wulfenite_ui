package wulf.compose.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tokens de espaciado. Unidad base 8 (regla del sistema: solo múltiplos, nunca
 * valores ad hoc).
 */
object WulfSpacing {
    /**
     * La unidad base. Todo lo demás es un múltiplo suyo; un valor que no lo sea
     * rompe el ritmo aunque a ojo no se note.
     */
    val unidad: Dp = 8.dp

    /** Separación entre elementos emparentados dentro de un mismo pozo. */
    val gapCelda: Dp = 16.dp

    /** Separación entre grupos distintos de contenido. */
    val gapGrupo: Dp = 24.dp

    /** Aire interior de un pozo normal. */
    val paddingCelda: Dp = 16.dp

    /** Aire interior de un pozo protagonista, del que se mira primero. */
    val paddingCeldaGrande: Dp = 24.dp

    /**
     * Ancho de la pared compartida entre dos pozos adyacentes. NO es "el fondo
     * asomándose" — es material de bandeja (`doc/reglas.md`, regla 2), así que
     * dos pozos vecinos comparten UNA sola pared de este ancho.
     */
    val pared: Dp = 12.dp

    /**
     * Borde exterior de la bandeja. Más grueso que la pared interior, como el
     * labio de una bandeja real.
     */
    val rim: Dp = 24.dp
}

/**
 * Los dos únicos radios del sistema.
 *
 * Dos y no una escala: el interior de la bandeja es rectilíneo y el canto
 * exterior es suave. Un radio intermedio no significa nada.
 */
object WulfRadius {
    /**
     * Interior rectilíneo: los compartimentos de una bandeja no son
     * rectángulos redondeados sueltos.
     */
    val pozo: Dp = 4.dp

    /** Exterior suave, como el canto de la bandeja. */
    val bandeja: Dp = 20.dp
}

/**
 * Duración del feedback táctil (onda al presionar), en milisegundos.
 *
 * A 200 el toque no llegaba a leerse — la onda crecía y se apagaba antes de que
 * la pantalla reaccionara, así que el elemento tocado nunca se veía lleno. Ahora
 * crece en 261, se sostiene un tercio del desvanecido y recién ahí se apaga. El
 * número exacto sale de probarlo con el dedo en el aparato, no de una tabla.
 */
const val WULF_FEEDBACK_TACTIL_MS: Int = 261

/**
 * Objetivo táctil mínimo. Coincide con la propia guía de Android, así que acá no
 * cuesta nada: todo lo tocable lo respeta (`doc/reglas.md`, regla 7).
 */
val wulfObjetivoTactilMin: Dp = 48.dp

/**
 * Lado de la miniatura de producto en una fila de lista.
 *
 * Deliberadamente por DEBAJO de [wulfObjetivoTactilMin]: no es un objetivo
 * táctil, es un hueco decorativo dentro de una fila que ya es tocable entera.
 * Existe como token porque escrito a mano como `44` invita a "corregirlo" a 48
 * creyendo que incumple la regla 7.
 */
val wulfMiniatura: Dp = 44.dp

/**
 * Metal del acento. Sobre laca, el pan de metal tradicional es oro, pero peltre
 * también lo es, y el jade es la incrustación de piedra que acompaña al pan.
 *
 * El reparto de responsabilidades es lo importante: el MODO (claro/oscuro)
 * determina la laca completa (blanco-verde u oscuro-rojo) y el metal es
 * ortogonal, igual en los dos modos.
 *
 * Conviene resolver un acento guardado cayendo al primero si no se reconoce:
 * así, si algún día se quita un metal, una preferencia vieja cae sola en vez de
 * reventar.
 */
enum class WulfAcento {
    /** Latón cálido. El de fábrica. */
    ORO,

    /** Verde metálico, más frío. */
    JADE,

    /** Gris metálico, el más neutro. Para cuando el color no debe opinar. */
    PELTRE,
}
