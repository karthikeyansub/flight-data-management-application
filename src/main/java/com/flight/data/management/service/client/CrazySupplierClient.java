package com.flight.data.management.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "crazySupplierClient", url = "${crazy-supplier.url}")
public interface CrazySupplierClient {

    @PostMapping(value = "/flights", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<CrazySupplierFlightResponse>> searchCrazySupplierFlights(
            @RequestBody final CrazySupplierFlightRequest crazySupplierFlightRequest);
}
