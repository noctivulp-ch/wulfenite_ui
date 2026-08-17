import 'package:flutter/material.dart';

import '../foco/foco.dart';
import '../tokens/efectos.dart';
import '../superficies/tinta.dart';

// CONTRATO DE DIRECCION (ver `doc/reglas.md`).
//
// THESIS: la pantalla es UNA bandeja de una sola pieza con el contenido
// hundido en pozos tallados, no una pagina de tarjetas flotando. Rechaza el
// modelo de Material 3 donde importancia = elevacion = superficie mas clara.
//
// OWN-WORLD: dos lacas completas, una por modo, tomadas de la construccion
// real de las cajas de las referencias (negras por fuera, ROJAS por dentro):
// oscuro = marco negro + pozos rojos + metal; claro = su reflejo, marco
// blanco + pozos verdes + metal. Marco continuo con borde exterior 24,
// paredes compartidas 12, radio exterior 20 contra radio de pozo 4. Cada pozo
// lleva borde de 1px y sombra de contacto arriba. El acento (oro/jade/peltre)
// es el unico acento y solo marca accion primaria y seleccion. Cero BoxShadow.
//
// STORY: la registradora distingue de un golpe que pozo es que, por tamano y
// por hueco; el marco nunca compite con los datos.
//
// FIRST VIEWPORT: rail izquierdo como banda de bandeja, grid de productos
// como pozos iguales, total en un pozo grande y propio.
//
// FORM: mundo fijado por el usuario (wulf), re-renderizado desde su rango
// material completo. No hubo tirada de concepto porque el mundo no se
// reemplaza: la falla anterior fue de ejecucion, no de seleccion.

/// Tokens de espaciado. Unidad base 8 (regla del sistema:
/// solo multiplos, nunca valores ad hoc).
class WulfSpacing {
  const WulfSpacing._();

  /// La unidad base. Todo lo demás es un múltiplo suyo; un valor que no lo sea
  /// rompe el ritmo aunque a ojo no se note.
  static const double unidad = 8;

  /// Separación entre elementos emparentados dentro de un mismo pozo.
  static const double gapCelda = unidad * 2; // 16

  /// Separación entre grupos distintos de contenido.
  static const double gapGrupo = unidad * 3; // 24

  /// Aire interior de un pozo normal.
  static const double paddingCelda = unidad * 2; // 16

  /// Aire interior de un pozo protagonista, del que se mira primero.
  static const double paddingCeldaGrande = unidad * 3; // 24

  /// Ancho de la pared compartida entre dos pozos adyacentes. NO es "el
  /// fondo asomandose" — es material de bandeja (`doc/reglas.md`, regla 2), asi
  /// que dos pozos vecinos comparten UNA sola pared de este ancho.
  static const double pared = unidad * 1.5; // 12

  /// Borde exterior de la bandeja. Mas grueso que la pared interior, como el
  /// labio de una bandeja real.
  static const double rim = unidad * 3; // 24

  /// El labio en un televisor, que es el doble.
  ///
  /// No es estetica: un televisor recorta los bordes de la imagen (overscan), y
  /// la guia de Android TV pide dejar libre un 5% por lado. Con el labio normal
  /// de 24, a 1080p quedan 48 px de 1920 — la MITAD de lo que hace falta, medido
  /// en el emulador. Lo que se pierde ahi es justo la cresta de los pozos de los
  /// extremos, que es la senal primaria de profundidad (regla 1).
  ///
  /// A diferencia de la gemela de Compose, aca hay que pedirlo: Flutter no
  /// expone el `uiMode` de Android, asi que quien sabe que corre en un televisor
  /// es la app. `WulfBandeja(television: true)`.
  static const double rimTelevision = unidad * 6; // 48
}

/// Los dos únicos radios del sistema.
///
/// Dos y no una escala: el interior de la bandeja es rectilíneo y el canto
/// exterior es suave. Un radio intermedio no significa nada.
class WulfRadius {
  const WulfRadius._();

