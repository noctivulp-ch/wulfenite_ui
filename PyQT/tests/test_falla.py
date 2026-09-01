"""Qué se muestra cuando algo no se pudo cargar.

Las dos cosas que vigila son de daño real, no de estética: que el detalle técnico
**no** se vea salvo que se pida —puede llevar nombres de tablas y consultas
enteras delante de un cliente— y que no se ofrezca un botón que no hace nada.
"""

from __future__ import annotations

from wulf_qt import Falla


def test_muestra_el_mensaje_en_el_idioma_de_quien_atiende(app):
    f = Falla("No se pudieron cargar las ventas")

    assert f.mensaje.text() == "No se pudieron cargar las ventas"


def test_sin_nada_que_reintentar_no_ofrece_el_boton(app):
    # Ofrecerlo seria prometer algo que no pasa.
    assert Falla("algo salió mal").boton is None


def test_con_reintento_aparece_el_boton_y_llama(app):
    llamadas = []
    f = Falla("algo salió mal", al_reintentar=lambda: llamadas.append(1))

    assert f.boton is not None
    f.boton.click()
    assert llamadas == [1]


def test_el_detalle_tecnico_NO_se_ve_por_defecto(app):
    # Es la parte que importa: una excepción de base de datos a pantalla llena
    # delante de quien atiende la caja fue el caso que motivó esta pieza.
    f = Falla("algo salió mal", error=RuntimeError("SELECT * FROM venta"))

    assert f.detalle is None


def test_pedido_explicitamente_si_se_ve_pero_plegado(app):
    f = Falla("algo salió mal", error=RuntimeError("SELECT * FROM venta"),
              mostrar_detalle=True)

    assert f.detalle is not None
    assert f.detalle.isHidden(), "el detalle arranca plegado"
    assert "SELECT" in f.detalle.text()


def test_se_puede_abrir_y_volver_a_cerrar(app):
    f = Falla("x", error=RuntimeError("detalle"), mostrar_detalle=True)

    f._alternar_detalle()
    assert not f.detalle.isHidden()

    f._alternar_detalle()
    assert f.detalle.isHidden()


def test_el_detalle_se_puede_seleccionar_para_copiarlo(app):
    # Un error que no se puede copiar obliga a transcribirlo a mano, que es como
    # se pierde la mitad de un mensaje de error.
    from PySide6.QtCore import Qt

    f = Falla("x", error=RuntimeError("detalle"), mostrar_detalle=True)

    assert f.detalle.textInteractionFlags() & Qt.TextInteractionFlag.TextSelectableByMouse
