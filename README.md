# IU Digital Radio

Aplicación Android de radio digital desarrollada con Kotlin y Jetpack Compose. El MVP permite seleccionar emisoras de radio por streaming, controlar la reproducción, tomar una foto de perfil y recibir retroalimentación háptica al interactuar con el reproductor.

## Funcionalidades

- Interfaz declarativa construida completamente con Jetpack Compose.
- Reproductor de audio basado en Media3 ExoPlayer.
- Catálogo de emisoras mock con selección en tiempo real.
- Controles de reproducción: Play, Pause, Mute y Unmute.
- Persistencia del estado de la emisora, reproducción y silencio durante cambios de configuración mediante `rememberSaveable`.
- Captura de una foto de perfil con `ActivityResultContracts.TakePicturePreview`.
- Solicitud del permiso de cámara en tiempo de ejecución.
- Vibración corta al interactuar con los controles del reproductor y el catálogo.

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- Android Gradle Plugin
- Media3 ExoPlayer 1.8.0
- Android SDK mínimo: 24
- Android SDK objetivo: 37
- Java 11

## Estructura principal

```text
IUDigitalRadio/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/example/iudigitalradio/
│           ├── MainActivity.kt
│           └── ui/theme/
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Requisitos

- Android Studio actualizado.
- JDK compatible con Java 11.
- Android SDK instalado con la plataforma configurada en el proyecto.
- Conexión a Internet para descargar dependencias y reproducir los streams.

## Ejecutar desde Android Studio

1. Abrir la carpeta del proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Crear o iniciar un emulador Android, o conectar un dispositivo Android físico.
4. Seleccionar la configuración `app`.
5. Pulsar el botón Run.

En un dispositivo físico se debe activar previamente la depuración USB desde las opciones de desarrollador.

## Comandos de consola

Desde la raíz del proyecto:

```powershell
.\gradlew.bat --refresh-dependencies
.\gradlew.bat clean :app:assembleDebug
```

El APK de pruebas se genera en:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Para instalarlo en un dispositivo Android conectado:

```powershell
adb devices
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

## Permisos

El manifiesto declara los siguientes permisos:

- `CAMERA`: necesario para tomar la foto de perfil.
- `INTERNET`: necesario para reproducir las emisoras.
- `VIBRATE`: necesario para la retroalimentación háptica.

El permiso de cámara se solicita únicamente cuando el usuario pulsa el botón de perfil.

## Emisoras de prueba

El catálogo actual utiliza streams públicos configurados en `MainActivity.kt`:

- NPR News
- Radio Paradise
- KEXP

La disponibilidad de cada stream depende del proveedor y de la conexión de red. Si una emisora deja de estar disponible, se puede sustituir su `streamUrl` en el modelo `RadioStation`.

## Limitaciones del MVP

- Las emisoras están definidas como datos locales de prueba; todavía no existe un backend.
- La foto de perfil se mantiene en memoria durante la sesión y no se almacena permanentemente.
- La reproducción está implementada para la actividad principal y no incluye todavía un servicio de reproducción en segundo plano ni controles de notificación.
- El APK debug está destinado a pruebas y no a distribución pública.

