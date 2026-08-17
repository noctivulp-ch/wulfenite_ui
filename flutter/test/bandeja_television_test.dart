import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wulf_flutter/wulf_flutter.dart';

/// El labio doble de televisor.
///
/// Un televisor recorta los bordes de la imagen, y lo que se pierde ahí es justo
/// la cresta de los pozos de los extremos — la señal primaria de profundidad
/// (regla 1). Medido en el emulador de Android TV: con el labio normal quedaba
/// un 2,5 % de margen contra el 5 % que pide la guía.
void main() {
  EdgeInsets labioDe(WidgetTester tester) {
    final padding = tester.widget<Padding>(
      find.descendant(of: find.byType(WulfBandeja), matching: find.byType(Padding)).first,
    );
    return padding.padding as EdgeInsets;
  }

  Widget conTema(Widget hijo) => MaterialApp(
    theme: buildWulfTheme(WulfAcento.oro),
    home: Scaffold(body: hijo),
  );

  testWidgets('en pantalla normal el labio es el de siempre', (tester) async {
    await tester.pumpWidget(conTema(const WulfBandeja(child: Text('x'))));

    expect(labioDe(tester).left, WulfSpacing.rim);
  });

  testWidgets('en televisor el labio es el doble', (tester) async {
    await tester.pumpWidget(
      conTema(const WulfBandeja(television: true, child: Text('x'))),
    );

    expect(labioDe(tester).left, WulfSpacing.rimTelevision);
  });

  test('el labio de televisor cubre el 5% de un 1080p', () {
    // La guía de Android TV pide 5 % por lado. A 1080p con densidad 2x, 48 dp
    // son 96 px de 1920 — exactamente el 5 %. Con los 24 dp normales serían la
    // mitad, que es el fallo que se midió en el emulador.
    const anchoPx = 1920;
    const densidad = 2;
    expect(WulfSpacing.rimTelevision * densidad / anchoPx, closeTo(0.05, 0.001));
    expect(WulfSpacing.rim * densidad / anchoPx, lessThan(0.05));
  });

  testWidgets('sin labio no se pone nada, ni en televisor', (tester) async {
    // Una bandeja anidada no repite el labio: el padre ya lo puso.
    await tester.pumpWidget(
      conTema(const WulfBandeja(rim: false, television: true, child: Text('x'))),
    );

    expect(
      find.descendant(of: find.byType(WulfBandeja), matching: find.byType(Padding)),
      findsNothing,
    );
  });
}
