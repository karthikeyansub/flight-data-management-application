package com.flight.data.management.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "FLIGHT")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "AIRLINE", nullable = false)
    private String airline;

    @Column(name = "SUPPLIER", nullable = false)
    private String supplier;

    @Column(name = "FARE", nullable = false)
    private BigDecimal fare;

    @Column(name = "DEPARTURE_AIRPORT", nullable = false)
    private String departureAirport;

    @Column(name = "DESTINATION_AIRPORT", nullable = false)
    private String destinationAirport;

    @Column(name = "DEPARTURE_TIME", nullable = false)
    private ZonedDateTime departureTime;

    @Column(name = "ARRIVAL_TIME", nullable = false)
    private ZonedDateTime arrivalTime;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "CREATED_AT", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;

    @Column(name = "LAST_UPDATED_AT", nullable = false)
    private ZonedDateTime lastUpdatedAt;
}
