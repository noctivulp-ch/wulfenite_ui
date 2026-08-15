"""El aviso: una notificación efímera que no ocupa la pantalla.

**Uno solo, para todo**: lo guardado, lo exportado, y también lo que salió mal
cuando no amerita un diálogo. Un diálogo interrumpe y hay que cerrarlo; un aviso
se lee de reojo y se va.

Reglas de la pieza, iguales a las de las gemelas:

- **Un rectángulo**, con el fondo del pozo y el metal por borde. Es el mismo
  canto que marca un pozo seleccionado: el aviso se lee como una pieza de la
  app, no como un cartel del sistema. Sin sombra — la profundidad la carga el
  canto (regla 1).
- **Entra y sale empujando desde la derecha**, no con un fundido: el movimiento
  dice de dónde viene y a dónde se va, y hacia el lado corto — entrar desde
  arriba lo cruzaría por delante del contenido que se está mirando.
- **Dos segundos**, y se va solo. Un clic lo saca antes.
- **Tope de media ventana**: pasado eso baja de línea en vez de estirarse. Una
  tira larguísima de una sola línea no se lee de un vistazo, que es lo único que
  un aviso tiene que conseguir.
"""

from __future__ import annotations

from PySide6.QtCore import (
    QEasingCurve,
    QEvent,
    QPoint,
    QPropertyAnimation,
    Qt,
    QTimer,
)
from PySide6.QtWidgets import QLabel, QWidget

from .tokens import GAP_GRUPO, PADDING_CELDA, RADIO_POZO, UNIDAD

#: Cuánto se queda en pantalla antes de irse solo.
VISIBLE_MS = 2000

#: Lo que tarda en entrar empujando.
ENTRADA_MS = 220

#: Al hacerle clic se va MÁS RÁPIDO de lo que entró: cerrarlo es decir "ya lo
#: vi", y hacerlo esperar la misma animación se siente pegajoso.
SALIDA_POR_CLIC_MS = 120


