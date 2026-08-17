# Las reglas

Lo que hace que esto se vea así **no son los colores**: son estas doce reglas y
las pruebas que las vigilan. Los tokens sin las reglas dan otra app cualquiera
con una paleta bonita.

Cada una lleva el porqué. No es adorno: una regla cuyo motivo se olvidó es una
regla que alguien va a saltarse con un buen argumento, y va a tener razón.

Salieron de una app de contabilidad para una soda —de ahí los ejemplos con
casados y cortes de caja— pero ninguna depende de ese dominio.

---

## Reglas que no se negocian

### 1. La profundidad la carga la cresta, no la sombra

Verificado numéricamente: **en modo oscuro es imposible** lograr 3:1 entre
dos superficies oscuras (el rango de luminancia está comprimido) — la
bandeja contra el pozo da 1.43:1. Por eso el tallado **no** puede depender
del escalón de valor bandeja↔pozo.

La señal primaria es la **cresta de pared**: una línea de 1px más clara en el
borde superior/izquierdo de cada pozo, verificada ≥3:1 contra la bandeja en
ambos modos (claro 3.20:1, oscuro 3.48:1). Es lo mismo que hace legible una
caja de laca negra real: los compartimentos se ven porque las crestas atrapan
la luz, no porque los fondos difieran de tono.

Corolario de la escena de uso: **prohibido usar sombras suaves de baja
opacidad como señal de profundidad.** Bajo sol directo desaparecen. Bordes
duros de 1px y saltos de valor reales, o nada.

### 2. La bandeja es continua; el hueco entre pozos es material, no fondo

El espacio entre dos pozos **es la pared de la bandeja**, con el ancho del
sistema (12). Nunca es "el fondo de la página asomándose". Dos pozos
adyacentes comparten una sola pared — nunca dos bordes enfrentados.

Consecuencia: una pantalla es *un* contenedor de bandeja con pozos recortados
dentro, no N tarjetas con `margin`.

### 3. Geometría invertida respecto a lo anterior

| Elemento | Radio |
|---|---|
| Bandeja (exterior) | 20 |
| Pozo (interior) | 4 |

Lo anterior tenía todo en 12 uniforme (cards, inputs, botones, list tiles,
diálogos), que es el rectángulo redondeado genérico. En una bandeja real el
exterior es suave y los compartimentos son rectilíneos.

### 4. El modo define la laca; el metal es ortogonal

**El modo (claro/oscuro) determina la paleta completa**, no un ajuste del
usuario. Una caja wulf física no cambia de color, pero una app sí — y
sostener lo contrario (bandeja oscura en los dos modos) tuvo dos costos
reales: los modos se veían casi iguales (parecía un "modo contraste", no un
modo claro) y todo lo que caía sobre la bandeja heredaba color de texto de
pozo, quedando ilegible.

Con una laca por modo, bandeja y pozo tienen **la misma polaridad** dentro de
cada modo, así que un texto mal asignado sigue siendo legible. Eso elimina
toda una clase de bug por construcción, en vez de tener que parchear pantalla
por pantalla.

El ajuste configurable de la [decisión 0020](docs/decisiones/0020-iconografia-vectorial-propia.md)
pasa a elegir el **acento** (oro / jade / peltre) — el pan de metal sobre laca,
que es tradicionalmente oro pero también peltre, más el jade de las
incrustaciones de piedra. Se
conserva la intención de 0020 (pocos, hardcodeados, coherentes) y se le da un
trabajo que no choca con el modo.

**El metal tiene dos fuerzas, no una:**

| Fuerza | Dónde | Por qué |
|---|---|---|
| **Ribete** (apagado, mezclado hacia el pozo) | borde de todos los pozos, campo en reposo | en laca real el pan de metal ribetea los cantos de la pieza |
| **Pleno** | acción primaria, selección actual, campo enfocado, indicador de estado | el acento marca acción, y sigue destacando porque el ribete está apagado |

El ribete se **deriva** del metal en código, no se hardcodea: con un tono fijo,
elegir peltre dejaba el elemento seleccionado en plata y todos los demás bordes
leyéndose dorados — el ajuste cambiaba una pieza y desafinaba el resto.
Verificado ≥3:1 contra la bandeja en los 3 metales × 2 modos, y hay una prueba
que exige que **cada metal dé un ribete distinto**.

