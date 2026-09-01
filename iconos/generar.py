#!/usr/bin/env python3
"""Genera el juego de iconos de Wulfenite.

Sin dependencias a propósito: escribe el SVG como texto. Una librería de dibujo
obligaría a montar un entorno para regenerar cuatro líneas, y esto lo tiene que
poder correr cualquiera de los tres paquetes.

    python3 iconos/generar.py

Las reglas del trazo están en `doc/iconos.md`. Las dos que más cuestan de
recordar, y que este archivo garantiza por construcción:

1. **Nunca `<style>` ni clases CSS.** Los atributos de presentación van en CADA
   elemento. `flutter_svg` no soporta `<style>` y lo ignora **en silencio**: las
   formas salen sin trazo y con relleno negro. No se ve en un visor normal —solo
   en la app— así que es un fallo que se descubre tarde.
2. **Un solo color**, que no es el color final: se retiñe en tiempo de ejecución.
"""

from __future__ import annotations

import math
from pathlib import Path

# ── Las constantes del trazo ──────────────────────────────────────────────────
GRILLA = 24
TRAZO = 1.75
COLOR = "#1A1A1A"

#: Margen que ningún icono pisa. Con 24 de grilla y trazo de 1.75, dibujar hasta
#: el borde deja medio trazo fuera del lienzo y el icono se ve recortado cuando
#: la app lo escala.
MARGEN = 2.5

SALIDA = Path(__file__).parent

_ATRIBUTOS = (
    f'fill="none" stroke="{COLOR}" stroke-width="{TRAZO}" '
    'stroke-linecap="round" stroke-linejoin="round"'
)
_RELLENO = f'fill="{COLOR}" stroke="none"'


# ── Primitivas ────────────────────────────────────────────────────────────────
def linea(x1, y1, x2, y2) -> str:
    return f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" {_ATRIBUTOS}/>'


def ruta(d: str) -> str:
    return f'<path d="{d}" {_ATRIBUTOS}/>'


def circulo(cx, cy, r) -> str:
    return f'<circle cx="{cx}" cy="{cy}" r="{r}" {_ATRIBUTOS}/>'


def punto(cx, cy, r=1.1) -> str:
    """Un círculo RELLENO.

    Los detalles pequeños se rellenan a propósito: a 16 px un círculo de trazo se
    convierte en una mancha gris y deja de leerse como un punto.
    """
    return f'<circle cx="{cx}" cy="{cy}" r="{r}" {_RELLENO}/>'


def rectangulo(x, y, ancho, alto, rx=1.5) -> str:
    return (f'<rect x="{x}" y="{y}" width="{ancho}" height="{alto}" rx="{rx}" '
            f'{_ATRIBUTOS}/>')


def poligono(puntos: str) -> str:
    return f'<polyline points="{puntos}" {_ATRIBUTOS}/>'


def triangulo_relleno(puntos: str) -> str:
    return f'<polygon points="{puntos}" {_RELLENO}/>'


def flecha(x1, y1, x2, y2, cabeza=4.0) -> str:
    """Una línea con punta.

    La punta se calcula sobre el ángulo real, así que una flecha diagonal tiene la
    punta bien puesta sin escribirla a mano.
    """
    angulo = math.atan2(y2 - y1, x2 - x1)
    abertura = math.radians(30)
    ax = x2 - cabeza * math.cos(angulo - abertura)
    ay = y2 - cabeza * math.sin(angulo - abertura)
    bx = x2 - cabeza * math.cos(angulo + abertura)
    by = y2 - cabeza * math.sin(angulo + abertura)
    return (linea(x1, y1, x2, y2)
            + poligono(f"{ax:.2f},{ay:.2f} {x2},{y2} {bx:.2f},{by:.2f}"))


