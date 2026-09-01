package wulf.compose.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Mezcla dos colores **canal a canal en sRGB**, con redondeo a 0..255.
 *
 * NO se usa el `lerp` de Compose, y no es un capricho: Compose interpola en
 * Oklab, y Dart —el original de este sistema— lo hace canal a canal sobre los
 * enteros de 0 a 255. Con la misma fusión los dos dan colores DISTINTOS.
 *
 * Lo cazó la prueba del tallado: el nivel marcado en modo oscuro salía a 2,79:1
 * con Oklab, por debajo del piso de 3:1, y a 3,09:1 con esta mezcla. O sea que
 * usar el `lerp` de la plataforma no habría dado "casi lo mismo": habría dado un
 * sistema que incumple su propia regla de contraste, y además un canto de pozo
 * que no coincide con el de las otras dos gemelas.
 *
 * Todo color DERIVADO del sistema —el canto, el tallado, el tono del toque—
 * tiene que pasar por acá.
 */
fun wulfLerp(a: Color, b: Color, t: Float): Color {
    fun canal(x: Float, y: Float): Float {
        val ex = Math.round(x * 255f)
        val ey = Math.round(y * 255f)
        return Math.round(ex + (ey - ex) * t).coerceIn(0, 255) / 255f
    }
    return Color(canal(a.red, b.red), canal(a.green, b.green), canal(a.blue, b.blue), 1f)
}

/**
 * Los seis tonos de un acabado: lo que `ColorScheme` no tiene cómo expresar.
 *
 * El mapeo deliberado con `ColorScheme`: `surface` es el POZO, no la bandeja,
 * para que el texto por defecto de cualquier componente caiga correcto sobre el
 * fondo de contenido. La bandeja vive acá y se usa explícitamente en el cromo
 * (barras, paredes).
 */
data class WulfLaca(
    /**
     * El plano continuo de la bandeja: fondo de pantalla y material de las
     * paredes entre pozos.
     */
    val bandeja: Color,
    /**
     * Ribete del pozo: 1 dp en su borde. Es la señal PRIMARIA de tallado
     * —verificada >=3:1 contra la bandeja en los 3 metales y los 2 modos, porque
     * en modo oscuro el escalón de valor bandeja/pozo no puede llegar a 3:1
     * (rango de luminancia comprimido)— y es el METAL APAGADO, no un tono fijo:
     * en laca real el pan de metal ribetea los cantos de la pieza. Con un tono
     * fijo, elegir peltre dejaba el elemento seleccionado en plata y todos los
     * demás bordes leyéndose dorados.
     */
    val paredCresta: Color,
    /** Fondo del pozo: la superficie donde vive el contenido. */
    val pozo: Color,
    /**
     * Sombra que la pared proyecta hacia dentro del pozo, en su borde superior.
     * Es un degradado corto y duro, nunca una sombra suave: bajo sol directo las
     * sombras de baja opacidad desaparecen.
     */
    val pozoContacto: Color,
    /** Texto sobre la bandeja — el cromo, no el contenido. */
    val textoEnBandeja: Color,
    /** Texto secundario sobre la bandeja: rótulos y apoyos. */
    val textoAtenuadoEnBandeja: Color,
)

// --- Paleta de autoría propia -----------------------------------------------
// Nada de generar la paleta desde una semilla: el algoritmo tonal de Material 3
// codifica elevación-como-claridad, que es exactamente lo contrario de un pozo,
// y es la causa raíz del aspecto por defecto (`doc/reglas.md`, regla 5).
//
// Contraste verificado con la fórmula WCAG sobre los pares realmente usados
// (texto >=4.5:1; bordes de superficie >=3:1 por WCAG 1.4.11). `ContrasteTest`
// recalcula estos números contra estas mismas constantes, para que no se
// degraden en una edición futura.
//
// Dos lacas completas, una por modo, tomadas de la construcción real de las
// cajas de laca de referencia: NEGRAS por fuera y ROJAS por dentro. El modo
// claro es su reflejo: BLANCA por fuera, VERDE por dentro.
//
//   oscuro -> negro (marco) + rojo (pozos) + metal
//   claro  -> blanco (marco) + verde (pozos) + metal

// Marco negro, interior de laca roja.
internal val bandejaOscura = Color(0xFF14100E)
internal val pozoOscuro = Color(0xFF3A1E1B)
internal val pozoContactoOscuro = Color(0xFF2D110E)
internal val textoEnPozoOscuro = Color(0xFFF5EAE2) // 12.85:1
internal val textoSecundarioEnPozoOscuro = Color(0xFFCBA99C) // 7.02:1
internal val textoEnBandejaOscura = Color(0xFFEDE4DC) // 15.07:1
internal val textoAtenuadoEnBandejaOscura = Color(0xFFA99C93) // 7.08:1

