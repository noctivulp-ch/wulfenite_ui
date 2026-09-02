#!/usr/bin/env python3
"""Incrusta los SVG como datos del paquete de Python.

**Por qué incrustar y no leer del disco.** El paquete leía los iconos de una
carpeta hermana, y eso se rompe en cuanto el programa se empaqueta: Nuitka en
modo *onefile*, PyInstaller y un wheel instalado dejan esa ruta apuntando fuera
del bundle. El fallo no aparece al desarrollar — solo en el ejecutable, y revienta
al construir el primer botón.

**Por qué no un enlace simbólico**, que era la otra salida: los enlaces se pierden
al empaquetar en zip o en wheel, no viajan bien en Windows, y varios
empaquetadores los siguen o los copian según la versión. Cambia un fallo seguro
por uno intermitente, que es peor.

Incrustados no hay ruta que romper: el juego entero son 33 KB de texto, menos que
una foto, y viaja como código.

    python3 iconos/a_modulo_python.py
"""

from __future__ import annotations

import sys
from pathlib import Path

AQUI = Path(__file__).parent
SALIDA = AQUI.parent / "PyQT" / "src" / "wulf_qt" / "_iconos_datos.py"

CABECERA = '''"""Los iconos del juego, incrustados como texto.

**Generado por `iconos/a_modulo_python.py`. No editar a mano.**

Van incrustados y no como archivos sueltos porque una ruta al disco se rompe en
cuanto el programa se empaqueta —Nuitka en *onefile*, PyInstaller, un wheel
instalado— y el fallo solo aparece en el ejecutable. Como código, no hay ruta que
romper.

Para cambiar un icono se toca `iconos/generar.py` y se vuelve a correr este
guion; este archivo es un artefacto.
"""

from __future__ import annotations

#: Nombre del icono a su SVG. Las claves son las de `doc/iconos.md`.
ICONOS: dict[str, str] = {
'''


def main() -> int:
    iconos = sorted(AQUI.glob("*.svg"))
    if not iconos:
        print("no hay iconos: corré primero generar.py")
        return 1

    lineas = [CABECERA]
    for archivo in iconos:
        svg = archivo.read_text(encoding="utf-8").strip()
        lineas.append(f"    {archivo.stem!r}: {svg!r},\n")
    lineas.append("}\n")

    SALIDA.write_text("".join(lineas), encoding="utf-8")
    peso = SALIDA.stat().st_size / 1024
    print(f"{len(iconos)} iconos incrustados en {SALIDA.name} ({peso:.0f} KB)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
