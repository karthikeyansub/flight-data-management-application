package com.flight.data.management.service.client;

import lombok.Builder;

@Builder
public record CrazySupplierFlightRequest(

        String departureAirportName,

        String arrivalAirportName,

        String outboundDateTime,

        String inboundDateTime) {
}
