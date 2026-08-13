import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wulf_flutter_ejemplo/main.dart';

/// El banco de estilo abre y se dibuja.
///
/// Es la prueba que de verdad valida la librería: el banco toca **todos** los
/// tokens del sistema —cada color, el relieve en sus tres niveles, los pozos, la
/// retícula— así que si se puede construir importando solo `wulf_flutter`, la
/// librería está completa. Lo que el banco tenga que resolver por su cuenta es
/// exactamente lo que le falta a la librería.
void main() {
  testWidgets('el banco de estilo se construye con la librería', (tester) async {
    // Grande a propósito: el banco pone el panel de control al lado de la
    // muestra, y a 800 de ancho no entra.
    tester.view.physicalSize = const Size(1600, 1200);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    await tester.pumpWidget(const SandboxApp());
    await tester.pump();

    expect(tester.takeException(), isNull);
  });
}
