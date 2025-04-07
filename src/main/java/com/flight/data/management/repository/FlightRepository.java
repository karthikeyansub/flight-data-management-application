package com.flight.data.management.repository;

import com.flight.data.management.model.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f WHERE " +
            "(:airline IS NULL OR f.airline = :airline) AND " +
            "f.departureAirport = :departureAirport AND " +
            "f.destinationAirport = :destinationAirport AND " +
            "f.departureTime >= :departureTime AND " +
            "f.arrivalTime <= :arrivalTime")
    List<Flight> searchFlights(@Param("airline") String airline,
                               @Param("departureAirport") String departureAirport,
                               @Param("destinationAirport") String destinationAirport,
                               @Param("departureTime") ZonedDateTime departureTime,
                               @Param("arrivalTime") ZonedDateTime arrivalTime);

}
