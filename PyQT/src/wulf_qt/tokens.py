"""Los valores del sistema: espaciado, radios, lacas y metales.

Son **los mismos números** que el paquete Dart `wulf_flutter`. No es una
coincidencia que haya que sostener a mano: `tests/test_paridad_con_dart.py` lee
el archivo Dart y falla si alguno se separa.

Nada de esto depende de Qt — se puede importar sin tener una aplicación viva,
que es lo que permite comprobar el contraste en una prueba sin abrir ventana.
"""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum

# ── Espaciado ─────────────────────────────────────────────────────────────────

#: La unidad base. Todo lo demás es un múltiplo suyo; un valor que no lo sea
#: rompe el ritmo aunque a ojo no se note.
UNIDAD = 8

#: Separación entre elementos emparentados dentro de un mismo pozo.
GAP_CELDA = UNIDAD * 2  # 16

#: Separación entre grupos distintos de contenido.
GAP_GRUPO = UNIDAD * 3  # 24

#: Aire interior de un pozo normal.
PADDING_CELDA = UNIDAD * 2  # 16

#: Aire interior de un pozo protagonista, del que se mira primero.
PADDING_CELDA_GRANDE = UNIDAD * 3  # 24

#: Ancho de la pared compartida entre dos pozos vecinos.
#:
#: NO es "el fondo asomándose": es material de bandeja, así que dos pozos
#: adyacentes comparten UNA sola pared de este ancho, no dos márgenes.
PARED = int(UNIDAD * 1.5)  # 12

#: Borde exterior de la bandeja. Más grueso que la pared interior, como el labio
#: de una bandeja de verdad.
RIM = UNIDAD * 3  # 24

# ── Radios ────────────────────────────────────────────────────────────────────

#: Interior rectilíneo: los compartimentos de una bandeja no son rectángulos
#: redondeados sueltos.
RADIO_POZO = 4

#: Exterior suave, como el canto de la bandeja.
RADIO_BANDEJA = 20

#: Objetivo táctil mínimo. Es una constante física, no una medida de pantalla:
#: no escala con nada.
OBJETIVO_TACTIL_MIN = 48


class Modo(Enum):
    """Luminosidad del acabado."""

    CLARO = "claro"
    OSCURO = "oscuro"


class Acento(Enum):
    """El metal. Es ortogonal al modo: los tres van con las dos lacas."""

    ORO = "oro"
    JADE = "jade"
    PELTRE = "peltre"


@dataclass(frozen=True)
class Laca:
    """Un acabado completo, en hexadecimal.

    Los nombres son por **rol**, no por color: `bandeja` es el plano continuo y
    `pozo` la superficie donde vive el contenido, digan lo que digan sus tonos.
    Es lo que permite cambiar de laca sin tocar una sola pantalla.
    """

    #: El plano continuo: fondo de ventana y material de las paredes.
    bandeja: str
    #: Fondo del compartimento, donde vive el contenido.
    pozo: str
    #: Sombra que la pared proyecta hacia dentro del pozo, en su borde superior.
    pozo_contacto: str
    #: Texto principal sobre el pozo.
    texto_en_pozo: str
    #: Texto secundario sobre el pozo.
    texto_secundario_en_pozo: str
    #: Texto principal sobre la bandeja — el cromo, no el contenido.
    texto_en_bandeja: str
    #: Texto secundario sobre la bandeja.
    texto_atenuado_en_bandeja: str
    #: Rojo de error.
    error: str
    #: Lo que se lee encima del rojo de error.
    sobre_error: str


#: Los dos acabados. Salen de cómo se construyen de verdad las cajas de laca:
#: negras por fuera y rojas por dentro. El claro es su reflejo.
LACAS: dict[Modo, Laca] = {
    Modo.OSCURO: Laca(
        bandeja="#14100E",
        pozo="#3A1E1B",
        pozo_contacto="#2D110E",
        texto_en_pozo="#F5EAE2",  # 12.85:1
        texto_secundario_en_pozo="#CBA99C",  # 7.02:1
        texto_en_bandeja="#EDE4DC",  # 15.07:1
        texto_atenuado_en_bandeja="#A99C93",  # 7.08:1
        error="#F0A08C",
        sobre_error="#2A0D07",
    ),
    Modo.CLARO: Laca(
        bandeja="#F8F7F2",
        pozo="#D7E1D3",
        pozo_contacto="#C4D2BF",
        texto_en_pozo="#1E2A1C",  # 11.12:1
        texto_secundario_en_pozo="#495747",  # 5.70:1
        texto_en_bandeja="#232522",  # 14.40:1
        texto_atenuado_en_bandeja="#55584F",  # 6.76:1
        error="#9E2F1B",
        sobre_error="#FFFFFF",
    ),
}

#: El metal de cada acento, por modo. Es el único acento del sistema y solo
#: marca acción primaria y selección: si estuviera en todas partes, no
#: destacaría en ninguna.
METALES: dict[Acento, dict[Modo, str]] = {
    Acento.ORO: {Modo.CLARO: "#7A4F0E", Modo.OSCURO: "#D9A54A"},
    Acento.JADE: {Modo.CLARO: "#156353", Modo.OSCURO: "#5FCBAB"},
    Acento.PELTRE: {Modo.CLARO: "#4F5450", Modo.OSCURO: "#C6C4BC"},
}

