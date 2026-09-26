# CatProjectAgent — Sistema Visual y Design System Completo

## 1. Visión General
**CatProjectAgent** es un sistema visual y de experiencia de usuario nativo para Android diseñado para coordinar múltiples agentes de inteligencia artificial sobre proyectos reales.

Su identidad visual se desmarca radicalmente de los clichés genéricos de startups de inteligencia artificial (cerebros brillantes, chips, cabezas de robots, neón saturado) a favor de un lenguaje **ingenieril, lúcido, cálido y distintivo**, anclado en su mascota: **el gato negro pixel art que da cuerda al ratón mecánico blanco**.

---

## 2. Jerarquía de Arquitectura Visual

```
Design System
      ↓
Theme (Theme.kt, Color.kt, Type.kt)
      ↓
Semantic Tokens (color.background, color.brand.catAmber, etc.)
      ↓
Core Components (Buttons, Chips, Cards, Badges, StatusPills)
      ↓
Organisms / Feature Views (ProjectCard, AgentChatBubble, ActionApprovalModal)
      ↓
Screens (Splash, Dashboard, Projects, Agents, Files, Chat, Settings, More)
      ↓
Navigation Flow & Project Experience
```

---

## 3. Resumen de Tokens Semánticos Principales

* **Fondo Primario:** `#0E1117`
* **Superficie de Tarjetas:** `#161B26`
* **Superficie Elevada / Modales:** `#1F2636`
* **Borde / Separadores:** `#283245`
* **Acento Primario (Ojos del Gato / Acciones):** `#F5A623` (Ámbar)
* **Texto Principal:** `#F0F4FC` (Alto contraste AAA)
* **Texto Secundario:** `#8E9CAE`
* **Ratón Mecánico:** `#F8FAFC` con llave plateada `#94A3B8`

---

## 4. Estructura de Agentes del Sistema

| Agente | Color Identificador | Icono Base | Especialidad |
| :--- | :--- | :--- | :--- |
| **Arquitecto** | Azul Cielo `#0EA5E9` | AccountTree / Hub | Cimientos, especificación, arquitectura y plan. |
| **Diseñador** | Violeta `#A855F7` | Palette / Brush | UX/UI, tokens, componentes y pantallas. |
| **Programador**| Verde Esmeralda `#10B981`| Terminal / Code | Código Kotlin, lógica de negocio y tests. |
| **Analista** | Ámbar `#F59E0B` | Rule / FactCheck | QA, auditoría, verificación de requisitos. |
| **Investigador**| Cian `#06B6D4` | Search / FindInPage| Benchmark, fuentes y documentación. |
| **Personalizado**| Magenta `#EC4899` | AddBox / Extension | Agentes especializados definidos por el usuario. |

---

## 5. Criterios de Aceptación Visual
1. Todas las pantallas del mockup (`Pantalla de inicio`, `Panel principal`, `Panel de proyectos`, `Panel de agentes`, `Panel de archivos`, `Panel de configuración`, `Nuevo proyecto`, `Chat con agente`, `Detalle del proyecto`, `Panel más`) deben ser plenamente interactivas y coherentes.
2. La mascota (el gato negro dando cuerda al ratón blanco) debe estar presente en el Splash, icono de launcher, encabezado de proyecto y estados vacíos.
3. El proyecto debe ser el centro neurálgico del flujo operativo: un proyecto contiene agentes, código, tareas y releases.
