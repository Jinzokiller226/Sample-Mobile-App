package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.InventoryItemEntity
import com.example.data.MySqlConfig
import com.example.data.VisitRecordEntity
import com.example.ui.ClinicTab
import com.example.ui.ClinicViewModel
import com.example.ui.theme.ClinicManagerTheme
import com.example.ui.theme.HealthyGreen
import com.example.ui.theme.LowStockAmber
import com.example.ui.theme.OutOfStockRed

@Composable
fun DashboardScreen(
    viewModel: ClinicViewModel,
    onAddPatientClick: () -> Unit,
    onAddInventoryClick: () -> Unit,
    onExportSqlClick: () -> Unit
) {
    val patientCount by viewModel.patientCount.collectAsState()
    val inventoryCount by viewModel.inventoryCount.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val visitCount by viewModel.visitCount.collectAsState()
    val lowStockItems by viewModel.lowStockInventory.collectAsState()
    val visits by viewModel.visits.collectAsState()
    val mySqlConfig by viewModel.mySqlConfig.collectAsState()

    DashboardContent(
        patientCount = patientCount,
        inventoryCount = inventoryCount,
        lowStockCount = lowStockCount,
        visitCount = visitCount,
        lowStockItems = lowStockItems,
        visits = visits,
        mySqlConfig = mySqlConfig,
        onAddPatientClick = onAddPatientClick,
        onAddInventoryClick = onAddInventoryClick,
        onExportSqlClick = onExportSqlClick,
        onPatientsMetricClick = { viewModel.selectTab(ClinicTab.PATIENTS) },
        onInventoryMetricClick = { viewModel.selectTab(ClinicTab.INVENTORY) },
        onLowStockMetricClick = {
            viewModel.inventoryCategoryFilter.value = "Low Stock"
            viewModel.selectTab(ClinicTab.INVENTORY)
        },
        onMySqlConfigClick = { viewModel.selectTab(ClinicTab.MYSQL_DB) }
    )
}

@Composable
fun DashboardContent(
    patientCount: Int,
    inventoryCount: Int,
    lowStockCount: Int,
    visitCount: Int,
    lowStockItems: List<InventoryItemEntity>,
    visits: List<VisitRecordEntity>,
    mySqlConfig: MySqlConfig,
    onAddPatientClick: () -> Unit,
    onAddInventoryClick: () -> Unit,
    onExportSqlClick: () -> Unit,
    onPatientsMetricClick: () -> Unit,
    onInventoryMetricClick: () -> Unit,
    onLowStockMetricClick: () -> Unit,
    onMySqlConfigClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Clinic Overview & Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time inventory stock, patient records",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Patients",
                        value = "$patientCount",
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPatientsMetricClick() }
                    )
                    MetricCard(
                        title = "Inventory Items",
                        value = "$inventoryCount",
                        icon = Icons.Default.Inventory2,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onInventoryMetricClick() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Low Stock Alerts",
                        value = "$lowStockCount",
                        icon = Icons.Default.Warning,
                        color = if (lowStockCount > 0) LowStockAmber else HealthyGreen,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onLowStockMetricClick() }
                    )
                    MetricCard(
                        title = "Total Visits",
                        value = "$visitCount",
                        icon = Icons.Default.MedicalServices,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // MySQL Database Connection Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMySqlConfigClick() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MySQL Host: ${mySqlConfig.host}:${mySqlConfig.port}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Database: ${mySqlConfig.databaseName} • Sync: ${if (mySqlConfig.lastSyncTimestamp > 0) "Active" else "Configured"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Manage MySQL DB",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Quick Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddPatientClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_patient"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Patient", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = onAddInventoryClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_item"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stock", style = MaterialTheme.typography.labelMedium)
                    }

//                    OutlinedButton(
//                        onClick = onExportSqlClick,
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(10.dp)
//                    ) {
//                        Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text("MySQL SQL", style = MaterialTheme.typography.labelMedium)
//                    }
                }
            }
        }

        // Low Stock Warnings
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = LowStockAmber.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddAlert,
                                contentDescription = null,
                                tint = LowStockAmber
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attention: ${lowStockItems.size} Inventory Item(s) Need Reordering",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LowStockAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        lowStockItems.take(3).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${item.quantity} ${item.unit} left (Reorder <= ${item.reorderLevel})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OutOfStockRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Visit Log Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Patient Visits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (visits.isEmpty()) {
            item {
                Text(
                    text = "No patient visits recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(visits.take(4)) { visit ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = visit.patientName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = visit.visitDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = visit.diagnosis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    val sampleLowStockItems = listOf(
        InventoryItemEntity(
                     itemCode = "MED-002",
   name = "Paracetamol 500mg Tablets",
            category = "Medicines",
            quantity = 18,
            reorderLevel = 50,
            unit = "Tablets",
            unitCost = 0.05,
            unitPrice = 0.20,
            expiryDate = "2027-11-20",
            supplier = "MediHealth Corp"
        ),
        InventoryItemEntity(
            itemCode = "SUP-102",
            name = "Nitrile Exam Gloves (Medium)",
            category = "Consumables",
            quantity = 12,
            reorderLevel = 30,
            unit = "Boxes",
            unitCost = 4.50,
            unitPrice = 9.00,
            expiryDate = "2028-06-30",
            supplier = "SafeTouch Medical"
        )
    )

    val sampleVisits = listOf(
        VisitRecordEntity(
            patientId = 1,
            patientName = "Eleanor Vance",
            visitDate = "2026-08-10",
            diagnosis = "Acute Upper Respiratory Infection",
            prescription = "Amoxicillin 500mg",
            doctorNotes = "Patient presents with low fever and cough.",
            cost = 45.00
        ),
        VisitRecordEntity(
            patientId = 2,
            patientName = "Marcus Sterling",
            visitDate = "2026-08-09",
            diagnosis = "Routine Diabetes Checkup",
            prescription = "Refill Metformin 850mg",
            doctorNotes = "Glucose levels stable.",
            cost = 35.00
        )
    )

    ClinicManagerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardContent(
                patientCount = 142,
                inventoryCount = 56,
                lowStockCount = sampleLowStockItems.size,
                visitCount = 890,
                lowStockItems = sampleLowStockItems,
                visits = sampleVisits,
                mySqlConfig = MySqlConfig(),
                onAddPatientClick = {},
                onAddInventoryClick = {},
                onExportSqlClick = {},
                onPatientsMetricClick = {},
                onInventoryMetricClick = {},
                onLowStockMetricClick = {},
                onMySqlConfigClick = {}
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}


