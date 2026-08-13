import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wulf_flutter/wulf_flutter.dart';

/// El tallado se consigue acercando el glifo al color de su superficie: es
/// literalmente contraste gastado en estetica. Esta prueba fija cuanto se puede
/// gastar.
///
/// Los valores salen del banco de pruebas, calibrados por el usuario en modo
/// OSCURO — donde su fusion de 0.50 queda al borde (4.30:1). En modo claro la
/// misma fusion cae a 2.83:1, porque la paleta clara tiene menos margen de
/// luminancia. De ahi que la fusion sea por modo y por nivel, y de ahi que esto
/// se pruebe en los dos modos y no solo en el que se miro.
double _contraste(Color a, Color b) {
  final la = a.computeLuminance();
  final lb = b.computeLuminance();
  final alto = la > lb ? la : lb;
  final bajo = la > lb ? lb : la;
  return (alto + 0.05) / (bajo + 0.05);
}

void main() {
  for (final brillo in Brightness.values) {
    final tema = buildWulfTheme(WulfAcento.oro, brightness: brillo);
    final laca = tema.extension<WulfLaca>()!;

    // Construye un contexto real para que `wulfColorTallado` pueda leer el
    // brillo del tema, que es lo que decide la fusion.
    Future<BuildContext> contextoCon(WidgetTester tester) async {
      late BuildContext capturado;
      await tester.pumpWidget(
        MaterialApp(
          theme: tema,
          home: Builder(
            builder: (context) {
              capturado = context;
              return const SizedBox.shrink();
            },
          ),
        ),
      );
      return capturado;
    }

    final superficies = {
      'pozo': (fondo: laca.pozo, base: tema.colorScheme.onSurface),
      'bandeja': (fondo: laca.bandeja, base: laca.textoEnBandeja),
    };

    superficies.forEach((nombreSup, sup) {
      testWidgets('tallado sutil mantiene AA (4.5:1) sobre $nombreSup en ${brillo.name}', (
        tester,
      ) async {
        final context = await contextoCon(tester);
        final glifo = wulfColorTallado(context, base: sup.base, sobre: sup.fondo);
        final ratio = _contraste(glifo, sup.fondo);
        expect(
          ratio,
          greaterThanOrEqualTo(4.5),
          reason:
              'texto de lectura tallado sobre $nombreSup en '
              '${brillo.name}: ${ratio.toStringAsFixed(2)}:1',
        );
      });

      testWidgets('tallado marcado mantiene 3:1 sobre $nombreSup en ${brillo.name}', (
        tester,
      ) async {
        // 3:1 es el minimo de WCAG para texto grande (1.4.3) y para
        // elementos no textuales (1.4.11) — el nivel marcado solo se usa en
        // >=24px e iconos, donde ese umbral aplica.
        final context = await contextoCon(tester);
        final glifo = wulfColorTallado(
          context,
          base: sup.base,
          sobre: sup.fondo,
          nivel: TalladoNivel.marcado,
        );
        final ratio = _contraste(glifo, sup.fondo);
        expect(
          ratio,
          greaterThanOrEqualTo(3.0),
          reason:
              'texto grande tallado sobre $nombreSup en '
              '${brillo.name}: ${ratio.toStringAsFixed(2)}:1',
        );
      });
    });

    testWidgets('el nivel marcado talla mas que el sutil en ${brillo.name}', (tester) async {
      // Si los dos niveles dieran lo mismo, la distincion seria decorativa.
      final context = await contextoCon(tester);
      final sutil = wulfColorTallado(context, base: tema.colorScheme.onSurface, sobre: laca.pozo);
      final marcado = wulfColorTallado(
        context,
        base: tema.colorScheme.onSurface,
        sobre: laca.pozo,
        nivel: TalladoNivel.marcado,
      );
      expect(
        _contraste(marcado, laca.pozo),
        lessThan(_contraste(sutil, laca.pozo)),
        reason: 'el nivel marcado debe fundirse mas con el fondo en ${brillo.name}',
      );
    });
  }
}
