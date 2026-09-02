import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wulf_flutter/wulf_flutter.dart';

/// Casillas y radios: que se vean distintos y que se vean de la casa.
///
/// La FORMA no hace falta vigilarla acá — Material dibuja la casilla cuadrada y
/// el radio redondo, y eso no se puede temar, así que no se puede romper. En Qt
/// sí se rompió (una sola regla para las dos, radio cuadrado) porque allí la
/// hoja de estilo reemplaza el dibujo del sistema.
///
/// Lo que sí puede romperse acá es el COLOR: si el tema deja de mapear `primary`
/// al metal, las dos salen del morado de Material en mitad de la laca sin que
/// nada avise. Eso es lo que pinchan estas pruebas.
void main() {
  Widget conControles({bool marcado = true}) => MaterialApp(
    theme: buildWulfTheme(WulfAcento.oro),
    home: Scaffold(
      body: Column(
        children: [
          Checkbox(value: marcado, onChanged: (_) {}),
          Radio<int>(value: 0, groupValue: marcado ? 0 : 1, onChanged: (_) {}),
        ],
      ),
    ),
  );

  testWidgets('la casilla marcada se llena del metal', (tester) async {
    await tester.pumpWidget(conControles());
    final tema = Theme.of(tester.element(find.byType(Checkbox)));
    final casilla = tester.widget<Checkbox>(find.byType(Checkbox));
    final relleno =
        casilla.fillColor?.resolve({WidgetState.selected}) ??
        tema.checkboxTheme.fillColor?.resolve({WidgetState.selected}) ??
        tema.colorScheme.primary;
    expect(relleno, tema.colorScheme.primary);
    expect(tema.colorScheme.primary, isNot(const ColorScheme.light().primary));
  });

  testWidgets('el radio elegido se pinta del metal', (tester) async {
    await tester.pumpWidget(conControles());
    final tema = Theme.of(tester.element(find.byType(Radio<int>)));
    final radio = tester.widget<Radio<int>>(find.byType(Radio<int>));
    final relleno =
        radio.fillColor?.resolve({WidgetState.selected}) ??
        tema.radioTheme.fillColor?.resolve({WidgetState.selected}) ??
        tema.colorScheme.primary;
    expect(relleno, tema.colorScheme.primary);
  });

  testWidgets('la marca de la casilla se lee sobre el metal', (tester) async {
    // El palito va en `onPrimary`. Con el metal claro de Wulfenite, blanco no se
    // vería — el tema tiene que estar dando el tono oscuro.
    await tester.pumpWidget(conControles());
    final tema = Theme.of(tester.element(find.byType(Checkbox)));
    expect(tema.colorScheme.onPrimary, isNot(tema.colorScheme.primary));
  });
}
