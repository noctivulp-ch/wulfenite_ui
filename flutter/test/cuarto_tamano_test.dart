import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wulf_flutter/wulf_flutter.dart';

/// El cuarto tamaño: el televisor.
///
/// La parte que hay que vigilar no es que exista, sino que **no salga del
/// ancho**. Un televisor de 1080p reporta unos 960 dp y por ancho caería en
/// `medio` — el reparto de una tablet que se mira a cuarenta centímetros. A tres
/// metros esa composición no se lee.
void main() {
  group('no sale del ancho', () {
    test('960 dp por sí solos son una tablet, no una tele', () {
      expect(WulfAncho.deAncho(960), WulfAncho.medio);
    });

    test('deAncho nunca devuelve television', () {
      for (final ancho in [0.0, 360.0, 599.0, 600.0, 1023.0, 1024.0, 1920.0, 3840.0]) {
        expect(WulfAncho.deAncho(ancho), isNot(WulfAncho.television));
      }
    });

    testWidgets('lo declara la app, y gana sobre cualquier ancho', (tester) async {
      late WulfAncho conTele;
      late WulfAncho sinTele;
      await tester.pumpWidget(
        MaterialApp(
          home: Builder(
            builder: (context) {
              conTele = WulfAncho.de(context, television: true);
              sinTele = WulfAncho.de(context);
              return const SizedBox();
            },
          ),
        ),
      );

      expect(conTele, WulfAncho.television);
      expect(sinTele, isNot(WulfAncho.television));
    });
  });

  group('lo que cambia de lejos', () {
    test('el labio es el doble', () {
      // La tele recorta los bordes y se come la cresta de los pozos de los
      // extremos, que es la señal primaria de profundidad.
      expect(WulfSpacing.rimTelevision, WulfSpacing.rim * 2);
    });

    test('el canto del foco engorda', () {
      // A tres metros un escalón de 1 no existe, y el foco es lo único que dice
      // dónde está uno con un mando.
      expect(WulfCanto.enfocadoDe(WulfAncho.television), greaterThan(WulfCanto.enfocado));
      expect(WulfCanto.enfocadoDe(WulfAncho.amplio), WulfCanto.enfocado);
    });

    test('los demás tamaños no se tocan', () {
      for (final a in [WulfAncho.compacto, WulfAncho.medio, WulfAncho.amplio]) {
        expect(WulfCanto.enfocadoDe(a), WulfCanto.enfocado);
        expect(a.esLejano, isFalse);
      }
      expect(WulfAncho.television.esLejano, isTrue);
    });
  });

  group('la retícula', () {
    test('una celda hereda el escritorio si no dice otra cosa', () {
      const celda = WulfCelda(compacto: 6, medio: 3, amplio: 2, hijo: SizedBox());
      expect(celda.columnasEn(WulfAncho.television), 2);
    });

    test('pero puede pedir uno propio: de lejos caben menos piezas', () {
      const celda = WulfCelda(
        compacto: 6,
        medio: 3,
        amplio: 2,
        television: 3,
        hijo: SizedBox(),
      );
      expect(celda.columnasEn(WulfAncho.television), 3);
      expect(celda.columnasEn(WulfAncho.amplio), 2);
    });

    test('porFila también', () {
      expect(wulfPorFila(WulfAncho.television, compacto: 3, medio: 2, amplio: 1), 6);
      expect(
        wulfPorFila(WulfAncho.television, compacto: 3, medio: 2, amplio: 1, television: 3),
        2,
      );
    });
  });
}
