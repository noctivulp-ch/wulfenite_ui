"""La regla 12: el foco se ve, y se ve con el mismo canto.

Sin un foco visible una app no se puede usar sin ratón. En escritorio se nota
menos que en televisión, pero es el mismo agujero: se pulsa Tab, algo se mueve,
y la pantalla no dice qué.

Lo que se comprueba acá es lo que se puede comprobar sin abrir una ventana: que
los tres escalones existan, que se distingan, y que la hoja de estilo los emita
para todo lo que se puede activar.
"""

from __future__ import annotations

import re

import pytest

from wulf_qt import (
    LACAS,
    SEPARACION_FOCO,
    Acento,
    Canto,
    Modo,
    contraste,
    hoja_de_estilo,
    tono_enfocado,
)
from wulf_qt.tema import canto_del_pozo
from wulf_qt.tokens import METALES

MODOS = list(Modo)
ACENTOS = list(Acento)


class TestLosTresEscalones:
    def test_cada_uno_es_mas_grueso_que_el_anterior(self):
        # Si dos estados comparten grosor, el de más arriba deja de leerse.
        assert Canto.REPOSO < Canto.SELECCIONADO < Canto.ENFOCADO

    def test_el_de_foco_dobla_al_de_reposo(self):
        # El escalón que en la mano se distingue perfectamente, a distancia de
        # televisor no existe. Mismo motivo por el que el objetivo táctil es de
        # 48 y no de 32.
        assert Canto.ENFOCADO >= Canto.REPOSO * 2


class TestElTonoEnfocado:
    @pytest.mark.parametrize("modo", MODOS)
    def test_se_separa_del_reposo(self, modo):
        # Es la mitad de lo que distingue enfocado de seleccionado. Sin
        # separación los dos se ven iguales en cuanto coinciden, que es lo
        # normal recorriendo una lista.
        pozo = LACAS[modo].pozo
        assert tono_enfocado(pozo, modo) != pozo

    @pytest.mark.parametrize("modo", MODOS)
    def test_no_se_aleja_tanto_que_rompa_el_texto(self, modo):
        # La otra cara: aclarar el pozo gasta contraste contra el texto que
        # lleva encima. La señal de foco no puede costar la legibilidad de lo
        # que se está enfocando.
        laca = LACAS[modo]
        razon = contraste(laca.texto_en_pozo, tono_enfocado(laca.pozo, modo))
        assert razon >= 4.5, f"texto sobre pozo enfocado en {modo}: {razon:.2f}:1"

    @pytest.mark.parametrize("acento", ACENTOS)
    @pytest.mark.parametrize("modo", MODOS)
    def test_el_canto_de_foco_pasa_3_a_1_contra_el_pozo_enfocado(self, acento, modo):
        # El canto es la señal PRIMARIA (regla 1), así que cumple el piso de
        # elemento no textual contra el fondo que le toca — que con foco NO es
        # el pozo en reposo, sino el aclarado.
        fondo = tono_enfocado(LACAS[modo].pozo, modo)
        razon = contraste(METALES[acento][modo], fondo)
        assert razon >= 3.0, f"canto de foco {acento.name} {modo}: {razon:.2f}:1"


class TestLaHojaDeEstilo:
    @pytest.fixture
    def qss(self):
        return hoja_de_estilo(Acento.ORO, Modo.OSCURO)

    def test_todo_lo_activable_declara_su_foco(self, qss):
        # Un control sin regla de foco es un control que el teclado recorre en
        # silencio. Se listan por nombre para que agregar un control nuevo sin
        # su foco falle acá y no en el aparato de alguien.
        for control in ("QPushButton:focus", "QLineEdit:focus"):
            assert control in qss, f"falta la regla de foco de {control}"

    def test_el_pozo_declara_su_foco(self, qss):
        assert ":focus" in qss
        assert qss.count(":focus") >= 4

    def test_el_foco_usa_el_grosor_del_token(self, qss):
        # No un 3 escrito a mano: si el token cambia, la hoja tiene que cambiar
        # con él.
        anchos = {int(n) for n in re.findall(r"border: (\d)px solid", qss)}
        assert Canto.ENFOCADO in anchos
        assert Canto.SELECCIONADO in anchos
        assert Canto.REPOSO in anchos

    def test_el_primario_enfocado_lleva_canto_de_texto_y_no_de_metal(self, qss):
        # El metal ya ocupa toda la pieza: un canto de metal encima de metal no
        # se vería, y el botón primario quedaría sin señal de foco.
        bloque = qss.split('QPushButton[primario="true"]:focus')[1].split("}")[0]
        sobre_metal = "#241A0C"  # oro oscuro
        assert sobre_metal.lower() in bloque.lower()


def test_la_separacion_del_foco_es_la_misma_que_la_del_toque():
    # A propósito: el aparato dice "acá estás" con el mismo material se llegue
    # con el dedo, con el mando o con el teclado. Las tres gemelas usan 0.10.
    assert SEPARACION_FOCO == 0.10


def test_el_canto_de_reposo_sigue_siendo_el_del_pozo():
    # El foco sube un escalón sobre lo que ya había; no reemplaza la cresta.
    assert canto_del_pozo(Acento.ORO, Modo.OSCURO) != METALES[Acento.ORO][Modo.OSCURO]
