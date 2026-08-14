package wulf.compose.tokens

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import wulf.compose.superficies.wulfIndication
import wulf.compose.superficies.WulfToqueReceta

/**
 * La laca vigente. Se lee con [WulfTheme.laca], no directamente.
 *
 * Con un valor por defecto en vez de reventar: una vista previa suelta o una
 * prueba de componente sin tema alrededor tiene que poder dibujarse.
 */
val LocalWulfLaca = staticCompositionLocalOf { lacaDe(WulfAcento.ORO, oscuro = false) }

/** Los efectos apagados. Por defecto ninguno: la experiencia completa. */
val LocalWulfEfectos = staticCompositionLocalOf { WulfEfectos() }

/** El acento vigente, por si la app quiere reflejarlo en su propia UI. */
val LocalWulfAcento = staticCompositionLocalOf { WulfAcento.ORO }

/**
 * Acceso a lo que Wulfenite añade sobre `MaterialTheme`.
 *
 * ```kotlin
 * val laca = WulfTheme.laca
 * Box(Modifier.background(laca.bandeja))
 * ```
 */
object WulfTheme {
    val laca: WulfLaca
        @Composable @ReadOnlyComposable get() = LocalWulfLaca.current

    val efectos: WulfEfectos
        @Composable @ReadOnlyComposable get() = LocalWulfEfectos.current

    val acento: WulfAcento
        @Composable @ReadOnlyComposable get() = LocalWulfAcento.current
}

/**
 * Escala tipográfica modular sobre la base de Material 3.
 *
 * Guía de uso:
 * - Hero de una pantalla (el total del día, por ejemplo) -> `displaySmall`
 * - Título de sección destacada -> `headlineSmall` (24)
 * - Subtítulo -> `titleLarge` (20)
 * - Texto de contenido normal -> `bodyLarge` (16, base)
 * - Texto secundario / metadatos -> `bodyMedium` (14)
 * - Etiqueta de botón -> `labelLarge` (14)
 * - Texto auxiliar chico (fecha, nota) -> `bodySmall` (12)
 */
private fun escalaTipografica(base: Typography): Typography = base.copy(
    displaySmall = base.displaySmall.copy(fontSize = 40.sp, fontWeight = FontWeight.W600),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.W600),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.W600),
    titleLarge = base.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.W500),
)

/**
 * El tema del sistema. Es el único punto de entrada.
 *
 * ```kotlin
 * WulfTheme(WulfAcento.JADE) {
 *     WulfBandeja { WulfPozo { Text("₡128 400") } }
 * }
 * ```
 *
 * **Sin color dinámico a propósito.** Android 12+ ofrece teñir la app desde el
 * fondo de pantalla, y Wulfenite tiene UN acento por diseño: el metal es la
 * única señal de acción y de selección en todo el sistema (`doc/reglas.md`,
 * regla 4). Dejar que el sistema lo reemplace por un color cualquiera rompería
 * las razones de contraste verificadas y con ellas la regla 1. Una app que
 * quiera color dinámico no quiere este sistema de diseño.
 *
 * @param acento el metal. Ortogonal al modo: el mismo en claro y en oscuro.
 * @param oscuro si no se dice, sigue al ajuste del sistema.
 * @param efectos qué adornos caros apagar en aparatos lentos.
 * @param toque la receta de la onda del toque.
 */
@Composable
fun WulfTheme(
    acento: WulfAcento = WulfAcento.ORO,
    oscuro: Boolean = isSystemInDarkTheme(),
    efectos: WulfEfectos = WulfEfectos(),
    toque: WulfToqueReceta = WulfToqueReceta(),
    content: @Composable () -> Unit,
) {
    val laca = lacaDe(acento, oscuro)
    val esquema = esquemaDe(acento, oscuro)

    CompositionLocalProvider(
        LocalWulfLaca provides laca,
        LocalWulfEfectos provides efectos,
        LocalWulfAcento provides acento,
        // La onda del toque se pinta con el material del elemento, nunca con
        // blanco. Reemplaza al `ripple` de fábrica en TODO el árbol, que es lo
        // que evita que un componente de Material meta su propio velo claro por
        // encima — el fallo que en Flutter costó anular `overlayColor` botón por
        // botón.
        LocalIndication provides wulfIndication(laca, oscuro, toque),
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = esquema.primary,
            backgroundColor = esquema.primary.copy(alpha = 0.35f),
        ),
    ) {
        MaterialTheme(
            colorScheme = esquema,
            typography = escalaTipografica(MaterialTheme.typography),
            content = content,
        )
    }
}

/** Estilo de texto por defecto sobre la bandeja (el cromo, no el contenido). */
@Composable
@ReadOnlyComposable
fun estiloEnBandeja(base: TextStyle = MaterialTheme.typography.bodyLarge): TextStyle =
    base.copy(color = LocalWulfLaca.current.textoEnBandeja)
