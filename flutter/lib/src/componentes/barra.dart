import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../foco/foco.dart';
import '../tokens/tema.dart';

/// Alto del carril. Fino a proposito: la barra es un pozo alargado, no una
/// pieza con volumen propio.
const double wulfAltoBarra = 8;

/// Cuanto avanza un paso del mando o de una flecha, en tanto por uno.
///
/// Cinco por ciento y no uno: con el 1% recorrer una pelicula de dos horas
/// serian cien pulsaciones. Con el 5%, veinte — y el ajuste fino sale de
/// mantener pulsado, que el sistema repite solo.
const double wulfPasoBusqueda = 0.05;

/// Una barra de progreso: cuanto va de algo que avanza solo.
///
/// **No se puede enfocar**, y es deliberado: no hay nada que hacer con ella. Un
/// destino del mando en el que no se puede actuar convierte el recorrido en un
/// laberinto (regla 12).
///
/// Para lo que SI se controla —buscar en un video, mover un volumen— esta
/// [WulfBarraBusqueda].
class WulfBarraProgreso extends StatelessWidget {
  /// Una barra que solo informa.
  const WulfBarraProgreso({super.key, required this.valor, this.secundario});

  /// De 0 a 1. Se recorta, asi que un calculo que se pase no rompe la pantalla.
  final double valor;

  /// Lo cargado por delante de la posicion, si lo hay. Es lo que distingue "va
  /// por el minuto 3" de "tiene hasta el minuto 8 en memoria".
  final double? secundario;

  @override
  Widget build(BuildContext context) {
    final laca = lacaDe(context);
    return Semantics(
      value: '${(valor.clamp(0.0, 1.0) * 100).round()}%',
      child: CustomPaint(
        size: const Size(double.infinity, wulfAltoBarra),
        painter: _PintorDeBarra(
          avance: valor.clamp(0.0, 1.0),
          secundario: secundario?.clamp(0.0, 1.0),
          carril: laca.pozo,
          cargado: laca.paredCresta,
          relleno: Theme.of(context).colorScheme.primary,
          canto: laca.paredCresta,
          grosorCanto: WulfCanto.reposo,
        ),
      ),
    );
  }
}

/// Una barra que se controla: buscar en un video, mover un volumen.
///
/// Es la pieza central de un reproductor, y por eso lo importante aca es que
/// **funcione con el mando**: izquierda y derecha mueven de a [paso], y el foco
/// se ve con el canto del sistema (regla 12). Sin esto, en un televisor no hay
/// forma de buscar.
///
/// El objetivo tactil es de 48 aunque el carril mida 8: se dibuja fino y se toca
/// ancho, que es lo que permite acertarle con el dedo sin engordar la pieza.
class WulfBarraBusqueda extends StatefulWidget {
  /// Una barra que se puede mover.
  const WulfBarraBusqueda({
    super.key,
    required this.valor,
    required this.onCambio,
    this.secundario,
    this.habilitado = true,
    this.paso = wulfPasoBusqueda,
    this.autofoco = false,
  });

  /// Posicion actual, de 0 a 1.
  final double valor;

  /// Recibe el valor nuevo ya recortado a 0..1.
  final ValueChanged<double> onCambio;

  /// Lo cargado por delante de la posicion.
  final double? secundario;

  /// En falso no recibe el foco ni responde: es la barra de algo que todavia no
  /// se puede mover.
  final bool habilitado;

  /// Cuanto mueve una pulsacion.
  final double paso;

  /// Si toma el foco al aparecer la pantalla. Ver [WulfFocoInicial].
  final bool autofoco;

  @override
  State<WulfBarraBusqueda> createState() => _WulfBarraBusquedaState();
}

class _WulfBarraBusquedaState extends State<WulfBarraBusqueda> {
  bool _enfocado = false;

  double get _valor => widget.valor.clamp(0.0, 1.0);

  void _mover(double delta) {
    widget.onCambio((_valor + delta).clamp(0.0, 1.0));
  }

  KeyEventResult _tecla(FocusNode nodo, KeyEvent evento) {
    // Solo al pulsar: atender tambien el soltar moveria dos pasos por cada
    // pulsacion.
    if (!widget.habilitado || evento is! KeyDownEvent) return KeyEventResult.ignored;

    if (evento.logicalKey == LogicalKeyboardKey.arrowLeft) {
      _mover(-widget.paso);
      return KeyEventResult.handled;
    }
    if (evento.logicalKey == LogicalKeyboardKey.arrowRight) {
      _mover(widget.paso);
      return KeyEventResult.handled;
    }
    // Arriba y abajo NO se consumen: son lo que saca el foco de la barra.
    // Tragarselos dejaria al mando encerrado aca.
    return KeyEventResult.ignored;
  }

