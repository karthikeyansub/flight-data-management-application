package com.flight.data.management.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.flight.data.management.model.ErrorResponse;
import com.flight.data.management.model.FlightDto;
import com.flight.data.management.model.FlightSearchDto;
import com.flight.data.management.util.TestDataUtil;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FlightApiControllerIT extends AbstractWireMockTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("Happy Flow")
    @TestMethodOrder(OrderAnnotation.class)
    class HappyFlow {

        @Test
        @DisplayName("GET:/api/flights - should return all flights")
        @Order(1)
        void testGetFlights_ShouldReturn_Status200_WithAllFlights() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/flights"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            List<FlightDto> response = objectMapper.readValue(responseJson, List.class);
            assertNotNull(response);
            assertEquals(2, response.size());
        }

        @Test
        @DisplayName("POST:/api/flights - should create new flight details in the database")
        @Order(2)
        void testCreateFlight_ReturnResponse_HttpStatusOK_WithCreatedFlightDetails() throws Exception {
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
            assertEquals("EasyJet", response.airline());
        }

        @Test
        @DisplayName("PUT:/api/flights/{id} - should update flight details in the database")
        @Order(3)
        void testUpdateFlight_ReturnResponse_HttpStatusOK_WithUpdatedFlightDetails() throws Exception {
            FlightDto request = TestDataUtil.getFlightDtoForUpdate(100L);
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/flights/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            FlightDto response = objectMapper.readValue(responseJson, FlightDto.class);
            assertNotNull(response);
            assertEquals(100, response.id());
            assertEquals("New Supplier", response.supplier());
            assertEquals(BigDecimal.valueOf(1500.00).setScale(2), response.fare());
        }

        @Test
        @DisplayName("DELETE:/api/flights/{id} - should delete flight details from the database")
        @Order(4)
        void testDeleteFlight_ReturnResponse_HttpStatus204() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/flights/100"))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNoContent())
                    .andReturn();
        }

        @Test
        @DisplayName("POST:/api/flights/search - should return flight details based on the search criteria")
        @Order(5)
        void testSearchFlight_ReturnResponse_HttpStatus200_WithFlights() throws Exception {
            stubFor(WireMock.post(urlEqualTo("/flights"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withBody(covertToJsonString(TestDataUtil.getCrazySupplierSearchResponse()).getBytes())));

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
            FlightDto searchedFlight = response.get(0);
            assertEquals(100, searchedFlight.id());
            assertEquals("KLM", searchedFlight.airline());
            assertEquals(BigDecimal.valueOf(1200.00).setScale(2), searchedFlight.fare());
        }
    }

    @Nested
    @DisplayName("Error Flow")
    class ErrorFlow {
        @Test
        @DisplayName("PUT:/api/flights/{id} - should return 404 - flight not found error response")
        void testUpdateFlight_ReturnResponse_HttpStatusOK_WithUpdatedFlightDetails() throws Exception {
            FlightDto request = TestDataUtil.getFlightDtoForUpdate(999L);
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/flights/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(covertToJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNotFound())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            ErrorResponse response = objectMapper.readValue(responseJson, ErrorResponse.class);
            assertNotNull(response);
            assertEquals("Flight not found", response.errorMessage());
        }
    }

    private String covertToJsonString(final Object request) throws JsonProcessingException {
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(request);
    }
}
