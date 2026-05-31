# MyWallet

MyWallet is a personal finance tracker with two local-first apps:

- `money_track`: Java Swing desktop app with SQLite.
- `mywallet_android`: Native Android Java app with SQLite.

Both apps support login, create user, forgot password, monthly budgets, transactions, savings goals, and multi-user data separation.

## Project Structure

```text
MyWallet/
|-- .github/workflows/
|   |-- build.yml
|   `-- build_android.yml
|-- docs/
|-- money_track/          Java Swing desktop app
|-- mywallet_android/     Android app
`-- README.md
```

## Build Desktop

```powershell
cd C:\Users\D4RK\Documents\Git\MyWallet\money_track
mvn test
mvn exec:java
```

Desktop CI builds installers with `.github/workflows/build.yml`.

## Build Android

```powershell
cd C:\Users\D4RK\Documents\Git\MyWallet\mywallet_android
.\build_debug.bat
```

Debug APK:

```text
mywallet_android/app/build/outputs/apk/debug/app-debug.apk
```

Android CI builds the APK with `.github/workflows/build_android.yml`.

## Seed Users

```text
admin / 1234
user / user
zaid / zaid
hamza / 9999
```

For seeded users, the recovery answer is the username.

## Use Case Diagram

```puml
@startuml
left to right direction

actor User
actor Admin

rectangle "MyWallet" {
  usecase "Login" as UC_Login
  usecase "Create User" as UC_CreateUser
  usecase "Forgot Password" as UC_ForgotPassword
  usecase "View Dashboard" as UC_Dashboard
  usecase "Create Budget" as UC_Budget
  usecase "Add Transaction" as UC_AddTransaction
  usecase "List Transactions" as UC_ListTransactions
  usecase "Edit Transaction" as UC_EditTransaction
  usecase "Delete Transaction" as UC_DeleteTransaction
  usecase "Create Goal" as UC_CreateGoal
  usecase "Add Savings" as UC_AddSavings
  usecase "Delete Goal" as UC_DeleteGoal
}

User --> UC_Login
User --> UC_CreateUser
User --> UC_ForgotPassword
User --> UC_Dashboard
User --> UC_Budget
User --> UC_AddTransaction
User --> UC_ListTransactions
User --> UC_EditTransaction
User --> UC_DeleteTransaction
User --> UC_CreateGoal
User --> UC_AddSavings
User --> UC_DeleteGoal

Admin --|> User

UC_AddTransaction ..> UC_Budget : requires monthly budget
UC_Dashboard ..> UC_ListTransactions : summarizes
@enduml
```

## Login Sequence

```puml
@startuml
actor User
boundary "Login UI" as LoginUI
control "UserService" as UserService
database "SQLite users" as Users
boundary "Dashboard UI" as Dashboard

User -> LoginUI : enter username/password
LoginUI -> UserService : login(username, password)
UserService -> Users : find user by credentials

alt valid credentials
  Users --> UserService : User(id, username, permissions)
  UserService --> LoginUI : user
  LoginUI -> Dashboard : open with user id
else invalid credentials
  Users --> UserService : empty
  UserService --> LoginUI : null / attempts++
  LoginUI --> User : show error
end
@enduml
```

## Transaction Sequence

```puml
@startuml
actor User
boundary "Transaction UI" as TxUI
control "TransactionService" as TxService
control "BudgetService" as BudgetService
database "SQLite" as DB

User -> TxUI : add amount/category/month/year
TxUI -> TxService : addTransaction(userId, amount, category, month, year)
TxService -> BudgetService : getBudget(userId, month, year)
BudgetService -> DB : select budget by user/month/year
DB --> BudgetService : budget

alt budget exists and amount <= remaining
  TxService -> DB : insert transaction(user_id, amount, category, month, year)
  TxService -> DB : update budget amount = amount - transaction
  TxService --> TxUI : success
  TxUI --> User : show saved
else no budget or over budget
  TxService --> TxUI : validation error
  TxUI --> User : show error
end
@enduml
```

## Class Diagram

