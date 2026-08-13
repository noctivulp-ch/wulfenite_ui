"""La retícula de seis columnas, y los pozos que se colocan en ella.

Seis y no cuatro ni doce: seis se divide en 2 y en 3, que son los repartos que
piden las pantallas de verdad —mitades y tercios—. Con cuatro los tercios no
cierran; con doce, cada columna queda tan fina que el número deja de significar
nada.

Lo que cambia entre tamaños de ventana **no es el número de columnas**: es
cuántas ocupa cada pieza. Eso es lo que hace que cinco pantallas distintas se
lean como la misma app.
"""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum

from PySide6.QtWidgets import QFrame, QGridLayout, QLayout, QVBoxLayout, QWidget

from .tema import BANDEJA, POZO
from .tokens import PADDING_CELDA, PARED, RIM

#: Cuántas columnas tiene la retícula. El mismo número en los tres tamaños.
COLUMNAS = 6


class Ancho(Enum):
    """El tamaño de la ventana, en tres tramos."""

    COMPACTO = "compacto"
    """Menos de 600 px: teléfono, o un panel lateral estrecho."""

    MEDIO = "medio"
    """De 600 a 1023: tablet."""

    AMPLIO = "amplio"
    """1024 o más: escritorio."""

    @staticmethod
    def de(ancho: int) -> "Ancho":
        """El tramo que le toca a un ancho en píxeles.

        Se decide a partir de un ancho y no de la ventana a propósito: un panel
        lateral estrecho dentro de un escritorio ancho tiene que componerse como
        compacto, o queda ilegible.
        """
        if ancho >= 1024:
            return Ancho.AMPLIO
        if ancho >= 600:
            return Ancho.MEDIO
        return Ancho.COMPACTO


@dataclass(frozen=True)
class Celda:
    """Una pieza y cuántas columnas ocupa en cada tamaño.

    Declarar los tres valores es obligatorio a propósito. Un valor por defecto
    del estilo "si no decís nada, ocupa todo" escondería justamente la decisión
    que esta retícula existe para hacer explícita.
    """

    widget: QWidget
    compacto: int
    medio: int
    amplio: int

    def __post_init__(self) -> None:
        for nombre in ("compacto", "medio", "amplio"):
            valor = getattr(self, nombre)
            if not 1 <= valor <= COLUMNAS:
                raise ValueError(f"{nombre}={valor}: tiene que estar entre 1 y {COLUMNAS}")

    def columnas_en(self, ancho: Ancho) -> int:
        """Cuántas columnas ocupa en un tamaño concreto."""
        return {
            Ancho.COMPACTO: self.compacto,
            Ancho.MEDIO: self.medio,
            Ancho.AMPLIO: self.amplio,
        }[ancho]


class Pozo(QFrame):
    """Un compartimento tallado en la bandeja.

    La forma la pone la hoja de estilo —canto de un píxel, radio pequeño, el
    degradado corto del contacto— así que esto es un `QFrame` con la clase
    puesta, no un widget que se pinta solo. Todo lo que se pueda dejar en QSS se
    deja: es lo que permite cambiar el acabado sin tocar los widgets.
    """

    def __init__(
        self,
        contenido: QWidget | None = None,
        *,
        padding: int = PADDING_CELDA,
        seleccionado: bool = False,
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(parent)
        self.setProperty("class", POZO)
        self.setProperty("seleccionado", "true" if seleccionado else "false")
        caja = QVBoxLayout(self)
        caja.setContentsMargins(padding, padding, padding, padding)
        caja.setSpacing(PARED)
        if contenido is not None:
            caja.addWidget(contenido)
        self._caja = caja

    def agregar(self, widget: QWidget) -> None:
        """Añade otro widget dentro del compartimento."""
        self._caja.addWidget(widget)

    def marcar(self, seleccionado: bool) -> None:
        """Enciende o apaga la selección: el canto pasa al metal y engorda.

        Hay que repolir porque Qt no reevalúa una regla que depende de una
        propiedad cuando la propiedad cambia — es el gotcha clásico de las hojas
        de estilo con selectores por atributo.
        """
        self.setProperty("seleccionado", "true" if seleccionado else "false")
        estilo = self.style()
        estilo.unpolish(self)
        estilo.polish(self)


class Bandeja(QWidget):
    """El plano continuo de una pantalla, con su labio exterior.

    Una por pantalla. Los pozos van dentro, separados por [PARED] — y ese hueco
    **no es fondo asomándose**, es material de bandeja: dos pozos vecinos
    comparten una sola pared, no dos márgenes.
    """

    def __init__(self, *, rim: bool = True, parent: QWidget | None = None) -> None:
        super().__init__(parent)
        self.setProperty("class", BANDEJA)
        borde = RIM if rim else 0
        self._caja = QVBoxLayout(self)
        self._caja.setContentsMargins(borde, borde, borde, borde)
        self._caja.setSpacing(PARED)

    def agregar(self, widget: QWidget) -> None:
        """Pone un widget —normalmente una [Cuadricula]— dentro de la bandeja."""
        self._caja.addWidget(widget)

    def layout_interno(self) -> QLayout:
        """El layout de la bandeja, para casos que necesiten más control."""
        return self._caja


class Cuadricula(QWidget):
    """Reparte celdas en filas de seis columnas.

    Recoloca sola al cambiar el tamaño de la ventana: se recalcula el tramo y,
    si cambió, se rehace el reparto. Solo si cambió — rehacerlo en cada píxel de
    un arrastre es lo que hace que una ventana se sienta pegajosa.

    ```python
    c = Cuadricula()
    c.poner([
        Celda(total,  compacto=6, medio=6, amplio=3),
        Celda(turno1, compacto=3, medio=3, amplio=1),
        Celda(turno2, compacto=3, medio=3, amplio=1),
    ])
    ```
    """

    def __init__(self, *, separacion: int = PARED, parent: QWidget | None = None) -> None:
        super().__init__(parent)
        self._celdas: list[Celda] = []
        self._ancho_actual: Ancho | None = None
        self._rejilla = QGridLayout(self)
        self._rejilla.setContentsMargins(0, 0, 0, 0)
        self._rejilla.setSpacing(separacion)

    def poner(self, celdas: list[Celda]) -> None:
        """Reemplaza el contenido por [celdas], en orden de lectura."""
        self._celdas = celdas
        self._ancho_actual = None
        self._recolocar()

    def resizeEvent(self, event) -> None:  # noqa: N802, ANN001 (API de Qt)
        self._recolocar()
        super().resizeEvent(event)

    def _recolocar(self) -> None:
        ancho = Ancho.de(self.width())
        if ancho == self._ancho_actual:
            return
        self._ancho_actual = ancho

        while self._rejilla.count():
            self._rejilla.takeAt(0)

        fila = columna = 0
        for celda in self._celdas:
            ocupa = celda.columnas_en(ancho)
            if columna + ocupa > COLUMNAS:
                fila += 1
                columna = 0
            self._rejilla.addWidget(celda.widget, fila, columna, 1, ocupa)
            columna += ocupa
            if columna >= COLUMNAS:
                fila += 1
                columna = 0

        for c in range(COLUMNAS):
            self._rejilla.setColumnStretch(c, 1)
