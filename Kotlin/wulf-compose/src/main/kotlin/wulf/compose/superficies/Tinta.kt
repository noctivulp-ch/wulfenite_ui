package wulf.compose.superficies

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.Modifier
import wulf.compose.tokens.wulfLerp
import kotlinx.coroutines.launch
import wulf.compose.tokens.WULF_FEEDBACK_TACTIL_MS
import wulf.compose.tokens.WulfLaca
import kotlin.math.hypot

/**
 * Cómo se dibuja el material del toque.
 *
 * `GRANO` fue la primera versión: puntos dispersos que crecen. Se descartó por
 * parecerse al ripple de Android Lollipop —esferas dispersándose— que no es el
 * lenguaje de una pieza de laca. Se conserva como opción porque **hace falta
 * para elegir**: sin poder ver la alternativa al lado, "se parece al de
 * Android" es una opinión y no una comparación.
 */
enum class WulfToqueTextura {
    /** Puntos dispersos, como papel rugoso. */
    GRANO,

    /**
     * Tinta que se absorbe: relleno continuo que se disuelve hacia el borde.
     * Es lo que se parece a laca — la superficie se humedece, no se salpica.
     */
    MANCHA,

    /** Vetas radiales tenues, como el pincelado del barniz. */
    VETA,
}

/**
 * Los parámetros ajustables del toque, juntos.
 *
 * Existen como objeto —y no como constantes sueltas— para que un laboratorio de
 * estilo pueda probar variantes en vivo sin tocar nada de producción.
 */
data class WulfToqueReceta(
    /** Con qué se dibuja el material del toque. */
    val textura: WulfToqueTextura = WulfToqueTextura.MANCHA,
    /**
     * Cuánto tarda la onda en cubrir el elemento, en milisegundos. El
     * desvanecido dura lo mismo y sostiene la opacidad durante su primer tercio.
     */
    val duracionMs: Int = WULF_FEEDBACK_TACTIL_MS,
    /** Opacidad máxima del relleno. */
    val opacidad: Float = 0.55f,
    /**
     * Qué parte del radio ocupa el degradado del borde. 0 deja un canto duro;
     * 1 disuelve la mancha entera.
     */
    val fusion: Float = 1f,
    /**
     * Cuánta cresta lleva el frente de la onda. Es una diferencia de tono SUTIL,
     * no un anillo dibujado: el borde tiene que dar impresión de fusión con el
     * material, no de contorno.
     */
    val borde: Float = 0.86f,
    /**
     * Cuánto se aleja el tono del toque del color de reposo del elemento. 0 lo
     * deja igual (invisible); 1 lo lleva al blanco puro sobre material oscuro y
     * al negro puro sobre material claro.
     */
    val separacion: Float = 0.10f,
)

/**
 * Tono del toque: el material del pozo separado de su color de reposo.
 *
 * No sirve un tono fijo ni el de contacto —que es la SOMBRA del pozo, o sea más
 * oscura que él: usarla restaba luminosidad y el toque quedaba casi invisible—.
 * Lo que hace falta es alejarse del reposo en la dirección que contrasta:
 * aclarar sobre material oscuro, oscurecer sobre material claro.
 */
internal fun tonoDeToque(pozo: Color, oscuro: Boolean, separacion: Float): Color =
    wulfLerp(pozo, if (oscuro) Color.White else Color.Black, separacion.coerceIn(0f, 1f))

/**
 * La onda del toque de Wulfenite, para poner en `LocalIndication`.
 *
 * Nace en el punto exacto de la presión, crece hasta cubrir el elemento y
 * desaparece. La duración es fija para cualquier toque: es una señal de
 * contacto, no un indicador de progreso.
 *
 * Pinta con el material del elemento, nunca con blanco: el relleno es el tono
 * del pozo separado de su reposo y el frente lleva un rastro de la cresta.
 */
fun wulfIndication(
    laca: WulfLaca,
    oscuro: Boolean,
    receta: WulfToqueReceta = WulfToqueReceta(),
): IndicationNodeFactory = WulfTintaFactory(
    relleno = tonoDeToque(laca.pozo, oscuro, receta.separacion),
    cresta = laca.paredCresta,
    receta = receta,
)

private data class WulfTintaFactory(
    val relleno: Color,
    val cresta: Color,
    val receta: WulfToqueReceta,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        WulfTintaNode(interactionSource, relleno, cresta, receta)
}

/** `easeOut` y `easeIn`, con las mismas curvas que usa la versión de Flutter. */
private val EASE_OUT: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)
private val EASE_IN: Easing = CubicBezierEasing(0.42f, 0f, 1f, 1f)

