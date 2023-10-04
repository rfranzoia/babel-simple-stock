package com.franzoia.orderservice.config;

import com.franzoia.common.dto.OrderStockMovementDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.util.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "ORDER-STOCK-MOVEMENT-SERVICE", path = "/order-stock-movement-service/api/v1/order-stock-movements",
        configuration = { OrderStockMovementErrorDecoder.class })
public interface OrderStockMovementFeignClient {

    @GetMapping("/order/{orderId}")
    List<OrderStockMovementDTO> listByOrder(@PathVariable("orderId") final Long orderId);

    @GetMapping("/stock-movement/{stockMovementId}")
    List<OrderStockMovementDTO> listByStockMovement(@PathVariable("stockMovementId") final Long stockMovementId);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderStockMovementDTO createOrderStockMovement(@Valid @RequestBody final OrderStockMovementDTO orderStockMovementDTO) throws ConstraintsViolationException;

    @DeleteMapping("/order/{orderId}/stock-movement/{stockMovementId}")
    ResponseEntity<ErrorResponse> deleteOrderStockMovement(@PathVariable("orderId") final long orderId,
                                                           @PathVariable("stockMovementId") final long stockMovementId) throws EntityNotFoundException;
}

