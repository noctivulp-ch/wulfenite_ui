import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wulf_flutter/wulf_flutter.dart';

/// La barra de búsqueda: la pieza central de un reproductor.
///
/// Lo que se vigila es que **se pueda buscar con el mando** y que la aritmética
/// del paso no deje al reproductor pidiendo una posición imposible.
void main() {
  Widget conTema(Widget hijo) => MaterialApp(
    theme: buildWulfTheme(WulfAcento.oro),
    home: Scaffold(body: hijo),
  );

  /// Una barra que recuerda a dónde la movieron.
  Widget barra({
    double valor = 0.5,
    required void Function(double) onCambio,
    bool habilitado = true,
    bool autofoco = true,
  }) => conTema(
    WulfBarraBusqueda(
      valor: valor,
      onCambio: onCambio,
      habilitado: habilitado,
      autofoco: autofoco,
    ),
  );

  group('buscar con el mando', () {
    testWidgets('la derecha avanza un paso', (tester) async {
      double? nuevo;
      await tester.pumpWidget(barra(valor: 0.5, onCambio: (v) => nuevo = v));
      await tester.pumpAndSettle();

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
      await tester.pump();

      expect(nuevo, closeTo(0.5 + wulfPasoBusqueda, 0.0001));
    });

    testWidgets('la izquierda retrocede un paso', (tester) async {
      double? nuevo;
      await tester.pumpWidget(barra(valor: 0.5, onCambio: (v) => nuevo = v));
      await tester.pumpAndSettle();

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowLeft);
      await tester.pump();

      expect(nuevo, closeTo(0.5 - wulfPasoBusqueda, 0.0001));
    });

    testWidgets('no se puede buscar más allá del final', (tester) async {
      // Sin el recorte, mantener pulsada la derecha manda la posición fuera del
      // archivo y el reproductor intenta saltar a un sitio que no existe.
      double? nuevo;
      await tester.pumpWidget(barra(valor: 0.99, onCambio: (v) => nuevo = v));
      await tester.pumpAndSettle();

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
      await tester.pump();

      expect(nuevo, 1.0);
    });

    testWidgets('no se puede buscar antes del principio', (tester) async {
      double? nuevo;
      await tester.pumpWidget(barra(valor: 0.01, onCambio: (v) => nuevo = v));
      await tester.pumpAndSettle();

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowLeft);
      await tester.pump();

      expect(nuevo, 0.0);
    });

    testWidgets('arriba y abajo NO se consumen: son la salida del foco', (tester) async {
      // Tragárselos dejaría al mando encerrado en la barra, sin forma de llegar
      // al resto de la pantalla.
      double? nuevo;
      await tester.pumpWidget(barra(onCambio: (v) => nuevo = v));
      await tester.pumpAndSettle();

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
      await tester.sendKeyEvent(LogicalKeyboardKey.arrowUp);
      await tester.pump();

      expect(nuevo, isNull);
    });

    testWidgets('deshabilitada no se mueve ni recibe el foco', (tester) async {
      double? nuevo;
      await tester.pumpWidget(
        barra(onCambio: (v) => nuevo = v, habilitado: false, autofoco: false),
      );
      await tester.pumpAndSettle();

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
      await tester.pump();

      expect(nuevo, isNull);
      expect(tester.widget<Focus>(find.byType(Focus).first).canRequestFocus, isFalse);
    });
  });

  group('el paso', () {
    test('recorrer la barra entera cuesta un número razonable de pulsaciones', () {
      // Con el 1% serían cien: recorrer una película de dos horas dejaría de ser
      // viable con el mando. El ajuste fino sale de mantener pulsado.
      final pulsaciones = (1 / wulfPasoBusqueda).round();
      expect(pulsaciones, inInclusiveRange(10, 25));
    });
  });

  group('cómo se ve', () {
    testWidgets('se dibuja fina pero se toca ancha', (tester) async {
      // El carril mide 8 y el objetivo táctil 48: se acierta con el dedo sin
      // engordar la pieza (regla 7).
      await tester.pumpWidget(barra(onCambio: (_) {}, autofoco: false));

      final caja = tester.getSize(find.byType(WulfBarraBusqueda));
      expect(caja.height, wulfObjetivoTactilMin);
      expect(wulfAltoBarra, lessThan(wulfObjetivoTactilMin));
    });

    testWidgets('la de progreso no entra en el recorrido del mando', (tester) async {
      // No hay nada que hacer con ella: pararse ahí convierte el recorrido en
      // un laberinto.
      await tester.pumpWidget(conTema(const WulfBarraProgreso(valor: 0.5)));

      // Se mira DENTRO de la barra: el andamiaje de MaterialApp trae sus
      // propios nodos de foco, y contarlos todos no dice nada.
      expect(
        find.descendant(of: find.byType(WulfBarraProgreso), matching: find.byType(Focus)),
        findsNothing,
      );
    });

    testWidgets('un valor fuera de rango no rompe la pantalla', (tester) async {
      // Un cálculo de posición sobre una duración todavía desconocida da
      // infinito o negativo.
      await tester.pumpWidget(conTema(const WulfBarraProgreso(valor: 3.7)));
      await tester.pump();

      expect(tester.takeException(), isNull);
    });
  });

  /// El borde del pozo que contiene [texto].
  ///
  /// Por el texto y no por posición: `Scaffold` y `Material` dibujan sus propias
  /// cajas decoradas, así que "la última" no es la que uno cree.
  BorderSide bordeDelPozoCon(WidgetTester tester, String texto) {
    final pozo = find.ancestor(of: find.text(texto), matching: find.byType(WulfPozo));
    final caja = tester.widget<DecoratedBox>(
      find.descendant(of: pozo, matching: find.byType(DecoratedBox)).first,
    );
    return ((caja.decoration as BoxDecoration).border! as Border).top;
  }

  group('el foco inicial', () {
    testWidgets('con autofoco la barra arranca enfocada', (tester) async {
      // Sin esto la primera pulsación del mando se gasta en que el sistema
      // elija por su cuenta, y suele elegir lo primero del árbol.
      double? nuevo;
      await tester.pumpWidget(barra(onCambio: (v) => nuevo = v));
      await tester.pumpAndSettle();

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
      await tester.pump();

      expect(nuevo, isNotNull, reason: 'la barra tendría que tener ya el foco');
    });

    testWidgets('WulfFocoInicial se lo da a lo que no lo trae', (tester) async {
      await tester.pumpWidget(
        conTema(
          Column(
            children: [
              WulfPozo(onTap: () {}, child: const Text('primero')),
              WulfFocoInicial(child: WulfPozo(onTap: () {}, child: const Text('el que quiero'))),
            ],
          ),
        ),
      );
      await tester.pumpAndSettle();

      // El segundo pozo tiene el foco aunque el primero esté antes en el árbol.
      expect(bordeDelPozoCon(tester, 'el que quiero').width, WulfCanto.enfocado);
      expect(bordeDelPozoCon(tester, 'primero').width, WulfCanto.reposo);
    });

    testWidgets('desactivado no pide nada', (tester) async {
      await tester.pumpWidget(
        conTema(
          WulfFocoInicial(
            activo: false,
            child: WulfPozo(onTap: () {}, child: const Text('x')),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(bordeDelPozoCon(tester, 'x').width, WulfCanto.reposo);
    });
  });
}
