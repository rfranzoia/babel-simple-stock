package com.franzoia.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceNotAvailableException extends Exception {
    public ServiceNotAvailableException(String message) {
        super(message);
    }
}
