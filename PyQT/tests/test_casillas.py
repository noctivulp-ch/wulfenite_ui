"""Que una casilla y un radio NO se vean iguales.

Una casilla marca varias cosas; un radio elige una. Si las dos salen con la
misma forma, no hay forma de saber cuál de las dos conductas espera la pantalla
sin probarla — y probarla significa marcar algo que a lo mejor no se quería
marcar.

Esto se rompió una vez: las dos compartían una sola regla en la hoja de estilo,
así que el radio salía cuadrado con las esquinas redondeadas. Lo encontró
alguien usando la librería, no las pruebas, porque hasta acá no había ninguna
que mirase la FORMA.
"""

import re

import pytest
from PySide6.QtWidgets import QCheckBox, QRadioButton

from wulf_qt import Acento, Modo, PADDING_CELDA, RADIO_POZO, hoja_de_estilo


@pytest.fixture(scope="module")
def hoja() -> str:
    return hoja_de_estilo(Acento.ORO, Modo.OSCURO)


def _radio_de(hoja: str, selector: str) -> int:
    """El `border-radius` que la hoja le da a ese indicador."""
    # Anclado a principio de línea: `QRadioButton::indicator` también aparece
    # en mitad de la regla que las dos comparten, y esa no lleva la forma.
    bloque = re.search(
        r"(?m)^" + re.escape(selector) + r"\s*\{([^}]*)\}",
        hoja,
    )
    assert bloque, f"la hoja ya no trae la regla de {selector}"
    px = re.search(r"border-radius:\s*(\d+)px", bloque.group(1))
    assert px, f"{selector} quedó sin border-radius"
    return int(px.group(1))


def test_la_casilla_es_cuadrada(hoja: str) -> None:
    assert _radio_de(hoja, "QCheckBox::indicator") == RADIO_POZO


def test_el_radio_es_redondo(hoja: str) -> None:
    """La mitad del lado: es lo que convierte el cuadro en un círculo."""
    assert _radio_de(hoja, "QRadioButton::indicator") == PADDING_CELDA // 2


def test_no_tienen_la_misma_forma(hoja: str) -> None:
    assert _radio_de(hoja, "QCheckBox::indicator") != _radio_de(hoja, "QRadioButton::indicator")


def test_el_radio_marcado_lleva_punto_y_no_relleno(hoja: str) -> None:
    """Relleno entero se confunde con una casilla redondeada; el aro con punto no."""
    bloque = re.search(r"QRadioButton::indicator:checked\s*\{([^}]*)\}", hoja)
    assert bloque and "qradialgradient" in bloque.group(1)


def test_las_dos_formas_llegan_al_dibujo(app, hoja: str, request) -> None:
    """La hoja puede decir una cosa y Qt pintar otra: acá se mira lo pintado.

    Se cuenta metal en las ESQUINAS del indicador. La casilla las llena; el
    círculo las deja al aire.
    """
    # La hoja se pone en la APLICACIÓN, que es compartida por toda la suite: sin
    # devolverla, las pruebas que corren después miden tamaños con estilos
    # puestos por esta. Ya pasó — el rail perdía su objetivo táctil.
    anterior = app.styleSheet()
    request.addfinalizer(lambda: app.setStyleSheet(anterior))
    app.setStyleSheet(hoja)

    metal = (0xD9, 0xA5, 0x4A)

    def esquinas_en_metal(widget) -> int:
        widget.setChecked(True)
        widget.setAutoExclusive(False)
        widget.resize(30, 24)
        widget.ensurePolished()
        imagen = widget.grab().toImage()
        cuenta = 0
        for y in range(2, 18):
            for x in range(2, 18):
                # Fuera del círculo inscrito en el cuadro de 16 del indicador.
                if (x - 10) ** 2 + (y - 11) ** 2 <= 46:
                    continue
                color = imagen.pixelColor(x, y)
                if all(abs(v - m) < 20 for v, m in zip((color.red(), color.green(), color.blue()), metal)):
                    cuenta += 1
        return cuenta

    casilla = esquinas_en_metal(QCheckBox("x"))
    radio = esquinas_en_metal(QRadioButton("x"))
    assert casilla > radio * 3, f"casilla={casilla} radio={radio}: no se distinguen al pintarlas"
