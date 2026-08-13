"""Los números de contraste del sistema, recalculados.

Blinda la legibilidad contra una edición futura que "mejore" un color y la
degrade sin que nadie lo note. Es la misma prueba que tiene el paquete Dart, con
la misma fórmula: si las dos pasan, las dos paletas son legibles.
"""

from __future__ import annotations

import pytest

from wulf_qt import (
    LACAS,
    METALES,
    SOBRE_METAL,
    Acento,
    Modo,
    canto_del_pozo,
    contraste,
)

MODOS = list(Modo)
ACENTOS = list(Acento)


@pytest.mark.parametrize("modo", MODOS)
def test_texto_cumple_aa(modo: Modo) -> None:
    """Todo texto del sistema llega a 4,5:1 — WCAG AA para texto normal."""
    laca = LACAS[modo]
    pares = {
        "texto en pozo": (laca.texto_en_pozo, laca.pozo),
        "texto secundario en pozo": (laca.texto_secundario_en_pozo, laca.pozo),
        "texto en bandeja": (laca.texto_en_bandeja, laca.bandeja),
        "texto atenuado en bandeja": (laca.texto_atenuado_en_bandeja, laca.bandeja),
        "texto sobre error": (laca.sobre_error, laca.error),
    }
    for nombre, (frente, fondo) in pares.items():
        ratio = contraste(frente, fondo)
        assert ratio >= 4.5, f"{nombre} en {modo.value}: {ratio:.2f}:1"


@pytest.mark.parametrize("acento", ACENTOS)
@pytest.mark.parametrize("modo", MODOS)
def test_texto_sobre_metal_cumple_aa(acento: Acento, modo: Modo) -> None:
    ratio = contraste(SOBRE_METAL[acento][modo], METALES[acento][modo])
    assert ratio >= 4.5, f"sobre {acento.value} en {modo.value}: {ratio:.2f}:1"


@pytest.mark.parametrize("acento", ACENTOS)
@pytest.mark.parametrize("modo", MODOS)
def test_el_enlace_se_lee(acento: Acento, modo: Modo) -> None:
    """Un enlace va en el color del acento sobre el pozo.

    Es lo primero que se rompe al añadir un metal: se elige por cómo se ve de
    canto o de relleno de botón, no por si se lee como texto.
    """
    ratio = contraste(METALES[acento][modo], LACAS[modo].pozo)
    assert ratio >= 4.5, f"enlace {acento.value} en {modo.value}: {ratio:.2f}:1"


@pytest.mark.parametrize("acento", ACENTOS)
@pytest.mark.parametrize("modo", MODOS)
def test_el_canto_del_pozo_llega_a_3(acento: Acento, modo: Modo) -> None:
    """El canto contra la bandeja llega a 3:1 — WCAG 1.4.11, no textual.

    Es la señal PRIMARIA de que hay un compartimento, y en modo oscuro es la
    única: entre dos superficies oscuras el salto de valor no puede llegar a 3:1
    porque el rango de luminancia está comprimido. Si esto cae, el sistema deja
    de leerse como tallado y pasa a ser rectángulos.
    """
    ratio = contraste(canto_del_pozo(acento, modo), LACAS[modo].bandeja)
    assert ratio >= 3.0, f"canto {acento.value} en {modo.value}: {ratio:.2f}:1"
