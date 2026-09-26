package com.example.ui.cat

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CatNavTab
import com.example.ui.cat.components.CatActionApprovalDialog
import com.example.ui.cat.components.CatBottomNavigationBar
import com.example.ui.cat.components.CatTopAppBar
import com.example.ui.cat.screens.CatAgentChatScreen
import com.example.ui.cat.screens.CatAgentsScreen
import com.example.ui.cat.screens.CatDashboardScreen
import com.example.ui.cat.screens.CatFilesScreen
import com.example.ui.cat.screens.CatMoreScreen
import com.example.ui.cat.screens.CatNewProjectScreen
import com.example.ui.cat.screens.CatProjectDetailScreen
import com.example.ui.cat.screens.CatProjectsScreen
import com.example.ui.cat.screens.BrainActions
import com.example.ui.cat.screens.CatSettingsScreen
import com.example.ui.cat.screens.CatSplashScreen
import com.example.ui.theme.CatBackground
import com.example.viewmodel.CatProjectViewModel
import com.example.viewmodel.CatScreenRoute

@Composable
fun CatProjectAgentApp(
    viewModel: CatProjectViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val workspacePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { viewModel.onWorkspaceTreePicked(it) }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Android Native BackHandler
    BackHandler(enabled = uiState.currentRoute != CatScreenRoute.MAIN_TABS || uiState.selectedTab != CatNavTab.INICIO) {
        when {
            uiState.currentRoute == CatScreenRoute.NEW_PROJECT ||
            uiState.currentRoute == CatScreenRoute.PROJECT_DETAIL ||
            uiState.currentRoute == CatScreenRoute.AGENT_CHAT ||
            uiState.currentRoute == CatScreenRoute.SETTINGS -> {
                viewModel.navigateTo(CatScreenRoute.MAIN_TABS)
            }
            uiState.selectedTab != CatNavTab.INICIO -> {
                viewModel.selectTab(CatNavTab.INICIO)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        containerColor = CatBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (uiState.currentRoute == CatScreenRoute.MAIN_TABS && uiState.selectedTab == CatNavTab.INICIO) {
                CatTopAppBar(
                    onOpenNotifications = { viewModel.requestEnvironmentSetup() },
                    onOpenSettings = { viewModel.navigateTo(CatScreenRoute.SETTINGS) }
                )
            }
        },
        bottomBar = {
            if (uiState.currentRoute == CatScreenRoute.MAIN_TABS) {
                CatBottomNavigationBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = uiState.currentRoute,
                label = "cat_screen_transition"
            ) { route ->
                when (route) {
                    CatScreenRoute.SPLASH -> {
                        CatSplashScreen()
                    }
                    CatScreenRoute.MAIN_TABS -> {
                        when (uiState.selectedTab) {
                            CatNavTab.INICIO -> {
                                CatDashboardScreen(
                                    projects = uiState.projects,
                                    onNewProjectClick = { viewModel.navigateTo(CatScreenRoute.NEW_PROJECT) },
                                    onProjectClick = { projId -> viewModel.openProjectDetail(projId) },
                                    onViewAllProjects = { viewModel.selectTab(CatNavTab.PROYECTOS) }
                                )
                            }
                            CatNavTab.PROYECTOS -> {
                                CatProjectsScreen(
                                    projects = uiState.projects,
                                    selectedFilter = uiState.projectFilter,
                                    onFilterChange = { viewModel.setProjectFilter(it) },
                                    searchQuery = uiState.projectSearchQuery,
                                    onSearchChange = { viewModel.setProjectSearchQuery(it) },
                                    onProjectClick = { projId -> viewModel.openProjectDetail(projId) },
                                    onNewProjectClick = { viewModel.navigateTo(CatScreenRoute.NEW_PROJECT) }
                                )
                            }
                            CatNavTab.AGENTES -> {
                                CatAgentsScreen(
                                    agents = uiState.agents,
                                    onAgentClick = { agentId -> viewModel.openAgentChat(agentId) },
                                    onOpenSettings = { viewModel.navigateTo(CatScreenRoute.SETTINGS) }
                                )
                            }
                            CatNavTab.ARCHIVOS -> {
                                CatFilesScreen(
                                    files = uiState.files,
                                    selectedFilter = uiState.fileFilter,
                                    onFilterChange = { viewModel.setFileFilter(it) },
                                    searchQuery = uiState.fileSearchQuery,
                                    onSearchChange = { viewModel.setFileSearchQuery(it) },
                                    onFileClick = { viewModel.openFilePreview(it) },
                                    onAddFileClick = { viewModel.requestEnvironmentSetup() }
                                )
                            }
                            CatNavTab.MAS -> {
                                CatMoreScreen(
                                    onOpenSettings = { viewModel.navigateTo(CatScreenRoute.SETTINGS) }
                                )
                            }
                        }
                    }
                    CatScreenRoute.NEW_PROJECT -> {
                        CatNewProjectScreen(
                            projectName = uiState.newProjectName,
                            onProjectNameChange = { viewModel.onNewProjectNameChange(it) },
                            projectDescription = uiState.newProjectDescription,
                            onProjectDescriptionChange = { viewModel.onNewProjectDescriptionChange(it) },
                            selectedCategory = uiState.selectedCategoryTemplate,
                            onCategorySelect = { viewModel.onCategoryTemplateSelect(it) },
                            selectedTemplate = uiState.selectedTemplateName,
                            onTemplateSelect = { viewModel.onTemplateSelect(it) },
                            onCreateProjectClick = { viewModel.createProject() },
                            onCloseClick = { viewModel.navigateTo(CatScreenRoute.MAIN_TABS) }
                        )
                    }
                    CatScreenRoute.PROJECT_DETAIL -> {
                        val selectedProj = uiState.projects.find { it.id == uiState.selectedProjectId }
                            ?: uiState.projects.firstOrNull()
                        CatProjectDetailScreen(
                            project = selectedProj,
                            openIssues = uiState.openIssues,
                            onBackClick = { viewModel.navigateTo(CatScreenRoute.MAIN_TABS) },
                            onRequestActionApproval = { viewModel.requestEnvironmentSetup() },
                            onRunCycle = { goal -> viewModel.runAgentCycle(goal) },
                            onOpenChatWithAgent = { agentId -> viewModel.openAgentChat(agentId) }
                        )
                    }
                    CatScreenRoute.AGENT_CHAT -> {
                        val selectedAgent = uiState.agents.find { it.id == uiState.selectedAgentId }
                            ?: uiState.agents.firstOrNull()
                        CatAgentChatScreen(
                            agent = selectedAgent,
                            messages = uiState.chatMessages,
                            inputText = uiState.currentChatInput,
                            onInputChange = { viewModel.onChatInputChange(it) },
                            onSendMessage = { viewModel.sendChatMessage(it) },
                            onQuickReplyClick = { viewModel.sendChatMessage(it) },
                            onBackClick = { viewModel.navigateTo(CatScreenRoute.MAIN_TABS) },
                            isAgentTyping = uiState.isAgentTyping
                        )
                    }
                    CatScreenRoute.SETTINGS -> {
                        CatSettingsScreen(
                            onBackClick = { viewModel.navigateTo(CatScreenRoute.MAIN_TABS) },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            isDarkMode = uiState.isDarkMode,
                            setup = uiState.setup,
                            brains = uiState.brains,
                            brainActions = BrainActions(
                                edit = viewModel::editBrain,
                                cancel = viewModel::cancelBrainEdit,
                                setProvider = viewModel::setBrainProvider,
                                setKey = viewModel::setBrainKey,
                                setModel = viewModel::setBrainModel,
                                save = viewModel::saveBrain,
                                test = viewModel::testBrain,
                                resetToPrincipal = viewModel::resetBrainToPrincipal
                            ),
                            onPickWorkspace = { workspacePicker.launch(null) },
                            onClearWorkspace = { viewModel.clearWorkspace() },
                            onPrepareEnvironment = { viewModel.requestEnvironmentSetup() }
                        )
                    }
                }
            }
        }

        // Action Approval Sheet Modal (Section 12.8 in user specification)
        if (uiState.showApprovalSheet && uiState.pendingApproval != null) {
            CatActionApprovalDialog(
                request = uiState.pendingApproval!!,
                onApprove = { viewModel.approvePendingAction() },
                onDeny = { viewModel.denyPendingAction() },
                onDismiss = { viewModel.dismissApprovalSheet() }
            )
        }

        if (uiState.filePreviewName != null && uiState.filePreviewContent != null) {
            CatFilePreviewDialog(
                name = uiState.filePreviewName!!,
                content = uiState.filePreviewContent!!,
                onClose = { viewModel.closeFilePreview() }
            )
        }
    }
}

@Composable
private fun CatFilePreviewDialog(
    name: String,
    content: String,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(text = name, fontSize = 16.sp) },
        text = {
            Text(
                text = content,
                fontSize = 12.sp,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("Cerrar") }
        }
    )
}
