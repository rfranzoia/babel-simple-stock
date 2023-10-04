package com.franzoia.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.franzoia.common.util.Dto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderDTO(Long id, LocalDateTime creationDate, @NotNull Long itemId, ItemDTO item,
                       @NotNull Long quantityOrdered, Long quantityFulfilled, @NotNull Long userId,
                       UserDTO user, OrderStatus status) implements Dto {}
