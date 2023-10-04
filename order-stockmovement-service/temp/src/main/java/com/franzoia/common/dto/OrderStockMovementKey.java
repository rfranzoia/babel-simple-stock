package com.franzoia.common.dto;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Embeddable
public class OrderStockMovementKey {

    @NotNull
    private Long orderId;

    @NotNull
    private Long stockMovementId;

}
