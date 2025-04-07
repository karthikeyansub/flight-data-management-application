package com.flight.data.management.model;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FlightDto(

        Long id,

        @NotBlank(message = "Airline name cannot be empty.")
        String airline,

        @NotBlank(message = "Supplier name cannot be empty.")
        String supplier,

        @NotNull(message = "Fare cannot be null.")
        @DecimalMin(value = "0.01", message = "Fare must be a positive number.")
        BigDecimal fare,

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
        String arrivalTime) {

}
