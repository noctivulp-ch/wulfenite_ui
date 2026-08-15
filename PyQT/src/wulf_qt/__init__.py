"""Wulfenite-Qt — superficies lacadas con canto metálico, para PySide6.

El nombre viene del mineral: placas cuadradas y finas con brillo metálico ámbar.
Es lo que hace el sistema — una bandeja continua con pozos hundidos, separados
por un canto de un píxel que es lo único que carga la profundidad.

Es la adaptación del paquete Dart `wulf_flutter`. **Los tokens son idénticos** y
hay una prueba que lo verifica; lo que cambia es cómo se pintan, porque Qt y
Flutter no dibujan igual.

```python
import sys
from PySide6.QtWidgets import QApplication, QLabel
from wulf_qt import Acento, Bandeja, Cuadricula, Celda, Modo, Pozo, aplicar

app = QApplication(sys.argv)
aplicar(app, Acento.ORO, Modo.OSCURO)

bandeja = Bandeja()
rejilla = Cuadricula()
rejilla.poner([
    Celda(Pozo(QLabel("₡128 400")), compacto=6, medio=6, amplio=3),
    Celda(Pozo(QLabel("Primer turno")), compacto=3, medio=3, amplio=1),
])
bandeja.agregar(rejilla)
bandeja.show()
app.exec()
```

Las doce reglas, con el porqué de cada una, están en `../doc/reglas.md`.
"""

from .componentes import (
    Acciones,
    Atenuado,
    Dialogo,
    Enlace,
    boton,
    boton_plano,
    boton_primario,
)
from .reticula import COLUMNAS, Ancho, Bandeja, Celda, Cuadricula, Pozo
from .aviso import Aviso, mostrar_aviso
from .foco import Canto, tono_enfocado
from .tema import ATENUADO, BANDEJA, POZO, aplicar, hoja_de_estilo, paleta
from .texto import FUSION, PROFUNDIDAD, EtiquetaAjustada, Tallado, color_tallado
from .tokens import (
    SEPARACION_FOCO,
    APAGADO_RIBETE,
    GAP_CELDA,
    GAP_GRUPO,
    LACAS,
    METALES,
    OBJETIVO_TACTIL_MIN,
    PADDING_CELDA,
    PADDING_CELDA_GRANDE,
    PARED,
    RADIO_BANDEJA,
    RADIO_POZO,
    RIM,
    SOBRE_METAL,
    UNIDAD,
    Acento,
    Laca,
    Modo,
    canto_del_pozo,
    contraste,
    mezclar,
)

__version__ = "0.1.0"

__all__ = [
    "APAGADO_RIBETE", "ATENUADO", "BANDEJA", "COLUMNAS", "FUSION", "GAP_CELDA",
    "GAP_GRUPO", "LACAS", "METALES", "OBJETIVO_TACTIL_MIN", "PADDING_CELDA",
    "PADDING_CELDA_GRANDE", "PARED", "POZO", "PROFUNDIDAD", "RADIO_BANDEJA",
    "RADIO_POZO", "RIM", "SOBRE_METAL", "UNIDAD",
    "Acciones", "Acento", "Ancho", "Atenuado", "Aviso", "Bandeja", "Canto", "Celda",
    "Cuadricula",
    "Dialogo", "Enlace", "EtiquetaAjustada", "Laca", "Modo", "Pozo", "Tallado",
    "SEPARACION_FOCO", "aplicar", "boton", "boton_plano", "boton_primario",
    "canto_del_pozo", "mostrar_aviso", "tono_enfocado",
    "color_tallado", "contraste", "hoja_de_estilo", "mezclar", "paleta",
]
