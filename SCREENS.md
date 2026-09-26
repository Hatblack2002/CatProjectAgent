# CatProjectAgent — Especificación Detallada de Pantallas (Screens)

Este documento especifica la estructura, componentes, estados y comportamiento de las 10 pantallas principales del sistema CatProjectAgent vistas en el mockup de diseño y ampliadas según los requerimientos.

---

## 1. Pantalla de Inicio / Splash (SplashScreen)
* **Objetivo:** Recibir al usuario y mostrar la identidad icónica del sistema en movimiento.
* **Elementos Clave:**
  * **Ilustración Central:** Gato negro en pixel art sentado con mirada curiosa y el ratón blanco de cuerda con ruedas y llave metálica visible.
  * **Nombre Oficial:** `CatProjectAgent` con tipografía prominente.
  * **Eslogan:** *"Tus ideas, múltiples agentes, un solo objetivo."*
  * **Barra de Carga:** Barra lineal en tono ámbar `#F5A623` con animación de avance progresivo.
* **Transición:** Tras 2 segundos o carga de sesión inicial, transiciona suavemente mediante crossfade hacia el Panel Principal.

---

## 2. Panel Principal / Dashboard (HomeScreen / DashboardScreen)
* **Objetivo:** Hub de control general para conocer el estado actual y arrancar iniciativas rápidamente.
* **Estructura y Componentes:**
  1. **Top Bar:** Logotipo miniatura de CatProjectAgent + Nombre de la app, botón de notificaciones (Campana con punto de alerta ámbar) y avatar circular del usuario.
  2. **Saludo y Prompt de Entrada:**
     * *"Hola, Usuario"* (`headlineLarge`, SemiBold).
     * *"¿Qué proyecto quieres construir hoy?"* (`bodyMedium`, `#8E9CAE`).
  3. **Tarjeta de Acción Rápida "Nuevo Proyecto":**
     * Fondo oscuro con borde acentuado.
     * Icono grande de adición en recuadro ámbar (`+`).
     * Título *"Nuevo proyecto"* + *"Crea un proyecto desde cero o con una plantilla."*
     * Flecha de navegación hacia la derecha (`ChevronRight`).
  4. **Sección "Tus proyectos" con enlace "Ver todos >":**
     * Lista vertical de tarjetas de proyecto recientes:
       * *App de Inventario* (En progreso, Última actividad: hace 2 h).
       * *Juego 2D Pixel* (En revisión, Última actividad: hace 5 h).
       * *Blog Personal* (Completado, Última actividad: hace 1 día).
       * *App de Tareas* (En pausa, Última actividad: hace 2 días).
  5. **Barra de Navegación Inferior:** Activa en la pestaña `Inicio`.

---

## 3. Panel de Proyectos (ProjectsScreen)
* **Objetivo:** Gestión integral, filtrado y búsqueda de todos los proyectos del usuario.
* **Estructura:**
  1. **Top Bar:** Título *"Proyectos"* con icono de menú lateral y botón de añadir nuevo proyecto.
  2. **Barra de Búsqueda:** Campo de texto estilizado *"Buscar proyectos..."* con icono de lupa a la izquierda y botón de filtros avanzados a la derecha.
  3. **Filtros por Estado (Chips Horizontales):**
     * `Todos` (Seleccionado, fondo ámbar `#F5A623`, texto `#1A1100`).
     * `Activos` (Contorno `#283245`, texto `#8E9CAE`).
     * `Pausados`.
     * `Completados`.
  4. **Lista de Proyectos:** Tarjetas con icono representativo, título, fecha relativa, estado en píldora de color y menú de 3 puntos (Opciones: *Abrir, Pausar, Duplicar, Exportar, Eliminar*).

---

## 4. Panel de Agentes (AgentsScreen)
* **Objetivo:** Supervisar y configurar el equipo de agentes de inteligencia artificial disponibles.
* **Estructura:**
  1. **Top Bar:** Título *"Agentes"* con icono de engranaje de configuración global de IA a la derecha.
  2. **Lista de Agentes del Sistema:**
     * **Arquitecto:** Icono cian, *"Define la estructura y plan del proyecto."*, Estado: `Activo`. Modelo: Claude 3.5 Sonnet / Gemini 1.5 Pro.
     * **Diseñador:** Icono púrpura, *"Crea la interfaz y experiencia visual."*, Estado: `Activo`.
     * **Programador:** Icono verde esmeralda, *"Implementa el código y la lógica."*, Estado: `Activo`.
     * **Analista:** Icono ámbar, *"Revisa, prueba y valida resultados."*, Estado: `En espera`.
     * **Investigador:** Icono azul cielo, *"Busca información y referencias."*, Estado: `Activo`.
     * **Personalizado:** Icono rosa, *"Agrega tus propios agentes especializados."*, Botón `+`.
  3. **Interacción:** Pulsar sobre cualquier agente abre el chat directo o el panel de asignación de directivas/herramientas.

---

