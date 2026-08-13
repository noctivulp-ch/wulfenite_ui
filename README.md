# Wulfenite-UI

Un sistema de diseño, dos librerías gemelas: **superficies lacadas con canto
metálico**, retícula de seis columnas, y componentes que llevan sus reglas
dentro.

El nombre viene del mineral — placas cuadradas y finas con brillo metálico
ámbar. Es lo que hace: una bandeja continua con pozos hundidos, separados por un
canto de un píxel que es lo único que carga la profundidad.

```
wulfenite_ui/
├── doc/reglas.md   ← las once reglas. Lo único que de verdad define el sistema
├── flutter/        ← paquete Dart · wulf_flutter
└── PyQT/           ← paquete Python · wulf_qt
```

## Las dos son la misma cosa

| | Flutter | PyQT |
|---|---|---|
| Paquete | `wulf_flutter` | `wulf_qt` |
| Cómo se aplica | `buildWulfTheme(WulfAcento.oro)` | `aplicar(app, Acento.ORO, Modo.OSCURO)` |
| Pruebas | 69 | 44 |
| Ejemplo | `example/` — banco de estilo | `ejemplo/banco.py` — banco de estilo |

Comparten prefijo de paquete (`wulf_`) para que convivan sin chocar, y comparten
**los valores exactos**: los mismos hexadecimales, el mismo espaciado, los mismos
radios.

Eso no se sostiene con disciplina. `PyQT/tests/test_paridad_con_dart.py` lee el
archivo Dart y falla si alguno se separa. Dos copias de una paleta es la manera
clásica de que un sistema de diseño se rompa: alguien ajusta un color de un lado,
el otro se queda viejo, y meses después las dos apps de la misma marca ya no se
parecen.

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

Cada una tiene su README con el detalle: [`flutter/`](flutter/README.md) ·
[`PyQT/`](PyQT/README.md).

## Lo que hace que se vea así no son los colores

Son las [once reglas](doc/reglas.md) y las pruebas que las vigilan. Los tokens
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
contraste, los controles.

**Viaja a medias:** el relieve. En Flutter son tres capas de widget; en Qt, tres
pasadas de `QPainter`. Funciona en las dos y está probado, pero es la pieza donde
las implementaciones son de verdad distintas — una hoja de estilo no sabe decir
"pintá este mismo glifo tres veces desplazado".

**No viaja:** iconos y cualquier widget que sepa de un dominio concreto.

## De dónde salió

De VulpTiketing, una app de contabilidad para una soda pequeña, donde el sistema
llevaba meses probado en teléfonos reales antes de mudarse acá. Esa app **no
depende de esto**: desde su versión 1.0.0 cada cambio ahí es una actualización
sobre datos de un negocio, y una refactorización estética no justifica ese
riesgo. Estas librerías son para los proyectos que vienen.

## Estado

**0.1.0 las dos, sin publicar.** El código está probado en aparatos reales, pero
la API puede moverse mientras no haya un segundo proyecto usándola.