def chevron(cx, cy, direccion: str, brazo=4.0) -> str:
    """Una punta sola, sin línea. Para "hay más por aquí"."""
    d = {"arriba": (0, -1), "abajo": (0, 1), "izquierda": (-1, 0), "derecha": (1, 0)}[direccion]
    if d[0]:  # horizontal
        return poligono(f"{cx - d[0]*brazo/2},{cy - brazo} "
                        f"{cx + d[0]*brazo/2},{cy} "
                        f"{cx - d[0]*brazo/2},{cy + brazo}")
    return poligono(f"{cx - brazo},{cy - d[1]*brazo/2} "
                    f"{cx},{cy + d[1]*brazo/2} "
                    f"{cx + brazo},{cy - d[1]*brazo/2}")


def escribir(nombre: str, *piezas: str) -> None:
    cuerpo = "".join(piezas)
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{GRILLA}" '
           f'height="{GRILLA}" viewBox="0 0 {GRILLA} {GRILLA}">{cuerpo}</svg>\n')
    (SALIDA / f"{nombre}.svg").write_text(svg, encoding="utf-8")


# ── Flechas y direcciones ─────────────────────────────────────────────────────
def flechas() -> None:
    escribir("flecha-arriba", flecha(12, 20, 12, 4))
    escribir("flecha-abajo", flecha(12, 4, 12, 20))
    escribir("flecha-izquierda", flecha(20, 12, 4, 12))
    escribir("flecha-derecha", flecha(4, 12, 20, 12))

    escribir("chevron-arriba", chevron(12, 13, "arriba", 5))
    escribir("chevron-abajo", chevron(12, 11, "abajo", 5))
    escribir("chevron-izquierda", chevron(13, 12, "izquierda", 5))
    escribir("chevron-derecha", chevron(11, 12, "derecha", 5))

    # Volver y avanzar: flecha con curva, para "deshacer el paso" y no "moverse".
    escribir("volver", ruta("M 9 7 L 4.5 11.5 L 9 16"),
             ruta("M 4.5 11.5 H 14 A 5.5 5.5 0 0 1 14 22.5 H 11"))
    escribir("avanzar", ruta("M 15 7 L 19.5 11.5 L 15 16"),
             ruta("M 19.5 11.5 H 10 A 5.5 5.5 0 0 0 10 22.5 H 13"))

    escribir("entrar", ruta("M 13 3.5 H 19 A 1.5 1.5 0 0 1 20.5 5 V 19 A 1.5 1.5 0 0 1 19 20.5 H 13"),
             flecha(3.5, 12, 13.5, 12))
    escribir("salir", ruta("M 11 3.5 H 5 A 1.5 1.5 0 0 0 3.5 5 V 19 A 1.5 1.5 0 0 0 5 20.5 H 11"),
             flecha(10.5, 12, 20.5, 12))

    escribir("expandir", flecha(11, 13, 4.5, 19.5, 3.4), flecha(13, 11, 19.5, 4.5, 3.4))
    escribir("contraer", flecha(4.5, 19.5, 11, 13, 3.4), flecha(19.5, 4.5, 13, 11, 3.4))

    escribir("ordenar", flecha(7, 20, 7, 4.5, 3.4), flecha(17, 4.5, 17, 20, 3.4))


def _puntas_del_triangulo(direccion: str, medio=5.4, alto=5.6) -> str:
    """Los tres vértices de un triángulo indicador, centrado en la grilla.

    Un poco más alto que ancho a propósito: un triángulo equilátero se lee como
    una señal de aviso, y este es un puntero.
    """
    cx = cy = 12
    if direccion == "derecha":
        return f"{cx - alto},{cy - medio} {cx + alto},{cy} {cx - alto},{cy + medio}"
    if direccion == "izquierda":
        return f"{cx + alto},{cy - medio} {cx - alto},{cy} {cx + alto},{cy + medio}"
    if direccion == "abajo":
        return f"{cx - medio},{cy - alto} {cx + medio},{cy - alto} {cx},{cy + alto}"
    return f"{cx - medio},{cy + alto} {cx + medio},{cy + alto} {cx},{cy - alto}"


