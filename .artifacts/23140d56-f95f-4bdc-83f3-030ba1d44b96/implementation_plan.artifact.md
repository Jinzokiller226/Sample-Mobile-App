# Implementation Plan - Switch Local DB to MySQL

This plan outlines the steps to migrate the application's primary data storage from a local Room database to a remote MySQL database using the connection parameters provided in `MySqlConfig`.

## User Review Required

> [!WARNING]
> This implementation will use direct JDBC connectivity to MySQL. While this fulfills the request to "switch the database connection", it is generally recommended to use a REST API as a bridge for production Android applications to ensure security and network reliability.
>
> **Important**: Ensure your MySQL server allows remote connections from your Android device's IP address and that the `clinic_db` database exists.

## Proposed Changes

### Dependencies & Configuration
#### [MODIFY] [libs.versions.toml](file:///E:/Testapp/Sample-Mobile-App/gradle/libs.versions.toml)
*   Add `mysql-connector-j` version `26.7.0`.

#### [MODIFY] [app/build.gradle.kts](file:///E:/Testapp/Sample-Mobile-App/app/build.gradle.kts)
*   Include the MySQL connector dependency.

---

### Data Layer
#### [NEW] [MySqlDataSource.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/data/MySqlDataSource.kt)
*   Implement a new data source that uses `java.sql.DriverManager` to connect to MySQL.
*   Construct the connection string: `jdbc:mysql://${config.host}:${config.port}/${config.databaseName}`.
*   Provide CRUD methods for Patients, Inventory, and Visits using raw SQL queries.

---

### Repository Layer
#### [MODIFY] [ClinicRepository.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/repository/ClinicRepository.kt)
*   Replace `ClinicDatabase` (Room) with `MySqlDataSource`.
*   Initialize `MySqlDataSource` using `MySqlSyncRepository.loadConfig()`.
*   Update all repository methods to use the new `MySqlDataSource`.

---

### Logic Layer
#### [MODIFY] [ClinicViewModel.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/ui/ClinicViewModel.kt)
*   Update the `updateMySqlConfig` method to trigger a repository refresh or connection update when settings change.

## Verification Plan

### Automated Tests
*   None (JDBC testing requires a live database).

### Manual Verification
1.  Configure MySQL connection settings in the "MySQL Database Integration" screen.
2.  Test the connection.
3.  Add a new patient and verify it is saved to the MySQL database (using a tool like MySQL Workbench).
4.  Verify that existing data in MySQL is loaded into the app's Dashboard and Lists.
