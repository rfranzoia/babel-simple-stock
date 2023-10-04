package com.franzoia.stockmovementservice.config;

import com.franzoia.common.config.DefaultErrorDecoder;

public class OrderStockMovementErrorDecoder extends DefaultErrorDecoder {

    protected OrderStockMovementErrorDecoder() {
        setServiceName("OrderStockMovement");
    }
}
