#!/usr/bin/env python3
"""Convierte los SVG a *vector drawables* de Android.

Android **no carga SVG**. Compose necesita el formato `<vector>` de Android, que
es el mismo dibujo con otra sintaxis y solo admite `<path>` — nada de `<line>`,
`<circle>` ni `<rect>`. Esta herramienta traduce las seis primitivas del juego.

Sale de aquí y no de Android Studio a propósito: importar 80 iconos a mano por la
interfaz es irrepetible y nadie lo va a rehacer cuando cambie el trazo.

    python3 iconos/a_vector_drawable.py
"""

from __future__ import annotations

import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

from generar import COLOR, GRILLA, TRAZO

AQUI = Path(__file__).parent
SALIDA = AQUI.parent / "Kotlin" / "wulf-compose" / "src" / "main" / "res" / "drawable"
SVG = "{http://www.w3.org/2000/svg}"


def _arco_de_circulo(cx: float, cy: float, r: float) -> str:
    """Un círculo como dos arcos: `<vector>` no tiene círculos."""
    return (f"M {cx - r},{cy} a {r},{r} 0 1,0 {2 * r},0 "
            f"a {r},{r} 0 1,0 {-2 * r},0 Z")


def _rectangulo_redondeado(x, y, ancho, alto, rx) -> str:
    rx = min(rx, ancho / 2, alto / 2)
    return (f"M {x + rx},{y} H {x + ancho - rx} A {rx},{rx} 0 0 1 {x + ancho},{y + rx} "
            f"V {y + alto - rx} A {rx},{rx} 0 0 1 {x + ancho - rx},{y + alto} "
            f"H {x + rx} A {rx},{rx} 0 0 1 {x},{y + alto - rx} "
            f"V {y + rx} A {rx},{rx} 0 0 1 {x + rx},{y} Z")


def _puntos_a_ruta(puntos: str, cerrar: bool) -> str:
    pares = re.findall(r"(-?\d+\.?\d*),(-?\d+\.?\d*)", puntos)
    if not pares:
        return ""
    d = "M " + " L ".join(f"{x},{y}" for x, y in pares)
    return d + (" Z" if cerrar else "")


def ruta_de(elemento: ET.Element) -> tuple[str, bool] | None:
    """Devuelve `(datos de ruta, si va relleno)` o `None` si no dibuja."""
    etiqueta = elemento.tag.replace(SVG, "")
    f = lambda a, d=0.0: float(elemento.get(a, d))  # noqa: E731
    relleno = elemento.get("fill") not in (None, "none")

    if etiqueta == "line":
        return f"M {f('x1')},{f('y1')} L {f('x2')},{f('y2')}", False
    if etiqueta == "path":
        return elemento.get("d", ""), relleno
    if etiqueta == "circle":
        return _arco_de_circulo(f("cx"), f("cy"), f("r")), relleno
    if etiqueta == "rect":
        return _rectangulo_redondeado(
            f("x"), f("y"), f("width"), f("height"), f("rx", 0)), relleno
    if etiqueta == "polyline":
        return _puntos_a_ruta(elemento.get("points", ""), cerrar=False), relleno
    if etiqueta == "polygon":
        return _puntos_a_ruta(elemento.get("points", ""), cerrar=True), relleno
    return None


def convertir(archivo: Path) -> str:
    raiz = ET.fromstring(archivo.read_text(encoding="utf-8"))
    rutas = []
    for elemento in raiz.iter():
        if elemento.tag == f"{SVG}svg":
            continue
        convertida = ruta_de(elemento)
        if convertida is None:
            continue
        datos, relleno = convertida
        if not datos:
            continue
        if relleno:
            rutas.append(f'    <path\n        android:fillColor="{COLOR}"\n'
                         f'        android:pathData="{datos}" />')
        else:
            rutas.append(f'    <path\n        android:strokeColor="{COLOR}"\n'
                         f'        android:strokeWidth="{TRAZO}"\n'
                         f'        android:strokeLineCap="round"\n'
                         f'        android:strokeLineJoin="round"\n'
                         f'        android:pathData="{datos}" />')

    cuerpo = "\n".join(rutas)
    return (f'<?xml version="1.0" encoding="utf-8"?>\n'
            f'<!-- Generado por iconos/a_vector_drawable.py. No editar a mano. -->\n'
            f'<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            f'    android:width="{GRILLA}dp"\n'
            f'    android:height="{GRILLA}dp"\n'
            f'    android:viewportWidth="{GRILLA}"\n'
            f'    android:viewportHeight="{GRILLA}">\n{cuerpo}\n</vector>\n')


def main() -> int:
    iconos = sorted(AQUI.glob("*.svg"))
    if not iconos:
        print("no hay iconos: corré primero generar.py")
        return 1

    SALIDA.mkdir(parents=True, exist_ok=True)
    for viejo in SALIDA.glob("wulf_*.xml"):
        viejo.unlink()

    for archivo in iconos:
        # Android exige minúsculas y guion bajo en los nombres de recurso, y el
        # prefijo evita chocar con los de la app que use la librería.
        nombre = "wulf_" + archivo.stem.replace("-", "_")
        (SALIDA / f"{nombre}.xml").write_text(convertir(archivo), encoding="utf-8")

    print(f"{len(iconos)} vector drawables en {SALIDA}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
