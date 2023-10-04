package com.franzoia.common.util.reactive;


import com.franzoia.common.util.DefaultEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveService<T extends DefaultEntity, ID> {

    Mono<T> findOne(ID id);

    Mono<T> save(T t);

    Mono<Void> delete(ID id);

    Flux<T> findAll();

}
