package com.example.ipt_prefinals.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ipt_prefinals.databinding.ItemFlightBinding
import com.example.ipt_prefinals.model.Flight

class FlightAdapter(
    private var flights: List<Flight>,
    private val onBookClick: (Flight) -> Unit
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    inner class FlightViewHolder(val binding: ItemFlightBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = flights[position]
        with(holder.binding) {
            tvFlightNumber.text = flight.flightNumber
            tvFlightPrice.text = flight.price
            tvOriginCode.text = flight.originCode
            tvOriginName.text = flight.origin
            tvDestinationCode.text = flight.destinationCode
            tvDestinationName.text = flight.destination
            tvFlightDuration.text = flight.duration
            tvFlightSchedule.text = "Departs: ${flight.departureTime}"

            btnBookFlight.setOnClickListener {
                onBookClick(flight)
            }
        }
    }

    override fun getItemCount(): Int = flights.size

    fun updateData(newFlights: List<Flight>) {
        flights = newFlights
        notifyDataSetChanged()
    }
}
