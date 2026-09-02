"""Los iconos: que carguen, que se tiñan y que fallen claro.

Lo que se vigila es lo que rompe en silencio: un teñido que pinta un cuadrado en
vez del trazo, un tamaño que se escala y sale borroso, y un nombre mal escrito
que devolvería un icono vacío sin decir nada.
"""

from __future__ import annotations

import pathlib

import pytest

from wulf_qt import GAP_ICONO, IconoTamano, disponibles, icono, pixmap


def test_el_juego_completo_esta_disponible(app):
    nombres = disponibles()

    assert len(nombres) >= 80
    for imprescindible in ("buscar", "correcto", "reproducir", "triangulo-derecha-lleno"):
        assert imprescindible in nombres


def test_un_nombre_que_no_existe_falla_claro(app):
    # Sin esto devolvería un icono vacío y la pantalla saldría con un hueco, sin
    # ningún error que explique por qué.
    with pytest.raises(KeyError, match="no hay un icono"):
        icono("no-existe", "#000000")


def test_los_dibujos_van_incrustados_y_no_se_leen_del_disco(app):
    # Es lo que hace que sobreviva a Nuitka en onefile, a PyInstaller y a un
    # wheel instalado: sin acceso al disco no hay ruta que apunte fuera del
    # bundle. La primera versión leía de una carpeta hermana y el ejecutable
    # reventaba al construir el primer botón.
    from wulf_qt import iconos as modulo

    fuente = pathlib.Path(modulo.__file__).read_text(encoding="utf-8")
    assert "__file__" not in fuente, "vuelve a resolver rutas: se romperá al empaquetar"
    assert "parents[" not in fuente
    assert "glob(" not in fuente


class TestElTamano:
    @pytest.mark.parametrize(
        "tamano", [IconoTamano.CHICO, IconoTamano.NORMAL, IconoTamano.GRANDE, IconoTamano.TELEVISION]
    )
    def test_se_rasteriza_al_tamaño_pedido(self, app, tamano):
        # Al tamaño final y no escalando después: escalar un trazo de 1,75 lo
        # deja borroso, que es lo que un juego de línea no puede permitirse.
        px = pixmap("buscar", "#D9A54A", tamano)

        assert px.width() == tamano
        assert px.height() == tamano

    def test_los_tamaños_van_de_menor_a_mayor(self, app):
        assert IconoTamano.CHICO < IconoTamano.NORMAL < IconoTamano.GRANDE < IconoTamano.TELEVISION

    def test_ninguno_es_el_objetivo_tactil(self, app):
        # El dibujo es lo que se ve, no lo que se toca: un icono de 24 dentro de
        # un botón sigue necesitando sus 48 alrededor.
        from wulf_qt import OBJETIVO_TACTIL_MIN

        assert IconoTamano.GRANDE < OBJETIVO_TACTIL_MIN


class TestElTeñido:
    def _colores(self, px):
        imagen = px.toImage()
        vistos = set()
        for y in range(imagen.height()):
            for x in range(imagen.width()):
                c = imagen.pixelColor(x, y)
                if c.alpha() > 200:
                    vistos.add((c.red(), c.green(), c.blue()))
        return vistos

    def test_el_trazo_toma_el_color_pedido(self, app):
        px = pixmap("correcto", "#D9A54A", IconoTamano.GRANDE)

        colores = self._colores(px)
        assert colores, "no se pintó nada"
        assert all(abs(r - 0xD9) < 6 and abs(g - 0xA5) < 6 for r, g, b in colores)

    def test_el_mismo_archivo_sirve_para_los_dos_estados(self, app):
        # Es la razón de que los SVG sean de un solo color: dos copias acabarían
        # desincronizadas.
        apagado = self._colores(pixmap("reportes" if "reportes" in disponibles() else "lista",
                                       "#A99C93", IconoTamano.NORMAL))
        encendido = self._colores(pixmap("reportes" if "reportes" in disponibles() else "lista",
                                         "#D9A54A", IconoTamano.NORMAL))

        assert apagado and encendido
        assert apagado != encendido

    def test_no_pinta_un_cuadrado_solido(self, app):
        # El fallo del modo de composición equivocado: con `SourceOver` sale un
        # bloque del color en vez del trazo teñido.
        px = pixmap("correcto", "#D9A54A", IconoTamano.GRANDE)
        imagen = px.toImage()

        opacos = sum(
            1
            for y in range(imagen.height())
            for x in range(imagen.width())
            if imagen.pixelColor(x, y).alpha() > 200
        )
        total = imagen.width() * imagen.height()
        assert opacos < total * 0.5, "parece un cuadrado relleno, no un trazo"


def test_el_aire_junto_al_texto_es_menor_que_la_unidad(app):
    # Con la unidad entera el par se lee como dos cosas sueltas en vez de como
    # una etiqueta con su icono.
    from wulf_qt import UNIDAD

    assert 0 < GAP_ICONO < UNIDAD
