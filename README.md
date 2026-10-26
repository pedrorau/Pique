# Pique

Guía de pesca para Android que funciona sin conexión: leer el agua, nudos, aparejos, carnadas y especies de la costa peruana.

Proyecto Kotlin Multiplatform con Compose Multiplatform. Android es el único target por ahora.

## Estructura

- `shared/`: UI, contenido y lógica (Compose Multiplatform). Aquí vive casi todo.
- `androidApp/`: punto de entrada Android (`MainActivity`) y recursos del launcher.
- `docs/design-system.md`: sistema de diseño (colores, tipografía, espaciado). Fuente de verdad del tema en `shared/.../ui/theme/`.
- `licenses/`: licencias OFL de las fuentes incluidas.

## Compilar

```
./gradlew :androidApp:assembleDebug
```

Para la build de release firmada hace falta `keystore.properties` en la raíz (no se sube al repo).
