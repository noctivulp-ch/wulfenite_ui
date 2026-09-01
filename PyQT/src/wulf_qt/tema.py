"""Aplica el sistema a una aplicación Qt: paleta y hoja de estilo.

Dos capas, y las dos hacen falta:

- **La paleta** (`QPalette`) es lo que consultan los widgets que se pintan
  solos y los diálogos nativos. Sin ella, un `QMessageBox` sale con los colores
  del escritorio y rompe el mundo.
- **La hoja de estilo** (QSS) es lo que da la forma: cantos de un píxel, radios,
  el degradado corto del contacto dentro del pozo.

Se fuerza el estilo **Fusion** antes de nada. Sin él, cada escritorio dibuja los
widgets a su manera y la mitad de las reglas de la hoja de estilo se ignoran —
el mismo código se vería distinto en GNOME, en KDE y en Windows.
"""

from __future__ import annotations

from PySide6.QtGui import QColor, QPalette
from PySide6.QtWidgets import QApplication

from .tokens import (
    SEPARACION_FOCO,
    Canto,
    LACAS,
    METALES,
    PADDING_CELDA,
    RADIO_BANDEJA,
    RADIO_POZO,
    SOBRE_METAL,
    UNIDAD,
    Acento,
    Laca,
    Modo,
    canto_del_pozo,
    mezclar,
)

#: Clase de estilo del compartimento. Se pone con `setProperty("class", POZO)`
#: para que la hoja de estilo lo alcance sin tener que subclasear.
POZO = "wulf-pozo"

#: Clase del plano continuo.
BANDEJA = "wulf-bandeja"

#: Clase del texto secundario, el que apoya sin competir.
ATENUADO = "wulf-atenuado"


def aplicar(app: QApplication, acento: Acento = Acento.ORO, modo: Modo = Modo.OSCURO) -> None:
    """Viste [app] entera con el acabado pedido.

    Se llama una vez al arrancar, y otra vez cada vez que el usuario cambie de
    metal o de modo:

    ```python
    app = QApplication(sys.argv)
    aplicar(app, Acento.ORO, Modo.OSCURO)
    ```
    """
    app.setStyle("Fusion")
    app.setPalette(paleta(acento, modo))
    app.setStyleSheet(hoja_de_estilo(acento, modo))


def paleta(acento: Acento, modo: Modo) -> QPalette:
    """La `QPalette` del acabado.

    El mapeo tiene una decisión deliberada: `Base` y `Window` **no** son lo
    mismo. `Window` es la bandeja y `Base` es el pozo, que es donde cae el
    contenido — así el texto por defecto de cualquier widget queda legible sobre
    el fondo correcto sin que haya que vestirlo a mano.
    """
    laca = LACAS[modo]
    metal = METALES[acento][modo]
    p = QPalette()

    p.setColor(QPalette.ColorRole.Window, QColor(laca.bandeja))
    p.setColor(QPalette.ColorRole.WindowText, QColor(laca.texto_en_bandeja))
    p.setColor(QPalette.ColorRole.Base, QColor(laca.pozo))
    p.setColor(QPalette.ColorRole.AlternateBase, QColor(laca.pozo_contacto))
    p.setColor(QPalette.ColorRole.Text, QColor(laca.texto_en_pozo))
    p.setColor(QPalette.ColorRole.PlaceholderText, QColor(laca.texto_secundario_en_pozo))
    p.setColor(QPalette.ColorRole.Button, QColor(laca.pozo))
    p.setColor(QPalette.ColorRole.ButtonText, QColor(laca.texto_en_pozo))
    p.setColor(QPalette.ColorRole.Highlight, QColor(metal))
    p.setColor(QPalette.ColorRole.HighlightedText, QColor(SOBRE_METAL[acento][modo]))
    p.setColor(QPalette.ColorRole.Link, QColor(metal))
    p.setColor(QPalette.ColorRole.ToolTipBase, QColor(laca.pozo))
    p.setColor(QPalette.ColorRole.ToolTipText, QColor(laca.texto_en_pozo))

    # Deshabilitado: el texto se acerca a su fondo en vez de irse a un gris
    # cualquiera, que sobre laca oscura se leería como otro color más.
    apagado = QPalette.ColorGroup.Disabled
    p.setColor(apagado, QPalette.ColorRole.Text, QColor(laca.texto_secundario_en_pozo))
    p.setColor(apagado, QPalette.ColorRole.ButtonText, QColor(laca.texto_secundario_en_pozo))
    p.setColor(apagado, QPalette.ColorRole.WindowText, QColor(laca.texto_atenuado_en_bandeja))
    return p


