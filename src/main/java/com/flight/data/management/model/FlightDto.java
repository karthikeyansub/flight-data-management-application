package com.flight.data.management.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
public record FlightDto(

    String airline,

    String supplier,

    BigDecimal fare,

    String departureAirport,

    String destinationAirport,

    ZonedDateTime departureTime,

    ZonedDateTime arrivalTime) {

}
