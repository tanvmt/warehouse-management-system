# 📦 Warehouse Management System (Multi-Module)

A robust, distributed Warehouse Management System (WMS) built with a Multi-Module Architecture. This system leverages gRPC for high-performance communication, JavaFX for a responsive client interface, and AspectJ (AOP) for advanced concurrency control and security.

## 🏗️ Multi-Module Architecture
The project is decoupled into four specialized modules to ensure high maintainability, strict encapsulation, and optimized build times:
| Module | Primary Responsibility | Core Technologies |
| :--- | :--- | :--- |
| **`warehouse-parent`** | Project Orchestrator & Bill of Materials (BOM). | Maven (POM) |
| **`common`** | gRPC definitions (.proto) & generated Java stubs. | gRPC, Protobuf |
| **`server`** | Business logic, JSON persistence, & AOP-driven services. | AspectJ, JWT, BCrypt |
| **`client`** | User Interface & gRPC service consumers. | JavaFX, Ikonli, PDF |

## 📂 Detailed Project Structure
```
warehouse-management-system/
├── common/
│   └── src/main/proto/            # Domain-driven gRPC Definitions (auth, user, product, etc.)
|   ├── pom.xml
├── server/
|   ├── data                       # file json.
│   ├── src/main/java/server/
│   │   ├── aspect/                # AOP: Concurrency Management (@ReadLock, @WriteLock)
│   │   ├── grpc/                  # gRPC Service Implementations (Controller Layer)
│   │   ├── service/               # Core Business Logic & Transactional flow
│   │   └── repository/            # Data Access Layer (JSON-based storage)
|   |   |__ datasource/            # Working with JSON files
│   │   └── interceptor/           # middleware of grpc ( Auth, GlobalHandlingException)
|   |   |__ container/             # apply IoC/DI
│   │   └── mapper/                # Map model with response and request
|   |   |__ model/                 # Entity 
│   │   └── validator/             # validate request
|   |   |__ exception/             # custom exception
|   ├── pom.xml
├── client/
│   ├── src/main/java/client/
│   │   ├── controller/            # JavaFX UI Event Handlers
│   │   ├── service/               # gRPC Client Wrappers
│   │   └── util/                  # Helper tools (PdfGenerator, Notifications)
│   └── src/main/resources/        # FXML Layouts, CSS Styles, & Assets
└── pom.xml                        # Root Parent POM (Dependency Management)
```
## ⚙️ Technology Stack
- **Language:** Java 11 (Optimized for AspectJ 1.9.21 compatibility)
- **Communication:** gRPC / Protocol Buffers (v1.60.0)
- **AOP:** AspectJ (Compile-Time Weaving)
- **Security:** JWT (Authentication) & BCrypt (Password Hashing)
- **Reporting:** iText 7 (Professional PDF Export)
- **GUI:** JavaFX 13 with ControlsFX and Ikonli icons
  
## 🛠️ Setup & Execution
### 1. Build the Entire System ###
Since the modules are interdependent (Client and Server rely on Common), you must build and install the project to your local repository first:
```
cd warehouse
# Execute at the project root
mvn clean install
```
*Note: This step triggers the AspectJ compiler to perform "Weaving" into the server module.*
### 2. Run the Server ###
```
# Execute from the server module
cd server
mvn exec:java "-Dexec.mainClass=server.ServerApp"
```
### 3. Run the Client (JavaFX) ###
```
# Execute from the client module
cd client
mvn javafx:run
```
## 🔐 Architectural Highlights
### 1. Concurrency Management with AspectJ ###
The system implements a custom ReadWriteLock mechanism via AOP to ensure data integrity during parallel stock operations:
- @ReadLock: Allows concurrent access for viewing product lists without blocking.
- @WriteLock: Ensures exclusive access for stock-altering operations (Import/Export).
### 2. Multi-Module Optimization ###
- **gRPC Isolation**: Keeping gRPC stubs in the *common* module prevents AspectJ from incorrectly weaving into generated code, avoiding potential bytecode corruption.
- **Faster Development**: Changes to the UI only require re-building the client module (*mvn install -pl client*), significantly reducing turn-around time.
### 3. Centralized Error Handling ###
A *GlobalExceptionHandlerInterceptor* on the server-side intercepts all business logic failures and maps them to standard gRPC Status Codes (e.g., *NOT_FOUND*, *ALREADY_EXISTS*), providing clear feedback to the client.
## 🚀 Key Features
- [x] **Product Management:** Complete CRUD with active/inactive status control.
- [x] **Secure Inventory Flow:** Thread-safe Import/Export operations.
- [x] **User Management:** Role-based access control (Admin vs. Staff).
- [x] **Real-time Dashboard:** Instant visual summaries of warehouse metrics.
- [x] **Audit Trail:** Detailed transaction history logging.
- [x] **Reporting:** One-click PDF report generation for inventory audits.

*Developed by Group 9 (Sang, Tấn, Nguyên, Thành, Tài).*
