"""Banco de estilo de Wulfenite-Qt — el ejemplo, y la validación.

    python ejemplo/banco.py

No es un "hola mundo": es donde se decide un acabado **mirándolo**, no
imaginándolo. Muestra los tres metales por las dos lacas, el relieve en sus tres
tratamientos, pozos de distintos tamaños y la retícula recolocándose al
redimensionar la ventana.

Sirve para dos cosas a la vez, y por eso es el ejemplo:

- **Valida la librería.** Está escrito importando solo `wulf_qt`. Lo que
  tuviera que resolver por su cuenta sería exactamente lo que le falta al
  paquete.
- **La enseña.** Se toca y se ve, en vez de leer una lista de constantes.

El equivalente en Flutter vive en el paquete Dart, y sirve para comparar los dos
lados uno al lado del otro — que es la única forma honesta de decir si la
adaptación quedó fiel.
"""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

from PySide6.QtCore import Qt  # noqa: E402
from PySide6.QtWidgets import (  # noqa: E402
    QApplication,
    QButtonGroup,
    QHBoxLayout,
    QLabel,
    QLineEdit,
    QMainWindow,
    QPushButton,
    QScrollArea,
    QVBoxLayout,
    QWidget,
)

from wulf_qt import (  # noqa: E402
    GAP_CELDA,
    LACAS,
    METALES,
    PADDING_CELDA_GRANDE,
    PARED,
    Acciones,
    Acento,
    Atenuado,
    Bandeja,
    Celda,
    Cuadricula,
    Enlace,
    EtiquetaAjustada,
    Modo,
    Pozo,
    Tallado,
    boton,
    boton_primario,
    canto_del_pozo,
    contraste,
    aplicar,
    color_tallado,
)


