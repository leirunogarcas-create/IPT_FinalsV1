package com.example.ipt_prefinals.model

import java.io.Serializable

data class Flight(
    val id: String = "",
    val flightNumber: String = "",
    val origin: String = "",
    val originCode: String = "",
    val destination: String = "",
    val destinationCode: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val duration: String = "",
    val price: String = "",
    val airline: String = "SkyWings Airlines"
) : Serializable {
    companion object {
        fun getSampleFlights(): List<Flight> {
            return listOf(
                Flight(
                    id = "FL001",
                    flightNumber = "SW-702",
                    origin = "Jakarta",
                    originCode = "CGK",
                    destination = "Bali",
                    destinationCode = "DPS",
                    departureTime = "07:30 AM",
                    arrivalTime = "10:15 AM",
                    duration = "1h 45m",
                    price = "$145"
                ),
                Flight(
                    id = "FL002",
                    flightNumber = "SW-880",
                    origin = "Manila",
                    originCode = "MNL",
                    destination = "Tokyo",
                    destinationCode = "HND",
                    departureTime = "09:15 AM",
                    arrivalTime = "02:30 PM",
                    duration = "4h 15m",
                    price = "$320"
                ),
                Flight(
                    id = "FL003",
                    flightNumber = "SW-310",
                    origin = "Singapore",
                    originCode = "SIN",
                    destination = "Bali",
                    destinationCode = "DPS",
                    departureTime = "11:00 AM",
                    arrivalTime = "01:45 PM",
                    duration = "2h 45m",
                    price = "$195"
                ),
                Flight(
                    id = "FL004",
                    flightNumber = "SW-505",
                    origin = "Manila",
                    originCode = "MNL",
                    destination = "Singapore",
                    destinationCode = "SIN",
                    departureTime = "01:20 PM",
                    arrivalTime = "04:50 PM",
                    duration = "3h 30m",
                    price = "$210"
                ),
                Flight(
                    id = "FL005",
                    flightNumber = "SW-920",
                    origin = "Tokyo",
                    originCode = "NRT",
                    destination = "Paris",
                    destinationCode = "CDG",
                    departureTime = "06:00 PM",
                    arrivalTime = "05:30 AM",
                    duration = "12h 30m",
                    price = "$850"
                )
            )
        }
    }
}
