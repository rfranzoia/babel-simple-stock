package com.franzoia.stockmovementservice.service.mapper;

import com.franzoia.common.dto.StockMovementDTO;
import com.franzoia.common.util.AbstractMapper;
import com.franzoia.stockmovementservice.model.StockMovement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockMovementMapper implements AbstractMapper<StockMovement, StockMovementDTO> {

    @Override
    public StockMovement convertDtoToEntity(StockMovementDTO dto) {
        return new StockMovement(dto.id(), dto.creationDate(), dto.itemId(), dto.quantityInformed(), dto.quantityAvailable());
    }

    @Override
    public List<StockMovement> convertDtoToEntity(List<StockMovementDTO> dto) {
        return dto.stream().map(this::convertDtoToEntity).collect(Collectors.toList());
    }

    @Override
    public StockMovementDTO convertEntityToDTO(StockMovement stockmovement) {
        return StockMovementDTO.builder()
                .id(stockmovement.getId())
                .creationDate(stockmovement.getCreationDate())
                .itemId(stockmovement.getItemId())
                .quantityInformed(stockmovement.getQuantityInformed())
                .quantityAvailable(stockmovement.getQuantityAvailable())
                .build();
    }

    @Override
    public List<StockMovementDTO> convertEntityToDTO(List<StockMovement> t) {
        return t.stream().map(this::convertEntityToDTO).collect(Collectors.toList());
    }
}
