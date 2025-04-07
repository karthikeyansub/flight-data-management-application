package com.flight.data.management.model;

import lombok.Builder;

import java.util.List;

@Builder
public record FlightResponse(List<FlightDto> flightDtoList) {
}
