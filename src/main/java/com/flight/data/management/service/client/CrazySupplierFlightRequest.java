package com.flight.data.management.service.client;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record CrazySupplierFlightRequest(

        String departureAirportName,

        String arrivalAirportName,

        ZonedDateTime outboundDateTime,

        ZonedDateTime inboundDateTime) {
}
