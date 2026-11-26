# SUNMI Broadcast Tester

Aplicación Android nativa Java para probar la recepción de códigos de barras SUNMI por Broadcast.

## Compatibilidad
- Android mínimo: 7.1 / API 25.
- targetSdkVersion: 28.
- Sin librerías externas ni permisos especiales.

## Intent escuchado
- Action: `com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED`
- Extra texto: `data`
- Extra bytes opcional: `source_byte`

## Compilar APK
1. Abrir esta carpeta desde Android Studio.
2. Instalar Android SDK Platform 28 y Build Tools compatibles cuando Android Studio lo solicite.
3. Ejecutar **Build > Build APK(s)**.
4. El APK de depuración aparecerá en `app/build/outputs/apk/debug/app-debug.apk`.

También se puede compilar desde terminal con Gradle 4.6 y Android Gradle Plugin 3.2.1:
`gradle assembleDebug`

## Instalar
Copiar `app-debug.apk` al SUNMI T2 Mini y abrirlo. Permitir instalación desde esa fuente si Android lo solicita.

## Prueba
1. Abrir la app y dejarla en primer plano.
2. En la configuración del lector SUNMI, elegir salida Broadcast/Intent y desactivar Keyboard Output para una prueba exclusiva.
3. Escanear un código. Debe aparecer en “Último código recibido” y en el historial.
4. “Enviar Broadcast de prueba” comprueba el receptor de la propia app, no el hardware.

## Nota
La aplicación escucha Broadcast mientras está en primer plano. La configuración exacta del lector integrado depende del firmware y del servicio de escáner instalado en el T2 Mini. No envía comandos de configuración al hardware para evitar modificar parámetros desconocidos del terminal.

## Compilación en la nube (GitHub Actions)
Este proyecto incluye el flujo `.github/workflows/android-apk.yml`.

1. Crea una cuenta gratuita en https://github.com/ si todavía no tienes una.
2. Crea un repositorio nuevo, por ejemplo `SUNMI-Broadcast-Tester`.
3. Descomprime este ZIP en tu computadora y carga **el contenido** de la carpeta `SUNMI-Broadcast-Tester` al repositorio (no subas el ZIP como único archivo). Puedes usar **Add file > Upload files** en la web de GitHub.
4. Abre la pestaña **Actions** del repositorio y habilita los workflows si GitHub lo solicita.
5. El flujo se ejecuta automáticamente al subir cambios. También puedes entrar en **Actions > Build SUNMI Broadcast Tester APK > Run workflow**.
6. Cuando termine con estado verde, abre esa ejecución, baja a **Artifacts** y descarga `SUNMI-Broadcast-Tester-debug-apk`.
7. Descomprime el artefacto: dentro estará `app-debug.apk`. Copia ese APK al SUNMI T2 Mini e instálalo.

La compilación en GitHub Actions requiere que autorices la ejecución del workflow. El artefacto se conserva durante 30 días. Esta automatización compila el APK; no prueba físicamente el escáner.
