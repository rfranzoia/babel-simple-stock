package com.franzoia.stockmovementservice.config;

import com.franzoia.common.config.DefaultErrorDecoder;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.InvalidRequestException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class ItemErrorDecoder extends DefaultErrorDecoder {

    protected ItemErrorDecoder() {
        setServiceName("Item");
    }
}