#: Lo que se lee encima del metal.
SOBRE_METAL: dict[Acento, dict[Modo, str]] = {
    Acento.ORO: {Modo.CLARO: "#FFFFFF", Modo.OSCURO: "#241A0C"},
    Acento.JADE: {Modo.CLARO: "#FFFFFF", Modo.OSCURO: "#06231C"},
    Acento.PELTRE: {Modo.CLARO: "#FFFFFF", Modo.OSCURO: "#1E1D1A"},
}

#: Cuánto se apaga el metal para hacer de canto del pozo.
#:
#: El canto es **metal apagado**, no un gris fijo: en una pieza de laca real el
#: pan de metal ribetea los cantos. Con un tono fijo, elegir peltre dejaba el
#: elemento seleccionado en plata y todos los demás bordes leyéndose dorados.
APAGADO_RIBETE: dict[Modo, float] = {Modo.CLARO: 0.40, Modo.OSCURO: 0.50}


def mezclar(a: str, b: str, t: float) -> str:
    """Interpola dos colores hexadecimales. `t=0` da [a], `t=1` da [b]."""
    ra, ga, ba = _rgb(a)
    rb, gb, bb = _rgb(b)
    return "#{:02X}{:02X}{:02X}".format(
        round(ra + (rb - ra) * t),
        round(ga + (gb - ga) * t),
        round(ba + (bb - ba) * t),
    )


def canto_del_pozo(acento: Acento, modo: Modo) -> str:
    """El ribete de 1 px del pozo: el metal apagado contra la bandeja.

    Es la señal **primaria** de que hay un compartimento tallado, y la única que
    funciona en modo oscuro: entre dos superficies oscuras el salto de valor no
    puede llegar a 3:1 porque el rango de luminancia está comprimido.
    """
    # Se apaga hacia el POZO y no hacia la bandeja: el canto es el labio del
    # hueco, así que su tono sale de lo que hay dentro. Apagarlo hacia la
    # bandeja lo dejaba por debajo de 3:1 contra ella —lo cazó la prueba de
    # contraste al portar el paquete—, que es justo lo que el canto existe para
    # cumplir.
    return mezclar(METALES[acento][modo], LACAS[modo].pozo, APAGADO_RIBETE[modo])


def contraste(a: str, b: str) -> float:
    """Razón de contraste WCAG entre dos colores hexadecimales.

    Se implementa acá y no se importa de ninguna parte porque es la fórmula que
    decide si el sistema es legible, y una dependencia externa para cuatro
    líneas es una dependencia que algún día no compila.
    """
    la, lb = _luminancia(a), _luminancia(b)
    alto, bajo = max(la, lb), min(la, lb)
    return (alto + 0.05) / (bajo + 0.05)


def _rgb(hexadecimal: str) -> tuple[int, int, int]:
    h = hexadecimal.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


def _luminancia(hexadecimal: str) -> float:
    def canal(v: int) -> float:
        c = v / 255
        return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4

    r, g, b = _rgb(hexadecimal)
    return 0.2126 * canal(r) + 0.7152 * canal(g) + 0.0722 * canal(b)


class Canto:
    """Grosor del canto según el estado, en píxeles. Es la regla 12 en números.

    El foco NO estrena una señal propia: usa la que el sistema ya tiene para
    decir "este", el canto de la regla 1, subida un escalón más. Inventarle un
    halo o una sombra sería contradecir la regla 1 justo donde más se nota.

    | estado | canto | relleno |
    |---|---|---|
    | reposo | cresta (metal apagado), 1 | pozo |
    | seleccionado | metal pleno, 2 | pozo |
    | enfocado | metal pleno, 3 | pozo aclarado |

    Que enfocado lleve **además** el relleno separado es lo que lo distingue de
    seleccionado cuando los dos caen a la vez, que es el caso normal recorriendo
    una lista con el teclado: se recorre la selección.
    """

    #: En reposo. La cresta, que es material de bandeja.
    REPOSO = 1

    #: Seleccionado: metal pleno.
    SELECCIONADO = 2

    #: Enfocado por teclado o mando.
    #:
    #: Tres y no dos: el escalón que en la mano se distingue perfectamente, a
    #: distancia de televisor no existe. Mismo motivo por el que el objetivo
    #: táctil es de 48 y no de 32.
    ENFOCADO = 3


#: Cuánto se aclara el pozo enfocado. Coincide a propósito con la separación de
#: la onda del toque de las gemelas: el aparato dice "acá estás" con el mismo
#: material se llegue con el dedo, con el mando o con el teclado.
SEPARACION_FOCO = 0.10


#: El labio de la bandeja en un televisor, que es el doble.
#:
#: Existe por paridad con las gemelas y **no se usa en escritorio**: el overscan
#: es cosa de televisores, y un monitor no recorta la imagen. Está acá para que
#: los tres paquetes digan los mismos números y para el día en que alguien monte
#: una interfaz Qt en una tele.
RIM_TELEVISION = UNIDAD * 6
