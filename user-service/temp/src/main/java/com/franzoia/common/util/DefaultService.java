package com.franzoia.common.util;


import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class DefaultService<DTO extends Dto, T extends DefaultEntity, ID, M extends AbstractMapper<T, DTO>> implements Service<T, ID> {

	protected final CrudRepository<T, ID> repository;

	@Getter
	protected final AbstractMapper<T, DTO> mapper;

	protected final String name;

	public DefaultService(final CrudRepository<T, ID> repository, M mapper) {
		this.repository = repository;
		this.mapper = mapper;
		this.name = this.getClass().getSimpleName().replaceAll("Service", "");
	}

	@Override
	public T findOne(final ID id) throws EntityNotFoundException {
		return findByIdChecked(id);
	}

	@Override
	public T create(T t) throws ConstraintsViolationException {
		try {
            return repository.save(t);
		} catch (DataIntegrityViolationException e) {
			log.error("ConstraintsViolationException while creating an {}: {}", this.name, t.toString(), e);
			throw new ConstraintsViolationException(e);
		}
		
	}

	@Override
	@Transactional
	public void delete(final ID id) throws EntityNotFoundException {
		T t = findByIdChecked(id);
		repository.delete(t);
	}

	@Override
	public List<T> findAll() {
		List<T> list = new ArrayList<>();
		repository.findAll().forEach(list::add);
		return list;
	}

	protected T findByIdChecked(final ID id) throws EntityNotFoundException {
		T t = repository.findById(id)
						.orElseThrow(() -> new EntityNotFoundException(String.format("Could not find %s with id: %s", name, id)));
		Hibernate.initialize(t);
		return t;
	}

}
