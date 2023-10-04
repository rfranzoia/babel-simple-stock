package com.franzoia.orderservice.repository;

import com.franzoia.common.dto.OrderStatus;
import com.franzoia.orderservice.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {

    List<Order> findAllByItemIdOrderByCreationDate(final Long itemId);

    List<Order> findAllByItemIdAndStatusOrderByCreationDate(final Long itemId, final OrderStatus status);

    List<Order> findAllByUserIdOrderByCreationDate(final Long itemId);

}
