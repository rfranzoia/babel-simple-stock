package com.franzoia.orderstockmovementservice.repository;

import com.franzoia.common.dto.OrderStockMovementKey;
import com.franzoia.orderstockmovementservice.model.OrderStockMovement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStockMovementRepository extends CrudRepository<OrderStockMovement, OrderStockMovementKey> {

    @Query(value = "SELECT osm " +
                    "FROM OrderStockMovement osm " +
                    "WHERE osm.key.orderId = :order " +
                    "ORDER BY osm.key.stockMovementId")
    List<OrderStockMovement> findAllByKeyOrderId(@Param("order") final Long order);

    @Query(value = "SELECT osm " +
                    "FROM OrderStockMovement osm " +
                    "WHERE osm.key.stockMovementId = :movement " +
                    "ORDER BY osm.key.stockMovementId")
    List<OrderStockMovement> findAllByKeyStockMovementId(@Param("movement") final Long movement);
}
