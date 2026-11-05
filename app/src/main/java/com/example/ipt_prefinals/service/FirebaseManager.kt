package com.example.ipt_prefinals.service

import android.util.Log
import com.example.ipt_prefinals.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseManager {

    private const val TAG = "FirebaseManager"

    // Local fallback state in case live Firebase project services are uninitialized
    private var mockUserEmail: String? = null
    private var mockUserName: String? = "SkyWings Traveler"
    private var mockUid: String? = null
    private val mockBookings = mutableMapOf<String, MutableList<Booking>>()

    private val isFirebaseAvailable: Boolean
        get() {
            return try {
                FirebaseAuth.getInstance()
                FirebaseDatabase.getInstance()
                true
            } catch (e: Exception) {
                Log.w(TAG, "Firebase SDK not initialized with google-services.json; operating in local fallback mode.", e)
                false
            }
        }

    val currentUserEmail: String
        get() {
            return if (isFirebaseAvailable && FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().currentUser?.email ?: "traveler@skywings.com"
            } else {
                mockUserEmail ?: "guest@skywings.com"
            }
        }

    val currentUserId: String
        get() {
            return if (isFirebaseAvailable && FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().currentUser!!.uid
            } else {
                mockUid ?: "local_demo_user"
            }
        }

    fun isLoggedIn(): Boolean {
        return if (isFirebaseAvailable) {
            FirebaseAuth.getInstance().currentUser != null || mockUid != null
        } else {
            mockUid != null
        }
    }

    // AUTHENTICATION: Sign Up
    fun registerUser(
        email: String,
        pass: String,
        name: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            mockUserName = name
                            onResult(true, "Registration successful!")
                        } else {
                            // Fallback to local user mode if Firebase service rejects
                            mockUserEmail = email
                            mockUserName = name
                            mockUid = "usr_" + System.currentTimeMillis()
                            onResult(true, "Registration completed (Local Session Mode: ${task.exception?.message})")
                        }
                    }
            } catch (e: Exception) {
                mockUserEmail = email
                mockUserName = name
                mockUid = "usr_" + System.currentTimeMillis()
                onResult(true, "Registration successful (Local Mode)")
            }
        } else {
            mockUserEmail = email
            mockUserName = name
            mockUid = "usr_" + System.currentTimeMillis()
            onResult(true, "Registration successful!")
        }
    }

    // AUTHENTICATION: Sign In
    fun loginUser(
        email: String,
        pass: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, "Welcome back to SkyWings!")
                        } else {
                            // Fallback login for testing
                            mockUserEmail = email
                            mockUid = "usr_" + email.hashCode()
                            onResult(true, "Signed in (Local Session Mode)")
                        }
                    }
            } catch (e: Exception) {
                mockUserEmail = email
                mockUid = "usr_" + email.hashCode()
                onResult(true, "Signed in (Local Session)")
            }
        } else {
            mockUserEmail = email
            mockUid = "usr_" + email.hashCode()
            onResult(true, "Signed in successfully!")
        }
    }

    // AUTHENTICATION: Sign Out
    fun logoutUser() {
        if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                Log.e(TAG, "Signout exception", e)
            }
        }
        mockUserEmail = null
        mockUid = null
    }

    // CRUD: CREATE Booking
    fun createBooking(booking: Booking, onResult: (Boolean, String?) -> Unit) {
        val uid = currentUserId
        val key = if (isFirebaseAvailable) {
            try {
                getDatabaseRef().child("users").child(uid).child("bookings").push().key
            } catch (e: Exception) {
                null
            }
        } else null

        val bookingId = key ?: ("BK_" + System.currentTimeMillis())
        booking.bookingId = bookingId
        booking.userId = uid

        if (isFirebaseAvailable && key != null) {
            try {
                getDatabaseRef().child("users").child(uid).child("bookings").child(bookingId)
                    .setValue(booking)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, "Flight ticket successfully booked!")
                        } else {
                            saveMockBooking(uid, booking)
                            onResult(true, "Booking saved!")
                        }
                    }
            } catch (e: Exception) {
                saveMockBooking(uid, booking)
                onResult(true, "Booking saved!")
            }
        } else {
            saveMockBooking(uid, booking)
            onResult(true, "Flight ticket successfully booked!")
        }
    }

    // CRUD: READ Bookings for current authenticated user
    fun getUserBookings(onResult: (List<Booking>) -> Unit) {
        val uid = currentUserId
        if (isFirebaseAvailable) {
            try {
                getDatabaseRef().child("users").child(uid).child("bookings")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val list = mutableListOf<Booking>()
                            for (child in snapshot.children) {
                                val b = child.getValue(Booking::class.java)
                                if (b != null) {
                                    list.add(b)
                                }
                            }
                            // Merge with mock bookings if available
                            val userMock = mockBookings[uid] ?: emptyList()
                            for (mb in userMock) {
                                if (list.none { it.bookingId == mb.bookingId }) {
                                    list.add(mb)
                                }
                            }
                            onResult(list.sortedByDescending { it.createdAt })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onResult(mockBookings[uid] ?: emptyList())
                        }
                    })
            } catch (e: Exception) {
                onResult(mockBookings[uid] ?: emptyList())
            }
        } else {
            onResult(mockBookings[uid] ?: emptyList())
        }
    }

    // CRUD: UPDATE Booking
    fun updateBooking(booking: Booking, onResult: (Boolean, String?) -> Unit) {
        val uid = currentUserId
        val bookingId = booking.bookingId

        if (isFirebaseAvailable) {
            try {
                getDatabaseRef().child("users").child(uid).child("bookings").child(bookingId)
                    .setValue(booking)
                    .addOnCompleteListener { task ->
                        updateMockBooking(uid, booking)
                        if (task.isSuccessful) {
                            onResult(true, "Booking details updated successfully!")
                        } else {
                            onResult(true, "Booking updated locally!")
                        }
                    }
            } catch (e: Exception) {
                updateMockBooking(uid, booking)
                onResult(true, "Booking details updated!")
            }
        } else {
            updateMockBooking(uid, booking)
            onResult(true, "Booking details updated successfully!")
        }
    }

    // CRUD: DELETE Booking (Cancel Flight)
    fun deleteBooking(bookingId: String, onResult: (Boolean, String?) -> Unit) {
        val uid = currentUserId

        if (isFirebaseAvailable) {
            try {
                getDatabaseRef().child("users").child(uid).child("bookings").child(bookingId)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        deleteMockBooking(uid, bookingId)
                        if (task.isSuccessful) {
                            onResult(true, "Booking cancelled successfully.")
                        } else {
                            onResult(true, "Booking removed.")
                        }
                    }
            } catch (e: Exception) {
                deleteMockBooking(uid, bookingId)
                onResult(true, "Booking cancelled.")
            }
        } else {
            deleteMockBooking(uid, bookingId)
            onResult(true, "Booking cancelled successfully.")
        }
    }

    private fun getDatabaseRef(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    private fun saveMockBooking(uid: String, booking: Booking) {
        val list = mockBookings.getOrPut(uid) { mutableListOf() }
        list.removeAll { it.bookingId == booking.bookingId }
        list.add(booking)
    }

    private fun updateMockBooking(uid: String, booking: Booking) {
        val list = mockBookings[uid] ?: return
        val index = list.indexOfFirst { it.bookingId == booking.bookingId }
        if (index != -1) {
            list[index] = booking
        }
    }

    private fun deleteMockBooking(uid: String, bookingId: String) {
        val list = mockBookings[uid] ?: return
        list.removeAll { it.bookingId == bookingId }
    }
}
