package wulf.compose.texto

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import wulf.compose.tokens.wulfLerp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.layout
import wulf.compose.tokens.WulfEfectoPesado
import wulf.compose.tokens.WulfTheme
import kotlin.math.roundToInt

/**
 * Cuánta jerarquía tiene el glifo, y con ella cuánto contraste se puede gastar
 * en el efecto.
 *
 * El tallado se consigue acercando el glifo al color de su superficie, así que
 * es literalmente contraste gastado en estética. Gastarlo donde el tamaño lo
 * compensa (números grandes, iconos) y no donde hay que leer.
 */
enum class TalladoNivel {
    /** Texto de lectura. Se queda por debajo del techo de 4,5:1 (AA normal). */
    SUTIL,

    /**
     * Texto de 24 sp o más e iconos, donde WCAG permite 3:1 — texto grande por
     * 1.4.3, elementos no textuales por 1.4.11. Acá el tallado se ve de verdad.
     */
    MARCADO,
}

/**
 * Qué le pasa al glifo respecto de su superficie.
 *
 * El sistema usa [TALLADO] y nada más. Los otros dos existen porque **hacen
 * falta para elegir**: sin poder ver el realzado al lado, "hundido" es una
 * palabra y no una decisión — y más de una vez lo que alguien cree hundido es en
 * realidad lo contrario.
 */
enum class WulfTratamiento {
    /** Sin relieve. El glifo se apoya encima de la superficie. */
    PLANO,

    /**
     * Grabado: el borde de arriba queda en sombra y el de abajo atrapa luz, como
     * una letra incisa con luz desde arriba. Es el del sistema.
     */
    TALLADO,

    /** Lo contrario: el glifo sobresale de la superficie. */
    REALZADO,
}

/** Desplazamiento en px de las capas de luz y sombra. */
const val WULF_TALLADO_PROFUNDIDAD = 1.25f

private const val LUZ_INTENSIDAD = 0.70f
private const val SOMBRA_INTENSIDAD = 0.65f

/**
 * Cuánto se funde el glifo con su superficie, por modo y por nivel.
 *
 * Calibrado a mano en un banco de estilo. La fusión no puede ser un solo número:
 * medida en modo oscuro queda al borde de 4,5:1, y la paleta clara tiene mucho
 * menos margen de luminancia, donde la misma fusión cae a 2,83:1. De ahí los
 * techos medidos:
 *
 * |        | fusión máx 4,5:1 | fusión máx 3:1 |
 * |--------|------------------|----------------|
 * | oscuro | 0,48             | 0,62           |
 * | claro  | 0,32             | 0,47           |
 */
internal val FUSION: Map<TalladoNivel, Pair<Float, Float>> = mapOf(
    // (claro, oscuro)
    TalladoNivel.SUTIL to (0.28f to 0.42f),
    TalladoNivel.MARCADO to (0.46f to 0.62f),
)

/**
 * Color de glifo tallado: el color base acercado a su superficie.
 *
 * [sobre] es la superficie donde cae el glifo — el pozo o la bandeja. Pasarla
 * mal es el fallo que más se repite con esta pieza, así que es obligatoria y no
 * tiene valor por defecto.
 */
fun colorTallado(
    base: Color,
    sobre: Color,
    oscuro: Boolean,
    nivel: TalladoNivel = TalladoNivel.SUTIL,
): Color {
    val (claro, fosco) = FUSION.getValue(nivel)
    return wulfLerp(base, sobre, if (oscuro) fosco else claro)
}

/**
 * Aplica el relieve a cualquier hijo — texto, icono o vector propio.
 *
 * Pinta el mismo contenido tres veces: sombra desplazada hacia arriba, luz hacia
 * abajo, y el glifo encima. La pared de arriba de la incisión queda en sombra y
 * su fondo atrapa luz desde arriba; el realzado es exactamente lo mismo al
 * revés, que es la razón de que sea un signo y no otra implementación.
 *
 * @param sobre el color REAL de la superficie de detrás. De ahí salen la luz y
 *   la sombra, para que funcione igual sobre pozo o sobre bandeja.
 */
