# Wulfenite-UI

Sistema de diseño para Flutter: **superficies lacadas con canto metálico**,
retícula de seis columnas, y componentes que llevan sus reglas dentro.

El nombre viene del mineral — placas cuadradas y finas con brillo metálico
ámbar. Es lo que hace: una bandeja continua con pozos hundidos, separados por un
canto de un píxel que es lo único que carga la profundidad.

```dart
import 'package:wulf_flutter/wulf_flutter.dart';

MaterialApp(
  theme: buildWulfTheme(WulfAcento.oro),
  darkTheme: buildWulfTheme(WulfAcento.oro, brightness: Brightness.dark),
  home: Scaffold(
    body: WulfBandeja(
      child: WulfCuadricula(
        hijos: [
          WulfCelda(compacto: 6, medio: 6, amplio: 3, hijo: WulfPozo(child: total)),
          WulfCelda(compacto: 3, medio: 3, amplio: 3, hijo: WulfPozo(child: turno)),
        ],
      ),
    ),
  ),
)
```

## Qué trae

| | |
|---|---|
| **Tokens** | Espaciado en múltiplos de 8, dos radios, tres acabados de laca × dos luminosidades, tres metales |
| **Superficies** | `WulfBandeja`, `WulfPozo`, la tinta del toque, el velo de los insets del sistema |
| **Retícula** | `WulfCuadricula` y `WulfCelda`: seis columnas, tres tamaños de pantalla |
| **Texto** | `WulfTallado` (relieve grabado) y `WulfEtiquetaAjustada` (encoge antes que partirse) |
| **Componentes** | `WulfDialogo`, `WulfAcciones`, `WulfEnlace`, `WulfAviso`, `WulfFalla` |

## Lo que lo distingue

**No es un tema, son reglas.** Un `WulfDialogo` pone la acción que confirma a la
derecha porque para guardar se pulsa a la derecha sin mirar. Un `WulfEnlace` va
subrayado *y* en el color del acento porque el color solo no basta para quien no
distingue ese tono. Esas decisiones están **dentro del widget**, no en una guía
que alguien tiene que recordar — y hay pruebas que las vigilan.

**La geometría va al revés.** El contenido se hunde en la bandeja en vez de
flotar sobre el fondo. Cero `BoxShadow`: la profundidad la carga el canto de un
píxel, que es lo único que sobrevive a la luz del sol y lo único que funciona en
modo oscuro, donde el salto de valor entre dos superficies oscuras no llega a
3:1.

**El contraste está medido, no estimado.** `test/contraste_test.dart` recalcula
la fórmula WCAG sobre el tema realmente construido, en los tres metales por las
dos luminosidades. Añadir un metal que no cumpla rompe la prueba.

Las doce reglas, con el porqué de cada una, están en
[`doc/reglas.md`](../doc/reglas.md).

## El ejemplo es un banco de estilo

`example/` no es un "hola mundo": es un laboratorio donde **cada color del
sistema se edita en vivo**, se compara el relieve grabado contra el realzado y
el plano, y la configuración resultante **se exporta a un archivo** para que
quede como especificación exacta en vez de interpretada de una captura.

```bash
cd example && flutter run -d linux
```

Sirve para dos cosas a la vez, y por eso es el ejemplo:

- **Valida la librería.** Si el banco se puede escribir importando solo
  `wulf_flutter`, la librería está completa. Lo que el banco tenga que resolver
  por su cuenta es exactamente lo que le falta.
- **La enseña.** Se toca y se ve, en vez de leer una lista de constantes.

De hecho ya cumplió su función: al portarlo salió que la librería no sabía hacer
relieve **realzado** ni aceptar luz y sombra dadas a mano. Las dos cosas entraron
—`WulfTratamiento` y `WulfTallado.conColores`— y el banco dejó de necesitar su
copia local.

## Estado

**0.1.0, y todavía no publicado.** Sale de una app en producción, así que el
código está probado en aparatos reales, pero la API puede moverse mientras no
haya un segundo proyecto usándola.

- 69 pruebas, incluidas las que blindan contraste, orden de acciones y relieve.
- Sin dependencias más allá de Flutter.

## Lo que NO trae, a propósito

- **Iconos.** El sistema retinta iconos monocromos, pero el juego de iconos es de
  cada app.
- **Gestión de estado.** Los efectos apagables viajan por el tema, no por una
  librería de estado: así los widgets no conocen la app que los usa.
- **Fuentes.** El tema respeta la que ponga la app.
