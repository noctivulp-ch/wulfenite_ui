import 'package:flutter/material.dart';

import '../tokens/efectos.dart';

/// Tallado: el glifo se lee grabado en su superficie en vez de apoyado encima.
///
/// Calibrado por el usuario en el banco de pruebas (`test_driver/sandbox.dart`)
/// y exportado el 2026-07-30: profundidad 1.25, luz 0.70, sombra 0.65 — una
/// incision poco profunda con el canto brillante, mas "metal pulido" que
/// "surco". Esos tres valores se toman tal cual.
///
/// Lo que NO se toma tal cual es la fusion del glifo con el fondo. El usuario
/// la calibro en 0.50 mirando modo oscuro, donde queda al borde (4.30:1), pero
/// la paleta clara tiene mucho menos margen de luminancia y ahi la misma fusion
/// cae a 2.83:1. Por eso la fusion es por MODO y por NIVEL, con los techos
/// medidos:
///
/// | | fusion max 4.5:1 | fusion max 3:1 |
/// |---|---|---|
/// | oscuro | 0.48 | 0.62 |
/// | claro | 0.32 | 0.47 |
///
/// El tallado se consigue acercando el glifo al color de su superficie, asi que
/// es literalmente contraste gastado en estetica. Gastarlo donde el tamano lo
/// compensa (numeros grandes, iconos) y no donde hay que leer.
enum TalladoNivel {
  /// Texto de lectura. Se queda por debajo del techo de 4.5:1 (AA normal).
  sutil,

  /// Texto de 24px o mas e iconos, donde WCAG permite 3:1 — texto grande por
  /// 1.4.3, elementos no textuales por 1.4.11. Aca el tallado se ve de verdad.
  marcado,
}

/// Qué le pasa al glifo respecto de su superficie.
///
/// El sistema usa [tallado] y nada más. Los otros dos existen porque **hacen
/// falta para elegir**: sin poder ver el realzado al lado, "hundido" es una
/// palabra y no una decisión — y más de una vez lo que alguien cree hundido es
/// en realidad lo contrario. El banco de estilo del ejemplo los compara.
enum WulfTratamiento {
  /// Sin relieve. El glifo se apoya encima de la superficie.
  plano,

  /// Grabado: el borde de arriba queda en sombra y el de abajo atrapa luz, como
  /// una letra incisa con luz desde arriba. Es el del sistema.
  tallado,

  /// Lo contrario: el glifo sobresale de la superficie.
  realzado,
}

/// Desplazamiento en px de las capas de luz y sombra.
const double wulfTalladoProfundidad = 1.25;

const double _luzIntensidad = 0.70;
const double _sombraIntensidad = 0.65;

const _fusion = {
  TalladoNivel.sutil: (claro: 0.28, oscuro: 0.42),
  TalladoNivel.marcado: (claro: 0.46, oscuro: 0.62),
};

/// Color de glifo tallado: el color base acercado a su superficie.
///
/// [sobre] es la superficie donde cae el glifo — el pozo o la bandeja. Pasarla
/// mal es el bug que ya aparecio dos veces en este proyecto, asi que es
/// obligatoria y no tiene default.
Color wulfColorTallado(
  BuildContext context, {
  required Color base,
  required Color sobre,
  TalladoNivel nivel = TalladoNivel.sutil,
}) {
  final claro = Theme.of(context).brightness == Brightness.light;
  final f = _fusion[nivel]!;
  return Color.lerp(base, sobre, claro ? f.claro : f.oscuro)!;
}

/// Aplica el relieve a cualquier hijo — texto, icono de Material o SVG propio.
///
/// `ColorFiltered` con `srcATop` respeta el alfa del hijo, asi que no hace
/// falta una implementacion por tipo de widget.
class WulfTallado extends StatelessWidget {
  /// Talla [child] sobre el color [sobre], del que salen la luz y la sombra.
  ///
  /// Es el constructor de una app: se declara dónde cae el glifo y el sistema
  /// resuelve el resto.
  const WulfTallado({
    super.key,
    required this.child,
    required Color this.sobre,
    this.profundidad = wulfTalladoProfundidad,
    this.tratamiento = WulfTratamiento.tallado,
  }) : luz = null,
       sombra = null;

  /// Talla [child] con [luz] y [sombra] dados a mano.
  ///
  /// Es el constructor de un banco de estilo: sirve para probar combinaciones
  /// que el sistema no elegiría. En una app se usa el otro — ahí el relieve
  /// tiene que salir de la superficie real, o deja de parecer relieve en cuanto
  /// cambia el fondo.
  const WulfTallado.conColores({
    super.key,
    required this.child,
    required Color this.luz,
    required Color this.sombra,
    this.profundidad = wulfTalladoProfundidad,
    this.tratamiento = WulfTratamiento.tallado,
  }) : sobre = null;

  /// Lo que se graba. Normalmente un `Text` o un icono.
  final Widget child;

