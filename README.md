# Pata Cargo - Logística Colaborativa Regional

**Pata Cargo** es una solución móvil de logística *last-mile* diseñada para la región del VIRCH y Puerto Madryn. La plataforma optimiza el transporte de paquetes conectando a usuarios con conductores particulares que realizan rutas habituales, integrando un sistema de pagos seguro y validación de identidad.

---

## 🏗️ Decisiones de arquitectura

Este proyecto fue desarrollado aplicando estándares de la industria para garantizar escalabilidad y mantenimiento:

*   **Arquitectura:** Implementación de **Clean Architecture** dividida en capas (Data, Domain, UI) con el patrón **MVVM** (Model-View-ViewModel) para una separación de responsabilidades clara.
*   **Offline-First:** Sincronización robusta entre **Firebase Firestore** y persistencia local con **Room**, permitiendo que la app funcione sin conexión.
*   **Seguridad Financiera:** Flujo de pagos tipo **Escrow** (Garantía) integrado con el SDK de **Mercado Pago**. El dinero se retiene de forma segura y solo se libera cuando ambas partes validan la entrega mediante códigos únicos.
*   **Validación Digital:** Auditoría mediante generación y escaneo de **Códigos QR** (ZXing) para certificar cada etapa del envío.
*   **UI Declarativa:** Interfaz moderna construida íntegramente con **Jetpack Compose** y Material Design 3.

---

## 🧐 Justificación Técnica

### ¿Por qué Firestore + Room?
Se optó por una arquitectura híbrida para garantizar la disponibilidad de los datos:
*   **Firestore** actúa como la fuente de verdad en la nube, gestionando el tiempo real necesario para el *matching* de cargas y transportistas.
*   **Room** funciona como caché local y base de datos persistente, asegurando que el usuario pueda consultar sus envíos activos o su historial incluso en zonas de baja cobertura (común en rutas regionales).

### ¿Por qué Jetpack Compose?
La elección de Compose sobre el sistema de Views tradicional se debe a:
*   **Productividad:** Reducción drástica de código *boilerplate* y errores de estado.
*   **Consistencia:** Facilita la creación de un sistema de diseño coherente (Atomic Design) que se adapta dinámicamente a diferentes tamaños de pantalla.
*   **Rendimiento:** Animaciones más fluidas y una gestión de estado reactiva que se integra perfectamente con Kotlin Coroutines y Flow.

---

## 🛠️ Stack Tecnológico

| Categoría | Tecnologías |
| :--- | :--- |
| **Lenguaje** | Kotlin + Coroutines & Flow |
| **UI** | Jetpack Compose, Navigation Component, Material 3 |
| **Persistencia** | Room Database, DataStore |
| **Backend/Auth** | Firebase (Auth, Firestore, Storage) |
| **Pagos** | Mercado Pago SDK (Checkout Pro) |
| **Hardware** | CameraX (Escaneo QR), Biometría |
| **Dependencias** | Retrofit2, OkHttp3, Moshi, KSP |

---

## 🚀 Funcionalidades Destacadas

### 📦 Módulo de Envío (Sender)
*   **Publicación Inteligente:** Flujo dinámico para descripción de carga y seguros.
*   **Gestión de Garantías:** Integración con pasarela de pagos para retención de fondos segura.
*   **Certificación QR:** Generación de token único para validación de retiro.

### 🚚 Módulo de Transporte (Carrier)
*   **Matching por Corredores:** Algoritmo de filtrado para encontrar cargas en rutas específicas.
*   **Billetera Digital:** Visualización de saldos y gestión de cobros realizados.
*   **Identidad Verificada:** Sistema de selfie-check para validación de portadores (Simulado).

### 🛡️ Panel de Control (Admin)
*   Dashboard para la moderación de usuarios, auditoría de transacciones y resolución de disputas.

---

## 📸 Demostración Visual

<div align="center">
  <img src="assets/screenshots/Enviador 1.jpeg" width="23%" alt="Pantalla de Envío 1" />
  <img src="assets/screenshots/Enviador 2.jpeg" width="23%" alt="Pantalla de Envío 2" />
  <img src="assets/screenshots/Enviador 3.jpeg" width="23%" alt="Pantalla de Envío 3" />
  <img src="assets/screenshots/Inicio sesion protegida.jpeg" width="23%" alt="Pantalla de Login" />
</div>

> [!NOTE]
> Para ver el flujo completo en detalle, puedes explorar la carpeta [assets/screenshots](assets/screenshots).

---

## ⚙️ Configuración del Entorno

1. **Clonar y Sincronizar:** Requiere Android Studio Ladybug+ y Gradle 8.7.
2. **Firebase:** Colocar el archivo `google-services.json` en la carpeta `/app`.
3. **Variables de Entorno:** Renombrar `.env.example` a `.env` y configurar las credenciales de Mercado Pago.
4. **Build:** Ejecutar la tarea `:app:assembleDebug`.

---

## 📄 Estructura del Proyecto

```text
com.patacargo.virchm/
├── api/            # Client y DTOs para servicios externos
├── data/           # Repositorios, Room Entities y DAOs
├── ui/
│   ├── components/ # Atomic Design: componentes reutilizables
│   ├── screens/    # Pantallas principales (Sender, Carrier, Admin)
│   ├── theme/      # Definición de tokens de diseño (Material 3)
│   └── PataCargoViewModel.kt # State Management global
└── MainActivity.kt # Entry point y navegación
```

---

## ✍️ Autor
**Nicolás R. W.** - Desarrollador Android enfocado en soluciones escalables y seguras.
