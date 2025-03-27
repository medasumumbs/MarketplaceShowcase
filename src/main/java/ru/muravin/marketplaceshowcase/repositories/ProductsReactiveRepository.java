package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.marketplaceshowcase.models.Product;

@Repository
public interface ProductsReactiveRepository extends R2dbcRepository<Product, Long> {
    Flux<Product> findAll(PageRequest pageRequest);

    Flux<Product> findByNameLike(String name, PageRequest pageRequest);

    Mono<Long> countByNameLike(String pattern);
}
