package com.example.ipt_prefinals.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ipt_prefinals.databinding.ItemBookingBinding
import com.example.ipt_prefinals.model.Booking

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onEditClick: (Booking) -> Unit,
    private val onDeleteClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        with(holder.binding) {
            tvTicketFlightNum.text = "✈ BOARDING PASS: ${booking.flightNumber}"
            tvTicketStatus.text = booking.status.uppercase()
            tvTicketRoute.text = "${booking.origin} ➔ ${booking.destination}"
            tvPassengerName.text = booking.passengerName
            tvSeatNumber.text = booking.seatNumber
            tvCabinClass.text = booking.cabinClass
            tvDepartureDate.text = booking.departureDate
            tvTicketPrice.text = booking.price

            btnEditBooking.setOnClickListener {
                onEditClick(booking)
            }

            btnDeleteBooking.setOnClickListener {
                onDeleteClick(booking)
            }
        }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateData(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
