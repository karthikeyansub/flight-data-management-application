package com.flight.data.management.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CrazySupplierFlightResponseDto(
        String carrier,

        BigDecimal basePrice,

        BigDecimal tax,

        String departureAirportName,

        String arrivalAirportName,

        String outboundDateTime,

        String inboundDateTime) {
}
