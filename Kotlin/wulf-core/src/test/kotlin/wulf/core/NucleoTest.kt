package wulf.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El núcleo, comprobado sin dibujar nada.
 *
 * Que estas pruebas no necesiten Android ni Compose no es una comodidad: es la
 * demostración de que el sistema no depende de dónde se programe. Si alguna
 * necesitara una pantalla, es que algo se coló donde no debía.
 */
class NucleoTest {

    private val modos = listOf(false, true)

    private fun exigir(razon: Double, piso: Double, que: String) =
        assertTrue("$que da %.2f:1 y el piso es %.1f:1".format(razon, piso), razon >= piso)

    // ── El cuarto tamaño ────────────────────────────────────────────────────

    @Test
    fun `el televisor no sale del ancho, sale de la plataforma`() {
        // Un televisor de 1080p reporta unos 960 dp: por ancho caería en MEDIO,
        // que es el reparto de una tablet que se mira a cuarenta centímetros.
        assertEquals(WulfAncho.MEDIO, WulfAncho.deAncho(960))
        assertEquals(WulfAncho.TELEVISION, WulfAncho.de(960, esTelevision = true))
    }

    @Test
    fun `deAncho nunca devuelve television`() {
        // Si lo devolviera, un monitor de escritorio ancho se compondría como una
        // tele, que es el error contrario y también inservible.
        for (ancho in listOf(0, 360, 599, 600, 1023, 1024, 1920, 3840)) {
            assertTrue(WulfAncho.deAncho(ancho) != WulfAncho.TELEVISION)
        }
    }

    @Test
    fun `la television gana sobre cualquier ancho`() {
        for (ancho in listOf(360, 800, 1920)) {
            assertEquals(WulfAncho.TELEVISION, WulfAncho.de(ancho, esTelevision = true))
        }
    }

    @Test
    fun `los cortes de ancho caen donde dicen`() {
        assertEquals(WulfAncho.COMPACTO, WulfAncho.deAncho(360))
        assertEquals(WulfAncho.COMPACTO, WulfAncho.deAncho(599))
        assertEquals(WulfAncho.MEDIO, WulfAncho.deAncho(600))
        assertEquals(WulfAncho.MEDIO, WulfAncho.deAncho(1023))
        assertEquals(WulfAncho.AMPLIO, WulfAncho.deAncho(1024))
    }

    @Test
    fun `en television el labio es el doble y el foco engorda`() {
        // Las dos cosas por lo mismo: se mira de lejos. El labio, porque la tele
        // recorta los bordes; el canto, porque a tres metros 1 dp no existe.
        assertEquals(WulfEspaciado.RIM * 2, WulfEspaciado.rimDe(WulfAncho.TELEVISION), 0.01f)
        assertTrue(WulfCanto.enfocadoDe(WulfAncho.TELEVISION) > WulfCanto.enfocadoDe(WulfAncho.AMPLIO))
    }

    @Test
    fun `los demas tamaños conservan el labio y el canto de siempre`() {
        for (a in listOf(WulfAncho.COMPACTO, WulfAncho.MEDIO, WulfAncho.AMPLIO)) {
            assertEquals(WulfEspaciado.RIM, WulfEspaciado.rimDe(a), 0.01f)
            assertEquals(WulfCanto.ENFOCADO, WulfCanto.enfocadoDe(a), 0.01f)
        }
    }

    @Test
    fun `el labio de television cubre el 5 por ciento de un 1080p`() {
        // La guía de Android TV pide 5 % por lado. A 1080p con densidad 2x, 48 dp
        // son 96 px de 1920 — exactamente el 5 %.
        assertEquals(0.05, (WulfEspaciado.RIM_TELEVISION * 2 / 1920.0), 0.001)
        assertTrue(WulfEspaciado.RIM * 2 / 1920.0 < 0.05)
    }

    // ── La retícula ─────────────────────────────────────────────────────────

    @Test
    fun `una celda que no entra abre la siguiente fila, nunca se parte`() {
        assertEquals(listOf(listOf(3, 3), listOf(3)), repartirEnFilas(listOf(3, 3, 3)))
    }

