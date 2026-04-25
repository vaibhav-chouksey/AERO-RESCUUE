<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
<img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
<img src="https://img.shields.io/badge/Winner-Smart%20India%20Hackathon-FF6B35?style=for-the-badge&logo=trophy&logoColor=white"/>

<br/><br/>



# 🚁 AERO RESCUE
### *Drone-Powered Disaster Management & Emergency Response Platform*

**🏆 Smart India Hackathon Winner — PSID 25047**

*A real-time Android application enabling government rescue teams to coordinate drone-assisted emergency operations during natural disasters*

---

[![Android](https://img.shields.io/badge/Min%20SDK-26-green?style=flat-square)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue?style=flat-square)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-Academic-orange?style=flat-square)](#license)

</div>

---

## 📖 Table of Contents

- [The Problem We Solved](#-the-problem-we-solved)
- [What is Aero Rescue?](#-what-is-aero-rescue)
- [App Screenshots](#-app-screenshots)
- [Core Features](#-core-features)
- [Tech Stack](#️-tech-stack)
- [System Architecture](#-system-architecture)
- [Project Structure](#-project-structure)
- [APIs & Integrations](#-apis--integrations)
- [Getting Started](#-getting-started)
- [How It Works](#-how-it-works)
- [Team](#-team)

---

## 🆘 The Problem We Solved

During floods, earthquakes, and other natural disasters in India, rescue teams face critical challenges:

- ❌ **No real-time situational awareness** — Rescue teams are blind to where victims are trapped
- ❌ **Fragmented coordination** — Field teams and control rooms operate in silos with no shared data
- ❌ **Drone deployment delays** — Without a unified system, dispatching aerial support is slow and uncoordinated
- ❌ **Poor weather intelligence** — Teams are unaware of conditions that affect rescue operations
- ❌ **Manual report management** — Disaster reports are paper-based or scattered across channels

**Aero Rescue eliminates all of these.** It creates a live, connected pipeline from the disaster site → the drone → the control room.

---

## 🚁 What is Aero Rescue?

Aero Rescue is an **Android-native disaster management application** purpose-built for government rescue authorities. It connects ground-level disaster reporters with drone operators and central control rooms through a single, unified platform.

> Think of it as a **mission control system in your pocket** — where every disaster report triggers a coordinated drone response, in real time.

**Built for:** Government disaster management departments, NDRF teams, municipal emergency response units

---

## 📱 App Screenshots

<table>
  <tr>
    <td align="center">
      <strong>🏠 Home Dashboard</strong><br/>
      Personalized control panel with drone status and disaster report access
    </td>
    <td align="center">
      <strong>📋 Report Details</strong><br/>
      Full situational report with GPS coordinates, people count, and supply needs
    </td>
  </tr>
  <tr>
    <td align="center">
      <strong>🗺️ Live Mission Map</strong><br/>
      Real-time Mapbox map showing drone location and Help Center pin
    </td>
    <td align="center">
      <strong>👤 User Profile</strong><br/>
      Rescue personnel profile with live location and personal details
    </td>
  </tr>
</table>

---

## ✨ Core Features

### 🚨 Disaster Report Management
- Submit and view structured disaster reports with report IDs (e.g. `REP-2025-001`)
- Each report captures: description, GPS coordinates, estimated people count, and resource needs
- Timestamped entries for chronological mission tracking
- Reports feed directly into the control room action pipeline

### 🛸 Drone Dispatch & Tracking
- **One-tap drone dispatch** — "Dispatch Drone" button sends GPS coordinates to control room
- **Real-time drone location** pulled from Firebase Realtime Database
- Live mission status indicator: *Locating Drone... → En Route → On Site*
- Drone position overlaid on interactive map

### 🗺️ Interactive Mission Map (Mapbox)
- Custom Mapbox GL map with live overlays
- Help Center pinned with labeled marker
- Drone position updates in real time via Firebase
- User's current GPS location with re-center button

### 🌦️ Weather Intelligence
- Dedicated weather screen for mission-area conditions
- Helps rescue teams assess flyability and on-ground conditions

### 📍 Location Services
- Auto-detects and displays rescuer's current address using reverse geocoding
- Location can be manually updated via "Update Location" button
- Location stored in profile and shared with control room as needed

### 👤 Rescue Personnel Profile
- Full profile management: name, email, phone, gender, address
- Firebase Authentication for secure login
- Sign-in/sign-out with credential management

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Language** | Kotlin | Primary development language |
| **UI Framework** | Jetpack Compose | Declarative, modern Android UI |
| **Architecture** | MVVM + Repository Pattern | Separation of concerns, testability |
| **Dependency Injection** | Dagger Hilt | Modular, scalable DI |
| **Backend / Realtime DB** | Firebase Realtime Database | Live drone location syncing |
| **Authentication** | Firebase Authentication | Secure user login |
| **Mapping** | Mapbox SDK | Custom maps with live pins |
| **Networking** | Retrofit | REST API communication |
| **State Management** | StateFlow / LiveData | Reactive UI state |
| **Async** | Kotlin Coroutines | Background operations |
| **IDE** | Android Studio | Development environment |

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      AERO RESCUE APP                         │
│                                                             │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────────┐  │
│  │  Compose │───▶│  ViewModel   │───▶│   Repository     │  │
│  │   UI     │◀───│  (StateFlow) │◀───│   (Data Layer)   │  │
│  └──────────┘    └──────────────┘    └────────┬─────────┘  │
│                                               │              │
│              ┌────────────────────────────────┘              │
│              ▼                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                  DATA SOURCES                        │   │
│  │  ┌──────────────┐  ┌─────────────┐  ┌───────────┐  │   │
│  │  │   Firebase   │  │  REST APIs  │  │  Mapbox   │  │   │
│  │  │  (Realtime   │  │  (Retrofit) │  │   SDK     │  │   │
│  │  │   DB + Auth) │  │             │  │           │  │   │
│  │  └──────────────┘  └─────────────┘  └───────────┘  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
         │                    │
         ▼                    ▼
  ┌─────────────┐    ┌────────────────┐
  │  FIREBASE   │    │  CONTROL ROOM  │
  │  Cloud      │    │  (Coordinates  │
  │  (Drone GPS)│    │   Dispatched)  │
  └─────────────┘    └────────────────┘
```

**MVVM Flow:**
- **Model** — Data classes, Firebase repository, Retrofit API services
- **ViewModel** — Holds UI state via `StateFlow`; handles business logic
- **View** — Jetpack Compose screens that observe and render state
- **Repository** — Single source of truth; abstracts Firebase and REST calls

**Hilt DI** provides ViewModels, repositories, and network clients with clean lifecycle management.

---

## 📁 Project Structure

```
AERO-RESCUUE/
└── DisasterManager2/
    ├── app/
    │   ├── src/main/java/.../
    │   │   ├── ui/
    │   │   │   ├── screens/
    │   │   │   │   ├── HomeScreen.kt          # Dashboard with drone status & report tiles
    │   │   │   │   ├── MapScreen.kt           # Live Mapbox map with drone & help center pins
    │   │   │   │   ├── ReportDetailScreen.kt  # Full disaster report view + dispatch action
    │   │   │   │   ├── WeatherScreen.kt       # Mission-area weather conditions
    │   │   │   │   └── ProfileScreen.kt       # Rescue personnel profile & settings
    │   │   │   ├── components/               # Reusable Compose components
    │   │   │   └── navigation/               # Compose NavHost & route definitions
    │   │   ├── viewmodel/
    │   │   │   ├── HomeViewModel.kt          # Drone status, report list state
    │   │   │   ├── MapViewModel.kt           # Live drone location from Firebase
    │   │   │   └── ReportViewModel.kt        # Report data & dispatch logic
    │   │   ├── repository/
    │   │   │   ├── DisasterRepository.kt     # Firebase + REST data layer
    │   │   │   └── LocationRepository.kt     # GPS & reverse geocoding
    │   │   ├── model/
    │   │   │   ├── DisasterReport.kt         # Report data class
    │   │   │   ├── DroneStatus.kt            # Drone state model
    │   │   │   └── UserProfile.kt            # Rescue personnel model
    │   │   ├── di/
    │   │   │   └── AppModule.kt             # Hilt DI module (Firebase, Retrofit, etc.)
    │   │   └── MainActivity.kt              # Entry point, Compose host
    │   ├── build.gradle.kts                 # App-level dependencies
    │   └── google-services.json            # Firebase config (not committed)
    └── build.gradle.kts                     # Project-level Gradle config
```

---

## 🔌 APIs & Integrations

| Service | Usage |
|---|---|
| **Firebase Realtime Database** | Drone GPS coordinates streamed live to map screen |
| **Firebase Authentication** | Email/password login for rescue personnel |
| **Mapbox Maps SDK** | Interactive map rendering with custom markers and overlays |
| **Android Fused Location Provider** | Rescuer's current GPS coordinates |
| **Geocoding API** | Converts GPS coordinates to human-readable address |
| **REST API (Retrofit)** | Disaster report CRUD, drone dispatch instructions to control room |
| **Weather API** | Real-time weather data for mission planning |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 26+
- A Firebase project with Realtime Database and Authentication enabled
- Mapbox account and API token
- Internet-connected device or emulator

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/vaibhav-chouksey/AERO-RESCUUE.git
cd AERO-RESCUUE/DisasterManager2

# 2. Open in Android Studio
# File → Open → select the DisasterManager2 folder

# 3. Add your Firebase config
# Download google-services.json from your Firebase Console
# Place it in: app/google-services.json

# 4. Add your Mapbox token
# In local.properties, add:
# MAPBOX_ACCESS_TOKEN=your_token_here

# 5. Sync Gradle and Run
# Click "Sync Now" when prompted, then Run on device/emulator
```

---

## ⚙️ How It Works

**End-to-end rescue flow in 4 steps:**

```
1. REPORT RECEIVED
   └── Rescuer or citizen submits disaster report
       → Report ID generated (e.g. REP-2025-001)
       → Stored with GPS, description, people count, needs

2. ASSESSMENT
   └── Control room views report details
       → Sees location on Mapbox map
       → Reviews: "Slum area submerged; residents displaced to highway"
       → 10 people need: Medical Supplies, Food

3. DRONE DISPATCHED
   └── Control room taps "Dispatch Drone"
       → GPS coordinates sent to drone control system via API
       → Drone begins navigating to target coordinates
       → Live status: "Locating Drone..." → "En Route"

4. LIVE TRACKING
   └── Drone's real-time GPS streamed to Firebase
       → App reads location via Firebase listener
       → Map screen updates drone pin in real time
       → Rescue team tracks aerial unit live
```

---

## 🔮 Roadmap

- [ ] Push notifications for new disaster reports
- [ ] Offline mode with local report caching
- [ ] Admin dashboard for authorities
- [ ] Multi-drone tracking on single map
- [ ] AI-powered damage assessment from drone imagery
- [ ] Resource allocation optimization engine
- [ ] Multi-language support (Hindi, Marathi, Telugu)

---

## 👨‍💻 Team

**Vaibhav Chouksey** — Android Developer & Project Lead
- Built the complete Android application
- Integrated Firebase, Mapbox, and drone dispatch pipeline
- Designed MVVM architecture with Hilt DI

📧 vaibhav765@gmail.com

---

## 🏆 Achievement

> **Winner — Smart India Hackathon (SIH)**
> *Government Disaster Management Track*
>
> Aero Rescue was recognized for its innovative approach to combining drone technology with a mobile-first government platform, enabling faster disaster response through real-time coordination.

---

## 📄 License

This project was developed for the Smart India Hackathon and academic purposes.

---

<div align="center">

**If this project helped you, give it a ⭐ on GitHub!**

*Built with ❤️ for disaster relief — because every second counts.*

</div>
