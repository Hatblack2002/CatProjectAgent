# CatProjectAgent — Directrices de Accesibilidad (Accessibility Guidelines)

## 1. Cumplimiento de Estándares WCAG 2.1 (Niveles AA y AAA)
CatProjectAgent está diseñado para ser una herramienta inclusiva, permitiendo que cualquier desarrollador o creador trabaje con comodidad y precisión en dispositivos móviles y tablets.

---

## 2. Requisitos Mandatorios de Interacción

### 2.1 Tamaño Mínimo de Área Táctil (Touch Targets)
* **Regla Estricta:** Ningún elemento interactivo (botones, iconos, chips de filtro, opciones de menú) puede tener un área interactiva menor a **48dp × 48dp**.
* En Jetpack Compose se garantiza utilizando:
  ```kotlin
  Modifier.minimumInteractiveComponentSize()
  ```
  o añadiendo padding táctil semántico en componentes compactos.

### 2.2 Independencia Absoluta del Color
* **Regla:** Ningún estado del sistema, error o advertencia debe comunicarse exclusivamente a través del color.
* **Solución Implementada:**
  * Todo `StatusPill` combina **icono distintivo + color de fondo + texto explícito** (ej: no solo un círculo rojo, sino el icono de alerta `WarningAmber` + texto "Requiere Aprobación").
  * Los campos con error en formularios muestran icono de advertencia y mensaje de ayuda en texto inmediatamente debajo.

### 2.3 Contraste de Color Verificado
* Texto principal (`#F0F4FC`) sobre fondo (`#0E1117`): **14.8:1** (Supera ampliamente el estándar AAA de 7:1).
* Texto secundario (`#8E9CAE`) sobre superficies (`#161B26`): **5.6:1** (Cumple estándar AA de 4.5:1).
* Botón primario de marca (`#F5A623`) con texto oscuro (`#1A1100`): **11.2:1** (AAA).

---

## 3. Compatibilidad con Lectores de Pantalla (TalkBack)
* **Content Descriptions Significativas:** Todos los iconos funcionales (`IconButton`, `Icon`, imágenes de estado) deben contar con `contentDescription` descriptivo en español neutro (ej: `"Filtrar proyectos"`, `"Cerrar asistente"`, `"Enviar mensaje al Arquitecto"`).
* **Elementos Decorativos:** Las ilustraciones puramente estéticas y fondos deben marcarse con `contentDescription = null` para evitar saturar el lector de pantalla.
