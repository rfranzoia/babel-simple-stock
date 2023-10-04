package com.franzoia.stockmovementservice.config;

import com.franzoia.common.dto.OrderDTO;
import com.franzoia.common.dto.OrderStatus;
import com.franzoia.common.dto.StockDTO;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.InvalidRequestException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ORDER-SERVICE", path = "/order-service/api/v1/orders",
        configuration = { OrderErrorDecoder.class })
public interface OrderFeignClient {

    @GetMapping("/item/{itemId}/status/{status}")
    List<OrderDTO> listByItemAndStatus(@PathVariable final Long itemId, @PathVariable final OrderStatus status)
            throws EntityNotFoundException, ServiceNotAvailableException;

    @PutMapping("/{orderId}")
    public OrderDTO update(@PathVariable final long orderId, @RequestBody final OrderDTO orderDTO)
            throws EntityNotFoundException, ServiceNotAvailableException;
}

