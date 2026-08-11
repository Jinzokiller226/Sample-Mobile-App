package com.example.repository

import android.content.Context
import com.example.data.ClinicDatabase
import com.example.data.InventoryItemEntity
import com.example.data.PatientEntity
import com.example.data.VisitRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ClinicRepository(context: Context) {
    private val db = ClinicDatabase.getDatabase(context)
    private val patientDao = db.patientDao()
    private val inventoryDao = db.inventoryDao()
    private val visitRecordDao = db.visitRecordDao()

    val allPatients: Flow<List<PatientEntity>> = patientDao.getAllPatients()
    val allInventory: Flow<List<InventoryItemEntity>> = inventoryDao.getAllInventory()
    val lowStockInventory: Flow<List<InventoryItemEntity>> = inventoryDao.getLowStockItems()
    val allVisits: Flow<List<VisitRecordEntity>> = visitRecordDao.getAllVisits()

    val patientCount: Flow<Int> = patientDao.getPatientCount()
    val inventoryCount: Flow<Int> = inventoryDao.getInventoryCount()
    val lowStockCount: Flow<Int> = inventoryDao.getLowStockCount()
    val visitCount: Flow<Int> = visitRecordDao.getVisitCount()

    fun searchPatients(query: String): Flow<List<PatientEntity>> {
        return if (query.isBlank()) allPatients else patientDao.searchPatients(query)
    }

    fun searchInventory(query: String): Flow<List<InventoryItemEntity>> {
        return if (query.isBlank()) allInventory else inventoryDao.searchInventory(query)
    }

    fun getVisitsForPatient(patientId: Long): Flow<List<VisitRecordEntity>> {
        return visitRecordDao.getVisitsForPatient(patientId)
    }

    suspend fun insertPatient(patient: PatientEntity): Long = withContext(Dispatchers.IO) {
        patientDao.insertPatient(patient)
    }

    suspend fun updatePatient(patient: PatientEntity) = withContext(Dispatchers.IO) {
        patientDao.updatePatient(patient)
    }

    suspend fun deletePatient(patient: PatientEntity) = withContext(Dispatchers.IO) {
        patientDao.deletePatient(patient)
    }

    suspend fun insertInventoryItem(item: InventoryItemEntity): Long = withContext(Dispatchers.IO) {
        inventoryDao.insertItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        inventoryDao.updateItem(item)
    }

    suspend fun deleteInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        inventoryDao.deleteItem(item)
    }

    suspend fun updateStockQuantity(id: Long, newQuantity: Int) = withContext(Dispatchers.IO) {
        inventoryDao.updateQuantity(id, newQuantity)
    }

    suspend fun insertVisitRecord(visit: VisitRecordEntity): Long = withContext(Dispatchers.IO) {
        visitRecordDao.insertVisit(visit)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val patientsSnap = patientDao.getAllPatientsSnapshot()
        if (patientsSnap.isEmpty()) {
            val p1Id = patientDao.insertPatient(
                PatientEntity(
                    patientCode = "PAT-1001",
                    name = "Eleanor Vance",
                    age = 34,
                    gender = "Female",
                    phone = "+1 (555) 234-5678",
                    email = "eleanor.vance@clinic.org",
                    address = "124 Oak Street, Suite 4",
                    allergies = "Penicillin, Latex",
                    bloodType = "O+",
                    medicalHistory = "Mild Asthma, Annual Checkup routine"
                )
            )
            val p2Id = patientDao.insertPatient(
                PatientEntity(
                    patientCode = "PAT-1002",
                    name = "Marcus Sterling",
                    age = 48,
                    gender = "Male",
                    phone = "+1 (555) 345-6789",
                    email = "marcus.sterling@example.com",
                    address = "58 Pine Avenue",
                    allergies = "None reported",
                    bloodType = "A+",
                    medicalHistory = "Type 2 Diabetes (Managed), High Cholesterol"
                )
            )
            patientDao.insertPatient(
                PatientEntity(
                    patientCode = "PAT-1003",
                    name = "Sophia Rodriguez",
                    age = 29,
                    gender = "Female",
                    phone = "+1 (555) 456-7890",
                    email = "s.rodriguez@example.com",
                    address = "912 Elm Boulevard",
                    allergies = "Sulfa drugs",
                    bloodType = "B-",
                    medicalHistory = "Hypertension, Seasonal allergies"
                )
            )
            patientDao.insertPatient(
                PatientEntity(
                    patientCode = "PAT-1004",
                    name = "David Kim",
                    age = 52,
                    gender = "Male",
                    phone = "+1 (555) 567-8901",
                    email = "david.kim@example.com",
                    address = "304 Cedar Lane",
                    allergies = "Lactose",
                    bloodType = "AB+",
                    medicalHistory = "Previous knee surgery (2023)"
                )
            )

            // Seed Visit Records
            visitRecordDao.insertVisit(
                VisitRecordEntity(
                    patientId = p1Id,
                    patientName = "Eleanor Vance",
                    visitDate = "2026-08-10",
                    diagnosis = "Acute Upper Respiratory Infection",
                    prescription = "Amoxicillin 500mg (1 tab TID x 7d)",
                    doctorNotes = "Patient presents with low fever and cough. Lungs clear.",
                    cost = 45.00
                )
            )
            visitRecordDao.insertVisit(
                VisitRecordEntity(
                    patientId = p2Id,
                    patientName = "Marcus Sterling",
                    visitDate = "2026-08-09",
                    diagnosis = "Routine Diabetes Checkup & Bloodwork",
                    prescription = "Refill Metformin 850mg (1 tab BID)",
                    doctorNotes = "Glucose levels stable. HbA1c at 6.8%. Diet advised.",
                    cost = 35.00
                )
            )
        }

        val invSnap = inventoryDao.getAllInventorySnapshot()
        if (invSnap.isEmpty()) {
            inventoryDao.insertItem(
                InventoryItemEntity(
                    itemCode = "MED-001",
                    name = "Amoxicillin 500mg Capsules",
                    category = "Medicines",
                    quantity = 120,
                    reorderLevel = 30,
                    unit = "Tablets",
                    unitCost = 0.25,
                    unitPrice = 0.75,
                    expiryDate = "2027-08-15",
                    supplier = "PharmaCare Supplies"
                )
            )
            inventoryDao.insertItem(
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
                )
            )
            inventoryDao.insertItem(
                InventoryItemEntity(
                    itemCode = "MED-003",
                    name = "Metformin 850mg Tablets",
                    category = "Medicines",
                    quantity = 85,
                    reorderLevel = 25,
                    unit = "Tablets",
                    unitCost = 0.15,
                    unitPrice = 0.50,
                    expiryDate = "2028-02-10",
                    supplier = "BioLife Labs"
                )
            )
            inventoryDao.insertItem(
                InventoryItemEntity(
                    itemCode = "SUP-101",
                    name = "Sterile Gauze Pads 4x4 (Pack of 10)",
                    category = "Consumables",
                    quantity = 200,
                    reorderLevel = 40,
                    unit = "Packs",
                    unitCost = 0.10,
                    unitPrice = 0.35,
                    expiryDate = "2029-01-01",
                    supplier = "Global Medical Supplies"
                )
            )
            inventoryDao.insertItem(
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
            inventoryDao.insertItem(
                InventoryItemEntity(
                    itemCode = "EQP-201",
                    name = "Digital Non-Contact Thermometer",
                    category = "Equipment",
                    quantity = 15,
                    reorderLevel = 5,
                    unit = "Units",
                    unitCost = 12.00,
                    unitPrice = 22.00,
                    expiryDate = "N/A",
                    supplier = "MedTech Instruments"
                )
            )
        }
    }

    suspend fun generateMySqlExportScript(): String = withContext(Dispatchers.IO) {
        val patients = patientDao.getAllPatientsSnapshot()
        val inventory = inventoryDao.getAllInventorySnapshot()
        val visits = visitRecordDao.getAllVisitsSnapshot()

        val sb = StringBuilder()
        sb.append("-- ============================================\n")
        sb.append("-- Clinic Manager MySQL Database Dump & DDL Script\n")
        sb.append("-- Generated: ").append(java.util.Date().toString()).append("\n")
        sb.append("-- ============================================\n\n")

        sb.append("CREATE DATABASE IF NOT EXISTS `clinic_db`;\n")
        sb.append("USE `clinic_db`;\n\n")

        sb.append("-- 1. Table structure for `patients` --\n")
        sb.append("DROP TABLE IF EXISTS `patients`;\n")
        sb.append("CREATE TABLE `patients` (\n")
        sb.append("  `id` BIGINT NOT NULL AUTO_INCREMENT,\n")
        sb.append("  `patient_code` VARCHAR(50) NOT NULL,\n")
        sb.append("  `name` VARCHAR(150) NOT NULL,\n")
        sb.append("  `age` INT NOT NULL,\n")
        sb.append("  `gender` VARCHAR(20) NOT NULL,\n")
        sb.append("  `phone` VARCHAR(50),\n")
        sb.append("  `email` VARCHAR(100),\n")
        sb.append("  `address` TEXT,\n")
        sb.append("  `allergies` TEXT,\n")
        sb.append("  `blood_type` VARCHAR(10),\n")
        sb.append("  `medical_history` TEXT,\n")
        sb.append("  `created_at` BIGINT,\n")
        sb.append("  PRIMARY KEY (`id`),\n")
        sb.append("  UNIQUE KEY `uk_patient_code` (`patient_code`)\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n")

        if (patients.isNotEmpty()) {
            sb.append("INSERT INTO `patients` (`id`, `patient_code`, `name`, `age`, `gender`, `phone`, `email`, `address`, `allergies`, `blood_type`, `medical_history`, `created_at`) VALUES\n")
            patients.forEachIndexed { index, p ->
                val comma = if (index == patients.size - 1) ";" else ","
                sb.append("(${p.id}, '${escapeSql(p.patientCode)}', '${escapeSql(p.name)}', ${p.age}, '${escapeSql(p.gender)}', '${escapeSql(p.phone)}', '${escapeSql(p.email)}', '${escapeSql(p.address)}', '${escapeSql(p.allergies)}', '${escapeSql(p.bloodType)}', '${escapeSql(p.medicalHistory)}', ${p.createdAt})$comma\n")
            }
            sb.append("\n")
        }

        sb.append("-- 2. Table structure for `inventory_items` --\n")
        sb.append("DROP TABLE IF EXISTS `inventory_items`;\n")
        sb.append("CREATE TABLE `inventory_items` (\n")
        sb.append("  `id` BIGINT NOT NULL AUTO_INCREMENT,\n")
        sb.append("  `item_code` VARCHAR(50) NOT NULL,\n")
        sb.append("  `name` VARCHAR(150) NOT NULL,\n")
        sb.append("  `category` VARCHAR(50) NOT NULL,\n")
        sb.append("  `quantity` INT NOT NULL DEFAULT 0,\n")
        sb.append("  `reorder_level` INT NOT NULL DEFAULT 10,\n")
        sb.append("  `unit` VARCHAR(30) NOT NULL,\n")
        sb.append("  `unit_cost` DECIMAL(10,2) NOT NULL DEFAULT 0.00,\n")
        sb.append("  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,\n")
        sb.append("  `expiry_date` VARCHAR(30),\n")
        sb.append("  `supplier` VARCHAR(100),\n")
        sb.append("  `updated_at` BIGINT,\n")
        sb.append("  PRIMARY KEY (`id`),\n")
        sb.append("  UNIQUE KEY `uk_item_code` (`item_code`)\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n")

        if (inventory.isNotEmpty()) {
            sb.append("INSERT INTO `inventory_items` (`id`, `item_code`, `name`, `category`, `quantity`, `reorder_level`, `unit`, `unit_cost`, `unit_price`, `expiry_date`, `supplier`, `updated_at`) VALUES\n")
            inventory.forEachIndexed { index, item ->
                val comma = if (index == inventory.size - 1) ";" else ","
                sb.append("(${item.id}, '${escapeSql(item.itemCode)}', '${escapeSql(item.name)}', '${escapeSql(item.category)}', ${item.quantity}, ${item.reorderLevel}, '${escapeSql(item.unit)}', ${item.unitCost}, ${item.unitPrice}, '${escapeSql(item.expiryDate)}', '${escapeSql(item.supplier)}', ${item.updatedAt})$comma\n")
            }
            sb.append("\n")
        }

        sb.append("-- 3. Table structure for `visit_records` --\n")
        sb.append("DROP TABLE IF EXISTS `visit_records`;\n")
        sb.append("CREATE TABLE `visit_records` (\n")
        sb.append("  `id` BIGINT NOT NULL AUTO_INCREMENT,\n")
        sb.append("  `patient_id` BIGINT NOT NULL,\n")
        sb.append("  `patient_name` VARCHAR(150) NOT NULL,\n")
        sb.append("  `visit_date` VARCHAR(30) NOT NULL,\n")
        sb.append("  `diagnosis` TEXT,\n")
        sb.append("  `prescription` TEXT,\n")
        sb.append("  `doctor_notes` TEXT,\n")
        sb.append("  `cost` DECIMAL(10,2) NOT NULL DEFAULT 0.00,\n")
        sb.append("  PRIMARY KEY (`id`),\n")
        sb.append("  KEY `fk_visit_patient` (`patient_id`)\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n")

        if (visits.isNotEmpty()) {
            sb.append("INSERT INTO `visit_records` (`id`, `patient_id`, `patient_name`, `visit_date`, `diagnosis`, `prescription`, `doctor_notes`, `cost`) VALUES\n")
            visits.forEachIndexed { index, v ->
                val comma = if (index == visits.size - 1) ";" else ","
                sb.append("(${v.id}, ${v.patientId}, '${escapeSql(v.patientName)}', '${escapeSql(v.visitDate)}', '${escapeSql(v.diagnosis)}', '${escapeSql(v.prescription)}', '${escapeSql(v.doctorNotes)}', ${v.cost})$comma\n")
            }
            sb.append("\n")
        }

        sb.toString()
    }

    private fun escapeSql(value: String): String {
        return value.replace("'", "''").replace("\\", "\\\\")
    }
}
