"""La retícula reparte en seis columnas y recoloca al cambiar de tamaño."""

from __future__ import annotations

import pytest
from PySide6.QtWidgets import QLabel

from wulf_qt import COLUMNAS, Ancho, Bandeja, Celda, Cuadricula, Pozo


def test_los_tramos_de_ancho() -> None:
    assert Ancho.de(360) is Ancho.COMPACTO
    assert Ancho.de(599) is Ancho.COMPACTO
    assert Ancho.de(600) is Ancho.MEDIO
    assert Ancho.de(1023) is Ancho.MEDIO
    assert Ancho.de(1024) is Ancho.AMPLIO


def test_una_celda_no_puede_pasarse_de_columnas() -> None:
    """Vale más reventar al construirla que dibujar una fila rota."""
    with pytest.raises(ValueError):
        Celda(QLabel(), compacto=7, medio=6, amplio=6)
    with pytest.raises(ValueError):
        Celda(QLabel(), compacto=0, medio=6, amplio=6)


def test_reparte_en_filas_de_seis(app) -> None:
    etiquetas = [QLabel(f"{i}") for i in range(4)]
    rejilla = Cuadricula()
    # Tres de a dos entran en una fila; la cuarta abre otra.
    rejilla.poner([Celda(e, compacto=2, medio=2, amplio=2) for e in etiquetas])
    rejilla.resize(1200, 400)
    rejilla.show()
    rejilla.layout().activate()

    filas = {e.y() for e in etiquetas}
    assert len(filas) == 2, "las tres primeras van juntas y la cuarta abre fila"


def test_la_misma_composicion_cambia_de_forma(app) -> None:
    """Lo que cambia entre tamaños no es el número de columnas: es cuántas ocupa
    cada pieza. Es lo que hace que sea la misma app y no otra."""
    a, b = QLabel("a"), QLabel("b")
    rejilla = Cuadricula()
    rejilla.poner(
        [
            Celda(a, compacto=6, medio=3, amplio=3),
            Celda(b, compacto=6, medio=3, amplio=3),
        ]
    )
    rejilla.show()

    rejilla.resize(360, 400)
    rejilla.layout().activate()
    assert a.y() != b.y(), "en teléfono se apilan"

    rejilla.resize(1200, 400)
    rejilla.layout().activate()
    assert a.y() == b.y(), "en escritorio van lado a lado"


def test_el_pozo_marca_su_seleccion(app) -> None:
    pozo = Pozo(QLabel("hola"))
    assert pozo.property("seleccionado") == "false"

    pozo.marcar(True)
    assert pozo.property("seleccionado") == "true"


def test_la_bandeja_deja_su_labio(app) -> None:
    from wulf_qt import RIM

    bandeja = Bandeja()
    margenes = bandeja.layout_interno().contentsMargins()
    assert margenes.left() == RIM

    sin_labio = Bandeja(rim=False)
    assert sin_labio.layout_interno().contentsMargins().left() == 0


def test_las_columnas_son_seis() -> None:
    assert COLUMNAS == 6
