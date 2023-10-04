package com.franzoia.common.util;

import java.util.List;

public interface AbstractMapper<T extends DefaultEntity, DTO extends Dto> {

    T convertDtoToEntity(DTO dto);

    List<T> convertDtoToEntity(List<DTO> dto);

    DTO convertEntityToDTO(T t);

    List<DTO> convertEntityToDTO(List<T> t);
}
