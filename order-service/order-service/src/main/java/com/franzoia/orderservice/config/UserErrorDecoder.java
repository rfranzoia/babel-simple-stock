package com.franzoia.orderservice.config;

import com.franzoia.common.config.DefaultErrorDecoder;

public class UserErrorDecoder extends DefaultErrorDecoder {

    protected UserErrorDecoder() {
        setServiceName("User");
    }
}
