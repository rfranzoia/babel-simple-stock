package com.franzoia.orderservice.service.mapper;

import com.franzoia.common.dto.OrderDTO;
import com.franzoia.common.util.AbstractMapper;
import com.franzoia.orderservice.model.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper implements AbstractMapper<Order, OrderDTO> {

    @Override
    public Order convertDtoToEntity(OrderDTO dto) {
        return new Order(dto.id(), dto.creationDate(), dto.itemId(), dto.quantityOrdered(), dto.quantityFulfilled(), dto.userId(), dto.status());
    }

    @Override
    public List<Order> convertDtoToEntity(List<OrderDTO> dto) {
        return dto.stream().map(this::convertDtoToEntity).collect(Collectors.toList());
    }

    @Override
    public OrderDTO convertEntityToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .creationDate(order.getCreationDate())
                .itemId(order.getItemId())
                .quantityOrdered(order.getQuantityOrdered())
                .quantityFulfilled(order.getQuantityFulfilled())
                .userId(order.getUserId())
                .status(order.getStatus())
                .build();
    }

    @Override
    public List<OrderDTO> convertEntityToDTO(List<Order> t) {
        return t.stream().map(this::convertEntityToDTO).collect(Collectors.toList());
    }
}
