# Paridad entre los tres paquetes

**La regla:** una pieza está en los tres, o está escrito aquí por qué no y cómo se
consigue lo mismo a mano. Un hueco sin explicar es peor que no tener la pieza —
quien lo encuentre va a improvisar, y ahí se pierde la identidad.

Este documento se actualiza **en el mismo commit** que añade o quita algo.

---

## Lo que hay

| Pieza | Flutter | PyQT | Compose |
|---|---|---|---|
| Tokens y paleta | ✓ | ✓ | ✓ |
| Retícula de 6 columnas | ✓ | ✓ | ✓ |
| Cuarto tamaño (televisor) | ✓ | ✓ | ✓ |
| Bandeja y pozo | ✓ | ✓ | ✓ |
| Tallado, y apagarlo | ✓ | ✓ | ✓ |
| Etiqueta que encoge | ✓ | ✓ | ✓ |
| Diálogo y orden de acciones | ✓ | ✓ | ✓ |
| Enlace | ✓ | ✓ | ✓ |
| Aviso | ✓ | ✓ | ✓ |
| Falla | ✓ | ✓ | ✓ |
| Foco y sus tres escalones | ✓ | ✓ | ✓ |
| Foco inicial de pantalla | ✓ | ✓ | ✓ |
| Barra de progreso y deslizador | ✓ | ✓ | ✓ |
| Controles (botón, campo, interruptor) | tema | ✓ | ✓ |
| Velo superior | ✓ | **no aplica** | ✓ |
| Onda del toque | ✓ | **por el sistema** | ✓ |
| Pestañas | **a mano** | ✓ | **a mano** |
| Rail de navegación | ✓ | ✓ | ✓ |
| Iconos y su pieza | ✓ | ✓ | ✓ |

---

## Los huecos, y qué hacer en cada uno

### Velo superior — no aplica en escritorio

El velo existe para que el contenido pase **por debajo** de la muesca de la cámara
y de las notificaciones que bajan, en vez de que un margen seguro deje un borde
muerto.

Una ventana de escritorio no tiene muesca ni barra de estado encima: el gestor de
ventanas ya da un área limpia. Poner un degradado ahí sería una mancha sin
motivo.

**Si alguna vez hace falta** —una interfaz Qt a pantalla completa en una tele, por
ejemplo— es un `QWidget` con un degradado vertical del color de bandeja a
transparente, alto `RIM_TELEVISION`, y `WA_TransparentForMouseEvents` para que no
se coma un clic.

### Onda del toque — la pone el sistema

En Flutter y en Compose la onda se reemplaza entera porque **la de fábrica es
blanca translúcida** y rompe la laca. Es una pelea que hay que dar.

En Qt no: el estilo Fusion marca la pulsación oscureciendo el propio widget, que
es exactamente lo que Wulfenite quiere. La hoja de estilo ya lo redefine con el
tono de contacto:

```
QPushButton:pressed { background-color: <pozo_contacto>; }
```

**No se va a portar** la onda animada. Sería reimplementar un `QPainter` animado
en cada widget para conseguir algo que el estilo ya hace bien, y la regla 1 —la
profundidad la carga el canto— se cumple igual sin ella.

### Pestañas — pieza solo en Qt, receta en los otros dos

En Qt hay pieza porque hay **un problema que resolver**: la barra pinta una línea
con el realce del escritorio que ninguna hoja de estilo alcanza. Ver
`quitar_linea_de_pestanas`.

En Flutter y en Compose **ese problema no existe**, así que no hay pieza. Pero el
acabado sí tiene que ser el mismo, y esta es la receta:

- La pestaña activa se marca **en el borde que toca al panel**, con el metal y el
  grosor de `Canto.seleccionado`. Arriba marca abajo; a la izquierda, a la
  derecha.
- La inactiva va en **texto atenuado sobre bandeja**; la activa, en texto pleno.
- **Sin fondo propio y sin caja**: una pestaña no es un pozo. El pozo es el panel
  que hay debajo.
- El objetivo táctil, 48.

**Flutter**

```dart
TabBar(
  indicatorColor: Theme.of(context).colorScheme.primary,
  indicatorWeight: WulfCanto.seleccionado,
  labelColor: lacaDe(context).textoEnBandeja,
  unselectedLabelColor: lacaDe(context).textoAtenuadoEnBandeja,
  dividerColor: Colors.transparent,   // la línea de fábrica bajo la barra
  tabs: …,
)
```

