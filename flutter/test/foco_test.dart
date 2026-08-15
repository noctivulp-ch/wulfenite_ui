import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wulf_flutter/wulf_flutter.dart';

/// La regla 12: el foco se ve, y se ve con el mismo canto.
///
/// Sin un foco visible una app **no se puede usar en un televisor**. No es una
/// mejora de accesibilidad: el mando mueve el foco y nada en pantalla dice
/// dónde está.
void main() {
  /// El borde que el pozo tiene pintado ahora mismo.
  BorderSide bordeDelPozo(WidgetTester tester) {
    final caja = tester.widget<DecoratedBox>(
      find.descendant(of: find.byType(WulfPozo), matching: find.byType(DecoratedBox)).first,
    );
    return ((caja.decoration as BoxDecoration).border! as Border).top;
  }

  Widget conTema(Widget hijo) => MaterialApp(
    theme: buildWulfTheme(WulfAcento.oro),
    home: Scaffold(body: hijo),
  );

  group('los tres escalones del canto', () {
    test('cada uno es más grueso que el anterior', () {
      // Si dos estados comparten grosor, el de más arriba deja de leerse.
      expect(WulfCanto.reposo, lessThan(WulfCanto.seleccionado));
      expect(WulfCanto.seleccionado, lessThan(WulfCanto.enfocado));
    });

    test('el de foco dobla al de reposo', () {
      // A tres metros un escalón de 1 no existe. Es el mismo motivo por el que
      // el objetivo táctil es de 48 y no de 32.
      expect(WulfCanto.enfocado, greaterThanOrEqualTo(WulfCanto.reposo * 2));
    });
  });

  testWidgets('en reposo el pozo lleva la cresta, fina', (tester) async {
    await tester.pumpWidget(conTema(WulfPozo(onTap: () {}, child: const Text('x'))));

    expect(bordeDelPozo(tester).width, WulfCanto.reposo);
  });

  testWidgets('seleccionado engorda y toma el metal', (tester) async {
    await tester.pumpWidget(
      conTema(WulfPozo(onTap: () {}, seleccionado: true, child: const Text('x'))),
    );

    final borde = bordeDelPozo(tester);
    expect(borde.width, WulfCanto.seleccionado);
    expect(borde.color, buildWulfTheme(WulfAcento.oro).colorScheme.primary);
  });

  /// Mueve el foco como lo haría un mando o un teclado: recorriendo el árbol,
  /// no pidiéndoselo a un nodo concreto. Es lo que se quiere comprobar — que el
  /// pozo ESTÁ en el recorrido, no solo que sabe pintarse enfocado.
  Future<void> avanzarFoco(WidgetTester tester) async {
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();
  }

  testWidgets('al recibir el foco el canto sube al escalón de arriba', (tester) async {
    await tester.pumpWidget(conTema(WulfPozo(onTap: () {}, child: const Text('x'))));
    expect(bordeDelPozo(tester).width, WulfCanto.reposo);

    await avanzarFoco(tester);

    expect(
      bordeDelPozo(tester).width,
      WulfCanto.enfocado,
      reason: 'sin esto el mando mueve el foco y la pantalla no lo dice',
    );
  });

  testWidgets('enfocado gana sobre seleccionado', (tester) async {
    // Navegando con el mando se RECORRE la selección, así que lo que hay que
    // ver moverse es el foco.
    await tester.pumpWidget(
      conTema(WulfPozo(onTap: () {}, seleccionado: true, child: const Text('x'))),
    );
    await avanzarFoco(tester);

    expect(bordeDelPozo(tester).width, WulfCanto.enfocado);
  });

  testWidgets('un pozo no enfocable se salta en el recorrido', (tester) async {
    await tester.pumpWidget(
      conTema(WulfPozo(onTap: () {}, enfocable: false, child: const Text('x'))),
    );
    await avanzarFoco(tester);

    expect(bordeDelPozo(tester).width, WulfCanto.reposo);
  });

  testWidgets('un pozo no enfocable no pide el foco', (tester) async {
    await tester.pumpWidget(
      conTema(WulfPozo(onTap: () {}, enfocable: false, child: const Text('x'))),
    );

    expect(tester.widget<InkWell>(find.byType(InkWell)).canRequestFocus, isFalse);
  });

  testWidgets('un pozo sin toque no entra en el recorrido', (tester) async {
    // Un pozo decorativo no es un destino del mando: pararse en algo que no
    // hace nada es lo que convierte una pantalla en un laberinto.
    await tester.pumpWidget(conTema(const WulfPozo(child: Text('x'))));

    expect(find.byType(InkWell), findsNothing);
  });

  group('el tono del pozo enfocado', () {
    test('se separa del de reposo en los dos modos', () {
      for (final brillo in Brightness.values) {
        final laca = _lacaDe(brillo);
        expect(
          wulfTonoEnfocado(laca.pozo, brillo),
          isNot(laca.pozo),
          reason: 'sin separación, enfocado y seleccionado se ven iguales al coincidir',
        );
      }
    });

    test('usa la misma separación que la onda del toque', () {
      // A propósito: el aparato dice "acá estás" con el mismo material se
      // llegue con el dedo o con el mando.
      expect(wulfSeparacionFoco, const WulfToqueReceta().separacion);
    });
  });
}

WulfLaca _lacaDe(Brightness brillo) {
  final tema = buildWulfTheme(WulfAcento.oro, brightness: brillo);
  return tema.extension<WulfLaca>()!;
}
