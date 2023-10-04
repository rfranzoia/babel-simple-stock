package com.franzoia.common.util.reactive;

import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.util.AbstractMapper;
import com.franzoia.common.util.DefaultEntity;
import com.franzoia.common.util.Dto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class DefaultReactiveService<DTO extends Dto, T extends DefaultEntity, ID, M extends AbstractMapper<T, DTO>> implements ReactiveService<T, ID> {

	protected final ReactiveCrudRepository<T, ID> repository;

	final protected Mono<T> constraintViolation = Mono.error(new ConstraintsViolationException("Couldn't save entity"));
	final protected Mono<T> entityNotFound = Mono.error(new EntityNotFoundException("Entity not found"));

	@Getter
	protected final AbstractMapper<T, DTO> mapper;

	public DefaultReactiveService(final ReactiveCrudRepository<T, ID> repository, M mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public Mono<T> findOne(final ID id) {
		return repository.findById(id)
				.switchIfEmpty(entityNotFound);
	}

	@Override
	public Mono<T> save(T t) {
		return repository.save(t);
	}

	@Override
	@Transactional
	public Mono<Void> delete(final ID id) {
		return repository.findById(id)
					.switchIfEmpty(entityNotFound)
					.flatMap(repository::delete);
    }

	@Override
	public Flux<T> findAll() {
		return repository.findAll();
	}

	protected Flux<T> findAny() {
		return repository.findAll();
	}
	
	protected Mono<T> findByIdChecked(final ID id) {
		return repository.findById(id).switchIfEmpty(entityNotFound);
	}

}
