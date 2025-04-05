package com.flight.data.management.model;

import jakarta.validation.constraints.Pattern;

public record FlightSearchDto(

        String airline,

        String departureAirport,

        String destinationAirport,

        @Pattern(regexp = """
        ^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z$
        """, message = "Departure time must be ISO_DATE_TIME format (UTC timezone).")
        String departureTime,

        @Pattern(regexp = """
        ^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z$
        """, message = "Arrival time must be ISO_DATE_TIME format (UTC timezone).")
        String arrivalTime
) {
}
