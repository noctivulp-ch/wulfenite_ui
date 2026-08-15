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

from .tema import mezclar
from .tokens import SEPARACION_FOCO, Canto, Modo

__all__ = ["Canto", "SEPARACION_FOCO", "tono_enfocado"]


def tono_enfocado(pozo: str, modo: Modo) -> str:
    """El tono de una superficie que tiene el foco.

    Se aleja del reposo en la dirección que contrasta —aclarar sobre material
    oscuro, oscurecer sobre material claro—. Un tono fijo no sirve: el mismo gris
    que destaca sobre el pozo oscuro desaparece sobre el claro.
    """
    hacia = "#FFFFFF" if modo is Modo.OSCURO else "#000000"
    return mezclar(pozo, hacia, SEPARACION_FOCO)