El acento nunca es decoración y nunca aparece en estados inactivos.

### 5. Nada de `ColorScheme.fromSeed`

La paleta es de autoría propia, no generada por el algoritmo tonal de Google.
`fromSeed` + `DynamicSchemeVariant.neutral` fue la causa raíz del aspecto por
defecto: codifica elevación-como-claridad, que es lo contrario de un pozo.

### 6. Tallado: contraste gastado a propósito, y con techo

El texto se lee **grabado en su superficie**, no apoyado encima
(`lib/src/**/tallado.dart`). Calibrado por el usuario en el banco de
pruebas: profundidad 1.25, luz 0.70, sombra 0.65 — incisión poco profunda con
el canto brillante.

El efecto se consigue acercando el glifo al color de su fondo, así que **es
literalmente contraste gastado en estética**. Por eso tiene dos niveles y un
techo medido:

| Nivel | Dónde | Piso |
|---|---|---|
| `sutil` | texto de lectura, precios | 4.5:1 (AA) |
| `marcado` | ≥24px e iconos | 3:1 (WCAG 1.4.3 / 1.4.11) |

La fusión es **por modo**, no global: la calibración se hizo en oscuro, donde
0.50 queda al borde (4.30:1), pero en claro la misma fusión cae a 2.83:1 porque
la paleta clara tiene menos margen de luminancia. Techos medidos: 0.48 / 0.62 en
oscuro, 0.32 / 0.47 en claro.

Consecuencia aceptada: **en modo claro el tallado es más sutil** y no hay forma
de igualarlo sin romper AA.

`marcado` no se usa en un dato que deba dominar aunque su tamaño lo permita: el
precio de un producto con `marcado` quedaba más apagado que el nombre, o sea el
tallado se comía justo la jerarquía que había que sostener. La jerarquía la dan
tamaño y peso; el tallado no es una herramienta de jerarquía.

### 7. Táctil de verdad

Corre en tablet: todo objetivo interactivo tiene **mínimo 48dp**. Vale para
pozos tocables, botones y filas de lista.

### 8. Sin componentes stock dentro del mundo comprometido

`NavigationRail`, `Card` y `VerticalDivider` de Material dentro de una
bandeja comprometida son fugas. La navegación es una banda de bandeja; las
celdas son pozos.

### 9. Confirmar está a la derecha; cancelar, a su izquierda

En un diálogo, la acción que **confirma** —guardar, crear, agregar, restaurar—
va abajo a la derecha, y cancelar a su izquierda, **en la misma fila**. Las
acciones sobre el objeto (reactivar, eliminar, cámara) bajan a las filas
siguientes de la columna izquierda: lejos del sitio al que va el pulgar a
confirmar, que es justo donde no se quiere un borrado.

No es estética. Para guardar se pulsa a la derecha sin mirar, y la app lo tenía
al revés en todas sus ventanas: en "crear categoría al vuelo" eso significa
pulsar Cancelar creyendo guardar. Lo encontró el dueño usándola.

Un diálogo cuya única acción es salir la deja a la derecha: sin nada que
confirmar no hay confusión posible, y es donde llega el dedo.

Y **nunca apilados en la misma columna**. El formulario de producto ponía
Cancelar encima de Guardar, las dos a la derecha: el pulgar caía en una o en
otra según la altura.

Lo vigila `test/orden_acciones_test.dart` con la geometría real, porque es una
convención que se rompe sola — al agregar un botón, lo natural es ponerlo donde
quepa.

### 10. Una etiqueta encoge antes que partirse

Los rótulos de una rejilla no se cortan a mitad de palabra. Si no entran,
**baja el tamaño de letra**; si ya entran, no se toca nada.

Venía de "Optimización" saliendo como "Optimizaci / ón" en un tile de ~69 dp:
Flutter parte por carácter cuando una palabra sola es más ancha que la línea.
`WulfEtiquetaAjustada` lo resuelve con `FittedBox`, y no con un `LayoutBuilder`
que mida el hueco — dentro del `IntrinsicHeight` de la cuadrícula, un
`LayoutBuilder` no sabe dar dimensiones intrínsecas y reventaba la pantalla
entera.

### 11. Un enlace se ve como un enlace

