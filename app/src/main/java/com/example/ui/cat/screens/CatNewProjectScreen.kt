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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectCategory
import com.example.ui.theme.CatAmberPrimary
import com.example.ui.theme.CatBackground
import com.example.ui.theme.CatBorder
import com.example.ui.theme.CatOnAmber
import com.example.ui.theme.CatSurface
import com.example.ui.theme.CatSurfaceElevated
import com.example.ui.theme.CatTextPrimary
import com.example.ui.theme.CatTextSecondary
import com.example.ui.theme.CatTextTertiary

data class TemplateItem(
    val title: String,
    val description: String,
    val category: ProjectCategory
)

@Composable
fun CatNewProjectScreen(
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    projectDescription: String,
    onProjectDescriptionChange: (String) -> Unit,
    selectedCategory: ProjectCategory,
    onCategorySelect: (ProjectCategory) -> Unit,
    selectedTemplate: String,
    onTemplateSelect: (String) -> Unit,
    onCreateProjectClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryList = ProjectCategory.values().toList()

    val templates = listOf(
        TemplateItem("Proyecto desde cero", "Comienza con una base limpia.", ProjectCategory.TODO),
        TemplateItem("App móvil", "Aplicación para Android/iOS.", ProjectCategory.APPS),
        TemplateItem("Sitio web", "Landing page o sitio completo.", ProjectCategory.WEB),
        TemplateItem("Juego 2D", "Con motor de juego integrado.", ProjectCategory.JUEGOS),
        TemplateItem("API / Backend", "Servicio y base de datos.", ProjectCategory.OTROS)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CatBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top bar with Close "X" (Exact Mockup Match)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = CatTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Nuevo proyecto",
                    color = CatTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onCloseClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = CatTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Nombre del proyecto",
                    color = CatTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = onProjectNameChange,
                    placeholder = {
                        Text("Ej. App de mensajería", color = CatTextTertiary, fontSize = 14.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CatSurface,
                        unfocusedContainerColor = CatSurface,
                        focusedBorderColor = CatAmberPrimary,
                        unfocusedBorderColor = CatBorder,
                        focusedTextColor = CatTextPrimary,
                        unfocusedTextColor = CatTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_name_input")
                )
            }

            item {
                Text(
                    text = "Descripción (opcional)",
                    color = CatTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = projectDescription,
                    onValueChange = onProjectDescriptionChange,
                    placeholder = {
                        Text("Describe brevemente tu proyecto...", color = CatTextTertiary, fontSize = 14.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CatSurface,
                        unfocusedContainerColor = CatSurface,
                        focusedBorderColor = CatAmberPrimary,
                        unfocusedBorderColor = CatBorder,
                        focusedTextColor = CatTextPrimary,
                        unfocusedTextColor = CatTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_desc_input")
                )
            }

            item {
                Text(
                    text = "Selecciona una plantilla",
                    color = CatTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chips (Todo, Apps, Web, Juegos, Otros)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryList) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) CatAmberPrimary else CatSurface)
                                .border(1.dp, if (isSelected) CatAmberPrimary else CatBorder, RoundedCornerShape(20.dp))
                                .clickable { onCategorySelect(category) }
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.label,
                                color = if (isSelected) CatOnAmber else CatTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Template cards (Exact Mockup Match)
            items(templates) { template ->
                val isSelected = selectedTemplate == template.title
                Surface(
                    color = if (isSelected) CatSurfaceElevated else CatSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CatAmberPrimary else CatBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTemplateSelect(template.title) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CatSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isSelected) CatAmberPrimary else CatTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = template.title,
                                    color = CatTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = template.description,
                                    color = CatTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = if (isSelected) CatAmberPrimary else CatTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Big Bottom Yellow Button (Exact Mockup Match: "Crear proyecto")
        Button(
            onClick = onCreateProjectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = CatAmberPrimary,
                contentColor = CatOnAmber
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("create_project_button")
        ) {
            Text(
                text = "Crear proyecto",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
