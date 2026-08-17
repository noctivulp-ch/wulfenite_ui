import 'package:flutter/material.dart';

import '../foco/foco.dart';
import '../tokens/tema.dart';

/// El plano de la bandeja: un unico contenedor continuo por pantalla, con el
/// labio exterior (`rim`) alrededor. Los pozos van dentro, separados entre si
/// por `WulfSpacing.pared` — ese hueco NO es fondo de pagina asomandose, es
/// material de bandeja (`doc/reglas.md`, regla 2).
///
/// No lleva color de fondo propio porque el `Scaffold` ya pinta la bandeja
/// (`scaffoldBackgroundColor`); esto solo aporta el labio y el radio exterior
/// cuando la bandeja esta anidada dentro de otra superficie.
class WulfBandeja extends StatelessWidget {
  /// Envuelve [child] en el labio de la bandeja.
  const WulfBandeja({super.key, required this.child, this.rim = true});

  /// Lo que va dentro de la bandeja, normalmente una retícula de pozos.
  final Widget child;

  /// Si la bandeja aporta su labio exterior. En falso cuando el contenedor
  /// padre ya lo puso y esta es una subdivision interna.
  final bool rim;

  @override
  Widget build(BuildContext context) {
    if (!rim) return child;
    return Padding(padding: const EdgeInsets.all(WulfSpacing.rim), child: child);
  }
}

/// Un compartimento tallado en la bandeja.
///
/// Tres senales, en orden de importancia, y ninguna es una sombra proyectada:
/// 1. `paredCresta` como contorno de 1px — la cresta de la pared atrapando la
///    luz. Es la senal PRIMARIA porque es la unica que sobrevive a luz
///    ambiente fuerte y la unica que funciona en modo oscuro, donde el
///    escalon de valor bandeja/pozo no llega a 3:1.
/// 2. `pozoContacto` como degradado corto y duro en el borde superior interno
///    — la sombra que la pared proyecta hacia dentro del hueco.
/// 3. El salto de valor entre bandeja y pozo, que en modo claro es enorme
///    (13:1) y en oscuro es un apoyo secundario (1.43:1).
///
/// Radio 4, no 12: el interior de una bandeja es rectilineo. El radio grande
/// vive solo en el canto exterior.
///
/// Un pozo tocable es ademas **enfocable**: recibe el foco del mando o del
/// teclado y lo muestra subiendo el canto a metal pleno de 3 y aclarando el
/// relleno (regla 12). Sin eso la app no se puede usar en un televisor — el
/// mando mueve el foco y nada en pantalla dice donde esta.
class WulfPozo extends StatefulWidget {
  /// Un compartimento con [child] dentro.
  ///
  /// Con [onTap] u [onLongPress] el pozo se vuelve tocable y garantiza el
  /// objetivo táctil mínimo, así que no hace falta envolverlo en un botón.
  const WulfPozo({
    super.key,
    required this.child,
    this.onTap,
    this.onLongPress,
    this.seleccionado = false,
    this.enfocable = true,
    this.autofoco = false,
    this.padding = const EdgeInsets.all(WulfSpacing.paddingCelda),
    this.radio = WulfRadius.pozo,
  });

  /// El contenido del compartimento.
  final Widget child;

  /// Qué pasa al tocarlo. Nulo deja el pozo como superficie, sin reacción.
  final VoidCallback? onTap;

  /// Acción secundaria del compartimento. Se usa para lo que no debe competir
  /// con el toque simple — exportar un reporte, por ejemplo.
  final VoidCallback? onLongPress;

  /// Seleccion actual: la cresta pasa al metal pleno y engorda. El acento
  /// nunca se usa decorativamente, solo para esto y para accion primaria.
  final bool seleccionado;

  /// Si toma el foco al aparecer la pantalla.
  ///
  /// Uno por pantalla: dos piezas peleandose el foco inicial dejan el resultado
  /// al azar del orden del arbol. Ver [WulfFocoInicial] para lo que no es un
  /// pozo.
  final bool autofoco;

  /// Si entra en el recorrido del mando y del teclado.
  ///
  /// En falso el pozo se toca pero no se puede alcanzar sin pantalla tactil.
  /// Solo para lo que ya es alcanzable de otra forma; apagarlo por costumbre es
  /// como una pantalla se vuelve inusable con mando.
  final bool enfocable;

  /// Aire entre el canto y el contenido. El contenido nunca toca el borde.
  final EdgeInsetsGeometry padding;

  /// Redondeo del compartimento. Por defecto [WulfRadius.pozo], que es pequeño
  /// a propósito: el interior de una bandeja es rectilíneo y el radio grande
  /// vive solo en el canto exterior.
  final double radio;

  @override
  State<WulfPozo> createState() => _WulfPozoState();
}

class _WulfPozoState extends State<WulfPozo> {
  bool _enfocado = false;

  @override
  Widget build(BuildContext context) {
    final laca = lacaDe(context);
    final tema = Theme.of(context);
    final acento = tema.colorScheme.primary;
    final borde = BorderRadius.circular(widget.radio);
    final tocable = widget.onTap != null || widget.onLongPress != null;

    // Enfocado gana sobre seleccionado: navegando con el mando se RECORRE la
    // seleccion, asi que lo que hay que ver moverse es el foco.
    final fondo = _enfocado ? wulfTonoEnfocado(laca.pozo, tema.brightness) : laca.pozo;

    Widget contenido = Padding(padding: widget.padding, child: widget.child);

    if (tocable) {
      contenido = Material(
        type: MaterialType.transparency,
        child: InkWell(
          onTap: widget.onTap,
          onLongPress: widget.onLongPress,
          borderRadius: borde,
          canRequestFocus: widget.enfocable,
          autofocus: widget.autofoco,
          onFocusChange: (tiene) => setState(() => _enfocado = tiene),
          // El resaltado de foco de fabrica es un rectangulo translucido, que
          // es exactamente la senal que este sistema no usa. Lo apaga: el foco
          // lo dice el canto.
          focusColor: Colors.transparent,
          child: ConstrainedBox(
            constraints: const BoxConstraints(minHeight: wulfObjetivoTactilMin),
            child: contenido,
          ),
        ),
      );
    }

    return DecoratedBox(
      decoration: BoxDecoration(
        borderRadius: borde,
        // Borde uniforme a proposito: Flutter no permite un `Border` de lados
        // distintos junto con `borderRadius`, y el labio del hueco atrapa luz
        // en todo su perimetro de todas formas.
        border: Border.all(
          color: _enfocado || widget.seleccionado ? acento : laca.paredCresta,
          width: switch ((_enfocado, widget.seleccionado)) {
            (true, _) => WulfCanto.enfocado,
            (false, true) => WulfCanto.seleccionado,
            _ => WulfCanto.reposo,
          },
        ),
        // La sombra de contacto ocupa solo la primera franja del pozo: es el
        // canto de la pared, no un degradado decorativo de arriba a abajo.
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [laca.pozoContacto, fondo],
          stops: const [0.0, 0.07],
        ),
      ),
      child: contenido,
    );
  }
}
