package com.flight.data.management.service.client;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CrazySupplierFlightResponse(
        String carrier,

        BigDecimal basePrice,

        BigDecimal tax,

        String departureAirportName,

        String arrivalAirportName,

        String outboundDateTime,

        String inboundDateTime) {
}
