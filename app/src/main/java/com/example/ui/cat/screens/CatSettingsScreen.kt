package com.example.ui.cat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ProviderRegistry
import com.example.ui.theme.CatAmberPrimary
import com.example.ui.theme.CatBackground
import com.example.ui.theme.CatBorder
import com.example.ui.theme.CatSurface
import com.example.ui.theme.CatSurfaceElevated
import com.example.ui.theme.CatSurfaceVariant
import com.example.ui.theme.CatTextPrimary
import com.example.ui.theme.CatTextSecondary
import com.example.viewmodel.BrainEditorState
import com.example.viewmodel.BrainsState
import com.example.viewmodel.SetupState

/** Acciones del editor de cerebros, agrupadas por slot. */
data class BrainActions(
    val edit: (String) -> Unit,
    val cancel: (String) -> Unit,
    val setProvider: (String, String) -> Unit,
    val setKey: (String, String) -> Unit,
    val setModel: (String, String) -> Unit,
    val save: (String) -> Unit,
    val test: (String) -> Unit,
    val resetToPrincipal: (String) -> Unit
)

@Composable
fun CatSettingsScreen(
    onBackClick: () -> Unit,
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean = true,
    setup: SetupState,
    brains: BrainsState,
    brainActions: BrainActions,
    onPickWorkspace: () -> Unit,
    onClearWorkspace: () -> Unit,
    onPrepareEnvironment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CatBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = CatTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Configuración",
                color = CatTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Preferencias",
                    color = CatTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = CatSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = "Tema",
                        subtitle = if (isDarkMode) "Oscuro" else "Claro",
                        onClick = onToggleDarkMode
                    )
                }
            }
            item {
                Text(
                    text = "Cerebros de IA",
                    color = CatTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (brains.summary.isNotBlank()) brains.summary
                    else "un solo cerebro puede atender a todos los agentes",
                    color = CatTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    brains.slots.forEach { slot ->
                        BrainCard(slot = slot, actions = brainActions)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Todos los cerebros usan tool calling real: los agentes leen, " +
                        "escriben, ejecutan comandos y compilan en tu dispositivo, no simulan nada.",
                    color = CatTextSecondary,
                    fontSize = 11.sp
                )
            }

            item {
                Text(
                    text = "Workspace",
                    color = CatTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = CatSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = CatTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Carpeta del dispositivo",
                                    color = CatTextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (setup.workspaceConfigured) setup.workspaceLabel
                                    else "workspace privado de la app (elige una carpeta para ver los archivos en tu explorador)",
                                    color = CatTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row {
                            Surface(
                                color = CatSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CatAmberPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onPickWorkspace() }
                            ) {
                                Text(
                                    text = "Elegir carpeta",
                                    color = CatAmberPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                color = CatSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onClearWorkspace() }
                            ) {
                                Text(
                                    text = "Usar privado",
                                    color = CatTextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Entorno Linux",
                    color = CatTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = CatSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = CatTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ubuntu 24.04 + PRoot (motor interno)",
                                    color = CatTextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (setup.environmentReady) "listo: los agentes ejecutan comandos reales"
                                    else "no instalado: ${setup.environmentDetail}",
                                    color = CatTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (!setup.environmentReady) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                color = CatSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CatAmberPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPrepareEnvironment() }
                            ) {
                                Text(
                                    text = "Preparar entorno (~200 MB, con verificación SHA256)",
                                    color = CatAmberPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Seguridad",
                    color = CatTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = CatSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CatTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Aprobaciones de acciones sensibles",
                                color = CatTextPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Eliminar archivos, instalar dependencias y ejecutar comandos siempre piden tu confirmación.",
                                color = CatTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BrainCard(slot: BrainEditorState, actions: BrainActions) {
    Surface(
        color = CatSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (slot.hasKey) CatAmberPrimary.copy(alpha = 0.35f) else CatBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = if (slot.hasKey) CatAmberPrimary else CatTextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.title,
                        color = CatTextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = slot.subtitle,
                        color = CatTextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (!slot.editing) {
                    Surface(
                        color = CatSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                        modifier = Modifier.clickable { actions.edit(slot.slot) }
                    ) {
                        Text(
                            text = if (slot.hasKey) "Cambiar" else "Configurar",
                            color = CatAmberPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (slot.editing) {
                BrainEditor(slot = slot, actions = actions)
            }
        }
    }
}

@Composable
private fun BrainEditor(slot: BrainEditorState, actions: BrainActions) {
    val descriptor = ProviderRegistry.descriptor(slot.providerId)
    var showKey by remember(slot.slot) { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Proveedor",
        color = CatTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Selector de proveedor: chips en filas de 2, sin APIs experimentales.
    val providers = ProviderRegistry.descriptors
    providers.chunked(2).forEach { rowProviders ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            rowProviders.forEach { provider ->
                val selected = provider.id == slot.providerId
                Surface(
                    color = if (selected) CatAmberPrimary.copy(alpha = 0.15f) else CatSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selected) CatAmberPrimary else CatBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { actions.setProvider(slot.slot, provider.id) }
                ) {
                    Text(
                        text = if (provider.isPrimaryBrain) "${provider.displayName} ★" else provider.displayName,
                        color = if (selected) CatAmberPrimary else CatTextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
            if (rowProviders.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }

    Spacer(modifier = Modifier.height(4.dp))

    OutlinedTextField(
        value = slot.apiKeyDraft,
        onValueChange = { actions.setKey(slot.slot, it) },
        placeholder = {
            Text("Tu API key de ${descriptor?.displayName.orEmpty()}", color = CatTextSecondary, fontSize = 13.sp)
        },
        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { showKey = !showKey }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (showKey) "Ocultar clave" else "Mostrar clave",
                    tint = CatTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CatSurface,
            unfocusedContainerColor = CatSurface,
            focusedBorderColor = CatAmberPrimary,
            unfocusedBorderColor = CatBorder,
            focusedTextColor = CatTextPrimary,
            unfocusedTextColor = CatTextPrimary,
            cursorColor = CatAmberPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Text(
        text = descriptor?.keyHint.orEmpty(),
        color = CatTextSecondary,
        fontSize = 10.5.sp,
        modifier = Modifier.padding(top = 4.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = slot.modelDraft,
        onValueChange = { actions.setModel(slot.slot, it) },
        placeholder = {
            Text("Modelo (por defecto: ${descriptor?.defaultModel.orEmpty()})", color = CatTextSecondary, fontSize = 13.sp)
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CatSurface,
            unfocusedContainerColor = CatSurface,
            focusedBorderColor = CatAmberPrimary,
            unfocusedBorderColor = CatBorder,
            focusedTextColor = CatTextPrimary,
            unfocusedTextColor = CatTextPrimary,
            cursorColor = CatAmberPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Text(
        text = "Modelos reales de ${descriptor?.displayName.orEmpty()}: " +
            descriptor?.models?.joinToString(", ").orEmpty(),
        color = CatTextSecondary,
        fontSize = 10.5.sp,
        modifier = Modifier.padding(top = 4.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditorButton(
            text = if (slot.testing) "Probando…" else "Probar",
            highlighted = false,
            enabled = !slot.testing,
            modifier = Modifier.weight(1f),
            onClick = { actions.test(slot.slot) }
        )
        EditorButton(
            text = "Guardar",
            highlighted = true,
            enabled = slot.apiKeyDraft.isNotBlank(),
            modifier = Modifier.weight(1f),
            onClick = { actions.save(slot.slot) }
        )
        EditorButton(
            text = "Cancelar",
            highlighted = false,
            enabled = true,
            modifier = Modifier.weight(1f),
            onClick = { actions.cancel(slot.slot) }
        )
    }

    if (!slot.isPrincipal && slot.hasKey) {
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = CatSurfaceVariant,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { actions.resetToPrincipal(slot.slot) }
        ) {
            Text(
                text = "Quitar cerebro propio y usar el principal",
                color = CatTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
            )
        }
    }

    slot.testDetail?.let { detail ->
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (slot.testOk == true) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (slot.testOk == true) CatAmberPrimary else CatTextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = detail,
                color = if (slot.testOk == true) CatAmberPrimary else CatTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun EditorButton(
    text: String,
    highlighted: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = when {
            !enabled -> CatSurfaceVariant
            highlighted -> CatAmberPrimary.copy(alpha = 0.18f)
            else -> CatSurfaceElevated
        },
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (highlighted) CatAmberPrimary.copy(alpha = 0.6f) else CatBorder
        ),
        modifier = modifier.clickable(enabled = enabled) { onClick() }
    ) {
        Text(
            text = text,
            color = if (enabled && highlighted) CatAmberPrimary else CatTextSecondary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CatTextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    color = CatTextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = CatTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = CatTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
