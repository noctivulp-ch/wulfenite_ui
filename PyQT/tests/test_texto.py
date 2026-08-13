"""El relieve se pinta de verdad, y la etiqueta encoge.

El relieve es la pieza que **no** se puede hacer con una hoja de estilo: son tres
copias del glifo desplazadas y pintadas con `QPainter`. Por eso se comprueba
rasterizando y comparando píxeles — mirar la pantalla no vale, porque el efecto
es deliberadamente sutil ("más metal pulido que surco") y a simple vista los tres
tratamientos se parecen.
"""

from __future__ import annotations

from PySide6.QtGui import QImage

from wulf_qt import LACAS, EtiquetaAjustada, Modo, Tallado, color_tallado, contraste


def _rasterizar(etiqueta) -> QImage:
    fuente = etiqueta.font()
    fuente.setPointSize(28)
    etiqueta.setFont(fuente)
    etiqueta.resize(240, 60)
    imagen = QImage(240, 60, QImage.Format.Format_ARGB32)
    imagen.fill(0)
    etiqueta.render(imagen)
    return imagen


def _distintos(a: QImage, b: QImage) -> int:
    return sum(
        1
        for x in range(0, a.width(), 2)
        for y in range(0, a.height(), 2)
        if a.pixel(x, y) != b.pixel(x, y)
    )


def test_grabado_y_realzado_no_se_dibujan_igual(app) -> None:
    """Realzado es el mismo dibujo con el signo cambiado, no otro efecto."""
    pozo = LACAS[Modo.OSCURO].pozo
    grabado = _rasterizar(Tallado("3800", sobre=pozo))
    realzado = _rasterizar(Tallado("3800", sobre=pozo, realzado=True))

    assert _distintos(grabado, realzado) > 50


def test_con_profundidad_cero_no_hay_relieve(app) -> None:
    """Es el interruptor para aparatos lentos: se apaga sin tocar el árbol."""
    pozo = LACAS[Modo.OSCURO].pozo
    grabado = _rasterizar(Tallado("3800", sobre=pozo))
    plano = _rasterizar(Tallado("3800", sobre=pozo, profundidad=0))

    assert _distintos(grabado, plano) > 50


def test_el_interruptor_global_apaga_el_efecto(app) -> None:
    pozo = LACAS[Modo.OSCURO].pozo
    con = _rasterizar(Tallado("3800", sobre=pozo))
    try:
        Tallado.efecto_activo = False
        sin = _rasterizar(Tallado("3800", sobre=pozo))
    finally:
        Tallado.efecto_activo = True

    assert _distintos(con, sin) > 50


def test_el_color_tallado_gasta_contraste_pero_con_techo() -> None:
    """El grabado se consigue acercando el glifo a su superficie, así que es
    literalmente contraste gastado. El techo es lo que impide gastarlo donde hay
    que leer."""
    laca = LACAS[Modo.OSCURO]
    sutil = color_tallado(laca.texto_en_pozo, laca.pozo, nivel="sutil", modo="oscuro")
    marcado = color_tallado(laca.texto_en_pozo, laca.pozo, nivel="marcado", modo="oscuro")

    # Texto de lectura: sigue cumpliendo AA.
    assert contraste(sutil, laca.pozo) >= 4.5
    # Texto grande e iconos: WCAG permite 3:1, y ahí el tallado se ve de verdad.
    assert contraste(marcado, laca.pozo) >= 3.0
    assert contraste(marcado, laca.pozo) < contraste(sutil, laca.pozo)


def _con_tamano(texto: str, puntos: float, minimo: float = 9) -> EtiquetaAjustada:
    """Una etiqueta con un tamaño de letra explícito.

    Hace falta decirlo: la fuente por defecto de Qt en esta máquina ya es de 9 pt,
    que es el mínimo, así que sin esto no habría margen que recortar y la prueba
    pasaría por el motivo equivocado.
    """
    etiqueta = EtiquetaAjustada(texto, minimo=minimo)
    fuente = etiqueta.font()
    fuente.setPointSizeF(puntos)
    etiqueta.setFont(fuente)
    return etiqueta


def test_la_etiqueta_encoge_en_vez_de_partirse(app) -> None:
    """El caso que la trajo: una palabra sola más ancha que su celda. Qt la
    partiría por carácter — "Optimizaci / ón"."""
    etiqueta = _con_tamano("Optimización", 14)

    assert etiqueta.tamano_para(40) < 14


def test_lo_que_entra_no_encoge(app) -> None:
    """Solo reduce: lo que ya cabía se queda igual. Es la diferencia con meterlo
    todo en un ajuste automático, que encogería también lo que entraba bien."""
    etiqueta = _con_tamano("Tema", 14)

    assert etiqueta.tamano_para(400) == 14


def test_no_baja_de_un_tamano_legible(app) -> None:
    """Con un ancho imposible se planta en el mínimo. Si una etiqueta llega ahí,
    el arreglo es acortar la palabra, no seguir bajando."""
    etiqueta = _con_tamano("Electroencefalografista", 14, minimo=9)

    assert etiqueta.tamano_para(10) >= 9
