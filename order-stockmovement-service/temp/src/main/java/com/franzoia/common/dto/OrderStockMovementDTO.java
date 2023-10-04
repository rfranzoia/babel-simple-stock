package com.franzoia.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.franzoia.common.util.Dto;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStockMovementDTO implements Dto {

    private OrderStockMovementKey key;
    private OrderDTO order;
    private StockMovementDTO stockMovement;
    private Long quantityUsed;

}
