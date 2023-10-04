package com.franzoia.stockmovementservice.config;

import com.franzoia.common.dto.ItemDTO;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ITEM-SERVICE", path = "/item-service/api/v1/items",
        configuration = { ItemErrorDecoder.class })
public interface ItemFeignClient {

    @GetMapping
    List<ItemDTO> listItems() throws ServiceNotAvailableException;

    @GetMapping("/{itemId}")
    ItemDTO getByItemId(@PathVariable("itemId") final Long itemId) throws EntityNotFoundException, ServiceNotAvailableException;

    @GetMapping("/name/{name}")
    List<ItemDTO> listItemsByName(@PathVariable("name") final String name) throws ServiceNotAvailableException;

}

