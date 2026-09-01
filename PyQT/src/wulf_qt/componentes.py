"""Componentes que llevan sus reglas dentro.

Es la diferencia entre un tema y un sistema de diseño. Un tema pinta lo que le
den; esto **impide** colocar mal los botones de un diálogo, porque la única
manera de construirlo pone la acción que confirma donde tiene que ir.

Una regla escrita en una guía es una regla que alguien va a saltarse con un buen
argumento. Una regla escrita en el constructor, no.
"""

from __future__ import annotations

from PySide6.QtCore import Qt
from PySide6.QtWidgets import (
    QDialog,
    QProxyStyle,
    QStyle,
    QStyleOptionTab,
    QTabBar,
    QDialogButtonBox,
    QHBoxLayout,
    QLabel,
    QPushButton,
    QVBoxLayout,
    QWidget,
)

from .tema import ATENUADO, POZO
from .tokens import Canto, GAP_CELDA, OBJETIVO_TACTIL_MIN, PADDING_CELDA, PARED, UNIDAD


def boton_primario(texto: str, parent: QWidget | None = None) -> QPushButton:
    """El botón de la acción que confirma: relleno en metal.

    Uno por pantalla. El acento no se usa decorativamente — si estuviera en
    todos los botones no destacaría en ninguno.
    """
    b = QPushButton(texto, parent)
    b.setProperty("primario", "true")
    b.setMinimumHeight(OBJETIVO_TACTIL_MIN)
    return b


def boton(texto: str, parent: QWidget | None = None) -> QPushButton:
    """Un botón normal: un pozo pequeño."""
    b = QPushButton(texto, parent)
    b.setMinimumHeight(OBJETIVO_TACTIL_MIN)
    return b


def boton_plano(texto: str, parent: QWidget | None = None) -> QPushButton:
    """Un botón sin canto, para lo secundario que no debe pesar."""
    b = QPushButton(texto, parent)
    b.setProperty("plano", "true")
    b.setMinimumHeight(OBJETIVO_TACTIL_MIN)
    return b


