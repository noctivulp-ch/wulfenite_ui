# wulf-compose

Wulfenite para **Android nativo**, en Kotlin con Jetpack Compose. Gemela de
[`flutter/`](../flutter/README.md) y de [`PyQT/`](../PyQT/README.md): los mismos
hexadecimales, el mismo espaciado, las mismas [doce reglas](../doc/reglas.md).

```kotlin
WulfTheme(WulfAcento.JADE) {
    WulfBandeja {
        WulfPozo { Text("₡128 400") }
    }
}
```

## Cuándo tiene sentido

Una app Flutter **ya corre en Android**, así que este paquete no es una
alternativa a la gemela de Dart: es para cuando el proyecto es nativo por algún
otro motivo y Flutter no está sobre la mesa.

## Qué hay dentro

| Pieza | Qué es |
|---|---|
| `tokens/` | Espaciado, radios, los tres metales, las dos lacas, el `ColorScheme` completo |
| `superficies/` | Bandeja y pozo — la geometría invertida, la cresta de 1 dp, la onda del toque |
| `reticula/` | Las seis columnas y los tres tamaños |
| `texto/` | El relieve grabado y la etiqueta que encoge |
| `componentes/` | Diálogo, acciones, panel, enlace, falla, aviso y controles |
| `foco/` | Los tres escalones del canto — lo que hace usable la app con mando |
| `ejemplo/` | El banco de estilo: una app que toca todas las piezas |

## Correrlo

```bash
./gradlew :wulf-compose:testDebugUnitTest   # 40 pruebas, sin emulador
./gradlew :ejemplo:installDebug             # el banco de estilo en un aparato
```

Las pruebas corren en la JVM porque lo que vigilan es aritmética: razones de
contraste, reparto de la retícula, emparejado de acciones y paridad con el Dart.
Nada de eso necesita pintar píxeles, y así se ejecutan en segundos.

`local.properties` apunta al SDK de esta máquina y está fuera del control de
versiones; hay que recrearlo con `sdk.dir=/ruta/al/Android/Sdk`.

## El foco, que es lo que la hace usable en televisión

Todo lo que se puede activar muestra cuándo tiene el foco: el canto sube a metal
pleno de 3 dp y el relleno se aclara ([regla 12](../doc/reglas.md)). Un `WulfPozo`
tocable es enfocable por defecto; `enfocable = false` lo saca del recorrido.

No es una mejora de accesibilidad: **sin esto la app no se puede usar con un
mando**. El mando mueve el foco y nada en pantalla dice dónde está.

Se comprobó recorriendo el banco de estilo con el D-pad. Ahí salió un fallo que
no se veía leyendo el código: el enlace enfocado se marcaba con el recuadro gris
de fábrica de Compose, la única señal de toda la app que no era la cresta.

## Tres cosas que este port tuvo que decidir

### El color dinámico se rechaza, y es una postura

Android 12+ ofrece teñir la app desde el fondo de pantalla. Wulfenite tiene **un**
acento por diseño: el metal es la única señal de acción y de selección en todo el
sistema (regla 4), y sus razones de contraste están verificadas metal por metal.
Dejar que el sistema lo reemplace por un color cualquiera rompe esa verificación
y con ella la regla 1.

No está puesto como opción. Una app que quiera color dinámico no quiere este
sistema de diseño.

### La mezcla de colores es propia, no la de la plataforma

`wulfLerp` mezcla **canal a canal en sRGB**, como hace Dart. El `lerp` de Compose
interpola en Oklab, y con la misma fusión da un color distinto.

No es un detalle académico: con Oklab, el nivel marcado del tallado en modo
oscuro salía a **2,79:1**, por debajo del piso de 3:1 que el propio sistema se
exige, y a 3,09:1 con la mezcla correcta. O sea que usar el `lerp` de la
plataforma no daba "casi lo mismo": daba un sistema que incumple su propia regla
de contraste, y un canto de pozo que no coincide con el de las otras dos gemelas
— **con las constantes de la paleta coincidiendo perfectamente**.

Lo cazó `TalladoTest`. Ahora `ParidadConDartTest` comprueba el canto **derivado**
y no solo las constantes, que es lo que lo habría detectado solo.

### Todos los roles de Material 3 se mapean, sin excepción

Un rol sin mapear no se nota leyendo el código: se nota en pantalla. La primera
vez que se ejecutó el ejemplo salió una píldora **azul violácea** en mitad de la
laca — era `secondaryContainer` viniendo del esquema morado de fábrica.

Secundario y terciario **no** son otros colores acá: apuntan al canto y al metal,
porque el sistema tiene un solo acento. Y `surfaceTint` va transparente: el tinte
de elevación de M3 es exactamente el efecto que este sistema rechaza — aclarar
una superficie para decir que está más alta.

## Lo que cambia respecto de la gemela de Flutter

| | Flutter | Compose |
|---|---|---|
| Aplicar el tema | `buildWulfTheme(...)` devuelve un `ThemeData` | `WulfTheme { }` envuelve el contenido |
| Leer la laca | `lacaDe(context)` | `WulfTheme.laca` |
| Fondo de pantalla | `scaffoldBackgroundColor` | lo pinta `WulfBandeja` |
| Onda del toque | `InteractiveInkFeature` | `IndicationNodeFactory` en `LocalIndication` |
| Etiqueta que encoge | `FittedBox(scaleDown)` | mide y reintenta, con suelo de 9 sp |
| Altura compartida | `IntrinsicHeight` | `Modifier.height(IntrinsicSize.Min)` |

La onda se reemplaza **en todo el árbol** desde el tema. Es más limpio que en
Flutter, donde hubo que anular el velo blanco de fábrica botón por botón.

## Estado

**0.1.0, sin publicar.** Compila, pasa sus 40 pruebas y el banco de estilo corre
—y se recorre con el D-pad— en un aparato real. La API puede moverse mientras no haya un segundo proyecto
usándola.

Lo que **no** está portado: no hay pruebas de interfaz (harían falta Robolectric
o un emulador), y la textura `GRANO` de la onda usa una espiral fija en vez de la
dispersión de la versión de Dart — se ve equivalente, pero no es el mismo dibujo.
