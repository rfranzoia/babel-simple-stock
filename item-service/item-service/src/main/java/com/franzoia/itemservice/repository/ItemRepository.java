package com.franzoia.itemservice.repository;

import com.franzoia.itemservice.model.Item;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

    Item findByName(final String nane);
}
