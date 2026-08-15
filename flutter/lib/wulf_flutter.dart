/// Wulfenite-UI — superficies lacadas con canto metálico.
///
/// El nombre viene del mineral: placas cuadradas y finas, con brillo metálico
/// ámbar. Es lo que hace el sistema — una bandeja de laca con pozos hundidos,
/// separados por un canto de un píxel que es lo único que carga la profundidad.
///
/// ## Las tres piezas
///
/// **La bandeja y el pozo.** La geometría va al revés de lo habitual: el
/// contenido se **hunde** en la bandeja en vez de flotar sobre el fondo. Nada
/// de sombras suaves — la profundidad la carga el canto.
///
/// **La retícula de seis columnas.** Cada pieza declara cuántas columnas ocupa
/// en cada tamaño de pantalla. Seis porque se divide en dos y en tres, que son
/// los repartos que piden las pantallas de verdad.
///
/// **Los componentes con sus reglas dentro.** Un diálogo pone la acción que
/// confirma a la derecha porque para guardar se pulsa a la derecha sin mirar;
/// un enlace va subrayado y en el color del acento porque el color solo no
/// basta. Las reglas no se recuerdan: están escritas en el widget y hay pruebas
/// que las vigilan.
///
/// ## Empezar
///
/// ```dart
/// MaterialApp(
///   theme: construirWulfTheme(WulfAcento.oro),
///   home: Scaffold(
///     body: WulfBandeja(
///       child: WulfPozo(child: Text('Hola')),
///     ),
///   ),
/// )
/// ```
///
/// Las reglas completas, con el porqué de cada una, están en `../doc/reglas.md`.
library;

export 'src/componentes/aviso.dart';
export 'src/componentes/dialogo.dart';
export 'src/componentes/enlace.dart';
export 'src/componentes/falla.dart';
export 'src/foco/foco.dart';
export 'src/reticula/cuadricula.dart';
export 'src/superficies/bandeja.dart';
export 'src/superficies/tinta.dart';
export 'src/superficies/velo.dart';
export 'src/texto/etiqueta_ajustada.dart';
export 'src/texto/tallado.dart';
export 'src/tokens/efectos.dart';
export 'src/tokens/tema.dart';
