import 'package:flutter/material.dart';

/// Grosor del canto según el estado. Es la regla 12 en forma de número.
///
/// El foco NO estrena una señal propia: usa la que el sistema ya tiene para
/// decir "este", el canto de la regla 1, subida un escalón más. Inventarle un
/// halo o una sombra sería contradecir la regla 1 justo donde más se nota — un
/// mando a tres metros.
///
/// | estado | canto | relleno |
/// |---|---|---|
/// | reposo | cresta (metal apagado), 1 | pozo |
/// | seleccionado | metal pleno, 2 | pozo |
/// | enfocado | metal pleno, 3 | pozo aclarado |
///
/// Que enfocado lleve **además** el relleno separado es lo que lo distingue de
/// seleccionado cuando los dos caen a la vez, que es el caso normal navegando
/// una lista con el mando: se recorre la selección.
class WulfCanto {
  const WulfCanto._();

  /// En reposo. La cresta, que es material de bandeja.
  static const double reposo = 1;

  /// Seleccionado: metal pleno.
  static const double seleccionado = 2;

  /// Enfocado por mando o teclado.
  ///
  /// Tres y no dos: en televisión se mira a tres metros, y un escalón de 1 que
  /// en la mano se distingue perfectamente, ahí no existe. Es el mismo motivo
  /// por el que el objetivo táctil es de 48 y no de 32.
  static const double enfocado = 3;
}

/// Cuánto se aclara el pozo enfocado, en la misma escala que el tono del toque.
///
/// Coincide a propósito con la separación de la onda: el aparato dice "acá
/// estás" con el mismo material en los dos casos, se llegue con el dedo o con el
/// mando.
const double wulfSeparacionFoco = 0.10;

/// El tono de una superficie que tiene el foco.
///
/// Se aleja del reposo en la dirección que contrasta —aclarar sobre material
/// oscuro, oscurecer sobre material claro—, igual que el tono del toque. Un tono
/// fijo no sirve: el mismo gris que destaca sobre el pozo oscuro desaparece
/// sobre el claro.
Color wulfTonoEnfocado(Color pozo, Brightness brillo) {
  final hacia = brillo == Brightness.dark ? Colors.white : Colors.black;
  return Color.lerp(pozo, hacia, wulfSeparacionFoco)!;
}
