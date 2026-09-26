# CatProjectAgent 🐱⚙️

**CatProjectAgent** es una plataforma nativa de Android diseñada para coordinar y orquestar múltiples agentes autónomos de inteligencia artificial (**Arquitecto**, **Diseñador**, **Programador**, **Analista**, **Investigador**) que colaboran en proyectos de ingeniería y desarrollo reales.

> *«El gato pone el proyecto en marcha. El ratón representa aquello que se pone en movimiento. Los agentes trabajan juntos para convertir una idea en un proyecto real.»*

---

## 🎨 Identidad Visual y Mascota
* **Mascota Oficial:** Un gato negro artesanal en pixel art con expresivos ojos ámbar dorados (`#F5A623`).
* **Compañero:** Un ratón blanco mecánico de juguete sobre ruedas con una llave de cuerda en la espalda (`#F8FAFC` y `#94A3B8`).
* **Concepto:** Una pata sostiene el juguete mientras la otra gira la llave mecánica para ponerlo en marcha. Simboliza curiosidad, control, inteligencia y dinamismo técnico.
* **Cero Clichés de IA:** Se eliminaron deliberadamente cabezas de robots, circuitos, cerebros brillantes y destellos mágicos para crear un sistema de diseño propio y profesional.

---

## 📱 Pantallas Principales (10 Pantallas Interactivas)
1. **Pantalla de Inicio (Splash):** Mascota pixel art oficial, animación de carga y eslogan de marca.
2. **Dashboard / Panel Principal:** Saludo, tarjeta "Nuevo proyecto", banner animado del gato persiguiendo al ratón de cuerda y proyectos recientes.
3. **Panel de Proyectos:** Búsqueda en tiempo real y filtrado por chips (*Todos, Activos, Pausados, Completados*).
4. **Panel de Agentes:** Visualización del equipo (*Arquitecto, Diseñador, Programador, Analista, Investigador, Personalizado*) con sus estados y modelos.
5. **Panel de Archivos:** Centro de artefactos con carpetas (*Documentos, Imágenes, Código, Modelos, Recursos*) y archivos del proyecto.
6. **Panel de Configuración:** Gestión de perfil, preferencias, proveedor de modelos de IA y seguridad biométrica.
7. **Nuevo Proyecto:** Creación guiada con categorías y plantillas (*Proyecto desde cero, App móvil, Sitio web, Juego 2D, API / Backend*).
8. **Chat con Agente:** Conversación contextual con el Arquitecto o Programador con chips de respuesta rápida (*Android, iOS, Ambas*).
9. **Detalle del Proyecto:** Pestañas (*Resumen, Tareas, Archivos, Config*), barra de progreso general (65%), banner del proyecto y lista de tareas con estados.
10. **Panel Más:** Perfil, historial, ayuda, configuración y cierre de sesión.
11. **Hoja de Aprobación de Acciones:** Diálogo modal de confirmación transparente cuando un agente solicita modificar o borrar archivos.

---

## 📚 Documentación de Diseño y Arquitectura (Entregables)
Todo el sistema está documentado en especificaciones listas para implementación:

* [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md): Sistema visual completo y arquitectura de tokens.
* [BRAND_GUIDELINES.md](BRAND_GUIDELINES.md): Filosofía de marca, metáfora del gato y ratón, restricciones y tono de voz.
* [COLOR_SYSTEM.md](COLOR_SYSTEM.md): Tokens de color, ratios de contraste WCAG 2.1 AAA/AA y paleta por agente.
* [TYPOGRAPHY.md](TYPOGRAPHY.md): Escala tipográfica en `sp`, familias y line heights.
* [COMPONENTS.md](COMPONENTS.md): Catálogo de componentes reutilizables (tarjetas, botones, píldoras de estado).
* [SCREENS.md](SCREENS.md): Especificación minuciosa de todas las pantallas del mockup.
* [NAVIGATION.md](NAVIGATION.md): Flujos de navegación, árbol de rutas y BackHandler nativo.
* [MOTION.md](MOTION.md): Animación insignia en bucle del gato y ratón mecánico con la llave girando a 360°.
* [ACCESSIBILITY.md](ACCESSIBILITY.md): Guía de accesibilidad, targets táctiles mínimos de 48dp y TalkBack.
* [COMPONENT_RESEARCH.md](COMPONENT_RESEARCH.md): Investigación técnica de bibliotecas Android (*Reuse Before Rebuild*).
* [IMPLEMENTATION_CONTRACT.md](IMPLEMENTATION_CONTRACT.md): Contrato técnico formal para el agente programador (**Chat Z**).

---

## 🛠️ Stack Tecnológico
* **Lenguaje:** Kotlin 2.2
* **UI Toolkit:** Jetpack Compose (Material 3)
* **Arquitectura:** MVVM + StateFlow + Clean Architecture
* **Carga de Imágenes:** Coil Compose 2.7.0
* **Persistencia Local:** AndroidX Room 2.7.0
* **Build System:** Gradle (Kotlin DSL - .gradle.kts)
