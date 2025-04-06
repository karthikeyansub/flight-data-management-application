package com.flight.data.management.service.client;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
public record CrazySupplierFlightResponse(
        String carrier,

        BigDecimal basePrice,

        BigDecimal tax,

        String departureAirportName,

        String arrivalAirportName,

        ZonedDateTime outboundDateTime,

        ZonedDateTime inboundDateTime) {
}
