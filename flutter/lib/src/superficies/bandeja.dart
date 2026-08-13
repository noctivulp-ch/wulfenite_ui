import 'package:flutter/material.dart';

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
class WulfPozo extends StatelessWidget {
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

  /// Seleccion actual: la cresta pasa al oro de laca y engorda. El acento
  /// nunca se usa decorativamente, solo para esto y para accion primaria.
  final bool seleccionado;

  /// Aire entre el canto y el contenido. El contenido nunca toca el borde.
  final EdgeInsetsGeometry padding;

  /// Redondeo del compartimento. Por defecto [WulfRadius.pozo], que es pequeño
  /// a propósito: el interior de una bandeja es rectilíneo y el radio grande
  /// vive solo en el canto exterior.
  final double radio;

  @override
  Widget build(BuildContext context) {
    final laca = lacaDe(context);
    final acento = Theme.of(context).colorScheme.primary;
    final borde = BorderRadius.circular(radio);

    Widget contenido = Padding(padding: padding, child: child);

    if (onTap != null || onLongPress != null) {
      contenido = Material(
        type: MaterialType.transparency,
        child: InkWell(
          onTap: onTap,
          onLongPress: onLongPress,
          borderRadius: borde,
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
          color: seleccionado ? acento : laca.paredCresta,
          width: seleccionado ? 2 : 1,
        ),
        // La sombra de contacto ocupa solo la primera franja del pozo: es el
        // canto de la pared, no un degradado decorativo de arriba a abajo.
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [laca.pozoContacto, laca.pozo],
          stops: const [0.0, 0.07],
        ),
      ),
      child: contenido,
    );
  }
}
