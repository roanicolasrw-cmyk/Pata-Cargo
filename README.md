# Pata Cargo - Logística Colaborativa Regional

**Pata Cargo** es una solución móvil de logística *last-mile* diseñada para la región del VIRCH y Puerto Madryn. La plataforma optimiza el transporte de paquetes conectando a usuarios con conductores particulares que realizan rutas habituales, integrando un sistema de pagos seguro y validación de identidad.

---

## 💎 Valor Técnico y Arquitectura

Este proyecto demuestra el dominio de estándares de desarrollo Android modernos y la resolución de problemas complejos:

*   **Arquitectura:** Clean Architecture con patrón **MVVM** (Model-View-ViewModel).
*   **Offline-First:** Sincronización robusta entre **Firebase Firestore** y persistencia local con **Room**.
*   **Seguridad Financiera:** Implementación de flujo **Escrow** (Garantía de pago) mediante el SDK de **Mercado Pago**, donde el dinero solo se libera tras la validación mutua.
*   **Interacción Digital:** Sistema de auditoría mediante generación y escaneo de **Códigos QR** (ZXing) para certificar entregas.
*   **UI/UX:** Interfaz 100% declarativa con **Jetpack Compose** y Material Design 3.

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
> [!TIP]
> Puedes ver capturas de pantalla del flujo completo en la carpeta `/assets/screenshots`.

*(Insertar aquí GIF del flujo principal: Publicación -> Pago -> Entrega)*

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
