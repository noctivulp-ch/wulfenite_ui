# Los iconos

Ochenta iconos de trazo, en `iconos/`. SVG plano de 24x24, un solo color, sin
dependencias para regenerarlos.

```bash
python3 iconos/generar.py    # los escribe todos
python3 iconos/validar.py    # comprueba que cumplen las reglas
```

Son **de la librería, no de una app**: genéricos a propósito. Un icono que sabe de
casados y cortes de caja no viaja al proyecto siguiente.

---

## Las reglas del trazo

Son cinco y no son estéticas: cada una viene de algo que se vio salir mal.

| | valor | por qué |
|---|---|---|
| Grilla | `24 x 24` | El tamaño al que se dibuja. Escalar de 24 a 16 o a 48 no rompe nada; dibujar a 17 sí. |
| Trazo | `1.75` | Uniforme en **todo** el juego. Un icono con trazo distinto se ve "de otra familia" aunque nadie sepa decir por qué. |
| Puntas | `round` en cap y join | Es lo que hace que un trazo grueso no parezca cortado con tijera. |
| Relleno | `none`, salvo excepciones | Estilo de contorno. Las excepciones están listadas abajo. |
| Color | `#1A1A1A`, uno solo | **No es el color final**: se retiñe al dibujar. |

### Margen: nada toca el borde

Con trazo de 1,75, una línea en la coordenada 0 deja **medio trazo fuera del
lienzo** y el icono se ve recortado en cuanto la app lo escala. El juego se queda
en torno a 2,5 de margen. `validar.py` lo comprueba.

### Un solo color, y no es el que se va a ver

Cada SVG usa `#1A1A1A` sin ninguna intención de que ese sea el color final: se
retiñe en tiempo de ejecución. Es lo que permite que **el mismo archivo** sirva
para el estado inactivo —texto atenuado— y para el activo —metal— sin mantener
dos copias que acaban desincronizadas.

| | cómo se retiñe |
|---|---|
| Flutter | `flutter_svg` + `ColorFilter.mode(color, BlendMode.srcIn)` |
| Compose | `Icon(painterResource(...), tint = color)` |
| PyQt | `QPixmap` + `QPainter` con `CompositionMode_SourceIn` |

### El relleno, solo donde hace falta

Casi todo es contorno. Se rellena **cuando el contorno deja de leerse a tamaño
chico**:

- **Los mandos del reproductor** (`reproducir`, `pausa`, `parar`, `anterior`,
  `siguiente`, `retroceder`, `adelantar`). A tamaño de mando de televisor, un
  triángulo de trazo se confunde con una flecha.
- **Los triángulos indicadores** en su versión `-lleno`.
- **Los puntos pequeños** — el del signo de admiración, los de `compartir`. Un
  círculo de trazo con radio 1 es una mancha gris.

Si hace falta añadir uno relleno, va también a la lista de `validar.py`, o la
comprobación lo marca.

---

## La regla que más cuesta: nunca `<style>`

Los atributos de presentación van **en cada elemento**, no en un bloque `<style>`
ni en clases CSS.

No es una preferencia. `flutter_svg` **no soporta `<style>` y lo ignora en
silencio**: las formas quedan sin trazo y con relleno negro sólido. Y como los
visores de escritorio sí lo soportan, **el icono se ve perfecto al revisarlo** y
mal solo dentro de la app.

Se descubrió por un error en la consola de las pruebas, no mirando el dibujo. Por
eso `validar.py` lo comprueba lo primero.

---

## Cómo dibujar uno nuevo

1. Se añade una función al grupo que le toque en `generar.py`, con las primitivas
   de ahí (`linea`, `ruta`, `circulo`, `punto`, `rectangulo`, `flecha`,
   `chevron`...). Todas aplican el trazo correcto por construcción.
2. `python3 iconos/generar.py && python3 iconos/validar.py`.
3. **Mirarlo a 16 px**, no a 150. Es el tamaño al que se va a usar, y la mitad de
   los problemas solo aparecen ahí.

```bash
rsvg-convert -w 16 -h 16 -b '#F8F7F2' iconos/mi-icono.svg -o /tmp/chico.png
```

### Tres cosas que ya salieron mal

**Un dibujo puede decir otra cosa que su nombre.** El primer `ajustes` era un
círculo con rayos alrededor: eso es un **sol**, no un engranaje. Lo que lo
convierte en engranaje es el **aro** entre el centro y los dientes. El sol se
conservó, con el nombre que le corresponde: `brillo`.

**Una forma que representa un objeto tiene que cerrar.** El primer `filtrar` era
una polilínea abierta y se leía como una bandera. Con `Z` es un embudo.

**Un chevron y un triángulo no son lo mismo.** El chevron es una V abierta y
sugiere continuidad — *"sigue por aquí"*. El triángulo es una forma cerrada y
señala — *"esto es lo elegido"*. En un menú que se recorre con mando, el lleno es
el cursor y el hueco el destino disponible. Por eso están los dos, y por eso los
triángulos son un poco más altos que anchos: uno equilátero se lee como una señal
de aviso, no como un puntero.

---

---

## Usarlos en código

Los tres paquetes traen la pieza `WulfIcono` / `icono()`: **no se cargan a mano**.
La pieza resuelve el tamaño, el teñido y la accesibilidad de una vez, que son
justo las tres cosas que cada app resolvería distinto.

### Flutter

Los SVG **viajan dentro del paquete**: una app que use `wulf_flutter` no tiene que
copiarlos ni declararlos en su `pubspec`.

```dart
WulfIcono('buscar', descripcion: 'Buscar')
WulfIcono('reportes', descripcion: 'Reportes', activo: true)
WulfIcono('reproducir', descripcion: null, tamano: WulfIconoTamano.grande)
```

