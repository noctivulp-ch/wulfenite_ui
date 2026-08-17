"""Las barras: la que informa y la que se controla.

Lo que se vigila es la distinción —una se enfoca y la otra no— y la aritmética
del paso, que es donde un reproductor se rompe de verdad: buscar más allá del
final, o un paso tan fino que recorrer una película sean cien pulsaciones.
"""

from __future__ import annotations

from PySide6.QtCore import Qt
from PySide6.QtWidgets import QWidget

from wulf_qt import (
    ALTO_BARRA,
    OBJETIVO_TACTIL_MIN,
    PASO_BUSQUEDA,
    Acento,
    BarraBusqueda,
    Canto,
    Modo,
    barra_progreso,
    foco_inicial,
    hoja_de_estilo,
)


class TestQuienSeEnfoca:
    def test_la_de_progreso_no_entra_en_el_recorrido(self, app):
        # No hay nada que hacer con ella: pararse ahí convierte el recorrido en
        # un laberinto (regla 12).
        assert barra_progreso().focusPolicy() == Qt.FocusPolicy.NoFocus

    def test_la_de_busqueda_se_alcanza_con_flechas_y_no_solo_con_tabulador(self, app):
        # `StrongFocus` y no `TabFocus`: en un mando no hay tabulador.
        assert BarraBusqueda().focusPolicy() == Qt.FocusPolicy.StrongFocus


class TestBuscarConElTeclado:
    def test_un_paso_mueve_lo_que_dice_el_token(self, app):
        barra = BarraBusqueda()
        barra.poner_valor(0.5)
        barra.setValue(barra.value() + barra.singleStep())

        assert barra.valor() == 0.55

    def test_no_se_puede_buscar_mas_alla_del_final(self, app):
        # Sin el recorte, mantener pulsada la derecha manda la posición fuera
        # del archivo y el reproductor salta a un sitio que no existe.
        barra = BarraBusqueda()
        barra.poner_valor(3.7)

        assert barra.valor() == 1.0

    def test_no_se_puede_buscar_antes_del_principio(self, app):
        barra = BarraBusqueda()
        barra.poner_valor(-2.0)

        assert barra.valor() == 0.0

    def test_recorrer_la_barra_cuesta_un_numero_razonable_de_pulsaciones(self, app):
        # Con el 1% serían cien: recorrer una película de dos horas dejaría de
        # ser viable. El ajuste fino sale de mantener pulsado.
        pulsaciones = round(1 / PASO_BUSQUEDA)
        assert 10 <= pulsaciones <= 25

    def test_avanzar_pagina_mueve_mas_que_una_flecha(self, app):
        barra = BarraBusqueda()
        assert barra.pageStep() > barra.singleStep()


class TestComoSeVe:
    def test_se_dibuja_fina_pero_se_toca_ancha(self, app):
        # El carril mide 8 y el objetivo táctil 48: se acierta con el dedo sin
        # engordar la pieza (regla 7).
        assert ALTO_BARRA < OBJETIVO_TACTIL_MIN
        assert BarraBusqueda().height() == OBJETIVO_TACTIL_MIN
        assert barra_progreso().height() == ALTO_BARRA

    def test_el_foco_engorda_el_canto(self, app):
        qss = hoja_de_estilo(Acento.ORO, Modo.OSCURO)
        bloque = qss.split("QSlider[class=\"wulf-barra\"]:focus::groove:horizontal")[1]
        bloque = bloque.split("}")[0]

        assert f"{Canto.ENFOCADO}px" in bloque

    def test_el_avance_es_el_metal(self, app):
        qss = hoja_de_estilo(Acento.ORO, Modo.OSCURO)
        bloque = qss.split("QSlider[class=\"wulf-barra\"]::sub-page:horizontal")[1]
        bloque = bloque.split("}")[0]

        assert "#D9A54A" in bloque.upper()

    def test_ninguna_barra_lleva_sombra(self, app):
        # La profundidad la carga el canto, también acá (regla 1).
        qss = hoja_de_estilo(Acento.ORO, Modo.OSCURO)
        for bloque in qss.split("wulf-barra")[1:]:
            assert "shadow" not in bloque.split("}")[0].lower()


class TestFocoInicial:
    def test_le_da_el_foco_al_widget(self, app):
        # Sin foco inicial la primera pulsación se gasta en que Qt elija por su
        # cuenta, y elige el primero del orden de tabulación.
        ventana = QWidget()
        primero = BarraBusqueda(ventana)
        elegido = BarraBusqueda(ventana)
        ventana.show()

        foco_inicial(elegido)
        app.processEvents()

        # `focusWidget()` y no `hasFocus()`: bajo la plataforma `offscreen` la
        # ventana nunca se activa, y `hasFocus` exige ventana activa además de
        # foco. Lo que importa acá es cuál de los dos lo tiene dentro.
        assert ventana.focusWidget() is elegido
        assert ventana.focusWidget() is not primero

    def test_se_aplaza_al_siguiente_ciclo(self, app):
        # Pedido durante la construcción, el widget todavía no está en una
        # ventana y Qt lo descarta sin decir nada: el fallo se ve como "a veces
        # funciona".
        ventana = QWidget()
        barra = BarraBusqueda(ventana)
        ventana.show()

        foco_inicial(barra)
        assert ventana.focusWidget() is not barra, "no debería tomarlo en la misma llamada"

        app.processEvents()
        assert ventana.focusWidget() is barra
