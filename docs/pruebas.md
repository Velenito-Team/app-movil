# Verificación del 26 de noviembre de 2026

APK: `dist/velenito-comprobantes-debug.apk`, versión 1.0.0, Android 8.0 o superior.

## Resultados ejecutados

| Verificación | Resultado |
| --- | --- |
| Pruebas Android con Robolectric | 12 aprobadas, 0 fallidas |
| Pruebas del contrato público del backend | 3 aprobadas, 0 fallidas |
| Android Lint debug | 0 errores, 7 advertencias |
| Compilación `assembleDebug` | Correcta |
| Firma de APK, `apksigner verify --verbose` | Válida, esquema v2 |
| Paquete y actividad principal, `aapt dump badging` | `co.edu.udistrital.velenito.mobile`, `ui.MainActivity` |

SHA-256 de la APK entregada:

```text
91e034935dcad5adbc8bea61fe4321ddc201b43159b8f2ee11ce14276f0f002a
```

## Correspondencia con los escenarios BDD

- **Acceso:** `AcceptanceTest` comprueba productos, cantidades fraccionarias, total y forma de pago en las vistas Android. `ReceiptClientTest` comprueba una petición GET real a un servidor HTTP de pruebas, sin cabeceras de autorización ni cookies.
- **Privacidad:** la respuesta de prueba incluye deliberadamente nombre, documento, teléfono y costos de compra. Se verifica que esos valores no aparezcan en el comprobante fiado. `PublicReceiptTest` comprueba la proyección pública del backend.
- **Anulación:** `MainActivityTest` abre un enlace compartido mediante un Intent Android, consulta por HTTP y espera que la actividad muestre «Anulada» con sus productos. Luego abre un token inválido y comprueba que desaparezca el detalle anterior y aparezca el error genérico.
- Se verifican enlaces web existentes, enlace nativo, URL de API, servidor alternativo para QR con localhost y rechazo de enlaces con credenciales, tokens malformados o contenido ejecutable.
- Las respuestas HTTP 400, 401, 404, 410, 302 y 500 producen el mismo mensaje público. La app no sigue redirecciones.

Las pruebas usan comprobantes ficticios y un servidor HTTP de prueba local. No crean ni modifican ventas en la base de datos de la tienda.

## Alcance y pendientes

La comprobación del entorno local encontró el backend en 8080 sin responder y el frontend en 8081 devolviendo 502. No se inició ni se detuvo el backend. Las pruebas del backend se compilaron en una copia aislada para no reemplazar los archivos de ejecución existentes en `Backend/target`.

La cámara, el permiso de cámara y la conectividad desde un teléfono físico aún deben probarse con la APK instalada. No se ejecutó un emulador ni se instaló la APK en un teléfono durante esta verificación.

Las advertencias de Lint corresponden a configuración futura de App Links, atributos del filtro de enlaces, una dependencia con versión más reciente, reglas de transferencia de datos de Android 12 y textos en español aún sin externalizar a recursos. No se ocultaron advertencias ni se desactivaron verificaciones para compilar.

## Repetir

```powershell
.\build.ps1
```

Informes completos: `app/build/reports/tests/testDebugUnitTest/index.html` y `app/build/reports/lint-results-debug.html`.

Para los tres tests del backend: `PublicReceiptTest.java` está en el proyecto hermano `Backend/src/test/java/co/edu/udistrital/backend/service`.
