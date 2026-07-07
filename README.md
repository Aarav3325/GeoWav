# GeoWav

GeoWav is a modern Android application for real-time location awareness that helps friends, families and trusted groups stay connected without sacrificing privacy.

Designed around the idea of awareness rather than surveillance, GeoWav combines live location sharing, intelligent place awareness, movement history and timeline replay into a seamless experience powered by Jetpack Compose and Firebase.

---

## Features

### Place Awareness

- Create awareness places using Google Places or custom map locations
- Configurable awareness radius
- Automatic arrival and departure detection
- Background geofence monitoring
- Real-time place notifications

### Live Location Sharing

- Share your location with trusted circle members in real time
- Smooth animated location updates
- Live route visualization on Google Maps
- Session-based location sharing
- Audience-controlled visibility
- Automatic session lifecycle management

### Observe Mode

- Watch a member's live movement in real time
- Multi-member observe experience
- Timeline playback with interactive controls
- Animated avatar markers
- Stay point visualization
- Stable camera tracking for smoother map interaction


### Activity Feed

- Unified awareness feed across your circle
- Arrival and departure events
- Live activity updates
- Session summaries
- People-first activity cards

### Session Timeline

- Automatic movement history
- Interactive timeline replay
- Route visualization
- Reverse geocoded locations
- Stay point detection
- Distance and duration tracking

### Emergency Mode

- Dedicated emergency location sharing
- High-visibility emergency indicators
- Trusted contact notifications
- Time-bound emergency sessions

### Privacy Controls

- Share only with selected people
- Session-level visibility
- User-controlled location sharing
- No retroactive access to previous sessions
- Privacy-first architecture

---

## Architecture

GeoWav follows modern Android development practices.

- MVVM Architecture
- Clean Architecture
- Repository Pattern
- StateFlow & Kotlin Flow
- Dependency Injection with Hilt
- Reactive UI with Jetpack Compose
- Modular and scalable project structure

---

## Tech Stack

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Coroutines
- StateFlow
- Hilt
- WorkManager

### Maps & Location

- Google Maps Compose
- Google Maps SDK
- Google Places API
- Android Geofencing API
- Fused Location Provider
- Geocoder API

### Backend

- Firebase Authentication
- Firebase Realtime Database
- Firebase Cloud Messaging

### Libraries

- Coil
- Kotlin Serialization
- RevenueCat
- Timber

---

## Performance Optimizations

GeoWav is optimized for smooth real-time location experiences.

- Animated marker interpolation
- Stable camera updates
- Marker bitmap caching
- Reduced unnecessary recompositions
- Optimized Firebase synchronization
- Efficient StateFlow-based UI updates
- Smart location update handling

---


## Roadmap

- Offline-first support
- Battery optimization improvements
- Enhanced place intelligence
- Richer movement insights

---

## Author

**Aarav Halvadiya**

GitHub: https://github.com/Aarav3325

LinkedIn: https://www.linkedin.com/in/aaravhalvadiya
