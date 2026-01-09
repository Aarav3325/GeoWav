# GeoWav – Location-Based Alert & Activity Tracker

GeoWav is an Android application that helps users track when they **enter or leave selected places** and receive **real-time alerts**. The app records all location events with date and time, allowing users to view their movement history anytime.

---

## Features

- Select important places using **Google Places API**
- Detect entry and exit events using location tracking
- Receive automatic alerts when entering or leaving a place
- Send alerts to a selected phone number using **WhatsApp Cloud API**
- Maintain a detailed activity log with date, time, and location
- Secure user authentication with **Firebase Authentication**
- Store user data and logs in **Firebase Realtime Database**
- Handles foreground and background location permissions properly

---

## Tech Stack

- **Language:** Kotlin  
- **Architecture:** MVVM Architecture  
- **Location Services:** Fused Location Provider  
- **APIs:**  
  - Google Places API  
  - WhatsApp Cloud API  
- **Backend & Auth:**  
  - Firebase Authentication  
  - Firebase Realtime Database  

---

## 📂 App Workflow

1. User signs in using Firebase Authentication  
2. User selects places using Google Places API  
3. App monitors location in foreground and background  
4. Entry and exit events are detected  
5. Alerts are sent automatically via WhatsApp  
6. Events are logged and stored in Firebase  
7. User can view full activity history inside the app  

---

## Permissions Used

- Foreground location access  
- Background location access  
- Internet access  

Permissions are handled carefully to ensure user privacy and reliable background tracking.

---

## Learning Outcomes

This project helped me gain hands-on experience with:
- Android location services and permissions
- Background execution limits
- Real-time database updates
- API integration in Android
- Handling real-world edge cases like battery usage and app lifecycle

---

## Future Improvements

- Add geofencing for better battery efficiency  
- Offline caching for activity logs  
- Notification customization  

---

## Author

**Aarav Halvadiya**  
- GitHub: https://github.com/Aarav3325  
- LinkedIn: https://www.linkedin.com/in/aaravhalvadiya  
