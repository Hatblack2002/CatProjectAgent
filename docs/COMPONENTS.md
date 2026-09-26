# CatProjectAgent — Catálogo de Componentes Reutilizables

Este catálogo define la anatomía, estados, variantes, tokens de diseño y comportamiento accesible de los componentes del sistema CatProjectAgent en Android Jetpack Compose.

---

## 1. ProjectCard (Tarjeta de Proyecto)
* **Propósito:** Mostrar el resumen interactivo de un proyecto activo o archivado en Dashboard y Lista de Proyectos.
* **Anatomía:**
  1. Icono de categoría con fondo redondeado (12dp radius) coloreado según el tipo (App móvil, Juego 2D, Web, API).
  2. Título de proyecto (`titleLarge`, peso SemiBold, `#F0F4FC`).
  3. Badge de estado (`StatusPill`: "En progreso", "En revisión", "Completado", "En pausa").
  4. Metadatos de última actividad (`bodySmall`, `#8E9CAE`, ej: "Última actividad: hace 2 h").
  5. Menú contextual (3 puntos verticales `MoreVert`, touch target 48x48 dp).
* **Variantes:**
  * `Compact`: Usada en el carrusel/lista vertical del Dashboard.
  * `Detailed`: Usada en la pantalla de Proyectos con métricas de archivos y agentes asignados.
* **Estados:** Default (elevación 1dp, borde `#283245`), Pressed (ripple sutil), Focused (borde `#F5A623`).

---

## 2. AgentCard (Tarjeta de Agente)
* **Propósito:** Representar la identidad técnica, especialidad y estado en tiempo real de cada agente autónomo.
* **Anatomía:**
  1. Avatar del agente con el color temático (Arquitecto azul `#0EA5E9`, Diseñador violeta `#A855F7`, Programador verde `#10B981`, Analista naranja `#F59E0B`, Investigador cyan `#06B6D4`).
  2. Nombre del agente (`titleMedium`, `#F0F4FC`).
  3. Badge de estado dinámico (`Activo`, `Pensando`, `Trabajando`, `En espera`).
  4. Descripción de responsabilidades (`bodyMedium`, `#8E9CAE`).
  5. Etiqueta de modelo asignado (ej: `Claude 3.5 Sonnet`, `Gemini 1.5 Pro`, `GPT-4o`).
* **Interacciones:** Al tocar la tarjeta, navega directamente a la sesión de chat contextual o panel de configuración del agente.

---

## 3. StatusPill (Insignia de Estado Multi-Dimensional)
* **Propósito:** Comunicar con precisión el estado de un agente, proyecto o tarea sin depender exclusivamente del color.
* **Anatomía:** Contenedor redondeado tipo píldora (100dp corner radius), padding horizontal 10dp, vertical 4dp.
  * Icono SVG de 14dp (Check, Sync, Warning, Hourglass).
  * Texto en `labelMedium` con mayúscula inicial.
* **Variantes de Estado:**
  * `Activo`: Verde `#10B981` sobre `#0A3222`.
  * `Pensando`: Azul `#38BDF8` sobre `#0C2B42`, con animación de pulso suave.
  * `Trabajando`: Ámbar `#F5A623` sobre `#332200`, con icono giratorio continuo.
  * `En Espera`: Naranja `#F59E0B` sobre `#332005`.
  * `Requiere Aprobación`: Rojo `#EF4444` sobre `#3B1215`, borde de alerta 1dp.
  * `Completado`: Verde `#10B981` con doble check.
  * `Error`: Rojo `#EF4444` con cruz de alerta.

---

## 4. ChatBubble (Burbujas de Conversación Contextual)
* **Propósito:** Mostrar los diálogos y acuerdos entre el usuario y los agentes dentro de un proyecto.
* **Anatomía:**
  * **Burbuja de Usuario:** Alineada a la derecha, fondo `#1E3A8A` / `#2563EB`, texto blanco `#FFFFFF`, esquinas redondeadas 16dp con la esquina inferior derecha a 4dp. Timestamp inferior derecha.
  * **Burbuja de Agente:** Alineada a la izquierda, avatar del agente en miniatura a la izquierda. Fondo `#161B26`, borde `#283245`, esquinas redondeadas 16dp con la esquina inferior izquierda a 4dp.
  * **Elementos Internos de Agente:**
    * Nombre del agente con su color temático (`labelMedium`).
    * Contenido en Markdown renderizado (listas ordenadas, negritas, fragmentos de código).
    * **Chips de Sugerencia Rápida:** Opciones clicables ("Android", "iOS", "Ambas") que permiten responder con un solo toque.
    * Timestamp inferior derecha.

---

## 5. ToolActionCard & ConfirmationSheet (Acción y Aprobación)
* **Propósito:** Transparencia absoluta ante acciones críticas de los agentes (modificación de código, borrado, comandos shell).
* **Anatomía de la Tarjeta:**
  1. Icono de advertencia o escudo.
  2. Titular claro: *"El agente Programador solicita modificar 14 archivos"*.
  3. Motivo explicado en lenguaje natural: *"Actualizar llamadas de la API de autenticación y refactorizar modelos de usuario."*
  4. Lista expandible de archivos afectados con badges de adición (+ verde) o eliminación (- rojo).
  5. Botones de acción:
    * `Permitir Acción`: Botón primario ámbar `#F5A623`.
    * `Rechazar`: Botón outlined borde `#EF4444` texto `#EF4444`.
    * `Revisar Cambios (Diff)`: Botón secundario `#283245`.

---

## 6. FileRow & FolderRow (Explorador de Archivos)
* **Propósito:** Gestionar y auditar los archivos del proyecto con aspecto técnico profesional.
* **Anatomía:**
  * Icono de carpeta dorada `#F59E0B` o archivo con extensión reconocible (`.md`, `.json`, `.kt`, `.png`).
  * Nombre del archivo/carpeta (`titleMedium`, `#F0F4FC`).
  * Subtítulo informativo (`bodySmall`, ej: "12 elementos" o "2.4 KB • hace 2 h").
  * Botón de opciones (3 puntos verticales) para previsualizar, renombrar, descargar o borrar.

---

## 7. AppBottomNavigationBar (Barra de Navegación M3)
* **Propósito:** Navegación persistente entre las 5 secciones cardinales de la app.
* **Destinos:**
  1. `Inicio` (Icono Home)
  2. `Proyectos` (Icono FolderCopy)
  3. `Agentes` (Icono GroupWork / Extension)
  4. `Archivos` (Icono InsertDriveFile / Folder)
  5. `Más` (Icono GridView / Settings)
* **Estilos:** Altura 64dp, fondo `#161B26`, indicador de selección con píldora ámbar suave, iconos de 24dp con touch target de 48dp, etiqueta en `labelSmall`.
