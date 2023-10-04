package com.franzoia.stockmovementservice.repository;

import com.franzoia.stockmovementservice.model.StockMovement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends CrudRepository<StockMovement, Long> {

    List<StockMovement> findAllByItemIdOrderByCreationDate(final Long itemId);

    List<StockMovement> findAllByQuantityAvailableGreaterThan(final Long quantityAvailable);
}
