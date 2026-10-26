# Pique — guía de pesca offline (KMP)

## Qué es

App Android de contenido didáctico de pesca: leer el agua (pozos, resacas), modalidades (surfcasting, peña, spinning, boya, fondo, trolling, jigging, kayak), nudos animados, aparejos, carnadas, especies de la costa peruana con tallas mínimas de PRODUCE y seguridad. No busca rentabilidad; el objetivo es una app con sustancia que pase la revisión de Play y se actualice con contenido nuevo.

## Principios

1. **Offline total** — sin red, sin backend, sin APIs. Todo el contenido va empaquetado en la app.
2. **Sin cuenta ni datos personales** — no hay login, analítica ni permisos innecesarios.
3. **Interactiva, no un PDF** — cada sección tiene animación, diagrama o interacción (evita rechazo por funcionalidad mínima).
4. **Animaciones dibujadas con código** en `Canvas` de Compose. Nada de imágenes o videos de terceros.
5. **Sin comunidad por ahora** — nada de publicar capturas ni perfiles.

## Arquitectura

- `shared` (KMP, solo target Android por ahora): pantallas Compose, tema, contenido y lógica.
  - `ui/theme/` — `Color.kt`, `Type.kt`, `Theme.kt`. Reflejan `docs/design-system.md`; cambiar ambos juntos.
  - Recursos en `composeResources/` (strings, fuentes). Paquete `com.pedrorau.pique.resources`.
  - Contenido en `composeResources/files/` como JSON (textos en español dentro del JSON). Nudos: `files/knots/<id>.json` y su lista en `files/knots/index.json`; el formato está documentado en `knots/Knot.kt`.
- `androidApp`: solo `MainActivity`, manifest, tema de ventana y launcher.

## Diseño

- Fuente de verdad: `docs/design-system.md`. Versión visual: artefacto "Pique Design System".
- Usar `MaterialTheme` para lo que Material 3 cubre y `PiqueTheme.colors` / `PiqueTheme.typography` para ilustración, estados y el estilo `data`.
- `accent` (naranja boya) una sola vez por pantalla. Colores de mar/arena/espuma solo en ilustraciones.
- Textos de UI en español (tuteo, vocabulario de orilla peruana), siempre en `strings.xml`. El contenido (nudos, especies) va en los JSON.

## Convenciones

- Versiones alineadas con CallFilter (AGP 9, Kotlin 2.4, Compose Multiplatform 1.12, minSdk 29).
- Trabajo en `main` hasta tener la arquitectura base; luego una rama por feature.
- El usuario hace los commits y los push.
