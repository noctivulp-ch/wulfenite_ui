package wulf.core

/**
 * Un color del sistema, como 0xRRGGBB.
 *
 * Un entero y no el `Color` de ninguna interfaz: es lo que permite que la paleta
 * y todo lo derivado de ella se calculen y se comprueben sin depender de dónde
 * se vaya a dibujar.
 */
typealias WulfColor = Int

/**
 * Mezcla dos colores **canal a canal en sRGB**, con redondeo a 0..255.
 *
 * Escrita a mano y no tomada de la plataforma, y no es capricho: Compose
 * interpola en Oklab, Dart lo hace canal a canal, y con la misma fusión los dos
 * dan colores DISTINTOS. Medido: el tallado marcado en modo oscuro salía a
 * 2,79:1 con Oklab —por debajo del piso de 3:1 que el sistema se exige— y a
 * 3,09:1 con esta mezcla, **con las constantes de la paleta coincidiendo
 * perfectamente**.
 *
 * Todo color DERIVADO —el canto, el tallado, el tono del toque— pasa por acá.
 */
fun wulfLerp(a: WulfColor, b: WulfColor, t: Float): WulfColor {
    fun canal(desplazamiento: Int): Int {
        val ea = (a shr desplazamiento) and 0xFF
        val eb = (b shr desplazamiento) and 0xFF
        return Math.round(ea + (eb - ea) * t).coerceIn(0, 255)
    }
    return (canal(16) shl 16) or (canal(8) shl 8) or canal(0)
}

/** Luminancia relativa según WCAG 2.1. */
fun luminanciaRelativa(color: WulfColor): Double {
    fun canal(desplazamiento: Int): Double {
        val v = ((color shr desplazamiento) and 0xFF) / 255.0
        return if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
    }
    return 0.2126 * canal(16) + 0.7152 * canal(8) + 0.0722 * canal(0)
}

/**
 * Razón de contraste WCAG entre dos colores opacos, de 1:1 a 21:1.
 *
 * Pública porque el sistema se sostiene con estos números: quien añada un metal
 * tiene que poder comprobarlo con la MISMA fórmula, no con una parecida.
 */
fun contraste(a: WulfColor, b: WulfColor): Double {
    val la = luminanciaRelativa(a)
    val lb = luminanciaRelativa(b)
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
}

/** Los seis tonos de un acabado. */
data class WulfLaca(
    val bandeja: WulfColor,
    val paredCresta: WulfColor,
    val pozo: WulfColor,
    val pozoContacto: WulfColor,
    val textoEnBandeja: WulfColor,
    val textoAtenuadoEnBandeja: WulfColor,
)

// Marco negro, interior de laca roja.
const val BANDEJA_OSCURA = 0x14100E
const val POZO_OSCURO = 0x3A1E1B
const val POZO_CONTACTO_OSCURO = 0x2D110E
const val TEXTO_EN_POZO_OSCURO = 0xF5EAE2
const val TEXTO_SECUNDARIO_EN_POZO_OSCURO = 0xCBA99C
const val TEXTO_EN_BANDEJA_OSCURA = 0xEDE4DC
const val TEXTO_ATENUADO_EN_BANDEJA_OSCURA = 0xA99C93

// Marco blanco, interior de laca verde.
const val BANDEJA_CLARA = 0xF8F7F2
const val POZO_CLARO = 0xD7E1D3
const val POZO_CONTACTO_CLARO = 0xC4D2BF
const val TEXTO_EN_POZO_CLARO = 0x1E2A1C
const val TEXTO_SECUNDARIO_EN_POZO_CLARO = 0x495747
const val TEXTO_EN_BANDEJA_CLARA = 0x232522
const val TEXTO_ATENUADO_EN_BANDEJA_CLARA = 0x55584F

/** Metal por acento: `claro` a `oscuro`. */
val METALES: Map<WulfAcento, Pair<WulfColor, WulfColor>> = mapOf(
    WulfAcento.ORO to (0x7A4F0E to 0xD9A54A),
    WulfAcento.JADE to (0x156353 to 0x5FCBAB),
    WulfAcento.PELTRE to (0x4F5450 to 0xC6C4BC),
)

const val SOBRE_METAL_CLARO = 0xFFFFFF
val SOBRE_METAL_OSCURO: Map<WulfAcento, WulfColor> = mapOf(
    WulfAcento.ORO to 0x241A0C,
    WulfAcento.JADE to 0x06231C,
    WulfAcento.PELTRE to 0x1E1D1A,
)

const val ROJO_ERROR_CLARO = 0x9E2F1B
const val ROJO_ERROR_OSCURO = 0xF0A08C
const val SOBRE_ERROR_CLARO = 0xFFFFFF
const val SOBRE_ERROR_OSCURO = 0x2A0D07

/** Cuánto se apaga el metal para servir de ribete. */
const val APAGADO_RIBETE_CLARO = 0.40f
const val APAGADO_RIBETE_OSCURO = 0.50f

/**
 * Ribete: el metal mezclado **hacia el pozo**.
 *
 * Hacia el pozo y no hacia la bandeja: mezclarlo hacia la bandeja lo deja por
 * debajo de 3:1 contra ella, que es justo lo que el canto existe para cumplir.
 */
fun ribeteDe(acento: WulfAcento, oscuro: Boolean): WulfColor {
    val metal = if (oscuro) METALES.getValue(acento).second else METALES.getValue(acento).first
    val pozo = if (oscuro) POZO_OSCURO else POZO_CLARO
    return wulfLerp(metal, pozo, if (oscuro) APAGADO_RIBETE_OSCURO else APAGADO_RIBETE_CLARO)
}

/** La laca completa de un acento y un modo. */
fun lacaDe(acento: WulfAcento, oscuro: Boolean): WulfLaca = WulfLaca(
    bandeja = if (oscuro) BANDEJA_OSCURA else BANDEJA_CLARA,
    paredCresta = ribeteDe(acento, oscuro),
    pozo = if (oscuro) POZO_OSCURO else POZO_CLARO,
    pozoContacto = if (oscuro) POZO_CONTACTO_OSCURO else POZO_CONTACTO_CLARO,
    textoEnBandeja = if (oscuro) TEXTO_EN_BANDEJA_OSCURA else TEXTO_EN_BANDEJA_CLARA,
    textoAtenuadoEnBandeja =
        if (oscuro) TEXTO_ATENUADO_EN_BANDEJA_OSCURA else TEXTO_ATENUADO_EN_BANDEJA_CLARA,
)

/** El tono de una superficie tocada o enfocada: separada de su reposo. */
fun tonoSeparado(pozo: WulfColor, oscuro: Boolean, separacion: Float): WulfColor =
    wulfLerp(pozo, if (oscuro) 0xFFFFFF else 0x000000, separacion.coerceIn(0f, 1f))
