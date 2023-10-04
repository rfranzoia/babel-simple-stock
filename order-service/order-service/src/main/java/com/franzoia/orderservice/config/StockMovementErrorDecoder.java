package com.franzoia.orderservice.config;

import com.franzoia.common.config.DefaultErrorDecoder;

public class StockMovementErrorDecoder extends DefaultErrorDecoder {

    protected StockMovementErrorDecoder() {
        setServiceName("Stock");
    }
}
