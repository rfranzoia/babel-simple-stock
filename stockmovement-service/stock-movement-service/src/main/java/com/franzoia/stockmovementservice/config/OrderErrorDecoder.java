package com.franzoia.stockmovementservice.config;

import com.franzoia.common.config.DefaultErrorDecoder;

public class OrderErrorDecoder extends DefaultErrorDecoder {

    protected OrderErrorDecoder() {
        setServiceName("Order");
    }
}
