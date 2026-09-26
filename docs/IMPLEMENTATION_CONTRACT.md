# CatProjectAgent — Contrato de Implementación para el Agente Programador (Chat Z)

## 1. Alcance y Propósito del Contrato
Este documento constituye el contrato técnico formal para el agente de programación (Chat Z). Define con precisión absoluta **qué construir**, **qué NO construir**, la arquitectura de código Kotlin + Jetpack Compose, las bibliotecas verificadas a reutilizar y los criterios de aceptación.

---

## 2. Lo que Chat Z DEBE Construir (Requisitos Mandatorios)

1. **Identidad y Nomenclatura:**
   * Nombre oficial inmutable: `CatProjectAgent`.
   * `app_name` en `strings.xml`: `"CatProjectAgent"`.
   * `metadata.json`: `"name": "CatProjectAgent"`.
   * Icono adaptativo nativo de Android configurado con el gato negro pixel art y ratón de juguete.

2. **Theming y Tokens de Diseño:**
   * Implementación de la paleta oscura en `ui/theme/Color.kt` y `ui/theme/Theme.kt` utilizando los tokens documentados en `docs/COLOR_SYSTEM.md`.
   * Tipografía M3 en `ui/theme/Type.kt` alineada con `docs/TYPOGRAPHY.md`.

3. **10 Pantallas Interactivas Nativas:**
   * `SplashScreen`: Logo del gato dando cuerda al ratón blanco, barra de carga ámbar y eslogan oficial.
   * `DashboardScreen` (Panel principal): Saludo a usuario, tarjeta destacada "Nuevo proyecto", lista de proyectos recientes con estados y navegación inferior.
   * `ProjectsScreen`: Búsqueda de proyectos, chips de filtro (`Todos`, `Activos`, `Pausados`, `Completados`), tarjetas con menú contextual (3 puntos).
   * `AgentsScreen`: Lista de agentes (Arquitecto, Diseñador, Programador, Analista, Investigador, Personalizado) con colores semánticos, modelos asignados y píldoras de estado (`Activo`, `En espera`).
   * `FilesScreen`: Explorador con carpetas (`Documentos`, `Imágenes`, `Código`, `Modelos`, `Recursos`), archivos (`README.md`, `config.json`), filtros y botón `+`.
   * `SettingsScreen`: Perfil de usuario, preferencias de tema/idioma/notificaciones, configuración de proveedores de IA y seguridad.
   * `NewProjectScreen` (Sheet/Modal): Formulario con nombre, descripción, selector de categorías por chips (`Todo`, `Apps`, `Web`, `Juegos`, `Otros`), selección de plantillas y botón de creación.
   * `AgentChatScreen`: Conversación contextual con el Arquitecto o Programador, mensaje de bienvenida, preguntas numeradas, chips de respuesta rápida (`Android`, `iOS`, `Ambas`) y campo de texto con botón enviar.
   * `ProjectDetailScreen`: Pestañas (`Resumen`, `Tareas`, `Archivos`, `Config`), barra de progreso general (65%), banner con la mascota, y lista de tareas con estados (`Completada`, `En progreso`, `Pendiente`).
   * `MoreScreen`: Perfil de usuario, historial, configuración rápida, ayuda, información de versión y cerrar sesión.
   * `ActionApprovalSheet`: Diálogo modal de confirmación transparente cuando un agente solicita modificar o borrar archivos.

4. **Componentes Reutilizables:**
   * `ProjectCard`, `AgentCard`, `StatusPill`, `ChatBubble`, `ToolActionCard`, `FileRow`, `FolderRow`, `CategoryChipGroup`.

---

## 3. Lo que Chat Z NO DEBE Construir (Restricciones y Prohibiciones)

1. **NO** reintroducir conceptos de proyectos obsoletos: Nada de TerminalHouse, Ubuntu, PRoot, Termux o Cyber Command OS en la UI ni en los nombres de las clases de presentación.
2. **NO** usar logos o metáforas genéricas de IA: Prohibido usar cabezas de robot, circuitos integrados, cerebros con neón o estrellas mágicas de IA.
3. **NO** pintar al gato agresivo ni sustituir el ratón blanco de juguete por un ratón de carne y hueso.
4. **NO** romper la compatibilidad de compilación de Android (`compile_applet` debe pasar limpiamente).
5. **NO** hardcodear claves API ni secretos en el código fuente.

---

## 4. Estado de Verificación de Componentes y Bibliotecas

| Componente / Biblioteca | Estado | Justificación |
| :--- | :--- | :--- |
| **Jetpack Compose M3** | `VERIFIED / APPROVED` | Base oficial de UI para Android moderno. |
| **Coil Image Loader** | `VERIFIED / APPROVED` | Para cargar los recursos de arte pixel de CatProjectAgent. |
| **Room Database** | `VERIFIED / APPROVED` | Para la persistencia local de proyectos, agentes y tareas. |
| **StateFlow / MVVM** | `VERIFIED / APPROVED` | Arquitectura reactiva sin lag ni recomposiciones innecesarias. |
| **Android Vector Graphics** | `VERIFIED / APPROVED` | Para iconografía escalable y touch targets accesibles. |

---

## 5. Criterio de Finalización para Chat Z
El trabajo de implementación se considerará exitoso cuando:
1. El código compile sin errores ni warnings críticos (`compile_applet` exitoso).
2. El usuario pueda navegar libremente por todas las 10 pantallas y experimentar el flujo del proyecto.
3. La interfaz refleje fielmente la identidad visual de CatProjectAgent.
