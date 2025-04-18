package com.flight.data.management.exception.handler;

import com.flight.data.management.exception.ResourceNotFoundException;
import com.flight.data.management.exception.ValidationException;
import com.flight.data.management.model.ErrorResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {


    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleGenericException(final Exception exception) {
        log.error("Internal server error - {}", exception.getMessage());
        return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), "Something went wrong", null));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(final FeignException exception) {
        log.error("FeignException - http status: {}, error message: {}",
                exception.status(), exception.getMessage());
        if(exception.status() == NOT_FOUND.value()) {
            return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse(NOT_FOUND.name(), exception.getMessage(), null));
        } else {
            log.error("FeignException - stack trace: {}", getStackTrace(exception));
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ErrorResponse(INTERNAL_SERVER_ERROR.name(), "Something went wrong", null));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        log.warn("Missing required field - {}", exception.getMessage());
        BindingResult result = exception.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        return ResponseEntity.badRequest().body(processFieldErrors(fieldErrors));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(final ValidationException exception) {
        log.warn("Validation error - {}", exception.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.name(), exception.getMessage(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(final ResourceNotFoundException exception) {
        log.warn("Resource not found error - {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(HttpStatus.NOT_FOUND.name(), exception.getMessage(), null));
    }

    private ErrorResponse processFieldErrors(List<FieldError> fieldErrors) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError: fieldErrors) {
            errors.add(fieldError.getDefaultMessage());
        }
        return new ErrorResponse(HttpStatus.BAD_REQUEST.name(), "Input validation error", errors);
    }
}
