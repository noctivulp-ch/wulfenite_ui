// Kotlin puro: ni Android ni Compose.
//
// Es lo que hace que el sistema no dependa de dónde se vaya a programar. Todo lo
// que se puede decidir con aritmética —los tokens, la paleta, el contraste, el
// reparto de la retícula, los grosores del canto— vive acá y no sabe nada de
// ninguna interfaz. Encima se le pone la piel que toque: Compose en Android, y
// mañana lo que sea, sin recalcular un solo número.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(17) }

dependencies {
    testImplementation(libs.junit)
}
