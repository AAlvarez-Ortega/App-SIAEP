# App-SISAEP Android

Versión 1.2: permite corregir un preregistro pendiente y reenviar uno rechazado. El formulario recupera los datos desde Supabase con el comprobante privado guardado en el dispositivo. Una solicitud aceptada se conserva sin permitir cambios desde este flujo.

La aplicación utiliza App-SISAEP (`wdgsvdjojwjebjrpgopn`). Requiere la migración 008 de correcciones disponible en el repositorio del panel administrativo, junto con las migraciones anteriores. Endpoints nuevos: `sisaep_solicitud_propia` y `sisaep_corregir_solicitud`. El comprobante nunca se publica ni se incorpora al APK.

Los duplicados de solicitudes activas continúan bloqueados. Los rechazos permiten otro intento; las correcciones conservan el historial y vuelven a revisión. Una revisión simultánea exige recargar los datos antes de guardar.

Compilación: JDK de Android Studio, Android SDK 36 y `gradlew assembleDebug testDebugUnitTest`. Android 10 o posterior. El APK de pruebas conserva el identificador de paquete y certificado de desarrollo para actualizar la instalación existente.
