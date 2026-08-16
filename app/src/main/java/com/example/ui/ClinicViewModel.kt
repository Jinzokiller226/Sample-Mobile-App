package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InventoryItemEntity
import com.example.data.MySqlConfig
import com.example.data.PatientEntity
import com.example.data.VisitRecordEntity
import com.example.repository.ClinicRepository
import com.example.repository.MySqlSyncRepository
import com.example.repository.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ClinicTab {
    DASHBOARD, PATIENTS, INVENTORY, MYSQL_DB, ACCOUNT
}

class ClinicViewModel(application: Application) : AndroidViewModel(application) {
    private val clinicRepository = ClinicRepository(application)
    private val mySqlSyncRepository = MySqlSyncRepository(application)

    // Current Navigation Tab
    private val _selectedTab = MutableStateFlow(ClinicTab.DASHBOARD)
    val selectedTab: StateFlow<ClinicTab> = _selectedTab.asStateFlow()

    // Search queries
    val patientSearchQuery = MutableStateFlow("")
    val inventorySearchQuery = MutableStateFlow("")
    val inventoryCategoryFilter = MutableStateFlow("All")

    // Reactive Data Flows
    val patients: StateFlow<List<PatientEntity>> = patientSearchQuery
        .flatMapLatest { query -> clinicRepository.searchPatients(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<InventoryItemEntity>> = combine(
        inventorySearchQuery,
        inventoryCategoryFilter
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        clinicRepository.searchInventory(query)
    }.combine(inventoryCategoryFilter) { list, category ->
        when (category) {
            "All" -> list
            "Low Stock" -> list.filter { it.quantity <= it.reorderLevel }
            else -> list.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockInventory: StateFlow<List<InventoryItemEntity>> = clinicRepository.lowStockInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visits: StateFlow<List<VisitRecordEntity>> = clinicRepository.allVisits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val patientCount: StateFlow<Int> = clinicRepository.patientCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inventoryCount: StateFlow<Int> = clinicRepository.inventoryCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockCount: StateFlow<Int> = clinicRepository.lowStockCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val visitCount: StateFlow<Int> = clinicRepository.visitCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Selected patient for detail view
    private val _selectedPatient = MutableStateFlow<PatientEntity?>(null)
    val selectedPatient: StateFlow<PatientEntity?> = _selectedPatient.asStateFlow()

    val selectedPatientVisits: StateFlow<List<VisitRecordEntity>> = _selectedPatient
        .flatMapLatest { patient ->
            if (patient != null) clinicRepository.getVisitsForPatient(patient.id)
            else MutableStateFlow(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // MySQL Connection Settings State
    private val _mySqlConfig = MutableStateFlow(mySqlSyncRepository.loadConfig())
    val mySqlConfig: StateFlow<MySqlConfig> = _mySqlConfig.asStateFlow()

    // Sync status message & loading state
    private val _syncResult = MutableStateFlow<SyncResult?>(null)
    val syncResult: StateFlow<SyncResult?> = _syncResult.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Generated SQL Export String
    private val _exportedSql = MutableStateFlow<String?>(null)
    val exportedSql: StateFlow<String?> = _exportedSql.asStateFlow()

    // UI Toast Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            clinicRepository.seedInitialDataIfEmpty()
        }
    }

    fun selectTab(tab: ClinicTab) {
        _selectedTab.value = tab
    }

    fun selectPatient(patient: PatientEntity?) {
        _selectedPatient.value = patient
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun clearSyncResult() {
        _syncResult.value = null
    }

    fun clearExportedSql() {
        _exportedSql.value = null
    }

    // Patient Actions
    fun addPatient(
        name: String,
        ageStr: String,
        gender: String,
        phone: String,
        email: String,
        address: String,
        allergies: String,
        bloodType: String,
        medicalHistory: String
    ) {
        viewModelScope.launch {
            val age = ageStr.toIntOrNull() ?: 30
            val codeNumber = (1000..9999).random()
            val patient = PatientEntity(
                patientCode = "PAT-$codeNumber",
                name = name.trim(),
                age = age,
                gender = gender,
                phone = phone.trim(),
                email = email.trim(),
                address = address.trim(),
                allergies = allergies.trim().ifEmpty { "None" },
                bloodType = bloodType,
                medicalHistory = medicalHistory.trim().ifEmpty { "None" }
            )
            clinicRepository.insertPatient(patient)
            _userMessage.value = "Patient '${patient.name}' registered successfully."
        }
    }

    fun updatePatient(patient: PatientEntity) {
        viewModelScope.launch {
            clinicRepository.updatePatient(patient)
            _userMessage.value = "Patient '${patient.name}' record updated."
        }
    }

    fun deletePatient(patient: PatientEntity) {
        viewModelScope.launch {
            clinicRepository.deletePatient(patient)
            if (_selectedPatient.value?.id == patient.id) {
                _selectedPatient.value = null
            }
            _userMessage.value = "Patient '${patient.name}' removed."
        }
    }

    // Inventory Actions
    fun addInventoryItem(
        name: String,
        category: String,
        quantityStr: String,
        reorderStr: String,
        unit: String,
        costStr: String,
        priceStr: String,
        expiryDate: String,
        supplier: String
    ) {
        viewModelScope.launch {
            val qty = quantityStr.toIntOrNull() ?: 0
            val reorder = reorderStr.toIntOrNull() ?: 10
            val cost = costStr.toDoubleOrNull() ?: 0.0
            val price = priceStr.toDoubleOrNull() ?: 0.0

            val prefix = when (category) {
                "Medicines" -> "MED"
                "Consumables" -> "SUP"
                else -> "EQP"
            }
            val codeNum = (100..999).random()

            val item = InventoryItemEntity(
                itemCode = "$prefix-$codeNum",
                name = name.trim(),
                category = category,
                quantity = qty,
                reorderLevel = reorder,
                unit = unit.ifBlank { "Units" },
                unitCost = cost,
                unitPrice = price,
                expiryDate = expiryDate.trim().ifEmpty { "N/A" },
                supplier = supplier.trim().ifEmpty { "Standard Supplier" }
            )
            clinicRepository.insertInventoryItem(item)
            _userMessage.value = "Inventory item '${item.name}' added."
        }
    }

    fun updateInventoryStock(item: InventoryItemEntity, delta: Int) {
        viewModelScope.launch {
            val newQty = (item.quantity + delta).coerceAtLeast(0)
            clinicRepository.updateStockQuantity(item.id, newQty)
            _userMessage.value = "Stock for '${item.name}' updated to $newQty ${item.unit}."
        }
    }

    fun deleteInventoryItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            clinicRepository.deleteInventoryItem(item)
            _userMessage.value = "Inventory item '${item.name}' removed."
        }
    }

    // Visit Record Actions
    fun addVisitRecord(
        patientId: Long,
        patientName: String,
        visitDate: String,
        diagnosis: String,
        prescription: String,
        doctorNotes: String,
        costStr: String
    ) {
        viewModelScope.launch {
            val cost = costStr.toDoubleOrNull() ?: 0.0
            val visit = VisitRecordEntity(
                patientId = patientId,
                patientName = patientName,
                visitDate = visitDate.ifBlank { "2026-08-11" },
                diagnosis = diagnosis.trim(),
                prescription = prescription.trim(),
                doctorNotes = doctorNotes.trim(),
                cost = cost
            )
            clinicRepository.insertVisitRecord(visit)
            _userMessage.value = "Visit record created for $patientName."
        }
    }

    // MySQL Database Actions
    fun updateMySqlConfig(config: MySqlConfig) {
        _mySqlConfig.value = config
        mySqlSyncRepository.saveConfig(config)
        clinicRepository.triggerRefresh()
        _userMessage.value = "MySQL connection configuration saved."
    }

    fun testMySqlConnection() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = mySqlSyncRepository.testConnection(_mySqlConfig.value)
            _syncResult.value = result
            _isSyncing.value = false
        }
    }

    fun syncToMySql() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = mySqlSyncRepository.syncDataToMySql(_mySqlConfig.value, clinicRepository)
            _syncResult.value = result
            _mySqlConfig.value = mySqlSyncRepository.loadConfig()
            _isSyncing.value = false
        }
    }

    fun generateSqlScript() {
        viewModelScope.launch {
            val sql = clinicRepository.generateMySqlExportScript()
            _exportedSql.value = sql
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            clinicRepository.seedInitialDataIfEmpty()
            _userMessage.value = "Sample clinic records refreshed."
        }
    }
}
