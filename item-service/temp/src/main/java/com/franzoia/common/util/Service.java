package com.franzoia.common.util;


import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;

import java.util.List;

public interface Service<T extends DefaultEntity, ID> {

    T findOne(ID id) throws EntityNotFoundException;

    T create(T t) throws ConstraintsViolationException;

    void delete(ID id) throws EntityNotFoundException, ServiceNotAvailableException;

    List<T> findAll();

}