class Banco(QMainWindow):
    """La ventana del banco: barra de acabado arriba, muestras debajo."""

    def __init__(self) -> None:
        super().__init__()
        self.setWindowTitle("Wulfenite-Qt — banco de estilo")
        self.resize(1280, 900)
        self._acento = Acento.ORO
        self._modo = Modo.OSCURO
        self._construir()

    def _construir(self) -> None:
        raiz = Bandeja()
        raiz.agregar(self._barra())

        self._muestras = QWidget()
        self._columna = QVBoxLayout(self._muestras)
        self._columna.setContentsMargins(0, 0, 0, 0)
        self._columna.setSpacing(PARED)

        desplazable = QScrollArea()
        desplazable.setWidgetResizable(True)
        desplazable.setFrameShape(QScrollArea.Shape.NoFrame)
        desplazable.setWidget(self._muestras)
        raiz.agregar(desplazable)

        self.setCentralWidget(raiz)
        self._repintar()

    def _barra(self) -> QWidget:
        fila = QWidget()
        caja = QHBoxLayout(fila)
        caja.setContentsMargins(0, 0, 0, 0)
        caja.setSpacing(GAP_CELDA)

        self._grupo_modo = QButtonGroup(self)
        for modo in Modo:
            b = QPushButton(modo.value.capitalize())
            b.setCheckable(True)
            b.setChecked(modo is self._modo)
            b.clicked.connect(lambda _, m=modo: self._cambiar(modo=m))
            self._grupo_modo.addButton(b)
            caja.addWidget(b)

        caja.addSpacing(GAP_CELDA)

        self._grupo_metal = QButtonGroup(self)
        for acento in Acento:
            b = QPushButton(acento.value.capitalize())
            b.setCheckable(True)
            b.setChecked(acento is self._acento)
            b.clicked.connect(lambda _, a=acento: self._cambiar(acento=a))
            self._grupo_metal.addButton(b)
            caja.addWidget(b)

        caja.addStretch(1)
        caja.addWidget(Enlace("Las reglas", "https://example.invalid/reglas"))
        return fila

    def _cambiar(self, *, acento: Acento | None = None, modo: Modo | None = None) -> None:
        if acento is not None:
            self._acento = acento
        if modo is not None:
            self._modo = modo
        aplicar(QApplication.instance(), self._acento, self._modo)
        self._repintar()

    def _repintar(self) -> None:
        while self._columna.count():
            elemento = self._columna.takeAt(0)
            if elemento.widget():
                elemento.widget().deleteLater()

        laca = LACAS[self._modo]
        metal = METALES[self._acento][self._modo]
        modo = self._modo.value

        self._columna.addWidget(self._seccion_relieve(laca, modo))
        self._columna.addWidget(self._seccion_formas(laca))
        self._columna.addWidget(self._seccion_acciones())
        self._columna.addWidget(self._seccion_numeros(laca, metal))
        self._columna.addStretch(1)

    def _seccion_relieve(self, laca, modo: str) -> QWidget:
        rejilla = Cuadricula()
        celdas = []
        for titulo, realzado in (("plano", None), ("tallado", False), ("realzado", True)):
            pozo = Pozo(padding=PADDING_CELDA_GRANDE)
            pozo.agregar(Atenuado(titulo))

            monto = Tallado(
                "₡3 800",
                sobre=laca.pozo,
                realzado=bool(realzado),
                profundidad=0 if realzado is None else 1.25,
            )
            fuente = monto.font()
            fuente.setPointSize(28)
            monto.setFont(fuente)
            # El glifo grabado gasta contraste a propósito: se acerca al color de
            # su superficie. Por eso el nivel "marcado" solo va en texto grande.
            monto.setStyleSheet(
                f"color: {color_tallado(laca.texto_en_pozo, laca.pozo, nivel='marcado', modo=modo)};"
            )
            pozo.agregar(monto)
            pozo.agregar(QLabel("Casado con chuleta"))
            celdas.append(Celda(pozo, compacto=6, medio=2, amplio=2))
        rejilla.poner(celdas)
        return self._envuelto("Relieve — tres tratamientos", rejilla)

    def _seccion_formas(self, laca) -> QWidget:
        rejilla = Cuadricula()
        hero = Pozo(padding=PADDING_CELDA_GRANDE)
        hero.agregar(Atenuado("hero — 3 de 6 columnas"))
        total = Tallado("₡128 400", sobre=laca.pozo)
        fuente = total.font()
        fuente.setPointSize(34)
        total.setFont(fuente)
        hero.agregar(total)

        celdas = [Celda(hero, compacto=6, medio=6, amplio=3)]
        for etiqueta in ("Primer turno", "Segundo turno", "Optimización"):
            pozo = Pozo()
            # La etiqueta encoge antes que partirse: "Optimización" no entra en
            # una celda estrecha y Qt la cortaría a mitad de palabra.
            pozo.agregar(EtiquetaAjustada(etiqueta))
            celdas.append(Celda(pozo, compacto=2, medio=2, amplio=1))

        seleccionado = Pozo(QLabel("seleccionado"), seleccionado=True)
        celdas.append(Celda(seleccionado, compacto=6, medio=6, amplio=6))
        rejilla.poner(celdas)
        return self._envuelto("Formas y retícula — redimensioná la ventana", rejilla)

    def _seccion_acciones(self) -> QWidget:
        pozo = Pozo(padding=PADDING_CELDA_GRANDE)
        pozo.agregar(
            Atenuado(
                "Confirmar a la derecha, cancelar a su izquierda, y lo destructivo "
                "abajo a la izquierda — lejos de donde llega el pulgar a guardar."
            )
        )
        campo = QLineEdit()
        campo.setPlaceholderText("Nombre del producto")
        pozo.agregar(campo)
        pozo.agregar(
            Acciones(
                confirmar=boton_primario("Guardar"),
                cancelar=boton("Cancelar"),
                extras=[boton("Eliminar")],
            )
        )
        return self._envuelto("Acciones", pozo)

    def _seccion_numeros(self, laca, metal: str) -> QWidget:
        pozo = Pozo(padding=PADDING_CELDA_GRANDE)
        canto = canto_del_pozo(self._acento, self._modo)
        medidas = [
            ("texto en pozo", laca.texto_en_pozo, laca.pozo, 4.5),
            ("texto en bandeja", laca.texto_en_bandeja, laca.bandeja, 4.5),
            ("enlace sobre pozo", metal, laca.pozo, 4.5),
            ("canto contra bandeja", canto, laca.bandeja, 3.0),
        ]
        for nombre, frente, fondo, piso in medidas:
            ratio = contraste(frente, fondo)
            estado = "cumple" if ratio >= piso else "POR DEBAJO"
            fila = QLabel(f"{nombre}: {ratio:.2f}:1 — {estado} de {piso}:1")
            fila.setTextInteractionFlags(Qt.TextInteractionFlag.TextSelectableByMouse)
            pozo.agregar(fila)
        return self._envuelto("Contraste medido, no estimado", pozo)

    def _envuelto(self, titulo: str, contenido: QWidget) -> QWidget:
        caja = QWidget()
        columna = QVBoxLayout(caja)
        columna.setContentsMargins(0, 0, 0, 0)
        columna.setSpacing(PARED)
        rotulo = QLabel(titulo)
        fuente = rotulo.font()
        fuente.setPointSizeF(fuente.pointSizeF() * 1.2)
        fuente.setWeight(fuente.Weight.DemiBold)
        rotulo.setFont(fuente)
        columna.addWidget(rotulo)
        columna.addWidget(contenido)
        return caja


def main() -> None:
    app = QApplication(sys.argv)
    aplicar(app, Acento.ORO, Modo.OSCURO)
    ventana = Banco()
    ventana.show()
    sys.exit(app.exec())


if __name__ == "__main__":
    main()
