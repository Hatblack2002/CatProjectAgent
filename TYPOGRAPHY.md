# CatProjectAgent — Sistema Tipográfico (Typography System)

## 1. Principios Tipográficos
CatProjectAgent requiere una jerarquía tipográfica que proyecte:
1. **Claridad Técnica y Legibilidad:** Sesiones largas de lectura de especificaciones de proyectos, prompts y código fuente.
2. **Personalidad sin Clichés:** Evitar tipografías retro arcade ilegibles para texto general o fuentes con clichés de ciencia ficción que resten profesionalismo.
3. **Escalares Accesibles:** Cumplimiento con las unidades escalares de Android (`sp`) para respetar las preferencias de accesibilidad del sistema operativo.

---

## 2. Familias Tipográficas Recomendadas

* **Familia Primaria (Display, Títulos, UI, Body, Labels):** `Inter` o `Roboto Flex` / Sistema Android nativo (`FontFamily.Default` / `SansSerif`).
  * Excelente renderizado en pantallas OLED de alta densidad (xxhdpi/xxxhdpi).
  * Amplia gama de pesos desde Light (300) hasta ExtraBold (800).
* **Familia Monospace (Código, Logs, Diff, Hashes, Rutas de archivo):** `JetBrains Mono` o `Roboto Mono` (`FontFamily.Monospace`).
  * Altura de x generosa y diferenciación clara entre `0` (cero) y `O`, `1`, `l` e `I`.
  * Figuras tabulares alineadas para columnas de datos.

---

## 3. Escala Tipográfica Normalizada (Type Scale Tokens)

| Token M3 | Peso (FontWeight) | Tamaño (sp) | Interlineado (sp) | Espaciado (LetterSpacing) | Casos de Uso |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `type.displayLarge` | Bold (700) | 32sp | 40sp | -0.25sp | Splash, pantalla de bienvenida |
| `type.displayMedium`| SemiBold (600) | 28sp | 36sp | 0.0sp | Título de detalle de proyecto |
| `type.headlineLarge`| SemiBold (600) | 24sp | 32sp | 0.0sp | Encabezados de Dashboard ("Hola, Usuario") |
| `type.headlineMedium`| Medium (500) | 20sp | 28sp | 0.15sp | Títulos de secciones ("Tus proyectos", "Agentes") |
| `type.titleLarge` | SemiBold (600) | 18sp | 24sp | 0.1sp | Nombres de tarjetas de proyecto, modales |
| `type.titleMedium` | Medium (500) | 16sp | 22sp | 0.15sp | Títulos de agentes, cabeceras de chat |
| `type.bodyLarge` | Normal (400) | 16sp | 24sp | 0.5sp | Mensajes de chat del usuario y agentes |
| `type.bodyMedium` | Normal (400) | 14sp | 20sp | 0.25sp | Descripciones de proyectos y agentes |
| `type.bodySmall` | Normal (400) | 12sp | 16sp | 0.4sp | Metadatos secundarios, timestamps ("Hace 2 h") |
| `type.labelLarge` | Medium (600) | 14sp | 20sp | 0.1sp | Texto en botones de acción ("Nuevo proyecto") |
| `type.labelMedium` | SemiBold (600) | 12sp | 16sp | 0.5sp | Chips de filtro ("Todos", "Activos"), badges |
| `type.labelSmall` | Medium (500) | 10sp | 14sp | 0.5sp | Etiquetas de la barra de navegación inferior |
| `type.codeMedium` | Monospace (400) | 13sp | 18sp | 0.0sp | Bloques de código en chat, rutas de archivos |
| `type.codeSmall` | Monospace (500) | 11sp | 14sp | 0.0sp | Hashes git, tamaños de archivo ("24 KB") |