@Composable
fun WulfTallado(
    sobre: Color,
    modifier: Modifier = Modifier,
    profundidad: Float = WULF_TALLADO_PROFUNDIDAD,
    tratamiento: WulfTratamiento = WulfTratamiento.TALLADO,
    luz: Color? = null,
    sombra: Color? = null,
    content: @Composable () -> Unit,
) {
    val activo = WulfTheme.efectos.activo(WulfEfectoPesado.TALLADO)
    if (profundidad <= 0f || tratamiento == WulfTratamiento.PLANO || !activo) {
        Box(modifier) { content() }
        return
    }

    val tonoLuz = luz ?: wulfLerp(sobre, Color.White, LUZ_INTENSIDAD)
    val tonoSombra = sombra ?: wulfLerp(sobre, Color.Black, SOMBRA_INTENSIDAD)
    val signo = if (tratamiento == WulfTratamiento.TALLADO) 1f else -1f

    Box(modifier) {
        CapaDeRelieve(tonoSombra, -profundidad * signo, content)
        CapaDeRelieve(tonoLuz, profundidad * signo, content)
        content()
    }
}

/**
 * Una de las dos capas de decoración del relieve.
 *
 * `clearAndSetSemantics` las saca del árbol de accesibilidad. Sin eso cada texto
 * tallado aparece TRES veces: un lector de pantalla lee la misma frase tres
 * seguidas y cualquier navegación por semántica recibe la etiqueta triplicada.
 * El efecto visual no lo delata de ninguna manera — se descubrió listando los
 * elementos interactuables de una pantalla.
 */
@Composable
private fun CapaDeRelieve(color: Color, dy: Float, content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(color = color),
        androidx.compose.material3.LocalContentColor provides color,
    ) {
        Box(
            Modifier
                .clearAndSetSemantics {}
                .layout { medible, restricciones ->
                    val colocable = medible.measure(restricciones)
                    layout(colocable.width, colocable.height) {
                        colocable.place(IntOffset(0, dy.roundToInt()))
                    }
                },
        ) { content() }
    }
}

/**
 * Texto tallado: resuelve el color del glifo y le aplica el relieve en un solo
 * paso. Es el atajo para el caso que más se usa.
 */
@Composable
fun WulfTextoTallado(
    texto: String,
    sobre: Color,
    modifier: Modifier = Modifier,
    estilo: TextStyle = LocalTextStyle.current,
    nivel: TalladoNivel = TalladoNivel.SUTIL,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val oscuro = !MaterialTheme.colorScheme.isLight()
    val base = estilo.color.takeIf { it != Color.Unspecified }
        ?: androidx.compose.material3.LocalContentColor.current

    // Sin relieve, el glifo recupera su color PLENO. La fusión lo acerca a la
    // superficie a propósito —contraste gastado en el efecto—, y conservarla sin
    // el relieve que la justifica dejaría el texto lavado y peor de leer que
    // antes de apagar nada.
    if (!WulfTheme.efectos.activo(WulfEfectoPesado.TALLADO)) {
        Text(texto, modifier, style = estilo.copy(color = base), maxLines = maxLines, overflow = overflow)
        return
    }

    WulfTallado(sobre = sobre, modifier = modifier) {
        Text(
            text = texto,
            style = estilo.copy(color = colorTallado(base, sobre, oscuro, nivel)),
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}

/**
 * Si el esquema es el claro. Se deduce de la luminancia del fondo en vez de
 * arrastrar un booleano: así un `WulfTextoTallado` puesto dentro de un tema
 * anidado sigue acertando.
 */
internal fun androidx.compose.material3.ColorScheme.isLight(): Boolean =
    wulf.compose.tokens.luminanciaRelativa(surface) > 0.5
