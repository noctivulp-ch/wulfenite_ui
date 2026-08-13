# Cambios

## 0.1.0 — sin publicar

Adaptación a PySide6 del paquete Dart `wulf_flutter`.

- `tokens` — los mismos valores que el lado Dart, sin depender de Qt.
- `tema` — `aplicar()`: Fusion + `QPalette` + hoja de estilo generada.
- `reticula` — `Bandeja`, `Pozo`, `Cuadricula` de seis columnas, `Celda`, `Ancho`.
- `texto` — `Tallado` con `QPainter` y `EtiquetaAjustada`.
- `componentes` — `Acciones` con el orden decidido, `Dialogo`, `Enlace`,
  `Atenuado` y las tres formas de botón.
- 44 pruebas, incluidas contraste WCAG, paridad de tokens con el archivo Dart,
  orden de acciones por geometría y relieve por comparación de píxeles.
- Banco de estilo en `ejemplo/banco.py`.

Dos fallos que salieron durante el port, los dos cazados por pruebas:

- El canto del pozo se apagaba hacia la bandeja en vez de hacia el pozo, y eso
  lo dejaba por debajo de 3:1 contra la bandeja.
- `EtiquetaAjustada` medía con la fuente vieja y cambiaba el estado del widget
  dentro de `paintEvent`, que es camino a un bucle de repintado. Ahora el
  cálculo es una función pura y pública, `tamano_para()`.
