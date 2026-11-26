# El Veleñito — comprobantes Android

App nativa Android (Java), Android 8.0 o superior. Escanea QR, recibe enlaces compartidos y consulta comprobantes sin cuenta ni conexión directa a MongoDB.

## Instalar y usar

1. Instala `dist/velenito-comprobantes-debug.apk` en el celular. Es una APK de pruebas firmada con la clave de desarrollo, no una publicación de Play Store.
2. Pulsa **Escanear QR** y autoriza la cámara, o pega el enlace en **Enlace del comprobante** y pulsa **Abrir comprobante**.
3. También puedes compartir un enlace desde el navegador hacia **El Veleñito**.
4. La pantalla muestra productos, cantidades, subtotales, total, fecha, forma de pago y estado. Una venta anulada muestra **Anulada**.

Los QR actuales de la web tienen el formato `https://servidor/#comprobante/<token>`. También se admiten la ruta pública de la API y `velenito://comprobante/<token>`; este último requiere configurar previamente el servidor.

## Conectar con tu proyecto

Por defecto, el servidor se toma del enlace. Si tu QR contiene `localhost` o `127.0.0.1`, abre **Configurar servidor** e indica la dirección accesible desde el teléfono:

```text
http://IP-DEL-COMPUTADOR:8081
```

El frontend en 8081 debe reenviar `/api` al backend. También puedes indicar directamente `http://IP-DEL-COMPUTADOR:8080`. Ambos dispositivos necesitan conectividad entre sí; no uses la dirección de MongoDB en esta app. Para el emulador Android, el equipo anfitrión suele ser `http://10.0.2.2:8080`.

La configuración es solo el origen (sin rutas, credenciales ni token). La APK debug admite HTTP para las pruebas de red local; la variante release exige HTTPS. Los enlaces vencidos, inválidos o no disponibles muestran el mismo mensaje: **Comprobante no disponible.**

Para abrir automáticamente enlaces web desde un dominio propio, compila con `-PreceiptHost=tu-dominio` y publica la asociación Android App Links del dominio. El valor de desarrollo es `localhost`. Sin asociación verificada, Android puede abrir el navegador; compartir el enlace o pegarlo en la app sigue disponible.

## Compilar y probar

Requisitos: JDK 17 o 21 y Android SDK con plataforma 35 y Build Tools 35.0.0. Configura `sdk.dir` en `local.properties` o la variable `ANDROID_HOME`.

```powershell
.\build.ps1
```

El script ejecuta pruebas, análisis estático y compilación, y copia la APK a `dist/velenito-comprobantes-debug.apk`. Usa una caché Gradle dentro de `.tools` para evitar problemas de Windows con perfiles de usuario que contienen tildes. También puedes usar `./gradlew.bat testDebugUnitTest lintDebug assembleDebug` en un entorno sin ese problema.

Resultado original: `app/build/outputs/apk/debug/app-debug.apk`. Para generar la variante release sin firma de distribución: `./gradlew.bat assembleRelease`. La clave de publicación debe administrarse fuera del repositorio.

## Estructura

- `domain/`: comprobante público y validación de enlaces.
- `data/`: consulta HTTP GET y lectura del contrato JSON del backend.
- `ui/`: escáner, manejo de enlaces y vistas nativas.
- `app/src/test/`: pruebas de enlaces, HTTP y escenarios de aceptación en vistas Android con Robolectric.
- `docs/aceptacion.feature`: los tres escenarios BDD solicitados.

## Privacidad

La app solo consulta `GET /api/invoices/receipt/{token}`. No envía autorización, no sigue redirecciones y no registra ni guarda comprobantes o tokens. Conserva únicamente el servidor configurado. El modelo y la pantalla usan una lista explícita de campos públicos: ignoran nombre/documento/teléfono del cliente, costos de compra y cualquier campo adicional que devuelva el servidor. El backend debe mantener esta misma protección; ocultar datos en el móvil por sí solo no protege una API.

Los tokens de los enlaces dan acceso al comprobante: compártelos únicamente con quien deba consultarlo. La aplicación no renueva enlaces ni modifica ventas, inventario o fiados.

## Alcance de las pruebas

Las pruebas automatizadas verifican respuestas HTTP, consulta sin autenticación, representación de importes/cantidades, privacidad del fiado, estado de anulación y errores genéricos. La prueba física de cámara, permisos y alcance de la red del celular se realiza con la APK instalada; no se sustituye por los tests de escritorio.
