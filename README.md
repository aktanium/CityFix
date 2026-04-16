# 📱 CityFix — Project Documentation

## 📌 1. Project Overview

**CityFix** is a modern Android mobile application designed to help citizens report and manage urban infrastructure issues such as damaged roads, garbage accumulation, broken streetlights, and other public concerns.

The application allows users to create detailed reports including location, images, and descriptions, improving communication between citizens and city services.

---

## 🎯 2. Objectives

- Simplify reporting of city problems  
- Increase public awareness of infrastructure issues  
- Provide a centralized system for tracking problem status  
- Enable better transparency in issue resolution  

---

## 👥 3. Target Users

- Citizens  
- Municipal services (future scope)  
- Local communities  

---

## ⚙️ 4. Technology Stack

| Layer              | Technology |
|--------------------|-----------|
| Language           | Kotlin |
| UI                 | Jetpack Compose |
| Design System      | Material 3 |
| Architecture       | MVVM + Repository Pattern |
| State Management   | ViewModel + StateFlow |
| Navigation         | Compose Navigation |
| Database           | Room |
| Preferences        | DataStore |
| Async              | Kotlin Coroutines |
| Image Loading      | Coil |
| Dependency Injection | Hilt |
| Background Tasks   | WorkManager (optional) |

---

## 🏗️ 5. Architecture

The project follows **MVVM (Model-View-ViewModel)** architecture combined with the **Repository Pattern**.

### 🔁 Data Flow

UI (Compose)
↓
ViewModel
↓
Repository
↓
Room / DataStore / Remote API


### 📦 Layers

#### 1. Presentation Layer
- Jetpack Compose UI  
- ViewModels  
- StateFlow  

#### 2. Domain Layer (optional but recommended)
- Business logic  
- UseCases  

#### 3. Data Layer
- Repository  
- Room Database  
- DataStore  
- Remote API (future)  

---

## 📱 6. Features

### ✅ Core Features

#### 1. Create Report
Users can:
- Add title and description  
- Select category  
- Attach image  
- Choose location  
- Submit report  

#### 2. View Reports
- List of all reports  
- Filter by category/status  
- Real-time updates  

#### 3. Report Details
- Full description  
- Image preview  
- Location  
- Status tracking  

#### 4. Status Management
- New  
- In Progress  
- Resolved  

---

### ⭐ Optional Features (Advanced)

- Map integration (Google Maps)  
- Push notifications  
- Offline-first support  
- Report voting system  
- Admin panel (future)  

---

## 🗄️ 7. Data Model

### 📄 Report Entity

```kotlin
@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val imageUri: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val createdAt: Long
)
## 🧭 8. Navigation Structure

- **ReportListScreen**
- **AddReportScreen**
- **ReportDetailScreen**
- **ProfileScreen** (optional)

Navigation is handled using **Compose Navigation**.

---

## 🔄 9. State Management

State is managed using:
- **ViewModel**
- **StateFlow**

Example:
```kotlin
val reports: StateFlow<List<Report>>
```
UI automatically reacts to state updates.

---

## ⚡ 10. Asynchronous Operations

Handled using **Kotlin Coroutines**:
- Database operations
- Network calls (future)
- Background processing

---

## 💉 11. Dependency Injection

Implemented using **Hilt**.

**Benefits:**
- Cleaner code
- Easier testing
- Better scalability

---

## 🗃️ 12. Data Persistence

### 🟢 Room Database
- Stores reports locally
- Supports offline usage

### ⚙️ DataStore
Stores user preferences:
- Theme settings
- Filters

---

## 🖼️ 13. Image Handling

- Users can attach images to reports
- Images are loaded using **Coil**
- Supports efficient memory usage

---

## 🔄 14. Background Work (Optional)

Using **WorkManager**:
- Sync reports with server
- Retry failed uploads
- Scheduled tasks

---

## 🔐 15. Security Considerations

- Input validation
- Safe storage of user preferences
- Prepared for authentication integration (Firebase/Auth)

---

## 🧪 16. Testing Strategy

- Unit tests for ViewModel and Repository
- UI tests for Compose screens
- Manual testing for user flows

---

## 📂 17. Project Structure

```text
data/
  local/
  repository/

domain/
  model/
  usecase/

ui/
  screens/
  components/

di/
viewmodel/
```

---

## 🚀 18. Future Improvements

- Firebase backend integration
- Real-time updates
- Admin dashboard
- AI-based issue classification
- Multi-language support

---

## 🧾 19. Conclusion

**CityFix** is a scalable and modern Android application built using industry-standard technologies. It demonstrates strong architectural principles, reactive UI design, and efficient data handling.

The project can be extended into a full production system with backend integration and real-time features.

