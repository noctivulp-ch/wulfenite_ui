# Wulfenite-Qt

Sistema de diseño para **PySide6**: superficies lacadas con canto metálico,
retícula de seis columnas, y componentes que llevan sus reglas dentro.

Es la adaptación del paquete Dart
[`../flutter`](../flutter). **Los tokens son idénticos** —hay una prueba
que lo verifica leyendo el archivo Dart— y lo que cambia es cómo se pintan,
porque Qt y Flutter no dibujan igual.

```python
import sys
from PySide6.QtWidgets import QApplication, QLabel
from wulf_qt import Acento, Bandeja, Celda, Cuadricula, Modo, Pozo, aplicar

app = QApplication(sys.argv)
aplicar(app, Acento.ORO, Modo.OSCURO)

bandeja = Bandeja()
rejilla = Cuadricula()
rejilla.poner([
    Celda(Pozo(QLabel("₡128 400")), compacto=6, medio=6, amplio=3),
    Celda(Pozo(QLabel("Primer turno")), compacto=3, medio=3, amplio=1),
    Celda(Pozo(QLabel("Segundo turno")), compacto=3, medio=3, amplio=1),
])
bandeja.agregar(rejilla)
bandeja.show()
app.exec()
```

## Qué trae

| Módulo | Qué es |
|---|---|
| `tokens` | Espaciado en múltiplos de 8, dos radios, dos lacas, tres metales, y la fórmula de contraste WCAG. **Sin Qt**: se importa sin abrir ventana |
| `tema` | `aplicar(app, acento, modo)` — Fusion + `QPalette` + hoja de estilo |
| `reticula` | `Bandeja`, `Pozo`, `Cuadricula`, `Celda`, `Ancho` |
| `texto` | `Tallado` (relieve pintado) y `EtiquetaAjustada` (encoge antes que partirse) |
| `componentes` | `Acciones`, `Dialogo`, `Enlace`, `Atenuado`, y las tres formas de botón |

## Qué viaja del sistema, y qué no

Conviene decirlo sin adornos.

**Viaja entero:** los tokens, la retícula, el reparto de acciones, el piso de
contraste, los botones, las entradas, las listas, los menús. Todo eso se expresa
en `QPalette` + QSS y se ve igual que en Flutter.

**Viaja a medias:** el **relieve**. En Flutter son tres capas de widget; acá son
tres pasadas de `QPainter`, porque una hoja de estilo no tiene forma de decir
"pintá este mismo glifo tres veces desplazado". Funciona y está probado
rasterizando, pero es la pieza donde las dos implementaciones son de verdad
distintas.

**No viaja:** nada del catálogo de iconos, y ningún widget que sepa de un dominio
concreto.

## Las reglas están en el código, no en una guía

Es la diferencia entre un tema y un sistema de diseño:

```python
Acciones(confirmar=boton_primario("Guardar"), cancelar=boton("Cancelar"))
```

No hay forma de construirlo con el botón de guardar a la izquierda. Y no es
manía: en la app de la que salió esto, el orden estaba invertido en los once
diálogos, y en el de "crear categoría al vuelo" eso significa **pulsar Cancelar
creyendo guardar**. Lo encontró el dueño usándola, no una revisión de código.

Las catorce reglas completas, con el porqué de cada una, están en el paquete Dart:
[`doc/reglas.md`](../doc/reglas.md).

## Probar

```bash
python -m venv .venv && .venv/bin/pip install -e ".[dev]"
.venv/bin/python -m pytest
```

44 pruebas. Las que importan:

- **Contraste** — recalcula WCAG sobre la paleta real, en los tres metales por
  las dos lacas. Añadir un metal que no cumpla rompe la prueba.
- **Paridad con Dart** — lee el archivo Dart y compara los tokens. Se salta con
  un motivo claro si el otro paquete no está a mano.
- **Orden de acciones** — mide la geometría real: dónde acaba cada botón.
- **Relieve** — rasteriza y compara píxeles, porque a simple vista el efecto es
  deliberadamente sutil.

Corren sin servidor gráfico (`QT_QPA_PLATFORM=offscreen`).

## El ejemplo es un banco de estilo

```bash
.venv/bin/python ejemplo/banco.py
```

No es un "hola mundo": es donde se decide un acabado **mirándolo**. Los tres
metales por las dos lacas, el relieve en sus tres tratamientos, la retícula
recolocándose al redimensionar, y el contraste medido en pantalla.

Está escrito importando solo `wulf_qt`: lo que tuviera que resolver por su
cuenta sería exactamente lo que le falta al paquete. Su gemelo en Flutter vive en
el paquete Dart, y sirve para comparar los dos lados uno al lado del otro — que
es la única forma honesta de decir si la adaptación quedó fiel.

## Lo que Qt mete por su cuenta

Un `QTabBar` pinta bajo las pestañas una línea con el **realce del escritorio**
—un azul que no sale de ningún metal de Wulfenite— y **ninguna regla de QSS la
alcanza**: `drawBase` es una propiedad del widget, no algo que la hoja de estilo
pueda tocar. Se probaron el borde del panel, el del `QTabBar`, el de `tab-bar` y
el fondo; ninguna sirve.

```python
self.pestanas = quitar_linea_de_pestanas(QTabWidget())
```

El valor de fábrica además **depende de cómo se arme la barra** —medido en
PySide6 6.11.1: una `QTabBar()` suelta viene con la línea encendida y la de un
`QTabWidget` no— así que conviene llamarlo siempre.

Lo que **no** hace es decidir cómo se ven las pestañas. Eso sería una pieza
completa, y media pieza sería la única del sistema que no define su acabado.

### Verticales

Funcionan. La marca del activo va en el borde que **toca al panel**, y ese borde
cambia con la posición — arriba marca abajo, a la izquierda marca a la derecha.

Con una sola regla de subrayado no funcionaban: unas pestañas a la izquierda
quedaban subrayadas por abajo, o sea con la marca **cruzada** respecto del panel.
Medido pintando: el indicador salía de **56×2** y ahora sale de **2×40**.

```python
tabs = quitar_linea_de_pestanas(QTabWidget())
tabs.setTabPosition(QTabWidget.TabPosition.West)
```

**El texto sigue rotado, y no tiene arreglo con el tema puesto.** La única vía es
un estilo delegado, y Qt lo descarta en cuanto una hoja de estilo toca
`QTabBar::tab` — medido: 75×29 sin hoja, 51×67 con ella.

`enderezar_texto_de_pestanas(tabs)` existe y **devuelve si lo consiguió**, para
que nadie lo dé por hecho. Con el tema puesto devuelve `False`. Las salidas están
en [`doc/paridad.md`](../doc/paridad.md); la corta es **usar un rail** —una
columna de `boton_plano`— en vez de pestañas verticales.

## Estado

**0.1.0, sin publicar.** La paridad de tokens está verificada y las reglas están
probadas, pero la API puede moverse mientras no haya un segundo proyecto
usándola.

Ya cazó un fallo real: la prueba de contraste detectó que el canto del pozo se
apagaba hacia la bandeja en vez de hacia el pozo, que lo dejaba por debajo de
3:1 contra ella — justo lo que el canto existe para cumplir.
