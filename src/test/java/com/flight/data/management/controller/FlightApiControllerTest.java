package com.flight.data.management.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.flight.data.management.config.WebSecurityConfig;
import com.flight.data.management.model.ErrorResponse;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.model.FlightSearchDto;
import com.flight.data.management.service.FlightService;
import com.flight.data.management.util.TestDataUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlightApiController.class)
@Import(WebSecurityConfig.class)
@ActiveProfiles("test")
class FlightApiControllerTest {

    @MockitoBean
    private FlightService mockFlightService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Happy Flow")
    class HappyFlow {

        @Test
        void testGetFlights_ReturnResponse_HttpStatusOK_WithAllFlights() throws Exception {
            when(mockFlightService.getFlights()).thenReturn(TestDataUtil.getFlightDtoList());

            MvcResult result = mockMvc.perform(get("/api/flights"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            List<FlightDto> response = objectMapper.readValue(responseJson, List.class);
            assertNotNull(response);
            assertEquals(2, response.size());
            verify(mockFlightService, times(1)).getFlights();
        }

        @Test
        void testCreateFlight_ReturnResponse_HttpStatusOK_WithCreatedFlightDetails() throws Exception {
            when(mockFlightService.createFlight(any())).thenReturn(TestDataUtil.getFlightDtoList().get(0));

            FlightDto request = TestDataUtil.getFlightDto(null);
            MvcResult result = mockMvc.perform(post("/api/flights")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            FlightDto response = objectMapper.readValue(responseJson, FlightDto.class);
            assertNotNull(response);
            assertEquals(1, response.id());
            verify(mockFlightService, times(1)).createFlight(any());
        }

        @Test
        void testUpdateFlight_ReturnResponse_HttpStatusOK_WithUpdatedFlightDetails() throws Exception {
            when(mockFlightService.updateFlight(anyLong(), any())).thenReturn(TestDataUtil.getFlightDtoList().get(0));

            FlightDto request = TestDataUtil.getFlightDto(1L);
            MvcResult result = mockMvc.perform(put("/api/flights/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            FlightDto response = objectMapper.readValue(responseJson, FlightDto.class);
            assertNotNull(response);
            assertEquals(1, response.id());
            verify(mockFlightService, times(1)).updateFlight(anyLong(), any());
        }

        @Test
        void testDeleteFlight_ReturnResponse_HttpStatus204() throws Exception {
            doNothing().when(mockFlightService).deleteFlight(anyLong());

            MvcResult result = mockMvc.perform(delete("/api/flights/1"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNoContent())
                    .andReturn();

            verify(mockFlightService, times(1)).deleteFlight(anyLong());
        }

        @Test
        void testSearchFlight_ReturnStatus200_WithFlights() throws Exception {
            when(mockFlightService.searchFlights(any())).thenReturn(List.of(TestDataUtil.getFlightDtoList().get(0)));

            FlightSearchDto request = TestDataUtil.getFlightSearchDto();
            MvcResult result = mockMvc.perform(post("/api/flights/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            List<FlightDto> response = objectMapper.readValue(responseJson, List.class);
            assertNotNull(response);
            assertEquals(1, response.size());
            verify(mockFlightService, times(1)).searchFlights(any());
        }
    }

    @Nested
    @DisplayName("Error Flow")
    class ErrorFlow {

        @Test
        void testCreateFlight_ReturnStatus400_WithErrorResponse() throws Exception {
            FlightDto request = FlightDto.builder()
                    .departureAirport("Amsterdam")
                    .fare(BigDecimal.valueOf(-200.50))
                    .departureTime(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();
            MvcResult result = mockMvc.perform(post("/api/flights")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            ErrorResponse response = objectMapper.readValue(responseJson, ErrorResponse.class);
            assertNotNull(response);
            assertTrue(response.errorDetails().contains("Airline name cannot be empty."));
            assertTrue(response.errorDetails().contains("Fare must be a positive number."));
            assertTrue(response.errorDetails().contains("Departure airport code must be 3 characters."));
            assertTrue(response.errorDetails().contains("Departure time must be ISO_DATE_TIME format (UTC timezone)."));
            assertTrue(response.errorDetails().contains("Arrival time cannot be null."));
            verify(mockFlightService, times(0)).createFlight(any());
        }

        @Test
        void testCreateFlight_ReturnStatus400_WithErrorResponse_DueToDepartureTimeIsAfterArrivalTime() throws Exception {
            FlightDto request = TestDataUtil.getFlightDtoWithInvalidDepartureArrivalTime(null);
            MvcResult result = mockMvc.perform(post("/api/flights")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            ErrorResponse response = objectMapper.readValue(responseJson, ErrorResponse.class);
            assertNotNull(response);
            assertEquals("Invalid departure and arrival time.", response.errorMessage());
            verify(mockFlightService, times(0)).createFlight(any());
        }

        @Test
        void testUpdateFlight_ReturnStatus400_WithErrorResponse() throws Exception {
            FlightDto request = FlightDto.builder()
                    .id(1L)
                    .airline("KLM")
                    .destinationAirport("Amsterdam")
                    .arrivalTime(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();
            MvcResult result = mockMvc.perform(put("/api/flights/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            ErrorResponse response = objectMapper.readValue(responseJson, ErrorResponse.class);
            assertNotNull(response);
            assertTrue(response.errorDetails().contains("Supplier name cannot be empty."));
            assertTrue(response.errorDetails().contains("Fare cannot be null."));
            assertTrue(response.errorDetails().contains("Departure airport code cannot be null."));
            assertTrue(response.errorDetails().contains("Destination airport code must be 3 characters."));
            assertTrue(response.errorDetails().contains("Departure time cannot be null."));
            assertTrue(response.errorDetails().contains("Arrival time must be ISO_DATE_TIME format (UTC timezone)."));
            verify(mockFlightService, times(0)).updateFlight(anyLong(), any());
        }

        @Test
        void testUpdateFlight_ReturnStatus400_WithErrorResponse_DueToDepartureTimeIsAfterArrivalTime() throws Exception {
            FlightDto request = TestDataUtil.getFlightDtoWithInvalidDepartureArrivalTime(1L);
            MvcResult result = mockMvc.perform(put("/api/flights/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            ErrorResponse response = objectMapper.readValue(responseJson, ErrorResponse.class);
            assertNotNull(response);
            assertEquals("Invalid departure and arrival time.", response.errorMessage());
            verify(mockFlightService, times(0)).updateFlight(anyLong(), any());
        }

        @Test
        void testSearchFlight_ReturnStatus400_WithErrorResponse() throws Exception {
            FlightSearchDto request = FlightSearchDto.builder()
                    .destinationAirport("Amsterdam")
                    .departureTime(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).build();
            MvcResult result = mockMvc.perform(post("/api/flights/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            ErrorResponse response = objectMapper.readValue(responseJson, ErrorResponse.class);
            assertNotNull(response);
            assertTrue(response.errorDetails().contains("Departure airport code cannot be null."));
            assertTrue(response.errorDetails().contains("Destination airport code must be 3 characters."));
            assertTrue(response.errorDetails().contains("Departure time must be ISO_DATE_TIME format (UTC timezone)."));
            assertTrue(response.errorDetails().contains("Arrival time cannot be null."));
            verify(mockFlightService, times(0)).searchFlights(any());
        }
    }

    private String covertToJsonString(final Object request) throws JsonProcessingException {
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(request);
    }
}