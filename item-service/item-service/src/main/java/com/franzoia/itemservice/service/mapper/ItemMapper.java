package com.franzoia.itemservice.service.mapper;

import com.franzoia.itemservice.model.Item;
import com.franzoia.common.dto.ItemDTO;
import com.franzoia.common.util.AbstractMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper implements AbstractMapper<Item, ItemDTO> {

    @Override
    public Item convertDtoToEntity(ItemDTO dto) {
        return new Item(dto.id(), dto.name());
    }

    @Override
    public List<Item> convertDtoToEntity(List<ItemDTO> dto) {
        return dto.stream().map(this::convertDtoToEntity).collect(Collectors.toList());
    }

    @Override
    public ItemDTO convertEntityToDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }

    @Override
    public List<ItemDTO> convertEntityToDTO(List<Item> t) {
        return t.stream().map(this::convertEntityToDTO).collect(Collectors.toList());
    }
}