  /// El color de la superficie donde se graba. De ahí salen la luz y la sombra
  /// del relieve —para que funcione igual sobre pozo o sobre bandeja—, así que
  /// tiene que ser el color REAL de detrás. Pasarlo mal es el fallo que más se
  /// repite con esta pieza.
  ///
  /// Nulo solo con [WulfTallado.conColores], donde los dos tonos vienen dados.
  final Color? sobre;

  /// Cuánto se hunde, en píxeles de desplazamiento. En 0 devuelve el hijo tal
  /// cual, que es lo que permite apagar el efecto sin cambiar el árbol.
  final double profundidad;

  /// Grabado, realzado o plano. El sistema usa grabado; los otros están para
  /// poder compararlos.
  final WulfTratamiento tratamiento;

  /// Color de la capa de luz, o nulo para derivarlo de [sobre].
  final Color? luz;

  /// Color de la capa de sombra, o nulo para derivarlo de [sobre].
  final Color? sombra;

  @override
  Widget build(BuildContext context) {
    // Apagado desde Configuración → Optimización: se devuelve el hijo tal cual,
    // sin las dos capas extra. Es el efecto más caro de la app al desplazar.
    if (profundidad <= 0 ||
        tratamiento == WulfTratamiento.plano ||
        !wulfEfectoActivo(context, WulfEfectoPesado.tallado)) {
      return child;
    }

    // La pared de arriba de la incision queda en sombra y su fondo atrapa luz
    // desde arriba: sombra desplazada hacia arriba, luz hacia abajo. Realzado es
    // exactamente lo mismo al revés, que es la razón de que sea un signo y no
    // otra implementación.
    final tonoLuz = luz ?? Color.lerp(sobre!, Colors.white, _luzIntensidad)!;
    final tonoSombra = sombra ?? Color.lerp(sobre!, Colors.black, _sombraIntensidad)!;
    final signo = tratamiento == WulfTratamiento.tallado ? 1.0 : -1.0;

    // Las dos capas de relieve son DECORACIÓN: el mismo glifo repintado en
    // sombra y en luz. `ExcludeSemantics` las saca del árbol de accesibilidad.
    //
    // Sin esto cada texto tallado aparecía TRES veces: un lector de pantalla
    // leía "Arroz cantones (entero)" tres seguidas, y la navegación por
    // semántica del harness recibía la etiqueta triplicada. Se vio al listar
    // los interactuables de Venta Rápida — el efecto visual no lo delataba de
    // ninguna manera.
    Widget capa(Color color, double dy) {
      return ExcludeSemantics(
        child: Transform.translate(
          offset: Offset(0, dy),
          child: ColorFiltered(
            colorFilter: ColorFilter.mode(color, BlendMode.srcATop),
            child: child,
          ),
        ),
      );
    }

    return Stack(
      children: [capa(tonoSombra, -profundidad * signo), capa(tonoLuz, profundidad * signo), child],
    );
  }
}

/// Texto tallado. Atajo para el caso mas comun: resuelve el color del glifo y
/// le aplica el relieve en un solo paso.
class WulfTextoTallado extends StatelessWidget {
  /// Texto grabado sobre el color [sobre].
  ///
  /// Atajo de [WulfTallado] para el caso que más se usa, con el nivel de
  /// jerarquía resuelto en profundidad.
  const WulfTextoTallado(
    this.texto, {
    super.key,
    required this.sobre,
    this.estilo,
    this.nivel = TalladoNivel.sutil,
    this.maxLines,
    this.overflow,
  });

  /// Lo que dice.
  final String texto;

  /// El color de la superficie donde se graba. De ahí salen la luz y la sombra
  /// del relieve, así que tiene que ser el color REAL de detrás.
  final Color sobre;

  /// Estilo del texto. Sin él, el del tema.
  final TextStyle? estilo;

  /// Cuánta jerarquía tiene: cuanto más alto, más hondo el grabado.
  final TalladoNivel nivel;

  /// Líneas máximas, como en `Text`.
  final int? maxLines;

  /// Qué hacer cuando no cabe, como en `Text`.
  final TextOverflow? overflow;

  @override
  Widget build(BuildContext context) {
    final base = estilo?.color ?? DefaultTextStyle.of(context).style.color!;
    // Sin relieve, el glifo recupera su color PLENO. `wulfColorTallado` lo
    // acerca a la superficie a propósito —contraste gastado en el efecto—, y
    // conservar esa fusión sin el relieve que la justifica dejaría el texto
    // lavado y peor de leer que antes de apagar nada.
    if (!wulfEfectoActivo(context, WulfEfectoPesado.tallado)) {
      return Text(
        texto,
        maxLines: maxLines,
        overflow: overflow,
        style: (estilo ?? const TextStyle()).copyWith(color: base),
      );
    }
    return WulfTallado(
      sobre: sobre,
      child: Text(
        texto,
        maxLines: maxLines,
        overflow: overflow,
        style: (estilo ?? const TextStyle()).copyWith(
          color: wulfColorTallado(context, base: base, sobre: sobre, nivel: nivel),
        ),
      ),
    );
  }
}