class Acciones(QWidget):
    """La fila de botones de un diálogo, con el orden decidido.

    **La acción que confirma va a la derecha y cancelar a su izquierda, en la
    misma fila.** No es estética: para guardar se pulsa a la derecha sin mirar,
    y una app que lo tenga al revés hace que cancelar se pulse creyendo guardar.

    Lo que actúa **sobre el objeto** —reactivar, eliminar, elegir un archivo— va
    en [extras], que caen a la izquierda y debajo: lejos del sitio al que llega
    el pulgar a confirmar, que es justo donde no se quiere un borrado.

    ```python
    Acciones(confirmar=boton_primario("Guardar"), cancelar=boton("Cancelar"))
    ```

    Con [confirmar] en `None` —un diálogo que solo se cierra— la salida se queda
    a la derecha: sin nada que confirmar no hay confusión posible, y es donde
    llega el dedo.
    """

    def __init__(
        self,
        *,
        confirmar: QPushButton | None = None,
        cancelar: QPushButton | None = None,
        extras: list[QPushButton] | None = None,
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(parent)
        columna = QVBoxLayout(self)
        columna.setContentsMargins(0, 0, 0, 0)
        columna.setSpacing(UNIDAD)

        fila = QHBoxLayout()
        fila.setSpacing(GAP_CELDA)
        if confirmar is None:
            fila.addStretch(1)
            if cancelar is not None:
                fila.addWidget(cancelar)
        else:
            if cancelar is not None:
                fila.addWidget(cancelar)
            fila.addStretch(1)
            fila.addWidget(confirmar)
        columna.addLayout(fila)

        for extra in extras or []:
            secundaria = QHBoxLayout()
            secundaria.addWidget(extra)
            secundaria.addStretch(1)
            columna.addLayout(secundaria)


class Dialogo(QDialog):
    """Un diálogo del sistema: título, cuerpo y [Acciones].

    Se construye con las acciones ya montadas, así que no hay forma de acabar
    con el botón de guardar en el sitio equivocado.
    """

    def __init__(
        self,
        titulo: str,
        contenido: QWidget,
        acciones: Acciones,
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(parent)
        self.setWindowTitle(titulo)
        columna = QVBoxLayout(self)
        columna.setContentsMargins(
            PADDING_CELDA, PADDING_CELDA, PADDING_CELDA, PADDING_CELDA
        )
        columna.setSpacing(PARED)

        encabezado = QLabel(titulo)
        fuente = encabezado.font()
        fuente.setPointSizeF(fuente.pointSizeF() * 1.25)
        fuente.setWeight(fuente.Weight.DemiBold)
        encabezado.setFont(fuente)

        columna.addWidget(encabezado)
        columna.addWidget(contenido, 1)
        columna.addWidget(acciones)

        # `QDialogButtonBox` reordena los botones según el escritorio: en GNOME
        # el de confirmar va a un lado y en KDE al otro. Eso es exactamente lo
        # que esta regla no admite, así que no se usa.
        assert not isinstance(acciones, QDialogButtonBox)


class Enlace(QLabel):
    """Un enlace con pinta de enlace: color del acento **y** subrayado.

    Las dos cosas, no una. El color solo no basta —quien no distingue ese tono
    ve texto normal— y el subrayado es la pista que no depende del color.

    Puede no mostrar la dirección: el texto puede ser un nombre. Lo que no puede
    es no parecer tocable. Un texto que hace algo al pulsarlo sin anunciarlo no
    es discreto, es indescifrable.
    """

    def __init__(
        self,
        texto: str,
        url: str,
        *,
        parent: QWidget | None = None,
    ) -> None:
        # Qt pinta los enlaces con `QPalette.Link`, que el tema ya puso en el
        # metal, así que el color sale solo del acabado elegido.
        super().__init__(f'<a href="{url}" style="text-decoration: underline;">{texto}</a>', parent)
        self.setTextFormat(Qt.TextFormat.RichText)
        self.setOpenExternalLinks(True)
        self.setTextInteractionFlags(Qt.TextInteractionFlag.TextBrowserInteraction)
        self.setCursor(Qt.CursorShape.PointingHandCursor)
        self.setMinimumHeight(OBJETIVO_TACTIL_MIN)


class Atenuado(QLabel):
    """Texto de apoyo: rótulos y explicaciones que no compiten con el dato."""

    def __init__(self, texto: str, parent: QWidget | None = None) -> None:
        super().__init__(texto, parent)
        self.setProperty("class", ATENUADO)
        self.setWordWrap(True)


def quitar_linea_de_pestanas(pestanas: QWidget) -> QWidget:
    """Apaga la línea que Qt pinta bajo la barra de pestañas.

    **Es un color del sistema colándose en una paleta cerrada**, que es
    exactamente el problema que esta librería existe para resolver. La barra pinta
    bajo las pestañas una línea con el realce del escritorio —visto en una app
    real: un `#154759` que no sale de ningún metal de Wulfenite— y **ninguna regla
    de hoja de estilo la alcanza**: `drawBase` es una propiedad del `QTabBar`, no
    algo que el QSS pueda tocar.

    **El valor por defecto depende de cómo se arme la barra, y por eso conviene
    forzarlo siempre.** Medido en PySide6 6.11.1 con estilo Fusion:

    | | `drawBase` de fábrica |
    |---|---|
    | `QTabBar()` suelto | **`True`** — pinta la línea |
    | `QTabWidget().tabBar()` | `False` |

    O sea que el mismo código se ve distinto según por dónde se creó la barra, y
    puede cambiar entre versiones de Qt. Llamar a esto siempre cuesta una línea y
    quita la duda.

    Se comprobó una por una, y todas fallan salvo la última:

    | intento | resultado |
    |---|---|
    | sin nada | línea azul |
    | `pane` con borde 0 | línea azul |
    | `QTabBar` con borde 0 | línea azul |
    | `tab-bar` sin borde | línea azul |
    | `QTabBar` con fondo | línea azul |
    | **`setDrawBase(False)`** | **limpia** |

    No da error: solo se ve mal, y hay que saber que `setDrawBase` existe. Por eso
    está acá y no en una nota — una regla escrita en una guía es una regla que
    alguien se salta.

    ```python
    self.pestanas = quitar_linea_de_pestanas(QTabWidget())
    ```

    **Lo que NO hace:** decidir cómo se ven las pestañas. Tipografía, subrayado
    del activo y estados de foco siguen siendo del acabado por defecto de Qt. Es
    deliberado: una pieza `Pestañas` completa obliga a diseñar todo eso, y media
    pieza sería la única del sistema que no define su acabado — peor que no
    tenerla. Esto arregla el daño real y deja la puerta abierta.

    Devuelve el mismo widget, para poder encadenarlo.
    """
    # Acepta las dos formas —un `QTabWidget` o un `QTabBar` suelto— porque el
    # valor de fábrica difiere justo entre ellas, y quien llama no tendría por qué
    # saber cuál de las dos tiene en la mano.
    barra = pestanas.tabBar() if hasattr(pestanas, "tabBar") else pestanas
    if hasattr(barra, "setDrawBase"):
        barra.setDrawBase(False)
    return pestanas


class Falla(QWidget):
    """Qué se muestra cuando algo no se pudo cargar.

    Nace de un caso real: una pantalla de reportes falló en un aparato y lo que
    apareció fue una excepción de base de datos con la consulta entera, a pantalla
    llena, delante de quien atendía el mostrador. El error estaba capturado — pero
    **capturar no es lo mismo que contar**.

    Dos audiencias, y por eso dos niveles:

    - quien usa la app lee una frase en su idioma y un botón para reintentar;
    - quien la desarrolla ve el detalle técnico, pero **solo si se le pide**,
      porque en el mostrador no le sirve a nadie y encima puede filtrar nombres de
      tablas.

    Lo que **no** hace es tragarse el error: el fallo se sigue viendo, y sigue
    siendo evidente que algo está mal. Un cero silencioso dejaría al negocio
    creyendo que no vendió nada.

    ```python
    Falla("No se pudieron cargar las ventas", al_reintentar=self.recargar)
    ```

    `mostrar_detalle` viene en falso a propósito: la librería no puede saber si
    esto corre en depuración. La app lo decide.
    """

    def __init__(
        self,
        mensaje: str,
        *,
        error: object | None = None,
        al_reintentar=None,
        mostrar_detalle: bool = False,
        texto_reintentar: str = "Reintentar",
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(parent)
        self.setProperty("class", POZO)
        self.setAttribute(Qt.WidgetAttribute.WA_StyledBackground, True)

        caja = QVBoxLayout(self)
        caja.setContentsMargins(PADDING_CELDA, PADDING_CELDA, PADDING_CELDA, PADDING_CELDA)
        caja.setSpacing(UNIDAD)

        self.mensaje = QLabel(mensaje, self)
        self.mensaje.setWordWrap(True)
        caja.addWidget(self.mensaje)

        # Nulo esconde el botón: sin nada que reintentar, ofrecerlo es prometer
        # algo que no pasa.
        self.boton = None
        if al_reintentar is not None:
            self.boton = boton_primario(texto_reintentar, self)
            self.boton.clicked.connect(al_reintentar)
            fila = QHBoxLayout()
            fila.addWidget(self.boton)
            fila.addStretch(1)
            caja.addLayout(fila)

        # Plegado: el detalle es para depurar, no para mirar de frente.
        self.detalle = None
        if mostrar_detalle and error is not None:
            self._abrir = boton_plano("Detalle técnico", self)
            self.detalle = QLabel(str(error), self)
            self.detalle.setWordWrap(True)
            self.detalle.setTextInteractionFlags(
                Qt.TextInteractionFlag.TextSelectableByMouse
            )
            self.detalle.hide()
            self._abrir.clicked.connect(self._alternar_detalle)
            caja.addWidget(self._abrir)
            caja.addWidget(self.detalle)

    def _alternar_detalle(self) -> None:
        # `isHidden` y no `isVisible`: mientras la ventana no se haya mostrado,
        # `isVisible` es falso aunque el detalle esté desplegado, y el alternador
        # se quedaba encendido para siempre. `isHidden` es el estado explícito y
        # no depende de que el padre esté en pantalla.
        visible = self.detalle.isHidden()
        self.detalle.setVisible(visible)
        self._abrir.setText("Ocultar detalle técnico" if visible else "Detalle técnico")


class _EstiloDePestanasDerechas(QProxyStyle):
    """Dibuja el texto de las pestañas en horizontal aunque estén de lado.

    Qt rota el texto 90° cuando las pestañas van a la izquierda o a la derecha, y
    no hay forma de pedirle que no lo haga desde la hoja de estilo. Se arregla en
    dos sitios y hacen falta los dos:

    - **el tamaño**, o la pestaña queda alta y estrecha —con la forma del texto
      rotado— y el texto horizontal no cabe;
    - **el dibujo de la etiqueta**, diciéndole a Qt que la pinte como si la
      pestaña estuviera arriba.

    Es privado: se usa a través de [enderezar_texto_de_pestanas].
    """

    def sizeFromContents(self, tipo, opcion, tamano, widget):  # noqa: N802 (API de Qt)
        medido = super().sizeFromContents(tipo, opcion, tamano, widget)
        # Solo si viene más alto que ancho: es la firma de una pestaña de lado, y
        # así una pestaña normal no se toca.
        if tipo == QStyle.ContentsType.CT_TabBarTab and medido.width() < medido.height():
            medido.transpose()
        return medido

    def drawControl(self, elemento, opcion, pintor, widget):  # noqa: N802 (API de Qt)
        if elemento == QStyle.ControlElement.CE_TabBarTabLabel:
            derecha = QStyleOptionTab(opcion)
            # Mentirle a Qt sobre la posición SOLO para la etiqueta: el resto de
            # la pestaña se sigue dibujando en su sitio, con su borde y su marca.
            derecha.shape = QTabBar.Shape.RoundedNorth
            super().drawControl(elemento, derecha, pintor, widget)
            return
        super().drawControl(elemento, opcion, pintor, widget)


def enderezar_texto_de_pestanas(pestanas: QWidget) -> bool:
    """Intenta dejar el texto horizontal en unas pestañas verticales.

    Qt las rota 90°, y leer de lado es incómodo. Esto transpone el tamaño de la
    pestaña y le dice a Qt que pinte la etiqueta como si estuviera arriba.

    **Devuelve si lo consiguió, y muchas veces será `False`.** No es un capricho
    de la API: hay una limitación de Qt que no se puede sortear desde aquí.

    ## La limitación, medida

    Un `QProxyStyle` es la única forma de enderezar ese texto. Pero **en cuanto una
    hoja de estilo toca `QTabBar::tab`, Qt sustituye el estilo del widget por el
    suyo propio y descarta el proxy.** Comprobado:

    | | tamaño de la pestaña | estilo del widget |
    |---|---|---|
    | sin hoja de estilo | **75x29** — texto horizontal | el proxy |
    | con la hoja de Wulfenite | 51x67 — texto rotado | `QCommonStyle` |

    Y el orden da igual: aplicarlo antes o después de mostrar la ventana lleva al
    mismo sitio.

    Como Wulfenite **sí** estiliza las pestañas —color del texto, marca del
    activo, aire— el proxy se pierde en cuanto se aplica el tema. Por eso esto
    avisa en vez de fallar en silencio: quien lo llame y vea `False` sabe que el
    texto va a salir rotado y puede decidir.

    ## Qué hacer entonces

    - **Pestañas arriba.** Es lo que hace Wulfenite por defecto y no tiene este
      problema.
    - **Un rail de navegación**: una columna de botones en vez de pestañas
      verticales. Se arma con `boton_plano` y el pozo, se lee de frente y entra en
      el recorrido del teclado como cualquier otro control.
    - **Renunciar al estilo de las pestañas** para esa ventana, si de verdad hace
      falta que sean verticales y legibles. Se pierde el acabado del sistema en
      esa barra, que es peor negocio casi siempre.

    ```python
    if not enderezar_texto_de_pestanas(tabs):
        ...  # el texto va a salir rotado
    ```
    """
    barra = pestanas.tabBar() if hasattr(pestanas, "tabBar") else pestanas
    # Tiene que ser una barra de pestañas de verdad. `setStyle` no sirve de
    # guarda: lo tienen TODOS los widgets, así que colocarle este estilo a una
    # etiqueta habría devuelto `True` tan contento.
    if not isinstance(barra, QTabBar):
        return False

    estilo = _EstiloDePestanasDerechas()
    # El estilo se cuelga del widget para que viva lo mismo que él: sin la
    # referencia, Python lo recoge y Qt se queda apuntando a nada.
    barra._wulf_estilo = estilo
    barra.setStyle(estilo)

    # Se comprueba que haya quedado puesto. Si una hoja de estilo toca las
    # pestañas, Qt lo reemplaza por el suyo y esto devuelve `False` — que es
    # exactamente lo que hay que saber.
    return isinstance(barra.style(), _EstiloDePestanasDerechas)


class Rail(QWidget):
    """La columna de navegación: los destinos de la app, en vertical.

    **Es la respuesta del sistema a la navegación lateral**, y existe en vez de
    unas pestañas verticales por un motivo concreto: Qt **rota el texto** en unas
    pestañas de lado, y con el tema puesto eso **no tiene arreglo** — descarta el
    estilo delegado en cuanto una hoja de estilo toca `QTabBar::tab`. Acá el texto
    va horizontal siempre, sin depender de ningún estilo delegado.

    Reglas de la pieza:

    - **Vive en la bandeja, no en un pozo.** Es cromo, no contenido. El pozo es lo
      que hay al lado.
    - **La marca del activo va en el borde que toca al contenido** — el derecho —,
      con el metal y el grosor de selección. Misma regla que en las pestañas: la
      marca señala hacia lo que se está mirando.
    - **Cada destino entra en el recorrido del teclado** y muestra el foco como
      todo lo demás.
    - **48 de alto mínimo.**

    ```python
    rail = Rail(["Venta", "Catálogo", "Reportes"], al_elegir=self.ir_a)
    rail.seleccionar(0)
    ```
    """

    def __init__(
        self,
        destinos: list[str],
        *,
        al_elegir=None,
        ancho: int = 88,
        parent: QWidget | None = None,
    ) -> None:
        super().__init__(parent)
        self.setProperty("class", "wulf-rail")
        self.setAttribute(Qt.WidgetAttribute.WA_StyledBackground, True)
        self.setFixedWidth(ancho)

        self._al_elegir = al_elegir
        self._botones: list[QPushButton] = []
        self._activo = -1

        caja = QVBoxLayout(self)
        caja.setContentsMargins(0, UNIDAD, 0, UNIDAD)
        caja.setSpacing(UNIDAD // 2)

        for indice, etiqueta in enumerate(destinos):
            b = QPushButton(etiqueta, self)
            b.setProperty("class", "wulf-rail-destino")
            b.setMinimumHeight(OBJETIVO_TACTIL_MIN)
            b.setCheckable(True)
            # `StrongFocus` y no el de un botón normal: un rail se recorre con las
            # flechas de un mando, donde no hay tabulador.
            b.setFocusPolicy(Qt.FocusPolicy.StrongFocus)
            b.clicked.connect(lambda _=False, i=indice: self.seleccionar(i))
            caja.addWidget(b)
            self._botones.append(b)

        caja.addStretch(1)
        if destinos:
            self.seleccionar(0, avisar=False)

    def seleccionar(self, indice: int, *, avisar: bool = True) -> None:
        """Marca un destino como activo. Fuera de rango no hace nada."""
        if not 0 <= indice < len(self._botones):
            return
        self._activo = indice
        for i, b in enumerate(self._botones):
            b.setChecked(i == indice)
            # Se repinta a mano: cambiar una propiedad no invalida el estilo por
            # su cuenta, y el destino se quedaba con la marca del anterior.
            b.style().unpolish(b)
            b.style().polish(b)
        if avisar and self._al_elegir is not None:
            self._al_elegir(indice)

    @property
    def activo(self) -> int:
        """El índice del destino marcado."""
        return self._activo


def qss_del_rail(bandeja: str, texto: str, atenuado: str, metal: str, enfocado: str) -> str:
    """El QSS del rail. Lo concatena `hoja_de_estilo`."""
    return f"""
QWidget[class="wulf-rail"] {{ background-color: {bandeja}; }}

/* Sin caja y sin fondo propio: un destino no es un pozo, es cromo sobre la
   bandeja. El pozo es lo que hay al lado. */
QPushButton[class="wulf-rail-destino"] {{
    background: transparent;
    color: {atenuado};
    border: none;
    padding: {UNIDAD}px;
    text-align: center;
}}
/* La marca en el borde DERECHO, que es el que toca al contenido. */
QPushButton[class="wulf-rail-destino"]:checked {{
    color: {texto};
    border-right: {Canto.SELECCIONADO}px solid {metal};
}}
QPushButton[class="wulf-rail-destino"]:focus {{
    color: {texto};
    background-color: {enfocado};
    border-right: {Canto.ENFOCADO}px solid {metal};
}}
"""