def triangulos() -> None:
    """Punteros direccionales, en hueco y en lleno.

    No sobran teniendo chevrones: **dicen cosas distintas**. Un chevron es una V
    abierta y sugiere continuidad —"sigue por aquí", "hay más"—; un triángulo es
    una forma cerrada y señala —"estás aquí", "esto es lo elegido"—. En un menú
    que se recorre con mando, el triángulo lleno es el cursor y el hueco es el
    destino disponible.
    """
    for direccion in ("arriba", "abajo", "izquierda", "derecha"):
        puntas = _puntas_del_triangulo(direccion)
        # Hueco: `Z` cierra la figura. Sin cerrar quedaría un chevron con una
        # pata de más, que es justo lo que no se quiere.
        primero, segundo, tercero = puntas.split()
        escribir(f"triangulo-{direccion}",
                 ruta(f"M {primero.replace(',', ' ')} L {segundo.replace(',', ' ')} "
                      f"L {tercero.replace(',', ' ')} Z"))
        escribir(f"triangulo-{direccion}-lleno", triangulo_relleno(puntas))


# ── Señales de estado ─────────────────────────────────────────────────────────
def senales() -> None:
    escribir("correcto", poligono("5,12.5 10,17.5 19,7"))
    escribir("error", linea(6.5, 6.5, 17.5, 17.5), linea(17.5, 6.5, 6.5, 17.5))

    # El triángulo del aviso lleva el signo partido en dos: la barra y el punto.
    # Un "!" de una pieza a 16 px se lee como una raya sin más.
    escribir("aviso",
             ruta("M 12 3.5 L 21.5 20 H 2.5 Z"),
             linea(12, 9.5, 12, 14),
             punto(12, 17, 1.0))
    escribir("informacion", circulo(12, 12, 8.5), punto(12, 7.8, 1.0),
             linea(12, 11, 12, 16.5))

    escribir("pendiente", circulo(12, 12, 8.5), poligono("12,7 12,12 15.5,14"))
    escribir("bloqueado",
             rectangulo(5, 10.5, 14, 10, 2),
             ruta("M 8 10.5 V 7.5 A 4 4 0 0 1 16 7.5 V 10.5"),
             punto(12, 15.5, 1.1))
    escribir("desbloqueado",
             rectangulo(5, 10.5, 14, 10, 2),
             ruta("M 8 10.5 V 7.5 A 4 4 0 0 1 15.5 5.8"),
             punto(12, 15.5, 1.1))

    escribir("visible",
             ruta("M 2.5 12 C 5.5 7 8.8 4.8 12 4.8 C 15.2 4.8 18.5 7 21.5 12 "
                  "C 18.5 17 15.2 19.2 12 19.2 C 8.8 19.2 5.5 17 2.5 12 Z"),
             circulo(12, 12, 3))
    escribir("oculto",
             ruta("M 4.5 8.5 C 3.7 9.5 3 10.7 2.5 12 C 5.5 17 8.8 19.2 12 19.2 "
                  "C 13.4 19.2 14.8 18.8 16.1 18.1"),
             ruta("M 19.4 15.2 C 20.2 14.3 21 13.2 21.5 12 C 18.5 7 15.2 4.8 12 4.8 "
                  "C 11.2 4.8 10.4 4.9 9.6 5.2"),
             linea(4, 4, 20, 20))

    escribir("favorito",
             ruta("M 12 20.2 L 4.6 13 A 4.6 4.6 0 0 1 12 7.6 A 4.6 4.6 0 0 1 19.4 13 Z"))
    escribir("destacado",
             ruta("M 12 3.8 L 14.5 9.3 L 20.4 10 L 16 14 L 17.2 19.9 L 12 17 "
                  "L 6.8 19.9 L 8 14 L 3.6 10 L 9.5 9.3 Z"))


