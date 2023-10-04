package com.franzoia.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.franzoia.common.util.Dto;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class StockDTO implements Dto {

    private StockKey key;
    private ItemDTO item;
    private Long quantityAvailable;

}
