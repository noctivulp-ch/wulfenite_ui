"""La acción que confirma va a la derecha, y cancelar a su izquierda.

No es estética: para guardar se pulsa a la derecha sin mirar. En la app de la
que salió este sistema estaba al revés en los once diálogos, y en el de "crear
categoría al vuelo" eso significa pulsar Cancelar creyendo guardar. Lo encontró
el dueño usándola, no una revisión de código.

Se comprueba con la geometría real, no leyendo el código: lo que importa es
dónde acaba el botón en la pantalla. Es una convención que se rompe sola — al
agregar un botón, lo natural es ponerlo donde quepa.
"""

from __future__ import annotations

from PySide6.QtWidgets import QLabel

from wulf_qt import Acciones, Dialogo, boton, boton_primario


def _colocado(widget) -> None:
    """Fuerza el cálculo de geometría sin abrir una ventana."""
    widget.resize(400, 200)
    widget.show()
    widget.layout().activate()


def test_confirmar_queda_a_la_derecha_de_cancelar(app) -> None:
    guardar = boton_primario("Guardar")
    cancelar = boton("Cancelar")
    acciones = Acciones(confirmar=guardar, cancelar=cancelar)
    _colocado(acciones)

    assert guardar.x() > cancelar.x(), "guardar tiene que quedar a la derecha"


def test_van_en_la_misma_fila_no_apilados(app) -> None:
    """Apilados, el pulgar cae en uno o en otro según la altura."""
    guardar = boton_primario("Guardar")
    cancelar = boton("Cancelar")
    acciones = Acciones(confirmar=guardar, cancelar=cancelar)
    _colocado(acciones)

    assert guardar.y() == cancelar.y()


def test_lo_destructivo_queda_lejos_del_boton_de_guardar(app) -> None:
    """Los extras caen a la izquierda y debajo.

    Es el formulario de un objeto que se puede borrar: lo peligroso no puede
    estar donde el pulgar va a confirmar.
    """
    guardar = boton_primario("Guardar")
    cancelar = boton("Cancelar")
    eliminar = boton("Eliminar")
    acciones = Acciones(confirmar=guardar, cancelar=cancelar, extras=[eliminar])
    _colocado(acciones)

    assert eliminar.x() < guardar.x()
    assert eliminar.y() > guardar.y()


def test_sin_nada_que_confirmar_la_salida_va_a_la_derecha(app) -> None:
    """Sin acción primaria no hay confusión posible, y la derecha es donde llega
    el dedo."""
    cerrar = boton("Cerrar")
    acciones = Acciones(cancelar=cerrar)
    _colocado(acciones)

    assert cerrar.x() + cerrar.width() / 2 > acciones.width() / 2


def test_el_dialogo_monta_sus_acciones(app) -> None:
    guardar = boton_primario("Guardar")
    cancelar = boton("Cancelar")
    dialogo = Dialogo(
        "Editar producto",
        QLabel("cuerpo"),
        Acciones(confirmar=guardar, cancelar=cancelar),
    )
    _colocado(dialogo)

    assert guardar.x() > cancelar.x()
    assert dialogo.windowTitle() == "Editar producto"