private class WulfTintaNode(
    private val interactionSource: InteractionSource,
    private val relleno: Color,
    private val cresta: Color,
    private val receta: WulfToqueReceta,
) : Modifier.Node(), DrawModifierNode {

    private var origen: Offset? = null
    private val avance = Animatable(0f)
    private val alfa = Animatable(0f)

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaccion ->
                when (interaccion) {
                    is PressInteraction.Press -> {
                        origen = interaccion.pressPosition
                        avance.snapTo(0f)
                        alfa.snapTo(1f)
                        launch {
                            avance.animateTo(1f, tween(receta.duracionMs, easing = EASE_OUT))
                        }
                    }

                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        // El desvanecido sostiene la opacidad durante su primer
                        // tercio: sin esa meseta el toque se apagaba antes de
                        // que la pantalla reaccionara y nunca se veía lleno.
                        alfa.animateTo(
                            0f,
                            tween(
                                durationMillis = receta.duracionMs,
                                delayMillis = receta.duracionMs / 3,
                                easing = EASE_IN,
                            ),
                        )
                        origen = null
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val centro = origen ?: return
        val opacidad = receta.opacidad * alfa.value
        if (opacidad <= 0f) return

        val radio = radioObjetivo(size, centro) * avance.value
        if (radio <= 0f) return

        when (receta.textura) {
            WulfToqueTextura.MANCHA -> pintarMancha(centro, radio, opacidad)
            WulfToqueTextura.VETA -> pintarVeta(centro, radio, opacidad)
            WulfToqueTextura.GRANO -> pintarGrano(centro, radio, opacidad)
        }
        pintarFrente(centro, radio, opacidad)
    }

    /** Lo que hay que cubrir: la esquina más lejana al punto de la presión. */
    private fun radioObjetivo(tamano: Size, centro: Offset): Float {
        val dx = maxOf(centro.x, tamano.width - centro.x)
        val dy = maxOf(centro.y, tamano.height - centro.y)
        return hypot(dx, dy)
    }

    private fun DrawScope.pintarMancha(
        centro: Offset,
        radio: Float,
        opacidad: Float,
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                // El degradado empieza a disolverse donde diga `fusion`: en 1
                // la mancha entera se difumina, en 0 queda un canto duro.
                colorStops = arrayOf(
                    0f to relleno.copy(alpha = opacidad),
                    (1f - receta.fusion).coerceIn(0f, 1f) to relleno.copy(alpha = opacidad),
                    1f to relleno.copy(alpha = 0f),
                ),
                center = centro,
                radius = radio,
            ),
            radius = radio,
            center = centro,
        )
    }

    /**
     * El frente NO es un anillo dibujado: es un rastro de cresta que aparece
     * solo en la última franja del radio y se disuelve hacia dentro. Un trazo de
     * 1 dp opaco leía como contorno, y lo que se busca es fusión con el
     * material.
     */
    private fun DrawScope.pintarFrente(
        centro: Offset,
        radio: Float,
        opacidad: Float,
    ) {
        if (receta.borde <= 0f) return
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.82f to cresta.copy(alpha = 0f),
                    1f to cresta.copy(alpha = opacidad * receta.borde),
                ),
                center = centro,
                radius = radio,
            ),
            radius = radio,
            center = centro,
        )
    }

    private fun DrawScope.pintarVeta(
        centro: Offset,
        radio: Float,
        opacidad: Float,
    ) {
        pintarMancha(centro, radio, opacidad * 0.7f)
        val ancho = maxOf(1f, radio * 0.05f)
        VETAS.forEach { angulo ->
            val direccion = Offset(kotlin.math.cos(angulo), kotlin.math.sin(angulo))
            drawLine(
                color = relleno.copy(alpha = opacidad * 0.35f),
                start = centro + direccion * (radio * 0.15f),
                end = centro + direccion * radio,
                strokeWidth = ancho,
            )
        }
    }

    private fun DrawScope.pintarGrano(
        centro: Offset,
        radio: Float,
        opacidad: Float,
    ) {
        drawCircle(
            color = relleno.copy(alpha = opacidad * 0.35f),
            radius = radio,
            center = centro,
        )
        GRANOS.forEach { (dx, dy) ->
            drawCircle(
                color = relleno.copy(alpha = opacidad),
                radius = maxOf(1f, radio * 0.045f),
                center = centro + Offset(dx * radio, dy * radio),
            )
        }
    }

    private companion object {
        /** Ángulos fijos, no aleatorios: la misma pieza tocada dos veces se ve igual. */
        val VETAS = FloatArray(9) { it * (2f * Math.PI.toFloat() / 9f) }

        /**
         * Posiciones normalizadas de los puntos, fijas por el mismo motivo.
         * Salen de una espiral áurea para que no se lean como una retícula.
         */
        val GRANOS: List<Pair<Float, Float>> = List(22) { i ->
            val t = (i + 0.5f) / 22f
            val r = kotlin.math.sqrt(t)
            val a = i * 2.39996f
            (r * kotlin.math.cos(a)) to (r * kotlin.math.sin(a))
        }
    }
}
