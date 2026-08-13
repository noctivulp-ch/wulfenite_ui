import 'package:flutter/material.dart';

/// Efectos visuales que se pueden apagar desde Configuración → Optimización.
///
/// Acá va lo que **cuesta pintar y no cambia lo que la app hace**: adorno, no
/// función. El aparato del negocio es de gama baja, y en él la diferencia entre
/// un desplazamiento fluido y uno con tirones puede ser un solo efecto.
///
/// Para agregar uno nuevo: una constante acá, respetarla donde se pinta, y una
/// entrada en el panel de Optimización. Nada más — el resto lo lleva el tema.
enum WulfEfectoPesado {
  /// El relieve grabado de los números y los iconos: pinta el mismo glifo tres
  /// veces (sombra, luz y glifo) para que se lea hundido en la superficie.
  ///
  /// Medido en el teléfono del negocio: apagarlo baja el raster al desplazar el
  /// catálogo de 12,9 ms a 7,9 y los frames fuera de presupuesto de 50 a 4.
  /// Es, por lejos, el efecto más caro del sistema.
  tallado;

  /// Nombre corto para mostrar en un panel de ajustes.
  String get etiqueta => switch (this) {
    WulfEfectoPesado.tallado => 'Relieve grabado',
  };

  /// Qué gana el usuario al apagarlo, en una frase.
  String get explicacion => switch (this) {
    WulfEfectoPesado.tallado =>
      'Los números y los iconos se ven hundidos en la superficie. Apagarlo es lo '
          'que más alivia el desplazamiento en teléfonos lentos.',
  };
}

/// Qué efectos están apagados, disponible en cualquier punto del árbol.
///
/// Va por el TEMA y no por una librería de estado a propósito: los widgets del
/// sistema no conocen la app que los usa, así que [WulfTallado] no necesita
/// saber que existe una pantalla de ajustes. La app traduce su preferencia a
/// esto y la pasa a [buildWulfTheme].
@immutable
class WulfEfectos extends ThemeExtension<WulfEfectos> {
  /// Sin argumentos, todos los efectos encendidos.
  const WulfEfectos({this.apagados = const {}});

  /// Los efectos que este aparato NO debe pintar.
  final Set<WulfEfectoPesado> apagados;

  /// Si [efecto] se puede pintar. Es lo que consultan los widgets.
  bool activo(WulfEfectoPesado efecto) => !apagados.contains(efecto);

  @override
  WulfEfectos copyWith({Set<WulfEfectoPesado>? apagados}) =>
      WulfEfectos(apagados: apagados ?? this.apagados);

  /// Sin interpolación: un efecto está o no está, y medio efecto durante la
  /// transición de tema se vería como un parpadeo.
  @override
  WulfEfectos lerp(ThemeExtension<WulfEfectos>? other, double t) =>
      t < 0.5 ? this : (other as WulfEfectos? ?? this);
}

/// Si un efecto debe pintarse en este punto del árbol.
///
/// Devuelve `true` cuando no hay tema con la extensión —tests de widget
/// sueltos, por ejemplo—: el comportamiento por defecto es la app completa.
bool wulfEfectoActivo(BuildContext context, WulfEfectoPesado efecto) =>
    Theme.of(context).extension<WulfEfectos>()?.activo(efecto) ?? true;
