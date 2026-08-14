package wulf.compose

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import wulf.compose.tokens.APAGADO_RIBETE_CLARO
import wulf.compose.tokens.APAGADO_RIBETE_OSCURO
import wulf.compose.tokens.METALES
import wulf.compose.tokens.SOBRE_METAL_OSCURO
import wulf.compose.tokens.WulfAcento
import wulf.compose.tokens.bandejaClara
import wulf.compose.tokens.bandejaOscura
import wulf.compose.tokens.pozoClaro
import wulf.compose.tokens.ribeteDe
import wulf.compose.tokens.pozoContactoClaro
import wulf.compose.tokens.pozoContactoOscuro
import wulf.compose.tokens.pozoOscuro
import wulf.compose.tokens.textoAtenuadoEnBandejaClara
import wulf.compose.tokens.textoAtenuadoEnBandejaOscura
import wulf.compose.tokens.textoEnBandejaClara
import wulf.compose.tokens.textoEnBandejaOscura
import wulf.compose.tokens.textoEnPozoClaro
import wulf.compose.tokens.textoEnPozoOscuro
import wulf.compose.tokens.textoSecundarioEnPozoClaro
import wulf.compose.tokens.textoSecundarioEnPozoOscuro
import java.io.File

/**
 * Las tres gemelas dicen los mismos números.
 *
 * Wulfenite existe en Dart, en Python y en Kotlin, y **tres** copias de la
 * paleta son la manera clásica de que un sistema de diseño se rompa: alguien
 * ajusta un color en un lado, los otros se quedan viejos, y meses después las
 * apps de la misma marca ya no se parecen. Con dos copias ya era un riesgo; con
 * tres, es cuestión de tiempo.
 *
 * Esta prueba lee el **archivo Dart** —que es el original— y compara. Si el
 * paquete Dart no está a mano, porque alguien se trajo solo este módulo, se
 * salta con un motivo claro: la comprobación tiene sentido en la máquina donde
 * se desarrollan los tres, no en la de quien usa uno.
 *
 * La gemela de Python ya cazó con esta misma técnica un fallo real: al portar, el
 * canto del pozo se apagaba hacia la bandeja en vez de hacia el pozo, y eso lo
 * dejaba por debajo de 3:1 contra la bandeja — justo lo que el canto existe para
 * cumplir.
 */
class ParidadConDartTest {

    private fun fuenteDart(): String? {
        val candidatos = listOfNotNull(
            System.getenv("WULFENITE_FLUTTER"),
            // Compose/wulf-compose -> Compose -> wulfenite_ui
            File("../../flutter").absolutePath,
        )
        for (ruta in candidatos) {
            val archivo = File(ruta, "lib/src/tokens/tema.dart")
            if (archivo.exists()) return archivo.readText()
        }
        return null
    }

    /** Todas las constantes `const _nombre = Color(0xFFRRGGBB);` del archivo. */
    private fun colores(fuente: String): Map<String, String> =
        Regex("""const\s+(_\w+)\s*=\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\)""")
            .findAll(fuente)
            .associate { it.groupValues[1] to "#${it.groupValues[2].uppercase()}" }

    private fun hex(color: Color): String =
        "#%02X%02X%02X".format(
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt(),
        )

    @Test
    fun `la paleta base dice lo mismo que en Dart`() {
        val fuente = fuenteDart()
        assumeTrue(
            "No se encontró el paquete Dart. Poné WULFENITE_FLUTTER apuntando a él " +
                "para comprobar que las paletas siguen diciendo lo mismo.",
            fuente != null,
        )
        val dart = colores(fuente!!)

        val equivalencias = mapOf(
            "_bandejaOscura" to bandejaOscura,
            "_pozoOscuro" to pozoOscuro,
            "_pozoContactoOscuro" to pozoContactoOscuro,
            "_textoEnPozoOscuro" to textoEnPozoOscuro,
            "_textoSecundarioEnPozoOscuro" to textoSecundarioEnPozoOscuro,
            "_textoEnBandejaOscura" to textoEnBandejaOscura,
            "_textoAtenuadoEnBandejaOscura" to textoAtenuadoEnBandejaOscura,
            "_bandejaClara" to bandejaClara,
            "_pozoClaro" to pozoClaro,
            "_pozoContactoClaro" to pozoContactoClaro,
            "_textoEnPozoClaro" to textoEnPozoClaro,
            "_textoSecundarioEnPozoClaro" to textoSecundarioEnPozoClaro,
            "_textoEnBandejaClara" to textoEnBandejaClara,
            "_textoAtenuadoEnBandejaClara" to textoAtenuadoEnBandejaClara,
        )

        equivalencias.forEach { (nombreDart, color) ->
            val esperado = dart[nombreDart]
                ?: error("`$nombreDart` ya no existe en el Dart: la paleta cambió de forma")
            assertEquals("$nombreDart se separó", esperado, hex(color))
        }
    }

