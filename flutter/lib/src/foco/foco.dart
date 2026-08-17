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

/// Pide el foco para lo que envuelve cuando la pantalla aparece.
///
/// **Una pantalla sin foco inicial deja al mando sin punto de partida**: la
/// primera pulsacion se gasta en que el sistema elija por su cuenta, y suele
/// elegir mal — lo primero del arbol, que casi nunca es lo que se quiere hacer.
///
/// Es una linea alrededor de la pieza que la pantalla quiere como destino de
/// entrada:
///
/// ```dart
/// WulfFocoInicial(child: WulfPozo(onTap: reproducir, child: …))
/// ```
///
/// Los widgets del sistema que ya reciben foco —[WulfPozo], la barra de
/// busqueda— llevan ademas su propio `autofoco`, que hace lo mismo sin envolver
/// nada. Este sirve para lo que no lo tiene.
class WulfFocoInicial extends StatefulWidget {
  /// Envuelve [child] y le da el foco al aparecer.
  const WulfFocoInicial({super.key, required this.child, this.activo = true});

  /// Lo que arranca enfocado.
  final Widget child;

  /// En falso no pide nada. Sirve para que la pantalla decida en caliente cual
  /// de sus piezas arranca enfocada —la lista si hay elementos, el mensaje de
  /// vacio si no— sin mover el arbol de sitio.
  final bool activo;

  @override
  State<WulfFocoInicial> createState() => _WulfFocoInicialState();
}

class _WulfFocoInicialState extends State<WulfFocoInicial> {
  /// Un AMBITO y no un nodo suelto, y esa es toda la pieza.
  ///
  /// Un `Focus(autofocus: true)` se queda el foco EL MISMO, asi que envolviendo
  /// un pozo el foco cae en el envoltorio: el pozo no se pinta enfocado y pulsar
  /// no lo activa. Un ambito, en cambio, pasa el foco a su primer descendiente
  /// enfocable, que es lo que promete el nombre.
  final FocusScopeNode _ambito = FocusScopeNode(debugLabel: 'wulfFocoInicial');

  @override
  void initState() {
    super.initState();
    if (widget.activo) {
      // Tras el primer cuadro: antes de eso los descendientes todavia no
      // existen y no hay a quien pasarle el foco.
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted) return;
        _ambito.requestFocus();
        // Y ademas BAJAR. Un ambito al que se le pide el foco se lo queda EL,
        // asi que sin esto el foco cae en el envoltorio: el pozo de dentro no
        // se pinta enfocado y pulsar no lo activa. `nextFocus` lo pasa al primer
        // descendiente enfocable, que es lo que promete el nombre de la pieza.
        _ambito.nextFocus();
      });
    }
  }

  @override
  void dispose() {
    _ambito.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!widget.activo) return widget.child;
    return FocusScope(node: _ambito, child: widget.child);
  }
}
