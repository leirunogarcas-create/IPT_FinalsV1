package com.example.ipt_prefinals

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ipt_prefinals.adapter.BookingAdapter
import com.example.ipt_prefinals.adapter.FlightAdapter
import com.example.ipt_prefinals.databinding.ActivityMainBinding
import com.example.ipt_prefinals.databinding.DialogBookFlightBinding
import com.example.ipt_prefinals.databinding.DialogEditBookingBinding
import com.example.ipt_prefinals.model.Booking
import com.example.ipt_prefinals.model.Flight
import com.example.ipt_prefinals.service.FirebaseManager
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var flightAdapter: FlightAdapter
    private lateinit var bookingAdapter: BookingAdapter

    private val cabinClasses = arrayOf("Economy", "Premium Economy", "Business Class", "First Class")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AUTH GUARD: Ensure user is logged in
        if (!FirebaseManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserHeader()
        setupTabs()
        setupFlightList()
        setupBookingList()
        loadUserBookings()
    }

    private fun setupUserHeader() {
        binding.tvUserBadge.text = FirebaseManager.currentUserEmail

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to log out of SkyWings Airlines?")
                .setPositiveButton("Logout") { _, _ ->
                    FirebaseManager.logoutUser()
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Explore Flights"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("My Bookings"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.layoutFlightsTab.visibility = View.VISIBLE
                        binding.layoutBookingsTab.visibility = View.GONE
                    }
                    1 -> {
                        binding.layoutFlightsTab.visibility = View.GONE
                        binding.layoutBookingsTab.visibility = View.VISIBLE
                        loadUserBookings()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupFlightList() {
        val flights = Flight.getSampleFlights()
        flightAdapter = FlightAdapter(flights) { selectedFlight ->
            showCreateBookingDialog(selectedFlight)
        }
        binding.rvFlights.layoutManager = LinearLayoutManager(this)
        binding.rvFlights.adapter = flightAdapter
    }

    private fun setupBookingList() {
        bookingAdapter = BookingAdapter(
            bookings = emptyList(),
            onEditClick = { bookingToEdit ->
                showUpdateBookingDialog(bookingToEdit)
            },
            onDeleteClick = { bookingToDelete ->
                confirmDeleteBooking(bookingToDelete)
            }
        )
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = bookingAdapter
    }

    // CRUD: READ Operation
    private fun loadUserBookings() {
        binding.bookingsProgress.visibility = View.VISIBLE
        FirebaseManager.getUserBookings { bookings ->
            binding.bookingsProgress.visibility = View.GONE
            bookingAdapter.updateData(bookings)

            if (bookings.isEmpty()) {
                binding.tvEmptyBookings.visibility = View.VISIBLE
                binding.rvBookings.visibility = View.GONE
            } else {
                binding.tvEmptyBookings.visibility = View.GONE
                binding.rvBookings.visibility = View.VISIBLE
            }
        }
    }

    // CRUD: CREATE Operation
    private fun showCreateBookingDialog(flight: Flight) {
        val dialogBinding = DialogBookFlightBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvDialogTitle.text = "Book Flight Ticket"
        dialogBinding.tvDialogSubtitle.text = "${flight.flightNumber} (${flight.originCode} ➔ ${flight.destinationCode})"

        // Pre-fill user details and date
        dialogBinding.etBookDate.setText("2026-11-20")
        dialogBinding.etBookSeat.setText("14B")
        val currentEmail = FirebaseManager.currentUserEmail
        val defaultName = if (currentEmail.contains("@")) {
            currentEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else "SkyWings Traveler"
        dialogBinding.etBookPassengerName.setText(defaultName)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cabinClasses)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spCabinClass.adapter = spinnerAdapter

        dialogBinding.btnCancelBook.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirmBook.setOnClickListener {
            val passengerName = dialogBinding.etBookPassengerName.text.toString().trim()
            val date = dialogBinding.etBookDate.text.toString().trim()
            val seat = dialogBinding.etBookSeat.text.toString().trim()
            val selectedCabin = dialogBinding.spCabinClass.selectedItem.toString()

            if (passengerName.isEmpty()) {
                dialogBinding.etBookPassengerName.error = "Passenger name required"
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                dialogBinding.etBookDate.error = "Departure date required"
                return@setOnClickListener
            }

            val newBooking = Booking(
                flightNumber = flight.flightNumber,
                origin = "${flight.origin} (${flight.originCode})",
                destination = "${flight.destination} (${flight.destinationCode})",
                departureDate = date,
                departureTime = flight.departureTime,
                passengerName = passengerName,
                seatNumber = seat.ifEmpty { "12A" },
                cabinClass = selectedCabin,
                price = flight.price,
                status = "Confirmed"
            )

            FirebaseManager.createBooking(newBooking) { success, msg ->
                Toast.makeText(this, msg ?: "Ticket booked!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                // Switch tab to My Bookings
                binding.tabLayout.getTabAt(1)?.select()
            }
        }

        dialog.show()
    }

    // CRUD: UPDATE Operation
    private fun showUpdateBookingDialog(booking: Booking) {
        val dialogBinding = DialogEditBookingBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvEditSubtitle.text = "Flight: ${booking.flightNumber} (${booking.origin} ➔ ${booking.destination})"
        dialogBinding.etEditPassengerName.setText(booking.passengerName)
        dialogBinding.etEditDate.setText(booking.departureDate)
        dialogBinding.etEditSeat.setText(booking.seatNumber)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cabinClasses)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spEditCabinClass.adapter = spinnerAdapter
        val classIndex = cabinClasses.indexOf(booking.cabinClass)
        if (classIndex >= 0) {
            dialogBinding.spEditCabinClass.setSelection(classIndex)
        }

        dialogBinding.btnCancelEdit.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSaveEdit.setOnClickListener {
            val newName = dialogBinding.etEditPassengerName.text.toString().trim()
            val newDate = dialogBinding.etEditDate.text.toString().trim()
            val newSeat = dialogBinding.etEditSeat.text.toString().trim()
            val newClass = dialogBinding.spEditCabinClass.selectedItem.toString()

            if (newName.isEmpty() || newDate.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedBooking = booking.copy(
                passengerName = newName,
                departureDate = newDate,
                seatNumber = newSeat.ifEmpty { booking.seatNumber },
                cabinClass = newClass,
                status = "Updated"
            )

            FirebaseManager.updateBooking(updatedBooking) { success, msg ->
                Toast.makeText(this, msg ?: "Booking updated!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadUserBookings()
            }
        }

        dialog.show()
    }

    // CRUD: DELETE Operation
    private fun confirmDeleteBooking(booking: Booking) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Flight Ticket")
            .setMessage("Are you sure you want to cancel booking for flight ${booking.flightNumber} (${booking.origin} to ${booking.destination})?")
            .setPositiveButton("Cancel Ticket") { _, _ ->
                FirebaseManager.deleteBooking(booking.bookingId) { success, msg ->
                    Toast.makeText(this, msg ?: "Booking cancelled", Toast.LENGTH_SHORT).show()
                    loadUserBookings()
                }
            }
            .setNegativeButton("Keep Ticket", null)
            .show()
    }
}
