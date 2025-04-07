package com.flight.data.management.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record FlightSearchDto(

        String airline,

        @NotNull(message = "Departure airport code cannot be null.")
        @Size(min = 3, max = 3, message = "Departure airport code must be 3 characters.")
        String departureAirport,

        @NotNull(message = "Destination airport code cannot be null.")
        @Size(min = 3, max = 3, message = "Destination airport code must be 3 characters.")
        String destinationAirport,

        @NotNull(message = "Departure time cannot be null.")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,9}Z\\[UTC]$", message = "Departure time must be ISO_DATE_TIME format (UTC timezone).")
        String departureTime,

        @NotNull(message = "Arrival time cannot be null.")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,9}Z\\[UTC]$", message = "Arrival time must be ISO_DATE_TIME format (UTC timezone).")
        String arrivalTime
) {
}
