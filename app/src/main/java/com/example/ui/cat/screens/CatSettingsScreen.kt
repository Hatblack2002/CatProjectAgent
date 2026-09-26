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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CatAmberPrimary
import com.example.ui.theme.CatBackground
import com.example.ui.theme.CatBorder
import com.example.ui.theme.CatSurface
import com.example.ui.theme.CatSurfaceElevated
import com.example.ui.theme.CatSurfaceVariant
import com.example.ui.theme.CatTextPrimary
import com.example.ui.theme.CatTextSecondary
import com.example.viewmodel.SetupState

@Composable
fun CatSettingsScreen(
    onBackClick: () -> Unit,
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean = true,
    setup: SetupState,
    onPickWorkspace: () -> Unit,
    onClearWorkspace: () -> Unit,
    onPrepareEnvironment: () -> Unit,
    onTestProvider: () -> Unit,
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
                    text = "Proveedor de IA",
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
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = CatTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Google Gemini (gemini-2.5-flash)",
                                    color = CatTextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when {
                                        setup.checking -> "comprobando conexión real…"
                                        setup.providerConfigured -> "configurado · ${setup.providerDetail}"
                                        else -> "sin GEMINI_API_KEY: define la clave en el .env del proyecto y recompila"
                                    },
                                    color = if (setup.providerConfigured) CatTextSecondary else CatAmberPrimary,
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(onClick = onTestProvider, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Probar conexión",
                                    tint = CatTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Capacidades reales: texto, tool calling, streaming, visión, entrada de archivos.",
                            color = CatTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
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