// Marco blanco, interior de laca verde.
internal val bandejaClara = Color(0xFFF8F7F2)
internal val pozoClaro = Color(0xFFD7E1D3)
internal val pozoContactoClaro = Color(0xFFC4D2BF)
internal val textoEnPozoClaro = Color(0xFF1E2A1C) // 11.12:1
internal val textoSecundarioEnPozoClaro = Color(0xFF495747) // 5.70:1
internal val textoEnBandejaClara = Color(0xFF232522) // 14.40:1
internal val textoAtenuadoEnBandejaClara = Color(0xFF55584F) // 6.76:1

/** Metal del acento, ortogonal al modo: `claro` a `oscuro`. */
internal val METALES: Map<WulfAcento, Pair<Color, Color>> = mapOf(
    WulfAcento.ORO to (Color(0xFF7A4F0E) to Color(0xFFD9A54A)),
    WulfAcento.JADE to (Color(0xFF156353) to Color(0xFF5FCBAB)),
    WulfAcento.PELTRE to (Color(0xFF4F5450) to Color(0xFFC6C4BC)),
)

internal val sobreMetalClaro = Color(0xFFFFFFFF)
internal val SOBRE_METAL_OSCURO: Map<WulfAcento, Color> = mapOf(
    WulfAcento.ORO to Color(0xFF241A0C),
    WulfAcento.JADE to Color(0xFF06231C),
    WulfAcento.PELTRE to Color(0xFF1E1D1A),
)

internal val rojoErrorClaro = Color(0xFF9E2F1B)
internal val rojoErrorOscuro = Color(0xFFF0A08C)
internal val sobreErrorClaro = Color(0xFFFFFFFF)
internal val sobreErrorOscuro = Color(0xFF2A0D07)

/**
 * Cuánto se apaga el metal para servir de ribete. En laca real el pan de metal
 * ribetea los cantos de la pieza, así que el borde de los pozos ES el metal —
 * pero apagado, para que la selección (metal a plena fuerza) siga destacando.
 */
internal const val APAGADO_RIBETE_CLARO = 0.40f
internal const val APAGADO_RIBETE_OSCURO = 0.50f

/**
 * Ribete: el metal mezclado **hacia el pozo**. Derivado y no escrito a mano para
 * que siga siendo correcto si un metal cambia.
 *
 * Hacia el pozo y no hacia la bandeja: mezclarlo hacia la bandeja lo deja por
 * debajo de 3:1 contra ella, que es justo lo que el canto existe para cumplir.
 * Ese fallo lo cazó la prueba de paridad al portar a Python, y acá se hereda ya
 * corregido.
 */
fun ribeteDe(acento: WulfAcento, oscuro: Boolean): Color {
    val metal = if (oscuro) METALES.getValue(acento).second else METALES.getValue(acento).first
    val pozo = if (oscuro) pozoOscuro else pozoClaro
    return wulfLerp(metal, pozo, if (oscuro) APAGADO_RIBETE_OSCURO else APAGADO_RIBETE_CLARO)
}

/** La laca completa de un acento y un modo. */
fun lacaDe(acento: WulfAcento, oscuro: Boolean): WulfLaca = WulfLaca(
    bandeja = if (oscuro) bandejaOscura else bandejaClara,
    paredCresta = ribeteDe(acento, oscuro),
    pozo = if (oscuro) pozoOscuro else pozoClaro,
    pozoContacto = if (oscuro) pozoContactoOscuro else pozoContactoClaro,
    textoEnBandeja = if (oscuro) textoEnBandejaOscura else textoEnBandejaClara,
    textoAtenuadoEnBandeja =
        if (oscuro) textoAtenuadoEnBandejaOscura else textoAtenuadoEnBandejaClara,
)

/**
 * El `ColorScheme` de Material 3 mapeado a propósito.
 *
 * Toda la escalera de contenedores apunta al mismo pozo: neutraliza el "más
 * elevado = más claro" para que ningún componente derive solo hacia un tono más
 * claro y vuelva a parecer una tarjeta flotante.
 */
