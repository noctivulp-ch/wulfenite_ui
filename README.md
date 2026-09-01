# Wulfenite-UI

Un sistema de diseño, tres librerías gemelas: **superficies lacadas con canto
metálico**, retícula de seis columnas, y componentes que llevan sus reglas
dentro.

El nombre viene del mineral — placas cuadradas y finas con brillo metálico
ámbar. Es lo que hace: una bandeja continua con pozos hundidos, separados por un
canto de un píxel que es lo único que carga la profundidad.

```
wulfenite_ui/
├── doc/reglas.md   ← las catorce reglas. Lo único que de verdad define el sistema
├── flutter/        ← paquete Dart · wulf_flutter
├── PyQT/           ← paquete Python · wulf_qt
└── Kotlin/         ← dos subpaquetes
    ├── wulf-core/      Kotlin puro: los números, sin Android ni Compose
    └── wulf-compose/   la piel de Android sobre ese núcleo
```

## Las tres son la misma cosa

| | Flutter | PyQT | Kotlin |
|---|---|---|---|
| Paquete | `wulf_flutter` | `wulf_qt` | `wulf-core` + `wulf-compose` |
| Cómo se aplica | `buildWulfTheme(WulfAcento.oro)` | `aplicar(app, Acento.ORO, Modo.OSCURO)` | `WulfTheme(WulfAcento.ORO) { }` |
| Pruebas | 106 | 90 | 19 + 36 |
| Ejemplo | `example/` | `ejemplo/banco.py` | `ejemplo/` |

Comparten prefijo de paquete (`wulf`) para que convivan sin chocar, y comparten
**los valores exactos**: los mismos hexadecimales, el mismo espaciado, los mismos
radios.

Eso no se sostiene con disciplina. Cada port lee el archivo Dart y falla si algún
valor se separa —`PyQT/tests/test_paridad_con_dart.py` y
`Compose/.../ParidadConDartTest.kt`—. Tres copias de una paleta es la manera
clásica de que un sistema de diseño se rompa: alguien ajusta un color de un lado,
los otros se quedan viejos, y meses después las apps de la misma marca ya no se
parecen.

Y no es hipotético: la paridad ya cazó **dos** fallos reales. En Python, un canto
que se apagaba hacia el lado equivocado y quedaba por debajo de 3:1. En Kotlin,
una mezcla de colores que interpolaba en otro espacio y dejaba el tallado en
2,79:1 — **con las constantes coincidiendo perfectamente**. Por eso la paridad
comprueba ahora los colores DERIVADOS, no solo las constantes.

## Empezar

**Flutter**

```dart
import 'package:wulf_flutter/wulf_flutter.dart';

MaterialApp(
  theme: buildWulfTheme(WulfAcento.oro),
  home: Scaffold(
    body: WulfBandeja(child: WulfPozo(child: Text('₡128 400'))),
  ),
)
```

**PyQT**

```python
from wulf_qt import Acento, Bandeja, Modo, Pozo, aplicar

aplicar(app, Acento.ORO, Modo.OSCURO)
bandeja = Bandeja()
bandeja.agregar(Pozo(QLabel("₡128 400")))
```

**Compose**

```kotlin
import wulf.compose.superficies.*
import wulf.compose.tokens.*

WulfTheme(WulfAcento.ORO) {
    WulfBandeja { WulfPozo { Text("₡128 400") } }
}
```

Cada una tiene su README con el detalle: [`flutter/`](flutter/README.md) ·
[`PyQT/`](PyQT/README.md) · [`Kotlin/`](Kotlin/README.md).

## Capas, medidas y parpadeos

Lo que **no falla**: no lanza excepción, no rompe una prueba, no sale en una
revisión de código. Solo se ve mal. El orden de las capas de un pozo, las medidas
que no se pueden calcular, los colores que aparecen de la nada y los parpadeos —
todo con el caso real que lo destapó — están en
[`doc/capas-y-medidas.md`](doc/capas-y-medidas.md).

