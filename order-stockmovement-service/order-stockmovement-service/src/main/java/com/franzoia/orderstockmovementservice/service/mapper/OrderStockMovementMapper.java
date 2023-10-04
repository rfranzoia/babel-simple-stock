package com.franzoia.orderstockmovementservice.service.mapper;

import com.franzoia.orderstockmovementservice.model.OrderStockMovement;
import com.franzoia.common.dto.OrderStockMovementDTO;
import com.franzoia.common.util.AbstractMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderStockMovementMapper implements AbstractMapper<OrderStockMovement, OrderStockMovementDTO> {

    @Override
    public OrderStockMovement convertDtoToEntity(OrderStockMovementDTO dto) {
        return new OrderStockMovement(dto.getKey(), dto.getQuantityUsed());
    }

    @Override
    public List<OrderStockMovement> convertDtoToEntity(List<OrderStockMovementDTO> dto) {
        return dto.stream().map(this::convertDtoToEntity).collect(Collectors.toList());
    }

    @Override
    public OrderStockMovementDTO convertEntityToDTO(OrderStockMovement orderstockmovement) {
        return OrderStockMovementDTO.builder()
                .key(orderstockmovement.getKey())
                .quantityUsed(orderstockmovement.getQuantityUsed())
                .build();
    }

    @Override
    public List<OrderStockMovementDTO> convertEntityToDTO(List<OrderStockMovement> t) {
        return t.stream().map(this::convertEntityToDTO).collect(Collectors.toList());
    }
}