  /// Interior rectilineo: los compartimentos de una bandeja no son
  /// rectangulos redondeados sueltos.
  static const double pozo = 4;

  /// Exterior suave, como el canto de la bandeja.
  static const double bandeja = 20;
}

/// Duracion del feedback tactil (onda al presionar).
///
/// A 200 el toque no llegaba a leerse — la onda crecia y se apagaba antes de
/// que la pantalla reaccionara, asi que el elemento tocado nunca se veia lleno.
/// Ahora crece en 261, se sostiene un tercio del desvanecido y recien ahi se
/// apaga. El numero exacto sale de probarlo con el dedo en el aparato
/// (test_driver/lab_toque.dart), no de una tabla.
const Duration wulfFeedbackTactilDuracion = Duration(milliseconds: 261);

/// Objetivo tactil minimo. Corre en tablet en el mostrador (escena de uso
/// confirmada), asi que todo lo tocable respeta esto — DESIGN.md, regla 6.
const double wulfObjetivoTactilMin = 48;

/// Lado de la miniatura de producto en una fila de lista.
///
/// Deliberadamente por DEBAJO de [wulfObjetivoTactilMin]: no es un objetivo
/// tactil, es un hueco decorativo dentro de una fila que ya es tocable entera.
/// Estaba escrito a mano como `44` en dos pantallas, y ese numero suelto invita
/// a "corregirlo" a 48 creyendo que incumple la regla 6.
const double wulfMiniatura = 44;

/// Metal del acento. Sobre laca, el pan de metal tradicional es oro, pero
/// peltre tambien lo es, y el jade es la incrustacion de piedra que acompana al
/// pan — asi que el ajuste configurable de la decision 0020 pasa a elegir el
/// ACENTO, no el color de la laca.
///
/// El tercero era cobre y paso a jade: cobre y oro son los dos un marron
/// anaranjado y a simple vista no se distinguian, o sea que el ajuste ofrecia
/// tres opciones y entregaba dos. El jade separa la paleta de verdad y sigue
/// siendo material de laca.
///
/// El reparto de responsabilidades es lo importante: el MODO (claro/oscuro)
/// determina la laca completa (blanco-verde u oscuro-rojo) y el metal es
/// ortogonal, igual en los dos modos. Antes el ajuste variaba la bandeja, lo
/// que chocaba con que el modo tambien la variara.
///
/// Conviene resolver el acento guardado con `orElse` al primero: así, si algún
/// día se quita un metal, una preferencia vieja cae sola en vez de reventar.
enum WulfAcento {
  /// Latón cálido. El de fábrica.
  oro,

  /// Verde metálico, más frío.
  jade,

  /// Gris metálico, el más neutro. Para cuando el color no debe opinar.
  peltre,
}

/// Tokens de material que `ColorScheme` no tiene como expresar: la bandeja
/// (el plano continuo), la cresta de pared iluminada y la sombra de contacto
/// dentro del pozo.
///
/// El mapeo deliberado con `ColorScheme`: `surface` es el POZO, no la
/// bandeja, para que el texto por defecto de cualquier widget caiga correcto
/// sobre el fondo de contenido. La bandeja va en `scaffoldBackgroundColor` y
/// en estos tokens, y se usa explicitamente en el cromo (rail, paredes).
@immutable
class WulfLaca extends ThemeExtension<WulfLaca> {
  /// Los seis tonos de un acabado. Se obtiene del tema con [lacaDe], no se
  /// construye a mano salvo para inventar un acabado nuevo.
  const WulfLaca({
    required this.bandeja,
    required this.paredCresta,
    required this.pozo,
    required this.pozoContacto,
    required this.textoEnBandeja,
    required this.textoAtenuadoEnBandeja,
  });

  /// El plano continuo de la bandeja: fondo de pantalla y material de las
  /// paredes entre pozos.
  final Color bandeja;

