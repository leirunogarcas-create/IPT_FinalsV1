package com.example.ipt_prefinals.model

import java.io.Serializable

data class Booking(
    var bookingId: String = "",
    var userId: String = "",
    var flightNumber: String = "",
    var origin: String = "",
    var destination: String = "",
    var departureDate: String = "",
    var departureTime: String = "",
    var passengerName: String = "",
    var seatNumber: String = "",
    var cabinClass: String = "Economy",
    var price: String = "",
    var status: String = "Confirmed",
    var createdAt: Long = System.currentTimeMillis()
) : Serializable
