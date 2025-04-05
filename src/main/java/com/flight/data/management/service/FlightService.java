package com.flight.data.management.service;

import com.flight.data.management.exception.ResourceNotFoundException;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.model.FlightSearchDto;
import com.flight.data.management.model.entity.Flight;
import com.flight.data.management.repository.FlightRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class FlightService {

    private FlightRepository flightRepository;

    public List<FlightDto> getFlights() {
        return flightRepository.findAll().stream().map(flight ->
                FlightDto.builder()
                        .airline(flight.getAirline())
                        .supplier(flight.getSupplier())
                        .fare(flight.getFare())
                        .departureAirport(flight.getDepartureAirport())
                        .destinationAirport(flight.getDestinationAirport())
                        .departureTime(flight.getDepartureTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .arrivalTime(flight.getArrivalTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build()
            ).toList();
    }

    public void createFlight(final FlightDto flightDto) {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);
        Flight flight = Flight.builder()
                .airline(flightDto.airline())
                .supplier(flightDto.supplier())
                .fare(flightDto.fare().setScale(2, RoundingMode.HALF_EVEN))
                .departureAirport(flightDto.departureAirport().toUpperCase())
                .destinationAirport(flightDto.destinationAirport().toUpperCase())
                .departureTime(covertStringToDateTime(flightDto.departureTime()))
                .arrivalTime(covertStringToDateTime(flightDto.arrivalTime()))
                .createdBy("USER")//TODO
                .createdAt(utcNow)
                .updatedBy("USER")//TODO
                .lastUpdatedAt(utcNow)
                .build();
        flightRepository.save(flight);
    }

    public void updateFlight(final Long id, final FlightDto flightDto) {
        final Flight flight = flightRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        flight.setAirline(flightDto.airline());
        flight.setSupplier(flightDto.supplier());
        flight.setFare(flightDto.fare().setScale(2, RoundingMode.HALF_EVEN));
        flight.setDepartureAirport(flightDto.departureAirport().toUpperCase());
        flight.setDestinationAirport(flightDto.destinationAirport().toUpperCase());
        flight.setDepartureTime(covertStringToDateTime(flightDto.departureTime()));
        flight.setArrivalTime(covertStringToDateTime(flightDto.arrivalTime()));
        flight.setUpdatedBy("USER");//TODO
        flight.setLastUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));

        flightRepository.save(flight);
    }

    public void deleteFlight(final Long id) {
        final Flight flight = flightRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        flightRepository.delete(flight);
    }

    public void searchFlights(final FlightSearchDto flightSearchDto) {
        //TODO:
    }

    //TODO: Exception handling
    private ZonedDateTime covertStringToDateTime(final String isoDateTimeString) {
        Instant instant = Instant.parse(isoDateTimeString);
        return instant.atZone(ZoneId.of("UTC"));
    }
}
