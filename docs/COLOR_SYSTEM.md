# CatProjectAgent — Sistema de Color y Tokens Visuales

## 1. Filosofía del Sistema de Color
El sistema de color de **CatProjectAgent** equilibra la seriedad técnica de una herramienta de ingeniería con la calidez distintiva de su mascota (los ojos ámbar curiosos del gato negro y la pureza del ratón mecánico blanco).

* **Enfoque Dark Mode Principal:** Diseñado con fondos oscuros profundos basados en pizarra/obsidiana (`#0E1117`), reduciendo la fatiga visual en sesiones prolongadas de desarrollo y lectura de código.
* **Jerarquía de Elevación M3:** Las tarjetas y contenedores utilizan elevaciones tonales perceptibles (`#161B26` y `#1F2636`) en lugar de sombras artificiales pesadas.
* **Tokens Semánticos Rigurosos:** Ningún color se codifica directamente en las vistas; se emplean tokens semánticos normalizados.

---

## 2. Paleta Fundamental y Tokens Base

| Token Semántico | Valor Hex | Rol / Aplicación | Contraste (WCAG) |
| :--- | :--- | :--- | :--- |
| `color.background` | `#0E1117` | Fondo principal de pantallas | Fondo base |
| `color.surface` | `#161B26` | Tarjetas de proyectos, barras, contenedores | Contraste con texto: > 12:1 (AAA) |
| `color.surfaceElevated` | `#1F2636` | Modales, diálogos flotantes, tarjetas activas | Elevación 1 |
| `color.surfaceVariant` | `#252D3F` | Inputs de texto, chips inactivos, divisores | Elevación 2 |
| `color.border` | `#283245` | Bordes de tarjetas y separadores sutiles | Relación 3:1 |
| `color.borderFocused` | `#F5A623` | Borde en foco o selección activa | Relación 4.5:1 |
| `color.textPrimary` | `#F0F4FC` | Títulos, nombres de proyecto, texto legible | 14.8:1 sobre Background (AAA) |
| `color.textSecondary` | `#8E9CAE` | Metadatos, horas, subtítulos, roles | 5.6:1 sobre Surface (AA) |
| `color.textTertiary` | `#5C687A` | Placeholders, marcas de agua, deshabilitados | 3.2:1 (Legible secundario) |

---

## 3. Colores de Identidad de Marca

| Token | Hex | Descripción / Uso |
| :--- | :--- | :--- |
| `color.brand.catAmber` | `#F5A623` | **Primario de Marca:** Ojos del gato, botón "Nuevo proyecto", FAB, acentos clave. |
| `color.brand.catAmberDark`| `#D97706` | Hover / Estado presionado del primario. |
| `color.brand.catAmberContainer` | `#2E2007` | Fondos de chips destacados y badges de acción. |
| `color.brand.onAmber` | `#1A1100` | Texto sobre botón ámbar (Contraste 11.2:1 AAA). |
| `color.brand.toyMouseWhite` | `#F8FAFC` | Blanco puro del ratón de juguete, acentos nítidos. |
| `color.brand.toyMouseSilver`| `#94A3B8` | Plata mecánica de la llave y ruedas del ratón. |
| `color.brand.toyMousePink` | `#F472B6` | Toque sutil de las orejas/nariz del ratón de cuerda. |

---

## 4. Codificación Cromática de Agentes
Cada agente de la plataforma posee una personalidad técnica y un color semántico consistente que lo distingue a lo largo de toda la UI (en tarjetas, burbujas de chat, archivos generados y registros de auditoría):

| Agente | Color Token | Hex | Significado Simbólico |
| :--- | :--- | :--- | :--- |
| **Arquitecto** | `color.agent.architect` | `#0EA5E9` (Sky Blue) | Estructura, cimientos, visión global del sistema. |
| **Diseñador** | `color.agent.designer` | `#A855F7` (Purple) | Estética, interfaz, creatividad, paletas y UX. |
| **Programador**| `color.agent.coder` | `#10B981` (Emerald) | Lógica, compilación, ejecución limpia de código. |
| **Analista** | `color.agent.analyst` | `#F59E0B` (Amber Orange) | Auditoría, verificación, detección de errores y QA. |
| **Investigador**| `color.agent.researcher`| `#06B6D4` (Cyan) | Exploración, benchmarks, fuentes y documentación. |
| **Personalizado**| `color.agent.custom` | `#EC4899` (Pink) | Agentes especializados creados por el usuario. |

---

## 5. Estados del Sistema (Multi-Dimensionales)
Los estados nunca dependen exclusivamente del color; cada estado asocia siempre color, texto e icono:

| Estado | Token Color | Hex | Icono Asociado | Significado UX |
| :--- | :--- | :--- | :--- | :--- |
| **Activo** | `color.status.active` | `#10B981` | CheckCircle / Dot pulsante | Agente listo y disponible. |
| **Pensando** | `color.status.thinking` | `#38BDF8` | Psychology / Sparkle sutil | Procesando razonamiento o prompts. |
| **Trabajando** | `color.status.working` | `#F5A623` | Sync / Wrench animado | Generando código o ejecutando pasos. |
| **En Espera** | `color.status.waiting` | `#F59E0B` | HourglassTop / Pause | Aguardando tarea o input. |
| **Requiere Aprobación**| `color.status.approval`| `#EF4444` | WarningAmber / ShieldAlert | El agente necesita confirmación humana explícita. |
| **Completado** | `color.status.completed`| `#10B981` | DoneAll | Tarea concluida con éxito verificable. |
| **Error** | `color.status.error` | `#EF4444` | ErrorOutline | Fallo en compilación o llamada a API. |
| **Pausado** | `color.status.paused` | `#6B7280` | PauseCircle | Operación detenida por el usuario. |
| **Verificando** | `color.status.verifying`| `#8B5CF6` | FactCheck / Rule | Análisis estático o pruebas en curso. |
| **Desconectado**| `color.status.offline` | `#4B5563` | CloudOff | Sin conexión a API o backend. |