# ── Acciones ──────────────────────────────────────────────────────────────────
def acciones() -> None:
    escribir("anadir", linea(12, 5, 12, 19), linea(5, 12, 19, 12))
    escribir("quitar", linea(5, 12, 19, 12))
    escribir("anadir-circulo", circulo(12, 12, 8.5), linea(12, 8, 12, 16), linea(8, 12, 16, 12))
    escribir("quitar-circulo", circulo(12, 12, 8.5), linea(8, 12, 16, 12))

    escribir("editar",
             ruta("M 4 20 L 4.8 15.8 L 16 4.6 A 2 2 0 0 1 19.4 7.9 L 8.2 19.2 Z"),
             linea(14.4, 6.2, 17.8, 9.6))

    escribir("eliminar",
             ruta("M 5.5 7 H 18.5"),
             ruta("M 9 7 V 5.2 A 1.2 1.2 0 0 1 10.2 4 H 13.8 A 1.2 1.2 0 0 1 15 5.2 V 7"),
             ruta("M 7 7 L 7.9 19.2 A 1.5 1.5 0 0 0 9.4 20.5 H 14.6 "
                  "A 1.5 1.5 0 0 0 16.1 19.2 L 17 7"),
             linea(10.5, 10.5, 10.5, 17), linea(13.5, 10.5, 13.5, 17))

    escribir("buscar", circulo(10.5, 10.5, 6), linea(15, 15, 20, 20))
    # Cerrado con `Z`: abierto, el embudo no se lee como tal — parecía una
    # bandera. Una forma que representa un objeto tiene que cerrar.
    escribir("filtrar",
             ruta("M 3.5 5 H 20.5 L 13.8 12.8 V 20.3 L 10.2 18.3 V 12.8 Z"))

    escribir("guardar",
             ruta("M 4.5 5.5 A 1.5 1.5 0 0 1 6 4 H 16 L 20 8 V 18.5 "
                  "A 1.5 1.5 0 0 1 18.5 20 H 6 A 1.5 1.5 0 0 1 4.5 18.5 Z"),
             rectangulo(8.5, 4, 7, 5, 0.8),
             rectangulo(8, 13, 8, 7, 0.8))

    escribir("copiar", rectangulo(8.5, 3.5, 12, 12, 2),
             ruta("M 15.5 20.5 H 5 A 1.5 1.5 0 0 1 3.5 19 V 8.5"))

    escribir("compartir", punto(6, 12, 2.4), punto(17.5, 6.5, 2.4), punto(17.5, 17.5, 2.4),
             linea(8.2, 10.8, 15.4, 7.6), linea(8.2, 13.2, 15.4, 16.4))

    escribir("descargar", flecha(12, 4, 12, 15.5),
             ruta("M 4.5 15 V 19 A 1.5 1.5 0 0 0 6 20.5 H 18 A 1.5 1.5 0 0 0 19.5 19 V 15"))
    escribir("subir", flecha(12, 15.5, 12, 4),
             ruta("M 4.5 15 V 19 A 1.5 1.5 0 0 0 6 20.5 H 18 A 1.5 1.5 0 0 0 19.5 19 V 15"))

    escribir("actualizar",
             ruta("M 20 12 A 8 8 0 1 1 17.2 5.9"),
             poligono("13.5,5 17.8,5.6 18.4,1.4"))

    escribir("mas-opciones", punto(12, 5.5), punto(12, 12), punto(12, 18.5))
    escribir("menu", linea(4, 7, 20, 7), linea(4, 12, 20, 12), linea(4, 17, 20, 17))
    escribir("cerrar", linea(6, 6, 18, 18), linea(18, 6, 6, 18))


