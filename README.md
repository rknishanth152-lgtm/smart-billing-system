# Smart Billing & Inventory System
### Shree Annapurna Edible Oil Company

A desktop billing and inventory management application built with Java Swing and MySQL, designed for the day-to-day operations of Shree Annapurna Edible Oil Company.

---

## Features

### Admin Dashboard
- **Stock Overview** — Browse all products with live stock levels, prices, and vendor assignments; edit stock quantities inline
- **Vendor Management** — Add, edit, and delete vendor records (name, contact, phone, email, address)
- **Low Stock Alerts** — Highlights all products below their configured minimum stock threshold
- **Sales Reports** — Date-range filtered sales summaries with per-product revenue breakdown and bar chart visualization
- **Dashboard Metrics** — Live count of total products, low-stock items, and today's total sales revenue

### Employee Dashboard
- **Billing Panel** — Search products by name or barcode, build a cart, select payment mode (Cash / UPI / Card), and process a sale
- **Sequential Invoice Numbering** — Invoice numbers are generated atomically inside a database transaction (`BILL-000001`, `BILL-000002`, …)
- **Employee Sales History** — View all invoices personally processed, sorted newest first
- **Stock Deduction** — Every completed sale deducts stock from the database within the same atomic transaction; insufficient stock is caught and the transaction is rolled back

### Security
- Role-based login: Admin and Employee roles are enforced at authentication
- Employees cannot access the Admin Dashboard

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | Java Swing + FlatLaf 3.2.5 |
| Database | MySQL (dedicated instance on port 33066) |
| JDBC Driver | mysql-connector-j 8.3.0 |
| Build Tool | Apache Maven 3.x |
| Packaging | Maven Shade Plugin (fat JAR) |

---

## Project Structure

```
capstone/
├── pom.xml                          # Maven build configuration
├── schema.sql                       # Root-level database schema (dev reference)
├── run_app.bat                      # Convenience script to run the fat JAR
├── run_migration.bat                # Convenience script to run schema migration
├── compile_and_run.bat              # Full compile-and-run script
│
├── src/
│   └── main/
│       ├── java/com/smartbilling/
│       │   ├── Main.java                        # Application entry point
│       │   ├── DataConverter.java               # One-time data seed/conversion utility
│       │   ├── dao/                             # Data Access Objects (JDBC queries)
│       │   │   ├── DashboardDAO.java
│       │   │   ├── ProductDAO.java
│       │   │   ├── ReportDAO.java
│       │   │   ├── SaleDAO.java
│       │   │   ├── UserDAO.java
│       │   │   └── VendorDAO.java
│       │   ├── database/
│       │   │   └── DatabaseConnection.java      # JDBC connection factory
│       │   ├── model/                           # Plain Java model objects
│       │   │   ├── DashboardMetrics.java
│       │   │   ├── Product.java
│       │   │   ├── ProductSaleReport.java
│       │   │   ├── Sale.java
│       │   │   ├── SaleItem.java
│       │   │   ├── SimpleSale.java
│       │   │   ├── User.java
│       │   │   └── Vendor.java
│       │   ├── service/
│       │   │   └── AuthService.java             # Login / logout / session
│       │   └── ui/                              # Swing UI panels and frames
│       │       ├── AdminDashboard.java
│       │       ├── BillingPanel.java
│       │       ├── EmployeeDashboard.java
│       │       ├── EmployeeSalesHistoryPanel.java
│       │       ├── LoginForm.java
│       │       ├── SalesBarChartPanel.java
│       │       ├── SalesReportPanel.java
│       │       ├── StockAlertPanel.java
│       │       ├── StockOverviewPanel.java
│       │       ├── UITheme.java
│       │       └── VendorManagementPanel.java
│       └── resources/
│           ├── schema.sql                       # Bundled schema (classpath resource)
│           └── images/
│               └── shree_annapurna_logo.png
│
└── deployment/
    ├── ShreeAnnapurnaInstaller.iss              # Inno Setup installer script
    ├── build-installer.bat                      # Installer build script
    ├── app-config.example.properties            # Database config template (commit this)
    ├── app-config.properties                    # Real credentials — DO NOT COMMIT
    ├── mysql-scripts/
    │   └── set-password.sql
    ├── wizard-side.bmp                          # Installer branding
    ├── wizard-top.bmp                           # Installer branding
    └── README.txt                               # Installer build instructions
```

---

## Prerequisites

- **Java 17** (or compatible JDK)
- **Maven 3.6+** on PATH
- **MySQL** running on port `33066` with database `smart_billing_db`
  - See `schema.sql` for the database schema
  - Default credentials used by the app: `root` / `root123` (configurable)

---

## Database Setup

1. Start your MySQL instance on port **33066** (or update `DatabaseConnection.java` for your port).
2. Create the database:
   ```sql
   CREATE DATABASE smart_billing_db CHARACTER SET utf8mb4;
   ```
3. Run the schema:
   ```bash
   mysql -u root -p --port 33066 smart_billing_db < schema.sql
   ```

---

## Build

```bash
mvn clean package
```

This produces:

```
target/smart-billing-system-1.0-SNAPSHOT.jar
```

The JAR is a self-contained fat JAR (Maven Shade) with `Main-Class: com.smartbilling.Main` in its manifest.

---

## Run

```bash
java -jar target/smart-billing-system-1.0-SNAPSHOT.jar
```

Or use the convenience script:

```bat
run_app.bat
```

---

## Default Login

| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Employee | emp1 | emp123 |

*(Passwords are stored as BCrypt hashes in the `users` table. Seed data is defined in `schema.sql`.)*

---

## Windows Installer

The `deployment/` directory contains an **Inno Setup** script to build a Windows installer (`ShreeAnnapurnaBillingSetup.exe`).

> **Note:** The bundled MySQL initialization step has a known unresolved issue. The installer build is currently for local/development use only and is not yet production-ready. See `deployment/README.txt` for manual build prerequisites.

To build the installer locally (Inno Setup 6 must be installed):

```bat
cd deployment
build-installer.bat
```

---

## .gitignore Summary

The following are excluded from version control:
- `target/` — Maven build output
- `lib/` — manually copied JARs (resolved by Maven)
- `META-INF/` — generated manifest
- `dependency-reduced-pom.xml` — Maven Shade artifact
- `deployment/mysql/` — bundled MySQL binary (large binary, local only)
- `deployment/output/` — generated installer EXE
- `deployment/app-config.properties` — **contains real database credentials**
- `src/main/java/com/smartbilling/oracleJdk-26/` — accidentally embedded JDK

---

## License

This project was developed as an academic capstone project for Shree Annapurna Edible Oil Company.
