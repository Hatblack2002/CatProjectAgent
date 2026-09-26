# CatProjectAgent — Arquitectura y Flujos de Navegación

## 1. Modelo de Navegación
CatProjectAgent utiliza un modelo de navegación híbrido moderno:
1. **Navegación Primaria por Pestañas (Persistent Bottom Bar):**
   * Controla las 5 secciones de primer nivel: `Inicio`, `Proyectos`, `Agentes`, `Archivos`, `Más`.
   * Preserva el estado de cada pestaña mediante State Hoisting en el ViewModel central.
2. **Navegación Jerárquica y Modal (Stack & Bottom Sheets):**
   * Pantallas de detalle (`ProjectDetailScreen`, `AgentChatScreen`, `SettingsScreen`) se apilan con animación de transición horizontal o vertical según corresponda.
   * Flujos de creación rápida (`NewProjectSheet`) y de aprobación (`ActionApprovalSheet`) utilizan Bottom Sheets modales de media o pantalla completa.

---

## 2. Mapa de Rutas y Transiciones

```
[SplashScreen] 
      │ (Crossfade 400ms tras inicialización)
      ▼
┌─────────────────────────────────────────────────────────────┐
│                       MAIN SCAFFOLD                         │
│                                                             │
│   ┌──────────┐  ┌───────────┐  ┌─────────┐  ┌────────────┐  │
│   │  Inicio  │  │ Proyectos │  │ Agentes │  │  Archivos  │  │
│   └────┬─────┘  └─────┬─────┘  └────┬────┘  └─────┬──────┘  │
└────────┼──────────────┼─────────────┼─────────────┼─────────┘
         │              │             │             │
         ├──────────────┼─────────────┼─────────────┤
         │              ▼             ▼             │
         │     [ProjectDetail]   [AgentChat]        │
         │              │             │             │
         │              └──────┬──────┘             │
         │                     │                    │
         ▼                     ▼                    ▼
[NewProjectSheet]      [ActionApprovalModal]   [FileViewer]
```

---

## 3. Manejo de BackHandler (Android Native)
* **Pestañas Raíz:** Si el usuario presiona el botón "Atrás" físico o gesto de sistema en cualquier pestaña secundaria (`Proyectos`, `Agentes`, `Archivos`, `Más`), el sistema redirige primero a la pestaña raíz `Inicio`. Si se presiona nuevamente en `Inicio`, se minimiza la aplicación.
* **Sub-pantallas y Modales:** En `ProjectDetailScreen`, `AgentChatScreen` o `NewProjectSheet`, `BackHandler` cierra la vista actual y retorna a la pantalla anterior sin pérdida de estado.
