package com.franzoia.common.config;

import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.InvalidRequestException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class DefaultErrorDecoder implements ErrorDecoder {

    final Function<String, String> getExceptionMessage = message -> message.substring(message.lastIndexOf('['));

    protected String serviceName;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        FeignException exception = FeignException.errorStatus(methodKey, response);
        log.info("Exception: {} {}", serviceName, FeignException.errorStatus(methodKey, response).getMessage());
        return switch (exception.status()) {
            case 400 -> new InvalidRequestException(getExceptionMessage.apply(exception.toString()));
            case 404 -> new EntityNotFoundException(getExceptionMessage.apply(exception.toString()));
            case 500, 503 -> new ServiceNotAvailableException(String.format("%s Service is unavailable", serviceName));
            default -> new Exception(getExceptionMessage.apply(exception.toString()));
        };
    }

}
