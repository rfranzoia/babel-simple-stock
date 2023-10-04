package com.franzoia.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.franzoia.common.util.Dto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StockMovementDTO(Long id, LocalDateTime creationDate,
                               @NotNull Long itemId, ItemDTO item,
                               @NotNull Long quantityInformed, Long quantityAvailable) implements Dto {}
