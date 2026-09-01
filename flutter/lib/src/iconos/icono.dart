import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

import '../reticula/cuadricula.dart';
import '../tokens/tema.dart';

/// Tamaños a los que se dibuja un icono.
///
/// Tres y no una escala libre: un icono a 22 junto a otro a 24 se ve descuadrado
/// sin que nadie sepa decir por que, y con tres nombres nadie tiene que elegir
/// un numero.
///
/// **Ninguno es el objetivo tactil.** Un icono de 24 dentro de un boton sigue
/// necesitando sus 48 alrededor: el dibujo es lo que se ve, no lo que se toca.
class WulfIconoTamano {
  const WulfIconoTamano._();

  /// En linea con un texto: junto a una etiqueta, dentro de un campo.
  static const double chico = 20;

  /// El de siempre. Es el tamaño de la grilla, asi que no se reescala.
  static const double normal = 24;

  /// Protagonista: un destino del rail, un estado vacio, un mando grande.
  static const double grande = 32;

  /// En un televisor el normal se queda corto, por la misma razon que el labio
  /// de la bandeja y el canto del foco: se mira de lejos.
  static const double television = 36;

  /// El tamaño que le toca a un ancho de pantalla.
  static double de(WulfAncho ancho, {double base = normal}) =>
      ancho.esLejano && base < television ? television : base;
}

/// Aire entre un icono y el texto que lo acompaña.
///
/// La mitad de la unidad: con la unidad entera el par se lee como dos cosas
/// sueltas en vez de como una etiqueta con su icono.
const double wulfGapIcono = 4;

/// Un icono del juego, teñido y a su tamaño.
///
/// Los archivos son de **un solo color**, que no es el color final: se retiñen
/// al dibujar. Es lo que permite que el mismo archivo sirva para el estado
/// inactivo y para el activo sin mantener dos copias que acaban
/// desincronizadas.
///
/// ```dart
/// WulfIcono('buscar', descripcion: 'Buscar')
/// WulfIcono('reportes', descripcion: 'Reportes', activo: true)
/// ```
///
/// El nombre es el del archivo sin extension — los que hay estan listados en
/// `doc/iconos.md`.
class WulfIcono extends StatelessWidget {
  /// Un icono por su nombre.
  const WulfIcono(
    this.nombre, {
    super.key,
    required this.descripcion,
    this.activo = false,
    this.tamano = WulfIconoTamano.normal,
    this.color,
    this.television = false,
  });

  /// El nombre del archivo, sin `.svg`.
  final String nombre;

  /// Lo que oye un lector de pantalla.
  ///
  /// **Nulo solo si el icono es decorativo**: si acompaña a un texto que ya dice
  /// lo mismo, repetirlo hace que se lea dos veces.
  final String? descripcion;

  /// Con el metal en vez del texto atenuado. Misma señal que en el resto del
  /// sistema: el acento marca lo elegido.
  final bool activo;

  /// A que tamaño se dibuja.
  final double tamano;

  /// Un color a mano, si hace falta salirse de activo/inactivo.
  final Color? color;

  /// En un televisor crece. Hay que decirlo: Flutter no expone el `uiMode`.
  final bool television;

  @override
  Widget build(BuildContext context) {
    final laca = lacaDe(context);
    final tinte = color ??
        (activo ? Theme.of(context).colorScheme.primary : laca.textoAtenuadoEnBandeja);
    // Nunca por debajo del tamaño de lejos, pero sin encoger uno que ya se pidio
    // mas grande.
    final medida = television && tamano < WulfIconoTamano.television
        ? WulfIconoTamano.television
        : tamano;

    return Semantics(
      label: descripcion,
      excludeSemantics: descripcion != null,
      child: SvgPicture.asset(
        'assets/iconos/$nombre.svg',
        package: 'wulf_flutter',
        width: medida,
        height: medida,
        // `srcIn` respeta el alfa del dibujo, asi que retiñe el trazo sin
        // pintar un cuadrado del color encima.
        colorFilter: ColorFilter.mode(tinte, BlendMode.srcIn),
      ),
    );
  }
}
