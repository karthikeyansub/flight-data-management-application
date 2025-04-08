package com.flight.data.management.util;

import com.flight.data.management.model.FlightDto;
import com.flight.data.management.model.FlightSearchDto;
import com.flight.data.management.model.entity.Flight;
import com.flight.data.management.service.client.CrazySupplierFlightResponse;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TestDataUtil {

    public static final String UTC_DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,9}Z\\[UTC]$";

    public static List<Flight> getFlights() {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        List<Flight> flights = new ArrayList<>();
        flights.add(Flight.builder()
                .id(1L)
                .airline("KLM")
                .supplier("supplier1")
                .departureAirport("AMS")
                .destinationAirport("MAA")
                .fare(new BigDecimal(1200))
                .departureTime(utcNow.plusHours(1))
                .arrivalTime(utcNow.plusHours(10))
                .build());
        flights.add(Flight.builder()
                .id(2L)
                .airline("Air France")
                .supplier("supplier2")
                .departureAirport("CDG")
                .destinationAirport("MAA")
                .fare(new BigDecimal(1300))
                .departureTime(utcNow.plusHours(2))
                .arrivalTime(utcNow.plusHours(12))
                .build());
        return flights;
    }

    public static List<FlightDto> getFlightDtoList() {
        return getFlights().stream().map(TestDataUtil::getFlightDto).toList();
    }

    public static FlightDto getFlightDto(final Long id) {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        return FlightDto.builder()
                .id(id)
                .airline("EasyJet")
                .supplier("supplier3")
                .departureAirport("AMS")
                .destinationAirport("FCO")
                .fare(new BigDecimal(100))
                .departureTime(utcNow.plusHours(5).format(DateTimeFormatter.ISO_DATE_TIME))
                .arrivalTime(utcNow.plusHours(10).format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    public static FlightDto getFlightDtoForUpdate(final long id) {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        return FlightDto.builder()
                .id(id)
                .airline("KLM")
                .supplier("New Supplier")
                .departureAirport("AMS")
                .destinationAirport("MAA")
                .fare(new BigDecimal(1500))
                .departureTime(utcNow.plusHours(1).format(DateTimeFormatter.ISO_DATE_TIME))
                .arrivalTime(utcNow.plusHours(10).format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    public static FlightDto getFlightDtoWithInvalidDepartureArrivalTime(final Long id) {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        return FlightDto.builder()
                .id(id)
                .airline("EasyJet")
                .supplier("supplier3")
                .departureAirport("AMS")
                .destinationAirport("FCO")
                .fare(new BigDecimal(100))
                .departureTime(utcNow.plusHours(10).format(DateTimeFormatter.ISO_DATE_TIME))
                .arrivalTime(utcNow.plusHours(5).format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    public static FlightSearchDto getFlightSearchDto() {
        return FlightSearchDto.builder()
                .airline("KLM")
                .departureAirport("AMS")
                .destinationAirport("MAA")
                .departureTime("2025-04-07T16:07:09.3714066Z[UTC]")
                .arrivalTime("2025-04-08T01:07:09.3714066Z[UTC]")
                .build();
    }

    public static List<CrazySupplierFlightResponse> getCrazySupplierSearchResponse() {
        ZonedDateTime cetNow = ZonedDateTime.now(ZoneId.of("CET"));
        return List.of(CrazySupplierFlightResponse.builder()
                .carrier("Transavia")
                .departureAirportName("AMS")
                .arrivalAirportName("BCN")
                .basePrice(new BigDecimal("150.00"))
                .tax(new BigDecimal("50.50"))
                .outboundDateTime(cetNow.plusHours(1).format(DateTimeFormatter.ISO_DATE_TIME))
                .inboundDateTime(cetNow.plusHours(10).format(DateTimeFormatter.ISO_DATE_TIME))
                .build());
    }

    private static FlightDto getFlightDto(Flight flight) {
        return FlightDto.builder()
                .id(flight.getId())
                .airline(flight.getAirline())
                .supplier(flight.getSupplier())
                .fare(flight.getFare())
                .departureAirport(flight.getDepartureAirport())
                .destinationAirport(flight.getDestinationAirport())
                .departureTime(flight.getDepartureTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .arrivalTime(flight.getArrivalTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}
