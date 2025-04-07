package com.flight.data.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FlightDataManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightDataManagementApplication.class, args);
	}

}