def hoja_de_estilo(acento: Acento, modo: Modo) -> str:
    """El QSS del acabado.

    Se devuelve en vez de aplicarse para poder concatenarle el de la app:

    ```python
    app.setStyleSheet(hoja_de_estilo(acento, modo) + mi_qss)
    ```
    """
    laca: Laca = LACAS[modo]
    metal = METALES[acento][modo]
    canto = canto_del_pozo(acento, modo)
    sobre_metal = SOBRE_METAL[acento][modo]
    # El metal al pasar por encima y al pulsar: hacia el texto y hacia el fondo.
    metal_hover = mezclar(metal, laca.texto_en_pozo, 0.15)
    metal_pulsado = mezclar(metal, laca.bandeja, 0.20)
    # El pozo cuando tiene el foco (regla 12): separado de su reposo en la
    # dirección que contrasta. Se calcula acá y no se importa de `foco` para no
    # cerrar un ciclo — ese módulo importa `mezclar` de este.
    pozo_enfocado = mezclar(
        laca.pozo, "#FFFFFF" if modo is Modo.OSCURO else "#000000", SEPARACION_FOCO
    )

    from .aviso import qss_del_aviso
    from .barra import qss_de_las_barras
    from .componentes import qss_del_rail

    return qss_del_aviso(laca.pozo, metal, laca.texto_en_pozo) + qss_de_las_barras(
        laca.pozo, pozo_enfocado, metal, canto
    ) + qss_del_rail(
        laca.bandeja, laca.texto_en_bandeja, laca.texto_atenuado_en_bandeja,
        metal, pozo_enfocado,
    ) + f"""
/* ── La bandeja: un plano continuo, sin nada flotando encima ─────────────── */
QMainWindow, QDialog {{
    background-color: {laca.bandeja};
    color: {laca.texto_en_bandeja};
}}
QWidget {{
    color: {laca.texto_en_bandeja};
}}
QWidget[class="{BANDEJA}"] {{
    background-color: {laca.bandeja};
    border-radius: {RADIO_BANDEJA}px;
}}
QLabel[class="{ATENUADO}"] {{
    color: {laca.texto_atenuado_en_bandeja};
}}

/* ── El pozo: hundido, con canto de 1px y el contacto arriba ─────────────────
   Sin sombra proyectada. El canto es la señal primaria y el degradado corto
   ocupa solo la primera franja: es el borde de la pared, no un adorno de
   arriba a abajo. */
QFrame[class="{POZO}"], QWidget[class="{POZO}"] {{
    background: qlineargradient(x1:0, y1:0, x2:0, y2:0.07,
                                stop:0 {laca.pozo_contacto}, stop:1 {laca.pozo});
    border: 1px solid {canto};
    border-radius: {RADIO_POZO}px;
    color: {laca.texto_en_pozo};
}}
QFrame[class="{POZO}"][seleccionado="true"] {{
    border: {Canto.SELECCIONADO}px solid {metal};
}}
/* Enfocado gana sobre seleccionado: recorriendo con el teclado se RECORRE la
   selección, así que lo que hay que ver moverse es el foco. Por eso lleva
   además el relleno aclarado — los dos estados caen juntos todo el tiempo. */
QFrame[class="{POZO}"]:focus, QWidget[class="{POZO}"]:focus {{
    border: {Canto.ENFOCADO}px solid {metal};
    background: {pozo_enfocado};
}}

/* ── Entradas de texto: también son pozos ────────────────────────────────── */
QLineEdit, QTextEdit, QPlainTextEdit, QSpinBox, QDoubleSpinBox, QComboBox {{
    background-color: {laca.pozo};
    color: {laca.texto_en_pozo};
    border: 1px solid {canto};
    border-radius: {RADIO_POZO}px;
    padding: {UNIDAD}px {UNIDAD}px;
    selection-background-color: {metal};
    selection-color: {sobre_metal};
}}
QLineEdit:focus, QTextEdit:focus, QPlainTextEdit:focus,
QSpinBox:focus, QDoubleSpinBox:focus, QComboBox:focus {{
    border: {Canto.ENFOCADO}px solid {metal};
    background-color: {pozo_enfocado};
}}

/* ── Botones ─────────────────────────────────────────────────────────────────
   El de acción primaria va en metal; los demás son pozos. El acento no se usa
   decorativamente: solo marca lo primario y lo seleccionado. */
QPushButton {{
    background-color: {laca.pozo};
    color: {laca.texto_en_pozo};
    border: 1px solid {canto};
    border-radius: {RADIO_POZO}px;
    padding: {UNIDAD}px {PADDING_CELDA}px;
    min-height: {UNIDAD * 3}px;
}}
QPushButton:hover {{ background-color: {mezclar(laca.pozo, laca.texto_en_pozo, 0.06)}; }}
QPushButton:pressed {{ background-color: {laca.pozo_contacto}; }}
QPushButton:disabled {{ color: {laca.texto_secundario_en_pozo}; }}
/* Sin esto se pulsa Tab, el foco recorre los botones y nada en pantalla lo dice
   — la app queda inusable sin ratón. */
QPushButton:focus {{
    border: {Canto.ENFOCADO}px solid {metal};
    background-color: {pozo_enfocado};
}}

QPushButton[primario="true"] {{
    background-color: {metal};
    color: {sobre_metal};
    border: 1px solid {metal};
    font-weight: 600;
}}
QPushButton[primario="true"]:hover {{ background-color: {metal_hover}; }}
QPushButton[primario="true"]:pressed {{ background-color: {metal_pulsado}; }}
/* En el relleno el canto se dibuja con el color del TEXTO: el metal ya ocupa
   toda la pieza, así que un canto de metal encima de metal no se vería. */
QPushButton[primario="true"]:focus {{
    border: {Canto.ENFOCADO}px solid {sobre_metal};
    background-color: {metal};
}}

QPushButton[plano="true"] {{
    background: transparent;
    border: 1px solid transparent;
}}
QPushButton[plano="true"]:hover {{ border: 1px solid {canto}; }}
/* Plano no lleva canto en reposo —es lo que lo hace plano— pero enfocado tiene
   que verse igual que los demás, o el foco lo salta sin que nadie lo note. */
QPushButton[plano="true"]:focus {{
    border: {Canto.ENFOCADO}px solid {metal};
    background: transparent;
}}

/* ── Listas y tablas ─────────────────────────────────────────────────────── */
QListView, QTreeView, QTableView {{
    background-color: {laca.pozo};
    alternate-background-color: {laca.pozo_contacto};
    border: 1px solid {canto};
    border-radius: {RADIO_POZO}px;
    selection-background-color: {metal};
    selection-color: {sobre_metal};
}}
QHeaderView::section {{
    background-color: {laca.bandeja};
    color: {laca.texto_atenuado_en_bandeja};
    border: none;
    border-bottom: 1px solid {canto};
    padding: {UNIDAD}px;
}}

/* ── Menús y barras: material de bandeja, no de pozo ─────────────────────── */
QMenuBar, QMenu, QToolBar, QStatusBar {{
    background-color: {laca.bandeja};
    color: {laca.texto_en_bandeja};
    border: none;
}}
QMenu {{
    border: 1px solid {canto};
    border-radius: {RADIO_POZO}px;
    padding: {UNIDAD // 2}px;
}}
QMenu::item {{
    padding: {UNIDAD}px {PADDING_CELDA}px;
    border-radius: {RADIO_POZO}px;
}}
QMenu::item:selected, QMenuBar::item:selected {{
    background-color: {metal};
    color: {sobre_metal};
}}
QMenu::separator {{
    height: 1px;
    background: {canto};
    margin: {UNIDAD // 2}px 0;
}}

/* ── Separadores y barras de desplazamiento ─────────────────────────────────
   La barra es un canto, no un bloque: en una bandeja no hay piezas flotando. */
QScrollBar:vertical, QScrollBar:horizontal {{
    background: transparent;
    width: {UNIDAD + 4}px;
    height: {UNIDAD + 4}px;
    margin: 0;
}}
QScrollBar::handle:vertical, QScrollBar::handle:horizontal {{
    background: {canto};
    border-radius: {RADIO_POZO}px;
    min-height: {UNIDAD * 3}px;
    min-width: {UNIDAD * 3}px;
}}
QScrollBar::handle:hover {{ background: {metal}; }}
QScrollBar::add-line, QScrollBar::sub-line {{ height: 0; width: 0; }}
QScrollBar::add-page, QScrollBar::sub-page {{ background: transparent; }}

/* ── Casillas y pestañas ─────────────────────────────────────────────────── */
QCheckBox::indicator, QRadioButton::indicator {{
    width: {PADDING_CELDA}px;
    height: {PADDING_CELDA}px;
    border: 1px solid {canto};
    border-radius: {RADIO_POZO}px;
    background: {laca.pozo};
}}
QCheckBox::indicator:checked, QRadioButton::indicator:checked {{
    background: {metal};
    border: 1px solid {metal};
}}
QTabBar::tab {{
    background: transparent;
    color: {laca.texto_atenuado_en_bandeja};
    padding: {UNIDAD}px {PADDING_CELDA}px;
    border: none;
}}
/* La marca del activo va en el borde que TOCA AL PANEL, y ese borde cambia con
   la posición de las pestañas. Con una sola regla de `border-bottom`, unas
   pestañas verticales quedaban subrayadas por abajo —la marca cruzada respecto
   del panel— y se veía mal sin que nada fallara. Comprobado midiendo píxeles:
   arriba el indicador sale de 56x2, y a la izquierda de 2x40. */
QTabBar::tab:selected {{
    color: {laca.texto_en_bandeja};
}}
QTabBar::tab:top:selected    {{ border-bottom: {Canto.SELECCIONADO}px solid {metal}; }}
QTabBar::tab:bottom:selected {{ border-top:    {Canto.SELECCIONADO}px solid {metal}; }}
QTabBar::tab:left:selected   {{ border-right:  {Canto.SELECCIONADO}px solid {metal}; }}
QTabBar::tab:right:selected  {{ border-left:   {Canto.SELECCIONADO}px solid {metal}; }}
QTabWidget::pane {{ border: none; }}

/* ── Tooltips ────────────────────────────────────────────────────────────── */
QToolTip {{
    background-color: {laca.pozo};
    color: {laca.texto_en_pozo};
    border: 1px solid {canto};
    border-radius: {RADIO_POZO}px;
    padding: {UNIDAD}px;
}}
"""