    @Test
    fun `ninguna fila pasa de las columnas disponibles y no se pierde nada`() {
        val spans = listOf(4, 3, 2, 2, 1, 6, 5, 1, 3)
        repartirEnFilas(spans).forEach { assertTrue(it.sum() <= WULF_COLUMNAS) }
        assertEquals(spans, repartirEnFilas(spans).flatten())
    }

    @Test
    fun `porFila da el televisor por separado`() {
        // Por defecto la tele hereda el reparto de escritorio, pero se puede
        // pedir uno propio: de lejos suelen caber menos piezas.
        assertEquals(6, wulfPorFila(WulfAncho.TELEVISION, compacto = 3, medio = 2, amplio = 1))
        assertEquals(2, wulfPorFila(WulfAncho.TELEVISION, compacto = 3, medio = 2, amplio = 1, television = 3))
    }

    // ── La paleta ───────────────────────────────────────────────────────────

    @Test
    fun `la formula WCAG da 21 a 1 entre blanco y negro`() {
        assertEquals(21.0, contraste(0xFFFFFF, 0x000000), 0.1)
        assertEquals(1.0, contraste(0xFFFFFF, 0xFFFFFF), 0.001)
    }

    @Test
    fun `la cresta llega a 3 a 1 contra la bandeja en los 3 metales y los 2 modos`() {
        for (acento in WulfAcento.entries) for (oscuro in modos) {
            val laca = lacaDe(acento, oscuro)
            exigir(contraste(laca.paredCresta, laca.bandeja), 3.0, "cresta de $acento")
        }
    }

    @Test
    fun `el canto se apaga hacia el pozo, no hacia la bandeja`() {
        for (acento in WulfAcento.entries) for (oscuro in modos) {
            val laca = lacaDe(acento, oscuro)
            assertTrue(contraste(laca.paredCresta, laca.pozo) < contraste(laca.paredCresta, laca.bandeja))
        }
    }

    @Test
    fun `el metal como texto sobre el pozo pasa AA`() {
        for (acento in WulfAcento.entries) for (oscuro in modos) {
            val metal = if (oscuro) METALES.getValue(acento).second else METALES.getValue(acento).first
            val pozo = if (oscuro) POZO_OSCURO else POZO_CLARO
            exigir(contraste(metal, pozo), 4.5, "metal $acento sobre pozo")
        }
    }

    @Test
    fun `la mezcla es canal a canal en sRGB`() {
        // Si alguien la cambiara por la de una plataforma, este número se movería
        // y con él el canto y el tallado de las tres gemelas.
        assertEquals(0x808080, wulfLerp(0x000000, 0xFFFFFF, 0.5f))
        assertEquals(0xFFFFFF, wulfLerp(0x000000, 0xFFFFFF, 1f))
    }

    // ── El tallado ──────────────────────────────────────────────────────────

    @Test
    fun `el tallado respeta sus techos en los dos modos`() {
        for (oscuro in modos) {
            val pozo = if (oscuro) POZO_OSCURO else POZO_CLARO
            val texto = if (oscuro) TEXTO_EN_POZO_OSCURO else TEXTO_EN_POZO_CLARO
            exigir(contraste(colorTallado(texto, pozo, oscuro, TalladoNivel.SUTIL), pozo), 4.5, "sutil")
            exigir(contraste(colorTallado(texto, pozo, oscuro, TalladoNivel.MARCADO), pozo), 3.0, "marcado")
        }
    }

    // ── La barra ────────────────────────────────────────────────────────────

    @Test
    fun `buscar no se sale del archivo`() {
        assertEquals(1f, moverPasos(0.95f, 5), 0.0001f)
        assertEquals(0f, moverPasos(0.05f, -5), 0.0001f)
        assertEquals(0.55f, moverPasos(0.5f, 1), 0.0001f)
    }

    @Test
    fun `recorrer la barra cuesta un numero razonable de pulsaciones`() {
        assertTrue((1f / WULF_PASO_DESLIZADOR).toInt() in 10..25)
    }

    @Test
    fun `la barra se dibuja fina pero se toca ancha`() {
        assertTrue(WULF_ALTO_BARRA < WULF_OBJETIVO_TACTIL_MIN)
    }
}