    @Test
    fun `los metales dicen lo mismo que en Dart`() {
        val fuente = fuenteDart()
        assumeTrue("Sin el paquete Dart a mano", fuente != null)

        // `WulfAcento.oro: (claro: Color(0xFF7A4F0E), oscuro: Color(0xFFD9A54A))`
        val patron = Regex(
            """WulfAcento\.(\w+):\s*\(claro:\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\),\s*""" +
                """oscuro:\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\)\)""",
        )
        val enDart = patron.findAll(fuente!!).associate {
            it.groupValues[1].uppercase() to
                ("#${it.groupValues[2].uppercase()}" to "#${it.groupValues[3].uppercase()}")
        }

        assertEquals("cambió el número de metales", METALES.size, enDart.size)
        METALES.forEach { (acento, par) ->
            val (claroDart, oscuroDart) = enDart[acento.name]
                ?: error("el metal ${acento.name} no está en el Dart")
            assertEquals("${acento.name} claro", claroDart, hex(par.first))
            assertEquals("${acento.name} oscuro", oscuroDart, hex(par.second))
        }
    }

    @Test
    fun `el apagado del canto dice lo mismo que en Dart`() {
        val fuente = fuenteDart()
        assumeTrue("Sin el paquete Dart a mano", fuente != null)

        fun constante(nombre: String): Float =
            Regex("""const\s+$nombre\s*=\s*([\d.]+)""").find(fuente!!)
                ?.groupValues?.get(1)?.toFloat()
                ?: error("`$nombre` ya no está en el Dart")

        assertEquals(constante("_apagadoRibeteClaro"), APAGADO_RIBETE_CLARO, 0.0001f)
        assertEquals(constante("_apagadoRibeteOscuro"), APAGADO_RIBETE_OSCURO, 0.0001f)
    }

    /**
     * El canto DERIVADO, no solo las constantes de las que sale.
     *
     * Es la prueba que de verdad importa de este archivo, y la que habría cazado
     * sola el fallo que costó encontrar a mano: Compose interpola colores en
     * Oklab y Dart canal a canal en sRGB, así que la misma fórmula sobre las
     * mismas constantes daba un canto DISTINTO en cada gemela — con las
     * constantes coincidiendo perfectamente.
     *
     * Acá la mezcla se escribe otra vez, a mano y aparte, en vez de llamar a la
     * de la librería: una prueba que usa la misma función que verifica no puede
     * detectar que esa función esté mal.
     */
    @Test
    fun `el canto derivado coincide con el que produciria el Dart`() {
        val fuente = fuenteDart()
        assumeTrue("Sin el paquete Dart a mano", fuente != null)
        val dart = colores(fuente!!)

        fun mezclaSrgb(a: String, b: String, t: Float): String {
            fun canal(i: Int): Int {
                val ea = a.substring(1 + i * 2, 3 + i * 2).toInt(16)
                val eb = b.substring(1 + i * 2, 3 + i * 2).toInt(16)
                return Math.round(ea + (eb - ea) * t)
            }
            return "#%02X%02X%02X".format(canal(0), canal(1), canal(2))
        }

        val patron = Regex(
            """WulfAcento\.(\w+):\s*\(claro:\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\),\s*""" +
                """oscuro:\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\)\)""",
        )
        val metalesDart = patron.findAll(fuente).associate {
            it.groupValues[1].uppercase() to
                ("#${it.groupValues[2].uppercase()}" to "#${it.groupValues[3].uppercase()}")
        }

        for (acento in WulfAcento.entries) {
            val (metalClaro, metalOscuro) = metalesDart.getValue(acento.name)
            val esperadoClaro = mezclaSrgb(metalClaro, dart.getValue("_pozoClaro"), APAGADO_RIBETE_CLARO)
            val esperadoOscuro = mezclaSrgb(metalOscuro, dart.getValue("_pozoOscuro"), APAGADO_RIBETE_OSCURO)

            assertEquals("canto de $acento claro", esperadoClaro, hex(ribeteDe(acento, oscuro = false)))
            assertEquals("canto de $acento oscuro", esperadoOscuro, hex(ribeteDe(acento, oscuro = true)))
        }
    }

    @Test
    fun `hay un color sobre metal por cada metal`() {
        // No depende del Dart: es una comprobación de forma que evita el fallo
        // de añadir un metal y olvidar su color de texto encima, que revienta
        // recién al construir el tema con ese acento.
        assertEquals(WulfAcento.entries.size, METALES.size)
        assertEquals(WulfAcento.entries.size, SOBRE_METAL_OSCURO.size)
        WulfAcento.entries.forEach { acento ->
            assert(METALES.containsKey(acento)) { "falta el metal de $acento" }
            assert(SOBRE_METAL_OSCURO.containsKey(acento)) { "falta el sobre-metal de $acento" }
        }
    }
}
