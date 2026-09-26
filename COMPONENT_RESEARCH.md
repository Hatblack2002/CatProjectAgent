# CatProjectAgent — Investigación Rigurosa de Componentes y Bibliotecas Android

Este documento compila la investigación técnica profunda de componentes y bibliotecas maduras existentes en el ecosistema Android y Jetpack Compose para evitar la reinvención innecesaria de la rueda ("Reuse Before Rebuild"), documentando compatibilidad verificada, licencias y análisis de riesgos.

---

## 1. Coil (Compose Image Loading)
* **Nombre:** Coil (Coroutine Image Loader)
* **Repositorio:** `coil-kt/coil`
* **URL:** `https://github.com/coil-kt/coil`
* **Licencia:** Apache License 2.0
* **Lenguaje:** Kotlin
* **Tecnología:** Kotlin Coroutines, OkHttp, Android Canvas / Bitmaps
* **Android Compatible:** Sí (API 21+)
* **Compose Compatible:** Sí (`coil-compose`, paquete oficial)
* **Estado de Mantenimiento:** Activo y altamente mantenido por Instacart / Colin White.
* **Última Actividad Conocida:** Versión 2.7.0 (y releases 3.x para Compose Multiplatform en 2024-2025).
* **Dependencias:** `org.jetbrains.kotlinx:kotlinx-coroutines-android`, `com.squareup.okhttp3:okhttp`
* **Qué resuelve:** Carga eficiente, asíncrona y con memoria caché de imágenes en Compose (avatares de agentes, logotipos pixel art de CatProjectAgent, banners de proyectos).
* **Qué NO resuelve:** No edita imágenes ni genera spritesheets en tiempo real.
* **Cómo podría integrarse:**
  ```kotlin
  AsyncImage(
      model = project.bannerUrl,
      contentDescription = "Banner del proyecto",
      modifier = Modifier.fillMaxWidth().height(160.dp)
  )
  ```
* **Riesgos:** Bajo. Es el estándar de facto recomendado oficialmente por Google para Jetpack Compose.

---

## 2. Compose Markdown (Renderizado de Respuestas de Agentes)
* **Nombre:** compose-markdown (o rica integración con Commonmark / Markwon)
* **Repositorio:** `jeziellago/compose-markdown`
* **URL:** `https://github.com/jeziellago/compose-markdown`
* **Licencia:** MIT License
* **Lenguaje:** Kotlin
* **Tecnología:** Jetpack Compose, Commonmark
* **Android Compatible:** Sí
* **Compose Compatible:** Sí (Nativo Composable `MarkdownText`)
* **Estado de Mantenimiento:** Mantenido por la comunidad; alternativas estables incluyen Markwon con `AndroidView`.
* **Última Actividad Conocida:** 2024.
* **Dependencias:** `org.commonmark:commonmark:0.21.0`
* **Qué resuelve:** Renderizado de respuestas técnicas de agentes con listas numeradas, viñetas, enlaces, citas y fragmentos de código en bloques de Markdown.
* **Qué NO resuelve:** No incluye resaltado de sintaxis sintáctico completo para 50+ lenguajes (requiere prism o tree-sitter auxiliar).
* **Cómo podría integrarse:**
  ```kotlin
  MarkdownText(
      markdown = agentMessage.content,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface
  )
  ```
* **Riesgos:** Medio-Bajo. Puede estilizarse con fuentes monospace y paleta del tema CatProjectAgent.

---

## 3. Vico (Data Visualization & Métricas de Proyectos)
* **Nombre:** Vico
* **Repositorio:** `patrykandpatrick/vico`
* **URL:** `https://github.com/patrykandpatrick/vico`
* **Licencia:** Apache License 2.0
* **Lenguaje:** Kotlin
* **Tecnología:** Jetpack Compose Canvas nativo
* **Android Compatible:** Sí (API 21+)
* **Compose Compatible:** Sí (`vico-compose` y `vico-compose-m3`)
* **Estado de Mantenimiento:** Muy activo, diseñado específicamente para M3.
* **Última Actividad Conocida:** 2024-2025.
* **Dependencias:** Dependencias base de Compose UI.
* **Qué resuelve:** Gráficos de barras y líneas para el Dashboard y Detalle del Proyecto (commits por agente, archivos modificados, velocidad del proyecto, progreso semanal).
* **Qué NO resuelve:** No gestiona modelos de datos relacionales ni bases de datos.
* **Cómo podría integrarse:**
  ```kotlin
  ProvideChartStyle {
      Chart(
          chart = lineChart(),
          model = projectActivityChartModel,
          modifier = Modifier.fillMaxWidth().height(180.dp)
      )
  }
  ```
