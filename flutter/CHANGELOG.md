# Cambios

## 0.1.0 — sin publicar

Primera versión. Sale de VulpTiketing, una app de contabilidad en producción, así
que el código llevaba meses probado en aparatos reales antes de mudarse acá.

- Tokens: espaciado en múltiplos de 8, dos radios, tres acabados de laca por dos
  luminosidades, tres metales.
- Superficies: `WulfBandeja`, `WulfPozo`, tinta del toque, velo de insets.
- Retícula de seis columnas con tres tamaños de pantalla.
- Texto: `WulfTallado` y `WulfEtiquetaAjustada`.
- Componentes: `WulfDialogo`, `WulfAcciones`, `WulfEnlace`, `WulfAviso`,
  `WulfFalla`.
- 69 pruebas, incluidas las que blindan contraste WCAG, orden de acciones y
  relieve.

Añadido durante la extracción, porque el banco de estilo lo pidió al portarlo:

- `WulfTratamiento` — grabado, realzado o plano.
- `WulfTallado.conColores` — luz y sombra a mano, para bancos de estilo.

Renombrado respecto de la app de origen: `AcentoTinte` → `WulfAcento`,
`EfectoPesado` → `WulfEfectoPesado`.