  /// Ribete del pozo: 1px en su borde. Es la senal PRIMARIA de tallado
  /// —verificada >=3:1 contra la bandeja en los 3 metales y los 2 modos,
  /// porque en modo oscuro el escalon de valor bandeja/pozo no puede llegar a
  /// 3:1 (rango de luminancia comprimido)— y es el METAL APAGADO, no un tono
  /// fijo: en laca real el pan de metal ribetea los cantos de la pieza. Con un
  /// tono fijo, elegir peltre dejaba el elemento seleccionado en plata y todos
  /// los demas bordes leyendose dorados. Ver `doc/reglas.md`, reglas 1 y 4.
  final Color paredCresta;

  /// Fondo del pozo: la superficie donde vive el contenido.
  final Color pozo;

  /// Sombra que la pared proyecta hacia dentro del pozo, en su borde
  /// superior. Es un degradado corto y duro, nunca una sombra suave: bajo sol
  /// directo las sombras de baja opacidad desaparecen.
  final Color pozoContacto;

  /// Texto sobre la bandeja — el cromo, no el contenido.
  final Color textoEnBandeja;

  /// Texto secundario sobre la bandeja: rótulos y apoyos.
  final Color textoAtenuadoEnBandeja;

  @override
  WulfLaca copyWith({
    Color? bandeja,
    Color? paredCresta,
    Color? pozo,
    Color? pozoContacto,
    Color? textoEnBandeja,
    Color? textoAtenuadoEnBandeja,
  }) {
    return WulfLaca(
      bandeja: bandeja ?? this.bandeja,
      paredCresta: paredCresta ?? this.paredCresta,
      pozo: pozo ?? this.pozo,
      pozoContacto: pozoContacto ?? this.pozoContacto,
      textoEnBandeja: textoEnBandeja ?? this.textoEnBandeja,
      textoAtenuadoEnBandeja: textoAtenuadoEnBandeja ?? this.textoAtenuadoEnBandeja,
    );
  }

  @override
  WulfLaca lerp(WulfLaca? other, double t) {
    if (other == null) return this;
    return WulfLaca(
      bandeja: Color.lerp(bandeja, other.bandeja, t)!,
      paredCresta: Color.lerp(paredCresta, other.paredCresta, t)!,
      pozo: Color.lerp(pozo, other.pozo, t)!,
      pozoContacto: Color.lerp(pozoContacto, other.pozoContacto, t)!,
      textoEnBandeja: Color.lerp(textoEnBandeja, other.textoEnBandeja, t)!,
      textoAtenuadoEnBandeja: Color.lerp(textoAtenuadoEnBandeja, other.textoAtenuadoEnBandeja, t)!,
    );
  }
}

/// Atajo para leer los tokens de laca desde un widget.
WulfLaca lacaDe(BuildContext context) => Theme.of(context).extension<WulfLaca>()!;

// --- Paleta de autoria propia -----------------------------------------------
// Nada de `ColorScheme.fromSeed`: el algoritmo tonal de Material 3 codifica
// elevacion-como-claridad, que es exactamente lo contrario de un pozo, y fue
// la causa raiz del aspecto por defecto (DESIGN.md, regla 5).
//
// Contraste verificado con la formula WCAG sobre los pares realmente usados
// (texto >=4.5:1; bordes de superficie >=3:1 por WCAG 1.4.11). La prueba
// `test/contraste_laca_test.dart` recalcula estos numeros contra estas
// mismas constantes, para que no se degraden en una edicion futura.