## 5. Panel de Archivos (FilesScreen)
* **Objetivo:** Explorador centralizado de los artefactos, código fuente y documentos generados por los agentes.
* **Estructura:**
  1. **Top Bar:** Título *"Archivos"* y botón de acción rápida `+` (Crear carpeta / Importar archivo).
  2. **Búsqueda y Filtros:** Campo *"Buscar archivos..."* y pestañas: `Todos`, `Proyectos`, `Recientes`.
  3. **Carpetas Estructuradas:**
     * 📁 `Documentos` (12 elementos)
     * 📁 `Imágenes` (8 elementos)
     * 📁 `Código` (24 elementos)
     * 📁 `Modelos` (6 elementos)
     * 📁 `Recursos` (10 elementos)
  4. **Archivos Sueltos / Clave:**
     * 📄 `README.md` (2.4 KB • hace 2 h)
     * ⚙️ `config.json` (1.2 KB • hace 3 h)
  5. **Acciones:** Previsualización de Markdown, sintaxis resaltada para archivos de código y exportación.

---

## 6. Panel de Configuración (SettingsScreen)
* **Objetivo:** Ajustes globales de cuenta, privacidad, modelos de lenguaje y comportamiento del sistema.
* **Secciones:**
  1. **Cuenta:** Tarjeta de perfil con foto, nombre *"Usuario"*, email y botón de editar perfil.
  2. **Preferencias:**
     * *Tema:* Oscuro / Claro / Sistema.
     * *Idioma:* Español (con selector internacional).
     * *Notificaciones:* Activadas (push ante requerimientos de aprobación).
     * *Uso de datos:* Solo Wi-Fi para descargas pesadas de artefactos.
  3. **Modelos de IA:**
     * *Proveedor principal:* Selector entre OpenAI, Anthropic, Gemini, o servidor local (Ollama).
     * *Gestor de Claves API:* Entrada segura con encriptación local en Keystore.
  4. **Seguridad:**
     * *Autenticación:* PIN / Biometría para aprobar modificaciones directas en el código.

---

## 7. Pantalla / Sheet: Nuevo Proyecto (NewProjectScreen)
* **Objetivo:** Asistente ágil para iniciar un proyecto guiado por los agentes.
* **Campos y Pasos:**
  1. **Encabezado:** Título *"Nuevo proyecto"* con botón de cierre `X` en la esquina superior derecha.
  2. **Nombre del Proyecto:** Campo con placeholder *"Ej. App de mensajería"*.
  3. **Descripción (opcional):** Campo multilínea *"Describe brevemente tu proyecto..."*.
  4. **Selector de Plantilla / Categoría (Chips):** `Todo`, `Apps`, `Web`, `Juegos`, `Otros`.
  5. **Tarjetas de Plantilla:**
     * *Proyecto desde cero* (Comienza con una base limpia).
     * *App móvil* (Aplicación para Android/iOS).
     * *Sitio web* (Landing page o sitio completo).
     * *Juego 2D* (Con motor de juego integrado).
     * *API / Backend* (Servicio y base de datos).
  6. **Botón Principal:** Botón ancho ámbar *"Crear proyecto"*.

---

## 8. Chat con Agente (AgentChatScreen)
* **Objetivo:** Conversación interactiva con contexto de proyecto, llamadas a herramientas y respuestas guiadas.
* **Estructura:**
  1. **Top Bar:** Botón Volver (`ArrowBack`), Avatar del agente + Nombre (*Arquitecto*), badge de estado (*Activo*), menú de 3 puntos.
  2. **Área de Mensajes:**
     * Burbuja del usuario: *"Quiero crear una app de tareas que se sincronice en la nube."*
     * Respuesta del Arquitecto con estructura paso a paso:
       * *"Perfecto. Para este proyecto necesito confirmar algunos detalles:"*
       * `1. ¿Qué plataforma prefieres? (Android, iOS o ambas)`
       * `2. ¿Qué funciones básicas quieres incluir?`
       * `3. ¿Tienes alguna preferencia de tecnología o lenguaje?`
     * **Chips de Sugerencia Rápida (Quick Reply):** `Android`, `iOS`, `Ambas`.
  3. **Barra de Entrada de Mensaje:** Campo redondeado *"Escribe tu mensaje..."* con icono de adjuntar archivo y botón circular ámbar de enviar.

---

## 9. Detalle del Proyecto (ProjectDetailScreen)
* **Objetivo:** Visión 360° del estado de avance, tareas, archivos vinculados y releases de un proyecto particular.
* **Estructura:**
  1. **Cabecera:** Nombre del proyecto (*App de Inventario*), badge *"En progreso"*.
  2. **Pestañas de Navegación Interna:** `Resumen`, `Tareas`, `Archivos`, `Config`.
  3. **Banner del Proyecto:** Ilustración enmarcada del gato y ratón mecánico con descripción sintética.
  4. **Barra de Progreso General:** Barra con porcentaje destacado (*65% completado*).
  5. **Tareas Recientes con Estado:**
     * 🟢 *Diseño de interfaz:* `Completada`
     * 🟣 *Base de datos:* `En progreso`
     * ⚪ *Integración de API:* `Pendiente`
     * ⚪ *Pruebas:* `Pendiente`
  6. **Botón de Acción:** *"Ver detalles"* o *"Gestionar tareas"*.

---

## 10. Panel "Más" (MoreScreen)
* **Objetivo:** Accesos directos a perfil, historial histórico, soporte y créditos del sistema.
* **Elementos:**
  1. Tarjeta de Usuario con avatar, nombre y email.
  2. Lista de accesos con iconos:
     * 🕒 *Historial de actividad y auditoría*
     * ⚙️ *Configuración rápida*
     * ❓ *Ayuda y documentación*
     * ℹ️ *Sobre la aplicación (Versión, créditos, licencia)*
     * 🚪 *Cerrar sesión* (Color rojo con confirmación)
