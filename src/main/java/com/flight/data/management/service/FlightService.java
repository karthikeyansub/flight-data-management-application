package com.flight.data.management.service;

import com.flight.data.management.exception.ResourceNotFoundException;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.model.FlightSearchDto;
import com.flight.data.management.model.entity.Flight;
import com.flight.data.management.repository.FlightRepository;
import com.flight.data.management.service.client.CrazySupplierClient;
import com.flight.data.management.service.client.CrazySupplierFlightRequest;
import com.flight.data.management.service.client.CrazySupplierFlightResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FlightService {

    private CrazySupplierClient crazySupplierClient;

    private FlightRepository flightRepository;

    private EntityManager entityManager;

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

    public List<Flight> searchFlights(final FlightSearchDto flightSearchDto) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Flight> criteriaQuery = criteriaBuilder.createQuery(Flight.class);
        Root<Flight> root = criteriaQuery.from(Flight.class);

        List<Predicate> predicates = new ArrayList<>();

        if (flightSearchDto.departureAirport() != null && !flightSearchDto.departureAirport().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("departureAirport"), flightSearchDto.departureAirport()));
        }

        if (flightSearchDto.destinationAirport() != null && !flightSearchDto.destinationAirport().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("destinationAirport"), flightSearchDto.destinationAirport()));
        }

        if (flightSearchDto.departureTime() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("departureTime"), covertStringToDateTime(flightSearchDto.departureTime())));
        }

        if (flightSearchDto.arrivalTime() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("arrivalTime"), covertStringToDateTime(flightSearchDto.arrivalTime())));
        }

        if (flightSearchDto.airline() != null && !flightSearchDto.airline().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("airline"), flightSearchDto.airline()));
        }

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        //Result from database
        List<Flight> searchResult = entityManager.createQuery(criteriaQuery).getResultList();

        //Result from CrazySupplier
        CrazySupplierFlightRequest crazySupplierFlightRequest = CrazySupplierFlightRequest.builder()
                .departureAirportName(flightSearchDto.departureAirport())
                .arrivalAirportName(flightSearchDto.destinationAirport())
                .outboundDateTime(flightSearchDto.departureTime())
                .inboundDateTime(flightSearchDto.arrivalTime())
                .build();
        ResponseEntity<List<CrazySupplierFlightResponse>> response = crazySupplierClient.searchCrazySupplierFlights(crazySupplierFlightRequest);
        if(response.getStatusCode() == HttpStatus.OK) {
            List<Flight> crazySupplierFlights = response.getBody().stream().map(csFlight -> Flight.builder()
                    .airline(csFlight.carrier())
                    .supplier("Crazy Supplier")
                    .fare(csFlight.basePrice().add(csFlight.tax()).setScale(2, RoundingMode.HALF_EVEN))
                    .departureAirport(csFlight.departureAirportName())
                    .destinationAirport(csFlight.arrivalAirportName())
                    .departureTime(covertStringToDateTime(csFlight.outboundDateTime()))
                    .arrivalTime(covertStringToDateTime(csFlight.inboundDateTime()))
                    .build()).toList();
            searchResult.addAll(crazySupplierFlights);
        } else {
            //TODO:
            throw new ResourceNotFoundException("");
        }

        return searchResult;
    }

    //TODO: Exception handling
    private ZonedDateTime covertStringToDateTime(final String isoDateTimeString) {
        Instant instant = Instant.parse(isoDateTimeString);
        return instant.atZone(ZoneId.of("UTC"));
    }
}