// Dos lacas completas, una por modo. La caja wulf fisica no cambia de color,
// pero una app si — y sostener que la bandeja siguiera oscura en modo claro
// hacia que los dos modos se vieran casi iguales (parecia un "modo contraste",
// no un modo claro) y era la causa raiz de la mala legibilidad: todo lo que
// caia sobre la bandeja heredaba color de texto de pozo.
//
// El par sale de la construccion real de las cajas de laca de las referencias
// (docs/pantallas/referencias-visuales/): NEGRAS por fuera y ROJAS por dentro.
// El modo claro es su reflejo: BLANCA por fuera, VERDE por dentro.
//
//   oscuro -> negro (marco) + rojo (pozos) + metal
//   claro  -> blanco (marco) + verde (pozos) + metal

// Marco negro, interior de laca roja.
const _bandejaOscura = Color(0xFF14100E);
const _pozoOscuro = Color(0xFF3A1E1B);
const _pozoContactoOscuro = Color(0xFF2D110E);
const _textoEnPozoOscuro = Color(0xFFF5EAE2); // 12.85:1
const _textoSecundarioEnPozoOscuro = Color(0xFFCBA99C); // 7.02:1
const _textoEnBandejaOscura = Color(0xFFEDE4DC); // 15.07:1
const _textoAtenuadoEnBandejaOscura = Color(0xFFA99C93); // 7.08:1

// Marco blanco, interior de laca verde.
const _bandejaClara = Color(0xFFF8F7F2);
const _pozoClaro = Color(0xFFD7E1D3);
const _pozoContactoClaro = Color(0xFFC4D2BF);
const _textoEnPozoClaro = Color(0xFF1E2A1C); // 11.12:1
const _textoSecundarioEnPozoClaro = Color(0xFF495747); // 5.70:1
const _textoEnBandejaClara = Color(0xFF232522); // 14.40:1
const _textoAtenuadoEnBandejaClara = Color(0xFF55584F); // 6.76:1

// Metal del acento, ortogonal al modo. Cada uno verificado como texto sobre
// bandeja Y sobre pozo, y como relleno con su propio color de texto encima.
const _metales = {
  WulfAcento.oro: (claro: Color(0xFF7A4F0E), oscuro: Color(0xFFD9A54A)),
  WulfAcento.jade: (claro: Color(0xFF156353), oscuro: Color(0xFF5FCBAB)),
  WulfAcento.peltre: (claro: Color(0xFF4F5450), oscuro: Color(0xFFC6C4BC)),
};
const _sobreMetalClaro = Color(0xFFFFFFFF);
const _sobreMetalOscuro = {
  WulfAcento.oro: Color(0xFF241A0C),
  WulfAcento.jade: Color(0xFF06231C),
  WulfAcento.peltre: Color(0xFF1E1D1A),
};

const _rojoErrorClaro = Color(0xFF9E2F1B);
const _rojoErrorOscuro = Color(0xFFF0A08C);
const _sobreErrorClaro = Color(0xFFFFFFFF);
const _sobreErrorOscuro = Color(0xFF2A0D07);

/// Cuanto se apaga el metal para servir de ribete. En laca real el pan de
/// metal ribetea los cantos de la pieza, asi que el borde de los pozos ES el
/// metal — pero apagado, para que la seleccion (metal a plena fuerza) siga
/// destacando. Con un borde de tono fijo, elegir peltre dejaba el resto de la
/// pantalla leyendose dorada: el ajuste cambiaba una pieza y desafinaba todo
/// lo demas.
const _apagadoRibeteClaro = 0.40;
const _apagadoRibeteOscuro = 0.50;

/// Ribete: el metal mezclado hacia el pozo. Derivado y no hardcodeado para que
/// siga siendo correcto si un metal cambia. Verificado >=3:1 contra la bandeja
/// en los 3 metales x 2 modos (`test/contraste_laca_test.dart`).
Color _ribeteDe(WulfAcento acento, Brightness brillo) {
  final claro = brillo == Brightness.light;
  final metal = claro ? _metales[acento]!.claro : _metales[acento]!.oscuro;
  final pozo = claro ? _pozoClaro : _pozoOscuro;
  return Color.lerp(metal, pozo, claro ? _apagadoRibeteClaro : _apagadoRibeteOscuro)!;
}

