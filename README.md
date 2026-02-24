# GeoWav

GeoWav is a production-focused Android application that combines **geofencing automation**, **real-time live location sharing**, and **session-based timeline history** into a privacy-aware location intelligence system.

The application is designed with clean architecture principles, reactive state management, and scalable Firebase integration.

---

# Core Functionalities

## Geofencing (Primary Feature)

- Select important places using Google Places API  
- Continuous background geofence monitoring  
- Automatic detection of entry and exit events  
- Accurate timestamp logging  
- Works even when the app is in background  

Geofencing is the foundational system of the application.

The system detects when a user enters or exits predefined zones and triggers real-time actions without requiring the app to be open.

---

## WhatsApp Cloud API Integration (Primary Feature)

- Automatic WhatsApp message alerts on entry/exit events  
- Cloud-based message dispatch  
- Configurable recipient number  
- Real-time alert triggering  

This transforms GeoWav into an automated location-based notification system.

When a geofence event occurs, the system immediately sends a WhatsApp notification through the Cloud API, enabling real-time remote awareness.

---

# Live Location Sharing

- Real-time live location updates using Firebase Realtime Database  
- Smooth animated marker movement  
- Polyline-based path visualization  
- Distance-based coordinate filtering to prevent noisy updates  
- Supports multiple active viewers  

Users can share their live location with selected connections. Movement is rendered dynamically on Google Maps with smooth transitions and live path drawing.

---

# Emergency Mode

- Dedicated emergency sharing state  
- Real-time countdown timer  
- Visually differentiated emergency UI  
- Automatic session termination handling  
- Special highlighting on map  

Emergency mode allows users to temporarily escalate sharing with stronger visibility and time-bound tracking.

---

# Activity Logging

- Logs all geofence entry and exit events  
- Timestamped activity tracking  
- Daily activity filtering  
- Organized event history  

Provides a structured record of user movement relative to selected places.

---

# Session History System

- Automatically stores completed live sharing sessions  
- Reverse geocoding for readable start and end addresses  
- Stores full polyline route data  
- Session duration tracking  
- Secure session-level visibility control  

Each completed session captures:

- Start & end coordinates  
- Start & end timestamps  
- Human-readable addresses  
- Full route path  
- List of participants allowed to view the session  

Session history visibility is restricted only to users included in that session’s sharing audience.

---

# Timeline Screen

- User-wise session grouping  
- Chronological ordering  
- Clean, contextual timeline UI  
- Session duration display  
- “View on Map” navigation  

Each timeline item clearly displays:

- User name  
- Session date  
- Start time & address  
- End time & address  
- Total session duration  

---

# Timeline Map Preview

- Custom start and end markers  
- Polyline rendering of recorded route  
- Auto camera bounds adjustment  
- Bottom information tray with session details  
- Smooth map UI integration  

Users can preview any historical session visually on the map with a clean and contextual layout.

---

# Privacy & Access Control

GeoWav implements session-level access control:

- Only users included in a session’s `sharedWith` list can view that session  
- No retroactive access to past sessions  
- No global history visibility  
- Audience snapshot stored at session creation  

This ensures privacy is preserved even if connections change later.

---

# Architecture

The project follows a scalable and clean structure:

- MVVM Architecture  
- Repository Pattern  
- Reactive StateFlow-based state management  
- CallbackFlow for Firebase real-time listeners  
- Separation of UI state and domain logic  

---

# Tech Stack

- Kotlin  
- Jetpack Compose  
- Google Maps Compose  
- Firebase Realtime Database  
- Coroutines & Flow  
- Hilt (Dependency Injection)  
- Google Places API  
- WhatsApp Cloud API  
- Android Geofencing API  
- Geocoder API (Reverse geocoding)  

---

# Real-Time System Design

- Location updates observed using callbackFlow  
- Marker animation with Compose state updates  
- Polyline updates filtered by distance threshold  
- Session compression after stop (start & end preserved)  
- Listener cleanup to prevent memory leaks  

---

# Edge Case Handling

- Invalid coordinate filtering  
- Duplicate point filtering  
- Geocoder failure handling  
- Background monitoring reliability  
- Session audience freezing at creation  
- Proper coroutine lifecycle handling  

---

# What This Project Demonstrates

- Real-time distributed system handling  
- Privacy-aware data modeling  
- Automated geofence-triggered messaging  
- Clean reactive UI architecture  
- Advanced Google Maps integration  
- Production-level Android system design  

---

# Future Enhancements

- Mutual live sharing detection  
- Live distance calculation between users  
- Route snapping to roads  
- Session statistics (distance, speed, duration analytics)  
- Performance optimizations  

---

# Author

Aarav Halvadiya  

GitHub: https://github.com/Aarav3325  
LinkedIn: https://www.linkedin.com/in/aaravhalvadiya  
