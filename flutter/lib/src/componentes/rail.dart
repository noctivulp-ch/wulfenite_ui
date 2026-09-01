import 'package:flutter/material.dart';

import '../foco/foco.dart';
import '../texto/etiqueta_ajustada.dart';
import '../tokens/tema.dart';

/// Un destino del rail.
class WulfDestino {
  /// Un destino, con su etiqueta y opcionalmente un icono encima.
  const WulfDestino({required this.etiqueta, this.icono});

  /// Lo que se lee. Encoge antes que partirse.
  final String etiqueta;

  /// Icono opcional, encima de la etiqueta.
  final Widget? icono;
}

/// Ancho por defecto del rail. Cabe una palabra corta sin encoger.
const double wulfAnchoRail = 88;

/// La columna de navegacion: los destinos de la app, en vertical.
///
/// **Es la respuesta del sistema a la navegacion lateral**, y existe en vez de
/// unas pestañas verticales por un motivo concreto: las pestañas verticales
/// **rotan el texto**, y leer de lado es incomodo. Aca el texto va horizontal
/// siempre.
///
/// En Qt esa rotacion ademas no tiene arreglo con el tema puesto —Qt descarta el
/// estilo delegado en cuanto una hoja de estilo toca las pestañas— asi que el
/// rail es la unica forma de tener navegacion vertical con el acabado del sistema
/// en las tres plataformas. Ver `doc/paridad.md`.
///
/// Reglas de la pieza:
/// - **Vive en la bandeja, no en un pozo.** Es cromo, no contenido.
/// - **La marca del activo va en el borde que toca al contenido** — el derecho—,
///   con el metal y el grosor de seleccion. Misma regla que en las pestañas: la
///   marca señala hacia lo que se esta mirando.
/// - **Cada destino se enfoca** y lo muestra como todo lo demas (regla 12).
/// - **48 de alto minimo**, y el texto encoge antes que partirse.
class WulfRail extends StatelessWidget {
  /// La columna de destinos.
  const WulfRail({
    super.key,
    required this.destinos,
    required this.seleccionado,
    required this.onSeleccionar,
    this.ancho = wulfAnchoRail,
    this.television = false,
  });

  /// Los destinos, en orden de lectura.
  final List<WulfDestino> destinos;

  /// Cual esta activo.
  final int seleccionado;

  /// Se llama con el indice elegido.
  final ValueChanged<int> onSeleccionar;

  /// Cuanto ocupa de ancho.
  final double ancho;

  /// En un televisor el canto del foco engorda. Hay que decirlo: Flutter no
  /// expone el `uiMode` de Android.
  final bool television;

  @override
  Widget build(BuildContext context) {
    final laca = lacaDe(context);
    return Container(
      width: ancho,
      color: laca.bandeja,
      padding: const EdgeInsets.symmetric(vertical: WulfSpacing.unidad),
      child: Column(
        children: [
          for (var i = 0; i < destinos.length; i++)
            _Destino(
              destino: destinos[i],
              activo: i == seleccionado,
              television: television,
              onElegir: () => onSeleccionar(i),
            ),
        ],
      ),
    );
  }
}

class _Destino extends StatefulWidget {
  const _Destino({
    required this.destino,
    required this.activo,
    required this.television,
    required this.onElegir,
  });

  final WulfDestino destino;
  final bool activo;
  final bool television;
  final VoidCallback onElegir;

  @override
  State<_Destino> createState() => _DestinoState();
}

class _DestinoState extends State<_Destino> {
  bool _enfocado = false;

  @override
  Widget build(BuildContext context) {
    final laca = lacaDe(context);
    final tema = Theme.of(context);
    final acento = tema.colorScheme.primary;

    final grosor = _enfocado
        ? (widget.television ? WulfCanto.enfocadoTelevision : WulfCanto.enfocado)
        : (widget.activo ? WulfCanto.seleccionado : 0.0);

    return Padding(
      padding: const EdgeInsets.only(bottom: WulfSpacing.unidad / 2),
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: _enfocado ? wulfTonoEnfocado(laca.bandeja, tema.brightness) : laca.bandeja,
          // La marca en el borde DERECHO: el que toca al contenido. Misma regla
          // que en las pestañas — señala hacia lo que se esta mirando.
          border: grosor > 0
              ? Border(right: BorderSide(color: acento, width: grosor))
              : null,
        ),
        child: Material(
          type: MaterialType.transparency,
          child: InkWell(
            onTap: widget.onElegir,
            onFocusChange: (tiene) => setState(() => _enfocado = tiene),
            // El resaltado de foco de fabrica es un rectangulo translucido, que
            // es la unica señal que este sistema no usa.
            focusColor: Colors.transparent,
            child: ConstrainedBox(
              constraints: const BoxConstraints(minHeight: wulfObjetivoTactilMin),
              child: Padding(
                padding: const EdgeInsets.all(WulfSpacing.unidad),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (widget.destino.icono != null) ...[
                      widget.destino.icono!,
                      const SizedBox(height: WulfSpacing.unidad / 2),
                    ],
                    WulfEtiquetaAjustada(
                      widget.destino.etiqueta,
                      // Activo en texto pleno, el resto atenuado: el mismo
                      // reparto que en las pestañas.
                      estilo: tema.textTheme.bodyMedium?.copyWith(
                        color: widget.activo
                            ? laca.textoEnBandeja
                            : laca.textoAtenuadoEnBandeja,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
