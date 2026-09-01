import 'package:flutter/material.dart';

import '../tokens/tema.dart';

/// Los tres tamaños de pantalla de la app, definidos UNA vez.
///
/// Antes había un solo número suelto —`width >= 720` en `DashboardShell`— y
/// ninguna otra pantalla reaccionaba al ancho. Con el layout repartido en cinco
/// archivos, cada uno eligiendo sus propios umbrales, la app deja de leerse como
/// una sola cosa.
///
/// Los cortes salen del parque real de aparatos: la soda usa un Honor X6c, que
/// son 360 dp de ancho lógico igual que cualquier teléfono; la tablet ronda los
/// 800; el escritorio va de 1024 para arriba.
enum WulfAncho {
  /// Teléfono. 360 dp es el caso de diseño.
  compacto,

  /// Tablet.
  medio,

  /// Escritorio.
  amplio,

  /// Televisor: se mira de lejos y se maneja con mando.
  ///
  /// **No sale del ancho, sale de la plataforma.** Un televisor de 1080p reporta
  /// unos 960 dp lógicos —medido en el emulador de Android TV— y por ancho caería
  /// en [medio], que es el reparto de una tablet que se mira a cuarenta
  /// centímetros. A tres metros esa composición no se lee.
  ///
  /// Flutter no expone el `uiMode` de Android, así que quien sabe que corre en un
  /// televisor es la app: se declara con [de].
  television;

  static const double _corteMedio = 600;
  static const double _corteAmplio = 1024;

  /// El tamaño que le toca a la ventana actual.
  ///
  /// [television] gana sobre el ancho, porque la distancia de lectura no se
  /// puede medir en pixeles. Hay que decirlo: Flutter no expone ese dato.
  static WulfAncho de(BuildContext context, {bool television = false}) =>
      television ? WulfAncho.television : deAncho(MediaQuery.sizeOf(context).width);

  /// Separado de [de] para poder decidir a partir del ancho de una caja y no
  /// del de la ventana: un panel lateral angosto dentro de un escritorio ancho
  /// necesita componerse como `compacto`.
  static WulfAncho deAncho(double ancho) {
    if (ancho >= _corteAmplio) return WulfAncho.amplio;
    if (ancho >= _corteMedio) return WulfAncho.medio;
    return WulfAncho.compacto;
  }

  /// Atajo para el caso que más se consulta: si hay que componer para teléfono.
  bool get esCompacto => this == WulfAncho.compacto;

  /// Si se mira de lejos. Lo que cambia no es el ancho sino el tamaño de todo:
  /// el labio de la bandeja, el canto del foco y cuántas piezas caben antes de
  /// que deje de leerse.
  bool get esLejano => this == WulfAncho.television;
}

/// Cuántas columnas tiene la retícula. Es el mismo número en los tres tamaños:
/// lo que cambia es cuántas ocupa cada pieza.
///
/// Seis porque se divide en 2 y en 3, que son los repartos que piden estas
/// pantallas — mitades para los dos rankings, tercios para los tres turnos. Con
/// cuatro los tercios no cierran; con doce, a 360 dp cada columna queda en unos
/// 21 dp y el número deja de significar nada.
const int wulfColumnas = 6;

/// Cuántos elementos entran por fila si cada uno ocupa el span indicado.
///
/// Para las listas LARGAS —el catálogo, que las pruebas de volumen ejercitan con
/// 5.000 productos— no se puede usar [WulfCuadricula]: construye todas sus
/// celdas de una, y una lista de miles tiene que ser perezosa. Estas se arman
/// con un `GridView` de `crossAxisCount` EXPLÍCITO, y este helper es el que hace
/// que ese número salga del mismo vocabulario de spans que el resto de la app en
/// vez de ser un `maxCrossAxisExtent` que reparte por ancho disponible.
int wulfPorFila(
  WulfAncho ancho, {
  required int compacto,
  required int medio,
  required int amplio,
  int? television,
}) {
  final span = switch (ancho) {
    WulfAncho.compacto => compacto,
    WulfAncho.medio => medio,
    WulfAncho.amplio => amplio,
    // Por defecto hereda el de escritorio, que es lo razonable de partida — pero
    // se puede pedir otro: de lejos suelen caber menos piezas por fila, no mas.
    WulfAncho.television => television ?? amplio,
  };
  return wulfColumnas ~/ span;
}

/// Una pieza de la retícula, con cuántas columnas ocupa en cada tamaño.
///
/// Declarar los tres valores es obligatorio a propósito. Un default del estilo
/// "si no decís nada, ocupa todo" esconde justamente la decisión que este
/// sistema existe para hacer explícita.
class WulfCelda {
  /// Una celda que ocupa [compacto], [medio] y [amplio] columnas según el
  /// tamaño de la pantalla.
  const WulfCelda({
    required this.compacto,
    required this.medio,
    required this.amplio,
    this.television,
    required this.hijo,
  }) : assert(compacto >= 1 && compacto <= wulfColumnas),
       assert(medio >= 1 && medio <= wulfColumnas),
       assert(amplio >= 1 && amplio <= wulfColumnas);

  /// Ocupa lo mismo en los tres tamaños.
  const WulfCelda.fija(int columnas, {required Widget hijo})
    : this(compacto: columnas, medio: columnas, amplio: columnas, hijo: hijo);

  /// Columnas que ocupa en teléfono.
  final int compacto;

  /// Columnas que ocupa en tablet.
  final int medio;

