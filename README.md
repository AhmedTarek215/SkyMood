# SkyMood ⛅

SkyMood is a modern, feature-rich weather forecasting application built with **Jetpack Compose** and **Kotlin**. It provides accurate real-time weather data, 5-day forecasts, and weather alerting systems, all designed with a dynamic UI.

---

## Features

### Weather Data
- **Real-time Forecast**: Detailed current weather conditions.
- **5-Day / Hourly Forecast**: Plan ahead with granular weather data.

### Location Management
- **Dual Location modes**: Choose between **GPS (Automatic)** for real-time tracking or **Map (Manual)** to pick any location globally.
- **OpenStreetMap Integration**: Seamless map picking experience using OSMDroid.
- **Favorites**: Save your frequent locations for quick access.

### Weather Alerts
- **Notifications**: Subtle push alerts for weather updates.
- **Alarms**: Audio and vibration alerts for critical weather warnings, featuring a custom overlay activity.
- **Scheduling**: Plan alerts for specific durations with a user-friendly time picker.

### Personalized Settings
- **Temperature Units**: Support for Celsius (°C), Fahrenheit (°F), and Kelvin (K).
- **Wind Speed Units**: Support for Meters per second (m/s) and Miles per hour (mph).
- **Multi-language**: Fully localized in **English** and **Arabic** with proper RTL (Right-to-Left) support.
- **Auto-Refresh**: Weather data automatically refreshes when units or language settings are changed.

### Offline Experience
- **Caching**: View the last known weather data even without an internet connection.
- **Offline Safety**: Settings changes and favorite additions are smartly blocked when offline to prevent data inconsistency, providing clear user feedback via snackbars.

---

## Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/compose) (100% Declarative UI)
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow / StateFlow](https://kotlinlang.org/docs/flow.html)
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Local Preferences**: [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [Gson](https://github.com/google/gson)
- **Dependency Injection**: Manual Injection / Repository Pattern
- **Map Engine**: [OSMDroid](https://github.com/osmdroid/osmdroid)
- **Backend**: [OpenWeatherMap API](https://openweathermap.org/api)

---

## 📂 Project Structure

com.example.skymood
├── data
│   ├── database      # Room DB, DAOs, and Entities
│   ├── network       # Retrofit service and client
│   ├── settings      # DataStore Preferences Manager
│   └── weather       # Repository, Local/Remote Data Sources, Models
├── presentation      # UI Layer
│   ├── home          # Home screen and Weather components
│   ├── favorites     # Saved locations management
│   ├── map           # Map picker logic
│   ├── settings      # App preferences
│   └── weatheralerts # Alert system and Alarms
├── utils             # Network utilities and Constants
└── MainActivity.kt   # App entry point with Locale support

---

## 🧪 Testing

SkyMood maintains a robust test suite covering all critical layers:
- **Unit Tests**: MockK based tests for ViewModels, Repository, and LocalDataSource.
- **Instrumented Tests**: In-memory Room database tests for DAOs.
- **Coverage**: Includes success paths, error handling, and offline fallback logic.

### Running Tests
```bash
# Run JVM Unit Tests
./gradlew testDebugUnitTest

# Run DAO Instrumented Tests (requires device/emulator)
./gradlew connectedDebugAndroidTest
```

---

## 🚀 Setup & Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/AhmedTarek215/SkyMood.git
   ```
2. **Open in Android Studio**: Use the latest Hedgehog or later.
3. **API Key**: Ensure a valid OpenWeatherMap API key is set in `utils/Constants.kt`.
4. **Build**: Build the project using Gradle.
