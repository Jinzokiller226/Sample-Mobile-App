# Implementation Plan - Working Login Screen and Account Management

This plan outlines the steps to add a robust, local authentication and account management system to the Clinic Manager app.

## User Review Required

> [!IMPORTANT]
> This implementation uses a local Room database to store user accounts and Jetpack DataStore to manage the login session. This ensures the features work immediately without requiring external configuration (like Firebase).

## Proposed Changes

### Dependencies & Setup
#### [MODIFY] [app/build.gradle.kts](file:///E:/Testapp/Sample-Mobile-App/app/build.gradle.kts)
*   Uncomment and enable `androidx.navigation.compose` and `androidx.datastore.preferences`.

---

### Data Layer
#### [NEW] [UserAccountEntity.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/data/UserAccountEntity.kt)
*   Define the user account model (id, email, password, name, role).

#### [NEW] [UserDao.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/data/UserDao.kt)
*   Define database operations for user registration and authentication.

#### [MODIFY] [ClinicDatabase.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/data/ClinicDatabase.kt)
*   Add `UserAccountEntity` to the entities list.
*   Expose `UserDao`.

---

### Repository Layer
#### [NEW] [AuthRepository.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/repository/AuthRepository.kt)
*   Handle user authentication logic (Login, Register, Logout).
*   Manage session state using `DataStore`.

---

### Logic Layer
#### [NEW] [AuthViewModel.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/ui/AuthViewModel.kt)
*   Manage `AuthState` (Loading, Authenticated, Unauthenticated).
*   Expose login and registration methods.

#### [MODIFY] [ClinicTab.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/ui/ClinicViewModel.kt)
*   Add `ACCOUNT` to the `ClinicTab` enum.

---

### UI Layer
#### [NEW] [LoginScreen.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/ui/screens/LoginScreen.kt)
*   Create a professional login interface with email/password validation.

#### [NEW] [RegisterScreen.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/ui/screens/RegisterScreen.kt)
*   Create a registration interface for new users.

#### [NEW] [AccountScreen.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/ui/screens/AccountScreen.kt)
*   Profile overview, account settings, and logout functionality.

#### [MODIFY] [MainActivity.kt](file:///E:/Testapp/Sample-Mobile-App/app/src/main/java/com/example/MainActivity.kt)
*   Integrate `AuthViewModel`.
*   Implement a root-level check for `AuthState`.
*   Update Bottom Navigation to include the Account tab.

## Verification Plan

### Automated Tests
*   I will add unit tests for `AuthRepository` to verify login/register logic.
*   I will add a Composable preview for the `LoginScreen`.

### Manual Verification
1.  Launch the app; it should open to the `LoginScreen`.
2.  Register a new account.
3.  Log in with the new credentials.
4.  Navigate to the `Account` tab and verify user details.
5.  Log out and ensure the app returns to the `LoginScreen`.
