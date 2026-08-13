"""Los dos paquetes dicen los mismos números.

Wulfenite existe en Dart y en Python, y las dos copias de la paleta son la
manera clásica de que un sistema de diseño se rompa: alguien ajusta un color en
un lado, el otro se queda viejo, y meses después las dos apps de la misma marca
ya no se parecen.

Esta prueba lee el **archivo Dart** y compara. Si el paquete Dart no está a mano
—porque se instaló este solo desde PyPI— se salta con un motivo claro: la
comprobación tiene sentido en la máquina donde se desarrollan los dos, no en la
de quien solo usa uno.

Ya cazó un fallo real: al portar, el canto del pozo se apagaba hacia la bandeja
en vez de hacia el pozo, y eso lo dejaba por debajo de 3:1 contra la bandeja —
justo lo que el canto existe para cumplir.
"""

from __future__ import annotations

import os
import re
from pathlib import Path

import pytest

from wulf_qt import APAGADO_RIBETE, LACAS, METALES, SOBRE_METAL, Acento, Modo

#: Dónde buscar el paquete Dart. La variable de entorno gana, para poder
#: correrlo desde otro sitio sin tocar el archivo.
_CANDIDATOS = [
    os.environ.get("WULFENITE_FLUTTER", ""),
    str(Path(__file__).resolve().parents[2] / "flutter"),
]


def _tema_dart() -> str:
    for ruta in _CANDIDATOS:
        if not ruta:
            continue
        archivo = Path(ruta) / "lib/src/tokens/tema.dart"
        if archivo.exists():
            return archivo.read_text(encoding="utf-8")
    pytest.skip(
        "No se encontró el paquete Dart. Poné WULFENITE_FLUTTER apuntando a él "
        "para comprobar que las dos paletas siguen diciendo lo mismo."
    )
    raise AssertionError  # pragma: no cover — `skip` no vuelve


def _colores(fuente: str) -> dict[str, str]:
    """Todas las constantes `const _nombre = Color(0xFFRRGGBB);` del archivo."""
    patron = re.compile(r"const\s+(_\w+)\s*=\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\)")
    return {n: f"#{v.upper()}" for n, v in patron.findall(fuente)}


EQUIVALENCIAS = {
    "_bandejaOscura": (Modo.OSCURO, "bandeja"),
    "_pozoOscuro": (Modo.OSCURO, "pozo"),
    "_pozoContactoOscuro": (Modo.OSCURO, "pozo_contacto"),
    "_textoEnPozoOscuro": (Modo.OSCURO, "texto_en_pozo"),
    "_textoSecundarioEnPozoOscuro": (Modo.OSCURO, "texto_secundario_en_pozo"),
    "_textoEnBandejaOscura": (Modo.OSCURO, "texto_en_bandeja"),
    "_textoAtenuadoEnBandejaOscura": (Modo.OSCURO, "texto_atenuado_en_bandeja"),
    "_bandejaClara": (Modo.CLARO, "bandeja"),
    "_pozoClaro": (Modo.CLARO, "pozo"),
    "_pozoContactoClaro": (Modo.CLARO, "pozo_contacto"),
    "_textoEnPozoClaro": (Modo.CLARO, "texto_en_pozo"),
    "_textoSecundarioEnPozoClaro": (Modo.CLARO, "texto_secundario_en_pozo"),
    "_textoEnBandejaClara": (Modo.CLARO, "texto_en_bandeja"),
    "_textoAtenuadoEnBandejaClara": (Modo.CLARO, "texto_atenuado_en_bandeja"),
    "_rojoErrorOscuro": (Modo.OSCURO, "error"),
    "_sobreErrorOscuro": (Modo.OSCURO, "sobre_error"),
    "_rojoErrorClaro": (Modo.CLARO, "error"),
    "_sobreErrorClaro": (Modo.CLARO, "sobre_error"),
}


def test_las_lacas_coinciden() -> None:
    dart = _colores(_tema_dart())
    faltan = [n for n in EQUIVALENCIAS if n not in dart]
    assert not faltan, f"el archivo Dart ya no define: {faltan} — ¿se renombraron?"

    for nombre, (modo, campo) in EQUIVALENCIAS.items():
        assert getattr(LACAS[modo], campo) == dart[nombre], (
            f"{campo} de la laca {modo.value}: Python dice "
            f"{getattr(LACAS[modo], campo)} y Dart {dart[nombre]}"
        )


def test_los_metales_coinciden() -> None:
    fuente = _tema_dart()
    patron = re.compile(
        r"WulfAcento\.(\w+):\s*\(claro:\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\),"
        r"\s*oscuro:\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\)\)"
    )
    encontrados = patron.findall(fuente)
    assert len(encontrados) == len(Acento), "el Dart no declara los tres metales"

    for nombre, claro, oscuro in encontrados:
        acento = Acento[nombre.upper()]
        assert METALES[acento][Modo.CLARO] == f"#{claro.upper()}"
        assert METALES[acento][Modo.OSCURO] == f"#{oscuro.upper()}"


def test_el_apagado_del_canto_coincide() -> None:
    fuente = _tema_dart()
    claro = float(re.search(r"_apagadoRibeteClaro\s*=\s*([\d.]+)", fuente).group(1))
    oscuro = float(re.search(r"_apagadoRibeteOscuro\s*=\s*([\d.]+)", fuente).group(1))
    assert APAGADO_RIBETE[Modo.CLARO] == claro
    assert APAGADO_RIBETE[Modo.OSCURO] == oscuro


def test_lo_que_va_encima_del_metal_coincide() -> None:
    fuente = _tema_dart()
    claro = re.search(r"_sobreMetalClaro\s*=\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\)", fuente)
    for acento in Acento:
        assert SOBRE_METAL[acento][Modo.CLARO] == f"#{claro.group(1).upper()}"

    oscuros = re.findall(
        r"WulfAcento\.(\w+):\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\)", fuente
    )
    for nombre, valor in oscuros:
        assert SOBRE_METAL[Acento[nombre.upper()]][Modo.OSCURO] == f"#{valor.upper()}"


def test_el_espaciado_coincide() -> None:
    """La unidad base y sus múltiplos son los mismos en los dos paquetes."""
    from wulf_qt import PARED, RADIO_BANDEJA, RADIO_POZO, RIM, UNIDAD

    fuente = (Path(_CANDIDATOS[1]) / "lib/src/tokens/tema.dart")
    if not fuente.exists():
        pytest.skip("sin el paquete Dart a mano")
    texto = fuente.read_text(encoding="utf-8")

    assert re.search(r"double unidad = 8\b", texto), "la unidad base cambió en Dart"
    assert UNIDAD == 8
    assert PARED == 12 and re.search(r"double pared = unidad \* 1\.5", texto)
    assert RIM == 24 and re.search(r"double rim = unidad \* 3", texto)
    assert RADIO_POZO == 4 and re.search(r"double pozo = 4", texto)
    assert RADIO_BANDEJA == 20 and re.search(r"double bandeja = 20", texto)
