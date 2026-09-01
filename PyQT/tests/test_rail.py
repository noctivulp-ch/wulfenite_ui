"""El rail: la columna de navegación vertical.

Existe porque unas pestañas verticales rotan el texto y con el tema puesto eso no
tiene arreglo en Qt. Lo que vigilan estas pruebas es que el rail **no herede** los
problemas que venía a evitar: que el texto se lea de frente, que la marca del
activo señale al contenido, y que se pueda recorrer sin ratón.
"""

from __future__ import annotations

from PySide6.QtCore import Qt

from wulf_qt import Acento, Canto, Modo, Rail, hoja_de_estilo


class TestSeleccion:
    def test_arranca_en_el_primero(self, app):
        assert Rail(["Venta", "Catálogo"]).activo == 0

    def test_construirlo_no_dispara_el_aviso(self, app):
        # Si avisara al construirse, la pantalla navegaría sola al abrirse.
        avisos = []
        Rail(["Venta", "Catálogo"], al_elegir=avisos.append)

        assert avisos == []

    def test_elegir_avisa_una_vez_con_el_indice(self, app):
        avisos = []
        rail = Rail(["Venta", "Catálogo", "Reportes"], al_elegir=avisos.append)

        rail.seleccionar(2)

        assert rail.activo == 2
        assert avisos == [2]

    def test_un_indice_fuera_de_rango_no_hace_nada(self, app):
        # Un cálculo que se pase no puede dejar el rail sin ningún destino
        # marcado: la pantalla quedaría sin decir dónde está uno.
        avisos = []
        rail = Rail(["Venta", "Catálogo"], al_elegir=avisos.append)

        rail.seleccionar(7)
        rail.seleccionar(-1)

        assert rail.activo == 0
        assert avisos == []

    def test_solo_uno_queda_marcado(self, app):
        rail = Rail(["Venta", "Catálogo", "Reportes"])

        rail.seleccionar(1)

        marcados = [b.isChecked() for b in rail._botones]
        assert marcados == [False, True, False]

    def test_un_rail_vacio_no_revienta(self, app):
        assert Rail([]).activo == -1


class TestComoSeRecorre:
    def test_cada_destino_se_alcanza_con_flechas_y_no_solo_con_tabulador(self, app):
        # En un mando no hay tabulador, y el rail es justo la pieza que se usa
        # para navegar con uno.
        rail = Rail(["Venta", "Catálogo"])

        for boton in rail._botones:
            assert boton.focusPolicy() == Qt.FocusPolicy.StrongFocus

    def test_el_objetivo_tactil_se_respeta(self, app):
        from wulf_qt import OBJETIVO_TACTIL_MIN

        rail = Rail(["Venta"])

        assert rail._botones[0].minimumHeight() == OBJETIVO_TACTIL_MIN


class TestComoSeVe:
    def _bloque(self, selector: str) -> str:
        qss = hoja_de_estilo(Acento.ORO, Modo.OSCURO)
        assert selector in qss, f"falta la regla de {selector}"
        return qss.split(selector)[1].split("}")[0]

    def test_la_marca_del_activo_va_en_el_borde_que_toca_al_contenido(self, app):
        # A la derecha, no debajo: el contenido está al lado. Es la misma regla
        # que en las pestañas.
        bloque = self._bloque('QPushButton[class="wulf-rail-destino"]:checked')

        assert "border-right" in bloque
        assert f"{Canto.SELECCIONADO}px" in bloque

    def test_el_foco_engorda_esa_misma_marca(self, app):
        bloque = self._bloque('QPushButton[class="wulf-rail-destino"]:focus')

        assert "border-right" in bloque
        assert f"{Canto.ENFOCADO}px" in bloque

    def test_un_destino_no_es_un_pozo(self, app):
        # Es cromo sobre la bandeja: sin caja y sin fondo propio. El pozo es lo
        # que hay al lado.
        bloque = self._bloque('QPushButton[class="wulf-rail-destino"] ')

        assert "background: transparent" in bloque
        assert "border: none" in bloque

    def test_el_activo_va_en_texto_pleno_y_el_resto_atenuado(self, app):
        from wulf_qt import LACAS

        laca = LACAS[Modo.OSCURO]
        reposo = self._bloque('QPushButton[class="wulf-rail-destino"] ')
        activo = self._bloque('QPushButton[class="wulf-rail-destino"]:checked')

        assert laca.texto_atenuado_en_bandeja.lower() in reposo.lower()
        assert laca.texto_en_bandeja.lower() in activo.lower()
