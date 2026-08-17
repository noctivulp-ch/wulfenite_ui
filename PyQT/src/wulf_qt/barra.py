"""Las barras: la que solo informa y la que se controla.

La distinción no es cosmética. Una barra de progreso **no se puede enfocar**
—no hay nada que hacer con ella, y un destino del teclado en el que no se puede
actuar convierte el recorrido en un laberinto (regla 12)—. Una barra de búsqueda
sí, porque es la pieza central de un reproductor: sin ella no se puede buscar sin
ratón.

Qt ya mueve un `QSlider` con las flechas, así que acá lo que hace falta es la
forma —un pozo alargado— y que el foco se vea.
"""

from __future__ import annotations

from PySide6.QtCore import Qt
from PySide6.QtWidgets import QProgressBar, QSlider, QWidget

from .tokens import Canto, OBJETIVO_TACTIL_MIN

#: Alto del carril. Fino a propósito: la barra es un pozo alargado, no una pieza
#: con volumen propio.
ALTO_BARRA = 8

#: Cuánto avanza una pulsación de flecha, en tanto por uno.
#:
#: Cinco por ciento y no uno: con el 1% recorrer una película de dos horas serían
#: cien pulsaciones. Con el 5%, veinte — y el ajuste fino sale de mantener
#: pulsado, que el sistema repite solo.
PASO_BUSQUEDA = 0.05

#: La escala interna del deslizador. Se trabaja en enteros porque `QSlider` no
#: admite decimales; la API de fuera habla de 0 a 1 como las gemelas.
_ESCALA = 1000


def barra_progreso(parent: QWidget | None = None) -> QProgressBar:
    """Una barra que solo informa: cuánto va de algo que avanza solo.

    No entra en el recorrido del teclado, y es deliberado.
    """
    b = QProgressBar(parent)
    b.setRange(0, _ESCALA)
    b.setTextVisible(False)
    b.setFixedHeight(ALTO_BARRA)
    b.setFocusPolicy(Qt.FocusPolicy.NoFocus)
    b.setProperty("class", "wulf-barra")
    return b


class BarraBusqueda(QSlider):
    """Una barra que se controla: buscar en un vídeo, mover un volumen.

    Habla de 0 a 1 por fuera —igual que las gemelas— y por dentro usa enteros,
    que es lo único que `QSlider` entiende.

    El objetivo táctil es de 48 aunque el carril mida 8: se dibuja fina y se toca
    ancha, que es lo que permite acertarle con el dedo sin engordar la pieza.
    """

    def __init__(self, parent: QWidget | None = None) -> None:
        super().__init__(Qt.Orientation.Horizontal, parent)
        self.setRange(0, _ESCALA)
        self.setFixedHeight(OBJETIVO_TACTIL_MIN)
        # Las flechas mueven un paso; RePág/AvPág, diez. Qt lo hace solo con
        # estos dos números, así que no hay que atender teclas a mano.
        self.setSingleStep(int(PASO_BUSQUEDA * _ESCALA))
        self.setPageStep(int(PASO_BUSQUEDA * _ESCALA) * 2)
        # Fuerte y no `TabFocus`: tiene que alcanzarse también con las flechas
        # del mando, no solo con el tabulador.
        self.setFocusPolicy(Qt.FocusPolicy.StrongFocus)
        self.setProperty("class", "wulf-barra")

    def valor(self) -> float:
        """La posición, de 0 a 1."""
        return self.value() / _ESCALA

    def poner_valor(self, valor: float) -> None:
        """Coloca la posición. Se recorta, así que un cálculo que se pase no rompe nada."""
        self.setValue(int(min(max(valor, 0.0), 1.0) * _ESCALA))


def qss_de_las_barras(pozo: str, carril_enfocado: str, metal: str, canto: str) -> str:
    """El QSS de las dos barras. Lo concatena `hoja_de_estilo`.

    La barra es un **pozo alargado**: carril hundido con su canto y el avance en
    metal. Ni sombra ni degradado de volumen — la profundidad la carga el canto
    (regla 1), también acá.
    """
    radio = ALTO_BARRA // 2
    return f"""
QProgressBar[class="wulf-barra"] {{
    background-color: {pozo};
    border: {Canto.REPOSO}px solid {canto};
    border-radius: {radio}px;
}}
QProgressBar[class="wulf-barra"]::chunk {{
    background-color: {metal};
    border-radius: {radio}px;
}}

/* El carril del deslizador, del alto de la barra y no del widget: se dibuja
   fino aunque el objetivo táctil sea de 48. */
QSlider[class="wulf-barra"]::groove:horizontal {{
    background-color: {pozo};
    border: {Canto.REPOSO}px solid {canto};
    border-radius: {radio}px;
    height: {ALTO_BARRA}px;
}}
QSlider[class="wulf-barra"]::sub-page:horizontal {{
    background-color: {metal};
    border-radius: {radio}px;
}}
QSlider[class="wulf-barra"]::handle:horizontal {{
    background-color: {metal};
    border: {Canto.REPOSO}px solid {metal};
    width: {ALTO_BARRA * 2}px;
    border-radius: {radio}px;
    margin: -{radio}px 0;
}}
/* Sin esto se pulsa Tab, el foco cae en la barra y nada lo dice — que en un
   reproductor significa no poder buscar sin ratón. */
QSlider[class="wulf-barra"]:focus::groove:horizontal {{
    background-color: {carril_enfocado};
    border: {Canto.ENFOCADO}px solid {metal};
}}
QSlider[class="wulf-barra"]:disabled::sub-page:horizontal,
QSlider[class="wulf-barra"]:disabled::handle:horizontal {{
    background-color: {canto};
    border-color: {canto};
}}
"""
