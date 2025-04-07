package com.flight.data.management.service;

import com.flight.data.management.exception.ResourceNotFoundException;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.repository.FlightRepository;
import com.flight.data.management.service.client.CrazySupplierClient;
import com.flight.data.management.util.TestDataUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        when(mockFlightRepository.findAll()).thenReturn(TestDataUtil.getFlights());

        List<FlightDto> result = classUnderTest.getFlights();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
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


}