# ── Reproductor ───────────────────────────────────────────────────────────────
def reproductor() -> None:
    # Los mandos van RELLENOS: a tamaño de mando de televisor un triángulo de
    # trazo se confunde con una flecha, y el relleno lo separa del resto del set.
    escribir("reproducir", triangulo_relleno("7.5,4.5 19.5,12 7.5,19.5"))
    escribir("pausa", rectangulo(7, 4.5, 3.6, 15, 1), rectangulo(13.4, 4.5, 3.6, 15, 1))
    escribir("parar", rectangulo(6, 6, 12, 12, 1.5))

    escribir("anterior", triangulo_relleno("18.5,5 18.5,19 8,12"),
             rectangulo(5, 5, 2.2, 14, 0.8))
    escribir("siguiente", triangulo_relleno("5.5,5 5.5,19 16,12"),
             rectangulo(16.8, 5, 2.2, 14, 0.8))

    escribir("retroceder", triangulo_relleno("12,5 12,19 3.5,12"),
             triangulo_relleno("20.5,5 20.5,19 12,12"))
    escribir("adelantar", triangulo_relleno("3.5,5 3.5,19 12,12"),
             triangulo_relleno("12,5 12,19 20.5,12"))

    escribir("volumen",
             ruta("M 3.5 9.5 H 7 L 11.5 5.5 V 18.5 L 7 14.5 H 3.5 Z"),
             ruta("M 14.5 9.5 A 3.5 3.5 0 0 1 14.5 14.5"),
             ruta("M 17 7 A 7 7 0 0 1 17 17"))
    escribir("silencio",
             ruta("M 3.5 9.5 H 7 L 11.5 5.5 V 18.5 L 7 14.5 H 3.5 Z"),
             linea(15, 9.5, 20, 14.5), linea(20, 9.5, 15, 14.5))

    escribir("pantalla-completa",
             ruta("M 4 9 V 5 A 1 1 0 0 1 5 4 H 9"),
             ruta("M 15 4 H 19 A 1 1 0 0 1 20 5 V 9"),
             ruta("M 20 15 V 19 A 1 1 0 0 1 19 20 H 15"),
             ruta("M 9 20 H 5 A 1 1 0 0 1 4 19 V 15"))


# ── Objetos y contenedores ────────────────────────────────────────────────────
def objetos() -> None:
    escribir("archivo",
             ruta("M 6 3.5 H 14 L 18.5 8 V 20.5 H 6 A 1.5 1.5 0 0 1 4.5 19 V 5 "
                  "A 1.5 1.5 0 0 1 6 3.5 Z"),
             poligono("14,3.5 14,8 18.5,8"))
    escribir("carpeta",
             ruta("M 3.5 6.5 A 1.5 1.5 0 0 1 5 5 H 9.5 L 12 7.5 H 19 "
                  "A 1.5 1.5 0 0 1 20.5 9 V 18 A 1.5 1.5 0 0 1 19 19.5 H 5 "
                  "A 1.5 1.5 0 0 1 3.5 18 Z"))
    escribir("imagen", rectangulo(3.5, 4.5, 17, 15, 2), punto(9, 10, 1.4),
             poligono("5,17 10,12 13.5,15.5 16,13 20,17"))

    escribir("lista", punto(5, 7, 1.0), punto(5, 12, 1.0), punto(5, 17, 1.0),
             linea(9, 7, 20, 7), linea(9, 12, 20, 12), linea(9, 17, 20, 17))
    escribir("cuadricula", rectangulo(4, 4, 7, 7, 1.2), rectangulo(13, 4, 7, 7, 1.2),
             rectangulo(4, 13, 7, 7, 1.2), rectangulo(13, 13, 7, 7, 1.2))
    escribir("tabla", rectangulo(3.5, 4.5, 17, 15, 2),
             linea(3.5, 9.5, 20.5, 9.5), linea(9.5, 9.5, 9.5, 19.5))

    escribir("ajustes", _engranaje())
    # El mismo dibujo SIN el aro es un sol, no un engranaje: la primera versión
    # de "ajustes" se leía como brillo. Se conserva con el nombre que le toca.
    escribir("brillo", circulo(12, 12, 3.2), _rayos(5.6, 9.2))
    escribir("usuario", circulo(12, 8, 4),
             ruta("M 4.5 20.5 A 7.5 7.5 0 0 1 19.5 20.5"))
    escribir("casa", ruta("M 3.5 11 L 12 4 L 20.5 11 V 19 A 1.5 1.5 0 0 1 19 20.5 "
                          "H 5 A 1.5 1.5 0 0 1 3.5 19 Z"),
             ruta("M 9.5 20.5 V 14 H 14.5 V 20.5"))
    escribir("calendario", rectangulo(3.5, 5.5, 17, 15, 2),
             linea(3.5, 10.5, 20.5, 10.5), linea(8, 3, 8, 7), linea(16, 3, 16, 7))
    escribir("etiqueta",
             ruta("M 11 3.5 H 19.5 A 1 1 0 0 1 20.5 4.5 V 13 L 11.5 21.5 "
                  "A 1.4 1.4 0 0 1 9.5 21.5 L 2.5 14.5 A 1.4 1.4 0 0 1 2.5 12.5 Z"),
             punto(16.5, 7.5, 1.2))
    escribir("reloj", circulo(12, 12, 8.5), poligono("12,6.5 12,12 16,14"))


