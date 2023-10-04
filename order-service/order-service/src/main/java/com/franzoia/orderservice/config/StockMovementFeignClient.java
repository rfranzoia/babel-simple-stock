package com.franzoia.orderservice.config;

import com.franzoia.common.dto.StockDTO;
import com.franzoia.common.dto.StockMovementDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.InvalidRequestException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "STOCK-MOVEMENT-SERVICE", path = "/stock-movement-service/api/v1/stock-movements",
        configuration = { StockMovementErrorDecoder.class })
public interface StockMovementFeignClient {


    @GetMapping("/quantityAvailable")
    public List<StockMovementDTO> listNonZeroQuantityAvailable();

    @PutMapping("/{stock_movementId}")
    public StockMovementDTO update(@PathVariable final long stock_movementId, @RequestBody final StockMovementDTO stockmovementDTO)
            throws EntityNotFoundException, ConstraintsViolationException, ServiceNotAvailableException;

}

