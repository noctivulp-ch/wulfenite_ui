"""Los iconos del juego, teñidos y a su tamaño.

Los archivos son de **un solo color**, que no es el color final: se retiñen al
dibujar. Es lo que permite que el mismo archivo sirva para el estado inactivo y
para el activo sin mantener dos copias que acaban desincronizadas.

Qt carga SVG de fábrica, así que no hace falta convertirlos a nada — pero sí
**rasterizar al tamaño final**: un `QIcon` escalado desde un pixmap de otro
tamaño sale borroso, y el trazo de 1,75 es justo donde más se nota.

**Los dibujos van incrustados, no se leen del disco.** La primera versión los
buscaba en una carpeta hermana del paquete, y eso se rompe en cuanto el programa
se empaqueta: Nuitka en *onefile*, PyInstaller y un wheel instalado dejan esa
ruta apuntando fuera del bundle. El fallo no aparece al desarrollar — solo en el
ejecutable, y revienta al construir el primer botón.

Un enlace simbólico tampoco servía: se pierde al empaquetar en zip o en wheel, no
viaja bien en Windows, y según el empaquetador se sigue o se copia. Cambia un
fallo seguro por uno intermitente, que es peor de encontrar.

Incrustados no hay ruta que romper. Son 36 KB de texto, menos que una foto.
"""

from __future__ import annotations

from PySide6.QtCore import QByteArray, QSize, Qt
from PySide6.QtGui import QColor, QIcon, QPainter, QPixmap
from PySide6.QtSvg import QSvgRenderer

from ._iconos_datos import ICONOS


class IconoTamano:
    """Tamaños a los que se dibuja un icono, en píxeles.

    Tres y no una escala libre: uno a 22 junto a otro a 24 se ve descuadrado sin
    que nadie sepa decir por qué.

    **Ninguno es el objetivo táctil.** Un icono de 24 dentro de un botón sigue
    necesitando sus 48 alrededor: el dibujo es lo que se ve, no lo que se toca.
    """

    #: En línea con un texto: junto a una etiqueta, dentro de un campo.
    CHICO = 20

    #: El de siempre. Es el tamaño de la grilla, así que no se reescala.
    NORMAL = 24

    #: Protagonista: un destino del rail, un estado vacío.
    GRANDE = 32

    #: En un televisor el normal se queda corto, por la misma razón que el labio
    #: de la bandeja: se mira de lejos.
    TELEVISION = 36


#: Aire entre un icono y el texto que lo acompaña. La mitad de la unidad: con la
#: unidad entera el par se lee como dos cosas sueltas.
GAP_ICONO = 4


def svg_de(nombre: str) -> str:
    """El dibujo de un icono. Revienta claro si no existe.

    Con un `KeyError` pelado el mensaje sería solo el nombre, y quien lo lea no
    sabría si el icono no existe o si el paquete se instaló mal.
    """
    try:
        return ICONOS[nombre]
    except KeyError:
        raise KeyError(
            f"no hay un icono llamado {nombre!r}. Hay {len(ICONOS)}, "
            f"listados en doc/iconos.md"
        ) from None


def pixmap(nombre: str, color: str, tamano: int = IconoTamano.NORMAL) -> QPixmap:
    """Un icono rasterizado al tamaño pedido y teñido.

    Se rasteriza **al tamaño final** y no se escala después: escalar un trazo de
    1,75 lo deja borroso, que es justo lo que un juego de iconos de línea no
    puede permitirse.
    """
    lienzo = QPixmap(QSize(tamano, tamano))
    lienzo.fill(Qt.GlobalColor.transparent)

    pintor = QPainter(lienzo)
    pintor.setRenderHint(QPainter.RenderHint.Antialiasing)
    QSvgRenderer(QByteArray(svg_de(nombre).encode("utf-8"))).render(pintor)

    # `SourceIn` respeta el alfa del dibujo: tiñe el trazo en vez de pintar un
    # cuadrado del color encima. Con `SourceOver` saldría un bloque sólido.
    pintor.setCompositionMode(QPainter.CompositionMode.CompositionMode_SourceIn)
    pintor.fillRect(lienzo.rect(), QColor(color))
    pintor.end()

    return lienzo


def icono(nombre: str, color: str, tamano: int = IconoTamano.NORMAL) -> QIcon:
    """Un `QIcon` listo para un botón o una acción."""
    return QIcon(pixmap(nombre, color, tamano))


def disponibles() -> list[str]:
    """Los nombres de todos los iconos del juego."""
    return sorted(ICONOS)
