package com.franzoia.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.franzoia.common.util.Dto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemDTO(Long id, @NotNull String name) implements Dto {}
