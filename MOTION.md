# CatProjectAgent — Motion Design & Sistema de Animaciones

## 1. Principios del Movimiento
1. **Intencional y Eficiente:** Las animaciones proporcionan feedback de estado y contexto espacial sin retrasar la productividad del desarrollador.
2. **Personalidad Lúdica pero Mesurada:** La animación del gato persiguiendo al ratón de cuerda es la pieza insignia de la identidad de CatProjectAgent; el resto de la interfaz mantiene transiciones limpias y fluidas basadas en Material 3.

---

## 2. La Animación Insignia: El Gato y el Ratón de Cuerda

### 2.1 Concepto y Coreografía
* **El Ratón Blanco de Juguete:**
  * Corre velozmente hacia adelante con sus ruedas girando.
  * La llave mecánica en su espalda gira a velocidad constante (giro continuo de 360° cada 800ms).
  * Movimiento ligeramente saltarín propio de un mecanismo de cuerda ("wind-up toy hop").
* **El Gato Negro Pixel Art:**
  * Corre inmediatamente detrás con pasos elásticos y cola oscilante.
  * Ojos ámbar bien abiertos con expresión curiosa y juguetona, mirando fijamente la llave del ratón.
  * Estira una pata de vez en cuando como intentando darle otra vuelta a la cuerda.
* **Loop Infinito:**
  * La animación funciona en bucle continuo de 2.4 segundos sin necesidad de que el gato alcance o atrape al ratón.
  * Simboliza que el sistema se mantiene en perpetuo movimiento y refinamiento creativo.

### 2.2 Especificación Técnica en Android Jetpack Compose
* **Implementación:** `Canvas` o spritesheet pixel-art renderizada con `rememberInfiniteTransition()`.
* **Interpolador de Giro de Llave:** `LinearEasing` (rotación 0° a 360° cada 750ms).
* **Interpolador de Desplazamiento:** `FastOutSlowInEasing` para las zancadas y el rebote del muelle.

---

## 3. Tokens de Tiempo y Curvas de Aceleración M3

| Token de Movimiento | Duración | Curva (Easing) | Aplicación |
| :--- | :--- | :--- | :--- |
| `motion.durationShort` | 150ms | `FastOutSlowInEasing` | Pulsación de botones, toggle de chips |
| `motion.durationMedium`| 300ms | `CubicBezier(0.2f, 0f, 0f, 1f)`| Apertura de Bottom Sheet, cambio de pestaña |
| `motion.durationLong` | 500ms | `EmphasizedDecelerateEasing`| Entrada de tarjetas de proyecto y diálogos |
| `motion.durationSplash`| 2200ms| `LinearOutSlowInEasing` | Carga de splash y bienvenida |
