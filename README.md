# Pata Cargo - Logística Colaborativa (VIRCH & Madryn)

**Pata Cargo** es una plataforma móvil innovadora diseñada para transformar la logística en la región del Valle Inferior del Río Chubut (VIRCH) y Puerto Madryn. La aplicación facilita el transporte de paquetes aprovechando los viajes habituales de los ciudadanos, integrando seguridad, pagos digitales y una experiencia de usuario moderna.

---

## ✍️ Autoría
Este proyecto ha sido desarrollado íntegramente por el usuario de esta cuenta. 
Demuestra habilidades avanzadas en desarrollo Android moderno, integración de servicios financieros y diseño de sistemas de confianza descentralizada.

---

## 🚀 Tecnologías Utilizadas

- **Lenguaje:** [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) - Interfaz declarativa de última generación.
- **Arquitectura:** MVVM (Model-View-ViewModel) siguiendo principios de Clean Architecture.
- **Base de Datos:** [Room](https://developer.android.com/training/data-storage/room) - Persistencia local eficiente.
- **Backend & Auth:** [Firebase](https://firebase.google.com/) (Authentication, Firestore).
- **Pasarela de Pagos:** [Mercado Pago SDK](https://www.mercadopago.com.ar/developers/) - Gestión de pagos y sistema de Escrow (Garantía).
- **Hardware & Multimedia:**
  - [CameraX](https://developer.android.com/training/camerax) para escaneo de códigos en tiempo real.
  - [ZXing](https://github.com/zxing/zxing) para la generación dinámica de códigos QR.
- **Inyección de Dependencias:** Gestión de estado robusta mediante ViewModels y el patrón Repository.
- **Concurrencia:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & Flow para flujos de datos asíncronos.

---

## 🏗️ Arquitectura y Patrones

El proyecto implementa las mejores prácticas recomendadas por Google:
- **UI Layer:** Componibles reactivos que consumen el estado del ViewModel.
- **Domain Layer:** Lógica de negocio desacoplada en repositorios.
- **Data Layer:** Gestión híbrida de datos (Firebase para nube, Room para caché local).
- **Seguridad:** Validación biométrica simulada y encriptación de tokens de sesión.

---

## 🛠️ Funcionalidades Principales

### 📦 Para el Enviador (Sender)
- **Publicación de Cargas:** Sistema intuitivo para describir paquetes y rutas.
- **Calculador de Costos:** Estimación automática basada en tamaño, distancia y seguros.
- **Garantía Escrow:** El pago solo se libera al transportista una vez confirmada la entrega.
- **Validación por QR:** Generación de códigos únicos para certificar el retiro del paquete.

### 🚚 Para el Portador (Carrier)
- **Buscador por Corredores:** Filtros inteligentes para encontrar cargas en rutas frecuentes.
- **Rutas Habituales:** Configuración de viajes diarios para emparejamiento automático.
- **Validación Biométrica:** Sistema de selfie para verificar la identidad del portador.
- **Billetera Digital:** Integración con Mercado Pago para cobros seguros y rápidos.

### 🛡️ Panel de Administración (Admin)
- **Gestión de Identidades:** Aprobación manual de portadores verificados.
- **Auditoría de Fondos:** Monitoreo de transacciones y comisiones de plataforma.
- **Resolución de Conflictos:** Canal para gestionar disputas entre usuarios.

---

## 📸 Capturas de Pantalla
> Las capturas de pantalla se encuentran en la carpeta `/assets/screenshots`.

*(Próximamente: Galería de imágenes)*

---

## 🎥 Video Demostración
Se puede encontrar un video con el flujo completo de la aplicación en la carpeta `/assets/video`.

---

## ⚙️ Cómo ejecutar el proyecto

1. **Clonar el repositorio:**
   ```bash
   git clone <url-del-repositorio>
   ```
2. **Prerrequisitos:**
   - Android Studio Ladybug o superior.
   - Archivo `google-services.json` configurado en la carpeta `/app`.
3. **Configuración:**
   - Sincronizar el proyecto con Gradle.
   - Ejecutar en un dispositivo con API 24 o superior.

---

## 📄 Estructura del Proyecto

El proyecto sigue una organización modular por capas para facilitar el mantenimiento y la escalabilidad:

```text
Pata-Cargo/
├── app/
│   └── src/main/java/com/patacargo/virchm/
│       ├── api/            # Definiciones de Retrofit y Mercado Pago
│       ├── data/           # Entidades Room, DAOs y Repositorio unificado
│       ├── ui/
│       │   ├── components/ # Widgets reutilizables (Logos, Badges, etc.)
│       │   ├── dialogs/    # Ventanas emergentes de interacción
│       │   ├── screens/    # Flujos por rol (Sender, Carrier, Admin)
│       │   ├── theme/      # Definición de Material Design 3 (Colores, Tipografía)
│       │   └── PataCargoViewModel.kt # Lógica de estado global
│       └── MainActivity.kt # Punto de entrada y Shell de la app
├── assets/                 # Recursos multimedia para el repositorio
└── docs/                   # Documentación técnica adicional
```
