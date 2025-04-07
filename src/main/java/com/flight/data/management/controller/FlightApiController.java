package com.flight.data.management.controller;

import com.flight.data.management.exception.ValidationException;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.model.FlightResponse;
import com.flight.data.management.model.FlightSearchDto;
import com.flight.data.management.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/flights")
@Slf4j
public class FlightApiController {

    private FlightService flightService;

    @Operation(summary = "Get all flight information",
            description = " This API will return all flight information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns flight details successfully"),
            @ApiResponse(responseCode = "500", description = "System errors")
    })
    @GetMapping
    public FlightResponse getFlights() {
        log.info("Received request for GET: /api/flights");

        List<FlightDto> flightDtoList = flightService.getFlights();
        return FlightResponse.builder().flightDtoList(flightDtoList).build();
    }

    @Operation(summary = "Create new flight information",
            description = " This API will create new flight information",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                        Required origin, destination, airline, supplier, fare, departure, and arrival time.</br>
                        datetime must be in ISO_DATE_TIME format (UTC timezone).
                        """,
                    required = true,
                    content = @Content(schema = @Schema(implementation = FlightDto.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns created flight details successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "System errors")
    })
    @PostMapping
    public FlightDto createFlight(@RequestBody @Valid FlightDto flightDto) {
        log.info("Received request to create flight POST: /api/flights");

        if(!isDepartureTimeBeforeArrivalTime(flightDto.departureTime(), flightDto.arrivalTime())) {
            throw new ValidationException("Invalid departure and arrival time.");
        }

        return flightService.createFlight(flightDto);
    }

    @Operation(summary = "Update flight information",
            description = " This API will update the existing flight information",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                        Required origin, destination, airline, supplier, fare, departure, and arrival time.</br>
                        datetime must be in ISO_DATE_TIME format (UTC timezone).
                        """,
                    required = true,
                    content = @Content(schema = @Schema(implementation = FlightDto.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns updated flight details successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Flight not found"),
            @ApiResponse(responseCode = "500", description = "System errors")
    })
    @PutMapping("/{id}")
    public FlightDto updateFlight(@PathVariable Long id,
                                  @RequestBody @Valid FlightDto flightDto) {
        log.info("Received request to update flight PUT: /api/flights/{}", id);

        if(!isDepartureTimeBeforeArrivalTime(flightDto.departureTime(), flightDto.arrivalTime())) {
            throw new ValidationException("Invalid departure and arrival time.");
        }

        final ZonedDateTime departureTime = ZonedDateTime.parse(flightDto.departureTime());
        final ZonedDateTime arrivalTime = ZonedDateTime.parse(flightDto.arrivalTime());
        if(departureTime.isAfter(arrivalTime)) {
            throw new ValidationException("Invalid departure and arrival time.");
        }

        return flightService.updateFlight(id, flightDto);
    }

    @Operation(summary = "Delete flight by id",
            description = " This API will delete the flight information by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", description = "Flight not found"),
            @ApiResponse(responseCode = "500", description = "System errors")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        log.info("Received request to delete flight DELETE: /api/flights/{}", id);

        flightService.deleteFlight(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Search flight information",
            description = """
                    Search and filter flight data based on origin, destination, airline, departure, and arrival time.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                        Required origin, destination, departure, and arrival time. airline is optional.
                        """,
                    required = true,
                    content = @Content(schema = @Schema(implementation = FlightDto.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns updated flight details successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Flight not found"),
            @ApiResponse(responseCode = "500", description = "System errors")
    })
    @PostMapping("/search")
    public FlightResponse searchFlights(@RequestBody @Valid FlightSearchDto flightSearchDto) {
        log.info("Received request to search flights POST: /api/flights. Search params: {}", flightSearchDto.toString());

        if(!isDepartureTimeBeforeArrivalTime(flightSearchDto.departureTime(), flightSearchDto.arrivalTime())) {
            throw new ValidationException("Invalid departure and arrival time.");
        }

        List<FlightDto> flightDtoList = flightService.searchFlights(flightSearchDto);
        return FlightResponse.builder().flightDtoList(flightDtoList).build();
    }

    private static boolean isDepartureTimeBeforeArrivalTime(final String departureTimeString, final String arrivalTimeString) {
        final ZonedDateTime departureTime = ZonedDateTime.parse(departureTimeString);
        final ZonedDateTime arrivalTime = ZonedDateTime.parse(arrivalTimeString);

        return departureTime.isBefore(arrivalTime);
    }
}
