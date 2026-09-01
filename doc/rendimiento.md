# Rendimiento: qué cuesta, qué no ayuda, y cómo medirlo sin engañarse

Wulfenite pinta cosas caras a propósito. El relieve grabado dibuja **el mismo
glifo tres veces**, y eso se paga. Este documento dice cuánto, dónde está el
interruptor, y —lo que más vale— **cómo medir sin llegar a una conclusión
falsa**, que es lo que pasó la primera vez.

Los números salen de una app real de contabilidad corriendo en un teléfono de
gama baja, no de un banco sintético.

---

## Primero: ¿el costo está en armar o en pintar?

Es la pregunta que ordena todo lo demás, y se responde con el perfilador antes de
tocar una línea:

| dónde | qué significa | qué se arregla |
|---|---|---|
| **build** | el código que arma el árbol | menos trabajo por fotograma, menos reconstrucciones |
| **raster** | la GPU pintando | **qué** se dibuja: sombras, capas, mezclas |

Medido al desplazar un catálogo: **19,5 ms de raster contra 3,4 de build**. El
problema no estaba en el código que armaba la lista — estaba en lo que se
dibujaba. Optimizar el build ahí no habría movido la aguja.

**Si el costo está en raster, la solución es visual, no algorítmica.**

---

## Lo que cuesta: el relieve

Variantes medidas sobre la misma cuadrícula, mismo volumen:

| variante | raster medio | fotogramas lentos |
|---|---|---|
| como está | 12,9 ms | 50 / 125 |
| **sin relieve** | **7,9 ms** | **4 / 125** |
| sin velo superior | 11,3 ms | 30 / 125 |
| sin relieve ni velo | 8,7 ms | 14 / 125 |

El relieve es el costo dominante, y no es un descuido: pinta el glifo tres veces
—sombra, luz y glifo— y eso es exactamente lo que lo hace verse grabado.

**No se quita: se apaga por aparato.** Es adorno, no función, así que apagarlo no
cambia lo que la app hace. Y la preferencia es **local**: el teléfono viejo de la
caja puede necesitarlo y la tablet nueva no.

En la pantalla real la ganancia es menor que en el experimento aislado —17,5 →
15,2 ms de raster, y los fotogramas fuera de presupuesto de 54/81 a 33/84—
porque la pantalla pinta muchas más cosas. Sigue siendo la palanca más grande que
hay.

### Cómo se cablea

**Flutter**

```dart
MaterialApp(
  theme: buildWulfTheme(
    acento,
    efectos: WulfEfectos(apagados: {if (!relievePuesto) WulfEfectoPesado.tallado}),
  ),
)
```

**Compose**

```kotlin
WulfTheme(
    acento = acento,
    efectos = if (relievePuesto) WulfEfectos() else WulfEfectos(setOf(WulfEfectoPesado.TALLADO)),
) { … }
```

El componente no sabe que existe una pantalla de ajustes: la app traduce su
preferencia a `WulfEfectos` y la pasa al tema. Es lo que permite que
`WulfTallado` se apague sin que ninguna pantalla se entere.

### Para agregar otro efecto apagable

Una constante en `WulfEfectoPesado`, respetarla donde se pinta, y una entrada en
el panel de ajustes de la app. Nada más — el resto lo lleva el tema.

---

## Lo que NO ayuda

**Envolver cada tarjeta de una lista en un `RepaintBoundary`.** Medido: 13,3 ms
contra 12,9 de base, y 54 fotogramas lentos contra 50. **Sale peor.**

El motivo: Flutter ya envuelve cada ítem de una lista por su cuenta
—`addRepaintBoundaries` viene en `true` en `SliverChildBuilderDelegate`—, así que
añadirlo a mano solo duplica capas. Es un consejo que circula mucho y que en una
lista no aplica.

Lo mismo vale para el equivalente de cada plataforma: antes de aislar el
repintado a mano, comprobar si el contenedor ya lo hace.

---

## Cómo medir sin engañarse

**Esta es la parte que más cuesta y la que más se salta.**

El primer experimento midió cada variante **una sola vez y en orden**, y dio una
mejora enorme a las últimas — hasta *"el `RepaintBoundary` baja el raster un
68 %"*. Era **falso**. Lo que mejoraba entre variante y variante no era la
variante: era el proceso, con los shaders ya compilados y la GPU caliente.

El error llegó a aplicarse a las cuatro listas de la app antes de que una medida
sobre la pantalla real mostrara exactamente **cero** diferencia.

Tres reglas que salieron de ahí:

1. **Dos pasadas, y se descarta la primera.** Cualquier comparación de variantes
   gráficas dentro del mismo proceso lo necesita, o se mide el calentamiento.
2. **Mismo volumen de datos para todas las variantes.** Una lista con 8
   elementos y otra con 5 000 no se comparan.
3. **Confirmar en la pantalla real.** El experimento aislado dice cuál gana; solo
   la pantalla completa dice cuánto se gana de verdad. La diferencia entre 12,9 →
   7,9 y 17,5 → 15,2 es toda esa distancia.

Y una que es más de actitud: **una mejora que parece enorme casi siempre es un
error de medición.** Si una variante da un 68 %, lo primero es dudar del banco,
no celebrar.

---

## Qué no hace falta optimizar

Un reporte anual sobre miles de ventas tardaba 200 ms. Se probaron tres
alternativas; la única con ganancia real ahorraba unos 80 ms **en la consulta que
menos se pide**, a cambio de complicar el esquema.

Se dejó como estaba. Doscientos milisegundos en algo que se abre una vez al mes
no es un problema; el índice extra que había que mantener, sí. **Medir también
sirve para decidir no hacer nada**, y ese resultado vale tanto como el otro.