fun esquemaDe(acento: WulfAcento, oscuro: Boolean): ColorScheme {
    val pozo = if (oscuro) pozoOscuro else pozoClaro
    val textoEnPozo = if (oscuro) textoEnPozoOscuro else textoEnPozoClaro
    val metal = if (oscuro) METALES.getValue(acento).second else METALES.getValue(acento).first
    val sobreMetal = if (oscuro) SOBRE_METAL_OSCURO.getValue(acento) else sobreMetalClaro
    val ribete = ribeteDe(acento, oscuro)
    val contacto = if (oscuro) pozoContactoOscuro else pozoContactoClaro

    val base = if (oscuro) darkColorScheme() else lightColorScheme()

    // TODOS los roles, sin dejar ninguno al valor de fábrica.
    //
    // No es exceso de celo: un rol sin mapear no se nota al leer el código, se
    // nota en pantalla — el primer ejemplo que se ejecutó sacó una píldora AZUL
    // VIOLÁCEA en mitad de la laca, que era `secondaryContainer` viniendo del
    // esquema morado por defecto de Material 3. Cualquier componente que toque
    // un rol olvidado filtra ese morado.
    return base.copy(
        primary = metal,
        onPrimary = sobreMetal,
        primaryContainer = metal,
        onPrimaryContainer = sobreMetal,
        inversePrimary = metal,

        // Secundario y terciario NO son otros colores: este sistema tiene UN
        // acento (regla 4). Se mapean al canto y al metal para que un componente
        // que los pida siga dentro de la paleta en vez de inventarse un color.
        secondary = ribete,
        onSecondary = if (oscuro) textoEnBandejaOscura else bandejaClara,
        secondaryContainer = ribete,
        onSecondaryContainer = if (oscuro) textoEnBandejaOscura else bandejaClara,
        tertiary = metal,
        onTertiary = sobreMetal,
        tertiaryContainer = metal,
        onTertiaryContainer = sobreMetal,

        error = if (oscuro) rojoErrorOscuro else rojoErrorClaro,
        onError = if (oscuro) sobreErrorOscuro else sobreErrorClaro,
        errorContainer = if (oscuro) rojoErrorOscuro else rojoErrorClaro,
        onErrorContainer = if (oscuro) sobreErrorOscuro else sobreErrorClaro,

        // `surface` es el POZO, no la bandeja: así el texto por defecto de
        // cualquier componente cae legible sobre el fondo de contenido.
        surface = pozo,
        onSurface = textoEnPozo,
        onSurfaceVariant =
            if (oscuro) textoSecundarioEnPozoOscuro else textoSecundarioEnPozoClaro,
        surfaceVariant = pozo,
        background = pozo,
        onBackground = textoEnPozo,

        // Toda la escalera de contenedores apunta al mismo pozo: neutraliza el
        // "más elevado = más claro" para que ningún componente derive solo hacia
        // un tono más claro y vuelva a parecer una tarjeta flotante.
        surfaceContainerLowest = pozo,
        surfaceContainerLow = pozo,
        surfaceContainer = pozo,
        surfaceContainerHigh = pozo,
        surfaceContainerHighest = pozo,
        surfaceBright = pozo,
        surfaceDim = pozo,

        // El tinte de elevación de M3 es exactamente el efecto que este sistema
        // rechaza: aclarar una superficie para decir que está más alta.
        // Transparente lo apaga sin tener que anularlo componente a componente.
        surfaceTint = Color.Transparent,

        inverseSurface = if (oscuro) bandejaClara else bandejaOscura,
        inverseOnSurface = if (oscuro) textoEnBandejaClara else textoEnBandejaOscura,

        // La cresta es literalmente el contorno del pozo.
        outline = ribete,
        outlineVariant = contacto,
        scrim = bandejaOscura,
    )
}

// --- Contraste ---------------------------------------------------------------

/**
 * Luminancia relativa según WCAG 2.1.
 *
 * Escrita a mano y no tomada de la librería a propósito: es la fórmula que
 * sostiene todas las cifras de contraste del sistema, y tenerla a la vista es lo
 * que permite auditarla. La misma está escrita igual en los paquetes de Dart y
 * de Python.
 */
fun luminanciaRelativa(color: Color): Double {
    fun canal(v: Float): Double {
        val c = v.toDouble()
        return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
    }
    return 0.2126 * canal(color.red) + 0.7152 * canal(color.green) + 0.0722 * canal(color.blue)
}

/**
 * Razón de contraste WCAG entre dos colores opacos, de 1:1 a 21:1.
 *
 * Es pública porque el sistema se sostiene con estos números: quien añada un
 * metal o un tono nuevo tiene que poder comprobarlo con la misma fórmula, no con
 * una parecida.
 */
fun contraste(a: Color, b: Color): Double {
    val la = luminanciaRelativa(a)
    val lb = luminanciaRelativa(b)
    val claro = maxOf(la, lb)
    val fosco = minOf(la, lb)
    return (claro + 0.05) / (fosco + 0.05)
}
