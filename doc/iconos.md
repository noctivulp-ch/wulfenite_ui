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