Color del acento **y** subrayado, las dos cosas. El color solo no basta: quien
no distingue ese tono ve texto normal, y el subrayado es la pista que no depende
del color. El acento cumple 4,5:1 sobre el pozo en los tres tintes y las dos
luminosidades — medido, y con prueba que lo vigila.

Puede no mostrar la dirección: la firma del autor dice su nombre, no la URL. Lo
que no puede es no parecer tocable. Un texto que hace algo al tocarlo sin
anunciarlo no es discreto, es indescifrable.

### 12. El foco se ve, y se ve con el mismo canto

Todo lo que se puede activar tiene que **decir cuándo lo tiene el foco** — el de
un mando, el de un teclado, el de un conmutador de accesibilidad. Y lo dice con
la señal que el sistema ya tiene para decir "este": el canto de la regla 1,
subido un escalón.

| estado | canto | relleno |
|---|---|---|
| reposo | cresta (metal apagado), 1 dp | pozo |
| seleccionado | metal pleno, 2 dp | pozo |
| enfocado | metal pleno, 3 dp | pozo aclarado |

Tres cosas que no son arbitrarias:

- **3 dp y no 2.** Un televisor se mira a tres metros. Un escalón que en la mano
  se distingue perfectamente, ahí no existe. Es el mismo motivo por el que el
  objetivo táctil es de 48 dp y no de 32.
- **Enfocado gana sobre seleccionado.** Navegando con el mando se *recorre* la
  selección, así que lo que hay que ver moverse es el foco. Por eso enfocado
  lleva **además** el relleno aclarado: los dos estados caen juntos todo el
  tiempo y tienen que seguir distinguiéndose.
- **Ni halo, ni sombra, ni agrandar.** Sería estrenar una señal nueva justo donde
  la regla 1 más importa, y una sombra a tres metros bajo la luz de una sala no
  se ve.

Lo que se aclara el pozo enfocado es lo mismo que se separa el tono del toque:
el aparato dice "acá estás" con el mismo material se llegue con el dedo o con el
mando.

Sin esto una app **no se puede usar en un televisor**. No es una mejora de
accesibilidad: el mando mueve el foco y nada en pantalla dice dónde está.

Dos consecuencias que se olvidan, y que cada paquete resuelve en una línea:

- **Solo se enfoca lo que se puede activar.** Una barra de progreso no entra en
  el recorrido; una de búsqueda sí. Un destino del mando en el que no hay nada
  que hacer convierte el recorrido en un laberinto.
- **Cada pantalla dice cuál es su punto de partida.** Sin foco inicial, la
  primera pulsación se gasta en que el sistema elija por su cuenta — y elige lo
  primero del árbol, que casi nunca es lo que se quiere hacer.

---

## Contraste verificado

Fórmula WCAG sobre los pares realmente usados (script en scratchpad de la
sesión; texto ≥4.5:1, bordes de superficie ≥3:1 por WCAG 1.4.11).

| Par | Claro | Oscuro |
|---|---|---|
| Texto principal en pozo | 11.12:1 | 12.85:1 |
| Texto secundario en pozo | 5.70:1 | 7.02:1 |
| Texto sobre bandeja | 14.40:1 | 15.07:1 |
| Texto atenuado sobre bandeja | 6.76:1 | 7.08:1 |
| Borde del pozo vs bandeja | 5.14:1 | 3.82:1 |
| Borde del pozo vs su fondo | 4.10:1 | 3.07:1 |

Los 3 metales, cada uno como texto sobre bandeja, como texto sobre pozo y
como relleno con su propio color encima: **18 pares, 0 fallas**. El peor caso
es 4.65:1 (oro como texto sobre pozo verde), por encima del mínimo AA.

---

## La mezcla de colores es propia en las tres

Los colores derivados —el canto, el tallado, el tono del toque— se calculan
mezclando **canal a canal en sRGB**, con una función escrita en cada paquete. No
se usa la de la plataforma.

Suena a manía hasta que se mide: Compose interpola en Oklab, y con la misma
fusión el tallado marcado en modo oscuro salía a **2,79:1** — por debajo del piso
de 3:1 que este documento exige— frente a 3,09:1 con la mezcla correcta. Y todo
eso **con las constantes de la paleta coincidiendo perfectamente** entre los tres
paquetes.

Por eso las pruebas de paridad comparan los colores DERIVADOS y no solo las
constantes. Una paleta idéntica no garantiza un sistema idéntico.

---
