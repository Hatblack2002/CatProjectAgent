package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// CatProjectAgent Official Color System
// High-contrast, dark obsidian palette with warm amber eyes of the cat
// and crisp white of the windup toy mouse.
// =========================================================================

// Base Background & Surfaces
val CatBackground = Color(0xFF0E1117)          // Obsidian dark base
val CatSurface = Color(0xFF161B26)             // Primary cards & bars
val CatSurfaceElevated = Color(0xFF1F2636)     // Elevated dialogs & sheets
val CatSurfaceVariant = Color(0xFF252D3F)      // Inputs, search bars, inactive chips
val CatBorder = Color(0xFF283245)              // Subtle dividers & borders
val CatBorderFocused = Color(0xFFF5A623)       // Focused input border

// Typography Contrast Colors
val CatTextPrimary = Color(0xFFF0F4FC)         // 14.8:1 AAA contrast
val CatTextSecondary = Color(0xFF8E9CAE)       // 5.6:1 AA contrast
val CatTextTertiary = Color(0xFF5C687A)        // Placeholders, disabled text

// Brand Identity: Amber Cat Eyes & White Wind-up Mouse
val CatAmberPrimary = Color(0xFFF5A623)        // Golden amber (New Project, FAB, Highlights)
val CatAmberHover = Color(0xFFFFB74D)          // Interactive hover/light
val CatAmberDark = Color(0xFFD97706)           // Pressed state
val CatAmberContainer = Color(0xFF2B1D04)      // Chip backgrounds & subtle pills
val CatOnAmber = Color(0xFF1A1100)             // High-contrast text on amber (11.2:1 AAA)
val ToyMouseWhite = Color(0xFFF8FAFC)          // Toy mouse body
val ToyMouseSilver = Color(0xFF94A3B8)         // Wind-up key & wheels
val ToyMousePink = Color(0xFFF472B6)           // Subtle mouse ears/nose

// Agent Semantic Coding (Unique identity per role)
val AgentArchitectBlue = Color(0xFF0EA5E9)     // Arquitecto (Structure & blueprint)
val AgentDesignerPurple = Color(0xFFA855F7)    // Diseñador (UI/UX & visual creativity)
val AgentCoderGreen = Color(0xFF10B981)        // Programador (Logic, code execution)
val AgentAnalystOrange = Color(0xFFF59E0B)     // Analista (QA, verification, inspection)
val AgentResearcherCyan = Color(0xFF06B6D4)    // Investigador (Discovery & references)
val AgentCustomPink = Color(0xFFEC4899)        // Personalizado (Custom user agents)

// System Status Indicators
val StatusActiveGreen = Color(0xFF10B981)
val StatusThinkingBlue = Color(0xFF38BDF8)
val StatusWorkingAmber = Color(0xFFF5A623)
val StatusWaitingOrange = Color(0xFFF59E0B)
val StatusApprovalRed = Color(0xFFEF4444)
val StatusCompletedGreen = Color(0xFF10B981)
val StatusErrorRed = Color(0xFFEF4444)
val StatusPausedGray = Color(0xFF6B7280)

// Aliases for backwards compatibility if needed
val BackgroundDark = CatBackground
val SurfaceDark = CatSurface
val SurfaceVariantDark = CatSurfaceVariant
val CardBorderDark = CatBorder
val TextPrimaryDark = CatTextPrimary
val TextSecondaryDark = CatTextSecondary
val AccentOrange = CatAmberPrimary
val ColorExito = StatusActiveGreen
val ColorError = StatusErrorRed
val ColorAviso = StatusWaitingOrange
val BordesSutiles = CatBorder
val FondoHeader = CatSurface
val FondoToolbar = CatSurface
val FondoMenuContextual = CatSurfaceElevated
val TextoPrimario = CatTextPrimary
val TextoSecundario = CatTextSecondary
val StatusOnlineGreen = StatusActiveGreen
val BackgroundLight = Color(0xFFF1F5F9)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFE2E8F0)
val CardBorderLight = Color(0xFFCBD5E1)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
