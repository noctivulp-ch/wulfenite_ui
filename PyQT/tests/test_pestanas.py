"""La línea que Qt pinta bajo la barra de pestañas.

Es un color del sistema colándose en una paleta cerrada, y no hay regla de hoja
de estilo que lo alcance: `drawBase` es una propiedad del `QTabBar`. La prueba
existe porque el fallo **no da error** — solo se ve mal.
"""

from __future__ import annotations

from PySide6.QtWidgets import QTabWidget, QWidget

from wulf_qt import Acento, Modo, hoja_de_estilo, quitar_linea_de_pestanas


def test_el_valor_de_fabrica_depende_de_como_se_arme_la_barra(app):
    # Es la razón de que el ayudante exista: el mismo código se ve distinto según
    # por dónde se creó la barra. Medido en PySide6 6.11.1 con Fusion.
    from PySide6.QtWidgets import QTabBar

    assert QTabBar().drawBase() is True, "una barra suelta SÍ pinta la línea"
    assert QTabWidget().tabBar().drawBase() is False


def test_el_ayudante_la_apaga_venga_de_donde_venga(app):
    from PySide6.QtWidgets import QTabBar

    tabs = quitar_linea_de_pestanas(QTabWidget())
    assert tabs.tabBar().drawBase() is False

    # Y una barra suelta, que es la que de verdad venía encendida. Acepta las dos
    # formas porque quien llama no tendría por qué saber cuál tiene en la mano.
    assert quitar_linea_de_pestanas(QTabBar()).drawBase() is False


def test_devuelve_el_mismo_widget_para_poder_encadenar(app):
    tabs = QTabWidget()

    assert quitar_linea_de_pestanas(tabs) is tabs


def test_no_revienta_con_algo_que_no_tiene_barra(app):
    # Se le puede pasar cualquier widget sin que explote: el arreglo es opcional
    # por naturaleza y no debería obligar a comprobar el tipo en cada llamada.
    suelto = QWidget()

    assert quitar_linea_de_pestanas(suelto) is suelto


def test_la_hoja_de_estilo_no_puede_arreglarlo(app):
    # Se deja escrito para que nadie lo intente otra vez: se probaron el borde del
    # panel, el del QTabBar, el de tab-bar y el fondo — y la línea seguía ahí.
    # Si alguien añade una regla de pestañas al QSS creyendo que basta, esta
    # prueba recuerda que no.
    qss = hoja_de_estilo(Acento.ORO, Modo.OSCURO)

    assert "drawBase" not in qss, "drawBase no es una propiedad de QSS"