### Compose

Android **no carga SVG**, así que el juego se publica además como *vector
drawables* — los genera `iconos/a_vector_drawable.py` desde los mismos archivos.
Los nombres pasan a `wulf_` + guion bajo, porque Android no admite guiones ni
mayúsculas en un recurso.

```kotlin
WulfIcono(R.drawable.wulf_buscar, "Buscar")
WulfIcono(R.drawable.wulf_reportes, "Reportes", activo = true)
```

### PyQt

Qt carga SVG de fábrica.

```python
boton.setIcon(icono("buscar", laca.texto_atenuado_en_bandeja))
boton.setIconSize(QSize(IconoTamano.NORMAL, IconoTamano.NORMAL))
```

---

## Los tamaños

Tres nombres y no una escala libre: un icono a 22 junto a otro a 24 se ve
descuadrado sin que nadie sepa decir por qué.

| | px | cuándo |
|---|---|---|
| `chico` | 20 | en línea con un texto: junto a una etiqueta, dentro de un campo |
| `normal` | **24** | el de siempre. Es el tamaño de la grilla, así que no se reescala |
| `grande` | 32 | protagonista: un destino del rail, un estado vacío |
| `television` | 36 | de lejos. Se aplica **solo** cuando lo pide el tamaño de pantalla |

**Ninguno es el objetivo táctil.** Un icono de 24 dentro de un botón sigue
necesitando sus **48** alrededor: el dibujo es lo que se ve, no lo que se toca.
Confundirlos deja botones de 24 imposibles de acertar con el dedo.

En televisor la pieza crece sola donde la plataforma lo dice (Compose) y con un
parámetro donde no lo expone (Flutter). Es la misma razón que el labio de la
bandeja y el canto del foco: a tres metros lo que en la mano se lee perfectamente
deja de leerse.

### El aire junto al texto

`wulfGapIcono` / `GAP_ICONO` = **4**, la mitad de la unidad. Con la unidad entera
el par se lee como dos cosas sueltas en vez de como una etiqueta con su icono.

---

## El teñido, y el error que hay que evitar

El color se aplica **respetando el alfa del dibujo**. Con el modo equivocado no
se tiñe el trazo: se pinta **un cuadrado sólido del color** encima.

| | qué usar |
|---|---|
| Flutter | `ColorFilter.mode(color, BlendMode.srcIn)` |
| Compose | el `tint` de `Icon` |
| PyQt | `QPainter` con `CompositionMode_SourceIn` |

Es siempre el mismo concepto —*source in*— y en las tres pasa lo mismo si se
usa el de por defecto. Hay una prueba en PyQt que cuenta píxeles opacos y falla
si el resultado parece un bloque en vez de un trazo.

### Qué color

| estado | color |
|---|---|
| normal | `textoAtenuadoEnBandeja` |
| activo / elegido | el **metal** (`primary`) |

Es la misma señal que en todo el sistema: el acento marca lo elegido, y no se usa
decorativamente. La pieza lo resuelve con `activo`; el parámetro de color a mano
está para lo que se salga de esos dos casos, como un icono dentro de una falla.

---

## Accesibilidad

`descripcion` es lo que oye un lector de pantalla, y **es obligatoria de pensar,
no de rellenar**:

- Un icono que **es** la acción —una lupa sola en una barra— la necesita.
- Un icono que **acompaña** a un texto que ya dice lo mismo va con `null`. Si no,
  se lee dos veces: *"Reportes Reportes"*.

---

## El juego

**Direcciones** (23) — `chevron-abajo`, `chevron-arriba`, `chevron-derecha`, `chevron-izquierda`, `flecha-abajo`, `flecha-arriba`, `flecha-derecha`, `flecha-izquierda`, `triangulo-abajo`, `triangulo-abajo-lleno`, `triangulo-arriba`, `triangulo-arriba-lleno`, `triangulo-derecha`, `triangulo-derecha-lleno`, `triangulo-izquierda`, `triangulo-izquierda-lleno`, `volver`, `avanzar`, `entrar`, `salir`, `expandir`, `contraer`, `ordenar`

**Señales** (11) — `correcto`, `error`, `aviso`, `informacion`, `pendiente`, `bloqueado`, `desbloqueado`, `visible`, `oculto`, `favorito`, `destacado`

**Acciones** (17) — `anadir`, `quitar`, `anadir-circulo`, `quitar-circulo`, `editar`, `eliminar`, `buscar`, `filtrar`, `guardar`, `copiar`, `compartir`, `descargar`, `subir`, `actualizar`, `mas-opciones`, `menu`, `cerrar`

**Reproductor** (10) — `reproducir`, `pausa`, `parar`, `anterior`, `siguiente`, `retroceder`, `adelantar`, `volumen`, `silencio`, `pantalla-completa`

**Objetos** (13) — `archivo`, `carpeta`, `imagen`, `lista`, `cuadricula`, `tabla`, `ajustes`, `brillo`, `usuario`, `casa`, `calendario`, `etiqueta`, `reloj`

**Conectividad** (6) — `wifi`, `sin-conexion`, `sincronizar`, `enlace`, `dispositivo`, `nube`

---

## Lo que este juego NO trae

**Iconos de dominio.** Un carrito, una caja registradora o un plato son de la app
que los necesita, no de la librería. Van en los recursos de esa app, con estas
mismas reglas para que no desentonen — el generador se puede copiar y recortar.

**Un icono por concepto de negocio.** Si dos pantallas necesitan distinguirse
—"Venta rápida" contra "Panel de venta"— eso pide dibujo a medida, y a medida
quiere decir en la app.
