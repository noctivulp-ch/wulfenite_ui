#!/usr/bin/env python3
"""Comprueba que los iconos cumplen las reglas del trazo.

No es decoración: cada comprobación de aquí corresponde a un fallo que **no se
ve** hasta que es tarde. Un `<style>` que `flutter_svg` ignora en silencio deja
las formas sin trazo y rellenas de negro — y en un visor de escritorio se ven
perfectas.

    python3 iconos/validar.py
"""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

from generar import COLOR, GRILLA, TRAZO

AQUI = Path(__file__).parent
SVG = "{http://www.w3.org/2000/svg}"

#: Los que llevan relleno a propósito: detalles pequeños que a 16 px se
#: convertirían en una mancha si fueran de trazo, y los mandos del reproductor.
CON_RELLENO = {"reproducir", "pausa", "parar", "anterior", "siguiente",
               "retroceder", "adelantar"} | {
    f"triangulo-{d}-lleno" for d in ("arriba", "abajo", "izquierda", "derecha")
}


def numeros(texto: str) -> list[float]:
    return [float(n) for n in re.findall(r"-?\d+\.?\d*", texto)]


def revisar(archivo: Path) -> list[str]:
    fallos = []
    crudo = archivo.read_text(encoding="utf-8")
    nombre = archivo.stem

    # 1. Nada de CSS. Es el fallo que no se ve en un visor.
    if "<style" in crudo or 'class="' in crudo:
        fallos.append("usa <style> o clases: flutter_svg lo ignora EN SILENCIO")

    raiz = ET.fromstring(crudo)
    if raiz.get("viewBox") != f"0 0 {GRILLA} {GRILLA}":
        fallos.append(f"viewBox {raiz.get('viewBox')!r}, se espera 0 0 {GRILLA} {GRILLA}")

    dibujables = [e for e in raiz.iter() if e.tag != f"{SVG}svg"]
    if not dibujables:
        fallos.append("no dibuja nada")

    for elemento in dibujables:
        etiqueta = elemento.tag.replace(SVG, "")

        # 2. Los atributos van en CADA elemento, no heredados de un contenedor.
        if elemento.get("stroke") is None and elemento.get("fill") is None:
            fallos.append(f"<{etiqueta}> sin stroke ni fill propios")

        if elemento.get("stroke") not in (None, "none"):
            if elemento.get("stroke") != COLOR:
                fallos.append(f"<{etiqueta}> con un color que no es {COLOR}")
            # 3. Un trazo distinto rompe la armonía con el resto del juego.
            if elemento.get("stroke-width") != str(TRAZO):
                fallos.append(f"<{etiqueta}> con trazo {elemento.get('stroke-width')}")
            for punta in ("stroke-linecap", "stroke-linejoin"):
                if elemento.get(punta) != "round":
                    fallos.append(f"<{etiqueta}> sin {punta} redondeado")

        # 4. Relleno solo donde está declarado.
        if elemento.get("fill") not in (None, "none") and nombre not in CON_RELLENO:
            if etiqueta not in ("circle", "polygon"):
                fallos.append(f"<{etiqueta}> relleno sin estar en la lista")

        # 5. Nada fuera del lienzo: medio trazo por fuera y el icono sale
        #    recortado cuando la app lo escala.
        coordenadas = []
        for atributo in ("x1", "y1", "x2", "y2", "cx", "cy", "x", "y"):
            if (v := elemento.get(atributo)) is not None:
                coordenadas.append(float(v))
        if (d := elemento.get("d")) is not None:
            coordenadas += numeros(d)
        if (p := elemento.get("points")) is not None:
            coordenadas += numeros(p)
        fuera = [c for c in coordenadas if c < -0.1 or c > GRILLA + 0.1]
        if fuera:
            fallos.append(f"<{etiqueta}> se sale del lienzo: {fuera[:3]}")

    return fallos


def main() -> int:
    iconos = sorted(AQUI.glob("*.svg"))
    if not iconos:
        print("no hay iconos: corré primero generar.py")
        return 1

    malos = 0
    for archivo in iconos:
        for fallo in revisar(archivo):
            print(f"  ✗ {archivo.name}: {fallo}")
            malos += 1

    print(f"{len(iconos)} iconos, {malos} problemas")
    return 1 if malos else 0


if __name__ == "__main__":
    sys.exit(main())
