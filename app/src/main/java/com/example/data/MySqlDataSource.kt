package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.Properties

class MySqlDataSource(private val configProvider: () -> MySqlConfig) {

    private fun getConnection(withDb: Boolean = true): Connection? {
        val config = configProvider()
        val url = if (withDb) {
            "jdbc:mysql://${config.host}:${config.port}/${config.databaseName}"
        } else {
            "jdbc:mysql://${config.host}:${config.port}/"
        }
        val props = Properties().apply {
            put("user", config.username)
            put("password", config.password)
            put("useSSL", "false")
            put("allowPublicKeyRetrieval", "true")
            put("serverTimezone", "UTC")
            put("connectTimeout", "5000")
            put("socketTimeout", "10000")
        }
        return try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(url, props)
        } catch (e: Exception) {
            Log.e("MySqlDataSource", "Connection failed to $url: ${e.message}")
            throw Exception("JDBC Connection failed: ${e.message}")
        }
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection(withDb = false) // Test server reachability first
            val success = conn != null
            conn?.close()
            success
        } catch (e: Exception) {
            Log.e("MySqlDataSource", "Test connection failed: ${e.message}")
            false
        }
    }

    suspend fun initializeTables() = withContext(Dispatchers.IO) {
        val config = configProvider()
        val connNoDb = try { getConnection(withDb = false) } catch (_: Exception) { null }
        if (connNoDb != null) {
            try {
                val stmt = connNoDb.createStatement()
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `${config.databaseName}`")
            } finally {
                connNoDb.close()
            }
        }

        val conn = getConnection() ?: return@withContext
        try {
            val stmt = conn.createStatement()
            // ... (rest of the method)

            // Users table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    full_name VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL
                )
            """.trimIndent())

            // Patients table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS patients (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    patient_code VARCHAR(50) UNIQUE NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    age INT,
                    gender VARCHAR(20),
                    phone VARCHAR(50),
                    email VARCHAR(255),
                    address TEXT,
                    allergies TEXT,
                    blood_type VARCHAR(10),
                    medical_history TEXT,
                    created_at BIGINT
                )
            """.trimIndent())

            // Inventory items table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS inventory_items (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    item_code VARCHAR(50) UNIQUE NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    category VARCHAR(100),
                    quantity INT DEFAULT 0,
                    reorder_level INT DEFAULT 0,
                    unit VARCHAR(50),
                    unit_cost DOUBLE DEFAULT 0.0,
                    unit_price DOUBLE DEFAULT 0.0,
                    expiry_date VARCHAR(50),
                    supplier VARCHAR(255),
                    updated_at BIGINT
                )
            """.trimIndent())

            // Visit records table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS visit_records (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    patient_id BIGINT,
                    patient_name VARCHAR(255),
                    visit_date VARCHAR(50),
                    diagnosis TEXT,
                    prescription TEXT,
                    doctor_notes TEXT,
                    cost DOUBLE DEFAULT 0.0,
                    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
                )
            """.trimIndent())

        } catch (e: Exception) {
            Log.e("MySqlDataSource", "Failed to initialize tables: ${e.message}")
        } finally {
            conn.close()
        }
    }

    // --- Patient Operations ---

    suspend fun getAllPatients(): List<PatientEntity> = withContext(Dispatchers.IO) {
        val patients = mutableListOf<PatientEntity>()
        val conn = getConnection() ?: return@withContext emptyList()
        val query = "SELECT * FROM patients ORDER BY name ASC"
        try {
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(query)
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs))
            }
        } finally {
            conn.close()
        }
        patients
    }

    suspend fun searchPatients(queryStr: String): List<PatientEntity> = withContext(Dispatchers.IO) {
        val patients = mutableListOf<PatientEntity>()
        val conn = getConnection() ?: return@withContext emptyList()
        val sql = "SELECT * FROM patients WHERE name LIKE ? OR patient_code LIKE ? ORDER BY name ASC"
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setString(1, "%$queryStr%")
            pstmt.setString(2, "%$queryStr%")
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs))
            }
        } finally {
            conn.close()
        }
        patients
    }

    suspend fun insertPatient(p: PatientEntity): Long = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext -1L
        val sql = """
            INSERT INTO patients (patient_code, name, age, gender, phone, email, address, allergies, blood_type, medical_history, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        try {
            val pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
            pstmt.setString(1, p.patientCode)
            pstmt.setString(2, p.name)
            pstmt.setInt(3, p.age)
            pstmt.setString(4, p.gender)
            pstmt.setString(5, p.phone)
            pstmt.setString(6, p.email)
            pstmt.setString(7, p.address)
            pstmt.setString(8, p.allergies)
            pstmt.setString(9, p.bloodType)
            pstmt.setString(10, p.medicalHistory)
            pstmt.setLong(11, p.createdAt)
            pstmt.executeUpdate()
            val rs = pstmt.generatedKeys
            if (rs.next()) rs.getLong(1) else -1L
        } finally {
            conn.close()
        }
    }

    suspend fun updatePatient(p: PatientEntity) = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext
        val sql = """
            UPDATE patients SET name=?, age=?, gender=?, phone=?, email=?, address=?, allergies=?, blood_type=?, medical_history=?
            WHERE id=?
        """.trimIndent()
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setString(1, p.name)
            pstmt.setInt(2, p.age)
            pstmt.setString(3, p.gender)
            pstmt.setString(4, p.phone)
            pstmt.setString(5, p.email)
            pstmt.setString(6, p.address)
            pstmt.setString(7, p.allergies)
            pstmt.setString(8, p.bloodType)
            pstmt.setString(9, p.medicalHistory)
            pstmt.setLong(10, p.id)
            pstmt.executeUpdate()
        } finally {
            conn.close()
        }
    }

    suspend fun deletePatient(p: PatientEntity) = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext
        val sql = "DELETE FROM patients WHERE id = ?"
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setLong(1, p.id)
            pstmt.executeUpdate()
        } finally {
            conn.close()
        }
    }

    // --- Inventory Operations ---

    suspend fun getAllInventory(): List<InventoryItemEntity> = withContext(Dispatchers.IO) {
        val items = mutableListOf<InventoryItemEntity>()
        val conn = getConnection() ?: return@withContext emptyList()
        val query = "SELECT * FROM inventory_items ORDER BY name ASC"
        try {
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(query)
            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs))
            }
        } finally {
            conn.close()
        }
        items
    }

    suspend fun searchInventory(queryStr: String): List<InventoryItemEntity> = withContext(Dispatchers.IO) {
        val items = mutableListOf<InventoryItemEntity>()
        val conn = getConnection() ?: return@withContext emptyList()
        val sql = "SELECT * FROM inventory_items WHERE name LIKE ? OR item_code LIKE ? ORDER BY name ASC"
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setString(1, "%$queryStr%")
            pstmt.setString(2, "%$queryStr%")
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs))
            }
        } finally {
            conn.close()
        }
        items
    }

    suspend fun insertInventoryItem(item: InventoryItemEntity): Long = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext -1L
        val sql = """
            INSERT INTO inventory_items (item_code, name, category, quantity, reorder_level, unit, unit_cost, unit_price, expiry_date, supplier, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        try {
            val pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
            pstmt.setString(1, item.itemCode)
            pstmt.setString(2, item.name)
            pstmt.setString(3, item.category)
            pstmt.setInt(4, item.quantity)
            pstmt.setInt(5, item.reorderLevel)
            pstmt.setString(6, item.unit)
            pstmt.setDouble(7, item.unitCost)
            pstmt.setDouble(8, item.unitPrice)
            pstmt.setString(9, item.expiryDate)
            pstmt.setString(10, item.supplier)
            pstmt.setLong(11, item.updatedAt)
            pstmt.executeUpdate()
            val rs = pstmt.generatedKeys
            if (rs.next()) rs.getLong(1) else -1L
        } finally {
            conn.close()
        }
    }

    suspend fun updateInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext
        val sql = """
            UPDATE inventory_items SET name=?, category=?, quantity=?, reorder_level=?, unit=?, unit_cost=?, unit_price=?, expiry_date=?, supplier=?, updated_at=?
            WHERE id=?
        """.trimIndent()
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setString(1, item.name)
            pstmt.setString(2, item.category)
            pstmt.setInt(3, item.quantity)
            pstmt.setInt(4, item.reorderLevel)
            pstmt.setString(5, item.unit)
            pstmt.setDouble(6, item.unitCost)
            pstmt.setDouble(7, item.unitPrice)
            pstmt.setString(8, item.expiryDate)
            pstmt.setString(9, item.supplier)
            pstmt.setLong(10, item.updatedAt)
            pstmt.setLong(11, item.id)
            pstmt.executeUpdate()
        } finally {
            conn.close()
        }
    }

    suspend fun updateStockQuantity(id: Long, newQuantity: Int) = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext
        val sql = "UPDATE inventory_items SET quantity = ?, updated_at = ? WHERE id = ?"
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setInt(1, newQuantity)
            pstmt.setLong(2, System.currentTimeMillis())
            pstmt.setLong(3, id)
            pstmt.executeUpdate()
        } finally {
            conn.close()
        }
    }

    suspend fun deleteInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext
        val sql = "DELETE FROM inventory_items WHERE id = ?"
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setLong(1, item.id)
            pstmt.executeUpdate()
        } finally {
            conn.close()
        }
    }

    // --- Visit Records Operations ---

    suspend fun getAllVisits(): List<VisitRecordEntity> = withContext(Dispatchers.IO) {
        val visits = mutableListOf<VisitRecordEntity>()
        val conn = getConnection() ?: return@withContext emptyList()
        val query = "SELECT * FROM visit_records ORDER BY visit_date DESC"
        try {
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(query)
            while (rs.next()) {
                visits.add(mapResultSetToVisit(rs))
            }
        } finally {
            conn.close()
        }
        visits
    }

    suspend fun getVisitsForPatient(patientId: Long): List<VisitRecordEntity> = withContext(Dispatchers.IO) {
        val visits = mutableListOf<VisitRecordEntity>()
        val conn = getConnection() ?: return@withContext emptyList()
        val sql = "SELECT * FROM visit_records WHERE patient_id = ? ORDER BY visit_date DESC"
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setLong(1, patientId)
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                visits.add(mapResultSetToVisit(rs))
            }
        } finally {
            conn.close()
        }
        visits
    }

    suspend fun insertVisitRecord(v: VisitRecordEntity): Long = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext -1L
        val sql = """
            INSERT INTO visit_records (patient_id, patient_name, visit_date, diagnosis, prescription, doctor_notes, cost)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        try {
            val pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
            pstmt.setLong(1, v.patientId)
            pstmt.setString(2, v.patientName)
            pstmt.setString(3, v.visitDate)
            pstmt.setString(4, v.diagnosis)
            pstmt.setString(5, v.prescription)
            pstmt.setString(6, v.doctorNotes)
            pstmt.setDouble(7, v.cost)
            pstmt.executeUpdate()
            val rs = pstmt.generatedKeys
            if (rs.next()) rs.getLong(1) else -1L
        } finally {
            conn.close()
        }
    }

    // --- User Operations ---

    suspend fun getUserByEmail(email: String): UserAccountEntity? = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext null
        val sql = "SELECT * FROM users WHERE email = ?"
        try {
            val pstmt = conn.prepareStatement(sql)
            pstmt.setString(1, email)
            val rs = pstmt.executeQuery()
            if (rs.next()) mapResultSetToUser(rs) else null
        } finally {
            conn.close()
        }
    }

    suspend fun registerUser(u: UserAccountEntity): Long = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext -1L
        val sql = "INSERT INTO users (email, password_hash, full_name, role) VALUES (?, ?, ?, ?)"
        try {
            val pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
            pstmt.setString(1, u.email)
            pstmt.setString(2, u.passwordHash)
            pstmt.setString(3, u.fullName)
            pstmt.setString(4, u.role)
            pstmt.executeUpdate()
            val rs = pstmt.generatedKeys
            if (rs.next()) rs.getLong(1) else -1L
        } finally {
            conn.close()
        }
    }

    suspend fun getUserCount(): Int = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: return@withContext 0
        val sql = "SELECT COUNT(*) FROM users"
        try {
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(sql)
            if (rs.next()) rs.getInt(1) else 0
        } finally {
            conn.close()
        }
    }

    suspend fun executeScript(script: String) = withContext(Dispatchers.IO) {
        val conn = getConnection() ?: throw Exception("Failed to connect to MySQL")
        try {
            conn.autoCommit = false
            val stmt = conn.createStatement()
            val statements = script.split(";")
            for (sql in statements) {
                val trimmedSql = sql.trim()
                if (trimmedSql.isNotEmpty() && !trimmedSql.startsWith("--")) {
                    stmt.addBatch(trimmedSql)
                }
            }
            stmt.executeBatch()
            conn.commit()
        } catch (e: Exception) {
            try { conn.rollback() } catch (re: Exception) {}
            Log.e("MySqlDataSource", "Script execution failed: ${e.message}")
            throw e
        } finally {
            conn.close()
        }
    }

    // --- Helper Mapping Methods ---

    private fun mapResultSetToPatient(rs: ResultSet): PatientEntity {
        return PatientEntity(
            id = rs.getLong("id"),
            patientCode = rs.getString("patient_code"),
            name = rs.getString("name"),
            age = rs.getInt("age"),
            gender = rs.getString("gender"),
            phone = rs.getString("phone"),
            email = rs.getString("email"),
            address = rs.getString("address"),
            allergies = rs.getString("allergies"),
            bloodType = rs.getString("blood_type"),
            medicalHistory = rs.getString("medical_history"),
            createdAt = rs.getLong("created_at")
        )
    }

    private fun mapResultSetToInventoryItem(rs: ResultSet): InventoryItemEntity {
        return InventoryItemEntity(
            id = rs.getLong("id"),
            itemCode = rs.getString("item_code"),
            name = rs.getString("name"),
            category = rs.getString("category"),
            quantity = rs.getInt("quantity"),
            reorderLevel = rs.getInt("reorder_level"),
            unit = rs.getString("unit"),
            unitCost = rs.getDouble("unit_cost"),
            unitPrice = rs.getDouble("unit_price"),
            expiryDate = rs.getString("expiry_date"),
            supplier = rs.getString("supplier"),
            updatedAt = rs.getLong("updated_at")
        )
    }

    private fun mapResultSetToVisit(rs: ResultSet): VisitRecordEntity {
        return VisitRecordEntity(
            id = rs.getLong("id"),
            patientId = rs.getLong("patient_id"),
            patientName = rs.getString("patient_name"),
            visitDate = rs.getString("visit_date"),
            diagnosis = rs.getString("diagnosis"),
            prescription = rs.getString("prescription"),
            doctorNotes = rs.getString("doctor_notes"),
            cost = rs.getDouble("cost")
        )
    }

    private fun mapResultSetToUser(rs: ResultSet): UserAccountEntity {
        return UserAccountEntity(
            id = rs.getLong("id"),
            email = rs.getString("email"),
            passwordHash = rs.getString("password_hash"),
            fullName = rs.getString("full_name"),
            role = rs.getString("role")
        )
    }
}
