package com.example.ui.cat.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActionApprovalRequest
import com.example.model.AgentStatus
import com.example.model.CatAgent
import com.example.model.CatFileItem
import com.example.model.CatNavTab
import com.example.model.CatProject
import com.example.model.ProjectStatus
import com.example.ui.theme.CatAmberContainer
import com.example.ui.theme.CatAmberPrimary
import com.example.ui.theme.CatBorder
import com.example.ui.theme.CatOnAmber
import com.example.ui.theme.CatSurface
import com.example.ui.theme.CatSurfaceElevated
import com.example.ui.theme.CatSurfaceVariant
import com.example.ui.theme.CatTextPrimary
import com.example.ui.theme.CatTextSecondary
import com.example.ui.theme.CatTextTertiary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusApprovalRed
import com.example.ui.theme.StatusWaitingOrange
import com.example.ui.theme.ToyMouseWhite

// =========================================================================
// Top App Bar
// =========================================================================
@Composable
fun CatTopAppBar(
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CatSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo + Brand Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenSettings() }
            ) {
                // Pixel Art Cat Mascot Avatar
                Image(
                    painter = painterResource(id = R.drawable.cat_project_agent_logo),
                    contentDescription = "Logo de CatProjectAgent",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, CatAmberPrimary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Row {
                    Text(
                        text = "CatProject",
                        color = CatTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Agent",
                        color = CatAmberPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right: Notifications Bell + User Avatar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("top_bar_bell")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = CatTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(CatAmberPrimary)
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // User avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CatSurfaceElevated)
                        .border(1.dp, CatBorder, CircleShape)
                        .clickable { onOpenSettings() }
                        .testTag("top_bar_user_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        color = CatTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// =========================================================================
// Bottom Navigation Bar
// =========================================================================
@Composable
fun CatBottomNavigationBar(
    selectedTab: CatNavTab,
    onTabSelected: (CatNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CatSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CatNavTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val icon = when (tab) {
                    CatNavTab.INICIO -> Icons.Default.Home
                    CatNavTab.PROYECTOS -> Icons.Default.Folder
                    CatNavTab.AGENTES -> Icons.Default.People
                    CatNavTab.ARCHIVOS -> Icons.Default.Description
                    CatNavTab.MAS -> Icons.Default.GridView
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CatAmberContainer)
                                .border(1.dp, CatAmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = CatAmberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = CatTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = tab.label,
                        color = if (isSelected) CatAmberPrimary else CatTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// =========================================================================
// Status Pill
// =========================================================================
@Composable
fun CatStatusPill(
    statusText: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = statusText,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =========================================================================
// Project Card (Mockup Style)
// =========================================================================
@Composable
fun CatProjectCard(
    project: CatProject,
    onClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Surface(
        color = CatSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Colored Category Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(project.accentColorHex).copy(alpha = 0.2f))
                        .border(1.dp, Color(project.accentColorHex).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = project.name,
                        tint = Color(project.accentColorHex),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = project.name,
                        color = CatTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.status.label,
                            color = when (project.status) {
                                ProjectStatus.EN_PROGRESO -> CatAmberPrimary
                                ProjectStatus.EN_REVISION -> Color(0xFFA855F7)
                                ProjectStatus.COMPLETADO -> StatusActiveGreen
                                ProjectStatus.EN_PAUSA -> CatTextSecondary
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "  •  ${project.lastActivity}",
                            color = CatTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { expandedMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = CatTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier
                        .background(CatSurfaceElevated)
                        .border(1.dp, CatBorder, RoundedCornerShape(10.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Abrir detalle", color = CatTextPrimary) },
                        onClick = {
                            expandedMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicar", color = CatTextSecondary) },
                        onClick = { expandedMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Archivar", color = StatusApprovalRed) },
                        onClick = { expandedMenu = false }
                    )
                }
            }
        }
    }
}

// =========================================================================
// Agent Card (Mockup Style)
// =========================================================================
@Composable
fun CatAgentCard(
    agent: CatAgent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CatSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("agent_card_${agent.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Agent Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(agent.colorHex).copy(alpha = 0.2f))
                    .border(1.dp, Color(agent.colorHex).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = agent.name,
                    tint = Color(agent.colorHex),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = agent.name,
                        color = CatTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Status Pill
                    val (statusColor, statusIcon) = when (agent.status) {
                        AgentStatus.ACTIVO -> StatusActiveGreen to Icons.Default.CheckCircle
                        AgentStatus.PENSANDO -> Color(0xFF38BDF8) to Icons.Default.Sync
                        AgentStatus.TRABAJANDO -> CatAmberPrimary to Icons.Default.Sync
                        AgentStatus.EN_ESPERA -> StatusWaitingOrange to Icons.Default.HourglassTop
                        AgentStatus.REQUIERE_APROBACION -> StatusApprovalRed to Icons.Default.Warning
                        AgentStatus.COMPLETADO -> StatusActiveGreen to Icons.Default.CheckCircle
                        AgentStatus.PAUSADO -> CatTextSecondary to Icons.Default.PauseCircle
                        AgentStatus.ERROR -> StatusApprovalRed to Icons.Default.Warning
                    }

                    CatStatusPill(
                        statusText = agent.status.label,
                        color = statusColor,
                        icon = statusIcon
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = agent.role,
                    color = CatTextSecondary,
                    fontSize = 12.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${agent.model} • ${agent.provider}",
                    color = CatTextTertiary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// =========================================================================
// File & Folder Row (Mockup Style)
// =========================================================================
@Composable
fun CatFileRow(
    item: CatFileItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CatSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("file_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Folder / File Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isDirectory) CatAmberContainer else CatSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                        contentDescription = item.name,
                        tint = if (item.isDirectory) CatAmberPrimary else CatTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.name,
                        color = CatTextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val subtitle = if (item.isDirectory) {
                        "${item.itemCount ?: 0} elementos"
                    } else {
                        "${item.size ?: "N/D"} • ${item.lastModified}"
                    }

                    Text(
                        text = subtitle,
                        color = CatTextSecondary,
                        fontSize = 11.5.sp
                    )
                }
            }

            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = CatTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// =========================================================================
// Action Approval Dialog / Modal (Section 12.8)
// =========================================================================
@Composable
fun CatActionApprovalDialog(
    request: ActionApprovalRequest,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = CatSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CatAmberPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CatAmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CatAmberPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Aprobación Requerida",
                            color = CatTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Solicitado por: ${request.agentName}",
                            color = CatAmberPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = request.actionTitle,
                    color = CatTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = request.reason,
                    color = CatTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Archivos afectados:",
                    color = CatTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CatSurfaceVariant)
                        .padding(10.dp)
                ) {
                    request.affectedFiles.forEach { file ->
                        Text(
                            text = "• $file",
                            color = CatTextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDeny,
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusApprovalRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusApprovalRed)
                    ) {
                        Text("Rechazar")
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CatAmberPrimary,
                            contentColor = CatOnAmber
                        )
                    ) {
                        Text("Permitir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
