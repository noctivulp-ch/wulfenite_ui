"""El foco se ve, y se ve con el mismo canto (regla 12).

Todo lo que se puede activar tiene que decir cuándo lo tiene el foco — el de un
teclado, el de un mando, el de un conmutador de accesibilidad. Y lo dice con la
señal que el sistema ya tiene para decir "este": el canto de la regla 1, subido
un escalón.

Sin esto una app no se puede usar sin ratón. En escritorio se nota menos que en
televisión, pero es el mismo agujero: se pulsa Tab, algo se mueve, y la pantalla
no dice qué.

Los grosores viven en `tokens` —son tokens como cualquier otro, y ponerlos acá
haría que la hoja de estilo tuviera que importar de este módulo y este de ella—.
Acá queda lo que hay que calcular.
"""

from __future__ import annotations

from PySide6.QtCore import QTimer
from PySide6.QtWidgets import QWidget

from .tema import mezclar
from .tokens import SEPARACION_FOCO, Canto, Modo

__all__ = ["Canto", "SEPARACION_FOCO", "foco_inicial", "tono_enfocado"]


def tono_enfocado(pozo: str, modo: Modo) -> str:
    """El tono de una superficie que tiene el foco.

    Se aleja del reposo en la dirección que contrasta —aclarar sobre material
    oscuro, oscurecer sobre material claro—. Un tono fijo no sirve: el mismo gris
    que destaca sobre el pozo oscuro desaparece sobre el claro.
    """
    hacia = "#FFFFFF" if modo is Modo.OSCURO else "#000000"
    return mezclar(pozo, hacia, SEPARACION_FOCO)


def foco_inicial(widget: QWidget) -> None:
    """Le da el foco a [widget] cuando la ventana aparece.

    **Una pantalla sin foco inicial deja al teclado sin punto de partida**: la
    primera pulsación se gasta en que Qt elija por su cuenta, y elige el primero
    del orden de tabulación, que casi nunca es lo que se quiere hacer.

    Es una línea al final de armar la pantalla:

    ```python
    foco_inicial(self.barra)
    ```

    Se aplaza al siguiente ciclo de eventos a propósito: pedido durante la
    construcción, el widget todavía no está en una ventana y Qt lo descarta sin
    decir nada — el fallo se ve como "a veces funciona".
    """
    QTimer.singleShot(0, widget.setFocus)