* **Riesgos:** Bajo. Cumple con la directriz de evitar herramientas web (D3/WebView) en Android.

---

## 4. Room Database (Persistencia Local de Proyectos, Agentes y Tareas)
* **Nombre:** AndroidX Room
* **Repositorio:** `androidx/room` (Google AOSP)
* **URL:** `https://developer.android.com/training/data-storage/room`
* **Licencia:** Apache License 2.0
* **Lenguaje:** Kotlin / Java
* **Tecnología:** SQLite nativo, KSP (Kotlin Symbol Processing), Coroutines Flow
* **Android Compatible:** Sí (Core Android Jetpack)
* **Compose Compatible:** Sí (expone `Flow<List<Project>>` recolectable con `collectAsStateWithLifecycle`)
* **Estado de Mantenimiento:** Activo de máxima prioridad por Google Android Team.
* **Última Actividad Conocida:** Versión 2.7.0 / 2.6.x (2024-2025).
* **Dependencias:** `androidx.room:room-runtime`, `androidx.room:room-ktx`, `androidx.room:room-compiler`
* **Qué resuelve:** Almacenamiento local seguro, transaccional y sin conexión de proyectos creados, historial de conversaciones con agentes, archivos locales y configuración de usuario.
* **Qué NO resuelve:** No sincroniza automáticamente con servidores en la nube sin capa de red auxiliar.
* **Cómo podría integrarse:**
  `ProjectDao`, `AgentDao`, `TaskDao` con base de datos `CatProjectDatabase`.
* **Riesgos:** Ninguno. Es el estándar de arquitectura oficial de Google.

---

## 5. Navigation Compose (Gestión de Rutas y Backstack)
* **Nombre:** AndroidX Navigation Compose
* **Repositorio:** `androidx/navigation` (Google AOSP)
* **URL:** `https://developer.android.com/guide/navigation/navigation-compose`
* **Licencia:** Apache License 2.0
* **Lenguaje:** Kotlin
* **Tecnología:** Kotlinx Serialization type-safe routes
* **Android Compatible:** Sí
* **Compose Compatible:** Sí (Nativo)
* **Estado de Mantenimiento:** Activo oficial.
* **Última Actividad Conocida:** 2.8.9 (2024-2025 con soporte para `@Serializable` objects y data classes).
* **Dependencias:** `androidx.navigation:navigation-compose:2.8.9`
* **Qué resuelve:** Gestión del backstack de Android, transiciones animadas entre pantalla de Proyectos y Detalle, paso seguro de IDs de proyecto y rutas.
* **Qué NO resuelve:** No dibuja la interfaz gráfica por sí mismo.
* **Cómo podría integrarse:** `NavHost` central con rutas `DashboardRoute`, `ProjectsRoute`, `ProjectDetailRoute(id: String)`, `AgentChatRoute(agentId: String)`.
* **Riesgos:** Ninguno.

---

## 6. Accompanist Permissions (Manejo de Permisos en Compose)
* **Nombre:** Accompanist Permissions
* **Repositorio:** `google/accompanist`
* **URL:** `https://github.com/google/accompanist`
* **Licencia:** Apache License 2.0
* **Lenguaje:** Kotlin
* **Tecnología:** Jetpack Compose, Activity Result Contracts
* **Android Compatible:** Sí
* **Compose Compatible:** Sí
* **Estado de Mantenimiento:** Mantenido / Migrando progresivamente a APIs nativas de AndroidX.
* **Última Actividad Conocida:** 0.37.x.
* **Dependencias:** `com.google.accompanist:accompanist-permissions`
* **Qué resuelve:** Flujo declarativo para solicitar permisos de notificaciones (`POST_NOTIFICATIONS`) y biometría.
* **Riesgos:** Muy bajo; puede sustituirse directamente por `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` sin dependencias externas si se busca minimizar el APK.
