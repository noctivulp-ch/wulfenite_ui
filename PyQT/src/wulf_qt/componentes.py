"""Componentes que llevan sus reglas dentro.

Es la diferencia entre un tema y un sistema de diseño. Un tema pinta lo que le
den; esto **impide** colocar mal los botones de un diálogo, porque la única
manera de construirlo pone la acción que confirma donde tiene que ir.

Una regla escrita en una guía es una regla que alguien va a saltarse con un buen
argumento. Una regla escrita en el constructor, no.
"""

from __future__ import annotations

from PySide6.QtCore import Qt
from PySide6.QtWidgets import (
    QDialog,
    QDialogButtonBox,
    QHBoxLayout,
    QLabel,
    QPushButton,
    QVBoxLayout,
    QWidget,
)

from .tema import ATENUADO
from .tokens import GAP_CELDA, OBJETIVO_TACTIL_MIN, PADDING_CELDA, PARED, UNIDAD


def boton_primario(texto: str, parent: QWidget | None = None) -> QPushButton:
    """El botón de la acción que confirma: relleno en metal.

    Uno por pantalla. El acento no se usa decorativamente — si estuviera en
    todos los botones no destacaría en ninguno.
    """
    b = QPushButton(texto, parent)
    b.setProperty("primario", "true")
    b.setMinimumHeight(OBJETIVO_TACTIL_MIN)
    return b


def boton(texto: str, parent: QWidget | None = None) -> QPushButton:
    """Un botón normal: un pozo pequeño."""
    b = QPushButton(texto, parent)
    b.setMinimumHeight(OBJETIVO_TACTIL_MIN)
    return b


def boton_plano(texto: str, parent: QWidget | None = None) -> QPushButton:
    """Un botón sin canto, para lo secundario que no debe pesar."""
    b = QPushButton(texto, parent)
    b.setProperty("plano", "true")
    b.setMinimumHeight(OBJETIVO_TACTIL_MIN)
    return b


class Acciones(QWidget):
    """La fila de botones de un diálogo, con el orden decidido.

    **La acción que confirma va a la derecha y cancelar a su izquierda, en la
    misma fila.** No es estética: para guardar se pulsa a la derecha sin mirar,
    y una app que lo tenga al revés hace que cancelar se pulse creyendo guardar.

    Lo que actúa **sobre el objeto** —reactivar, eliminar, elegir un archivo— va
    en [extras], que caen a la izquierda y debajo: lejos del sitio al que llega
    el pulgar a confirmar, que es justo donde no se quiere un borrado.

    ```python
    Acciones(confirmar=boton_primario("Guardar"), cancelar=boton("Cancelar"))
    ```

    Con [confirmar] en `None` —un diálogo que solo se cierra— la salida se queda
    a la derecha: sin nada que confirmar no hay confusión posible, y es donde
    llega el dedo.
    """

    def __init__(
        self,
        *,
        confirmar: QPushButton | None = None,
        cancelar: QPushButton | None = None,
        extras: list[QPushButton] | None = None,
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(parent)
        columna = QVBoxLayout(self)
        columna.setContentsMargins(0, 0, 0, 0)
        columna.setSpacing(UNIDAD)

        fila = QHBoxLayout()
        fila.setSpacing(GAP_CELDA)
        if confirmar is None:
            fila.addStretch(1)
            if cancelar is not None:
                fila.addWidget(cancelar)
        else:
            if cancelar is not None:
                fila.addWidget(cancelar)
            fila.addStretch(1)
            fila.addWidget(confirmar)
        columna.addLayout(fila)

        for extra in extras or []:
            secundaria = QHBoxLayout()
            secundaria.addWidget(extra)
            secundaria.addStretch(1)
            columna.addLayout(secundaria)


class Dialogo(QDialog):
    """Un diálogo del sistema: título, cuerpo y [Acciones].

    Se construye con las acciones ya montadas, así que no hay forma de acabar
    con el botón de guardar en el sitio equivocado.
    """

    def __init__(
        self,
        titulo: str,
        contenido: QWidget,
        acciones: Acciones,
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(parent)
        self.setWindowTitle(titulo)
        columna = QVBoxLayout(self)
        columna.setContentsMargins(
            PADDING_CELDA, PADDING_CELDA, PADDING_CELDA, PADDING_CELDA
        )
        columna.setSpacing(PARED)

        encabezado = QLabel(titulo)
        fuente = encabezado.font()
        fuente.setPointSizeF(fuente.pointSizeF() * 1.25)
        fuente.setWeight(fuente.Weight.DemiBold)
        encabezado.setFont(fuente)

        columna.addWidget(encabezado)
        columna.addWidget(contenido, 1)
        columna.addWidget(acciones)

        # `QDialogButtonBox` reordena los botones según el escritorio: en GNOME
        # el de confirmar va a un lado y en KDE al otro. Eso es exactamente lo
        # que esta regla no admite, así que no se usa.
        assert not isinstance(acciones, QDialogButtonBox)


class Enlace(QLabel):
    """Un enlace con pinta de enlace: color del acento **y** subrayado.

    Las dos cosas, no una. El color solo no basta —quien no distingue ese tono
    ve texto normal— y el subrayado es la pista que no depende del color.

    Puede no mostrar la dirección: el texto puede ser un nombre. Lo que no puede
    es no parecer tocable. Un texto que hace algo al pulsarlo sin anunciarlo no
    es discreto, es indescifrable.
    """

    def __init__(
        self,
        texto: str,
        url: str,
        *,
        parent: QWidget | None = None,
    ) -> None:
        # Qt pinta los enlaces con `QPalette.Link`, que el tema ya puso en el
        # metal, así que el color sale solo del acabado elegido.
        super().__init__(f'<a href="{url}" style="text-decoration: underline;">{texto}</a>', parent)
        self.setTextFormat(Qt.TextFormat.RichText)
        self.setOpenExternalLinks(True)
        self.setTextInteractionFlags(Qt.TextInteractionFlag.TextBrowserInteraction)
        self.setCursor(Qt.CursorShape.PointingHandCursor)
        self.setMinimumHeight(OBJETIVO_TACTIL_MIN)


class Atenuado(QLabel):
    """Texto de apoyo: rótulos y explicaciones que no compiten con el dato."""

    def __init__(self, texto: str, parent: QWidget | None = None) -> None:
        super().__init__(texto, parent)
        self.setProperty("class", ATENUADO)
        self.setWordWrap(True)
