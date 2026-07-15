# Pata Cargo - Logística Colaborativa (VIRCH & Puerto Madryn)

**Pata Cargo** nace para solucionar la desconexión logística en la región del Valle Inferior del Río Chubut y Puerto Madryn, aprovechando el movimiento natural de las personas para transportar paquetes de forma segura, económica y confiable.

---

## 🚩 El Problema
En regiones con distancias considerables como el VIRCH, el envío de paquetes pequeños a través de servicios tradicionales suele ser:
*   **Costoso:** Tarifas mínimas elevadas para envíos de corta distancia.
*   **Lento:** Dependencia de horarios rígidos y centros de distribución.
*   **Informal:** Muchos envíos se coordinan por redes sociales sin garantías de pago ni seguridad para el paquete.

## 💡 La Solución
Una plataforma móvil que conecta a **Enviadores** con **Portadores** (personas que ya tienen planeado viajar entre ciudades).
*   **Seguridad:** Sistema de garantía (Escrow) donde el dinero se retiene hasta que el paquete es entregado.
*   **Validación:** Uso de códigos QR únicos para confirmar el retiro y la recepción.
*   **Confianza:** Validación de identidad y sistema de reputación para los portadores regionales.

## 🏗️ Arquitectura del Sistema
Para asegurar que la aplicación sea mantenible y escale, se aplicaron principios de **Clean Architecture**:
*   **Capa de UI:** Implementada totalmente en **Jetpack Compose**, permitiendo una interfaz reactiva y moderna.
*   **Capa de Dominio:** Lógica de negocio pura (Casos de uso) que no depende de librerías externas.
*   **Capa de Datos:** Implementación del patrón **Repository** con una estrategia **Offline-First**, utilizando **Room** para caché local y **Firebase Firestore** para sincronización en tiempo real.

## 🛠️ Tecnologías Principales
*   **Lenguaje:** Kotlin + Coroutines & Flow.
*   **Persistencia:** Room (Local) & Firestore (Cloud).
*   **Integraciones:** Mercado Pago SDK (Pagos), CameraX (QR), Firebase Auth.

---

## 📸 Capturas de Pantalla
<div align="center">
  <img src="assets/screenshots/Enviador 1.jpeg" width="23%" alt="Detalle de envío" />
  <img src="assets/screenshots/Enviador 2.jpeg" width="23%" alt="Publicación" />
  <img src="assets/screenshots/Enviador 3.jpeg" width="23%" alt="Gestión de carga" />
  <img src="assets/screenshots/Inicio sesion protegida.jpeg" width="23%" alt="Seguridad" />
</div>

## 🎥 Video Demostración
> [!IMPORTANT]
> Puedes encontrar una demo en video del flujo completo en la carpeta `assets/video` (o [haz clic aquí si está disponible en línea]).

---

## 🧐 Decisiones Técnicas: ¿Por qué construí esto así?

### ¿Por qué Firestore + Room?
La conectividad en las rutas de la Patagonia puede ser inestable. Elegí **Room** para que los usuarios siempre puedan ver sus envíos activos sin señal, y **Firestore** para gestionar el "matching" de cargas en tiempo real cuando recuperan la conexión.

### ¿Por qué Mercado Pago (Escrow)?
Para resolver la desconfianza entre desconocidos, implementé un flujo de **Garantía**. El enviador paga al publicar, el dinero queda a resguardo de la plataforma, y solo se libera al portador cuando el enviador escanea el QR de entrega exitosa.

### ¿Por qué Jetpack Compose?
Buscaba rapidez en el desarrollo de una UI compleja. Compose me permitió crear componentes reutilizables (como el selector de rutas) con mucho menos código que el sistema de Views tradicional, facilitando las animaciones y transiciones entre estados.

---

## 🗺️ Roadmap / Próximos Pasos
- [ ] Implementación de notificaciones Push para actualizaciones de estado.
- [ ] Sistema de tracking en tiempo real mediante GPS.
- [ ] Verificación de identidad mediante biometría facial real.
- [ ] Panel de estadísticas avanzado para administradores.

---

## ✍️ Autor
**Nicolás R. W.** - Desarrollador Android enfocado en crear soluciones tecnológicas con impacto real en la comunidad.

---

### ⚙️ Configuración rápida
1. Clonar el repositorio.
2. Añadir tu `google-services.json` en `/app`.
3. Configurar tus llaves de Mercado Pago en `.env`.
4. Build & Run en Android Studio.
