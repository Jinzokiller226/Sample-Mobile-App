package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.PatientEntity
import com.example.ui.AuthState
import com.example.ui.AuthViewModel
import com.example.ui.ClinicTab
import com.example.ui.ClinicViewModel
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.ClinicManagerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ClinicViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClinicManagerTheme {
                val authState by authViewModel.authState.collectAsState()

                when (authState) {
                    is AuthState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is AuthState.Authenticated -> {
                        ClinicManagerApp(viewModel = viewModel, authViewModel = authViewModel)
                    }
                    is AuthState.Unauthenticated -> {
                        var showRegister by remember { mutableStateOf(false) }
                        if (showRegister) {
                            RegisterScreen(authViewModel = authViewModel, onLoginClick = { showRegister = false })
                        } else {
                            LoginScreen(authViewModel = authViewModel, onRegisterClick = { showRegister = true })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicManagerApp(viewModel: ClinicViewModel, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val patientVisits by viewModel.selectedPatientVisits.collectAsState()
    val exportedSql by viewModel.exportedSql.collectAsState()

    var showAddPatientDialog by remember { mutableStateOf(false) }
    var showAddInventoryDialog by remember { mutableStateOf(false) }
    var patientForVisit by remember { mutableStateOf<PatientEntity?>(null) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bright Sight Optical Clinic",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == ClinicTab.DASHBOARD,
                    onClick = { viewModel.selectTab(ClinicTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == ClinicTab.PATIENTS,
                    onClick = { viewModel.selectTab(ClinicTab.PATIENTS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Patients") },
                    label = { Text("Patients") },
                    modifier = Modifier.testTag("nav_tab_patients")
                )
                NavigationBarItem(
                    selected = selectedTab == ClinicTab.INVENTORY,
                    onClick = { viewModel.selectTab(ClinicTab.INVENTORY) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    modifier = Modifier.testTag("nav_tab_inventory")
                )
                NavigationBarItem(
                    selected = selectedTab == ClinicTab.ACCOUNT,
                    onClick = { viewModel.selectTab(ClinicTab.ACCOUNT) },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Account") },
                    label = { Text("Account") },
                    modifier = Modifier.testTag("nav_tab_account")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                ClinicTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onAddPatientClick = { showAddPatientDialog = true },
                    onAddInventoryClick = { showAddInventoryDialog = true },
                    onExportSqlClick = { viewModel.generateSqlScript() }
                )
                ClinicTab.PATIENTS -> PatientsScreen(
                    viewModel = viewModel,
                    onAddPatientClick = { showAddPatientDialog = true },
                    onPatientClick = { patient -> viewModel.selectPatient(patient) }
                )
                ClinicTab.INVENTORY -> InventoryScreen(
                    viewModel = viewModel,
                    onAddInventoryClick = { showAddInventoryDialog = true }
                )
                ClinicTab.MYSQL_DB -> MySqlDatabaseScreen(
                    viewModel = viewModel,
                    onExportSqlClick = { viewModel.generateSqlScript() }
                )
                ClinicTab.ACCOUNT -> AccountScreen(
                    authViewModel = authViewModel
                )
            }


            // Dialog Modals
            if (showAddPatientDialog) {
                AddPatientDialog(
                    onDismiss = { showAddPatientDialog = false },
                    onConfirm = { name, age, gender, phone, email, address, allergies, bloodType, history ->
                        viewModel.addPatient(name, age, gender, phone, email, address, allergies, bloodType, history)
                    }
                )
            }

            if (showAddInventoryDialog) {
                AddInventoryDialog(
                    onDismiss = { showAddInventoryDialog = false },
                    onConfirm = { name, category, qty, reorder, unit, cost, price, expiry, supplier ->
                        viewModel.addInventoryItem(name, category, qty, reorder, unit, cost, price, expiry, supplier)
                    }
                )
            }

            patientForVisit?.let { patient ->
                AddVisitDialog(
                    patientId = patient.id,
                    patientName = patient.name,
                    onDismiss = { patientForVisit = null },
                    onConfirm = { pId, pName, date, diag, rx, notes, cost ->
                        viewModel.addVisitRecord(pId, pName, date, diag, rx, notes, cost)
                        patientForVisit = null
                    }
                )
            }

            selectedPatient?.let { patient ->
                PatientDetailDialog(
                    patient = patient,
                    visits = patientVisits,
                    onDismiss = { viewModel.selectPatient(null) },
                    onAddVisitClick = {
                        patientForVisit = patient
                    },
                    onDeleteClick = {
                        viewModel.deletePatient(patient)
                    }
                )
            }

            exportedSql?.let { sql ->
                SqlExportDialog(
                    sqlDump = sql,
                    onDismiss = { viewModel.clearExportedSql() }
                )
            }
        }
    }
}
