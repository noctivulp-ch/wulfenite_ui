"""El aviso: efímero, en la esquina, y uno solo a la vez.

Se mide la geometría de verdad con la plataforma `offscreen`, que es lo que
permite comprobar dónde acaba el aviso sin abrir una ventana.
"""

from __future__ import annotations

from PySide6.QtCore import QSize
from PySide6.QtGui import QResizeEvent
from PySide6.QtWidgets import QApplication, QWidget

from wulf_qt import Acento, Aviso, Modo, hoja_de_estilo, mostrar_aviso
from wulf_qt.aviso import SALIDA_POR_CLIC_MS, VISIBLE_MS
from wulf_qt.tokens import GAP_GRUPO


def _ventana(app, ancho=800, alto=600) -> QWidget:
    v = QWidget()
    v.resize(ancho, alto)
    return v


def _redimensionar(app, ventana: QWidget, ancho: int, alto: int) -> None:
    """Cambia el tamaño y entrega el evento, como haría un gestor de ventanas.

    No se usa `show()`: bajo la plataforma `offscreen` una ventana mostrada
    recibe el tamaño que le imponga la plataforma y pisa al pedido, así que la
    prueba pasaba o fallaba según el orden en que corriera. Enviar el evento a
    mano es lo mismo que hace Qt de verdad, y es determinista.
    """
    viejo = ventana.size()
    ventana.resize(ancho, alto)
    QApplication.sendEvent(ventana, QResizeEvent(QSize(ancho, alto), viejo))


class TestDondeCae:
    def test_arriba_a_la_derecha(self, app):
        # Arriba a la derecha y no abajo: abajo suelen estar el buscador y los
        # botones de acción, que es justo lo que un aviso no puede tapar.
        ventana = _ventana(app)
        aviso = mostrar_aviso(ventana, "Guardado")
        aviso._animacion.setCurrentTime(aviso._animacion.duration())

        assert aviso.y() == GAP_GRUPO
        derecha = aviso.x() + aviso.width()
        assert derecha == ventana.width() - GAP_GRUPO

    def test_no_pasa_de_media_ventana(self, app):
        # Más largo que eso deja de ser algo que se capta de un vistazo, así que
        # baja de línea en vez de estirarse.
        ventana = _ventana(app, ancho=800)
        aviso = mostrar_aviso(ventana, "Una frase larguísima " * 20)

        assert aviso.width() <= ventana.width() // 2

    def test_lo_corto_no_se_estira(self, app):
        ventana = _ventana(app, ancho=800)
        aviso = mostrar_aviso(ventana, "Ok")

        assert aviso.width() < ventana.width() // 2

    def test_se_recoloca_al_cambiar_el_tamano(self, app):
        # Sin esto, agrandar la ventana con un aviso en pantalla lo deja pegado
        # donde estaba, lejos de la esquina donde debería estar.
        ventana = _ventana(app, ancho=800)
        aviso = mostrar_aviso(ventana, "Guardado")
        aviso._animacion.setCurrentTime(aviso._animacion.duration())
        aviso._animacion.stop()

        _redimensionar(app, ventana, 1200, 600)

        assert aviso.x() + aviso.width() == 1200 - GAP_GRUPO

    def test_se_recoloca_aunque_todavia_este_entrando(self, app):
        # El caso que el primer intento no cubría: si el cambio de tamaño cae
        # MIENTRAS el aviso entra, la animación conserva el destino viejo y lo
        # deja en el sitio equivocado apenas siga corriendo.
        ventana = _ventana(app, ancho=800)
        aviso = mostrar_aviso(ventana, "Guardado")

        _redimensionar(app, ventana, 1200, 600)
        aviso._animacion.setCurrentTime(aviso._animacion.duration())

        assert aviso.x() + aviso.width() == 1200 - GAP_GRUPO