  /// Columnas que ocupa en escritorio.
  final int amplio;

  /// Columnas en televisor. Nulo hereda el de escritorio.
  final int? television;

  /// El contenido de la celda.
  final Widget hijo;

  /// Cuántas columnas ocupa en un [ancho] concreto.
  int columnasEn(WulfAncho ancho) => switch (ancho) {
    WulfAncho.compacto => compacto,
    WulfAncho.medio => medio,
    WulfAncho.amplio => amplio,
    WulfAncho.television => television ?? amplio,
  };
}

/// La retícula wulf: reparte sus celdas en filas de [wulfColumnas] columnas.
///
/// Todas las celdas de una misma fila comparten altura. Eso no es un detalle
/// estético: es lo que hace que el resultado se lea como una caja wulf y no
/// como columnas sueltas de distinto largo. Es además el patrón que la
/// [decisión 0023](../../../docs/decisiones/0023-refinamiento-wulf-post-implementacion.md)
/// ya prescribía (`IntrinsicHeight` + `stretch`).
///
/// No usa `GridView` por tres razones:
/// - `GridView` no sabe de celdas que ocupen varias columnas sin traerse un
///   paquete de grid escalonado;
/// - reparte por `maxCrossAxisExtent`, o sea que el número de columnas termina
///   siendo un efecto secundario del ancho disponible en vez de una decisión —
///   que es exactamente el problema que este widget viene a resolver;
/// - su `spacing` es un margen, y acá el hueco entre pozos es MATERIAL de
///   bandeja (`doc/reglas.md`, regla 2), no fondo asomándose.
class WulfCuadricula extends StatelessWidget {
  /// Reparte [hijos] en filas de seis columnas.
  const WulfCuadricula({
    super.key,
    required this.hijos,
    this.separacion = WulfSpacing.pared,
    this.anchoForzado,
    this.llenarAlto = false,
  });

  /// Las celdas, en orden de lectura. Se empaquetan en filas según lo que
  /// ocupe cada una.
  final List<WulfCelda> hijos;

  /// El hueco entre celdas. Por defecto la pared compartida entre pozos.
  final double separacion;

  /// Para componer contra el ancho de una caja y no el de la ventana. Sirve
  /// cuando la retícula vive dentro de un panel angosto.
  final WulfAncho? anchoForzado;

  /// Las filas se reparten el alto disponible en vez de medir su contenido.
  ///
  /// Hace falta cuando las celdas contienen algo que SCROLLEA. `IntrinsicHeight`
  /// —que es como se comparte altura en el modo normal— pide a sus hijos que
  /// declaren su alto natural, y un `ListView` no sabe hacerlo: revienta con
  /// altura infinita. Es el mismo tropiezo que ya había dejado un comentario en
  /// Reportes antes de que existiera este widget.
  ///
  /// Exige que el padre acote el alto (un `Expanded`, por ejemplo).
  final bool llenarAlto;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final ancho = anchoForzado ?? WulfAncho.deAncho(constraints.maxWidth);
        final filas = _repartirEnFilas(ancho);

        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          mainAxisSize: llenarAlto ? MainAxisSize.max : MainAxisSize.min,
          children: [
            for (var i = 0; i < filas.length; i++) ...[
              if (i > 0) SizedBox(height: separacion),
              if (llenarAlto) Expanded(child: _fila(filas[i], ancho)) else _fila(filas[i], ancho),
            ],
          ],
        );
      },
    );
  }

  /// Empaqueta las celdas en filas sin partir ninguna: una celda que no entra en
  /// lo que queda de la fila abre la siguiente.
  List<List<WulfCelda>> _repartirEnFilas(WulfAncho ancho) {
    final filas = <List<WulfCelda>>[];
    var actual = <WulfCelda>[];
    var ocupadas = 0;

    for (final celda in hijos) {
      final columnas = celda.columnasEn(ancho);
      if (ocupadas + columnas > wulfColumnas && actual.isNotEmpty) {
        filas.add(actual);
        actual = <WulfCelda>[];
        ocupadas = 0;
      }
      actual.add(celda);
      ocupadas += columnas;
    }
    if (actual.isNotEmpty) filas.add(actual);
    return filas;
  }

  Widget _fila(List<WulfCelda> celdas, WulfAncho ancho) {
    final ocupadas = celdas.fold<int>(0, (suma, c) => suma + c.columnasEn(ancho));

    final fila = Row(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        for (var i = 0; i < celdas.length; i++) ...[
          if (i > 0) SizedBox(width: separacion),
          Expanded(flex: celdas[i].columnasEn(ancho), child: celdas[i].hijo),
        ],
        // Una fila a medio llenar mantiene el ancho de sus celdas en vez de
        // estirarlas: si la última fila tuviera celdas más anchas que las de
        // arriba, la retícula dejaría de leerse como una retícula.
        if (ocupadas < wulfColumnas) ...[
          SizedBox(width: separacion),
          Expanded(flex: wulfColumnas - ocupadas, child: const SizedBox()),
        ],
      ],
    );

    // Sin `llenarAlto`, `IntrinsicHeight` es lo que le da al `Row` una altura
    // finita real antes de repartirla con `stretch`. Con `llenarAlto` el padre
    // ya acotó el alto, así que `stretch` alcanza — y meter `IntrinsicHeight`
    // ahí rompería con cualquier hijo que scrollee.
    return llenarAlto ? fila : IntrinsicHeight(child: fila);
  }
}