class Aviso(QLabel):
    """El aviso concreto. No se construye a mano: se usa `mostrar_aviso`."""

    def __init__(self, mensaje: str, parent: QWidget) -> None:
        super().__init__(mensaje, parent)
        self.setWordWrap(True)
        self.setTextInteractionFlags(Qt.TextInteractionFlag.NoTextInteraction)
        # Es decoración informativa: no se puede enfocar ni se mete en el
        # recorrido del teclado. Pararse en algo que se va solo en dos segundos
        # sería una trampa.
        self.setFocusPolicy(Qt.FocusPolicy.NoFocus)
        self.setAttribute(Qt.WidgetAttribute.WA_StyledBackground, True)
        self.setProperty("class", "wulf-aviso")
        self.setContentsMargins(PADDING_CELDA, UNIDAD, PADDING_CELDA, UNIDAD)

        self._animacion = QPropertyAnimation(self, b"pos", self)
        self._reloj = QTimer(self)
        self._reloj.setSingleShot(True)
        self._reloj.timeout.connect(lambda: self._cerrar(ENTRADA_MS))
        self._cerrando = False

        parent.installEventFilter(self)

    # -- colocación ---------------------------------------------------------

    def _destino(self) -> QPoint:
        """Arriba a la derecha, dentro del labio de la bandeja."""
        padre = self.parentWidget()
        return QPoint(padre.width() - self.width() - GAP_GRUPO, GAP_GRUPO)

    def _fuera(self) -> QPoint:
        """Fuera del borde derecho, de donde entra y a donde vuelve."""
        destino = self._destino()
        return QPoint(self.parentWidget().width() + int(self.width() * 0.4), destino.y())

    def _recolocar(self) -> None:
        padre = self.parentWidget()
        # Media ventana de tope. Más largo deja de ser algo que se capta de un
        # vistazo, así que baja de línea.
        self.setMaximumWidth(max(padre.width() // 2, 1))
        self.adjustSize()

    def eventFilter(self, objeto: QWidget, evento: QEvent) -> bool:
        """Si la ventana cambia de tamaño, el aviso se recoloca.

        Sin esto, agrandar la ventana con un aviso en pantalla lo deja pegado
        donde estaba, lejos de la esquina donde debería estar.

        Y hay que refrescar **también la animación en curso**: sus extremos se
        calcularon con el ancho viejo, así que si el cambio de tamaño cae
        mientras el aviso todavía está entrando, el destino guardado lo devuelve
        al sitio equivocado apenas siga corriendo.
        """
        if evento.type() != QEvent.Type.Resize or objeto is not self.parentWidget():
            return False

        self._recolocar()
        corriendo = self._animacion.state() == QPropertyAnimation.State.Running

        if self._cerrando:
            if corriendo:
                self._animacion.setEndValue(self._fuera())
            return False

        if corriendo:
            self._animacion.setStartValue(self._fuera())
            self._animacion.setEndValue(self._destino())
        else:
            self.move(self._destino())
        return False

    # -- ciclo de vida ------------------------------------------------------

    def entrar(self) -> None:
        self._recolocar()
        self.move(self._fuera())
        self.show()
        self.raise_()

        self._animacion.stop()
        self._animacion.setDuration(ENTRADA_MS)
        self._animacion.setEasingCurve(QEasingCurve.Type.OutCubic)
        self._animacion.setStartValue(self._fuera())
        self._animacion.setEndValue(self._destino())
        self._animacion.start()

        self._reloj.start(VISIBLE_MS)

    def mousePressEvent(self, evento) -> None:  # noqa: ANN001 - firma de Qt
        self._cerrar(SALIDA_POR_CLIC_MS)

    def _cerrar(self, duracion: int) -> None:
        if self._cerrando:
            return
        self._cerrando = True
        self._reloj.stop()

        self._animacion.stop()
        self._animacion.setDuration(duracion)
        self._animacion.setEasingCurve(QEasingCurve.Type.InCubic)
        self._animacion.setStartValue(self.pos())
        self._animacion.setEndValue(self._fuera())
        # `deleteLater` y no `close`: el aviso es de usar y tirar, y dejarlos
        # vivos acumula un widget por cada acción del usuario.
        self._animacion.finished.connect(self.deleteLater)
        self._animacion.start()


#: El aviso vivo, por ventana. Uno a la vez: dos encimados se tapan entre sí y
#: ninguno se lee. El nuevo reemplaza al anterior, que es lo que pasa cuando
#: alguien encadena acciones rápido.
_activos: dict[int, Aviso] = {}


def mostrar_aviso(ventana: QWidget, mensaje: str) -> Aviso:
    """Muestra [mensaje] sobre [ventana] y devuelve el aviso.

    ```python
    mostrar_aviso(self, "Guardado")
    ```

    La ventana tiene que ser la que ocupa la pantalla —no un widget interior—
    porque el aviso se coloca respecto de su esquina superior derecha.
    """
    anterior = _activos.pop(id(ventana), None)
    if anterior is not None:
        try:
            anterior._cerrar(SALIDA_POR_CLIC_MS)
        except RuntimeError:
            # Ya lo destruyó Qt: el diccionario tenía una referencia muerta.
            pass

    aviso = Aviso(mensaje, ventana)
    _activos[id(ventana)] = aviso
    aviso.destroyed.connect(lambda: _activos.pop(id(ventana), None))
    aviso.entrar()
    return aviso


def qss_del_aviso(pozo: str, metal: str, texto: str) -> str:
    """El QSS del aviso. Lo concatena `hoja_de_estilo`."""
    return f"""
QLabel[class="wulf-aviso"] {{
    background-color: {pozo};
    color: {texto};
    border: 1px solid {metal};
    border-radius: {RADIO_POZO}px;
}}
"""