```puml
@startuml
skinparam classAttributeIconSize 0

class User {
  +int id
  +String username
  +int permissions
}

class Budget {
  +Integer id
  +double amount
  +double totalAmount
  +int month
  +int year
}

class Transaction {
  +Integer id
  +double amount
  +String category
  +int month
  +int year
}

class Goal {
  +Integer id
  +String name
  +double targetAmount
  +double savedAmount
  +int getProgressPercent()
}

interface IUserRepository
interface IBudgetRepository
interface ITransactionRepository
interface IGoalRepository

class UserRepositoryImpl
class BudgetRepositoryImpl
class TransactionRepositoryImpl
class GoalRepositoryImpl

class UserService
class BudgetService
class TransactionService
class GoalService
class DashBoardService
class DatabaseManager
class DatabaseSetup

IUserRepository <|.. UserRepositoryImpl
IBudgetRepository <|.. BudgetRepositoryImpl
ITransactionRepository <|.. TransactionRepositoryImpl
IGoalRepository <|.. GoalRepositoryImpl

UserService --> IUserRepository
BudgetService --> IBudgetRepository
TransactionService --> ITransactionRepository
TransactionService --> BudgetService
GoalService --> IGoalRepository
DashBoardService --> ITransactionRepository
DashBoardService --> BudgetService

UserRepositoryImpl --> DatabaseManager
BudgetRepositoryImpl --> DatabaseManager
TransactionRepositoryImpl --> DatabaseManager
GoalRepositoryImpl --> DatabaseManager
DatabaseSetup --> DatabaseManager
@enduml
```

## Database Schema

```puml
@startuml
hide circle
skinparam linetype ortho

entity users {
  * id : INTEGER <<PK>>
  --
  * username : TEXT <<UNIQUE>>
  * password : TEXT
  * permissions : INTEGER
  * recovery_answer : TEXT
}

entity budgets {
  * id : INTEGER <<PK>>
  --
  * user_id : INTEGER <<FK>>
  * amount : REAL
  * month : INTEGER
  * year : INTEGER
  * total_amount : REAL
  --
  UNIQUE(user_id, month, year)
}

entity transactions {
  * id : INTEGER <<PK>>
  --
  * user_id : INTEGER <<FK>>
  * amount : REAL
  * category : TEXT
  * month : INTEGER
  * year : INTEGER
}

entity goals {
  * id : INTEGER <<PK>>
  --
  * user_id : INTEGER <<FK>>
  * name : TEXT
  * target_amount : REAL
  * saved_amount : REAL
}

users ||--o{ budgets : owns
users ||--o{ transactions : owns
users ||--o{ goals : owns
@enduml
```

## Services Diagram

```puml
@startuml
left to right direction

package "UI Layer" {
  [Login Screen]
  [Dashboard]
  [Budget Form]
  [Transaction Form]
  [Transaction List]
  [Goals Screen]
}

package "Service Layer" {
  [UserService]
  [BudgetService]
  [TransactionService]
  [GoalService]
  [DashBoardService]
}

package "Repository Layer" {
  [UserRepository]
  [BudgetRepository]
  [TransactionRepository]
  [GoalRepository]
}

database "SQLite" as SQLite

[Login Screen] --> [UserService]
[Dashboard] --> [DashBoardService]
[Budget Form] --> [BudgetService]
[Transaction Form] --> [TransactionService]
[Transaction List] --> [TransactionService]
[Goals Screen] --> [GoalService]

[UserService] --> [UserRepository]
[BudgetService] --> [BudgetRepository]
[TransactionService] --> [TransactionRepository]
[TransactionService] --> [BudgetService]
[GoalService] --> [GoalRepository]
[DashBoardService] --> [TransactionRepository]
[DashBoardService] --> [BudgetService]

[UserRepository] --> SQLite
[BudgetRepository] --> SQLite
[TransactionRepository] --> SQLite
[GoalRepository] --> SQLite
@enduml
```

## GitHub Actions

- `build.yml`: builds desktop installers for Windows, Linux, and macOS.
- `build_android.yml`: builds the Android debug APK.