`dividerColor: transparent` es el equivalente exacto de lo que hace
`quitar_linea_de_pestanas` en Qt: Material dibuja su propia línea bajo la barra, y
también es un color que no es de la paleta.

**Compose**

```kotlin
TabRow(
    selectedTabIndex = actual,
    containerColor = WulfTheme.laca.bandeja,
    contentColor = WulfTheme.laca.textoEnBandeja,
    divider = {},                       // la línea de fábrica
    indicator = { posiciones ->
        TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(posiciones[actual]),
            height = WulfCanto.seleccionado,
            color = MaterialTheme.colorScheme.primary,
        )
    },
) { … }
```

### Pestañas verticales, y el texto rotado

En Qt van con `setTabPosition`, y la marca del activo ya cae en el borde que toca
al panel. **Lo que no se puede arreglar es que Qt rote el texto 90°.**

Se intentó con un `QProxyStyle`, que es la única vía. Funciona… hasta que se
aplica el tema:

| | tamaño de la pestaña | estilo del widget |
|---|---|---|
| sin hoja de estilo | **75×29** — texto horizontal | el proxy |
| con la hoja de Wulfenite | 51×67 — texto rotado | el de Qt |

**En cuanto una hoja de estilo toca `QTabBar::tab`, Qt sustituye el estilo del
widget por el suyo y descarta el proxy.** El orden no cambia nada. Y como
Wulfenite sí estiliza las pestañas, el proxy siempre se pierde.

`enderezar_texto_de_pestanas` existe igual y **devuelve si lo consiguió**, para
que nadie se quede creyendo que está arreglado. Con el tema puesto devuelve
`False`.

Las tres salidas, en orden de preferencia:

1. **Pestañas arriba**, que es lo que hace Wulfenite por defecto.
2. **El rail** (`Rail` en Qt, `WulfRail` en las otras dos). Es la pieza que el
   sistema trae para esto: se lee de frente, entra en el recorrido del teclado y
   no depende de ningún estilo delegado. **Es la recomendación**, y está en los
   tres paquetes.
3. **Renunciar al estilo de esa barra**, si de verdad hacen falta verticales y
   legibles. Se pierde el acabado del sistema ahí, que casi siempre es peor
   negocio.

### Controles — tres caminos y el mismo resultado

| | cómo se consigue |
|---|---|
| Flutter | **por el tema**: `buildWulfTheme` define botones, campos y sus estados |
| Compose | **componentes propios**: Compose no permite temar formas ni tamaños |
| PyQT | **hoja de estilo** para campos e interruptores, y `boton`/`boton_primario`/`boton_plano` para lo que no basta con QSS |

No es incoherencia: es que cada plataforma da un punto de extensión distinto. Lo
que tiene que salir igual es el resultado — radio de pozo, 48 de alto, contorno
de metal y foco visible.

**Si algo se ve distinto entre los tres, es un fallo.** Los tokens son los mismos
y las pruebas de contraste corren en los tres.

---

### Los iconos, y el formato de cada uno

Los 80 SVG viven en `iconos/` y son **de la librería entera**. Lo que cambia es
cómo llegan a cada plataforma:

| | formato | de dónde |
|---|---|---|
| Flutter | SVG | copiados a `flutter/assets/iconos/`, dentro del paquete |
| PyQt | SVG | leídos de `iconos/` — Qt los carga de fábrica |
| Compose | *vector drawable* | generados a `res/drawable/wulf_*.xml` |

**Android no carga SVG**, así que el juego se convierte con
`iconos/a_vector_drawable.py`. Sale de ahí y no de Android Studio a propósito:
importar 80 iconos a mano por la interfaz es irrepetible, y nadie lo iba a rehacer
cuando cambiara el trazo.

Los tres se regeneran del mismo sitio, así que un icono nuevo aparece en las tres
corriendo dos guiones.

## Lo que NO viaja, y no es un hueco

- **Los iconos** y cualquier pieza que sepa de un dominio concreto.
- **El relieve, implementado**: tres capas de widget en Flutter y en Compose,
  tres pasadas de `QPainter` en Qt. El resultado es el mismo y está probado; la
  implementación es de verdad distinta.
- **La mezcla de colores**: cada paquete lleva la suya, canal a canal en sRGB.
  Delegar en la de la plataforma es lo que rompió el contraste del port de
  Kotlin — ver la nota al final de `reglas.md`.