  @override
  Widget build(BuildContext context) {
    final laca = lacaDe(context);
    final tema = Theme.of(context);
    final acento = tema.colorScheme.primary;

    final carril = _enfocado ? wulfTonoEnfocado(laca.pozo, tema.brightness) : laca.pozo;

    return Semantics(
      slider: true,
      value: '${(_valor * 100).round()}%',
      child: Focus(
        canRequestFocus: widget.habilitado,
        autofocus: widget.autofoco,
        onKeyEvent: _tecla,
        onFocusChange: (tiene) => setState(() => _enfocado = tiene),
        child: GestureDetector(
          // Un toque en el carril lleva la posicion ahi: en pantalla tactil
          // nadie espera tener que arrastrar desde el punto exacto.
          onTapDown: widget.habilitado
              ? (detalle) {
                  final ancho = context.size?.width ?? 0;
                  if (ancho > 0) widget.onCambio((detalle.localPosition.dx / ancho).clamp(0.0, 1.0));
                }
              : null,
          onHorizontalDragUpdate: widget.habilitado
              ? (detalle) {
                  final ancho = context.size?.width ?? 0;
                  if (ancho > 0) widget.onCambio((detalle.localPosition.dx / ancho).clamp(0.0, 1.0));
                }
              : null,
          child: SizedBox(
            // Fino de dibujo, ancho de toque.
            height: wulfObjetivoTactilMin,
            width: double.infinity,
            child: CustomPaint(
              painter: _PintorDeBarra(
                avance: _valor,
                secundario: widget.secundario?.clamp(0.0, 1.0),
                carril: carril,
                cargado: laca.paredCresta,
                relleno: widget.habilitado ? acento : laca.paredCresta,
                canto: _enfocado ? acento : laca.paredCresta,
                grosorCanto: _enfocado ? WulfCanto.enfocado : WulfCanto.reposo,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// El dibujo, compartido por las dos.
///
/// La barra es un **pozo alargado**: carril hundido con su canto y el avance en
/// metal. Ni sombra ni degradado de volumen — la profundidad la carga el canto
/// (regla 1), tambien aca.
class _PintorDeBarra extends CustomPainter {
  const _PintorDeBarra({
    required this.avance,
    required this.secundario,
    required this.carril,
    required this.cargado,
    required this.relleno,
    required this.canto,
    required this.grosorCanto,
  });

  final double avance;
  final double? secundario;
  final Color carril;
  final Color cargado;
  final Color relleno;
  final Color canto;
  final double grosorCanto;

  @override
  void paint(Canvas canvas, Size size) {
    final arriba = (size.height - wulfAltoBarra) / 2;
    final radio = Radius.circular(wulfAltoBarra / 2);

    void tramo(double hasta, Color color) {
      if (hasta <= 0) return;
      canvas.drawRRect(
        RRect.fromRectAndRadius(
          Rect.fromLTWH(0, arriba, size.width * hasta, wulfAltoBarra),
          radio,
        ),
        Paint()..color = color,
      );
    }

    tramo(1, carril);
    // Lo cargado se pinta con el CANTO, que es el metal apagado: "metal, pero
    // mas callado", que es exactamente lo que significa. Con el tono de contacto
    // —mas oscuro que el carril— el tramo cargado se veia como un agujero en
    // mitad de la barra en vez de como avance.
    //
    // Y va DEBAJO del avance: pintado encima taparia la posicion justo cuando la
    // memoria va por delante, que es siempre.
    if (secundario != null) tramo(secundario!, cargado);
    tramo(avance, relleno);

    canvas.drawRRect(
      RRect.fromRectAndRadius(
        Rect.fromLTWH(0, arriba, size.width, wulfAltoBarra),
        radio,
      ),
      Paint()
        ..color = canto
        ..style = PaintingStyle.stroke
        ..strokeWidth = grosorCanto,
    );
  }

  @override
  bool shouldRepaint(_PintorDeBarra viejo) =>
      viejo.avance != avance ||
      viejo.secundario != secundario ||
      viejo.carril != carril ||
      viejo.relleno != relleno ||
      viejo.canto != canto ||
      viejo.grosorCanto != grosorCanto;
}
