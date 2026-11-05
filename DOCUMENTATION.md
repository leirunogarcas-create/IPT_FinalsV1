# SkyWings Airlines - Technical Project Documentation

## 1. Project Overview
- **App Name**: SkyWings Airlines
- **Category**: Airline Booking & Flight Management System
- **Key Concepts Integrated**:
  - Secure User Authentication (Firebase Auth)
  - Realtime Database Integration (Firebase Realtime Database)
  - Full CRUD Operations (Create, Read, Update, Delete)
  - Modern Mobile UI/UX Design

---

## 2. Design Choices

### UI & Aesthetics
- **Theme Palette**: Deep Navy Sky (`#0D47A1`), Ocean Blue (`#0288D1`), Gold Accent (`#FFB300`), and Ice White Card background (`#FFFFFF`).
- **Boarding Pass Card Concept**: Booked flight tickets are styled to mimic digital boarding passes complete with flight numbers, route codes (e.g. `MNL ➔ DPS`), seat assignments, cabin class tags, and status badges.
- **Tab Navigation**: Clean separation between **Explore Flights** (catalog discovery) and **My Bookings** (active ticket manager & CRUD hub).

### Architecture & Engineering
- **ViewBinding**: Utilized across all activities, viewholders, and dialogs for type-safe view references and clean boilerplate-free code.
- **Firebase Service Abstraction (`FirebaseManager`)**: Centralized object handling authentication state, Realtime Database listeners, and fallback execution.

---

## 3. Data Model & Database Schema

### Firebase Realtime Database Structure
User data is strictly isolated by authenticated Firebase User ID (`uid`):

```json
{
  "users": {
    "UID_ABC123456": {
      "bookings": {
        "BK_17300000001": {
          "bookingId": "BK_17300000001",
          "userId": "UID_ABC123456",
          "flightNumber": "SW-702",
          "origin": "Jakarta (CGK)",
          "destination": "Bali (DPS)",
          "departureDate": "2026-11-20",
          "departureTime": "07:30 AM",
          "passengerName": "John Doe",
          "seatNumber": "14B",
          "cabinClass": "Economy",
          "price": "$145",
          "status": "Confirmed",
          "createdAt": 1730000000000
        }
      }
    }
  }
}
```

---

## 4. CRUD Functionality Breakdown

| CRUD Operation | Action Trigger | Implementation Details |
| :--- | :--- | :--- |
| **CREATE** | Tap **"Book Flight"** on any destination card | Opens `dialog_book_flight.xml`, captures passenger name, seat number, cabin class, and departure date, saving a new record under `/users/{uid}/bookings`. |
| **READ** | Tap **"My Bookings"** tab | Listens to `/users/{uid}/bookings` node in real-time and populates `rvBookings` with interactive boarding pass cards. |
| **UPDATE** | Tap **"Edit Ticket"** on a boarding pass | Opens `dialog_edit_booking.xml`, allows modifying passenger name, date, seat, or cabin class, and updates the database node. |
| **DELETE** | Tap **"Cancel Ticket"** on a boarding pass | Displays confirmation dialog; on user confirmation, removes the ticket node from `/users/{uid}/bookings`. |

---

## 5. Key Challenges & Solutions

1. **Unauthenticated Route Protection**:
   - *Challenge*: Ensuring users cannot access main flight booking features without logging in.
   - *Solution*: Implemented an Auth Guard in `MainActivity`'s `onCreate()` checking `FirebaseManager.isLoggedIn()`. If unauthenticated, the app immediately redirects to `LoginActivity` and terminates `MainActivity`.

2. **Graceful Firebase Environment Fallback**:
   - *Challenge*: Ensuring the app compiles, runs, and remains 100% testable even if client machines execute the project prior to placing `google-services.json` in `app/`.
   - *Solution*: Built a dual-mode fallback architecture in `FirebaseManager` that seamlessly interfaces with live Firebase SDKs when available, while providing an in-memory session manager if Firebase configuration is pending.
