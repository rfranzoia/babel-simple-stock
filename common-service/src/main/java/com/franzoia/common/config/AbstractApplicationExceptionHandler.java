package com.franzoia.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.InvalidRequestException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import com.franzoia.common.util.ErrorResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.Optional;

@Slf4j
public abstract class AbstractApplicationExceptionHandler {

    @ExceptionHandler({ServiceNotAvailableException.class})
    public ResponseEntity<ErrorResponse> handleProductServiceNotAvailableException(ServiceNotAvailableException exception, WebRequest request) {
        return getResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), exception, request);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(EntityNotFoundException exception, WebRequest request) {
        return getResponse(HttpStatus.NOT_FOUND.value(), exception, request);
    }

    @ExceptionHandler({InvalidRequestException.class})
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(InvalidRequestException exception, WebRequest request) {
        return getResponse(HttpStatus.BAD_REQUEST.value(), exception, request);
    }

    @ExceptionHandler({ConstraintsViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintsViolationException(ConstraintsViolationException exception, WebRequest request) {
        return getResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception, request);
    }

    @ExceptionHandler({FeignException.class})
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException exception, WebRequest request) {
        return getResponse(exception.status(), exception, request);
    }

    protected ResponseEntity<ErrorResponse> getResponse(Integer statusCode, Exception exception, WebRequest request) {
        log.info("message from service: {}", getExceptionMessage(exception).toString());
        return new ResponseEntity<>(ErrorResponse.builder()
                .timestamp(new Date())
                .code(statusCode)
                .status(HttpStatus.valueOf(statusCode).toString())
                .message(getExceptionMessage(exception).get().getMessage())
                .details(request.getDescription(false))
                .build(), HttpStatus.valueOf(statusCode));
    }

    protected Optional<ErrorResponse> getExceptionMessage(Exception ex) {
        try {
            String[] messages = ex.getMessage().split("\\[");
            ObjectMapper mapper = new ObjectMapper();
            return Optional.of(mapper.readValue(messages[messages.length - 1], ErrorResponse.class));
        } catch (JsonProcessingException ignored) {
            return Optional.of(ErrorResponse.builder()
                    .message(ex.getMessage())
                    .build());
        }
    }

}
