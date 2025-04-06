package com.flight.data.management.service;

import com.flight.data.management.exception.ResourceNotFoundException;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.model.entity.Flight;
import com.flight.data.management.repository.FlightRepository;
import com.flight.data.management.service.client.CrazySupplierClient;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class FlightServiceTest {

    private FlightService classUnderTest;

    @Mock
    private FlightRepository mockFlightRepository;

    @Mock
    private CrazySupplierClient mockCrazySupplierClient;

    @Mock
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        classUnderTest = new FlightService(entityManager, mockFlightRepository, mockCrazySupplierClient);
    }

    @Test
    void testGetFlights_ReturnAllFlights() {
        when(mockFlightRepository.findAll()).thenReturn(getFlights());

        List<FlightDto> result = classUnderTest.getFlights();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(mockFlightRepository, times(1)).findAll();
    }

    @Test
    void testCreateFlight_SaveFlightSuccessfully() {
        when(mockFlightRepository.save(any())).thenReturn(new Flight());

        FlightDto flightDataToSave = getFlightDto(null);
        classUnderTest.createFlight(flightDataToSave);
        verify(mockFlightRepository, times(1)).save(any());
    }

    @Test
    void testUpdateFlight_UpdateFlightSuccessfully() {
        when(mockFlightRepository.findById(anyLong())).thenReturn(Optional.of(getFlights().get(1)));
        when(mockFlightRepository.save(any())).thenReturn(new Flight());

        FlightDto flightDataToUpdate = getFlightDto(1L);
        classUnderTest.updateFlight(1L, flightDataToUpdate);

        verify(mockFlightRepository, times(1)).findById(any());
        verify(mockFlightRepository, times(1)).save(any());
    }

    @Test
    void testUpdateFlight_ThrowResourceNotFoundException_WhenFlightNotExists() {
        when(mockFlightRepository.findById(any())).thenReturn(Optional.empty());

        FlightDto flightDataToUpdate = getFlightDto(1L);
        assertThrows(ResourceNotFoundException.class, () -> classUnderTest.updateFlight(1L, flightDataToUpdate));

        verify(mockFlightRepository, times(1)).findById(any());
        verify(mockFlightRepository, times(0)).save(any());
    }

    @Test
    void testDeleteFlight_DeleteFlightSuccessfully() {
        when(mockFlightRepository.findById(anyLong())).thenReturn(Optional.of(getFlights().get(1)));
        doNothing().when(mockFlightRepository).delete(any());

        classUnderTest.deleteFlight(1L);

        verify(mockFlightRepository, times(1)).findById(any());
        verify(mockFlightRepository, times(1)).delete(any());
    }

    @Test
    void testDeleteFlight_ThrowResourceNotFoundException_WhenFlightNotExists() {
        when(mockFlightRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> classUnderTest.deleteFlight(1L));

        verify(mockFlightRepository, times(1)).findById(any());
        verify(mockFlightRepository, times(0)).delete(any());
    }

    private List<Flight> getFlights() {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        List<Flight> flights = new ArrayList<>();
        flights.add(Flight.builder()
                .id(1L)
                .airline("KLM")
                .supplier("supplier1")
                .departureAirport("AMS")
                .destinationAirport("MAA")
                .fare(new BigDecimal(1200))
                .departureTime(utcNow.plusHours(1))
                .arrivalTime(utcNow.plusHours(10))
                .build());
        flights.add(Flight.builder()
                .id(2L)
                .airline("Air France")
                .supplier("supplier2")
                .departureAirport("CDG")
                .destinationAirport("MAA")
                .fare(new BigDecimal(1300))
                .departureTime(utcNow.plusHours(2))
                .arrivalTime(utcNow.plusHours(12))
                .build());
        return flights;
    }

    private FlightDto getFlightDto(final Long id) {
        ZonedDateTime utcNow = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));
        return FlightDto.builder()
                .id(id)
                .airline("EasyJet")
                .supplier("supplier3")
                .departureAirport("AMS")
                .destinationAirport("FCO")
                .fare(new BigDecimal(100))
                .departureTime(utcNow.plusHours(5).format(DateTimeFormatter.ISO_DATE_TIME))
                .arrivalTime(utcNow.plusHours(10).format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}