import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wulf_flutter/wulf_flutter.dart';

/// El rail: la columna de navegación vertical.
///
/// Existe porque unas pestañas verticales rotan el texto. Lo que se vigila es que
/// no herede el problema que venía a evitar: texto de frente, marca del activo
/// señalando al contenido, y recorrible sin ratón.
void main() {
  Widget conRail({
    int seleccionado = 0,
    ValueChanged<int>? onSeleccionar,
    bool television = false,
  }) => MaterialApp(
    theme: buildWulfTheme(WulfAcento.oro),
    home: Scaffold(
      body: WulfRail(
        destinos: const [
          WulfDestino(etiqueta: 'Venta'),
          WulfDestino(etiqueta: 'Catálogo'),
          WulfDestino(etiqueta: 'Reportes'),
        ],
        seleccionado: seleccionado,
        onSeleccionar: onSeleccionar ?? (_) {},
        television: television,
      ),
    ),
  );

  /// El borde del destino que contiene [texto].
  Border? bordeDe(WidgetTester tester, String texto) {
    final caja = tester.widget<DecoratedBox>(
      find
          .ancestor(of: find.text(texto), matching: find.byType(DecoratedBox))
          .first,
    );
    return (caja.decoration as BoxDecoration).border as Border?;
  }

  testWidgets('muestra todos los destinos con su texto', (tester) async {
    await tester.pumpWidget(conRail());

    for (final n in ['Venta', 'Catálogo', 'Reportes']) {
      expect(find.text(n), findsOneWidget);
    }
  });

  testWidgets('la marca del activo va en el borde DERECHO', (tester) async {
    // A la derecha y no debajo: el contenido está al lado. Misma regla que en
    // las pestañas — la marca señala hacia lo que se está mirando.
    await tester.pumpWidget(conRail(seleccionado: 1));

    final borde = bordeDe(tester, 'Catálogo');
    expect(borde?.right.width, WulfCanto.seleccionado);
    expect(borde?.bottom.width ?? 0, 0, reason: 'no se subraya, se marca al lado');
  });

  testWidgets('los inactivos no llevan marca', (tester) async {
    await tester.pumpWidget(conRail(seleccionado: 1));

    expect(bordeDe(tester, 'Venta'), isNull);
  });

  testWidgets('tocar un destino avisa con su índice', (tester) async {
    int? elegido;
    await tester.pumpWidget(conRail(onSeleccionar: (i) => elegido = i));

    await tester.tap(find.text('Reportes'));
    await tester.pump();

    expect(elegido, 2);
  });

  testWidgets('cada destino entra en el recorrido del teclado', (tester) async {
    // Es la pieza que se usa para navegar con un mando: si no se puede enfocar,
    // no sirve para lo que existe.
    await tester.pumpWidget(conRail());

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();

    expect(bordeDe(tester, 'Venta')?.right.width, WulfCanto.enfocado);
  });

  testWidgets('en televisor el canto del foco engorda', (tester) async {
    await tester.pumpWidget(conRail(television: true));

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();

    expect(bordeDe(tester, 'Venta')?.right.width, WulfCanto.enfocadoTelevision);
  });

  testWidgets('el texto encoge antes que partirse', (tester) async {
    // Un destino con nombre largo en un rail estrecho: la etiqueta ajustada es
    // lo que evita "Config / uración".
    await tester.pumpWidget(
      MaterialApp(
        theme: buildWulfTheme(WulfAcento.oro),
        home: Scaffold(
          body: WulfRail(
            destinos: const [WulfDestino(etiqueta: 'Configuración')],
            seleccionado: 0,
            onSeleccionar: (_) {},
          ),
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(WulfEtiquetaAjustada), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
