package com.flight.data.management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String errorTitle,
                            String errorMessage,
                            List<String> errorDetails)  {

}
