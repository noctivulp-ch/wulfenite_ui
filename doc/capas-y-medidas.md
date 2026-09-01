# Capas, medidas y parpadeos

Lo que este documento recoge **no falla**. No lanza una excepción, no rompe una
prueba, no aparece en una revisión de código. Solo se ve mal — y a veces solo en
un aparato, o solo la primera vez que se abre una pantalla.

Todo lo de aquí se descubrió mirando una pantalla, no leyendo código. Por eso está
escrito: la próxima vez sale gratis.

---

## El orden de las capas de un pozo

Un pozo son cuatro cosas apiladas, y **el orden importa**:

```
1. recorte al radio     ← lo primero, o el fondo se sale por las esquinas
2. fondo (degradado)    ← el contacto ocupa solo la primera franja
3. canto de 1 dp        ← encima del fondo, o el fondo lo tapa a medias
4. contenido            ← con su aire, sin tocar el borde
```

Poner el recorte después del fondo deja **esquinas cuadradas asomando** bajo un
borde redondeado. Es sutil, se ve como "algo raro" antes de que uno sepa qué
mirar, y no hay prueba estática que lo cace.

### El degradado del contacto no es decorativo

Va del contacto al pozo en el **7 % de arriba**, no de arriba a abajo. Si se
estira a toda la altura, el pozo deja de parecer hundido y pasa a parecer una
tarjeta con un degradado — que es justo el aspecto que el sistema rechaza.

### Un tramo "por delante" no puede ser más oscuro que el fondo

En la barra, lo cargado se pinta con el **canto** —metal apagado— y no con el
tono de contacto. Con el contacto, que es **más oscuro que el carril**, el tramo
en memoria se veía como un **agujero en mitad de la barra** en vez de como
avance. Pasó, y se vio en la captura, no en el código.

La regla general: **lo que va "por delante" se pinta más callado, nunca más
oscuro que el fondo sobre el que va.**

---

## Colores que aparecen de la nada

**El color de contenido por defecto no es el que uno cree.** En Compose,
`LocalContentColor` fuera de un `Surface` de Material vale **negro**. Un `Text`
sin estilo dentro de una bandeja oscura sale negro sobre casi-negro: **invisible,
sin ningún error**.

Por eso `WulfBandeja` y `WulfPozo` **proveen el color de contenido** de su
superficie. Es la mitad de por qué existen como piezas y no como un simple fondo.

**Y los roles del tema hay que mapearlos todos.** Un rol sin mapear no se nota
leyendo: se nota en pantalla. La primera ejecución del ejemplo sacó una **píldora
azul violácea** en mitad de la laca — era `secondaryContainer` viniendo del
esquema morado de fábrica de Material 3.

Corolario para cualquier plataforma: **si el sistema tiene un color por defecto
para algo, hay que decidirlo, no heredarlo.** Lo mismo pasa en Qt con la línea
bajo las pestañas, que es del escritorio y ninguna hoja de estilo alcanza.

---

## Medidas que no se pueden calcular

Hay contenedores que **preguntan a sus hijos cuánto miden** antes de repartir. Y
hay hijos que no saben contestar.

| pide medida intrínseca | no sabe contestar |
|---|---|
| fila de altura compartida | cualquier cosa que **scrollee** |
| diálogo que se ajusta al contenido | una **cuadrícula perezosa** |

Cuando se cruzan, el resultado **no siempre es un error**:

- A veces revienta con un mensaje claro — *"no soporta dimensiones
  intrínsecas"*—, y eso es lo bueno.
- A veces sale **la pantalla en blanco**. Un diálogo con una cuadrícula dentro
  apareció **vacío**: solo el velo, sin contenido y sin un solo error en la
  consola. Se descubrió en el aparato.

Por eso la cuadrícula del sistema tiene un modo aparte —`llenarAlto`— para cuando
sus celdas contienen algo que scrollea: reparte el alto que el padre ya acotó, en
vez de preguntar cuánto miden.

**La regla de oro:** si una celda lleva una lista dentro, la fila no puede medir
por contenido. Y si un diálogo lleva una cuadrícula, el scroll va **dentro** del
contenido, no envolviendo el diálogo.

---

## Parpadeos

### El que se mide antes de dibujarse

`WulfEtiquetaAjustada` **dibuja, mira si desbordó, y encoge**. Eso significa que
existe un fotograma con el texto al tamaño grande antes de encogerlo.

Se nota poco porque es un solo fotograma, pero **se nota en una lista larga**:
las etiquetas "saltan" al aparecer. Tres cosas lo contienen:

- Se baja de **medio punto** en cada intento, no de uno: los saltos entre
  etiquetas vecinas dejan de ser visibles.
- Hay un **suelo** (9 sp). Sin él, una caja de 2 dp haría bajar el tamaño hasta
  cero probando, un fotograma por paso.
- El estado se reinicia con el **texto y el tamaño de partida**, no en cada
  recomposición: si no, la etiqueta volvería a medir cada vez que se redibuja y
  parpadearía sin parar.

En Flutter el problema no existe porque el ajuste es parte del layout, no una
medición en dos tiempos. **Misma pieza, distinto riesgo según la plataforma** —
conviene saberlo antes de portarla a una tercera.

### El que aparece dos veces

Dos avisos encimados se tapan entre sí y **no se lee ninguno**. Pasa cuando
alguien encadena acciones rápido, que es lo normal en una caja. Por eso hay
**uno solo a la vez**: el nuevo reemplaza al anterior.

### El que se queda donde estaba

Si la ventana cambia de tamaño **mientras** el aviso está entrando, la animación
conserva el destino que calculó con el ancho viejo y lo deja en el sitio
equivocado apenas siga corriendo. Refrescar la posición no basta: hay que
**refrescar también los extremos de la animación en curso**.

Vale para cualquier cosa animada hacia una posición calculada.

### El foco que llega tarde

Al comprobar en qué rol se entró, la comprobación corría en cuanto aparecía el
buscador — pero el botón que la distinguía **tarda más en pintarse**. Con un rol
fallaba; con el otro **daba verde por el motivo equivocado**, porque ahí lo que
se esperaba era justo una ausencia.

**Una pantalla a medio pintar puede dar el resultado correcto por casualidad.**
Es el peor tipo de fallo de temporización: solo se cae de un lado, así que parece
un problema del otro.

---

## Pestañas verticales

Funcionan, y la marca del activo va **en el borde que toca al panel** — que
cambia con la posición. Con una sola regla de subrayado, unas pestañas a la
izquierda quedaban subrayadas por abajo: la marca **cruzada** respecto del panel.

Comprobado contando píxeles: arriba el indicador sale de **56×2**, y a la
izquierda de **2×40**. Hay una prueba que lo vigila midiendo la imagen, porque es
exactamente lo que no sale en una prueba estática.

En Qt hace falta además `quitar_linea_de_pestanas`, por la línea del escritorio.

---

## Cómo se cazan estas cosas

Ninguna salió de leer código. Todas salieron de lo mismo:

1. **Mirar la pantalla de verdad**, en el aparato de verdad. El emulador y el
   escritorio esconden la mitad.
2. **Medir la imagen cuando se pueda.** Contar píxeles del color del acento dice
   si un indicador está donde debe; una captura comparada dice si el margen es el
   que se cree. Es lento de escribir una vez y automático para siempre.
3. **Desconfiar del verde.** Una prueba que pasa porque la pantalla no había
   terminado de pintarse pasa igual de verde que una que pasa por el motivo
   correcto.
