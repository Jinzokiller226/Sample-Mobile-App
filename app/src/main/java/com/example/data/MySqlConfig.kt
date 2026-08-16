package com.example.data

data class MySqlConfig(
    val host: String = "192.168.50.147",
    val port: Int = 3306,
    val databaseName: String = "clinic_db",
    val username: String = "clinic_admin",
    val password: String = "clinic123",
    val apiEndpoint: String = "http://192.168.50.147:8080/api/clinic/sync",
    val lastSyncTimestamp: Long = 0,
    val autoSyncEnabled: Boolean = false
)