WulfLaca _lacaDe(WulfAcento acento, Brightness brillo) {
  final claro = brillo == Brightness.light;
  return WulfLaca(
    bandeja: claro ? _bandejaClara : _bandejaOscura,
    paredCresta: _ribeteDe(acento, brillo),
    pozo: claro ? _pozoClaro : _pozoOscuro,
    pozoContacto: claro ? _pozoContactoClaro : _pozoContactoOscuro,
    textoEnBandeja: claro ? _textoEnBandejaClara : _textoEnBandejaOscura,
    textoAtenuadoEnBandeja: claro ? _textoAtenuadoEnBandejaClara : _textoAtenuadoEnBandejaOscura,
  );
}

ColorScheme _esquemaDe(WulfAcento acento, Brightness brillo) {
  final claro = brillo == Brightness.light;
  final pozo = claro ? _pozoClaro : _pozoOscuro;
  final textoEnPozo = claro ? _textoEnPozoClaro : _textoEnPozoOscuro;
  final metal = claro ? _metales[acento]!.claro : _metales[acento]!.oscuro;
  final sobreMetal = claro ? _sobreMetalClaro : _sobreMetalOscuro[acento]!;

  return ColorScheme(
    brightness: brillo,
    primary: metal,
    onPrimary: sobreMetal,
    primaryContainer: metal,
    onPrimaryContainer: sobreMetal,
    secondary: _ribeteDe(acento, brillo),
    onSecondary: claro ? _bandejaClara : _textoEnBandejaOscura,
    error: claro ? _rojoErrorClaro : _rojoErrorOscuro,
    onError: claro ? _sobreErrorClaro : _sobreErrorOscuro,
    // `surface` es el POZO, no la bandeja: asi el texto por defecto de
    // cualquier widget cae legible sobre el fondo de contenido.
    surface: pozo,
    onSurface: textoEnPozo,
    onSurfaceVariant: claro ? _textoSecundarioEnPozoClaro : _textoSecundarioEnPozoOscuro,
    // Toda la escalera de contenedores de M3 apunta al mismo pozo: neutraliza
    // el "mas elevado = mas claro" para que ningun widget derive solo hacia
    // un tono mas claro y vuelva a parecer una tarjeta flotante.
    surfaceContainerLowest: pozo,
    surfaceContainerLow: pozo,
    surfaceContainer: pozo,
    surfaceContainerHigh: pozo,
    surfaceContainerHighest: pozo,
    // La cresta es literalmente el contorno del pozo.
    outline: _ribeteDe(acento, brillo),
    outlineVariant: claro ? _pozoContactoClaro : _pozoContactoOscuro,
  );
}

/// Escala tipografica modular sobre la base de Material 3.
///
/// Guia de uso:
/// - Hero de una pantalla (ej. total del dia en Reportes) -> `displaySmall`
/// - Titulo de seccion destacada -> `headlineSmall` (24)
/// - Subtitulo -> `titleLarge` (20)
/// - Texto de contenido normal -> `bodyLarge` (16, base)
/// - Texto secundario / metadata -> `bodyMedium` (14)
/// - Etiqueta de boton -> `labelLarge` (14)
/// - Texto auxiliar chico (fecha, nota) -> `bodySmall` (12)
TextTheme _escalaTipografica(TextTheme base) {
  return base.copyWith(
    displaySmall: base.displaySmall?.copyWith(fontSize: 40, fontWeight: FontWeight.w600),
    headlineLarge: base.headlineLarge?.copyWith(fontWeight: FontWeight.w600),
    headlineSmall: base.headlineSmall?.copyWith(fontWeight: FontWeight.w600),
    titleLarge: base.titleLarge?.copyWith(fontSize: 20, fontWeight: FontWeight.w500),
  );
}

