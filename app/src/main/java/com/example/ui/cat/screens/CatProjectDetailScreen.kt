package com.example.ui.cat.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CatProject
import com.example.model.ProjectTask
import com.example.model.TaskStatus
import com.example.ui.cat.components.CatStatusPill
import com.example.ui.theme.CatAmberPrimary
import com.example.ui.theme.CatBackground
import com.example.ui.theme.CatBorder
import com.example.ui.theme.CatOnAmber
import com.example.ui.theme.CatSurface
import com.example.ui.theme.CatSurfaceElevated
import com.example.ui.theme.CatSurfaceVariant
import com.example.ui.theme.CatTextPrimary
import com.example.ui.theme.CatTextSecondary
import com.example.ui.theme.CatTextTertiary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusWaitingOrange

@Composable
fun CatProjectDetailScreen(
    project: CatProject?,
    openIssues: Int,
    onBackClick: () -> Unit,
    onRequestActionApproval: () -> Unit,
    onRunCycle: (String) -> Unit,
    onOpenChatWithAgent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val projName = project?.name ?: "Proyecto no encontrado"
    val progress = project?.progressPercent ?: 0
    var selectedTab by remember { mutableStateOf("Resumen") }
    val tabs = listOf("Resumen", "Tareas", "Archivos", "Config")

    val tasks = project?.tasks.orEmpty()
    var showCycleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CatBackground)
    ) {
        // Top bar (Exact Mockup Match)
        Surface(
            color = CatSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = CatTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = projName,
                        color = CatTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    CatStatusPill(
                        statusText = project?.status?.label ?: "Sin estado",
                        color = if (project != null) StatusActiveGreen else CatTextTertiary,
                        icon = Icons.Default.CheckCircle
                    )
                }

                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = CatTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Internal Navigation Tabs (Resumen, Tareas, Archivos, Config)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CatSurface)
                .border(1.dp, CatBorder)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Column(
                    modifier = Modifier
                        .clickable { selectedTab = tab }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) CatAmberPrimary else CatTextSecondary,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(2.5.dp)
                                .background(CatAmberPrimary, RoundedCornerShape(2.dp))
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.5.dp))
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))

                // Project Hero Banner with Mascot Artwork (Exact Mockup Match)
                Surface(
                    color = CatSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Project Mascot Image
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CatAmberPrimary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cat_project_agent_logo),
                                contentDescription = "Logo proyecto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = projName,
                                color = CatTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = project?.description ?: "Este proyecto no tiene descripción todavía.",
                                color = CatTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 3
                            )
                        }
                    }
                }
            }

            // Tarjeta de progreso real (calculado desde las tareas en Room)
            item {
                Surface(
                    color = CatSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progreso general",
                                color = CatTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "$progress%",
                                color = CatAmberPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CatAmberPrimary,
                            trackColor = CatSurfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // Tareas recientes Section (Exact Mockup Match)
            item {
                Text(
                    text = "Tareas recientes",
                    color = CatTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (tasks.isEmpty()) {
                item {
                    Surface(
                        color = CatSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No hay tareas todavía. Ejecuta el ciclo multiagente o pídele al Arquitecto un plan desde el chat: las tareas se crean con herramientas reales.",
                            color = CatTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            items(tasks) { task ->
                Surface(
                    color = CatSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icon according to status
                            val (icon, tint) = when (task.status) {
                                TaskStatus.COMPLETADA -> Icons.Default.CheckCircle to StatusActiveGreen
                                TaskStatus.EN_PROGRESO -> Icons.Default.Sync to Color(0xFFA855F7)
                                TaskStatus.PENDIENTE -> Icons.Default.RadioButtonUnchecked to CatTextTertiary
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = task.title,
                                    color = CatTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Asignado a: ${task.assignedAgent}",
                                    color = CatTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = task.status.label,
                            color = when (task.status) {
                                TaskStatus.COMPLETADA -> StatusActiveGreen
                                TaskStatus.EN_PROGRESO -> Color(0xFFA855F7)
                                TaskStatus.PENDIENTE -> CatTextSecondary
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Yellow "Ver detalles" Button (Exact Mockup Match)
                Button(
                    onClick = { onOpenChatWithAgent("agent_architect") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatAmberPrimary,
                        contentColor = CatOnAmber
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Ver detalles & Conversar con Agentes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (openIssues > 0) {
                    Surface(
                        color = CatSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusWaitingOrange.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$openIssues issue(s) abiertos registrados por el Analista. Consulta el chat para la evidencia.",
                            color = CatTextSecondary,
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = { showCycleDialog = true },
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatAmberPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Ejecutar ciclo multiagente real",
                        color = CatAmberPrimary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onRequestActionApproval,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CatAmberPrimary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Preparar entorno Linux (Ubuntu)",
                        color = CatAmberPrimary.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showCycleDialog) {
        val goalInput = remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCycleDialog = false },
            title = { Text("Ciclo multiagente real") },
            text = {
                Column {
                    Text(
                        "Arquitecto → Programador → Analista sobre el proyecto real. Los pasos sensibles pedirán tu aprobación.",
                        color = CatTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = goalInput.value,
                        onValueChange = { goalInput.value = it },
                        label = { Text("Objetivo del ciclo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showCycleDialog = false
                        onRunCycle(goalInput.value.ifBlank { "implementar el objetivo del proyecto" })
                    }
                ) { Text("Ejecutar") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCycleDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
