package com.franzoia.common.dto;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Embeddable
public class StockKey implements Serializable {

    @NotNull
    private String yearMonth;

    @NotNull
    private Long itemId;

}