/// Tono del toque: el material del pozo separado de su color de reposo.
///
/// No sirve un tono fijo ni `pozoContacto` —que es la SOMBRA de contacto, o sea
/// más oscura que el pozo: usarla restaba luminosidad y el toque quedaba casi
/// invisible—. Lo que hace falta es alejarse del reposo en la dirección que
/// contrasta: aclarar sobre material oscuro, oscurecer sobre material claro.
Color _tonoDeToque(Color pozo, Brightness brightness, double separacion) {
  final hacia = brightness == Brightness.dark ? Colors.white : Colors.black;
  return Color.lerp(pozo, hacia, separacion.clamp(0.0, 1.0))!;
}

/// El contorno de un control, que engorda al recibir el foco (regla 12).
///
/// Se resuelve por estado y no con un valor fijo porque es la única forma de que
/// el foco se vea en un botón: sin esto, un mando de televisor recorre los
/// botones de la app y nada en pantalla lo dice.
WidgetStateProperty<BorderSide> _contornoConFoco(Color color, double reposo) {
  return WidgetStateProperty.resolveWith((estados) {
    final enfocado = estados.contains(WidgetState.focused);
    return BorderSide(color: color, width: enfocado ? WulfCanto.enfocado : reposo);
  });
}

/// Los botones pintan un velo propio (`overlayColor`) ADEMÁS del splash, y de
/// fábrica ese velo es blanco translúcido. Era lo que hacía que el toque se
/// viera con color en unos sitios y blanco en otros: donde había un botón,
/// ganaba el velo. Anulado, manda siempre la onda.
const WidgetStateProperty<Color> _sinVeloDeBoton = WidgetStatePropertyAll(Colors.transparent);

