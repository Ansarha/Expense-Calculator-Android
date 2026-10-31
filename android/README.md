# Expense Calculator — Android App (Kotlin & Jetpack Compose)

A modern Android application built using **Kotlin**, **Jetpack Compose (Material 3)**, and **Gradle 9.2** for shared household and room expense tracking, designed to pair with the Expense Calculator backend.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.2.10
- **UI Toolkit:** Jetpack Compose with Material 3 design system
- **Build Tool:** Gradle 9.2 (Wrapper configured with `gradle-9.2-bin.zip`)
- **Android Gradle Plugin (AGP):** 9.2.1 (Built-in Kotlin support)
- **Minimum SDK:** API 26 (Android 8.0)
- **Target / Compile SDK:** API 35 (Android 15) / API 36 ready
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with unidirectional data flow (StateFlow)
- **Navigation:** Jetpack Navigation Compose with animated bottom navigation
- **Package:** `com.expensecalci.app`

---

## 📁 Project Structure

```
android/
├── app/
│   ├── build.gradle.kts                # App module configuration & Compose dependencies
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/expensecalci/app/
│       │   │   ├── MainActivity.kt     # Edge-to-edge entry point
│       │   │   ├── model/              # Domain models (Expense, Settlement, FinancialOverview)
│       │   │   ├── viewmodel/          # StateFlow ViewModel with Min-Cash-Flow engine
│       │   │   ├── ui/
│       │   │   │   ├── theme/          # Material 3 Color, Type, and Theme
│       │   │   │   ├── components/     # Reusable MetricCard & ExpenseItemCard
│       │   │   │   ├── navigation/     # NavHost & bottom bar destinations
│       │   │   │   └── screens/        # Home, Expenses, AddExpense, Settlement, Analytics
│       │   └── res/                    # Adaptive icons, colors, themes, strings
│       └── test/                       # Unit tests for finances and settlements
├── gradle/
│   ├── libs.versions.toml              # Modern Gradle Version Catalog
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties   # Gradle 9.2 distribution URL
├── build.gradle.kts                    # Root build script
├── settings.gradle.kts                 # Repositories & module definition
├── gradle.properties                   # JVM & AndroidX optimization flags
├── gradlew & gradlew.bat               # Gradle wrapper CLI scripts
└── .gitignore
```

---

## 🚀 Key Features

1. **Balance & Financial Stability Overview (SRS Sections 26-27, 33-35)**:
   - Tracks **Adjusted Balance** (`Income - Expenses + Receivable - Payable`).
   - Categorizes financial status into **STABLE**, **MODERATE**, or **AT RISK**.
2. **Greedy Min-Cash-Flow Settlement Engine (SRS Sections 30-32, 108)**:
   - Calculates pairwise net balances across participants.
   - Minimizes the total number of cash-transfer transactions needed to clear all debts.
   - Lifecycle tracking: `PENDING` $\rightarrow$ `PAID` $\rightarrow$ `CONFIRMED`.
3. **Expense Management**:
   - Equal, percentage, custom, and occupancy-based daily split types.
   - Categorization (Food, Rent, Utilities, Groceries, Entertainment, Transport, Misc).
   - Fast filtering by category.
4. **Analytics & Budget Utilization**:
   - Visual progress indicators for monthly budget tracking.
   - Category-wise spending breakdown and percentage distributions.

---

## 💻 How to Open and Run

### In Android Studio
1. Open **Android Studio** (Meerkat / Ladybug or newer recommended).
2. Choose **Open** and select the `android/` directory (`d:\My____Projects\Expense_calci_project_Android_app\android`).
3. Allow Gradle to sync dependencies automatically using the bundled Gradle 9.2 wrapper.
4. Select an emulator or connected device (API 26+) and click **Run** (`Shift + F10`).

### From Command Line
Run unit tests:
```bash
./gradlew test
```

Assemble debug APK:
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`