class TestUnoALaVez:
    def test_el_nuevo_reemplaza_al_anterior(self, app):
        # Dos avisos encimados se tapan entre sí y ninguno se lee. Pasa cuando
        # alguien encadena acciones rápido.
        ventana = _ventana(app)
        primero = mostrar_aviso(ventana, "Uno")
        segundo = mostrar_aviso(ventana, "Dos")

        assert primero is not segundo
        assert primero._cerrando, "el anterior debería estar saliendo"
        assert not segundo._cerrando

    def test_cada_ventana_lleva_el_suyo(self, app):
        # Dos ventanas abiertas no comparten aviso: el de una no puede cerrar el
        # de la otra.
        una, otra = _ventana(app), _ventana(app)
        a = mostrar_aviso(una, "Uno")
        mostrar_aviso(otra, "Dos")

        assert not a._cerrando


class TestCuantoDura:
    def test_dos_segundos_y_se_va_solo(self, app):
        ventana = _ventana(app)
        aviso = mostrar_aviso(ventana, "Guardado")

        assert aviso._reloj.isActive()
        assert aviso._reloj.interval() == VISIBLE_MS

    def test_el_clic_lo_saca_mas_rapido_de_lo_que_entro(self, app):
        # Cerrarlo es decir "ya lo vi", y hacerlo esperar la misma animación se
        # siente pegajoso.
        ventana = _ventana(app)
        aviso = mostrar_aviso(ventana, "Guardado")
        entrada = aviso._animacion.duration()

        aviso._cerrar(SALIDA_POR_CLIC_MS)

        assert aviso._animacion.duration() < entrada
        assert not aviso._reloj.isActive(), "el reloj tiene que pararse al cerrar a mano"

    def test_cerrarlo_dos_veces_no_reinicia_la_salida(self, app):
        ventana = _ventana(app)
        aviso = mostrar_aviso(ventana, "Guardado")

        aviso._cerrar(SALIDA_POR_CLIC_MS)
        aviso._cerrar(999)

        assert aviso._animacion.duration() == SALIDA_POR_CLIC_MS


class TestComoSeVe:
    def test_entra_empujando_desde_la_derecha(self, app):
        # El movimiento dice de dónde viene y a dónde se va, y hacia el lado
        # corto: entrar desde arriba lo cruzaría por delante del contenido que
        # se está mirando.
        ventana = _ventana(app)
        aviso = mostrar_aviso(ventana, "Guardado")

        inicio = aviso._animacion.startValue()
        fin = aviso._animacion.endValue()
        assert inicio.x() > fin.x(), "tiene que venir de la derecha"
        assert inicio.y() == fin.y(), "y solo de la derecha, sin caer de arriba"

    def test_lleva_el_canto_del_metal_y_ninguna_sombra(self, app):
        # Es el mismo canto que marca un pozo seleccionado: el aviso se lee como
        # una pieza de la app, no como un cartel del sistema. La profundidad la
        # carga el canto, nunca una sombra (regla 1).
        qss = hoja_de_estilo(Acento.ORO, Modo.OSCURO)
        bloque = qss.split('QLabel[class="wulf-aviso"]')[1].split("}")[0]

        assert "#D9A54A" in bloque.upper(), "el borde debería ser el metal"
        assert "shadow" not in bloque.lower()

    def test_no_entra_en_el_recorrido_del_teclado(self, app):
        # Pararse con Tab en algo que se va solo en dos segundos sería una
        # trampa: el foco quedaría en un widget destruido.
        from PySide6.QtCore import Qt

        ventana = _ventana(app)
        aviso = mostrar_aviso(ventana, "Guardado")

        assert aviso.focusPolicy() == Qt.FocusPolicy.NoFocus

    def test_el_texto_baja_de_linea(self, app):
        ventana = _ventana(app)
        aviso = mostrar_aviso(ventana, "x" * 5)

        assert aviso.wordWrap()


def test_es_un_QLabel_para_que_lo_lea_un_lector_de_pantalla(app):
    # Un rectángulo pintado a mano no anuncia su texto. Que sea una etiqueta de
    # verdad es lo que hace que el aviso exista para quien no lo ve.
    ventana = _ventana(app)
    aviso = mostrar_aviso(ventana, "Guardado")

    assert isinstance(aviso, Aviso)
    assert aviso.text() == "Guardado"