/// El tema completo del sistema para un [acento] y una luminosidad.
///
/// Es el único punto de entrada: devuelve un `ThemeData` con el `ColorScheme`
/// mapeado a propósito —`surface` es el **pozo**, no la bandeja— más la
/// extensión [WulfLaca] con lo que `ColorScheme` no sabe expresar.
///
/// ```dart
/// MaterialApp(theme: buildWulfTheme(WulfAcento.oro));
/// ```
///
/// [toque] cambia la receta de la onda del toque y [efectos] permite apagar
/// los adornos caros en aparatos lentos.
ThemeData buildWulfTheme(
  WulfAcento acento, {
  Brightness brightness = Brightness.light,
  WulfToqueReceta toque = const WulfToqueReceta(),
  WulfEfectos efectos = const WulfEfectos(),
}) {
  final scheme = _esquemaDe(acento, brightness);
  final laca = _lacaDe(acento, brightness);
  final base = ThemeData(useMaterial3: true, colorScheme: scheme);

  return base.copyWith(
    extensions: [laca, efectos],
    textTheme: _escalaTipografica(base.textTheme),
    // La bandeja es el plano de fondo de toda pantalla.
    scaffoldBackgroundColor: laca.bandeja,
    // El toque se pinta con el material del elemento, no con blanco: el pozo
    // SEPARADO de su tono de reposo —más claro en oscuro, más oscuro en
    // claro— y el frente de la onda con la cresta, que es literalmente su
    // borde.
    splashFactory: WulfGrainSplash.factoriaDe(
      relleno: _tonoDeToque(laca.pozo, brightness, toque.separacion),
      cresta: laca.paredCresta,
      receta: toque,
    ),
    // Sin estas dos, encima de la onda se pintaba el resaltado de fábrica:
    // un rectángulo BLANCO translúcido. Era el "sigue blanca" que se veía —
    // el color de la onda estaba bien debajo. El resaltado lo hace ahora la
    // onda misma, que se sostiene hasta apagarse.
    highlightColor: Colors.transparent,
    splashColor: _tonoDeToque(laca.pozo, brightness, toque.separacion),
    // Sin elevacion ni sombra: la profundidad la da la cresta, no una sombra
    // proyectada (DESIGN.md, regla 1).
    cardTheme: CardThemeData(
      color: laca.pozo,
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(WulfRadius.pozo)),
      margin: EdgeInsets.zero,
    ),
    dialogTheme: DialogThemeData(
      backgroundColor: laca.pozo,
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(WulfRadius.bandeja)),
      insetPadding: const EdgeInsets.all(WulfSpacing.gapGrupo),
    ),
    inputDecorationTheme: InputDecorationThemeData(
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(WulfRadius.pozo)),
      // En reposo el campo es un hueco (borde de pozo); al enfocarse pasa a ser
      // accion, y entonces toma el metal — misma logica que los botones.
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(WulfRadius.pozo),
        borderSide: BorderSide(color: laca.paredCresta),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(WulfRadius.pozo),
        borderSide: BorderSide(color: scheme.primary, width: 2),
      ),
      contentPadding: const EdgeInsets.symmetric(
        horizontal: WulfSpacing.paddingCelda,
        vertical: WulfSpacing.unidad * 1.5,
      ),
    ),
    // El contorno de un boton es el metal, no el borde del pozo: el acento
    // marca accion, y un boton ES accion (DESIGN.md, regla 4). Antes heredaban
    // `outline`, que es material de bandeja, y no se distinguian de un hueco.
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        minimumSize: const Size(0, wulfObjetivoTactilMin),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(WulfRadius.pozo)),
      ).copyWith(
        overlayColor: _sinVeloDeBoton,
        side: _contornoConFoco(scheme.primary, WulfCanto.reposo),
      ),
    ),
    segmentedButtonTheme: SegmentedButtonThemeData(
      style: SegmentedButton.styleFrom(
        side: BorderSide(color: scheme.primary),
      ).copyWith(overlayColor: _sinVeloDeBoton),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        minimumSize: const Size(0, wulfObjetivoTactilMin),
        padding: const EdgeInsets.symmetric(
          horizontal: WulfSpacing.paddingCeldaGrande,
          vertical: WulfSpacing.paddingCelda,
        ),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(WulfRadius.pozo)),
      ).copyWith(
        overlayColor: _sinVeloDeBoton,
        // En el relleno el contorno solo aparece con el foco: el metal ya ocupa
        // toda la pieza, asi que el canto se dibuja con el color del texto para
        // que se separe de el.
        side: WidgetStateProperty.resolveWith((estados) {
          if (!estados.contains(WidgetState.focused)) return BorderSide.none;
          return BorderSide(color: scheme.onPrimary, width: WulfCanto.enfocado);
        }),
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        minimumSize: const Size(0, wulfObjetivoTactilMin),
        padding: const EdgeInsets.symmetric(
          horizontal: WulfSpacing.paddingCelda,
          vertical: WulfSpacing.unidad,
        ),
      ).copyWith(
        overlayColor: _sinVeloDeBoton,
        // Plano no lleva contorno en reposo — es lo que lo hace plano—, pero
        // enfocado tiene que verse igual que los demas o el mando lo salta sin
        // que nadie note que paso por el.
        side: WidgetStateProperty.resolveWith((estados) {
          if (!estados.contains(WidgetState.focused)) return BorderSide.none;
          return BorderSide(color: scheme.primary, width: WulfCanto.enfocado);
        }),
      ),
    ),
    iconButtonTheme: IconButtonThemeData(style: ButtonStyle(overlayColor: _sinVeloDeBoton)),
    listTileTheme: ListTileThemeData(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(WulfRadius.pozo)),
      minVerticalPadding: WulfSpacing.unidad * 1.5,
      contentPadding: const EdgeInsets.symmetric(
        horizontal: WulfSpacing.paddingCelda,
        vertical: WulfSpacing.unidad,
      ),
    ),
    appBarTheme: AppBarThemeData(
      centerTitle: false,
      elevation: 0,
      backgroundColor: laca.bandeja,
      foregroundColor: laca.textoEnBandeja,
    ),
  );
}
