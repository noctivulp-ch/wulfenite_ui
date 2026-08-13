"""Texto grabado en la superficie, y etiquetas que encogen antes que partirse.

Esto es lo que **no** se puede hacer con una hoja de estilo. El relieve son tres
copias del mismo glifo —sombra, luz y glifo— desplazadas un píxel y pintadas con
`QPainter`; QSS no tiene forma de expresar eso.

El costo hay que decirlo: pintar tres veces cuesta tres veces. En el proyecto de
origen, medido en un teléfono de gama baja, apagar este efecto bajó el raster al
desplazar una lista de 12,9 ms a 7,9. Por eso `Tallado` se puede apagar entero
con [Tallado.efecto_activo].
"""

from __future__ import annotations

from PySide6.QtCore import Qt
from PySide6.QtGui import QColor, QFont, QFontMetricsF, QPainter, QPaintEvent
from PySide6.QtWidgets import QLabel, QWidget

from .tokens import mezclar

#: Desplazamiento en píxeles de las capas de luz y sombra.
PROFUNDIDAD = 1.25

#: Cuánto se aclara la superficie para la capa de luz.
_LUZ = 0.70

#: Cuánto se oscurece para la capa de sombra.
_SOMBRA = 0.65

#: Cuánto se acerca el glifo al color de su superficie, por nivel.
#:
#: El tallado se consigue **gastando contraste**: el glifo se acerca al color de
#: detrás. Por eso hay techo y por eso el techo depende del modo — la paleta
#: clara tiene mucho menos margen de luminancia que la oscura.
FUSION = {
    "sutil": {"claro": 0.28, "oscuro": 0.42},
    "marcado": {"claro": 0.46, "oscuro": 0.62},
}


def color_tallado(base: str, sobre: str, *, nivel: str = "sutil", modo: str = "oscuro") -> str:
    """El color de un glifo grabado: [base] acercado a su superficie [sobre].

    Gastar contraste donde el tamaño lo compensa —números grandes, iconos— y no
    donde hay que leer. Con `nivel="sutil"` el resultado se queda por debajo del
    techo de 4,5:1 que pide un texto normal; con `"marcado"`, por debajo del 3:1
    que WCAG permite a texto grande y a elementos no textuales.
    """
    return mezclar(base, sobre, FUSION[nivel][modo])


class Tallado(QLabel):
    """Una etiqueta cuyo texto se lee grabado en la superficie.

    ```python
    t = Tallado("₡128 400", sobre=laca.pozo)
    t.setStyleSheet("font-size: 32px;")
    ```

    [sobre] es el color **real** de detrás. Pasarlo mal es el fallo que más se
    repite con esta pieza: el relieve se calcula desde ahí, así que con el color
    equivocado se ve una sombra flotando en vez de una incisión.
    """

    #: Puesto en `False`, se dibuja como una etiqueta normal. Es el interruptor
    #: para aparatos lentos: no hay que cambiar el árbol de widgets.
    efecto_activo = True

    def __init__(
        self,
        texto: str = "",
        *,
        sobre: str,
        profundidad: float = PROFUNDIDAD,
        realzado: bool = False,
        luz: str | None = None,
        sombra: str | None = None,
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(texto, parent)
        self._sobre = sobre
        self._profundidad = profundidad
        #: Realzado es el mismo dibujo con el signo cambiado. Existe porque hace
        #: falta para elegir: sin verlo al lado, "hundido" es una palabra.
        self._realzado = realzado
        self._luz = luz
        self._sombra = sombra

    def paintEvent(self, event: QPaintEvent) -> None:  # noqa: N802 (API de Qt)
        if not self.efecto_activo or self._profundidad <= 0:
            super().paintEvent(event)
            return

        luz = QColor(self._luz or mezclar(self._sobre, "#FFFFFF", _LUZ))
        sombra = QColor(self._sombra or mezclar(self._sobre, "#000000", _SOMBRA))
        # La pared de arriba de la incisión queda en sombra y su fondo atrapa la
        # luz que viene de arriba. Realzado invierte exactamente eso.
        signo = -1.0 if self._realzado else 1.0

        pintor = QPainter(self)
        pintor.setRenderHint(QPainter.RenderHint.TextAntialiasing)
        opciones = int(self.alignment() | Qt.TextFlag.TextWordWrap)

        for color, dy in ((sombra, -self._profundidad * signo), (luz, self._profundidad * signo)):
            pintor.setPen(color)
            pintor.drawText(self.rect().adjusted(0, round(dy), 0, round(dy)), opciones, self.text())

        pintor.setPen(self.palette().color(self.foregroundRole()))
        pintor.drawText(self.rect(), opciones, self.text())
        pintor.end()


class EtiquetaAjustada(QLabel):
    """Una etiqueta que **encoge la tipografía antes que partirse**.

    El caso que la trajo: un rótulo de una palabra en una celda estrecha. Qt
    parte por carácter cuando una palabra sola es más ancha que la línea, y
    "Optimización" sale como "Optimizaci / ón".

    Encoge lo justo y nunca agranda: lo que ya entraba se queda igual. Por
    debajo de [minimo] se planta — se prefiere un texto apretado a uno que no se
    puede leer, y si una etiqueta llega ahí el arreglo es acortar la palabra.
    """

    def __init__(
        self,
        texto: str = "",
        *,
        minimo: float = 9,
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(texto, parent)
        self._minimo = minimo
        self.setAlignment(Qt.AlignmentFlag.AlignCenter)

    def tamano_para(self, ancho: int) -> float:
        """El tamaño de letra con el que el texto entra en [ancho].

        Es una función pura y pública a propósito: así se puede comprobar sin
        rasterizar nada, y el pintado no tiene que cambiar el estado del widget.
        Cambiar la fuente **dentro** de `paintEvent` fue el primer intento, y es
        camino a un bucle de repintado: tocar la fuente pide otro layout, que
        pide otro pintado.
        """
        fuente = QFont(self.font())
        tamano = fuente.pointSizeF()
        if tamano <= 0 or not self.text():
            return tamano

        # Medio punto por paso: saltos más grandes se notan entre dos etiquetas
        # vecinas de la misma fila.
        while tamano > self._minimo:
            fuente.setPointSizeF(tamano)
            if QFontMetricsF(fuente).horizontalAdvance(self.text()) <= ancho:
                break
            tamano -= 0.5
        return tamano

    def paintEvent(self, event: QPaintEvent) -> None:  # noqa: N802 (API de Qt)
        fuente = QFont(self.font())
        tamano = self.tamano_para(self.width())
        if tamano == fuente.pointSizeF():
            super().paintEvent(event)
            return

        fuente.setPointSizeF(tamano)
        pintor = QPainter(self)
        pintor.setRenderHint(QPainter.RenderHint.TextAntialiasing)
        pintor.setFont(fuente)
        pintor.setPen(self.palette().color(self.foregroundRole()))
        pintor.drawText(self.rect(), int(self.alignment()), self.text())
        pintor.end()
