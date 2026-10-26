# Sistema de diseño de Pique

> Fuente de verdad para el código. La versión visual, con portada y vistas previas, está en el artefacto [Pique Design System](https://claude.ai/artifact/DgAXiQ6XsRqjqdTo9pnRmE). Si cambias un valor aquí, cámbialo también en el tema de Compose y en el artefacto.

Pique es una guía de pesca para Android que funciona sin conexión. Enseña a leer el agua, hacer nudos, armar aparejos, encarnar y reconocer las especies de la costa peruana. Habla como un pescador con experiencia que te acompaña en la orilla: claro, directo y sin relleno.

Este sistema es la primera versión. Define la paleta, la tipografía, los espaciados y los radios, y cómo se traducen a Material 3 en Jetpack Compose. Todavía no incluye componentes; se agregarán cuando existan las primeras pantallas.

## Principios

1. **Se usa en la playa.** Hay sol directo, manos mojadas y poco tiempo. El texto es grande, el contraste alto y los objetivos táctiles miden al menos 48 dp.
2. **Mostrar antes que explicar.** Un nudo se aprende viéndolo atarse, no leyendo un párrafo. Cada ficha empieza con su animación o diagrama.
3. **Un dato, un porqué.** Cada consejo trae una línea que explica por qué funciona ("al borde del pozo: la corriente no se lleva la carnada").
4. **Funciona sin señal.** Todo el contenido viene dentro de la app. Nada espera una conexión.

## Voz y tono

- Tutea al lector: "lanza", "amarra", "revisa la talla".
- Usa los nombres que se usan en la orilla peruana: *muy muy*, *peña*, *pique*, *llenante*, *pejerrey*. Si hay un término técnico, va entre paréntesis la primera vez: "pozo (canaleta de resaca)".
- Frases cortas y verbos de acción. Nada de "es importante destacar que".
- Las advertencias de seguridad son directas y sin dramatismo: "Si una resaca te arrastra, nada paralelo a la orilla."

| En vez de | Escribe |
|---|---|
| Se recomienda realizar el nudo con cuidado | Moja el nudo antes de apretarlo |
| Error: talla insuficiente | Bajo la talla mínima (55 cm). Devuélvelo al agua |
| Contenido no disponible | Esta sección llega en la próxima versión |

## Color

La paleta sale del mar frío de Humboldt y de la boya naranja que marca el pique.

- **surface** es gris niebla, no blanco: baja el resplandor al sol. Las tarjetas van en **surface-raised**.
- **primary** (Humboldt) es el color de la marca y de toda la interacción: botones, pestaña activa, selección.
- **accent** (boya) se usa **una vez por pantalla** para lo que el ojo debe encontrar primero: el punto de lanzamiento, el paso actual de un nudo, el botón Reproducir. Si hay dos cosas naranjas, una sobra.
- **sea-deep**, **sea-shallow**, **sand** y **foam** son solo para ilustraciones y animaciones (el pozo, los perfiles de fondo). No se usan en controles.
- **ok**, **caution** y **danger** comunican estado (talla permitida, oleaje, peligro) y siempre van con ícono o texto, nunca solo color.

Todo el texto cumple 4,5:1 sobre su fondo en ambos temas, y los bordes de controles cumplen 3:1.

## Tipografía

- **Archivo** (800/700) para títulos. Es firme y algo condensada, como la rotulación de una caja de aparejos.
- **Atkinson Hyperlegible** para leer. Se diseñó para máxima legibilidad y distingue bien formas parecidas (I/l/1, 0/O), útil al sol.
- **IBM Plex Mono** solo para medidas y cifras: "55 cm", "plomo 80 g", "anzuelo n.º 2", "paso 3/7".

Las tres están en Google Fonts. En Android conviene incluirlas como recursos en `res/font` para que funcionen sin conexión desde el primer arranque.

## Espaciado y forma

Escala de 4 dp: 4, 8, 12, 16, 24 y 32. El margen lateral de pantalla es **space-4** (16 dp).

Radios: chips y campos **radius-sm** (8), tarjetas y lienzos de animación **radius-md** (16), hojas inferiores **radius-lg** (28), botones en píldora **radius-full**.

## Iconografía

Usar **Material Symbols Rounded** con trazo de 2 dp (peso 400, sin relleno), en **ink** o **primary**. Los extremos redondeados recuerdan al sedal. Los íconos rellenos se reservan para el estado seleccionado de la barra inferior.

## Ilustración y animación

- Las animaciones se dibujan con código en `Canvas` de Compose; no se usan videos ni fotos de terceros.
- **Vista de playa:** desde arriba, mar arriba y orilla abajo. Agua con degradado de sea-shallow a sea-deep según la profundidad, espuma en foam, arena en sand.
- **Nudos:** el sedal en ink sobre surface-raised, y el tramo que se mueve en el paso actual en accent.
- La animación siempre tiene pausa y control paso a paso. Si el sistema pide reducir el movimiento, arranca en pausa.

## Correspondencia con Material 3 (Compose)

| Token | `ColorScheme` |
|---|---|
| surface | background, surface |
| surface-raised | surfaceContainerLow |
| surface-sunken | surfaceContainerHighest |
| ink / ink-muted | onSurface / onSurfaceVariant |
| line / line-strong | outlineVariant / outline |
| primary, on-primary, primary-container, on-primary-container | primary, onPrimary, primaryContainer, onPrimaryContainer |
| accent, on-accent, accent-container, on-accent-container | tertiary, onTertiary, tertiaryContainer, onTertiaryContainer |
| danger / on-danger | error / onError |

| Estilo | `Typography` |
|---|---|
| display | displaySmall |
| headline | headlineMedium |
| title | titleLarge |
| body | bodyLarge |
| body-small | bodyMedium |
| label | labelLarge |
| overline | labelSmall |
| data | estilo propio (`PiqueType.data`) |

Las formas se asignan así: `Shapes(small = 8.dp, medium = 16.dp, extraLarge = 28.dp)`.

## Tokens

### Color

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| `surface` | `#eef3f2` | `#0b1619` | Fondo de pantalla. Gris niebla (la garúa de la costa), nunca blanco puro: reduce el brillo al sol. M3: background / surface. |
| `surface-raised` | `#ffffff` | `#13242a` | Tarjetas, hojas inferiores y diálogos sobre surface. M3: surfaceContainerLow. |
| `surface-sunken` | `#dfe8e7` | `#07100f` | Campos de búsqueda, pistas de progreso y zonas de fondo dentro de una tarjeta. M3: surfaceContainerHighest. |
| `ink` | `#0f2229` | `#e4eeee` | Texto principal e íconos sobre surface y surface-raised. M3: onSurface. |
| `ink-muted` | `#4a6168` | `#96adb2` | Texto secundario: subtítulos, metadatos, pies de foto. M3: onSurfaceVariant. |
| `line` | `#c3d2d1` | `#24404a` | Divisores y bordes de tarjeta de 1 dp. Decorativo: no usar como único indicador de estado. M3: outlineVariant. |
| `line-strong` | `#6f878d` | `#6a868d` | Borde de campos de texto, chips no seleccionados y anillos de foco en reposo (3:1 sobre surface). M3: outline. |
| `primary` | `#0b5563` | `#5cc0c9` | Humboldt: el azul verdoso del agua fría. Botones principales, pestaña activa, enlaces y estados seleccionados. M3: primary. |
| `on-primary` | `#ffffff` | `#00363d` | Texto e íconos sobre primary. M3: onPrimary. |
| `primary-container` | `#cbe9ec` | `#0f3d46` | Fondo de chips seleccionados, la pestaña activa de la barra inferior y tarjetas destacadas. M3: primaryContainer. |
| `on-primary-container` | `#05333c` | `#cbe9ec` | Texto e íconos sobre primary-container. M3: onPrimaryContainer. |
| `accent` | `#b8431a` | `#ff8a5c` | Boya: el naranja del flotador. Una sola acción o marca por pantalla: el punto de lanzamiento, el paso actual de un nudo, el botón Reproducir. M3: tertiary. |
| `on-accent` | `#ffffff` | `#3a1000` | Texto e íconos sobre accent. M3: onTertiary. |
| `accent-container` | `#ffdccb` | `#5a2410` | Fondo suave para consejos y avisos de técnica. M3: tertiaryContainer. |
| `on-accent-container` | `#4a1400` | `#ffdccb` | Texto sobre accent-container. M3: onTertiaryContainer. |
| `sea-deep` | `#0e3d55` | `#082a3b` | Agua profunda en ilustraciones y animaciones (pozo, perfiles). Solo ilustración, no UI. |
| `sea-shallow` | `#5fbfb3` | `#3f9a90` | Agua poco profunda y barras de arena sumergidas en ilustraciones. Solo ilustración. |
| `sand` | `#d8c49c` | `#b29a6e` | Arena, playa y fondo marino en ilustraciones. También el bloque de la portada. Solo ilustración. |
| `foam` | `#f5faf8` | `#e8f2ef` | Espuma y olas rompiendo en ilustraciones. Solo ilustración. |
| `ok` | `#2b7a4b` | `#7fd6a2` | Talla permitida, condición buena. Acompañar siempre con ícono o texto, nunca solo color. |
| `caution` | `#8a5a00` | `#f0c060` | Precaución: oleaje moderado, marea rápida, especie en riesgo. |
| `danger` | `#b3261e` | `#f2b8b5` | Peligro o prohibido: bajo talla mínima, oleaje anómalo, resaca. M3: error. |
| `on-danger` | `#ffffff` | `#601410` | Texto sobre danger. M3: onError. |

### Tipografía

| Familia | Pila |
|---|---|
| `display` | "Archivo", "Roboto Condensed", system-ui, sans-serif |
| `body` | "Atkinson Hyperlegible", Roboto, system-ui, sans-serif |
| `mono` | "IBM Plex Mono", "Roboto Mono", ui-monospace, monospace |

| Estilo | Familia | Tamaño | Interlineado | Peso | Espaciado | Uso |
|---|---|---|---|---|---|---|
| `display` | display | 40 sp | 44 sp | 800 | -0.01em | Una vez por pantalla, en portadas de sección. M3: displaySmall. |
| `headline` | display | 28 sp | 34 sp | 700 | 0 | Título de una ficha: nudo, especie, modalidad. M3: headlineMedium. |
| `title` | display | 20 sp | 26 sp | 700 | 0 | Títulos de tarjeta y de barra superior. M3: titleLarge. |
| `body` | body | 17 sp | 26 sp | 400 | 0 | Texto de lectura. 17 sp porque se lee al aire libre, con sol y a veces con el brazo extendido. M3: bodyLarge. |
| `body-small` | body | 15 sp | 22 sp | 400 | 0 | Descripciones en tarjetas y listas. M3: bodyMedium. |
| `label` | body | 14 sp | 18 sp | 700 | 0.02em | Botones, chips, pestañas. M3: labelLarge. |
| `overline` | body | 12 sp | 16 sp | 700 | 0.1em | Categoría encima de un título, en mayúsculas. M3: labelSmall. |
| `data` | mono | 15 sp | 20 sp | 500 | 0 | Medidas y cifras: tallas, gramos de plomo, número de anzuelo, pasos (3/7). |

### Espaciado

| Token | Valor | Uso |
|---|---|---|
| `space-1` | 4 dp | Entre ícono y su texto. |
| `space-2` | 8 dp | Entre chips; entre título y subtítulo. |
| `space-3` | 12 dp | Relleno interno de chips y filas compactas. |
| `space-4` | 16 dp | Margen lateral de pantalla y relleno de tarjeta. |
| `space-6` | 24 dp | Entre bloques de una ficha. |
| `space-8` | 32 dp | Entre secciones de una pantalla larga. |

### Radios

| Token | Valor | Uso |
|---|---|---|
| `radius-sm` | 8 dp | Chips, campos de texto, miniaturas. |
| `radius-md` | 16 dp | Tarjetas y lienzos de animación. |
| `radius-lg` | 28 dp | Hojas inferiores y diálogos. |
| `radius-full` | 999 dp | Botones (forma de píldora) y FAB. |

## Pendiente

- Componentes (botones, tarjetas, chips, barra inferior, reproductor de animación) cuando existan las primeras pantallas.
- Tema de Compose (`Color.kt`, `Type.kt`, `Theme.kt`) cuando se cree el proyecto KMP.
- Ícono de la app.
