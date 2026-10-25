#!/usr/bin/env python3
"""Regenera las tres capturas del README (doc/capturas/*.png) desde el banco Qt.

    python3 tools/capturas/capturas.py

Abre `PyQT/ejemplo/banco.py` con la plataforma `offscreen` de Qt: no aparece
ninguna ventana. Saca el banco en oscuro con oro, en claro con peltre, y une
claro y oscuro con jade en un corte diagonal. Necesita PySide6, Pillow y NumPy.
"""

from __future__ import annotations

import os
import sys
import tempfile
from pathlib import Path

os.environ.setdefault("QT_QPA_PLATFORM", "offscreen")

RAIZ = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(RAIZ / "PyQT" / "ejemplo"))
sys.path.insert(0, str(Path(__file__).resolve().parent))

from PySide6.QtCore import QEvent  # noqa: E402
from PySide6.QtWidgets import QApplication  # noqa: E402

import banco  # noqa: E402
from corte_diagonal import componer  # noqa: E402
from PIL import Image  # noqa: E402

SALIDA = RAIZ / "doc" / "capturas"
ANCHO, ALTO = 1280, 800


def capturar(ventana: banco.Banco, modo: str, metal: str, destino: Path) -> None:
    # Por los botones de la barra, como lo haría una persona: así también
    # quedan marcados los que corresponden.
    for grupo, rotulo in ((ventana._grupo_modo, modo), (ventana._grupo_metal, metal)):
        next(b for b in grupo.buttons() if b.text() == rotulo).click()
    # Las muestras viejas se borran con deleteLater, que fuera de app.exec()
    # no corre solo: sin forzarlo, la captura sale con las dos tandas
    # superpuestas.
    for _ in range(3):
        QApplication.sendPostedEvents(None, QEvent.Type.DeferredDelete.value)
        QApplication.processEvents()
    ventana.grab().save(str(destino))


def main() -> None:
    app = QApplication(sys.argv)
    banco.aplicar(app, banco.Acento.ORO, banco.Modo.OSCURO)
    ventana = banco.Banco()
    ventana.resize(ANCHO, ALTO)
    ventana.show()
    SALIDA.mkdir(parents=True, exist_ok=True)

    capturar(ventana, "Oscuro", "Oro", SALIDA / "banco-oscuro-oro.png")
    capturar(ventana, "Claro", "Peltre", SALIDA / "banco-claro-peltre.png")
    with tempfile.TemporaryDirectory() as tmp:
        claro, oscuro = Path(tmp, "claro.png"), Path(tmp, "oscuro.png")
        capturar(ventana, "Claro", "Jade", claro)
        capturar(ventana, "Oscuro", "Jade", oscuro)
        componer(Image.open(claro), Image.open(oscuro), 0.035, 3).save(
            SALIDA / "banco-claro-oscuro.png", optimize=True
        )
    print("\n".join(sorted(p.name for p in SALIDA.glob("*.png"))))


if __name__ == "__main__":
    main()
