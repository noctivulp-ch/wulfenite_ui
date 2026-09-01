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


class TestPestanasVerticales:
    """La marca del activo va en el borde que TOCA AL PANEL, y ese borde cambia
    con la posición de las pestañas.

    **Verificado midiendo píxeles**, no deducido: con la regla vieja de
    `border-bottom` el indicador de unas pestañas a la izquierda salía de 56x2
    —horizontal, cruzado respecto del panel— y con las reglas por posición sale
    de 2x40, vertical y pegado al panel.

    Esa medición no vive acá: pintar y contar píxeles con la `QApplication`
    compartida de la suite la tumbaba con un fallo de bus. Lo que queda es la
    regla en sí, que es lo que alguien puede romper sin darse cuenta al tocar el
    QSS — y el motivo por el que existe, escrito arriba.
    """

    def _bloque(self, selector: str) -> str:
        from wulf_qt import Acento, Modo, hoja_de_estilo

        qss = hoja_de_estilo(Acento.ORO, Modo.OSCURO)
        assert selector in qss, f"falta la regla de {selector}"
        return qss.split(selector)[1].split("}")[0]

    def test_cada_posicion_marca_el_borde_que_toca_al_panel(self, app):
        # Arriba el panel está debajo, así que la marca va abajo. A la izquierda
        # el panel está a la derecha, así que va a la derecha. Etcétera.
        esperado = {
            "QTabBar::tab:top:selected": "border-bottom",
            "QTabBar::tab:bottom:selected": "border-top",
            "QTabBar::tab:left:selected": "border-right",
            "QTabBar::tab:right:selected": "border-left",
        }
        for selector, borde in esperado.items():
            assert borde in self._bloque(selector), f"{selector} debería marcar {borde}"

    def test_la_regla_general_ya_no_subraya(self, app):
        # Si volviera a llevar `border-bottom`, se sumaría al de cada posición y
        # las pestañas verticales quedarían marcadas por dos lados.
        from wulf_qt import Acento, Modo, hoja_de_estilo

        qss = hoja_de_estilo(Acento.ORO, Modo.OSCURO)
        general = qss.split("QTabBar::tab:selected {")[1].split("}")[0]
        assert "border" not in general

    def test_el_grosor_es_el_del_canto_seleccionado(self, app):
        from wulf_qt import Canto

        assert f"{Canto.SELECCIONADO}px" in self._bloque("QTabBar::tab:left:selected")