def _rayos(desde: float, hasta: float, cuantos: int = 8) -> str:
    """Segmentos radiales repartidos alrededor del centro."""
    piezas = []
    for i in range(cuantos):
        a = math.radians(i * 360 / cuantos)
        piezas.append(linea(
            round(12 + desde * math.cos(a), 2), round(12 + desde * math.sin(a), 2),
            round(12 + hasta * math.cos(a), 2), round(12 + hasta * math.sin(a), 2)))
    return "".join(piezas)


def _engranaje() -> str:
    """Hub, aro y dientes.

    **El aro es lo que lo distingue de un sol.** Sin él, unos segmentos radiales
    alrededor de un círculo se leen como brillo — pasó, y el icono decía otra cosa
    que la que nombraba.
    """
    return circulo(12, 12, 3) + circulo(12, 12, 6.9) + _rayos(6.9, 9.3)


# ── Conectividad ──────────────────────────────────────────────────────────────
def conectividad() -> None:
    escribir("wifi",
             ruta("M 3 9 A 13 13 0 0 1 21 9"),
             ruta("M 6.5 12.8 A 8 8 0 0 1 17.5 12.8"),
             ruta("M 9.8 16.4 A 3.5 3.5 0 0 1 14.2 16.4"),
             punto(12, 19.5, 1.2))
    escribir("sin-conexion",
             ruta("M 6.5 12.8 A 8 8 0 0 1 11 10.3"),
             ruta("M 13.6 10.4 A 8 8 0 0 1 17.5 12.8"),
             punto(12, 19.5, 1.2),
             linea(4, 4, 20, 20))
    escribir("sincronizar",
             ruta("M 19.5 10.5 A 7.5 7.5 0 0 0 6.8 6.6"),
             poligono("6.5,3 6.5,7 10.5,7"),
             ruta("M 4.5 13.5 A 7.5 7.5 0 0 0 17.2 17.4"),
             poligono("17.5,21 17.5,17 13.5,17"))
    escribir("enlace",
             ruta("M 10.5 13.5 A 3.8 3.8 0 0 0 16 13.5 L 19 10.5 A 3.9 3.9 0 0 0 13.5 5 L 12 6.5"),
             ruta("M 13.5 10.5 A 3.8 3.8 0 0 0 8 10.5 L 5 13.5 A 3.9 3.9 0 0 0 10.5 19 L 12 17.5"))
    escribir("dispositivo", rectangulo(7, 3, 10, 18, 2), linea(10.5, 6, 13.5, 6),
             punto(12, 18, 1.0))
    escribir("nube",
             ruta("M 7.5 18.5 A 4.5 4.5 0 0 1 7.9 9.6 A 5.6 5.6 0 0 1 18.2 11 "
                  "A 3.8 3.8 0 0 1 17.5 18.5 Z"))


def main() -> None:
    for grupo in (flechas, triangulos, senales, acciones, reproductor, objetos,
                  conectividad):
        grupo()
    hechos = sorted(p.name for p in SALIDA.glob("*.svg"))
    print(f"{len(hechos)} iconos en {SALIDA}")


if __name__ == "__main__":
    main()
