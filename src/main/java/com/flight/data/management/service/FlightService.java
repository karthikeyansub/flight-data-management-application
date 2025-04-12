package com.flight.data.management.service;

import com.flight.data.management.exception.CrazySupplierException;
import com.flight.data.management.exception.ResourceNotFoundException;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.model.FlightSearchDto;
import com.flight.data.management.model.entity.Flight;
import com.flight.data.management.repository.FlightRepository;
import com.flight.data.management.service.client.CrazySupplierClient;
import com.flight.data.management.service.client.CrazySupplierFlightRequest;
import com.flight.data.management.service.client.CrazySupplierFlightResponse;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class FlightService {

    private FlightRepository flightRepository;

    private CrazySupplierClient crazySupplierClient;

    public List<FlightDto> getFlights() {
        return flightRepository.findAll().stream().map(FlightService::getFlightDto
            ).toList();
    }

    public FlightDto createFlight(final FlightDto flightDto) {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);
        Flight flight = Flight.builder()
                .airline(flightDto.airline())
                .supplier(flightDto.supplier())
                .fare(flightDto.fare().setScale(2, RoundingMode.HALF_EVEN))
                .departureAirport(flightDto.departureAirport().toUpperCase())
                .destinationAirport(flightDto.destinationAirport().toUpperCase())
                .departureTime(covertStringToDateTime(flightDto.departureTime()))
                .arrivalTime(covertStringToDateTime(flightDto.arrivalTime()))
                .createdBy("USER")
                .createdAt(utcNow)
                .updatedBy("USER")
                .lastUpdatedAt(utcNow)
                .build();
        Flight savedFlight = flightRepository.save(flight);
        return getFlightDto(savedFlight);
    }

    public FlightDto updateFlight(final Long id, final FlightDto flightDto) {
        final Flight flight = flightRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        flight.setAirline(flightDto.airline());
        flight.setSupplier(flightDto.supplier());
        flight.setFare(flightDto.fare().setScale(2, RoundingMode.HALF_EVEN));
        flight.setDepartureAirport(flightDto.departureAirport().toUpperCase());
        flight.setDestinationAirport(flightDto.destinationAirport().toUpperCase());
        flight.setDepartureTime(covertStringToDateTime(flightDto.departureTime()));
        flight.setArrivalTime(covertStringToDateTime(flightDto.arrivalTime()));
        flight.setUpdatedBy("USER");
        flight.setLastUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));

        Flight updatedFlight = flightRepository.save(flight);

        return getFlightDto(updatedFlight);
    }

    public void deleteFlight(final Long id) {
        final Flight flight = flightRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        flightRepository.delete(flight);
    }

    public List<FlightDto> searchFlights(final FlightSearchDto flightSearchDto) {

        //Result from database
        log.debug("Search flights from database");
        List<Flight> searchResult = flightRepository.searchFlights(flightSearchDto.airline(), flightSearchDto.departureAirport(),
                flightSearchDto.destinationAirport(), covertStringToDateTime(flightSearchDto.departureTime()), covertStringToDateTime(flightSearchDto.arrivalTime()));
        List<FlightDto> flights = new ArrayList<>(searchResult.stream().map(FlightService::getFlightDto).toList());

        log.debug("Begin search flights from crazy supplier service");
        //Result from CrazySupplier
        List<FlightDto> crazyFlightSearchResult = searchCrazySupplierFlights(flightSearchDto);

        //Combine flight search result from both Database and Crazy Supplier
        flights.addAll(crazyFlightSearchResult);

        return flights;
    }

    @CircuitBreaker(name = "crazy-supplier-service", fallbackMethod = "crazySupplierServiceFallbackMethod")
    @Retryable(retryFor = RetryableException.class, backoff = @Backoff(delay = 100))
    private List<FlightDto> searchCrazySupplierFlights(FlightSearchDto flightSearchDto) {
        CrazySupplierFlightRequest crazySupplierFlightRequest = CrazySupplierFlightRequest.builder()
                .departureAirportName(flightSearchDto.departureAirport())
                .arrivalAirportName(flightSearchDto.destinationAirport())
                //Convert UTC to CET timezone as crazy supplier flight accepts CET timezone.
                .outboundDateTime(convertUTCToCET(flightSearchDto.departureTime()))
                .inboundDateTime(convertUTCToCET(flightSearchDto.arrivalTime()))
                .build();
        ResponseEntity<List<CrazySupplierFlightResponse>> response = crazySupplierClient.searchCrazySupplierFlights(crazySupplierFlightRequest);
        if(response.getStatusCode() == HttpStatus.OK) {
            List<CrazySupplierFlightResponse> crazySupplierFlightResponses = response.getBody();
            if(crazySupplierFlightResponses != null) {
                return response.getBody().stream().map(csFlight -> FlightDto.builder()
                        .airline(csFlight.carrier())
                        .supplier("Crazy Supplier")
                        .fare(csFlight.basePrice().add(csFlight.tax()).setScale(2, RoundingMode.HALF_EVEN))
                        .departureAirport(csFlight.departureAirportName())
                        .destinationAirport(csFlight.arrivalAirportName())
                        //Converts to CET to UTC timezone for search api response
                        .departureTime(convertCETToUTC(csFlight.outboundDateTime()).format(DateTimeFormatter.ISO_DATE_TIME))
                        .arrivalTime(convertCETToUTC(csFlight.inboundDateTime()).format(DateTimeFormatter.ISO_DATE_TIME))
                        .build()).toList();
            }
        }
        return Collections.emptyList();
    }

    private void crazySupplierServiceFallbackMethod(Throwable throwable) {
        throw new CrazySupplierException("Fallback response due to error in crazy supplier service: " + throwable.getMessage());
    }

    private static FlightDto getFlightDto(Flight flight) {
        return FlightDto.builder()
                .id(flight.getId())
                .airline(flight.getAirline())
                .supplier(flight.getSupplier())
                .fare(flight.getFare())
                .departureAirport(flight.getDepartureAirport())
                .destinationAirport(flight.getDestinationAirport())
                .departureTime(flight.getDepartureTime().withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE_TIME))
                .arrivalTime(flight.getArrivalTime().withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    public static void main(String[] args) {
        System.out.println(covertStringToDateTime("2025-04-07T16:07:09.3714066Z[UTC]"));
    }

    public static ZonedDateTime covertStringToDateTime(final String utcDateTimeString) {
        return ZonedDateTime.parse(utcDateTimeString);
    }

    private ZonedDateTime convertUTCToCET(final String utcDateTimeString) {
        return ZonedDateTime.parse(utcDateTimeString).withZoneSameInstant(ZoneId.of("CET"));
    }

    private ZonedDateTime convertCETToUTC(final String cetDateTime) {
        return ZonedDateTime.parse(cetDateTime).withZoneSameInstant(ZoneId.of("UTC"));
    }
}