## Rendimiento

El relieve pinta el mismo glifo tres veces, y eso se paga. Cuánto cuesta, dónde
está el interruptor para apagarlo por aparato, y **cómo medirlo sin llegar a una
conclusión falsa** —que es lo que pasó la primera vez— están en
[`doc/rendimiento.md`](doc/rendimiento.md).

El resumen de una línea: el costo está en **raster**, no en build; apagar el
relieve baja los fotogramas lentos de 50/125 a 4/125; y envolver cada tarjeta en
un `RepaintBoundary` **empeora**, porque la lista ya lo hace sola.

## Lo que hace que se vea así no son los colores

Son las [catorce reglas](doc/reglas.md) y las pruebas que las vigilan. Los tokens
sin las reglas dan otra app cualquiera con una paleta bonita.

Tres ejemplos de regla que está **dentro del componente**, no en una guía que
alguien tiene que recordar:

- La acción que confirma va a la derecha y cancelar a su izquierda, porque para
  guardar se pulsa a la derecha sin mirar.
- Un enlace va subrayado **y** en el color del acento, porque el color solo no
  basta para quien no distingue ese tono.
- Una etiqueta encoge antes que partirse a mitad de palabra.

Las tres salieron de fallos vistos usando una app de verdad, no de una revisión
de código.

## Qué viaja entre las dos, y qué no

**Viaja entero:** los tokens, la retícula, el reparto de acciones, el piso de
contraste, los controles, el aviso y los tres escalones del foco.

**Viaja a medias:** el relieve. En Flutter son tres capas de widget; en Qt, tres
pasadas de `QPainter`; en Compose, otra vez tres capas. Funciona en las tres y
está probado, pero es la pieza donde las implementaciones son de verdad distintas
— una hoja de estilo no sabe decir "pintá este mismo glifo tres veces
desplazado".

**Viaja, pero hay que escribirlo:** la mezcla de colores. Cada plataforma
interpola en un espacio distinto por su cuenta, así que las tres llevan su propia
mezcla canal a canal en sRGB. Delegar en la de la plataforma es lo que rompió el
contraste del port de Kotlin.

**No viaja:** iconos y cualquier widget que sepa de un dominio concreto.

## De dónde salió

De VulpTiketing, una app de contabilidad para una soda pequeña, donde el sistema
llevaba meses probado en teléfonos reales antes de mudarse acá. Esa app **no
depende de esto**: desde su versión 1.0.0 cada cambio ahí es una actualización
sobre datos de un negocio, y una refactorización estética no justifica ese
riesgo. Estas librerías son para los proyectos que vienen.

## Qué falta para que las tres digan lo mismo

La paridad no está cerrada, y conviene tenerlo escrito en vez de descubrirlo al
necesitar la pieza:

| pieza | Flutter | PyQT | Compose |
|---|---|---|---|
| Tokens, retícula, tallado, diálogo, enlace, etiqueta | ✓ | ✓ | ✓ |
| Aviso | ✓ | ✓ | ✓ |
| Foco / D-pad (regla 12) | ✓ | ✓ | ✓ |
| Cuarto tamaño: televisor | ✓ | ✓ | ✓ |
| Barras (progreso y búsqueda) | ✓ | ✓ | ✓ |
| Foco inicial de pantalla | ✓ | ✓ | ✓ |
| Falla | ✓ | — | ✓ |
| Velo superior | ✓ | — | ✓ |
| Onda del toque | ✓ | — | ✓ |

Las tres que le faltan a PyQT son de escritorio, donde pesan menos: no hay
muesca de cámara que esquivar con un velo, y Qt ya trae su propia realimentación
al pulsar. Aun así son huecos, no decisiones.

## Estado

**0.1.0 las tres, sin publicar.** El código está probado en aparatos reales, pero
la API puede moverse mientras no haya un segundo proyecto usándola.
