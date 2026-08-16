package com.example.repository

import android.content.Context
import com.example.data.InventoryItemEntity
import com.example.data.MySqlDataSource
import com.example.data.PatientEntity
import com.example.data.VisitRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ClinicRepository(context: Context) {
    private val database = com.example.data.ClinicDatabase.getDatabase(context)
    private val patientDao = database.patientDao()
    private val inventoryDao = database.inventoryDao()
    private val visitDao = database.visitDao()
    private val userDao = database.userDao()

    private val mySqlSyncRepository = MySqlSyncRepository(context)
    private val mySqlDataSource = MySqlDataSource { mySqlSyncRepository.loadConfig() }

    private val refreshTrigger = MutableStateFlow(0)

    fun triggerRefresh() {
        refreshTrigger.value += 1
    }

    val allPatients: Flow<List<PatientEntity>> = patientDao.getAllPatients()

    val allInventory: Flow<List<InventoryItemEntity>> = inventoryDao.getAllInventory()

    val lowStockInventory: Flow<List<InventoryItemEntity>> = allInventory.map { list ->
        list.filter { it.quantity <= it.reorderLevel }
    }

    val allVisits: Flow<List<VisitRecordEntity>> = visitDao.getAllVisits()

    val patientCount: Flow<Int> = allPatients.map { it.size }
    val inventoryCount: Flow<Int> = allInventory.map { it.size }
    val lowStockCount: Flow<Int> = lowStockInventory.map { it.size }
    val visitCount: Flow<Int> = allVisits.map { it.size }

    fun searchPatients(query: String): Flow<List<PatientEntity>> {
        return if (query.isBlank()) allPatients else patientDao.searchPatients(query)
    }

    fun searchInventory(query: String): Flow<List<InventoryItemEntity>> {
        return if (query.isBlank()) allInventory else inventoryDao.searchInventory(query)
    }

    fun getVisitsForPatient(patientId: Long): Flow<List<VisitRecordEntity>> {
        return visitDao.getVisitsForPatient(patientId)
    }

    suspend fun insertPatient(patient: PatientEntity): Long = withContext(Dispatchers.IO) {
        patientDao.insert(patient)
    }

    suspend fun updatePatient(patient: PatientEntity) = withContext(Dispatchers.IO) {
        patientDao.update(patient)
    }

    suspend fun deletePatient(patient: PatientEntity) = withContext(Dispatchers.IO) {
        patientDao.delete(patient)
    }

    suspend fun insertInventoryItem(item: InventoryItemEntity): Long = withContext(Dispatchers.IO) {
        inventoryDao.insert(item)
    }

    suspend fun updateInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        inventoryDao.update(item)
    }

    suspend fun deleteInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        inventoryDao.delete(item)
    }

    suspend fun updateStockQuantity(id: Long, newQuantity: Int) = withContext(Dispatchers.IO) {
        inventoryDao.updateStock(id, newQuantity, System.currentTimeMillis())
    }

    suspend fun insertVisitRecord(visit: VisitRecordEntity): Long = withContext(Dispatchers.IO) {
        visitDao.insert(visit)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Seeding removed as requested
    }

    suspend fun generateMySqlExportScript(): String = withContext(Dispatchers.IO) {
        val patients = patientDao.getAllPatients().firstOrNull() ?: emptyList()
        val inventory = inventoryDao.getAllInventory().firstOrNull() ?: emptyList()
        val visits = visitDao.getAllVisits().firstOrNull() ?: emptyList()
        val users = userDao.getAllUsers()

        val sb = StringBuilder()
        sb.append("-- MySQL Backup Script\n")
        sb.append("SET FOREIGN_KEY_CHECKS = 0;\n")
        sb.append("TRUNCATE TABLE `visit_records`;\n")
        sb.append("TRUNCATE TABLE `patients`;\n")
        sb.append("TRUNCATE TABLE `inventory_items`;\n")
        sb.append("TRUNCATE TABLE `users`;\n")
        sb.append("SET FOREIGN_KEY_CHECKS = 1;\n\n")

        // Users
        if (users.isNotEmpty()) {
            sb.append("INSERT INTO `users` (`id`, `email`, `password_hash`, `full_name`, `role`) VALUES \n")
            users.forEachIndexed { i, u ->
                sb.append("(${u.id}, '${esc(u.email)}', '${esc(u.passwordHash)}', '${esc(u.fullName)}', '${esc(u.role)}')")
                if (i < users.size - 1) sb.append(",\n") else sb.append(";\n\n")
            }
        }

        // Patients
        if (patients.isNotEmpty()) {
            sb.append("INSERT INTO `patients` (`id`, `patient_code`, `name`, `age`, `gender`, `phone`, `email`, `address`, `allergies`, `blood_type`, `medical_history`, `created_at`) VALUES \n")
            patients.forEachIndexed { i, p ->
                sb.append("(${p.id}, '${esc(p.patientCode)}', '${esc(p.name)}', ${p.age}, '${esc(p.gender)}', '${esc(p.phone)}', '${esc(p.email)}', '${esc(p.address)}', '${esc(p.allergies)}', '${esc(p.bloodType)}', '${esc(p.medicalHistory)}', ${p.createdAt})")
                if (i < patients.size - 1) sb.append(",\n") else sb.append(";\n\n")
            }
        }

        // Inventory
        if (inventory.isNotEmpty()) {
            sb.append("INSERT INTO `inventory_items` (`id`, `item_code`, `name`, `category`, `quantity`, `reorder_level`, `unit`, `unit_cost`, `unit_price`, `expiry_date`, `supplier`, `updated_at`) VALUES \n")
            inventory.forEachIndexed { i, item ->
                sb.append("(${item.id}, '${esc(item.itemCode)}', '${esc(item.name)}', '${esc(item.category)}', ${item.quantity}, ${item.reorderLevel}, '${esc(item.unit)}', ${item.unitCost}, ${item.unitPrice}, '${esc(item.expiryDate)}', '${esc(item.supplier)}', ${item.updatedAt})")
                if (i < inventory.size - 1) sb.append(",\n") else sb.append(";\n\n")
            }
        }

        // Visits
        if (visits.isNotEmpty()) {
            sb.append("INSERT INTO `visit_records` (`id`, `patient_id`, `patient_name`, `visit_date`, `diagnosis`, `prescription`, `doctor_notes`, `cost`) VALUES \n")
            visits.forEachIndexed { i, v ->
                sb.append("(${v.id}, ${v.patientId}, '${esc(v.patientName)}', '${esc(v.visitDate)}', '${esc(v.diagnosis)}', '${esc(v.prescription)}', '${esc(v.doctorNotes)}', ${v.cost})")
                if (i < visits.size - 1) sb.append(",\n") else sb.append(";\n\n")
            }
        }

        sb.toString()
    }

    private fun esc(s: String): String = s.replace("'", "''")
}
