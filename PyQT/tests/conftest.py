"""Una sola `QApplication` para toda la sesión, y sin abrir ventanas.

Qt solo admite una aplicación por proceso, así que crearla por prueba revienta.
Y `offscreen` permite medir geometría de verdad —dónde acaba cada botón— sin que
haga falta un servidor gráfico: es lo que hace que estas pruebas corran igual en
la máquina de uno y en un servidor de integración.
"""

from __future__ import annotations

import os
import sys

import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "src"))
os.environ.setdefault("QT_QPA_PLATFORM", "offscreen")

from PySide6.QtWidgets import QApplication  # noqa: E402


@pytest.fixture(scope="session")
def app() -> QApplication:
    return QApplication.instance() or QApplication([])
