package com.flight.data.management.service;

import com.flight.data.management.exception.CrazySupplierException;
import com.flight.data.management.exception.ResourceNotFoundException;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.repository.FlightRepository;
import com.flight.data.management.service.client.CrazySupplierClient;
import com.flight.data.management.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static com.flight.data.management.util.TestDataUtil.UTC_DATE_PATTERN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class FlightServiceTest {

    private FlightService classUnderTest;

    @Mock
    private FlightRepository mockFlightRepository;

    @Mock
    private CrazySupplierClient mockCrazySupplierClient;

    @BeforeEach
    void setUp() {
        classUnderTest = new FlightService(mockFlightRepository, mockCrazySupplierClient);
    }

    @Test
    void testGetFlights_ReturnAllFlights() {
        when(mockFlightRepository.findAll()).thenReturn(TestDataUtil.getFlights());

        List<FlightDto> result = classUnderTest.getFlights();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        assertTrue(result.get(0).departureTime().matches(UTC_DATE_PATTERN));
        verify(mockFlightRepository, times(1)).findAll();
    }

    @Test
    void testCreateFlight_SaveFlightSuccessfully() {
        when(mockFlightRepository.save(any())).thenReturn(TestDataUtil.getFlights().get(0));

        FlightDto flightDataToSave = TestDataUtil.getFlightDto(null);
        FlightDto result = classUnderTest.createFlight(flightDataToSave);

        assertEquals(1L, result.id());
        verify(mockFlightRepository, times(1)).save(any());
    }

    @Test
    void testUpdateFlight_UpdateFlightSuccessfully() {
        when(mockFlightRepository.findById(anyLong())).thenReturn(Optional.of(TestDataUtil.getFlights().get(0)));
        when(mockFlightRepository.save(any())).thenReturn(TestDataUtil.getFlights().get(0));

        FlightDto flightDataToUpdate = TestDataUtil.getFlightDto(1L);
        FlightDto result = classUnderTest.updateFlight(1L, flightDataToUpdate);

        assertEquals(1L, result.id());
        assertTrue(result.departureTime().matches(UTC_DATE_PATTERN));
        verify(mockFlightRepository, times(1)).findById(any());
        verify(mockFlightRepository, times(1)).save(any());
    }

    @Test
    void testUpdateFlight_ThrowResourceNotFoundException_WhenFlightNotExists() {
        when(mockFlightRepository.findById(any())).thenReturn(Optional.empty());

        FlightDto flightDataToUpdate = TestDataUtil.getFlightDto(1L);
        assertThrows(ResourceNotFoundException.class, () -> classUnderTest.updateFlight(1L, flightDataToUpdate));

        verify(mockFlightRepository, times(1)).findById(any());
        verify(mockFlightRepository, times(0)).save(any());
    }

    @Test
    void testDeleteFlight_DeleteFlightSuccessfully() {
        when(mockFlightRepository.findById(anyLong())).thenReturn(Optional.of(TestDataUtil.getFlights().get(1)));
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

    @Test
    void testSearchFlights_ReturnFlights_BasedOnFliters() {
        when(mockFlightRepository.searchFlights(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(TestDataUtil.getFlights());

        when(mockCrazySupplierClient.searchCrazySupplierFlights(any()))
                .thenReturn(ResponseEntity.ok(TestDataUtil.getCrazySupplierSearchResponse()));

        List<FlightDto> result = classUnderTest.searchFlights(TestDataUtil.getFlightSearchDto());

        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        assertTrue(result.get(0).arrivalTime().matches(UTC_DATE_PATTERN));
        verify(mockFlightRepository, times(1)).searchFlights(anyString(), anyString(), anyString(), any(), any());
        verify(mockCrazySupplierClient, times(1)).searchCrazySupplierFlights(any());
    }

    @Test
    void testSearchFlights_Throws_CrazySupplierException() {
        when(mockFlightRepository.searchFlights(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(TestDataUtil.getFlights());

        when(mockCrazySupplierClient.searchCrazySupplierFlights(any()))
                .thenReturn(ResponseEntity.internalServerError().body(TestDataUtil.getCrazySupplierSearchResponse()));

        assertThrows(CrazySupplierException.class, () -> classUnderTest.searchFlights(TestDataUtil.getFlightSearchDto()));

        verify(mockFlightRepository, times(1)).searchFlights(anyString(), anyString(), anyString(), any(), any());
        verify(mockCrazySupplierClient, times(1)).searchCrazySupplierFlights(any());
    }

}